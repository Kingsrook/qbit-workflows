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

package com.kingsrook.qbits.workflows.metadata;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import com.kingsrook.qbits.workflows.definition.WorkflowType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qbits.workflows.execution.WorkflowTypeTesterInterface;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.values.QCustomPossibleValueProvider;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** PVS for the variableName field on WorkflowTestAssertion
 *******************************************************************************/
public class WorkflowTestAssertionVariableNamePossibleValueSource implements QCustomPossibleValueProvider<String>, MetaDataProducerInterface<QPossibleValueSource>
{
   public static final String NAME = " WorkflowTestAssertionVariableName";



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QPossibleValueSource produce(QInstance qInstance) throws QException
   {
      return new QPossibleValueSource()
         .withName(NAME)
         .withIdType(QFieldType.STRING)
         .withCustomCodeReference(new QCodeReference(getClass()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QPossibleValue<String> getPossibleValue(Serializable idValue)
   {
      if(idValue == null)
      {
         return (null);
      }

      String   idString = ValueUtils.getValueAsString(idValue);
      String[] idParts  = idString.split("\\.");
      if(idParts.length != 2)
      {
         return (null);
      }

      String         tableName = idParts[0];
      String         fieldName = idParts[1];
      QTableMetaData table     = QContext.getQInstance().getTable(tableName);
      QFieldMetaData field     = table.getField(fieldName);
      return (new QPossibleValue<>(idString, field.getLabel()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QPossibleValue<String>> search(SearchPossibleValueSourceInput input) throws QException
   {
      Integer workflowTestScenarioId = ValueUtils.getValueAsInteger(CollectionUtils.nonNullMap(input.getOtherValues()).get("workflowTestScenarioId"));
      if(workflowTestScenarioId == null)
      {
         return Collections.emptyList();
      }

      QRecord workflowTestScenario = GetAction.execute(WorkflowTestScenario.TABLE_NAME, workflowTestScenarioId);
      if(workflowTestScenario == null)
      {
         return Collections.emptyList();
      }

      QRecord workflow = GetAction.execute(Workflow.TABLE_NAME, workflowTestScenario.getValueInteger("workflowId"));
      if(workflow == null)
      {
         return Collections.emptyList();
      }

      String            workflowTypeName  = workflow.getValueString("workflowTypeName");
      WorkflowsRegistry workflowsRegistry = WorkflowsRegistry.of(QContext.getQInstance());
      WorkflowType      workflowType      = workflowsRegistry.getWorkflowType(workflowTypeName);
      if(workflowType == null)
      {
         return Collections.emptyList();
      }

      WorkflowTypeTesterInterface workflowTypeTester = QCodeLoader.getAdHoc(WorkflowTypeTesterInterface.class, workflowType.getTester());
      return (workflowTypeTester.searchTestAssertionVariableNamePossibleValues(workflow, input));
   }
}
