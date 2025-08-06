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

package com.kingsrook.qbits.workflows.metadata;


import java.util.Collections;
import java.util.List;
import com.kingsrook.qbits.workflows.WorkflowsQBitConfig;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowTestAssertion;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaDataContainer;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.FilterAndColumnsSetupData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 * Widget for setting up a filter on the workflow's table, to be used in a
 * WorkflowTestAssertion.
 *******************************************************************************/
public class WorkflowTestAssertionFilterWidget extends AbstractWidgetRenderer implements MetaDataProducerInterface<QWidgetMetaData>
{
   public static final String NAME = "WorkflowTestAssertionFilterWidget";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QWidgetMetaData produce(QInstance qInstance) throws QException
   {
      QWidgetMetaData widget = new QWidgetMetaData()
         .withName(NAME)
         .withLabel("Filter")
         .withIsCard(true)
         .withType(WidgetType.FILTER_AND_COLUMNS_SETUP.getType())
         .withCodeReference(new QCodeReference(getClass()));

      return (widget);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      FilterAndColumnsSetupData widgetData = new FilterAndColumnsSetupData(null, false, true, Collections.emptyList());
      widgetData.setHidePreview(true);
      widgetData.setHideSortBy(true);

      QRecord workflowRecord = null;

      /////////////////////////////////////////////////////////////////////////////////////////////
      // we'll be called in 2 modes:                                                             //
      // from a create-new, after the user selected a scenario, we'll have a scenarioId as input //
      // or from an edit, we'll have 'id', which is the test-assertion's id.                     //
      // from either mode - we need to get to a workflow record, to know what table we're for.   //
      /////////////////////////////////////////////////////////////////////////////////////////////
      Integer workflowTestAssertionId = ValueUtils.getValueAsInteger(input.getQueryParams().get("id"));
      Integer workflowTestScenarioId  = ValueUtils.getValueAsInteger(input.getQueryParams().get("workflowTestScenarioId"));
      if(workflowTestAssertionId == null)
      {
         if(workflowTestScenarioId != null)
         {
            QRecord workflowTestScenario = GetAction.execute(WorkflowTestScenario.TABLE_NAME, workflowTestScenarioId);
            if(workflowTestScenario != null)
            {
               Integer workflowId = workflowTestScenario.toEntity(WorkflowTestScenario.class).getWorkflowId();
               workflowRecord = GetAction.execute(Workflow.TABLE_NAME, workflowId);
            }
         }
      }
      else
      {
         List<QRecord> records = new QueryAction().execute(new QueryInput(Workflow.TABLE_NAME)
               .withFilter(new QQueryFilter(new QFilterCriteria(WorkflowTestAssertion.TABLE_NAME + ".id", QCriteriaOperator.EQUALS, workflowTestAssertionId)))
               .withQueryJoin(new QueryJoin(WorkflowTestScenario.TABLE_NAME).withBaseTableOrAlias(Workflow.TABLE_NAME))
               .withQueryJoin(new QueryJoin(WorkflowTestAssertion.TABLE_NAME).withBaseTableOrAlias(WorkflowTestScenario.TABLE_NAME))
            )
            .getRecords();

         if(!records.isEmpty())
         {
            workflowRecord = records.get(0);
         }
      }

      if(workflowRecord != null)
      {
         widgetData.setTableName(workflowRecord.getValueString("tableName"));

         if(WorkflowsQBitConfig.isApiModuleAvailableAndDoesQBitIncludeApiVersions())
         {
            widgetData.setIsApiVersioned(true);

            Integer currentRevisionId = workflowRecord.getValueInteger("currentWorkflowRevisionId");
            if(currentRevisionId != null)
            {
               QRecord workflowRevision = new GetAction().executeForRecord(new GetInput(WorkflowRevision.TABLE_NAME).withPrimaryKey(currentRevisionId));
               if(workflowRevision != null)
               {
                  String apiName = workflowRevision.getValueString("apiName");
                  if(StringUtils.hasContent(apiName))
                  {
                     widgetData.setApiName(apiName);
                     ApiInstanceMetaDataContainer apiInstanceMetaDataContainer = ApiInstanceMetaDataContainer.of(QContext.getQInstance());
                     if(apiInstanceMetaDataContainer != null)
                     {
                        ApiInstanceMetaData apiInstanceMetaData = apiInstanceMetaDataContainer.getApis().get(apiName);
                        if(apiInstanceMetaData != null)
                        {
                           widgetData.setApiPath(apiInstanceMetaData.getPath().replaceFirst("^/+", "").replaceFirst("/+$", ""));
                        }
                     }
                  }

                  String apiVersion = workflowRevision.getValueString("apiVersion");
                  if(StringUtils.hasContent(apiVersion))
                  {
                     widgetData.setApiVersion(apiVersion);
                  }
               }
            }
         }
      }

      return new RenderWidgetOutput(widgetData);
   }

}
