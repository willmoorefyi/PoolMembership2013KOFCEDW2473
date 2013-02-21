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
            .attr('id', 'relationFirstName' + totalRelations).attr('name', 'relationFirstName').attr('type', 'text').attr('placeholder', 'First Name').attr('title', 'First Name') 
         )
      ).append($('<td>')
         .append($('<input>')
            .attr('id', 'relationLastName' + totalRelations).attr('name', 'relationLastName').attr('type', 'text').attr('placeholder', 'Last Name').attr('title', 'Last Name') 
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
 * Reset the cost element, then validate the input elements for a given input box and, if it matches the pattern, display the cost in the input box
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
	$('#confirmDlg').modal('show');
}

/**
 * Submit the form to the server, including all selections.  Will respond with a status, which will then be shown to the user in the modal dialog boxes
 * @returns {Boolean}
 */
function submitForm() {
	$('#confirmDlg').modal('hide');

	var formObj = { 'member' : {} };
	//serialize all the simple objects
	$('#application-form input:not(#RelationTable input, #rateTabsContent input)').each(function(index) {
		formObj.member[$(this).attr('name')] = $(this).val();
	});
	$('application-form #RelationTable tbody tr').find('td').each(function(index) {
		if(index == 0) {
			formObj.member['dependent'] = [];
		}
		$(this).find('input, select').each(function(j) {
			formObj.member.dependent[index][$(this).attr('name')] = $(this).val();
		});
	});
	
	$.ajax(formUrl, {
		data: JSON.stringify(formObj),
		contentType: 'application/json; charset=UTF-8',
		dataType: 'json',
	    type:'POST',
		success: function(response) {
			if (response.status == 'FAIL') {
				$('#responseDlgTitle').text('Error');
				$('#responseDlgBody').text('There were errors with your submission.');
				$('#responseDlgOk').click(function() {
					$('#responseDlg').modal('hide');
				});
				$('#responseDlg').modal('show');
			}
			else {
				$('#responseDlgTitle').text('Success!');
				$('#responseDlgBody').text('Your changes were successfully committed.');
				$('#responseDlgOk').click(function() {
					$('#responseDlg').modal('hide');
				});
				$('#responseDlg').modal('show');
			}
			
		}, 
		error: function(jqXHR, textStatus, errorThrown) {
			$('#responseDlgTitle').text('Error');
			$('#responseDlgBody').text('There were errors with your submission. Server Responded with ' + textStatus + ': ' + errorThrown);
			$('#responseDlgOk').click(function() {
				$('#responseDlg').modal('hide');
			});
			$('#responseDlg').modal('show');
		}
	});
	return false;
}