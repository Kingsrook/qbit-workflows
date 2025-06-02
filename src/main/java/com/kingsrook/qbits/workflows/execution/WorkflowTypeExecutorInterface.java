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
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** interface for the code that executes a workflow type - though really just
 ** pre & post, plus pre- & post- individual steps
 *******************************************************************************/
public interface WorkflowTypeExecutorInterface
{

   /***************************************************************************
    **
    ***************************************************************************/
   default void preRun(WorkflowExecutionContext context, Workflow workflow, WorkflowRevision workflowRevision) throws QException
   {

   }

   /***************************************************************************
    **
    ***************************************************************************/
   default void postRun(WorkflowExecutionContext context) throws QException
   {

   }


   /***************************************************************************
    **
    ***************************************************************************/
   default void handleException(Exception e, WorkflowExecutionContext values) throws QException
   {

   }

   /***************************************************************************
    *
    ***************************************************************************/
   default void preStep(WorkflowStep step, WorkflowExecutionContext context) throws QException
   {

   }

   /***************************************************************************
    *
    ***************************************************************************/
   default Serializable postStep(WorkflowStep step, WorkflowExecutionContext context, Serializable stepOutput) throws QException
   {
      return stepOutput;
   }

   /***************************************************************************
    **
    ***************************************************************************/
   default QBackendTransaction openTransaction(Workflow workflow, WorkflowRevision workflowRevision) throws QException
   {
      if(StringUtils.hasContent(workflow.getTableName()))
      {
         return (QBackendTransaction.openFor(new InsertInput(workflow.getTableName())));
      }

      return (null);
   }
}
