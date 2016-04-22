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
package org.conqat.lib.simulink.testutils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.ZipException;

import org.conqat.lib.commons.logging.SimpleLogger;
import org.conqat.lib.commons.test.CCSMTestCaseBase;
import org.conqat.lib.simulink.builder.ModelBuildingParameters;
import org.conqat.lib.simulink.builder.SimulinkModelBuilder;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.stateflow.IStateflowNodeContainer;
import org.conqat.lib.simulink.model.stateflow.StateflowNodeBase;

/**
 * Base class for Simulink tests.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51962 $
 * @ConQAT.Rating GREEN Hash: 35DA1E211EF3D1EDEA2A8B9EED66BFF5
 */
public abstract class SimulinkTestBase extends CCSMTestCaseBase {

	/** Load Simulink model. */
	protected SimulinkModel loadModel(String filename)
			throws SimulinkModelBuildingException, ZipException, IOException {
		ModelBuildingParameters parameters = new ModelBuildingParameters();
		return loadModel(filename, parameters);
	}

	/** Load Simulink model. */
	protected SimulinkModel loadModel(String filename,
			ModelBuildingParameters parameters)
			throws SimulinkModelBuildingException, ZipException, IOException {
		try (SimulinkModelBuilder builder = new SimulinkModelBuilder(
				useTestFile(filename), new SimpleLogger())) {
			return builder.buildModel(parameters.setCharset(Charset
					.forName("windows-1252")));
		}
	}

	/** Get child node with specified id. Returns null if none was found. */
	protected StateflowNodeBase getStateflowNode(
			IStateflowNodeContainer<?> container, String id) {
		for (StateflowNodeBase element : container.getNodes()) {
			if (element.getStateflowId().equals(id)) {
				return element;
			}
		}
		return null;
	}

	/** Replace escaped newline characters. */
	protected static String unescapeNewlines(String string) {
		return string.replaceAll("\\\\n", "\n");
	}
}