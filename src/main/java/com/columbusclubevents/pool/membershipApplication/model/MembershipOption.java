package com.columbusclubevents.pool.membershipApplication.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.appengine.api.datastore.Key;

@Entity
public class MembershipOption implements Serializable {
	private static final long serialVersionUID = -8321133238256072160L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key Id;
	
	//@Column(unique=true) //GAE doesn't support unique columns
	@NotEmpty
	public String optionKey;

	@NotEmpty
	public String optionLabel;
	
	@NotEmpty
	@Pattern(regexp="\\d+(\\.\\d{2})?")
	public String cost;

	public MembershipOption() {
		super();
	}

	public Key getId() {
		return Id;
	}

	public String getOptionLabel() {
		return optionLabel;
	}

	public void setOptionLabel(String optionLabel) {
		this.optionLabel = optionLabel;
	}

	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	public String getOptionKey() {
		return optionKey;
	}

	public void setOptionKey(String optionKey) {
		this.optionKey = optionKey;
	}

	@Override
	public String toString() {
		return "MembershipOption [Id=" + Id + ", optionKey=" + optionKey
				+ ", optionLabel=" + optionLabel + ", cost=" + cost
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Id == null) ? 0 : Id.hashCode());
		result = prime * result + ((cost == null) ? 0 : cost.hashCode());
		result = prime * result
				+ ((optionKey == null) ? 0 : optionKey.hashCode());
		result = prime * result
				+ ((optionLabel == null) ? 0 : optionLabel.hashCode());
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
		MembershipOption other = (MembershipOption) obj;
		if (Id == null) {
			if (other.Id != null)
				return false;
		} else if (!Id.equals(other.Id))
			return false;
		if (cost == null) {
			if (other.cost != null)
				return false;
		} else if (!cost.equals(other.cost))
			return false;
		if (optionKey == null) {
			if (other.optionKey != null)
				return false;
		} else if (!optionKey.equals(other.optionKey))
			return false;
		if (optionLabel == null) {
			if (other.optionLabel != null)
				return false;
		} else if (!optionLabel.equals(other.optionLabel))
			return false;
		return true;
	}
}
