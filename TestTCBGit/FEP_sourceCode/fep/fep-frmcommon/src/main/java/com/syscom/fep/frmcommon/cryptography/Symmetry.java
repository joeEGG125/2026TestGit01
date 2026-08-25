package com.syscom.fep.frmcommon.cryptography;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.syscom.fep.frmcommon.log.LogHelper;

public class Symmetry {
    private static final LogHelper logger = new LogHelper();

    private Symmetry() {
    }

    public static class Decrypt {
        private Decrypt() {
        }
        
        public static String decrypt(byte[] dataInput, byte[] secretKey, byte[] iv) throws Exception {
            try {
                SecretKeySpec skeySpec = new SecretKeySpec(secretKey, "AES");
                Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
                GCMParameterSpec ivparam = new GCMParameterSpec(128, iv, 0, 12);
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivparam);
                byte[] original = cipher.doFinal(dataInput);
                String originalString = new String(original, "utf-8");
                return originalString;
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static class Encrypt {
        private Encrypt() {
        }
        
        public static byte[] encrypt(String encData ,byte[] secretKey,byte[] iv) throws Exception {
            Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
            SecretKeySpec skeySpec = new SecretKeySpec(secretKey, "AES");
            GCMParameterSpec ivparam = new GCMParameterSpec(128, iv, 0, 12);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivparam);
            byte[] encrypted = cipher.doFinal(encData.getBytes("utf-8"));
            return encrypted;
        }
    }
}
