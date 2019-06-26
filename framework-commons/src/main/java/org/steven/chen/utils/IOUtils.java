package org.steven.chen.utils;

import java.io.*;
import java.nio.CharBuffer;

public class IOUtils {

    private static final int BUFFER_SIZE = 1024;

    public static String read(Reader reader) throws IOException {
        try (StringWriter writer = new StringWriter()) {
            write(reader, writer);
            return writer.getBuffer().toString();
        }
    }

    public static long write(Reader reader, Writer writer) throws IOException {
        return write(reader, writer, BUFFER_SIZE);
    }

    public static long write(Reader reader, Writer writer, int bufferSize) throws IOException {

        int read;
        long total = 0;

        CharBuffer buf = CharBuffer.allocate(bufferSize);
        while ((read = reader.read(buf)) != -1) {
            buf.flip();
            writer.write(buf.array(), 0, read);
            buf.clear();
            total += read;
        }

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
            while ((count = reader.read(buffer)) != -1) {
                writer.write(buffer.array(), 0, count);
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
        byte[] buffer = new byte[BUFFER_SIZE];

        int count;
        while ((count = in.read(buffer)) != -1) {
            output.write(buffer, 0, count);
        }

        output.flush();
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
