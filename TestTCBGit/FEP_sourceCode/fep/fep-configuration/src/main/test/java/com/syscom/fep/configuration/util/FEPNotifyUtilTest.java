package com.syscom.fep.configuration.util;

import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.ConfigurationBaseTest;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.vo.enums.FEPNotify;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;

public class FEPNotifyUtilTest extends ConfigurationBaseTest {
    @Autowired
    private SysconfExtMapper sysconfExtMapper;

    @BeforeEach
    public void init() {
        Sysconf sysconf = new Sysconf();
        sysconf.setSysconfDatatype("varchar");
        sysconf.setSysconfEncrypt((short) 0);
        sysconf.setSysconfFreq(null);
        sysconf.setSysconfReadonly((short) 0);
        sysconf.setSysconfRemark("Remark");
        sysconf.setSysconfSubsysno((short) CMNConfig.SubSystemNo);
        sysconf.setSysconfType("CMN");
        sysconf.setUpdateTime(Calendar.getInstance().getTime());
        sysconf.setUpdateUserid(0);
        for (FEPNotify notify : FEPNotify.values()) {
            sysconf.setSysconfName(notify.name());
            if (notify.name().endsWith("_Customize"))
                continue;
            if (notify.name().startsWith("FEPNotifyMail_")) {
                sysconf.setSysconfValue(notify.name().toLowerCase() + "@tcb.com.tw");
            } else if (notify.name().startsWith("FEPNotifyPhone_")) {
                sysconf.setSysconfValue(Integer.toString(notify.name().toLowerCase().hashCode()));
            }
            sysconfExtMapper.insert(sysconf);
        }
    }

    @Test
    public void test() {
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("Richard_Yu@email.lingan.com.cn"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("Richard_Yu@email.lingan.com.cn;Annie_Bai@email.lingan.com.cn"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("Richard_Yu@email.lingan.com.cn;Annie_Bai@email.lingan.com.cn;myfifa2005@qq.com"), ","));

        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyMail_APD;Richard_Yu@email.lingan.com.cn"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyMail_APD;Richard_Yu@email.lingan.com.cn;Annie_Bai@email.lingan.com.cn"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyMail_APD;Richard_Yu@email.lingan.com.cn;Annie_Bai@email.lingan.com.cn;myfifa2005@qq.com"), ","));

        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyMail_SYS;Richard_Yu@email.lingan.com.cn"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyMail_SYS;Richard_Yu@email.lingan.com.cn;Annie_Bai@email.lingan.com.cn"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyMail_SYS;Richard_Yu@email.lingan.com.cn;Annie_Bai@email.lingan.com.cn;myfifa2005@qq.com"), ","));

        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyMail_APD;FEPNotifyMail_SYS;Richard_Yu@email.lingan.com.cn"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyMail_APD;FEPNotifyMail_SYS;Richard_Yu@email.lingan.com.cn;Annie_Bai@email.lingan.com.cn"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyMail_APD;FEPNotifyMail_SYS;Richard_Yu@email.lingan.com.cn;Annie_Bai@email.lingan.com.cn;myfifa2005@qq.com"), ","));

        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("18502910982"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("18502910982;18502922463"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("18502910982;18502922463;15332466180"), ","));

        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyPhone_APD;18502910982"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyPhone_APD;18502910982;18502922463"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyPhone_APD;18502910982;18502922463;15332466180"), ","));

        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyPhone_SYS;18502910982"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyPhone_SYS;18502910982;18502922463"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyPhone_SYS;18502910982;18502922463;15332466180"), ","));

        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyPhone_APD;FEPNotifyPhone_SYS;18502910982"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyPhone_APD;FEPNotifyPhone_SYS;18502910982;18502922463"), ","));
        UnitTestLogger.info("====================", StringUtils.join(FEPNotifyUtil.getNotifiyList("FEPNotifyPhone_APD;FEPNotifyPhone_SYS;18502910982;18502922463;15332466180"), ","));
    }
}
