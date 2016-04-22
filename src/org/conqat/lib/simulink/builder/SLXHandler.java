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

import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_BlockType;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_ClassName;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_Dimension;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_Name;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_ObjectID;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_PropName;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_Ref;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SID;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SSID;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_Type;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_id;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_name;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Array;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Block;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_ModelInformation;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Object;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Parameter;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_chart;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_data;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_event;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_instance;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_junction;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_machine;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_state;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_target;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_transition;

import java.util.ArrayDeque;
import java.util.Deque;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX handler for building {@link MutableMDLSection} from an .slx file.
 * 
 * @author $Author: schulte$
 * @version $Rev: 51734 $
 * @ConQAT.Rating GREEN Hash: 35974A68A38E6BEE3B70198B6178B04A
 */
public class SLXHandler extends DefaultHandler {

	/** Parsing stack. The top-most element is the currently parsed element. */
	private final Deque<MutableMDLSection> stack = new ArrayDeque<MutableMDLSection>();

	/** Locator to retrieve line number while parsing. */
	private Locator locator;

	/** Root model section. */
	private MutableMDLSection rootModelSection = null;

	/** {@inheritDoc} */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (SECTION_ModelInformation.equals(localName)) {
			MutableMDLSection modelInformation = new MutableMDLSection(
					SECTION_ModelInformation, locator.getLineNumber());
			stack.push(modelInformation);
			return;
		}

		if (stack.isEmpty()) {
			throw new SAXException(
					"Slx file does not start with ModelInformation");
		}

		startInnerElement(localName, attributes);
	}

	/**
	 * Starts an inner element, i.e. not the top-level model information
	 * element.
	 */
	private void startInnerElement(String localName, Attributes attributes) {
		switch (localName) {
		case SECTION_Parameter:
			startParameterElement(attributes);
			break;
		case SECTION_Block:
			createSubSectionWithAttributes(localName, attributes, PARAM_Name,
					PARAM_BlockType, PARAM_SID);
			break;
		case SECTION_Object:
			createSubSectionWithAttributes(localName, attributes,
					PARAM_PropName, PARAM_ObjectID, PARAM_ClassName);
			break;
		case SECTION_Array:
			createSubSectionWithAttributes(localName, attributes,
					PARAM_PropName, PARAM_Type, PARAM_Dimension);
			break;
		case SECTION_state:
		case SECTION_transition:
		case SECTION_junction:
		case SECTION_event:
		case SECTION_target:
		case SECTION_data:
		case SECTION_machine:
		case SECTION_chart:
		case SECTION_instance:
			createSubSectionWithAttributes(localName, attributes, PARAM_id,
					PARAM_SSID, PARAM_name);
			break;
		default:
			createSubSectionWithAttributes(localName, attributes);
		}
	}

	/** Starts an XML element of type "parameter". */
	private void startParameterElement(Attributes attributes) {
		MutableMDLSection currentSection = stack.peek();
		currentSection.setCurrentParameter(attributes.getValue(PARAM_Name));

		/* Some rare parameters use a ref attribute and have no text. */
		String ref = attributes.getValue(PARAM_Ref);
		if (ref != null) {
			currentSection.setParameter(attributes.getValue(PARAM_Name), ref);
		}
	}

	/**
	 * Creates a new subsection based on the given local name and pushes it to
	 * the {@link #stack}. All parameters given are copied from the attributes
	 * to the section (if they are not null).
	 */
	private void createSubSectionWithAttributes(String localName,
			Attributes attributes, String... parameterNames) {
		MutableMDLSection subSection = new MutableMDLSection(localName,
				locator.getLineNumber());
		copyNonNullParameters(attributes, subSection, parameterNames);
		pushSectionToStack(subSection);
	}

	/**
	 * Copies all of the given parameter from the attributes to the section if
	 * they exist (not null).
	 */
	private void copyNonNullParameters(Attributes attributes,
			MutableMDLSection subSection, String... parameterNames) {
		for (String parameterName : parameterNames) {
			String value = attributes.getValue(parameterName);
			if (value != null) {
				subSection.setParameter(parameterName, value);
			}
		}
	}

	/**
	 * Push a sub section to the stack. May only be invoked on a non-empty
	 * stack.
	 */
	private void pushSectionToStack(MutableMDLSection subSection) {
		MutableMDLSection parent = stack.peek();
		parent.addSubSection(subSection);
		stack.push(subSection);
	}

	/** {@inheritDoc} */
	@Override
	public void endElement(String uri, String localName, String qName) {
		if (SECTION_ModelInformation.equals(localName)) {
			rootModelSection = stack.pop();
		} else if (SECTION_Parameter.equals(localName) && !stack.isEmpty()) {
			MutableMDLSection section = stack.peek();
			String currentParameter = section.getCurrentParameter();
			section.appendParameter(currentParameter, "");
			section.resetCurrentParameter();
		} else if (stack.size() > 1) {
			MutableMDLSection section = stack.pop();
			section.resetCurrentParameter();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void characters(char[] ch, int start, int length) {
		if (!stack.isEmpty()) {
			MutableMDLSection container = stack.peek();
			String currentParameter = container.getCurrentParameter();
			if (currentParameter != null) {
				container.appendParameter(currentParameter, new String(ch,
						start, length));
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	/** Returns the root of the model (called "model information" in the XML). */
	public MutableMDLSection getRootModelSection() {
		return rootModelSection;
	}
}
