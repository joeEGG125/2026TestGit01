package com.syscom.fep.configuration;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ConfigurationTestApplication.class)
public class ConfigurationBaseTest {
    protected static final LogHelper UnitTestLogger = LogHelperFactory.getUnitTestLogger();
}
