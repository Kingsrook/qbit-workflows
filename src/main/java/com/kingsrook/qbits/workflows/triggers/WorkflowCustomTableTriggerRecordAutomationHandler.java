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

package com.kingsrook.qbits.workflows.triggers;


import com.kingsrook.qbits.workflows.implementations.recordworkflows.RunRecordWorkflowProcessMetaDataProducer;
import com.kingsrook.qqq.backend.core.actions.automation.CustomTableTriggerRecordAutomationHandler;
import com.kingsrook.qqq.backend.core.actions.automation.RunCustomTableTriggerRecordAutomationHandler;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.automation.RecordAutomationInput;
import com.kingsrook.qqq.backend.core.model.automation.TableTrigger;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** to allow qqq, which doesn't know about workflows, to run a workflow in
 ** response to a table record automation, this class implements the
 ** CustomTableTriggerRecordAutomationHandler, which is how it'll get called
 ** by QQQ.
 *******************************************************************************/
public class WorkflowCustomTableTriggerRecordAutomationHandler implements CustomTableTriggerRecordAutomationHandler
{

   /***************************************************************************
    ** make the RunCustomTableTriggerRecordAutomationHandler aware of this
    ** class.
    ***************************************************************************/
   public static void register()
   {
      RunCustomTableTriggerRecordAutomationHandler.registerHandler(WorkflowCustomTableTriggerRecordAutomationHandler.class.getName(), new QCodeReference(WorkflowCustomTableTriggerRecordAutomationHandler.class));
   }



   /***************************************************************************
    ** decide if this class recognizes the data in the recordAutomationInput
    ** (e.g., if it has a workflowId), and if we should handle the request or not.
    ***************************************************************************/
   @Override
   public boolean handlesThisInput(RecordAutomationInput recordAutomationInput) throws QException
   {
      Integer workflowId = getWorkflowId(recordAutomationInput);
      if(workflowId != null)
      {
         return (true);
      }
      return (false);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static Integer getWorkflowId(RecordAutomationInput recordAutomationInput) throws QException
   {
      Integer workflowId     = null;
      Integer tableTriggerId = ValueUtils.getValueAsInteger(recordAutomationInput.getAction().getValues().get("tableTriggerId"));
      if(tableTriggerId != null)
      {
         QRecord tableTriggerRecord = GetAction.execute(TableTrigger.TABLE_NAME, tableTriggerId);
         if(tableTriggerRecord != null)
         {
            workflowId = tableTriggerRecord.getValueInteger("workflowId");
         }
      }
      return workflowId;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void execute(RecordAutomationInput recordAutomationInput) throws QException
   {
      String  tableName  = recordAutomationInput.getTableName();
      Integer workflowId = getWorkflowId(recordAutomationInput);

      for(QRecord record : recordAutomationInput.getRecordList())
      {
         RunProcessInput input = new RunProcessInput();
         input.setProcessName(RunRecordWorkflowProcessMetaDataProducer.NAME);
         input.setCallback(QProcessCallbackFactory.forRecord(record));
         input.addValue("workflowId", workflowId);
         input.addValue("tableName", tableName);
         input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
         RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);
      }
   }
}
