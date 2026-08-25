package com.syscom.fep.configuration;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.Rollback;

@SpringBootApplication
@ComponentScan(basePackages = { "com.syscom.fep" })
@Rollback(true)
public class ConfigurationTestApplication {
    static {
        Jasypt.loadEncryptorKey(null);
    }
}