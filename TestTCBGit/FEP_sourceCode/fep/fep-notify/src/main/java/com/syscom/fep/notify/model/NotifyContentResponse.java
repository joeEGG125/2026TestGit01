package com.syscom.fep.notify.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class NotifyContentResponse {
    private String contentIndex;
    private String message;
    private String contentStatus;
}
