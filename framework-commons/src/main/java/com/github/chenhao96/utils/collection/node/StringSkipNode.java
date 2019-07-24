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

import java.util.Map;
import java.util.Random;

public class StringSkipNode<V> implements Node<String, V> {

    protected int size;
    protected NodeItem root;

    private final int maxLevel;
    private final int[] levelArray;
    private final Random random = new Random();
    private static final int DEFAULT_MAX_LEVEL = 16;

    public StringSkipNode() {
        this(DEFAULT_MAX_LEVEL);
    }

    public StringSkipNode(int maxLevel) {
        if (maxLevel < 1) {
            throw new IllegalArgumentException("maxLevel param must be greater than 1.");
        }
        this.maxLevel = maxLevel;
        this.levelArray = new int[maxLevel];
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public void clear() {
        this.root = null;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public V get(Object key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        ResultNode node = queryHashKey(key.hashCode());
        return node == null ? null : node.currentNode == null ? null : node.currentNode.value;
    }

    public int[] getLevelArray() {
        int[] result = new int[levelArray.length];
        System.arraycopy(levelArray, 0, result, 0, result.length);
        return result;
    }

    @Override
    public V put(String key, V value) {

        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        if (this.size >= Integer.MAX_VALUE)
            throw new IllegalArgumentException("container element reaches the upper limit.");

        int keyHash = key.hashCode();
        int levelCode = randomLevel();
        this.levelArray[levelCode]++;
        if (initRoot(key, value, keyHash, levelCode)) return null;

        this.size++;
        NodeItem current = new NodeItem();
        current.level = levelCode;
        current.index = keyHash;
        current.value = value;
        current.key = key;
        return putLevelNode(current);
    }

    @Override
    public V remove(Object key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        ResultNode node = queryHashKey(key.hashCode());
        if (node != null) {
            this.size--;
            //TODO:


            return node.currentNode.value;
        }
        return null;
    }

    private int randomLevel() {
        int result = random.nextInt(maxLevel);
        int size = random.nextInt(maxLevel);
        for (int i = 0; i < size; i++) {
            result += random.nextInt(maxLevel);
        }
        return result % maxLevel;
    }

    private boolean initRoot(String key, V value, int keyHash, int levelCode) {
        if (this.root == null) {
            this.root = new NodeItem();
            this.root.level = levelCode;
            this.root.index = keyHash;
            this.root.value = value;
            this.root.key = key;
            this.size = 1;
            return true;
        }
        return false;
    }

    private NodeItem findLessCurrent(int hash, NodeItem current) {
        while (true) {
            if (current.index > hash) return null;
            if (current.index == hash) return current;
            if (current.index < hash) {
                if (current.dataNext == null || current.dataNext.index > hash) return current;
                current = current.dataNext;
            }
        }
    }

    //TODO:
    private V putLevelNode(NodeItem value) {
        V result = null;
        NodeItem current = this.root, root = this.root, previousLevel = null;
        while (current != null) {
            NodeItem tmp = findLessCurrent(value.index, current);
            if (tmp == null) {
                if (value.level == current.level) {
                    value.dataNext = current;
                    root = value;
                } else {
                    value.levelNext = current;
                    if (previousLevel != null) {
                        previousLevel.levelNext = value;
                    } else {
                        root = value;
                    }
                }
                break;
            } else {
                if (tmp.index == value.index) {
                    result = tmp.value;
                    tmp.value = value.value;
                    break;
                } else if (tmp.index < value.index) {
                    if (tmp.level == value.level) {
                        value.dataNext = tmp.dataNext;
                        tmp.dataNext = value;
                        break;
                    }
                    if (tmp.levelNext != null) {
                        current = tmp.levelNext;
                        previousLevel = tmp;
                    } else {
                        tmp.levelNext = value;
                        break;
                    }
                }
            }
        }
        this.root = root;
        return result;
    }

    private ResultNode queryHashKey(int hashCode) {
        NodeItem previousLevel = null, previous = null, current = this.root;
        while (true) {
            if (current == null) return null;
            if (hashCode < current.index) return null;
            if (hashCode == current.index) return new ResultNode(previousLevel, previous, current);
            NodeItem nextData = current.dataNext;
            if (nextData == null || hashCode < nextData.index) {
                previous = null;
                previousLevel = current;
                current = current.levelNext;
            } else if (hashCode > nextData.index) {
                previous = current;
                current = nextData;
            }
        }
    }

    private class ResultNode {
        private NodeItem previousLevel;
        private NodeItem previousNode;
        private NodeItem currentNode;

        public ResultNode(NodeItem previousLevel, NodeItem previousNode, NodeItem currentNode) {
            this.previousLevel = previousLevel;
            this.previousNode = previousNode;
            this.currentNode = currentNode;
        }
    }

    protected class NodeItem implements Map.Entry<String, V> {
        private V value;
        private int index;
        private int level;
        private String key;
        private NodeItem dataNext;
        private NodeItem levelNext;

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V result = this.value;
            this.value = value;
            return result;
        }
    }
}
