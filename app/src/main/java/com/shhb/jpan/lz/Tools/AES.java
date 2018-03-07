package com.shhb.jpan.lz.Tools;

import java.security.MessageDigest;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Moon on 2016/4/5.
 */
public class AES {
    /**
     * 获取10位随机数
     * @return
     */
    public static long get10Random() {
        Random random = new Random();
        String result = "";
        for (int i = 0; i < 10; i++) {
            result += (random.nextInt(10));
        }
        String resultFirst = result.substring(0, 1);
        String resultSecond = result.substring(1, result.length());
        if (resultFirst.contains("0")) {
            resultFirst = "1";
            result = resultFirst + resultSecond;
        }
        long poor = (Long.parseLong(result.trim()));
        return poor;
    }

    /**
     * 将生成的10随机数减2
     * @param random
     * @return
     */
    public static String longMinusNum(String random){
        long poor = (Long.parseLong(random.trim())) - 2;
        return poor+"";
    }

    /**
     * 16位小md5加密
     *
     * @return md5(value) or ""
     */
    public static String md5(String val) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(val.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            result = buf.substring(8,8+16);
//            System.out.println("MD5(" + val + ",32) = " + result);
//            System.out.println("MD5(" + val + ",16) = " + buf.toString().substring(8, 24));
        } catch (Exception e) {
            System.out.println(e);
        }
        return result;
    }

    /**
     * 加密
     *
     * @param content key
     * @return
     */
    public static String encrypt(String content,String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();

            byte[] dataBytes = content.getBytes();
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }
            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(key.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);
            return MyBase64.encode(encrypted);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }

    /**
     * 解密
     *
     * @param data
     * @return
     */
    public static String decrypt(String data, String key) {
        try {
            String text = MyBase64.decode(data.getBytes());
            byte[] by = convertTobyte(text);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(key.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            byte[] original = cipher.doFinal(by);
            String originalString = new String(original);
            return originalString.trim();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }

    /**
     * @param data
     * @return
     */
    private static byte[] convertTobyte(String data) {
        int maxLength = data.length();
        byte[] by = new byte[maxLength];
        char[] chars = data.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            by[i] = (byte) chars[i];
        }
        return by;
    }

}
