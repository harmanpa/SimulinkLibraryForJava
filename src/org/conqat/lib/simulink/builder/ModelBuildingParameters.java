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
package org.conqat.lib.simulink.builder;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.conqat.lib.commons.logging.ILogger;
import org.conqat.lib.commons.logging.SimpleLogger;

/**
 * Parameter class that controls how certain cases during model building should
 * be handled. The default values provided during initialization already are a
 * useful starting point.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51947 $
 * @ConQAT.Rating GREEN Hash: F4B992CD1C403DFD671703EC67EE13CC
 */
public class ModelBuildingParameters {

	/**
	 * Whether to preserve unconnected lines in the model. Default is false, as
	 * they are not helpful for analysis. However, when rendering lines, you
	 * might want to include unconnected lines as well.
	 */
	private boolean preserveUnconnectedLines = false;

	/**
	 * The charset to be used for parsing the MDL file (required, as this is
	 * plain text).
	 */
	private Charset charset = Charset.defaultCharset();

	/** The logger to be used during model construction. */
	private ILogger logger = new SimpleLogger();

	/**
	 * The directories to search referenced models in. Default contains the JVM
	 * working directory.
	 */
	private final List<File> referenceDirectories = new ArrayList<>(
			Arrays.asList(new File(".")));

	/**
	 * Creates a new instance of model building parameters with the following
	 * default values:
	 * <ul>
	 * <li>Preserve unconnected lines: false</li>
	 * <li>Charset: platform default</li>
	 * <li>Logger: SimpleLogger (console)</li>
	 * <li>Reference lookup directories: JVM working directory.</li>
	 * </ul>
	 */
	public ModelBuildingParameters() {
		// this is just provided to have a point to document defaults
	}

	/** Returns {@link #preserveUnconnectedLines}. */
	public boolean isPreserveUnconnectedLines() {
		return preserveUnconnectedLines;
	}

	/**
	 * Sets {@link #preserveUnconnectedLines}. Returns <code>this</code> to
	 * allow chaining.
	 */
	public ModelBuildingParameters setPreserveUnconnectedLines(
			boolean preserveUnconnectedLines) {
		this.preserveUnconnectedLines = preserveUnconnectedLines;
		return this;
	}

	/**
	 * Returns the logger to be used for reporting errors during model
	 * construction and interpretation.
	 */
	public ILogger getLogger() {
		return logger;
	}

	/**
	 * Sets the logger to be used for reporting errors during model construction
	 * and interpretation.
	 */
	/* package */ModelBuildingParameters setLogger(ILogger logger) {
		this.logger = logger;
		return this;
	}

	/** Returns {@link #charset}. */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * Sets {@link #charset}. Returns <code>this</code> to allow chaining.
	 */
	public ModelBuildingParameters setCharset(Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * Returns the directory paths that should be searched when looking for
	 * references. The returned list may be modified.
	 */
	public List<File> getReferencePaths() {
		return referenceDirectories;
	}

	/**
	 * Adds a path to search for referenced models. Returns <code>this</code> to
	 * allow chaining.
	 */
	public ModelBuildingParameters addReferencePath(File path) {
		referenceDirectories.add(path);
		return this;
	}
}
