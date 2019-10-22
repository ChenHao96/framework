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

package com.github.chenhao96.database.redis.distributedlock;

import com.github.chenhao96.database.redis.RedisAdaptor;
import com.github.chenhao96.utils.CommonsUtil;
import com.github.chenhao96.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.commands.JedisClusterScriptingCommands;
import redis.clients.jedis.commands.JedisCommands;
import redis.clients.jedis.params.SetParams;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@Service
public class DistributedLockServiceImpl implements DistributedLockService {

    private static final long REPLY = 0L;
    private static final int LOCK_EXPIRE_TIME_SECOND = 120;
    private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();
    private static final String LOCK_PREFIX = "com.github.chenhao96.java.lock.pk.%s.%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedLockServiceImpl.class);
    private static final String UNLOCK_SCRIPT = "  if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n    return redis.call(\"del\",KEYS[1])\n  else\n        return 0\n  end\n";
    private static final String EXPIRE_LOCK_SCRIPT = "  if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n    return redis.call(\"expire\",KEYS[1],ARGV[2])\n  else\n        return 0\n  end\n";

    @Resource
    private RedisAdaptor redisAdaptor;

    public boolean tryLock(String businessCode, String uniqueKey) {
        return tryLock(businessCode, uniqueKey, LOCK_EXPIRE_TIME_SECOND);
    }

    public boolean tryLock(String businessCode, String uniqueKey, int expireTimeSeconds) {
        LockedFuture lockedFuture = tryReturningFutureLock(businessCode, uniqueKey, expireTimeSeconds);
        boolean lockedResult = lockedFuture.getLockedResult();
        if (lockedResult) {
            threadLocal.set(lockedFuture.getLockedToken());
        }
        return lockedResult;
    }

    public LockedFuture tryReturningFutureLock(String businessCode, String uniqueKey) {
        return tryReturningFutureLock(businessCode, uniqueKey, LOCK_EXPIRE_TIME_SECOND);
    }

    public LockedFuture tryReturningFutureLock(String businessCode, String uniqueKey, int expireTimeSeconds) {

        final String token = UUID.randomUUID().toString().replaceAll("-", "");
        final String key = generateKey(businessCode, uniqueKey);

        try {
            String result = redisAdaptor.execute(connection -> {
                JedisCommands commands = (JedisCommands) connection.getNativeConnection();
                return commands.set(key, token, new SetParams().nx().ex(expireTimeSeconds));
            });
            if (StringUtil.isNotEmpty(result)) {
                LOGGER.info("get Lock success , key: {}, thread name: {}", key, Thread.currentThread().getName());
                return new LockedFuture(true, token);
            }
        } catch (Exception var7) {
            LOGGER.warn("get Lock exception", var7);
            throw new RuntimeException(var7.getMessage(), var7);
        }

        LOGGER.info("get Lock fail, key: {}", key);
        return new LockedFuture(false, token);
    }

    public void unLock(String businessCode, String uniqueKey) {
        String key = generateKey(businessCode, uniqueKey);
        String token = threadLocal.get();
        compareTokenAndDelKey(key, token);
    }

    public void unLock(String businessCode, String uniqueKey, LockedFuture lockedFuture) {
        String key = generateKey(businessCode, uniqueKey);
        String token = lockedFuture.getLockedToken();
        compareTokenAndDelKey(key, token);
    }

    public boolean flushExpireTimeAfterLocked(String businessCode, String uniqueKey, int expireTimeSeconds) {
        String key = String.format(LOCK_PREFIX, businessCode, uniqueKey);
        String token = threadLocal.get();
        return compareTokenAndExpireKey(key, token, expireTimeSeconds);
    }

    public boolean flushExpireTimeAfterLocked(String businessCode, String uniqueKey, LockedFuture lockedFuture, int expireTimeSeconds) {
        String key = String.format(LOCK_PREFIX, businessCode, uniqueKey);
        String token = lockedFuture.getLockedToken();
        return compareTokenAndExpireKey(key, token, expireTimeSeconds);
    }

    private boolean compareTokenAndExpireKey(String key, String token, int expireTimeSeconds) {
        if (StringUtil.isNotBlank(token)) {
            Long result = redisAdaptor.execute(connection -> {
                Object nativeConnection = connection.getNativeConnection();
                if (CommonsUtil.isInstanceProperty(JedisClusterScriptingCommands.class, nativeConnection)) {
                    JedisClusterScriptingCommands commands = (JedisClusterScriptingCommands) nativeConnection;
                    return (Long) commands.eval(EXPIRE_LOCK_SCRIPT, Collections.singletonList(key), Arrays.asList(token, String.valueOf(expireTimeSeconds)));
                }
//                if (nativeConnection instanceof JedisCluster) {// 集群模式
//                    return (Long) ((JedisCluster) nativeConnection).eval(EXPIRE_LOCK_SCRIPT, Collections.singletonList(key), Arrays.asList(token, String.valueOf(expireTimeSeconds)));
//                } else if (nativeConnection instanceof Jedis) {// 单机模式
//                    return (Long) ((Jedis) nativeConnection).eval(EXPIRE_LOCK_SCRIPT, Collections.singletonList(key), Arrays.asList(token, String.valueOf(expireTimeSeconds)));
//                }
                return REPLY;
            });
            if (result > REPLY) {
                LOGGER.info("expire lock success , key : {}, thread name: {}", key, Thread.currentThread().getName());
                return true;
            }
        }
        return false;
    }

    private void compareTokenAndDelKey(String key, String token) {
        if (StringUtil.isNotBlank(token)) {
            Long result = redisAdaptor.execute(connection -> {
                Object nativeConnection = connection.getNativeConnection();
                if (CommonsUtil.isInstanceProperty(JedisClusterScriptingCommands.class, nativeConnection)) {
                    JedisClusterScriptingCommands commands = (JedisClusterScriptingCommands) nativeConnection;
                    return (Long) commands.eval(UNLOCK_SCRIPT, Collections.singletonList(key), Collections.singletonList(token));
                }
//                if (nativeConnection instanceof JedisCluster) {// 集群模式
//                    return (Long) ((JedisCluster) nativeConnection).eval(UNLOCK_SCRIPT, Collections.singletonList(key), Collections.singletonList(token));
//                } else if (nativeConnection instanceof Jedis) {// 单机模式
//                    return (Long) ((Jedis) nativeConnection).eval(UNLOCK_SCRIPT, Collections.singletonList(key), Collections.singletonList(token));
//                }
                return REPLY;
            });
            if (result > REPLY) {
                LOGGER.info("unLock success , key : {}, thread name: {}", key, Thread.currentThread().getName());
            }
        }
    }

    private String generateKey(String businessCode, String uniqueKey) {
        return String.format(LOCK_PREFIX, businessCode, uniqueKey);
    }
}
