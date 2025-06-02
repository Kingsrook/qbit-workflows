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
import com.fasterxml.jackson.core.type.TypeReference;
import com.kingsrook.qbits.workflows.definition.WorkflowStepType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
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
import com.kingsrook.qqq.backend.core.utils.ObjectUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.json.JSONObject;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** process to save a new revision of a workflow, with its steps and links.
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
               .withField(new QFieldMetaData("workflowId", QFieldType.INTEGER))
               .withField(new QFieldMetaData("apiName", QFieldType.STRING).withPossibleValueSourceName("apiName"))
               .withField(new QFieldMetaData("apiVersion", QFieldType.STRING).withPossibleValueSourceName("apiVersion"))
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
      QBackendTransaction transaction = null;

      try
      {
         InsertInput workflowRevisionInsertInput = new InsertInput(WorkflowRevision.TABLE_NAME);
         transaction = QBackendTransaction.openFor(workflowRevisionInsertInput);

         ///////////////////////////////////////////////////////////////////////////////////
         // load the workflow that the revision is for (mostly just validating it exists) //
         ///////////////////////////////////////////////////////////////////////////////////
         Integer workflowId = runBackendStepInput.getValueInteger("workflowId");
         if(workflowId == null)
         {
            throw (new QUserFacingException("Workflow id is required for storing new workflow revision."));
         }

         QRecord workflowRecord = GetAction.execute(Workflow.TABLE_NAME, workflowId);
         if(workflowRecord == null)
         {
            throw (new QUserFacingException("Workflow not found by id: " + workflowId));
         }

         ////////////////////////////
         // look up next versionNo //
         ////////////////////////////
         AggregateInput aggregateInput = new AggregateInput();
         aggregateInput.setTableName(WorkflowRevision.TABLE_NAME);
         aggregateInput.setFilter(new QQueryFilter(new QFilterCriteria("workflowId", QCriteriaOperator.EQUALS, workflowId)));
         Aggregate maxId = new Aggregate("versionNo", AggregateOperator.MAX);
         aggregateInput.withAggregate(maxId);
         AggregateOutput aggregateOutput = new AggregateAction().execute(aggregateInput);

         Integer versionNo = null;
         if(!aggregateOutput.getResults().isEmpty())
         {
            Serializable maxValue = aggregateOutput.getResults().get(0).getAggregateValue(maxId);
            versionNo = ValueUtils.getValueAsInteger(Objects.requireNonNullElse(maxValue, 0)) + 1;
         }

         if(versionNo == null)
         {
            throw new QUserFacingException("Error getting next version number for workflow.");
         }

         //////////////////////////////////
         // insert the workflow revision //
         //////////////////////////////////
         String           commitMessage    = runBackendStepInput.getValueString("commitMessage");
         WorkflowRevision workflowRevision = new WorkflowRevision();
         workflowRevision.setWorkflowId(workflowId);
         workflowRevision.setVersionNo(versionNo);
         workflowRevision.setCommitMessage(StringUtils.hasContent(commitMessage) ? commitMessage : "New workflow revision created by  " + QContext.getQSession().getUser().getFullName());
         workflowRevision.setApiName(runBackendStepInput.getValueString("apiName"));
         workflowRevision.setApiVersion(runBackendStepInput.getValueString("apiVersion"));
         workflowRevision.setStartStepNo(1);
         workflowRevision.setAuthor(ObjectUtils.tryElse(() -> QContext.getQSession().getUser().getFullName(), "Unknown"));

         QRecord workflowRevisionRecord = workflowRevision.toQRecord();
         setAdditionalValuesInWorkflowRevisionRecord(transaction, runBackendStepInput, workflowRevisionRecord);

         workflowRevisionInsertInput
            .withTransaction(transaction)
            .withRecord(workflowRevisionRecord);

         InsertOutput insertOutput             = new InsertAction().execute(workflowRevisionInsertInput);
         QRecord      insertedWorkflowRevision = insertOutput.getRecords().get(0);
         Integer      insertedRevisionId       = insertedWorkflowRevision.getValueInteger("id");
         if(insertedRevisionId == null)
         {
            throw (new QUserFacingException("Error inserting new workflow revision"));
         }

         /////////////////////////////////////////////////////////////////////////
         // store steps (first setting revision id and a fresh summary on each) //
         /////////////////////////////////////////////////////////////////////////
         List<WorkflowStep> workflowSteps = JsonUtils.toObject(runBackendStepInput.getValueString("steps"), new TypeReference<>() {});
         workflowSteps.forEach(step ->
         {
            step.setWorkflowRevisionId(insertedRevisionId);

            try
            {
               WorkflowStepType workflowStepType = WorkflowsRegistry.getInstance().getWorkflowStepType(step.getWorkflowStepTypeName());
               JSONObject       values           = new JSONObject(step.getInputValuesJson());
               step.setSummary(workflowStepType.getDynamicStepSummary(workflowId, values));
            }
            catch(Exception e)
            {
               LOG.warn("Error setting step summary for step", e, logPair("stepTypeName", step.getWorkflowStepTypeName()), logPair("workflowRevisionId", insertedRevisionId));
            }
         });
         new InsertAction().execute(new InsertInput(WorkflowStep.TABLE_NAME).withRecordEntities(workflowSteps).withTransaction(transaction));

         /////////////////
         // store links //
         /////////////////
         List<WorkflowLink> workflowLinks = JsonUtils.toObject(runBackendStepInput.getValueString("links"), new TypeReference<>() {});
         workflowLinks.forEach(link -> link.setWorkflowRevisionId(insertedRevisionId));
         new InsertAction().execute(new InsertInput(WorkflowLink.TABLE_NAME).withRecordEntities(workflowLinks).withTransaction(transaction));

         //////////////////////////////////////////////////
         // update workflow with the current revision id //
         //////////////////////////////////////////////////
         new UpdateAction().execute(new UpdateInput(Workflow.TABLE_NAME).withTransaction(transaction).withRecord(new Workflow()
            .withId(workflowId)
            .withCurrentWorkflowRevisionId(insertedRevisionId)
            .toQRecordOnlyChangedFields(true)));

         postAction(transaction, workflowId, insertedRevisionId);

         transaction.commit();

         runBackendStepOutput.addValue("workflowId", workflowId);
         runBackendStepOutput.addValue("workflowRevisionId", insertedRevisionId);
         runBackendStepOutput.addValue("versionNo", versionNo);
      }
      catch(QUserFacingException ufe)
      {
         if(transaction != null)
         {
            transaction.rollback();
         }
         LOG.info("User-facing error storing workflow revision", ufe);
         throw ufe;
      }
      catch(Exception e)
      {
         if(transaction != null)
         {
            transaction.rollback();
         }
         String message = "Error storing workflow revision";
         LOG.warn(message, e);
         throw new QException(message, e);
      }
      finally
      {
         if(transaction != null)
         {
            transaction.close();
         }
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void postAction(QBackendTransaction transaction, Integer workflowId, Integer workflowRevisionId) throws QException
   {

   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void setAdditionalValuesInWorkflowRecord(QBackendTransaction transaction, RunBackendStepInput runBackendStepInput, QRecord workflowRecord)
   {

   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void setAdditionalValuesInWorkflowRevisionRecord(QBackendTransaction transaction, RunBackendStepInput runBackendStepInput, QRecord workflowRevisionRecord)
   {

   }

}
