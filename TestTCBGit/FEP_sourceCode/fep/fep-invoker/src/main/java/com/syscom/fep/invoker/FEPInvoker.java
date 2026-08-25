package com.syscom.fep.invoker;

import com.syscom.fep.base.FEPBaseMethod;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.vo.communication.BaseCommu;
import com.syscom.fep.vo.communication.ToCBSCommu;
import com.syscom.fep.vo.communication.ToFISCCommu;
import com.syscom.fep.vo.enums.ClientType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class FEPInvoker extends FEPBaseMethod {
    @Autowired
    private FEPConfig fepConfig;
    @Autowired
    private RestfulClientFactory restfulClientFactory;
    @Autowired
    private SimpleNettyClientFactory simpleNettyClientFactory;

    /**
     * 丟訊息給FEP ATM
     *
     * @param request
     * @param timeout
     * @return
     * @throws Exception
     */
    public String sendReceiveToFEPATM(BaseCommu request, int timeout) throws Exception {
        String rtn;
        switch (fepConfig.getAtmProtocol()) {
            case restful:
                rtn = restfulClientFactory.sendReceive(ClientType.TO_FEP_ATM, fepConfig.getAtmURL(), request, timeout);
                break;
            case socket:
                rtn = simpleNettyClientFactory.sendReceive(ClientType.TO_FEP_ATM, request, timeout);
                break;
            default:
                throw ExceptionUtil.createIllegalArgumentException("Unsupported Protocol = [", fepConfig.getAtmProtocol().name(), "]");
        }
        return rtn;
    }

    public String sendReceiveToFEPATMMON(BaseCommu request, int timeout, String url) throws Exception {
        String rtn;
        switch (fepConfig.getAtmProtocol()) {
            case restful:
                rtn = restfulClientFactory.sendReceive(ClientType.TO_FEP_ATMMON, url, request, timeout);
                break;
            default:
                throw ExceptionUtil.createIllegalArgumentException("Unsupported Protocol = [", fepConfig.getAtmProtocol().name(), "]");
        }
        return rtn;
    }

    public String sendReceiveToFEPATMMONForAtmmon(BaseCommu request, int timeout, String url) throws Exception {
        String rtn;
        rtn = restfulClientFactory.sendReceive(ClientType.TO_FEP_ATMMON, url, request, timeout);
        return rtn;
    }

    /**
     * 丟訊息給FEP FISC
     *
     * @param request
     * @param timeout
     * @return
     * @throws Exception
     */
    public String sendReceiveToFEPFISC(BaseCommu request, int timeout) throws Exception {
        String rtn;
        switch (fepConfig.getFiscProtocol()) {
            case restful:
                rtn = restfulClientFactory.sendReceive(ClientType.TO_FEP_FISC, fepConfig.getFiscURL(), request, timeout);
                break;
            case socket:
                rtn = simpleNettyClientFactory.sendReceive(ClientType.TO_FEP_FISC, request, timeout);
                break;
            default:
                throw ExceptionUtil.createIllegalArgumentException("Unsupported Protocol = [", fepConfig.getFiscProtocol().name(), "]");
        }
        return rtn;
    }

    /**
     * 丟訊息給FISC GW
     *
     * @param request
     * @param timeout
     * @return
     * @throws Exception
     */
    public String sendReceiveToFISCGW(ToFISCCommu request, int timeout) throws Exception {
        String rtn;
        switch (request.getProtocol()) {
            case restful:
                rtn = restfulClientFactory.sendReceive(ClientType.TO_FISC_GW, request.getRestfulUrl(), request, timeout);
                break;
            case socket:
                rtn = simpleNettyClientFactory.sendReceive(ClientType.TO_FISC_GW, request, timeout);
                break;
            default:
                throw ExceptionUtil.createIllegalArgumentException("Unsupported Protocol = [", fepConfig.getFiscProtocol().name(), "]");
        }
        return rtn;
    }

    /**
     * 丟訊息到InQueue11X1
     *
     * @param request
     * @param timeout
     * @return
     * @throws Exception
     */
    public String sendReceiveToInQueue11X1(String request, int timeout) throws Exception {
        String rtn = restfulClientFactory.sendReceive(ClientType.InQueue11X1, fepConfig.getInQueue11X1URL(), request, timeout);
        return rtn;
    }

    /**
     * 丟訊息給CBS GW
     *
     * @param request
     * @param timeout
     * @return
     * @throws Exception
     */
    public String sendReceiveToCBSGW(ToCBSCommu request, int timeout) throws Exception {
        String rtn = simpleNettyClientFactory.sendReceive(ClientType.TO_CBS_GW, request, timeout);
        return rtn;
    }
}
