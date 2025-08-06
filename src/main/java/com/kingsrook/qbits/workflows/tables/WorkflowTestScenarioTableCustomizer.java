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

package com.kingsrook.qbits.workflows.tables;


import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import com.kingsrook.qbits.workflows.WorkflowsQBitConfig;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qqq.api.actions.QRecordApiAdapter;
import com.kingsrook.qqq.backend.core.actions.customizers.OldRecordHelper;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizerInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import com.kingsrook.qqq.backend.core.processes.utils.RecordLookupHelper;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.json.JSONObject;
import org.json.JSONTokener;


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowTestScenarioTableCustomizer implements TableCustomizerInterface
{
   private RecordLookupHelper recordLookupHelper = new RecordLookupHelper();



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public List<QRecord> preInsertOrUpdate(AbstractActionInput input, List<QRecord> records, boolean isPreview, Optional<List<QRecord>> oldRecordList) throws QException
   {
      if(WorkflowsQBitConfig.isApiModuleAvailableAndDoesQBitIncludeApiVersions())
      {
         OldRecordHelper oldRecordHelper = new OldRecordHelper(WorkflowTestScenario.TABLE_NAME, oldRecordList);

         for(QRecord record : records)
         {
            Integer workflowId     = ValueUtils.getValueAsInteger(getValue(record, oldRecordHelper.getOldRecord(record), "workflowId"));
            Integer sourceRecordId = ValueUtils.getValueAsInteger(getValue(record, oldRecordHelper.getOldRecord(record), "sourceRecordId"));
            String  apiName        = ValueUtils.getValueAsString(getValue(record, oldRecordHelper.getOldRecord(record), "apiName"));
            String  apiVersion     = ValueUtils.getValueAsString(getValue(record, oldRecordHelper.getOldRecord(record), "apiVersion"));
            String  apiJson        = ValueUtils.getValueAsString(getValue(record, oldRecordHelper.getOldRecord(record), "apiJson"));

            if(sourceRecordId == null && !StringUtils.hasContent(apiName) && !StringUtils.hasContent(apiVersion) && !StringUtils.hasContent(apiJson))
            {
               record.addError(new BadInputStatusMessage("Either Source Record Id or all API fields must be given."));
            }
            else if(sourceRecordId != null)
            {
               if(StringUtils.hasContent(apiName) || StringUtils.hasContent(apiVersion) || StringUtils.hasContent(apiJson))
               {
                  record.addError(new BadInputStatusMessage("If Source Record Id is given, then no API fields may be given."));
               }
            }
            else if(!StringUtils.hasContent(apiName) || !StringUtils.hasContent(apiVersion) || !StringUtils.hasContent(apiJson))
            {
               record.addError(new BadInputStatusMessage("If not giving Source Record Id, then all API fields must be given."));
            }

            if(CollectionUtils.nullSafeIsEmpty(record.getErrors()) && StringUtils.hasContent(apiName) && StringUtils.hasContent(apiVersion) && StringUtils.hasContent(apiJson))
            {
               validateJsonBody(record, workflowId, apiJson, apiName, apiVersion);
            }
         }
      }

      return (records);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void validateJsonBody(QRecord record, Integer workflowId, String apiJson, String apiName, String apiVersion) throws QException
   {
      try
      {
         QRecord workflow = recordLookupHelper.getRecordByKey(Workflow.TABLE_NAME, "id", workflowId);
         if(workflow != null)
         {
            JSONTokener jsonTokener = new JSONTokener(apiJson);
            JSONObject  jsonObject  = new JSONObject(jsonTokener);
            QRecordApiAdapter.apiJsonObjectToQRecord(jsonObject, workflow.getValueString("tableName"), apiName, apiVersion, true);
         }
      }
      catch(Exception e)
      {
         record.addError(new BadInputStatusMessage("API JSON was not valid: " + e.getMessage()));
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private Serializable getValue(QRecord record, Optional<QRecord> oldRecord, String fieldName)
   {
      if(record.getValues().containsKey(fieldName))
      {
         return (record.getValues().get(fieldName));
      }

      if(oldRecord.isPresent() && oldRecord.get().getValues().containsKey(fieldName))
      {
         return (oldRecord.get().getValues().get(fieldName));
      }

      return null;
   }

}
