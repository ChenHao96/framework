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

package org.steven.chen.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.steven.chen.utils.StringUtil;

import java.io.IOException;

public class MappingJackson2HttpMessageConverter extends org.springframework.http.converter.json.MappingJackson2HttpMessageConverter {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ThreadLocal<String> holder = new ThreadLocal<>();

    public MappingJackson2HttpMessageConverter() {
        super(mapper);
    }

    static {
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected static void setJsonPCallBackName(String callBackName) {
        holder.set(callBackName);
    }

    @Override
    protected void writePrefix(JsonGenerator generator, Object object) throws IOException {
        String callbackName = holder.get();
        if (StringUtil.isNotEmpty(callbackName)) {
            generator.writeRaw(String.format("%s(", callbackName));
        }
    }

    @Override
    protected void writeSuffix(JsonGenerator generator, Object object) throws IOException {
        String callbackName = holder.get();
        if (StringUtil.isNotEmpty(callbackName)) {
            generator.writeRaw(");");
        }
        holder.remove();
    }
}
