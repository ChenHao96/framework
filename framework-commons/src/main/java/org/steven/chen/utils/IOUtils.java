package org.steven.chen.utils;

import java.io.*;
import java.nio.CharBuffer;

public class IOUtils {

    private static final int BUFFER_SIZE = 1024;

    public static String read(Reader reader) throws IOException {
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

    public static String readStream2String(InputStream in, String charSet) throws IOException {
        if (in == null) return "";

        BufferedReader reader = null;
        StringWriter writer = new StringWriter();
        CharBuffer buffer = CharBuffer.allocate(BUFFER_SIZE);

        try {
            int count;
            reader = new BufferedReader(new InputStreamReader(in, charSet));
            while ((count = reader.read(buffer)) > 0) {
                buffer.flip();
                writer.write(buffer.array(), 0, count);
                buffer.clear();
            }
            return writer.toString();
        } finally {
            CommonsUtil.safeClose(in);
            CommonsUtil.safeClose(reader);
            CommonsUtil.safeClose(writer);
        }
    }

    public static byte[] readStream2ByteArray(InputStream in) throws IOException {
        if (in == null) return new byte[0];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(in, output);
        return output.toByteArray();
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
