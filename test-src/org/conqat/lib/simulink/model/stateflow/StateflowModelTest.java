/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright 2005-2011 The ConQAT Project                                   |
|                                                                          |
| Licensed under the Apache License, Version 2.0 (the "License");          |
| you may not use this file except in compliance with the License.         |
| You may obtain a copy of the License at                                  |
|                                                                          |
|    http://www.apache.org/licenses/LICENSE-2.0                            |
|                                                                          |
| Unless required by applicable law or agreed to in writing, software      |
| distributed under the License is distributed on an "AS IS" BASIS,        |
| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. |
| See the License for the specific language governing permissions and      |
| limitations under the License.                                           |
+-------------------------------------------------------------------------*/
package org.conqat.lib.simulink.model.stateflow;

import java.util.Map;

import org.conqat.lib.commons.collections.IdentityHashSet;
import org.conqat.lib.commons.test.DeepCloneTestUtils;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.testutils.SimulinkTestBase;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Test for the Stateflow part of the model.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51959 $
 * @ConQAT.Rating GREEN Hash: 4A50847E3D9D3D9631E2E16F6047D40D
 */
public class StateflowModelTest extends SimulinkTestBase {

	/** Chart under test. */
	private StateflowChart chart;

	/** Load model and set block. */
	@Override
	public void setUp() throws Exception {
		chart = ((StateflowBlock) loadModel("internal_remove.mdl").getBlock(
				"internal_remove/Stateflow")).getChart();
	}

	/** Test if chart removal works. */
	public void testRemoveChart() {

		StateflowMachine machine = chart.getMachine();

		IdentityHashSet<StateflowChart> charts = DeepCloneTestUtils
				.getAllReferencedObjects(machine, StateflowChart.class,
						getClass().getPackage().getName());

		for (StateflowChart chart : charts) {
			chart.getStateflowBlock().remove();
			assertFalse(machine.getCharts().contains(chart));
			assertNull(chart.getParent());
		}
	}

	/** Test if data removal works. */
	public void testRemoveData() {
		IdentityHashSet<StateflowData> data = DeepCloneTestUtils
				.getAllReferencedObjects(chart, StateflowData.class, getClass()
						.getPackage().getName());

		for (StateflowData date : data) {
			StateflowDeclContainerBase<?> parent = date.getParent();
			date.remove();
			assertFalse(parent.getData().contains(date));
			assertNull(date.getParent());
		}
	}

	/** Test if event removal works. */
	public void testRemoveEvent() {
		IdentityHashSet<StateflowEvent> events = DeepCloneTestUtils
				.getAllReferencedObjects(chart, StateflowEvent.class,
						getClass().getPackage().getName());

		for (StateflowEvent event : events) {
			StateflowDeclContainerBase<?> parent = event.getParent();
			event.remove();
			assertFalse(parent.getEvents().contains(event));
			assertNull(event.getParent());
		}
	}

	/** Test if machine removal works. */
	public void testRemoveMachine() {
		StateflowMachine machine = chart.getMachine();
		SimulinkModel model = machine.getModel();
		testRemoveChart();
		machine.remove();
		assertNull(machine.getModel());
		assertNull(model.getStateflowMachine());
		assertTrue(machine.getCharts().isEmpty());
		assertTrue(machine.getData().isEmpty());
		assertTrue(machine.getEvents().isEmpty());
		assertTrue(machine.getTargets().isEmpty());
	}

	/** Test if node removal works. */
	public void testRemoveNode() {
		IdentityHashSet<StateflowNodeBase> nodes = DeepCloneTestUtils
				.getAllReferencedObjects(chart, StateflowNodeBase.class,
						getClass().getPackage().getName());

		for (StateflowNodeBase node : nodes) {
			IStateflowNodeContainer<?> parent = node.getParent();
			node.remove();

			assertFalse(parent.getNodes().contains(node));
			assertNull(node.getParent());

			assertTrue(node.getOutTransitions().isEmpty());
			assertTrue(node.getInTransitions().isEmpty());
		}

		// after we removed all nodes, there should be no transitions left
		IdentityHashSet<StateflowTransition> transitions = DeepCloneTestUtils
				.getAllReferencedObjects(chart, StateflowTransition.class,
						getClass().getPackage().getName());
		assertTrue(transitions.isEmpty());
	}

	/** Test if event removal works. */
	public void testRemoveTarget() {
		IdentityHashSet<StateflowTarget> targets = DeepCloneTestUtils
				.getAllReferencedObjects(chart, StateflowTarget.class,
						getClass().getPackage().getName());

		for (StateflowTarget target : targets) {
			StateflowMachine parent = target.getParent();
			target.remove();
			assertFalse(parent.getEvents().contains(target));
			assertNull(target.getParent());
		}
	}

	/** Test if transition removal works. */
	public void testRemoveTransition() {
		IdentityHashSet<StateflowTransition> transitions = DeepCloneTestUtils
				.getAllReferencedObjects(chart, StateflowTransition.class,
						getClass().getPackage().getName());

		for (StateflowTransition transition : transitions) {
			StateflowNodeBase src = transition.getSrc();
			StateflowNodeBase dst = transition.getDst();

			int dstCount = dst.getInTransitions().size();

			int srcCount = 1;
			if (src != null) {
				srcCount = src.getOutTransitions().size();
			}

			transition.remove();
			assertEquals(dstCount - 1, dst.getInTransitions().size());
			assertFalse(dst.getInTransitions().contains(transition));

			// default transition
			if (src == null) {
				continue;
			}

			assertEquals(srcCount - 1, src.getOutTransitions().size());
			assertFalse(src.getOutTransitions().contains(transition));

			assertNull(transition.getSrc());
			assertNull(transition.getDst());
		}
	}

	/**
	 * This tests if the blocks beneath the Stateflow block are read. See CR
	 * #1502.
	 */
	public void testHiddenBlocks() throws Exception {
		SimulinkModel model = loadModel("chart_input.mdl");
		StateflowBlock block = (StateflowBlock) model
				.getBlock("chart_input/Chart");

		Map<String, SimulinkBlock> map = SimulinkUtils.createIdToNodeMap(block);
		assertFalse(map.isEmpty());

		assertTrue(map.containsKey("chart_input/Chart/input1"));

		// The blocks really contain spaces
		assertTrue(map.containsKey("chart_input/Chart/ Demux "));
		assertTrue(map.containsKey("chart_input/Chart/ SFunction "));
		assertTrue(map.containsKey("chart_input/Chart/ Terminator "));
	}
}
