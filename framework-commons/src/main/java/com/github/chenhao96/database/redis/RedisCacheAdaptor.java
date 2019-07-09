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

package com.github.chenhao96.database.redis;

import com.github.chenhao96.cache.CacheAdaptor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface RedisCacheAdaptor<V> extends CacheAdaptor<String, V> {

    Set<String> keys(String pattern);

    RedisDataType getKeyType(String key);

    Long getExpire(String key, TimeUnit timeUnit);

    void setExpire(String key, Date date);

    void setExpire(String key, long duration);

    void setExpire(String key, long duration, TimeUnit timeUnit);

    void set(String key, V value, long duration);

    void set(String key, V value, long duration, TimeUnit timeUnit);

    void increment(String key);

    void increment(String key, BigDecimal delta);

    void decrement(String key);

    void decrement(String key, Long delta);

    V hashGet(String key, String field);

    Map<String, V> hashGet(String key);

    void hashSet(String key, String field, V value);

    void hashIncrement(String key, String field);

    void hashIncrement(String key, String field, BigDecimal delta);

    void hashSet(String key, Map<String, V> values);

    Set<String> hashFields(String key);

    boolean hashFieldExist(String key, String field);

    void hashDelField(String key, String... fields);
}
