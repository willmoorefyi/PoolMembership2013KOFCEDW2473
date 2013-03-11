package com.columbusclubevents.pool.membershipApplication.paypal.json.request;

public class PaymentCreditCard {
	private String memberId;
	private String lastName;
	
	//billing address info
	private String addressLine1;
	private String addressLine2;
	private String addressCity;
	private String addressState;
	private String addressPostalCode;
	private String addressPhone;
	
	//Credit card info
	private String ccType;
	private String ccNumber;
	private String ccExpireMonth;
	private String ccExpireYear;
	private String ccCvv2;
	
	//attributes to fill in after request completes
	private String firstName;
	private String amount;
	

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

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

	public String getAddressCity() {
		return addressCity;
	}

	public void setAddressCity(String addressCity) {
		this.addressCity = addressCity;
	}

	public String getAddressState() {
		return addressState;
	}

	public void setAddressState(String addressState) {
		this.addressState = addressState;
	}

	public String getAddressPostalCode() {
		return addressPostalCode;
	}

	public void setAddressPostalCode(String addressPostalCode) {
		this.addressPostalCode = addressPostalCode;
	}

	public String getAddressPhone() {
		return addressPhone;
	}

	public void setAddressPhone(String addressPhone) {
		this.addressPhone = addressPhone;
	}

	public String getCcType() {
		return ccType;
	}

	public void setCcType(String ccType) {
		this.ccType = ccType;
	}

	public String getCcNumber() {
		return ccNumber;
	}

	public void setCcNumber(String ccNumber) {
		this.ccNumber = ccNumber;
	}

	public String getCcExpireMonth() {
		return ccExpireMonth;
	}

	public void setCcExpireMonth(String ccExpireMonth) {
		this.ccExpireMonth = ccExpireMonth;
	}

	public String getCcExpireYear() {
		return ccExpireYear;
	}

	public void setCcExpireYear(String ccExpireYear) {
		this.ccExpireYear = ccExpireYear;
	}

	public String getCcCvv2() {
		return ccCvv2;
	}

	public void setCcCvv2(String ccCvv2) {
		this.ccCvv2 = ccCvv2;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PaymentCreditCard [memberId=").append(memberId)
				.append(", lastName=").append(lastName)
				.append(", addressLine1=").append(addressLine1)
				.append(", addressLine2=").append(addressLine2)
				.append(", addressCity=").append(addressCity)
				.append(", addressState=").append(addressState)
				.append(", addressPostalCode=").append(addressPostalCode)
				.append(", addressPhone=").append(addressPhone)
				.append(", ccType=").append(ccType).append(", ccNumber=")
				.append(ccNumber).append(", ccExpireMonth=")
				.append(ccExpireMonth).append(", ccExpireYear=")
				.append(ccExpireYear).append(", ccCvv2=").append(ccCvv2)
				.append(", firstName=").append(firstName).append(", amount=")
				.append(amount).append("]");
		return builder.toString();
	}
}
