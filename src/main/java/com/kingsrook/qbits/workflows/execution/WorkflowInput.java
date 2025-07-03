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
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowInput extends AbstractActionInput implements Serializable
{
   private Integer                   workflowId;
   private Map<String, Serializable> values;

   private WorkflowExecutionContext workflowExecutionContext;

   private QBackendTransaction transaction;



   /*******************************************************************************
    ** Getter for workflowId
    *******************************************************************************/
   public Integer getWorkflowId()
   {
      return (this.workflowId);
   }



   /*******************************************************************************
    ** Setter for workflowId
    *******************************************************************************/
   public void setWorkflowId(Integer workflowId)
   {
      this.workflowId = workflowId;
   }



   /*******************************************************************************
    ** Fluent setter for workflowId
    *******************************************************************************/
   public WorkflowInput withWorkflowId(Integer workflowId)
   {
      this.workflowId = workflowId;
      return (this);
   }



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
   public WorkflowInput withValues(Map<String, Serializable> values)
   {
      this.values = values;
      return (this);
   }



   /*******************************************************************************
    * Getter for transaction
    * @see #withTransaction(QBackendTransaction)
    *******************************************************************************/
   public QBackendTransaction getTransaction()
   {
      return (this.transaction);
   }



   /*******************************************************************************
    * Setter for transaction
    * @see #withTransaction(QBackendTransaction)
    *******************************************************************************/
   public void setTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
   }



   /*******************************************************************************
    * Fluent setter for transaction
    *
    * @param transaction
    * a backend transaction owned by the caller, which will be used in the workflow's
    * execution context.  If non-null, it is assumed that the caller 100% owns the
    * transaction, and will manage its end-of-life (commit, rollback, close).  If
    * null, then the workflow executor will create a new transaction (going through
    * WorkflowTypeExecutorInterface.openTransaction), and will do the commit/rollback/
    * close on it.
    *
    * @return this
    *******************************************************************************/
   public WorkflowInput withTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
      return (this);
   }



   /*******************************************************************************
    * Getter for workflowExecutionContext
    * @see #withWorkflowExecutionContext(WorkflowExecutionContext)
    *******************************************************************************/
   public WorkflowExecutionContext getWorkflowExecutionContext()
   {
      return (this.workflowExecutionContext);
   }



   /*******************************************************************************
    * Setter for workflowExecutionContext
    * @see #withWorkflowExecutionContext(WorkflowExecutionContext)
    *******************************************************************************/
   public void setWorkflowExecutionContext(WorkflowExecutionContext workflowExecutionContext)
   {
      this.workflowExecutionContext = workflowExecutionContext;
   }



   /*******************************************************************************
    * Fluent setter for workflowExecutionContext
    *
    * @param workflowExecutionContext
    * Allow caller to supply a WorkflowExecutionContext object (or subclass)
    *
    * @return this
    *******************************************************************************/
   public WorkflowInput withWorkflowExecutionContext(WorkflowExecutionContext workflowExecutionContext)
   {
      this.workflowExecutionContext = workflowExecutionContext;
      return (this);
   }

}
