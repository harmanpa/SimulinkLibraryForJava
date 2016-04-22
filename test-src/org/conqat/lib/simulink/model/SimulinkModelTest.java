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
package org.conqat.lib.simulink.model;

import java.util.ArrayList;
import java.util.Set;

import org.conqat.lib.commons.test.DeepCloneTestUtils;
import org.conqat.lib.simulink.testutils.SimulinkTestBase;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Test for the Stateflow part of the model.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51755 $
 * @ConQAT.Rating GREEN Hash: 01A82906BBE0E40D61F6210E35695491
 */
public class SimulinkModelTest extends SimulinkTestBase {

	/** Model under test. */
	private SimulinkModel model;

	/** Load model and set block. */
	@Override
	public void setUp() throws Exception {
		model = loadModel("remove.mdl");
	}

	/** Test if annotation removal works. */
	public void testRemoveAnnotation() {
		Set<SimulinkAnnotation> annotations = DeepCloneTestUtils
				.getAllReferencedObjects(model, SimulinkAnnotation.class,
						getClass().getPackage().getName());

		for (SimulinkAnnotation annotation : annotations) {
			SimulinkBlock parent = (SimulinkBlock) annotation.getParent();
			annotation.remove();
			assertFalse(parent.getAnnotations().contains(annotation));
			assertNull(annotation.getParent());
		}
	}

	/** Test if block removal works. */
	public void testRemoveBlock() {
		// this is not done in a recursive manner as we would need to care about
		// removal order
		for (SimulinkBlock block : new ArrayList<SimulinkBlock>(
				model.getSubBlocks())) {
			SimulinkBlock parent = block.getParent();
			block.remove();
			assertNull(block.getParent());
			assertFalse(parent.getSubBlocks().contains(block));
			assertTrue(block.getSubBlocks().isEmpty());
			assertTrue(block.getOutPorts().isEmpty());
			assertTrue(block.getInPorts().isEmpty());
		}
	}

	/** Test if inport removal works. */
	public void testRemoveInPort() {
		Set<SimulinkInPort> inPorts = DeepCloneTestUtils
				.getAllReferencedObjects(model, SimulinkInPort.class,
						getClass().getPackage().getName());

		for (SimulinkInPort inPort : inPorts) {
			SimulinkBlock block = inPort.getBlock();
			inPort.remove();
			assertFalse(block.getOutPorts().contains(inPort));
			assertNull(inPort.getLine());
			assertNull(inPort.getBlock());
		}
	}

	/** Test if line removal works. */
	public void testRemoveLine() {
		Set<SimulinkLine> lines = DeepCloneTestUtils.getAllReferencedObjects(
				model, SimulinkLine.class, getClass().getPackage().getName());

		for (SimulinkLine line : lines) {
			SimulinkOutPort src = line.getSrcPort();
			SimulinkInPort dst = line.getDstPort();
			line.remove();
			assertFalse(src.getLines().contains(line));
			assertNull(dst.getLine());
			assertNull(line.getSrcPort());
			assertNull(line.getDstPort());
		}
	}

	/** Test if out port removal works. */
	public void testRemoveOutPort() {
		Set<SimulinkOutPort> outPorts = DeepCloneTestUtils
				.getAllReferencedObjects(model, SimulinkOutPort.class,
						getClass().getPackage().getName());

		for (SimulinkOutPort outPort : outPorts) {
			SimulinkBlock block = outPort.getBlock();
			outPort.remove();
			assertFalse(block.getOutPorts().contains(outPort));
			assertTrue(outPort.getLines().isEmpty());
			assertNull(outPort.getBlock());
		}
	}

	/**
	 * Test if deep cloning works for flattened models, i.e. models with
	 * cross-subsystem lines. This does not contain any assertions but rather
	 * checks if deep cloning throws any exception.
	 */
	public void testDeepCloneForFlattenedmodels() throws Exception {
		model = loadModel("cross_subsystem_line.mdl");
		SimulinkBlock in = model
				.getBlock("cross_subsystem_line/Subsystem1/In1");
		SimulinkBlock out = model
				.getBlock("cross_subsystem_line/Subsystem2/Out1");

		SimulinkOutPort outPort = new SimulinkOutPort(in, "2");
		SimulinkInPort inPort = new SimulinkInPort(out, "2");

		new SimulinkLine(outPort, inPort,
				SimulinkUtils.getLowestCommonAncestor(in, out));
		assertNotNull(model.deepClone());
	}
}
