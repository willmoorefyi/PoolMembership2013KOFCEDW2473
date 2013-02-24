package com.columbusclubevents.pool.membershipApplication.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.codehaus.jackson.annotate.JsonManagedReference;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Represents a single member.  Should have getters /setters etc, but this just works.
 * 
 * @author wmoore
 *
 */
@Entity
public class Member implements Serializable {
	
	private static final long serialVersionUID = 6864054721613306242L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;
	
	private PersonName name;
	
	private String addressLine1;
	
	private String addressLine2;
	
	private String city;
	
	private String state;
	
	private String zip;
	
	private String primaryPhone;
	
	private String secondaryPhone;
	
	private String email;
	
	private String validationInput;
	
	private MembershipOption membershipOption;
	
	private String memberStatus = "new";
	
	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}

	@NotNull 
	@Valid
	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	public PersonName getName() {
		return name;
	}

	public void setName(PersonName name) {
		this.name = name;
	}

	@NotEmpty(message="You must specify an address")
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

	@NotEmpty(message="You must specify a city")
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@NotEmpty(message="You must specify a state")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	@NotEmpty 
	@Pattern(regexp = "^\\d{5}(?:[-\\s]\\d{4})?$", 
			message="Please enter a valid US zip code (5 digits, optionally with 4 digit extension)")
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}
	
	@Valid
	@JsonManagedReference
	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	public List<Dependent> dependents;

	@NotEmpty @Pattern(regexp="1?\\W*([2-9][0-8][0-9])\\W*([2-9][0-9]{2})\\W*([0-9]{4})(\\s?e?x?t?(\\d*))?", 
			message="Please enter a valid US phone number (10 digits, with or without sentinel characters and extension)")
	public String getPrimaryPhone() {
		return primaryPhone;
	}

	public void setPrimaryPhone(String primaryPhone) {
		this.primaryPhone = primaryPhone;
	}
	
	@Pattern(regexp="1?\\W*([2-9][0-8][0-9])\\W*([2-9][0-9]{2})\\W*([0-9]{4})(\\s?e?x?t?(\\d*))?", 
			message="Please enter a valid US phone number (10 digits, with or without sentinel characters and extension)")
	public String getSecondaryPhone() {
		return secondaryPhone;
	}

	public void setSecondaryPhone(String secondaryPhone) {
		this.secondaryPhone = secondaryPhone;
	}

	@NotEmpty @Email(message="Please enter a valid email address")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getValidationInput() {
		return validationInput;
	}

	public void setValidationInput(String validationInput) {
		this.validationInput = validationInput;
	}

	public String getMemberStatus() {
		return memberStatus;
	}

	public void setMemberStatus(String memberStatus) {
		this.memberStatus = memberStatus;
	}

	public List<Dependent> getDependents() {
		return dependents;
	}

	public void setDependents(List<Dependent> dependents) {
		this.dependents = dependents;
	}

	@NotNull
	public MembershipOption getMembershipOption() {
		return membershipOption;
	}

	public void setMembershipOption(MembershipOption membershipOption) {
		this.membershipOption = membershipOption;
	}

	@Override
	public String toString() {
		return "Member [Id=" + Id + ", name=" + name + ", addressLine1="
				+ addressLine1 + ", addressLine2=" + addressLine2 + ", city="
				+ city + ", state=" + state + ", zip=" + zip
				+ ", primaryPhone=" + primaryPhone + ", secondaryPhone="
				+ secondaryPhone + ", email=" + email + ", validationInput="
				+ validationInput + ", mebershipOption=" + membershipOption
				+ ", memberStatus=" + memberStatus + ", dependents="
				+ dependents + "]";
	}

}
