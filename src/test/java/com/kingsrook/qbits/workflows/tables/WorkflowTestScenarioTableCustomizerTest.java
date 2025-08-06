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

package com.kingsrook.qbits.workflows.tables;


import java.util.List;
import java.util.Optional;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qbits.workflows.WorkflowsTestDataSource;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.lambdas.UnsafeFunction;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for WorkflowTestScenarioTableCustomizer 
 *******************************************************************************/
class WorkflowTestScenarioTableCustomizerTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPreInsert() throws QException
   {
      assertSuccess(1, null, null, null);
      assertSuccess(1, "", "", "");
      assertSuccess(null, "apiName", "v1", "{}");

      assertError("Either Source Record Id or all API fields must be given", null, null, null, null);

      assertError("no API fields may be given", 1, "apiName", null, null);
      assertError("no API fields may be given", 1, null, "v1", null);
      assertError("no API fields may be given", 1, null, null, "{}");
      assertError("no API fields may be given", 1, "apiName", null, "{}");
      assertError("no API fields may be given", 1, "apiName", "v1", "{}");

      assertError("all API fields must be given", null, "apiName", "v1", null);
      assertError("all API fields must be given", null, "apiName", null, "{}");
      assertError("all API fields must be given", null, null, "v1", "{}");
      assertError("all API fields must be given", null, "apiName", "v1", "");
      assertError("all API fields must be given", null, "apiName", "", "{}");
      assertError("all API fields must be given", null, "", "v1", "{}");
      assertError("all API fields must be given", null, "", "v1", null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPreUpdate() throws QException
   {
      WorkflowTestScenario oldRecord = new WorkflowTestScenario()
         .withApiName("myApi")
         .withApiVersion("v1")
         .withApiJson("{}");

      WorkflowTestScenario newRecord = new WorkflowTestScenario()
         .withSourceRecordId(1);

      QRecord validatedRecord = new WorkflowTestScenarioTableCustomizer().preUpdate(null, List.of(newRecord.toQRecordOnlyChangedFields(false)), true, Optional.of(List.of(oldRecord.toQRecordOnlyChangedFields(false)))).get(0);
      assertThat(validatedRecord.getErrorsAsString()).contains("no API fields may be given");

      validatedRecord = new WorkflowTestScenarioTableCustomizer().preUpdate(null, List.of(new QRecord()), true, Optional.of(List.of(new QRecord()))).get(0);
      assertThat(validatedRecord.getErrorsAsString()).contains("Source Record Id or all API fields");

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testApiJsonValidation() throws Exception
   {
      Integer workflowId = WorkflowsTestDataSource.insertTestWorkflow();

      UnsafeFunction<String, QRecord, ?> run = apiJson ->
      {
         WorkflowTestScenario newRecord = new WorkflowTestScenario()
            .withWorkflowId(workflowId)
            .withApiName(BaseTest.API_NAME)
            .withApiVersion(BaseTest.V1)
            .withApiJson(apiJson);

         return new WorkflowTestScenarioTableCustomizer().preInsert(null, List.of(newRecord.toQRecord()), true).get(0);
      };

      assertThat(run.apply("{").getErrorsAsString()).contains("API JSON was not valid: A JSONObject text must end with '}'");
      assertThat(run.apply("[]").getErrorsAsString()).contains("API JSON was not valid: A JSONObject text must begin with '{'");
      assertThat(run.apply("""
         "name": "Doug\"""").getErrorsAsString()).contains("API JSON was not valid: A JSONObject text must begin with '{'");

      assertThat(run.apply("""
         {
            "notAField": true
         }""").getErrorsAsString()).contains("API JSON was not valid: Request body contained 1 unrecognized field name: notAField");

      assertThat(run.apply("{}").getErrors()).isNullOrEmpty();

      assertThat(run.apply("""
         {
            "id": 47,
            "name": "Bob",
            "workflowTypeName": "someWorkflowType",
            "tableName": "myTable"
         }
         """).getErrors()).isNullOrEmpty();

   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void assertError(String expectedMessageToContain, Integer sourceRecordId, String apiName, String apiVersion, String apiJson) throws QException
   {
      WorkflowTestScenario workflowTestScenario = new WorkflowTestScenario()
         .withSourceRecordId(sourceRecordId)
         .withApiName(apiName)
         .withApiVersion(apiVersion)
         .withApiJson(apiJson);

      QRecord validatedRecord = new WorkflowTestScenarioTableCustomizer().preInsert(null, List.of(workflowTestScenario.toQRecord()), true).get(0);
      assertThat(validatedRecord.getErrorsAsString()).contains(expectedMessageToContain);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void assertSuccess(Integer sourceRecordId, String apiName, String apiVersion, String apiJson) throws QException
   {
      WorkflowTestScenario workflowTestScenario = new WorkflowTestScenario()
         .withSourceRecordId(sourceRecordId)
         .withApiName(apiName)
         .withApiVersion(apiVersion)
         .withApiJson(apiJson);

      QRecord validatedRecord = new WorkflowTestScenarioTableCustomizer().preInsert(null, List.of(workflowTestScenario.toQRecord()), true).get(0);
      assertEquals(0, validatedRecord.getErrors().size());
   }

}