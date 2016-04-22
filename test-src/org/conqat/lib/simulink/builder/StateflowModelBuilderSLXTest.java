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

import org.conqat.lib.simulink.model.stateflow.StateflowTarget;

/**
 * Test class for the Stateflow builder. This tests if the model in <b>.slx
 * format</b> is <em>built</em> properly. The test specified here are far from
 * complete but meant as basis to conveniently add more tests.
 * 
 * @author $Author: hummelb $
 * @version $Rev: 52208 $
 * @ConQAT.Rating GREEN Hash: 66A6B52DB640AB60221CB21D143CF066
 */
public class StateflowModelBuilderSLXTest extends StateflowModelBuilderTestBase {

	/** {@inheritDoc} */
	@Override
	protected String getTestModelName() {
		return "model02_2013a.slx";
	}

	/** {@inheritDoc} */
	@Override
	protected void additionalMachineAsserts(StateflowTarget target) {
		assertEquals("167", target.getStateflowId());
	}

	/** {@inheritDoc} */
	@Override
	protected String resolveId(ETestModelId id) {
		switch (id) {
		case HOTEL_STATEFLOW:
			return "84:5";
		case ASSEMBLY_POINT:
			return "84:10";
		case HISTORY_JUNCTION:
			return "84:55";
		case JUNCTION1:
			return "84:52";
		case JUNCTION2:
			return "84:53";
		case JUNCTION3:
			return "84:54";
		case JUNCTION4:
			return "84:21";
		case FAMILY_SUITE:
			return "84:7";
		case SMOKING_ROOM:
			return "84:2";
		case NON_SMOKING_ROOM:
			return "84:1";
		case FUNC_STATE1:
			return "84:19";
		case FUNC_STATE2:
			return "84:14";
		}
		throw new AssertionError("Unknown ID " + id);
	}

}