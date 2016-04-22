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
package org.conqat.lib.simulink.model;


/**
 * Class for Simulink objects, which are a construct for structured storage of
 * meta-data in the model. It seems they were introduced in a later version of
 * the Simulink file format.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 50251 $
 * @ConQAT.Rating GREEN Hash: D761A4072A8FFAFB644CC119C238B1E7
 */
public class SimulinkObject extends SimulinkElementBase {

	/** Default constructor. */
	public SimulinkObject() {
		// required to preserve default constructor.
	}

	/** Copy constructor. */
	private SimulinkObject(SimulinkObject other) {
		super(other);
	}

	/** {@inheritDoc} */
	@Override
	public SimulinkObject deepClone() {
		return new SimulinkObject(this);
	}

}
