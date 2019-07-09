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

package com.github.chenhao96.component.net;

import org.steven.chen.utils.mapper.Jackson2FlatMapperK;

import java.util.Map;

public class DefaultMessageConvertToHandlerArgs implements MessageConvertToHandlerArgs {

    private static final ThreadLocal<CommonsMessage> holder = new ThreadLocal<>();

    protected Jackson2FlatMapperK object2FlatMapper = new Jackson2FlatMapperK();

    protected CommonsMessage getCommonsMessage() {
        return getCommonsMessage(false);
    }

    protected CommonsMessage getCommonsMessage(boolean instance) {
        CommonsMessage message = holder.get();
        message = message == null ? instance ? new CommonsMessage() : null : message;
        return message;
    }

    @Override
    public void setCommonsMessage(CommonsMessage message) {
        holder.set(message);
    }

    public void removeCommonsMessage() {
        holder.remove();
    }

    public Map<String, Object> convertArgs() {
        throw new AbstractMethodError();
    }

    public CommonsMessage convertMessageReturn(Object obj) {
        throw new AbstractMethodError();
    }
}
