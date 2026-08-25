package com.syscom.fep.configuration.util;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.vo.enums.FEPNotify;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FEPNotifyUtil {
    private static final LogHelper logger = LogHelperFactory.getGeneralLogger();
    private static final SysconfExtMapper sysconfMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);

    private FEPNotifyUtil() {}

    public static List<String> getNotifiyList(String notify) {
        if (StringUtils.isBlank(notify))
            return new ArrayList<>();
        List<String> notifyList = StringUtil.split(notify, ';', ',');
        return getNotifiyList(notifyList);
    }

    public static List<String> getNotifiyList(List<String> notifyList) {
        List<String> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(notifyList) || notifyList.stream().allMatch(StringUtils::isBlank))
            return result;
        for (String notifier : notifyList) {
            if (StringUtils.isBlank(notifier))
                continue;
            if (FEPNotify.valueOfName(notifier) != null) {
                result.addAll(getNotifyListFromSysconf(notifier));
            } else {
                result.add(notifier);
            }
        }
        return result;
    }

    private static List<String> getNotifyListFromSysconf(String sysconfName) {
        try {
            Sysconf sysconf = sysconfMapper.selectByPrimaryKey((short) CMNConfig.SubSystemNo, sysconfName);
            if (sysconf != null && StringUtils.isNotBlank(sysconf.getSysconfValue())) {
                List<String> notifyList = StringUtil.split(sysconf.getSysconfValue(), ',', ';');
                if (CollectionUtils.isNotEmpty(notifyList))
                    return notifyList.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error(e, "Get Sysconf failed, sysconfSubsysno:", CMNConfig.SubSystemNo, ", sysconfName:", sysconfName);
        }
        return new ArrayList<>();
    }
}
