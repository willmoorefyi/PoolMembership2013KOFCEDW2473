package com.columbusclubevents.pool.membershipApplication.model;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class MemberRequest {

	private Member member;
	
	private MembershipOption membershipOption;
	
	@Valid
	@NotNull
	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
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
		StringBuilder builder = new StringBuilder();
		builder.append("MemberRequest [member=").append(member)
				.append(", memberOpt=").append(membershipOption).append("]");
		return builder.toString();
	}
	
}
