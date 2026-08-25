$(document).ready(function() {
	$("th i[class*='fa-sort']").bind('click', function() {
		var hash = $(this).attr('data-sort-hash');
		var form = { 'hash': hash };
		doFormSubmit('/doColumnSort', form);
	});
})