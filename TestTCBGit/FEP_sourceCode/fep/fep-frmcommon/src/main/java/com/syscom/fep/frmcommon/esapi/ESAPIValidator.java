package com.syscom.fep.frmcommon.esapi;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.ValidationException;
import org.owasp.esapi.reference.DefaultValidator;

public class ESAPIValidator {
    private final static LogHelper logger = new LogHelper();
    private static final DefaultValidator validator = (DefaultValidator) ESAPI.validator();

    private ESAPIValidator() {}

    public static String getValidFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) return fileName;
        try {
            return validator.getValidInput("Path", fileName, "FileName", 255, false);
        } catch (ValidationException e) {
            logger.exceptionMsg(e, e.getMessage());
            return fileName;
        }
    }

    public static String getValidFilePath(String filePath) {
        return getValidDirectoryName(filePath);
    }

    public static String getValidDirectoryName(String directoryName) {
        if (StringUtils.isBlank(directoryName)) return directoryName;
        // ESAPI對path中含有\\處理兼容性不好, 所以這裡就先將\\轉為/
        if (directoryName.contains("\\")) {
            directoryName = StringUtils.replace(directoryName, "\\", "/");
        }
        try {
            return validator.getValidInput("Path", directoryName, "DirectoryName", 255, false);
        } catch (ValidationException e) {
            logger.exceptionMsg(e, e.getMessage());
            return directoryName;
        }
    }

    public static Integer getValidInteger(String input, int minValue, int maxValue, boolean allowNull, Integer... defaultValue) {
        try {
            return ESAPI.validator().getValidInteger("", input, minValue, maxValue, allowNull);
        } catch (ValidationException e) {
            logger.exceptionMsg(e, e.getMessage());
            return ArrayUtils.isNotEmpty(defaultValue) ? defaultValue[0] : null;
        }
    }

    public static String getValidInput(String input, int maxLength, boolean allowNull, String... defaultValue) {
        try {
            return ESAPI.validator().getValidInput("", input, "inputValue", maxLength, allowNull);
        } catch (ValidationException e) {
            logger.exceptionMsg(e, e.getMessage());
            return ArrayUtils.isNotEmpty(defaultValue) ? defaultValue[0] : null;
        }
    }
}
