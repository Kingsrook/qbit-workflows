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


import com.kingsrook.qbits.workflows.WorkflowsQBitConfig;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.tracing.WorkflowTracerInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitComponentMetaDataProducer;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;


/*******************************************************************************
 ** Meta Data Producer for RunRecordWorkflow process - the generic process
 ** that can appear on all tables, to run workflows against records.
 *******************************************************************************/
public class RunRecordWorkflowProcessMetaDataProducer extends QBitComponentMetaDataProducer<QProcessMetaData, WorkflowsQBitConfig>
{
   public static final String NAME = "RunRecordWorkflow";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      QProcessMetaData processMetaData = StreamedETLWithFrontendProcess.processMetaDataBuilder()
         .withName(NAME)
         .withLabel("Run Workflow")
         .withIcon(new QIcon().withName("account_tree"))
         .withSupportsFullValidation(false)
         .withExtractStepClass(RunRecordWorkflowExtractStep.class)
         .withTransformStepClass(RunRecordWorkflowTransformStep.class)
         .withLoadStepClass(RunRecordWorkflowLoadStep.class)
         .getProcessMetaData();

      processMetaData.withStep(0, new QFrontendStepMetaData()
         .withName("input")
         .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
         .withFormField(new QFieldMetaData("workflowId", QFieldType.INTEGER).withPossibleValueSourceName(Workflow.TABLE_NAME)
            .withPossibleValueSourceFilter(new QQueryFilter(
               new QFilterCriteria("workflowTypeName", QCriteriaOperator.EQUALS, "RecordWorkflow"),
               new QFilterCriteria("tableName", QCriteriaOperator.EQUALS, "${input.tableName}")
            ))));

      QBackendStepMetaData executeStep = processMetaData.getBackendStep(StreamedETLWithFrontendProcess.STEP_NAME_EXECUTE);
      executeStep.getInputMetaData()
         .withField(new QFieldMetaData("workflowTracerCodeReference", QFieldType.STRING)
            .withDefaultValue(getQBitConfig().getWorkflowTracerCodeReference()))
         .withField(new QFieldMetaData("workflowTracerCodeReference_expectedType", QFieldType.STRING)
            .withDefaultValue(WorkflowTracerInterface.class.getName()));

      return (processMetaData);
   }

}
