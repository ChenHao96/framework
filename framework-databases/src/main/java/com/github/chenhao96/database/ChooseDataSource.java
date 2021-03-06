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

package com.github.chenhao96.database;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ChooseDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<DataSourceType> holder = new ThreadLocal<DataSourceType>() {
        @Override
        protected DataSourceType initialValue() {
            return DataSourceType.MASTER;
        }
    };

    public static void setDataSource(DataSourceType source) {
        holder.set(source);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return holder.get();
    }
}
