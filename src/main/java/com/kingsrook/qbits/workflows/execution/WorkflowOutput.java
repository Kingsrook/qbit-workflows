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
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowOutput extends AbstractActionOutput implements Serializable
{
   private Exception                 exception;
   private Map<String, Serializable> values;
   private List<WorkflowTrace>       traceList;



   /*******************************************************************************
    ** Getter for values
    *******************************************************************************/
   public Map<String, Serializable> getValues()
   {
      return (this.values);
   }



   /*******************************************************************************
    ** Setter for values
    *******************************************************************************/
   public void setValues(Map<String, Serializable> values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Fluent setter for values
    *******************************************************************************/
   public WorkflowOutput withValues(Map<String, Serializable> values)
   {
      this.values = values;
      return (this);
   }



   /*******************************************************************************
    ** Getter for exception
    *******************************************************************************/
   public Exception getException()
   {
      return (this.exception);
   }



   /*******************************************************************************
    ** Setter for exception
    *******************************************************************************/
   public void setException(Exception exception)
   {
      this.exception = exception;
   }



   /*******************************************************************************
    ** Fluent setter for exception
    *******************************************************************************/
   public WorkflowOutput withException(Exception exception)
   {
      this.exception = exception;
      return (this);
   }


   /*******************************************************************************
    ** Getter for traceList
    *******************************************************************************/
   public List<WorkflowTrace> getTraceList()
   {
      return (this.traceList);
   }



   /*******************************************************************************
    ** Setter for traceList
    *******************************************************************************/
   public void setTraceList(List<WorkflowTrace> traceList)
   {
      this.traceList = traceList;
   }



   /*******************************************************************************
    ** Fluent setter for traceList
    *******************************************************************************/
   public WorkflowOutput withTraceList(List<WorkflowTrace> traceList)
   {
      this.traceList = traceList;
      return (this);
   }


}
