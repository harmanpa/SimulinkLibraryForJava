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
package org.conqat.lib.simulink.smoke;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.conqat.lib.commons.filesystem.FileExtensionFilter;
import org.conqat.lib.commons.filesystem.FileSystemUtils;
import org.conqat.lib.commons.string.StringUtils;

/**
 * The smoke test suite inspects this package's test data directory. For each
 * file the test defined {@link SmokeTestlet} is executed.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 50201 $
 * @ConQAT.Rating GREEN Hash: DA901F634966AF29080EFBBAD2D00086
 */
public class SmokeTest {

	/** Create a smoke test suite. */
	public static Test suite() throws UnsupportedEncodingException, IOException {

		File dir = new File("test-data/"
				+ SmokeTest.class.getPackage().getName());

		List<File> files = listMdlFiles(dir);

		// create suite
		TestSuite suite = new TestSuite("SmokeTest");
		suite.setName("Smoke Test [" + files.size() + " test files]");
		for (File file : files) {
			suite.addTest(new SmokeTestlet(file));
		}

		// return suite
		return suite;
	}

	/**
	 * List MDL files from test-data. Additionally, load MDL files from paths in
	 * extra file.
	 */
	private static List<File> listMdlFiles(File dir)
			throws UnsupportedEncodingException, IOException {
		List<File> files = FileSystemUtils.listFilesRecursively(dir,
				new FileExtensionFilter("mdl"));

		File additionalPathsFile = new File(dir, "additional-paths.txt");
		if (additionalPathsFile.canRead()) {
			for (String line : FileSystemUtils
					.readLinesUTF8(additionalPathsFile)) {
				if (StringUtils.isEmpty(line) || line.startsWith("#")) {
					continue;
				}

				files.addAll(FileSystemUtils.listFilesRecursively(
						new File(line), new FileExtensionFilter("mdl")));
			}
		}
		return files;
	}

}