package com.syscom.fep.web.form.common;

import com.syscom.fep.web.form.BaseForm;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class UI_080500_FormAction extends BaseForm {
    /**
     * 要執行動作的FISCGW名稱
     */
    private String name;
    private Target target;
    private Action action;
    /**
     * 勾選主要線路的FISCGW名稱
     */
    private List<String> primary;
    /**
     * 勾選備援線路的FISCGW名稱
     */
    private List<String> secondary;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public List<String> getPrimary() {
        return primary;
    }

    public void setPrimary(List<String> primary) {
        this.primary = primary;
    }

    public List<String> getSecondary() {
        return secondary;
    }

    public void setSecondary(List<String> secondary) {
        this.secondary = secondary;
    }

    /**
     * 若有不同FISCGW的主要及備援線路同時勾選的情況
     * 比如FISCGW1勾主要, FISCGW2勾備援, 或FISCGW1勾備援, FISCGW2勾主要
     * 按確定時出現警告『不可同時在不同主機啟用同一種線路!』離開不做任何處理
     *
     * @return
     */
    public boolean isDifferentChannel() {
        if (CollectionUtils.isNotEmpty(primary) && primary.size() == 1
                && CollectionUtils.isNotEmpty(secondary) && secondary.size() == 1) {
            return !primary.get(0).equals(secondary.get(0));
        }
        return false;
    }

    public static enum Target {
        channel("線路"), service("服務");

        private final String description;

        private Target(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static enum Channel {
        all("全部線路"),
        primary("主要線路"),
        secondary("備援線路");

        private final String description;

        private Channel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static enum Action {
        start("啟動"),
        stop("停止"),
        check("檢查狀態");

        private final String description;

        private Action(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
