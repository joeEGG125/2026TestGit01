package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.Charset;

public class ConvertUtil {
    private static final String HEX_STRING = "0123456789ABCDEF";

    private ConvertUtil() {}

    /**
     * 十六進制字串轉字節數組
     * 2024-09-11 Richard modified for 【Missing HSTS Header】
     *
     * @param hexString
     * @return
     */
    public static byte[] hexToBytes(String hexString) {
        // try (ByteArrayOutputStream baos = new ByteArrayOutputStream(hexString.length() / 2)) {
        //     for (int i = 0; i < hexString.length(); i += 2) {
        //         baos.write((HEX_STRING.indexOf(hexString.charAt(i)) << 4 | HEX_STRING.indexOf(hexString.charAt(i + 1))));
        //     }
        //     return baos.toByteArray();
        // } catch (Throwable t) {
        //     LogHelper.getAdditionalLogger().warn("[hexToBytes]", t.getMessage());
        //     return new byte[0];
        // }
        try {
            return Hex.decodeHex(hexString);
        } catch (DecoderException e) {
            LogHelper.getAdditionalLogger().warn("[hexToBytes]", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * 將字節數組由源編碼轉為目標編碼
     *
     * @param bytes
     * @param srcCharsetName
     * @param dstCharsetName
     * @return
     */
    public static byte[] toBytes(byte[] bytes, String srcCharsetName, String dstCharsetName) {
        return new String(bytes, Charset.forName(srcCharsetName)).getBytes(Charset.forName(dstCharsetName));
    }

    /**
     * 將字節數組由源編碼轉為目標編碼, 並限制最大長度
     *
     * @param bytes
     * @param srcCharsetName
     * @param dstCharsetName
     * @param maxLength
     * @return
     */
    public static byte[] toBytes(byte[] bytes, String srcCharsetName, String dstCharsetName, int maxLength) {
        if (maxLength < 0) {
            return new String(bytes, Charset.forName(srcCharsetName)).getBytes(Charset.forName(dstCharsetName));
        } else if (maxLength == 0) {
            return new byte[0];
        }
        byte[] result = new byte[0];
        String tmp = new String(bytes, Charset.forName(srcCharsetName));
        for (int i = 0; i < tmp.length(); i++) {
            char ch = tmp.charAt(i);
            byte[] b = String.valueOf(ch).getBytes(Charset.forName(dstCharsetName));
            if (result.length + b.length <= maxLength) {
                result = ArrayUtils.addAll(result, b);
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * 字節數字轉十六進制
     *
     * @param bytes
     * @return
     */
    public static String toHex(byte[] bytes) {
        return toHex(bytes, 0, bytes.length);
    }

    public static String asciiToHex(String ascii) {
        return asciiToHex(ascii, true);
    }

    public static String asciiToHex(String ascii, boolean toUpperCase) {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < ascii.length(); i++) {
            if (toUpperCase)
                hex.append(Integer.toHexString(ascii.charAt(i)).toUpperCase());
            else
                hex.append(Integer.toHexString(ascii.charAt(i)));
        }
        return hex.toString();
    }

    /**
     * 字節數組轉十六進制字串
     *
     * @param bytes
     * @param offset
     * @param length
     * @return
     */
    public static String toHex(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        // 2023-05-30 Richard modified
        // 修正Unchecked Input for Loop Condition
        // if (offset + length >= bytes.length) {
        int MAX_LOOPS = bytes.length - offset;
        if (length >= MAX_LOOPS) {
            length = MAX_LOOPS;
        }
        for (int i = 0; i < length; i++) {
            sb.append(toHex(bytes[i + offset]));
        }
        return sb.toString();
    }

    /**
     * 字節轉十六進制字串
     *
     * @param b
     * @return
     */
    public static String toHex(byte b) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(HEX_STRING.charAt(0xf & b >> 4)));
        sb.append(String.valueOf(HEX_STRING.charAt(b & 0xf)));
        return sb.toString();
    }

    /**
     * 十六進制字串轉字節
     *
     * @param hexString
     * @return
     */
    public static byte hexToByte(String hexString) {
        return Byte.parseByte(hexString, 0x10);
    }

    /**
     * 十六進制int型字串轉字節
     *
     * @param hexInt
     * @return
     */
    public static byte intToByte(String hexInt) {
        return (byte) Integer.parseInt(hexInt, 16);
    }

    /**
     * 將字節數組轉為目標編碼的字串
     *
     * @param bytes
     * @param charsetName
     * @return
     */
    public static String toString(byte[] bytes, String charsetName) {
        return toString(bytes, Charset.forName(charsetName));
    }

    public static String toString(byte[] bytes, int offset, int length, String charsetName) {
        return new String(bytes, offset, length, Charset.forName(charsetName));
    }

    public static String toString(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }

    public static String toString(byte[] bytes, int offset, int length, Charset charset) {
        return new String(bytes, offset, length, charset);
    }

    /**
     * 將字節數組轉為目標編碼的字串
     *
     * @param str
     * @param charsetName
     * @return
     */
    public static byte[] toBytes(String str, String charsetName) {
        return toBytes(str, Charset.forName(charsetName));
    }

    public static byte[] toBytes(String str, Charset charset) {
        return str.getBytes(charset);
    }

    // 將HEX字串轉換為ASCII字串
    public static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder();
        // 每2個字符代表1個HEX數字
        for (int i = 0; i < hexStr.length(); i += 2) {
            // 確保不會超出範圍
            if (i + 2 > hexStr.length()) {
                break;
            }
            // 從HEX字符串中擷取2個字符轉換成10進位的整數
            String str = hexStr.substring(i, i + 2);
            if (str.matches("[0-9A-Fa-f]+")) {//檢查是否為合法的16位元
                int decimal = Integer.parseInt(str, 16);
                // 將10進位的數字轉換成對應的ASCII字符
                output.append((char) decimal);
            }
        }
        return output.toString();
    }
}
