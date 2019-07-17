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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisStringCacheAdaptorImpl implements RedisStringCacheAdaptor {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${redis.server.key.prefix}")
    private String keyPrefix;

    private String newKey(String key) {
        return String.format("%s.%s", keyPrefix, key);
    }

    @Override
    public boolean delete(String key) {
        redisTemplate.delete(newKey(key));
        return true;
    }

    @Override
    public boolean exist(String key) {
        return redisTemplate.hasKey(newKey(key));
    }

    @Override
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(newKey(pattern));
    }

    @Override
    public RedisDataType getKeyType(String key) {
        DataType dataType = redisTemplate.boundValueOps(newKey(key)).getType();
        return RedisDataType.fromCode(dataType.code());
    }

    @Override
    public Long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(newKey(key), timeUnit);
    }

    @Override
    public void setExpire(String key, long duration) {
        redisTemplate.boundValueOps(newKey(key)).expire(duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setExpire(String key, Date date) {
        redisTemplate.boundValueOps(newKey(key)).expireAt(date);
    }

    @Override
    public void setExpire(String key, long duration, TimeUnit timeUnit) {
        redisTemplate.boundValueOps(newKey(key)).expire(duration, timeUnit);
    }

    @Override
    public String get(String key) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(newKey(key));
        return operations.get();
    }

    @Override
    public void set(String key, String value) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(newKey(key));
        operations.set(value);
    }

    @Override
    public void set(String key, String value, long duration) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(newKey(key));
        operations.set(value, duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public void set(String key, String value, long duration, TimeUnit timeUnit) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(newKey(key));
        operations.set(value, duration, timeUnit);
    }

    @Override
    public void increment(String key) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(newKey(key));
        operations.increment();
    }

    @Override
    public void increment(String key, BigDecimal delta) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(newKey(key));
        operations.increment(delta.doubleValue());
    }

    @Override
    public void decrement(String key) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(newKey(key));
        operations.decrement();
    }

    @Override
    public void decrement(String key, Long delta) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(newKey(key));
        operations.decrement(delta);
    }

    @Override
    public String hashGet(String key, String field) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(newKey(key));
        return operations.get(field);
    }

    @Override
    public Map<String, String> hashGet(String key) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(newKey(key));
        return operations.entries();
    }

    @Override
    public void hashSet(String key, String field, String value) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(newKey(key));
        operations.put(field, value);
    }

    @Override
    public void hashIncrement(String key, String field) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(newKey(key));
        operations.increment(field, 1);
    }

    @Override
    public void hashIncrement(String key, String field, BigDecimal delta) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(newKey(key));
        operations.increment(field, delta.doubleValue());
    }

    @Override
    public void hashSet(String key, Map<String, String> values) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(newKey(key));
        operations.putAll(values);
    }

    @Override
    public Set<String> hashFields(String key) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(newKey(key));
        return operations.keys();
    }

    @Override
    public boolean hashFieldExist(String key, String field) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(newKey(key));
        return operations.hasKey(field);
    }

    @Override
    public void hashDelField(String key, String... fields) {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(newKey(key));
        operations.delete(fields);
    }
}
