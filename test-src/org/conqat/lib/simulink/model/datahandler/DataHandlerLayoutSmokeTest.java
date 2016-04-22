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

import java.io.File;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.conqat.lib.commons.filesystem.FileExtensionFilter;
import org.conqat.lib.commons.filesystem.FileSystemUtils;
import org.conqat.lib.commons.test.CCSMTestCaseBase;
import org.conqat.lib.simulink.util.SimulinkBlockRenderer;

/**
 * Smoke test for the layout aspects of the data handlers. This uses the
 * {@link SimulinkBlockRenderer} to create an image and compare it to a stored
 * reference image. <b>Attention</b>: This test may not run on every platform as
 * it relies on image rendering which may differ between platforms.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51957 $
 * @ConQAT.Rating GREEN Hash: 5CD56B6A1FE7851C6C51783345797AA4
 */
public class DataHandlerLayoutSmokeTest extends CCSMTestCaseBase {

	/** Create a smoke test suite. */
	public static Test suite() {

		File dir = new DataHandlerLayoutSmokeTest().getTestDataDirectory();
		List<File> files = FileSystemUtils.listFilesRecursively(dir,
				new FileExtensionFilter("mdl", "slx"));

		TestSuite suite = new TestSuite(
				DataHandlerLayoutSmokeTest.class.getSimpleName());
		suite.setName(DataHandlerLayoutSmokeTest.class.getSimpleName() + " ["
				+ files.size() + " test files]");
		for (File file : files) {
			suite.addTest(new DataHandlerLayoutSmokeTestlet(file));
		}
		return suite;
	}
}
