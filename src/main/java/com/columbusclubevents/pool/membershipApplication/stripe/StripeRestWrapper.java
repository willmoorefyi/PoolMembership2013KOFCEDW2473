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

/**
 * Service class to wrap requests and responses to and from Stripe's RESTful API.  This class utilizes the Stripe helper
 * classes pulled from Maven to generate a request and process a response into a Java bean class, {@link StripeRestWrapper}.
 * 
 * As input, utilizes the Stripe-specific {@link PaymentCreditCard} object to capture all necesasry input information.  This
 * Java bean class can easily be deserialized JSON data input directly from the client.
 * 
 * TODO Defensive coding / check if the Payment credit card is valid before continuing.
 * TODO Unit tests for this class are critical due to its usage as a payment processor
 * 
 * @author wmoore
 *
 */
@Service
public class StripeRestWrapper {
	Logger log = LoggerFactory.getLogger(StripeRestWrapper.class);

	private String secretKey;
	private String publishableKey;

	public PaymentCreditCardResponse postCCPayment(PaymentCreditCard paymentCC) {
		log.debug("Processing credit card payment:{}", paymentCC);

		PaymentCreditCardResponse resp;
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
			resp = new PaymentCreditCardResponse();
			resp.setSuccess(true);
			resp.setSuccessId(charge.getId());
			
		} catch (CardException e) {
			log.error("Stripe rejected payment information for member '{}' with ID '{}'", paymentCC.getLastName(), paymentCC.getMemberId());
			resp = createExceptionResponse(e.getMessage(), e.getCode(), e.getParam());
		} catch (InvalidRequestException e) {
			log.error("Error occurred submitting the request to Stripe: Invalid Request", e);
			resp = createExceptionResponse(e.getMessage(), e.getParam());
		} catch (AuthenticationException e) {
			log.error("Authentication Error occurred while sending payment information to Stripe.", e);
			resp = createExceptionResponse("Internal server error, could not connect to payment processor servers. Please try again later");
		} catch (APIConnectionException e) {
			log.error("API Connection error occurred while sending payment informatio to Stripe", e);
			resp = createExceptionResponse("Internal server error, could not connect to payment processor servers. Please try again later");
		} catch (StripeException e) {
			log.error("Generic Stripe Exception occurred while sending payment informatio to Stripe", e);
			resp = createExceptionResponse("Internal server error, could not connect to payment processor servers. Please try again later");
		} catch (Exception e) {
			log.error("Generic Exception occurred while sending payment informatio to Stripe", e);
			resp = createExceptionResponse("Internal server error occurred. Please try again later");
		}
		
		log.debug("Returning response to controller: {}", resp);
		return resp;
	}

	/**
	 * Return the application's secret key
	 * @return The secret key (to communicate with Stripe's servers)
	 */
	public String getSecretKey() {
		return secretKey;
	}

	/**
	 * Set the secret key (used to communicate with Stripe to identify your website, and decrypt the card token if it was encrypted with the publishable key)
	 * @param secretKey The secret key for the application
	 */
	@Value("${stripe.secret}")
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	/**
	 * Return the application's publishable key
	 * @return The publishable key (to encrypt credit card values into tokens using Stripe.js)
	 */
	public String getPublishableKey() {
		return publishableKey;
	}

	/**
	 * Set the publishable key (used in Stripe.js to generate a token for the credit card the user entered)
	 * @param publishableKey The publishable key for the application
	 */
	@Value("${stripe.publishable}")
	public void setPublishableKey(String publishableKey) {
		this.publishableKey = publishableKey;
	}
	
	/**
	 * Helper method to create an exception response back to the client, when Stripe payment fails 
	 * @param message The user-friendly message to display to the end-user
	 * @param param The paramater the card failure is assoicated with, if applicable.
	 * @return A response that can be serialized back to the user to indicate payment failed
	 */
	private PaymentCreditCardResponse createExceptionResponse(String message, String param) {
		return createExceptionResponse(message, null, param);
	}
	
	/**
	 * Helper method to create an exception response back to the client, when Stripe payment fails 
	 * @param message The user-friendly message to display to the end-user
	 * @return A response that can be serialized back to the user to indicate payment failed
	 */
	private PaymentCreditCardResponse createExceptionResponse(String message) {
		return createExceptionResponse(message, null, null);
	}
	
	/**
	 * Helper method to create an exception response back to the client, when Stripe payment fails 
	 * @param message The user-friendly message to display to the end-user
	 * @param code The response code, only used when a credit card is rejected for some reason (see Stripe's documentation)
	 * @param param The paramater the card failure is assoicated with, if applicable.
	 * @return A response that can be serialized back to the user to indicate payment failed
	 */
	private PaymentCreditCardResponse createExceptionResponse(String message, String code, String param) {
		PaymentCreditCardResponse response = new PaymentCreditCardResponse(); 
		response.setSuccess(false);
		response.setMessage(message);
		response.setCode(code);
		response.setParam(param);
		return response;
	}
}
