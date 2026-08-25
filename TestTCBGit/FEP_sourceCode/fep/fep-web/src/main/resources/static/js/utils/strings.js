/**
 * 判斷一個字串是否是空白字串, 包括空格、製表符、換頁符
 *
 * @param str
 */
function stringIsBlank(str) {
    if (str === null || 'undefined' === typeof str) {
        return true;
    }
    return !stringIsNotBlank(str);
}

function stringIsNotBlank(str) {
    return $.trim(str) !== '';
}