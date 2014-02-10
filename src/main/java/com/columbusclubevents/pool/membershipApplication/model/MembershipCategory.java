package com.columbusclubevents.pool.membershipApplication.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.codehaus.jackson.annotate.JsonManagedReference;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class MembershipCategory implements Serializable {

	@Transient
	Logger log = LoggerFactory.getLogger(MembershipCategory.class);
	private static final long serialVersionUID = -8630292711855527925L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;
	
	private String tabDescription;
	
	private String userDescription;
	
	private boolean validationRequired;
	
	private String validationText;
	
	private String validationHint;
	
	private String validationConstraint;

	private List<MembershipOption> memberOptions;
	
	public MembershipCategory() {
		this.memberOptions = new ArrayList<MembershipOption>();
		this.tabDescription = null;
		this.userDescription = null;
		this.validationRequired = false;
		this.validationText = null;
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
	}

	@NotEmpty
	public String getTabDescription() {
		return tabDescription;
	}

	public void setTabDescription(String tabDescription) {
		this.tabDescription = tabDescription;
	}

	@NotEmpty
	public String getUserDescription() {
		return userDescription;
	}

	public void setUserDescription(String userDescription) {
		this.userDescription = userDescription;
	}

	public boolean isValidationRequired() {
		return validationRequired;
	}

	public void setValidationRequired(boolean validationRequired) {
		this.validationRequired = validationRequired;
	}

	public String getValidationText() {
		return validationText;
	}

	public void setValidationText(String validationText) {
		this.validationText = validationText;
	}

	public String getValidationHint() {
		return validationHint;
	}

	public void setValidationHint(String validationHint) {
		this.validationHint = validationHint;
	}

	public String getValidationConstraint() {
		return validationConstraint;
	}

	public void setValidationConstraint(String validationConstraint) {
		this.validationConstraint = validationConstraint;
	}

	@Valid
	@Size(min=1)
	@JsonManagedReference
	@OneToMany(mappedBy = "memberCategoryParent", targetEntity=MembershipOption.class, cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	public List<MembershipOption> getMemberOptions() {
		return memberOptions;
	}

	public void setMemberOptions(List<MembershipOption> memberOptions) {
		this.memberOptions = memberOptions;
	}
	
    @PreUpdate
    @PrePersist
    public void updateMemberOptionGraph() {
    	for(MembershipOption opt : this.memberOptions)  {
    		log.debug("Setting member category parent for option with key=" + opt.getOptionKey());
    		opt.setMemberCategoryParent(this);
    	}
    }
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MembershipCategory [");
		sb.append("Id=");sb.append(Id);
		sb.append(", tabDescription=");sb.append(tabDescription);
		sb.append(", userDescription=");sb.append(userDescription);
		sb.append(", validationRequired=");sb.append(validationRequired);
		sb.append(", validationText=");sb.append(validationText);
		sb.append(", validationHint=");sb.append(validationHint);
		sb.append(", validationConstraint=");sb.append(validationConstraint);
		sb.append(", memberOptions=");
		for(MembershipOption opt : memberOptions) {
			sb.append("{");sb.append(opt.toString());sb.append("} ");
		}
		sb.append("] ");
		return sb.toString();
	}
}
