package com.github.chenhao96.utils.encrypt;

import com.github.chenhao96.utils.CommonsUtil;
import com.github.chenhao96.utils.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class AESUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AESUtils.class);
    /**
     * 加密算法
     * DES               密钥长度必须为56
     * DESede(TripleDES) 密钥长度必须为112或168
     * AES               密钥长度必须为128,192或256,但是192和256存在问题
     * Blowfish          密钥长度必须为8的倍数, 范围仅从32到448(含)
     * RC2               密钥长度必须在40至1024之间
     * RC4(ARCFOUR)      密钥长度必须在40至1024之间
     **/
    private static final String ALGORITHM = "AES";
    /**
     * 密钥长度
     */
    private static final int KEY_SIZE = 128;

    /**
     * 将content的内容以password的密钥加密
     *
     * @param content  被加密码的内容
     * @param password 密钥
     * @return 加密后的秘串
     */
    public static String encrypt(String content, String password) {

        checkParam(content, password);

        try {
            SecretKeySpec e = getSecretKeySpec(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] byteContent = content.getBytes(CommonsUtil.DEFAULT_ENCODING);
            cipher.init(Cipher.ENCRYPT_MODE, e);
            byte[] result = cipher.doFinal(byteContent);
            return new Base64().encodeToString(result);

        } catch (Exception e) {
            LOGGER.warn("string:{} encrypt fail", content, e);
            throw new IllegalStateException("字符串加密失败", e);
        }
    }

    /**
     * 将content的内容以password的密钥解密
     *
     * @param content  被解密码的内容
     * @param password 密钥
     * @return 原始数据
     */
    public static String decrypt(String content, String password) {

        checkParam(content, password);

        try {
            SecretKeySpec e = getSecretKeySpec(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, e);
            byte[] result = cipher.doFinal(new Base64().decode(content));
            return new String(result, CommonsUtil.DEFAULT_ENCODING);
        } catch (Exception e) {
            LOGGER.warn("string:{} decrypt fail", content, e);
            throw new IllegalStateException("字符串解密失败", e);
        }
    }

    /**
     * 检查参数
     *
     * @param content  内容
     * @param password 密钥
     */
    private static void checkParam(String content, String password) {
        Preconditions.checkArgument(StringUtil.isNotBlank(content), "content is empty");
        Preconditions.checkArgument(StringUtil.isNotBlank(password), "password is empty");
    }

    private static SecretKeySpec getSecretKeySpec(String password) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(password.getBytes());
        keyGenerator.init(KEY_SIZE, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        return new SecretKeySpec(enCodeFormat, ALGORITHM);
    }
}
