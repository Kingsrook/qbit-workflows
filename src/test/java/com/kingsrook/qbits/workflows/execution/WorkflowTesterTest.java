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

package com.kingsrook.qbits.workflows.execution;


import java.util.List;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qbits.workflows.TestWorkflowDefinitions;
import com.kingsrook.qbits.workflows.WorkflowsTestDataSource;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowTestAssertion;
import com.kingsrook.qbits.workflows.model.WorkflowTestRun;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qbits.workflows.model.WorkflowTestStatus;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for WorkflowTester 
 *******************************************************************************/
class WorkflowTesterTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test2ScenariosPass() throws Exception
   {
      TestWorkflowDefinitions.registerTestWorkflowTypes();
      Integer workflowId = WorkflowsTestDataSource.insertTestWorkflow();

      new InsertAction().execute(new InsertInput(WorkflowTestScenario.TABLE_NAME).withRecordEntities(List.of(
         new WorkflowTestScenario()
            .withWorkflowId(workflowId)
            .withName("Apple")
            .withSourceRecordId(0)
            .withAssertions(List.of(
               new WorkflowTestAssertion().withName("seed should be 0").withVariableName("seedValue").withExpectedValue("0"),
               new WorkflowTestAssertion().withName("sum should be 11").withVariableName("sum").withExpectedValue("11")
            )),
         new WorkflowTestScenario()
            .withWorkflowId(workflowId)
            .withName("Banana")
            .withSourceRecordId(1)
            .withAssertions(List.of(
               new WorkflowTestAssertion().withName("sum should be 12").withVariableName("sum").withExpectedValue("12")
            ))
      )));

      WorkflowTesterInput  input = new WorkflowTesterInput()
         .withWorkflow(new GetAction().executeForRecord(new GetInput(Workflow.TABLE_NAME).withPrimaryKey(workflowId)))
         .withWorkflowTestScenarioList(QueryAction.execute(WorkflowTestScenario.TABLE_NAME, new QQueryFilter(new QFilterCriteria("workflowId", QCriteriaOperator.EQUALS, workflowId))));
      WorkflowTesterOutput output = new WorkflowTesterOutput();
      new WorkflowTester().execute(input, output);

      WorkflowTestRun workflowTestRun = output.getWorkflowTestRun();
      assertEquals(WorkflowTestStatus.PASS.getId(), workflowTestRun.getStatus());
      assertEquals(2, workflowTestRun.getScenarioCount());
      assertEquals(2, workflowTestRun.getScenarioPassCount());
      assertEquals(0, workflowTestRun.getScenarioFailCount());
      assertEquals(3, workflowTestRun.getAssertionCount());
      assertEquals(3, workflowTestRun.getAssertionPassCount());
      assertEquals(0, workflowTestRun.getAssertionFailCount());
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test1ScenariosPass1Fail() throws Exception
   {
      TestWorkflowDefinitions.registerTestWorkflowTypes();
      Integer workflowId = WorkflowsTestDataSource.insertTestWorkflow();

      new InsertAction().execute(new InsertInput(WorkflowTestScenario.TABLE_NAME).withRecordEntities(List.of(
         new WorkflowTestScenario()
            .withWorkflowId(workflowId)
            .withName("Apple")
            .withSourceRecordId(0)
            .withAssertions(List.of(
               new WorkflowTestAssertion().withName("seed should be 0").withVariableName("seedValue").withExpectedValue("0"),
               new WorkflowTestAssertion().withName("sum should be 10").withVariableName("sum").withExpectedValue("10") // expected to fail
            )),
         new WorkflowTestScenario()
            .withWorkflowId(workflowId)
            .withName("Banana")
            .withSourceRecordId(1)
            .withAssertions(List.of(
               new WorkflowTestAssertion().withName("sum should be 12").withVariableName("sum").withExpectedValue("12")
            ))
      )));

      WorkflowTesterInput  input = new WorkflowTesterInput()
         .withWorkflow(new GetAction().executeForRecord(new GetInput(Workflow.TABLE_NAME).withPrimaryKey(workflowId)))
         .withWorkflowTestScenarioList(QueryAction.execute(WorkflowTestScenario.TABLE_NAME, new QQueryFilter(new QFilterCriteria("workflowId", QCriteriaOperator.EQUALS, workflowId))));
      WorkflowTesterOutput output = new WorkflowTesterOutput();
      new WorkflowTester().execute(input, output);

      WorkflowTestRun workflowTestRun = output.getWorkflowTestRun();
      assertEquals(WorkflowTestStatus.FAIL.getId(), workflowTestRun.getStatus());
      assertEquals(2, workflowTestRun.getScenarioCount());
      assertEquals(1, workflowTestRun.getScenarioPassCount());
      assertEquals(1, workflowTestRun.getScenarioFailCount());
      assertEquals(3, workflowTestRun.getAssertionCount());
      assertEquals(2, workflowTestRun.getAssertionPassCount());
      assertEquals(1, workflowTestRun.getAssertionFailCount());
   }

}