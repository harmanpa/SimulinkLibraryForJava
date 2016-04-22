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

import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Library;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Model;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Stateflow;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.conqat.lib.commons.filesystem.FileSystemUtils;
import org.conqat.lib.commons.logging.ILogger;
import org.conqat.lib.commons.xml.XMLUtils;
import org.conqat.lib.simulink.model.ParameterizedElement;
import org.conqat.lib.simulink.model.SimulinkConstants;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.model.datahandler.ModelDataHandlerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Main Simulink/Stateflow model building class.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51733 $
 * @ConQAT.Rating GREEN Hash: EC1E36E3815DF6E31FF71D20F22FD177
 */
public class SimulinkModelBuilder implements Closeable {

	/** Path to model file within slx zip. */
	private static final String SLX_MODEL_FILE = "simulink/blockdiagram.xml";

	/** Slx file extension. */
	public static final String SLX_FILE_EXTENSION = ".slx";

	/** Mdl file extension. */
	public static final String MDL_FILE_EXTENSION = ".mdl";

	/** InputStream to read model. */
	private final InputStream inputStream;

	/**
	 * Flag to indicate whether the model is in the new .slx or the old .mdl
	 * file format.
	 */
	private final boolean isSlxFormat;

	/** Logger. */
	private final ILogger logger;

	/** Origin id. May be null. */
	private final String originId;

	/**
	 * Create new model builder.
	 * 
	 * @param inputStream
	 *            the stream to build the model from.
	 * @param logger
	 *            logger for reporting anomalies. You may use SimpleLogger here.
	 * @param filename
	 *            the name of the file. This is only used for determining the
	 *            file format to be used.
	 * @param originId
	 *            the origin id for the model. See
	 *            {@link SimulinkModel#getOriginId()}
	 */
	public SimulinkModelBuilder(InputStream inputStream, ILogger logger,
			String filename, String originId) throws IOException {
		if (filename.toLowerCase().endsWith(SLX_FILE_EXTENSION)) {
			ZipInputStream zipInputStream = new ZipInputStream(inputStream);
			moveStreamToModelFileEntry(zipInputStream);
			this.inputStream = zipInputStream;
			this.isSlxFormat = true;
		} else if (filename.toLowerCase().endsWith(MDL_FILE_EXTENSION)) {
			this.inputStream = inputStream;
			this.isSlxFormat = false;
		} else {
			throw new IOException("Unknown Simulink file extension found for "
					+ filename);
		}
		this.logger = logger;
		this.originId = originId;
	}

	/**
	 * Moves the internal position of the given ZIP stream to point to the
	 * {@link #SLX_MODEL_FILE}. Throws an exception if the entry was not found.
	 */
	private void moveStreamToModelFileEntry(ZipInputStream zipInputStream)
			throws IOException {
		ZipEntry entry;
		while ((entry = zipInputStream.getNextEntry()) != null) {
			if (SLX_MODEL_FILE.equals(entry.getName())) {
				return;
			}
		}
		throw new IOException("No entry named " + SLX_FILE_EXTENSION
				+ " found.");
	}

	/**
	 * Create model builder.
	 * 
	 * @param file
	 *            file to parse
	 * @param logger
	 *            logger for reporting anomalies. You may use SimpleLogger here.
	 * @param originId
	 *            the origin id for the model. See
	 *            {@link SimulinkModel#getOriginId()}
	 * @throws ZipException
	 *             if a ZIP format error has occurred
	 * @throws IOException
	 *             if an I/O error has occurred
	 */
	public SimulinkModelBuilder(File file, ILogger logger, String originId)
			throws ZipException, IOException {
		this(new FileInputStream(file), logger, file.getName(), originId);
	}

	/**
	 * Create model builder. Origin id of the model is set to null.
	 * 
	 * @param file
	 *            file to parse
	 * @param logger
	 *            logger for reporting anomalies. You may use SimpleLogger here.
	 * @throws ZipException
	 *             if a ZIP format error has occurred
	 * @throws IOException
	 *             if an I/O error has occurred
	 */
	public SimulinkModelBuilder(File file, ILogger logger) throws ZipException,
			IOException {
		this(file, logger, null);
	}

	/** Build and return model with default parameters. */
	public SimulinkModel buildModel() throws SimulinkModelBuildingException {
		return buildModel(new ModelBuildingParameters());
	}

	/** Build and return model. */
	public SimulinkModel buildModel(ModelBuildingParameters parameters)
			throws SimulinkModelBuildingException {
		parameters.setLogger(logger);
		MDLSection simulinkFile = parseFile(parameters);

		MDLSection modelSection = getSimulinkModelSection(simulinkFile);
		SimulinkModel model = new SimulinkModel(modelSection.getName().equals(
				SECTION_Library), originId,
				ModelDataHandlerFactory.createModelHandler(modelSection
						.getParameter(SimulinkConstants.PARAM_Version),
						isSlxFormat, parameters));
		addParameters(model, modelSection);

		// build Stateflow machine first, as the state machines are referenced
		// from Simulink blocks
		MDLSection stateflowSection = simulinkFile
				.getFirstSubSection(SECTION_Stateflow);
		if (stateflowSection != null) {
			new StateflowBuilder(model, logger)
					.buildStateflow(stateflowSection);
		}

		new SimulinkBuilder(model, parameters, isSlxFormat)
				.buildSimulink(modelSection);
		return model;
	}

	/**
	 * Determine the section that holds the Simulink model. This may be
	 * {@link SimulinkConstants#SECTION_Model} or
	 * {@link SimulinkConstants#SECTION_Library}</code>.
	 * 
	 * @param simulinkFile
	 *            the Simulink file
	 * @throws SimulinkModelBuildingException
	 *             if no or multiple {@link SimulinkConstants#SECTION_Model}/
	 *             {@link SimulinkConstants#SECTION_Library}</code> were found
	 */
	private static MDLSection getSimulinkModelSection(MDLSection simulinkFile)
			throws SimulinkModelBuildingException {
		List<MDLSection> namedBlocks = simulinkFile
				.getSubSections(SECTION_Model);

		if (namedBlocks.isEmpty()) {
			namedBlocks = simulinkFile.getSubSections(SECTION_Library);
		}

		if (namedBlocks.size() != 1) {
			throw new SimulinkModelBuildingException(
					"Model must have exactly one Model or Library block.");
		}

		return namedBlocks.get(0);
	}

	/**
	 * Parse Simulink file.
	 * 
	 * @throws SimulinkModelBuildingException
	 *             if an exception occurred during parsing.
	 */
	private MDLSection parseFile(ModelBuildingParameters parameters)
			throws SimulinkModelBuildingException {
		MDLSection section = null;
		if (isSlxFormat) {
			SLXHandler handler = new SLXHandler();
			try {
				XMLUtils.parseSAX(new InputSource(inputStream), handler);
				MutableMDLSection slxFile = handler.getRootModelSection();
				SLXModelSanitizer.sanitize(slxFile);
				section = slxFile.asImmutable();
			} catch (SAXException | IOException e) {
				throw new SimulinkModelBuildingException(e);
			}
		} else {
			MDLScanner scanner = new MDLScanner(new InputStreamReader(
					inputStream, parameters.getCharset()));
			MDLParser parser = new MDLParser(scanner, logger);
			try {
				section = (MDLSection) parser.parse().value;
			} catch (Exception e) {
				throw new SimulinkModelBuildingException(e);
			}
		}
		return section;
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		FileSystemUtils.close(inputStream);
	}

	/**
	 * Add all parameters defined in a section to a Simulink block. The
	 * {@link SimulinkConstants#PARAM_Points} parameter is treated specially
	 * here. This parameter stores layout information and this is merged instead
	 * of overwritten. This behavior is required to deal with the hierarchy in
	 * lines caused by branches.
	 */
	/* package */static void addParameters(ParameterizedElement element,
			MDLSection section) {
		for (String name : section.getParameterNames()) {

			// we handle the points specially, as they must be joined to allow
			// proper layouting
			if (SimulinkConstants.PARAM_Points.equals(name)) {
				String value = element
						.getParameter(SimulinkConstants.PARAM_Points);
				String newValue = section
						.getParameter(SimulinkConstants.PARAM_Points);
				if (value == null) {
					value = newValue;
				} else if (newValue != null) {
					// prepend value by stripping the opening/closing bracket
					// from the arrays, i.e. [1; 2] and [3; 4] should become
					// [1; 2; 3; 4]
					value = newValue.substring(0, newValue.length() - 1) + "; "
							+ value.substring(1);
				}
				element.setParameter(SimulinkConstants.PARAM_Points, value);
			} else {
				element.setParameter(name, section.getParameter(name));
			}
		}
	}

}