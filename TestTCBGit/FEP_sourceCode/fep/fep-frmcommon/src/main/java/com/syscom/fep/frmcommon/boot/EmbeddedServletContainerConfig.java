package com.syscom.fep.frmcommon.boot;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;

@Configuration
@ConfigurationProperties(prefix = "spring.fep.embedded.server")
@ConditionalOnProperty(prefix = "spring.fep.embedded.server", name = {"baseDir", "docRoot"})
// @RefreshScope
public class EmbeddedServletContainerConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    private final LogHelper logger = new LogHelper();
    private String baseDir;
    private String docRoot;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        if (StringUtils.isNotBlank(this.baseDir)) {
            File baseFile = this.createDirectory(this.baseDir);
            if (baseFile != null) factory.setBaseDirectory(baseFile);
        }
        if (StringUtils.isNotBlank(this.docRoot)) {
            File docFile = this.createDirectory(this.docRoot);
            if (docFile != null) factory.setDocumentRoot(docFile);
        }
    }

    private File createDirectory(String directory) {
        File file = new File(directory);
        if (!file.exists()) {
            if (file.mkdirs()) {
                logger.debug("Mkdirs Succeed, directory = [", directory, "]");
            } else {
                logger.error("Mkdirs Failed, directory = [", directory, "]");
                return null; // 如果mkdirs失敗, 則返回null
            }
        } else {
            logger.debug("Directory already exist, directory = [", directory, "]");
        }
        return file;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public void setDocRoot(String docRoot) {
        this.docRoot = docRoot;
    }

    @PostConstruct
    public void print() {
        Field[] fields = EmbeddedServletContainerConfig.class.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            int repeat = 2;
            // 列印配置檔內容
            StringBuilder sb = new StringBuilder();
            sb.append("Embedded Servlet Container Configuration:\r\n");
            for (Field field : fields) {
                if ("logger".equals(field.getName())) continue;
                ReflectionUtils.makeAccessible(field);
                Value annotation = field.getAnnotation(Value.class);
                sb.append(StringUtils.repeat(StringUtils.SPACE, repeat));
                if (annotation != null) {
                    sb.append(annotation.value().substring(annotation.value().indexOf("${") + 2, annotation.value().contains(":") ? annotation.value().indexOf(":") : annotation.value().length() - 1));
                } else {
                    ConfigurationProperties annotation2 = this.getClass().getAnnotation(ConfigurationProperties.class);
                    if (annotation2 != null) {
                        String prefix = annotation2.prefix();
                        if (StringUtils.isNotBlank(prefix)) {
                            sb.append(prefix).append(".");
                        }
                    }
                    sb.append(field.getName());
                }
                sb.append(" = ").append(ReflectionUtils.getField(field, this)).append("\r\n");
            }
            logger.info(sb.toString());
        }
    }
}
