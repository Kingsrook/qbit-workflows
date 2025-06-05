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

package com.kingsrook.qbits.workflows.implementations.recordworkflows;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import com.kingsrook.qbits.workflows.definition.OutboundLinkMode;
import com.kingsrook.qbits.workflows.definition.WorkflowStepType;
import com.kingsrook.qbits.workflows.execution.WorkflowExecutionContext;
import com.kingsrook.qbits.workflows.execution.WorkflowStepExecutorInterface;
import com.kingsrook.qbits.workflows.execution.WorkflowStepOutput;
import com.kingsrook.qbits.workflows.implementations.WorkflowStepUtils;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.api.actions.GetTableApiFieldsAction;
import com.kingsrook.qqq.api.actions.QRecordApiAdapter;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsInput;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsOutput;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.actions.values.SearchPossibleValueSourceAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAndJoinTable;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.FormAdjusterInput;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.FormAdjusterInterface;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.FormAdjusterOutput;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.RunFormAdjusterProcess;
import com.kingsrook.qqq.frontend.materialdashboard.model.metadata.MaterialDashboardFieldMetaData;
import org.json.JSONObject;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** workflow step that updates a record with one value in one field
 *******************************************************************************/
public class UpdateInputRecordFieldStep extends WorkflowStepType implements WorkflowStepExecutorInterface
{
   public static final String NAME = "updateInputRecordField";

   private static final QLogger LOG = QLogger.getLogger(UpdateInputRecordFieldStep.class);



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public UpdateInputRecordFieldStep()
   {
      this.withName(NAME)
         .withOutboundLinkMode(OutboundLinkMode.ONE)
         .withLabel("Update Record Field")
         .withIconUrl("data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0Ij4KICAgPHBhdGggZD0iTTIyIDI0SDJ2LTRoMjB2NHpNMTMuMDYgNS4xOWwzLjc1IDMuNzVMNy43NSAxOEg0di0zLjc1bDkuMDYtOS4wNnptNC44MiAyLjY4LTMuNzUtMy43NSAxLjgzLTEuODNjLjM5LS4zOSAxLjAyLS4zOSAxLjQxIDBsMi4zNCAyLjM0Yy4zOS4zOS4zOSAxLjAyIDAgMS40MWwtMS44MyAxLjgzeiIvPgo8L3N2Zz4K")
         .withExecutor(new QCodeReference(getClass()))
         .withDescription("Update a value in a field on the record being processed")
         .withInputFields(List.of(
            new QFieldMetaData("fieldName", QFieldType.STRING)
               .withLabel("Field")
               .withPossibleValueSourceName(RecordWorkflowFieldNamePossibleValueSource.NAME)
               .withGridColumns(12)
               .withIsRequired(true)
               .withSupplementalMetaData(new MaterialDashboardFieldMetaData()
                  .withFormAdjusterIdentifier("workflowStepType:updateInputRecordFieldStep:fieldName")
                  .withFieldsToDisableWhileRunningAdjusters(Set.of("value"))
                  .withOnChangeFormAdjuster(new QCodeReference(UpdateInputRecordFieldMetaDataAdjuster.class))
                  .withOnLoadFormAdjuster(new QCodeReference(UpdateInputRecordFieldMetaDataAdjuster.class))),
            new QFieldMetaData("value", QFieldType.STRING).withGridColumns(12)
         ));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class UpdateInputRecordFieldMetaDataAdjuster implements FormAdjusterInterface
   {

      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public FormAdjusterOutput execute(FormAdjusterInput input) throws QException
      {
         //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // get the new value for the 'fieldName' field - which will tell us what meta-data we need to set for the 'value' field //
         //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         String newValue = ValueUtils.getValueAsString(input.getNewValue());

         //////////////////////////////////////////////
         // start building the updated 'value' field //
         //////////////////////////////////////////////
         QFieldMetaData updatedField = new QFieldMetaData("value", QFieldType.STRING)
            .withLabel("Value")
            .withIsEditable(false);

         String tableName = getTableNameForWorkflow(input);

         ////////////////////////////////////////////////////////////////////////////////////////////////
         // if there is a new value (e.g., a selection has been made for fieldName), look up the field //
         ////////////////////////////////////////////////////////////////////////////////////////////////
         if(StringUtils.hasContent(newValue))
         {
            try
            {
               WorkflowRevision workflowRevision = buildWorkflowRevisionFromInputJSONValues(input.getAllValues().get("workflowRevisionValuesJSON"));
               if(workflowRevision != null && WorkflowStepUtils.useApi(workflowRevision))
               {
                  Optional<QFieldMetaData> foundField = getApiField(newValue, tableName, workflowRevision);
                  if(foundField.isPresent())
                  {
                     updatedField = foundField.get().clone();
                     updatedField.setName("value");
                     updatedField.setLabel("Value (" + foundField.get().getLabel() + ")");
                  }
               }
               else
               {
                  QTableMetaData    table             = QContext.getQInstance().getTable(tableName);
                  FieldAndJoinTable fieldAndJoinTable = FieldAndJoinTable.get(table, newValue);

                  if(fieldAndJoinTable != null)
                  {
                     updatedField = fieldAndJoinTable.field().clone();
                     updatedField.setName("value");
                     updatedField.setLabel("Value (" + fieldAndJoinTable.field().getLabel() + ")");
                  }
               }
            }
            catch(Exception e)
            {
               LOG.info("Error getting field from table", e, logPair("fieldName", newValue));
            }
         }

         ///////////////////////////////////////////////////////////////////////////////////
         // build the output object, starting with the new meta-data of the 'value' field //
         ///////////////////////////////////////////////////////////////////////////////////
         FormAdjusterOutput output = new FormAdjusterOutput();
         output.setUpdatedFieldMetaData(Map.of("value", new QFrontendFieldMetaData(updatedField
            /////////////////////////////////////////////////////////////////////////////////////////////////
            // set attributes on the field that are needed regardless of whether we found the field or not //
            /////////////////////////////////////////////////////////////////////////////////////////////////
            .withGridColumns(12)
         )));

         if(RunFormAdjusterProcess.EVENT_ON_CHANGE.equals(input.getEvent()))
         {
            /////////////////////////////////////////////////////////////////////
            // for an on-change event clear out the value in the 'value' field //
            /////////////////////////////////////////////////////////////////////
            output.setFieldsToClear(Set.of("value"));
         }
         else if(RunFormAdjusterProcess.EVENT_ON_LOAD.equals(input.getEvent()))
         {
            /////////////////////////////////////////////////////////////////////////
            // for an on-load event, for PVS fields, look up the value for display //
            /////////////////////////////////////////////////////////////////////////
            Object oldValueObject = Objects.requireNonNullElse(input.getAllValues(), Collections.emptyMap()).get("value");
            if(StringUtils.hasContent(updatedField.getPossibleValueSourceName()) && oldValueObject instanceof Serializable oldValue)
            {
               SearchPossibleValueSourceOutput searchPossibleValueSourceOutput = new SearchPossibleValueSourceAction().execute(new SearchPossibleValueSourceInput()
                  .withIdList(List.of(oldValue))
                  .withPossibleValueSourceName(updatedField.getPossibleValueSourceName())
               );

               if(CollectionUtils.nullSafeHasContents(searchPossibleValueSourceOutput.getResults()))
               {
                  output.setUpdatedFieldDisplayValues(Map.of("value", searchPossibleValueSourceOutput.getResults().get(0).getLabel()));
               }
            }
         }

         return output;
      }



      /***************************************************************************
       **
       ***************************************************************************/
      private WorkflowRevision buildWorkflowRevisionFromInputJSONValues(Serializable workflowRevisionValuesJSON)
      {
         try
         {
            JSONObject       jsonObject       = new JSONObject(ValueUtils.getValueAsString(workflowRevisionValuesJSON));
            WorkflowRevision workflowRevision = new WorkflowRevision();

            if(jsonObject.has("apiName"))
            {
               workflowRevision.setApiName(jsonObject.getString("apiName"));
            }

            if(jsonObject.has("apiVersion"))
            {
               workflowRevision.setApiVersion(jsonObject.getString("apiVersion"));
            }

            return (workflowRevision);
         }
         catch(Exception e)
         {
            LOG.warn("Error building workflow revision from inputValuesJSON", e);
         }
         return null;
      }



      /***************************************************************************
       **
       ***************************************************************************/
      private static String getTableNameForWorkflow(FormAdjusterInput input) throws QException
      {
         String tableName          = null;
         String workflowValuesJSON = ValueUtils.getValueAsString(CollectionUtils.nonNullMap(input.getAllValues()).get("workflowValuesJSON"));
         if(StringUtils.hasContent(workflowValuesJSON))
         {
            JSONObject workflowValuesJSONObject = new JSONObject(workflowValuesJSON);
            if(workflowValuesJSONObject.has("tableName"))
            {
               tableName = workflowValuesJSONObject.getString("tableName");
            }
         }

         if(tableName == null)
         {
            throw (new QException("Could not get table name from input"));
         }
         return tableName;
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static Optional<QFieldMetaData> getApiField(String fieldNameMaybeWithTableNamePrefix, String tableName, WorkflowRevision workflowRevision) throws QException
   {
      String fieldName = (fieldNameMaybeWithTableNamePrefix.contains(".") ? fieldNameMaybeWithTableNamePrefix.substring(fieldNameMaybeWithTableNamePrefix.indexOf(".") + 1) : fieldNameMaybeWithTableNamePrefix);
      GetTableApiFieldsOutput tableApiFieldsOutput = new GetTableApiFieldsAction().execute(new GetTableApiFieldsInput()
         .withTableName(tableName)
         .withApiName(workflowRevision.getApiName())
         .withVersion(workflowRevision.getApiVersion()));
      Optional<QFieldMetaData> foundField = tableApiFieldsOutput.getFields().stream().filter(f -> fieldName.equals(f.getName())).findFirst();
      return foundField;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public String getDynamicStepSummary(Integer workflowId, JSONObject values) throws QException
   {
      String fieldName = values.optString("fieldName", null);
      String value     = values.optString("value", null);

      String apiName = null;
      String apiVersion = null;
      if(values.has("workflowRevision"))
      {
         Object o = values.get("workflowRevision");
         if(o instanceof JSONObject jsonObject)
         {
            apiName = jsonObject.optString("apiName");
            apiVersion = jsonObject.optString("apiVersion");
         }
      }

      return getStepSummary(workflowId, fieldName, apiName, apiVersion, value, false);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private String getStepSummary(Integer workflowId, String fieldName, String apiName, String apiVersion, String value, boolean isPastTense)
   {
      String         fieldLabel = null;
      QTableMetaData table      = null;

      if(StringUtils.hasContent(fieldName))
      {
         try
         {
            if(workflowId != null)
            {
               QRecord workflowRecord = GetAction.execute(Workflow.TABLE_NAME, workflowId);
               table = QContext.getQInstance().getTable(workflowRecord.getValueString("tableName"));

               WorkflowRevision workflowRevision = new WorkflowRevision().withApiName(apiName).withApiVersion(apiVersion);
               if(WorkflowStepUtils.useApi(workflowRevision))
               {
                  Optional<QFieldMetaData> optionalApiField = getApiField(fieldName, table.getName(), workflowRevision);
                  if(optionalApiField.isPresent())
                  {
                     // fieldName = optionalApiField.field().getName(); // hmm?
                     fieldLabel = optionalApiField.get().getLabel();
                  }
               }
               else
               {
                  FieldAndJoinTable fieldAndJoinTable = FieldAndJoinTable.get(table, fieldName);
                  fieldName = fieldAndJoinTable.field().getName();
                  fieldLabel = fieldAndJoinTable.field().getLabel();
               }
            }
         }
         catch(Exception e)
         {
            fieldLabel = fieldName;
         }
      }

      String verb = isPastTense ? "was" : "will be";

      if(StringUtils.hasContent(fieldLabel))
      {
         if(StringUtils.hasContent(value))
         {
            String displayValue = value;
            try
            {
               QRecord record = new QRecord().withValue(fieldName, value);
               QValueFormatter.setDisplayValuesInRecordsIncludingPossibleValueTranslations(table, List.of(record));
               displayValue = record.getDisplayValue(fieldName);
            }
            catch(Exception e)
            {
               /////////////////////////////
               // leave as original value //
               /////////////////////////////
            }

            return (fieldLabel + " " + verb + " set to '" + displayValue + "'");
         }
         else
         {
            return (fieldLabel + " " + verb + " cleared out");
         }
      }

      return null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public WorkflowStepOutput execute(WorkflowStep step, Map<String, Serializable> inputValues, WorkflowExecutionContext context) throws QException
   {
      if(WorkflowStepUtils.useApi(context.getWorkflowRevision()))
      {
         return executeViaApi(inputValues, context);
      }
      else
      {
         return executeWithoutApi(inputValues, context);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private WorkflowStepOutput executeViaApi(Map<String, Serializable> inputValues, WorkflowExecutionContext context) throws QException
   {
      Workflow         workflow         = context.getWorkflow();
      WorkflowRevision workflowRevision = context.getWorkflowRevision();

      String fieldName = ValueUtils.getValueAsString(inputValues.get("fieldName"));
      String value     = ValueUtils.getValueAsString(inputValues.get("value"));

      if(fieldName.contains("."))
      {
         /////////////////////////////////////////////////////////////////
         // field names from the PVS are always qualified by table name //
         /////////////////////////////////////////////////////////////////
         fieldName = fieldName.substring(fieldName.indexOf(".") + 1);
      }

      JSONObject apiRecordToUpdate = new JSONObject();
      apiRecordToUpdate.put(fieldName, value);
      QRecord recordToUpdate = QRecordApiAdapter.apiJsonObjectToQRecord(apiRecordToUpdate, workflow.getTableName(), workflowRevision.getApiName(), workflowRevision.getApiVersion(), false);

      QRecord originalRecord = RecordWorkflowUtils.getRecordFromContext(context);
      copyIdAndSecurityFieldsIntoRecordToUpdate(originalRecord, recordToUpdate);

      return executeCommonEndingWithOrWithoutApi(context, originalRecord, recordToUpdate, fieldName, value);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private WorkflowStepOutput executeWithoutApi(Map<String, Serializable> inputValues, WorkflowExecutionContext context) throws QException
   {
      QRecord originalRecord = RecordWorkflowUtils.getRecordFromContext(context);

      ////////////////////////////////////////////////////////////////
      // copy values from the source record into a record-to-update //
      ////////////////////////////////////////////////////////////////
      QRecord recordToUpdate = new QRecord();
      copyIdAndSecurityFieldsIntoRecordToUpdate(originalRecord, recordToUpdate);

      ///////////////////////////////////////////////
      // set the new value in the record-to-update //
      ///////////////////////////////////////////////
      String fieldName = ValueUtils.getValueAsString(inputValues.get("fieldName"));
      String value     = ValueUtils.getValueAsString(inputValues.get("value"));

      if(fieldName.contains("."))
      {
         /////////////////////////////////////////////////////////////////
         // field names from the PVS are always qualified by table name //
         /////////////////////////////////////////////////////////////////
         fieldName = fieldName.substring(fieldName.indexOf(".") + 1);
      }

      recordToUpdate.setValue(fieldName, value);

      return executeCommonEndingWithOrWithoutApi(context, originalRecord, recordToUpdate, fieldName, value);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private WorkflowStepOutput executeCommonEndingWithOrWithoutApi(WorkflowExecutionContext context, QRecord originalRecord, QRecord recordToUpdate, String fieldName, String value) throws QException
   {
      String         tableName = context.getWorkflow().getTableName();
      QTableMetaData table     = QContext.getQInstance().getTable(tableName);

      UpdateOutput updateOutput = new UpdateAction().execute(new UpdateInput(tableName)
         .withTransaction(context.getTransaction())
         .withRecord(recordToUpdate));

      QRecord outputRecord = updateOutput.getRecords().get(0);
      if(CollectionUtils.nullSafeHasContents(outputRecord.getErrors()))
      {
         throw (new QException("Error updating record: " + outputRecord.getErrorsAsString()));
      }
      else
      {
         ///////////////////////////////////////
         // refresh the object in the context //
         ///////////////////////////////////////
         QRecord updatedRecord = GetAction.execute(tableName, originalRecord.getValue(table.getPrimaryKeyField()));
         RecordWorkflowUtils.updateRecordInContext(context, updatedRecord);

         String stepSummary = getStepSummary(context.getWorkflow().getId(), context.getWorkflowRevision().getApiName(), context.getWorkflowRevision().getApiVersion(), fieldName, value, true);
         return new WorkflowStepOutput(value, stepSummary);
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private static void copyIdAndSecurityFieldsIntoRecordToUpdate(QRecord originalRecord, QRecord recordToUpdate)
   {
      recordToUpdate.setValue("id", originalRecord.getValueInteger("id"));
      for(RecordSecurityLock recordSecurityLock : CollectionUtils.nonNullList(QContext.getQInstance().getTable(originalRecord.getTableName()).getRecordSecurityLocks()))
      {
         String securityFieldName = recordSecurityLock.getFieldName();
         recordToUpdate.setValue(securityFieldName, originalRecord.getValue(securityFieldName));
      }
   }

}
