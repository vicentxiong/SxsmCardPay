package com.mwdev.sxsmcardpay.util;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by xiongxin on 16-8-13.
 */
public class PosUtil {

    private static final String HEXES = "0123456789ABCDEF";


    /*
     *字节数据转换成16进制字符串
     *
     */
    public static String byteArray2Hex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    /**
     * 16进制格式的字符串转成16进制byte 44 --> byte 0x44
     *
     * @param hexString
     * @return
     */
    public static byte[] HexStringToByteArray(String hexString) {//
        if (hexString == null || hexString.equals("")) {
            return new byte[] {};
        }
        if (hexString.length() == 1 || hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /*
     *生成pin 的密文
     * 参数
     * 1 pin密钥
     * 2 pin明文字符串
     * 3 pan主帐号
     *
     */
    public static byte[] createPinEncryption(String pin,String pan){
        byte[] decryption = new byte[8];
        byte[] bytePin = new byte[8];
        byte[] bytePan = new byte[8];
        Arrays.fill(bytePin, (byte) 0xFF);
        Arrays.fill(bytePan, (byte)0x00);

        byte[] srcPin = HexStringToByteArray(pin);
        byte[] srcPan = HexStringToByteArray(pan);

        bytePin[0] = 0x06;
        System.arraycopy(srcPin, 0, bytePin, 1, srcPin.length);
        System.arraycopy(srcPan, 2, bytePan, 2, srcPan.length-2);

        for (int i = 0; i < 8; i++) {
            decryption[i] = (byte) (bytePin[i]^bytePan[i]);
        }

        return decryption;
    }

    /*
     *计算mac值
     * 参数
     * 1 mac密钥
     * 2 需要计算的数据
     *
     */
    public static byte[] calMacCode(byte[] mak,byte[] src){
        int srcLen = src.length;
        int m_len  = srcLen%8;
        int count = srcLen + (m_len==0?m_len:(8-m_len));

        byte[] calData = new byte[count];
        byte[] _data8 = new byte[8];
        Arrays.fill(calData, (byte)0);
        Arrays.fill(_data8, (byte)0);
        System.arraycopy(src, 0, calData, 0, srcLen);
        System.arraycopy(calData, 0, _data8, 0, 8);

        for (int i = 1; i < count/8; i++) {
            for (int j = 0; j < 8; j++) {
                _data8[j] = (byte) (_data8[j]^calData[i*8+j]);
            }
        }

        return TriDesEncryption(mak, _data8);
    }

    /*
     *3DES ECB 加密
     *
     */
    public static byte[] TriDesEncryption(byte[] byteKey, byte[] dec) {
        try {
            byte[] en_key = new byte[24];
            if (byteKey.length == 16) {
                System.arraycopy(byteKey, 0, en_key, 0, 16);
                System.arraycopy(byteKey, 0, en_key, 16, 8);
            }
            SecretKeySpec key = new SecretKeySpec(en_key, "DESede");
            Cipher ecipher = Cipher.getInstance("DESede/ECB/NoPadding");
            ecipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] en_b = ecipher.doFinal(dec);
            return en_b;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     *3DES ECB 解密
     *
     */
    public static byte[] TriDesDecryption(byte[] byteKey, byte[] dec) {
        try {
            byte[] en_key = new byte[24];
            if (byteKey.length == 16) {
                System.arraycopy(byteKey, 0, en_key, 0, 16);
                System.arraycopy(byteKey, 0, en_key, 16, 8);
            }
            SecretKey key =  new SecretKeySpec(en_key, "DESede");
            Cipher dcipher = Cipher.getInstance("DESede/ECB/NoPadding");
            dcipher.init(Cipher.DECRYPT_MODE, key);

            byte[] de_b = dcipher.doFinal(dec);

            return de_b;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }
}
