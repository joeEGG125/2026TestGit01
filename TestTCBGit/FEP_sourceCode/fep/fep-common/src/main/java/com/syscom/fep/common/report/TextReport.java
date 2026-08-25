package com.syscom.fep.common.report;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.servlet.server.Encoding;

import java.io.*;

public class TextReport {
    private OutputStreamWriter _sw;
    private StringBuffer _ReportHeading;
    private StringBuffer _ReportFooting;
    //Add by David Tai on 2013-04-24 for 在報表結尾增加報表說明頁
    private String _ReportNoteFile = StringUtils.EMPTY;
    private Integer _ReportFootingRows;
    private StringBuffer _PageHeading;
    private Integer _PageHeadingRows;
    private StringBuffer _PageFooting;
    private Integer _PageFootingRows;
    private Integer _DetailRows;
    private String _ReportFile;
    private Integer _PageLines;
    private Integer _lineCount;
    private Integer _pageCount;
    private boolean _IsSkipLine = false;
    private boolean _IsReportFootingInSinglePage = false;
    private boolean _IsReportFootingNeedPageFooting = false;

    public StringBuffer get_ReportHeading() {
        return _ReportHeading;
    }

    public void set_ReportHeading(StringBuffer _ReportHeading) {
        this._ReportHeading = _ReportHeading;
    }

    public StringBuffer get_ReportFooting() {
        return _ReportFooting;
    }

    public void set_ReportFooting(StringBuffer _ReportFooting) {
        this._ReportFooting = _ReportFooting;
    }

    public String get_ReportNoteFile() {
        return _ReportNoteFile;
    }

    public void set_ReportNoteFile(String _ReportNoteFile) {
        this._ReportNoteFile = _ReportNoteFile;
    }

    public Integer get_ReportFootingRows() {
        return _ReportFootingRows;
    }

    public void set_ReportFootingRows(Integer _ReportFootingRows) {
        this._ReportFootingRows = _ReportFootingRows;
    }

    public StringBuffer get_PageHeading() {
        return _PageHeading;
    }

    public void set_PageHeading(StringBuffer _PageHeading) {
        this._PageHeading = _PageHeading;
    }

    public Integer get_PageHeadingRows() {
        return _PageHeadingRows;
    }

    public void set_PageHeadingRows(Integer _PageHeadingRows) {
        this._PageHeadingRows = _PageHeadingRows;
    }

    public StringBuffer get_PageFooting() {
        return _PageFooting;
    }

    public void set_PageFooting(StringBuffer _PageFooting) {
        this._PageFooting = _PageFooting;
    }

    public Integer get_PageFootingRows() {
        return _PageFootingRows;
    }

    public void set_PageFootingRows(Integer _PageFootingRows) {
        this._PageFootingRows = _PageFootingRows;
    }

    public Integer get_DetailRows() {
        return _DetailRows;
    }

    public void set_DetailRows(Integer _DetailRows) {
        this._DetailRows = _DetailRows;
    }

    public String get_ReportFile() {
        return _ReportFile;
    }

    public Integer get_PageLines() {
        return _PageLines;
    }

    public void set_PageLines(Integer _PageLines) {
        this._PageLines = _PageLines;
    }

    public Integer get_lineCount() {
        return _lineCount;
    }

    public void set_lineCount(Integer _lineCount) {
        this._lineCount = _lineCount;
    }

    public Integer get_pageCount() {
        return _pageCount;
    }

    public void set_pageCount(Integer _pageCount) {
        this._pageCount = _pageCount;
    }

    public boolean is_IsSkipLine() {
        return _IsSkipLine;
    }

    public void set_IsSkipLine(boolean _IsSkipLine) {
        this._IsSkipLine = _IsSkipLine;
    }

    public boolean is_IsReportFootingInSinglePage() {
        return _IsReportFootingInSinglePage;
    }

    public void set_IsReportFootingInSinglePage(boolean _IsReportFootingInSinglePage) {
        this._IsReportFootingInSinglePage = _IsReportFootingInSinglePage;
    }

    public boolean is_IsReportFootingNeedPageFooting() {
        return _IsReportFootingNeedPageFooting;
    }

    public void set_IsReportFootingNeedPageFooting(boolean _IsReportFootingNeedPageFooting) {
        this._IsReportFootingNeedPageFooting = _IsReportFootingNeedPageFooting;
    }

    public TextReport(String reportFile) throws Exception {
        if (reportFile.toString().length() > 0) {
            _sw = new OutputStreamWriter(new FileOutputStream(reportFile), "big5");
            _lineCount = 0;
            _pageCount = 0;
            _ReportFile = reportFile;
        } else {
            throw new Exception("Report Name(" + reportFile + ") 參數不得空白!");
        }
    }

    public TextReport(String reportFile, Encoding encoding) throws Exception {
        if (reportFile.toString().length() > 0) {
            _sw = new OutputStreamWriter(new FileOutputStream(reportFile), encoding.toString());
            _lineCount = 0;
            _pageCount = 0;
            _ReportFile = reportFile;
        } else {
            throw new Exception("Report Name(" + reportFile + ") 參數不得空白!");
        }
    }

    public TextReport(String reportFile, boolean append) throws Exception {
        if (reportFile.toString().length() > 0) {
            _sw = new OutputStreamWriter(new FileOutputStream(reportFile), "big5");
            _lineCount = 0;
            _pageCount = 0;
            _ReportFile = reportFile;
        } else {
            throw new Exception("Report Name(" + reportFile + ") 參數不得空白!");
        }
    }

    public TextReport(String reportFile, boolean append, Encoding encoding) throws Exception {
        if (reportFile.toString().length() > 0) {
            _sw = new OutputStreamWriter(new FileOutputStream(reportFile), "big5");
            _lineCount = 0;
            _pageCount = 0;
            _ReportFile = reportFile;
        } else {
            throw new Exception("Report Name(" + reportFile + ") 參數不得空白!");
        }
    }

    public void PrintDetail(String detailLine) throws Exception {
        try {
            if (_lineCount == 0) {
                PrintPageHeading();
            } else {
                //跳頁
                //Modify by David Tai on 2011-09-29 for 頁尾行數也要計算，非>=(移除=)
                if ((_lineCount + _DetailRows + get_PageFootingRows()) > _PageLines) {
                    PrintPageFooting();
                    PrintPageHeading();
                }
                _sw.write(detailLine + "\r\n");
                _lineCount += _DetailRows;
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void PrintPageHeading() throws Exception {
        String pageHeading = _PageHeading.toString();
        try {
            _pageCount += 1;
            _sw.write(pageHeading.replace("XXXX", _pageCount.toString()));
            _lineCount = get_PageHeadingRows();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void PrintPageFooting() throws Exception {
        try {
            //Modify by David Tai on 2011-09-29 for 先判斷跳行
            if (_IsSkipLine) {
                for (int i = 0; i < (get_PageLines() - _lineCount - 1 - get_PageFootingRows()); i++) {
                    _sw.write("\r\n");
                }
                if (_PageFooting != null) {
                    _sw.write(_PageFooting.toString());
                }
            }

            //_sw.Flush()
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void PrintReportFooting() throws Exception {
        Integer skipLines;
        try {
            //Modify by David Tai on 2011-09-29 for 判斷是否表尾在同一頁
            if (_IsReportFootingInSinglePage) {
                PrintPageFooting();
                PrintPageHeading();
            }

            if (_IsSkipLine) {
                skipLines = get_PageLines() - _lineCount - 1 - get_ReportFootingRows() - get_PageFootingRows();
                if (skipLines < 0) {
                    //所剩行數不夠列印頁尾及表尾所以要換頁
                    PrintPageFooting();
                    PrintPageHeading();
                }
                for (int i = 0; i < (get_PageLines() - _lineCount - 1 - get_ReportFootingRows() - get_PageFootingRows()); i++) {
                    _sw.write("\r\n");
                }
            }

            if (_PageFooting != null) {
                _sw.write(_PageFooting.toString());
            }
            if (_ReportFooting != null) {
                _sw.write(_ReportFooting.toString());
            }
            //Add by David Tai on 2013-04-24 for 在報表結尾增加報表說明頁
            if (StringUtils.isNotBlank(_ReportNoteFile)) {
                PrintReportNote();
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    /*
     *Add by David Tai on 2013-04-24 for 在報表結尾增加報表說明頁
     *需在呼叫端先檢查是否有此報表說明檔
     */
    public void PrintReportNote() throws Exception {
        Integer lineNote = 0;
        try {
            FileInputStream fis = new FileInputStream(_ReportNoteFile);
            InputStreamReader srNote = new InputStreamReader(fis, "Unicode");
            BufferedReader sr = new BufferedReader(srNote);
            String str = "";
            StringBuffer sb = new StringBuffer();
            while ((str = sr.readLine()) != null) {
                sb.append(str).append("\r\n");
                lineNote += 1;
            }
            PrintPageHeading();
            _sw.write(sb.toString());
            //補空行
            for (int i = 0; i < (get_PageLines() - _lineCount - 1 - lineNote); i++) {
                _sw.write("\r\n");
            }

        } catch (Exception ex) {
            throw ex;
        }
    }

    public void Dispose() throws Exception {
        if (_pageCount == 0 && _lineCount == 0) {
            PrintPageHeading();
        }
        PrintReportFooting();
        _sw.close();
    }
}
