package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

public class ShuffleUtil {
    private static LogHelper logger = new LogHelper();

    private ShuffleUtil() {
    }

    public static String shuffle(String data, String key) throws Exception {
        if (data.length() > 32)
            throw ExceptionUtil.createException("Shuffle data length error");
        // 1.KEY = pack key
        // 2. A(8) = KEY + KEY
        // 3. B(16)= data(右靠左補null)
        // 4. C(8) = pack B(16)
        // 5. D = A XOR C
        // 6. E = unpack D
        // 7. 取E 最右邊data長度做為suffle值
        byte[] a = new byte[8];
        byte[] b = new byte[16];
        byte[] c = new byte[8];
        // pack key
        String packKey = pack(key);
        // A(8)=key + key
        a = ConvertUtil.hexToBytes(StringUtils.join(packKey, packKey));
        // B(16)
        byte[] tmp = ConvertUtil.hexToBytes(data);
        System.arraycopy(tmp, 0, b, b.length - tmp.length, tmp.length);
        // C(8) = pack B(16)
        byte[] tmpc = ConvertUtil.hexToBytes(pack(ConvertUtil.toHex(b)));
        System.arraycopy(tmpc, 0, c, c.length - tmpc.length, tmpc.length);
        // D=A XOR C
        String d = xor(a, c);
        // E = unpack D
        String e = unPack(d);
        // System.out.println(e);
        // System.out.println(pack(e));
        String result = e.substring(e.length() - data.length());
        return result;
        //return convertFromHex(result);
    }

    private static String convertFromHex(String hexString, boolean... dump) {
        try {
            byte[] bytes = Hex.decodeHex(hexString);
            bytes = ArrayUtils.removeAllOccurrences(bytes, (byte) 0);
            String result = ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
            if (ArrayUtils.isNotEmpty(dump) && dump[0]) {
                logger.info(result);
            }
            return result;
        } catch (DecoderException e) {
            logger.exceptionMsg(e, e.getMessage());
        }
        return StringUtils.EMPTY;
    }

    private static String pack(String data) {
        StringBuilder pack = new StringBuilder(StringUtils.EMPTY);
        for (int i = 1; i < data.length(); i += 2) {
            pack.append(data.substring(i, i + 1));
        }
        return pack.toString();
    }

    private static String unPack(String data) {
        StringBuilder tmp = new StringBuilder(StringUtils.EMPTY);
        for (char c : data.toCharArray()) {
            tmp.append("3").append(c);
        }
        return tmp.toString();
    }

    private static String xor(byte[] a, byte[] b) {
        if (a.length == b.length) {
            byte[] result = new byte[a.length];
            for (int i = 0; i < a.length; i++) {
                result[i] = (byte) (a[i] ^ b[i]);
            }
            String hex = ConvertUtil.toHex(result);
            return hex;
        } else {
            throw ExceptionUtil.createIllegalArgumentException("a.length = [", a.length, "] is not equals with b.length = [", b.length, "]");
        }
    }
}
