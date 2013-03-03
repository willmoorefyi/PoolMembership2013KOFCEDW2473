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

	@Override
	public String toString() {
		return "Dependent [Id=" + Id + ", name=" + name + ", relationType="
				+ relationType + "]";
	}
}
