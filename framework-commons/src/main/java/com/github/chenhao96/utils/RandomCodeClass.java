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
package com.github.chenhao96.utils;

import java.util.*;

public final class RandomCodeClass {

    private char[] characterSet;

    private RandomCodeClass(CodeCharArray... characterSets) {

        if (characterSets == null || characterSets.length == 0) {
            throw new IllegalArgumentException("argument can not null or nothing.");
        }

        int length = 0;
        for (CodeCharArray characterSet : characterSets) {
            if (characterSet == null || characterSet.length == 0) {
                continue;
            }
            length += characterSet.length;
        }

        if (length == 0) {
            throw new IllegalArgumentException("argument can not nothing.");
        }

        int index = 0;
        char[] tmp = new char[length];
        for (CodeCharArray characterSet : characterSets) {
            if (characterSet == null || characterSet.length == 0) continue;
            for (char c : characterSet.body) tmp[index++] = c;
        }

        this.characterSet = tmp;
    }

    private RandomCodeClass(char[] characterSets) {
        if (characterSets == null || characterSets.length == 0) {
            throw new IllegalArgumentException("argument can not null or nothing.");
        }
        char[] tmp = new char[characterSets.length];
        System.arraycopy(characterSets, 0, tmp, 0, characterSets.length);
        this.characterSet = tmp;
    }

    public static RandomCodeClass getInstance(CodeCharArray... codeChars) {
        if (codeChars == null || codeChars.length == 0) {
            return new RandomCodeClass(CodeCharArray.NUMBER);
        } else {
            return new RandomCodeClass(codeChars);
        }
    }

    public static RandomCodeClass getInstance(char[] chars) {
        if (chars == null || chars.length == 0) {
            return new RandomCodeClass(CodeCharArray.NUMBER);
        } else {
            return new RandomCodeClass(chars);
        }
    }

    public String createCode(int length) {
        Set<String> result = createSetCode(length, 1);
        return result.iterator().next();
    }

    public Set<String> createSetCode(int length, int size) {
        characterSet = exchangeCode(characterSet, new Random());
        Set<String> result = new HashSet<>(size);
        createCode(result, length, size, characterSet);
        return result;
    }

    public List<String> createListCode(int length, int size) {
        characterSet = exchangeCode(characterSet, new Random());
        List<String> result = new ArrayList<>(size);
        createCode(result, length, size, characterSet);
        return result;
    }

    private static void createCode(Collection<String> collection, int length, int size, char[] tmp) {
        Random random = new Random();
        while (collection.size() < size) {
            tmp = exchangeCode(tmp, random);
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(tmp[random.nextInt(tmp.length)]);
            }
            collection.add(sb.toString());
        }
    }

    private static char[] exchangeCode(char[] tmp, Random random) {
        int index;
        char swap;
        for (int i = 0; i < tmp.length; i++) {
            index = random.nextInt(tmp.length - i);
            swap = tmp[index];
            tmp[index] = tmp[tmp.length - i - 1];
            tmp[tmp.length - i - 1] = swap;
        }
        return tmp;
    }

    public enum CodeCharArray {
        NUMBER("0123456789".toCharArray()),
        LESS_LETTER("abcdefghijklmnopqrstuvwxyz".toCharArray()),
        LARGE_LETTER("ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()),
        CHARACTER(new char[]{'`', '-', '=', '[', ']', '\\', ';', '\'', ',', '.', '/', '~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', '{', '}', '|', ':', '"', '<', '>', '?'});

        private int length;
        private char[] body;

        CodeCharArray(char[] body) {
            this.body = body;
            this.length = body.length;
        }
    }
}
