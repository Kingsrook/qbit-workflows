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
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 * specialization of ObjectInWorkflowContext, for QRecordEntities - with
 * conversion to and from QRecord provided here.
 *******************************************************************************/
public class QRecordEntityInWorkflowContext<T extends QRecordEntity & Serializable> extends ObjectInWorkflowContext<T>
{
   private final Class<T> entityClass;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QRecordEntityInWorkflowContext(WorkflowExecutionContext context, String key, Class<T> entityClass)
   {
      super(context, key);
      this.entityClass = entityClass;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public QRecord getRecord()
   {
      return get().toQRecord();
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void set(QRecord record)
   {
      try
      {
         super.set(QRecordEntity.fromQRecord(entityClass, record));
      }
      catch(QException e)
      {
         throw new QRuntimeException("Error setting RecordEntityInContext", e);
      }
   }
}
