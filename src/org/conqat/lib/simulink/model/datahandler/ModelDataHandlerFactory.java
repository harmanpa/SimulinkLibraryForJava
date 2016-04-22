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

import org.conqat.lib.commons.error.FormatException;
import org.conqat.lib.commons.version.Version;
import org.conqat.lib.simulink.builder.ModelBuildingParameters;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;

/**
 * Factory for creating the {@link ModelDataHandlerBase} for a given model. This
 * uses the version information contained in the model.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51482 $
 * @ConQAT.Rating GREEN Hash: C74E64B1DE46C90BFA4D0C74007047FC
 */
public class ModelDataHandlerFactory {

	/**
	 * Returns a {@link ModelDataHandlerBase} based on the model version and
	 * parameters provided. This is expected to be only called from the builder
	 * package.
	 * 
	 * @param versionString
	 *            the version expected in MAJOR.MINOR format. An exception is
	 *            thrown if this is null or does not follow this format.
	 */
	public static ModelDataHandlerBase createModelHandler(String versionString,
			boolean isSlxFormat, ModelBuildingParameters parameters)
			throws SimulinkModelBuildingException {
		if (versionString == null) {
			throw new SimulinkModelBuildingException(
					"Model version parameter missing in model!");
		}

		try {
			Version version = Version.parseVersion(versionString);
			return createModelHandler(version, isSlxFormat, parameters);
		} catch (FormatException e) {
			throw new SimulinkModelBuildingException(
					"Could not parse simulink model version: " + versionString,
					e);
		}
	}

	/**
	 * Returns a {@link ModelDataHandlerBase} based on the model version and
	 * parameters provided.
	 */
	private static ModelDataHandlerBase createModelHandler(Version version,
			boolean isSlxFormat, ModelBuildingParameters parameters) {
		// 7.0 is the 2007 version of Matlab
		if (!isSlxFormat && version.getMajor() <= 7) {
			return new ModelDataHandler2008b(parameters);
		}
		return new ModelDataHandler2009a(parameters);
	}
}
