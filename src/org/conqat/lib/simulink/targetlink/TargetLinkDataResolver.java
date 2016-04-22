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
package org.conqat.lib.simulink.targetlink;

import java.io.StringReader;
import java.util.Map;

import org.conqat.lib.commons.visitor.IVisitor;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkObject;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * This visitor identifies Targetlink blocks, parses their data, unfolds it and
 * stores it as normal parameters at the block. The parameter names of nestes
 * Targetlink structs are separated by {@value #PARAMETER_SEPARATOR}.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 50405 $
 * @ConQAT.Rating GREEN Hash: 40372F67628222479F356CC3C4CC252A
 */
public class TargetLinkDataResolver implements
		IVisitor<SimulinkBlock, SimulinkModelBuildingException> {

	/** Separator for Targetlink parameter names. */
	public static final String PARAMETER_SEPARATOR = "/";

	/**
	 * If this is a Targetlink block, parse Targetlink data, resolve the structs
	 * and stores parameters at the block.
	 */
	@Override
	public void visit(SimulinkBlock block)
			throws SimulinkModelBuildingException {
		if (SimulinkUtils.isTargetlinkBlock(block)) {
			unfoldTargetlinkData(block);
		}
	}

	/**
	 * Parse Targetlink data, resolve the structs and store parameters at the
	 * block. Currently this only analyzes Targetlink data stored at parameter
	 * {@link SimulinkConstants#PARAM_TARGETLINK_DATA}.
	 */
	private void unfoldTargetlinkData(SimulinkBlock block)
			throws SimulinkModelBuildingException {
		String data = extractTargetlinkData(block);
		if (data == null) {
			return;
		}

		TargetlinkStruct struct = parseTargetlinkData(block, data);
		Map<String, String> values = struct.getParameters();
		for (String key : values.keySet()) {
			block.setParameter(SimulinkConstants.PARAM_TARGETLINK_DATA + key,
					values.get(key));
		}
	}

	/**
	 * Returns the Targetlink data from a block, which is either stored in an
	 * object or in a specific parameter. Returns null if no data was found.
	 */
	private String extractTargetlinkData(SimulinkBlock block) {

		// unfortunately, the new Targetlink format using objects for storage is
		// not documented. Our best guess is to use a fixed path of object
		// classes.

		SimulinkObject maskObject = SimulinkUtils.findObjectByClass(block,
				SimulinkConstants.PARAM_Simulink_Mask);
		SimulinkObject maskParameterObject = SimulinkUtils.findObjectByClass(
				maskObject, SimulinkConstants.PARAM_Simulink_MaskParameter);
		if (maskParameterObject != null
				&& SimulinkConstants.PARAM_TARGETLINK_DATA
						.equals(maskParameterObject.getName())) {
			return maskParameterObject
					.getParameter(SimulinkConstants.PARAM_Value);
		}

		// Second option is to use the mask value string
		if (SimulinkUtils.isTargetlinkMaskType(block)) {
			String maskValueString = block
					.getParameter(SimulinkConstants.PARAM_MaskValueString);
			if (maskValueString != null) {
				// only include the part before the "|", as the remainder may
				// contain clutter
				return maskValueString.split("[|]", 2)[0];
			}
		}

		return block.getParameter(SimulinkConstants.PARAM_TARGETLINK_DATA);
	}

	/** Parse Targetlink data. */
	private TargetlinkStruct parseTargetlinkData(SimulinkBlock block,
			String data) throws SimulinkModelBuildingException {
		TargetlinkDataScanner scanner = new TargetlinkDataScanner(
				new StringReader(data));
		TargetlinkDataParser parser = new TargetlinkDataParser(scanner);
		try {
			return (TargetlinkStruct) parser.parse().value;
		} catch (Exception ex) {
			// The parser uses unchecked exceptions, so we have to use this
			// general catch clause
			throw new SimulinkModelBuildingException(ex + " in block "
					+ block.getId());
		}

	}
}