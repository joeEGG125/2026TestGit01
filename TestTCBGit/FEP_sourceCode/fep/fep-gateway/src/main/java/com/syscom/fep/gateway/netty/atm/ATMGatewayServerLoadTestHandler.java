package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.util.GatewayUtil;
import com.syscom.fep.vo.communication.ToATMCommuAtmmstr;
import com.syscom.fep.vo.communication.ToATMCommuAtmmstrList;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用於壓測模式下數據處理
 *
 * @author Richard
 */
public class ATMGatewayServerLoadTestHandler {
    private static final LogHelper logger = LogHelperFactory.getTraceLogger();
    private static final ATMGatewayServerConfiguration configuration = SpringBeanFactoryUtil.getBean(ATMGatewayServerConfiguration.class);
    private static final GatewayUtil gatewayUtil = SpringBeanFactoryUtil.getBean(GatewayUtil.class);
    private static final List<ToATMCommuAtmmstr> list = Collections.synchronizedList(new ArrayList<>());

    private ATMGatewayServerLoadTestHandler() {}

    /**
     * 初始化
     */
    public static void initialize() {
        if (configuration.isLoadTestMode()) {
            // 載入所有的fepConnection=1的ATM主檔資料
            ToATMCommuAtmmstrList toATMCommuAtmmstrList = ATMGatewayServerToFEPATM.getInstance().getAtmmstrList(new LogData(), (short) 1, gatewayUtil.getTimeout(configuration));
            if (toATMCommuAtmmstrList != null && CollectionUtils.isNotEmpty(toATMCommuAtmmstrList.getAtmmstrs())) {
                for (ToATMCommuAtmmstrList.ToATMCommuAtmmstr atmmstr : toATMCommuAtmmstrList.getAtmmstrs()) {
                    ToATMCommuAtmmstr target = new ToATMCommuAtmmstr();
                    BeanUtils.copyProperties(atmmstr, target);
                    list.add(target);
                }
                logger.debug("[ATMGatewayServerLoadTestHandler][initialize]Load ATMMSTR succeed, size = [", list.size(), "]");
            }
        }
    }

    /**
     * 銷毀
     */
    public static void preDestroy() {
        if (configuration.isLoadTestMode()) {
            list.clear();
        }
    }

    /**
     * 獲取一筆ATM主檔資料
     *
     * @param identity
     * @return
     */
    public static ToATMCommuAtmmstr fetchATMMSTR(String identity) {
        if (configuration.isLoadTestMode()) {
            if (!list.isEmpty()) {
                ToATMCommuAtmmstr atmmstr = list.remove(0);
                logger.debug("[ATMGatewayServerLoadTestHandler][fetchATMMSTR][", identity, "]fetch ATMMSTR succeed, atmAtmno:", atmmstr.getAtmAtmno(), ", size:", list.size());
                return atmmstr;
            }
        }
        return null;
    }

    /**
     * 歸還一筆ATM主檔資料
     *
     * @param atmmstr
     */
    public static void returnATMMSTR(ToATMCommuAtmmstr atmmstr) {
        if (configuration.isLoadTestMode()) {
            if (atmmstr != null) {
                list.add(atmmstr);
                logger.debug("[ATMGatewayServerLoadTestHandler][returnATMMSTR]return ATMMSTR succeed, atmAtmno:", atmmstr.getAtmAtmno(), ", size:", list.size());
            }
        }
    }
}
