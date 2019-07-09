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

package com.github.chenhao96;

import org.steven.chen.utils.mapper.Jackson2FlatMapperK;

import java.util.Map;

public class TestJackson2FlatMapper {

    private static final Jackson2FlatMapperK jackson2FlatMapper = new Jackson2FlatMapperK();

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        TestModel model = new TestModel();
        model.setU('A');
        Map<String, Object> param = jackson2FlatMapper.toFlatMapper(model);
        System.out.println(param);
        System.out.println(jackson2FlatMapper.toFlatMapper(param));
        System.out.println(System.currentTimeMillis() - start);
    }
}
