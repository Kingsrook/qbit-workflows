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


import com.kingsrook.qbits.workflows.definition.WorkflowType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** widget for editing the workflow on the workflow table
 *******************************************************************************/
public class WorkflowEditorWidget extends BaseQSequentialWorkflowWidgetRenderer
{
   private static final QLogger LOG = QLogger.getLogger(WorkflowEditorWidget.class);

   public static final String NAME = "WorkflowEditor";



   /***************************************************************************
    **
    ***************************************************************************/
   protected String getWidgetName()
   {
      return (NAME);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   protected ReadonlyState getReadonlyState()
   {
      return ReadonlyState.INITIALLY;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   protected OutputData getOutputData(RenderWidgetInput input) throws QException
   {
      return new OutputData()
      {
         private String workflowId = input.getQueryParams().get("id");



         /***************************************************************************
          **
          ***************************************************************************/
         public Integer getWorkflowId()
         {
            return (ValueUtils.getValueAsInteger(workflowId));
         }



         /***************************************************************************
          *
          ***************************************************************************/
         public Boolean getIncludeTestForm()
         {
            try
            {
               if(workflowId != null)
               {
                  QRecord workflowRecord = GetAction.execute(Workflow.TABLE_NAME, workflowId);
                  if(workflowRecord != null)
                  {
                     String       workflowTypeName = workflowRecord.getValueString("workflowTypeName");
                     WorkflowType workflowType     = WorkflowsRegistry.of(QContext.getQInstance()).getWorkflowType(workflowTypeName);
                     if(workflowType.getTester() != null)
                     {
                        return (true);
                     }
                  }
               }
            }
            catch(Exception e)
            {
               LOG.warn("Error getting workflow record", e, logPair("workflowId", workflowId));
            }

            return (false);
         }

      };
   }
}
