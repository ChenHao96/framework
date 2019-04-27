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

package org.steven.chen.game.model;

import java.util.HashSet;
import java.util.Map;

public class TestModel {

    private int code;
    private String name;
    private TestModel model;
    private HashSet<TestModel> cards;
    private Map<String, Object> ext;

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

    public HashSet<TestModel> getCards() {
        return cards;
    }

    public void setCards(HashSet<TestModel> cards) {
        this.cards = cards;
    }

    public Map<String, Object> getExt() {
        return ext;
    }

    public void setExt(Map<String, Object> ext) {
        this.ext = ext;
    }

    public TestModel getModel() {
        return model;
    }

    public void setModel(TestModel model) {
        this.model = model;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TestModel{");
        sb.append("code=").append(code);
        sb.append(", name='").append(name).append('\'');
        sb.append(", cards=").append(cards);
        sb.append(", model=").append(model);
        sb.append(", ext=").append(ext);
        sb.append('}');
        return sb.toString();
    }
}
