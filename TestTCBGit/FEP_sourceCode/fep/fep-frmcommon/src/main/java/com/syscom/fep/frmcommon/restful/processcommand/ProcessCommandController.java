package com.syscom.fep.frmcommon.restful.processcommand;

import com.google.gson.Gson;
import com.syscom.fep.frmcommon.io.StreamGobbler;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CommandLineUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 接收並執行外部指令的控制器
 *
 * @author Richard
 */
public class ProcessCommandController {
    private final LogHelper logger = new LogHelper();
    private static final String URL = "/process/command";
    protected static final String MESSAGE_IN = "<<<<<<<<<<";
    protected static final String MESSAGE_OUT = ">>>>>>>>>>";

    /**
     * 設置並發數
     * <p>
     * curl -H "Content-Type:application/json" -d "{\"command\":\"ping\",\"args\":{\"Program\":\"127.0.0.1\"},\"argsRemain\":[\"true\"],\"argsFormat\":\"KEY_VALUE_COLON\",\"argsExtractors\":[{\"argsKey\":\"Program\"},{\"argsKey\":\"\"}],\"ioOutput\":true,\"charsetName\":\"GB2312\"}" -X POST http://localhost:8302/process/command
     *
     * @param request
     * @return
     */
    @RequestMapping(value = URL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ProcessCommandResponse sendReceive(@RequestBody ProcessCommandRequest request) {
        Gson gson = new Gson();
        logger.info("[", URL, "]", MESSAGE_IN, gson.toJson(request));
        ProcessCommandResponse response = this.processCommand(request);
        logger.info("[", URL, "]", MESSAGE_OUT, gson.toJson(response));
        return response;
    }

    /**
     * 依據傳入的參數, 執行相應的指令
     *
     * @param request
     * @return
     */
    protected ProcessCommandResponse processCommand(ProcessCommandRequest request) {
        ProcessCommandResponse response = new ProcessCommandResponse();
        response.setResult(false);
        String command = request.getCommand();
        if (StringUtils.isBlank(command)) {
            response.setErrorCode(ProcessCommandErrorCode.MISSING_ARGUMENT_COMMAND);
            return response;
        }
        List<String> commands = makeArgs(request);
        logger.debug("Start to execute command = [", StringUtils.join(commands, StringUtils.SPACE), "]");
        // ProcessBuilder processBuilder = new ProcessBuilder().command(commands);
        // processBuilder.redirectErrorStream(true);
        try {
            // Process process = processBuilder.start();
            Process process = CommandLineUtil.getProcess(commands.toArray(new String[0])); // 2025-01-08 Richard modified for Command Injection
            Consumer<String> consumer = request.isIoOutput() ? logger::debug : null;
            String charsetName = StringUtils.isBlank(request.getCharsetName()) ? StandardCharsets.UTF_8.displayName() : request.getCharsetName();
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), charsetName, consumer);
            new Thread(streamGobbler).start();
            logger.debug("Succeed to execute command = [", StringUtils.join(commands, StringUtils.SPACE), "]");
            response.setResult(true);
        } catch (Exception e) {
            logger.error(e, "Failed to execute command = [", StringUtils.join(commands, StringUtils.SPACE), "]");
        }
        return response;
    }

    /**
     * 重新組建參數
     *
     * @param request
     * @return
     */
    protected List<String> makeArgs(ProcessCommandRequest request) {
        List<String> commands = new ArrayList<>();
        commands.add(request.getCommand());
        // 提取傳入的參數, 按照一定format組建新的參數
        if (MapUtils.isNotEmpty(request.getParam())) {
            // 按照自定義的解構器
            if (CollectionUtils.isNotEmpty(request.getExtractors())) {
                request.getExtractors().forEach(extractor -> commands.addAll(extractor.extract(request.getFormat(), request.getParam())));
            }
            // 按照自定義的格式
            else if (request.getFormat() != null) {
                request.getParam().forEach((key, value) -> commands.add(request.getFormat().format(key, value)));
            }
            // 預設key=value的格式
            else {
                request.getParam().forEach((key, value) -> commands.add(ProcessCommandArgsFormat.KEY_VALUE_EQUALS.format(key, value)));
            }
        }
        // 剩餘自定義的參數
        if (CollectionUtils.isNotEmpty(request.getRemain())) {
            commands.addAll(request.getRemain());
        }
        return commands;
    }
}
