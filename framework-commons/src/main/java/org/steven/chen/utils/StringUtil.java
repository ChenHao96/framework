package org.steven.chen.utils;

import java.io.StringWriter;
import java.util.*;

public class StringUtil {

    private static final Map<Character, String> entityNames = new HashMap<>(5);

    static {
        entityNames.put((char) 34, "quot");
        entityNames.put((char) 38, "amp");
        entityNames.put((char) 39, "apos");
        entityNames.put((char) 60, "lt");
        entityNames.put((char) 62, "gt");
    }

    public static String escapeXML(String str) {

        StringWriter stringWriter = new StringWriter((int) ((double) str.length() + (double) str.length() * 0.1D));

        int len = str.length();
        for (int i = 0; i < len; ++i) {

            char c = str.charAt(i);
            String entityName = entityNames.get(c);
            if (entityName == null) {
                stringWriter.write(c);
            } else {
                stringWriter.write(38);
                stringWriter.write(entityName);
                stringWriter.write(59);
            }
        }

        return stringWriter.toString();
    }

    public static Set<String> comma2Set(String value) {

        if (isNotBlank(value)) {

            String[] errs = value.split(",");
            Set<String> errFields = new HashSet<>(errs.length);
            Collections.addAll(errFields, errs);

            return errFields;
        }

        return null;
    }

    public static List<String> comma2List(String value) {

        if (isNotBlank(value)) {

            String[] errs = value.split(",");
            List<String> errFields = new ArrayList<>(errs.length);
            Collections.addAll(errFields, errs);

            return errFields;
        }

        return null;
    }

    public static String halfValue(String value) {

        if (isNotBlank(value)) {
            if (value.length() <= 3) {
                return value.substring(0, value.length() - 1) + "*";
            }

            int halfLength = value.length() / 2;
            int halfLengthHalf = halfLength / 2;
            StringBuilder pid = new StringBuilder(value.substring(0, halfLengthHalf));
            for (int i = 0; i < halfLength; i++) {
                pid.append("*");
            }

            pid.append(value.substring(halfLength + halfLengthHalf));
            return pid.toString();
        }

        return value;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean equals(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }
}
