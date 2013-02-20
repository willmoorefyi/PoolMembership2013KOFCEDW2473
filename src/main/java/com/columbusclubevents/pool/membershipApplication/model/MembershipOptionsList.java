package com.columbusclubevents.pool.membershipApplication.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

public class MembershipOptionsList {
	
	@Valid
	@Size(min=1)
	private List<MembershipCategory> memberCategories;
	
	public MembershipOptionsList() {
		this.memberCategories = new ArrayList<MembershipCategory>();
	}
	
	public void add(MembershipCategory opt) {
		this.memberCategories.add(opt);
	}

	public List<MembershipCategory> getMemberCategories() {
		return memberCategories;
	}

	public void setMemberCategories(List<MembershipCategory> memberCategories) {
		this.memberCategories = memberCategories;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MembershipOptionsList [memberCategories=");
		for(MembershipCategory opt : memberCategories) {
			sb.append("{");sb.append(opt.toString());sb.append("} ");
		}
		sb.append("]");
		return sb.toString();
	}
}
