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
import java.util.Map;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;


/*******************************************************************************
 **
 *******************************************************************************/
public interface WorkflowTypeExecutorInterface
{

   /***************************************************************************
    **
    ***************************************************************************/
   default void preRun(Map<String, Serializable> context, Workflow workflow, WorkflowRevision workflowRevision) throws QException
   {

   }

   /***************************************************************************
    **
    ***************************************************************************/
   default void postRun(Map<String, Serializable> context) throws QException
   {

   }


   /***************************************************************************
    *
    ***************************************************************************/
   default void preStep(WorkflowStep step, Map<String, Serializable> context) throws QException
   {

   }

   /***************************************************************************
    *
    ***************************************************************************/
   default Serializable postStep(WorkflowStep step, Map<String, Serializable> context, Serializable stepOutput) throws QException
   {
      return stepOutput;
   }

}
