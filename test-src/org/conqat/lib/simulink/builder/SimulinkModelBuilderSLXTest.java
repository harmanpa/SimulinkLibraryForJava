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
 * Performs model building checks for the <b>.slx format</b>. See
 * {@link SimulinkModelBuilderTestBase}.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51750 $
 * @ConQAT.Rating GREEN Hash: 64361427F6C7A5BE8A3E28750011E473
 */
public class SimulinkModelBuilderSLXTest extends SimulinkModelBuilderTestBase {

	/** {@inheritDoc} */
	@Override
	protected String resolveModelName(String basename) {
		return basename + "_2013a" + SimulinkModelBuilder.SLX_FILE_EXTENSION;
	}
}