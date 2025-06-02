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


import java.util.List;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qbits.workflows.WorkflowsTestDataSource;
import com.kingsrook.qbits.workflows.implementations.recordworkflows.RecordWorkflowsDefinitionProducer;
import com.kingsrook.qbits.workflows.implementations.recordworkflows.RunRecordWorkflowProcessMetaDataProducer;
import com.kingsrook.qbits.workflows.model.WorkflowRunLog;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit test for RunRecordWorkflowExtractStep 
 *******************************************************************************/
class RunRecordWorkflowProcessTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      /////////////////////////////////////////////////////////////////////////////////////
      // a bit meta here, we're going to run a workflow on itself...                     //
      // the workflow should rename a workflow whose name starts with test- to be a uuid //
      // else, if name doesn't start with test, it becomes test-.                        //
      // so running in a loop, we should see it toggle back and forth                    //
      /////////////////////////////////////////////////////////////////////////////////////
      new RecordWorkflowsDefinitionProducer().produce(QContext.getQInstance());
      Integer workflowId = WorkflowsTestDataSource.insertTestRecordWorkflow();

      for(int i = 0; i < 2; i++)
      {
         WorkflowRunLog workflowRunLog = runProcessAssertingNoExceptionReturningRunLogs(workflowId);
         assertThat(workflowRunLog.getSteps().stream().map(wrls -> wrls.getOutputDataJson())).anyMatch(s -> s.equals("true"));
         assertThat(workflowRunLog.getSteps().stream().map(wrls -> wrls.getOutputDataJson())).noneMatch(s -> s.startsWith("Name was set to test-"));

         workflowRunLog = runProcessAssertingNoExceptionReturningRunLogs(workflowId);
         assertThat(workflowRunLog.getSteps().stream().map(wrls -> wrls.getOutputDataJson())).anyMatch(s -> s.equals("false"));
         assertThat(workflowRunLog.getSteps().stream().map(wrls -> wrls.getOutputDataJson())).anyMatch(s -> s.startsWith("Name was set to test-"));
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static WorkflowRunLog runProcessAssertingNoExceptionReturningRunLogs(Integer workflowId) throws QException
   {
      RunProcessInput input = new RunProcessInput();
      input.setProcessName(RunRecordWorkflowProcessMetaDataProducer.NAME);
      input.setCallback(QProcessCallbackFactory.forPrimaryKey("id", workflowId));
      input.addValue("tableName", "workflow");
      input.addValue("workflowId", workflowId);
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);

      List<ProcessSummaryLineInterface> processSummaryLines = (List<ProcessSummaryLineInterface>) runProcessOutput.getValue("processResults");
      assertThat(processSummaryLines.get(0))
         .hasFieldOrPropertyWithValue("status", Status.OK)
         .hasFieldOrPropertyWithValue("message", "had the workflow ran against them.");
      assertThat(processSummaryLines.get(1))
         .hasFieldOrPropertyWithValue("status", Status.OK)
         .hasFieldOrPropertyWithValue("message", "Created 1 Successful Workflow Log");

      List<WorkflowRunLog> workflowRunLogs = new QueryAction().execute(new QueryInput(WorkflowRunLog.TABLE_NAME)
            .withFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria("workflowId", QCriteriaOperator.EQUALS, workflowId))
               .withOrderBy(new QFilterOrderBy("id", false))
               .withLimit(1))
            .withIncludeAssociations(true))
         .getRecordEntities(WorkflowRunLog.class);
      return workflowRunLogs.get(0);
   }

}