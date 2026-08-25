/**
 * 頁面上的元件增加popover
 *
 * @param id 元件的id
 * @param message 要顯示的訊息
 */
function showPopover(id, message) {
	var element = $('#' + id);
	element.attr('data-toggle', 'popover')
		.attr('data-placement', 'right')
		.attr('data-template', '<div class="popover panel-popover-error" role="tooltip"><div class="arrow"></div><div class="popover-body"></div></div>')
		.attr('data-content', message + '<span class="popover-close popover-close-' + id + '" aria-hidden="true">×</span>')
		.attr('data-html', true);
	element.popover('show');
	$('.popover-close-' + id).click(function() {
		hidePopover(id);
	});
}
/**
 * 頁面上的元件移除popover
 *
 * @param id 元件的id
 */
function hidePopover(id) {
	var element = $('#' + id);
	if (typeof(element.attr('data-toggle'))!="undefined") {
		// $(element).popover('hide');
		element.removeAttr('data-toggle')
			.removeAttr('data-placement')
			.removeAttr('data-template')
			.removeAttr('data-content')
			.removeAttr('data-original-title')
			.removeAttr('title')
			.removeAttr('aria-describedby')
			.removeAttr('aria-invalid')
			.removeAttr('data-html');
		element.popover('dispose');
	}
}
/**
 * 頁面上的元件關閉pop
 *
 * @param id 元件的id
 */
function closePopover(id) {
	$('.popover-close-' + id).trigger('click');
}
/**
 * 頁面上所有的元件關閉pop
 */
function closeAllPopovers() {
    $("span[class^='popover-close popover-close-']").trigger('click');
}