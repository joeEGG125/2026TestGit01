package com.syscom.fep.batch.job;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.batch.base.vo.restful.request.ListSchedulerRequest;
import com.syscom.fep.batch.base.vo.restful.request.OperateSchedulerRequest;
import com.syscom.fep.batch.base.vo.restful.response.ListSchedulerResponse;
import com.syscom.fep.batch.base.vo.restful.response.OperateSchedulerResponse;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.owasp.esapi.ESAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class BatchJobController extends FEPBase {
    private static final String CLASS_NAME = BatchJobController.class.getSimpleName();
    private static final LogHelper logger = LogHelperFactory.getGeneralLogger();
    @Autowired
    private BatchJobManager manager;

    @RequestMapping(value = "/listScheduler", method = RequestMethod.POST, produces = "application/xml;charset=UTF-8")
    @ResponseBody
    public String listScheduler(@RequestBody String messageIn) {
        messageIn = ESAPI.encoder().decodeForHTML(messageIn); // 21023-09-18 Richard add 這裡要轉義一下
        logger.info("[", CLASS_NAME, "][listScheduler]", Const.MESSAGE_IN, messageIn);
        ListSchedulerRequest request = deserializeFromXml(messageIn, ListSchedulerRequest.class);
        ListSchedulerResponse response = manager.listScheduler(request);
        String messageOut = serializeToXml(response);
        logger.info("[", CLASS_NAME, "][listScheduler]", Const.MESSAGE_OUT, messageOut);
        messageOut = ESAPI.encoder().encodeForHTML(messageOut); // 21023-09-18 Richard add 這裡要轉義一下
        return messageOut;
    }

    @RequestMapping(value = "/operateScheduler", method = RequestMethod.POST, produces = "application/xml;charset=UTF-8")
    @ResponseBody
    public String operateScheduler(@RequestBody String messageIn) {
        messageIn = ESAPI.encoder().decodeForHTML(messageIn); // 21023-09-18 Richard add 這裡要轉義一下
        logger.info("[", CLASS_NAME, "][operateScheduler]", Const.MESSAGE_IN, messageIn);
        OperateSchedulerRequest request = deserializeFromXml(messageIn, OperateSchedulerRequest.class);
        OperateSchedulerResponse response = manager.operateScheduler(request);
        String messageOut = serializeToXml(response);
        logger.info("[", CLASS_NAME, "][operateScheduler]", Const.MESSAGE_OUT, messageOut);
        messageOut = ESAPI.encoder().encodeForHTML(messageOut); // 21023-09-18 Richard add 這裡要轉義一下
        return messageOut;
    }
}
