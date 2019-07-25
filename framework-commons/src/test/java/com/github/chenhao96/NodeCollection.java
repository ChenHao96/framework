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
import com.github.chenhao96.utils.collection.node.Node;
import com.github.chenhao96.utils.collection.node.StringSkipNode;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class NodeCollection {

    private List<String> keys;
    private static final int forSize = 10;
    private static final int keyLength = 100;
    private static final int keySize = 100000;

    @Before
    public void before() {
        RandomCodeClass randomCodeClass = RandomCodeClass.getInstance(RandomCodeClass.CodeCharArray.NUMBER,
                RandomCodeClass.CodeCharArray.LARGE_LETTER,
                RandomCodeClass.CodeCharArray.LESS_LETTER);
        this.keys = randomCodeClass.createListCode(keyLength, keySize);
    }

    @Test
    public void testSkipNode() {
        int count;
        Record insertMin = new Record(), queryMin = new Record(), removeMin = new Record();
        long insertMinTime = System.currentTimeMillis(), queryMinTime = insertMinTime, removeMinTime = insertMinTime;
        for (int y = 0; y < forSize; y++) {

            count = 0;
            long startTime = System.currentTimeMillis();
            Node<String, Integer> map = new StringSkipNode<>();
            for (int k = 0; k < keys.size(); k++) {
                Integer value = map.put(keys.get(k), k);
                if (value != null) count++;
            }
            long insertTime = System.currentTimeMillis() - startTime;
            System.out.printf("%d, put use time:%d\n", y, insertTime);

            startTime = System.currentTimeMillis();
            for (int k = 0; k < keys.size(); k++) {
                Integer value = map.get(keys.get(k));
                if (value != null && value == k) count++;
            }
            long queryTime = System.currentTimeMillis() - startTime;
            System.out.printf("%d,query use time:%d\n", y, queryTime);
            if (count != keys.size()) {
                System.out.printf("count:%d fail.\n", count);
                return;
            }

//            count = 0;
            startTime = System.currentTimeMillis();
//            for (int k = 0; k < keys.size(); k++) {
//                Integer value = map.remove(keys.get(k));
//                if (value != null && value == k) count++;
//            }
            long removeTime = System.currentTimeMillis() - startTime;
//            System.out.printf("%d,remove use time:%d\n", y, removeTime);
//            if (count != keys.size()) {
//                System.out.printf("count:%d fail.\n", count);
//                return;
//            }

            if (insertTime < insertMinTime) {
                insertMinTime = insertTime;
                insertMin.insertMinTime = insertTime;
                insertMin.queryMinTime = queryTime;
                insertMin.removeMinTime = removeTime;
            }

            if (queryTime < queryMinTime) {
                queryMinTime = queryTime;
                queryMin.insertMinTime = insertTime;
                queryMin.queryMinTime = queryTime;
                queryMin.removeMinTime = removeTime;
            }

            if (removeTime < removeMinTime) {
                removeMinTime = removeTime;
                removeMin.insertMinTime = insertTime;
                removeMin.queryMinTime = queryTime;
                removeMin.removeMinTime = removeTime;
            }
        }

        System.out.printf("queryMin:%s\n", queryMin);
        System.out.printf("queryMin:%s\n", removeMin);
        System.out.printf("insertMin:%s\n", insertMin);
    }

    private class Record {
        private long queryMinTime;
        private long insertMinTime;
        private long removeMinTime;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Record{");
            sb.append("queryMinTime=").append(queryMinTime);
            sb.append(", insertMinTime=").append(insertMinTime);
            sb.append(", removeMinTime=").append(removeMinTime);
            sb.append('}');
            return sb.toString();
        }
    }
}
