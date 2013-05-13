$(document).ready(function() {
	getCurrentMemberStatusValues();
	$('#editButton').click(editForm);
	$('#confirmDlgOk').click(approveForm);
	$('#approveButton').click(clickApprove);
	$('#exportButton').click(clickExport);
});

/**
 * Retrieve the current member status values an application can possibly be in.  To build the sort bar
 */
function getCurrentMemberStatusValues() {
	var url = $('#statusForm').attr('action');;
	$.getJSON(url, function(data) {
		$.each(data, function(index, status) {
			$('#navbar').append($('<li>').append($('<a>').attr('href', '#').attr('status', status).append(status).click(clickNavbar) ) );
		});
		getDefaultApplicationStatus();
	});
}

/**
 * Fetch the default application status (item #2 = PAID)
 */
function getDefaultApplicationStatus() {
	//simulate a click on item #2
	$('#navbar a').get(1).click();
}

/**
 * Handler for a click on the navbar.  To reload the list
 * @param event
 */
function clickNavbar(event) {
	var filter = $(this).attr('status');
	var url = $('#memberForm').attr('action');

	$('#navbar li').removeClass('active');
	$(this).parent().addClass('active');
	
	lastSelect = null;
	$('#memberTable tr').off();
	$('#memberTable > tbody').empty();	
	$.ajax(url, {
		data: JSON.stringify(filter),
		contentType: 'application/json; charset=UTF-8',
		dataType: 'json',
	    type:'POST',
	    success: function(data) {
	    	$.each(data, function(index, member) {
				addMember(member);
			});
	    	//make the table click-able
	    	$('#memberTable tr').on('click', memberTableClick);
	    }
	});
}

/**
 * Add a new row to the table representing the member
 * @param member The member object to add
 */
function addMember(member) {
	$('#memberTable').append(
		$('<tr>').attr('id', member.id)
			.append($('<td>').append(member.applicationTime))
			.append($('<td>').append(member.firstName))
			.append($('<td>').append(member.lastName))
			.append($('<td>').append(member.addressLine1).append($('<br/>'))
					.append(member.addressLine2 + ' ' + member.city + ', ' + member.state + ' ' + member.zip))
			.append($('<td>').append(member.memberType))
			.append($('<td>').append(member.validationInput))
			.append($('<td>').append(member.memberStatus))
			.append($('<td>').append(member.email))
			.append(makeDependents(member.dependents))
	);
}

/**
 * Add a table entry with a list of dependents for the given member.
 * @param dependents
 * @returns
 */
function makeDependents(dependents) {
	var root = $('<td>');
	if(dependents && dependents !== null) {
		$.each(dependents, function(index, dependent) {
			var str = dependent.relationType + ': ' + dependent.name.firstName + ' ' + dependent.name.lastName + ' ' +
				(dependent.extraData == null ? '' : '(' + dependent.extraData + ')');
			root.append($('<p>').append(str));
		});
	}
	
	return root;
}

var iLastRowSel = null;

/**
 * Handle regular, shift- and control-click events on the table.
 * @param event
 */
function memberTableClick(event) {
    var iRowSel = $(this).closest("tr").prevAll("tr").length + 1;      
	if(event.ctrlKey) {
		//simple case - control click.  Add or remove selected class
	    if ($(this).hasClass('row_selected')) {
	        $(this).removeClass('row_selected info');
	    }
	    else {
	        $(this).addClass('row_selected info');
	    }
	}
	else {
    	//clear previously selected
		$('#memberTable tr').removeClass('row_selected info');
		if (event.shiftKey) {
	    	//shift click
	        var start = Math.min(iRowSel, iLastRowSel);
	        var end = Math.max(iRowSel, iLastRowSel);
	    	$('#memberTable').find('tr:gt('+(start-1)+'):lt('+(end)+')').addClass('row_selected info');
	    } else {
	    	//regular click
	    	$(this).addClass('row_selected info');
	    }
	}
    iLastRowSel = iRowSel;
}

function clickApprove(event) {
	$('#confirmDlg').modal('show');
}

function approveForm(event) {
	disableConfirmForm();
	
	var requestData = [ ];
	
	$('tr.row_selected').each(function(index, elem) {
		var memberUpdateRequest = { };
		memberUpdateRequest.id = elem.id;
		memberUpdateRequest.memberStatus = "APPROVED";
		requestData[index] = memberUpdateRequest; 
	});

	var url = "/manage/update-applications.json";
	$.ajax(url, {
		data: JSON.stringify(requestData),
		contentType: 'application/json; charset=UTF-8',
		dataType: 'json',
	    type:'POST',
		success: function(response) {
			if (response.status == 'FAIL') {
				processServerErrors(response.errorMessageList);
			}
			else {
				showSuccess("Submission Successful", "Your changes were successfully committted.  Please refresh the page to see your changes take effect.");
			}
			
		}, 
		error: function(jqXHR, textStatus, errorThrown) {
			showError('Server Error', 'There were errors with your submission. Server Responded with ' + textStatus + ': ' + errorThrown);
		}
	});
	return false;

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
 * Show the response modal dialog box in a "success" state with the specified header and message.
 * @param errorLabel The title of the message box
 * @param errorBody The message to put in the message box body.
 */
function showSuccess(msgLabel, msgBody) {
	$('#confirmDlg').modal('hide');
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

function editForm(event) {
	//do nothing
	//$(this).find('.row_selected')
}

function clickExport(event) {
	var status = $('#navbar li.active a').attr('status');
	//do nothing with status
	$('#downloadForm').submit();
}