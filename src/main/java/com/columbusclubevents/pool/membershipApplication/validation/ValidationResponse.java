package com.columbusclubevents.pool.membershipApplication.validation;

import java.util.List;

public class ValidationResponse {
	private String status;
	private List<ErrorMessage> errorMessageList;
	private String successIdentifier;
	private String lastName;
	private String url;
	
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ValidationResponse{");
		sb.append("status='").append(status).append('\'');
		sb.append(", errorMessageList=").append(errorMessageList);
		sb.append(", successIdentifier='").append(successIdentifier).append('\'');
		sb.append(", lastName='").append(lastName).append('\'');
		sb.append(", url='").append(url).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
