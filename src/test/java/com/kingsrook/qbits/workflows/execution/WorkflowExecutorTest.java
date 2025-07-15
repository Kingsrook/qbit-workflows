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


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qbits.workflows.TestWorkflowDefinitions;
import com.kingsrook.qbits.workflows.WorkflowsTestDataSource;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for WorkflowExecutor 
 *******************************************************************************/
class WorkflowExecutorTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      TestWorkflowDefinitions.registerTestWorkflowTypes();
      Integer workflowId = WorkflowsTestDataSource.insertTestWorkflow();

      WorkflowOutput output;

      /////////////////////////////////
      // initial run of the workflow //
      /////////////////////////////////
      output = executeWorkflow(workflowId, Map.of("condition", true, "seedValue", 0));
      assertNull(output.getException());
      assertEquals(11, output.getValues().get("sum"));

      ///////////////////////////////////////////////////////////////////
      // demonstrate taking the other conditional branch, versus above //
      ///////////////////////////////////////////////////////////////////
      output = executeWorkflow(workflowId, Map.of("condition", false, "seedValue", 0));
      assertNull(output.getException());
      assertEquals(12, output.getValues().get("sum"));

      ///////////////////////////////////////////////////////////////////////////////////////////
      // Show that the seed value was used in the pre-run - different seed gives different sum //
      ///////////////////////////////////////////////////////////////////////////////////////////
      output = executeWorkflow(workflowId, Map.of("condition", true, "seedValue", 10));
      assertNull(output.getException());
      assertEquals(21, output.getValues().get("sum"));

      //////////////////////////////
      // Show that post run works //
      //////////////////////////////
      output = executeWorkflow(workflowId, Map.of("condition", true, "overrideSumInPostRun", 1701));
      assertNull(output.getException());
      assertEquals(1701, output.getValues().get("sum"));

      //////////////////////////////
      // Show that pre step works //
      //////////////////////////////
      output = executeWorkflow(workflowId, Map.of("condition", true, "doubleSumInEveryPreStep", true));
      assertNull(output.getException());
      assertEquals(43, output.getValues().get("sum"));

      ///////////////////////////////
      // Show that post step works //
      ///////////////////////////////
      output = executeWorkflow(workflowId, Map.of("condition", true, "overrideReturnValueInPostStep", false));
      assertNull(output.getException());
      assertEquals(12, output.getValues().get("sum"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testContainers() throws QException
   {
      TestWorkflowDefinitions.registerTestWorkflowTypes();

      {
         /////////////////////////////////////
         // . 1                             //
         // . |                             //
         // /-2-\  opens container2 (push)  //
         // | | |                           //
         // | 3 |                           //
         // | | |                           //
         // \-4-/  last in container2 (pop) //
         // . |                             //
         // . 5                             //
         /////////////////////////////////////
         Workflow workflow = WorkflowsTestDataSource.insertWorkflowAndInitialRevision(TestWorkflowDefinitions.TEST_WORKFLOW_TYPE, null);
         WorkflowsTestDataSource.insertSteps(workflow, List.of(
            WorkflowsTestDataSource.newStep(1, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 1)),
            WorkflowsTestDataSource.newStep(2, TestWorkflowDefinitions.CONTAINER, Map.of()),
            WorkflowsTestDataSource.newStep(3, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 2)),
            WorkflowsTestDataSource.newStep(4, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 3)),
            WorkflowsTestDataSource.newStep(5, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 4))
         ));
         WorkflowsTestDataSource.insertLinks(workflow, List.of(
            WorkflowsTestDataSource.newLink(1, 2),
            WorkflowsTestDataSource.newLink(2, 3, "push"),
            WorkflowsTestDataSource.newLink(3, 4),
            WorkflowsTestDataSource.newLink(2, 5, "pop")
         ));

         WorkflowOutput output = executeWorkflow(workflow.getId(), Map.of("seedValue", 0));
         assertEquals(10, output.getValues().get("sum"));
      }

      {
         ///////////////////////////////////////////////
         // .   1                                     //
         // .   |                                     //
         // /---2---\  opens outer container2 (push)  //
         // |   |   |                                 //
         // |   3   |                                 //
         // |   |   |                                 //
         // | /-4-\ |  opens nested container4 (push) //
         // | | | | |                                 //
         // | | 5 | |                                 //
         // | | | | |                                 //
         // | \-6-/ | last in nested container (pop)  //
         // |   |   |                                 //
         // \---7---/ last in outer container (pop)   //
         // .   |                                     //
         // .   8                                     //
         ///////////////////////////////////////////////
         Workflow workflow = WorkflowsTestDataSource.insertWorkflowAndInitialRevision(TestWorkflowDefinitions.TEST_WORKFLOW_TYPE, null);
         WorkflowsTestDataSource.insertSteps(workflow, List.of(
            WorkflowsTestDataSource.newStep(1, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 1)),
            WorkflowsTestDataSource.newStep(2, TestWorkflowDefinitions.CONTAINER, Map.of()),
            WorkflowsTestDataSource.newStep(3, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 2)),
            WorkflowsTestDataSource.newStep(4, TestWorkflowDefinitions.CONTAINER, Map.of()),
            WorkflowsTestDataSource.newStep(5, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 3)),
            WorkflowsTestDataSource.newStep(6, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 4)),
            WorkflowsTestDataSource.newStep(7, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 5)),
            WorkflowsTestDataSource.newStep(8, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 6))
         ));
         WorkflowsTestDataSource.insertLinks(workflow, List.of(
            WorkflowsTestDataSource.newLink(1, 2),
            WorkflowsTestDataSource.newLink(2, 3, "push"),
            WorkflowsTestDataSource.newLink(3, 4),
            WorkflowsTestDataSource.newLink(4, 5, "push"),
            WorkflowsTestDataSource.newLink(5, 6),
            WorkflowsTestDataSource.newLink(4, 7, "pop"),
            WorkflowsTestDataSource.newLink(2, 8, "pop")
         ));

         WorkflowOutput output = executeWorkflow(workflow.getId(), Map.of("seedValue", 0));
         assertEquals(21, output.getValues().get("sum"));
      }

      {
         ////////////////////////////////////////////////////////////////////////////////////////////////
         // /---1---\  opens outer container1 (push)                                                   //
         // |   |   |                                                                                  //
         // | /-2-\ |  opens nested container2 (push)                                                  //
         // | | | | |                                                                                  //
         // .\\-3-//  last in nested container2 and outer container1 (but since no next step, no pops) //
         ////////////////////////////////////////////////////////////////////////////////////////////////
         Workflow workflow = WorkflowsTestDataSource.insertWorkflowAndInitialRevision(TestWorkflowDefinitions.TEST_WORKFLOW_TYPE, null);
         WorkflowsTestDataSource.insertSteps(workflow, List.of(
            WorkflowsTestDataSource.newStep(1, TestWorkflowDefinitions.CONTAINER, Map.of()),
            WorkflowsTestDataSource.newStep(2, TestWorkflowDefinitions.CONTAINER, Map.of()),
            WorkflowsTestDataSource.newStep(3, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 1))
         ));
         WorkflowsTestDataSource.insertLinks(workflow, List.of(
            WorkflowsTestDataSource.newLink(1, 2, "push"),
            WorkflowsTestDataSource.newLink(2, 3, "push")
         ));

         WorkflowOutput output = executeWorkflow(workflow.getId(), Map.of("seedValue", 0));
         assertEquals(1, output.getValues().get("sum"));
      }

      {
         ////////////////////////////////////////////////////////////////////////////////
         // /---1---\  opens outer container1 (push)                                   //
         // |   |   |                                                                  //
         // | /-2-\ |  opens nested container2 (push)                                  //
         // | | | | |                                                                  //
         // .\\-3-//  last in nested container2 outer container1 (pop (the outermost)) //
         // .   |                                                                      //
         // .   4                                                                      //
         ////////////////////////////////////////////////////////////////////////////////
         Workflow workflow = WorkflowsTestDataSource.insertWorkflowAndInitialRevision(TestWorkflowDefinitions.TEST_WORKFLOW_TYPE, null);
         WorkflowsTestDataSource.insertSteps(workflow, List.of(
            WorkflowsTestDataSource.newStep(1, TestWorkflowDefinitions.CONTAINER, Map.of()),
            WorkflowsTestDataSource.newStep(2, TestWorkflowDefinitions.CONTAINER, Map.of()),
            WorkflowsTestDataSource.newStep(3, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 1)),
            WorkflowsTestDataSource.newStep(4, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 2))
         ));
         WorkflowsTestDataSource.insertLinks(workflow, List.of(
            WorkflowsTestDataSource.newLink(1, 2, "push"),
            WorkflowsTestDataSource.newLink(2, 3, "push"),
            WorkflowsTestDataSource.newLink(1, 4, "pop")
         ));

         WorkflowOutput output = executeWorkflow(workflow.getId(), Map.of("seedValue", 0));
         assertEquals(3, output.getValues().get("sum"));
      }

      {
         /////////////////////////////////////////////////////////////////
         // /---1---\  opens outer container1                           //
         // |   |   |                                                   //
         // | <-2-> |  opens empty container2 and immediately closes it //
         // |   |   |                                                   //
         // \---3---/ last in outer container1                          //
         /////////////////////////////////////////////////////////////////
         Workflow workflow = WorkflowsTestDataSource.insertWorkflowAndInitialRevision(TestWorkflowDefinitions.TEST_WORKFLOW_TYPE, null);
         WorkflowsTestDataSource.insertSteps(workflow, List.of(
            WorkflowsTestDataSource.newStep(1, TestWorkflowDefinitions.CONTAINER, Map.of()),
            WorkflowsTestDataSource.newStep(2, TestWorkflowDefinitions.CONTAINER, Map.of()),
            WorkflowsTestDataSource.newStep(3, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 1))
         ));
         WorkflowsTestDataSource.insertLinks(workflow, List.of(
            WorkflowsTestDataSource.newLink(1, 2, "push"),
            WorkflowsTestDataSource.newLink(2, 3, "pop")
         ));

         WorkflowOutput output = executeWorkflow(workflow.getId(), Map.of("seedValue", 0));
         assertEquals(1, output.getValues().get("sum"));
      }

      {
         /////////////////////////////////////////////////////
         // . <-1->    opens empty container1 and closes it //
         // .   |                                           //
         // . <-2->    opens empty container2 and closes it //
         // .   |                                           //
         // . <-3->    opens empty container3 and closes it //
         // .   |                                           //
         // .   4                                           //
         /////////////////////////////////////////////////////
         Workflow workflow = WorkflowsTestDataSource.insertWorkflowAndInitialRevision(TestWorkflowDefinitions.TEST_WORKFLOW_TYPE, null);
         WorkflowsTestDataSource.insertSteps(workflow, List.of(
            WorkflowsTestDataSource.newStep(1, TestWorkflowDefinitions.CONTAINER, Map.of()),
            WorkflowsTestDataSource.newStep(2, TestWorkflowDefinitions.CONTAINER, Map.of()),
            WorkflowsTestDataSource.newStep(3, TestWorkflowDefinitions.CONTAINER, Map.of()),
            WorkflowsTestDataSource.newStep(4, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("x", 1))
         ));
         WorkflowsTestDataSource.insertLinks(workflow, List.of(
            WorkflowsTestDataSource.newLink(1, 2, "pop"),
            WorkflowsTestDataSource.newLink(2, 3, "pop"),
            WorkflowsTestDataSource.newLink(3, 4, "pop")
         ));

         WorkflowOutput output = executeWorkflow(workflow.getId(), Map.of("seedValue", 0));
         assertEquals(1, output.getValues().get("sum"));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testError() throws QException
   {
      TestWorkflowDefinitions.registerTestWorkflowTypes();
      Integer workflowId = WorkflowsTestDataSource.insertTestWorkflow();

      WorkflowOutput output;
      output = executeWorkflow(workflowId, MapBuilder.of("condition", true, "seedValue", null));
      assertThat(output.getException()).isInstanceOf(NullPointerException.class);
      assertThat(output.getWorkflowRunLog().getErrorMessage()).contains("intValue()\" because \"sum\" is null");
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private WorkflowOutput executeWorkflow(Integer workflowId, Map<String, Serializable> values) throws QException
   {
      WorkflowInput input = new WorkflowInput();
      input.setWorkflowId(workflowId);
      input.setValues(values);

      WorkflowOutput output = new WorkflowOutput();
      new WorkflowExecutor().execute(input, output);

      return (output);
   }

}