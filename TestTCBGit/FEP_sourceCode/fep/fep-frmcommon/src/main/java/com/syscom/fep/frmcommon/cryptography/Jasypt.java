package com.syscom.fep.frmcommon.cryptography;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.EnvPropertiesUtil;
import com.syscom.fep.frmcommon.util.IOUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Jasypt {
    private static final LogHelper logger = new LogHelper();
    private static boolean loaded = false;
    private static StringEncryptor jasyptStringEncryptor;
    private static final String SYSTEM_KEY_PROPERTIES_FILE = "fep.jasypt.config.file";
    private static final String PROPERTIES_FILE = "application-integration.properties";
    private static final String CONFIGURATION_JASYPT_PREFIX = "jasypt.encryptor.";
    private static final String CONFIGURATION_FEP_PREFIX = "spring.fep." + CONFIGURATION_JASYPT_PREFIX;
    private static final String PROPERTY_KEY_SSCODE = "sscode";
    private static final String PROPERTY_KEY_ALGORITHM = "algorithm";
    private static final String PROPERTY_KEY_KEYOBTENTIONITERATIONS = "keyObtentionIterations";
    private static final String PROPERTY_KEY_POOLSIZE = "poolSize";
    private static final String PROPERTY_KEY_SALTGENERATORCLASSNAME = "saltGeneratorClassname";
    private static final String PROPERTY_KEY_IVGENERATORCLASSNAME = "ivGeneratorClassname";
    private static final String PROPERTY_KEY_STRINGOUTPUTTYPE = "stringOutputType";
    private static final Map<String, String> DefPropertyValues = new HashMap<String, String>() {{
        put(PROPERTY_KEY_SSCODE, "Syscom@123");
        put(PROPERTY_KEY_ALGORITHM, "PBEWITHHMACSHA512ANDAES_256");
        put(PROPERTY_KEY_KEYOBTENTIONITERATIONS, "1000");
        put(PROPERTY_KEY_POOLSIZE, "1");
        put(PROPERTY_KEY_SALTGENERATORCLASSNAME, "org.jasypt.salt.RandomSaltGenerator");
        put(PROPERTY_KEY_IVGENERATORCLASSNAME, "org.jasypt.iv.RandomIvGenerator");
        put(PROPERTY_KEY_STRINGOUTPUTTYPE, "base64");
    }};

    private Jasypt() {}

    public static String encrypt(String input, boolean... includeMark) {
        loadEncryptorKey(null);
        if (ArrayUtils.isNotEmpty(includeMark) && includeMark[0]) {
            return StringUtils.join("ENC(", jasyptStringEncryptor.encrypt(input), ")");
        }
        return jasyptStringEncryptor.encrypt(input);
    }

    public static String decrypt(String input) {
        loadEncryptorKey(null);
        if (input.startsWith("ENC(") && input.endsWith(")")) {
            return jasyptStringEncryptor.decrypt(StringUtils.substring(input, 4, input.lastIndexOf(")")));
        }
        return jasyptStringEncryptor.decrypt(input);
    }

    public synchronized static void loadEncryptorKey(String path) {
        if (loaded) return;
        loaded = true;
        Properties properties = null;
        if (StringUtils.isBlank(path)) {
            path = System.getProperty(SYSTEM_KEY_PROPERTIES_FILE, PROPERTIES_FILE);
        }
        properties = new Properties();
        try (InputStream in = IOUtil.openInputStream(path)) {
            properties.load(in);
        } catch (Exception e) {
            logger.warn(e, "[loadEncryptorKey]File \"", path, "\" load failed!!!");
        }
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(getEncryptorProperty(properties, PROPERTY_KEY_SSCODE, DefPropertyValues.get(PROPERTY_KEY_SSCODE)));
        config.setAlgorithm(getEncryptorProperty(properties, PROPERTY_KEY_ALGORITHM, DefPropertyValues.get(PROPERTY_KEY_ALGORITHM)));
        config.setKeyObtentionIterations(getEncryptorProperty(properties, PROPERTY_KEY_KEYOBTENTIONITERATIONS, DefPropertyValues.get(PROPERTY_KEY_KEYOBTENTIONITERATIONS)));
        config.setPoolSize(getEncryptorProperty(properties, PROPERTY_KEY_POOLSIZE, DefPropertyValues.get(PROPERTY_KEY_POOLSIZE)));
        // config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName(getEncryptorProperty(properties, PROPERTY_KEY_SALTGENERATORCLASSNAME, DefPropertyValues.get(PROPERTY_KEY_SALTGENERATORCLASSNAME)));
        config.setIvGeneratorClassName(getEncryptorProperty(properties, PROPERTY_KEY_IVGENERATORCLASSNAME, DefPropertyValues.get(PROPERTY_KEY_IVGENERATORCLASSNAME)));
        config.setStringOutputType(getEncryptorProperty(properties, PROPERTY_KEY_STRINGOUTPUTTYPE, DefPropertyValues.get(PROPERTY_KEY_STRINGOUTPUTTYPE)));
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(config);
        jasyptStringEncryptor = encryptor;
    }

    private static String getEncryptorProperty(Properties properties, String propertyName, String defaultValue) {
        String value = null;
        if (properties != null) {
            value = properties.getProperty(getJasyptPropertyKey(propertyName), null);
            if (value == null)
                value = properties.getProperty(CONFIGURATION_FEP_PREFIX + propertyName, null);
        }
        if (value == null)
            value = System.getProperty(getJasyptPropertyKey(propertyName), null);
        if (value == null)
            value = System.getProperty(CONFIGURATION_FEP_PREFIX + propertyName, null);
        if (value == null)
            value = EnvPropertiesUtil.getProperty(getJasyptPropertyKey(propertyName), null);
        if (value == null)
            value = EnvPropertiesUtil.getProperty(CONFIGURATION_FEP_PREFIX + propertyName, defaultValue);
        System.setProperty(getJasyptPropertyKey(propertyName), value);
        return value;
    }

    private static String getJasyptPropertyKey(String propertyName) {
        if (PROPERTY_KEY_SSCODE.equals(propertyName)) {
            return new String(Base64.getDecoder().decode("amFzeXB0LmVuY3J5cHRvci5wYXNzd29yZA=="), StandardCharsets.UTF_8);
        }
        return CONFIGURATION_JASYPT_PREFIX + propertyName;
    }
}