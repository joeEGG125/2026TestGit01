package com.syscom.fep.mybatis.ext.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

/**
 * 交易資料模型類別
 * @author Zonghao
 */
@Setter
@Getter
public class UcdidMsgRsExt {
    // Header
    private UcdidRsHeadersExt headers;
    //Body
    private List<UcdidRsBodyExt> body;


    // 預設建構子
    public UcdidMsgRsExt() {
    }

    @Override
    public String toString() {
        return "{\n" +
                "  \"headers\": \"" + Objects.toString(headers, "") + "\"\n" +
                "  \"body\": \"" + Objects.toString(body, "") + "\"\n" +
                "}";
    }
}