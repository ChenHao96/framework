/**
 * Copyright 2019 ChenHao96
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

package com.github.chenhao96.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CacheAdaptorImpl<K, V> implements CacheAdaptor<K, V> {

    private static final int MAP_INITIAL_CAPACITY = 100;

    private Map<K, V> cacheMap;
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public CacheAdaptorImpl() {
        cacheMap = new HashMap<>(MAP_INITIAL_CAPACITY);
    }

    public CacheAdaptorImpl(int initialCapacity) {
        cacheMap = new HashMap<>(initialCapacity);
    }

    @Override
    public V get(K key) {
        lock.readLock().lock();
        try {
            return cacheMap.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void set(K key, V value) {
        lock.writeLock().lock();
        try {
            cacheMap.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean delete(K key) {
        lock.readLock().lock();
        try {
            V value = cacheMap.remove(key);
            return value != null;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean exist(K key) {
        lock.readLock().lock();
        try {
            return cacheMap != null && cacheMap.keySet().contains(key);
        } finally {
            lock.readLock().unlock();
        }
    }
}
