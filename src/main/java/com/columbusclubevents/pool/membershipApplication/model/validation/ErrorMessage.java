package com.columbusclubevents.pool.membershipApplication.model.validation;

public class ErrorMessage {

	private String fieldName;
	private String message;
	
	public ErrorMessage(String fieldName, String message) {
		this.fieldName = fieldName;
		this.message = message;
	}
	public String getFieldName() {
		return fieldName;
	}
	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return "ErrorMessage [fieldName=" + fieldName + ", message=" + message + "]";
	}
}
