/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright 2005-2011 the ConQAT Project                                   |
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
package org.conqat.lib.simulink.builder;

import java.util.HashSet;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.stateflow.StateflowBlock;
import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.model.stateflow.StateflowData;
import org.conqat.lib.simulink.model.stateflow.StateflowEvent;
import org.conqat.lib.simulink.model.stateflow.StateflowJunction;
import org.conqat.lib.simulink.model.stateflow.StateflowMachine;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;
import org.conqat.lib.simulink.model.stateflow.StateflowState;
import org.conqat.lib.simulink.model.stateflow.StateflowTarget;
import org.conqat.lib.simulink.model.stateflow.StateflowTransition;
import org.conqat.lib.simulink.testutils.SimulinkTestBase;

/**
 * Base class for testing Stateflow builder.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51955 $
 * @ConQAT.Rating GREEN Hash: 987AD07E8FBD1E88632D7113C350E2C8
 */
public abstract class StateflowModelBuilderTestBase extends SimulinkTestBase {

	/** Block under test. */
	private StateflowBlock block;

	/** Load model and set block. */
	@Override
	public void setUp() throws Exception {
		SimulinkModel model = loadModel(getTestModelName());
		block = (StateflowBlock) model.getBlock(model.getName() + "/Stateflow");
	}

	/** Returns the name of the model under test. */
	protected abstract String getTestModelName();

	/** Test if {@link StateflowMachine} was built correctly. */
	public void testMachine() {
		assertSame(block.getChart().getMachine(), block.getModel()
				.getStateflowMachine());

		StateflowMachine machine = block.getChart().getMachine();
		assertEquals(1, machine.getCharts().size());

		StateflowChart chart = machine.getCharts().iterator().next();
		assertEquals("Stateflow", chart.getName());

		assertEquals(1, machine.getTargets().size());
		StateflowTarget target = machine.getTargets().iterator().next();
		assertEquals("sfun", target.getParameter(SimulinkConstants.PARAM_name));

		additionalMachineAsserts(target);
	}

	/** Hook for additional machine assertions in {@link #testMachine()}. */
	protected abstract void additionalMachineAsserts(StateflowTarget target);

	/** Test if {@link StateflowChart} was built correctly. */
	public void testChart() {
		StateflowChart chart = block.getChart();
		assertEquals("Stateflow", chart.getName());
		assertEquals(2, chart.getNodes().size());

		StateflowState assemblyPoint = (StateflowState) getStateflowNode(chart,
				resolveId(ETestModelId.ASSEMBLY_POINT));
		assertEquals("AssemblyPoint", assemblyPoint.getLabel());

		StateflowState hotelStateflow = (StateflowState) getStateflowNode(
				chart, resolveId(ETestModelId.HOTEL_STATEFLOW));
		assertEquals("HotelStateflow", hotelStateflow.getLabel());

		checkEventsAndData(chart);
	}

	/** Test if {@link StateflowState}s were built correctly. */
	public void testState() {
		StateflowChart chart = block.getChart();
		StateflowState assemblyPoint = (StateflowState) getStateflowNode(chart,
				resolveId(ETestModelId.ASSEMBLY_POINT));
		assertEquals("AssemblyPoint", assemblyPoint.getLabel());
		assertTrue(assemblyPoint.getNodes().isEmpty());

		StateflowState hotelStateflow = (StateflowState) getStateflowNode(
				chart, resolveId(ETestModelId.HOTEL_STATEFLOW));
		assertEquals("HotelStateflow", hotelStateflow.getLabel());

		assertEquals(13, hotelStateflow.getNodes().size());

		checkJunctions(hotelStateflow);
		checkStates(hotelStateflow);
	}

	/** Checks the junctions in {@link #testState()}. */
	private void checkJunctions(StateflowState hotelStateflow) {
		StateflowJunction junction = (StateflowJunction) getStateflowNode(
				hotelStateflow, resolveId(ETestModelId.HISTORY_JUNCTION));
		assertEquals("HISTORY_JUNCTION", junction.getType());

		junction = (StateflowJunction) getStateflowNode(hotelStateflow,
				resolveId(ETestModelId.JUNCTION1));
		assertEquals("CONNECTIVE_JUNCTION", junction.getType());

		junction = (StateflowJunction) getStateflowNode(hotelStateflow,
				resolveId(ETestModelId.JUNCTION2));
		assertEquals("CONNECTIVE_JUNCTION", junction.getType());

		junction = (StateflowJunction) getStateflowNode(hotelStateflow,
				resolveId(ETestModelId.JUNCTION3));
		assertEquals("CONNECTIVE_JUNCTION", junction.getType());
	}

	/** Checks the states in {@link #testState()}. */
	private void checkStates(StateflowState hotelStateflow) {
		StateflowState state = (StateflowState) getStateflowNode(
				hotelStateflow, resolveId(ETestModelId.FAMILY_SUITE));
		assertEquals("FamilySuite", state.getLabel());
		assertEquals("OR_STATE",
				state.getParameter(SimulinkConstants.PARAM_type));

		state = (StateflowState) getStateflowNode(hotelStateflow,
				resolveId(ETestModelId.SMOKING_ROOM));
		assertEquals("SmokingRoom", state.getLabel());

		state = (StateflowState) getStateflowNode(hotelStateflow,
				resolveId(ETestModelId.NON_SMOKING_ROOM));
		assertEquals(
				"NonSmokingRoom\n entry: b=unpack();\n during: b=callhome();\n exit: b=pack();",
				unescapeNewlines(state.getLabel()));

		state = (StateflowState) getStateflowNode(hotelStateflow,
				resolveId(ETestModelId.FUNC_STATE1));
		assertEquals("y=unpack", state.getLabel());
		assertEquals("FUNC_STATE",
				state.getParameter(SimulinkConstants.PARAM_type));

		state = (StateflowState) getStateflowNode(hotelStateflow,
				resolveId(ETestModelId.FUNC_STATE2));
		assertEquals("y=pack", state.getLabel());
		assertEquals("FUNC_STATE",
				state.getParameter(SimulinkConstants.PARAM_type));
		assertEquals("1", state.getParameter("truthTable.isTruthTable"));
	}

	/** Test if {@link StateflowTransition}s were built correctly. */
	public void testTransition() {
		StateflowChart chart = block.getChart();
		StateflowState assemblyPoint = checkAssemblyPoint(chart);
		StateflowState hotelStateflow = checkHotelStateflowNode(chart,
				assemblyPoint);
		checkTransitionsToJunctions(hotelStateflow);
	}

	/** Subcheck of {@link #testTransition()}. */
	private StateflowState checkAssemblyPoint(StateflowChart chart) {
		StateflowState assemblyPoint = (StateflowState) getStateflowNode(chart,
				resolveId(ETestModelId.ASSEMBLY_POINT));
		assertEquals(1, assemblyPoint.getInTransitions().size());
		StateflowTransition inTransition = assemblyPoint.getInTransitions()
				.iterator().next();
		assertEquals(resolveId(ETestModelId.HOTEL_STATEFLOW), inTransition
				.getSrc().getStateflowId());
		assertEquals(resolveId(ETestModelId.ASSEMBLY_POINT), inTransition
				.getDst().getStateflowId());
		assertEquals("Firealarm", inTransition.getLabel());

		assertEquals(1, assemblyPoint.getOutTransitions().size());
		StateflowTransition outTransition = assemblyPoint.getOutTransitions()
				.iterator().next();
		assertEquals(resolveId(ETestModelId.ASSEMBLY_POINT), outTransition
				.getSrc().getStateflowId());
		assertEquals(resolveId(ETestModelId.HOTEL_STATEFLOW), outTransition
				.getDst().getStateflowId());
		assertEquals("Allclear", outTransition.getLabel());
		return assemblyPoint;
	}

	/** Subcheck of {@link #testTransition()}. */
	private StateflowState checkHotelStateflowNode(StateflowChart chart,
			StateflowState assemblyPoint) {
		StateflowState hotelStateflow = (StateflowState) getStateflowNode(
				chart, resolveId(ETestModelId.HOTEL_STATEFLOW));
		assertEquals(2, hotelStateflow.getInTransitions().size());

		StateflowTransition transition = getTransitionWithSrc(hotelStateflow,
				null);
		assertNotNull(transition);

		transition = getTransitionWithSrc(hotelStateflow, assemblyPoint);
		assertEquals("Allclear", transition.getLabel());

		assertEquals(1, hotelStateflow.getOutTransitions().size());
		transition = getTransitionWithDst(hotelStateflow, assemblyPoint);
		assertEquals("Firealarm", transition.getLabel());
		return hotelStateflow;
	}

	/** Subcheck of {@link #testTransition()}. */
	private void checkTransitionsToJunctions(StateflowState hotelStateflow) {
		StateflowTransition transition;
		StateflowState wait = (StateflowState) getStateflowNode(hotelStateflow,
				resolveId(ETestModelId.JUNCTION4));
		assertEquals(1, wait.getOutTransitions().size());
		transition = wait.getOutTransitions().iterator().next();
		assertEquals("checkin", transition.getLabel());
		StateflowJunction junction = (StateflowJunction) transition.getDst();
		assertEquals(resolveId(ETestModelId.JUNCTION2),
				junction.getStateflowId());

		assertSame(transition, junction.getInTransitions().iterator().next());

		assertEquals(2, junction.getOutTransitions().size());
	}

	/** Enumeration of model IDs used in this test. */
	protected static enum ETestModelId {
		/** Id. */
		HOTEL_STATEFLOW,
		/** Id. */
		ASSEMBLY_POINT,
		/** Id. */
		HISTORY_JUNCTION,
		/** Id. */
		JUNCTION1,
		/** Id. */
		JUNCTION2,
		/** Id. */
		JUNCTION3,
		/** Id. */
		JUNCTION4,
		/** Id. */
		FAMILY_SUITE,
		/** Id. */
		SMOKING_ROOM,
		/** Id. */
		NON_SMOKING_ROOM,
		/** Id. */
		FUNC_STATE1,
		/** Id. */
		FUNC_STATE2
	}

	/** Returns the actual model ID in the tested model for a symbolic model id. */
	protected abstract String resolveId(ETestModelId id);

	/** Get the outgoing transition of a node with a specified destination. */
	protected StateflowTransition getTransitionWithDst(StateflowNodeBase node,
			StateflowNodeBase dst) {
		for (StateflowTransition transition : node.getOutTransitions()) {
			if (transition.getDst() == dst) {
				return transition;
			}
		}
		return null;
	}

	/** Get the incoming transition of a node with a specified source or null. */
	protected StateflowTransition getTransitionWithSrc(StateflowNodeBase node,
			StateflowNodeBase src) {
		for (StateflowTransition transition : node.getInTransitions()) {
			if (transition.getSrc() == src) {
				return transition;
			}
		}
		return null;
	}

	/** Checks that the chart contains the test events and data. */
	protected void checkEventsAndData(StateflowChart chart) {
		HashSet<String> names = new HashSet<String>();
		for (StateflowEvent event : chart.getEvents()) {
			names.add(event.getName());
		}
		assertTrue(names.containsAll(CollectionUtils.asHashSet("Allclear",
				"checkin", "clk", "E", "Firealarm")));

		names.clear();

		for (StateflowData data : chart.getData()) {
			names.add(data.getName());
		}
		assertTrue(names.containsAll(CollectionUtils.asHashSet("b", "c1",
				"guest", "rich", "smoking")));
	}
}
