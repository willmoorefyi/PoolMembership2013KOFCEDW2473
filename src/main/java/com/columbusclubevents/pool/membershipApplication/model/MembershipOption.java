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
}
