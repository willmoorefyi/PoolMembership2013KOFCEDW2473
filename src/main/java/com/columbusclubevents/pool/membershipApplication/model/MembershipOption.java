package com.columbusclubevents.pool.membershipApplication.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.codehaus.jackson.annotate.JsonBackReference;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.appengine.api.datastore.Key;

@Entity
public class MembershipOption implements Serializable {
	private static final long serialVersionUID = -8321133238256072160L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Key Id;
	
	private String optionKey;

	private String optionLabel;
	
	private Integer cost;
	
	private MembershipCategory memberCategoryParent;

	public MembershipOption() {
		super();
	}

	public Key getId() {
		return Id;
	}
	
	public void setId(Key id) {
		Id = id;
	}

	//@Column(unique=true) //GAE doesn't support unique columns
	@NotEmpty
	public String getOptionKey() {
		return optionKey;
	}

	public void setOptionKey(String optionKey) {
		this.optionKey = optionKey;
	}

	@NotEmpty
	public String getOptionLabel() {
		return optionLabel;
	}

	public void setOptionLabel(String optionLabel) {
		this.optionLabel = optionLabel;
	}

	@NotNull
	public Integer getCost() {
		return cost;
	}

	public void setCost(Integer cost) {
		this.cost = cost;
	}

	@JsonBackReference
	@ManyToOne(fetch=FetchType.EAGER)
	public MembershipCategory getMemberCategoryParent() {
		return memberCategoryParent;
	}

	public void setMemberCategoryParent(MembershipCategory memberCategoryParent) {
		this.memberCategoryParent = memberCategoryParent;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MembershipOption [Id=").append(Id)
				.append(", optionKey=").append(optionKey)
				.append(", optionLabel=").append(optionLabel)
				.append(", cost=").append(cost)
				.append(", memberCategoryParent=").append(memberCategoryParent == null ? "null" : memberCategoryParent.getId())
				.append("]");
		return builder.toString();
	}
}
