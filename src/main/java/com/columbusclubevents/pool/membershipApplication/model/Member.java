package com.columbusclubevents.pool.membershipApplication.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonManagedReference;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single member.  Should have getters /setters etc, but this just works.
 * 
 * @author wmoore
 *
 */
@Entity
public class Member implements Serializable {
	Logger log = LoggerFactory.getLogger(Member.class);
	private static final long serialVersionUID = 6864054721613306242L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String firstName;
	private Character middleInitial;
	private String lastName;
	
	private List<Dependent> dependents;
	
	private String addressLine1;
	private String addressLine2;
	private String city;
	private String state;
	private String zip;
	
	private String primaryPhone;
	private String secondaryPhone;
	private String email;
	
	private String validationInput;
	private String memberType;
	private MemberStatus memberStatus;
	private Integer memberCost;
	//private String paymentOption;
	private Boolean memberPaid;
	private String paymentId;
	
	//not handling time zones, no idea what GAE is doing with time zones here.
	private Date applicationTime;
	
	//google doesn't really handle unowned relationships in JPA well
	//private MembershipOption membershipOption;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
	@Pattern(regexp="^(A[LKSZRAEP]|C[AOT]|D[EC]|F[LM]|G[AU]|HI|I[ADLN]|K[SY]|LA|M[ADEHINOPST]|N[CDEHJMVY]|O[HKR]|P[ARW]|RI|S[CD]|T[NX]|UT|V[AIT]|W[AIVY])$",
			message="Please enter a valid 2-character US state code")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	@NotEmpty(message="Zip Code cannot be empty.")
	@Pattern(regexp = "^\\d{5}(?:[-\\s]\\d{4})?$", 
			message="Please enter a valid US zip code (5 digits, optionally with 4 digit extension)")
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	@NotEmpty @Pattern(regexp="^$|1?\\W*([2-9][0-8][0-9])\\W*([2-9][0-9]{2})\\W*([0-9]{4})(\\s?e?x?t?(\\d*))?", 
			message="Please enter a valid US phone number (10 digits, with or without sentinel characters and extension)")
	public String getPrimaryPhone() {
		return primaryPhone;
	}

	public void setPrimaryPhone(String primaryPhone) {
		this.primaryPhone = primaryPhone;
	}
	
	@Pattern(regexp="^$|1?\\W*([2-9][0-8][0-9])\\W*([2-9][0-9]{2})\\W*([0-9]{4})(\\s?e?x?t?(\\d*))?", 
			message="Please enter a valid US phone number (10 digits, with or without sentinel characters and extension)")
	public String getSecondaryPhone() {
		return secondaryPhone;
	}

	public void setSecondaryPhone(String secondaryPhone) {
		this.secondaryPhone = secondaryPhone;
	}

	@NotEmpty(message="You must specify an email address")
	@Email(message="Please enter a valid email address")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Valid
	@JsonManagedReference
	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	public List<Dependent> getDependents() {
		return dependents;
	}

	public void setDependents(List<Dependent> dependents) {
		this.dependents = dependents;
	}

	public String getValidationInput() {
		return validationInput;
	}

	public void setValidationInput(String validationInput) {
		this.validationInput = validationInput;
	}

	public Integer getMemberCost() {
		return memberCost;
	}

	public void setMemberCost(Integer memberCost) {
		this.memberCost = memberCost;
	}

    @Enumerated(EnumType.STRING)
	public MemberStatus getMemberStatus() {
		return memberStatus;
	}

	public void setMemberStatus(MemberStatus memberStatus) {
		this.memberStatus = memberStatus;
	}

	public String getMemberType() {
		return memberType;
	}

	public void setMemberType(String memberType) {
		this.memberType = memberType;
	}

	/*
	@NotEmpty(message="You must specify a payment option")
	public String getPaymentOption() {
		return paymentOption;
	}

	public void setPaymentOption(String paymentOption) {
		this.paymentOption = paymentOption;
	}
	*/

	public Boolean getMemberPaid() {
		return memberPaid;
	}

	public void setMemberPaid(Boolean memberPaid) {
		this.memberPaid = memberPaid;
	}

	@NotEmpty(message = "You must specify a first name")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public Character getMiddleInitial() {
		return middleInitial;
	}

	public void setMiddleInitial(Character middleInitial) {
		this.middleInitial = middleInitial;
	}

	@NotEmpty(message = "You must specify a last name")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public Date getApplicationTime() {
		return applicationTime;
	}

	public void setApplicationTime(Date applicationTime) {
		this.applicationTime = applicationTime;
	}

    @PreUpdate
    public void updateMemberValues() {
    	log.debug("Converting values to appropriate case");
    	this.firstName = StringUtils.upperCase(this.firstName);
    	this.middleInitial = (this.middleInitial == null ? null : Character.toUpperCase(this.middleInitial));
    	this.lastName = StringUtils.upperCase(this.lastName);
    	
    	this.addressLine1 = StringUtils.upperCase(this.addressLine1);
    	this.addressLine2 = StringUtils.upperCase(this.addressLine2);
    	this.city = StringUtils.upperCase(this.city);
    	this.state = StringUtils.upperCase(this.state);
    	
    	this.email = StringUtils.lowerCase(this.email);
    }
    
    @PrePersist
    public void setApplicationDate() {
    	log.debug("Setting application date");
    	this.applicationTime = new Date();
    	
    	this.updateMemberValues();
    }
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Member [id=").append(id).append(", firstName=")
				.append(firstName).append(", middleInitial=")
				.append(middleInitial).append(", lastName=").append(lastName)
				.append(", dependents=").append(dependents)
				.append(", addressLine1=").append(addressLine1)
				.append(", addressLine2=").append(addressLine2)
				.append(", city=").append(city).append(", state=")
				.append(state).append(", zip=").append(zip)
				.append(", primaryPhone=").append(primaryPhone)
				.append(", secondaryPhone=").append(secondaryPhone)
				.append(", email=").append(email).append(", validationInput=")
				.append(validationInput).append(", memberType=")
				.append(memberType).append(", memberStatus=")
				.append(memberStatus).append(", memberCost=")
				.append(memberCost).append(", memberPaid=")
				.append(memberPaid).append("paymentId, =")
				.append(paymentId).append("]");
		return builder.toString();
	}
	
	/*
	@NotNull 
	@Valid
	@OneToOne(cascade = CascadeType.ALL)
	public PersonName getName() {
		return name;
	}

	public void setName(PersonName name) {
		this.name = name;
	}
	*/
}
