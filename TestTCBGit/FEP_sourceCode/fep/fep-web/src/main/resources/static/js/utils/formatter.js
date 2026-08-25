/**
 * 格式化數字
 * 
 * @param num 待格式化的數字
 * @param decLen 小數點保留位數
 * @returns
 */
function formatNumber(num, decLen) {
	var re = '\\d(?=(\\d{3})+' + (decLen > 0 ? '\\.' : '$') + ')';
	return num.toFixed(Math.max(0, ~~decLen)).replace(new RegExp(re, 'g'), '$&,');
}