package com.syscom.fep.web.controller.demo;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.esapi.ESAPIValidator;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.form.BaseForm;
import com.syscom.fep.web.util.WebUtil;
import org.owasp.esapi.ESAPI;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.util.Map;

@Controller
public class DemoDownloadController extends BaseController {

    @Override
    public void pageOnLoad(ModelMap mode) {
//        String userHome = System.getProperties().getProperty("user.home");
//        Path userHomeAbsolutePath = Paths.get(userHome).toAbsolutePath().normalize();
//        File directory = new File(CleanPathUtil.cleanString(userHomeAbsolutePath.toString()), CleanPathUtil.cleanString("Desktop"));
//        File[] files = directory.listFiles((File file) -> {
//            return file.isFile();
//        });
        PageInfo<File> pageInfo = new PageInfo<>();
//        pageInfo.setList(Arrays.asList(files));
        BaseForm form = new BaseForm();
        PageData<BaseForm, File> pageData = new PageData<BaseForm, File>(pageInfo, form);
        WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
    }


    @PostMapping(value = "/demo/DemoDownload/download")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> doDownload(@RequestBody Map<String, Object> form) {
        this.infoMessage("開始下載檔案, 條件 = [", form.toString(), "]");
        String path = ESAPIValidator.getValidFilePath(ESAPI.encoder().decodeForHTML((String) form.get("path")));
        return download(path);
    }
}
