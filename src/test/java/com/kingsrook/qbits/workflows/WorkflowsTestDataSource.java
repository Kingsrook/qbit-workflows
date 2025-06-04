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

package com.kingsrook.qbits.workflows;


import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.kingsrook.qbits.workflows.implementations.recordworkflows.RecordWorkflowsDefinitionProducer;
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


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowsTestDataSource
{

   /***************************************************************************
    **
    ***************************************************************************/
   public static Integer insertTestWorkflow() throws QException
   {
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

      return (workflowId);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public static Workflow insertWorkflowAndInitialRevision(String workflowTypeName, String tableName) throws QException
   {
      return insertWorkflowAndInitialRevision(workflowTypeName, tableName, null, null);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public static Workflow insertWorkflowAndInitialRevision(String workflowTypeName, String tableName, String apiName, String apiVersion) throws QException
   {
      QRecord workflow = new InsertAction().execute(new InsertInput(Workflow.TABLE_NAME).withRecordEntity(new Workflow()
         .withWorkflowTypeName(RecordWorkflowsDefinitionProducer.WORKFLOW_TYPE)
         .withTableName(tableName)
         .withName("test-" + UUID.randomUUID())
      )).getRecords().get(0);
      Integer workflowId = workflow.getValueInteger("id");

      QRecord workflowRevision = new InsertAction().execute(new InsertInput(WorkflowRevision.TABLE_NAME).withRecordEntity(new WorkflowRevision()
         .withWorkflowId(workflowId)
         .withStartStepNo(1)
         .withApiName(apiName)
         .withApiVersion(apiVersion)
      )).getRecords().get(0);
      Integer revisionId = workflowRevision.getValueInteger("id");

      new UpdateAction().execute(new UpdateInput(Workflow.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", workflowId)
         .withValue("currentWorkflowRevisionId", revisionId)
      ));

      return new Workflow(workflow).withCurrentWorkflowRevisionId(revisionId);
   }

}
