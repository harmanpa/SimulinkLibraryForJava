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
package org.conqat.lib.simulink.model;

import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_BlockType;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_Points;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SID;
import static org.conqat.lib.simulink.model.SimulinkConstants.PARAM_SourceType;
import static org.conqat.lib.simulink.model.SimulinkConstants.TYPE_Reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.conqat.lib.commons.assertion.CCSMAssert;
import org.conqat.lib.commons.assertion.CCSMPre;
import org.conqat.lib.commons.assertion.PreconditionException;
import org.conqat.lib.commons.clone.DeepCloneException;
import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.IdentityHashSet;
import org.conqat.lib.commons.collections.UnmodifiableCollection;
import org.conqat.lib.commons.collections.UnmodifiableSet;
import org.conqat.lib.commons.string.StringUtils;
import org.conqat.lib.commons.test.ADeepCloneTestExclude;
import org.conqat.lib.simulink.builder.SimulinkModelBuildingException;
import org.conqat.lib.simulink.model.datahandler.BlockLayoutData;
import org.conqat.lib.simulink.model.datahandler.LabelLayoutData;
import org.conqat.lib.simulink.util.SimulinkUtils;

/**
 * A Simulink block has a type and maintains a parameter map, a list of sub
 * blocks, a list of annotations and in/out-ports.
 * 
 * @author $Author: hummelb $
 * @version $Rev: 52104 $
 * @ConQAT.Rating GREEN Hash: 2F514EC6E8B27C8E2298C7A86E364757
 */
public class SimulinkBlock extends SimulinkElementBase {

	/** The subBlocks of this block indexed by name. */
	private final Map<String, SimulinkBlock> subBlocks = new HashMap<>();

	/** Inports of this block indexed by port index. */
	private final Map<String, SimulinkInPort> inPorts = new HashMap<>();

	/** Outports of this block indexed by port index. */
	private final Map<String, SimulinkOutPort> outPorts = new HashMap<>();

	/** Annotations of this block. */
	private final Set<SimulinkAnnotation> annotations = new IdentityHashSet<>();

	/**
	 * The lines directly contained in this block (e.g. connecting child
	 * blocks).
	 */
	private final Set<SimulinkLine> containedLines = new IdentityHashSet<>();

	/** Create new Simulink block. */
	public SimulinkBlock() {
		// empty but required to keep default constructor
	}

	/**
	 * Copy constructor. This is used from the {@link SimulinkModel} during
	 * cloning.
	 */
	protected SimulinkBlock(SimulinkBlock origBlock) throws DeepCloneException {
		super(origBlock);

		for (SimulinkInPort inPort : origBlock.inPorts.values()) {
			new SimulinkInPort(this, inPort.getIndex());
		}

		for (SimulinkOutPort outPort : origBlock.outPorts.values()) {
			new SimulinkOutPort(this, outPort.getIndex());
		}

		for (SimulinkAnnotation annotation : origBlock.annotations) {
			addAnnotation(annotation.deepClone());
		}

		// Recursively deep clone sub blocks
		for (SimulinkBlock subBlock : origBlock.subBlocks.values()) {
			addSubBlock(subBlock.deepClone());
		}

		for (SimulinkLine line : origBlock.containedLines) {
			cloneLine(line, origBlock);
		}
	}

	/**
	 * Clone a single line.
	 * 
	 * @param origLine
	 *            the line to clone.
	 * @param origBlock
	 *            the original block containing the line.
	 */
	private void cloneLine(SimulinkLine origLine, SimulinkBlock origBlock) {
		SimulinkOutPort cloneSrcPort = findCloneSrcPort(origBlock,
				origLine.getSrcPort());
		SimulinkInPort cloneDestPort = findCloneDestPort(origBlock,
				origLine.getDstPort());
		SimulinkLine line = new SimulinkLine(cloneSrcPort, cloneDestPort, this);
		SimulinkUtils.copyParameters(origLine, line);
	}

	/**
	 * Locates and returns the matching source port during cloning (copy
	 * construction) based on original block and port.
	 */
	@SuppressWarnings("null")
	private SimulinkOutPort findCloneSrcPort(SimulinkBlock origBlock,
			SimulinkOutPort origSourcePort) throws AssertionError {
		SimulinkOutPort cloneSourcePort = null;
		if (origSourcePort != null) {
			SimulinkBlock cloneSourceBlock = resolveRelativeBlock(
					origSourcePort.getBlock(), origBlock);
			CCSMAssert.isFalse(cloneSourceBlock == null,
					"Cloning Problem: Src block "
							+ origSourcePort.getBlock().getName()
							+ " not found.");
			cloneSourcePort = cloneSourceBlock.getOutPort(origSourcePort
					.getIndex());
			CCSMAssert.isFalse(
					cloneSourcePort == null,
					"Cloning Problem: Src port with index "
							+ origSourcePort.getIndex() + " not found.");
		}
		return cloneSourcePort;
	}

	/**
	 * Locates and returns the matching destination port during cloning (copy
	 * construction) based on original block and port.
	 */
	@SuppressWarnings("null")
	private SimulinkInPort findCloneDestPort(SimulinkBlock origBlock,
			SimulinkInPort origDestPort) throws AssertionError {
		SimulinkInPort cloneDestPort = null;
		if (origDestPort != null) {
			SimulinkBlock cloneDestBlock = resolveRelativeBlock(
					origDestPort.getBlock(), origBlock);
			CCSMAssert
					.isFalse(cloneDestBlock == null,
							"Cloning Problem: Dst block "
									+ origDestPort.getBlock().getName()
									+ " not found.");
			cloneDestPort = cloneDestBlock.getInPort(origDestPort.getIndex());
			CCSMAssert.isFalse(
					cloneDestPort == null,
					"Cloning Problem: Dst port with index "
							+ origDestPort.getIndex() + " not found.");
		}
		return cloneDestPort;
	}

	/** Add an annotation. */
	public void addAnnotation(SimulinkAnnotation annotation) {
		annotations.add(annotation);
		annotation.setParent(this);
	}

	/** Adds a sub block. */
	public void addSubBlock(SimulinkBlock subBlock) {
		CCSMPre.isTrue(subBlock.getParent() == null,
				"May not add block which already has a parent!");
		subBlock.setParent(this);

		CCSMPre.isFalse(subBlocks.containsKey(subBlock.getName()),
				"Block already has a sub block called: " + subBlock.getName());
		subBlocks.put(subBlock.getName(), subBlock);
	}

	/** Get annotations. */
	public UnmodifiableSet<SimulinkAnnotation> getAnnotations() {
		return CollectionUtils.asUnmodifiable(annotations);
	}

	/** Get all incoming lines of this block. */
	public List<SimulinkLine> getInLines() {
		List<SimulinkLine> inLines = new ArrayList<SimulinkLine>();

		for (SimulinkInPort inPort : getInPorts()) {
			if (inPort.getLine() != null) {
				inLines.add(inPort.getLine());
			}
		}
		return inLines;
	}

	/**
	 * Get inport by index or <code>null</code> if no inport with this index was
	 * found.
	 */
	public SimulinkInPort getInPort(String portIndex) {
		return inPorts.get(portIndex);
	}

	/** Returns the inports this block. */
	public UnmodifiableCollection<SimulinkInPort> getInPorts() {
		return CollectionUtils.asUnmodifiable(inPorts.values());
	}

	/**
	 * Get all outgoing lines of this block.
	 */
	public Collection<SimulinkLine> getOutLines() {
		List<SimulinkLine> outLines = new ArrayList<SimulinkLine>();

		for (SimulinkOutPort outPort : outPorts.values()) {
			outLines.addAll(outPort.getLines());
		}
		return outLines;
	}

	/**
	 * Get outport by index or <code>null</code> if no outport with this index
	 * was found.
	 */
	public SimulinkOutPort getOutPort(String portIndex) {
		return outPorts.get(portIndex);
	}

	/** Returns the outport of this block. */
	public UnmodifiableCollection<SimulinkOutPort> getOutPorts() {
		return CollectionUtils.asUnmodifiable(outPorts.values());
	}

	/** Returns the lines that are directly contained in this block. */
	public UnmodifiableCollection<SimulinkLine> getContainedLines() {
		return CollectionUtils.asUnmodifiable(containedLines);
	}

	/**
	 * Returns the lines that are contained in this block or one of its
	 * descendants.
	 */
	public Collection<SimulinkLine> getContainedLinesRecursively() {
		List<SimulinkLine> lines = new ArrayList<>(containedLines);
		for (SimulinkBlock subBlock : subBlocks.values()) {
			lines.addAll(subBlock.getContainedLinesRecursively());
		}
		return lines;
	}

	/**
	 * If this block is of type 'Reference' this returns
	 * <code>Reference.&lt;source type of the reference&gt;</code>. Otherwise
	 * this just returns the type of the block. Returns null if no type is set
	 * for a block.
	 */
	public String getResolvedType() {
		String type = getType();
		if (TYPE_Reference.equals(type)) {
			// source block can contain library path, such as
			// "simulink/Signal\nRouting/Manual Switch"
			String sourceBlock = getParameter(SimulinkConstants.PARAM_SourceBlock);
			if (sourceBlock != null && sourceBlock.startsWith("simulink/")) {
				return sourceBlock.replaceAll("^.*/|\\s",
						StringUtils.EMPTY_STRING);
			}

			String sourceType = getParameter(PARAM_SourceType);
			if (!StringUtils.isEmpty(sourceType)) {
				return TYPE_Reference + "." + sourceType;
			}
		}
		return type;
	}

	/**
	 * Returns information on the referenced block. Returns null if this block
	 * does not reference another block.
	 */
	@ADeepCloneTestExclude
	public ReferencedBlockInfo getReferencedBlockInfo() {
		if (SimulinkConstants.TYPE_ModelReference.equals(getType())) {
			String modelName = getParameter(SimulinkConstants.PARAM_ModelNameDialog);
			if (modelName == null) {
				modelName = getParameter(SimulinkConstants.PARAM_ModelName);
			}
			if (modelName != null) {
				String blockName = StringUtils.removeLastPart(modelName, '.');
				return new ReferencedBlockInfo(modelName, blockName, getModel()
						.getModelDataHandler());
			}
		}

		if (SimulinkConstants.TYPE_Reference.equals(getType())) {
			String sourceBlock = getParameter(SimulinkConstants.PARAM_SourceBlock);
			if (sourceBlock != null) {
				String fileName = sourceBlock
						.split(SimulinkUtils.SIMULINK_ID_SEPARATOR)[0];
				return new ReferencedBlockInfo(fileName, sourceBlock,
						getModel().getModelDataHandler());
			}
		}

		return null;
	}

	/**
	 * Get named sub block or <code>null</code> if no sub block with the given
	 * name is present.
	 */
	public SimulinkBlock getSubBlock(String name) {
		return subBlocks.get(name);
	}

	/**
	 * Get sub block by SID or <code>null</code> if no sub block with the given
	 * SID is present.
	 */
	public SimulinkBlock getSubBlockBySID(String sid) {
		CCSMPre.isNotNull(sid);
		for (Entry<String, SimulinkBlock> entry : subBlocks.entrySet()) {
			SimulinkBlock subBlock = entry.getValue();
			String subBlockSID = subBlock.getParameter(PARAM_SID);
			if (sid.equals(subBlockSID)) {
				return subBlock;
			}
		}
		return null;
	}

	/** Returns the sub blocks of this block. */
	public UnmodifiableCollection<SimulinkBlock> getSubBlocks() {
		return CollectionUtils.asUnmodifiable(subBlocks.values());
	}

	/** Returns the type (or null if no type was declared for the block). */
	public String getType() {
		// We have to access the super class here as we do not want any defaults
		// (infinite recursion)
		return getDeclaredParameter(PARAM_BlockType);
	}

	/** Returns whether this block has subBlocks. */
	public boolean hasSubBlocks() {
		return !subBlocks.isEmpty();
	}

	/**
	 * Unlinks this object from the simulink tree. Also removes all sub-blocks,
	 * ports and annotations
	 */
	@Override
	public void remove() {

		// have to create new ArrayList during removal to avoid
		// CuncurrentModificationException as the call to remove eventually
		// removes the sub block from the subBlocks map. Same for the other
		// calls below
		for (SimulinkBlock subBlock : new ArrayList<SimulinkBlock>(
				subBlocks.values())) {
			subBlock.remove();
		}

		for (SimulinkOutPort outPort : new ArrayList<SimulinkOutPort>(
				getOutPorts())) {
			outPort.remove();
		}

		for (SimulinkInPort inPort : new ArrayList<SimulinkInPort>(getInPorts())) {
			inPort.remove();
		}

		for (SimulinkAnnotation annotation : new ArrayList<SimulinkAnnotation>(
				annotations)) {
			annotation.remove();
		}

		super.remove();
	}

	/**
	 * Unlinks this object from the simulink tree. Unlike {@link #remove()} does
	 * not unlink sub-blocks, ports and annotations.
	 * 
	 * @see SimulinkElementBase#remove
	 */
	public void detach() {
		super.remove();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getId() + " [" + getType() + ", " + inPorts.size() + ":"
				+ outPorts.size() + "]";
	}

	/**
	 * Creates a deep clone of this block. Please note that is possible to clone
	 * a single block but the resulting block will behave not properly as it
	 * does not belong to {@link SimulinkModel}. Therefore it is strongly
	 * recommended to deep clone only whole models.
	 */
	@Override
	public SimulinkBlock deepClone() throws DeepCloneException {
		return new SimulinkBlock(this);
	}

	/**
	 * Add a inport to this block.
	 * 
	 * @throws PreconditionException
	 *             if the port does not belong to this block or a port with the
	 *             same index was defined before.
	 */
	/* package */void addInPort(SimulinkInPort inPort)
			throws PreconditionException {
		CCSMPre.isTrue(inPort.getBlock() == this,
				"Port does not belong to block.");
		CCSMPre.isFalse(inPorts.containsKey(inPort.getIndex()),
				"Port with index " + inPort.getIndex() + " already defined.");

		inPorts.put(inPort.getIndex(), inPort);
	}

	/**
	 * Add a outport to this block.
	 * 
	 * @throws PreconditionException
	 *             if the port does not belong to this block or a port with the
	 *             same index was defined before.
	 */
	/* package */void addOutPort(SimulinkOutPort outPort)
			throws PreconditionException {
		CCSMPre.isTrue(outPort.getBlock() == this,
				"Port does not belong to block.");
		CCSMPre.isFalse(outPorts.containsKey(outPort.getIndex()),
				"Port with index " + outPort.getIndex() + " already defined.");

		outPorts.put(outPort.getIndex(), outPort);
	}

	/**
	 * Get block default parameter.
	 */
	@Override
	/* package */String getDefaultParameter(String name) {
		return getModel().getTypeBlockDefaultParameter(getType(), name);
	}

	/**
	 * Get block default parameter names.
	 */
	@Override
	/* package */Set<String> getDefaultParameterNames() {
		return getModel().getBlockDefaultParameterNames(getType());
	}

	/** {@inheritDoc} */
	@Override
	protected void removeElement(SimulinkElementBase element) {
		if (element instanceof SimulinkAnnotation) {
			annotations.remove(element);
		} else if (element instanceof SimulinkBlock) {
			subBlocks.remove(element.getName());
		} else {
			super.removeElement(element);
		}
	}

	/** Remove in port. Port must belong to this block. */
	/* package */void removeInPort(SimulinkInPort inPort) {
		CCSMPre.isTrue(inPorts.containsValue(inPort),
				"Port does not belong to this block!");
		inPorts.remove(inPort.getIndex());
	}

	/** Remove out port. Port must belong to this block. */
	/* package */void removeOutPort(SimulinkOutPort outPort) {
		CCSMPre.isTrue(outPorts.containsValue(outPort),
				"Port does not belong to this block!");
		outPorts.remove(outPort.getIndex());
	}

	/**
	 * Removes lines from this block. All of the lines have to be connected to
	 * this block.
	 * 
	 * @param lines
	 *            The lines that should be removed
	 */
	private void removeLines(Collection<SimulinkLine> lines) {
		for (SimulinkLine line : lines) {
			CCSMPre.isTrue(getInLines().contains(line)
					|| getOutLines().contains(line),
					"Only lines that are connected to this block can be removed by this block");
			line.remove();
		}
	}

	/**
	 * Removes all incoming lines of this block
	 */
	public void removeInLines() {
		removeLines(getInLines());
	}

	/**
	 * Removes all outgoing lines of this block
	 */
	private void removeOutLines() {
		removeLines(getOutLines());
	}

	/**
	 * Returns the block that is in the same relation to this block as is
	 * <code>block</code> to <code>root</code>. This may only be called if
	 * <code>block</code> is a descendant of <code>root</code> and such a
	 * relative block actually exists. Otherwise assertion exceptions are
	 * thrown.
	 */
	@SuppressWarnings("null")
	private SimulinkBlock resolveRelativeBlock(SimulinkBlock block,
			SimulinkBlock root) {

		// if we reach null here during recursive calls, the original input
		// block was not a descendant
		CCSMAssert
				.isNotNull(block, "Block must be a descendant of root block!");

		if (block == root) {
			return this;
		}

		SimulinkBlock resultParent = resolveRelativeBlock(block.getParent(),
				root);
		CCSMAssert
				.isFalse(resultParent == null, "Parent block does not exist.");

		return resultParent.getSubBlock(block.getName());
	}

	/**
	 * Replaces this block by another if the compatibility check succeeds.
	 * 
	 * @param replacement
	 *            The block to be replaced
	 * @param parameters
	 *            the names of parameters to copy to the replacement.
	 * 
	 * @see SimulinkUtils#checkCompatibility(SimulinkBlock, SimulinkBlock)
	 * 
	 */
	public void replace(SimulinkBlock replacement, String... parameters)
			throws SimulinkModelBuildingException {
		if (!SimulinkUtils.checkCompatibility(this, replacement)) {
			throw new SimulinkModelBuildingException(
					"Blocks are not compatible!");
		}

		replacement.removeInLines();
		redirectInPorts(replacement);
		replacement.removeOutLines();
		redirectOutPorts(replacement);

		CCSMAssert.isTrue(getInLines().isEmpty(),
				"In-lines have not been properly replaced");
		CCSMAssert.isTrue(getOutLines().isEmpty(),
				"Out-lines have not been properly replaced");

		// Save the parent block now, as it is not available after detaching
		SimulinkBlock parent = getParent();

		// Detach this block from its parent block. We have to do this so we can
		// safely add the replacement
		detach();

		replacement.detach();

		parent.addSubBlock(replacement);

		// Copy all parameters from this block to the replacement block
		for (String name : parameters) {
			String value = getParameter(name);
			if (value != null) {
				replacement.setParameter(name, value);
			}
		}

		// Now we can completely remove this block
		remove();
	}

	/**
	 * Redirect the in-ports that of this block to {@code toBlock}
	 * 
	 * @see #redirectInPort
	 * @param toBlock
	 *            The block where the lines are redirected to
	 */
	private void redirectInPorts(SimulinkBlock toBlock)
			throws SimulinkModelBuildingException {
		// Redirect the incoming lines of this block to the replacement block
		for (SimulinkInPort port : getInPorts()) {
			redirectInPort(port, toBlock);
		}
	}

	/**
	 * Redirect the out-ports of this block to {@code toBlock}
	 * 
	 * @see #redirectOutPort
	 * @param toBlock
	 *            The block where the lines are redirected to
	 */
	private void redirectOutPorts(SimulinkBlock toBlock)
			throws SimulinkModelBuildingException {
		// Redirect the incoming lines of this block to the replacement block
		for (SimulinkOutPort port : getOutPorts()) {
			redirectOutPort(port, toBlock);
		}
	}

	/**
	 * Connect the out-port that is connected with {@code oldInPort} with the
	 * in-port of {@code target} that has the same index as {@code oldInPort}
	 * and remove the old lines.
	 * 
	 * @param oldInPort
	 *            The in-port that is to be redirected
	 * @param target
	 *            The target block that will be connected
	 */
	private void redirectInPort(SimulinkInPort oldInPort, SimulinkBlock target)
			throws SimulinkModelBuildingException {

		SimulinkLine line = oldInPort.getLine();
		if (line == null) {
			return;
		}

		// The new in-port has the same index as old one
		SimulinkInPort newInPort = target.getInPort(oldInPort.getIndex());

		if (newInPort == null) {
			throw new SimulinkModelBuildingException(
					"Port could not be redirected.");
		}

		// Copy points parameter so that line will be displayed correctly by an
		// editor
		SimulinkUtils.replaceLine(line, line.getSrcPort(), newInPort,
				PARAM_Points);
	}

	/**
	 * Connect the in-ports that are connected with {@code oldOutPort} with the
	 * out-port of {@code target} that has the same index as {@code oldOutPort}.
	 * 
	 * @param oldOutPort
	 *            The out-port that is to be redirected
	 * @param target
	 *            The target block that will be connected
	 */
	private void redirectOutPort(SimulinkOutPort oldOutPort,
			SimulinkBlock target) throws SimulinkModelBuildingException {

		// The new out-port has the same index as the old one
		SimulinkOutPort newOutPort = target.getOutPort(oldOutPort.getIndex());
		if (newOutPort == null) {
			throw new SimulinkModelBuildingException(
					"Port could not be redirected.");
		}
		// Avoid concurrent modification exception by copying
		Set<SimulinkLine> lines = new HashSet<SimulinkLine>(
				oldOutPort.getLines());
		for (SimulinkLine line : lines) {
			// Copy points parameter so that line will be displayed correctly by
			// an editor
			SimulinkUtils.replaceLine(line, newOutPort, line.getDstPort(),
					PARAM_Points);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void setParent(SimulinkElementBase parent) {
		CCSMAssert.isTrue(parent instanceof SimulinkBlock,
				"Blocks may only be attached to other blocks!");
		super.setParent(parent);
	}

	/** {@inheritDoc} */
	@Override
	public SimulinkBlock getParent() {
		return (SimulinkBlock) super.getParent();
	}

	/**
	 * Adds a contained line to this block. This is only called from the
	 * {@link SimulinkLine}.
	 */
	/* package */void addLine(SimulinkLine line) {
		CCSMPre.isFalse(containedLines.contains(line),
				"Line is already contained in this block.");
		CCSMPre.isTrue(line.getContainer() == this,
				"Line's container does not match.");
		containedLines.add(line);
	}

	/**
	 * Remove contained line. This is only called from the {@link SimulinkLine}.
	 */
	/* package */void removeLine(SimulinkLine line)
			throws IllegalArgumentException {
		CCSMPre.isTrue(containedLines.contains(line),
				"Line is not contained in this block.");
		containedLines.remove(line);
	}

	/**
	 * Returns the layout data for this block. This data is parsed from the
	 * model with each call, so repeated access should be avoided by storing the
	 * result in a local variable.
	 */
	@ADeepCloneTestExclude
	public BlockLayoutData obtainLayoutData() {
		return getModel().getModelDataHandler().obtainBlockLayoutData(this);
	}

	/**
	 * Returns the layout data for the label of this block. This data is parsed
	 * from the model with each call, so repeated access should be avoided by
	 * storing the result in a local variable.
	 */
	@ADeepCloneTestExclude
	public LabelLayoutData obtainLabelData() {
		return getModel().getModelDataHandler().obtainBlockLabelData(this);
	}
}