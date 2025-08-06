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

package com.kingsrook.qbits.workflows.implementations.recordworkflows;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.gson.reflect.TypeToken;
import com.kingsrook.qbits.workflows.execution.ObjectInWorkflowContext;
import com.kingsrook.qbits.workflows.execution.WorkflowExecutionContext;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 *
 *******************************************************************************/
public class RecordWorkflowContext extends WorkflowExecutionContext
{
   public final ObjectInWorkflowContext<QRecord> record = new ObjectInWorkflowContext<>(this, "record");

   public final ObjectInWorkflowContext<Boolean> doesRecordNeedUpdated = new ObjectInWorkflowContext<>(this, "doesRecordNeedUpdated", false);

   public final ObjectInWorkflowContext<HashMap<String, ArrayList<QRecord>>>    recordsToInsert     = new ObjectInWorkflowContext<>(this, "recordsToInsert", new HashMap<>());
   public final ObjectInWorkflowContext<HashMap<String, HashSet<Serializable>>> primaryKeysToDelete = new ObjectInWorkflowContext<>(this, "primaryKeysToDelete", new HashMap<>());

   ///////////////////////////////////////////////////////////////////////////////////////////
   // records already stored in the backend that are joined with the main record.           //
   // should only be accessed via the getRelatedRecords method, which lazy inits (by query) //
   ///////////////////////////////////////////////////////////////////////////////////////////
   private final ObjectInWorkflowContext<HashMap<JoinKey, ArrayList<QRecord>>> joinRecords = new ObjectInWorkflowContext<>(this, "joinRecords", new HashMap<>());



   private record JoinKey(String joinName, ArrayList<Serializable> joinValues)
   {

   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void addRecordToInsert(String tableName, QRecordEntity entity)
   {
      addRecordsToInsert(tableName, List.of(entity.toQRecord()));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void addRecordToInsert(String tableName, QRecord record)
   {
      addRecordsToInsert(tableName, List.of(record));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void addRecordsToInsert(String tableName, List<QRecord> records)
   {
      HashMap<String, ArrayList<QRecord>> map = recordsToInsert.get();
      map.computeIfAbsent(tableName, k -> new ArrayList<>()).addAll(records);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public List<QRecord> getRecordsToInsert(String tableName)
   {
      HashMap<String, ArrayList<QRecord>> map = recordsToInsert.get();
      return (map.computeIfAbsent(tableName, k -> new ArrayList<>()));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void addPrimaryKeyToDelete(String tableName, Serializable primaryKey)
   {
      addPrimaryKeysToDelete(tableName, List.of(primaryKey));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void addPrimaryKeysToDelete(String tableName, Collection<Serializable> primaryKeys)
   {
      HashMap<String, HashSet<Serializable>> map = primaryKeysToDelete.get();
      map.computeIfAbsent(tableName, k -> new HashSet<>()).addAll(primaryKeys);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public Set<Serializable> getPrimaryKeysToDelete(String tableName)
   {
      HashMap<String, HashSet<Serializable>> map = primaryKeysToDelete.get();
      return (map.computeIfAbsent(tableName, k -> new HashSet<>()));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public List<QRecord> getJoinRecords(QueryJoin queryJoin) throws QException
   {
      JoinRecordsKeyConstruction result = getJoinRecordsKeyConstruction(queryJoin, record.get());
      if(joinRecords.get().get(result.key()) == null)
      {
         ArrayList<QRecord> records = CollectionUtils.useOrWrap(result.makeEmpty() ? Collections.emptyList() : QueryAction.execute(queryJoin.getJoinTable(), result.filter()), new TypeToken<>() {});
         joinRecords.get().put(result.key(), records);
      }
      return joinRecords.get().get(result.key());
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private JoinRecordsKeyConstruction getJoinRecordsKeyConstruction(QueryJoin queryJoin, QRecord mainRecord)
   {
      ArrayList<Serializable> joinValues    = new ArrayList<>();
      JoinKey                 key           = new JoinKey(queryJoin.getJoinMetaData().getName(), joinValues);
      String                  baseTableName = getWorkflow().getTableName();

      QQueryFilter filter    = new QQueryFilter();
      boolean      makeEmpty = false;

      for(JoinOn joinOn : queryJoin.getJoinMetaData().getJoinOns())
      {
         Serializable mainTableValue;
         String       joinTableField;
         if(queryJoin.getJoinMetaData().getLeftTable().equals(baseTableName))
         {
            mainTableValue = mainRecord.getValue(joinOn.getLeftField());
            joinTableField = joinOn.getRightField();
         }
         else
         {
            mainTableValue = mainRecord.getValue(joinOn.getRightField());
            joinTableField = joinOn.getLeftField();
         }

         key.joinValues.add(mainTableValue);

         if(mainTableValue == null)
         {
            // throw (new QException("Missing join value in " + baseTableName + " getting related " + tableName + " records"));
            makeEmpty = true;
         }

         filter.addCriteria(new QFilterCriteria(joinTableField, QCriteriaOperator.EQUALS, mainTableValue));
      }
      JoinRecordsKeyConstruction result = new JoinRecordsKeyConstruction(key, filter, makeEmpty);
      return result;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private record JoinRecordsKeyConstruction(JoinKey key, QQueryFilter filter, boolean makeEmpty)
   {

   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void setJoinRecords(QueryJoin queryJoin, QRecord mainRecord, List<QRecord> records)
   {
      ArrayList<QRecord>         recordArrayList = CollectionUtils.useOrWrap(records, new TypeToken<>() {});
      JoinRecordsKeyConstruction result          = getJoinRecordsKeyConstruction(queryJoin, mainRecord);
      joinRecords.get().put(result.key(), recordArrayList);
   }
}
