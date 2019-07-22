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

package com.github.chenhao96.database.redis.distributedlock;

public interface DistributedLockService {

    boolean tryLock(String businessCode, String uniqueKey);

    boolean tryLock(String businessCode, String uniqueKey, int expireTimeSeconds);

    LockedFuture tryReturningFutureLock(String businessCode, String uniqueKey);

    LockedFuture tryReturningFutureLock(String businessCode, String uniqueKey, int expireTimeSeconds);

    void unLock(String businessCode, String uniqueKey);

    void unLock(String businessCode, String uniqueKey, LockedFuture lockedFuture);

    boolean flushExpireTimeAfterLocked(String businessCode, String uniqueKey, int expireTimeSeconds);

    boolean flushExpireTimeAfterLocked(String businessCode, String uniqueKey, LockedFuture lockedFuture, int expireTimeSeconds);
}
