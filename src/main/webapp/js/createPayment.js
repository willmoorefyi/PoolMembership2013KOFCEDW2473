
$(document).ready(function() {
	$('input, textarea').placeholder();
	$('#submitCCButton').click(confirmForm);
	$('.modal').appendTo($('body'));
	$('#amount').val('$ ' + $('#amount').val() + '.00');
});

/**
 * Serialiaze a form component into a JSON object to post
 * @param form the form root element
 * @param paymentObj the object to serialize the values onto
 * @param selector the selector to retrieve input elements off the form
 */
function serializeFormObject(form, paymentObj, selector) {
	form.find(selector).each(function(index) {
		if($(this).attr('name')) {
			paymentObj[$(this).attr('name')] = $(this).val();
		}
	});
}
