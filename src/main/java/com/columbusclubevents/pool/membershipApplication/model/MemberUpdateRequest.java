package com.columbusclubevents.pool.membershipApplication.model;

public class MemberUpdateRequest {
	private Long id;
	private MemberStatus memberStatus;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public MemberStatus getMemberStatus() {
		return memberStatus;
	}
	public void setMemberStatus(MemberStatus memberStatus) {
		this.memberStatus = memberStatus;
	}
	@Override
   public String toString() {
	   StringBuilder builder = new StringBuilder();
	   builder.append("MemberUpdateRequest [id=").append(id)
	         .append(", memberStatus=").append(memberStatus).append("]");
	   return builder.toString();
   }
	
	
}
