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
 * Test class for the Stateflow builder. This tests if the model in <b>.mdl
 * format</b> is <em>built</em> properly. The test specified here are far from
 * complete but meant as basis to conveniently add more tests.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51754 $
 * @ConQAT.Rating GREEN Hash: D11BAC16285E033CABF17C4A0C39A435
 */
public class StateflowModelBuilderMDLTest extends StateflowModelBuilderTestBase {

	/** {@inheritDoc} */
	@Override
	protected String getTestModelName() {
		return "model02.mdl";
	}

	/** {@inheritDoc} */
	@Override
	protected void additionalMachineAsserts(StateflowTarget target) {
		assertEquals("85", target.getStateflowId());
		assertEquals("Default Simulink S-Function Target.",
				target.getParameter("description"));
	}

	/** {@inheritDoc} */
	@Override
	protected String resolveId(ETestModelId id) {
		switch (id) {
		case HOTEL_STATEFLOW:
			return "7";
		case ASSEMBLY_POINT:
			return "12";
		case HISTORY_JUNCTION:
			return "21";
		case JUNCTION1:
			return "18";
		case JUNCTION2:
			return "19";
		case JUNCTION3:
			return "20";
		case JUNCTION4:
			return "17";
		case FAMILY_SUITE:
			return "9";
		case SMOKING_ROOM:
			return "4";
		case NON_SMOKING_ROOM:
			return "3";
		case FUNC_STATE1:
			return "16";
		case FUNC_STATE2:
			return "14";
		}
		throw new AssertionError("Unknown ID " + id);
	}
}