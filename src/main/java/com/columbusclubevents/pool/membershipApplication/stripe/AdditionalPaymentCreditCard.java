package com.columbusclubevents.pool.membershipApplication.stripe;

import com.columbusclubevents.pool.membershipApplication.model.Member;
import com.columbusclubevents.pool.membershipApplication.model.MemberAdditionalPayment;
import com.google.common.base.Objects;
import org.hibernate.validator.constraints.NotEmpty;

public class AdditionalPaymentCreditCard {
	PaymentCreditCard paymentCreditCard;

	private String paymentId;
	private String originalPaymentAmount;
	private String finalMemberAmount;

	public static AdditionalPaymentCreditCard fromPayment(MemberAdditionalPayment additionalPayment, Member member) {
		AdditionalPaymentCreditCard additionalPaymentCreditCard = new AdditionalPaymentCreditCard();
		additionalPaymentCreditCard.paymentCreditCard = PaymentCreditCard.fromMember(member);
		//clear out the amount
		additionalPaymentCreditCard.paymentCreditCard.setAmount(additionalPayment.getMemberPayment().toString());
		additionalPayment.setPaymentId(additionalPayment.getId().toString());
		return additionalPaymentCreditCard;
	}

	public PaymentCreditCard getPaymentCreditCard() {
		return paymentCreditCard;
	}

	public void setPaymentCreditCard(PaymentCreditCard paymentCreditCard) {
		this.paymentCreditCard = paymentCreditCard;
	}

	@NotEmpty(message="A valid payment ID is required for making additional member payments")
	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getOriginalPaymentAmount() {
		return originalPaymentAmount;
	}

	public void setOriginalPaymentAmount(String originalPaymentAmount) {
		this.originalPaymentAmount = originalPaymentAmount;
	}

	public String getFinalMemberAmount() {
		return finalMemberAmount;
	}

	public void setFinalMemberAmount(String finalMemberAmount) {
		this.finalMemberAmount = finalMemberAmount;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("paymentCreditCard", paymentCreditCard)
				.add("paymentId", paymentId)
				.add("originalPaymentAmount", originalPaymentAmount)
				.add("finalMemberAmount", finalMemberAmount)
				.toString();
	}
}
