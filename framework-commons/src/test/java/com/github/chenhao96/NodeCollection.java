/**
 * Copyright 2017-2019 ChenHao96
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
package com.github.chenhao96;

import com.github.chenhao96.utils.RandomCodeClass;
import com.github.chenhao96.utils.collection.node.StringSkipNode;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class NodeCollection {

    private List<String> keys;
    private static final int forSize = 3;
    private static final int keyLength = 100;
    private static final int keySize = 100000;
    private static final int levelSize = keySize / 100;

    @Before
    public void before() {
        RandomCodeClass randomCodeClass = RandomCodeClass.getInstance(RandomCodeClass.CodeCharArray.NUMBER,
                RandomCodeClass.CodeCharArray.LARGE_LETTER,
                RandomCodeClass.CodeCharArray.LESS_LETTER);
        this.keys = randomCodeClass.createListCode(keyLength, keySize);
    }

    @Test
    public void testSkipNode() {
        final int beginLevel = 16;
        Record insertMin = new Record(), queryMin = new Record();
        long insertMinTime = System.currentTimeMillis(), queryMinTime = insertMinTime;
        for (int y = 0; y < forSize; y++) {
            for (int i = beginLevel; i < levelSize + beginLevel; i++) {

                long startTime = System.currentTimeMillis();
                StringSkipNode<String> map = new StringSkipNode<>(i);
                for (String key : keys) map.put(key, key);
                long endTime = System.currentTimeMillis();
                long insertTime = endTime - startTime;
                keys.forEach(map::get);
                long queryTime = System.currentTimeMillis() - endTime;
                System.out.printf("%d,%d, put use time:%d, query use time:%d\n", y, i, insertTime, queryTime);

                if (insertTime < insertMinTime) {
                    insertMinTime = insertTime;
                    insertMin.node = map;
                    insertMin.insertMinTime = insertTime;
                    insertMin.queryMinTime = queryTime;
                }

                if (queryTime < queryMinTime) {
                    queryMinTime = queryTime;
                    queryMin.node = map;
                    queryMin.insertMinTime = insertTime;
                    queryMin.queryMinTime = queryTime;
                }
            }
        }

        System.out.printf("insertMin:%s\n", insertMin);
        System.out.printf("queryMin:%s\n", queryMin);
    }

    private class Record {
        private long queryMinTime;
        private long insertMinTime;
        private StringSkipNode<String> node;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Record{");
            sb.append("insertMinTime=").append(insertMinTime);
            sb.append(", queryMinTime=").append(queryMinTime);
            sb.append(", node=").append(Arrays.toString(node.getLevelArray()));
            sb.append('}');
            return sb.toString();
        }
    }
}