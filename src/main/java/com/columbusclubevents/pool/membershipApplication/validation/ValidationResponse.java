package com.columbusclubevents.pool.membershipApplication.validation;

import java.util.List;

public class ValidationResponse {
	private String status;
	private List<ErrorMessage> errorMessageList;
	private String successIdentifier;
	private String lastName;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<ErrorMessage> getErrorMessageList() {
		return this.errorMessageList;
	}

	public void setErrorMessageList(List<ErrorMessage> errorMessageList) {
		this.errorMessageList = errorMessageList;
	}

	public String getSuccessIdentifier() {
		return successIdentifier;
	}

	public void setSuccessIdentifier(String successIdentifier) {
		this.successIdentifier = successIdentifier;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	
}
