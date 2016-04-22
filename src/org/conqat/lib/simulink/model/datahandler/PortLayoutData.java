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

import java.awt.Color;
import java.awt.Point;

/**
 * Encapsulates all information required for layouting a port (relative to its
 * parent's canvas).
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51483 $
 * @ConQAT.Rating GREEN Hash: 71C3939267CA0EA7CA70005F10FCAF11
 */
public class PortLayoutData {

	/** The position of the port. */
	private final Point position;

	/**
	 * The direction in degree (0 to 360). A value of 0 indicates the normal
	 * position for a non-rotated block, i.e. an input port that comes from the
	 * left resp. an output port that points to the right. Other values are
	 * counter-clockwise rotations.
	 */
	private final double direction;

	/** The color used for the port. */
	private final Color color;

	/** Constructor. */
	/* package */PortLayoutData(Point position, double direction, Color color) {
		this.position = position;
		this.direction = direction;
		this.color = color;
	}

	/** Returns {@link #position}. */
	public Point getPosition() {
		return position;
	}

	/**
	 * Returns the direction in degree (0 to 360). A value of 0 indicates the
	 * normal position for a non-rotated block, i.e. an input port that comes
	 * from the left resp. an output port that points to the right. Other values
	 * are counter-clockwise rotations.
	 */
	public double getDirection() {
		return direction;
	}

	/** Returns {@link #color}. */
	public Color getColor() {
		return color;
	}
}
