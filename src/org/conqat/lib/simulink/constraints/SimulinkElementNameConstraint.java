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
package org.conqat.lib.simulink.constraints;

import org.conqat.lib.commons.constraint.ConstraintViolationException;
import org.conqat.lib.commons.constraint.ILocalConstraint;
import org.conqat.lib.simulink.model.SimulinkElementBase;
import org.conqat.lib.simulink.model.SimulinkObject;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * This constraint checks if a Simulink element has a non-empty name and ensures
 * that the name does not start or end with a slash. This is a mandatory
 * constraint.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 50436 $
 * @ConQAT.Rating GREEN Hash: 26E09C9257E0B11B4B4827D0D5E68ED5
 */
public class SimulinkElementNameConstraint implements
		ILocalConstraint<SimulinkElementBase> {

	/** {@inheritDoc} */
	@Override
	public void checkLocalConstraint(SimulinkElementBase element)
			throws ConstraintViolationException {
		if (element instanceof SimulinkObject) {
			// objects are unnamed
			return;
		}

		String name = element.getName();
		if (name == null || name.length() == 0) {
			throw new ConstraintViolationException("Element " + element
					+ " has no name.", element);
		}
		if (SimulinkUtils.startsOrEndsWithSeparator(name)) {
			throw new ConstraintViolationException("Name of element " + element
					+ " starts or ends with a slash.", element);
		}
	}
}