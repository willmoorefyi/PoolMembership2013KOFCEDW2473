package com.columbusclubevents.pool.membershipApplication.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.appengine.api.datastore.Key;

/**
 * The name of an individual user, useful for searching across both members and dependents
 * 
 * @author wmoore
 *
 */
@Entity
public class PersonName implements Serializable {
	
	private static final long serialVersionUID = 527119005059887705L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key Id;
	
	private String firstName;
	
	private char middleInitial;
	
	private String lastName;

	public Key getId() {
		return Id;
	}

	public void setId(Key id) {
		Id = id;
	}

	@NotEmpty(message = "You must specify a first name")
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

	@NotEmpty(message = "You must specify a last name")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	public String toString() {
		return "UserName [Id=" + Id + ", firstName=" + firstName
				+ ", middleInitial=" + middleInitial + ", lastName=" + lastName
				+ "]";
	}
}
