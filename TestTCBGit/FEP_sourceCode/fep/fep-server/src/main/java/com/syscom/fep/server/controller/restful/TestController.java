package com.syscom.fep.server.controller.restful;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.io.StreamGobbler;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CommandLineUtil;
import com.syscom.fep.server.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.function.Consumer;

public class TestController extends BaseController {
    private final LogHelper logger = LogHelperFactory.getTraceLogger();

    /**
     * curl -H "Content-Type:application/json" -d "Hello World" -X POST http://localhost:8080/recv/test
     *
     * @param messageIn
     * @return
     */
    @RequestMapping(value = "/recv/test", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String sendReceive(@RequestBody String messageIn) {
        // Restful進來的電文
        return this.processRequestData(ProgramFlow.RESTFulIn, messageIn);
    }

    /**
     * 處理進來的電文並回應
     *
     * @param programFlow
     * @param messageIn
     * @return
     */
    @Override
    protected String processRequestData(ProgramFlow programFlow, String messageIn) {
        logger.debug(Const.MESSAGE_IN, messageIn);
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//           logger.error(e, e.getMessage());
//        }
        runProcess("E:\\home\\tcb\\fep-app\\fep-gateway-atm\\fep-gateway-atm-run.bat");
        // runProcess("ping 127.0.0.1 -t");
        String messageOut = messageIn + StringUtils.SPACE + Const.REPLY_OK;
        logger.debug(Const.MESSAGE_OUT, messageIn);
        return messageOut;
    }

    private void runProcess(String command) {
        Process process = null;
        try {
            process = CommandLineUtil.getProcess(command.split("\\s+"));
            Consumer<String> consumer = logger::debug;
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(),"GBK", consumer);
            Thread t = new Thread(streamGobbler);
            // t.setDaemon(true);
            t.start();
            logger.info("Execute successful, command = [", command, "]");
        } catch (Throwable e) {
            logger.exceptionMsg(e, "Execute command = [", command, "] with exception occur, ", e.getMessage());
        }
    }
}
