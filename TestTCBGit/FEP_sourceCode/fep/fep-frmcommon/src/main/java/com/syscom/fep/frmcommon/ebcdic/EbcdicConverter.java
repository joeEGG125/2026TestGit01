package com.syscom.fep.frmcommon.ebcdic;

import com.ibm.as400.access.*;
import com.syscom.fep.frmcommon.astarloadutils.AStarLoadUtils;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EbcdicConverter {
    private static final LogHelper logger = new LogHelper();
    public static final String[] ENCIRCLE_FOR_CHINESE = new String[] {"0E", "0F"};
    public static final String ENGLISH = "IBM037";
    public static final String TRADITIONAL_CHINESE = "IBM937";
    private static final byte EBCDIC_REPLACEMENT_BYTE_1 = (byte) 0x44; //★:0E 44E6 0F
    private static final byte EBCDIC_REPLACEMENT_BYTE_2 = (byte) 0xE6;
    private static final byte EBCDIC_SHIFT_OUT = (byte) 0x0E;
    private static final byte EBCDIC_SHIFT_IN = (byte) 0x0F;

    private EbcdicConverter() {}

    /**
     * IBM data to Java Short
     *
     * @param bytes
     * @return
     */
    public static short toShort(byte[] bytes) {
        AS400Bin2 bin2Converter = new AS400Bin2();
        short value = bin2Converter.toShort(bytes);
        return value;
    }

    /**
     * IBM data to Java Integer
     *
     * @param bytes
     * @return
     */
    public static int toInteger(byte[] bytes) {
        AS400Bin4 bin4Converter = new AS400Bin4();
        int value = bin4Converter.toInt(bytes);
        return value;
    }

    /**
     * IBM data to Java Float
     *
     * @param bytes
     * @return
     */
    public static float toFloat(byte[] bytes) {
        AS400Float4 float4Converter = new AS400Float4();
        float value = float4Converter.toFloat(bytes);
        return value;
    }

    /**
     * IBM data to Java Double
     *
     * @param bytes
     * @return
     */
    public static double toDouble(byte[] bytes) {
        AS400Float8 double8Converter = new AS400Float8();
        double value = double8Converter.toDouble(bytes);
        return value;
    }

    /**
     * IBM data to Java String
     *
     * @param ccsid
     * @param bytes
     * @return
     */
    public static String toString(CCSID ccsid, byte[] bytes) {
        AS400Text textConverter = new AS400Text(bytes.length, ccsid.getValue());
        String value = (String) textConverter.toObject(bytes);
        return value;
    }

    /**
     * IBM Hex String to Java String
     *
     * @param ccsid
     * @param hex
     * @return
     */
    public static String fromHex(CCSID ccsid, String hex) {
        byte[] bytes = ConvertUtil.hexToBytes(hex.toUpperCase());
        String value = toString(ccsid, bytes);
        return value;
    }

    /**
     * Java Short to IBM data
     *
     * @param value
     * @return
     */
    public static byte[] toBytes(short value) {
        AS400Bin2 bin2Converter = new AS400Bin2();
        byte[] bytes = bin2Converter.toBytes(value);
        return bytes;
    }

    /**
     * Java Integer to IBM data
     *
     * @param value
     * @return
     */
    public static byte[] toBytes(int value) {
        AS400Bin4 bin4Converter = new AS400Bin4();
        byte[] bytes = bin4Converter.toBytes(value);
        return bytes;
    }

    /**
     * Java Float to IBM data
     *
     * @param value
     * @return
     */
    public static byte[] toBytes(float value) {
        AS400Float4 float4Converter = new AS400Float4();
        byte[] bytes = float4Converter.toBytes(value);
        return bytes;
    }

    /**
     * Java Double to IBM data
     *
     * @param value
     * @return
     */
    public static byte[] toBytes(double value) {
        AS400Float8 double8Converter = new AS400Float8();
        byte[] bytes = double8Converter.toBytes(value);
        return bytes;
    }

    /**
     * Java String to IBM data
     *
     * @param length
     * @param ccsid
     * @param value
     * @return
     */
    public static byte[] toBytes(CCSID ccsid, int length, String value) {
        AS400Text textConverter = new AS400Text(length, ccsid.getValue());
        byte[] bytes = textConverter.toBytes(value);
        return bytes;
    }

    /**
     * Java String to IBM Hex String
     *
     * @param length
     * @param ccsid
     * @param value
     * @return
     */
    public static String toHex(CCSID ccsid, int length, String value) {
        byte[] bytes = toBytes(ccsid, length, value);
        String hex = ConvertUtil.toHex(bytes);
        return hex;
    }

    /**
     * Java String to IBM Hex String
     * <p>
     * 這個方法會自動判斷字串中是否含有中文, 如果有則以中文方式轉換, 並使用OE和OF前後包起來
     *
     * @param length
     * @param value
     * @return
     */
    public static String toHex(int length, String value) {
        return toHex(length, value, false);
    }

    /**
     * Java String to IBM Hex String
     * <p>
     * 這個方法會自動判斷字串中是否含有中文, 如果有則以中文方式轉換, 並使用OE和OF前後包起來
     *
     * @param length
     * @param value
     * @return
     */
    public static String toHex(int length, String value, boolean leftPadding) {
        return toHex(length, value, ENCIRCLE_FOR_CHINESE, leftPadding);
    }

    /**
     * Java String to IBM Hex String
     * <p>
     * 這個方法會自動判斷字串中是否含有中文, 如果有則以中文方式轉換, 並使用encircle[0]和encircle[1]前後包起來
     *
     * @param length
     * @param value
     * @param encircle
     * @param leftPadding
     * @return
     */
    public static String toHex(int length, String value, String[] encircle, boolean leftPadding) {
        int counted = 0;
        StringBuilder sb = new StringBuilder();
        List<String> list = StringUtil.splitByChinese(value);
        for (String str : list) {
            if (StringUtil.isChinese(str)) {
                byte[] bytes = toBytes(CCSID.Traditional_Chinese, str.length() * 2, str);
                counted += bytes.length;
                String hex = ConvertUtil.toHex(bytes);
                if (ArrayUtils.isNotEmpty(encircle) && encircle.length >= 2) {
                    hex = StringUtils.join(encircle[0], hex, encircle[1]);
                    counted += 2;
                }
                sb.append(hex);
            } else {
                sb.append(toHex(CCSID.English, str.length(), str));
                counted += str.length();
            }
        }
        // 位數不夠, 則右邊補齊空白
        if (counted < length) {
            String padding = StringUtils.repeat(StringUtils.SPACE, length - counted);
            if (leftPadding) {
                sb.insert(0, toHex(CCSID.English, padding.length(), padding));
            } else {
                sb.append(toHex(CCSID.English, padding.length(), padding));
            }
        }
        return sb.toString();
    }

    /**
     * IBM Hex String to Java Hex String
     *
     * @param ccsid
     * @param hex
     * @param charsets
     * @return
     */
    public static String iHexToJHex(CCSID ccsid, String hex, Charset... charsets) {
        String value = fromHex(ccsid, hex);
        byte[] bytes = null;
        if (ArrayUtils.isEmpty(charsets) || charsets[0] == null) {
            bytes = value.getBytes(StandardCharsets.UTF_8);
        } else {
            bytes = value.getBytes(charsets[0]);
        }
        return StringUtil.toHex(bytes);
    }

    /**
     * Java Hex String to IBM Hex String
     *
     * @param ccsid
     * @param length
     * @param hex
     * @param charsets
     * @return
     */
    public static String jHexToIHex(CCSID ccsid, int length, String hex, Charset... charsets) {
        byte[] bytes = ConvertUtil.hexToBytes(hex);
        String str = null;
        if (ArrayUtils.isEmpty(charsets) || charsets[0] == null) {
            str = ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
        } else {
            str = ConvertUtil.toString(bytes, charsets[0]);
        }
        return toHex(ccsid, length, str);
    }

    /**
     * IBM Whole Hex String to Java String With Traditional Chinese for Non-English and Encircle with "OE" "OF"
     *
     * @param hex
     * @return
     */
    public static String fromWholeHex(String hex) {
        return fromWholeHex(hex, CCSID.Traditional_Chinese, ENCIRCLE_FOR_CHINESE);
    }

    /**
     * IBM Whole Hex String to Java String Encircle with "OE" "OF"
     *
     * @param hex
     * @param nonEnglish
     * @return
     */
    public static String fromWholeHex(String hex, CCSID nonEnglish) {
        return fromWholeHex(hex, nonEnglish, ENCIRCLE_FOR_CHINESE);
    }

    /**
     * IBM Whole Hex String to Java String
     *
     * @param hex        IBM HEX String
     * @param nonEnglish 非英文部分需要轉換為那種語言
     * @param encircle   非英文部分在IBM HEX String中前後包裹的字符
     * @return
     */
    public static String fromWholeHex(String hex, CCSID nonEnglish, String[] encircle) {
        StringBuilder sb = new StringBuilder();
        // 每段非英數開始的位置, 也就是每段encircle[0]所在的位置
        int begin = hex.indexOf(encircle[0]);
        // 注意begin必須是偶數, 如果是奇數, 表示取得不對
        while (begin != -1 && begin % 2 != 0) {
            begin = hex.indexOf(encircle[0], begin + 1);
        }
        // 每段非英數終止的位置, 也就是每段encircle[1]所在的位置
        int end = -1;
        // 找完encircle最後的位置
        int last = 0;
        while (begin != -1) {
            end = hex.indexOf(encircle[1], begin);
            // 注意end必須是偶數, 如果是奇數, 表示取得不對
            while (end != -1 && end % 2 != 0) {
                end = hex.indexOf(encircle[1], end + 1);
            }
            if (end != -1) {
                // 先轉last和begin之間的英數的部分
                if (begin != 0) sb.append(fromHex(CCSID.English, hex.substring(last, begin)));
                // 再轉begin和end之間非英數的部分
                sb.append(fromHex(nonEnglish, hex.substring(begin + encircle[0].length(), end)));
                // 取出下一個begin
                begin = hex.indexOf(encircle[0], end + encircle[1].length());
                // 注意begin必須是偶數, 如果是奇數, 表示取得不對
                while (begin != -1 && begin % 2 != 0) {
                    begin = hex.indexOf(encircle[0], begin + 1);
                }
                // 確定last
                last = end + encircle[1].length();
            } else {
                break;
            }
        }
        // 如last為0, 表示沒有非英數的部分, 則直接全部轉
        if (last == 0) {
            return fromHex(CCSID.English, hex);
        }
        // 最後將剩餘的英數部分轉換
        else if (last != hex.length()) {
            sb.append(fromHex(CCSID.English, hex.substring(last)));
        }
        return sb.toString();
    }

    /**
     * IBM Whole Hex String to Java Hex String With Traditional Chinese for Non-English and Encircle with "OE" "OF"
     *
     * @param hex
     * @return
     */
    public static String iWholeHexToJHex(String hex) {
        return iWholeHexToJHex(hex, CCSID.Traditional_Chinese, ENCIRCLE_FOR_CHINESE);
    }

    /**
     * IBM Whole Hex String to Java Hex String Encircle with "OE" "OF"
     *
     * @param hex
     * @param nonEnglish
     * @return
     */
    public static String iWholeHexToJHex(String hex, CCSID nonEnglish) {
        return iWholeHexToJHex(hex, nonEnglish, ENCIRCLE_FOR_CHINESE);
    }

    /**
     * IBM Whole Hex String to Java Hex String
     *
     * @param hex
     * @param nonEnglish
     * @param encircle
     * @param charsets
     * @return
     */
    public static String iWholeHexToJHex(String hex, CCSID nonEnglish, String[] encircle, Charset... charsets) {
        String value = fromWholeHex(hex, nonEnglish, encircle);
        byte[] bytes = null;
        if (ArrayUtils.isEmpty(charsets) || charsets[0] == null) {
            bytes = value.getBytes(StandardCharsets.UTF_8);
        } else {
            bytes = value.getBytes(charsets[0]);
        }
        return StringUtil.toHex(bytes);
    }

    /**
     * 判斷EDBIC字串中含有 0E開頭0F結尾的字串 如有找到回傳中文 如找不到EDBIC轉後返回
     *
     * @param str 要轉的參數(EDBIC)
     * @return
     */
    public static String changeChinese(String str) {
        String result = "";
        try {
            String regexStart = ENCIRCLE_FOR_CHINESE[0];
            int maxLength = str.length();
            int sindex = str.indexOf(regexStart);
            //判斷 0E 若從中間開始，則需為偶數的位址
            if (sindex == 0 || (sindex > 0 && (sindex % 2 == 0))) {
                int eIndex = -1;
                String _str = str;
                for (int i = 0; i < maxLength; i++) {
                    eIndex = getEndChineseKey(_str);
                    if (eIndex < 0) {
                        break;
                    }
                    if (eIndex % 2 == 0) {
                        break;
                    } else {
                        _str = str.substring(sindex + 2, eIndex);
                    }
                }
                if (eIndex > 0) {
                    _str = str.substring(sindex + 2, eIndex);
                    //判斷不為最開頭的處理
                    if (sindex > 0) {
                        result = result.concat(EbcdicConverter.fromHex(CCSID.English, str.substring(0, sindex)));
                    }
                    result = result.concat(EbcdicConverter.fromHex(CCSID.Traditional_Chinese, _str));
                    //判斷不是最尾端的處理
                    if (eIndex + 2 != str.length()) {
                        result = result.concat(EbcdicConverter.fromHex(CCSID.English, str.substring(eIndex + 2)));
                    }
                } else {
                    result = EbcdicConverter.fromHex(CCSID.English, str);
                }
            } else {
                result = EbcdicConverter.fromHex(CCSID.English, str);
            }
        } catch (Exception e) {
            result = EbcdicConverter.fromHex(CCSID.English, str);
            logger.error(e, e.getMessage());
        }
        return result;
    }

    /**
     * 回傳字串中是否有0F存在，沒有就回傳-1
     *
     * @param str
     * @return
     */
    private static int getEndChineseKey(String str) {
        int result = -1;
        String regexEnd = ENCIRCLE_FOR_CHINESE[1];
        int eIndex = str.lastIndexOf(regexEnd);
        if (eIndex > 0) {
            return eIndex;
        }
        return result;
    }

    /**
     * 英數字串ASCII轉EBCDIC
     *
     * @param ascii
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String toHex(String ascii) throws UnsupportedEncodingException {
        return StringUtil.toHex(ascii.getBytes(ENGLISH));
    }

    /**
     * 英數字節ASCII轉EBCDIC
     *
     * @param ascii
     * @return
     * @throws UnsupportedEncodingException
     */
    public static byte[] toBytes(byte[] ascii) throws UnsupportedEncodingException {
        return ConvertUtil.toBytes(ascii, StandardCharsets.US_ASCII.name(), ENGLISH);
    }

    /**
     * 英數繁中字串ASCII轉EBCDIC, 注意這個方法轉出的EBCDIC中文的部分前後會有OE OF包圍
     *
     * @param ascii
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String toTraditionalChineseHex(String ascii) throws UnsupportedEncodingException {
        return StringUtil.toHex(ascii.getBytes(TRADITIONAL_CHINESE));
    }

    /**
     * 通用方法：將 ASCII/Unicode 字串轉換為 IBM937 Hex 字串，
     * 且確保 0F 緊跟在 DBCS 序列之後，不會被 6F 替換字元隔開。
     * * @param ascii 待轉換的字串
     * @return IBM937 編碼的 Hex 字串
     * @throws UnsupportedEncodingException
     */
    public static String toTraditionalChineseHexGeneral(String ascii) throws UnsupportedEncodingException {
        Charset ibm937 = Charset.forName(TRADITIONAL_CHINESE); //IBM937
        CharsetEncoder encoder = ibm937.newEncoder();

        // 1. 設定錯誤處理：遇到無法編碼的字元時，我們不讓編碼器自動替換，而是 REPORT，這樣我們才能捕捉到錯誤並手動插入 44E6。
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        encoder.onMalformedInput(CodingErrorAction.REPORT);

        List<Byte> resultBytes = new ArrayList<>();
        boolean inDbcsMode = false; //是否處於DBCS模式

        for (int i = 0; i < ascii.length(); i++) {
            char currentChar = ascii.charAt(i);
            String charStr = String.valueOf(currentChar);

            try {
                // 嘗試使用編碼器轉換單一字元
                ByteBuffer encodedBuffer = encoder.encode(java.nio.CharBuffer.wrap(charStr));
                byte[] encodedBytes = new byte[encodedBuffer.remaining()];
                encodedBuffer.get(encodedBytes);

                // 判斷位元組長度
                int len = encodedBytes.length;

                if (len == 1) {
                    // --- 狀況 A: 單一位元組 (SBCS) ---

                    // 如果目前處於 DBCS 模式，必須先發送 0F 移出
                    if (inDbcsMode) {
                        resultBytes.add(EBCDIC_SHIFT_IN); // 0F
                        inDbcsMode = false;
                    }
                    // 寫入 SBCS 字元
                    resultBytes.add(encodedBytes[0]);

                } else if (len == 2) {
                    // --- 狀況 B: 雙位元組 (DBCS) ---

                    // DBCS 字元在 IBM937 中通常是兩個位元組 (例如 42D6)
                    // 如果目前不是 DBCS 模式，必須先發送 0E 移入
                    if (!inDbcsMode) {
                        resultBytes.add(EBCDIC_SHIFT_OUT); // 0E
                        inDbcsMode = true;
                    }
                    // 寫入 DBCS 位元組
                    resultBytes.add(encodedBytes[0]);
                    resultBytes.add(encodedBytes[1]);

                } else if (len > 2 && encodedBytes[0] == EBCDIC_SHIFT_OUT && encodedBytes[len - 1] == EBCDIC_SHIFT_IN) {
                    // --- 狀況 C: 編碼器將單一字元編碼為 0E...0F (這在逐字元處理時不應該發生，但作為防禦性編程)
                    // 如果發生，我們只取出中間的 DBCS 內容
                    if (!inDbcsMode) {
                        resultBytes.add(EBCDIC_SHIFT_OUT); // 0E
                        inDbcsMode = true;
                    }
                    for (int j = 1; j < len - 1; j++) {
                        resultBytes.add(encodedBytes[j]);
                    }
                } else {
                    // --- 狀況 D: 位元組長度不為 1 或 2，且不是標準 0E...0F 格式 (異常情況)

                    // 如果目前不是處於 DBCS 模式，必須先發送 0E 移出
                    if (!inDbcsMode) {
                        resultBytes.add(EBCDIC_SHIFT_OUT); // 0E
                        inDbcsMode = true;
                    }
                    // 寫入 44E6 替換字元
                    resultBytes.add(EBCDIC_REPLACEMENT_BYTE_1); // 44
                    resultBytes.add(EBCDIC_REPLACEMENT_BYTE_2); // E6
                }

            } catch (Exception e) {
                // --- 狀況 E: 編碼失敗 (無法對應的字元，即您要替換成 6F 的情況) ---

                // 如果目前不是 DBCS 模式，必須先發送 0E 移出
                if (!inDbcsMode) {
                    resultBytes.add(EBCDIC_SHIFT_OUT); // 0E
                    inDbcsMode = true;
                }
                // 寫入 44E6 替換字元
                resultBytes.add(EBCDIC_REPLACEMENT_BYTE_1); // 44
                resultBytes.add(EBCDIC_REPLACEMENT_BYTE_2); // E6
            }
        } // 迴圈結束

        // 處理字串末尾：如果最後停留在 DBCS 模式，需要發送 0F 移出
        if (inDbcsMode) {
            resultBytes.add(EBCDIC_SHIFT_IN); // 0F
        }

        // 將 List<Byte> 轉換為 byte[]
        byte[] finalBytes = new byte[resultBytes.size()];
        for (int i = 0; i < resultBytes.size(); i++) {
            finalBytes[i] = resultBytes.get(i);
        }

        return StringUtil.toHex(finalBytes);
    }
    /**通用方法：將 ASCII/Unicode 字串轉換為 IBM937 Hex 字串，
     * 且確保 0F 緊跟在 DBCS 序列之後，不會被 6F 替換字元隔開，不足電文規格的長度補空白的Hex 40
     * @param oriString 待轉換的字串
     * @param fullLength 電文規格的長度
     * @return IBM937 編碼的 Hex 字串，不足電文規格的長度補空白的Hex 40
     * @throws UnsupportedEncodingException
     */
    public static String toTraditionalChineseHexbyZibaFillSpace(String oriString, int fullLength) throws Exception {
        // 直接轉成 IBM937 Byte 陣列，這是底層數據的真實樣貌
        byte[] ebcdicBytes = AStarLoadUtils.convertUnicodeStrToIBM937Bytes(oriString);
        int byteLimit = fullLength / 2; // 因為回傳的是 Hex String，所以 Byte 長度減半
        byte[] resultBytes;

        try {
            if (ebcdicBytes.length > byteLimit) {
                resultBytes = new byte[byteLimit];
                System.arraycopy(ebcdicBytes, 0, resultBytes, 0, byteLimit);
                // 檢查狀態：如果在截斷點還有未閉合的 0E (Shift-Out)
                boolean isOpen = false;
                for (int i = 0; i < byteLimit; i++) {
                    if (resultBytes[i] == 0x0E) isOpen = true;
                    if (resultBytes[i] == 0x0F) isOpen = false;
                }
                if (isOpen) {
                    // 最後一個字沒閉合會導致亂碼，強制補上 0F，並處理前一個 Byte 避免中斷半個中文字
                    resultBytes[byteLimit - 1] = 0x0F;
                }
            } else {
                resultBytes = ebcdicBytes;
            }
            // 轉 Hex 加上右側補空白 (EBCDIC 的空白是 0x40)
            StringBuilder hexBuilder = new StringBuilder();
            for (byte b : resultBytes) hexBuilder.append(String.format("%02X", b));

            return StringUtils.rightPad(hexBuilder.toString(), fullLength, "40");

        }catch (Exception e) {
            logger.error(e, e.getMessage());
            return StringUtils.rightPad("", fullLength, "40");
        }
    }

    /**
     * 英數繁中字節ASCII轉EBCDIC, 注意這個方法轉出的EBCDIC中文的部分前後會有OE OF包圍
     *
     * @param ascii
     * @return
     */
    public static byte[] toTraditionalChineseBytes(byte[] ascii) {
        return ConvertUtil.toBytes(ascii, StandardCharsets.US_ASCII.name(), TRADITIONAL_CHINESE);
    }

    /**
     * 英數字串EBCDIC轉ASCII
     *
     * @param ebcdic
     * @return
     * @throws DecoderException
     */
    public static String fromHex(String ebcdic) throws DecoderException {
        return StringUtil.fromHex(ebcdic, Charset.forName(ENGLISH), true);
    }

    /**
     * 英數字節EBCDIC轉ASCII
     *
     * @param ebcdic
     * @return
     */
    public static byte[] fromBytes(byte[] ebcdic) {
        return ConvertUtil.toBytes(ebcdic, ENGLISH, StandardCharsets.US_ASCII.name());
    }

    /**
     * 英數繁中字串EBCDIC轉ASCII, 注意這個方法待轉出的EBCDIC中文的部分前後會有OE OF包圍
     *
     * @param ebcdic
     * @return
     * @throws DecoderException
     */
    public static String fromTraditionalChineseHex(String ebcdic) throws DecoderException {
        return StringUtil.fromHex(ebcdic, Charset.forName(TRADITIONAL_CHINESE), true);
    }

    /**
     * 英數繁中字節EBCDIC轉ASCII, 注意這個方法待轉出的EBCDIC中文的部分前後會有OE OF包圍
     *
     * @param ebcdic
     * @return
     */
    public static byte[] fromTraditionalChineseBytes(byte[] ebcdic) {
        return ConvertUtil.toBytes(ebcdic, TRADITIONAL_CHINESE, StandardCharsets.US_ASCII.name());
    }

    /**
     * 查找mark位置 如果是中英混合, 中文的部分會用0E-0F包裹起來, 則找出對應的位置 int[] {flag, begin, end},
     * flag為0表示英數字, flag為1表示中文
     *
     * @param ebcdic
     * @return
     */
    public static List<int[]> foundMarkPosition(String ebcdic) {
        return foundMarkPosition(ebcdic, ENCIRCLE_FOR_CHINESE);
    }

    /**
     * 查找mark位置 如果是中英混合, 中文的部分會用encircle[0]-encircle[1]包裹起來, 則找出對應的位置 int[] {flag, begin, end}, flag為0表示英數字, flag為1表示中文
     *
     * @param ebcdic
     * @param encircle
     * @return
     */
    public static List<int[]> foundMarkPosition(String ebcdic, String[] encircle) {
        List<int[]> list = new ArrayList<>();
        if (StringUtils.isBlank(ebcdic)) return list;
        // 每段非英數開始的位置, 也就是每段encircle[0]所在的位置
        int begin = ebcdic.indexOf(encircle[0]);
        // 注意begin必須是偶數, 如果是奇數, 表示取得不對
        while (begin != -1 && begin % 2 != 0) {
            begin = ebcdic.indexOf(encircle[0], begin + 1);
        }
        // 每段非英數終止的位置, 也就是每段encircle[1]所在的位置
        int end = -1;
        // 找完encircle最後的位置
        int last = 0;
        while (begin != -1) {
            end = ebcdic.indexOf(encircle[1], begin);
            // 注意end必須是偶數, 如果是奇數, 表示取得不對
            while (end != -1 && end % 2 != 0) {
                end = ebcdic.indexOf(encircle[1], end + 1);
            }
            if (end != -1) {
                // 先轉last和begin之間的英數的部分
                if (begin != 0)
                    list.add(new int[] {0, last, begin});
                // 再轉begin和end之間非英數的部分
                list.add(new int[] {1, begin, end + encircle[1].length()});
                // 取出下一個begin
                begin = ebcdic.indexOf(encircle[0], end + encircle[1].length());
                // 注意begin必須是偶數, 如果是奇數, 表示取得不對
                while (begin != -1 && begin % 2 != 0) {
                    begin = ebcdic.indexOf(encircle[0], begin + 1);
                }
                // 確定last
                last = end + encircle[1].length();
            } else {
                break;
            }
        }
        // 如last為0, 表示沒有非英數的部分, 則直接全部轉
        if (last == 0) {
            list.add(new int[] {0, 0, ebcdic.length()});
        }
        // 最後將剩餘的英數部分轉換
        else if (last != ebcdic.length()) {
            list.add(new int[] {0, last, ebcdic.length()});
        }
        return list;
    }
}
