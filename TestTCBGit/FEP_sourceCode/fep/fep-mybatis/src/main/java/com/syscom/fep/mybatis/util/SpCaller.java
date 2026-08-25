package com.syscom.fep.mybatis.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.mapper.UserDefineExtMapper;
import com.syscom.fep.mybatis.model.Ejfno;
import com.syscom.fep.mybatis.model.Fcrmnoctl;
import com.syscom.fep.mybatis.model.Fcrmoutsno;
import com.syscom.fep.mybatis.model.Rmfiscout1;
import com.syscom.fep.mybatis.model.Rmfiscout4;
import com.syscom.fep.mybatis.model.Rmnoctl;
import com.syscom.fep.mybatis.model.Rmoutsno;
import com.syscom.fep.mybatis.model.Webaudit;

/**
 * 所有呼叫Storage Procedure都寫在這裡
 *
 * @author Richard
 */
@Component
@Lazy
public class SpCaller {
    public static final String OUT_ID = "ID";
    public static final String OUT_INTERVAL = "INTERVAL";
    public static final String OUT_RESULT = "RESULT";
    public static final String OUT_NEXTID = "NEXTID";

    @Autowired
    private UserDefineExtMapper userDefineExtMapper;

    // 2022-11-23 Richard marked
    // EJ的獲取方式改為從Sequence中取, 所以這個方法mark掉
    // public Ejfno getEj() {
    // 	Map<String, Integer> args = new HashMap<>();
    // 	userDefineExtMapper.getEj(args);
    // 	Ejfno ejfno = new Ejfno();
    // 	ejfno.setEjfno(args.get(OUT_ID));
    // 	ejfno.setInterval(args.get(OUT_INTERVAL));
    // 	return ejfno;
    // }

    // 2022-11-22 Richard marked
    // Stan的獲取方式改為從Sequence中取, 所以這個方法mark掉
    // public Stan getStan() {
    // 	Map<String, Integer> args = new HashMap<>();
    // 	userDefineExtMapper.getStan(args);
    // 	Stan stan = new Stan();
    // 	stan.setStan(args.get(OUT_ID));
    // 	stan.setInterval(args.get(OUT_INTERVAL));
    // 	return stan;
    // }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getPbmrByIdNo(String idno, String startday) {
        Map<String, Object> args = new HashMap<>();
        args.put("IDNO", idno);
        args.put("STARTDAY", startday);
        userDefineExtMapper.getPbmrByIdNo(args);
        return (List<Map<String, Object>>) args.get(OUT_RESULT);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> queryIbtId(String idno, String startday) {
        Map<String, Object> args = new HashMap<>();
        args.put("IDNO", idno);
        args.put("STARTDAY", startday);
        userDefineExtMapper.queryIbtId(args);
        return (List<Map<String, Object>>) args.get(OUT_RESULT);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getBoxByIdNo(String idno, String startday) {
        Map<String, Object> args = new HashMap<>();
        args.put("IDNO", idno);
        args.put("STARTDAY", startday);
        userDefineExtMapper.getBoxByIdNo(args);
        return (List<Map<String, Object>>) args.get(OUT_RESULT);
    }

    public Long getAtmTxSeq(String atmstatAtmno) {
        Map<String, Object> args = new HashMap<>();
        args.put("ATMNO", atmstatAtmno);
        userDefineExtMapper.getAtmTxSeq(args);
        return ((Integer) args.get(OUT_NEXTID)).longValue();
    }

    public void decrementRMFISCOUT4NO(Rmfiscout4 defRMFISCOUT4) {
        Map<String, Object> args = new HashMap<>();
        args.put("RECEIVERBANKATMNO", defRMFISCOUT4.getRmfiscout4ReceiverBank());
        args.put("SENDERBANK", defRMFISCOUT4.getRmfiscout4SenderBank());
        userDefineExtMapper.decrementRMFISCOUT4NO(args);
        defRMFISCOUT4.setRmfiscout4No((Integer) args.get("PREID"));
    }

    public Integer getRMFISCOUT4NO(Rmfiscout4 defRMFISCOUT4) {
        Map<String, Object> args = new HashMap<>();
        args.put("RECEIVERBANK", defRMFISCOUT4.getRmfiscout4ReceiverBank());
        args.put("SENDERBANK", defRMFISCOUT4.getRmfiscout4SenderBank());
        userDefineExtMapper.getRMFISCOUT4NO(args);
        defRMFISCOUT4.setRmfiscout4No((Integer) args.get(OUT_NEXTID));
        return ((Integer) args.get(OUT_NEXTID));
    }

    public Integer getRMNO(Rmnoctl defRmnoctl) {
        Map<String, Object> args = new HashMap<>();
        args.put("brno", defRmnoctl.getRmnoctlBrno());
        args.put("category", defRmnoctl.getRmnoctlCategory());
        userDefineExtMapper.getRMNO(args);
        return ((Integer) args.get(OUT_NEXTID));
    }

    public Long getRMFISCOUT1NO(Rmfiscout1 rmfiscout1, boolean isRepNo) {
        Map<String, Object> args = new HashMap<>();
        args.put("RECEIVERBANK", rmfiscout1.getRmfiscout1ReceiverBank());
        args.put("SENDERBANK", rmfiscout1.getRmfiscout1SenderBank());
        if (isRepNo) {
            args.put("ISREP", "1");
        } else {
            args.put("ISREP", "0");
        }
        args.put("NEXTID", 0);
        userDefineExtMapper.getRMFISCOUT1NO(args);
        return ((Integer) args.get(OUT_NEXTID)).longValue();
    }

    public Long getRMOUTSNONO(Rmoutsno rmoutsno, boolean isRepNo) {
        Map<String, Object> args = new HashMap<>();
        args.put("RECEIVERBANK", rmoutsno.getRmoutsnoReceiverBank());
        args.put("SENDERBANK", rmoutsno.getRmoutsnoSenderBank());
        if (isRepNo) {
            args.put("ISREP", "1");
        } else {
            args.put("ISREP", "0");
        }
        args.put("NEXTID", 0);
        userDefineExtMapper.getRMOUTSNONO(args);
        return ((Integer) args.get(OUT_NEXTID)).longValue();
    }

    public Long getRMFISCOUT4NO(Rmfiscout4 rmfiscout4, boolean isRepNo) {
        Map<String, Object> args = new HashMap<>();
        args.put("RECEIVERBANK", rmfiscout4.getRmfiscout4ReceiverBank());
        args.put("SENDERBANK", rmfiscout4.getRmfiscout4SenderBank());
        args.put("NEXTID", 0);
        userDefineExtMapper.getRMFISCOUT4NO(args);
        return ((Integer) args.get(OUT_NEXTID)).longValue();
    }

    public Long getFCRMNO(Fcrmnoctl fcrmnoctl) {
        Map<String, Object> args = new HashMap<>();
        args.put("BRNO", fcrmnoctl.getFcrmnoctlBrno());
        args.put("CATEGORY", fcrmnoctl.getFcrmnoctlCategory());
        args.put("NEXTID", 0);
        userDefineExtMapper.getFCRMNO(args);
        return ((Integer) args.get(OUT_NEXTID)).longValue();
    }

    public Long getFCRMOUTSNO(Fcrmoutsno fcrmoutsno) {
        Map<String, Object> args = new HashMap<>();
        args.put("RECEIVERBANK", fcrmoutsno.getFcrmoutsnoReceiverBank());
        args.put("SENDERBANK", fcrmoutsno.getFcrmoutsnoSenderBank());
        args.put("NEXTID", 0);
        userDefineExtMapper.getFCRMOUTSNO(args);
        return ((Integer) args.get(OUT_NEXTID)).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> queryAllMembersByBossID(String loginID) {
        Map<String, Object> args = new HashMap<>();
        args.put("BOSSID", loginID);
        return userDefineExtMapper.queryAllMembers(args);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> queryAllMembers(String loginID) {
        Map<String, Object> args = new HashMap<>();
        args.put("BOSSID", loginID);
        return userDefineExtMapper.queryAllMembers(args);
    }

    @SuppressWarnings("unchecked")
    public List<HashMap<String, Object>> queryAllAuditData(Webaudit audit, String dtbegin, String dtend, String bossid, String programid, String displayShowAudit, Integer pageNum, Integer pageSize, String sqlSortExpression) throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("AUDITTIMESTR", dtbegin);
        args.put("AUDITTIMEEND", dtend);
        if (StringUtils.isNotBlank(audit.getAudituser())) {
            args.put("USERID", audit.getAudituser());
        } else {
            args.put("USERID", "");
            args.put("BOSSID", bossid);
        }
        args.put("PROGRAMID", programid);
        args.put("PROGRAMNAME", audit.getAuditprogramname());
        args.put("SHOWAUDIT", displayShowAudit);
        args.put("SQLSORTEXPRESSION", sqlSortExpression);
        List<HashMap<String, Object>> listdata = null;
        if (StringUtils.isNotBlank(bossid)) {
            listdata = userDefineExtMapper.queryDirectReports(args);
        } else {
            listdata = userDefineExtMapper.queryWebAudit(args);
        }
        if (listdata != null) {
            for (HashMap<String, Object> map : listdata) {
                map.compute("showAudit", (k, showAudit) -> showAudit != null ? (DbHelper.toBoolean(Short.parseShort(showAudit.toString())) ? "有" : "無") : StringUtils.EMPTY);
                map.compute("auditData", (k, auditData) -> {
                    try {
                        return auditData != null ? DB2Util.getClobValue(auditData, StringUtils.EMPTY) : StringUtils.EMPTY;
                    } catch (Exception e) {
                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                        return StringUtils.EMPTY;
                    }
                });
                map.compute("SQLCOMMANDTEXT", (k, sqlcommandText) -> {
                    try {
                        return sqlcommandText != null ? DB2Util.getClobValue(sqlcommandText, StringUtils.EMPTY) : StringUtils.EMPTY;
                    } catch (Exception e) {
                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                        return StringUtils.EMPTY;
                    }
                });
                map.compute("auditTime", (k, auditTime) -> auditTime != null ? FormatUtil.dateTimeFormat(CalendarUtil.clone((Date) auditTime), FormatUtil.FORMAT_DATE_YYYY_MM_DD_HH_MM_SS) : StringUtils.EMPTY);
            }
        }
        return listdata;
    }

}