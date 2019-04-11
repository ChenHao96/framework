package org.steven.chen.utils.encrypt;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

public class MD5Utils {

    public static String getMD5Hex(String data) {
        Preconditions.checkArgument(StringUtils.isNotBlank(data), "Data to md5 is empty.");
        return DigestUtils.md5Hex(data);
    }

    public static boolean isEquals(String data, String md5) {
        Preconditions.checkArgument(StringUtils.isNotBlank(md5), "Md5 is empty.");
        return StringUtils.equals(getMD5Hex(data), md5);
    }
}
