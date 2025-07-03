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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.google.gson.reflect.TypeToken;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** process that fetches a workflow revision and its steps & links
 *******************************************************************************/
public class GetWorkflowRevisionProcess implements BackendStep, MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "getWorkflowRevision";

   private static final QLogger LOG = QLogger.getLogger(GetWorkflowRevisionProcess.class);



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
               .withField(new QFieldMetaData("workflowRevisionId", QFieldType.INTEGER))
            ));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      Integer workflowRevisionId     = runBackendStepInput.getValueInteger("workflowRevisionId");
      Integer workflowId             = runBackendStepInput.getValueInteger("workflowId");
      QRecord workflowRecord         = null;
      QRecord workflowRevisionRecord = null;

      //////////////////////////////////////////////
      // if given a workflow id, look it up first //
      //////////////////////////////////////////////
      if(workflowId != null)
      {
         workflowRecord = lookupWorkflow(runBackendStepOutput, workflowId);
         if(workflowRevisionId == null)
         {
            workflowRevisionId = workflowRecord.getValueInteger("currentWorkflowRevisionId");
         }
      }

      //////////////////////////////////////////////////////////////////////////////////////////////
      // if we have a revision id (either from input, or from the worfkow's current), the load it //
      //////////////////////////////////////////////////////////////////////////////////////////////
      if(workflowRevisionId != null)
      {
         workflowRevisionRecord = new GetAction().executeForRecord(new GetInput(WorkflowRevision.TABLE_NAME)
            .withIncludeAssociations(true)
            .withPrimaryKey(workflowRevisionId));
         if(workflowRevisionRecord == null)
         {
            throw new QException("Workflow Revision not found: " + workflowRevisionId);
         }

         ////////////////////////////////////////////////////////////////////////////////////////////////////
         // Frontend component was written to expect not QRecords, but simple value maps (as originally    //
         // we were serializing QRecordEntities).  But, in case an application added fields to the table   //
         // (e.g., a security field), it wouldn't be in the entity, so, we need to serialize the value     //
         // map from the QRecord AND any associations that are needed similarly - thus - call this method. //
         ////////////////////////////////////////////////////////////////////////////////////////////////////
         LinkedHashMap<String, Serializable> workflowRevisionForResult = prepareWorkflowRevisionRecordForResponse(workflowRevisionRecord);
         runBackendStepOutput.addValue("workflowRevision", workflowRevisionForResult);
      }

      //////////////////////////////////////////////////////////////////////////////////////////////////////
      // if we didn't previously load the workflow, but we do have a revision, then load the workflow now //
      //////////////////////////////////////////////////////////////////////////////////////////////////////
      if(workflowRecord == null && workflowRevisionRecord != null)
      {
         lookupWorkflow(runBackendStepOutput, workflowRevisionRecord.getValueInteger("workflowId"));
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private static LinkedHashMap<String, Serializable> prepareWorkflowRevisionRecordForResponse(QRecord record)
   {
      LinkedHashMap<String, Serializable> workflowRevisionForResult = CollectionUtils.useOrWrap(record.getValues(), new TypeToken<>() {});
      record.getAssociatedRecords().forEach((key, value) ->
      {
         ArrayList<Serializable> associationList = new ArrayList<>();
         workflowRevisionForResult.put(key, associationList);
         for(QRecord associatedRecord : value)
         {
            LinkedHashMap<String, Serializable> associatedRecordForResult = CollectionUtils.useOrWrap(associatedRecord.getValues(), new TypeToken<>() {});
            associationList.add(associatedRecordForResult);
         }
      });
      return workflowRevisionForResult;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static QRecord lookupWorkflow(RunBackendStepOutput runBackendStepOutput, Integer workflowId) throws QException
   {
      QRecord workflowRecord = GetAction.execute(Workflow.TABLE_NAME, workflowId);
      if(workflowRecord == null)
      {
         throw new QException("Workflow not found: " + workflowId);
      }

      LinkedHashMap<String, Serializable> workflowForResult = CollectionUtils.useOrWrap(workflowRecord.getValues(), new TypeToken<>() {});
      runBackendStepOutput.addValue("workflow", workflowForResult);
      return workflowRecord;
   }

}
