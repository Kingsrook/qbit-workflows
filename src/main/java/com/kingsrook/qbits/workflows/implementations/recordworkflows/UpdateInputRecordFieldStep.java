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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.kingsrook.qbits.workflows.definition.OutboundLinkMode;
import com.kingsrook.qbits.workflows.definition.WorkflowStepType;
import com.kingsrook.qbits.workflows.execution.WorkflowExecutionContext;
import com.kingsrook.qbits.workflows.execution.WorkflowStepExecutorInterface;
import com.kingsrook.qbits.workflows.execution.WorkflowStepOutput;
import com.kingsrook.qbits.workflows.execution.WorkflowStepValidatorInterface;
import com.kingsrook.qbits.workflows.implementations.WorkflowStepUtils;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.api.actions.GetTableApiFieldsAction;
import com.kingsrook.qqq.api.actions.QRecordApiAdapter;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsInput;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsOutput;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAndJoinTable;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.frontend.materialdashboard.model.metadata.MaterialDashboardFieldMetaData;
import org.json.JSONObject;


/*******************************************************************************
 ** workflow step that updates a record with one value in one field
 *******************************************************************************/
public class UpdateInputRecordFieldStep extends WorkflowStepType implements WorkflowStepExecutorInterface, WorkflowStepValidatorInterface
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
         .withValidator(new QCodeReference(getClass()))
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
   public static Optional<QFieldMetaData> getApiField(String fieldNameMaybeWithTableNamePrefix, String tableName, WorkflowRevision workflowRevision) throws QException
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
   public String getDynamicStepSummary(Integer workflowId, Map<String, Serializable> inputValues) throws QException
   {
      String fieldName = ValueUtils.getValueAsString(inputValues.get("fieldName"));
      String value     = ValueUtils.getValueAsString(inputValues.get("value"));

      String apiName    = null;
      String apiVersion = null;
      if(inputValues.get("workflowRevision") != null)
      {
         Object o = inputValues.get("workflowRevision");
         if(o instanceof Map revisionMap)
         {
            apiName = ValueUtils.getValueAsString(revisionMap.get("apiName"));
            apiVersion = ValueUtils.getValueAsString(revisionMap.get("apiVersion"));
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
   public WorkflowStepOutput execute(WorkflowStep step, Map<String, Serializable> inputValues, WorkflowExecutionContext workflowExecutionContext) throws QException
   {
      RecordWorkflowContext context = (RecordWorkflowContext) workflowExecutionContext;

      QRecord record = context.record.get();

      String fieldName = ValueUtils.getValueAsString(inputValues.get("fieldName"));
      String value     = ValueUtils.getValueAsString(inputValues.get("value"));

      if(WorkflowStepUtils.useApi(context.getWorkflowRevision()))
      {
         String actualFieldName = getActualFieldNameThroughApi(fieldName, value, context);
         record.setValue(actualFieldName, value);
      }
      else
      {
         record.setValue(fieldName, value);
      }

      context.doesRecordNeedUpdated.set(true);

      String stepSummary = getStepSummary(context.getWorkflow().getId(), fieldName, context.getWorkflowRevision().getApiName(), context.getWorkflowRevision().getApiVersion(), value, true);
      return new WorkflowStepOutput(value, stepSummary);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private String getActualFieldNameThroughApi(String fieldName, String value, WorkflowExecutionContext context) throws QException
   {
      Workflow         workflow         = context.getWorkflow();
      WorkflowRevision workflowRevision = context.getWorkflowRevision();

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

      Iterator<String> iterator = recordToUpdate.getValues().keySet().iterator();
      if(iterator.hasNext())
      {
         return (iterator.next());
      }

      throw (new QException("Couldn't find field %s in API: %s Version: %s".formatted(fieldName, workflowRevision.getApiName(), workflowRevision.getApiVersion())));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public void validate(WorkflowStep step, Map<String, Serializable> inputValues, QRecord workflowRevision, QRecord workflow, List<String> errors) throws QException
   {
      if(WorkflowStepUtils.useApi(new WorkflowRevision(workflowRevision)))
      {
         String fieldName  = ValueUtils.getValueAsString(inputValues.get("fieldName"));
         String apiName    = ValueUtils.getValueAsString(workflowRevision.getValueString("apiName"));
         String apiVersion = ValueUtils.getValueAsString(workflowRevision.getValueString("apiVersion"));
         String tableName  = ValueUtils.getValueAsString(workflow.getValueString("tableName"));

         Optional<QFieldMetaData> optionalApiField = getApiField(fieldName, tableName, new WorkflowRevision(workflowRevision));
         if(optionalApiField.isEmpty())
         {
            errors.add("Could not find field '" + fieldName + "' in table '" + tableName + "' for API '" + apiName + "' version '" + apiVersion + "'");
         }
      }
   }

}
