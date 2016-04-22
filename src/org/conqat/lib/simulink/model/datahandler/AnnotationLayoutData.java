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
 * Encapsulates all information required for layouting an annotation (relative
 * to its parent's canvas).
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51475 $
 * @ConQAT.Rating GREEN Hash: AE70239A3F6E6F18004A80568ACB7A2A
 */
public class AnnotationLayoutData extends RectangleLayoutDataBase {

	/** Whether the border should be visible. */
	private final boolean borderVisible;

	/** Constructor. */
	/* package */AnnotationLayoutData(Rectangle position,
			Color foregroundColor, Color backgroundColor, boolean borderVisible) {
		super(position, foregroundColor, backgroundColor);
		this.borderVisible = borderVisible;
	}

	/** Returns whether the border should be visible. */
	public boolean isBorderVisible() {
		return borderVisible;
	}
}
