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
package org.conqat.lib.simulink.builder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipException;

import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.model.ParameterizedElement;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.testutils.SimulinkTestBase;

/**
 * Tests for _2013a.mdl and _2013a.slx comparison. This tests if a model
 * constructed from <b>_2013a.slx format</b> <em>equals</em> a model constructed
 * from <b>_2013a.mdl format</b>.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51954 $
 * @ConQAT.Rating GREEN Hash: 18EDB08E4357C379BF1E73E3334BAD31
 */
public class SLXMDLModelComparisonTest extends SimulinkTestBase {

	/** The names of parameters that are ignored during comparison. */
	private static final Set<String> IGNORED_PARAMETERS = new HashSet<>(
			Arrays.asList("SID", "System.SIDHighWatermark",
					"RTWModifiedTimeStamp", "LastModifiedDate",
					"LastModifiedBy", "MdlSubVersion", "Version", "Created",
					"Creator", "ConfigurationManager", "System.Name",
					"System.Location", "SigBuilderData", "VnvData"));

	/**
	 * Test that the 'annotation' model is built equally in slx and mdl format.
	 */
	public void testAnnotationsModel() throws SimulinkModelBuildingException,
			ZipException, IOException {
		assertModelsAreEqual("annotations_2013a");
	}

	/**
	 * Test that the 'branching' model is built equally in slx and mdl format.
	 */
	public void testBranchingModel() throws SimulinkModelBuildingException,
			ZipException, IOException {
		assertModelsAreEqual("branching_2013a");
	}

	/**
	 * Test that the 'model01' model is built equally in slx and mdl format.
	 */
	public void testModel01Model() throws SimulinkModelBuildingException,
			ZipException, IOException {
		assertModelsAreEqual("model01_2013a");
	}

	/**
	 * Test that the 'model02' model is built equally in slx and mdl format.
	 */
	public void testModel02Model() throws SimulinkModelBuildingException,
			ZipException, IOException {
		assertModelsAreEqual("model02_2013a");
	}

	/**
	 * Test that the 'unconnected_lines' model is built equally in slx and mdl
	 * format.
	 */
	public void testUnconnectedLinesModel()
			throws SimulinkModelBuildingException, ZipException, IOException {
		assertModelsAreEqual("unconnected_lines_2013a");
	}

	/**
	 * Asserts the the MDL and SLX files with given basename are the same after
	 * parsing.
	 */
	private void assertModelsAreEqual(String modelBaseName)
			throws ZipException, SimulinkModelBuildingException, IOException {
		SimulinkModel mdlModel = loadModel(modelBaseName
				+ SimulinkModelBuilder.MDL_FILE_EXTENSION);
		SimulinkModel slxModel = loadModel(modelBaseName
				+ SimulinkModelBuilder.SLX_FILE_EXTENSION);
		assertModelsAreEqual(mdlModel, slxModel);
	}

	/** Assert that models are equal in sub blocks and parameters. */
	private static void assertModelsAreEqual(SimulinkModel mdlModel,
			SimulinkModel slxModel) throws UnsupportedEncodingException {
		assertParametersAreEqual(extractParameters(mdlModel),
				extractParameters(slxModel));

		for (SimulinkBlock mdlBlock : mdlModel.getSubBlocks()) {

			String name = mdlBlock.getName();
			assertThat(name, notNullValue());

			SimulinkBlock slxBlock = slxModel
					.getSubBlock(unescapeNewlines(name));
			assertThat("Not block found in SLX for " + name, slxBlock,
					notNullValue());

			assertBlocksAreEqual(mdlBlock, slxBlock);
		}
	}

	/** Assert that blocks are equal. */
	private static void assertBlocksAreEqual(SimulinkBlock mdlBlock,
			SimulinkBlock slxBlock) throws UnsupportedEncodingException {
		assertParametersAreEqual(extractParameters(mdlBlock),
				extractParameters(slxBlock));

		for (SimulinkBlock mdlSubBlock : mdlBlock.getSubBlocks()) {

			String name = mdlSubBlock.getName();
			assertThat(name, notNullValue());

			SimulinkBlock slxSubBlock = slxBlock
					.getSubBlock(unescapeNewlines(name));
			assertThat(slxBlock, notNullValue());

			assertBlocksAreEqual(mdlSubBlock, slxSubBlock);
		}
	}

	/** Extract parameter map from immutable element. */
	private static Map<String, String> extractParameters(
			ParameterizedElement element) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		for (String paramName : element.getParameterNames()) {
			String paramValue = element.getParameter(paramName);
			params.put(
					StringUtils.removeWhitespace(unescapeNewlines(paramName)),
					StringUtils.removeWhitespace(unescapeNewlines(paramValue)));
		}
		return params;
	}

	/** Compare parameter maps, ignoring not supported parameters. */
	private static void assertParametersAreEqual(Map<String, String> mdlParams,
			Map<String, String> slxParams) {
		for (Entry<String, String> mdlParam : mdlParams.entrySet()) {
			if (!IGNORED_PARAMETERS.contains(mdlParam.getKey())) {
				String key = mdlParam.getKey();
				String value = mdlParam.getValue();

				assertThat(key, notNullValue());
				assertThat(value, notNullValue());

				key = unescapeNewlines(key);
				value = unescapeNewlines(value);

				assertThat(slxParams, hasEntry(key, value));
			}
		}
	}
}