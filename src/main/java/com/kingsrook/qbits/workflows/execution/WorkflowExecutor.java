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

package com.kingsrook.qbits.workflows.execution;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.google.gson.reflect.TypeToken;
import com.kingsrook.qbits.workflows.definition.WorkflowStepType;
import com.kingsrook.qbits.workflows.definition.WorkflowType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.actions.AbstractQActionBiConsumer;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.json.JSONObject;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowExecutor extends AbstractQActionBiConsumer<WorkflowInput, WorkflowOutput>
{
   private static final QLogger LOG = QLogger.getLogger(WorkflowExecutor.class);



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void execute(WorkflowInput workflowInput, WorkflowOutput workflowOutput) throws QException
   {
      /////////////////////////////////////////////////////////////////////////////////////////
      // get values map - initializing it if needed, and wrapping in modifiable ds if needed //
      /////////////////////////////////////////////////////////////////////////////////////////
      Map<String, Serializable> values = Objects.requireNonNullElseGet(workflowInput.getValues(), () -> new LinkedHashMap<>());
      values = CollectionUtils.useOrWrap(values, TypeToken.get(LinkedHashMap.class));

      ///////////////////////////
      // initialize trace list //
      ///////////////////////////
      List<WorkflowTrace> traceList = new ArrayList<>();
      workflowOutput.setTraceList(traceList);

      try
      {
         ////////////////////////////////////////////////////////////
         // load the workflow (current revision), steps, and links //
         ////////////////////////////////////////////////////////////
         Workflow                           workflow         = getWorkflow(workflowInput.getWorkflowId());
         WorkflowRevision                   workflowRevision = getWorkflowRevision(workflow.getCurrentWorkflowRevisionId());
         Map<Integer, WorkflowStep>         stepMap          = loadSteps(workflowRevision);
         ListingHash<Integer, WorkflowLink> linkMap          = loadLinks(workflowRevision);

         ////////////////////////////////////////////
         // load type-executor, and do its pre-run //
         ////////////////////////////////////////////
         WorkflowType workflowType = WorkflowsRegistry.getInstance().getWorkflowType(workflow.getWorkflowTypeName());
         if(workflowType == null)
         {
            throw new QException("Workflow type not found by name: " + workflow.getWorkflowTypeName());
         }
         WorkflowTypeExecutorInterface workflowTypeExecutor = QCodeLoader.getAdHoc(WorkflowTypeExecutorInterface.class, workflowType.getExecutor());
         workflowTypeExecutor.preRun(values, workflow, workflowRevision);

         ///////////////
         // step loop //
         ///////////////
         Integer stepNo = workflowRevision.getStartStepNo();
         while(stepNo != null)
         {
            WorkflowStep step = stepMap.get(stepNo);
            if(step == null)
            {
               throw new QException("Step not found by stepNo: " + stepNo);
            }

            WorkflowTrace workflowTrace = new WorkflowTrace();
            workflowTrace.setStepNo(stepNo);
            traceList.add(workflowTrace);

            Serializable stepOutput = executeStep(step, workflowTypeExecutor, values);
            workflowTrace.setStepOutput(stepOutput);

            stepNo = getNextStepNo(stepOutput, stepNo, linkMap);
         }

         //////////////////////////////////
         // post-run, then output values //
         //////////////////////////////////
         workflowTypeExecutor.postRun(values);
         workflowOutput.setValues(values);
      }
      catch(Exception e)
      {
         LOG.info("Exception running workflow", e, logPair("workflowId", workflowInput.getWorkflowId()));
         workflowOutput.setException(e);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private Integer getNextStepNo(Serializable stepOutput, Integer fromStepNo, ListingHash<Integer, WorkflowLink> linkMap) throws QException
   {
      List<WorkflowLink> links = linkMap.get(fromStepNo);
      if(CollectionUtils.nullSafeIsEmpty(links))
      {
         // todo:trace "no outbound links found?"
         return (null);
      }

      Class<? extends Serializable> stepOutputClass = stepOutput == null ? null : stepOutput.getClass();

      for(WorkflowLink link : links)
      {
         if(link.getConditionValue() == null)
         {
            return (link.getToStepNo());
         }
         else
         {
            try
            {
               Serializable valueAsType = stepOutputClass == null ? link.getConditionValue() : ValueUtils.getValueAsType(stepOutputClass, link.getConditionValue());
               if(Objects.equals(valueAsType, stepOutput))
               {
                  return link.getToStepNo();
               }
            }
            catch(Exception e)
            {
               LOG.debug("Unable to evaluate condition value: " + link.getConditionValue() + " for step: " + fromStepNo, e);
            }
         }
      }

      return (null);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private Serializable executeStep(WorkflowStep step, WorkflowTypeExecutorInterface workflowTypeExecutor, Map<String, Serializable> values) throws QException
   {
      WorkflowStepType workflowStepType = WorkflowsRegistry.getInstance().getWorkflowStepType(step.getWorkflowStepTypeName());
      if(workflowStepType == null)
      {
         throw new QException("Workflow step type not found by name: " + step.getWorkflowStepTypeName());
      }

      WorkflowStepExecutorInterface workflowStepExecutor = QCodeLoader.getAdHoc(WorkflowStepExecutorInterface.class, workflowStepType.getExecutor());

      workflowTypeExecutor.preStep(step, values);

      JSONObject                jsonObject  = JsonUtils.toJSONObject(step.getInputValuesJson());
      Map<String, Object>       mapValues   = jsonObject.toMap();
      Map<String, Serializable> inputValues = new LinkedHashMap<>();

      for(Map.Entry<String, Object> entry : mapValues.entrySet())
      {
         if(entry.getValue() instanceof Serializable s)
         {
            inputValues.put(entry.getKey(), s);
         }
      }

      Serializable stepOutput = workflowStepExecutor.execute(step, inputValues, values);
      stepOutput = workflowTypeExecutor.postStep(step, values, stepOutput);

      return stepOutput;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private Map<Integer, WorkflowStep> loadSteps(WorkflowRevision workflowRevision)
   {
      Map<Integer, WorkflowStep> steps = new HashMap<>();
      for(WorkflowStep workflowStep : CollectionUtils.nonNullList(workflowRevision.getSteps()))
      {
         steps.put(workflowStep.getStepNo(), workflowStep);
      }
      return steps;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private ListingHash<Integer, WorkflowLink> loadLinks(WorkflowRevision workflowRevision)
   {
      ListingHash<Integer, WorkflowLink> links = new ListingHash<>();
      for(WorkflowLink workflowLink : CollectionUtils.nonNullList(workflowRevision.getLinks()))
      {
         links.add(workflowLink.getFromStepNo(), workflowLink);
      }
      return links;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private WorkflowRevision getWorkflowRevision(Integer workflowRevisionId) throws QException
   {
      QRecord workflowRevision = new GetAction().executeForRecord(new GetInput(WorkflowRevision.TABLE_NAME)
         .withIncludeAssociations(true)
         .withPrimaryKey(workflowRevisionId));
      if(workflowRevision == null)
      {
         throw new QException("Workflow Revision not found by id: " + workflowRevisionId);
      }

      return (new WorkflowRevision(workflowRevision));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private static Workflow getWorkflow(Integer workflowId) throws QException
   {
      QRecord workflow = new GetAction().executeForRecord(new GetInput(Workflow.TABLE_NAME).withPrimaryKey(workflowId));
      if(workflow == null)
      {
         throw new QException("Workflow not found by id: " + workflowId);
      }
      return new Workflow(workflow);
   }

}
