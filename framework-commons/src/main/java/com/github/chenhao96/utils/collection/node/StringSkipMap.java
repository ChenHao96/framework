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
import java.util.Map;
import java.util.Set;

public class StringSkipMap<V> extends StringSkipNode<V> implements Map<String, V> {

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        //TODO:
        return false;
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> map) {
        if (map == null || map.size() == 0) return;
        Set<? extends Entry<? extends String, ? extends V>> entries = map.entrySet();
        for (Entry<? extends String, ? extends V> entry : entries) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Set<String> keySet() {
        //TODO:
        return null;
    }

    @Override
    public Collection<V> values() {
        //TODO:
        return null;
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        //TODO:
        return null;
    }
}
