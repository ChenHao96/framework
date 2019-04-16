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

package org.steven.chen.database.mybatis.interceptor;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.steven.chen.database.ChooseDataSource;
import org.steven.chen.database.DataSourceType;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Intercepts(
        {@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class MyBatisSQLMonitorInterceptor implements Interceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyBatisSQLMonitorInterceptor.class);
    private static final Map<String, DataSourceType> CACHE_MAP = new ConcurrentHashMap<>();

    public Object intercept(Invocation invocation) throws Throwable {
        String sqlMappingID = "";
        int transactionID = 0;
        try {

            Transaction transaction = ((Executor) invocation.getTarget()).getTransaction();
            transactionID = transaction.hashCode();
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            sqlMappingID = mappedStatement.getId();

            DataSourceType dataSourceType;
            if ((dataSourceType = CACHE_MAP.get(mappedStatement.getId())) == null) {
                if (!mappedStatement.getSqlCommandType().equals(SqlCommandType.SELECT)) {
                    dataSourceType = DataSourceType.MASTER;
                } else {
                    dataSourceType = DataSourceType.SLAVE;
                    BoundSql boundSql = mappedStatement.getSqlSource().getBoundSql(invocation.getArgs()[1]);
                    String sql = boundSql.getSql();
                    if (StringUtils.hasLength(sql)) {
                        if (!sql.trim().toLowerCase().startsWith("select")) {
                            dataSourceType = DataSourceType.MASTER;
                        }
                    }
                }
                CACHE_MAP.put(sqlMappingID, dataSourceType);
            }
            ChooseDataSource.setDataSource(dataSourceType);
        } catch (Throwable e) {
            LOGGER.warn("get db url from invocation error !", e);
        }

        long beginTime = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            LOGGER.info("SQL:[id:{}][ts:{}][cost:{}ms] ", sqlMappingID, transactionID, endTime - beginTime);
        }
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties properties) {
    }
}
