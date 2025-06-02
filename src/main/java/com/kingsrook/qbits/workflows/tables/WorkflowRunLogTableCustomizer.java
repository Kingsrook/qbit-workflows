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

package com.kingsrook.qbits.workflows.tables;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizerInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.QueryOrGetInputInterface;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.tables.QQQTable;
import com.kingsrook.qqq.backend.core.processes.utils.GeneralProcessUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowRunLogTableCustomizer implements TableCustomizerInterface
{
   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QRecord> postQuery(QueryOrGetInputInterface queryInput, List<QRecord> records) throws QException
   {
      /////////////////////////////////////////////////////////////////////////////////////////
      // note, this is a second copy of this logic (first being in standard process traces). //
      // let the rule of 3 apply if we find ourselves copying it again                       //
      /////////////////////////////////////////////////////////////////////////////////////////
      if(queryInput.getShouldGenerateDisplayValues())
      {
         //////////////////////////////////////////////////////////////////////////////////////////////////
         // for records with a inputRecordQqqTableId - look up that table name, then set a display-value //
         // for the Link type adornment, to the inputRecordId record within that table.                  //
         //////////////////////////////////////////////////////////////////////////////////////////////////
         Set<Serializable> tableIds = records.stream().map(r -> r.getValue("inputRecordQqqTableId")).filter(Objects::nonNull).collect(Collectors.toSet());
         if(!tableIds.isEmpty())
         {
            Map<Serializable, QRecord> tableMap = GeneralProcessUtils.loadTableToMap(QQQTable.TABLE_NAME, "id", new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, tableIds)));

            for(QRecord record : records)
            {
               QRecord qqqTableRecord = tableMap.get(record.getValue("inputRecordQqqTableId"));
               if(qqqTableRecord != null && record.getValue("inputRecordId") != null)
               {
                  record.setDisplayValue("inputRecordId:" + AdornmentType.LinkValues.TO_RECORD_FROM_TABLE_DYNAMIC, qqqTableRecord.getValueString("name"));
               }
            }
         }
      }

      return (records);
   }
}
