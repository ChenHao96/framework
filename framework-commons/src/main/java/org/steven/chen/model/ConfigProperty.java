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

    private static final int DEFAULT_SOCKET_PORT = 8899;
    private static final boolean DEFAULT_SOCKET_SSL = false;
    private static final long DEFAULT_NO_DATA_WAIT_TIME = TimeUnit.SECONDS.toMillis(30);
    private static final int DEFAULT_THREAD_POOL_SIZE = (int) (Runtime.getRuntime().availableProcessors() * 2.5);

    private static final ConfigProperty inner = new ConfigProperty();

    public ConfigProperty() {
        this.socketSsl = DEFAULT_SOCKET_SSL;
        this.socketPort = DEFAULT_SOCKET_PORT;
        this.noDataWaitTime = DEFAULT_NO_DATA_WAIT_TIME;
        this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
    }

    public static ConfigProperty getInstance() {
        return inner;
    }

    private boolean socketSsl;
    private int socketPort;
    private long noDataWaitTime;
    private int threadPoolSize;

    public long getNoDataWaitTime() {
        return noDataWaitTime;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public boolean getSocketSsl() {
        return socketSsl;
    }

    public int getSocketPort() {
        return socketPort;
    }

    public static long getNoDataWaitTimeStatic() {
        return inner.noDataWaitTime;
    }

    public static int getThreadPoolSizeStatic() {
        return inner.threadPoolSize;
    }

    public static boolean getSocketSslStatic() {
        return inner.socketSsl;
    }

    public static int getSocketPortStatic() {
        return inner.socketPort;
    }

    public void setSocketPort(int socketPort) {
        this.socketPort = socketPort;
    }

    public void setNoDataWaitTime(long noDataWaitTime) {
        this.noDataWaitTime = noDataWaitTime;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public void setSocketSsl(boolean socketSsl) {
        this.socketSsl = socketSsl;
    }
}
