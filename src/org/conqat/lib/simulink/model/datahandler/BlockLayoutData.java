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
 * Encapsulates all information required for layouting a block (relative to its
 * parent's canvas).
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51476 $
 * @ConQAT.Rating GREEN Hash: 43F4C629860D08390AFFDDC229CAE3DD
 */
public class BlockLayoutData extends RectangleLayoutDataBase {

	/** The orientation of the block. */
	private final EOrientation orientation;

	/** Constructor. */
	/* package */BlockLayoutData(Rectangle position, EOrientation orientation,
			Color foregroundColor, Color backgroundColor) {
		super(position, foregroundColor, backgroundColor);
		this.orientation = orientation;
	}

	/** Returns {@link #orientation}. */
	public EOrientation getOrientation() {
		return orientation;
	}
}
