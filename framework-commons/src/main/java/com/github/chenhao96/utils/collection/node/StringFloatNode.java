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

public class StringFloatNode<V> implements Node<String, V> {

    private int size;
    private FloatLevelNode root;

    private final int maxLevel;
    private final Random random = new Random();
    private static final int DEFAULT_MAX_LEVEL = 16;

    public StringFloatNode() {
        this(DEFAULT_MAX_LEVEL);
    }

    public StringFloatNode(int maxLevel) {
        if (maxLevel < 1) {
            throw new IllegalArgumentException("maxLevel param must be greater than 1.");
        }
        this.maxLevel = maxLevel;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public V get(Object key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        FloatNode node = queryHashValue(null, null, this.root, key.hashCode());
        return node == null ? null : node.currentNode == null ? null : node.currentNode.value;
    }

    @Override
    public V put(String key, V value) {

        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        if (this.size >= Integer.MAX_VALUE)
            throw new IllegalArgumentException("container element reaches the upper limit.");

        int hashCode = key.hashCode();
        int levelCode = randomLevel();
        if (initRoot(value, hashCode, levelCode)) return null;

        V result = null;
        FloatNode queryNode = queryHashValue(null, null, this.root, key.hashCode());
        if (queryNode != null) {
            result = queryNode.currentNode.value;
            queryNode.currentNode.value = value;
        } else {
            this.size++;
            FloatLevelNode current = new FloatLevelNode();
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
        FloatNode node = queryHashValue(null, null, this.root, key.hashCode());
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
            this.root = new FloatLevelNode();
            this.root.level = levelCode;
            this.root.index = hashCode;
            this.root.value = value;
            this.size = 1;
            return true;
        }
        return false;
    }

    private FloatLevelNode putLevelNode(FloatLevelNode current, FloatLevelNode node) {
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

    private FloatNode queryHashValue(FloatLevelNode previousLevel, FloatLevelNode previous, FloatLevelNode current, int hashCode) {
        if (current != null) {
            if (current.index > hashCode) {
                FloatLevelNode nextLevel = current.levelNext;
                return queryHashValue(current, previous, nextLevel, hashCode);
            } else if (current.index < hashCode) {
                return queryHashValue(previousLevel, current, current.dataNext, hashCode);
            }
            return new FloatNode(previousLevel, previous, current);
        }
        return null;
    }

    private class FloatNode {
        private FloatLevelNode previousLevel;
        private FloatLevelNode previousNode;
        private FloatLevelNode currentNode;
        public FloatNode(FloatLevelNode previousLevel, FloatLevelNode previousNode, FloatLevelNode currentNode) {
            this.previousLevel = previousLevel;
            this.previousNode = previousNode;
            this.currentNode = currentNode;
        }
    }

    private class FloatLevelNode {
        private V value;
        private int index;
        private int level;
        private FloatLevelNode dataNext;
        private FloatLevelNode levelNext;
    }
}
