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

import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_BlockType;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_Name;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SID;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_id;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Block;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Children;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Model;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_ModelInformation;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Stateflow;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_System;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_chart;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_junction;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_machine;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_state;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_transition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.conqat.lib.simulink.testutils.SimulinkTestBase;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Tests for {@link SLXHandler}. This tests if the model is <em>parsed</em>
 * properly.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51953 $
 * @ConQAT.Rating GREEN Hash: C1042FCB6824E860A5EF2E9B86356F76
 */
public class SLXHandlerTest extends SimulinkTestBase {

	/** Test if block name is set properly. */
	public void testSlxBlockName() throws Exception {

		Attributes emptyAttributes = mock(Attributes.class);

		SLXHandler handler = createModelInformationHead();
		handler.startElement(null, SECTION_Model, null, emptyAttributes);

		Attributes blockAttributes = mock(Attributes.class);

		String blockType = "Sum";
		String blockName = "Sum1";
		String blockSID = "12";

		when(blockAttributes.getValue(PARAM_BlockType)).thenReturn(blockType);
		when(blockAttributes.getValue(PARAM_Name)).thenReturn(blockName);
		when(blockAttributes.getValue(PARAM_SID)).thenReturn(blockSID);

		handler.startElement(null, SECTION_System, null, emptyAttributes);
		handler.startElement(null, SECTION_Block, null, blockAttributes);

		handler.endElement(null, SECTION_Block, null);
		handler.endElement(null, SECTION_System, null);
		handler.endElement(null, SECTION_Model, null);
		createModelInformationTail(handler);

		MutableMDLSection rootSection = handler.getRootModelSection();

		verify(blockAttributes).getValue(PARAM_BlockType);
		verify(blockAttributes).getValue(PARAM_Name);
		verify(blockAttributes).getValue(PARAM_SID);

		MutableMDLSection model = rootSection.getFirstSubSection(SECTION_Model);
		MutableMDLSection system = model.getFirstSubSection(SECTION_System);
		MutableMDLSection block = system.getFirstSubSection(SECTION_Block);

		String resolvedBlockType = block.getParameter(PARAM_BlockType);
		String resolvedBlockName = block.getParameter(PARAM_Name);
		String resolvedBlockSID = block.getParameter(PARAM_SID);

		assertThat(resolvedBlockType, equalTo(blockType));
		assertThat(resolvedBlockName, equalTo(blockName));
		assertThat(resolvedBlockSID, equalTo(blockSID));

	}

	/** Test stateflow model creation. */
	public void testStateflowModelCreation() throws Exception {
		Attributes emptyAttributes = mock(Attributes.class);

		SLXHandler handler = createModelInformationHead();
		handler.startElement(null, SECTION_Stateflow, null, emptyAttributes);

		Attributes idAttributes = mock(Attributes.class);
		when(idAttributes.getValue(PARAM_id)).thenReturn("1", "2", "3", "4",
				"5", "6", "7", "8", "9", "10");

		driveSaxHandler(emptyAttributes, handler, idAttributes);

		MutableMDLSection rootSection = handler.getRootModelSection();

		verify(idAttributes, times(8)).getValue(PARAM_id);

		MutableMDLSection stateflow = rootSection
				.getFirstSubSection(SECTION_Stateflow);

		MutableMDLSection machine = stateflow
				.getFirstSubSection(SECTION_machine);
		assertThat(machine, notNullValue());
		assertThat(machine.getSubSections().getValues(), not(empty()));

		MutableMDLSection machineChildren = machine
				.getFirstSubSection(SECTION_Children);
		assertThat(machineChildren, notNullValue());
		assertThat(machineChildren.getSubSections().getValues(), not(empty()));

		MutableMDLSection chart = machineChildren
				.getFirstSubSection(SECTION_chart);
		assertThat(chart, notNullValue());
		assertThat(chart.getSubSections().getValues(), not(empty()));

		MutableMDLSection chartChildren = chart
				.getFirstSubSection(SECTION_Children);
		assertThat(chartChildren, notNullValue());
		assertThat(chartChildren.getSubSections().getValues(), not(empty()));

		assertThat(chartChildren.getSubSections(SECTION_state), hasSize(2));
		assertThat(chartChildren.getSubSections(SECTION_transition), hasSize(3));
		assertThat(chartChildren.getSubSections(SECTION_junction), hasSize(1));
	}

	/** Feeds the SAX handler with data for the test. */
	private void driveSaxHandler(Attributes emptyAttributes,
			SLXHandler handler, Attributes idAttributes) throws SAXException {
		handler.startElement(null, SECTION_machine, null, idAttributes);
		handler.startElement(null, SECTION_Children, null, emptyAttributes);

		handler.startElement(null, SECTION_chart, null, idAttributes);
		handler.startElement(null, SECTION_Children, null, emptyAttributes);

		handler.startElement(null, SECTION_state, null, idAttributes);
		handler.endElement(null, SECTION_state, null);

		handler.startElement(null, SECTION_state, null, idAttributes);
		handler.endElement(null, SECTION_state, null);

		handler.startElement(null, SECTION_transition, null, idAttributes);
		handler.endElement(null, SECTION_transition, null);
		handler.startElement(null, SECTION_transition, null, idAttributes);
		handler.endElement(null, SECTION_transition, null);
		handler.startElement(null, SECTION_transition, null, idAttributes);
		handler.endElement(null, SECTION_transition, null);
		handler.startElement(null, SECTION_junction, null, idAttributes);
		handler.endElement(null, SECTION_junction, null);

		handler.endElement(null, SECTION_Children, null);
		handler.endElement(null, SECTION_chart, null);

		handler.endElement(null, SECTION_Children, null);
		handler.endElement(null, SECTION_machine, null);
		handler.endElement(null, SECTION_Stateflow, null);

		createModelInformationTail(handler);
	}

	/** Setup basic slx tags for an empty model. */
	private SLXHandler createModelInformationHead() throws SAXException {
		SLXHandler handler = new SLXHandler();
		Locator locator = mock(Locator.class);
		when(locator.getLineNumber()).thenReturn(0);
		handler.setDocumentLocator(locator);

		Attributes emptyAttributes = mock(Attributes.class);

		handler.startElement(null, SECTION_ModelInformation, null,
				emptyAttributes);
		return handler;
	}

	/** Finalize model information tag. */
	private SLXHandler createModelInformationTail(SLXHandler handler) {
		handler.endElement(null, SECTION_ModelInformation, null);
		return handler;
	}

}