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

import java.io.File;

import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.builder.SimulinkModelBuilder;
import org.conqat.lib.simulink.model.datahandler.ModelDataHandlerBase;

/**
 * Information on a referenced block.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51736 $
 * @ConQAT.Rating GREEN Hash: BC9C9407D8953453BB869781F050EB4B
 */
public class ReferencedBlockInfo {

	/** Extensions to be tested when looking for referenced models. */
	private static final String[] TESTED_EXTENSIONS = {
			StringUtils.EMPTY_STRING, SimulinkModelBuilder.MDL_FILE_EXTENSION,
			SimulinkModelBuilder.SLX_FILE_EXTENSION };

	/** Simple name of the referenced model. */
	private final String modelName;

	/** Fully qualified name of the block within the model. */
	private final String blockName;

	/**
	 * File for the referenced model. This may be null if the file could not be
	 * found.
	 */
	private final File modelFile;

	/** Constructor. */
	/* package */ReferencedBlockInfo(String modelName, String blockName,
			ModelDataHandlerBase modelDataHandler) {
		this.modelName = modelName;
		this.blockName = blockName;
		this.modelFile = locateModelFile(modelName, modelDataHandler);
	}

	/**
	 * Attempts to find the file for a given model name. Returns the
	 * corresponding file or null.
	 */
	private File locateModelFile(String modelName,
			ModelDataHandlerBase modelDataHandler) {
		for (File basePath : modelDataHandler.getReferenceDirectories()) {
			for (String extension : TESTED_EXTENSIONS) {
				File modelFile = new File(basePath, modelName + extension);
				if (modelFile.isFile()) {
					return modelFile;
				}
			}
		}

		return null;
	}

	/** Returns the simple name of the referenced model. */
	public String getModelName() {
		return modelName;
	}

	/** Returns the fully qualified name of the block within the model. */
	public String getBlockName() {
		return blockName;
	}

	/**
	 * Returns the file for the referenced model. This may be null if the file
	 * could not be found.
	 */
	public File getModelFile() {
		return modelFile;
	}

	/** Returns whether the model file for the referenced model has been found. */
	public boolean isModelFileFound() {
		return modelFile != null;
	}
}