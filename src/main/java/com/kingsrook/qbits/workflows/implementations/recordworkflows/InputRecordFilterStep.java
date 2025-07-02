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
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.definition.OutboundLinkMode;
import com.kingsrook.qbits.workflows.definition.OutboundLinkOption;
import com.kingsrook.qbits.workflows.definition.WorkflowStepType;
import com.kingsrook.qbits.workflows.execution.WorkflowExecutionContext;
import com.kingsrook.qbits.workflows.execution.WorkflowStepExecutorInterface;
import com.kingsrook.qbits.workflows.execution.WorkflowStepOutput;
import com.kingsrook.qbits.workflows.implementations.WorkflowStepUtils;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.api.actions.GetTableApiFieldsAction;
import com.kingsrook.qqq.api.utils.ApiQueryFilterUtils;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import org.json.JSONObject;


/*******************************************************************************
 ** workflow step that compares the input record to a filter
 *******************************************************************************/
public class InputRecordFilterStep extends WorkflowStepType implements WorkflowStepExecutorInterface
{
   private static final QLogger LOG = QLogger.getLogger(InputRecordFilterStep.class);

   public static final String NAME = "inputRecordFilter";



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public InputRecordFilterStep()
   {
      this.withName(NAME)
         .withOutboundLinkMode(OutboundLinkMode.TWO)
         .withOutboundLinkOptions(List.of(
            new OutboundLinkOption().withValue("true").withLabel("Then"),
            new OutboundLinkOption().withValue("false").withLabel("Otherwise")
         ))
         .withLabel("If Record Matches Filter")
         .withIconUrl("data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0Ij4KICAgPHBhdGggZD0iTTQuMjUgNS42MUM2LjI3IDguMiAxMCAxMyAxMCAxM3Y2YzAgLjU1LjQ1IDEgMSAxaDJjLjU1IDAgMS0uNDUgMS0xdi02czMuNzItNC44IDUuNzQtNy4zOWMuNTEtLjY2LjA0LTEuNjEtLjc5LTEuNjFINS4wNGMtLjgzIDAtMS4zLjk1LS43OSAxLjYxeiIvPgo8L3N2Zz4K")
         .withExecutor(new QCodeReference(getClass()))
         .withDescription("Choose a different set of actions based on if the record being processed matches a filter")
         .withInputWidgetNames(List.of(RecordWorkflowInputRecordFilterWidget.NAME));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public String getDynamicStepSummary(Integer workflowId, JSONObject values) throws QException
   {
      QRecord workflowRecord = GetAction.execute(Workflow.TABLE_NAME, workflowId);
      String  tableName      = workflowRecord.getValueString("tableName");
      return (RecordWorkflowUtils.getDynamicStepSummaryForFilter(tableName, values));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public WorkflowStepOutput execute(WorkflowStep step, Map<String, Serializable> inputValues, WorkflowExecutionContext context) throws QException
   {
      QRecord record = (QRecord) context.getValues().get("record");
      if(record == null)
      {
         throw (new QException("Missing record input in InputRecordFilterStep"));
      }

      QQueryFilter inputFilter = RecordWorkflowUtils.getFilterFromInput(inputValues);
      if(inputFilter == null)
      {
         throw (new QException("Missing filter input in InputRecordFilterStep"));
      }

      Integer          count;
      QFilterCriteria  idEqualsCriteria = new QFilterCriteria("id", QCriteriaOperator.EQUALS, record.getValueInteger("id"));
      WorkflowRevision workflowRevision = context.getWorkflowRevision();

      if(WorkflowStepUtils.useApi(workflowRevision))
      {
         count = countViaApi(context, inputFilter, idEqualsCriteria);
      }
      else
      {
         QQueryFilter actualFilter = new QQueryFilter()
            .withCriteria(idEqualsCriteria)
            .withSubFilter(inputFilter);

         count = new CountAction().execute(new CountInput(context.getWorkflow().getTableName())
            .withTransaction(context.getTransaction())
            .withFilter(actualFilter)).getCount();
      }

      if(count == null || count == 0)
      {
         return (new WorkflowStepOutput(false));
      }

      return (new WorkflowStepOutput(true));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private Integer countViaApi(WorkflowExecutionContext context, QQueryFilter inputFilter, QFilterCriteria idEqualsCriteria) throws QException
   {
      WorkflowRevision            workflowRevision   = context.getWorkflowRevision();
      Workflow                    workflow           = context.getWorkflow();
      Map<String, QFieldMetaData> tableApiFields     = GetTableApiFieldsAction.getTableApiFieldMap(new GetTableApiFieldsAction.ApiNameVersionAndTableName(workflowRevision.getApiName(), workflowRevision.getApiVersion(), workflow.getTableName()));
      List<String>                badRequestMessages = new ArrayList<>();

      CountInput countInput = new CountInput(context.getWorkflow().getTableName())
         .withTransaction(context.getTransaction());

      ApiQueryFilterUtils.manageCriteriaFields(inputFilter, tableApiFields, badRequestMessages, workflowRevision.getApiName(), workflowRevision.getApiVersion(), countInput);

      QQueryFilter actualFilter = new QQueryFilter()
         .withCriteria(idEqualsCriteria)
         .withSubFilter(inputFilter);

      countInput.setFilter(actualFilter);

      Integer count = new CountAction().execute(countInput).getCount();

      return count;
   }

   //////////////////////////////////////////////////////////////////////////////////////////////////////
   // in some future, we might want to do such a filter in-memory.  This code was WIP to support that. //
   //////////////////////////////////////////////////////////////////////////////////////////////////////
   // /***************************************************************************
   //  **
   //  ***************************************************************************/
   // public Serializable executeInMemory(WorkflowStep step, Map<String, Serializable> inputValues, WorkflowExecutionContext context) throws QException
   // {
   //    QRecord record = (QRecord) context.getValues().get("record");
   //    if(record == null)
   //    {
   //       throw (new QException("Missing record input in InputRecordFilterStep"));
   //    }

   //    QQueryFilter filter = RecordWorkflowUtils.getFilterFromInput(inputValues);
   //    if(filter == null)
   //    {
   //       throw (new QException("Missing filter input in InputRecordFilterStep"));
   //    }

   //    ArrayList<? extends QRecord> joinedRecordsForFilter = buildJoinedRecordsForFiltering(record, filter);
   //    for(QRecord recordWithJoins : joinedRecordsForFilter)
   //    {
   //       if(BackendQueryFilterUtils.doesRecordMatch(filter, recordWithJoins))
   //       {
   //          return (true);
   //       }
   //    }

   //    return (false);
   // }

   // /***************************************************************************
   //  ** given an order, and some -toOne joined records (e.g., order optimization plan
   //  ** or order-Carton, add the order's associated lines & extrinsics to make a list
   //  ** of QRecordWithJoinedRecords entities - that can be used for in-memory join filtering.
   //  ***************************************************************************/
   // private static ArrayList<? extends QRecord> buildJoinedRecordsForFiltering(QRecord mainRecord, QQueryFilter filter) throws QException
   // {
   //    Set<String> joinTablesInFilter = findJoinTablesInFilter(QContext.getQInstance().getTable(mainRecord.getTableName()), filter);
   //    if(joinTablesInFilter.isEmpty())
   //    {
   //       ArrayList<QRecord> rs = new ArrayList<>();
   //       rs.add(mainRecord);
   //       return (rs);
   //    }
   //    else
   //    {
   //       QTableMetaData                    mainTable = QContext.getQInstance().getTable(mainRecord.getTableName());
   //       List<Pair<String, List<QRecord>>> toJoin    = new ArrayList<>();
   //       for(String joinTableName : joinTablesInFilter)
   //       {
   //          try
   //          {
   //             ExposedJoin   exposedJoin  = findExposedJoin(mainTable, joinTableName);
   //             String        lastJoinName = exposedJoin.getJoinPath().get(exposedJoin.getJoinPath().size() - 1);
   //             QJoinMetaData join         = QContext.getQInstance().getJoin(lastJoinName); // todo actually need to walk the path
   //             boolean       isMany       = join.getType().equals(JoinType.ONE_TO_MANY) || join.getType().equals(JoinType.MANY_TO_MANY);

   //             QueryInput queryInput = new QueryInput();
   //             queryInput.setTableName(joinTableName);
   //             queryInput.setFilter(new QQueryFilter(new QFilterCriteria("orderId", QCriteriaOperator.EQUALS, mainRecord.getValueInteger("id")))); // todo totes wrong
   //             QueryOutput queryOutput = new QueryAction().execute(queryInput);

   //             List<QRecord> joinRecords = queryOutput.getRecords();
   //             toJoin.add(new Pair<>(exposedJoin.getJoinTable(), joinRecords.isEmpty() ? List.of(new QRecord()) : joinRecords));
   //          }
   //          catch(Exception e)
   //          {
   //             // todo this was incomplete
   //             LOG.warn("Error building joined records for filtering - skipping table, but continuing...", e);
   //          }
   //       }

   //       QRecordWithJoinedRecords recordWithJoinedRecords = new QRecordWithJoinedRecords(mainRecord);
   //       recordWithJoinedRecords.setTableName(mainRecord.getTableName());
   //       return (buildCrossProduct(new ArrayList<>(List.of(recordWithJoinedRecords)), toJoin));
   //    }
   // }

   // /***************************************************************************
   //  **
   //  ***************************************************************************/
   // private static ArrayList<QRecordWithJoinedRecords> buildCrossProduct(ArrayList<QRecordWithJoinedRecords> recordWithJoinedRecords, List<Pair<String, List<QRecord>>> toJoin)
   // {
   //    if(toJoin.isEmpty())
   //    {
   //       return (recordWithJoinedRecords);
   //    }

   //    Pair<String, List<QRecord>> firstPair     = toJoin.get(0);
   //    String                      joinTableName = firstPair.getA();

   //    ArrayList<QRecordWithJoinedRecords> nextList = new ArrayList<>();

   //    for(QRecordWithJoinedRecords recordWithJoinedRecord : recordWithJoinedRecords)
   //    {
   //       for(QRecord joinRecord : firstPair.getB())
   //       {
   //          QRecordWithJoinedRecords next = new QRecordWithJoinedRecords(recordWithJoinedRecord);
   //          next.addJoinedRecordValues(joinTableName, joinRecord);
   //          nextList.add(next);
   //       }
   //    }

   //    return buildCrossProduct(nextList, toJoin.subList(1, toJoin.size()));
   // }

   // /***************************************************************************
   //  **
   //  ***************************************************************************/
   // private static ExposedJoin findExposedJoin(QTableMetaData mainTable, String joinTableName) throws QException
   // {
   //    for(ExposedJoin exposedJoin : CollectionUtils.nonNullList(mainTable.getExposedJoins()))
   //    {
   //       if(exposedJoin.getJoinTable().equals(joinTableName))
   //       {
   //          return (exposedJoin);
   //       }
   //    }

   //    throw (new QException("Could not find exposed join from " + mainTable.getName() + " to " + joinTableName));
   // }

   // /***************************************************************************
   //  *
   //  ***************************************************************************/
   // private static Set<String> findJoinTablesInFilter(QTableMetaData mainTable, QQueryFilter filter) throws QException
   // {
   //    Set<String> rs = new HashSet<>();

   //    if(filter != null)
   //    {
   //       for(QFilterCriteria criteria : CollectionUtils.nonNullList(filter.getCriteria()))
   //       {
   //          FieldAndJoinTable fieldAndJoinTable = FieldAndJoinTable.get(mainTable, criteria.getFieldName());
   //          if(fieldAndJoinTable.joinTable() != null && !fieldAndJoinTable.joinTable().equals(mainTable))
   //          {
   //             rs.add(fieldAndJoinTable.joinTable().getName());
   //          }
   //       }

   //       for(QQueryFilter subFilter : CollectionUtils.nonNullList(filter.getSubFilters()))
   //       {
   //          rs.addAll(findJoinTablesInFilter(mainTable, subFilter));
   //       }
   //    }

   //    return (rs);
   // }

}
