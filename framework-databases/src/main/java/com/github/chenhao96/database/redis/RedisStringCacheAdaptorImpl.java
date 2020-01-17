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

import com.github.chenhao96.utils.StringUtil;
import com.github.chenhao96.utils.encrypt.MD5Utils;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisStringCacheAdaptorImpl implements RedisStringCacheAdaptor {

    private StringRedisTemplate redisTemplate;

    private String keyPrefix;

    public StringRedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    private String newKey(String key) {
        if (StringUtil.isEmpty(keyPrefix)) return key;
        StringBuffer sb = new StringBuffer();
        sb.append(keyPrefix);
        if (keyPrefix.lastIndexOf('.') != keyPrefix.length() - 1) {
            sb.append(".");
        }
        return sb.append(key).toString();
    }

    @Override
    public boolean delete(String key) {
        return redisTemplate.delete(newKey(key));
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
        setExpire(newKey(key), duration, TimeUnit.MILLISECONDS);
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
    public void set(String key, String value, Date date) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(newKey(key));
        operations.set(value);
        operations.expireAt(date);
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
        set(newKey(key), value, duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public void set(String key, String value, long duration, TimeUnit timeUnit) {
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(newKey(key));
        operations.set(value, duration, timeUnit);
    }

    @Override
    public void increment(String key) {
        increment(newKey(key), BigDecimal.ONE);
    }

    @Override
    public void increment(String key, BigDecimal delta) {
        key = newKey(key);
        if (StringUtil.isEmpty(key) || delta == null) return;
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(key);
        operations.increment(delta.doubleValue());
    }

    @Override
    public void decrement(String key) {
        decrement(newKey(key), BigDecimal.ONE);
    }

    @Override
    public void decrement(String key, BigDecimal delta) {
        key = newKey(key);
        if (StringUtil.isEmpty(key) || delta == null) return;
        BoundValueOperations<String, String> operations = redisTemplate.boundValueOps(key);
        operations.increment(delta.multiply(BigDecimal.valueOf(-1)).doubleValue());
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
        hashIncrement(newKey(key), field, BigDecimal.ONE);
    }

    @Override
    public void hashIncrement(String key, String field, BigDecimal delta) {
        key = newKey(key);
        if (StringUtil.isEmpty(key) || StringUtil.isEmpty(field) || delta == null) return;
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key);
        operations.increment(field, delta.doubleValue());
    }

    @Override
    public void hashDecrement(String key, String field) {
        hashIncrement(newKey(key), field, BigDecimal.valueOf(-1));
    }

    @Override
    public void hashDecrement(String key, String field, BigDecimal delta) {
        if (delta == null) return;
        hashIncrement(newKey(key), field, delta.multiply(BigDecimal.valueOf(-1)));
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

    @Override
    public void listLeftPush(String key, String value) {
        key = newKey(key);
        if (StringUtil.isEmpty(key) || StringUtil.isEmpty(value)) return;
        redisTemplate.boundListOps(key).leftPush(value);
    }

    @Override
    public void listRightPush(String key, String value) {
        key = newKey(key);
        if (StringUtil.isEmpty(key) || StringUtil.isEmpty(value)) return;
        redisTemplate.boundListOps(key).rightPush(value);
    }

    @Override
    public String listLeftPop(String key) {
        key = newKey(key);
        if (StringUtil.isEmpty(key)) return null;
        return redisTemplate.boundListOps(key).leftPop();
    }

    @Override
    public String listRightPop(String key) {
        key = newKey(key);
        if (StringUtil.isEmpty(key)) return null;
        return redisTemplate.boundListOps(key).rightPop();
    }

    @Override
    public long listSize(String key) {
        key = newKey(key);
        if (StringUtil.isEmpty(key)) return 0;
        return redisTemplate.boundListOps(key).size();
    }

    @Override
    public String getRandomId() {
        return MD5Utils.getMD5Hex(redisTemplate.randomKey());
    }
}
