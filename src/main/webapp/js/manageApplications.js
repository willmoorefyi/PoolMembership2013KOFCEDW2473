$(document).ready(function() {
	getCurrentMemberStatusValues();
	$('#setStatus').click(clickSetStatus);
    $('#confirmStatusDlgOk').click(setStatus);

    $('#approveButton').click(clickApprove);
    $('#confirmDlgOk').click(approveForm);

    $('#deleteButton').click(clickDelete);
    $('#deleteDlgOk').click(deleteForm);

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
            $('#status-select').append($('<option>').attr('value', status).append(status));
		});
		window.setTimeout(getDefaultApplicationStatus, 1);
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
            .append($('<td>').append(member.memberCost))
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

function clickSetStatus(event) {
    $('#statusDlg').modal('show');
}

function clickDelete(event) {
    $('#deleteDlg').modal('show');
}

function approveForm(event) {
	disableModalForm('#confirmDlgOk');
	
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
				processServerErrors(response.errorMessageList, '#confirmDlg', '#confirmDlgOk');
			}
			else {
				showSuccess("Submission Successful", "Your changes were successfully committted.  The selection will now refresh.", '#confirmDlg','#confirmDlgOk');
			}
			
		}, 
		error: function(jqXHR, textStatus, errorThrown) {
			showError('Server Error', 'There were errors with your submission. Server Responded with ' + textStatus + ': ' + errorThrown, '#confirmDlg', '#confirmDlgOk');
		}
	});
	return false;

}

function setStatus(event) {
    disableModalForm('#confirmStatusDlgOk');
    var requestData = [ ];

    var status = $('#status-select').val();
    $('tr.row_selected').each(function(index, elem) {
        var memberUpdateRequest = { };
        memberUpdateRequest.id = elem.id;
        memberUpdateRequest.memberStatus = status;
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
                processServerErrors(response.errorMessageList, '#statusDlg', '#confirmStatusDlgOk');
            }
            else {
                showSuccess("Submission Successful", "Your changes were successfully committted.  The selection will now refresh.", '#statusDlg','#confirmStatusDlgOk');
            }

        },
        error: function(jqXHR, textStatus, errorThrown) {
            showError('Server Error', 'There were errors with your submission. Server Responded with ' + textStatus + ': ' + errorThrown, '#statusDlg', '#confirmStatusDlgOk');
        }
    });
}

function deleteForm(event) {
    disableModalForm('#deleteDlgOk');

    var $selectedElem = $('tr.row_selected');
    if ($selectedElem.length < 1) {
        showError('Must Select 1', 'You must select 1 member to delete.', '#deleteDlg', '#deleteDlgOk');
    }
    else if($selectedElem.length > 1) {
        showError('Too Many Selected', 'You may only delete 1 member at a time.', '#deleteDlg', '#deleteDlgOk');
    }
    else {
        var memberDeleteId = $selectedElem.attr('id');
        $.ajax("/manage/" + memberDeleteId + "/member.json", {
            contentType: 'application/json; charset=UTF-8',
            dataType: 'json',
            type:'DELETE',
            success: function(response) {
                if (response.status == 'FAIL') {
                    processServerErrors(response.errorMessageList, '#deleteDlg', '#deleteDlgOk');
                }
                else {
                    showSuccess("Delete Successful", "Your member delete was successfully committed.  The selection will now refresh.", '#deleteDlg', '#deleteDlgOk');
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                showError('Server Error', 'There were errors with your submission. Server Responded with ' + textStatus + ': ' + errorThrown, '#deleteDlg', '#deleteDlgOk');
            }
        });
    }
}

/**
 * Disable the confirmation form while posting data
 */
function disableModalForm(formButton) {
	$(formButton).attr('disabled', 'disabled');
	$(formButton).addClass('disabled');
}

/**
 * Enable the confirmation form after data posting complete
 */
function enableModalForm(formButton) {
	$(formButton).removeAttr('disabled');
	$(formButton).removeClass('disabled');
}


/**
 * Process the errors returned by the server into the error message box
 * @param errorMessageList The list of errors from the server
 */
function processServerErrors(errorMessageList, srcDialog, srcButton) {
	var body = $('<div>').addClass('row-fluid')
		.append($('<div>').addClass('span12').append('There were errors with your submission'));
		
	$(errorMessageList).each(function(index) {
		body.append($('<div>').addClass('span12').append((index+1) + '. ' + this.message));
	});
	body.append($('<div>').addClass('span12').append('Please correct these errors and resubmit the form'));
	showError('Validation Errors on Submission', body, srcDialog, srcButton);
}

/**
 * Show the response modal dialog box in an "error" state with the specified header and message.
 * @param errorLabel The title of the message box
 * @param errorBody The message to put in the message box body. Note that this can contain HTML markup.
 */
function showError(errorLabel, errorBody, srcDialog, srcButton) {
	$(srcDialog).modal('hide');
	enableModalForm(srcButton);
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
function showSuccess(msgLabel, msgBody, srcDialog, srcButton) {
	$(srcDialog).modal('hide');
	$('#responseDlg').removeClass('alert alert-error');
	enableModalForm(srcButton);
	$('#responseDlgTitle').text(msgLabel);
	$('#responseDlgBody').empty().append(msgBody);
	$('#responseDlgOk').removeClass('btn-danger').click(function() {
		$('#responseDlg').modal('hide');
		$('#responseDlgOk').off('click');
	});
	$('#responseDlg').modal('show');
    window.setTimeout(function() {
        //$('#navbar a').get(1).click();
        $('#navbar').find('.active').children('a').click();
    }, 10);
}

function clickExport(event) {
	var status = $('#navbar li.active a').attr('status');
	//do nothing with status
	$('#downloadForm').submit();
}