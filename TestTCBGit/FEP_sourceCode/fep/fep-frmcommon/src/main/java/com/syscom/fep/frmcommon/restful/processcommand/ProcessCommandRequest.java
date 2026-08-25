package com.syscom.fep.frmcommon.restful.processcommand;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.syscom.fep.frmcommon.communication.BaseRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProcessCommandRequest extends BaseRequest<String> {
    @JsonProperty("command")
    private String command;
    @JsonProperty("args")
    private Map<String, String> param;
    @JsonProperty("argsRemain")
    private List<String> remain;
    @JsonProperty("argsFormat")
    private ProcessCommandArgsFormat format;
    @JsonProperty("argsExtractors")
    private List<ProcessCommandArgsExtractor> extractors;
    @JsonProperty("ioOutput")
    private boolean ioOutput;
    @JsonProperty("charsetName")
    private String charsetName;
}
