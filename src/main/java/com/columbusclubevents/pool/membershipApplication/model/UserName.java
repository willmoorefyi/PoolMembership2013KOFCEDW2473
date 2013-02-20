package com.columbusclubevents.pool.membershipApplication.model;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * The name of an individual user, useful for searching across both members and dependents
 * 
 * @author wmoore
 *
 */
public class UserName {

	@NotEmpty
	private String firstName;
	
	private char middleInitial;
	
	@NotEmpty
	private String lastName;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public char getMiddleInitial() {
		return middleInitial;
	}

	public void setMiddleInitial(char middleInitial) {
		this.middleInitial = middleInitial;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
