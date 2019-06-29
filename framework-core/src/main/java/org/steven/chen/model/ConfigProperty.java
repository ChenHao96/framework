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

package org.steven.chen.model;

import java.util.concurrent.TimeUnit;

public class ConfigProperty {

    public static final String SOCKET_PORT_KEY = "steven.java.socket.port";
    public static final String ENABLE_SOCKET_SSL_KEY = "steven.netty.socket.ssl";
    public static final String THREAD_POOL_SIZE_KEY = "steven.task.executor.poolSize";
    public static final String SOCKET_NO_DATA_WAIT_TIME_KEY = "steven.java.socket.noDataWaitTime";

    private static final String SOCKET_PORT_DEFAULT = "8899";
    private static final String ENABLE_SOCKET_SSL_DEFAULT = "false";
    private static final String SOCKET_NO_DATA_WAIT_TIME_DEFAULT = TimeUnit.SECONDS.toMillis(30) + "";
    private static final String THREAD_POOL_SIZE_DEFAULT = (Runtime.getRuntime().availableProcessors() * 2.5) + "";

    public static int getSocketPort() {
        return Integer.valueOf(System.getProperty(SOCKET_PORT_KEY, SOCKET_PORT_DEFAULT));
    }

    public static int getThreadPoolSize() {
        return Integer.valueOf(System.getProperty(THREAD_POOL_SIZE_KEY, THREAD_POOL_SIZE_DEFAULT));
    }

    public static boolean getSocketSsl() {
        return Boolean.valueOf(System.getProperty(ENABLE_SOCKET_SSL_KEY, ENABLE_SOCKET_SSL_DEFAULT));
    }

    public static long getNoDataWaitTime() {
        return Long.valueOf(System.getProperty(SOCKET_NO_DATA_WAIT_TIME_KEY, SOCKET_NO_DATA_WAIT_TIME_DEFAULT));
    }

    public static void setSocketPort(int port) {
        System.setProperty(SOCKET_PORT_KEY, port + "");
    }

    public static void setSocketSsl(boolean enable) {
        System.setProperty(ENABLE_SOCKET_SSL_KEY, enable + "");
    }

    public static void setThreadPoolSize(int poolSize) {
        System.setProperty(THREAD_POOL_SIZE_KEY, poolSize + "");
    }

    public static void setNoDataWaitTime(long noDataWaitTime) {
        System.setProperty(SOCKET_NO_DATA_WAIT_TIME_KEY, noDataWaitTime + "");
    }
}
