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


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qbits.workflows.definition.WorkflowStepTypeCategory;
import com.kingsrook.qbits.workflows.definition.WorkflowType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.EmptyMetaDataProducerOutput;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;


/*******************************************************************************
 ** class to define the RecordWorkflows workflow type.
 *******************************************************************************/
public class RecordWorkflowsDefinitionProducer implements MetaDataProducerInterface<EmptyMetaDataProducerOutput>
{
   public static final String WORKFLOW_TYPE = "RecordWorkflow";



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public EmptyMetaDataProducerOutput produce(QInstance qInstance) throws QException
   {
      registerStepTypes();
      registerWorkflow();
      return (new EmptyMetaDataProducerOutput());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void registerStepTypes() throws QException
   {
      ////////////////////////////
      // first, general actions //
      ////////////////////////////
      WorkflowsRegistry.getInstance().registerWorkflowStepType(new UpdateInputRecordFieldStep());

      //////////////////////
      // then, conditions //
      //////////////////////
      WorkflowsRegistry.getInstance().registerWorkflowStepType(new InputRecordFilterStep());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void registerWorkflow() throws QException
   {
      WorkflowsRegistry.getInstance().registerWorkflowType(makeRecordWorkflow());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   static WorkflowType makeRecordWorkflow()
   {
      List<String> actionStepTypes = new ArrayList<>();
      addActionStepTypeNamesToList(actionStepTypes);

      List<String> conditionStepTypes = new ArrayList<>();
      addConditionStepTypeNamesToList(conditionStepTypes);

      return new WorkflowType()
         .withName(WORKFLOW_TYPE)
         .withLabel("Record Workflow")
         .withExecutor(new QCodeReference(RecordWorkflowTypeExecutor.class))
         .withDescription("Apply custom logic to any record from any table.  Can be automatically ran via Table Triggers, or manually via the Run Workflow action.")
         .withStepTypeCategories(List.of(
            new WorkflowStepTypeCategory()
               .withName("actions")
               .withLabel("Actions")
               .withWorkflowStepTypes(actionStepTypes),
            new WorkflowStepTypeCategory()
               .withName("conditions")
               .withLabel("Conditions")
               .withWorkflowStepTypes(conditionStepTypes)
         ));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void addConditionStepTypeNamesToList(List<String> conditionStepTypes)
   {
      conditionStepTypes.add(InputRecordFilterStep.NAME);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void addActionStepTypeNamesToList(List<String> actionStepTypes)
   {
      actionStepTypes.add(UpdateInputRecordFieldStep.NAME);
   }

}
