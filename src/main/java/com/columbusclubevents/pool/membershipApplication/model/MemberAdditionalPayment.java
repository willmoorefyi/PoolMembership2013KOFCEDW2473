package com.columbusclubevents.pool.membershipApplication.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class MemberAdditionalPayment implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long memberId;

	private Integer originalMemberCost;
	private Integer finalMemberCost;
	private Integer memberPayment;

	private Boolean memberPaid;
	private String paymentId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public Integer getOriginalMemberCost() {
		return originalMemberCost;
	}

	public void setOriginalMemberCost(Integer originalMemberCost) {
		this.originalMemberCost = originalMemberCost;
	}

	public Integer getFinalMemberCost() {
		return finalMemberCost;
	}

	public void setFinalMemberCost(Integer finalMemberCost) {
		this.finalMemberCost = finalMemberCost;
	}

	public Integer getMemberPayment() {
		return memberPayment;
	}

	public void setMemberPayment(Integer memberPayment) {
		this.memberPayment = memberPayment;
	}

	public Boolean getMemberPaid() {
		return memberPaid;
	}

	public void setMemberPaid(Boolean memberPaid) {
		this.memberPaid = memberPaid;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("MemberAdditionalPayment{");
		sb.append("id=").append(id);
		sb.append(", memberId=").append(memberId);
		sb.append(", originalMemberCost=").append(originalMemberCost);
		sb.append(", finalMemberCost=").append(finalMemberCost);
		sb.append(", memberPayment=").append(memberPayment);
		sb.append(", memberPaid=").append(memberPaid);
		sb.append(", paymentId='").append(paymentId).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
