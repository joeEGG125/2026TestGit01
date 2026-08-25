package com.syscom.fep.web.configurer;

import java.util.List;

public class WebApLogConfiguration {
    private String server;
    private List<String> logtype;
    private String service;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public List<String> getLogtype() {
        return logtype;
    }

    public void setLogtype(List<String> logtype) {
        this.logtype = logtype;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
