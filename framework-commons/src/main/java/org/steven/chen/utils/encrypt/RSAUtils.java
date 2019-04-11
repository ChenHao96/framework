package org.steven.chen.utils.encrypt;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RSAUtils.class);
    /**
     * 字符集
     */
    private static final String ENCODING = "utf-8";
    /**
     * 加密算法
     */
    public static final String KEY_ALGORITHM = "RSA";
    /**
     * 数字签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";
    /**
     * 密钥长度
     */
    private static final int KEY_SIZE = 1024;
    /**
     * 密钥对
     */
    private KeyPair keyPair;

    /**
     * 初始化密钥
     */
    public RSAUtils() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGen.initialize(KEY_SIZE);
            keyPair = keyPairGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("init RSAUtils fail", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 取得私钥
     *
     * @return 私钥串
     */
    public String getPrivateKey() {
        Key privateKey = keyPair.getPrivate();
        return Base64.encodeBase64String(privateKey.getEncoded());
    }

    /**
     * 取得公钥
     *
     * @return 公钥串
     */
    public String getPublicKey() {
        Key publicKey = keyPair.getPublic();
        return Base64.encodeBase64String(publicKey.getEncoded());
    }

    private static PrivateKey getPrivateKey(String key) {
        // 对密钥解密
        byte[] keyBytes = Base64.decodeBase64(key);

        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

            return keyFactory.generatePrivate(pkcs8KeySpec);
        } catch (Exception e) {
            LOGGER.warn("getPrivateKey: key:{} fail", key, e);
            throw new RuntimeException(e);
        }
    }

    private static byte[] getResultByPrivateKey(byte[] data, String key, int model) {
        PrivateKey privateKey = getPrivateKey(key);
        try {
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(model, privateKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            LOGGER.warn("getResultByPrivateKey: data:{},key:{},model:{} fail", data, key, model, e);
            throw new RuntimeException(e);
        }
    }

    private static PublicKey getPublicKey(String key) {
        // 对密钥解密
        byte[] keyBytes = Base64.decodeBase64(key);

        // 取得公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            return keyFactory.generatePublic(x509KeySpec);
        } catch (Exception e) {
            LOGGER.warn("getPublicKey: key:{} fail", key, e);
            throw new RuntimeException(e);
        }
    }

    private static byte[] getResultByPublicKey(byte[] data, String key, int model) {

        // 取公钥匙对象
        PublicKey publicKey = getPublicKey(key);
        try {
            // 对数据加密
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(model, publicKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            LOGGER.warn("getResultByPublicKey: data:{},key:{},model:{} fail", data, key, model, e);
            throw new RuntimeException(e);
        }
    }

    private static void checkParam(String content, String key) {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "key is empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(content), "content is empty");
    }



    /**
     * 解密<br>
     * 用私钥解密
     *
     * @param content    待解密内容
     * @param privateKey 私钥
     * @return 结果
     */
    public static String decryptByPrivateKey(String content, String privateKey) throws Exception {
        checkParam(content, privateKey);
        byte[] data = new Base64().decode(content);
        data = getResultByPrivateKey(data, privateKey, Cipher.DECRYPT_MODE);
        return new String(data, ENCODING);
    }

    /**
     * 加密<br>
     * 用私钥加密
     *
     * @param content    待加密内容
     * @param privateKey 私钥
     * @return 结果
     */
    public static String encryptByPrivateKey(String content, String privateKey) throws Exception {
        checkParam(content, privateKey);
        byte[] data = content.getBytes(ENCODING);
        data = getResultByPrivateKey(data, privateKey, Cipher.ENCRYPT_MODE);
        return new Base64().encodeToString(data);
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param content    加密数据
     * @param privateKey 私钥
     * @return 数字签名串
     */
    public static String sign(String content, String privateKey) {

        PrivateKey key = getPrivateKey(privateKey);
        try {
            // 用私钥对信息生成数字签名
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(key);
            signature.update(content.getBytes());

            //生成数字签名
            return Base64.encodeBase64String(signature.sign());
        } catch (Exception e) {
            LOGGER.warn("sign: content:{},privateKey:{} fail", content, privateKey, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 解密<br>
     * 用公钥解密
     *
     * @param content   待解密数据
     * @param publicKey 公钥
     * @return 结果
     */
    public static String decryptByPublicKey(String content, String publicKey) throws Exception {
        checkParam(content, publicKey);
        byte[] data = new Base64().decode(content);
        data = getResultByPublicKey(data, publicKey, Cipher.DECRYPT_MODE);
        return new String(data, ENCODING);
    }

    /**
     * 加密<br>
     * 用公钥加密
     *
     * @param content   待加密数据
     * @param publicKey 公钥
     * @return 结果
     */
    public static String encryptByPublicKey(String content, String publicKey) throws Exception {
        checkParam(content, publicKey);
        byte[] data = content.getBytes(ENCODING);
        data = getResultByPublicKey(data, publicKey, Cipher.ENCRYPT_MODE);
        return new Base64().encodeToString(data);
    }

    /**
     * 校验数字签名
     *
     * @param content   加密数据
     * @param sign      数字签名
     * @param publicKey 公钥
     * @return 校验成功返回true 失败返回false
     */
    public static boolean verify(String content, String sign, String publicKey) {

        // 取公钥匙对象
        PublicKey pubKey = getPublicKey(publicKey);
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(pubKey);
            signature.update(content.getBytes());

            // 验证签名是否正常
            return signature.verify(Base64.decodeBase64(sign));
        } catch (Exception e) {
            LOGGER.warn("verify: content:{},sign:{},publicKey:{} fail", content, sign, publicKey, e);
            throw new RuntimeException(e);
        }
    }
}
