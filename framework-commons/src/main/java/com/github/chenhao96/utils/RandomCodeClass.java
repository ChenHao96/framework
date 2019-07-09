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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class RandomCodeClass {

    private char[] characterSet;

    private RandomCodeClass() {
        this(CodeCharArray.NUMBER);
    }

    private RandomCodeClass(CodeCharArray... characterSets) {

        if (characterSets == null || characterSets.length == 0) {
            throw new IllegalArgumentException("argument can not null or nothing.");
        }

        int index = 0;
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

        char[] tmp = new char[length];
        for (CodeCharArray characterSet : characterSets) {
            if (characterSet == null || characterSet.length == 0) {
                continue;
            }
            for (char c : characterSet.body) {
                tmp[index++] = c;
            }
        }

        this.characterSet = tmp;
    }

    private RandomCodeClass(char[] characterSets) {

        if (characterSets == null || characterSets.length == 0) {
            throw new IllegalArgumentException("argument can not null or nothing.");
        }

        int index = 0;
        int length = characterSets.length;
        char[] tmp = new char[length];
        for (char c : characterSet) {
            tmp[index++] = c;
        }

        this.characterSet = tmp;
    }

    public static RandomCodeClass getInstance(CodeCharArray... codeChars) {
        if (codeChars == null || codeChars.length == 0) {
            return new RandomCodeClass();
        } else {
            return new RandomCodeClass(codeChars);
        }
    }

    public static RandomCodeClass getInstance(char[] chars) {
        if (chars == null || chars.length == 0) {
            return new RandomCodeClass();
        } else {
            return new RandomCodeClass(chars);
        }
    }

    public String createCode(int length) {
        Set<String> result = createCode(length, 1);
        return result.iterator().next();
    }

    public Set<String> createCode(int length, int size) {
        characterSet = exchangeCode(characterSet, new Random());
        char[] tmp = characterSet;
        return createCode(length, size, tmp);
    }

    private static Set<String> createCode(int length, int size, char[] tmp) {

        Random random = new Random();
        Set<String> strings = new HashSet<>(size);
        while (strings.size() < size) {

            tmp = exchangeCode(tmp, random);
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(tmp[random.nextInt(tmp.length)]);
            }

            strings.add(sb.toString());
        }

        return strings;
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
