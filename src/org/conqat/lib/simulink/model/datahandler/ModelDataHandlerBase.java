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
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.UnmodifiableList;
import org.conqat.lib.commons.enums.EnumUtils;
import org.conqat.lib.commons.logging.ILogger;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.simulink.builder.ModelBuildingParameters;
import org.conqat.lib.simulink.model.ParameterizedElement;
import org.conqat.lib.simulink.model.SimulinkAnnotation;
import org.conqat.lib.simulink.model.SimulinkBlock;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkElementBase;
import org.conqat.lib.simulink.model.SimulinkInPort;
import org.conqat.lib.simulink.model.SimulinkLine;
import org.conqat.lib.simulink.model.SimulinkPortBase;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * Common base class for all data handlers for the Simulink model. The purpose
 * of the data handler is to interpret the data stored in Simulink's key/value
 * properties and provide a meaningful interpretation. Most of the
 * interpretation is layout oriented.
 * 
 * This class defines the core interface for the data handler and common
 * abstraction code. Subclasses may override some of the behavior as the
 * interpretation and especially the keys used changed between Simulink
 * versions.
 * 
 * @author $Author: hummelb $
 * @version $Rev: 52215 $
 * @ConQAT.Rating GREEN Hash: 6C62B6462F112350F02592377A61D32B
 */
public abstract class ModelDataHandlerBase {

	/**
	 * Default position (which in Simulink terms also contains dimensions) used
	 * for blocks in case of errors while parsing block position.
	 */
	private static final Rectangle BLOCK_DEFAULT_POSITION = new Rectangle(10,
			10, 30, 30);

	/** The unicode "en dash". */
	private static final String EN_DASH = "\u2013";

	/** Default font size in the Simulink IDE. */
	private static final int DEFAULT_FONT_SIZE = 10;

	/** Distance of a label from the block or line. */
	private static final int LABEL_DISTANCE = 4;

	/** Offset difference to be automatically corrected. */
	private static final int CORRECTION_OFFSET = 3;

	/** The font render context used for determining text width. */
	public static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(
			null, false, false);

	/** Font data used for port labels. */
	private static final FontData PORT_LABEL_FONT_DATA = new FontData(
			Font.SANS_SERIF, 9, false, false);

	/** Logger used for reporting any problems during model data extraction. */
	protected final ILogger logger;

	/** The directories to search for referenced models (MDL/SLX) in. */
	private final List<File> referenceDirectories;

	/** Constructor. */
	protected ModelDataHandlerBase(ModelBuildingParameters parameters) {
		logger = parameters.getLogger();
		this.referenceDirectories = new ArrayList<>(
				parameters.getReferencePaths());
	}

	/** Returns the layout data used for rendering a block. */
	public BlockLayoutData obtainBlockLayoutData(SimulinkBlock block) {

		return new BlockLayoutData(extractPosition(block),
				extractOrientation(block), extractColor(block,
						SimulinkConstants.PARAM_ForegroundColor, Color.BLACK),
				extractColor(block, SimulinkConstants.PARAM_BackgroundColor,
						Color.WHITE));
	}

	/** Returns the layout data used for rendering an annotation. */
	public AnnotationLayoutData obtainAnnotationLayoutData(
			SimulinkAnnotation annotation) {
		boolean borderVisible = SimulinkConstants.VALUE_on.equals(annotation
				.getParameter(SimulinkConstants.PARAM_DropShadow));

		return new AnnotationLayoutData(extractPosition(annotation),
				extractColor(annotation,
						SimulinkConstants.PARAM_ForegroundColor, Color.BLACK),
				extractColor(annotation,
						SimulinkConstants.PARAM_BackgroundColor, Color.WHITE),
				borderVisible);
	}

	/**
	 * Extracts an elements's position (which in Simulink terms also contains
	 * the size.
	 */
	private Rectangle extractPosition(SimulinkElementBase element) {
		String positionString = element
				.getParameter(SimulinkConstants.PARAM_Position);
		if (positionString == null) {
			logger.error("No position contained in model for element "
					+ element.getId() + ". Using default position.");
			return BLOCK_DEFAULT_POSITION;
		}
		int[] positionArray = SimulinkUtils
				.getIntParameterArray(positionString);
		if (positionArray.length == 2) {
			// can happen for annotations; determine size based on text
			positionArray = Arrays.copyOf(positionArray, 4);

			String text = SimulinkUtils.replaceSimulinkLineBreaks(element
					.getName());
			FontData font = extractFontData(element);
			Rectangle textBounds = determineTextBounds(text, font.getAwtFont());
			positionArray[0] -= (textBounds.width + LABEL_DISTANCE) / 2;
			positionArray[2] = positionArray[0] + textBounds.width
					+ LABEL_DISTANCE;
			positionArray[3] = positionArray[1] + textBounds.height
					+ LABEL_DISTANCE;
		}
		if (positionArray.length != 4) {
			logger.error("Unsupported position array found in model (length = "
					+ positionArray.length + " instead of 4) for element "
					+ element.getId() + ". Using default position.");
			return BLOCK_DEFAULT_POSITION;
		}
		return new Rectangle(positionArray[0], positionArray[1],
				positionArray[2] - positionArray[0], positionArray[3]
						- positionArray[1]);
	}

	/** Calculates the orientation. */
	protected abstract EOrientation extractOrientation(
			SimulinkElementBase element);

	/**
	 * Extracts a color from an element's parameters.
	 * 
	 * @param parameter
	 *            the name of the parameter.
	 * @param defaultColor
	 *            the color to use if no color information was found.
	 */
	private Color extractColor(SimulinkElementBase element, String parameter,
			Color defaultColor) {
		String colorString = element.getParameter(parameter);
		if (colorString == null) {
			return defaultColor;
		}

		if (colorString.startsWith("[")) {
			return parseArrayColor(element, colorString, defaultColor);
		}

		return parsePredefinedColor(element, colorString, defaultColor);
	}

	/**
	 * Parses and returns a color from a (RGB) color array.
	 * 
	 * @param element
	 *            the element (used for error reporting).
	 * @param colorString
	 *            the color string to parse (Matlab array representation).
	 * @param defaultColor
	 *            the default color to return in case of errors.
	 */
	private Color parseArrayColor(SimulinkElementBase element,
			String colorString, Color defaultColor) {
		try {
			double[] colorArray = SimulinkUtils
					.getDoubleParameterArray(colorString);
			if (colorArray.length != 3) {
				logger.error("Unsupported color array found in element "
						+ element.getId() + " (length = " + colorArray.length
						+ " instead of 3). Using default color.");
				return defaultColor;
			}
			return new Color((float) colorArray[0], (float) colorArray[1],
					(float) colorArray[2]);
		} catch (NumberFormatException e) {
			logger.error("Color array in element " + element.getId()
					+ " contained invalid number: " + colorString
					+ ". Using default color.");
			return defaultColor;
		}
	}

	/**
	 * Parses and returns a predefined color (i.e. explicit color string).
	 * 
	 * @param element
	 *            the element (used for error reporting).
	 * @param colorString
	 *            the color string to parse.
	 * @param defaultColor
	 *            the default color to return in case of errors.
	 */
	private Color parsePredefinedColor(SimulinkElementBase element,
			String colorString, Color defaultColor) {
		ESimulinkColor simulinkColor = EnumUtils.valueOfIgnoreCase(
				ESimulinkColor.class, colorString);
		if (simulinkColor == null) {
			logger.error("Unsupported color string found in element "
					+ element.getId() + " (" + colorString
					+ "). Using default color.");
			return defaultColor;
		}
		return simulinkColor.getColor();
	}

	/** Returns the layout data used for rendering a port. */
	public PortLayoutData obtainPortLayoutData(SimulinkPortBase port) {
		BlockLayoutData blockLayoutData = obtainBlockLayoutData(port.getBlock());
		return new PortLayoutData(PortLayoutUtils.getPortLocation(port,
				blockLayoutData), determineDirection(port, blockLayoutData),
				blockLayoutData.getForegroundColor());
	}

	/** Returns the direction the given port is oriented to in degree. */
	private double determineDirection(SimulinkPortBase port,
			BlockLayoutData blockLayoutData) {
		double direction = blockLayoutData.getOrientation().getDirection();
		if (SimulinkUtils.isRoundSum(port.getBlock())
				&& port instanceof SimulinkInPort) {
			double directionOffset = PortLayoutUtils.determineRoundSumAngle(
					port.getBlock(), Integer.parseInt(port.getIndex()))
					- Math.PI / 2;
			direction += directionOffset * 180. / Math.PI;
		} else if (port.isSpecialPort()) {
			direction += 270;
			if (PortLayoutUtils.isSpecialPortOnBottom(port)
					&& !blockLayoutData.getOrientation().isLeftOrDown()) {
				direction += 180;
			}
			direction %= 360;
		}
		return direction;
	}

	/** Returns the layout data used for rendering a line. */
	public LineLayoutData obtainLineLayoutData(SimulinkLine line) {
		List<Point> points = new ArrayList<>();

		if (line.getSrcPort() != null) {
			points.add(line.getSrcPort().obtainLayoutData().getPosition());

			// Simulink has an implicit bend point directly after the source
			// port
			Point sourceBend = PortLayoutUtils.getPrePortPoint(
					line.getSrcPort(), line.getSrcPort().getBlock()
							.obtainLayoutData());
			points.add(sourceBend);
		}

		extractPoints(line, points);

		if (line.getDstPort() != null) {
			SimulinkBlock destinationBlock = line.getDstPort().getBlock();
			points.add(PortLayoutUtils.getPrePortPoint(line.getDstPort(),
					destinationBlock.obtainLayoutData()));
			alignPoints(points);
			points.add(line.getDstPort().obtainLayoutData().getPosition());
		}

		Color color;
		if (line.getSrcPort() == null || line.getDstPort() == null) {
			color = ESimulinkColor.RED.getColor();
		} else {
			color = extractColor(line.getSrcPort().getBlock(),
					SimulinkConstants.PARAM_ForegroundColor, Color.BLACK);
		}

		return new LineLayoutData(points, color);
	}

	/**
	 * Aligns the points in the list for minor differences. This is performed
	 * backwards, i.e. starting from the last point. The first two points are
	 * not adjusted, as they rely on the output port.
	 */
	private void alignPoints(List<Point> points) {
		for (int i = points.size() - 2; i >= 2; --i) {
			Point correctedPoint = points.get(i);
			Point referencePoint = points.get(i + 1);

			if (Math.abs(correctedPoint.x - referencePoint.x) <= CORRECTION_OFFSET) {
				correctedPoint.x = referencePoint.x;
			}
			if (Math.abs(correctedPoint.y - referencePoint.y) <= CORRECTION_OFFSET) {
				correctedPoint.y = referencePoint.y;
			}
		}
	}

	/**
	 * Extracts the (inner) points for a line and adds them to the given points
	 * list.
	 */
	private void extractPoints(SimulinkLine line, List<Point> points) {
		String pointsText = line.getParameter(SimulinkConstants.PARAM_Points);
		if (pointsText == null) {
			// this happens for lines without extra bend points
			return;
		}

		try {
			int[] pointsArray = SimulinkUtils.getIntParameterArray(pointsText);
			for (int i = 0; i < pointsArray.length / 2; i++) {
				Point point = new Point(pointsArray[2 * i],
						pointsArray[2 * i + 1]);
				if (!points.isEmpty()) {
					Point lastPoint = CollectionUtils.getLast(points);
					point.x += lastPoint.x;
					point.y += lastPoint.y;
				}
				points.add(point);
			}
		} catch (NumberFormatException e) {
			logger.error("Points array contained invalid number: " + pointsText
					+ ". Skipping points.");
		}
	}

	/** Returns the label data used for rendering a block's label. */
	public LabelLayoutData obtainBlockLabelData(SimulinkBlock block) {
		BlockLayoutData blockLayoutData = obtainBlockLayoutData(block);

		String text = SimulinkUtils.replaceSimulinkLineBreaks(block.getName());

		FontData font = extractFontData(block);
		boolean alternatePlacement = SimulinkConstants.VALUE_alternate
				.equals(block
						.getParameter(SimulinkConstants.PARAM_NamePlacement));
		Point position = calculteBlockLabelPosition(blockLayoutData,
				determineTextBounds(text, font.getAwtFont()),
				alternatePlacement);

		boolean visible = !SimulinkConstants.VALUE_off.equals(block
				.getParameter(SimulinkConstants.PARAM_ShowName));
		return new LabelLayoutData(text, visible, font, position,
				blockLayoutData.getForegroundColor());
	}

	/** Calculates the position of the block's label. */
	private Point calculteBlockLabelPosition(BlockLayoutData blockLayoutData,
			Rectangle textBounds, boolean alternatePlacement) {
		double yPosition;
		double xPosition;
		if (blockLayoutData.getOrientation() == EOrientation.LEFT
				|| blockLayoutData.getOrientation() == EOrientation.RIGHT) {
			xPosition = blockLayoutData.getPosition().getCenterX()
					- textBounds.width / 2.;
			if (alternatePlacement) {
				yPosition = blockLayoutData.getPosition().getMinY()
						- textBounds.height - LABEL_DISTANCE;
			} else {
				yPosition = blockLayoutData.getPosition().getMaxY()
						+ LABEL_DISTANCE;
			}
		} else {
			yPosition = blockLayoutData.getPosition().getCenterY()
					- textBounds.height / 2.;
			if (alternatePlacement) {
				xPosition = blockLayoutData.getPosition().getMinX()
						- textBounds.width - LABEL_DISTANCE;
			} else {
				xPosition = blockLayoutData.getPosition().getMaxX()
						+ LABEL_DISTANCE;
			}
		}
		return new Point((int) xPosition, (int) yPosition);
	}

	/** Returns the bounds of a text for a given font. */
	public static Rectangle determineTextBounds(String text, Font font) {
		double maxWidth = 0;
		double sumHeight = 0;
		for (String line : StringUtils.splitLinesAsList(text)) {
			Rectangle2D bounds = font
					.getStringBounds(line, FONT_RENDER_CONTEXT);
			maxWidth = Math.max(maxWidth, bounds.getWidth());
			sumHeight += bounds.getHeight();
		}
		return new Rectangle((int) maxWidth, (int) sumHeight);
	}

	/** Extracts the font information to be used. */
	public FontData extractFontData(ParameterizedElement element) {
		String fontName = element
				.getParameter(SimulinkConstants.PARAM_FontName);
		if (fontName == null) {
			logger.error("Missing font name for element! Using default.");
			fontName = Font.SANS_SERIF;
		}

		int fontSize = DEFAULT_FONT_SIZE;
		String fontSizeValue = element
				.getParameter(SimulinkConstants.PARAM_FontSize);
		if (fontSizeValue != null) {
			try {
				fontSize = Integer.parseInt(fontSizeValue);
			} catch (NumberFormatException e) {
				logger.error("Invalid font size value: " + fontSizeValue);
			}
		}

		boolean bold = SimulinkConstants.VALUE_bold.equals(element
				.getParameter(SimulinkConstants.PARAM_FontWeight));
		boolean italic = SimulinkConstants.VALUE_italic.equals(element
				.getParameter(SimulinkConstants.PARAM_FontAngle));

		return new FontData(fontName, fontSize, bold, italic);
	}

	/**
	 * Returns the layout data used for rendering a line's label or null if no
	 * label should be shown.
	 */
	public LabelLayoutData obtainLineLabelData(SimulinkLine line) {
		int[] labels = extractLabels(line);
		if (labels == null) {
			return null;
		}

		LineLayoutData lineLayoutData = obtainLineLayoutData(line);
		List<Point> points = lineLayoutData.getPoints();
		if (line.getSrcPort() == null) {
			// replicate artificial start segment for unconnected lines
			points.add(0, points.get(0));
		}

		String text = line.getParameter(SimulinkConstants.PARAM_Name);
		if (StringUtils.isEmpty(text)) {
			return null;
		}
		text = SimulinkUtils.replaceSimulinkLineBreaks(text);

		int segment = labels[0];
		if (segment + 1 >= points.size()) {
			logger.error("Invalid segment " + segment + " used for line "
					+ text);
			return null;
		}

		FontData font = extractFontData(line);
		Point position = calculateLineLabelPosition(segment, labels[1], points,
				determineTextBounds(text, font.getAwtFont()));

		Color color = Color.BLACK;
		if (line.getSrcPort() != null && line.getDstPort() != null) {
			color = lineLayoutData.getColor();
		}

		return new LabelLayoutData(text, true, font, position, color);

	}

	/**
	 * Extracts the values of the labels parameter for a line. If no labels
	 * parameter is found or an error occurs, null is returned.
	 */
	private int[] extractLabels(SimulinkLine line) {
		String labelsString = line.getParameter(SimulinkConstants.PARAM_Labels);
		if (labelsString == null) {
			return null;
		}
		int[] labels;
		try {
			labels = SimulinkUtils.getIntParameterArray(labelsString);
			if (labels.length < 2) {
				logger.error("Invalid line labels: " + labelsString);
				return null;
			}
		} catch (NumberFormatException e) {
			logger.error("Had invalid labels parameter: " + labelsString);
			return null;
		}
		return labels;
	}

	/**
	 * Calculates the position of the line label.
	 * 
	 * @param segment
	 *            the segment of the line the label is centered for.
	 * @param side
	 *            the side of the line (0 or 1). This is interpreted
	 *            differently, depending on the direction of the line.
	 */
	private Point calculateLineLabelPosition(int segment, int side,
			List<Point> points, Rectangle textBounds) {
		boolean rightAligned = segment < 0;
		if (rightAligned) {
			segment = points.size() - 2;
		}

		int x1 = points.get(segment).x;
		int x2 = points.get(segment + 1).x;
		double x = (x2 + x1) / 2.;

		int y1 = points.get(segment).y;
		int y2 = points.get(segment + 1).y;
		double y = (y2 + y1) / 2.;

		if (x2 == x1) {
			// vertical line -> left or right of line
			y -= textBounds.height / 2.;
			if (side == 0 ^ (y1 > y2)) {
				x += LABEL_DISTANCE;
			} else {
				x -= textBounds.width - LABEL_DISTANCE;
			}
		} else {
			// other lines: center on top or bottom
			if (rightAligned) {
				x = x2 - textBounds.width - LABEL_DISTANCE;
			} else if (segment > 0) {
				// do not center first
				x -= textBounds.width / 2.;
			} else {
				x += LABEL_DISTANCE;
			}

			if (side == 0 ^ (x1 < x2) ^ (segment == 0) ^ rightAligned) {
				y += LABEL_DISTANCE;
			} else {
				y -= textBounds.height + LABEL_DISTANCE;
			}
		}

		return new Point((int) x, (int) y);
	}

	/** Returns the label data used for rendering a port's label or null. */
	public LabelLayoutData obtainPortLabelData(SimulinkPortBase port) {
		String blockType = port.getBlock().getType();
		if (SimulinkConstants.TYPE_SubSystem.equals(blockType)) {
			return obtainSubSystemPortLabelData(port);
		}

		if (port instanceof SimulinkInPort
				&& SimulinkConstants.TYPE_Sum.equals(blockType)) {
			return obtainSumInPortLabelData(port);
		}
		return null;
	}

	/**
	 * Returns label data for a subsystem's port or null if no label is used for
	 * this port.
	 */
	private LabelLayoutData obtainSubSystemPortLabelData(SimulinkPortBase port) {
		String showPortLabels = port.getBlock().getParameter(
				SimulinkConstants.PARAM_ShowPortLabels);

		PortLayoutData portLayoutData = port.obtainLayoutData();
		boolean visible = !SimulinkConstants.VALUE_none.equals(showPortLabels);

		EOrientation orientation = port.getBlock().obtainLayoutData()
				.getOrientation();
		if (orientation.isRotated()) {
			// top-down labels are not yet supported and thus are hidden
			visible = false;
		}

		SimulinkBlock portBlock = findPortBlock(port.getBlock(),
				port.getIndex(), port instanceof SimulinkInPort);
		if (portBlock == null || portBlock.getName() == null) {
			return null;
		}

		if (visible
				&& SimulinkConstants.VALUE_FromPortIcon.equals(showPortLabels)
				&& SimulinkConstants.VALUE_off.equals(portBlock
						.getParameter(SimulinkConstants.PARAM_ShowName))) {
			visible = false;
		}

		String text = portBlock.getName();
		Rectangle2D textBounds = PORT_LABEL_FONT_DATA.getAwtFont()
				.getStringBounds(text, FONT_RENDER_CONTEXT);

		Point position = new Point(portLayoutData.getPosition());
		position.y -= textBounds.getHeight() / 2;

		if ((port instanceof SimulinkInPort)
				^ (orientation == EOrientation.LEFT)) {
			position.x += LABEL_DISTANCE;
		} else {
			position.x -= LABEL_DISTANCE + textBounds.getWidth();
		}

		return new LabelLayoutData(text, visible, PORT_LABEL_FONT_DATA,
				position, portLayoutData.getColor());
	}

	/**
	 * Finds the block corresponding to a given port. Returns null if not found.
	 */
	private SimulinkBlock findPortBlock(SimulinkBlock parent, String index,
			boolean inPort) {
		for (SimulinkBlock block : parent.getSubBlocks()) {
			if (inPort
					&& !SimulinkConstants.TYPE_Inport.equals(block.getType())) {
				continue;
			}
			if (!inPort
					&& !SimulinkConstants.TYPE_Outport.equals(block.getType())) {
				continue;
			}

			if (index.equals(block.getParameter(SimulinkConstants.PARAM_Port))) {
				return block;
			}
		}

		return null;
	}

	/** Returns the label for an input port of a sum block. */
	private LabelLayoutData obtainSumInPortLabelData(SimulinkPortBase port) {
		PortLayoutData portLayoutData = port.obtainLayoutData();
		String portsDescription = port.getBlock().getParameter(
				SimulinkConstants.PARAM_Inputs);
		int index = Integer.parseInt(port.getIndex());
		String text = Character.toString(portsDescription
				.charAt(PortLayoutUtils.getLogicalIndexForPort(
						portsDescription, index)));
		if ("-".equals(text)) {
			// we use a unicode "en dash", as the minus is too short
			text = EN_DASH;
		}

		Point position = PortLayoutUtils.getInsetPortPoint(port, port
				.getBlock().obtainLayoutData(), -5);
		Rectangle bounds = determineTextBounds(text,
				PORT_LABEL_FONT_DATA.getAwtFont());
		position.x -= bounds.width / 2;
		position.y -= bounds.height / 2;

		return new LabelLayoutData(text, true, PORT_LABEL_FONT_DATA, position,
				portLayoutData.getColor());
	}

	/**
	 * Returns the directories that should be searched when looking for
	 * references.
	 */
	public UnmodifiableList<File> getReferenceDirectories() {
		return CollectionUtils.asUnmodifiable(referenceDirectories);
	}

	/** Calculates the label data for the annotation's label or null. */
	public LabelLayoutData obtainAnnotationLabelData(
			SimulinkAnnotation annotation) {
		String text = annotation.getParameter(SimulinkConstants.PARAM_Name);
		if (StringUtils.isEmpty(text)) {
			return null;
		}

		AnnotationLayoutData layoutData = obtainAnnotationLayoutData(annotation);

		Point position = new Point(layoutData.getPosition().x + LABEL_DISTANCE,
				layoutData.getPosition().y + LABEL_DISTANCE / 2);
		return new LabelLayoutData(text, true, extractFontData(annotation),
				position, layoutData.getForegroundColor());
	}
}
