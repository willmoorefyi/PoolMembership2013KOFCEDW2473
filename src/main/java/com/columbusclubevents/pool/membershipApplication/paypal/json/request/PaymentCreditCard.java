package com.columbusclubevents.pool.membershipApplication.paypal.json.request;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.util.MultiValueMap;

import com.columbusclubevents.pool.membershipApplication.model.Member;

/**
 * A payment credit card object
 * @author wmoore
 *
 */
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
	
	public static PaymentCreditCard fromMultiValueMap(MultiValueMap<String, String> formInput) {
		PaymentCreditCard paymentCC = new PaymentCreditCard();
		
		paymentCC.memberId = formInput.getFirst("memberId");
		paymentCC.lastName = formInput.getFirst("lastName");
		
		paymentCC.addressLine1 = formInput.getFirst("addressLine1");
		paymentCC.addressLine2 = formInput.getFirst("addressLine2");
		paymentCC.addressCity = formInput.getFirst("addressCity");
		paymentCC.addressState = formInput.getFirst("addressState");
		paymentCC.addressPostalCode = formInput.getFirst("addressPostalCode");
		paymentCC.addressPhone = formInput.getFirst("addressPhone");

		paymentCC.ccType = formInput.getFirst("ccType");
		paymentCC.ccNumber = formInput.getFirst("ccNumber");
		paymentCC.ccExpireMonth = formInput.getFirst("ccExpireMonth");
		paymentCC.ccExpireYear = formInput.getFirst("ccExpireYear");
		paymentCC.ccCvv2 = formInput.getFirst("ccCvv2");
		
		return paymentCC;
	}
	
	public static PaymentCreditCard fromMember(Member member) {
		PaymentCreditCard paymentCC = new PaymentCreditCard();
		
		paymentCC.memberId = member.getId().toString();
		paymentCC.lastName = member.getLastName();
		
		paymentCC.addressLine1 = member.getAddressLine1();
		paymentCC.addressLine2 = member.getAddressLine2();
		paymentCC.addressCity = member.getCity();
		paymentCC.addressState = member.getState();
		paymentCC.addressPostalCode = member.getZip();
		paymentCC.addressPhone = member.getPrimaryPhone();
		
		return paymentCC;
	}

	@NotEmpty(message="Something is wrong, your request must have an associated Member ID")
	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	@NotEmpty(message="Something is wrong, your request must have an associated Last Name")
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
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
	public String getAddressCity() {
		return addressCity;
	}

	public void setAddressCity(String addressCity) {
		this.addressCity = addressCity;
	}

	@NotEmpty(message="You must specify a state")
	@Pattern(regexp="^(A[LKSZRAEP]|C[AOT]|D[EC]|F[LM]|G[AU]|HI|I[ADLN]|K[SY]|LA|M[ADEHINOPST]|N[CDEHJMVY]|O[HKR]|P[ARW]|RI|S[CD]|T[NX]|UT|V[AIT]|W[AIVY])$",
			message="Please enter a valid 2-character US state code")
	public String getAddressState() {
		return addressState;
	}

	public void setAddressState(String addressState) {
		this.addressState = addressState;
	}

	@NotEmpty(message="Zip Code cannot be empty.")
	@Pattern(regexp = "^\\d{5}(?:[-\\s]\\d{4})?$", 
			message="Please enter a valid US zip code (5 digits, optionally with 4 digit extension)")
	public String getAddressPostalCode() {
		return addressPostalCode;
	}

	public void setAddressPostalCode(String addressPostalCode) {
		this.addressPostalCode = addressPostalCode;
	}

	@NotEmpty @Pattern(regexp="^$|1?\\W*([2-9][0-8][0-9])\\W*([2-9][0-9]{2})\\W*([0-9]{4})(\\s?e?x?t?(\\d*))?", 
			message="Please enter a valid US phone number (10 digits, with or without sentinel characters and extension)")
	public String getAddressPhone() {
		return addressPhone;
	}

	public void setAddressPhone(String addressPhone) {
		this.addressPhone = addressPhone;
	}

	@NotEmpty(message="You must specify a CC type")
	@Pattern(regexp="^visa|mastercard|amex|discover$", 
			message="Please select a valid Credit Card type.  Options are Visa, Mastercard, American Express, and Discover")
	public String getCcType() {
		return ccType;
	}

	public void setCcType(String ccType) {
		this.ccType = ccType;
	}

	@Pattern(regexp = "^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})$", 
			message="Please enter a valid credit card number (must be Visa, Mastercard, America Express, or Discover")
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

	@NotEmpty(message="A card security code is required" )
	@Pattern(regexp = "^(?!000)\\d{3,4}$", message="Please enter a valid card security code")
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
