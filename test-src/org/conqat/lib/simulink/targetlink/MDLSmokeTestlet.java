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

import org.conqat.lib.commons.logging.SimpleLogger;
import org.conqat.lib.commons.test.TestletBase;
import org.conqat.lib.simulink.builder.SimulinkModelBuilder;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.util.SimulinkUtils;
import org.junit.Ignore;

/**
 * Smoke testlet for Simulink/Stateflow. This builds a Simulink file and runs
 * the {@link TargetLinkDataResolver} on all blocks.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51961 $
 * @ConQAT.Rating GREEN Hash: D299B57770B9CA23FE563A947DE0A436
 */
@Ignore
public class MDLSmokeTestlet extends TestletBase {

	/** Name of file to read. */
	private final String filename;

	/** Create new testlet. */
	MDLSmokeTestlet(String filename) {
		this.filename = filename;
	}

	/** Return name smoke test. */
	@Override
	public String getName() {
		return filename;
	}

	/**
	 * Builds a Simulink file and run the {@link TargetLinkDataResolver} on all
	 * blocks.
	 */
	@Override
	public void test() throws Exception {
		try (SimulinkModelBuilder builder = new SimulinkModelBuilder(
				useTestFile(filename), new SimpleLogger())) {
			SimulinkModel model = builder.buildModel();
			SimulinkUtils.visitDepthFirst(model, new TargetLinkDataResolver());
		}
	}
}