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

package org.steven.chen.utils;

import org.springframework.beans.BeanUtils;

import java.io.Closeable;
import java.net.InetAddress;
import java.net.Socket;

public final class CommonsUtil {

    private CommonsUtil() {
    }

    public static String socketRemoteIP(Socket client) {
        if (client == null) return null;
        InetAddress inetAddress = client.getInetAddress();
        if (inetAddress == null) return null;
        return inetAddress.getHostAddress();
    }

    public static void safeClose(Closeable... closeables) {
        if (closeables != null && closeables.length > 0) {
            for (Closeable closeable : closeables) {
                if (closeable == null) continue;
                try {
                    closeable.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void safeClose(AutoCloseable... closeables) {
        if (closeables != null && closeables.length > 0) {
            for (AutoCloseable closeable : closeables) {
                if (closeable == null) continue;
                try {
                    closeable.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static boolean isSimpleProperty(Object obj) {
        if (obj == null) return true;
        Class clazz;
        if (obj instanceof Class) {
            clazz = (Class) obj;
        } else {
            clazz = obj.getClass();
        }
        return BeanUtils.isSimpleProperty(clazz);
    }

    public static boolean isInstanceProperty(Class target, Object sources) {
        return sources != null && (target != null && target.isInstance(sources));
    }
}
