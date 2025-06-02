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
import java.util.Set;
import com.kingsrook.qbits.workflows.definition.OutboundLinkMode;
import com.kingsrook.qbits.workflows.definition.WorkflowStepType;
import com.kingsrook.qbits.workflows.execution.WorkflowExecutionContext;
import com.kingsrook.qbits.workflows.execution.WorkflowStepExecutorInterface;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
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
         String newValue = ValueUtils.getValueAsString(input.getNewValue());

         QFieldMetaData updatedField = new QFieldMetaData("value", QFieldType.STRING)
            .withLabel("Value")
            .withIsEditable(false);

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

         if(StringUtils.hasContent(newValue))
         {
            try
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
            catch(Exception e)
            {
               LOG.info("Error getting field from table", e, logPair("fieldName", newValue));
            }
         }

         updatedField.setGridColumns(12);

         FormAdjusterOutput output = new FormAdjusterOutput();
         output.setUpdatedFieldMetaData(Map.of("value", new QFrontendFieldMetaData(updatedField)));

         ////////////////////////////////////////////////////////
         // on-change clear out the value in the 'value' field //
         ////////////////////////////////////////////////////////
         if(RunFormAdjusterProcess.EVENT_ON_CHANGE.equals(input.getEvent()))
         {
            output.setFieldsToClear(Set.of("value"));
         }
         else
         {
            ////////////////////////////////////////////////////////////
            // on-load, for PVS fields, look up the value for display //
            ////////////////////////////////////////////////////////////
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
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public String getDynamicStepSummary(Integer workflowId, JSONObject values) throws QException
   {
      String fieldName = values.optString("fieldName", null);
      String value     = values.optString("value", null);

      return getStepSummary(workflowId, fieldName, value, false);
   }


   /***************************************************************************
    **
    ***************************************************************************/
   private String getStepSummary(Integer workflowId, String fieldName, String value, boolean isPastTense)
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
               FieldAndJoinTable fieldAndJoinTable = FieldAndJoinTable.get(table, fieldName);
               fieldName = fieldAndJoinTable.field().getName();
               fieldLabel = fieldAndJoinTable.field().getLabel();
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

            return (fieldLabel + " " + verb + " set to " + displayValue);
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
   public Serializable execute(WorkflowStep step, Map<String, Serializable> inputValues, WorkflowExecutionContext context) throws QException
   {
      QRecord record = RecordWorkflowUtils.getRecordFromContext(context);

      ////////////////////////////////////////////////////////////////
      // copy values from the source record into a record-to-update //
      ////////////////////////////////////////////////////////////////
      /*
      QRecord recordToUpdate = new QRecord();
      recordToUpdate.setValue("id", record.getValueInteger("id"));
      for(RecordSecurityLock recordSecurityLock : CollectionUtils.nonNullList(QContext.getQInstance().getTable(record.getTableName()).getRecordSecurityLocks()))
      {
         String securityFieldName = recordSecurityLock.getFieldName();
         recordToUpdate.setValue(securityFieldName, recordToUpdate.getValue(securityFieldName));
      }
      */

      ///////////////////////////////////////////////
      // set the new value in the record-to-update //
      ///////////////////////////////////////////////
      String fieldName = ValueUtils.getValueAsString(inputValues.get("fieldName"));
      String value     = ValueUtils.getValueAsString(inputValues.get("value"));

      if(fieldName.contains("."))
      {
         QTableMetaData    table             = QContext.getQInstance().getTable(context.getWorkflow().getTableName());
         FieldAndJoinTable fieldAndJoinTable = FieldAndJoinTable.get(table, fieldName);
         fieldName = fieldAndJoinTable.field().getName();
      }

      // recordToUpdate.setValue(fieldName, value);
      record.setValue(fieldName, value);

      UpdateOutput updateOutput = new UpdateAction().execute(new UpdateInput(record.getTableName())
         .withTransaction(context.getTransaction())
         // .withRecord(recordToUpdate));
         .withRecord(record));

      // todo wip check for errors

      RecordWorkflowUtils.updateRecordInContext(context, record);

      //////////////////////////////////////////////////////////////////////////////////
      // todo - do we need to refresh the record in context w/ what was just updated? //
      // and if so - should we fully re-fetch it?                                     //
      //////////////////////////////////////////////////////////////////////////////////
      return getStepSummary(context.getWorkflow().getId(), fieldName, value, true);
   }

}
