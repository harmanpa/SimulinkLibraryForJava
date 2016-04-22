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
package org.conqat.lib.simulink.model;

import java.util.Set;

import org.conqat.lib.commons.test.ADeepCloneTestExclude;
import org.conqat.lib.simulink.model.datahandler.AnnotationLayoutData;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;

/**
 * Class for Simulink annotations, which are basically comments in the Simulink
 * model.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51470 $
 * @ConQAT.Rating GREEN Hash: 6D31B3885CC5E72B7359A2995535691E
 */
public class SimulinkAnnotation extends SimulinkElementBase {

	/**
	 * Name used for anonymous annotations (i.e. those without name parameter
	 * set).
	 */
	public static final String ANONYMOUS_ANNOTATION_NAME = "<unnamed annotation>";

	/** Create annotation. */
	public SimulinkAnnotation() {
		// only required to support default construction
	}

	/** Create annotation from other annotation (for deep cloning). */
	private SimulinkAnnotation(SimulinkAnnotation other) {
		super(other);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This may return {@link #ANONYMOUS_ANNOTATION_NAME} if the annotation has
	 * no name defined.
	 */
	@Override
	public String getName() {
		String name = super.getName();
		if (name == null) {
			return ANONYMOUS_ANNOTATION_NAME;
		}
		return name;
	}

	/**
	 * Get annotation default parameter.
	 */
	@Override
	/* package */String getDefaultParameter(String name) {
		return getModel().getAnnotationDefaultParameter(name);
	}

	/**
	 * Get annotation default parameter names.
	 */
	@Override
	/* package */Set<String> getDefaultParameterNames() {
		return getModel().getAnnotationDefaultParameterNames();
	}

	/** Deep clone annotation. */
	@Override
	public SimulinkAnnotation deepClone() {
		return new SimulinkAnnotation(this);
	}

	/**
	 * Returns the layout data for this annotation. This data is parsed from the
	 * model with each call, so repeated access should be avoided by storing the
	 * result in a local variable.
	 */
	@ADeepCloneTestExclude
	public AnnotationLayoutData obtainLayoutData() {
		return getModel().getModelDataHandler()
				.obtainAnnotationLayoutData(this);
	}

	/**
	 * Returns the label data for this annotation. This data is parsed from the
	 * model with each call, so repeated access should be avoided by storing the
	 * result in a local variable.
	 */
	@ADeepCloneTestExclude
	public LabelLayoutData obtainLabelData() {
		return getModel().getModelDataHandler().obtainAnnotationLabelData(this);
	}
}