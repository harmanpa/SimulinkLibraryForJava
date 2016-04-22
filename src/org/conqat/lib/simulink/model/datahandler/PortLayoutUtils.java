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
package org.conqat.lib.simulink.model.datahandler;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkPortBase;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Utility code for finding the position of ports.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51951 $
 * @ConQAT.Rating GREEN Hash: D30F3B3489BC0BDF739E824D8CABAAFA
 */
public class PortLayoutUtils {

	/**
	 * The number of pixels to add to the size of a round sum block when
	 * calculating the pre-port bend point for a line.
	 */
	private static final int ROUND_SUM_PRE_PORT_INSET = 13;

	/**
	 * The implicit grid size used in Simulink for placing ports, i.e. the
	 * distance between ports is a multiple of this.
	 */
	private static final int PORT_SPACING_GRID = 5;

	/**
	 * The number of pixels to add to the size of a "normal" block when
	 * calculating the pre-port bend point for a line connected to an outport.
	 */
	private static final int OUTPORT_INSET = PORT_SPACING_GRID;

	/**
	 * The number of pixels to add to the size of a "normal" block when
	 * calculating the pre-port bend point for a line connected to an inport.
	 */
	private static final int INPORT_INSET = 3 * PORT_SPACING_GRID;

	/** Returns the position of a port for the given block position. */
	public static Point getPortLocation(SimulinkPortBase port,
			BlockLayoutData blockLayout) {
		return getPortLocation(port, blockLayout, blockLayout.getPosition());
	}

	/** Returns the position of a port for the given block position. */
	private static Point getPortLocation(SimulinkPortBase port,
			BlockLayoutData blockLayout, Rectangle position) {
		EOrientation orientation = blockLayout.getOrientation();

		int x = position.x;
		int y = position.y;
		int width = position.width;
		int height = position.height;

		// swap for up/down
		if (orientation.isRotated()) {
			int tmp = width;
			width = height;
			height = tmp;
		}

		Point offset = determinePortOffset(port, width, height, orientation);

		// Flip for left/up, but not for the special ones
		if (orientation.isLeftOrUp() && !port.isSpecialPort()) {
			offset.x = width - offset.x;
		}

		// swap for up/down
		if (orientation.isRotated()) {
			return new Point(x + offset.y, y + offset.x);
		}
		return new Point(x + offset.x, y + offset.y);
	}

	/**
	 * Calculates and returns the x/y offset of the given port relative to a
	 * block with given width/height.
	 */
	private static Point determinePortOffset(SimulinkPortBase port, int width,
			int height, EOrientation orientation) {
		boolean isInput = port instanceof SimulinkInPort;
		SimulinkBlock block = port.getBlock();

		int xOffset = 0;
		if (!isInput) {
			xOffset = width;
		}

		String indexParam = port.getIndex();
		if (indexParam != null && indexParam.matches("\\d+")) {
			int index = Integer.parseInt(indexParam);

			if (isInput && SimulinkUtils.isRoundSum(block)) {
				return roundSumGetNthPortPos(block, index, width, height);
			}

			if (isInput && SimulinkConstants.TYPE_Sum.equals(block.getType())) {
				// for non-round sum blocks, we also have to adjust positions
				String portsDescription = block
						.getParameter(SimulinkConstants.PARAM_Inputs);
				return new Point(xOffset, getPortYOffset(
						portsDescription.length(),
						getLogicalIndexForPort(portsDescription, index) + 1,
						height));
			}

			int numPorts = 1;
			if (isInput) {
				numPorts = countNormalPorts(block.getInPorts());
			} else {
				numPorts = countNormalPorts(block.getOutPorts());
			}

			return new Point(xOffset, getPortYOffset(numPorts, index, height));
		}

		return determineSpecialPortOffset(port, width, height, orientation);
	}

	/** Returns the offset to use for special ports (trigger, enable). */
	private static Point determineSpecialPortOffset(SimulinkPortBase port,
			int width, int height, EOrientation orientation) {
		int specialPortCount = 0;
		for (SimulinkInPort inPort : port.getBlock().getInPorts()) {
			if (inPort.isSpecialPort()) {
				specialPortCount += 1;
			}
		}

		int y = 0;
		if (isSpecialPortOnBottom(port)) {
			y = height;
		}

		if (specialPortCount <= 1) {
			return new Point((width + 1) / 2, y);
		}

		// enable port is first unless down
		boolean first = port instanceof SimulinkInPort
				&& ((SimulinkInPort) port).isEnablePort();
		if (orientation == EOrientation.DOWN) {
			first = !first;
		}

		int distance = (int) (Math.round((width - 1) / 2. / PORT_SPACING_GRID) * PORT_SPACING_GRID);
		int offset = (width - distance + 1) / 2;
		if (first) {
			return new Point(offset, y);
		}
		return new Point(offset + distance, y);
	}

	/**
	 * Returns whether special ports should be drawn on the bottom side (instead
	 * of top).
	 */
	public static boolean isSpecialPortOnBottom(SimulinkPortBase port) {
		return SimulinkConstants.VALUE_alternate.equals(port.getBlock()
				.getParameter(SimulinkConstants.PARAM_NamePlacement));
	}

	/** Returns the y offset (from the top) for the n-th port (1 indexed). */
	private static int getPortYOffset(int numPorts, int portIndex, int height) {
		CCSMAssert.isTrue(portIndex >= 1 && portIndex <= numPorts,
				"Port index out of range: " + portIndex);

		// spacing between ports is rounded to a multitude of 5
		int portSpacing = (int) (Math
				.round((double) (height - numPorts / 2 - 1) / PORT_SPACING_GRID
						/ numPorts) * PORT_SPACING_GRID);
		portSpacing = Math.max(PORT_SPACING_GRID, portSpacing);

		int offset = (height - (numPorts - 1) * portSpacing) / 2;
		if (portSpacing > PORT_SPACING_GRID && offset < 0) {
			portSpacing -= PORT_SPACING_GRID;
			offset = (height - (numPorts - 1) * portSpacing) / 2;
		}

		return offset + (portIndex - 1) * portSpacing;
	}

	/**
	 * Returns the position of the n-th input port for a rounded sum port. This
	 * deals only with the non-rotated case.
	 */
	private static Point roundSumGetNthPortPos(SimulinkBlock block, int n,
			int width, int height) {
		double angle = determineRoundSumAngle(block, n);
		return new Point((int) Math.round((1. - Math.sin(angle)) * width / 2),
				(int) Math.round((1 - Math.cos(angle)) * height / 2));
	}

	/**
	 * Returns the angle to be used for the n-th input port of a round sum port.
	 * The angle is 0 for up and a half rotation (counter clock-wise)
	 * corresponds to PI.
	 */
	public static double determineRoundSumAngle(SimulinkBlock block, int n) {
		String ports = block.getParameter(SimulinkConstants.PARAM_Inputs);
		double angle = Math.PI * getLogicalIndexForPort(ports, n)
				/ (ports.length() - 1);
		if (ports.length() <= 1) {
			angle = Math.PI / 2;
		}
		return angle;
	}

	/**
	 * Returns the position of a point just before the actual port. Simulink
	 * seems to insert an addition bend point some pixel before.
	 */
	public static Point getPrePortPoint(SimulinkPortBase port,
			BlockLayoutData blockLayout) {
		int inset = OUTPORT_INSET;
		if (port instanceof SimulinkInPort) {
			if (SimulinkUtils.isRoundSum(port.getBlock())) {
				inset = ROUND_SUM_PRE_PORT_INSET;
			} else {
				inset = INPORT_INSET;
			}
		}
		return getInsetPortPoint(port, blockLayout, inset);
	}

	/**
	 * Returns the position of a point with a relative inset to the actual port.
	 * Simulink seems to insert an additional bend point some pixel before.
	 */
	public static Point getInsetPortPoint(SimulinkPortBase port,
			BlockLayoutData blockLayoutData, int inset) {
		int insetX = inset;
		int insetY = inset;
		if (SimulinkUtils.isRoundSum(port.getBlock())) {
			// nothing to do, adjust both directions
		} else if (blockLayoutData.getOrientation().isRotated()
				^ port.isSpecialPort()) {
			// only adjust y direction
			insetX = 0;
		} else {
			// only adjust x direction
			insetY = 0;
		}

		Rectangle position = new Rectangle(blockLayoutData.getPosition());
		position.grow(insetX, insetY);
		return getPortLocation(port, blockLayoutData, position);
	}

	/** Calculates the logical index of the port of given index of a sum block. */
	public static int getLogicalIndexForPort(String ports, int index) {
		for (int i = 0; i < ports.length(); ++i) {
			if (ports.charAt(i) != '|') {
				index--;
				if (index == 0) {
					return i;
				}
			}
		}
		return ports.length() - 1;
	}

	/**
	 * Counts the number of "normal" numeric ports. This is simply the maximal
	 * number of a port.
	 */
	private static int countNormalPorts(
			Collection<? extends SimulinkPortBase> ports) {
		int result = 0;
		for (SimulinkPortBase port : ports) {
			try {
				result = Math.max(result, Integer.parseInt(port.getIndex()));
			} catch (NumberFormatException e) {
				// ignore; result not increased
			}
		}
		return result;
	}

}