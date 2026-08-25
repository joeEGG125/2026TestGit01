package com.syscom.fep;

import com.syscom.fep.frmcommon.cryptography.Jasypt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.syscom.fep"})
public class FepNotifyApplication {

    static {
        Jasypt.loadEncryptorKey(null);
    }

    public static void main(String[] args) {
        try {
            SpringApplication.run(FepNotifyApplication.class, args);
        } catch (Exception e) {
            com.syscom.fep.common.log.LogHelperFactory.getGeneralLogger().error(e, "FepNotifyApplication start failed!!!");
        }
    }
}
