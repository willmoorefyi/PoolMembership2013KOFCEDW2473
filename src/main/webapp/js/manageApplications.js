$(document).ready(function() {
	getCurrentMemberStatusValues();
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
			//sorttable.makeSortable($('#memberTable').get(0));
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
			.append($('<td>').append(member.firstName))
			.append($('<td>').append(member.lastName))
			.append($('<td>').append(member.memberType))
			.append($('<td>').append(member.validationInput))
			.append($('<td>').append(member.memberStatus))
			.append($('<td>').append(member.email))
			.append(makeDependents(member.dependents))
	);
}

/**
 * Add a table entry with a list of dpeendents for the given member.
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
