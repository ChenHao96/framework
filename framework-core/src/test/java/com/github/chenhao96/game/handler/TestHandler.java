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

package com.github.chenhao96.game.handler;

import com.github.chenhao96.component.process.HandlerMapping;
import com.github.chenhao96.component.process.ProcessHandlerService;
import com.github.chenhao96.component.process.handler.MapParameter;
import com.github.chenhao96.game.model.TestModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;

@Component
@HandlerMapping(1)
public class TestHandler implements ProcessHandlerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestHandler.class);

    @HandlerMapping(1)
    public void say(int code, String name, TestModel model, HashSet<TestModel> cards, @MapParameter(prefix = "ext") Map<String, Object> ext) {
        LOGGER.info("say model:{}", model);
        LOGGER.info("say code:{},name:{}", code, name);
        LOGGER.info("say cards:{}", cards);
        LOGGER.info("say ext:{}", ext);
    }
}
