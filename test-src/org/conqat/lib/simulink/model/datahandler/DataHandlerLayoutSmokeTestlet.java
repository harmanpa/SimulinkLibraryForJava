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

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.conqat.lib.commons.filesystem.FileSystemUtils;
import org.conqat.lib.commons.logging.SimpleLogger;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.commons.test.TestletBase;
import org.conqat.lib.simulink.builder.ModelBuildingParameters;
import org.conqat.lib.simulink.builder.SimulinkModelBuilder;
import org.conqat.lib.simulink.model.SimulinkModel;
import org.conqat.lib.simulink.util.SimulinkBlockRenderer;
import org.junit.Ignore;

/**
 * Testlet for the {@link DataHandlerLayoutSmokeTest}.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51958 $
 * @ConQAT.Rating GREEN Hash: 984DD1812244ACAB97F502D8C7C1384E
 */
@Ignore
public class DataHandlerLayoutSmokeTestlet extends TestletBase {

	/** Name of the fixed font used for rendering. */
	private static final String FIXED_FONT_NAME = "font/DejaVuSansCondensed.ttf.gz";

	/** Name of file to read. */
	private final File simulinkFile;

	/** Create new testlet. */
	/* package */DataHandlerLayoutSmokeTestlet(File simulinkFile) {
		this.simulinkFile = simulinkFile;
	}

	/** Return name of config file as smoke test name */
	@Override
	public String getName() {
		return simulinkFile.getName();
	}

	/** {@inheritDoc} */
	@Override
	public void test() throws Exception {
		SimulinkBlockRenderer simulinkBlockRenderer = new SimulinkBlockRenderer();
		try (InputStream in = FileSystemUtils
				.autoDecompressStream(new FileInputStream(
						useTestFile(FIXED_FONT_NAME)))) {
			simulinkBlockRenderer.setOverrideFont(Font.createFont(
					Font.TRUETYPE_FONT, in));
		}

		SimulinkModel model;
		try (SimulinkModelBuilder simulinkModelBuilder = new SimulinkModelBuilder(
				simulinkFile, new SimpleLogger())) {
			model = simulinkModelBuilder
					.buildModel(new ModelBuildingParameters()
							.setPreserveUnconnectedLines(true));
		}
		BufferedImage actualImage = simulinkBlockRenderer.renderBlock(model);

		FileSystemUtils.ensureDirectoryExists(getTmpDirectory());
		ImageIO.write(actualImage, "PNG", new File(getTmpDirectory(),
				simulinkFile.getName() + ".png"));

		String baseName = StringUtils.stripSuffix(simulinkFile.getName(),
				SimulinkModelBuilder.MDL_FILE_EXTENSION);
		baseName = StringUtils.stripSuffix(baseName,
				SimulinkModelBuilder.SLX_FILE_EXTENSION);
		File expectedFile = useTestFile(baseName + "_expected.png");
		assertTrue("Expected image file " + expectedFile
				+ " missing or not readable!", expectedFile.canRead());
		BufferedImage expectedImage = ImageIO.read(expectedFile);

		assertImagesEqual(expectedImage, actualImage);
	}

	/** Asserts that two images are equal. */
	private void assertImagesEqual(BufferedImage expectedImage,
			BufferedImage actualImage) {
		assertEquals(expectedImage.getWidth(), actualImage.getWidth());
		assertEquals(expectedImage.getHeight(), actualImage.getHeight());
		for (int y = 0; y < expectedImage.getHeight(); ++y) {
			for (int x = 0; x < expectedImage.getWidth(); ++x) {
				assertEquals(expectedImage.getRGB(x, y),
						actualImage.getRGB(x, y));
			}
		}
	}
}
