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

package com.kingsrook.qbits.workflows.definition;


import java.io.Serializable;
import java.util.List;


/*******************************************************************************
 ** collection of step-types within a workflow type.  just organizational.
 *******************************************************************************/
public class WorkflowStepTypeCategory implements Serializable
{
   private String name;
   private String label;
   private List<String> workflowStepTypes;



   /*******************************************************************************
    ** Getter for name
    *******************************************************************************/
   public String getName()
   {
      return (this.name);
   }



   /*******************************************************************************
    ** Setter for name
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   public WorkflowStepTypeCategory withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for label
    *******************************************************************************/
   public String getLabel()
   {
      return (this.label);
   }



   /*******************************************************************************
    ** Setter for label
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    *******************************************************************************/
   public WorkflowStepTypeCategory withLabel(String label)
   {
      this.label = label;
      return (this);
   }


   /*******************************************************************************
    ** Getter for workflowStepTypes
    *******************************************************************************/
   public List<String> getWorkflowStepTypes()
   {
      return (this.workflowStepTypes);
   }



   /*******************************************************************************
    ** Setter for workflowStepTypes
    *******************************************************************************/
   public void setWorkflowStepTypes(List<String> workflowStepTypes)
   {
      this.workflowStepTypes = workflowStepTypes;
   }



   /*******************************************************************************
    ** Fluent setter for workflowStepTypes
    *******************************************************************************/
   public WorkflowStepTypeCategory withWorkflowStepTypes(List<String> workflowStepTypes)
   {
      this.workflowStepTypes = workflowStepTypes;
      return (this);
   }


}
