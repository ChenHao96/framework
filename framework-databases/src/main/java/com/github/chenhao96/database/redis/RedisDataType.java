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

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum RedisDataType {

    NONE("none"), STRING("string"), LIST("list"), SET("set"), ZSET("zset"), HASH("hash");

    private static final Map<String, RedisDataType> codeLookup;

    static {
        EnumSet<RedisDataType> enumSet = EnumSet.allOf(RedisDataType.class);
        codeLookup = new ConcurrentHashMap<>(enumSet.size());
        for (RedisDataType type : enumSet) codeLookup.put(type.code, type);

    }

    private final String code;

    RedisDataType(String name) {
        this.code = name;
    }

    public String code() {
        return code;
    }

    public static RedisDataType fromCode(String code) {
        RedisDataType data = codeLookup.get(code);
        if (data == null)
            throw new IllegalArgumentException("unknown data type code");
        return data;
    }
}
