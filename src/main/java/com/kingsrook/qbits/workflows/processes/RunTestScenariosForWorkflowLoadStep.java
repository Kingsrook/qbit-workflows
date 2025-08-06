/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qbits.workflows.processes;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.kingsrook.qbits.workflows.execution.WorkflowTester;
import com.kingsrook.qbits.workflows.execution.WorkflowTesterInput;
import com.kingsrook.qbits.workflows.execution.WorkflowTesterOutput;
import com.kingsrook.qbits.workflows.model.WorkflowTestRun;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryRecordLink;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractLoadStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ProcessSummaryProviderInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** run the test scenarios and build workflow run records
 *******************************************************************************/
public class RunTestScenariosForWorkflowLoadStep extends AbstractLoadStep implements ProcessSummaryProviderInterface
{
   private static final QLogger LOG = QLogger.getLogger(RunTestScenariosForWorkflowLoadStep.class);

   private List<Integer> insertedTestRunIds       = new ArrayList<>();
   private List<Integer> workflowIdsThatHadErrors = new ArrayList<>();

   private List<ProcessSummaryRecordLink> testRunLinks           = new ArrayList<>();
   private boolean                        mayIncludeTestRunLinks = true;
   private List<QRecord>                  insertedTestRuns       = new ArrayList<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      ListingHash<Integer, QRecord> testScenariosPerWorkflow = RunTestScenariosForWorkflowTransformStep.getTestScenariosPerWorkflow(runBackendStepInput);
      if(CollectionUtils.nullSafeIsEmpty(testScenariosPerWorkflow))
      {
         return;
      }

      Map<Integer, String> workflowNameByIdMap = new HashMap<>();
      List<QRecord>        recordsToInsert     = new ArrayList<>();
      for(QRecord workflow : runBackendStepInput.getRecords())
      {
         Integer workflowId = workflow.getValueInteger("id");
         workflowNameByIdMap.put(workflowId, workflow.getValueString("name"));
         try
         {
            WorkflowTesterInput input = new WorkflowTesterInput();
            input.setWorkflow(workflow);
            input.setWorkflowTestScenarioList(testScenariosPerWorkflow.get(workflowId));

            WorkflowTesterOutput output = new WorkflowTesterOutput();
            new WorkflowTester().execute(input, output);

            recordsToInsert.add(output.getWorkflowTestRun().toQRecord());
         }
         catch(Exception e)
         {
            workflowIdsThatHadErrors.add(workflowId);
            LOG.warn("Error running a workflow test", e, logPair("workflowId", workflowId));
         }
      }

      List<QRecord> insertedTestRuns = new InsertAction().execute(new InsertInput(WorkflowTestRun.TABLE_NAME).withRecords(recordsToInsert)).getRecords();
      insertedTestRunIds.addAll(insertedTestRuns.stream().map(r -> r.getValueInteger("id")).filter(Objects::nonNull).toList());

      if(mayIncludeTestRunLinks)
      {
         RunWorkflowTestScenarioLoadStep.makeTestRunLinks(insertedTestRuns, workflowNameByIdMap, testRunLinks);
         if(testRunLinks.size() > 50)
         {
            mayIncludeTestRunLinks = false;
         }
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> rs = getTransformStep().doGetProcessSummary(runBackendStepOutput, isForResultScreen);

      RunWorkflowTestScenarioLoadStep.addWorkflowTestRunStatusLines(rs, insertedTestRunIds, workflowIdsThatHadErrors, testRunLinks);

      return rs;
   }
}
