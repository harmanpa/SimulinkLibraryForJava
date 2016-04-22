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
package org.conqat.lib.commons.tree;

import java.util.LinkedHashMap;
import java.util.Map;

import org.conqat.lib.commons.collections.CollectionUtils;
import org.conqat.lib.commons.collections.UnmodifiableCollection;
import org.conqat.lib.commons.string.StringUtils;

/**
 * A simple node class that can be used with {@link TreeUtils}. See
 * {@link TreeUtilsTest} for an application of this class that uses strings as
 * keys.
 * 
 * @param <K>
 *            key used to identify children, e.g. String.
 * 
 * @author deissenb
 * @author $Author: heinemann $
 * @version $Rev: 52206 $
 * @ConQAT.Rating GREEN Hash: FCFB015F650CB077EF416DD92E492B2F
 */
public class SimpleTreeNode<K> {

	/** The children of this node as a mapping from key to child. */
	private final Map<K, SimpleTreeNode<K>> children = new LinkedHashMap<>();

	/** Key of this node. */
	private final K key;

	/** Create new node with specified key. */
	public SimpleTreeNode(K key) {
		this.key = key;
	}

	/**
	 * Returns the child with specified key. This returns <code>null</code> if
	 * child with provided key does not exist.
	 */
	public SimpleTreeNode<K> getChild(K key) {
		return children.get(key);
	}

	/** Add child. This overwrites existing child with same key. */
	public void addChild(SimpleTreeNode<K> child) {
		children.put(child.getKey(), child);
	}

	/** Returns the key of this node. */
	public K getKey() {
		return key;
	}

	/** Returns the children of this node. */
	public UnmodifiableCollection<SimpleTreeNode<K>> getChildren() {
		return CollectionUtils.asUnmodifiable(children.values());
	}

	/**
	 * This returns a nicely indented representation of the whole tree below
	 * this node.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(key + StringUtils.CR);
		for (SimpleTreeNode<K> child : children.values()) {
			result.append(StringUtils.prefixLines(child.toString(),
					StringUtils.TWO_SPACES, true));
			result.append(StringUtils.CR);
		}
		return result.toString();
	}
}