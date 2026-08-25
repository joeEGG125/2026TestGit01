package com.syscom.fep.batch.base.configurer;

public class BatchBaseConfigurationTask {
    private String path;
    private String jarNameTemplate;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getJarNameTemplate() {
        return jarNameTemplate;
    }

    public void setJarNameTemplate(String jarNameTemplate) {
        this.jarNameTemplate = jarNameTemplate;
    }
}
