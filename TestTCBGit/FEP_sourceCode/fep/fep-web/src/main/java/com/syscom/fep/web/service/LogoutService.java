package com.syscom.fep.web.service;

import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.SessionKey;
import com.syscom.fep.web.entity.User;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.security.Authentication;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import jakarta.servlet.http.HttpSession;

import java.util.stream.Stream;

@Service
public class LogoutService extends BaseService {

    @Autowired
    private Authentication authentication;

    public void processLogout(ModelMap mode) {
        User user = WebUtil.getUser();
        try {
            // TODO 做其他事情
            if (user != null) {
                authentication.logOff(Integer.parseInt(user.getUserId()), user.getLoginId(), user.getSrcIp());
            }
        } catch (Exception e) {
            String errMessage = StringUtils.join("帳號 = [", user.getLoginId(), "]發生異常, ", e.getMessage());
            this.warnMessage(e, errMessage);
            this.sendEMS(errMessage);
        } finally {
            // 清除session
            Stream.of(SessionKey.values()).forEach(WebUtil::removeFromSession);
            if (user != null) {
                // 記錄log
                this.infoMessage("帳號 = [", user.getLoginId(), "]登出");
                // 這裡記錄輸入過的使用者賬號
                WebUtil.putInAttribute(mode, AttributeName.UserId, user.getLoginId());
                WebUtil.putInAttribute(mode, AttributeName.LogoutUserId, user.getLoginId()); // 這句是為了WebAuditInterceptor
                // user資料重置
                user.reset();
                // user清除掉
                // user = null;
                // System.gc();
            }
            // 銷毀session
            HttpSession session = WebUtil.getSession();
            if (session != null) {
                session.invalidate();
            }
        }
    }
}
