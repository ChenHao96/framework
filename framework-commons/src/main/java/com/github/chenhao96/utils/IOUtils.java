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
package com.github.chenhao96.utils;

import java.io.*;
import java.nio.CharBuffer;

public class IOUtils {

    private static final int BUFFER_SIZE = 1024;

    public static String read(Reader reader) throws IOException {
        if (reader == null) return null;
        try (StringWriter writer = new StringWriter()) {
            int read;
            CharBuffer buf = CharBuffer.allocate(BUFFER_SIZE);
            while ((read = reader.read(buf)) > 0) {
                buf.flip();
                writer.write(buf.array(), 0, read);
                buf.clear();
            }
            return writer.getBuffer().toString();
        }
    }

    public static long write(InputStream in, OutputStream out) throws IOException {
        return write(in, out, BUFFER_SIZE);
    }

    public static long write(InputStream in, OutputStream out, int bufferSize) throws IOException {

        if (in == null) return 0;
        int count;
        long total = 0;

        byte[] buffer = new byte[bufferSize];
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
            total += count;
        }

        out.flush();
        return total;
    }

    @Deprecated
    public static String readStream2String(Reader reader) throws IOException {
        return read(reader);
    }

    public static String readStream2String(InputStream in, String charSet) throws IOException {
        if (in == null) return "";
        return read(new BufferedReader(new InputStreamReader(in, charSet)));
    }

    public static String readStream2String(InputStream in) throws IOException {
        return readStream2String(in, CommonsUtil.DEFAULT_ENCODING);
    }

    public static byte[] readStream2ByteArray(InputStream in) throws IOException {
        if (in == null) return new byte[0];
        ByteArrayOutputStream output = new ByteArrayOutputStream(BUFFER_SIZE);
        try {
            write(in, output);
            return output.toByteArray();
        } finally {
            CommonsUtil.safeClose(output);
        }
    }

    public static boolean checkFile(File file) {
        if (file == null) return false;
        try {
            return file.exists() && file.isFile() && file.canRead();
        } catch (SecurityException var5) {
            return false;
        }
    }
}
