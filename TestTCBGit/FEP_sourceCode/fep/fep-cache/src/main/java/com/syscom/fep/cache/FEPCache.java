package com.syscom.fep.cache;

import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.*;
import com.syscom.fep.mybatis.model.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class FEPCache {
    private static final String CLASS_NAME = FEPCache.class.getSimpleName();
    private static final LogHelper TRACELogger = LogHelperFactory.getTraceLogger();

    private static final List<Curcd> curcdList = new ArrayList<>();
    private static final List<Msgctl> msgctlList = new ArrayList<>();
    private static final List<Channel> channelList = new ArrayList<>();
    private static final List<Zone> zoneList = new ArrayList<>();
    private static final Map<Integer, List<Sysconf>> subsysnoToSysconfListMap = new HashMap<>();
    private static final RefBase<Sysstat> sysstat = new RefBase<>(null);

    private static final MsgctlExtMapper msgctlExtMapper = SpringBeanFactoryUtil.getBean(MsgctlExtMapper.class);
    private static final ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);
    private static final SysstatExtMapper sysstatExtMapper = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
    private static final SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
    private static final CurcdExtMapper curcdExtMapper = SpringBeanFactoryUtil.getBean(CurcdExtMapper.class);
    private static final ChannelExtMapper channelExtMapper = SpringBeanFactoryUtil.getBean(ChannelExtMapper.class);

    private FEPCache() {}

    public static void reloadCache(CacheItem cacheItem) throws Exception {
        switch (cacheItem) {
            case ALL:
                for (CacheItem item : CacheItem.values()) {
                    if (item == CacheItem.ALL) {
                        continue;
                    }
                    reloadCache(item);
                }
                break;
            case SYSSTAT:
                reloadSysstat();
                break;
            case MSGCTL:
                reloadMsgctlList();
                break;
            case ZONE:
                reloadZones();
                break;
            case CURCD:
                reloadCurcdList();
                break;
            case SYSCONF:
                reloadSysconfMap();
                break;
        }
    }

    public static List<Msgctl> getMsgctlList() {
        synchronized (msgctlList) {
            if (msgctlList.isEmpty()) {
                reloadMsgctlList();
            } else {
                TRACELogger.trace(CLASS_NAME, " Get MSGCTL From Cache");
            }
            return msgctlList;
        }
    }

    public static List<Curcd> getCurcdList() {
        synchronized (curcdList) {
            if (curcdList.isEmpty()) {
                reloadCurcdList();
            } else {
                TRACELogger.trace(CLASS_NAME, " Get CURCD From Cache");
            }
            return curcdList;
        }
    }

    public static Sysstat getSysstat() throws Exception {
        synchronized (sysstat) {
            if (sysstat.get() == null) {
                reloadSysstat();
            } else {
                TRACELogger.trace(CLASS_NAME, " Get SYSSTAT From Cache");
            }
            return sysstat.get();
        }
    }

    public static Msgctl getMsgctrl(String msgId) {
        return msgctlExtMapper.selectByPrimaryKey(msgId);
    }

    public static Msgctl getMsgctrlForIMS(String msgId) {
        return msgctlExtMapper.selectByLikeMsgidNotFisc(msgId);
    }

    public static Zone getZone(String zoneCode) {
        List<Zone> zoneList = FEPCache.getZoneList();
        Zone[] zones = new Zone[zoneList.size()];
        zoneList.toArray(zones);
        Zone zone = null;
        for (Zone z : zones) {
            zone = z;
            if (z.getZoneCode().equals(zoneCode)) {
                break;
            }
        }
        return zone;
    }

    private static List<Zone> getZoneList() {
        synchronized (zoneList) {
            if (zoneList.isEmpty()) {
                reloadZones();
            } else {
                TRACELogger.trace(CLASS_NAME, " Get ZONE From Cache");
            }
            return zoneList;
        }
    }

    public static List<Sysconf> getSysconfList(int subsys) {
        synchronized (subsysnoToSysconfListMap) {
            if (subsysnoToSysconfListMap.isEmpty()) {
                reloadSysconfMap();
            } else {
                TRACELogger.trace(CLASS_NAME, " Get SYSCONF by subsys = [", subsys, "] From Cache");
            }
            return subsysnoToSysconfListMap.get(subsys);
        }
    }

    public static Channel getChannel(String channelName) {
        List<Channel> channelList = getChannelList();
        Channel[] channels = new Channel[channelList.size()];
        channelList.toArray(channels);
        for (Channel channel : channels) {
            if (channel.getChannelName().equals(channelName)) {
                return channel;
            }
        }
        return null;
    }

    private static List<Channel> getChannelList() {
        synchronized (channelList) {
            reloadChannelList();
            return channelList;
        }
    }

    // ============================================== Reload Cache Start ==============================================

    private static void reloadSysstat() throws Exception {
        synchronized (sysstat) {
            TRACELogger.trace(CLASS_NAME, " Get SYSSTAT From DB");
            List<Sysstat> sysstatList = sysstatExtMapper.selectAll();
            if (CollectionUtils.isEmpty(sysstatList)) {
                throw ExceptionUtil.createException("無法取得SYSSTAT資料");
            }
            sysstat.set(sysstatList.get(0));
        }
    }

    private static void reloadMsgctlList() {
        synchronized (msgctlList) {
            TRACELogger.trace(CLASS_NAME, " Get MSGCTL From DB");
            List<Msgctl> list = msgctlExtMapper.selectAll();
            msgctlList.clear();
            msgctlList.addAll(list);
        }
    }

    //    20230322 Bruce 先註解掉
    private static void reloadZones() {
        synchronized (zoneList) {
            TRACELogger.trace(CLASS_NAME, " Get ZONE From DB");
            List<Zone> list = zoneExtMapper.selectAll();
            zoneList.clear();
            zoneList.addAll(list);
        }
    }

    private static void reloadSysconfMap() {
        synchronized (subsysnoToSysconfListMap) {
            TRACELogger.trace(CLASS_NAME, " Get SYSCONF From DB");
            List<Sysconf> sysconfList = sysconfExtMapper.queryAllData(StringUtils.EMPTY);
            List<Short> subsysnoList = sysconfList.stream().map(Sysconf::getSysconfSubsysno).distinct().collect(Collectors.toList());
            subsysnoToSysconfListMap.clear();
            for (short subsysno : subsysnoList) {
                List<Sysconf> sysconfForSubsysnoList = new ArrayList<>();
                for (Sysconf sysconf : sysconfList) {
                    if (subsysno == sysconf.getSysconfSubsysno()) {
                        if (DbHelper.toBoolean(sysconf.getSysconfEncrypt()) && sysconf.getSysconfValue() != null) {
                            byte[] base64 = Base64.getDecoder().decode(sysconf.getSysconfValue());
                            sysconf.setSysconfValue(ConvertUtil.toString(base64, PolyfillUtil.toCharsetName("950")));
                        }
                        sysconfForSubsysnoList.add(sysconf);
                    }
                }
                subsysnoToSysconfListMap.put((int) subsysno, sysconfForSubsysnoList);
            }
        }
    }

    private static void reloadCurcdList() {
        synchronized (curcdList) {
            TRACELogger.trace(CLASS_NAME, " Get CURCD From DB");
            List<Curcd> list = curcdExtMapper.selectAll();
            curcdList.clear();
            curcdList.addAll(list);
        }
    }

    private static void reloadChannelList() {
        synchronized (channelList) {
            TRACELogger.trace(CLASS_NAME, " Get Channel From DB");
            List<Channel> list = channelExtMapper.queryChannelOptions();
            channelList.clear();
            channelList.addAll(list);
        }
    }

    // ============================================== Reload Cache End ==============================================
}
