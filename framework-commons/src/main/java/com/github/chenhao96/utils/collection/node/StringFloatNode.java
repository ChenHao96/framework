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
        FloatNode node = queryHashValue(this.root, key.hashCode());
        return node == null ? null : node.currentNode.value;
    }

    @Override
    public V put(String key, V value) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        if (this.size >= Integer.MAX_VALUE)
            throw new IllegalArgumentException("container element reaches the upper limit.");
        int hashCode = key.hashCode();
        FloatLevelNode levelNode = this.root;
        if (levelNode == null) this.root = levelNode = new FloatLevelNode();
        FloatNode queryNode = queryHashValue(levelNode, hashCode);
        V result = null;
        if (queryNode != null) {
            result = queryNode.currentNode.value;
            queryNode.currentNode.value = value;
        } else {
            this.size++;
            int levelCode = randomLevel();
            //TODO:insert value
        }
        return result;
    }

    @Override
    public V remove(Object key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        FloatNode node = queryHashValue(this.root, key.hashCode());
        if (node != null) {
            this.size--;
            node.previousNode.dataNext = node.currentNode.dataNext;
            return node.currentNode.value;
        }
        return null;
    }

    private FloatNode queryHashValue(FloatLevelNode level, int hashCode) {
        if (level == null) return null;
        FloatLevelNode nextData = level.dataNext;
        if (nextData == null || nextData.index > hashCode) {
            FloatLevelNode nextLevel = level.levelNext;
            if (nextLevel != null) {
                return queryHashValue(nextLevel, hashCode);
            }
            return null;
        } else if (nextData.index < hashCode) {
            return queryHashValue(nextData, hashCode);
        }
        return new FloatNode(level, nextData);
    }

    private int randomLevel() {
        int result = random.nextInt(maxLevel);
        int size = random.nextInt(maxLevel);
        for (int i = 0; i < size; i++) {
            result += random.nextInt(maxLevel);
        }
        return result % maxLevel;
    }

    private class FloatNode {
        private FloatLevelNode previousNode;
        private FloatLevelNode currentNode;
        public FloatNode(FloatLevelNode previousNode, FloatLevelNode currentNode) {
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
