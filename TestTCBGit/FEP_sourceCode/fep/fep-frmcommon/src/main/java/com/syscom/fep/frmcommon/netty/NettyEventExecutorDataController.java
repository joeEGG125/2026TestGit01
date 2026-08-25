package com.syscom.fep.frmcommon.netty;

import com.syscom.fep.frmcommon.communication.BaseResponse;
import com.syscom.fep.frmcommon.esapi.ESAPIValidator;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NettyEventExecutorDataController {
    private static final String PROGRAM_NAME = NettyEventExecutorDataController.class.getSimpleName();
    private static final LogHelper logger = new LogHelper();
    private final static Map<String, NettyEventExecutorDataHandler> handlerMap = Collections.synchronizedMap(new HashMap<>());

    static {
        SpringBeanFactoryUtil.registerController(NettyEventExecutorDataController.class);
        logger.info("####################Registered [", PROGRAM_NAME, "] as SpringBean Controller succeed!!!");
    }

    /**
     * 增加NettyTransmissionStatisticsDataCollector
     *
     * @param identity
     * @param handler
     */
    public static void addCollector(String identity, NettyEventExecutorDataHandler handler) {
        if (handlerMap.containsKey(identity)) {
            RuntimeException e = ExceptionUtil.createRuntimeException(logger.error("[addCollector]cannot add handler again cause exist, identity = [", identity, "]"));
            logger.error(e, e.getMessage());
            throw e;
        } else {
            handlerMap.put(identity, handler);
            logger.info("[addCollector]identity = [", identity, "], handler = [", handler, "]");
        }
    }

    /**
     * 移除NettyTransmissionStatisticsDataCollector
     *
     * @param identity
     */
    public static void removeCollector(String identity) {
        NettyEventExecutorDataHandler handler = handlerMap.remove(identity);
        if (handler == null) {
            logger.warn("[removeCollector]cannot remove handler cause not exist, identity = [", identity, "]");
        } else {
            logger.info("[removeCollector]identity = [", identity, "]");
        }
    }

    /**
     * 獲取並發數
     * <p>
     * curl -X POST http://localhost:8080/recv/{identity}/getEventExecutorData
     *
     * @return
     */
    @RequestMapping(value = "/recv/{identity}/getEventExecutorData", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BaseResponse<NettyEventExecutorData, NettyEventExecutorDataErrorCode> getConcurrency(@PathVariable("identity") String identity) {
        BaseResponse<NettyEventExecutorData, NettyEventExecutorDataErrorCode> response = new BaseResponse<>();
        NettyEventExecutorDataHandler handler = handlerMap.get(identity);
        if (handler != null) {
            response.setData(handler.getNettyEventExecutorData());
        } else {
            response.setError("Cannot get Netty Event Executor Data cause handler is null!!!");
            response.setErrorCode(NettyEventExecutorDataErrorCode.HANDLER_NOT_EXIST);
        }
        return response;
    }

    /**
     * 設定並發數
     * <p>
     * curl -H "Content-Type:application/json" -d "{\"data\":\"10\"}" -X POST http://localhost:8080/recv/{identity}/setEventExecutorThreads
     *
     * @return
     */
    @RequestMapping(value = "/recv/{identity}/setEventExecutorThreads", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BaseResponse<NettyEventExecutorData, NettyEventExecutorDataErrorCode> setEventExecutorThreads(@PathVariable("identity") String identity, @RequestBody NettyEventExecutorRequest request) {
        BaseResponse<NettyEventExecutorData, NettyEventExecutorDataErrorCode> response = new BaseResponse<>();
        if (request == null) {
            response.setError("Invalid Input!!!");
            response.setErrorCode(NettyEventExecutorDataErrorCode.INVALID_INPUT);
            return response;
        }
        // 2024-11-05 Richard modified for 【Unchecked Input for Loop Condition】
        Integer nThreads = ESAPIValidator.getValidInteger(Integer.toString(request.getThreads()), 0, 50000, false);
        if (nThreads == null) {
            response.setError("Invalid Input!!!");
            response.setErrorCode(NettyEventExecutorDataErrorCode.INVALID_INPUT);
            return response;
        }
        NettyEventExecutorDataHandler handler = handlerMap.get(identity);
        if (handler != null) {
            try {
                // 先檢查線程數設定
                BaseResponse<?, NettyEventExecutorDataErrorCode> checkResponse = handler.checkEventExecutorThreads(nThreads);
                if (!checkResponse.isResult()) {
                    response.setError(checkResponse.getError());
                    response.setErrorCode(checkResponse.getErrorCode());
                    return response;
                }
                // 如果確定要修改線程數, 則進行修改
                if (request.isModified()) {
                    boolean result = handler.setNettyEventExecutorThreads(nThreads);
                    // 2024-11-18 Richard modified for 【Unchecked Input for Loop Condition】
                    // boolean result = ReflectUtil.envokeMethod(handler, "setNettyEventExecutorThreads", new Class[] {int.class}, new Object[] {nThreads}, false);
                    if (!result) {
                        response.setError("Set Netty Event Executor Threads failed!!!");
                        response.setErrorCode(NettyEventExecutorDataErrorCode.SET_EVENT_EXECUTOR_THREADS_FAILED);
                    }
                }
            } catch (Exception e) {
                response.setError(e.getMessage());
                if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains(NettyEventExecutorDataErrorCode.CANNOT_REDUCE_EVENT_EXECUTOR_THREADS.name())) {
                    response.setErrorCode(NettyEventExecutorDataErrorCode.CANNOT_REDUCE_EVENT_EXECUTOR_THREADS);
                } else {
                    response.setErrorCode(NettyEventExecutorDataErrorCode.SET_EVENT_EXECUTOR_THREADS_FAILED);
                }
            }
            // 無論是否設置成功與否, 都取一次當前的值
            response.setData(handler.getNettyEventExecutorData());
        } else {
            response.setError("cannot get statistics data cause handler is null!!!");
            response.setErrorCode(NettyEventExecutorDataErrorCode.HANDLER_NOT_EXIST);
        }
        return response;
    }
}
