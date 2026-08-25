/**
 * 將JSON對象轉為JSON字符串
 * 
 * @param obj
 * @returns
 */
function jsonObjToString(obj) {
	return JSON.stringify(obj, function(key, val) {
		if ('function' === typeof val) {
			return val + '';
		}
		return val;
	});
}
/**
 * 將JSON字符串轉為JSON對象
 * 
 * @param str
 * @returns
 */
function jsonStringToObj(str) {
	// json字符串只截取第一個{和最後一個}之間的內容
	// 避免某些browser的插件會自動append一些字串，例如IDM
	var beginIndex = str.indexOf("{");
	var endIndex = str.lastIndexOf("}");
	var subStr = str.substring(beginIndex, endIndex + 1);
	return JSON.parse(subStr);
}
/**
 * 將JSON對象轉成字符串後雙引號替換為單引號用於HTML中的Element屬性, 因為屬性預設使用雙引號, 所以必須把JSON中的雙引號替換為單引號
 * 
 * @param obj
 * @returns
 */
function jsonObjToStringForHTML(obj) {
	return jsonObjToString(obj).replace(/\"/g, "'");
}
/**
 * 將JSON字串中的單引號替換為雙引號, 再轉為JSON對象
 * 
 * @param str
 * @returns
 */
function jsonStringToObjForHTML(str) {
	return jsonStringToObj(str.replace(/\'/g, "\""));
}