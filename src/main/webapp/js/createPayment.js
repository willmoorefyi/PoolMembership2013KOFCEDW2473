
$(document).ready(function() {
	$('input, textarea').placeholder();
	$('#submitCCButton').click(confirmForm);
	$('.modal').appendTo($('body'));
	$('#amount').val('$ ' + $('#amount').val() + '.00');
});

/**
 * Simple method to launch the confirm form.  Currently only shows the "confirm" modal dialog.
 */
function confirmForm(elem) {
	//unassign any handlers to prevent dual-posting of data
	$('#confirmDlgOk').off('click');
	if($(this).parents('#payment-cc').length > 0) {
		$('#confirmDlgOk').click({ 'action' : $(this).parents('#payment-cc form').attr('action') }, submitCCForm);
	}
	else if($(this).parents('#payment-paypal').length > 0) {
		//do nothing
	}
	else {
		alert('Error! Could not determine payment type');
	}
	$('#confirmDlg').modal('show');
}


/**
 * Submit the credit card form, called when "ok" is clicked on the dialog
 * @param event The event that triggers this method
 * @returns {Boolean} Will always return false, to prevent event bubbling
 */
function submitCCForm(event) {
	disableConfirmForm();
	
	var paymentCC = { };
	
	//select the internal form
	var form = $('div.accordion-body.in form');
	serializeFormObject(form, paymentCC, 'input,select');
	var formCCUrl = event.data.action;
	
	$.ajax(formCCUrl, {
		data: JSON.stringify(paymentCC),
		contentType: 'application/json; charset=UTF-8',
		dataType: 'json',
	    type:'POST',
		success: function(response) {
			if (response.status == 'FAIL') {
				processServerErrors(response.errorMessageList);
			}
			else {
				var successParams = { 'id' : response.successIdentifier, 'lastName' : response.lastName };
				var url = response.url;
				successRedirect(successParams, url);
			}
		}, 
		error: function(jqXHR, textStatus, errorThrown) {
			showError('Server Error', 'There were errors with your submission. Server Responded with ' + textStatus + ': ' + errorThrown);
		}
	});
	return false;
}


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

/**
 * Disable the confirmation form while posting data
 */
function disableConfirmForm() {
	$('#confirmDlgOk').attr('disabled', 'disabled');
	$('#confirmDlgOk').addClass('disabled');
}
/**
 * Enable the confirmation form after data posting complete
 */
function enableConfirmForm() {
	$('#confirmDlgOk').removeAttr('disabled');
	$('#confirmDlgOk').removeClass('disabled');
}

/**
 * Creates a form and submits it with the parameters specified to the success URL
 * @param params The parameters to submit
 */
function successRedirect(params, url) {
	var form = $('<form>').attr('method', 'post').attr('action', url);
	$.each(params, function(name, value) {
		form.append($('<input>').attr('type','hidden').attr('name', name).val(value));
	});
	
	$(document.body).append(form);
	form.submit();
}

/**
 * Process the errors returned by the server into the error message box
 * @param errorMessageList The list of errors from the server
 */
function processServerErrors(errorMessageList) {
	var body = $('<div>').addClass('row-fluid')
		.append($('<div>').addClass('span12').append('There were errors with your submission'));
		
	$(errorMessageList).each(function(index) {
		body.append($('<div>').addClass('span12').append((index+1) + '. ' + this.message));
	});
	body.append($('<div>').addClass('span12').append('Please correct these errors and resubmit the form'));
	showError('Validation Errors on Submission', body);
}

/**
 * Show the response modal dialog box in an "error" state with the specified header and message.
 * @param errorLabel The title of the message box
 * @param errorBody The message to put in the message box body. Note that this can contain HTML markup.
 */
function showError(errorLabel, errorBody) {
	$('#confirmDlg').modal('hide');
	enableConfirmForm();
	$('#responseDlg').addClass('alert alert-error');
	$('#responseDlgTitle').text(errorLabel);
	$('#responseDlgBody').empty().append(errorBody);
	$('#responseDlgOk').addClass('btn-danger').click(function() {
		$('#responseDlg').modal('hide');
		$('#responseDlgOk').off('click');
	});
	$('#responseDlg').modal('show');
}