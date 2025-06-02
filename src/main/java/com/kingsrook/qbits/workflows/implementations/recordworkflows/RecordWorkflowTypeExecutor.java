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


import com.kingsrook.qbits.workflows.execution.WorkflowExecutionContext;
import com.kingsrook.qbits.workflows.execution.WorkflowTypeExecutorInterface;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qqq.backend.core.exceptions.QException;


/*******************************************************************************
 ** Workflow-type executor used for record workflows.
 *******************************************************************************/
public class RecordWorkflowTypeExecutor implements WorkflowTypeExecutorInterface
{
   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void preRun(WorkflowExecutionContext context, Workflow workflow, WorkflowRevision workflowRevision) throws QException
   {
      WorkflowTypeExecutorInterface.super.preRun(context, workflow, workflowRevision);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void postRun(WorkflowExecutionContext context) throws QException
   {
      WorkflowTypeExecutorInterface.super.postRun(context);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void handleException(Exception e, WorkflowExecutionContext values) throws QException
   {
      WorkflowTypeExecutorInterface.super.handleException(e, values);
   }


}
