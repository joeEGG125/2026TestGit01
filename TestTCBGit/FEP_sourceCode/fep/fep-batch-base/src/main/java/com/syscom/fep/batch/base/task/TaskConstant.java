package com.syscom.fep.batch.base.task;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.deslog.configuration.DataSourceDeslogConstant;
import com.syscom.fep.mybatis.ems.configuration.DataSourceEmsConstant;
import com.syscom.fep.mybatis.enc.configuration.DataSourceEncConstant;
import com.syscom.fep.mybatis.his.configuration.DataSourceHisConstant;

public interface TaskConstant {
    String CONFIGURATION_FILE = "task.properties";
    String DATA_SOURCE_PROPERTIES_KEY_PREFIX = "spring.datasource";
    String HIKARI_PROPERTIES_KEY_PREFIX = "spring.datasource.hikari";
    String PROPERTIES_KEY_LOG_PATH = "spring.fep.task.log.path";
    String PROPERTIES_KEY_LOG_CONSOLE_ENCODING = "spring.fep.task.log.console.encoding";
    String PROPERTIES_KEY_LOG_FILE_ENCODING = "spring.fep.task.log.file.encoding";
    String PKI_PROPERTIES_KEY = "spring.fep.pki.path";

    enum DBName {
        FEPDB(DataSourceConstant.BEAN_NAME_DATASOURCE),
        EMSDB(DataSourceEmsConstant.BEAN_NAME_DATASOURCE),
        DESDB(DataSourceEncConstant.BEAN_NAME_DATASOURCE),
        DESLOGDB(DataSourceDeslogConstant.BEAN_NAME_DATASOURCE),
        FEPHIS(DataSourceHisConstant.BEAN_NAME_DATASOURCE),
        FEPDB_BATCH(null), // 沒有在FEP專案中定義這個DataSource, 故dataSourceName就塞入null好了
        FEPHIS_BATCH(null); // 沒有在FEP專案中定義這個DataSource, 故dataSourceName就塞入null好了

        private final String dataSourceName;
        private final String dataSourceNameProperties;

        DBName(String dataSourceName) {
            this.dataSourceName = dataSourceName;
            if (name().contains("_")) {
                this.dataSourceNameProperties = name().toLowerCase().replace("_", "-");
            } else {
                this.dataSourceNameProperties = name().toLowerCase();
            }
        }

        public String getDataSourceName() {
            return dataSourceName;
        }

        public String getDataSourceNameProperties() {
            return dataSourceNameProperties;
        }
    }
}
