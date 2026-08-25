package com.syscom.fep.frmcommon.astarloadutils;

import com.AStar.TBConvert.Big5.ConvertBig5_UCS2;
import com.AStar.TBConvert.CNS.ConvertCNS_NHC;
import com.AStar.TBConvert.CNS.ConvertCNS_UCS2;
import com.AStar.TBConvert.Customize.TBConvertTCB;
import com.AStar.TBConvert.NHC.ConvertNHC_UCS2;
import com.AStar.TBConvert.UCS2.ConvertUCS2_CNS;
import com.AStar.TBConvert.UCS2.ConvertUCS2_NHC;
import com.AStar.TBConvert.UTF8.UTF8WebValidate;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.IOUtil;

import java.util.Objects;
//import com.tcb.twnb.util.ConfigureManager;

/**
 * @author AnsonTsai
 * @version 1.0, Jun 12, 2012
 * @ClassName: com.tcb.twnb.web.util.AStarLoadUtils
 * @Description: 滿天星工具類別.
 * @Copyright : Copyright (c) IBM Corp. 2012. All Rights Reserved.
 * @Company: IBM GBS Team.
 */
public class AStarLoadUtils {
    private static final LogHelper logger = new LogHelper();

    private static boolean isloaded;

    static {
        // isloaded = false;
        long nRet = 0;

        // 設定檔路徑
        // String configPath = "D:\\FEPSystem\\TCBFEP\\source\\fep\\fep-config\\src\\main\\resources\\TBConvert.conf";
        try {
            String configPath = Objects.requireNonNull(IOUtil.openFile("TBConvert.conf")).getAbsolutePath();
            //ConfigureManager config = new ConfigureManager();
            //nRet = TBConvertTCB.init(config.getAStarConfig());
            nRet = TBConvertTCB.init(configPath);
            if (nRet == TBConvertTCB.Result_Succeed) {
                isloaded = true;
            } else {
                logger.debug("TBConvertTCB.init fail as [" + nRet + "]");
            }
        } catch (Exception e) {
            logger.error(e, e.getMessage());
        }
    }

    /**
     * 若字霸有轉失敗會改★代替
     **/
    public static byte[] convertUnicodeStrToIBM937Bytes(String data) {
        // 預定義失敗回傳值：★ (IBM937: 0E 44E6 0F)
        final byte[] EBCDIC_STAR = new byte[] { 0x0E, 0x44, (byte)0xE6, 0x0F };

        try {
            validateTNSCode();
            //1. 先嘗試整串轉
            char[] chs = new char[data.length()];
            data.getChars(0, data.length(), chs, 0);

            ConvertUCS2_NHC converter = new ConvertUCS2_NHC();
            long nRet = converter.convert(null, chs, chs.length);

            //2. 若轉成功沒問題就直接return
            if (nRet == 0) {
                return converter.getResult();
    //            logger.debug("字霸轉換失敗！errCode = ConvertUCS2_NHC.convert() = [" + nRet + "]");
    //            throw new Exception("字霸轉換失敗！errCode = ConvertUCS2_NHC.convert() = [" + nRet + "]");
            }

            //3. 若遇到有轉失敗就進行逐字轉，無法轉的字體以★代替(0E 44E6 0F)
            logger.debug("字霸轉換失敗！errCode = ConvertUCS2_NHC.convert() = [" + nRet + "]");

            java.util.ArrayList<Byte> resultList = new java.util.ArrayList<>();
            boolean inDbcsMode = false; // 追蹤是否在雙位元組模式

            for (int i = 0; i < data.length(); i++) {
                char ch = data.charAt(i);
                char[] singleChar = new char[]{ch};

                ConvertUCS2_NHC singleConverter = new ConvertUCS2_NHC();
                long singleRet = singleConverter.convert(null, singleChar, 1);

                if (singleRet != 0) {
                    // 轉換失敗，使用星號 (0x44E6 - 雙位元組)
                    logger.debug("字元 [" + ch + "] (Unicode: 0x" +
                            String.format("%04X", (int)ch) +
                            ") 轉換失敗 (errCode=" + singleRet + ")，改用星號替代");

                    // 星號是雙位元組，需要確保在 DBCS 模式中
                    if (!inDbcsMode) {
                        resultList.add((byte)0x0E); // Shift-In
                        inDbcsMode = true;
                    }
                    resultList.add((byte)0x44);
                    resultList.add((byte)0xE6);
                } else {
                    byte[] converted = singleConverter.getResult();

                    // 根據長度判斷字元類型
                    // 雙位元組: 0x0E + 2 bytes + 0x0F = 4 bytes
                    // 單位元組: 1 byte
                    if (converted.length == 4) {
                        // 雙位元組字元：需要進入 DBCS 模式
                        if (!inDbcsMode) {
                            resultList.add((byte)0x0E);
                            inDbcsMode = true;
                        }
                        // 只加入實際資料 (跳過第一個 0x0E 和最後一個 0x0F)
                        for (int j = 1; j < converted.length - 1; j++) {
                            resultList.add(converted[j]);
                        }
                    } else if(converted.length == 1) {
                        // 單位元組字元：需要離開 DBCS 模式
                        if (inDbcsMode) {
                            resultList.add((byte)0x0F);
                            inDbcsMode = false;
                        }
                        // 加入實際資料
                        for (byte b : converted) {
                            resultList.add(b);
                        }
                    }
                }
            }

            // 如果最後還在 DBCS 模式，需要加上 Shift-Out
            if (inDbcsMode) {
                resultList.add((byte)0x0F);
            }

            // 將 List 轉成 byte[]
            byte[] result = new byte[resultList.size()];
            for (int i = 0; i < resultList.size(); i++) {
                result[i] = resultList.get(i);
            }
            return result;
        }catch (Exception e){
            logger.error(e, e.getMessage());
            return EBCDIC_STAR;
        }

    }

    public static String convertIBM937BytesToUnicodeStr(byte[] data) {
        final String ERROR_MARK = "★";
        try {
            validateTNSCode();

            ConvertNHC_UCS2 converter = new ConvertNHC_UCS2();
            long nRet = converter.convert(null, data, data.length);
            if (nRet != 0) {
                logger.debug("字霸轉換失敗！errCode = ConvertNHC_UCS2.convert() = [" + nRet + "]");
                return ERROR_MARK;
            }
            return new String(converter.getResult());
        }catch(Exception e) {
            logger.error(e, e.getMessage());
            return ERROR_MARK;
        }
    }

    public static String convertBIG5BytesToUnicodeStr(byte[] data) {
        final String ERROR_MARK = "★";
        try {
            validateTNSCode();
            ConvertBig5_UCS2 converter = new ConvertBig5_UCS2();
            long nRet = converter.convert(null, data, data.length);
            if (nRet != 0) {
                logger.warn("字霸轉換失敗！errCode = ConvertBig5_UCS2.convert() = [" + nRet + "]");
                return ERROR_MARK;
            }
            return new String(converter.getResult());
        }catch(Exception e) {
            logger.error(e, e.getMessage());
            return ERROR_MARK;
        }
    }

    public static byte[] convertUnicodeStrToCNSStr(String data) throws Exception {
        final byte[] CNS_STAR = new byte[] { 0x0E, 0x21, 0x78, 0x0F };
        try {
            validateTNSCode();

            char[] chs = new char[data.length()];
            data.getChars(0, data.length(), chs, 0);
            ConvertUCS2_CNS converter = new ConvertUCS2_CNS();
            long nRet = converter.convert(null, chs, chs.length);
            if (nRet != 0) {
                logger.debug("字霸轉換失敗！errCode = ConvertUCS2_CNS.convert() = [" + nRet + "]");
                return CNS_STAR;
            }

            byte[] udata = converter.getResult();
            return udata;
        }catch(Exception e) {
            logger.error(e, e.getMessage());
            return CNS_STAR;
        }
    }
    /**
     * 將CNS轉回uniCode
     **/
    public static String convertCNSStrToUnicodeStr(String data) {
        final String ERROR_MARK = "★";
        try {
            validateTNSCode();

            //將 hex string 轉成 byte[]
            byte[] byt = new byte[data.length() / 2];
            for (int i = 0; i < data.length(); i += 2) {
                String byteStr = data.substring(i, i + 2);
                byt[i / 2] = (byte) Integer.parseInt(byteStr, 16);
            }

            ConvertCNS_UCS2 converter = new ConvertCNS_UCS2();
            long nRet = converter.convert(null, byt, byt.length);

            if (nRet != 0) {
                logger.debug("字霸轉換失敗！errCode = ConvertCNS_UCS2.convert() = [" + nRet + "]");
                return ERROR_MARK;
            }

            return new String(converter.getResult());
        }catch(Exception e) {
            logger.error(e, e.getMessage());
            return ERROR_MARK;
        }
    }
    /**
     * 將CNS直接轉ebcdic
     **/
    public static byte[] convertCNSStrToIBM937Bytes(String data) {
        // 預定義失敗回傳值：★ (IBM937: 0E 44E6 0F)
        final byte[] EBCDIC_STAR = new byte[] { 0x0E, 0x44, (byte)0xE6, 0x0F };

        try {
            validateTNSCode();
            //將 hex string 轉成 byte[]
            byte[] byt = new byte[data.length() / 2];
            for (int i = 0; i < data.length(); i += 2) {
                String byteStr = data.substring(i, i + 2);
                byt[i / 2] = (byte) Integer.parseInt(byteStr, 16);
            }

            ConvertCNS_NHC converter = new ConvertCNS_NHC();
            long nRet = converter.convert(null, byt, byt.length);

            if (nRet != 0) {
                logger.debug("字霸轉換失敗！errCode = ConvertCNS_UCS2.convert() = [" + nRet + "]");
                return EBCDIC_STAR;
            }

            byte[] result = converter.getResult();
            return result;
        }catch (Exception e) {
            logger.error(e, e.getMessage());
            return EBCDIC_STAR;
        }
    }

    private static void validateTNSCode() throws Exception {
        if (!isloaded) {
            logger.debug("字霸字碼表尚未載入！");
            throw new Exception("字霸字碼表尚未載入！");
        }
    }

    /**
     * 網頁內容轉換.
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String convertWebInputWord4check(String data) throws Exception {
        validateTNSCode();

        // TB_UCS2_Web p_Table = new TB_UCS2_Web();
        // if(TBConvertTCB.isCustomize())
        // p_Table = (TB_UCS2_Web)TBConvertTCB.getTable("TB_UCS2_WEB.BIN");
        // byte[] byt = new byte[data.length()];
        // byt = data.getBytes();
        // UTF8WebValidate validate = new UTF8WebValidate();
        // long nRet = validate.validate(p_Table, byt, byt.length);

        byte[] byt = new byte[data.length()];
        byt = data.getBytes("UTF-8");
        UTF8WebValidate validate = new UTF8WebValidate();
        long nRet = validate.validate(null, byt, byt.length);

        if (nRet != 0) {
            logger.debug("網頁內容檢核失敗！errCode = UTF8WebValidate.validate() = [" + nRet + "]");
            throw new Exception("網頁內容檢核失敗！errCode = UTF8WebValidate.validate() = [" + nRet + "]");
        }

        return new String(validate.getResult(), "UTF-8");
    }
}
