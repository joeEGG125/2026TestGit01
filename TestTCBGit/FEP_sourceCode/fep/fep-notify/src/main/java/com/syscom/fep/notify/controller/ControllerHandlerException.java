package com.syscom.fep.notify.controller;

import com.syscom.fep.notify.dto.response.NotifyResponse;
import com.syscom.fep.notify.exception.NotifyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
@ResponseBody
public class ControllerHandlerException {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({NotifyException.class})
    public NotifyResponse handleServiceException(NotifyException error) {
        NotifyResponse notifyResponse = new NotifyResponse();
        notifyResponse.setClientId(error.getClientId());
        notifyResponse.setCode(error.getCode());
        notifyResponse.setMessage(error.getMsg());
        return notifyResponse;
    }

}
