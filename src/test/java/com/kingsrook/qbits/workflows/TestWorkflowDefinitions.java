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
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.definition.OutboundLinkMode;
import com.kingsrook.qbits.workflows.definition.OutboundLinkOption;
import com.kingsrook.qbits.workflows.definition.WorkflowStepType;
import com.kingsrook.qbits.workflows.definition.WorkflowType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qbits.workflows.execution.WorkflowExecutionContext;
import com.kingsrook.qbits.workflows.execution.WorkflowStepExecutorInterface;
import com.kingsrook.qbits.workflows.execution.WorkflowStepOutput;
import com.kingsrook.qbits.workflows.execution.WorkflowTypeExecutorInterface;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.context.QContext;
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
   public static final String CONTAINER           = "container";



   /***************************************************************************
    **
    ***************************************************************************/
   public static void registerTestWorkflowTypes() throws QException
   {
      WorkflowsRegistry.of(QContext.getQInstance()).registerWorkflowType(new WorkflowType()
         .withName(TEST_WORKFLOW_TYPE)
         .withLabel("Test Workflow Type")
         .withExecutor(new QCodeReference(TestWorkflowTypeExecutor.class))
         .withDescription("This is a test workflow type."));

      WorkflowsRegistry.of(QContext.getQInstance()).registerWorkflowStepType(new WorkflowStepType()
         .withName(ADD_X_TO_SUM_ACTION)
         .withLabel("Add X to Sum")
         .withOutboundLinkMode(OutboundLinkMode.ONE)
         .withExecutor(new QCodeReference(AddXToSumStepExecutor.class))
         .withDescription("Add int in `x` to int in `sum`"));

      WorkflowsRegistry.of(QContext.getQInstance()).registerWorkflowStepType(new WorkflowStepType()
         .withName(BOOLEAN_CONDITIONAL)
         .withLabel("Boolean Conditional")
         .withOutboundLinkMode(OutboundLinkMode.TWO)
         .withOutboundLinkOptions(List.of(
            new OutboundLinkOption().withValue("true").withLabel("Then"),
            new OutboundLinkOption().withValue("false").withLabel("Else")
         ))
         .withExecutor(new QCodeReference(BooleanConditionalExecutor.class))
         .withDescription("Evaluate boolean named `condition`"));

      WorkflowsRegistry.of(QContext.getQInstance()).registerWorkflowStepType(new WorkflowStepType()
         .withName(CONTAINER)
         .withOutboundLinkMode(OutboundLinkMode.CONTAINER)
         .withLabel("Container")
         .withDescription("Group steps in the UI"));
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
      public void preRun(WorkflowExecutionContext context, Workflow workflow, WorkflowRevision workflowRevision)
      {
         Integer seedValue = ValueUtils.getValueAsInteger(context.getValues().getOrDefault("seedValue", 0));
         context.getValues().put("sum", seedValue);
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public void postRun(WorkflowExecutionContext context)
      {
         Integer overrideSumInPostRun = ValueUtils.getValueAsInteger(context.getValues().get("overrideSumInPostRun"));
         if(overrideSumInPostRun != null)
         {
            context.getValues().put("sum", overrideSumInPostRun);
         }
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public void preStep(WorkflowStep step, WorkflowExecutionContext context)
      {
         Serializable doubleSumInEveryPreStep = context.getValues().get("doubleSumInEveryPreStep");
         if(doubleSumInEveryPreStep != null)
         {
            Integer sum = ValueUtils.getValueAsInteger(context.getValues().getOrDefault("sum", 0));
            context.getValues().put("sum", sum * 2);
         }
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public WorkflowStepOutput postStep(WorkflowStep step, WorkflowExecutionContext context, WorkflowStepOutput stepOutput)
      {
         Serializable overrideReturnValueInPostStep = context.getValues().get("overrideReturnValueInPostStep");
         if(overrideReturnValueInPostStep != null)
         {
            return (new WorkflowStepOutput(overrideReturnValueInPostStep, stepOutput.message()));
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
      public WorkflowStepOutput execute(WorkflowStep step, Map<String, Serializable> inputValues, WorkflowExecutionContext context)
      {
         Integer sum = ValueUtils.getValueAsInteger(context.getValues().getOrDefault("sum", 0));
         Integer x   = ValueUtils.getValueAsInteger(inputValues.getOrDefault("x", 0));

         Integer newSum = sum + x;
         context.getValues().put("sum", newSum);

         return (new WorkflowStepOutput(newSum, "Added %d and %d to make %d".formatted(sum, x, newSum)));
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
      public WorkflowStepOutput execute(WorkflowStep step, Map<String, Serializable> inputValues, WorkflowExecutionContext context)
      {
         Boolean condition = ValueUtils.getValueAsBoolean(context.getValues().getOrDefault("condition", false));
         return (new WorkflowStepOutput(condition));
      }
   }

}
