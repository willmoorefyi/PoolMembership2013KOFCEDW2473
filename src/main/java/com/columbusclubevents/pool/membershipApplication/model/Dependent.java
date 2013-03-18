package com.columbusclubevents.pool.membershipApplication.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.codehaus.jackson.annotate.JsonBackReference;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.appengine.api.datastore.Key;

@Entity
public class Dependent implements Serializable {

	private static final long serialVersionUID = 3310217138265498119L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key Id;

	private Member parent;
	
	private PersonName name;
	
	private String relationType;
	
	private String extraData;

	public Key getId() {
		return Id;
	}

	public void setId(Key id) {
		Id = id;
	}

	@JsonBackReference
	@ManyToOne(fetch=FetchType.LAZY)
	public Member getParent() {
		return parent;
	}

	public void setParent(Member parent) {
		this.parent = parent;
	}

	@NotNull
	@Valid
	@OneToOne(cascade = CascadeType.ALL)
	public PersonName getName() {
		return name;
	}

	public void setName(PersonName name) {
		this.name = name;
	}

	@NotEmpty
	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}
	
	public String getExtraData() {
		return extraData;
	}

	public void setExtraData(String extraData) {
		this.extraData = extraData;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Dependent [Id=").append(Id).append(", name=").append(name)
				.append(", relationType=").append(relationType)
				.append(", extraData=").append(extraData).append("]");
		return builder.toString();
	}
}
