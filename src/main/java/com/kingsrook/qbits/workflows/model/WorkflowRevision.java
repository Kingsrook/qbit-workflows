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


import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QAssociation;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildJoin;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildRecordListWidget;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildTable;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;


/*******************************************************************************
 ** QRecord Entity for WorkflowRevision table
 *******************************************************************************/
@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WorkflowRevision.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WorkflowStep.class,
         joinFieldName = "workflowRevisionId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Steps", enabled = true, maxRows = 250)),
      @ChildTable(
         childTableEntityClass = WorkflowLink.class,
         joinFieldName = "workflowRevisionId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Links", enabled = true, maxRows = 250))
   }
)
public class WorkflowRevision extends QRecordEntity
{
   public static final String TABLE_NAME = "workflowRevision";

   public static final String ASSOCIATION_NAME_WORKFLOW_STEP = "workflowSteps";
   public static final String ASSOCIATION_NAME_WORKFLOW_LINK = "workflowLink";



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
         String stepChildJoinName = QJoinMetaData.makeInferredJoinName(TABLE_NAME, WorkflowStep.TABLE_NAME);
         String linkChildJoinName = QJoinMetaData.makeInferredJoinName(TABLE_NAME, WorkflowLink.TABLE_NAME);

         table
            .withUniqueKey(new UniqueKey("workflowId", "versionNo"))
            .withIcon(new QIcon().withName("schema"))
            .withRecordLabelFormat("%s Version %s")
            .withRecordLabelFields("workflowId", "versionNo")
            .withSection(SectionFactory.defaultT1("id", "workflowId", "versionNo"))
            .withSection(SectionFactory.defaultT2("commitMessage", "author", "startStepNo"))
            .withSection(SectionFactory.customT2("steps", new QIcon("polyline")).withWidgetName(stepChildJoinName))
            .withSection(SectionFactory.customT2("links", new QIcon("link")).withWidgetName(linkChildJoinName))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"))
            .withAssociation(new Association().withName(ASSOCIATION_NAME_WORKFLOW_STEP).withAssociatedTableName(WorkflowStep.TABLE_NAME).withJoinName(stepChildJoinName))
            .withAssociation(new Association().withName(ASSOCIATION_NAME_WORKFLOW_LINK).withAssociatedTableName(WorkflowLink.TABLE_NAME).withJoinName(linkChildJoinName));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(possibleValueSourceName = Workflow.TABLE_NAME)
   private Integer workflowId;

   @QField()
   private Integer versionNo;

   @QField()
   private Integer startStepNo;

   @QField(maxLength = 500, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String commitMessage;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String author;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;

   @QAssociation(name = ASSOCIATION_NAME_WORKFLOW_STEP)
   private List<WorkflowStep> steps;

   @QAssociation(name = ASSOCIATION_NAME_WORKFLOW_LINK)
   private List<WorkflowLink> links;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WorkflowRevision()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WorkflowRevision(QRecord record)
   {
      populateFromQRecord(record);
   }



   /*******************************************************************************
    ** Getter for id
    *******************************************************************************/
   public Integer getId()
   {
      return (this.id);
   }



   /*******************************************************************************
    ** Setter for id
    *******************************************************************************/
   public void setId(Integer id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Fluent setter for id
    *******************************************************************************/
   public WorkflowRevision withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for createDate
    *******************************************************************************/
   public Instant getCreateDate()
   {
      return (this.createDate);
   }



   /*******************************************************************************
    ** Setter for createDate
    *******************************************************************************/
   public void setCreateDate(Instant createDate)
   {
      this.createDate = createDate;
   }



   /*******************************************************************************
    ** Fluent setter for createDate
    *******************************************************************************/
   public WorkflowRevision withCreateDate(Instant createDate)
   {
      this.createDate = createDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for modifyDate
    *******************************************************************************/
   public Instant getModifyDate()
   {
      return (this.modifyDate);
   }



   /*******************************************************************************
    ** Setter for modifyDate
    *******************************************************************************/
   public void setModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
   }



   /*******************************************************************************
    ** Fluent setter for modifyDate
    *******************************************************************************/
   public WorkflowRevision withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for versionNo
    *******************************************************************************/
   public Integer getVersionNo()
   {
      return (this.versionNo);
   }



   /*******************************************************************************
    ** Setter for versionNo
    *******************************************************************************/
   public void setVersionNo(Integer versionNo)
   {
      this.versionNo = versionNo;
   }



   /*******************************************************************************
    ** Fluent setter for versionNo
    *******************************************************************************/
   public WorkflowRevision withVersionNo(Integer versionNo)
   {
      this.versionNo = versionNo;
      return (this);
   }



   /*******************************************************************************
    ** Getter for startStepNo
    *******************************************************************************/
   public Integer getStartStepNo()
   {
      return (this.startStepNo);
   }



   /*******************************************************************************
    ** Setter for startStepNo
    *******************************************************************************/
   public void setStartStepNo(Integer startStepNo)
   {
      this.startStepNo = startStepNo;
   }



   /*******************************************************************************
    ** Fluent setter for startStepNo
    *******************************************************************************/
   public WorkflowRevision withStartStepNo(Integer startStepNo)
   {
      this.startStepNo = startStepNo;
      return (this);
   }



   /*******************************************************************************
    ** Getter for commitMessage
    *******************************************************************************/
   public String getCommitMessage()
   {
      return (this.commitMessage);
   }



   /*******************************************************************************
    ** Setter for commitMessage
    *******************************************************************************/
   public void setCommitMessage(String commitMessage)
   {
      this.commitMessage = commitMessage;
   }



   /*******************************************************************************
    ** Fluent setter for commitMessage
    *******************************************************************************/
   public WorkflowRevision withCommitMessage(String commitMessage)
   {
      this.commitMessage = commitMessage;
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowId
    *******************************************************************************/
   public Integer getWorkflowId()
   {
      return (this.workflowId);
   }



   /*******************************************************************************
    ** Setter for workflowId
    *******************************************************************************/
   public void setWorkflowId(Integer workflowId)
   {
      this.workflowId = workflowId;
   }



   /*******************************************************************************
    ** Fluent setter for workflowId
    *******************************************************************************/
   public WorkflowRevision withWorkflowId(Integer workflowId)
   {
      this.workflowId = workflowId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for steps
    *******************************************************************************/
   public List<WorkflowStep> getSteps()
   {
      return (this.steps);
   }



   /*******************************************************************************
    ** Setter for steps
    *******************************************************************************/
   public void setSteps(List<WorkflowStep> steps)
   {
      this.steps = steps;
   }



   /*******************************************************************************
    ** Fluent setter for steps
    *******************************************************************************/
   public WorkflowRevision withSteps(List<WorkflowStep> steps)
   {
      this.steps = steps;
      return (this);
   }



   /*******************************************************************************
    ** Getter for links
    *******************************************************************************/
   public List<WorkflowLink> getLinks()
   {
      return (this.links);
   }



   /*******************************************************************************
    ** Setter for links
    *******************************************************************************/
   public void setLinks(List<WorkflowLink> links)
   {
      this.links = links;
   }



   /*******************************************************************************
    ** Fluent setter for links
    *******************************************************************************/
   public WorkflowRevision withLinks(List<WorkflowLink> links)
   {
      this.links = links;
      return (this);
   }


   /*******************************************************************************
    ** Getter for author
    *******************************************************************************/
   public String getAuthor()
   {
      return (this.author);
   }



   /*******************************************************************************
    ** Setter for author
    *******************************************************************************/
   public void setAuthor(String author)
   {
      this.author = author;
   }



   /*******************************************************************************
    ** Fluent setter for author
    *******************************************************************************/
   public WorkflowRevision withAuthor(String author)
   {
      this.author = author;
      return (this);
   }


}
