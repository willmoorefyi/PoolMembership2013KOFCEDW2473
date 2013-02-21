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
	
	@NotEmpty(message = "You must specify a first name")
	public String firstName;
	
	public char middleInitial;
	
	@NotEmpty(message = "You must specify a last name")
	public String lastName;

	@Override
	public String toString() {
		return "UserName [Id=" + Id + ", firstName=" + firstName
				+ ", middleInitial=" + middleInitial + ", lastName=" + lastName
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Id == null) ? 0 : Id.hashCode());
		result = prime * result
				+ ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result
				+ ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + middleInitial;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersonName other = (PersonName) obj;
		if (Id == null) {
			if (other.Id != null)
				return false;
		} else if (!Id.equals(other.Id))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (middleInitial != other.middleInitial)
			return false;
		return true;
	}

}
