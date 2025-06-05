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


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qbits.workflows.WorkflowsTestDataSource;
import com.kingsrook.qbits.workflows.implementations.recordworkflows.InputRecordFilterStep;
import com.kingsrook.qbits.workflows.implementations.recordworkflows.RecordWorkflowsDefinitionProducer;
import com.kingsrook.qbits.workflows.implementations.recordworkflows.RunRecordWorkflowProcessMetaDataProducer;
import com.kingsrook.qbits.workflows.implementations.recordworkflows.UpdateInputRecordFieldStep;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowRunLog;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


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
      Workflow workflow   = WorkflowsTestDataSource.insertWorkflowAndInitialRevision(RecordWorkflowsDefinitionProducer.WORKFLOW_TYPE, Workflow.TABLE_NAME);
      Integer  workflowId = workflow.getId();
      Integer  revisionId = workflow.getCurrentWorkflowRevisionId();

      new InsertAction().execute(new InsertInput(WorkflowStep.TABLE_NAME).withRecordEntities(List.of(
         new WorkflowStep()
            .withWorkflowRevisionId(revisionId)
            .withStepNo(1)
            .withInputValuesJson(JsonUtils.toJson(Map.of("queryFilterJson", new QQueryFilter(new QFilterCriteria("name", QCriteriaOperator.STARTS_WITH, "test")))))
            .withWorkflowStepTypeName(InputRecordFilterStep.NAME),
         new WorkflowStep()
            .withWorkflowRevisionId(revisionId)
            .withStepNo(2)
            .withInputValuesJson(JsonUtils.toJson(Map.of("fieldName", "name", "value", UUID.randomUUID().toString())))
            .withWorkflowStepTypeName(UpdateInputRecordFieldStep.NAME),
         new WorkflowStep()
            .withWorkflowRevisionId(revisionId)
            .withStepNo(3)
            .withInputValuesJson(JsonUtils.toJson(Map.of("fieldName", "name", "value", "test-" + UUID.randomUUID())))
            .withWorkflowStepTypeName(UpdateInputRecordFieldStep.NAME)
      )));

      new InsertAction().execute(new InsertInput(WorkflowLink.TABLE_NAME).withRecordEntities(List.of(
         new WorkflowLink().withWorkflowRevisionId(revisionId)
            .withFromStepNo(1).withToStepNo(2)
            .withConditionValue("true"),
         new WorkflowLink().withWorkflowRevisionId(revisionId)
            .withFromStepNo(1).withToStepNo(3)
            .withConditionValue("false")
      )));

      for(int i = 0; i < 2; i++)
      {
         WorkflowRunLog workflowRunLog = runProcessAssertingNoExceptionReturningRunLogs(Workflow.TABLE_NAME, workflowId, workflowId);
         assertThat(workflowRunLog.getSteps().stream().map(wrls -> wrls.getOutputData())).anyMatch(s -> s.equals("true"));
         assertThat(workflowRunLog.getSteps().stream().map(wrls -> wrls.getMessage()).filter(Objects::nonNull)).noneMatch(s -> s.startsWith("Name was set to 'test-"));

         workflowRunLog = runProcessAssertingNoExceptionReturningRunLogs(Workflow.TABLE_NAME, workflowId, workflowId);
         assertThat(workflowRunLog.getSteps().stream().map(wrls -> wrls.getOutputData())).anyMatch(s -> s.equals("false"));
         assertThat(workflowRunLog.getSteps().stream().map(wrls -> wrls.getMessage()).filter(Objects::nonNull)).anyMatch(s -> s.startsWith("Name was set to 'test-"));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testMakeError() throws QException
   {
      Workflow workflow   = WorkflowsTestDataSource.insertWorkflowAndInitialRevision(RecordWorkflowsDefinitionProducer.WORKFLOW_TYPE, Workflow.TABLE_NAME);
      Integer  workflowId = workflow.getId();
      Integer  revisionId = workflow.getCurrentWorkflowRevisionId();

      new InsertAction().execute(new InsertInput(WorkflowStep.TABLE_NAME).withRecordEntities(List.of(
         new WorkflowStep()
            .withWorkflowRevisionId(revisionId)
            .withStepNo(1)
            .withInputValuesJson(JsonUtils.toJson(Map.of("fieldName", "name", "value", StringUtils.nCopies(1000, "n"))))
            .withWorkflowStepTypeName(UpdateInputRecordFieldStep.NAME)
      )));

      WorkflowRunLog workflowRunLog = runProcessAssertingExceptionReturningRunLogs(Workflow.TABLE_NAME, workflowId, workflowId);
      assertTrue(workflowRunLog.getHadError());
      assertThat(workflowRunLog.getErrorMessage())
         .contains("Error updating record")
         .contains("The value for Name is too long");
      assertEquals(1, workflowRunLog.getSteps().size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithApi() throws QException
   {
      Workflow workflow   = WorkflowsTestDataSource.insertWorkflowAndInitialRevision(RecordWorkflowsDefinitionProducer.WORKFLOW_TYPE, TABLE_NAME_PERSON, API_NAME, V1);
      Integer  workflowId = workflow.getId();
      Integer  revisionId = workflow.getCurrentWorkflowRevisionId();

      BiFunction<String, Serializable, QRecord> insertWorkflowStep = (fieldName, value) ->
      {
         try
         {
            new DeleteAction().execute(new DeleteInput(WorkflowStep.TABLE_NAME).withQueryFilter(new QQueryFilter(new QFilterCriteria("workflowRevisionId", QCriteriaOperator.EQUALS, revisionId))));

            return new InsertAction().execute(new InsertInput(WorkflowStep.TABLE_NAME).withRecordEntities(List.of(new WorkflowStep()
               .withWorkflowRevisionId(revisionId)
               .withStepNo(1)
               .withInputValuesJson(JsonUtils.toJson(Map.of("fieldName", fieldName, "value", value)))
               .withWorkflowStepTypeName(UpdateInputRecordFieldStep.NAME)
            ))).getRecords().get(0);
         }
         catch(QException e)
         {
            fail("Error inserting workflow step");
            return (null);
         }
      };

      QRecord originalPerson = new InsertAction().execute(new InsertInput(TABLE_NAME_PERSON).withRecord(new QRecord().withValue("firstName", "Original Joe"))).getRecords().get(0);
      Integer personId       = originalPerson.getValueInteger("id");

      {
         //////////////////////////////////////////////
         // simple update - field that's always been //
         //////////////////////////////////////////////
         insertWorkflowStep.apply("firstName", "Updated Joe");
         runProcessAssertingNoExceptionReturningRunLogs(TABLE_NAME_PERSON, personId, workflowId);
         QRecord updatedPerson = GetAction.execute(TABLE_NAME_PERSON, personId);
         assertEquals("Updated Joe", updatedPerson.getValue("firstName"));
      }

      {
         ////////////////////////////////////////////////////////////////////////////////
         // update of field name from V1, which isn't in table anymore (current is V2) //
         ////////////////////////////////////////////////////////////////////////////////
         insertWorkflowStep.apply("shoeCount", 47);
         runProcessAssertingNoExceptionReturningRunLogs(TABLE_NAME_PERSON, personId, workflowId);
         QRecord updatedPerson = GetAction.execute(TABLE_NAME_PERSON, personId);
         assertEquals(47, updatedPerson.getValueInteger("noOfShoes"));
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static RunProcessOutput runProcess(String tableName, Integer recordId, Integer workflowId) throws QException
   {
      RunProcessInput input = new RunProcessInput();
      input.setProcessName(RunRecordWorkflowProcessMetaDataProducer.NAME);
      input.setCallback(QProcessCallbackFactory.forPrimaryKey("id", recordId));
      input.addValue("tableName", tableName);
      input.addValue("workflowId", workflowId);
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);

      return (runProcessOutput);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static WorkflowRunLog getWorkflowRunLog(Integer workflowId) throws QException
   {
      List<WorkflowRunLog> workflowRunLogs = new QueryAction().execute(new QueryInput(WorkflowRunLog.TABLE_NAME)
            .withFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria("workflowId", QCriteriaOperator.EQUALS, workflowId))
               .withOrderBy(new QFilterOrderBy("id", false))
               .withLimit(1))
            .withIncludeAssociations(true))
         .getRecordEntities(WorkflowRunLog.class);
      return workflowRunLogs.get(0);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static WorkflowRunLog runProcessAssertingNoExceptionReturningRunLogs(String tableName, Integer recordId, Integer workflowId) throws QException
   {
      RunProcessOutput runProcessOutput = runProcess(tableName, recordId, workflowId);

      List<ProcessSummaryLineInterface> processSummaryLines = (List<ProcessSummaryLineInterface>) runProcessOutput.getValue("processResults");
      assertThat(processSummaryLines.get(0))
         .hasFieldOrPropertyWithValue("status", Status.OK)
         .hasFieldOrPropertyWithValue("message", "had the workflow ran against it.");
      assertThat(processSummaryLines.get(1))
         .hasFieldOrPropertyWithValue("status", Status.OK)
         .hasFieldOrPropertyWithValue("message", "Created 1 Successful Workflow Run Log");

      return getWorkflowRunLog(workflowId);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static WorkflowRunLog runProcessAssertingExceptionReturningRunLogs(String tableName, Integer recordId, Integer workflowId) throws QException
   {
      RunProcessOutput runProcessOutput = runProcess(tableName, recordId, workflowId);

      List<ProcessSummaryLineInterface> processSummaryLines = (List<ProcessSummaryLineInterface>) runProcessOutput.getValue("processResults");
      assertThat(processSummaryLines.get(0))
         .hasFieldOrPropertyWithValue("status", Status.OK)
         .hasFieldOrPropertyWithValue("message", "had the workflow ran against it.");
      assertThat(processSummaryLines.get(1))
         .hasFieldOrPropertyWithValue("status", Status.ERROR)
         .hasFieldOrPropertyWithValue("message", "Created 1 Workflow Run Log with Errors");

      return getWorkflowRunLog(workflowId);
   }

}