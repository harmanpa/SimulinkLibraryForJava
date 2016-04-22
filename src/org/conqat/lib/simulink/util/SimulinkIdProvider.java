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

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.collections.IIdProvider;
import org.conqat.lib.commons.test.DeepCloneTestUtils;
import org.conqat.lib.simulink.model.SimulinkAnnotation;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkElementBase;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkObject;
import org.conqat.lib.simulink.model.SimulinkPortBase;
import org.conqat.lib.simulink.model.stateflow.IStateflowElement;
import org.conqat.lib.simulink.model.stateflow.StateflowElementBase;
import org.conqat.lib.simulink.model.stateflow.StateflowTransition;

/**
 * Id provider to be used for {@link DeepCloneTestUtils}.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 50406 $
 * @ConQAT.Rating GREEN Hash: 1CF13614BEA41F07B4B4D60FCFB77435
 */
public class SimulinkIdProvider implements IIdProvider<String, Object> {

	/** Parameter for the object ID of an object. */
	private static final String OBJECT_ID_PARAMETER = "$ObjectID";

	/** {@inheritDoc} */
	@Override
	public String obtainId(Object object) {

		// Annotations do not always have a locally unique name. Hence we also
		// use the position to differentiate.
		if (object instanceof SimulinkAnnotation) {
			SimulinkAnnotation annotation = (SimulinkAnnotation) object;
			return annotation.getName() + "@"
					+ annotation.getParameter(SimulinkConstants.PARAM_Position);
		}

		// The same holds for objects, where we use the object id
		if (object instanceof SimulinkObject) {
			String objectId = ((SimulinkObject) object)
					.getParameter(OBJECT_ID_PARAMETER);
			CCSMAssert.isNotNull(objectId);
			return objectId;
		}

		if (object instanceof SimulinkElementBase) {
			return ((SimulinkElementBase) object).getName();
		}
		if (object instanceof StateflowElementBase<?>) {
			return ((IStateflowElement<?>) object).getStateflowId();
		}
		if (object instanceof SimulinkPortBase) {
			SimulinkPortBase port = (SimulinkPortBase) object;
			return port.getBlock().getId() + "-" + port.getIndex();
		}
		if (object instanceof SimulinkLine) {
			SimulinkLine line = (SimulinkLine) object;
			return getLineId(line);
		}
		if (object instanceof StateflowTransition) {
			StateflowTransition transition = (StateflowTransition) object;

			if (transition.getSrc() == null) {
				return "null-" + obtainId(transition.getDst());
			}

			return obtainId(transition.getSrc()) + "-"
					+ obtainId(transition.getDst());
		}

		throw new AssertionError("Unknown type " + object.getClass());
	}

	/** Returns an ID for a line. */
	private String getLineId(SimulinkLine line) {
		String sourceId = "<unconnected>";
		if (line.getSrcPort() != null) {
			sourceId = obtainId(line.getSrcPort());
		}

		String targetId = "<unconnected>";
		if (line.getDstPort() != null) {
			targetId = obtainId(line.getDstPort());
		}

		return sourceId + "-" + targetId;
	}
}
