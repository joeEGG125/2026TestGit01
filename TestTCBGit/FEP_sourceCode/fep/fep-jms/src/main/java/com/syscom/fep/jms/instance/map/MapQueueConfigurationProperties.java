package com.syscom.fep.jms.instance.map;

import com.syscom.fep.frmcommon.jms.JmsConfigurationProperties;
import com.syscom.fep.jms.JmsQueueNames;
import org.apache.commons.lang3.StringUtils;

public abstract class MapQueueConfigurationProperties<Key> extends JmsConfigurationProperties {
    /**
     * 所有的Queue名稱定義在這個內部類中
     */
    private JmsQueueNames queueNames;

    public JmsQueueNames getQueueNames() {
        return queueNames;
    }

    public void setQueueNames(JmsQueueNames queueNames) {
        this.queueNames = queueNames;
    }

    /**
     * 一個Key對應一個JMS相關的實例物件
     *
     * @return
     */
    public abstract Key getKey();

    @Override
    public String toString(String instanceName, String configurationPropertiesPrefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString(instanceName, configurationPropertiesPrefix));
        if (queueNames != null)
            queueNames.toString(sb, StringUtils.join(configurationPropertiesPrefix, ".queue-names"));
        return sb.toString();
    }
}
