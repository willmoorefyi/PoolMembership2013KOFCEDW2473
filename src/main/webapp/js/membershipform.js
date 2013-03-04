var totalRelations=0;
	
$(document).ready(function() {
	$('#relation-question').tooltip();
	$('#addRelationButton').click(addRelationTableRow);
	$('#submitButton').click(confirmForm);
	$('#confirmDlgOk').click(submitForm);
	$('#rateTabs a:first').tab('show');
	
	//attach the cost validation logic to the inputs (radios and text fields)
	$('#rateTabsContent input[type=text]').blur(validateCost);
	$('#rateTabsContent input[type=radio]').click(validateCostRadio);
});

/**
 * Add a new row to the relation table. Used when potential members want to add dependents
 */
function addRelationTableRow() {
   $('#RelationTable > tbody:last').append($('<tr>')
      .append($('<td>')
         .append($('<input>')
            .attr('id', 'relationFirstName' + totalRelations).attr('name', 'firstName').attr('type', 'text').attr('placeholder', 'First Name').attr('title', 'First Name') 
         )
      ).append($('<td>')
         .append($('<input>')
            .attr('id', 'relationLastName' + totalRelations).attr('name', 'lastName').attr('type', 'text').attr('placeholder', 'Last Name').attr('title', 'Last Name') 
         )
      ).append($('<td>')
         .append($('<select>')
            .attr('id', 'relationType' + totalRelations)
            .attr('name', 'relationType')
            .append('<option value="Spouse">Spouse</option>')
            .append('<option value="Child">Child</option>')
            .append('<option value="Parent">Parent / Grandparent</option>')
            .append('<option value="Nanny">Nanny / Caretaker</option>')
            .append('<option value="Other">Other Family</option>')
         )
      ).append($('<td>')
         .append($('<i>')
    		.attr('class', 'icon-remove').text(' ').hover(function() {
    			$(this).addClass('icon-highlight-hover');
    		},
    		function() {
    			$(this).removeClass('icon-highlight-hover');
    		}).click(function() {
    			$(this).closest('tr').remove();
    		})
         )
      )
   );
   $('#relationType'+totalRelations).prop("selectedIndex", -1);
   totalRelations++;
}

/**
 * Reset the cost element, then validate the input (if defined) and set a new cost in the input box if valid
 */
function validateCostRadio() {
	$('#finalCost').val('');
	var validationInput = $(this).data('validationInput');
	if(validationInput == '') {
		setCost($(this).data('cost'));
	}
	else {
		//call the cost validate with the underlying input elem
		validateCost(null, $('#' + validationInput));
	}
}

/**
 * Reset the cost element, then validate the input elements for a given input box and, if it matches the pattern, display the cost in the cost box
 */
function validateCost(inputEvent, refElem) {
	$('#finalCost').val('');
	var elem = refElem;
	if(inputEvent && !refElem ) {
		//we are operating on the input element;
		elem = this;
	}
	if($(elem).val() !== '' && $(elem).get(0).checkValidity()) {
		//we are valid
		var checkedElem = $("input[name=memberOption]:checked").get(0);
		if(checkedElem) {
			setCost($(checkedElem).data('cost'));
		}
	}
}

/**
 * Set a user-friendly cost value to display
 */
function setCost(val) {
	$('#finalCost').val('$' + parseFloat(val).toFixed(2));
}

/**
 * Simple method to launch the confirm form.  Currently only shows the "confirm" modal dialog.
 */
function confirmForm() {
	//verify a member option has been selected
	if($('#application-form #rateTabsContent input[name=memberOption]:checked').length == 0 || $('#application-form #paymentMethod input[name=paymentOption]:checked').length == 0) {
		showError('Invalid Submission', 'You must select a membership option and payment option to submit your membership application form.');
	}
	else {
		$('#confirmDlg').modal('show');
	}
}

/**
 * Submit the form to the server, including all selections.  Will respond with a status, which will then be shown to the user in the modal dialog boxes
 * @returns {Boolean}
 */
function submitForm() {
	disableConfirmForm();

	var memberRequest = { };
	memberRequest.member = { };
	var member = memberRequest.member;
	
	var form = $('#application-form');
	
	//serialize the name
	serializeFormObject(form, member, '#name input');
	
	//serialize the address
	serializeFormObject(form, member, '#addresses input');
	
	//serialize the phone numbers
	serializeFormObject(form, member, '#phones input');
	
	//serialize the emails
	serializeFormObject(form, member, '#emails input');
	
	//serialize the dependents
	member['dependents'] = [];
	form.find('#RelationTable tbody tr').each(function(index) {
		member.dependents[index] = { };
		member.dependents[index].name = { };
		serializeFormObject($(this), member.dependents[index].name, 'input[name="firstName"],input[name="lastName"]');
		serializeFormObject($(this), member.dependents[index], 'select[name="relationType"]');
	});
	
	//retrieve the membership option
	memberRequest.membershipOption = { };
	var memberOpt = form.find('#rateTabsContent input[name=memberOption]:checked');
	memberRequest.membershipOption.optionKey = $(memberOpt).val();
	var validationInputId = $(memberOpt).data('validation-input');
	if(validationInputId) {
		var validationInput = form.find('#' + validationInputId);
		if(validationInput.val() && validationInput.get(0).checkValidity()) {
			member.validationInput = validationInput.val();
		}
		else {
			//don't continue to post if the value on the form is invalid
			showError('Validation Error', 'You must enter a valid value for the membership option you have selected.');
			return false;
		}
	}
	
	//serialize the payment selection
	serializeFormObject(form, member, '#paymentMethod input:checked');
	
	$.ajax(formUrl, {
		data: JSON.stringify(memberRequest),
		contentType: 'application/json; charset=UTF-8',
		dataType: 'json',
	    type:'POST',
		success: function(response) {
			if (response.status == 'FAIL') {
				processServerErrors(response.errorMessageList);
			}
			else {
				//showSuccess("Success!", "Your membership application was succesfully received and stored. You will receive a response in the next few business days. Thanks for your interest!");
				//window.location.href=successUrl;
				var successParams = { 'memberId' : response.successIdentifier, 'lastName' : response.lastName };
				successRedirect(successParams);
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
 * @param memberObj the object to serialize the member values onto
 * @param selector the selector to retrieve input elements off the form
 */
function serializeFormObject(form, memberObj, selector) {
	form.find(selector).each(function(index) {
		if($(this).attr('name')) {
			memberObj[$(this).attr('name')] = $(this).val();
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

/**
 * Creates a form and submits it with the parameters specified to the success URL
 * @param params The parameters to submit
 */
function successRedirect(params) {
	var form = $('<form>').attr('method', 'post').attr('action', successUrl);
	$.each(params, function(name, value) {
		form.append($('<input>').attr('type','hidden').attr('name', name).val(value));
	});
	
	$(document.body).append(form);
	form.submit();
}

/**
 * Show the response modal dialog box in a "success" state with the specified header and message.
 * @param errorLabel The title of the message box
 * @param errorBody The message to put in the message box body.
 */
function showSuccess(msgLabel, msgBody) {
	$('#responseDlg').removeClass('alert alert-error');
	enableConfirmForm();
	$('#responseDlgTitle').text(msgLabel);
	$('#responseDlgBody').empty().append(msgBody);
	$('#responseDlgOk').removeClass('btn-danger').click(function() {
		$('#responseDlg').modal('hide');
		$('#responseDlgOk').off('click');
	});
	$('#responseDlg').modal('show');	
}