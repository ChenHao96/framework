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

package org.steven.chen.database.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SpringRedisStringCacheAdaptorImpl implements RedisStringCacheAdaptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean delete(String key) {
        redisTemplate.delete(key);
        return true;
    }

    @Override
    public boolean exist(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    @Override
    public RedisDataType getKeyType(String key) {
        DataType dataType = redisTemplate.boundValueOps(key).getType();
        return RedisDataType.fromCode(dataType.code());
    }

    @Override
    public Long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    @Override
    public void setExpire(String key, long duration) {
        redisTemplate.boundValueOps(key).expire(duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setExpire(String key, Date date) {
        redisTemplate.boundValueOps(key).expireAt(date);
    }

    @Override
    public void setExpire(String key, long duration, TimeUnit timeUnit) {
        redisTemplate.boundValueOps(key).expire(duration, timeUnit);
    }

    @Override
    public String get(String key) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(key);
        return operations.get();
    }

    @Override
    public void set(String key, String value) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(key);
        operations.set(value);
    }

    @Override
    public void set(String key, String value, long duration) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(key);
        operations.set(value, duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public void set(String key, String value, long duration, TimeUnit timeUnit) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(key);
        operations.set(value, duration, timeUnit);
    }

    @Override
    public void increment(String key) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(key);
        operations.increment(1);
    }

    @Override
    public void increment(String key, BigDecimal delta) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(key);
        operations.increment(delta.doubleValue());
    }

    @Override
    public String hashGet(String key, String field) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key);
        return operations.get(field);
    }

    @Override
    public Map<String, String> hashGet(String key) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key);
        return operations.entries();
    }

    @Override
    public void hashSet(String key, String field, String value) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key);
        operations.put(field, value);
    }

    @Override
    public void hashSet(String key, Map<String, String> values) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key);
        operations.putAll(values);
    }

    @Override
    public Set<String> hashFields(String key) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key);
        return operations.keys();
    }

    @Override
    public boolean hashFieldExist(String key, String field) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key);
        return operations.hasKey(field);
    }

    @Override
    public void hashDelField(String key, String... fields) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key);
        operations.delete(fields);
    }
}
