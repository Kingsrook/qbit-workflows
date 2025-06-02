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

package com.kingsrook.qbits.workflows.model;


import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;


/*******************************************************************************
 ** QRecord Entity for WorkflowRunLogStep table
 *******************************************************************************/
@QMetaDataProducingEntity(
   produceTableMetaData = true,
   tableMetaDataCustomizer = WorkflowRunLogStep.TableMetaDataCustomizer.class
)
public class WorkflowRunLogStep extends QRecordEntity implements Serializable
{
   public static final String TABLE_NAME = "workflowRunLogStep";



   /***************************************************************************
    **
    ***************************************************************************/
   public static class TableMetaDataCustomizer implements MetaDataCustomizerInterface<QTableMetaData>
   {

      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public QTableMetaData customizeMetaData(QInstance qInstance, QTableMetaData table) throws QException
      {
         table
            .withIcon(new QIcon().withName("reorder"))
            .withRecordLabelFormat("Step - %s (%s)")
            .withRecordLabelFields("seqNo", "workflowRunLogId")
            .withSection(SectionFactory.defaultT1("id", "workflowRunLogId", "seqNo"))
            .withSection(SectionFactory.defaultT2("workflowStepId", "inputDataJson", "outputDataJson"))
            .withSection(SectionFactory.defaultT3("startTimestamp", "endTimestamp"));

         return (table);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class RevisionChildListWidgetCustomizer implements MetaDataCustomizerInterface<QWidgetMetaData>
   {

      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public QWidgetMetaData customizeMetaData(QInstance qInstance, QWidgetMetaData widget) throws QException
      {
         widget.withDefaultValue("orderBy", new ArrayList<>(List.of(new QFilterOrderBy("seqNo", false))));
         return widget;
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Long id;

   @QField(possibleValueSourceName = WorkflowRunLog.TABLE_NAME)
   private Long workflowRunLogId;

   @QField(possibleValueSourceName = WorkflowStep.TABLE_NAME)
   private Integer workflowStepId;

   @QField()
   private Integer seqNo;

   @QField(maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS, label = "Input Data")
   private String inputDataJson;

   @QField(maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS, label = "Output Data")
   private String outputDataJson;

   @QField(isEditable = false)
   private Instant startTimestamp;

   @QField(isEditable = false)
   private Instant endTimestamp;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WorkflowRunLogStep()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WorkflowRunLogStep(QRecord record)
   {
      populateFromQRecord(record);
   }



   /*******************************************************************************
    ** Getter for id
    *******************************************************************************/
   public Long getId()
   {
      return (this.id);
   }



   /*******************************************************************************
    ** Setter for id
    *******************************************************************************/
   public void setId(Long id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Fluent setter for id
    *******************************************************************************/
   public WorkflowRunLogStep withId(Long id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowRunLogId
    *******************************************************************************/
   public Long getWorkflowRunLogId()
   {
      return (this.workflowRunLogId);
   }



   /*******************************************************************************
    ** Setter for workflowRunLogId
    *******************************************************************************/
   public void setWorkflowRunLogId(Long workflowRunLogId)
   {
      this.workflowRunLogId = workflowRunLogId;
   }



   /*******************************************************************************
    ** Fluent setter for workflowRunLogId
    *******************************************************************************/
   public WorkflowRunLogStep withWorkflowRunLogId(Long workflowRunLogId)
   {
      this.workflowRunLogId = workflowRunLogId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowStepId
    *******************************************************************************/
   public Integer getWorkflowStepId()
   {
      return (this.workflowStepId);
   }



   /*******************************************************************************
    ** Setter for workflowStepId
    *******************************************************************************/
   public void setWorkflowStepId(Integer workflowStepId)
   {
      this.workflowStepId = workflowStepId;
   }



   /*******************************************************************************
    ** Fluent setter for workflowStepId
    *******************************************************************************/
   public WorkflowRunLogStep withWorkflowStepId(Integer workflowStepId)
   {
      this.workflowStepId = workflowStepId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for seqNo
    *******************************************************************************/
   public Integer getSeqNo()
   {
      return (this.seqNo);
   }



   /*******************************************************************************
    ** Setter for seqNo
    *******************************************************************************/
   public void setSeqNo(Integer seqNo)
   {
      this.seqNo = seqNo;
   }



   /*******************************************************************************
    ** Fluent setter for seqNo
    *******************************************************************************/
   public WorkflowRunLogStep withSeqNo(Integer seqNo)
   {
      this.seqNo = seqNo;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputDataJson
    *******************************************************************************/
   public String getInputDataJson()
   {
      return (this.inputDataJson);
   }



   /*******************************************************************************
    ** Setter for inputDataJson
    *******************************************************************************/
   public void setInputDataJson(String inputDataJson)
   {
      this.inputDataJson = inputDataJson;
   }



   /*******************************************************************************
    ** Fluent setter for inputDataJson
    *******************************************************************************/
   public WorkflowRunLogStep withInputDataJson(String inputDataJson)
   {
      this.inputDataJson = inputDataJson;
      return (this);
   }



   /*******************************************************************************
    ** Getter for outputDataJson
    *******************************************************************************/
   public String getOutputDataJson()
   {
      return (this.outputDataJson);
   }



   /*******************************************************************************
    ** Setter for outputDataJson
    *******************************************************************************/
   public void setOutputDataJson(String outputDataJson)
   {
      this.outputDataJson = outputDataJson;
   }



   /*******************************************************************************
    ** Fluent setter for outputDataJson
    *******************************************************************************/
   public WorkflowRunLogStep withOutputDataJson(String outputDataJson)
   {
      this.outputDataJson = outputDataJson;
      return (this);
   }



   /*******************************************************************************
    ** Getter for startTimestamp
    *******************************************************************************/
   public Instant getStartTimestamp()
   {
      return (this.startTimestamp);
   }



   /*******************************************************************************
    ** Setter for startTimestamp
    *******************************************************************************/
   public void setStartTimestamp(Instant startTimestamp)
   {
      this.startTimestamp = startTimestamp;
   }



   /*******************************************************************************
    ** Fluent setter for startTimestamp
    *******************************************************************************/
   public WorkflowRunLogStep withStartTimestamp(Instant startTimestamp)
   {
      this.startTimestamp = startTimestamp;
      return (this);
   }



   /*******************************************************************************
    ** Getter for endTimestamp
    *******************************************************************************/
   public Instant getEndTimestamp()
   {
      return (this.endTimestamp);
   }



   /*******************************************************************************
    ** Setter for endTimestamp
    *******************************************************************************/
   public void setEndTimestamp(Instant endTimestamp)
   {
      this.endTimestamp = endTimestamp;
   }



   /*******************************************************************************
    ** Fluent setter for endTimestamp
    *******************************************************************************/
   public WorkflowRunLogStep withEndTimestamp(Instant endTimestamp)
   {
      this.endTimestamp = endTimestamp;
      return (this);
   }

}
