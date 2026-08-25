package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("request")
public class ToFEPCommuConfig extends BaseXmlCommu {
    private int configType;

    public int getConfigType() {
        return configType;
    }

    public void setConfigType(int configType) {
        this.configType = configType;
    }

    public static enum ConfigType {
        /**
         * 取CMNConfig的資料
         */
        CMN(1),
        /**
         * 取GWConfig的資料
         */
        GW(2),
        /**
         * 取ATMPConfig的資料
         */
        ATMP(4),
        /**
         * 取INBKConfig的資料
         */
        INBK(8),
        /**
         * 取INBKConfig的資料
         */
        RM(16),
        /**
         * 取CARDConfig的資料
         */
        CARD(32),
        /**
         * 全部
         */
        ALL(63);

        private int value;

        private ConfigType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ConfigType fromValue(int value) {
            for (ConfigType e : values()) {
                if (e.getValue() == value) {
                    return e;
                }
            }
            throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
        }
    }
}
