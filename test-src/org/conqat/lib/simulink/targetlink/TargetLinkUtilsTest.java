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
package org.conqat.lib.simulink.targetlink;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.conqat.lib.commons.collections.IdentityHashSet;
import org.conqat.lib.commons.collections.ListMap;
import org.conqat.lib.commons.collections.UnmodifiableSet;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkOutPort;
import org.conqat.lib.simulink.testutils.SimulinkTestBase;

/**
 * Test for the {@link TargetLinkUtils}.
 * 
 * @author $Author: kinnen $
 * @version $Rev: 41751 $
 * @ConQAT.Rating GREEN Hash: A9B5EF8001EA82077DC3F547BA9DCD01
 */
public class TargetLinkUtilsTest extends SimulinkTestBase {

	/**
	 * Some TargetLink Models contain synthetic SubBlocks that are not usable
	 * for the analysis. This tests if the TargetLink Models are properly
	 * filtered and the model remains consistent after the filtering Process.
	 */
	public void testFilterSyntheticBlocks() throws Exception {
		SimulinkModel model = loadModel("internal_apentry.mdl");

		// The simulink block with the TL_SimFrame property
		SimulinkBlock simFrameBlock = TargetLinkUtils
				.obtainSimFrameBlock(model);

		// The model contains such a block
		assertNotNull(simFrameBlock);

		// Remember the name of the Parent-Root block
		String name = simFrameBlock.getName();

		// find the block to be replaced
		SimulinkBlock tlSimFrameBlockSubsystem = simFrameBlock.getSubBlock(
				"Subsystem").getSubBlock(name);

		// Store the inPort- and outPort-Objects for the block
		// with the enabled TL Property
		IdentityHashSet<SimulinkInPort> tlSimFrameBlockInPorts = asIdentityHashSet(tlSimFrameBlockSubsystem
				.getInPorts());
		IdentityHashSet<SimulinkOutPort> tlSimFrameBlockOutPorts = asIdentityHashSet(tlSimFrameBlockSubsystem
				.getOutPorts());

		// Store the each block-Object for all subBlocks
		IdentityHashSet<SimulinkBlock> tlSimFrameBlockSubblocks = asIdentityHashSet(tlSimFrameBlockSubsystem
				.getSubBlocks());

		Map<String, SimulinkOutPort> tlSimInPortMapping = obtainInPortMappingForBlock(simFrameBlock);

		ListMap<String, SimulinkInPort> tlSimOutPortMapping = obtainOutPortMappingForBlock(simFrameBlock);

		// filter the synthetic blocks
		TargetLinkUtils.filterSyntheticBlocks(model);

		// check if the inPorts are equal after the model has been filtered
		assertEquals(
				"The inPort-Objects must have the same hashcode before and after the filtering proccess",
				asIdentityHashSet(model.getSubBlock(name).getInPorts()),
				tlSimFrameBlockInPorts);

		// check if the outPorts are equal after the model has been filtered
		assertEquals(
				"The outPort-Objects must have the same hashcode before and after the filtering proccess",
				asIdentityHashSet(model.getSubBlock(name).getOutPorts()),
				tlSimFrameBlockOutPorts);

		// Check that in the filtered Model the replaced block has the same
		// subBlocks as in the initial Model the block with the enabled TL
		// property
		assertEquals(
				"The replaced block and the new block must have exact the same subBlocks",
				tlSimFrameBlockSubblocks,
				asIdentityHashSet(model.getSubBlock(name).getSubBlocks()));

		// Check that the ports of replacement block are connected to the same
		// ports as the replaced block
		assertEquals(
				"The input ports of the replacement block must be connected to the same blocks as the ports of the replaced block",
				tlSimInPortMapping,
				obtainInPortMappingForBlock(model.getSubBlock(name)));
		assertTrue(
				"The output ports of the replacement block must be connected to the same blocks as the ports of the replaced block",
				areOutPortMappingsEqual(tlSimOutPortMapping,
						obtainOutPortMappingForBlock(model.getSubBlock(name))));
	}

	/**
	 * Obtains a mapping from port indices of input ports to the output ports
	 * they are connected to.
	 * 
	 * @param block
	 *            The block for which the mapping is obtained
	 * @return a Map that contains for each in-port index of the block the
	 *         connected out-port or null if its not connected.
	 */
	private static Map<String, SimulinkOutPort> obtainInPortMappingForBlock(
			SimulinkBlock block) {
		Map<String, SimulinkOutPort> portMapping = new HashMap<String, SimulinkOutPort>();
		for (SimulinkInPort inPort : block.getInPorts()) {
			portMapping.put(inPort.getIndex(), inPort.getLine().getSrcPort());
		}
		return portMapping;
	}

	/**
	 * Obtains a mapping from port indices of output ports to the input ports
	 * they are connected to.
	 * 
	 * @param block
	 *            The block for which the mapping is obtained
	 * @return a MultiMap that contains for each out-port index the in-ports to
	 *         which this out-port is connected
	 */
	private static ListMap<String, SimulinkInPort> obtainOutPortMappingForBlock(
			SimulinkBlock block) {
		ListMap<String, SimulinkInPort> portMapping = new ListMap<String, SimulinkInPort>();
		for (SimulinkOutPort outPort : block.getOutPorts()) {
			for (SimulinkLine line : outPort.getLines()) {
				portMapping.add(outPort.getIndex(), line.getDstPort());
			}
		}
		return portMapping;
	}

	/**
	 * Checks, if two OutPort mappings are equal. This is used as the collection
	 * types do not implement an equals method.
	 */
	private boolean areOutPortMappingsEqual(
			ListMap<String, SimulinkInPort> mapping,
			ListMap<String, SimulinkInPort> otherMapping) {

		UnmodifiableSet<String> mappingKeys = mapping.getKeys();
		UnmodifiableSet<String> otherMappingKeys = otherMapping.getKeys();

		// check if key sets equal
		if (!areSetsEqual(mappingKeys, otherMappingKeys)) {
			return false;
		}

		for (String key : mappingKeys) {

			// Check if each key is mapped to equal lists.
			// Use equals, as ListMap uses ArrayLists internally
			if (!mapping.getCollection(key).equals(
					otherMapping.getCollection(key))) {
				return false;
			}
		}
		return true;

	}

	/**
	 * Checks if two sets are equal. Two sets are equal if each is a sub-set of
	 * the other.
	 */
	private static <T> boolean areSetsEqual(Set<T> set, Set<T> otherSet) {
		return (set.containsAll(otherSet) && otherSet.containsAll(set));
	}

	/** Creates an IdentityHashSet from a collection */
	private static <T> IdentityHashSet<T> asIdentityHashSet(Collection<T> collection) {
		return new IdentityHashSet<T>(collection);
	}
}