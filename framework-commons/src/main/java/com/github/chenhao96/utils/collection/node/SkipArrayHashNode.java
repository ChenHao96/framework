/**
 * Copyright 2017-2019 ChenHao96
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.chenhao96.utils.collection.node;

public class SkipArrayHashNode<K, V> extends SkipHashNode<K, V> {

    private final int arraySize;
    private final int[] arrayCount;
    private final NodeItem[] arrayRoot;

    private static final int DEFAULT_ARRAY_SIZE = 10;

    public SkipArrayHashNode() {
        this(DEFAULT_ARRAY_SIZE);
    }

    @SuppressWarnings("unchecked")
    public SkipArrayHashNode(int arraySize) {
        if (arraySize < 1) throw new IllegalArgumentException("the arraySize parameter cannot be less than 1.");
        this.arraySize = arraySize;
        this.arrayCount = new int[arraySize];
        this.arrayRoot = new SkipHashNode.NodeItem[arraySize];
    }

    @Override
    public V get(Object key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        int hashCode = key.hashCode();
        NodeItem tmp = getNodeItemByHash(hashCode);
        if (tmp == null) return null;
        //TODO:bug
        ResultNode node = super.queryHashKey(tmp, hashCode);
        return node == null ? null : node.getCurrentNode() == null ? null : node.getCurrentNode().getValue();
    }

    @Override
    public V put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        int hashCode = key.hashCode();
        int index = getNodeIndex(hashCode);
        NodeItem tmp = getNodeItemByIndex(index);
        if (tmp == null) super.root = null;
        V result = super.putValue(key, value, hashCode);
        this.arrayRoot[index] = super.root;
        this.arrayCount[index]++;
        return result;
    }

    @Override
    public V remove(Object key) {
        int hashCode = key.hashCode();
        int index = getNodeIndex(hashCode);
        NodeItem tmp = getNodeItemByIndex(index);
        if (tmp != null && this.arrayCount[index] > 0) {
            V result = super.remove(super.queryHashKey(tmp, hashCode));
            if (result != null) {
                this.arrayCount[index]--;
                if (this.arrayCount[index] == 0) {
                    this.arrayRoot[index] = null;
                }
            }
            return result;
        }
        return null;
    }

    private int getNodeIndex(int hashCode) {
        return (this.arraySize + (hashCode % this.arraySize)) % this.arraySize;
    }

    private NodeItem getNodeItemByHash(int hashCode) {
        return getNodeItemByIndex(getNodeIndex(hashCode));
    }

    private NodeItem getNodeItemByIndex(int index) {
        if (index >= 0 && index < arraySize) {
            return this.arrayRoot[index];
        }
        return null;
    }

    @Override
    public void clear() {
        for (int i = 0; i < arrayRoot.length; i++) {
            arrayCount[i] = 0;
            arrayRoot[i] = null;
        }
        super.clear();
    }
}
