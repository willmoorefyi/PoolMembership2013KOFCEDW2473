
$(document).ready(function() {
    $('input, textarea').placeholder();
    $('#submitCCButton').click(confirmForm);
    $('.modal').appendTo($('body'));
    $('.currency').each(function(i, elem) {
        $(elem).val('$ ' + $(elem).val() + '.00');
    });
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
            if($(this).attr('name').indexOf('.') > -1) {
                var names = $(this).attr('name').split(/\./);
				paymentObj[names[0]] = paymentObj[names[0]] || {};
                paymentObj[names[0]][names[1]] = $(this).val();
            }
            else {
                paymentObj[$(this).attr('name')] = $(this).val();
            }

        }
    });
}
