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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SkipHashMap<K, V> extends SkipHashNode<K, V> implements Map<K, V> {

    private Set<Entry<K, V>> entries;

    @Override
    public V remove(Object key) {
        if (key == null) return null;
        V result = super.remove(key);
        if (result != null && entries != null) entries.remove(key);
        return result;
    }

    @Override
    public V put(K key, V value) {
        V result = super.put(key, value);
        if (key == null) return null;
        if (this.entries == null) this.entries = new LinkedHashSet<>();
        this.entries.add(new SkipHashEntry(key, value));
        return result;
    }

    @Override
    public boolean containsKey(Object obj) {
        if (obj == null) return false;
        if (entries == null || entries.size() == 0) {
            return false;
        }
        for (Entry<K, V> entry : entries) {
            if (obj.equals(entry.getKey())) return true;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object obj) {
        if (obj == null) return false;
        if (entries == null || entries.size() == 0) {
            return false;
        }
        for (Entry<K, V> entry : entries) {
            if (obj.equals(entry.getValue())) return true;
        }
        return false;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        map.entrySet();
    }

    @Override
    public Set<K> keySet() {
        if(entries==null)return null;

        return null;
    }

    @Override
    public Collection<V> values() {
        if(entries==null)return null;

        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return entries;
    }

    private class SkipHashEntry implements Entry<K, V> {

        private K key;
        private V value;

        public SkipHashEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            return this == obj || obj.hashCode() == hashCode();
        }

        @Override
        public int hashCode() {
            return key == null ? 0 : key.hashCode();
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public V getValue() {
            return this.value;
        }

        @Override
        public V setValue(V value) {
            V result = this.value;
            this.value = value;
            return result;
        }
    }
}
