package com.columbusclubevents.pool.membershipApplication.paypal;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.paypal.adaptive.core.APICredential;

@Service
public class PaypalWrapper {
	Logger log = LoggerFactory.getLogger(PaypalWrapper.class);

	private APICredential paypalCredential;
	
	private String payPalAPIUsername;
	private String payPalAPIPassword;
	private String payPalAPISignature;
	private String payPalAppID;
	private String payPalAccountEmail;
	
	public void setupCredential() {
		log.debug("setting up login credentials");
		// Obtain the credentials from your configs 
		paypalCredential = new APICredential();
		paypalCredential.setAPIUsername(payPalAPIUsername);
		paypalCredential.setAPIPassword(payPalAPIPassword);
		paypalCredential.setSignature(payPalAPISignature);

		// setup your AppID from X.com
		paypalCredential.setAppId(payPalAppID);

		// setup your Test Business account email 
		// in most cases this would be associated with API Credentials
		paypalCredential.setAccountEmail(payPalAccountEmail);
	}

	public String getPayPalAPIUsername() {
		return payPalAPIUsername;
	}

	@Value("${payPal.APIUsername}")
	public void setPayPalAPIUsername(String payPalAPIUsername) {
		this.payPalAPIUsername = payPalAPIUsername;
	}

	public String getPayPalAPIPassword() {
		return payPalAPIPassword;
	}

	@Value("${payPal.APIPassword}")
	public void setPayPalAPIPassword(String payPalAPIPassword) {
		this.payPalAPIPassword = payPalAPIPassword;
	}

	public String getPayPalAPISignature() {
		return payPalAPISignature;
	}

	@Value("${payPal.APISignature}")
	public void setPayPalAPISignature(String payPalAPISignature) {
		this.payPalAPISignature = payPalAPISignature;
	}

	public String getPayPalAppID() {
		return payPalAppID;
	}

	@Value("${payPal.AppID}")
	public void setPayPalAppID(String payPalAppID) {
		this.payPalAppID = payPalAppID;
	}

	public String getPayPalAccountEmail() {
		return payPalAccountEmail;
	}

	@Value("${payPal.AccountEmail}")
	public void setPayPalAccountEmail(String payPalAccountEmail) {
		this.payPalAccountEmail = payPalAccountEmail;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PaypalWrapper [payPalAPIUsername=")
				.append(payPalAPIUsername).append(", payPalAPIPassword=")
				.append(payPalAPIPassword).append(", payPalAPISignature=")
				.append(payPalAPISignature).append(", payPalAppID=")
				.append(payPalAppID).append(", payPalAccountEmail=")
				.append(payPalAccountEmail).append("]");
		return builder.toString();
	}
}
