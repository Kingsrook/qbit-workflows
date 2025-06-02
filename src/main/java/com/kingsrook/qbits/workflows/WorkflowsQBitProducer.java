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

package com.kingsrook.qbits.workflows;


import java.util.List;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowRunLog;
import com.kingsrook.qbits.workflows.model.WorkflowRunLogStep;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qbits.workflows.processes.StoreNewWorkflowRevisionProcess;
import com.kingsrook.qbits.workflows.triggers.TableTriggerCustomizerForWorkflows;
import com.kingsrook.qbits.workflows.triggers.WorkflowCustomTableTriggerRecordAutomationHandler;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.automation.TableTrigger;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerHelper;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppSection;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitProducer;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowsQBitProducer implements QBitProducer
{
   private WorkflowsQBitConfig workflowsQBitConfig;



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void produce(QInstance qInstance, String namespace) throws QException
   {
      QBitMetaData qBitMetaData = new QBitMetaData()
         .withGroupId("com.kingsrook.qbits")
         .withArtifactId("workflows")
         .withVersion("0.1.0")
         .withNamespace(namespace)
         .withConfig(workflowsQBitConfig);
      qInstance.addQBit(qBitMetaData);

      List<MetaDataProducerInterface<?>> producers = MetaDataProducerHelper.findProducers(getClass().getPackageName());
      finishProducing(qInstance, qBitMetaData, workflowsQBitConfig, producers);

      if(!workflowsQBitConfig.getIncludeApiVersions())
      {
         QTableMetaData workflowRevisionTable = qInstance.getTable(WorkflowRevision.TABLE_NAME);
         workflowRevisionTable.getFields().remove("apiVersion");
         workflowRevisionTable.getFields().remove("apiName");
         workflowRevisionTable.getSections().removeIf(s -> "api".equals(s.getName()));
         workflowRevisionTable.getSections().stream()
            .filter(s -> SectionFactory.getDefaultT2name().equals(s.getName()))
            .forEach(s -> s.setGridColumns(12));

         QProcessMetaData storeRevisionProcess = qInstance.getProcess(StoreNewWorkflowRevisionProcess.NAME);
         List<QFieldMetaData> storeProcessFieldList = storeRevisionProcess.getBackendStep("execute")
            .getInputMetaData()
            .getFieldList();
         storeProcessFieldList.removeIf(f -> f.getName().equals("apiVersion") || f.getName().equals("apiName"));
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public static QAppSection getAppSection(QInstance qInstance)
   {
      return (new QAppSection().withName("workflows")
         .withTable(Workflow.TABLE_NAME)
         .withTable(WorkflowRevision.TABLE_NAME)
         .withTable(WorkflowStep.TABLE_NAME)
         .withTable(WorkflowLink.TABLE_NAME)
         .withTable(WorkflowRunLog.TABLE_NAME)
         .withTable(WorkflowRunLogStep.TABLE_NAME));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void addWorkflowsToTableTriggers(QInstance qInstance) throws QException
   {
      QTableMetaData tableTriggerTable = qInstance.getTable(TableTrigger.TABLE_NAME);
      if(tableTriggerTable == null)
      {
         LOG.warn("Attempted to add workflows to the TableTriggers table, but that table is not defined in the QInstance (at least not at this point in time.");
         return;
      }

      ////////////////////////////////////////////////////////////////////////////////////
      // have the workflows custom-table-trigger handler register itself with the class //
      // that'll call it to run when an automation tries to run a workflow trigger      //
      ////////////////////////////////////////////////////////////////////////////////////
      WorkflowCustomTableTriggerRecordAutomationHandler.register();

      ////////////////////////////////////////////////
      // add workflowId field to tableTrigger table //
      ////////////////////////////////////////////////
      tableTriggerTable.withField(new QFieldMetaData("workflowId", QFieldType.INTEGER)
         .withBackendName("workflow_id")
         .withPossibleValueSourceName(Workflow.TABLE_NAME)
         .withPossibleValueSourceFilter(new QQueryFilter(
            new QFilterCriteria("tableName", QCriteriaOperator.EQUALS, "${input.tableName}"))));

      //////////////////////////////////////////////////////////////////////////////
      // place workflowId field after scriptId in whatever section scriptId is in //
      //////////////////////////////////////////////////////////////////////////////
      for(QFieldSection section : tableTriggerTable.getSections())
      {
         int scriptIdIndex = section.getFieldNames().indexOf("scriptId");
         if(scriptIdIndex > -1)
         {
            section.getFieldNames().add(scriptIdIndex + 1, "workflowId");
         }
      }

      //////////////////////////////////////////////////////////////////////////
      // if you have workflows, then you don't necessarily need a script - so //
      // change this field to not be required                                 //
      //////////////////////////////////////////////////////////////////////////
      tableTriggerTable.getField("scriptId").setIsRequired(false);

      ////////////////////////////////////////////////////////////////////////////////////////////
      // but - do add a pre insert/updated validator, to make sure one or the other is selected //
      ////////////////////////////////////////////////////////////////////////////////////////////
      tableTriggerTable.withCustomizer(TableCustomizers.PRE_INSERT_RECORD, new QCodeReference(TableTriggerCustomizerForWorkflows.class));
      tableTriggerTable.withCustomizer(TableCustomizers.PRE_UPDATE_RECORD, new QCodeReference(TableTriggerCustomizerForWorkflows.class));
   }



   /*******************************************************************************
    ** Getter for workflowsQBitConfig
    *******************************************************************************/
   public WorkflowsQBitConfig getWorkflowsQBitConfig()
   {
      return (this.workflowsQBitConfig);
   }



   /*******************************************************************************
    ** Setter for workflowsQBitConfig
    *******************************************************************************/
   public void setWorkflowsQBitConfig(WorkflowsQBitConfig workflowsQBitConfig)
   {
      this.workflowsQBitConfig = workflowsQBitConfig;
   }



   /*******************************************************************************
    ** Fluent setter for workflowsQBitConfig
    *******************************************************************************/
   public WorkflowsQBitProducer withWorkflowsQBitConfig(WorkflowsQBitConfig workflowsQBitConfig)
   {
      this.workflowsQBitConfig = workflowsQBitConfig;
      return (this);
   }

}
