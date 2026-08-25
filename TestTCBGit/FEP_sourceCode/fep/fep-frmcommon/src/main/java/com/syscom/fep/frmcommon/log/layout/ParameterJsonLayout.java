package com.syscom.fep.frmcommon.log.layout;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.json.classic.JsonLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ParameterJsonLayout extends JsonLayout {
    public static final Parameter MESSAGE_TYPE_LOG = new Parameter("messageType", "java.lang.String", "log");
    public static final Parameter MESSAGE_TYPE_ALERT = new Parameter("messageType", "java.lang.String", "alert");

    private final List<Parameter> parameters = new ArrayList<>();

    public void addParameter(Parameter parameter) {
        this.parameters.add(parameter);
    }

    @Override
    protected void addCustomDataToJsonMap(Map<String, Object> map, ILoggingEvent event) {
        for (Parameter parameter : parameters) {
            map.put(parameter.getName(), parameter.getLayout());
        }
    }

    public boolean containsParameter(Parameter parameter) {
        return parameters.stream().anyMatch(p ->
                Objects.equals(p.getName(), parameter.getName())
                        && Objects.equals(p.getLayout(), parameter.getLayout())
                        && Objects.equals(p.getType(), parameter.getType()));
    }

    public static class Parameter {
        private String name;
        private String type;
        private String layout;

        public Parameter() {}

        public Parameter(String name, String type, String layout) {
            this.name = name;
            this.type = type;
            this.layout = layout;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLayout() {
            return layout;
        }

        public void setLayout(String layout) {
            this.layout = layout;
        }
    }
}
