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
import java.util.HashSet;
import java.util.Set;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** validate if there are test scenarios for selected workflows
 *******************************************************************************/
public class RunWorkflowTestScenarioTransformStep extends AbstractTransformStep
{
   private static final QLogger LOG = QLogger.getLogger(RunWorkflowTestScenarioTransformStep.class);

   private ProcessSummaryLine okLine = new ProcessSummaryLine(Status.OK)
      .withMessageSuffix(".")
      .withSingularFutureMessage("will be run")
      .withPluralFutureMessage("will be run")
      .withSingularPastMessage("was run")
      .withPluralPastMessage("were runs");

   private Set<Integer> workflowIdSet = new HashSet<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      for(WorkflowTestScenario workflowTestScenario : runBackendStepInput.getRecordsAsEntities(WorkflowTestScenario.class))
      {
         workflowIdSet.add(workflowTestScenario.getWorkflowId());
         okLine.incrementCountAndAddPrimaryKey(workflowTestScenario.getId());
         runBackendStepOutput.addRecordEntity(workflowTestScenario);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> rs = new ArrayList<>();

      okLine.addSelfToListIfAnyCount(rs);

      rs.add(new ProcessSummaryLine(Status.INFO, workflowIdSet.size(), "Workflow Test Run" + (StringUtils.plural(workflowIdSet)) + " will be created"));

      return (rs);
   }

}
