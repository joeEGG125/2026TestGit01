package com.syscom.fep.notify.service;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.NotifyrulesetMapper;
import com.syscom.fep.mybatis.model.Notifyrule;
import com.syscom.fep.mybatis.model.Notifyruleset;
import com.syscom.fep.notify.exception.NotifyException;
import com.syscom.fep.notify.model.NotifyRuleSetExt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

import static com.syscom.fep.notify.cnst.NotifyConstant.INDEPENDEN_VAR_SYMBOL;
import static com.syscom.fep.notify.enums.NotifyStatusCode.RULESET_NOT_EXIST;

@Service
public class NotifyRuleSetService extends FEPBase {
    // private static LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private NotifyRuleService notifyRuleService;
    @Autowired
    private SystemVarsService systemVarsService;

    @CacheEvict(value = "notifyRuleSetCache", allEntries = true)
    public void cleanNotifyRuleSetCache(LogData logData) {
        // 清除全部的 Cache 不動到 JPA
        // logger.info("清除全部的 NotifyRuleSet Cache");
        logData.setRemark("清除全部的 NotifyRuleSet Cache");
        logData.setProgramName(StringUtils.join(ProgramName, ".cleanNotifyRuleSetCache"));
        logMessage(logData);
    }


    public void addNotifyRuleSet() {
        // 資料新增
    }

    @CacheEvict(value = "notifyRuleSetCache", allEntries = true)
    public void updateNotifyRuleSet() {
        // 資料庫更新時，要清除全部的 Cache
    }


    @Cacheable(value = "notifyRuleSetCache")
    public NotifyRuleSetExt getNotifyRuleSetById(LogData logData, Long inputRuleSetId) throws NotifyException {
        // 從 DB 取得資料
        NotifyrulesetMapper notifyrulesetMapper = SpringBeanFactoryUtil.getBean(NotifyrulesetMapper.class);
        Notifyruleset notifyruleset = notifyrulesetMapper.selectByPrimaryKey(inputRuleSetId);

        if (Objects.isNull(notifyruleset)) {
            // logger.warn(RULESET_NOT_EXIST.getDesc(), ", RuleSetId：", inputRuleSetId);
            throw new NotifyException(RULESET_NOT_EXIST, RULESET_NOT_EXIST.getDesc());
        }
        NotifyRuleSetExt notifyRuleSetExt = new NotifyRuleSetExt(notifyruleset);

        String ruleExpression = notifyruleset.getRuleExpression();

        char[] chars = ruleExpression.toCharArray();
        int pos = chars.length;
        String v = "";
        v += chars[--pos];
        boolean checkFirst = false;
        boolean checkSecond = false;
        List<String> list = new ArrayList<>();
        for (int i = pos; 0 < i; ) {
            checkFirst = (Character.isDigit(chars[i])) ? true : false;
            checkSecond = (Character.isDigit(chars[--i])) ? true : false;
            if (checkFirst == checkSecond) {
                v = chars[i] + v;
            } else {
                list.add(v);
                v = "" + chars[i];
            }
        }
        list.add(v);
        Collections.reverse(list);

        Pattern numberPattern = Pattern.compile("[0-9]*");
        String expression = "";

        for (String s : list) {
            if (numberPattern.matcher(s).matches()) {
                // 資料從 JPA 取得
                Notifyrule notifyRule = notifyRuleService.getNotifyRuleById(logData, Long.valueOf(s.toString()));
                String ruleTemplate = "";
                if ("NUMBER".equals(notifyRule.getParmValueType())) {
                    ruleTemplate = "(%s%s%s)";
                } else {
                    ruleTemplate = "('%s'%s'%s')";
                }
                String[] sysVars = {"#@SYSDATE#@"};         // 系統變數為動態,不是從db 取得
                String checkValue = notifyRule.getThreshold();
                if (Arrays.asList(sysVars).contains(checkValue)) {
                    // 系統變數為動態, Cache Ruleset 時,不可置換
                } else if (checkValue.indexOf("#@") == 0 && checkValue.lastIndexOf("#@") == checkValue.length() - 2) {
                    checkValue = systemVarsService.getSysStatCache(logData, checkValue);
                }

                String rulestr = String.format(ruleTemplate, INDEPENDEN_VAR_SYMBOL + notifyRule.getParmVarName() + INDEPENDEN_VAR_SYMBOL, notifyRule.getOperator(), checkValue);
                expression += rulestr;
            } else {
                expression += s;
            }
        }
        notifyRuleSetExt.setExpression(expression);
        // logger.info("根據 ruleSetId:", inputRuleSetId, "獲取 ruleSet");
        logData.setRemark(StringUtils.join("根據 ruleSetId:", inputRuleSetId, "獲取 ruleSet"));
        logData.setProgramName(StringUtils.join(ProgramName, ".getNotifyRuleSetById"));
        logMessage(logData);
        return notifyRuleSetExt;
    }
}
