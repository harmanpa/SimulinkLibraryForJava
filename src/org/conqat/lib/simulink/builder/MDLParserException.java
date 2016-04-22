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

/**
 * Exception thrown by the MDL parser. Exceptions may wrap other exceptions. *
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51725 $
 * @ConQAT.Rating GREEN Hash: 35D4B575E2F0FAEA89FC0B9506A31602
 */
public class MDLParserException extends Exception {

	/** Version used for serialization. */
	private static final long serialVersionUID = 1;

	/** Number of line where exception occurred (1-based). */
	private int lineNumber;

	/** Number of column where exception occurred (1-based). */
	private int columnNumber;

	/** Constructor. */
	public MDLParserException(String message) {
		super(message);
	}

	/** Constructor. */
	public MDLParserException(String message, int lineNumber, int columnNumber) {
		super(message + " [line: " + lineNumber + ", col: " + columnNumber
				+ "]");
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}

	/** Constructor. */
	public MDLParserException(Exception exception) {
		super("Unknown Exception caused by: " + exception.getMessage(),
				exception);
	}

	/** @see #columnNumber */
	public int getColumnNumber() {
		return columnNumber;
	}

	/** @see #lineNumber */
	public int getLineNumber() {
		return lineNumber;
	}
}