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
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_ComputedModelVersion;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_Dimension;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_Dst;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_DstBlock;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_DstPort;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_Name;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_ObjectID;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_PropName;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SID;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SSID;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SlxModelName;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_Src;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SrcBlock;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SrcPort;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_Type;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_Version;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_id;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_linkNode;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_machine;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_treeNode;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_BlockDefaults;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Branch;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Children;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Library;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Line;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Model;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_Stateflow;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_System;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_SystemDefaults;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_chart;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_data;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_event;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_junction;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_machine;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_state;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_target;
import static org.conqat.lib.simulink.model.SimulinkConstants.SECTION_transition;
import static org.conqat.lib.simulink.model.SimulinkConstants.TYPE_Enable;
import static org.conqat.lib.simulink.model.SimulinkConstants.TYPE_Trigger;
import static org.conqat.lib.simulink.model.SimulinkConstants.TYPE_Ifaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.conqat.lib.commons.string.StringUtils;

/**
 * Helper class to sanitize a model created in the new SLX file format to match
 * the MDL file format.
 * 
 * This class performs "flattening" in some cases, i.e. removal of hierarchy.
 * The parameters of the removed sections are moved to the parent sections.
 * 
 * @author $Author: hummelb $
 * @version $Rev: 52208 $
 * @ConQAT.Rating GREEN Hash: 959A50C94AFB0067FA9A583D52C1133F
 */
public class SLXModelSanitizer {

	/**
	 * Pattern to match block and port information.
	 * <ol>
	 * <li>block id</li>
	 * <li>matlab block id (only for matlab blocks)</li>
	 * <li>port type</li>
	 * <li>port id (optional in case of out, in, trigger or enable port)</li>
	 * <ol>
	 */
	private static final Pattern BLOCK_PORT_PATTERN = Pattern
			.compile("([\\d]+)(::[\\d]+)?((#)(out|in|trigger|enable|ifaction))((:)([\\d]+))?");

	/** Names of model subsections that should be flattened. */
	private static final Set<String> FLATTENED_MODEL_SUBSECTIONS = new HashSet<>(
			Arrays.asList("GraphicalInterface", "UserParameters",
					"ConfigManagerSettings", "EditorSettings",
					"SimulationSettings", "Verification", "ExternalMode",
					"EngineSettings", "ModelReferenceSettings",
					"ConfigurationSet", "ConcurrentExecutionSettings",
					"MaskDefaults", "MaskParameterDefaults"));

	/** Parameter names to be ignored during flattening. */
	private static final Set<String> FLATTENING_IGNORED_PARAMETER_NAMES = new HashSet<>(
			Arrays.asList(PARAM_PropName, PARAM_ObjectID, PARAM_ClassName,
					PARAM_Type, PARAM_Dimension, PARAM_BlockType, PARAM_SID));

	/**
	 * Sanitizes the given model to conform with MDL structure. Note that the
	 * section will be modified.
	 */
	public static void sanitize(MutableMDLSection section) {
		section.setParameter(PARAM_Name, StringUtils.EMPTY_STRING);

		sanitizeFirstModelSectionOfName(section, SECTION_Model);
		sanitizeFirstModelSectionOfName(section, SECTION_Library);

		MutableMDLSection stateflow = section
				.getFirstSubSection(SECTION_Stateflow);
		if (stateflow != null) {
			MutableMDLSection machine = stateflow
					.getFirstSubSection(SECTION_machine);
			if (machine != null) {
				sanitizeStateflowSection(stateflow, machine);
			}
		}
	}

	/**
	 * Sanitizes the first section of given name. The section name must refer to
	 * a model or a library (which is a special kind of model).
	 */
	private static void sanitizeFirstModelSectionOfName(MutableMDLSection file,
			String sectionName) {
		MutableMDLSection model = file.getFirstSubSection(sectionName);
		if (model != null) {
			flattenModel(model);
			sanitizeModel(model);
			sanitizeLines(model);
		}
	}

	/**
	 * The MDL file format only supports the following four sections to
	 * structure model meta information:
	 * <ul>
	 * <li>BlockDefaults</li>
	 * <li>AnnotationDefaults</li>
	 * <li>LineDefaults</li>
	 * <li>BlockParameterDefaults</li>
	 * </ul>
	 * 
	 * The sections of the SLX file format listed in
	 * {@link #FLATTENED_MODEL_SUBSECTIONS} are therefore removed and all
	 * parameters added to the model section.
	 */
	private static void flattenModel(MutableMDLSection model) {
		for (MutableMDLSection subSection : model.getSubSections().getValues()) {
			if (FLATTENED_MODEL_SUBSECTIONS.contains(subSection.getName())) {
				flattenSection(model, subSection);
			}
		}
	}

	/**
	 * Flattens the section and all subsections. Parameters are added to the
	 * model and the section and all subsections deleted.
	 */
	private static void flattenSection(MutableMDLSection model,
			MutableMDLSection section) {
		for (Entry<String, String> parameter : section.getParameters()
				.entrySet()) {
			// The following parameters must be ignored since they would
			// conflict with the model parameters.
			if (FLATTENING_IGNORED_PARAMETER_NAMES.contains(parameter.getKey())) {
				continue;
			}
			model.getParameters().put(parameter.getKey(), parameter.getValue());
		}

		for (MutableMDLSection subSection : section.getSubSections()
				.getValues()) {
			flattenSection(model, subSection);
		}

		model.removeSection(section);
	}

	/**
	 * Sanitizes the model:
	 * <ul>
	 * <li>Set the model name</li>
	 * <li>Set the model version</li>
	 * <li>Set the system name</li>
	 * </ul>
	 */
	private static void sanitizeModel(MutableMDLSection model) {
		String modelName = model.getParameter(PARAM_SlxModelName);
		if (modelName == null) {
			modelName = StringUtils.EMPTY_STRING;
		}
		model.setParameter(PARAM_Name, modelName);

		String version = model.getParameter(PARAM_ComputedModelVersion);
		if (version == null) {
			// Still unknown where the model version is stored if
			// PARAM_ComputedModelVersion is empty.
			version = "1.0";
		}
		model.setParameter(PARAM_Version, version);

		// The mdl format requires both the model and the system section to have
		// the model name.
		MutableMDLSection system = model.getFirstSubSection(SECTION_System);
		if (system != null) {
			system.setParameter(PARAM_Name, modelName);
			flattenSystemDefaults(model);
		}
	}

	/**
	 * Flattens the system defaults section by adding all parameters to the
	 * block defaults and removing the system defaults section from the model.
	 */
	/* package */static void flattenSystemDefaults(MutableMDLSection model) {
		MutableMDLSection blockDefaults = model
				.getFirstSubSection(SECTION_BlockDefaults);
		MutableMDLSection systemDefaults = model
				.getFirstSubSection(SECTION_SystemDefaults);

		if (systemDefaults != null) {
			// Create empty block defaults if the section does not exist yet.
			if (blockDefaults == null) {
				blockDefaults = new MutableMDLSection(SECTION_BlockDefaults, -1);
				model.addSubSection(blockDefaults);
			}

			for (Entry<String, String> parameter : systemDefaults
					.getParameters().entrySet()) {
				String key = SECTION_System + "." + parameter.getKey();
				blockDefaults.setParameter(key, parameter.getValue());
			}

			model.removeSection(systemDefaults);
		}
	}

	/** Sanitize lines and branches recursively. */
	private static void sanitizeLines(MutableMDLSection container) {
		for (MutableMDLSection subSection : container.getSubSections()
				.getValues()) {
			String name = subSection.getName();
			if (SECTION_Line.equals(name) || SECTION_Branch.equals(name)) {
				sanitizeLine(subSection);
			}
			sanitizeLines(subSection);
		}
	}

	/**
	 * Sanitizes a single line by extracting SrcBlock and SrcPort from Src
	 * parameter respectively DstBlock and DstPort from Dst parameter.
	 */
	/* package */static void sanitizeLine(MutableMDLSection lineOrBranch) {
		sanitizeLineSrcOrDst(lineOrBranch, PARAM_Src, PARAM_SrcPort,
				PARAM_SrcBlock, false);
		sanitizeLineSrcOrDst(lineOrBranch, PARAM_Dst, PARAM_DstPort,
				PARAM_DstBlock, true);
	}

	/** Sanitizes the Src or Dst part of a line. */
	private static void sanitizeLineSrcOrDst(MutableMDLSection lineOrBranch,
			String srcOrDstParameter, String srcOrDstPortParameter,
			String srcOrDstBlockParameter, boolean resolveSpecialPorts) {
		String value = lineOrBranch.getParameter(srcOrDstParameter);
		if (value == null) {
			return;
		}

		Matcher matcher = BLOCK_PORT_PATTERN.matcher(value);
		if (!matcher.matches()) {
			return;
		}

		String blockSID = matcher.group(1);

		// In case of matlab block append the sub id.
		if (matcher.group(2) != null) {
			blockSID += matcher.group(2);
		}

		String portId = matcher.group(8);
		if (resolveSpecialPorts) {
			String portType = matcher.group(5);
			// In case of trigger/enable/ifaction the textual type is used.
			if (TYPE_Trigger.equals(portType) || TYPE_Enable.equals(portType)
					|| TYPE_Ifaction.equals(portType)) {
				portId = portType;
			}
		}

		lineOrBranch.setParameter(srcOrDstBlockParameter, blockSID);
		lineOrBranch.setParameter(srcOrDstPortParameter, portId);
		lineOrBranch.removeParameter(srcOrDstParameter);
	}

	/** Recursively sanitize state flow element. */
	private static void sanitizeStateflowSection(
			MutableMDLSection stateflowModel, MutableMDLSection section) {
		for (MutableMDLSection subSection : section.getSubSections()
				.getValues()) {
			String name = subSection.getName();
			if (SECTION_Children.equals(name)) {
				sanitizeStateflowSection(stateflowModel, subSection);
				section.removeSection(subSection);
			} else {
				sanitizeId(subSection);
				if (SECTION_state.equals(name)) {
					buildTreeNode(section, subSection);
					sanitizeStateflowSection(stateflowModel, subSection);
					flattenStateflowSection(stateflowModel, subSection);
				} else if (SECTION_chart.equals(name)) {
					buildMachineId(section, subSection);
					sanitizeStateflowSection(stateflowModel, subSection);
					flattenStateflowSection(stateflowModel, subSection);
				} else if (SECTION_data.equals(name)
						|| SECTION_transition.equals(name)
						|| SECTION_junction.equals(name)
						|| SECTION_target.equals(name)
						|| SECTION_event.equals(name)) {
					buildLinkNode(section, subSection);
					sanitizeStateflowSection(stateflowModel, subSection);
					flattenStateflowSection(stateflowModel, subSection);
				}
				// Only the preceding elements are flattened. Other
				// stateflow elements remain in their parent section.
			}
		}
	}

	/** Sanitizes the section's id by converting ssid to id. */
	private static void sanitizeId(MutableMDLSection section) {
		String ssid = section.getParameter(PARAM_SSID);
		if (ssid != null) {
			// prefix with chart id, as ids are only unique within charts in SLX
			MutableMDLSection parentChart = findParentChartSection(section);
			if (parentChart != null) {
				ssid = parentChart.getParameter(PARAM_id) + ":" + ssid;
			}

			section.setParameter(PARAM_id, ssid);
			section.removeParameter(PARAM_SSID);
		}
	}

	/** Returns the surrounding parent chart section (or null). */
	private static MutableMDLSection findParentChartSection(
			MutableMDLSection section) {
		MutableMDLSection parent = section.getParentSection();
		while (parent != null) {
			if (parent.getName().equals(SECTION_chart)) {
				return parent;
			}
			parent = parent.getParentSection();
		}
		return null;
	}

	/** Create treeNode parameter, which indicates the state's parent. */
	private static void buildTreeNode(MutableMDLSection section,
			MutableMDLSection subSection) {
		MutableMDLSection parent = section.getParentSection();
		String parentId = parent.getParameter(PARAM_id);
		if (parentId != null) {
			subSection.setParameter(PARAM_treeNode, "[" + parentId + "]");
		}
	}

	/** Create machineId parameter, which indicates the chart's parent. */
	private static void buildMachineId(MutableMDLSection section,
			MutableMDLSection subSection) {
		MutableMDLSection parent = section.getParentSection();
		String parentId = parent.getParameter(PARAM_id);
		if (parentId != null) {
			subSection.setParameter(PARAM_machine, parentId);
		}
	}

	/** Create linkNode parameter, which indicates the sf element's parent. */
	private static void buildLinkNode(MutableMDLSection section,
			MutableMDLSection subSection) {
		MutableMDLSection parent = section.getParentSection();
		String parentId = parent.getParameter(PARAM_id);
		if (parentId != null) {
			subSection.setParameter(PARAM_linkNode, "[" + parentId + "]");
		}
	}

	/** Moves a section from its parent to the stateflow root. */
	private static void flattenStateflowSection(
			MutableMDLSection stateflowModel, MutableMDLSection section) {
		MutableMDLSection parent = section.getParentSection();
		if (parent != null) {
			parent.removeSection(section);
		}
		stateflowModel.addSubSection(section);
	}
}
