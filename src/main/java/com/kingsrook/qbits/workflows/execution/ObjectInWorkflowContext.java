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
 * Helper object for working with named and typed objects into a workflow context.
 *
 * The object itself is stored in the `context`, under the name specified by
 * the `key`.  The type is driven by the type-parameter to the class.
 *
 * The envisioned usage pattern would have class designed for a particular
 * workflow-type that `extends WorkflowExecutionContext`.  In that class would
 * be a number of public final members of this class, such as:
 * ```
 * public final ObjectInWorkflowContext[String] someString = new ObjectInWorkflowContext(this, "someString");
 * ```
 *
 * Then within workflow steps:
 * ```
 * context.someString.set("some value");
 * String theValue = context.someString.get();
 * ```
 *******************************************************************************/
public class ObjectInWorkflowContext<T extends Serializable>
{
   private final WorkflowExecutionContext context;
   private final String                   key;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ObjectInWorkflowContext(WorkflowExecutionContext context, String key)
   {
      this.context = context;
      this.key = key;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ObjectInWorkflowContext(WorkflowExecutionContext context, String key, T initialValue)
   {
      this.context = context;
      this.key = key;

      set(initialValue);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private String getKey()
   {
      return key;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public T get()
   {
      return (T) context.getValues().get(getKey());
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void set(T value)
   {
      context.getValues().put(getKey(), value);
   }
}
