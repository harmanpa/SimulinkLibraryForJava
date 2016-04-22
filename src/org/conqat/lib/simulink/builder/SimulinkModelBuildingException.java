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

/**
 * This class signals an exception that occurred during build of the Simulink
 * model.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 50844 $
 * @ConQAT.Rating GREEN Hash: 0B9EE8873CA578F787A5FD252F595952
 */
public class SimulinkModelBuildingException extends Exception {

	/** Version used for serialization. */
	private static final long serialVersionUID = 1;

	/** Create new exception. */
	public SimulinkModelBuildingException(String message) {
		super(message);
	}

	/** Create new exception. */
	public SimulinkModelBuildingException(String message, MDLSection section) {
		super(message + " [line: " + section.getLineNumber() + "]");
	}

	/** Create new exception. */
	public SimulinkModelBuildingException(Throwable throwable) {
		super(throwable);
	}

	/** Create new exception. */
	public SimulinkModelBuildingException(String message, Throwable throwable) {
		super(message, throwable);
	}

}