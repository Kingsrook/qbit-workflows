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
import java.util.function.Supplier;


/*******************************************************************************
 * specialization of {@link ObjectInWorkflowContext} lazily initializes the
 * object any time it is get()ed.  doesn't make sense for al objects in context!
 * but, does make sense, e.g., for list of records to insert.
 *******************************************************************************/
public class LazyInitObjectInWorkflowContext<T extends Serializable> extends ObjectInWorkflowContext<T>
{
   private final Supplier<T> supplier;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public LazyInitObjectInWorkflowContext(WorkflowExecutionContext context, String key, Supplier<T> supplier)
   {
      super(context, key);
      this.supplier = supplier;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public T get()
   {
      T t = super.get();
      if(t == null)
      {
         t = supplier.get();
         set(t);
      }
      return (t);
   }
}
