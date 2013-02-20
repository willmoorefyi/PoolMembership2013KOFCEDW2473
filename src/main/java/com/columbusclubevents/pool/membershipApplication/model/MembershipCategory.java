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
import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

@Entity
public class MembershipCategory implements Serializable {
	private static final long serialVersionUID = -8630292711855527925L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;
	
	@NotEmpty
	public String tabDescription;
	
	@NotEmpty
	public String userDescription;
	
	public boolean validationRequired;
	
	public String validationText;
	
	public String validationHint;
	
	public String validationConstraint;

	@Valid
	@Size(min=1)
	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	public List<MembershipOption> memberOptions;
	
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

	public String getTabDescription() {
		return tabDescription;
	}

	public void setTabDescription(String tabDescription) {
		this.tabDescription = tabDescription;
	}

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


	public void setMemberOptions(List<MembershipOption> memberOptions) {
		this.memberOptions = memberOptions;
	}

	public List<MembershipOption> getMemberOptions() {
		return memberOptions;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Id == null) ? 0 : Id.hashCode());
		result = prime * result
				+ ((memberOptions == null) ? 0 : memberOptions.hashCode());
		result = prime * result
				+ ((tabDescription == null) ? 0 : tabDescription.hashCode());
		result = prime * result
				+ ((userDescription == null) ? 0 : userDescription.hashCode());
		result = prime
				* result
				+ ((validationConstraint == null) ? 0 : validationConstraint
						.hashCode());
		result = prime * result
				+ ((validationHint == null) ? 0 : validationHint.hashCode());
		result = prime * result + (validationRequired ? 1231 : 1237);
		result = prime * result
				+ ((validationText == null) ? 0 : validationText.hashCode());
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
		MembershipCategory other = (MembershipCategory) obj;
		if (Id == null) {
			if (other.Id != null)
				return false;
		} else if (!Id.equals(other.Id))
			return false;
		if (memberOptions == null) {
			if (other.memberOptions != null)
				return false;
		} else if (!memberOptions.equals(other.memberOptions))
			return false;
		if (tabDescription == null) {
			if (other.tabDescription != null)
				return false;
		} else if (!tabDescription.equals(other.tabDescription))
			return false;
		if (userDescription == null) {
			if (other.userDescription != null)
				return false;
		} else if (!userDescription.equals(other.userDescription))
			return false;
		if (validationConstraint == null) {
			if (other.validationConstraint != null)
				return false;
		} else if (!validationConstraint.equals(other.validationConstraint))
			return false;
		if (validationHint == null) {
			if (other.validationHint != null)
				return false;
		} else if (!validationHint.equals(other.validationHint))
			return false;
		if (validationRequired != other.validationRequired)
			return false;
		if (validationText == null) {
			if (other.validationText != null)
				return false;
		} else if (!validationText.equals(other.validationText))
			return false;
		return true;
	}
}
