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

package com.github.chenhao96.component.executor;

import java.util.concurrent.TimeUnit;

public interface TaskExecutorService {

    void addHandler(Runnable task);

    void addHandlerDelay(Runnable task, long delay, TimeUnit unit);

    void addHandlerDelay(Runnable task, long initialDelay, long delay, TimeUnit unit);

    void addHandlerRate(Runnable task, long period, TimeUnit unit);

    void addHandlerRate(Runnable task, long initialDelay, long period, TimeUnit unit);
}
