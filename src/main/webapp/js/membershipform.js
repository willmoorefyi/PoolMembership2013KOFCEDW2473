var totalRelations=0;
	
$(document).ready(function() {
	$('#relation-question').tooltip();
	$('#addRelationButton').click(addRelationTableRow);
	$('#submitButton').click(confirmForm);;
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
	if($('#application-form #rateTabsContent input[name=memberOption]:checked').length == 0) {
		showError('Invalid Submission', 'You must select a membership option to submit your membership application form.');
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
	$('#confirmDlg').modal('hide');

	var member = { };
	
	var form = $('#application-form');
	
	//serialize the name
	member.name = { };
	serializeFormObject(form, member.name, '#name input');
	
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
		serializeFormObject($(this), member.dependents[index], 'input, select');		
	});
	
	//retrieve the membership option
	member.membershipOption = { };
	var memberOpt = form.find('#rateTabsContent input[name=memberOption]:checked');
	member.membershipOption.optionKey = $(memberOpt).val();
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
	
	$.ajax(formUrl, {
		data: JSON.stringify(member),
		contentType: 'application/json; charset=UTF-8',
		dataType: 'json',
	    type:'POST',
		success: function(response) {
			if (response.status == 'FAIL') {
				showError('Error', 'There were errors with your submission');
			}
			else {
				showSuccess("Success!", "Your membership application was succesfully received and stored. You will receive a response in the next few business days. Thanks for your interest!");
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
 * Show the response modal dialog box in an "error" state with the specified header and message.
 * @param errorLabel The title of the message box
 * @param errorBody The message to put in the message box body.
 */
function showError(errorLabel, errorBody) {
	$('#responseDlg').addClass('alert alert-error');
	$('#responseDlgTitle').text(errorLabel);
	$('#responseDlgBody').text(errorBody);
	$('#responseDlgOk').addClass('btn-danger').click(function() {
		$('#responseDlg').modal('hide');
		$('#responseDlgOk').off('click');
	});
	$('#responseDlg').modal('show');	
}

/**
 * Show the response modal dialog box in a "success" state with the specified header and message.
 * @param errorLabel The title of the message box
 * @param errorBody The message to put in the message box body.
 */
function showSuccess(msgLabel, msgBody) {
	$('#responseDlg').removeClass('alert alert-error');
	$('#responseDlgTitle').text(msgLabel);
	$('#responseDlgBody').text(msgBody);
	$('#responseDlgOk').removeClass('btn-danger').click(function() {
		$('#responseDlg').modal('hide');
		$('#responseDlgOk').off('click');
	});
	$('#responseDlg').modal('show');	
}