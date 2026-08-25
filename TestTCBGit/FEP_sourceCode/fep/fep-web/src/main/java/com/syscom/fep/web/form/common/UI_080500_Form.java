package com.syscom.fep.web.form.common;

import com.syscom.fep.web.form.BaseForm;
import com.syscom.fep.web.configurer.WebFISCConfiguration.WebFISCAgent;

public class UI_080500_Form extends BaseForm {
    private WebFISCAgent agent;
    /**
     * FISC GW是否有運行中
     */
    private boolean alive;
    /**
     * FISC GW Primary腳位是否有運行中
     */
    private boolean primaryAlive;
    /**
     * FISC GW Secondary腳位是否有運行中
     */
    private boolean secondaryAlive;

    public WebFISCAgent getAgent() {
        return agent;
    }

    public void setAgent(WebFISCAgent agent) {
        this.agent = agent;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isPrimaryAlive() {
        return primaryAlive;
    }

    public void setPrimaryAlive(boolean primaryAlive) {
        this.primaryAlive = primaryAlive;
    }

    public boolean isSecondaryAlive() {
        return secondaryAlive;
    }

    public void setSecondaryAlive(boolean secondaryAlive) {
        this.secondaryAlive = secondaryAlive;
    }
}
