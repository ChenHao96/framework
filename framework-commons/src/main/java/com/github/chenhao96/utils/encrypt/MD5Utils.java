package com.github.chenhao96.utils.encrypt;

import com.github.chenhao96.utils.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;

public class MD5Utils {

    public static String getMD5Hex(String data) {
        Preconditions.checkArgument(StringUtil.isNotBlank(data), "Data to md5 is empty.");
        return DigestUtils.md5Hex(data);
    }

    public static boolean isEquals(String data, String md5) {
        Preconditions.checkArgument(StringUtil.isNotBlank(md5), "Md5 is empty.");
        return StringUtil.equals(getMD5Hex(data), md5);
    }
}
