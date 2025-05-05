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


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowTrace
{
   private Integer      stepNo;
   private Serializable stepOutput;



   /*******************************************************************************
    ** Getter for stepNo
    *******************************************************************************/
   public Integer getStepNo()
   {
      return (this.stepNo);
   }



   /*******************************************************************************
    ** Setter for stepNo
    *******************************************************************************/
   public void setStepNo(Integer stepNo)
   {
      this.stepNo = stepNo;
   }



   /*******************************************************************************
    ** Fluent setter for stepNo
    *******************************************************************************/
   public WorkflowTrace withStepNo(Integer stepNo)
   {
      this.stepNo = stepNo;
      return (this);
   }



   /*******************************************************************************
    ** Getter for stepOutput
    *******************************************************************************/
   public Serializable getStepOutput()
   {
      return (this.stepOutput);
   }



   /*******************************************************************************
    ** Setter for stepOutput
    *******************************************************************************/
   public void setStepOutput(Serializable stepOutput)
   {
      this.stepOutput = stepOutput;
   }



   /*******************************************************************************
    ** Fluent setter for stepOutput
    *******************************************************************************/
   public WorkflowTrace withStepOutput(Serializable stepOutput)
   {
      this.stepOutput = stepOutput;
      return (this);
   }

}
