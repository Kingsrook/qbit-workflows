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

package com.kingsrook.qbits.workflows.implementations;


import com.kingsrook.qbits.workflows.implementations.recordworkflows.RecordWorkflowFieldNamePossibleValueSource;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;


/*******************************************************************************
 * specialization of RecordWorkflowFieldNamePossibleValueSource that works for
 * only a single table - as specified in the abstract getTableName method.
 *******************************************************************************/
public abstract class AbstractWorkflowSingleTableFieldNamePossibleValueSource extends RecordWorkflowFieldNamePossibleValueSource
{

   /***************************************************************************
    * specify the name for the possible value source produced by the implementation
    ***************************************************************************/
   public abstract String getName();


   /***************************************************************************
    * specify the tableName used in for the PVS produced by the implementation
    ***************************************************************************/
   public abstract String getTableName();



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public QPossibleValueSource produce(QInstance qInstance) throws QException
   {
      QPossibleValueSource possibleValueSource = super.produce(qInstance);
      possibleValueSource.setName(getName());

      return (possibleValueSource);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   protected final String getTableName(SearchPossibleValueSourceInput input)
   {
      return getTableName();
   }
}
