package com.syscom.fep.base.configurer;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ConfigurationPropertiesUtil;
import com.syscom.fep.frmcommon.util.IOUtil;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

@Configuration
@ConfigurationProperties(prefix = "spring.fep.pki")
@Lazy
// @RefreshScope
public class PKIConfig {
    private final LogHelper logger = LogHelperFactory.getGeneralLogger();
    private String path;
    private final Properties properties = new Properties();

    @PostConstruct
    public void postConstruct() {
        logger.info("try to load all PKI files in path:", path);
        if (StringUtils.isNotBlank(path)) {
            StringTokenizer st = new StringTokenizer(path, ",");
            while (st.hasMoreTokens()) {
                String path = st.nextToken().trim();
                try (InputStream in = IOUtil.openInputStream(path)) {
                    Properties props = new Properties();
                    props.load(in);
                    properties.putAll(props);
                } catch (Exception e) {
                    logger.error(e, "load PKI file failed, path" + e.getMessage());
                }
            }
        }
        logger.info(ConfigurationPropertiesUtil.info(this, "PKI Configuration", "logger"));
    }

    public String getProperty(String key) {
        return properties.getProperty(key, null);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
