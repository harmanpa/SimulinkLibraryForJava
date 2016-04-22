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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.conqat.lib.commons.collections.ListMap;
import org.conqat.lib.commons.collections.PairList;

/**
 * Mutable variant of {@link MDLSection} for consecutive model construction
 * during file parsing.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51731 $
 * @ConQAT.Rating GREEN Hash: CB61BD7944BC0A4472611660F8F43AF1
 */
/* package */class MutableMDLSection extends MDLSectionBase {

	/** The parent section. */
	private MutableMDLSection parentSection;

	/** Parameter map. */
	private final Map<String, String> parameters = new HashMap<>();

	/** Maps from section name to a list of sections. */
	private final ListMap<String, MutableMDLSection> subSections = new ListMap<>();

	/** Currently modified parameter. Used for parsing. May be null. */
	private String currentParameter = null;

	/** Constructor. */
	public MutableMDLSection(String name, int lineNumber) {
		super(name, lineNumber);
	}

	/** @see #currentParameter */
	public String getCurrentParameter() {
		return currentParameter;
	}

	/** @see #currentParameter */
	public void setCurrentParameter(String currentParameter) {
		this.currentParameter = currentParameter;
	}

	/** Resets the currently processed parameter to null. */
	public void resetCurrentParameter() {
		this.currentParameter = null;
	}

	/** Returns parameter value. */
	public String getParameter(String name) {
		return this.parameters.get(name);
	}

	/** Returns parameters. */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/** Sets the parameter. Old values get replaced. */
	public void setParameter(String name, String value) {
		this.parameters.put(name, value);
	}

	/** Appends a value to the specified parameter. */
	public void appendParameter(String name, String value) {
		if (!this.parameters.containsKey(name)) {
			this.parameters.put(name, value);
		} else {
			String currentValue = this.parameters.get(name);
			this.parameters.put(name, currentValue + value);
		}
	}

	/** Removes the given parameter. */
	public void removeParameter(String name) {
		this.parameters.remove(name);
	}

	/**
	 * Get the first sub section with a specified name or <code>null</code> if
	 * no section with the given name was found.
	 */
	public MutableMDLSection getFirstSubSection(String name) {
		List<MutableMDLSection> collection = this.subSections
				.getCollection(name);
		if (collection != null) {
			return collection.get(0);
		}
		return null;
	}

	/**
	 * Get all sub sections. Returns an empty list if this section has no sub
	 * sections.
	 */
	public ListMap<String, MutableMDLSection> getSubSections() {
		return subSections;
	}

	/**
	 * Get all sub sections with a given name. Returns an empty list if this
	 * section has no sub sections with the specified name.
	 */
	public List<MutableMDLSection> getSubSections(String name) {
		return this.subSections.getCollection(name);
	}

	/** Add sub section. */
	public void addSubSection(MutableMDLSection subSection) {
		subSection.parentSection = this;
		this.subSections.add(subSection.getName(), subSection);
	}

	/** @see #parentSection */
	public MutableMDLSection getParentSection() {
		return parentSection;
	}

	/** Remove sub section. */
	public void removeSection(MutableMDLSection subSection) {
		subSections.remove(subSection.getName(), subSection);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "MDLSectionContainer [name=" + getName() + ", lineNumber="
				+ getLineNumber() + ", parameters=" + parameters + "]";
	}

	/** Transforms this mutable section into an immutable one. */
	public MDLSection asImmutable() {
		List<MDLSection> subSections = new ArrayList<MDLSection>();
		for (MutableMDLSection containerSubSection : this.subSections
				.getValues()) {
			subSections.add(containerSubSection.asImmutable());
		}

		return new MDLSection(getName(), subSections,
				new PairList<String, String>(parameters), getLineNumber());
	}
}
