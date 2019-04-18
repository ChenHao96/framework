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
    private static final long DEFAULT_NO_DATA_WAIT_TIME = TimeUnit.SECONDS.toMillis(30);

    private Integer socketPort;

    private Long noDataWaitTime;

    private Integer threadPoolSize;

    private static final ConfigProperty self = new ConfigProperty();

    private ConfigProperty() {
    }

    public static ConfigProperty getInstance() {
        return self;
    }

    public static int getThreadPoolSize() {
        if (self.threadPoolSize == null || self.threadPoolSize < 1) {
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            self.threadPoolSize = (int) (availableProcessors * 2.5);
        }
        return self.threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        self.threadPoolSize = threadPoolSize;
    }

    public static Integer getSocketPort() {
        if (self.socketPort != null) {
            if (self.socketPort < 1) {
                self.socketPort = DEFAULT_SOCKET_PORT;
            }
        } else {
            self.socketPort = DEFAULT_SOCKET_PORT;
        }
        return self.socketPort;
    }

    public void setSocketPort(int socketPort) {
        self.socketPort = socketPort;
    }

    public static long getNoDataWaitTime() {
        if (self.noDataWaitTime != null) {
            if (self.noDataWaitTime < DEFAULT_NO_DATA_WAIT_TIME) {
                self.noDataWaitTime = DEFAULT_NO_DATA_WAIT_TIME;
            }
        } else {
            self.noDataWaitTime = DEFAULT_NO_DATA_WAIT_TIME;
        }
        return self.noDataWaitTime;
    }

    public void setNoDataWaitTime(long noDataWaitTime) {
        self.noDataWaitTime = noDataWaitTime;
    }
}
