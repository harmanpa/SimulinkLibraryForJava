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
import java.awt.Rectangle;

/**
 * Encapsulates all information required for layouting a rectangular element.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51762 $
 * @ConQAT.Rating GREEN Hash: F029A3D2384B9CC43D935F11D6982B00
 */
public class RectangleLayoutDataBase {

	/** Foreground color of the block. */
	private final Color foregroundColor;

	/** Background color of the block. */
	private final Color backgroundColor;

	/**
	 * The position of the block (which in Simulink terms also includes size
	 * information).
	 */
	private final Rectangle position;

	/** Constructor. */
	protected RectangleLayoutDataBase(Rectangle position,
			Color foregroundColor, Color backgroundColor) {
		this.position = position;
		this.foregroundColor = foregroundColor;
		this.backgroundColor = backgroundColor;
	}

	/** Returns {@link #foregroundColor}. */
	public Color getForegroundColor() {
		return foregroundColor;
	}

	/** Returns {@link #backgroundColor}. */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/** Returns {@link #position}. */
	public Rectangle getPosition() {
		return position;
	}

}
