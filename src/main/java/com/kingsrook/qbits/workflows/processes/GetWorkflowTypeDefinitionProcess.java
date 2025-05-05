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


import java.util.LinkedHashMap;
import com.kingsrook.qbits.workflows.definition.WorkflowStepType;
import com.kingsrook.qbits.workflows.definition.WorkflowStepTypeCategory;
import com.kingsrook.qbits.workflows.definition.WorkflowType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class GetWorkflowTypeDefinitionProcess implements BackendStep, MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "getWorkflowTypeDefinition";



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
               .withField(new QFieldMetaData("workflowTypeName", QFieldType.STRING))));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      String       workflowTypeName = runBackendStepInput.getValueString("workflowTypeName");
      WorkflowType workflowType     = WorkflowsRegistry.getInstance().getWorkflowType(workflowTypeName);
      if(workflowType == null)
      {
         throw new QException("Workflow type not found: " + workflowTypeName);
      }
      runBackendStepOutput.addValue("workflowType", workflowType);

      ////////////////////////////////////////////////////////////////////////////////
      // put all of the step types that the workflow type uses into a map to return //
      ////////////////////////////////////////////////////////////////////////////////
      LinkedHashMap<String, WorkflowStepType> workflowStepTypes = new LinkedHashMap<>();
      for(WorkflowStepTypeCategory stepTypeCategory : workflowType.getStepTypeCategories())
      {
         for(String workflowStepTypeName : stepTypeCategory.getWorkflowStepTypes())
         {
            WorkflowStepType workflowStepType = WorkflowsRegistry.getInstance().getWorkflowStepType(workflowStepTypeName);
            if(workflowStepType == null)
            {
               throw new QException("Workflow step type not found: " + workflowStepTypeName);
            }
            workflowStepTypes.put(workflowStepTypeName, workflowStepType);
         }
      }
      runBackendStepOutput.addValue("workflowStepTypes", workflowStepTypes);
   }

}
