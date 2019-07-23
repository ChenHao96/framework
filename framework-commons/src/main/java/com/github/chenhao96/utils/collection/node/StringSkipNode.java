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

import java.util.Random;

public class StringSkipNode<V> implements Node<String, V> {

    protected int size;
    protected NodeItem root;

    private final int[] levelArray;//TODO:test
    private final int maxLevel;
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
        return this.root == null;
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
        ResultNode node = queryHashValue(key.hashCode());
        return node == null ? null : node.currentNode == null ? null : node.currentNode.value;
    }

    //TODO:test
    public int[] getLevelArray() {
        return levelArray;
    }

    @Override
    public V put(String key, V value) {

        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        if (this.size >= Integer.MAX_VALUE)
            throw new IllegalArgumentException("container element reaches the upper limit.");

        int hashCode = key.hashCode();
        int levelCode = randomLevel();
        this.levelArray[levelCode]++;
        if (initRoot(value, hashCode, levelCode)) return null;

        V result = null;
        ResultNode queryNode = queryHashValue(key.hashCode());
        if (queryNode != null) {
            result = queryNode.currentNode.value;
            queryNode.currentNode.value = value;
        } else {
            this.size++;
            NodeItem current = new NodeItem();
            current.level = levelCode;
            current.index = hashCode;
            current.value = value;
            this.root = putLevelNode(current, this.root);
        }
        return result;
    }

    @Override
    public V remove(Object key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        ResultNode node = queryHashValue(key.hashCode());
        if (node != null) {
            this.size--;
            if (node.previousNode == null) {
                if (node.previousLevel == null) {
                    this.root = node.currentNode.dataNext;
                } else {
                    node.previousLevel.levelNext = node.currentNode.dataNext;
                }
            } else {
                node.previousNode.dataNext = node.currentNode.dataNext;
            }
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

    private boolean initRoot(V value, int hashCode, int levelCode) {
        if (this.root == null) {
            this.root = new NodeItem();
            this.root.level = levelCode;
            this.root.index = hashCode;
            this.root.value = value;
            this.size = 1;
            return true;
        }
        return false;
    }

    private NodeItem putLevelNode(NodeItem current, NodeItem node) {
        if (node == null) return current;
        if (current == null) return node;
        if (current.index < node.index) {
            if (current.level > node.level) {
                node.levelNext = putLevelNode(current, node.levelNext);
            } else {
                if (current.level < node.level) {
                    current.levelNext = node;
                } else {
                    current.dataNext = node;
                }
                return current;
            }
        } else if (current.index > node.index) {
            node.dataNext = putLevelNode(current, node.dataNext);
        } else {
            node.value = current.value;
        }
        return node;
    }

    private ResultNode queryHashValue(int hashCode) {
        NodeItem previousLevel = null, previous = null, current = this.root;
        while (true) {
            if (current == null) return null;
            if (current.index > hashCode) {
                previousLevel = current;//@1
                current = current.levelNext;//@2
            } else if (current.index < hashCode) {
                previous = current;//@1
                current = current.dataNext;//@2
            } else {
                return new ResultNode(previousLevel, previous, current);
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

    private class NodeItem {
        private V value;
        private int index;
        private int level;
        private NodeItem dataNext;
        private NodeItem levelNext;
    }
}
