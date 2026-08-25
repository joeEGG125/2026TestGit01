package com.syscom.fep.web.controller;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.delegate.ActionListener1;
import com.syscom.fep.frmcommon.esapi.ESAPIUtil;
import com.syscom.fep.frmcommon.esapi.ESAPIValidator;
import com.syscom.fep.frmcommon.jms.JmsHandler;
import com.syscom.fep.frmcommon.jms.JmsProperty;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.jms.JmsMsgPayloadOperator;
import com.syscom.fep.jms.entity.MsMessage;
import com.syscom.fep.web.base.FEPWebBase;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.User;
import com.syscom.fep.web.form.BaseForm;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@StackTracePointCut(caller = SvrConst.SVR_WEB)
public class BaseController extends FEPWebBase {
    @Autowired
    private JmsMsgPayloadOperator jmsMsgPayloadOperator;

    /**
     * 頁面加載初始化方法
     *
     * @param mode
     */
    public void pageOnLoad(ModelMap mode) {}

    /**
     * 重新定向
     *
     * @param url
     * @return
     */
    protected String redirectToUrl(String url) {
        String contextPath = WebUtil.getRequest().getContextPath();
        if (StringUtils.isNotBlank(contextPath) && url.contains(contextPath)) {
            url = StringUtils.replace(url, contextPath, StringUtils.EMPTY);
        }
        this.infoMessage("redirect to [", url, "]");
        return StringUtils.join("redirect:", url);
    }

    /**
     * 頁面顯示訊息
     *
     * @param map
     * @param messageType
     * @param msgs
     */
    protected void showMessage(Map<String, Object> map, MessageType messageType, Object... msgs) {
        WebUtil.putInAttribute(map, AttributeName.MessageType, messageType);
        String message = StringUtils.join(msgs);
        if (StringUtils.isBlank(message)) {
            message = StringUtils.join("未知", messageType.getDescription(), "訊息");
        }
        WebUtil.putInAttribute(map, AttributeName.Message, message);
    }

    /**
     * 頁面顯示訊息
     *
     * @param redirectAttributes
     * @param messageType
     * @param msgs
     */
    protected void showMessage(RedirectAttributes redirectAttributes, MessageType messageType, Object... msgs) {
        WebUtil.putInAttribute(redirectAttributes, AttributeName.MessageType, messageType);
        String message = StringUtils.join(msgs);
        if (StringUtils.isBlank(message)) {
            message = StringUtils.join("未知", MessageType.WARNING.getDescription(), "訊息");
        }
        WebUtil.putInAttribute(redirectAttributes, AttributeName.Message, message);
    }

    /**
     * clear message on page
     *
     * @param map
     */
    protected void clearMessage(Map<String, Object> map) {
        WebUtil.putInAttribute(map, AttributeName.MessageType, MessageType.INFO);
        WebUtil.putInAttribute(map, AttributeName.Message, StringUtils.EMPTY);
    }

    /**
     * 頁面顯示訊息
     *
     * @param map
     * @param messageType
     * @param pattern
     * @param arguments
     */
    protected void showMessageWithArgs(Map<String, Object> map, MessageType messageType, String pattern, Object... arguments) {
        WebUtil.putInAttribute(map, AttributeName.MessageType, messageType);
        String message = null;
        if (StringUtils.isBlank(pattern)) {
            message = StringUtils.join("未知", messageType.getDescription(), "訊息");
        } else {
            message = FormatUtil.messageFormat(pattern, arguments);
        }
        WebUtil.putInAttribute(map, AttributeName.Message, message);
    }

    /**
     * 保存當前表單資料
     *
     * @param map
     * @param form
     */
    protected void doKeepFormData(Map<String, Object> map, BaseForm form) {
        if (StringUtils.isBlank(form.getUrl())) {
            form.setUrl(WebUtil.getRequest().getRequestURI());
        }
        WebUtil.putInAttribute(map, AttributeName.Form, form);
        WebUtil.getUser().addForm(form);
    }

    /**
     * 改變title
     *
     * @param map
     * @param titles
     */
    protected void changeTitle(Map<String, Object> map, Object... titles) {
        WebUtil.putInAttribute(map, AttributeName.ChangedTitle, StringUtils.join(titles));
    }

    /**
     * 清除之前使用的表單資料
     *
     * @param user
     */
    protected void clearForm(User user) {
        user.reset();
    }

    /**
     * 根據查詢結果建立分頁對象, 此方法主要用於前台分頁, 即後台sql查詢所有的資料total, 然後根據頁面的查詢的頁數pageNum, 和設定的每頁筆數來進行分頁
     *
     * @param <T>
     * @param total
     * @param pageNum
     * @param pageSize
     * @return
     */
    protected <T> PageInfo<T> clientPaged(List<T> total, Integer pageNum, Integer pageSize) {
        if (CollectionUtils.isEmpty(total))
            return null;
        // 2024-06-18 Richard modified start for【Integer Overflow】
        int pageStart = MathUtil.safeGet(pageNum == 1 ? 0 : (pageNum - 1) * pageSize);
        int pageEnd = MathUtil.safeGet(total.size() < pageSize * pageNum ? total.size() : pageSize * pageNum);
        List<T> paged = (total.size() > pageStart) ? total.subList(pageStart, pageEnd) : new LinkedList<T>();
        int pages = MathUtil.safeGet(total.size() % pageSize == 0 ? total.size() / pageSize : (total.size() / pageSize) + 1);
        int starRow = MathUtil.safeGet(total.size() < pageSize * pageNum ? 1 + pageSize * (pageNum - 1) : 0);
        int endRow = MathUtil.safeGet(starRow - 1 + pageSize);
        // 2024-06-18 Richard modified end for【Integer Overflow】
        PageInfo<T> pageInfo = new PageInfo<T>(paged);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setPages(pages);
        pageInfo.setStartRow(starRow);
        pageInfo.setEndRow(endRow);
        pageInfo.setTotal(total.size());
        pageInfo.calcByNavigatePages(PageInfo.DEFAULT_NAVIGATE_PAGES);
        return pageInfo;
    }

    protected String doRedirectToSelectedMenu(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = WebUtil.getUser();
        // 如果user為null, 則直接踢到登入畫面
        if (user == null) {
            return this.redirectToUrl(Router.LOGIN.getUrl());
        }
        return this.redirectToUrl(user.getSelectedMenu().getUrl());
    }

    protected String doRedirectForPrevPage(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        return this.doRedirectForPrevPage(null, null, redirectAttributes, request);
    }

    protected String doRedirectForPrevPage(BaseForm redirectForm, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        return this.doRedirectForPrevPage(redirectForm, null, redirectAttributes, request);
    }

    protected String doRedirectForPrevPage(BaseResp<?> resp, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        return this.doRedirectForPrevPage(null, resp, redirectAttributes, request);
    }

    protected String doRedirectForPrevPage(BaseForm redirectForm, BaseResp<?> resp, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = WebUtil.getUser();
        // 如果user為null, 則直接踢到登入畫面
        if (user == null) {
            return this.redirectToUrl(Router.LOGIN.getUrl());
        }
        // 移除掉當前的頁面視圖
        user.removePageView();
        if (redirectForm == null) {
            redirectForm = user.getPrevPageForm(true);
        } else {
            BaseForm prevPageForm = user.getPrevPageForm(true);
            redirectForm.setUrl(prevPageForm.getUrl());
            redirectForm.setPageNum(prevPageForm.getPageNum());
            redirectForm.setPageSize(prevPageForm.getPageSize());
            redirectForm.setRedirectFromPageChanged(prevPageForm.isRedirectFromPageChanged());
            ReflectUtil.setFieldValue(redirectForm, "sqlSortExpressionList", ReflectUtil.getFieldValue(prevPageForm, "sqlSortExpressionList", null));
        }
        redirectAttributes.addFlashAttribute(redirectForm);
        request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
        if (resp != null) {
            this.showMessage(redirectAttributes, resp.getMessageType(), resp.getMessage());
        }
        return this.redirectToUrl(redirectForm.getUrl());
    }

    protected String doRedirectForCurrentPage(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        return this.doRedirectForCurrentPage(null, null, redirectAttributes, request);
    }

    protected String doRedirectForCurrentPage(BaseForm redirectForm, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        return this.doRedirectForCurrentPage(redirectForm, null, redirectAttributes, request);
    }

    protected String doRedirectForCurrentPage(BaseResp<?> resp, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        return this.doRedirectForCurrentPage(null, resp, redirectAttributes, request);
    }

    private String doRedirectForCurrentPage(BaseForm redirectForm, BaseResp<?> resp, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        User user = WebUtil.getUser();
        // 如果user為null, 則直接踢到登入畫面
        if (user == null) {
            return this.redirectToUrl(Router.LOGIN.getUrl());
        }
        if (redirectForm == null) {
            redirectForm = user.getCurrentPageForm();
        } else {
            BaseForm currentPageForm = user.getCurrentPageForm();
            redirectForm.setUrl(currentPageForm.getUrl());
            redirectForm.setPageNum(currentPageForm.getPageNum());
            redirectForm.setPageSize(currentPageForm.getPageSize());
            redirectForm.setRedirectFromPageChanged(currentPageForm.isRedirectFromPageChanged());
            ReflectUtil.setFieldValue(redirectForm, "sqlSortExpressionList", ReflectUtil.getFieldValue(currentPageForm, "sqlSortExpressionList", null));
        }
        redirectAttributes.addFlashAttribute(redirectForm);
        request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
        if (resp != null) {
            this.showMessage(redirectAttributes, resp.getMessageType(), resp.getMessage());
        }
        return this.redirectToUrl(redirectForm.getUrl());
    }

    /**
     * 丟訊息到Queue中
     *
     * @param destination Queue名稱
     * @param label
     * @param body
     * @param property
     * @param handler
     * @throws Exception
     */
    protected void sendQueue(String destination, String label, String body, JmsProperty property, JmsHandler handler) throws Exception {
        MsMessage<String> message = new MsMessage<String>(label, body);
        jmsMsgPayloadOperator.sendQueue(destination, message, property, handler);
    }

    /**
     * 下載檔案
     *
     * @param filePath 要下載的檔案路徑
     * @return
     */
    protected ResponseEntity<StreamingResponseBody> download(String filePath) {
        try {
            // 驗證下載路徑
            String sanitizedPath = ESAPIValidator.getValidDirectoryName(filePath);
            // 規范化路徑
            // Path fileAbsolutePath = Paths.get(CleanPathUtil.cleanString(sanitizedPath)).toAbsolutePath().normalize();
            // 2024-04-24 Richard modified start for 【Absolute Path Traversal】
            // File file = new File(CleanPathUtil.cleanString(fileAbsolutePath.toString()));
            Object file = ESAPIUtil.toFile(sanitizedPath);
            // 2024-04-24 Richard modified end for 【Absolute Path Traversal】
            String fileName = FilenameUtils.getName(filePath);
            String contentType = WebUtil.getRequest().getServletContext().getMimeType(((File) file).getAbsolutePath());
            return this.download(fileName, contentType, os -> {
                try {
                    // 2025-06-17 Richard modified for [Absolute Path Traversal]
                    RefBase<InputStream> rtn = new RefBase<>(null);
                    toStream(rtn, file);
                    IOUtils.copy(rtn.get(), os);
                } catch (Exception e) {
                    if (e instanceof IOException)
                        throw (IOException) e;
                    throw ExceptionUtil.createIOException(e, e.getMessage());
                }
            });
        } catch (Exception e) {
            return this.handleDownloadException(e);
        }
    }

    private static void toStream(RefBase<InputStream> rtn, Object file) throws IOException {
        rtn.set(FileUtils.openInputStream((File) file));
    }

    /**
     * 檔案下載, 將檔案直接輸出到流中, 主要用於大檔案下載, 避免將整個檔案讀入記憶體中造成記憶體溢出
     *
     * @param fileName       檔案名稱
     * @param contentType    檔案的類型
     * @param actionListener 動作執行監聽器
     * @return
     */
    protected ResponseEntity<StreamingResponseBody> download(String fileName, String contentType, ActionListener1<OutputStream> actionListener) {
        try {
            if (StringUtils.isBlank(contentType)) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            try {
                fileName = URLEncoder.encode(fileName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                warnMessage(e, e.getMessage());
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, StringUtils.join("attachment; filename=", fileName))
                    .body(os -> {
                        try {
                            actionListener.actionPerformed(os);
                        } catch (Exception e) {
                            if (e instanceof IOException)
                                throw (IOException) e;
                            throw ExceptionUtil.createIOException(e, e.getMessage());
                        }
                    });
        } catch (Exception e) {
            return this.handleDownloadException(e);
        }
    }

    /**
     * 處理檔案下載失敗
     *
     * @param errorMessages
     * @return
     */
    protected ResponseEntity<StreamingResponseBody> handleDownloadError(Object... errorMessages) {
        this.errorMessage(errorMessages);
        BaseResp<?> response = new BaseResp<>();
        response.setMessage(MessageType.DANGER, StringUtils.join("下載失敗, ", StringUtils.join(errorMessages)));
        return ResponseEntity.ok(os -> os.write(new Gson().toJson(response).getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 處理檔案下載失敗
     *
     * @param e
     * @return
     */
    protected ResponseEntity<StreamingResponseBody> handleDownloadException(Exception e) {
        this.errorMessage(e, e.getMessage());
        BaseResp<?> response = new BaseResp<>();
        response.setMessage(MessageType.DANGER, StringUtils.join("下載失敗, ", programError));
        return ResponseEntity.ok(os -> os.write(new Gson().toJson(response).getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 處理檔案下載失敗
     *
     * @param warnMessages
     * @return
     */
    protected ResponseEntity<StreamingResponseBody> handleDownloadWarn(Object... warnMessages) {
        this.warnMessage(warnMessages);
        BaseResp<?> response = new BaseResp<>();
        response.setMessage(MessageType.WARNING, StringUtils.join(warnMessages));
        return ResponseEntity.ok(os -> os.write(new Gson().toJson(response).getBytes(StandardCharsets.UTF_8)));
    }
}