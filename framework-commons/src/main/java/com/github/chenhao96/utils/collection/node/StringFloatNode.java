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

public class StringFloatNode<V> implements Node<String, V> {

    private int size;
    private FloatNodeItem<V> root;

    @Override
    public int size() {
        return size;
    }

    @Override
    public V get(String key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        if (this.root == null) return null;
        //TODO:
        throw new AbstractMethodError();
    }

    @Override
    public V put(String key, V value) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        //TODO:
        throw new AbstractMethodError();
    }

    @Override
    public V remove(String key) {
        if (key == null) throw new IllegalArgumentException("key is required! can not be null.");
        if (this.root == null) return null;
        //TODO:
        throw new AbstractMethodError();
    }
}
