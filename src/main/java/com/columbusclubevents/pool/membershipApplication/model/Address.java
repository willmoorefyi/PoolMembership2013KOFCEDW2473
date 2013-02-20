package com.columbusclubevents.pool.membershipApplication.model;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;

public class Address {

	@NotEmpty
	public String addressLine1;
	
	public String addressLine2;
	
	@NotEmpty
	public String city;
	
	@NotEmpty
	public String state;
	
	@NotEmpty 
	@Pattern(regexp = "^\\d{5}(?:[-\\s]\\d{4})?$")
	public String zip;

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}
}
