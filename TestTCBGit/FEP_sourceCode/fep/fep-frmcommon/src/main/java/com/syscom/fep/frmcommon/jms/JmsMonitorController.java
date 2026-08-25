package com.syscom.fep.frmcommon.jms;

import com.syscom.fep.frmcommon.jms.entity.JmsInfoConcurrency;
import com.syscom.fep.frmcommon.jms.entity.JmsMonitorConcurrencyRequest;
import com.syscom.fep.frmcommon.jms.entity.JmsMonitorResponse;
import com.syscom.fep.frmcommon.jms.entity.JmsMonitorResponseErrorCode;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JmsMonitorController {
    private final static LogHelper logger = new LogHelper();
    private final static Map<String, JmsNotifier<?, ?>> jmsNotifierMap = Collections.synchronizedMap(new HashMap<>());

    static {
        SpringBeanFactoryUtil.registerController(JmsMonitorController.class);
        logger.info("####################Registered as SpringBean Controller succeed!!!");
    }

    /**
     * 增加notifier
     *
     * @param identity
     * @param notifier
     */
    public static void addNotifier(String identity, JmsNotifier<?, ?> notifier) {
        if (jmsNotifierMap.containsKey(identity)) {
            RuntimeException e = ExceptionUtil.createRuntimeException(logger.error("[addNotifier]cannot add notifier again cause exist, identity = [", identity, "]"));
            logger.error(e, e.getMessage());
            throw e;
        } else {
            jmsNotifierMap.put(identity, notifier);
            logger.info("[addNotifier]identity = [", identity, "], notifier = [", notifier, "]");
        }
    }

    /**
     * 移除notifier
     *
     * @param identity
     */
    public static void removeNotifier(String identity) {
        JmsNotifier<?, ?> notifier = jmsNotifierMap.remove(identity);
        if (notifier == null) {
            logger.warn("[removeNotifier]cannot remove notifier cause not exist, identity = [", identity, "]");
        } else {
            logger.info("[removeNotifier]identity = [", identity, "]");
        }
    }

    /**
     * 獲取並發數
     * <p>
     * curl -X POST http://localhost:8080/consumers/{identity}/getConcurrency
     *
     * @return
     */
    @RequestMapping(value = "/consumers/{identity}/getConcurrency", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JmsMonitorResponse<JmsInfoConcurrency> getConcurrency(@PathVariable("identity") String identity) {
        JmsMonitorResponse<JmsInfoConcurrency> response = new JmsMonitorResponse<>();
        JmsNotifier<?, ?> jmsNotifier = jmsNotifierMap.get(identity);
        if (jmsNotifier != null) {
            response.setData(jmsNotifier.getConcurrency());
        } else {
            response.setError("cannot get concurrency cause consumers is null!!!");
            response.setErrorCode(JmsMonitorResponseErrorCode.CONSUMERS_NOT_EXIST);
        }
        return response;
    }

    /**
     * 設置並發數
     * <p>
     * curl -H "Content-Type:application/json" -d "{\"data\":\"10\"}" -X POST http://localhost:8080/consumers/{identity}/setConcurrency
     *
     * @param identity
     * @param request
     * @return
     */
    @RequestMapping(value = "/consumers/{identity}/setConcurrency", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JmsMonitorResponse<JmsInfoConcurrency> setConcurrency(@PathVariable("identity") String identity, @RequestBody JmsMonitorConcurrencyRequest request) {
        JmsMonitorResponse<JmsInfoConcurrency> response = new JmsMonitorResponse<>();
        if (request == null || StringUtils.isBlank(request.getConcurrency())) {
            response.setError("concurrency cannot be empty!!!");
            response.setErrorCode(JmsMonitorResponseErrorCode.INVALID_PARAMETER_CONCURRENCY_CANNOT_BE_EMPTY);
        } else {
            JmsNotifier<?, ?> jmsNotifier = jmsNotifierMap.get(identity);
            if (jmsNotifier != null) {
                try {
                    if (jmsNotifier.setConcurrency(request)) {
                        // 設置成功, 取一次當前的值
                        response.setData(jmsNotifier.getConcurrency());
                    }else{
                        response.setError("set concurrency failed!!!");
                        response.setErrorCode(JmsMonitorResponseErrorCode.SET_CONCURRENCY_FAILED);
                    }
                } catch (Exception e) {
                    response.setError(e.getMessage());
                    if (StringUtils.isNotBlank(e.getMessage())) {
                        if (e.getMessage().contains(JmsMonitorResponseErrorCode.CANNOT_REDUCE_CONCURRENCY.name())) {
                            response.setErrorCode(JmsMonitorResponseErrorCode.CANNOT_REDUCE_CONCURRENCY);
                        }
                        if (e.getMessage().contains(JmsMonitorResponseErrorCode.SET_CONCURRENCY_OVER_MAX_THREADS_RATIO.name())) {
                            response.setError(StringUtils.replace(e.getMessage(), JmsMonitorResponseErrorCode.SET_CONCURRENCY_OVER_MAX_THREADS_RATIO.name(), StringUtils.EMPTY));
                            response.setErrorCode(JmsMonitorResponseErrorCode.SET_CONCURRENCY_OVER_MAX_THREADS_RATIO);
                        }
                    } else {
                        response.setErrorCode(JmsMonitorResponseErrorCode.SET_CONCURRENCY_FAILED);
                    }
                }
            } else {
                response.setError("cannot set concurrency cause consumers is null!!!");
                response.setErrorCode(JmsMonitorResponseErrorCode.CONSUMERS_NOT_EXIST);
            }
        }
        return response;
    }

    /**
     * 開始接收Queue
     * <p>
     * curl -X POST http://localhost:8080/consumers/{identity}/startReceive
     *
     * @return
     */
    @RequestMapping(value = "/consumers/{identity}/startReceive", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JmsMonitorResponse<String> startReceive(@PathVariable("identity") String identity) {
        JmsMonitorResponse<String> response = new JmsMonitorResponse<>();
        JmsNotifier<?, ?> jmsNotifier = jmsNotifierMap.get(identity);
        if (jmsNotifier != null) {
            jmsNotifier.start();
            response.setData("OK");
        } else {
            response.setError("cannot start cause consumers is null!!!");
            response.setErrorCode(JmsMonitorResponseErrorCode.CONSUMERS_NOT_EXIST);
        }
        return response;
    }

    /**
     * 停止接收Queue
     * <p>
     * curl -X POST http://localhost:8080/consumers/{identity}/stopReceive
     *
     * @return
     */
    @RequestMapping(value = "/consumers/{identity}/stopReceive", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JmsMonitorResponse<String> stopReceive(@PathVariable("identity") String identity) {
        JmsMonitorResponse<String> response = new JmsMonitorResponse<>();
        JmsNotifier<?, ?> jmsNotifier = jmsNotifierMap.get(identity);
        if (jmsNotifier != null) {
            jmsNotifier.stop();
            response.setData("OK");
        } else {
            response.setError("cannot start cause consumers is null!!!");
            response.setErrorCode(JmsMonitorResponseErrorCode.CONSUMERS_NOT_EXIST);
        }
        return response;
    }
}
