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
package org.conqat.lib.simulink.model.datahandler;

import org.conqat.lib.simulink.builder.ModelBuildingParameters;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkElementBase;

/**
 * {@link ModelDataHandlerBase} implementation for the 2009a version of
 * Simulink.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51950 $
 * @ConQAT.Rating GREEN Hash: 1E736B57223873FA1F024B54DC15934A
 */
/* package */class ModelDataHandler2009a extends ModelDataHandlerBase {

	/** Constructor. */
	public ModelDataHandler2009a(ModelBuildingParameters parameters) {
		super(parameters);
	}

	/** {@inheritDoc} */
	@Override
	protected EOrientation extractOrientation(SimulinkElementBase element) {
		String rotationValue = element
				.getParameter(SimulinkConstants.PARAM_BlockRotation);

		if (rotationValue == null) {
			return EOrientation.RIGHT;
		}

		EOrientation orientation = EOrientation.fromRotationValue(
				rotationValue, logger);
		String mirrorValue = element
				.getParameter(SimulinkConstants.PARAM_BlockMirror);
		if (SimulinkConstants.VALUE_on.equals(mirrorValue)) {
			orientation = orientation.getOpposite();
		}
		return orientation;
	}
}
