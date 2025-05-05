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
import java.util.Objects;
import java.util.UUID;
import com.fasterxml.jackson.core.type.TypeReference;
import com.kingsrook.qbits.workflows.definition.WorkflowType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.AggregateAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.Aggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class StoreNewWorkflowRevisionProcess implements BackendStep, MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "storeNewWorkflowRevision";

   private static final QLogger LOG = QLogger.getLogger(StoreNewWorkflowRevisionProcess.class);



   /***************************************************************************
    **
    ***************************************************************************/
   @Override

   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withStep(new QBackendStepMetaData()
            .withName("execute")
            .withCode(new QCodeReference(getClass()))
            .withInputData(new QFunctionInputMetaData()
               .withField(new QFieldMetaData("workflowTypeName", QFieldType.STRING))
               .withField(new QFieldMetaData("workflowId", QFieldType.INTEGER))
               .withField(new QFieldMetaData("steps", QFieldType.STRING)) // JSON
               .withField(new QFieldMetaData("links", QFieldType.STRING)) // JSON
            ));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      try
      {
         String       workflowTypeName = runBackendStepInput.getValueString("workflowTypeName");
         WorkflowType workflowType     = WorkflowsRegistry.getInstance().getWorkflowType(workflowTypeName);
         if(workflowType == null)
         {
            throw new QException("Workflow type not found: " + workflowTypeName);
         }

         List<WorkflowStep> workflowSteps = JsonUtils.toObject(runBackendStepInput.getValueString("steps"), new TypeReference<>() {});
         List<WorkflowLink> workflowLinks = JsonUtils.toObject(runBackendStepInput.getValueString("links"), new TypeReference<>() {});
         Integer            workflowId    = runBackendStepInput.getValueInteger("workflowId");
         String             commitMessage = runBackendStepInput.getValueString("commitMessage");
         Integer            versionNo     = null;

         if(workflowId == null)
         {
            Workflow workflow = new Workflow().withWorkflowTypeName(workflowTypeName);
            workflow.setName("TODO WIP " + UUID.randomUUID()); // todo wip

            // todo wip extra fields (client id)
            QRecord workflowRecord = workflow.toQRecord();
            setAdditionalValuesInWorkflowRecord(runBackendStepInput, workflowRecord);

            workflowId = new InsertAction().execute(new InsertInput(Workflow.TABLE_NAME).withRecord(workflowRecord))
               .getRecords().get(0).getValueInteger("id");

            if(workflowId == null)
            {
               throw (new QUserFacingException("Error inserting new workflow"));
            }
            versionNo = 1;
         }
         else
         {
            QRecord workflowRecord = GetAction.execute(Workflow.TABLE_NAME, workflowId);
            if(workflowRecord == null)
            {
               throw (new QUserFacingException("Workflow not found by id: " + workflowId));
            }

            AggregateInput aggregateInput = new AggregateInput();
            aggregateInput.setTableName(WorkflowRevision.TABLE_NAME);
            aggregateInput.setFilter(new QQueryFilter(new QFilterCriteria("workflowId", QCriteriaOperator.EQUALS, workflowId)));
            Aggregate maxId = new Aggregate("versionNo", AggregateOperator.MAX);
            aggregateInput.withAggregate(maxId);
            AggregateOutput aggregateOutput = new AggregateAction().execute(aggregateInput);
            if(!aggregateOutput.getResults().isEmpty())
            {
               Serializable maxValue = aggregateOutput.getResults().get(0).getAggregateValue(maxId);
               versionNo = ValueUtils.getValueAsInteger(Objects.requireNonNullElse(maxValue, 0)) + 1;
            }

            if(versionNo == null)
            {
               throw new QUserFacingException("Error getting next version number for workflow.");
            }
         }

         WorkflowRevision workflowRevision = new WorkflowRevision();
         workflowRevision.setWorkflowId(workflowId);
         workflowRevision.setVersionNo(versionNo);
         workflowRevision.setCommitMessage(StringUtils.hasContent(commitMessage) ? commitMessage : "New workflow revision created by  " + QContext.getQSession().getUser().getFullName());
         workflowRevision.setStartStepNo(1); // todo - input? or delete field?

         try
         {
            workflowRevision.setAuthor(QContext.getQSession().getUser().getFullName());
         }
         catch(Exception e)
         {
            workflowRevision.setAuthor("Unknown");
         }

         QRecord workflowRevisionRecord = workflowRevision.toQRecord();
         setAdditionalValuesInWorkflowRevisionRecord(runBackendStepInput, workflowRevisionRecord);

         InsertOutput insertOutput             = new InsertAction().execute(new InsertInput(WorkflowRevision.TABLE_NAME).withRecord(workflowRevisionRecord));
         QRecord      insertedWorkflowRevision = insertOutput.getRecords().get(0);
         Integer      insertedRevisionId       = insertedWorkflowRevision.getValueInteger("id");
         if(insertedRevisionId == null)
         {
            throw (new QUserFacingException("Error inserting new workflow revision"));
         }

         workflowSteps.forEach(step -> step.setWorkflowRevisionId(insertedRevisionId));
         new InsertAction().execute(new InsertInput(WorkflowStep.TABLE_NAME).withRecordEntities(workflowSteps));

         workflowLinks.forEach(link -> link.setWorkflowRevisionId(insertedRevisionId));
         new InsertAction().execute(new InsertInput(WorkflowLink.TABLE_NAME).withRecordEntities(workflowLinks));

         new UpdateAction().execute(new UpdateInput(Workflow.TABLE_NAME).withRecord(new Workflow()
            .withId(workflowId)
            .withCurrentWorkflowRevisionId(insertedRevisionId)
            .toQRecordOnlyChangedFields(true)));

         postAction(workflowId, insertedRevisionId);

         runBackendStepOutput.addValue("workflowId", workflowId);
         runBackendStepOutput.addValue("workflowRevisionId", insertedRevisionId);
         runBackendStepOutput.addValue("versionNo", versionNo);
      }
      catch(QUserFacingException ufe)
      {
         LOG.info("User-facing error storing workflow revision", ufe);
         throw ufe;
      }
      catch(Exception e)
      {
         String message = "Error storing workflow revision";
         LOG.warn(message, e);
         throw new QException(message, e);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void postAction(Integer workflowId, Integer workflowRevisionId) throws QException
   {

   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void setAdditionalValuesInWorkflowRecord(RunBackendStepInput runBackendStepInput, QRecord workflowRecord)
   {

   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void setAdditionalValuesInWorkflowRevisionRecord(RunBackendStepInput runBackendStepInput, QRecord workflowRevisionRecord)
   {

   }

}
