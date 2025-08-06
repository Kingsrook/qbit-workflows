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
import com.kingsrook.qbits.workflows.model.WorkflowTestRun;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowTesterOutput extends AbstractActionOutput implements Serializable
{
   private WorkflowTestRun workflowTestRun;



   /*******************************************************************************
    * Getter for workflowTestRun
    * @see #withWorkflowTestRun(WorkflowTestRun)
    *******************************************************************************/
   public WorkflowTestRun getWorkflowTestRun()
   {
      return (this.workflowTestRun);
   }



   /*******************************************************************************
    * Setter for workflowTestRun
    * @see #withWorkflowTestRun(WorkflowTestRun)
    *******************************************************************************/
   public void setWorkflowTestRun(WorkflowTestRun workflowTestRun)
   {
      this.workflowTestRun = workflowTestRun;
   }



   /*******************************************************************************
    * Fluent setter for workflowTestRun
    *
    * @param workflowTestRun
    * The run log object - with populated children:  WorkflowTestRunScenario and
    * WorkflowTestOutput.
    * @return this
    *******************************************************************************/
   public WorkflowTesterOutput withWorkflowTestRun(WorkflowTestRun workflowTestRun)
   {
      this.workflowTestRun = workflowTestRun;
      return (this);
   }

}
