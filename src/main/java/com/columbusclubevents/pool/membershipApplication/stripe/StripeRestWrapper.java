package com.columbusclubevents.pool.membershipApplication.stripe;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

@Service
public class StripeRestWrapper {
	Logger log = LoggerFactory.getLogger(StripeRestWrapper.class);

	private String secretKey;
	private String publishableKey;

	public PaymentCreditCardResponse postCCPayment(PaymentCreditCard paymentCC) {
		log.debug("Processing credit card payment:{}", paymentCC);

		PaymentCreditCardResponse resp = new PaymentCreditCardResponse();
		try {
			Stripe.apiKey = this.secretKey;
			
			log.debug("Creating the charge parameters map");
			Map<String, Object> chargeParams = new HashMap<String, Object>();
			
			String amount = Integer.toString(Integer.valueOf(paymentCC.getAmount())*100);
			
			log.debug("Submitt payment of '{}'", amount);
			chargeParams.put("amount", amount); 
			chargeParams.put("currency", "usd"); 
			chargeParams.put("description", "Charge for " + paymentCC.getLastName() + ":" + paymentCC.getMemberId());
			
			log.debug("Building the card parameters map");
			Map<String, Object> cardParams = new HashMap<String, Object>();
			cardParams.put("number", paymentCC.getCcNumber());
			cardParams.put("exp_month", paymentCC.getCcExpireMonth());
			cardParams.put("exp_year", paymentCC.getCcExpireYear());
			cardParams.put("cvc", paymentCC.getCcCvv2());
			cardParams.put("name", paymentCC.getFullName());
			cardParams.put("address_line1", paymentCC.getAddressLine1());
			cardParams.put("address_line2", paymentCC.getAddressLine2());
			cardParams.put("address_city", paymentCC.getAddressCity());
			cardParams.put("address_zip", paymentCC.getAddressPostalCode());
			cardParams.put("address_state", paymentCC.getAddressState());
			//cardParams.put("address_country", "");
			
			chargeParams.put("card", cardParams);
			log.debug("Submitting the charge request: {}", chargeParams);
			Charge charge = Charge.create(chargeParams);
			
			log.info("Payment successfully charged. Payment Ref ID: {}", charge.getId());
			resp.setSuccess(true);
			resp.setSuccessId(charge.getId());
			
		} catch (CardException e) {
			log.error("Stripe rejected payment information for member '{}' with ID '{}'", paymentCC.getLastName(), paymentCC.getMemberId());
			resp.setSuccess(false);
			resp.setMessage(e.getMessage());
			resp.setCode(e.getCode());
			resp.setParam(e.getParam());
		} catch (InvalidRequestException e) {
			log.error("Error occurred submitting the request to Stripe: Invalid Request", e);
			resp.setSuccess(false);
			resp.setMessage(e.getMessage());
			resp.setParam(e.getParam());
		} catch (AuthenticationException e) {
			log.error("Authentication Error occurred while sending payment information to Stripe.", e);
			resp.setSuccess(false);
			resp.setMessage("Internal server error, could not connect to payment processor servers. Please try again later");
		} catch (APIConnectionException e) {
			log.error("API Connection error occurred while sending payment informatio to Stripe", e);
			resp.setSuccess(false);
			resp.setMessage("Internal server error, could not connect to payment processor servers. Please try again later");
		} catch (StripeException e) {
			log.error("Generic Stripe Exception occurred while sending payment informatio to Stripe", e);
			resp.setSuccess(false);
			resp.setMessage("Internal server error, could not connect to payment processor servers. Please try again later");
		} catch (Exception e) {
			log.error("Generic Exception occurred while sending payment informatio to Stripe", e);
			resp.setSuccess(false);
			resp.setMessage("Internal server error occurred. Please try again later");
		}
		
		log.debug("Returning response to controller: {}", resp);
		return resp;
	}

	public String getSecretKey() {
		return secretKey;
	}

	@Value("${stripe.secret}")
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getPublishableKey() {
		return publishableKey;
	}

	@Value("${stripe.publishable}")
	public void setPublishableKey(String publishableKey) {
		this.publishableKey = publishableKey;
	}
}
