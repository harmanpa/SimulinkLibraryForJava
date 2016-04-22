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
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.conqat.lib.commons.filesystem.FileSystemUtils;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.commons.test.TestletBase;
import org.junit.Ignore;

/**
 * Smoke testlet for Targetlink parser. This reads a Targetlink struct
 * definition from a text file and parses it.
 * 
 * @author $Author: hummelb $
 * @version $Rev: 50662 $
 * @ConQAT.Rating GREEN Hash: BEBBA329A12FC465838EEF48189DCB63
 */
@Ignore
public class TextFileBasedSmokeTestlet extends TestletBase {

	/** Name of file to read. */
	private final String filename;

	/** Create new testlet. */
	/* package */TextFileBasedSmokeTestlet(String filename) {
		this.filename = filename;
	}

	/** Return name smoke test. */
	@Override
	public String getName() {
		return filename;
	}

	/** Read Targetlink struct definition from a text file and parse it. */
	@Override
	public void test() throws Exception {
		TargetlinkDataScanner scanner = new TargetlinkDataScanner(
				new InputStreamReader(
						new FileInputStream(useTestFile(filename)),
						FileSystemUtils.UTF8_CHARSET));
		TargetlinkDataParser parser = new TargetlinkDataParser(scanner);
		TargetlinkStruct struct = (TargetlinkStruct) parser.parse().value;

		String actual = struct.toString();
		FileSystemUtils.writeFileUTF8(new File(getTmpDirectory(), filename
				+ ".actual"), actual);

		File expectedFile = new File(getTestDataDirectory(), filename
				+ ".expected");
		assertTrue("Expected file " + expectedFile
				+ " does not exist or is not readable!", expectedFile.canRead());
		String expected = FileSystemUtils.readFileUTF8(expectedFile);

		assertEquals(StringUtils.normalizeLineBreaks(expected),
				StringUtils.normalizeLineBreaks(actual));
	}
}