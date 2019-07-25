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

public class StringFloatNode<V> implements Node<String, V> {

    private int size;
    private NodeItem root;

    private final int maxLevel;
    private final int[] levelArray;
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
        this.levelArray = new int[maxLevel];
    }

    public int[] getLevelArray() {
        int[] result = new int[levelArray.length];
        System.arraycopy(levelArray, 0, result, 0, result.length);
        return result;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public V get(Object key) {
        return null;
    }

    @Override
    public V put(String key, V value) {
        return null;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void clear() {
        this.root = null;
    }

    private int randomLevel() {
        int result = random.nextInt(maxLevel);
        int size = random.nextInt(maxLevel);
        for (int i = 0; i < size; i++) {
            result += random.nextInt(maxLevel);
        }
        return result % maxLevel;
    }

    public class NodeItem implements Map.Entry<String, V> {

        public V value;
        public int index;
        public String key;
        public NodeItem dataNext;
        public NodeItem levelNext;

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
