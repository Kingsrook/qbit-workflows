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


import java.util.List;


/*******************************************************************************
 ** for a stepType with more than one outbound links, e.g., with conditional guards
 ** on them, an instance of this class describes one such link (e.g., "true" labeled
 ** for users as "Then").
 *******************************************************************************/
public class OutboundLinkOption
{
   private String value;
   private String label;

   private List<String> stepTypesToIncludeByDefault;



   /*******************************************************************************
    ** Getter for value
    *******************************************************************************/
   public String getValue()
   {
      return (this.value);
   }



   /*******************************************************************************
    ** Setter for value
    *******************************************************************************/
   public void setValue(String value)
   {
      this.value = value;
   }



   /*******************************************************************************
    ** Fluent setter for value
    *******************************************************************************/
   public OutboundLinkOption withValue(String value)
   {
      this.value = value;
      return (this);
   }



   /*******************************************************************************
    ** Getter for label
    *******************************************************************************/
   public String getLabel()
   {
      return (this.label);
   }



   /*******************************************************************************
    ** Setter for label
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    *******************************************************************************/
   public OutboundLinkOption withLabel(String label)
   {
      this.label = label;
      return (this);
   }


   /*******************************************************************************
    * Getter for stepTypesToIncludeByDefault
    * @see #withStepTypesToIncludeByDefault(List)
    *******************************************************************************/
   public List<String> getStepTypesToIncludeByDefault()
   {
      return (this.stepTypesToIncludeByDefault);
   }



   /*******************************************************************************
    * Setter for stepTypesToIncludeByDefault
    * @see #withStepTypesToIncludeByDefault(List)
    *******************************************************************************/
   public void setStepTypesToIncludeByDefault(List<String> stepTypesToIncludeByDefault)
   {
      this.stepTypesToIncludeByDefault = stepTypesToIncludeByDefault;
   }



   /*******************************************************************************
    * Fluent setter for stepTypesToIncludeByDefault
    *
    * @param stepTypesToIncludeByDefault for the use-case where, you want to have a
    * step type, that when you add it to a workflow, it implicitly contains some
    * sub-steps within one of its outbound links.  e.g., an on-failure branch that
    * calls some exception handling step.  Put the names of the step types to include
    * in this list.  Leave null to omit this behavior.
    * @return this
    *******************************************************************************/
   public OutboundLinkOption withStepTypesToIncludeByDefault(List<String> stepTypesToIncludeByDefault)
   {
      this.stepTypesToIncludeByDefault = stepTypesToIncludeByDefault;
      return (this);
   }


}
