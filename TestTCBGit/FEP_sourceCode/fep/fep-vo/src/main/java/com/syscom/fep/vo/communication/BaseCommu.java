package com.syscom.fep.vo.communication;

import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.delegate.ExecuteListener1;
import com.syscom.fep.frmcommon.delegate.ExecuteListener2;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.CompressionUtil;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public abstract class BaseCommu implements Serializable {
    public static final int HEADER_BASECOMMU_IDENTITY_SIZE = 8;
    public static final int HEADER_ENTITY_IDENTITY_SIZE = 8;
    public static final int HEADER_COMPRESSED_FLAG = 2;
    public static final int HEADER_LENGTH_SIZE = 8;
    /**
     * 用於區分物件, 在字串轉物件的時候會用到
     */
    private final String classname = this.getClass().getName();
    /**
     * 用於設定Response時的結果, 預設是true
     */
    private FEPReturnCode code = FEPReturnCode.Normal;
    /**
     * 錯誤訊息
     */
    private String errmsg;
    /**
     * Socket Host
     */
    @XStreamOmitField
    private String host;
    /**
     * Socket Port
     */
    @XStreamOmitField
    private int port;
    /**
     * AppDynamics使用的關聯key
     */
    private String correlationKey;

    /**
     * 是否對序列化後的字串, 轉為HEX字串
     *
     * @return
     */
    protected abstract boolean isSerializedToHex();

    /**
     * 是否對序列化的字串進行壓縮, 預設是false
     *
     * @return
     */
    protected abstract boolean isCompressed();

    /**
     * 將物件轉為序列化的字串
     *
     * @return
     */
    public abstract String serializedTo();

    public FEPReturnCode getCode() {
        return code;
    }

    public void setCode(FEPReturnCode code) {
        this.code = code;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    /**
     * 用來標識是否壓縮hex字串
     *
     * @return
     */
    public static String getHeaderBaseCommuIdentity() {
        return StringUtils.leftPad(Integer.toUnsignedString(BaseCommu.class.getName().hashCode(), 16).toUpperCase(), HEADER_BASECOMMU_IDENTITY_SIZE, "0");
    }

    /**
     * 如果是壓縮字串需要帶入最前面, 作為一個標識位
     *
     * @return
     */
    public String getHeaderEntityIdentity() {
        return getHeaderEntityIdentity(this.getClass());
    }

    /**
     * 如果是壓縮字串需要帶入最前面, 作為一個標識位
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T extends BaseCommu> String getHeaderEntityIdentity(Class<T> clazz) {
        return StringUtils.leftPad(Integer.toUnsignedString(clazz.getName().hashCode(), 16).toUpperCase(), HEADER_ENTITY_IDENTITY_SIZE, "0");
    }

    /**
     * 將物件轉為序列化的字串, 進行再次轉換處理
     *
     * @param serializedStr
     * @return
     */
    protected String serializedTo(String serializedStr) {
        String rtn = serializedStr;
        if (this.isSerializedToHex()) {
            LocalDateTime begin = LocalDateTime.now();
            LogHelperFactory.getTraceLogger().debug("[", classname, "][serializedTo]begin at ", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(begin));
            try {
                // hex字串轉byte
                byte[] bytes = ConvertUtil.toBytes(rtn, StandardCharsets.UTF_8);
                // 壓縮
                if (this.isCompressed())
                    bytes = CompressionUtil.compress(bytes);
                // 轉成hex
                rtn = ConvertUtil.toHex(bytes);
                // 4 bytes BaseCommu Identity + 4 bytes Entity Identity + 1 bytes Compressed Flag + 4 bytes length + Hex String
                rtn = StringUtils.join(
                        getHeaderBaseCommuIdentity(),
                        this.getHeaderEntityIdentity(),
                        StringUtils.leftPad(isCompressed() ? "1" : "0", HEADER_COMPRESSED_FLAG, "0"),
                        StringUtils.leftPad(Integer.toString(rtn.length(), 16).toUpperCase(), HEADER_LENGTH_SIZE, "0"),
                        rtn
                );
            } catch (IOException e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            } finally {
                LocalDateTime end = LocalDateTime.now();
                long duration = ChronoUnit.MILLIS.between(begin, end);
                LogHelperFactory.getTraceLogger().debug("[", classname, "][serializedTo]finished at ", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(end),
                        ", duration:", duration, " millisecond");
            }
        }
        return rtn;
    }

    /**
     * 將序列化的字串轉為物件
     *
     * @param serializedStr
     * @param clazz
     * @param fetchClassname
     * @param serializedFrom
     * @param <T>
     * @return
     * @throws Exception
     */
    @SuppressWarnings({"unchecked"})
    protected static <T extends BaseCommu> T serializedFrom(String serializedStr, Class<T> clazz, ExecuteListener1<String, String> fetchClassname, ExecuteListener2<T, String, Class<T>> serializedFrom) throws Exception {
        LocalDateTime begin = LocalDateTime.now();
        LogHelperFactory.getTraceLogger().debug("[serializedFrom]", clazz == null ? StringUtils.EMPTY : StringUtils.join("[", clazz.getName(), "]"),
                "begin at ", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(begin));
        try {
            String headerIdentity = null;
            int offset = 0;
            // 4 bytes BaseCommu Identity
            String headerBaseCommuIdentity = serializedStr.substring(offset, HEADER_BASECOMMU_IDENTITY_SIZE);
            offset += HEADER_BASECOMMU_IDENTITY_SIZE;
            boolean isCompressed = false;
            if (getHeaderBaseCommuIdentity().equals(headerBaseCommuIdentity)) {
                // 4 bytes Entity Identity
                headerIdentity = serializedStr.substring(offset, offset + HEADER_ENTITY_IDENTITY_SIZE);
                offset += HEADER_ENTITY_IDENTITY_SIZE;
                // 1 bytes Compressed Flag
                isCompressed = Integer.parseInt(serializedStr.substring(offset, offset + HEADER_COMPRESSED_FLAG)) == 1;
                offset += HEADER_COMPRESSED_FLAG;
                // 4 bytes length
                int length = Integer.parseInt(serializedStr.substring(offset, offset + HEADER_LENGTH_SIZE), 16);
                offset += HEADER_LENGTH_SIZE;
                String hex = serializedStr.substring(offset, offset + length);
                offset += length;
                // hex字串轉byte
                byte[] bytes = ConvertUtil.hexToBytes(hex);
                // 解壓
                if (isCompressed) {
                    // 2025-02-25 Richard modified for [Unchecked Input for Loop Condition]
                    // bytes = CompressionUtil.decompress(compressedBytes);
                    // bytes = ReflectUtil.envokeStaticMethod(CompressionUtil.class, "decompress", new Class[] {byte[].class}, new Object[] {bytes}, new byte[0]);
                    bytes = CompressionUtil.decompress(bytes);
                }
                // bytes再轉為的字串
                serializedStr = ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
            }
            if (clazz == null) {
                String classname = fetchClassname.execute(serializedStr);
                // 2025-09-08 Richard modified for [Unsafe Reflection]
                if (StringUtils.isNotBlank(classname) && classname.startsWith(BaseCommu.class.getPackageName())) {
                    clazz = (Class<T>) Class.forName(classname);
                } else {
                    return null;
                }
            }
            T t = serializedFrom.execute(serializedStr, clazz);
            // verify
            if (StringUtils.isNotBlank(headerIdentity) && !headerIdentity.equals(t.getHeaderEntityIdentity())) {
                throw new Exception(StringUtils.join("Incorrect Header Identity, Expect: ", t.getHeaderEntityIdentity(), ", Actual: " + headerIdentity));
            }
            return t;
        } finally {
            LocalDateTime end = LocalDateTime.now();
            long duration = ChronoUnit.MILLIS.between(begin, end);
            LogHelperFactory.getTraceLogger().debug("[serializedFrom]", clazz == null ? StringUtils.EMPTY : StringUtils.join("[", clazz.getName(), "]"),
                    "finished at ", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(end),
                    ", duration:", duration, " millisecond");
        }
    }
}