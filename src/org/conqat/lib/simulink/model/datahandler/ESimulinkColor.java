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

import org.conqat.lib.commons.color.ColorUtils;

/**
 * Enumeration of predefined colors in Simulink. The names of the constants
 * follow the names used in the model files (only casing is changed). Hence we
 * use no underscores. The colors use the color codes from the version 2013.2 of
 * Matlab.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 50854 $
 * @ConQAT.Rating GREEN Hash: 6DEBFF82F322358001FCE979ADAEDBEF
 */
public enum ESimulinkColor {

	/** The color. */
	BLACK(Color.BLACK),

	/** The color. */
	WHITE(Color.WHITE),

	/** The color. */
	RED("#e60000"),

	/** The color. */
	GREEN("#00d300"),

	/** The color. */
	BLUE("#0002cc"),

	/** The color. */
	MAGENTA("#fd43d4"),

	/** The color. */
	CYAN("#00d2d2"),

	/** The color. */
	YELLOW("#e9d055"),

	/** The color. */
	ORANGE("#ff7f00"),

	/** The color. */
	GRAY("#808080"),

	/** The color. */
	LIGHTBLUE("#63b9ff"),

	/** The color. */
	DARKGREEN("#699640");

	/** The actual color value. */
	private final Color color;

	/** Constructor. */
	private ESimulinkColor(Color color) {
		this.color = color;
	}

	/** Constructor. */
	private ESimulinkColor(String htmlColor) {
		this(ColorUtils.fromString(htmlColor));
	}

	/** Returns {@link #color}. */
	public Color getColor() {
		return color;
	}

}
