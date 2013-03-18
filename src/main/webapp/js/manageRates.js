$(document).ready(function() {
	$('#addRateButton').click(addRateFormRow);
	$('#submitButton').click(confirmForm);
	$('#confirmDlgOk').click(submitForm);
	getCurrentRateForm();
});

/**
 * Get the current rate form.  Called when the form is initialized to get the current rates in the backing store
 */
function getCurrentRateForm() {
	$.getJSON(currentRateUrl, function(data) {
		$.each(data.memberCategories, function() {
			addRateFormRow(this);
		});
	});
}

/**
 * Add a new row to the rate form, so a new rate tab can be created
 * @param inputData The input data used to create the row, usually only specified on the initial page form when the rates are loaded from the server
 */
function addRateFormRow(inputData) {
	var randomnumber=Math.floor(Math.random()*100000);
	$("#RateForm > fieldset")
		.append($('<div>').addClass('member-rate-category-block')
			.append($('<div>').addClass('row-fluid')
				.append($('<div>').addClass('span11')
					.append($('<legend>').append('Membership Category Tab'))
				)
				.append($('<div>').addClass('span1')
					.append($('<i>').addClass('icon-remove icon-remove-right').hover(function() {
							$(this).addClass('icon-highlight-hover');
						},
						function() {
							$(this).removeClass('icon-highlight-hover');
						}).click(function() {
			    			$(this).closest('div.member-rate-category-block').remove();
			    		})
					)
				)
			)
			//.append(createInputElem('hidden', 'id', null, null,  inputData && inputData.id ? inputData.id : '', null))
			.append(createFormEntryElem('text', 'tabDescription', 'Tab Description', 'span3', inputData && inputData.tabDescription ? inputData.tabDescription : ''))
			.append(createFormEntryElem('textarea', 'userDescription', 'Long Description', 'span7', inputData && inputData.userDescription ? inputData.userDescription : '', 3))
			.append(createFormEntryElem('checkbox', 'validationRequired', 'Validation Required', '', inputData && inputData.validationRequired ? inputData.validationRequired : ''))
			.append(createFormEntryElem('textarea', 'validationText', 'Validation Text', 'span7', inputData && inputData.validationText ? inputData.validationText : ''))
			.append(createFormEntryElem('text', 'validationHint', 'Validation Hint (Hover)', 'span3', inputData && inputData.validationHint ? inputData.validationHint : ''))
			.append(createFormEntryElem('text', 'validationConstraint', 'Validation Constraint (RegEx)', 'span7', inputData && inputData.validationConstraint ? inputData.validationConstraint : ''))
			.append($('<div>').addClass('row-fluid')
				.append(createMemberOptBlock(inputData && inputData.memberOptions ? inputData.memberOptions : null))
			)
			.append( $('<div>').addClass('row')
				.append($('<div>').addClass('span9 row-spacer'))
			)
		);
}

/**
 * Create the member option block, a sub-element of the member tab to contain all the membership options available under that tab
 * @param memberOptions The passed-in member options for the given tab
 * @returns The div block containing the member options
 */
function createMemberOptBlock(memberOptions) {
	var base = $('<div>').addClass('offset3 span9 member-opt-block');
	if(memberOptions && memberOptions.length > 0) {
		for(var i=0; i<memberOptions.length; i++) {
			base.append(createMemberOpt(memberOptions[i]));
		}
	} else {
		base.append(createMemberOpt());
	}
	base.append($('<button>').attr('type', 'button').addClass('btn btn-info').click(function() {
		$(this).closest('div.member-opt-block').children('button.btn').before(createMemberOpt());
	}).append('Add New Option'));
	return base;
}

/**
 * Create a single member option.  This will be represented as a radio button on the tab on the main membership form page.
 * @param memberOpt The passed-in member option to create the tab, or empty if none is defined
 * @returns The div block containing the member option with the passed-in values, or a single empty value if none is specified
 */
function createMemberOpt(memberOpt) {
	var base = $('<div>').addClass('row-fluid member-opt')
		.append($('<div>').addClass('span3').append($('<p>').append($('<strong>').append('Membership Option'))))
		.append($('<div>').addClass('span1 offset8')
			.append($('<i>').addClass('icon-remove icon-remove-right').hover(function() {
					$(this).addClass('icon-highlight-hover');
				},
				function() {
					$(this).removeClass('icon-highlight-hover');
				}).click(function() {
	    			$(this).closest('div.member-opt').remove();
	    		})
			)
		)
		//.append(createInputElem('hidden', 'id', null, null,  memberOpt && memberOpt.id ? JSON.stringify(memberOpt.id) : '', null))
		.append(createFormEntryElem('text', 'optionKey', 'Unique Option Key', 'span5', memberOpt && memberOpt.optionKey ? memberOpt.optionKey : ''))
		.append(createFormEntryElem('text', 'optionLabel', 'Option Label', 'span12', memberOpt && memberOpt.optionLabel ? memberOpt.optionLabel : ''))
		.append(createFormEntryElem('text', 'cost', 'Option Cost', 'span5', memberOpt && memberOpt.cost ? memberOpt.cost : ''));

	return base;
}


/**
 * Create an input element using Jquery and return it.  Supports both &lt;input&gt; and &lt;textarea&gt; elements.
 * Note: This is not optimized, so it will create useless and empty "type" and "row" attributes on the elements of opposite types 
 * @param inputType The type of input
 * @param inputName The name of the input element (name attribute)
 * @param labelText The text of the label for the element.  Also used for the placeholder text and title text (hover text)
 * @param cssClass The CSS class to apply to the input, for sizing
 * @param elemValue The value of the element
 * @param rows the rows on the textarea
 * @returns The created form block "control-group" div wrapped in a jquery object
 */
function createFormEntryElem(inputType, inputName, labelText, cssClass, elemValue, rows) {
	var randomnumber=Math.floor(Math.random()*100000);
	return $('<div>').addClass('control-group')
		.append($('<label>').addClass('control-label').attr('for', inputName + randomnumber).append(labelText))
		.append($('<div>').addClass('controls')
			.append(inputType == 'textarea' ? 
				createTextAreaElem(inputName, labelText, cssClass, elemValue, rows, randomnumber) : 
				createInputElem(inputType, inputName, labelText, cssClass, elemValue, randomnumber) )
		);
}

/**
 * Create an input element based on the passed-in values, for simplified form creation.  Necessary as textareas have very different behavior characteristics
 * @param inputName The name of the input element (name attribute)
 * @param titleText The placeholder text and title text (hover text)
 * @param cssClass The CSS class to apply to the input, for sizing
 * @param elemValue The value of the element
 * @param randomnumber A random number, used to match the form label to the input element to enable proper form behavior
 * @returns The created input element wrapped in a jquery object
 */
function createInputElem(inputType, inputName, titleText, cssClass, elemValue, randomnumber) {
	//set the default characteristics of a form element
	var elem = $('<input>').attr('name', inputName).attr('id', inputName + (randomnumber ? randomnumber : ''))
		.addClass(cssClass).attr('type', inputType).attr('placeholder', titleText)
		.attr('title', titleText);
	
	switch(inputType) {
		case 'checkbox' : 
			if(elemValue) elem.attr('checked', 'true');
			break;
		case 'text' :
			elem.attr('value', elemValue);
			break;
		default :
			// do nothing;
			
	}
	
	return elem;
}

/**
 * Create a text area element based on the passed-in values, for simplified form creation
 * @param inputName The name of the input element (name attribute)
 * @param titleText The placeholder text and title text (hover text)
 * @param cssClass The CSS class to apply to the input, for sizing
 * @param elemValue The value of the element
 * @param rows the rows on the textarea
 * @param randomnumber A random number, used to match the form label to the input element to enable proper form behavior
 * @returns The created textarea element wrapped in a jquery object
 */
function createTextAreaElem(inputName, titleText, cssClass, elemValue, rows, randomnumber) {
	return $('<textarea>').attr('name', inputName).attr('id', inputName + randomnumber)
		.addClass(cssClass).attr('placeholder', titleText).attr('rows', rows ? rows : '')
		.attr('title', titleText).append(elemValue);
}

/**
 * Simple method to launch the confirm form.  Currently only shows the "confirm" modal dialog.
 */
function confirmForm() {
	$('#confirmDlg').modal('show');
}

/**
 * Submit the form.  Called from the confirm modal dialog.
 * @returns {Boolean} Returns false to ensure the form isn't posted to the server, as all communication occurs via AJAX-JSON
 */
function submitForm() {
	$('#confirmDlg').modal('hide');
	var formObj = { 'memberCategories' : [] };
	$('#RateForm > fieldset').children('div.member-rate-category-block').each(function(i) {
		formObj.memberCategories[i] = { };
		var memberCategory = formObj.memberCategories[i];
		$(this).find('input:not(div.member-opt input), textarea:not(div.member-opt textarea)').each(function() {
			serializeInput(memberCategory, this);
		});
		memberCategory.memberOptions = [];
		$(this).find('div.member-opt').each(function(j) {
			memberCategory.memberOptions[j] = { };
			var memberOpt = memberCategory.memberOptions[j];
			$(this).find('input, textarea').each(function() {
				serializeInput(memberOpt, this);
			});
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

/**
 * Serialize a single object into the passed-in parent object, for JSON serialization
 * @param parent the parent object that we will serialize into, eventually for JSON
 * @param elem The elem to serialize
 */
function serializeInput(parent, elem) {
	var name = $(elem).attr('name');
	switch($(elem).attr('type')) {
	case 'checkbox' :
		//checkboxes
		parent[name] = elem.checked;
		break;
	case 'input' :
	default :
		//text and textareas
		//parse the ID fields, as those are serialized JSON values
		var val = $(elem).val();
		parent[name] = (name == 'id' && val ? JSON.parse(val) : val);
	}
}