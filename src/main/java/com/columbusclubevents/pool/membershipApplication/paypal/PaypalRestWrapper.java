package com.columbusclubevents.pool.membershipApplication.paypal;

//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Properties;
//
//import javax.annotation.PostConstruct;
//
//import org.apache.commons.lang.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;
//
//import com.columbusclubevents.pool.membershipApplication.paypal.json.request.PaymentCreditCard;
//import com.paypal.api.payments.Address;
//import com.paypal.api.payments.Amount;
//import com.paypal.api.payments.CreditCard;
//import com.paypal.api.payments.FundingInstrument;
//import com.paypal.api.payments.Payer;
//import com.paypal.api.payments.Payment;
//import com.paypal.api.payments.Transaction;
//import com.paypal.core.rest.APIContext;
//import com.paypal.core.rest.OAuthTokenCredential;
//import com.paypal.core.rest.PayPalRESTException;

/**
 * Method to pass payments to Paypal.  Not used.
 * 
 * @deprecated
 * @author wmoore
 *
 */
@Service
public class PaypalRestWrapper {
//	Logger log = LoggerFactory.getLogger(PaypalRestWrapper.class);
//	
//	//environment
//	private String payPalAPIEnvironment;
//	
//	//user info
//	private String payPalAPIclientId;
//	private String payPalAPISecret;
//	
//	private String payPalAPIVersion;
//	
//	private Properties paypalAPIProps;
//	
//	@PostConstruct
//	public void loadProperties() throws IOException {
//		Resource resource = new ClassPathResource("paypal_sdk_config.properties");
//		paypalAPIProps = PropertiesLoaderUtils.loadProperties(resource);
//	}
//	
//	public boolean postCCPayment(PaymentCreditCard paymentCC) {
//		log.debug("Processing credit card payment");
//		
//		String accessToken = getAccessToken();
//		if(accessToken == null) {
//			return false;
//		}
//		
//		Address billingAddress = new Address();
//		billingAddress.setCountryCode("US");
//		billingAddress.setLine1(paymentCC.getAddressLine1());
//		billingAddress.setLine2(StringUtils.isEmpty(paymentCC.getAddressLine2()) ? null : paymentCC.getAddressLine2() );
//		billingAddress.setPostalCode(paymentCC.getAddressPostalCode());
//		billingAddress.setState(paymentCC.getAddressState());
//		billingAddress.setCity(paymentCC.getAddressCity());
//
//		CreditCard creditCard = new CreditCard();
//		creditCard.setBillingAddress(billingAddress);
//		creditCard.setType(paymentCC.getCcType());
//		creditCard.setFirstName(paymentCC.getFirstName());
//		creditCard.setLastName(paymentCC.getLastName());
//		creditCard.setNumber(paymentCC.getCcNumber());
//		creditCard.setExpireMonth(paymentCC.getCcExpireMonth());
//		creditCard.setExpireYear(paymentCC.getCcExpireYear());
//		creditCard.setCvv2(paymentCC.getCcCvv2());
//
//		Amount amount = new Amount();
//		amount.setCurrency("USD");
//		amount.setTotal(paymentCC.getAmount());
//
//		Transaction transaction = new Transaction();
//		transaction.setAmount(amount);
//		transaction.setDescription("The total amount for your membership to the Knights of Columbus, Arlington council 2013 pool for the summer swimming season.");
//
//		List<Transaction> transactions = new ArrayList<Transaction>();
//		transactions.add(transaction);
//
//		FundingInstrument fundingInstrument = new FundingInstrument();
//		fundingInstrument.setCreditCard(creditCard);
//
//		List<FundingInstrument> fundingInstrumentList = new ArrayList<FundingInstrument>();
//		fundingInstrumentList.add(fundingInstrument);
//
//		Payer payer = new Payer();
//		payer.setFundingInstruments(fundingInstrumentList);
//		payer.setPaymentMethod("credit_card");
//
//		Payment payment = new Payment();
//		payment.setIntent("sale");
//		payment.setPayer(payer);
//		payment.setTransactions(transactions);
//		
//		try {
//			log.debug("Attempting to submit payment: {}", payment);
//			APIContext apiContext = new APIContext(accessToken);
//			Payment createdPayment = payment.create(apiContext);
//			log.info("Created payment with id '{}' and status '{}' ", createdPayment.getId(), createdPayment.getState());
//			String state = createdPayment.getState();
//			
//			if(state.equalsIgnoreCase("approved")) {
//				log.debug("Payment successful");
//				return true;
//			}
//			else {
//				return false;
//			}
//		} catch (PayPalRESTException e) {
//			log.error("Error occurred while attempting to create a paypal payment", e);
//			return false;
//		}
//	}
//	
//	/**
//	 * Retrieve the Access Token using the credentials read from the properties file
//	 * @return The Access Token as a string. <em>null</em> for invalid access token values
//	 */
//	private String getAccessToken() {
//		Payment.initConfig(paypalAPIProps);
//		OAuthTokenCredential tokenCredential = new OAuthTokenCredential(payPalAPIclientId, payPalAPISecret);
//		String accessToken = null;
//		try {
//			accessToken = tokenCredential.getAccessToken();
//		} catch (PayPalRESTException e) {
//			log.error("Exception occurred while trying to authenticate", e);
//		}
//		log.debug("received access token '{}'", accessToken);
//		return accessToken;
//	}
//
//	public String getPayPalAPIEnvironment() {
//		return payPalAPIEnvironment;
//	}
//
//	@Value("${paypal.rest.Endpoint}")
//	public void setPayPalAPIEnvironment(String payPalAPIEnvironment) {
//		this.payPalAPIEnvironment = payPalAPIEnvironment;
//	}
//
//	public String getPayPalAPIclientId() {
//		return payPalAPIclientId;
//	}
//
//	@Value("${paypal.rest.clientId}")
//	public void setPayPalAPIclientId(String payPalAPIclientId) {
//		this.payPalAPIclientId = payPalAPIclientId;
//	}
//
//	public String getPayPalAPISecret() {
//		return payPalAPISecret;
//	}
//
//	@Value("${paypal.rest.secret}")
//	public void setPayPalAPISecret(String payPalAPISecret) {
//		this.payPalAPISecret = payPalAPISecret;
//	}
//
//	public String getPayPalAPIVersion() {
//		return payPalAPIVersion;
//	}
//
//	@Value("${paypal.rest.version}")
//	public void setPayPalAPIVersion(String payPalAPIVersion) {
//		this.payPalAPIVersion = payPalAPIVersion;
//	}
	
}
