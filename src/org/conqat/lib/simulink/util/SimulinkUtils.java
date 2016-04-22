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
package org.conqat.lib.simulink.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.assertion.CCSMPre;
import org.conqat.lib.commons.assertion.PreconditionException;
import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.IdentityHashSet;
import org.conqat.lib.commons.error.NeverThrownRuntimeException;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.commons.visitor.IVisitor;
import org.conqat.lib.simulink.model.ParameterizedElement;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkElementBase;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkObject;
import org.conqat.lib.simulink.model.SimulinkOutPort;
import org.conqat.lib.simulink.model.SimulinkPortBase;
import org.conqat.lib.simulink.model.stateflow.IStateflowElement;
import org.conqat.lib.simulink.model.stateflow.IStateflowNodeContainer;
import org.conqat.lib.simulink.model.stateflow.StateflowBlock;
import org.conqat.lib.simulink.model.stateflow.StateflowChart;
import org.conqat.lib.simulink.model.stateflow.StateflowMachine;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;
import org.conqat.lib.simulink.model.stateflow.StateflowState;
import org.conqat.lib.simulink.model.stateflow.StateflowTarget;

/**
 * Collection of utility methods for Simulink models.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51748 $
 * @ConQAT.Rating GREEN Hash: EB99807BA7B4E6B582C7E936C98A3696
 */
public class SimulinkUtils {

	/** Object's class name parameter */
	private static final String CLASS_NAME_PARAMETER = "$ClassName";

	/** Separator used in Simulink ids. */
	public static final String SIMULINK_ID_SEPARATOR = "/";

	/** Prefix of targetlink blocks. */
	private static final String TARGETLINK_BLOCK_PREFIX = "TL_";

	/**
	 * Pattern used for splitting Simulink ids. This is required, as a double
	 * slash is not separator, but an escape. Note that Simulink names cannot
	 * start or end with a slash. Also see {@link #SIMULINK_ID_SEPARATOR}.
	 */
	private static final Pattern SIMULINK_ID_SPLIT_PATTERN = Pattern
			.compile("[^/]/[^/]");

	/** Copy parameters from one parameterized element to another. */
	public static void copyParameters(ParameterizedElement source,
			ParameterizedElement target) {
		for (String name : source.getParameterNames()) {
			target.setParameter(name, source.getParameter(name));
		}
	}

	/** Create map that maps from id to block. */
	public static Map<String, SimulinkBlock> createIdToNodeMap(
			SimulinkBlock block) {
		final Map<String, SimulinkBlock> map = new HashMap<>();
		visitDepthFirst(block,
				new IVisitor<SimulinkBlock, NeverThrownRuntimeException>() {
					@Override
					public void visit(SimulinkBlock block) {
						map.put(block.getId(), block);
					}
				});
		return map;
	}

	/**
	 * Builds a Simulink id from a parent and a local name (for which slashes
	 * will be escaped). If the parent is null, only the local name is used.
	 */
	public static String buildId(SimulinkElementBase parent, String localName) {
		if (parent != null) {
			return parent.getId() + SIMULINK_ID_SEPARATOR
					+ escapeSlashes(localName);
		}
		return escapeSlashes(localName);
	}

	/** Replaces forward slashes by double forward slashes. */
	public static String escapeSlashes(String string) {
		return string.replace(SIMULINK_ID_SEPARATOR, "//");
	}

	/**
	 * Replace double forward slashes by single forward slashes. This is the
	 * counterpart to {@link #escapeSlashes(String)}.
	 */
	private static String removeEscapedSlashes(String name) {
		return name.replace("//", SIMULINK_ID_SEPARATOR);
	}

	/**
	 * Get Simulink array parameter as array. This raises a
	 * {@link NumberFormatException} if the elements of the array are not
	 * integers.
	 */
	public static int[] getIntParameterArray(String parameter) {
		String[] parts = getStringParameterArray(parameter);
		int[] result = new int[parts.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.parseInt(parts[i]);
		}
		return result;
	}

	/**
	 * Get Simulink array parameter as array. This raises a
	 * {@link NumberFormatException} if the elements of the array are not
	 * doubles.
	 */
	public static double[] getDoubleParameterArray(String parameter) {
		String[] parts = getStringParameterArray(parameter);
		double[] result = new double[parts.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Double.parseDouble(parts[i]);
		}
		return result;
	}

	/** Get Simulink array parameter as array. */
	public static String[] getStringParameterArray(String parameter) {
		// remove brackets
		String content = parameter.substring(1, parameter.length() - 1);
		if (StringUtils.isEmpty(content)) {
			return new String[0];
		}
		return content.split("[,;] *");
	}

	/** Checks if a block is a target link block. */
	public static boolean isTargetlinkBlock(SimulinkBlock block) {
		return isTargetlinkBlockReference(block) || isTargetlinkMaskType(block)
				|| hasTargetlinkObject(block);
	}

	/** Returns whether the given block is a reference to a Targetlink type. */
	private static boolean isTargetlinkBlockReference(SimulinkBlock block) {
		return block.getType().equals(SimulinkConstants.TYPE_Reference)
				&& block.getParameter(SimulinkConstants.PARAM_SourceType)
						.startsWith(TARGETLINK_BLOCK_PREFIX);
	}

	/** Returns whether the mask type is set to a Targetlink type. */
	public static boolean isTargetlinkMaskType(SimulinkBlock block) {
		String maskType = block.getParameter("MaskType");
		return maskType != null && maskType.startsWith(TARGETLINK_BLOCK_PREFIX);
	}

	/**
	 * Returns whether this contains an object with a Targetlink type. This is
	 * the new way of marking Targetlink blocks.
	 */
	private static boolean hasTargetlinkObject(SimulinkBlock block) {
		SimulinkObject maskObject = findObjectByClass(block,
				SimulinkConstants.PARAM_Simulink_Mask);
		if (maskObject == null) {
			return false;
		}

		String typeParameter = maskObject.getParameter("Type");
		return typeParameter != null
				&& typeParameter.startsWith(TARGETLINK_BLOCK_PREFIX);
	}

	/** Split full qualified identifier. */
	public static List<String> splitSimulinkId(String id) {
		ArrayList<String> result = new ArrayList<String>();
		Matcher matcher = SIMULINK_ID_SPLIT_PATTERN.matcher(id);

		int begin = 0;
		while (matcher.find(begin)) {
			result.add(removeEscapedSlashes(id.substring(begin,
					matcher.start() + 1)));
			// pattern is one character longer than the slash
			begin = matcher.end() - 1;
		}
		result.add(removeEscapedSlashes(id.substring(begin)));

		return result;
	}

	/**
	 * Create Simulink id from a iteration of names. This takes care of proper
	 * escaping.
	 * 
	 * @throws PreconditionException
	 *             if one of names starts or ends with a slash
	 */
	public static String createSimulinkId(Iterable<String> names) {
		StringBuilder result = new StringBuilder();
		Iterator<String> it = names.iterator();
		while (it.hasNext()) {
			String name = it.next();
			CCSMPre.isFalse(startsOrEndsWithSeparator(name),
					"Simulink names cannot start or end with a slash.");
			result.append(escapeSlashes(name));
			if (it.hasNext()) {
				result.append("/");
			}
		}
		return result.toString();
	}

	/**
	 * Visit blocks in a depth first manner.
	 * 
	 * @param <X>
	 *            Type of exception thrown by the visitor.
	 * @param block
	 *            block to start with
	 * @param visitor
	 *            the visitor
	 * @throws X
	 *             exception thrown by the visitor.
	 */
	public static <X extends Exception> void visitDepthFirst(
			SimulinkBlock block, IVisitor<SimulinkBlock, X> visitor) throws X {
		visitor.visit(block);
		for (SimulinkBlock child : block.getSubBlocks()) {
			visitDepthFirst(child, visitor);
		}
	}

	/** Returns all recursively reachable subblocks of the given block. */
	public static List<SimulinkBlock> listBlocksDepthFirst(SimulinkBlock block) {
		final List<SimulinkBlock> result = new ArrayList<SimulinkBlock>();
		SimulinkUtils.visitDepthFirst(block,
				new IVisitor<SimulinkBlock, NeverThrownRuntimeException>() {
					@Override
					public void visit(SimulinkBlock block) {
						result.add(block);
					}
				});
		return result;
	}

	/**
	 * Calculate the set of all parent blocks up to the model for the given
	 * blocks.
	 */
	public static Set<SimulinkBlock> calculateParentSet(
			Collection<SimulinkBlock> blocks) {
		Set<SimulinkBlock> parents = new IdentityHashSet<SimulinkBlock>();
		for (SimulinkBlock block : blocks) {
			SimulinkModel model = block.getModel();
			while (block != model) {
				parents.add(block);
				block = block.getParent();
			}
		}
		return parents;
	}

	/** Recursively count sub blocks. */
	public static int countSubBlocks(SimulinkBlock block) {
		BlockAndLineCounter counter = new BlockAndLineCounter();
		SimulinkUtils.visitDepthFirst(block, counter);
		// minus the root block
		return counter.blockCount - 1;
	}

	/** Recursively count lines. */
	public static int countLines(SimulinkBlock block) {
		BlockAndLineCounter counter = new BlockAndLineCounter();
		for (SimulinkBlock child : block.getSubBlocks()) {
			SimulinkUtils.visitDepthFirst(child, counter);
		}
		return counter.lineCount;
	}

	/** Recursively count Stateflow states. */
	public static int countStates(IStateflowNodeContainer<?> node) {
		int count = 0;
		if (node instanceof StateflowState) {
			count = 1;
		} else {
			count = 0;
		}

		for (StateflowNodeBase element : node.getNodes()) {
			if (element instanceof IStateflowNodeContainer<?>) {
				count += countStates((IStateflowNodeContainer<?>) element);
			}
		}
		return count;
	}

	/** Count states of all charts of the machine. */
	public static int countStates(StateflowMachine stateflowMachine) {
		int stateCount = 0;
		for (StateflowChart chart : stateflowMachine.getCharts()) {
			stateCount += countStates(chart);
		}
		return stateCount;
	}

	/**
	 * Get the Stateflow chart a Stateflow element belongs to.
	 * 
	 * @return the Stateflow chart or <code>null</code> if the element is
	 *         unconnected or not associated with a chart, e.g.
	 *         {@link StateflowTarget}.
	 */
	public static StateflowChart getChart(IStateflowElement<?> element) {
		if (element instanceof StateflowChart) {
			return (StateflowChart) element;
		}
		IStateflowElement<?> parent = element.getParent();
		if (parent == null) {
			return null;
		}
		return getChart(parent);
	}

	/**
	 * Get the Stateflow block a Stateflow element belongs to.
	 * 
	 * @return the Stateflow block or <code>null</code> if the element is
	 *         unconnected or not associated with a chart, e.g.
	 *         {@link StateflowTarget}.
	 */
	public static StateflowBlock getBlock(IStateflowElement<?> element) {
		StateflowChart chart = getChart(element);
		if (chart == null) {
			return null;
		}
		return chart.getStateflowBlock();
	}

	/**
	 * Get name of a Stateflow state as defined in the Stateflow manual. As
	 * Stateflow awkwardly stores the names as part of the label, this is put in
	 * a utility methods and not directly at class {@link StateflowState}.
	 */
	public static String getStateName(StateflowState state) {
		String label = state.getLabel();
		if (StringUtils.isEmpty(label)) {
			return null;
		}
		String name = label.split("\\\\n")[0];

		// State names MAY end with a slash
		if (name.length() > 1 && name.endsWith("/")) {
			name = name.substring(0, name.length() - 1);
		}
		return name;
	}

	/**
	 * Get full qualified state name. This is deliberately not part of class
	 * {@link StateflowState} as names of Stateflow derives names from the state
	 * labels.
	 */
	public static String getFQStateName(StateflowState state) {
		String name = getStateName(state);
		IStateflowNodeContainer<?> parent = state.getParent();
		if (parent == null) {
			return name;
		}
		if (parent instanceof StateflowChart) {
			StateflowChart chart = (StateflowChart) parent;
			return chart.getStateflowBlock().getId() + "/" + name;
		}

		// Can be only a state
		return getFQStateName((StateflowState) parent) + "." + name;
	}

	/**
	 * Obtain out port block that is below the a Stateflow block and describes
	 * the output of a Stateflow chart.
	 * 
	 * What Simulink displays like an atomic Stateflow chart is internally
	 * represented as a Simulink sub system that itself contains multiple
	 * blocks. The sub system itself has a normal inport/outport which has only
	 * a number (as (almost) all ports of Simulink sub systems do). However the
	 * sub system contains a block of <b>type</b> Inport/Outport (quite
	 * confusing...) and this is the one the carries the name of the Stateflow
	 * output. Note that this is related to a past CR described at
	 * <https://bugzilla.informatik.tu-muenchen.de/show_bug.cgi?id=1502>.
	 * 
	 * The code is the following:
	 * <ul>
	 * <li>iterate over all child blocks of the sub system that represents the
	 * Stateflow chart
	 * <li>pick the one that is of type Inport/Outport and that has the same
	 * port index as the inport/outport of the sub system (the index defines the
	 * mapping between the Inport/Ouport block and the actual inport/outport)
	 * </ul>
	 */
	public static SimulinkBlock getStateflowOutport(SimulinkOutPort outPort) {
		return getStateflowPort(outPort, SimulinkConstants.TYPE_Outport);
	}

	/**
	 * Obtain in port for stateflow block. See
	 * {@link #getStateflowOutport(SimulinkOutPort)} for details.
	 */
	public static SimulinkBlock getStateflowInport(SimulinkInPort inPort) {
		return getStateflowPort(inPort, SimulinkConstants.TYPE_Inport);
	}

	/**
	 * Obtain port for stateflow block. See
	 * {@link #getStateflowOutport(SimulinkOutPort)} for details.
	 */
	private static SimulinkBlock getStateflowPort(SimulinkPortBase port,
			String portType) throws AssertionError {
		CCSMPre.isInstanceOf(port.getBlock(), StateflowBlock.class);
		SimulinkBlock result = null;
		for (SimulinkBlock block : port.getBlock().getSubBlocks()) {
			if (portType.equals(block.getType())
					&& block.getParameter(SimulinkConstants.PARAM_Port).equals(
							port.getIndex())) {
				CCSMAssert.isTrue(result == null,
						"We assummed that there is only one matching port.");
				result = block;
			}
		}
		return result;
	}

	/** Visitor for counting sub blocks. */
	private static class BlockAndLineCounter implements
			IVisitor<SimulinkBlock, NeverThrownRuntimeException> {
		/** Counter for blocks. */
		private int blockCount = 0;

		/** Counter for lines. */
		private int lineCount = 0;

		/** Count block. */
		@Override
		public void visit(SimulinkBlock element) {
			blockCount++;
			lineCount += element.getOutLines().size();
		}
	}

	/**
	 * This method checks if two block have the same syntactic interface, i.e.
	 * the same input and output ports.
	 */
	public static boolean checkCompatibility(SimulinkBlock block1,
			SimulinkBlock block2) {
		boolean inPortsEqual = obtainPortIndexes(block1.getInPorts()).equals(
				obtainPortIndexes(block2.getInPorts()));
		boolean outPortsEqual = obtainPortIndexes(block1.getOutPorts()).equals(
				obtainPortIndexes(block2.getOutPorts()));
		return inPortsEqual && outPortsEqual;
	}

	/** Creates a set of the indexes of the provided ports. */
	private static HashSet<String> obtainPortIndexes(
			Collection<? extends SimulinkPortBase> ports) {
		HashSet<String> indexes = new HashSet<String>();
		for (SimulinkPortBase port : ports) {
			indexes.add(port.getIndex());
		}
		return indexes;
	}

	/**
	 * Replaces this line with another line, copying the given parameter values.
	 * This method removes {@code line} and creates a new line with the given
	 * source and target ports.
	 * 
	 * @param line
	 *            The line to replace
	 * @param sourcePort
	 *            The source port of the new line
	 * @param destinationPort
	 *            The destination port of the new line
	 * @param parameters
	 *            The parameter that should be copied from {@code line} to the
	 *            new line
	 */
	public static SimulinkLine replaceLine(SimulinkLine line,
			SimulinkOutPort sourcePort, SimulinkInPort destinationPort,
			String... parameters) {

		// Save the parameters, as they are not accessible once the line is
		// removed
		Map<String, String> paramValues = new HashMap<String, String>();
		for (String param : parameters) {
			String value = line.getParameter(param);
			paramValues.put(param, value);
		}

		// We have first to remove line before creating the replacement, as the
		// lines are not allowed to share an in-port
		line.remove();

		SimulinkBlock newContainer = getLowestCommonAncestor(sourcePort
				.getBlock().getParent(), destinationPort.getBlock().getParent());
		SimulinkLine newLine = new SimulinkLine(sourcePort, destinationPort,
				newContainer);

		// Set the parameter values
		for (String param : parameters) {
			String value = paramValues.get(param);
			if (value != null) {
				newLine.setParameter(param, value);
			}
		}
		return newLine;
	}

	/**
	 * Returns the deepest block that is a common ancestor of both blocks. This
	 * requires both blocks to be in the same model
	 */
	public static SimulinkBlock getLowestCommonAncestor(SimulinkBlock block1,
			SimulinkBlock block2) {
		CCSMPre.isTrue(block1.getModel() == block2.getModel(),
				"Both blocks must be in same model!");

		Set<SimulinkBlock> parentSet1 = calculateParentSet(Collections
				.singletonList(block1));
		while (block2 != null) {
			if (parentSet1.contains(block2)) {
				return block2;
			}
			block2 = block2.getParent();
		}

		// blocks will meet at the model level
		return block1.getModel();
	}

	/** Returns a list of the given elements sorted by id. */
	public static <T extends SimulinkElementBase> List<T> sortById(
			Collection<T> blocks) {
		return CollectionUtils.sort(blocks, new Comparator<T>() {

			@Override
			public int compare(T block1, T block2) {
				return block1.getId().compareTo(block2.getId());
			}
		});
	}

	/**
	 * Returns the first object contained in the given element of given class
	 * (or null).
	 * 
	 * @param element
	 *            is this is null, null is returned.
	 */
	public static SimulinkObject findObjectByClass(SimulinkElementBase element,
			String className) {
		if (element == null) {
			return null;
		}

		for (SimulinkObject object : element.getObjects()) {
			if (className.equals(object.getParameter(CLASS_NAME_PARAMETER))) {
				return object;
			}
		}

		return null;
	}

	/** Returns whether the given name starts or end with a slash. */
	public static boolean startsOrEndsWithSeparator(String name) {
		return name.startsWith(SIMULINK_ID_SEPARATOR)
				|| name.endsWith(SIMULINK_ID_SEPARATOR);
	}

	/** Returns whether this block is an inport. */
	public static boolean isInport(SimulinkBlock block) {
		return SimulinkConstants.TYPE_Inport.equals(block.getType());
	}

	/** Returns whether this block is an outport. */
	public static boolean isOutport(SimulinkBlock block) {
		return SimulinkConstants.TYPE_Outport.equals(block.getType());
	}

	/** Returns whether the given block is a round sum block. */
	public static boolean isRoundSum(SimulinkBlock block) {
		return SimulinkConstants.TYPE_Sum.equals(block.getType())
				&& SimulinkConstants.SHAPE_round.equals(block
						.getParameter(SimulinkConstants.PARAM_IconShape));
	}

	/** Replaces Simulink line breaks with "real" line breaks. */
	public static String replaceSimulinkLineBreaks(String name) {
		return name.replaceAll("\\\\n", StringUtils.CR);
	}
}