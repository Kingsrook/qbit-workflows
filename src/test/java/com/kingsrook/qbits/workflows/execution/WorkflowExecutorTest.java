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

package com.kingsrook.qbits.workflows.execution;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qbits.workflows.TestWorkflowDefinitions;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for WorkflowExecutor 
 *******************************************************************************/
class WorkflowExecutorTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      TestWorkflowDefinitions.registerTestWorkflowTypes();

      QRecord workflow = new InsertAction().execute(new InsertInput(Workflow.TABLE_NAME).withRecordEntity(new Workflow()
         .withWorkflowTypeName(TestWorkflowDefinitions.TEST_WORKFLOW_TYPE)
         .withName("test")
      )).getRecords().get(0);
      Integer workflowId = workflow.getValueInteger("id");

      QRecord workflowRevision = new InsertAction().execute(new InsertInput(WorkflowRevision.TABLE_NAME).withRecordEntity(new WorkflowRevision()
         .withWorkflowId(workflowId)
         .withStartStepNo(1)
      )).getRecords().get(0);
      Integer revisionId = workflowRevision.getValueInteger("id");

      new UpdateAction().execute(new UpdateInput(Workflow.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", workflowId)
         .withValue("currentWorkflowRevisionId", revisionId)
      ));

      new InsertAction().execute(new InsertInput(WorkflowStep.TABLE_NAME).withRecordEntities(List.of(
         new WorkflowStep()
            .withWorkflowRevisionId(revisionId)
            .withStepNo(1)
            .withInputValuesJson(JsonUtils.toJson(Map.of("x", 1)))
            .withWorkflowStepTypeName(TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION),
         new WorkflowStep()
            .withWorkflowRevisionId(revisionId)
            .withStepNo(2)
            .withInputValuesJson(JsonUtils.toJson(Map.of("x", 2)))
            .withWorkflowStepTypeName(TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION),
         new WorkflowStep()
            .withWorkflowRevisionId(revisionId)
            .withStepNo(3)
            .withInputValuesJson(JsonUtils.toJson(Map.of("filter", new QQueryFilter(new QFilterCriteria("condition", QCriteriaOperator.EQUALS, true)))))
            .withWorkflowStepTypeName(TestWorkflowDefinitions.BOOLEAN_CONDITIONAL),
         new WorkflowStep()
            .withWorkflowRevisionId(revisionId)
            .withStepNo(4)
            .withInputValuesJson(JsonUtils.toJson(Map.of("x", 3)))
            .withWorkflowStepTypeName(TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION),
         new WorkflowStep()
            .withWorkflowRevisionId(revisionId)
            .withStepNo(5)
            .withInputValuesJson(JsonUtils.toJson(Map.of("x", 4)))
            .withWorkflowStepTypeName(TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION),
         new WorkflowStep()
            .withWorkflowRevisionId(revisionId)
            .withStepNo(6)
            .withInputValuesJson(JsonUtils.toJson(Map.of("x", 5)))
            .withWorkflowStepTypeName(TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION)
      )));

      new InsertAction().execute(new InsertInput(WorkflowLink.TABLE_NAME).withRecordEntities(List.of(
         new WorkflowLink().withWorkflowRevisionId(revisionId)
            .withFromStepNo(1).withToStepNo(2)
            .withConditionValue(null),
         new WorkflowLink().withWorkflowRevisionId(revisionId)
            .withFromStepNo(2).withToStepNo(3)
            .withConditionValue(null),
         new WorkflowLink().withWorkflowRevisionId(revisionId)
            .withFromStepNo(3).withToStepNo(4)
            .withConditionValue("true"),
         new WorkflowLink().withWorkflowRevisionId(revisionId)
            .withFromStepNo(3).withToStepNo(5)
            .withConditionValue("false"),
         new WorkflowLink().withWorkflowRevisionId(revisionId)
            .withFromStepNo(4).withToStepNo(6)
            .withConditionValue(null),
         new WorkflowLink().withWorkflowRevisionId(revisionId)
            .withFromStepNo(5).withToStepNo(6)
            .withConditionValue(null)
      )));

      WorkflowOutput output;

      /////////////////////////////////
      // initial run of the workflow //
      /////////////////////////////////
      output = executeWorkflow(workflowId, Map.of("condition", true, "seedValue", 0));
      assertNull(output.getException());
      assertEquals(11, output.getValues().get("sum"));

      ///////////////////////////////////////////////////////////////////
      // demonstrate taking the other conditional branch, versus above //
      ///////////////////////////////////////////////////////////////////
      output = executeWorkflow(workflowId, Map.of("condition", false, "seedValue", 0));
      assertNull(output.getException());
      assertEquals(12, output.getValues().get("sum"));

      ///////////////////////////////////////////////////////////////////////////////////////////
      // Show that the seed value was used in the pre-run - different seed gives different sum //
      ///////////////////////////////////////////////////////////////////////////////////////////
      output = executeWorkflow(workflowId, Map.of("condition", true, "seedValue", 10));
      assertNull(output.getException());
      assertEquals(21, output.getValues().get("sum"));

      //////////////////////////////
      // Show that post run works //
      //////////////////////////////
      output = executeWorkflow(workflowId, Map.of("condition", true, "overrideSumInPostRun", 1701));
      assertNull(output.getException());
      assertEquals(1701, output.getValues().get("sum"));

      //////////////////////////////
      // Show that pre step works //
      //////////////////////////////
      output = executeWorkflow(workflowId, Map.of("condition", true, "doubleSumInEveryPreStep", true));
      assertNull(output.getException());
      assertEquals(43, output.getValues().get("sum"));

      ///////////////////////////////
      // Show that post step works //
      ///////////////////////////////
      output = executeWorkflow(workflowId, Map.of("condition", true, "overrideReturnValueInPostStep", false));
      assertNull(output.getException());
      assertEquals(12, output.getValues().get("sum"));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private WorkflowOutput executeWorkflow(Integer workflowId, Map<String, Serializable> values) throws QException
   {
      WorkflowInput input = new WorkflowInput();
      input.setWorkflowId(workflowId);
      input.setValues(values);

      WorkflowOutput output = new WorkflowOutput();
      new WorkflowExecutor().execute(input, output);

      return (output);
   }

}