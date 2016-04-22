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
package org.conqat.lib.simulink.builder;

/**
 * Base class for sections in an MDL file.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51727 $
 * @ConQAT.Rating GREEN Hash: 5DA18D06F716A4115FEFF06A178296EA
 */
/* package */abstract class MDLSectionBase {

	/** Name of the section. */
	private final String name;

	/** Line number within the MDL file (1-based). */
	private final int lineNumber;

	/** Constructor. */
	protected MDLSectionBase(String name, int lineNumber) {
		this.name = name;
		this.lineNumber = lineNumber;
	}

	/** @see #name */
	public String getName() {
		return name;
	}

	/** @see #lineNumber */
	public int getLineNumber() {
		return lineNumber;
	}

	/** Returns string representation including name and line number. */
	@Override
	public String toString() {
		return name + " [line:" + lineNumber + "]";
	}

}
