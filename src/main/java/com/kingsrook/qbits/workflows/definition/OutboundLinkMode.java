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

package com.kingsrook.qbits.workflows.definition;


/***************************************************************************
 ** How many links can come out of a step-type.
 ** ZERO means a terminal step - nothing out of it.
 ** ONE is a regular statement, that just flows to its next.
 ** CONTAINER is itself a no-op, but it wraps (for UI reasons) a sequence
 ** TWO is for an if/else
 ** Unclear exactly how we'd do variable number of outbounds (e.g., a switch)
 ***************************************************************************/
public enum OutboundLinkMode
{
   ZERO,
   ONE,
   CONTAINER,
   TWO,
}
