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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;

import org.conqat.lib.commons.clone.DeepCloneException;
import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.logging.SimpleLogger;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.commons.test.DeepCloneTestUtils;
import org.conqat.lib.simulink.model.SimulinkAnnotation;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.SimulinkModelTest;
import org.conqat.lib.simulink.model.SimulinkOutPort;
import org.conqat.lib.simulink.testutils.SimulinkTestBase;
import org.conqat.lib.simulink.util.SimulinkIdProvider;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Base class for testing Simulink builder. This tests if the model is
 * <em>built</em> properly. Correct implementation of the model itself, e.g.
 * deep cloning, element removal, is tested in {@link SimulinkModelTest}.
 * <p>
 * The test specified here are far from complete but meant as basis to
 * conveniently add more tests.
 * 
 * @author $Author: schulte$
 * @version $Rev: 51750 $
 * @ConQAT.Rating GREEN Hash: B4A1E8877601B35A608E97DA9A463DB1
 */
public abstract class SimulinkModelBuilderTestBase extends SimulinkTestBase {

	/** Returns the moden name to use for a base name. */
	protected abstract String resolveModelName(String basename);

	/**
	 * Returns the names of the given lines as a single string with each line's
	 * name in a separate line.
	 */
	protected String listLineNames(Collection<SimulinkLine> lines) {
		List<String> names = new ArrayList<>();
		for (SimulinkLine line : lines) {
			String name = line.getParameter(SimulinkConstants.PARAM_Name);
			if (name == null) {
				name = "<unnamed>";
			}
			names.add(name);
		}
		return StringUtils.concat(CollectionUtils.sort(names), "\n");
	}

	/**
	 * Obtains a block specified by an id from the model and check if has the
	 * correct number of in and outports.
	 */
	protected SimulinkBlock checkBlock(SimulinkModel model, String id,
			int inPortCount, int outPortCount) {
		SimulinkBlock block = model.getBlock(id);
		assertNotNull(block);

		assertEquals(inPortCount, block.getInPorts().size());
		assertEquals(outPortCount, block.getOutPorts().size());

		return block;
	}

	/**
	 * Obtains a source and destination port from the model and ensures that
	 * they are connected by a line.
	 * 
	 * @return the connecting line
	 */
	protected SimulinkLine checkLine(SimulinkModel model, String sourceBlockId,
			String sourcePortIndex, String destBlockId, String destPortIndex) {

		SimulinkBlock srcBlock = model.getBlock(sourceBlockId);
		SimulinkBlock dstBlock = model.getBlock(destBlockId);

		SimulinkOutPort srcPort = srcBlock.getOutPort(sourcePortIndex);
		SimulinkInPort dstPort = dstBlock.getInPort(destPortIndex);

		for (SimulinkLine line : srcPort.getLines()) {
			if (line.getDstPort() == dstPort) {
				return line;
			}
		}

		fail("No line found between " + sourceBlockId + ":" + sourcePortIndex
				+ " and " + destBlockId + ":" + destPortIndex);
		return null;
	}

	/** Test if annotations are built correctly. */
	public void testAnnotations() throws SimulinkModelBuildingException,
			ZipException, IOException {
		SimulinkModel model = loadModel(resolveModelName("annotations"));
		String modelName = model.getName();

		assertEquals(2, model.getAnnotations().size());
		assertEquals(1, model.getBlock(modelName + "/Strecke").getAnnotations()
				.size());
		assertEquals(0, model.getBlock(modelName + "/Sum").getAnnotations()
				.size());

		SimulinkAnnotation annotation = model.getBlock(modelName + "/Strecke")
				.getAnnotations().iterator().next();
		assertEquals("DeepAnnotation", annotation.getName());

		// directly defined parameter
		assertTrue(annotation.getParameterNames().contains("Position"));
		assertEquals("[293, 181]", annotation.getParameter("Position"));

		// default parameter
		assertTrue(annotation.getParameterNames().contains("DropShadow"));
		assertEquals("off", annotation.getParameter("DropShadow"));
	}

	/** Test if blocks are built correctly. */
	public void testBlocks() throws SimulinkModelBuildingException,
			ZipException, IOException {
		SimulinkModel model = loadModel(resolveModelName("model01"));
		String modelName = model.getName();

		assertEquals(25, SimulinkUtils.createIdToNodeMap(model).size());

		checkBlock(model, modelName, 0, 0);
		SimulinkBlock block = checkBlock(model, modelName + "/Controller/PID",
				1, 1);
		checkBlock(model, modelName + "/Controller/PID/Sum1", 2, 1);

		// parameter is part of the "Block" section in the MDL file
		assertTrue(block.getParameterNames().contains("Position"));
		assertEquals("[230, 25, 270, 85]", block.getParameter("Position"));

		// parameter is part of the "System" section in the MDL file (the block
		// describes a subsystem)
		assertTrue(block.getParameterNames().contains("System.ZoomFactor"));
		assertEquals("100", block.getParameter("System.ZoomFactor"));

		// default parameter
		assertTrue(block.getParameterNames().contains("DropShadow"));
		assertEquals("off", block.getParameter("DropShadow"));
	}

	/** Test if lines are built correctly. */
	public void testLines() throws SimulinkModelBuildingException,
			ZipException, IOException {
		SimulinkModel model = loadModel(resolveModelName("model01"));
		String modelName = model.getName();

		checkLine(model, modelName + "/Controller/PID/Sum1", "1", modelName
				+ "/Controller/PID/Sum", "1");
		SimulinkLine line = checkLine(model,
				modelName + "/Controller/PID/Sum1", "1", modelName
						+ "/Controller/PID/I-Delay", "1");

		// parameter
		assertTrue(line.getParameterNames().contains("Name"));
		assertEquals("TestLine", line.getParameter("Name"));

		// default parameter
		assertTrue(line.getParameterNames().contains("FontName"));
		assertEquals("arial", line.getParameter("FontName"));
	}

	/** Tests handling of unconnected lines. */
	public void testUnconnectedLines() throws SimulinkModelBuildingException,
			DeepCloneException, ZipException, IOException {
		SimulinkModel model = loadModel(resolveModelName("unconnected_lines"));
		assertEquals("branched\nbranched\npartial",
				listLineNames(model.getContainedLinesRecursively()));

		// now do the same including unconnected lines
		try (SimulinkModelBuilder builder = new SimulinkModelBuilder(
				useTestFile(resolveModelName("unconnected_lines")),
				new SimpleLogger())) {
			model = builder.buildModel(new ModelBuildingParameters()
					.setPreserveUnconnectedLines(true));
			assertEquals(
					"branched\nbranched\nfree\nno dest\nno source\npartial\npartial",
					listLineNames(model.getContainedLinesRecursively()));

			// ensure that this also survives deep cloning
			SimulinkModel cloned = model.deepClone();
			assertEquals(
					"branched\nbranched\nfree\nno dest\nno source\npartial\npartial",
					listLineNames(cloned.getContainedLinesRecursively()));
			DeepCloneTestUtils.testDeepCloning(model, cloned,
					new SimulinkIdProvider(), "org.conqat.lib.simulink");
		}
	}

	/** Check if branches in lines are resolved correctly. */
	public void testBranching() throws SimulinkModelBuildingException,
			ZipException, IOException {
		SimulinkModel model = loadModel(resolveModelName("branching"));
		String modelName = model.getName();

		for (String out : Arrays.asList("/Out1", "/Out2", "/Out3", "/Out4")) {
			checkLine(model, modelName + "/In1", "1", modelName + out, "1");
		}
	}

	/**
	 * Test if ports are built correctly. This test loads a special MDL file
	 * that includes all block types and guarantees that all ports of the blocks
	 * are connected. This test iterates over all ports and checks if they are
	 * connected. If we find an unconnected port, this must have been made up by
	 * the builder. If the builder would built to few ports, an exception is
	 * thrown as lines cannot be connected.
	 */
	public void testPorts() throws SimulinkModelBuildingException,
			ZipException, IOException {
		SimulinkModel model = loadModel(resolveModelName("ports"));

		Set<SimulinkBlock> blocks = DeepCloneTestUtils.getAllReferencedObjects(
				model, SimulinkBlock.class, SimulinkBlock.class.getPackage()
						.getName());

		for (SimulinkBlock block : blocks) {
			if (block.getType().equals("SubSystem")) {
				continue;
			}
			for (SimulinkInPort inPort : block.getInPorts()) {
				assertNotNull("Type " + block.getType(), inPort.getLine());
			}
			for (SimulinkOutPort outPort : block.getOutPorts()) {
				assertFalse("Type " + block.getType(), outPort.getLines()
						.isEmpty());
			}
		}
	}

	/** Check if unknown port types are reported correctly. */
	public void testUnknownPorts() throws ZipException, IOException {
		try {
			loadModel(resolveModelName("internal_dc_motor_lib"));
			fail("Error for unknwon port expected.");
		} catch (SimulinkModelBuildingException e) {
			assertTrue(e.getMessage().contains("RCONN"));
		}
	}
}