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

package com.kingsrook.qbits.workflows;


import java.io.Serializable;
import java.util.Map;
import com.kingsrook.qbits.workflows.definition.WorkflowStepType;
import com.kingsrook.qbits.workflows.definition.WorkflowType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qbits.workflows.execution.WorkflowStepExecutorInterface;
import com.kingsrook.qbits.workflows.execution.WorkflowTypeExecutorInterface;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class TestWorkflowDefinitions
{
   public static final String TEST_WORKFLOW_TYPE  = "TestWorkflowType";
   public static final String ADD_X_TO_SUM_ACTION = "addXToSumAction";
   public static final String BOOLEAN_CONDITIONAL = "booleanConditional";



   /***************************************************************************
    **
    ***************************************************************************/
   public static void registerTestWorkflowTypes() throws QException
   {
      WorkflowsRegistry.getInstance().registerWorkflowType(new WorkflowType()
         .withName(TEST_WORKFLOW_TYPE)
         .withLabel("Test Workflow Type")
         .withExecutor(new QCodeReference(TestWorkflowTypeExecutor.class))
         .withDescription("This is a test workflow type."));

      WorkflowsRegistry.getInstance().registerWorkflowStepType(new WorkflowStepType()
         .withName(ADD_X_TO_SUM_ACTION)
         .withLabel("Add X to Sum")
         .withExecutor(new QCodeReference(AddXToSumStepExecutor.class))
         .withDescription("Add int in `x` to int in `sum`"));

      WorkflowsRegistry.getInstance().registerWorkflowStepType(new WorkflowStepType()
         .withName(BOOLEAN_CONDITIONAL)
         .withLabel("Boolean Conditional")
         .withExecutor(new QCodeReference(BooleanConditionalExecutor.class))
         .withDescription("Evaluate boolean named `condition`"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class TestWorkflowTypeExecutor implements WorkflowTypeExecutorInterface
   {
      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public void preRun(Map<String, Serializable> context, Workflow workflow, WorkflowRevision workflowRevision)
      {
         Integer seedValue = ValueUtils.getValueAsInteger(context.getOrDefault("seedValue", 0));
         context.put("sum", seedValue);
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public void postRun(Map<String, Serializable> context)
      {
         Integer overrideSumInPostRun = ValueUtils.getValueAsInteger(context.get("overrideSumInPostRun"));
         if(overrideSumInPostRun != null)
         {
            context.put("sum", overrideSumInPostRun);
         }
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public void preStep(WorkflowStep step, Map<String, Serializable> context)
      {
         Serializable doubleSumInEveryPreStep = context.get("doubleSumInEveryPreStep");
         if(doubleSumInEveryPreStep != null)
         {
            Integer sum = ValueUtils.getValueAsInteger(context.getOrDefault("sum", 0));
            context.put("sum", sum * 2);
         }
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public Serializable postStep(WorkflowStep step, Map<String, Serializable> context, Serializable stepOutput)
      {
         Serializable overrideReturnValueInPostStep = context.get("overrideReturnValueInPostStep");
         if(overrideReturnValueInPostStep != null)
         {
            return (overrideReturnValueInPostStep);
         }

         return stepOutput;
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class AddXToSumStepExecutor implements WorkflowStepExecutorInterface
   {
      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public Serializable execute(WorkflowStep step, Map<String, Serializable> inputValues, Map<String, Serializable> context)
      {
         Integer sum = ValueUtils.getValueAsInteger(context.getOrDefault("sum", 0));
         Integer x   = ValueUtils.getValueAsInteger(inputValues.getOrDefault("x", 0));

         Integer newSum = sum + x;
         context.put("sum", newSum);

         return (newSum);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class BooleanConditionalExecutor implements WorkflowStepExecutorInterface
   {
      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public Serializable execute(WorkflowStep step, Map<String, Serializable> inputValues, Map<String, Serializable> context)
      {
         /* ??
         Map<String, Object> inputValues = getInputValues(step);
         if(inputValues.containsKey("filter"))
         {
            JsonUtils.toObject(ValueUtils.getValueAsString(inputValues.get("filter")))
         }
         */

         Boolean condition = ValueUtils.getValueAsBoolean(context.getOrDefault("condition", false));
         return (condition);
      }
   }

}
