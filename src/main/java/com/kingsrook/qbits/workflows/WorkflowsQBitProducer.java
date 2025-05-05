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
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerHelper;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppSection;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitProducer;


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
         .withTable(WorkflowLink.TABLE_NAME));
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
