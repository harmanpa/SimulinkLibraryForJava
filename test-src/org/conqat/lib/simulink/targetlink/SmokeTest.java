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
package org.conqat.lib.simulink.targetlink;

import java.io.File;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.conqat.lib.commons.filesystem.FileExtensionFilter;
import org.conqat.lib.commons.filesystem.FileSystemUtils;

/**
 * The smoke test suite inspects this package's test data directory. For each
 * MDL file the test defined in {@link MDLSmokeTestlet} is executed. For each
 * TXT file the test defined in {@link TextFileBasedSmokeTestlet} is executed.
 * 
 * @author deissenb
 * @author $Author: heinemann $
 * @version $Rev: 50225 $
 * @ConQAT.Rating GREEN Hash: 486161B597C1DA9A22C4DF601C025870
 */
public class SmokeTest {

	/**
	 * Create a smoke test suite.
	 */
	public static Test suite() {

		File dir = new File("test-data/"
				+ SmokeTest.class.getPackage().getName());

		List<File> mdlFiles = FileSystemUtils.listFilesRecursively(dir,
				new FileExtensionFilter("mdl"));

		List<File> txtFiles = FileSystemUtils.listFilesRecursively(dir,
				new FileExtensionFilter("txt"));

		TestSuite suite = new TestSuite("SmokeTest");
		suite.setName("Smoke Test [" + (mdlFiles.size() + txtFiles.size())
				+ " test files]");
		for (File file : mdlFiles) {
			suite.addTest(new MDLSmokeTestlet(file.getName()));
		}

		for (File file : txtFiles) {
			suite.addTest(new TextFileBasedSmokeTestlet(file.getName()));
		}
		return suite;
	}
}