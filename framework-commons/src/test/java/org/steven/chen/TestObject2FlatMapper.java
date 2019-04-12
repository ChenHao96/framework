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

package org.steven.chen;

import org.steven.chen.utils.JsonUtils;

import java.util.*;

public class TestObject2FlatMapper {

    private int code;
    private String name;
    private int[] numbers;
    private Set<TestObject2FlatMapper> cards;
    private Map<String, Object> caches;
    private TestObject2FlatMapper node;

    public TestObject2FlatMapper() {
    }

    public TestObject2FlatMapper(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getNumbers() {
        return numbers;
    }

    public void setNumbers(int[] numbers) {
        this.numbers = numbers;
    }

    public Set<TestObject2FlatMapper> getCards() {
        return cards;
    }

    public void setCards(Set<TestObject2FlatMapper> cards) {
        this.cards = cards;
    }

    public Map<String, Object> getCaches() {
        return caches;
    }

    public void setCaches(Map<String, Object> caches) {
        this.caches = caches;
    }

    public TestObject2FlatMapper getNode() {
        return node;
    }

    public void setNode(TestObject2FlatMapper node) {
        this.node = node;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestObject2FlatMapper{");
        sb.append("code=").append(code);
        sb.append(", name='").append(name).append('\'');
        sb.append(", numbers=").append(Arrays.toString(numbers));
        sb.append(", cards=").append(cards);
        sb.append(", caches=").append(caches);
        sb.append(", node=").append(node);
        sb.append('}');
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {

        TestObject2FlatMapper test = new TestObject2FlatMapper();
        TestObject2FlatMapper test2 = new TestObject2FlatMapper();
        Map<String, Object> caches = new HashMap<>();
        caches.put("code", 1);
        caches.put("name", "Steven");
        Set<TestObject2FlatMapper> cards = new HashSet<>();
        caches.put("node", cards);
        test.setNode(test2);
        test.setCaches(caches);
        test.setCards(cards);
        cards.add(new TestObject2FlatMapper(2, "world"));
        test2.setCode(2);
        test2.setName("Chen");
        String json = JsonUtils.object2Json(test);
        System.out.println(JsonUtils.jsonStr2JsonNode(json));
        System.out.println(JsonUtils.jsonStr2Object(json, TestObject2FlatMapper.class));
//        Object2FlatMapper object2FlatMapper = new Object2FlatMapperBean();
//        Map<String, Object> hash = object2FlatMapper.toFlatMapper(test.getClass());
//        System.out.println(hash);
        //System.out.println(object2FlatMapper.fromFlatMapper(hash,TestObject2FlatMapper.class));
    }
}
