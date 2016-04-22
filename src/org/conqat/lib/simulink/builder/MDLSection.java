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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.ListMap;
import org.conqat.lib.commons.collections.PairList;
import org.conqat.lib.commons.collections.UnmodifiableList;
import org.conqat.lib.commons.collections.UnmodifiableSet;
import org.conqat.lib.commons.string.StringUtils;

/**
 * An MDL section has a name, maintains sub sections and a key-value map for
 * parameters. See <a href=
 * "http://www.mathworks.com/access/helpdesk/help/toolbox/simulink/index.html?/access/helpdesk/help/toolbox/simulink/slref/f22-7245.html&http://www.mathworks.com/access/helpdesk/help/toolbox/simulink/helptoc.html"
 * >Simulink Documentation</a> for details on the file format.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51726 $
 * @ConQAT.Rating GREEN Hash: 9EBA574101EE4BC3CABB81C0606E54E1
 */
/* package */class MDLSection extends MDLSectionBase {

	/** The parent section (may be null for the top-most section). */
	private MDLSection parentSection;

	/** Parameter map. */
	private final Map<String, String> parameters = new HashMap<>();

	/** Maps from section name to a list of sections. */
	private final ListMap<String, MDLSection> subSections = new ListMap<>();

	/** Create MDL section. */
	/* package */MDLSection(String name, int lineNumber) {
		super(name, lineNumber);
	}

	/**
	 * Create MDL section.
	 * 
	 * @param name
	 *            section name.
	 * @param sections
	 *            list of sub sections
	 * @param parameters
	 *            parameters of this section
	 * @param lineNumber
	 *            line number within the MDL file.
	 */
	/* package */MDLSection(String name, List<MDLSection> sections,
			PairList<String, String> parameters, int lineNumber) {
		this(name, lineNumber);

		for (MDLSection section : sections) {
			addSubSection(section);
		}

		for (int i = 0; i < parameters.size(); i++) {
			this.parameters
					.put(parameters.getFirst(i), parameters.getSecond(i));
		}
	}

	/**
	 * Adds a sub section and sets this section as parent section of the added
	 * section.
	 */
	private void addSubSection(MDLSection section) {
		section.parentSection = this;
		subSections.add(section.getName(), section);
	}

	/** @see #parentSection */
	public MDLSection getParentSection() {
		return parentSection;
	}

	/**
	 * Get the first sub section with a specified name or <code>null</code> if
	 * no section with the given name was found.
	 */
	public MDLSection getFirstSubSection(String name) {
		if (!hasSubSections(name)) {
			return null;
		}
		return subSections.getCollection(name).get(0);
	}

	/**
	 * Traverse section tree and return all leaves descending from this section.
	 */
	public List<MDLSection> getLeafSections() {
		ArrayList<MDLSection> result = new ArrayList<MDLSection>();
		for (MDLSection section : subSections.getValues()) {
			collectLeafSections(section, result);
		}
		return result;
	}

	/**
	 * Traverse section tree and return all leaves with a specified name
	 * descending from this section.
	 */
	public List<MDLSection> getLeafSections(String name) {
		ArrayList<MDLSection> result = new ArrayList<MDLSection>();

		for (MDLSection section : getLeafSections()) {
			if (section.getName().equals(name)) {
				result.add(section);
			}
		}

		return result;
	}

	/** Get parameter (or null if parameter does not exist). */
	public String getParameter(String name) {
		return parameters.get(name);
	}

	/**
	 * Gets parameter if present, otherwise returns default values.
	 */
	public String getParameter(String name, String defaultValue) {
		if (!parameters.containsKey(name)) {
			return defaultValue;
		}
		return parameters.get(name);
	}

	/**
	 * Get parameter map that includes all parameters of this section plus
	 * parameters of subsections. Parameters of subsections are separated by a
	 * dot: <code>&lt;section name&gt;.&lt;parameter name&gt;</code>.
	 */
	public Map<String, String> getParameterMapRecursively() {
		Map<String, String> map = new HashMap<String, String>();
		buildParameterMap(map, StringUtils.EMPTY_STRING);
		return map;
	}

	/** Get names of all parameters. */
	public UnmodifiableSet<String> getParameterNames() {
		return CollectionUtils.asUnmodifiable(parameters.keySet());
	}

	/**
	 * Get all sub sections. Returns an empty list if this section has no sub
	 * sections.
	 */
	public UnmodifiableList<MDLSection> getSubSections() {
		return CollectionUtils.asUnmodifiable(subSections.getValues());
	}

	/**
	 * Get all sub sections with a given name. Returns an empty list if this
	 * section has no sub sections with the specified name.
	 */
	public UnmodifiableList<MDLSection> getSubSections(String name) {
		if (!hasSubSections(name)) {
			return CollectionUtils.emptyList();
		}
		return CollectionUtils.asUnmodifiable(subSections.getCollection(name));
	}

	/** Checks if a parameter with given name is present. */
	public boolean hasParameter(String name) {
		return parameters.containsKey(name);
	}

	/** Checks if this section has sub sections with specified name. */
	public boolean hasSubSections(String name) {
		return subSections.containsCollection(name)
				&& !subSections.getCollection(name).isEmpty();
	}

	/**
	 * Build parameter map.
	 * 
	 * @param prefix
	 *            prefix that is added to the names of the parameters in the
	 *            map. This is used during recursion to also include the fully
	 *            qualified name of the section in the parameter names.
	 */
	private void buildParameterMap(Map<String, String> map, String prefix) {

		if (!StringUtils.isEmpty(prefix)) {
			prefix += ".";
		}

		for (String name : parameters.keySet()) {
			map.put(prefix + name, parameters.get(name));
		}

		for (MDLSection subSection : getSubSections()) {
			subSection.buildParameterMap(map, prefix + subSection.getName());
		}
	}

	/** Collect the leaves of a section. */
	private void collectLeafSections(MDLSection section,
			ArrayList<MDLSection> result) {
		if (!section.hasSubSections()) {
			result.add(section);
			return;
		}

		for (MDLSection child : section.getSubSections()) {
			collectLeafSections(child, result);
		}
	}

	/** Checks if this section has sub sections. */
	private boolean hasSubSections() {
		return subSections.getValueCount() > 0;
	}
}