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

package com.github.chenhao96.component.process.handler;

import java.util.HashMap;
import java.util.Map;

public final class BaseDataTypeUtil {

    private static final Map<Class, Class> baseDataType = toBaseNumberType();

    private static Map<Class, Class> toBaseNumberType() {
        Map<Class, Class> result = new HashMap<>(8);
        result.put(long.class, Long.class);
        result.put(int.class, Integer.class);
        result.put(double.class, Double.class);
        result.put(byte.class, Byte.class);
        result.put(short.class, Short.class);
        result.put(float.class, Float.class);
        result.put(boolean.class, Boolean.class);
        result.put(char.class, Character.class);
        return result;
    }

    protected static boolean isBaseDataType(Class<?> clazz) {
        return baseDataType.get(clazz) != null;
    }

    protected static Class<?> baseDataType2BoxDataType(Class<?> clazz) {
        Class result = baseDataType.get(clazz);
        return result == null ? clazz : result;
    }

    private BaseDataTypeUtil() {
    }
}
