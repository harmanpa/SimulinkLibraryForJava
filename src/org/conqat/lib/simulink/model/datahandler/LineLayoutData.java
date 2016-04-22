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
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates all information required for layouting a line (relative to its
 * parent's canvas).
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51741 $
 * @ConQAT.Rating GREEN Hash: 7CE49091573ECF3B44C9E80D72E458E4
 */
public class LineLayoutData {

	/**
	 * The points that define the segments of the line. This includes both the
	 * start and end point. All positions are global and absolute. The order of
	 * the points is from start to end.
	 */
	private final List<Point> points;

	/** The color of the line. */
	private final Color color;

	/** Constructor. */
	/* package */LineLayoutData(List<Point> points, Color color) {
		this.points = new ArrayList<>(points);
		this.color = color;
	}

	/**
	 * Returns the points that define the segments of the line. This includes
	 * both the start and end point. All positions are global and absolute. The
	 * order of the points is from start to end.
	 */
	public List<Point> getPoints() {
		return points;
	}

	/** Returns {@link #color}. */
	public Color getColor() {
		return color;
	}
}
