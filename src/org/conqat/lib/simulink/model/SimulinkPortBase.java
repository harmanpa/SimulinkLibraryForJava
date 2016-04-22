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

import org.conqat.lib.commons.test.ADeepCloneTestExclude;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.model.datahandler.PortLayoutData;

/**
 * Base class for Simulink ports.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51474 $
 * @ConQAT.Rating GREEN Hash: 5B66FEF02A67A5063E20D4DA99AD10DC
 */
public abstract class SimulinkPortBase {

	/** The block this port belongs to. */
	private SimulinkBlock block;

	/**
	 * The port index. This may be a number or a string like 'enable' indicating
	 * a special port.
	 */
	private final String index;

	/**
	 * Create Simulink port.
	 * 
	 * @param block
	 *            The block this port belongs to.
	 * @param index
	 *            The port index. This may be a number or a string like 'enable'
	 *            indicating a special port.
	 */
	protected SimulinkPortBase(SimulinkBlock block, String index) {
		this.block = block;
		this.index = index;
	}

	/**
	 * Get the port index. This may be a number or a string like 'enable'
	 * indicating a special port.
	 */
	public String getIndex() {
		return index;
	}

	/** Get the block this port belongs to. */
	public SimulinkBlock getBlock() {
		return block;
	}

	/** Get string representation of this block: &lt;index&gt;@&lt;block_id&gt;. */
	@Override
	public String toString() {
		return index + "@" + block.getId();
	}

	/**
	 * This only sets the block to <code>null</code>. Actual remove
	 * implementation is done in the sub classes.
	 */
	public void remove() {
		block = null;
	}

	/** Returns whether the port is connected to at least one line. */
	public abstract boolean isConnected();

	/**
	 * Returns the layout data for this port. This data is parsed from the model
	 * with each call, so repeated access should be avoided by storing the
	 * result in a local variable.
	 */
	@ADeepCloneTestExclude
	public PortLayoutData obtainLayoutData() {
		return getBlock().getModel().getModelDataHandler()
				.obtainPortLayoutData(this);
	}

	/**
	 * Returns the layout data for the label of this port or null if no label is
	 * available. This data is parsed from the model with each call, so repeated
	 * access should be avoided by storing the result in a local variable.
	 */
	@ADeepCloneTestExclude
	public LabelLayoutData obtainLabelData() {
		return getBlock().getModel().getModelDataHandler()
				.obtainPortLabelData(this);
	}

	/** Returns whether this is a special port (trigger, enable, etc.). */
	public boolean isSpecialPort() {
		return false;
	}
}