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

package com.kingsrook.qbits.workflows.tracing;


import java.io.Serializable;
import com.kingsrook.qbits.workflows.model.WorkflowRunLog;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;


/*******************************************************************************
 ** default implementation of a workflow run log tracer, that inserts into the
 ** workflowRunLog and workflowRunLogStep tables.
 *******************************************************************************/
public class WorkflowRunLogTracer implements WorkflowTracerInterface
{
   private static final QLogger LOG = QLogger.getLogger(WorkflowRunLogTracer.class);



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public Serializable handleWorkflowFinish(WorkflowRunLog workflowRunLog)
   {
      try
      {
         InsertOutput insertOutput = new InsertAction().execute(new InsertInput(WorkflowRunLog.TABLE_NAME).withRecordEntity(workflowRunLog));
         Long         insertedId   = insertOutput.getRecords().get(0).getValueLong("id");
         workflowRunLog.setId(insertedId);
         return (insertedId);
      }
      catch(Exception e)
      {
         LOG.warn("Error inserting workflow run log", e);
         return (null);
      }
   }

}
