package org.steven.chen.utils.encrypt;

import org.apache.commons.codec.binary.Base64;
import org.steven.chen.utils.CommonsUtil;

import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public final class RSAUtil {

    public static final String MD5_KEY_TYPE = "MD5";
    public static final String SHA_KEY_TYPE = "RSA";
    public static final String SHA256_KEY_TYPE = "RSA256";

    public static final String SIGN_ALGORITHMS_MD5 = "MD5WithRSA";
    public static final String SIGN_ALGORITHMS_SHA = "SHA1WithRSA";
    public static final String SIGN_ALGORITHMS_SHA256 = "SHA256WithRSA";

    private KeyPair keyPair;

    private RSAUtil(int keySize) {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(SHA_KEY_TYPE);
            keyPairGen.initialize(keySize);
            this.keyPair = keyPairGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static RSAUtil newInstance(int keySize) {
        return new RSAUtil(keySize);
    }

    public String getPrivateKey() {
        Key privateKey = keyPair.getPrivate();
        return Base64.encodeBase64String(privateKey.getEncoded());
    }

    public String getPublicKey() {
        Key publicKey = keyPair.getPublic();
        return Base64.encodeBase64String(publicKey.getEncoded());
    }

    private static Signature getSignature(String signType) throws NoSuchAlgorithmException {
        Signature signature;
        if (SHA256_KEY_TYPE.equals(signType)) {
            signature = Signature.getInstance(SIGN_ALGORITHMS_SHA256);
        } else if (SHA_KEY_TYPE.equals(signType)) {
            signature = Signature.getInstance(SIGN_ALGORITHMS_SHA);
        } else {
            signature = Signature.getInstance(SIGN_ALGORITHMS_MD5);
        }
        return signature;
    }

    public static boolean signPublicKey(String content, String publicKey, String sign, String signType) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(SHA_KEY_TYPE);
            byte[] encodedKey = Base64.decodeBase64(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
            Signature signature = getSignature(signType);
            signature.initVerify(pubKey);
            signature.update(content.getBytes(Charset.forName(CommonsUtil.SYSTEM_ENCODING)));
            return signature.verify(Base64.decodeBase64(sign));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String signPrivateKey(String content, String privateKey, String signType) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(SHA_KEY_TYPE);
            byte[] decodeKey = Base64.decodeBase64(privateKey);
            PrivateKey privateK = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodeKey));
            Signature signature = getSignature(signType);
            signature.initSign(privateK);
            signature.update(content.getBytes(Charset.forName(CommonsUtil.SYSTEM_ENCODING)));
            return Base64.encodeBase64String(signature.sign());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
