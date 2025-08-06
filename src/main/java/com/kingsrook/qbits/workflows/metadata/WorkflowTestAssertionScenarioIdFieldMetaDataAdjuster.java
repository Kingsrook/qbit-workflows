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


import java.util.Set;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.FormAdjusterInput;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.FormAdjusterInterface;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.FormAdjusterOutput;


/***************************************************************************
 * material-dashboard field-meta-data-adjuster to clear values that depend
 * on workflowTestScenarioId.
 ***************************************************************************/
public class WorkflowTestAssertionScenarioIdFieldMetaDataAdjuster implements FormAdjusterInterface
{

   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public FormAdjusterOutput execute(FormAdjusterInput input) throws QException
   {
      ///////////////////////////////////////////////////////////////////////////////////////////////
      // note, clearing variableName seems to not be working during original deployment of this... //
      ///////////////////////////////////////////////////////////////////////////////////////////////
      return (new FormAdjusterOutput()
         .withFieldsToClear(Set.of("variableName", "expectedValue", "queryFilterJson")));
   }

}
