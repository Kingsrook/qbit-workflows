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
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 * interface for the code that validates a step in a workflow, e.g., when it's
 * being saved.
 *******************************************************************************/
public interface WorkflowStepValidatorInterface
{

   /***************************************************************************
    * validate that a workflow step can be saved.
    *
    * @param step the step to validate against all the other params.
    * @param inputValues map of values from user
    * @param workflowRevision the workflow revision that the step will belong to
    * @param workflow the workflow that the revision will belong to
    * @param errors out param - any validation errors should be added to this list.
    ***************************************************************************/
   void validate(WorkflowStep step, Map<String, Serializable> inputValues, QRecord workflowRevision, QRecord workflow, List<String> errors) throws QException;

}
