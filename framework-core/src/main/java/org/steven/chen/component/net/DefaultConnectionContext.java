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

package org.steven.chen.component.net;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class DefaultConnectionContext implements ConnectionContext {

    private Map<String, Object> cacheMap = new HashMap<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void setAttribute(String name, Object obj) {
        lock.writeLock().lock();
        try {
            cacheMap.put(name, obj);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Object getAttribute(String name) {
        lock.readLock().lock();
        try {
            return cacheMap.get(name);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Set<String> getAttributeNames() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableSet(cacheMap.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Object removeAttribute(String name) {
        lock.writeLock().lock();
        try {
            return cacheMap.remove(name);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clearAttribute() {
        lock.writeLock().lock();
        try {
            cacheMap.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
