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

import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_DstBlock;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_DstPort;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SrcBlock;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SrcPort;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Branch;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Line;

import java.util.regex.Pattern;

import org.conqat.lib.commons.logging.ILogger;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkOutPort;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * This class is responsible for building the lines between Simulink blocks as
 * defined in the MDL file.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51732 $
 * @ConQAT.Rating GREEN Hash: A08A90AC5A446021263EDEDA38D261EE
 */
/* package */class SimulinkLineBuilder {

	/** The logger. */
	private final ILogger logger;

	/**
	 * Whether to preserve unconnected lines. Otherwise unconnected lines are
	 * logged and discarded.
	 */
	private final boolean preserveUnconnectedLines;

	/** Determines whether this is SLX format. */
	private final boolean isSlx;

	/**
	 * Pattern to match SID for SLX format. May be numeric or numeric followed
	 * by '::' followed by yet another numeric.
	 */
	private static final Pattern SID_PATTERN = Pattern
			.compile("([\\d]+)(::[\\d]+)?");

	/** Create line builder. */
	public SimulinkLineBuilder(ModelBuildingParameters modelBuildingParameters,
			boolean isSlx) {
		this.logger = modelBuildingParameters.getLogger();
		this.preserveUnconnectedLines = modelBuildingParameters
				.isPreserveUnconnectedLines();
		this.isSlx = isSlx;
	}

	/**
	 * Build all lines within a Simulink subsystem.
	 * 
	 * @param section
	 *            MDL section that describes the subsystem
	 * @param simulinkBlock
	 *            Simulink block that represents the subsystem
	 * @throws SimulinkModelBuildingException
	 *             if any error occurs.
	 */
	public void buildLines(MDLSection section, SimulinkBlock simulinkBlock)
			throws SimulinkModelBuildingException {
		for (MDLSection line : section.getSubSections(SECTION_Line)) {
			buildLine(line, simulinkBlock);
		}
	}

	/**
	 * Build a single line.
	 * 
	 * @param lineSection
	 *            section that describes the line.
	 * @param simulinkBlock
	 *            Simulink block that represents the subsystem
	 * @throws SimulinkModelBuildingException
	 *             if any error occurs.
	 */
	private void buildLine(MDLSection lineSection, SimulinkBlock simulinkBlock)
			throws SimulinkModelBuildingException {

		SimulinkOutPort sourcePort = determineSrcPort(lineSection,
				simulinkBlock);
		// missing source port has been logged in
		// determineSrcPort()/getSubBlockByParameter()
		if (sourcePort == null && !preserveUnconnectedLines) {
			return;
		}

		// If this line has no branches, create line and exit
		if (!lineSection.hasSubSections(SECTION_Branch)) {
			SimulinkInPort dstPort = determineDstPort(lineSection,
					simulinkBlock);
			// missing dst port has been logged in
			// determineDstPort()/getSubBlockByParameter()
			if (dstPort == null && !preserveUnconnectedLines) {
				return;
			}
			// constructor creates and connects line
			SimulinkLine line = new SimulinkLine(sourcePort, dstPort,
					simulinkBlock);
			addLineParameters(line, lineSection);
			return;
		}

		completeLine(lineSection, sourcePort, simulinkBlock);
	}

	/**
	 * Completes line building for a known source port from
	 * {@link #buildLine(MDLSection, SimulinkBlock)}.
	 */
	private void completeLine(MDLSection lineSection,
			SimulinkOutPort sourcePort, SimulinkBlock simulinkBlock)
			throws SimulinkModelBuildingException {
		// Flag to check if we found at least one destination for this line
		boolean destFound = false;

		// Only leaf branches contain destinations
		for (MDLSection branchSection : lineSection
				.getLeafSections(SECTION_Branch)) {
			if (preserveUnconnectedLines
					|| branchSection.hasParameter(PARAM_DstBlock)) {
				SimulinkInPort destPort = determineDstPort(branchSection,
						simulinkBlock);
				// missing dest port is logged before
				if (destPort == null && !preserveUnconnectedLines) {
					continue;
				}
				// constructor creates and connects line
				SimulinkLine line = new SimulinkLine(sourcePort, destPort,
						simulinkBlock);
				while (branchSection != lineSection) {
					addLineParameters(line, branchSection);
					branchSection = branchSection.getParentSection();
				}
				addLineParameters(line, lineSection);
				destFound = true;
			} else {
				logger.info(branchSection
						+ " is a leave branch without destination block. "
						+ "Ignoring branch.");
			}
		}

		if (!destFound && !preserveUnconnectedLines) {
			logger.info(lineSection + " in block " + simulinkBlock
					+ " has no destination." + " Ignoring line.");
		}
	}

	/**
	 * Adds all parameters from the given branch section to a line object.
	 * Additionally, this adjusts the labels parameter, by shifting the segment
	 * index if more points are prepended.
	 */
	private void addLineParameters(SimulinkLine line, MDLSection branchSection) {
		SimulinkModelBuilder.addParameters(line, branchSection);

		// if we do not have labels or the labels value was just added, nothing
		// has to be done
		if (!line.getDeclaredParameterNames().contains(
				SimulinkConstants.PARAM_Labels)
				|| branchSection.getParameterNames().contains(
						SimulinkConstants.PARAM_Labels)) {
			return;
		}

		String value = line.getParameter(SimulinkConstants.PARAM_Labels);

		// we have to adjust the first offset, which is the segment index
		int[] valueArray = SimulinkUtils.getIntParameterArray(value);
		String prependedPoints = branchSection
				.getParameter(SimulinkConstants.PARAM_Points);
		if (prependedPoints != null) {
			int pointCount = (StringUtils.countCharacter(prependedPoints, ',') + 1) / 2;
			valueArray[0] += pointCount;
		}
		value = "[" + valueArray[0] + "," + valueArray[1] + "]";
		line.setParameter(SimulinkConstants.PARAM_Labels, value);
	}

	/**
	 * Determine source port of a line.
	 * 
	 * @param section
	 *            that describes the source part of a line.
	 * @param simulinkBlock
	 *            the block this destination belongs to
	 * @return the source port or <code>null</code> if the section does not
	 *         specify a source port. This is logged as info unless
	 *         {@link #preserveUnconnectedLines} is true.
	 */
	private SimulinkOutPort determineSrcPort(MDLSection section,
			SimulinkBlock simulinkBlock) throws SimulinkModelBuildingException {
		SimulinkBlock sourceBlock = getSubBlockByParameter(section,
				simulinkBlock, PARAM_SrcBlock);
		if (sourceBlock == null) {
			return null;
		}

		String sourcePortIndex = section.getParameter(PARAM_SrcPort);
		SimulinkOutPort sourcePort = sourceBlock.getOutPort(sourcePortIndex);
		if (sourcePort == null) {
			throw new SimulinkModelBuildingException(section
					+ " refers to unknown source port " + sourcePortIndex);
		}

		return sourcePort;
	}

	/**
	 * Determine destination port of a line.
	 * 
	 * @param section
	 *            that describes the destination part of a line. This may be a
	 *            line or a branch section.
	 * @param simulinkBlock
	 *            the block this destination belongs to
	 * @return the destination port or <code>null</code> if the section does not
	 *         specify a destination port. This is logged as info unless
	 *         {@link #preserveUnconnectedLines} is true.
	 */
	private SimulinkInPort determineDstPort(MDLSection section,
			SimulinkBlock simulinkBlock) throws SimulinkModelBuildingException {
		SimulinkBlock destBlock = getSubBlockByParameter(section,
				simulinkBlock, PARAM_DstBlock);
		if (destBlock == null) {
			return null;
		}

		String destPortIndex = section.getParameter(PARAM_DstPort);
		SimulinkInPort destPort = destBlock.getInPort(destPortIndex);
		if (destPort == null) {
			throw new SimulinkModelBuildingException(section
					+ " refers to unknown destination port " + destPortIndex
					+ ".");
		}
		return destPort;
	}

	/**
	 * Returns the sub block of the given block whose name is given by the
	 * parameter of the section. If the parameter is not found in the section,
	 * null is returned. This condition is logged, depending on
	 * {@link #preserveUnconnectedLines}. If the sub block is not found, an
	 * exception is thrown.
	 * 
	 * @param section
	 *            the section to read the block name from.
	 * @param parentBlock
	 *            the block in which to look up the sub block.
	 * @param parameterName
	 *            the name of the parameter that holds the block's name.
	 */
	private SimulinkBlock getSubBlockByParameter(MDLSection section,
			SimulinkBlock parentBlock, String parameterName)
			throws SimulinkModelBuildingException {
		String subBlockName = section.getParameter(parameterName);
		if (subBlockName == null) {
			if (!preserveUnconnectedLines) {
				logger.info(section + " in block " + parentBlock + " has no "
						+ parameterName + ". Ignoring line.");
			}
			return null;
		}

		/*
		 * Slx format uses the SID instead of the Name parameter for block
		 * matching.
		 */
		boolean isSID = isSlx && SID_PATTERN.matcher(subBlockName).matches();
		SimulinkBlock resultBlock = null;
		if (isSID) {
			resultBlock = parentBlock.getSubBlockBySID(subBlockName);
		} else {
			resultBlock = parentBlock.getSubBlock(subBlockName);
		}

		if (resultBlock == null) {
			throw new SimulinkModelBuildingException(section
					+ " refers to unknown " + parameterName + " block "
					+ subBlockName + ".");
		}
		return resultBlock;
	}
}