package com.syscom.fep.web.base;

import com.syscom.fep.base.cnst.Const;

public interface WebConst {
    public static final String ERROR_FEP_USER_NOT_EXIST = "FEP系統帳號不存在!!";
    public static final String ERROR_LOGIN_ID_IS_BLANK = "取得的loginId為空白!!";
    public static final String ERROR_FEP_USER_NOT_ADDED = "未新增" + Const.KEY_WORDS_IN_MESSAGE + "使用者!!";
    public static final String ERROR_RETURN_ATTRIBUTE_IS_BLANK = "取得的" + Const.KEY_WORDS_IN_MESSAGE + "為空白!!";
    public static final String ERROR_FEP_ROLE_NOT_ADDED = "未新增" + Const.KEY_WORDS_IN_MESSAGE + "角色!!";
    public static final String ERROR_FEP_ROLE_RESOURCE_INQUIRY_FAILED = "查詢角色" + Const.KEY_WORDS_IN_MESSAGE + "權限出現錯誤!!";
    public static final String RESPONSE_TYPE_BLOB = "blob";
    public static final String REQUEST_HEADER_VALUE_XMLHTTPREQUEST = "XMLHttpRequest";
    public static final String REQUEST_HEADER_KEY_X_Requested_With = "X-Requested-With";
    public static final String REQUEST_HEADER_KEY_ACCEPT = "accept";
    public static final String REQUEST_HEADER_KEY_RESPONSE_TYPE = "Response-type";
}
