package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum CardStatus {

    /**
     *申請
     */
    Apply(0),
    /**
     *製卡
     */
    MakeCard(1),
    /**
     *寄送
     */
    Mail(2),
    /**
     *啟用
     */
    Active(5),
    /**
     *作廢
     */
    Invalidate(6);


    private int code;

    private CardStatus(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CardStatus fromCode(int code){
        for(CardStatus e : values()){
            if(e.getCode() == code){
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid code = [" + code + "]!!!");
    }

    public static CardStatus parse(Object nameOrCode){
        if(nameOrCode instanceof Integer){
            return fromCode((Integer)nameOrCode);
        }else if(nameOrCode instanceof Short){
            return fromCode((Short)nameOrCode);
        }
        if (nameOrCode instanceof String) {
            String nameOrCodeStr = (String) nameOrCode;
            if (StringUtils.isNumeric(nameOrCodeStr)) {
                return fromCode(Integer.parseInt(nameOrCodeStr));
            }
            for (CardStatus e : values()) {
                if (e.name().equalsIgnoreCase(nameOrCodeStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or code = [" + nameOrCode + "]!!!");
    }
}
