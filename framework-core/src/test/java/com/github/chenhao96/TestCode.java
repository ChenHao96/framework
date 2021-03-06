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

import com.github.chenhao96.component.manager.ComponentManager;
import com.github.chenhao96.component.process.ProcessInvokeService;
import com.github.chenhao96.component.process.handler.HandlerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class TestCode {

    @Resource
    private ComponentManager componentManager;

    @Resource
    private HandlerFactory handlerFactory;

    @Before
    public void before() {
        componentManager.initialize();
        if (componentManager.isInitialized()) {
            componentManager.startup();
        }
    }

    @Test
    public void testApp() throws Exception {
        ProcessInvokeService invokeService = handlerFactory.getProcessMethod((byte) 1, (byte) 1);
        if (invokeService != null) {
            Map<String, Object> param = new HashMap<>();
            param.put("code", 1);
            param.put("name", "name");
            param.put("cards[0].code", 2);
            param.put("model.code", 3);
            param.put("ext.code", 4);
            invokeService.invokeProcess(param);
        }
    }

    @After
    public void after() {
        if (componentManager.isStartup()) {
            componentManager.shutdown();
        }
    }
}
