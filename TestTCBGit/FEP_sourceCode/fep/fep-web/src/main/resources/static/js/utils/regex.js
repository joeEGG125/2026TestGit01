/**
 * 驗證是否是IP地址
 *
 * @param ip
 * @returns {boolean}
 */
function matchIp(ip) {
    var regex = /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
    return regex.test(ip);
}

/**
 * 從檔案下載的Response Header中的Content-Disposition獲取檔案名
 *
 * @param contentDisposition
 * @returns {string|*}
 */
function getFileNameFromContentDisposition(contentDisposition) {
    var filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
    var matches = filenameRegex.exec(contentDisposition);
    if (matches != null && matches[1]) {
        return matches[1].replace(/['"]/g, '');
    }
    return contentDisposition;
}

/**
 * 是否滿足decimal
 *
 * @param input
 * @param m
 * @param d
 * @returns {boolean}
 */
function matchDecimal(input, m, d) {
    var regex = new RegExp("^\\d{1," + m + "}(\\.\\d{1," + d + "})?$");
    return regex.test(input);
}