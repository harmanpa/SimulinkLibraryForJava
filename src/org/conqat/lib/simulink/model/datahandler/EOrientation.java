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

import org.conqat.lib.commons.logging.ILogger;

/**
 * Orientation used for simulink blocks.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51740 $
 * @ConQAT.Rating GREEN Hash: E5ADAFAD7E784C7E15F32A1F447EFC65
 */
public enum EOrientation {

	/** Right (normal) orientation. */
	RIGHT(0),

	/** Down orientation. */
	DOWN(270),

	/** Left orientation. */
	LEFT(180),

	/** Up orientation. */
	UP(90);

	/** The direction (see {@link #getDirection()}). */
	private final double direction;

	/** Constructor. */
	private EOrientation(double direction) {
		this.direction = direction;
	}

	/** Returns whether this is a rotation (LEFT is just a flip). */
	public boolean isRotated() {
		return this == DOWN || this == UP;
	}

	/** Returns whether this is LEFT or UP. */
	public boolean isLeftOrUp() {
		return this == LEFT || this == UP;
	}

	/** Returns whether this is LEFT or DOWN. */
	public boolean isLeftOrDown() {
		return this == LEFT || this == DOWN;
	}

	/**
	 * Returns the direction in degree (0 to 360). A value of 0 indicates the
	 * normal position for a non-rotated block. Other values are
	 * counter-clockwise rotations.
	 */
	public double getDirection() {
		return direction;
	}

	/** Returns the opposite orientation. */
	public EOrientation getOpposite() {
		switch (this) {
		case DOWN:
			return UP;
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		case UP:
			return DOWN;
		default:
			throw new AssertionError("Unknown orientation: " + this);
		}
	}

	/**
	 * Returns the orientation based on a rotation value encoded as degree
	 * string (i.e. "0", "90", etc.). In case of an invalid rotation value, an
	 * error is logged using the provided logger and {@link #RIGHT} is returned.
	 */
	public static EOrientation fromRotationValue(String rotationValue,
			ILogger logger) {
		switch (rotationValue) {
		case "0":
			return EOrientation.RIGHT;
		case "90":
			return EOrientation.DOWN;
		case "180":
			return EOrientation.LEFT;
		case "270":
			return EOrientation.UP;
		default:
			logger.error("Unknown rotation value: " + rotationValue
					+ ". Using default.");
			return EOrientation.RIGHT;
		}
	}
}