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

import org.conqat.lib.commons.assertion.CCSMPre;
import org.conqat.lib.commons.test.ADeepCloneTestExclude;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.model.datahandler.LineLayoutData;

/**
 * A Simulink line.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51949 $
 * @ConQAT.Rating GREEN Hash: 465EC708978254A28BDAA5A0F45F0CD1
 */
public class SimulinkLine extends ParameterizedElement {

	/**
	 * The source port of this line. This may be null to indicate an unconnected
	 * line.
	 */
	private SimulinkOutPort sourcePort;

	/**
	 * The target port of this line. This may be null to indicate an unconnected
	 * line.
	 */
	private SimulinkInPort destPort;

	/** The block containing the line. */
	private SimulinkBlock container;

	/**
	 * Creates a new line. This adds the line to the ports.
	 * 
	 * @param sourcePort
	 *            the source port (or null for unconnected source).
	 * @param destPort
	 *            the destination port (or null for unconnected destination).
	 * @param container
	 *            the containing block (never null).
	 */
	public SimulinkLine(SimulinkOutPort sourcePort, SimulinkInPort destPort,
			SimulinkBlock container) {
		CCSMPre.isNotNull(container, "Container must not be null!");

		this.sourcePort = sourcePort;
		this.destPort = destPort;
		this.container = container;

		if (sourcePort != null) {
			sourcePort.addLine(this);
		}

		if (destPort != null) {
			destPort.setLine(this);
		}

		container.addLine(this);
	}

	/** Returns target port or null for unconnected target. */
	public SimulinkInPort getDstPort() {
		return destPort;
	}

	/** Returns the containing block for this line. */
	public SimulinkBlock getContainer() {
		return container;
	}

	/** Get model this line belongs to. */
	public SimulinkModel getModel() {
		return container.getModel();
	}

	/** Returns source port or null for unconnected source. */
	public SimulinkOutPort getSrcPort() {
		return sourcePort;
	}

	/**
	 * Remove the line from the ports. After calling this, the element will be
	 * disconnected form the model and should not be used anymore.
	 */
	public void remove() {
		CCSMPre.isNotNull(container, "May not remove lines twice!");

		if (sourcePort != null) {
			sourcePort.removeLine(this);
			sourcePort = null;
		}

		if (destPort != null) {
			destPort.removeLine(this);
			destPort = null;
		}

		container.removeLine(this);
		container = null;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return toLineString();
	}

	/** Returns a string representation of the line. */
	public String toLineString() {
		return sourcePort + " -> " + destPort;
	}

	/** Get line default parameter. */
	@Override
	/* package */String getDefaultParameter(String name) {
		return getModel().getLineDefaultParameter(name);
	}

	/** Get line default parameter names. */
	@Override
	/* package */Set<String> getDefaultParameterNames() {
		return getModel().getLineDefaultParameterNames();
	}

	/**
	 * Returns the layout data for this line. This data is parsed from the model
	 * with each call, so repeated access should be avoided by storing the
	 * result in a local variable.
	 */
	@ADeepCloneTestExclude
	public LineLayoutData obtainLayoutData() {
		return container.getModel().getModelDataHandler()
				.obtainLineLayoutData(this);
	}

	/**
	 * Returns the layout data for the label of this line or null if no label is
	 * available. This data is parsed from the model with each call, so repeated
	 * access should be avoided by storing the result in a local variable.
	 */
	@ADeepCloneTestExclude
	public LabelLayoutData obtainLabelData() {
		return container.getModel().getModelDataHandler()
				.obtainLineLabelData(this);
	}
}