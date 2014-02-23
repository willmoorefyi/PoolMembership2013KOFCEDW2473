package com.columbusclubevents.pool.membershipApplication.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.validation.Valid;

import com.columbusclubevents.pool.membershipApplication.model.MemberAdditionalPayment;
import com.columbusclubevents.pool.membershipApplication.model.MemberNewPaymentRequest;
import com.columbusclubevents.pool.membershipApplication.stripe.AdditionalPaymentCreditCard;
import com.google.common.base.Splitter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.columbusclubevents.pool.membershipApplication.csv.CsvView;
import com.columbusclubevents.pool.membershipApplication.email.EmailSendException;
import com.columbusclubevents.pool.membershipApplication.email.MailSender;
import com.columbusclubevents.pool.membershipApplication.model.Dependent;
import com.columbusclubevents.pool.membershipApplication.model.Member;
import com.columbusclubevents.pool.membershipApplication.model.MemberRequest;
import com.columbusclubevents.pool.membershipApplication.model.MemberStatus;
import com.columbusclubevents.pool.membershipApplication.model.MemberUpdateRequest;
import com.columbusclubevents.pool.membershipApplication.model.MembershipCategory;
import com.columbusclubevents.pool.membershipApplication.model.MembershipOption;
import com.columbusclubevents.pool.membershipApplication.model.MembershipOptionsList;

import com.columbusclubevents.pool.membershipApplication.repository.MemberAdditionalPaymentRepository;
import com.columbusclubevents.pool.membershipApplication.repository.MemberRepository;
import com.columbusclubevents.pool.membershipApplication.repository.MembershipCategoryRepository;
import com.columbusclubevents.pool.membershipApplication.repository.MembershipOptionRepsository;
import com.columbusclubevents.pool.membershipApplication.stripe.PaymentCreditCard;
import com.columbusclubevents.pool.membershipApplication.stripe.PaymentCreditCardResponse;
import com.columbusclubevents.pool.membershipApplication.stripe.StripeRestWrapper;
import com.columbusclubevents.pool.membershipApplication.validation.ErrorMessage;
import com.columbusclubevents.pool.membershipApplication.validation.ValidationResponse;

/**
 * The primary application controller for the pool membership website.  All requests are handled through here.
 * This controller is implemented as a Spring MVC controller class using annotations. Spring-JPA is used to
 * autowire JAP repositories into the controller for interacting with the data model, removing the need for cumbersome
 * DAO classes.  
 * 
 * Unlike a traditional Spring MVC application, this class mostly processes JSON data using Jackson to deserialize JSON
 * requests (via the {@link RequestParam} and {@link RequestBody} annotations on method parameters) and then to 
 * serialize Java objects back to JSON using the {@link ResponseBody} annotation on method return parameters.
 * 
 *  For more information refer to <a href="http://blog.springsource.org/2010/01/25/ajax-simplifications-in-spring-3-0/">this excellent SpringSource blog article.</a>
 * 
 * @author wmoore
 *
 */
@Controller
public class ApplicationController {
	Logger log = LoggerFactory.getLogger(ApplicationController.class);
	
	/**
	 * Constants to use on the membership form to indicate if validation succeeded or not
	 */
	private static final String RESPONSE_SUCCESS = "SUCCESS";
	private static final String RESPONSE_FAILURE = "FAIL";


	private static final Charset ENCODING = Charset.forName("UTF-8");

	/**
	 * The Spring-JPA repository for accessing the underlying datastore {@link MembershipCategory} values.  
	 */
	@Autowired
	private MembershipCategoryRepository memberCategoryRepo;
	
	/**
	 * The Spring-JPA repository for accessing the underlying datastore {@link MembershipOption} values.  
	 */
	@Autowired
	private MembershipOptionRepsository memberOptionRepo;
	
	/**
	 * The Spring-JPA repository for accessing the underlying datastore {@link Member} values.  
	 */
	@Autowired
	private MemberRepository memberRepo;

	/**
	 * The Spring-JPA repository for accessing the underlying datastore {@link MemberAdditionalPayment} values.
	 */
	@Autowired
	private MemberAdditionalPaymentRepository memberAdditionalPaymentRepo;



	/**
	 * The Wrapper for the REST API integration with the payment processor, Stripe..
	 * This replaces the earlier PaypalRestWrapper, which was inconsistent in its performance.
	 */
	@Resource
	private StripeRestWrapper stripeRestWrapper;

	/**
	 * The helper class to send confirmation emails
	 */
	@Resource(name="mailSender")
	private MailSender mailSender;
	
	/**
	 * Method to invoke after the bean construction is complete.
	 * Was initially used to reconfigure slf4j / logback, but is no longer necessary due to the usage of
	 * {@link com.columbusclubevents.GAELogAppender}.
	 */
	@PostConstruct
	public void init() {
	    // assume SLF4J is bound to logback in the current environment
	    log.info("Entering application.");
	}
	
	/**
	 * Return a reference to the boostrap jspx page.
	 * @return The base string to locate the underling JSPX file to use for the
	 * {@link org.springframework.web.servlet.ViewResolver}
	 */
	@RequestMapping(value="/applicationBootstrap.htm",method=RequestMethod.GET)
	public String bootstrapForm(){
		log.debug("Received GET request on applicationBootstrap");
		return "application-bootstrap";
	}
	
	/**
	 * Return a reference to the membership form.
	 * @param model The object model. The {@link MembershipCategory} values are stored in here, and are used to render the appropriate JSPX page to the end-user.
	 * @return The base string to locate the underling JSPX file to use for the {@link org.springframework.web.servlet.ViewResolver}
	 */
	@RequestMapping(value="/start-membership-form",method=RequestMethod.GET)
	public String startForm(Model model) {
		log.debug("Received GET request on start-membership-form");
		MembershipOptionsList options = getCurrentOptions();
		model.addAttribute("membershipOptionsList", options);
		return "membership-form";
	}
	
	/**
	 * Handle the end-user submitting the form.  The {@link MemberRequest} is validated using JSR-303 Bean validation, and the results are stored
	 * in the {@link BindingResult}.  Additional business validation is performed here, and a {@link ValidationResponse} is constructed to send back
	 * to the end-user.  If validation succeeded, the users' data is also persisted in the datastore as a new {@link Member} object using the 
	 * {@link com.columbusclubevents.pool.membershipApplication.repository.MemberRepository}. The Javascript on the JSPX processes the response and prompts the
	 * user with the next step, or forwards the user to the next page
	 * 
	 * TODO Implement business validation in a custom validator, and attach to the Bean as JSR-303 validation.
	 * 
	 * @param memberRequest The values the user entered on the membership form, encapsualted as a JSON object.
	 * @param result The result of binding the input JSON values to the {@link MemberRequest} object.
	 * @return The results of validating the user's input data
	 */
	@RequestMapping(value="/submit-membership-form.json", method=RequestMethod.POST,consumes="application/json", produces="application/json")
	public @ResponseBody ValidationResponse submitMemberForm(@RequestBody @Valid MemberRequest memberRequest, BindingResult result) {
		log.debug("Received POST request on submit-membership-form.json with member request {}", memberRequest);
		if(result.hasErrors()){
			log.debug("Validation errors on input member form");
			return createErrorResponse(processErrors(result));
		}
		else {
			Member member = memberRequest.getMember();
			List<ErrorMessage> optionErrors = internalValidateMemberOptions(memberRequest.getMembershipOption(), member);
			if(optionErrors.size() > 0) {
				return createErrorResponse(optionErrors);
			}
			List<ErrorMessage> dependentErrors = internalValidateMemberDependents(member);
			if(dependentErrors.size() > 0) {
				return createErrorResponse(dependentErrors);
			}
			log.debug("Validation of member form succeeded!");
			MembershipOption opt = fetchOptionFromRequestOpt(memberRequest.getMembershipOption());
			String memberIdent = persistMember(member, opt);
			ValidationResponse res = createSuccessResponse();
			res.setSuccessIdentifier(memberIdent);
			res.setLastName(member.getLastName());
			return res;
		}
	}
	
	/**
	 * Show the user the success page.  Uses the input parameters containing the users' member ID and last name to retrieve the user data
	 * from the underlying datastore.  If there is a match, the user is shown the application-complete page with their data.  If there isn't
	 * a match, the user is forwarded to the no-match page.  
	 * 
	 * @param memberId The user's application ID
	 * @param lastName The user's last name
	 * @return The base string to locate the underling JSPX file to use for the ViewResolver, either the success or no-match page.
	 */
	@RequestMapping(value="/application-complete.htm")
	public String showSuccessPage(Model model, @RequestParam("id") String memberId, @RequestParam("lastName") String lastName) {
		log.debug("Application complete detected wtih id '{}' and last name '{}'", memberId, lastName);
		
		Member member = retrieveMember(memberId, lastName);
		log.debug("Executed search and member returned: {}", member);
		
		if(member != null) {
			model.addAttribute("member", member);
			return "application-complete";
		}
		else {
			return "member-no-match";
		}
	}

	/**
	 * Start the user's payment.  Retrieve the user's data (to pre-fill form fields using the {@link Model} to fill in values on the JSPX) and
	 * forward the user to the payment page.
	 * 
	 * @param model The object Model.  Put the Member object in to pre-fill the appropriate fields on the JSPX (such as the member's address).
	 * @param memberId The member ID.  Used to locate the correct member.
	 * @param lastName The member last name.  Used to locate the correct member.
	 * @return The base string to locate the underling JSPX file to use for the ViewResolver, either the payment page or no-match page.
	 */
	@RequestMapping(value="/start-payment.htm")
	public String startPayment(Model model, @RequestParam("id") String memberId, @RequestParam("lastName") String lastName) {
		log.debug("Received request to start payment for member ID '{}' with last name '{}'", memberId, lastName);

		Member member = retrieveMember(memberId, lastName);
		if(member != null) {
			//prep the new models
			PaymentCreditCard paymentCC = PaymentCreditCard.fromMember(member);
			model.addAttribute("paymentCC", paymentCC);
			return "create-payment";
		}
		else {
			return "member-no-match";
		}
	}
	
	/**
	 * Process the credit card payment for the end-user. Will return a success or failure response to the end-user, using the same {@link ValidationResponse}
	 * object the membership form uses.  Processing is more or less the same.  
	 * 
	 * @param paymentCC The payment information provided by the user.  This contains all of their input payment metadata.
	 * @param result The result of binding the input JSON values to the {@link PaymentCreditCard} object.
	 * @return The 
	 */
	@RequestMapping(value="/submit-payment-cc.json", method=RequestMethod.POST, consumes="application/json", produces="application/json")
	public @ResponseBody ValidationResponse payCC(@RequestBody @Valid PaymentCreditCard paymentCC, BindingResult result) {
		log.debug("Received request to pay with Credit Card: {}", paymentCC);
		
		//handle any binding errors
		if(result.hasErrors()){
			log.debug("Validation errors on input payment form");
			return createErrorResponse(processErrors(result));
		}
		
		String memberId = paymentCC.getMemberId();
		String lastName = paymentCC.getLastName();
		Member member = retrieveMember(memberId, lastName);

		//make sure the member is valid.
		if(member == null) {
			log.error("Invalid model passed into payCC: '{}'", paymentCC);
			return createSingleErrorResponse("memberId", "No matching member can be found for the combination of member ID and last name provided.");
		}

		log.debug("Setting additional properties on input CC request");
		paymentCC.setAmount(member.getMemberCost().toString());
		
		try {
			PaymentCreditCardResponse response = stripeRestWrapper.postCCPayment(paymentCC);
			if(response.getSuccess()) {
				member.setMemberPaid(true);
				member.setPaymentId(response.getSuccessId());
				member.setMemberStatus(MemberStatus.PAID);
				log.debug("Persisting member info {}", member);
				memberRepo.save(member);
				ValidationResponse res = createSuccessResponse();
				res.setUrl("/payment-complete.htm");
				res.setSuccessIdentifier(member.getId().toString());
				res.setLastName(member.getLastName());
				return res;
			} 
			else {
				String field = response.getParam() == null ? "id" : response.getParam();
				String message = response.getMessage();
				return createSingleErrorResponse(field, message);
			}
		}
		catch (Exception e) {
			log.error("Exception occurred parsing credit card request", e);
			return createSingleErrorResponse("id", "Error occurred processing your credit card information. Please try again later.");
		}
	}
	
	/**
	 * Show the payment success page, after payments are successfully processed.  The URL to this page is included in the {@link ValidationResponse} the
	 * create payment page responds to the client on a successful payment.
	 * 
	 * @param model The object Model.  Put the Member object in to pre-fill the appropriate fields on the JSPX (such as the member's address).
	 * @param memberId The member ID.  Used to locate the correct member.
	 * @param lastName The member last name.  Used to locate the correct member.
	 * @return The base string to locate the underling JSPX file to use for the ViewResolver, either the success page or no-match page.
	 */
	@RequestMapping(value="/payment-complete.htm")
	public String showPaymentSuccessPage(Model model, @RequestParam("id") String memberId, @RequestParam("lastName") String lastName) {

		Member member = retrieveMember(memberId, lastName);
		log.debug("Executed search and member returned: {}", member);
		
		if(member != null) {
			model.addAttribute("member", member);
			return "payment-complete";
		}
		else {
			return "member-no-match";
		}
	}

	@RequestMapping(value="/additional-payment-complete.htm")
	public String sendToAdditionalPaymentComplete(Model model, @RequestParam("id") String memberId, @RequestParam("lastName") String lastName) {
		Member member = retrieveMember(memberId, lastName);
		log.debug("Executed search and member returned: {}", member);

		if(member != null) {
			model.addAttribute("member", member);
			return "additional-payment-complete";
		}
		else {
			log.warn("Received payment complete, but user ID couldn't be found.  This should never happen!");
			return "member-no-match";
		}
	}

	/**
	 * Return a reference to the get member jspx page.  For looking up a successfully committed application.
	 * @return The base string to locate the underling JSPX file to use for the ViewResolver
	 */
	@RequestMapping(value="/retrieve-member.htm")
	public String retrieveMemberPage() {
		log.debug("Received GET request on retrieve-member");
		return "retrieve-member";
	}

	/**
	 * Start the user's additional payment. Retrieve the member info from the base64-encoded string
	 *
	 * @param model The object Model.  Put the Member object in to pre-fill the appropriate fields on the JSPX (such as the member's address).
	 * @param encodedUserInfo The Base-64 encoded user info
	 * @return The base string to locate the underling JSPX file to use for the ViewResolver, either the payment page or no-match page.
	 */
	@RequestMapping(value="/start-additional-payment-encoded.htm", method=RequestMethod.GET)
	public String startAdditionalPayment(Model model, @RequestParam("encodedUserInfo") String encodedUserInfo) {
		log.debug("Received request to start payment for encoded member info '{}'", encodedUserInfo);
		String viewName = null;

		String decodedStr = new String(Base64.decodeBase64(encodedUserInfo), ENCODING);
		log.debug("Decoded string read as : {}", decodedStr);
		if(StringUtils.isEmpty(decodedStr)) {
			viewName = "member-no-match";
		}
		else {
			final Map<String, String> splitMap = Splitter.on(',').omitEmptyStrings().trimResults().withKeyValueSeparator(":").split(decodedStr);
			String memberId = splitMap.get("memberId");
			String lastName = splitMap.get("lastName");
			String paymentId = splitMap.get("paymentId");
			viewName = startAdditionalPayment(model, memberId, lastName, paymentId);
		}
		log.debug("Sending user to view {}", viewName);
		return viewName;
	}

	/**
	 * Start the user's additional payment.  Retrieve the user's data (to pre-fill form fields using the {@link Model} to fill in values on the JSPX) and
	 * forward the user to the payment page.
	 * This method is a copy of startPayment.  It should be refactored, but at this point it's not worth the effort.
	 *
	 * @param model The object Model.  Put the Member object in to pre-fill the appropriate fields on the JSPX (such as the member's address).
	 * @param memberId The member ID.  Used to locate the correct member.
	 * @param lastName The member last name.  Used to locate the correct member.
	 * @param paymentId The ID of the user's additional payment
	 * @return The base string to locate the underling JSPX file to use for the ViewResolver, either the payment page or no-match page.
	 */
	@RequestMapping(value="/start-additional-payment.htm", method=RequestMethod.GET)
	public String startAdditionalPayment(Model model, @RequestParam("id") String memberId, @RequestParam("lastName") String lastName, @RequestParam("paymentId") String paymentId) {
		log.debug("Received request to start payment ID '{}' for member ID '{}' with last name '{}'", paymentId, memberId, lastName);

		Member member = retrieveMember(memberId, lastName);
		MemberAdditionalPayment additionalPayment = memberAdditionalPaymentRepo.findOne(Long.parseLong(paymentId));
		if(member != null && additionalPayment != null && member.getId() == additionalPayment.getMemberId()) {
			log.debug("Payment info: {}", additionalPayment);
			if(additionalPayment.getMemberPaid() != null && additionalPayment.getMemberPaid()) {
				log.debug("Received request for additional payment, but member has already paid with payment ID {}", additionalPayment);
				model.addAttribute("member", member);
				return "additional-payment-already-paid";
			}
			else {
				//prep the new models
				AdditionalPaymentCreditCard additionalPaymentCC = AdditionalPaymentCreditCard.fromPayment(additionalPayment, member);
				additionalPaymentCC.setPaymentId(paymentId);
				model.addAttribute("additionalPaymentCC", additionalPaymentCC);
				log.debug("Added payment CC to model and forwarding to view: {}", additionalPaymentCC);
				return "create-additional-payment";
			}
		}
		else {
			return "member-no-match";
		}
	}

	@Transactional
	@RequestMapping(value="/submit-additional-payment-cc.json", method=RequestMethod.POST, consumes="application/json", produces="application/json")
	public @ResponseBody ValidationResponse payAdditionalCC(@RequestBody @Valid AdditionalPaymentCreditCard additionalPaymentCC, BindingResult result) {
		log.debug("Received payment request for additional payment: {}", additionalPaymentCC);
		//handle any binding errors
		if(result.hasErrors()){
			log.debug("Validation errors on input payment form");
			return createErrorResponse(processErrors(result));
		}

		String memberId = additionalPaymentCC.getPaymentCreditCard().getMemberId();
		String lastName = additionalPaymentCC.getPaymentCreditCard().getLastName();
		Member member = retrieveMember(memberId, lastName);

		//make sure the member is valid.
		if(member == null) {
			log.error("Invalid model passed into payCC: '{}'", additionalPaymentCC);
			return createSingleErrorResponse("memberId", "No matching member can be found for the combination of member ID and last name provided.");
		}

		PaymentCreditCard paymentCC = additionalPaymentCC.getPaymentCreditCard();

		log.debug("Setting additional properties on input CC request");
		MemberAdditionalPayment paymentDetails = memberAdditionalPaymentRepo.findOne(Long.parseLong(additionalPaymentCC.getPaymentId()));
		log.debug("Setting payment amount to value: {}", paymentDetails.getMemberPayment());
		paymentCC.setAmount(paymentDetails.getMemberPayment().toString());

		try {
			PaymentCreditCardResponse response = stripeRestWrapper.postCCPayment(paymentCC);
			if(response.getSuccess()) {
				member.setMemberPaid(true);
				member.setMemberStatus(MemberStatus.PAID);
				log.debug("Persisting member info {}", member);
				memberRepo.save(member);
				log.debug("Member successfully persisted");
				fetchAndUpdateAdditionalPayment(Long.parseLong(additionalPaymentCC.getPaymentId()), response.getSuccessId());
				log.debug("Persisting Payment info {}", paymentDetails);
				ValidationResponse res = createSuccessResponse();
				res.setUrl("/additional-payment-complete.htm");
				res.setSuccessIdentifier(member.getId().toString());
				res.setLastName(member.getLastName());
				return res;
			}
			else {
				String field = response.getParam() == null ? "id" : response.getParam();
				String message = response.getMessage();
				return createSingleErrorResponse(field, message);
			}
		}
		catch (Exception e) {
			log.error("Exception occurred parsing credit card request", e);
			return createSingleErrorResponse("id", "Error occurred processing your credit card information. Please try again later.");
		}
	}
	
	/**
	 * Return a reference to the rates form jspx page.  For changing pool membership rates.
	 * @return The base string to locate the underling JSPX file to use for the ViewResolver
	 */
	@RequestMapping(value="/manage/manage-rates.htm",method= RequestMethod.GET)
	public String getRatesForm() {
		log.debug("Received GET request on manage-rates");
		return "manage-rates";
	}
	
	@RequestMapping(value="/manage/manage-rates.json",method=RequestMethod.POST,consumes="application/json", produces="application/json")
	public @ResponseBody ValidationResponse updateRates(@RequestBody @Valid MembershipOptionsList memberOpts, BindingResult result) {
		log.debug("Received POST request on manage-rates.json");
		log.debug("Membership options returned: {}", memberOpts);
		ValidationResponse res = new ValidationResponse();
		if(result.hasErrors()){
			log.debug("Validation errors encountered");
			res.setStatus("FAIL");
			res.setErrorMessageList(processErrors(result));

		} else {
			log.debug("Validation succeeded");
			clearAndUpdateMembershipCategories(memberOpts.getMemberCategories());
			log.debug("Membership Categories persisted to backing DB");
			res.setStatus("SUCCESS");
		}
		return res;
	}
	
	/**
	 * Retrieve the current rates available to the end-user, via a serialized {@link MembershipOptionsList} Java bean.
	 * @return The serialized rates the user can currently select from.
	 */
	@RequestMapping(value="/manage/get-default-rate.json", method={RequestMethod.GET, RequestMethod.POST}) 
	public @ResponseBody MembershipOptionsList getDefaultRates() {
		log.debug("Received request on get-default-rate");
		MembershipOptionsList options = getCurrentOptions();
		if(options.getMemberCategories().isEmpty()) {
			//construct an empty category if we don't have any options
			MembershipCategory cat = new MembershipCategory();
			List<MembershipOption> opts = new ArrayList<MembershipOption>();
			opts.add(new MembershipOption());
			cat.setMemberOptions(opts);
			options.add(cat);
		}
		return options;
	}
	
	/**
	 * Retrieve the current list of membership options from the backing database
	 * @return The current list of membership options from the backing database
	 */
	@RequestMapping(value="/get-current-rates.json", method={RequestMethod.GET, RequestMethod.POST}) 
	public @ResponseBody MembershipOptionsList getCurrentOptions() {
		MembershipOptionsList options = new MembershipOptionsList();
		for (MembershipCategory cat : memberCategoryRepo.findAll()) {
			log.debug("Found Membership category: " + cat.toString());
			options.add(cat);
		}
		return options;
	}
	
	/**
	 * Fetch the Manage Applications page
	 * @return The base string to locate the underling JSPX file to use for the ViewResolver
	 */
	@RequestMapping(value="/manage/manage-applications.htm",method= RequestMethod.GET)
	public String manageApplications() {
		log.debug("Received GET request on manage-applications.htm");
		return "manage-applications";
	}
	
	/**
	 * Retrieve the list of all possible member statuses. Used to generate the sort options for the table
	 * @return A list of member status objects, to serialize as JSON and return to the caller
	 */
	@RequestMapping(value="/manage/get-default-member-status-values.json",method=RequestMethod.GET, produces="application/json")
	public @ResponseBody List<MemberStatus> getMemberStatusValues() {
		log.debug("Received GET request on get-default-member-status-values.json");
		return Arrays.asList(MemberStatus.values());	
	}
	
	/**
	 * Retrieve the list of all members that match the filter criteria passed in.
	 * @param request The filter request from the client (as a string)
	 * @return The list of Member objects that match the filter status specified
	 */
	@RequestMapping(value="/manage/manage-applications.json",method=RequestMethod.POST, consumes="application/json", produces="application/json")
	public @ResponseBody List<Member> retrieveFilteredApplications(@RequestBody MemberStatus request) {
		log.debug("Received POST request on manage-applications.json with filter input '{}'", request);
		return fetchMembersByStatus(request);
	}
	
	@RequestMapping(value="/manage/download-applications.htm",method=RequestMethod.GET)
	public ModelAndView exportMembers(Model model) {
		log.debug("Received POST request on download-applications.htm to export CSV");
		List<Member> members = fetchAllMembers();
		model.addAttribute("members", members);
		return new ModelAndView(new CsvView(), model.asMap());
	}
	
	@RequestMapping(value="/manage/update-applications.json",method=RequestMethod.POST, consumes="application/json", produces="application/json")
	public @ResponseBody ValidationResponse updateMemberApplications(@RequestBody MemberUpdateRequest[] request, BindingResult result) {
		log.debug("Received POST request on update-applications.json with filter input '{}'", Arrays.asList(request));
		
		if(Arrays.asList(request).isEmpty()) {
			return createSingleErrorResponse("", "You did not specify any members to update");
		}

		//handle any binding errors
		if(result.hasErrors()){
			log.debug("Validation errors on input payment form");
			return createErrorResponse(processErrors(result));
		}
		
		Long invalidMemberId = null;
		//List<Member> members = new ArrayList<Member>();
		for(MemberUpdateRequest memberUpdateRequest : request) {
			Member member = memberRepo.findOne(memberUpdateRequest.getId());
			if(member == null) {
				invalidMemberId = memberUpdateRequest.getId();
				break;
			}
			member.setMemberStatus(memberUpdateRequest.getMemberStatus());
			//members.add(member);
			memberRepo.save(member);
			//initiate a task to send the approval email
			if(memberUpdateRequest.getMemberStatus().equals(MemberStatus.APPROVED)) {
				mailSender.googleEnqueueMessage(memberUpdateRequest.getId(), "/sendAcceptance.htm");
			}
		}
		if(invalidMemberId != null) {
			return createSingleErrorResponse(invalidMemberId.toString(), "Selected member ID '" + invalidMemberId + "' is invalid.  Please clear your selection, refresh the page, and try again");
		}
		else {
			//can't save multiple entities in one transaction, as they may span multiple entity groups due to the way GAE handles JPA entities
			//memberRepo.save(members);
			return createSuccessResponse();
		}
	}

	@RequestMapping(value="/manage/create-payment-requests.json",method=RequestMethod.POST, consumes="application/json", produces="application/json")
	public @ResponseBody ValidationResponse createNewPaymentRequests(@RequestBody MemberNewPaymentRequest[] request, BindingResult result) {
		log.debug("Received POST request on create-payment-requests.htm with filter input '{}'", Arrays.asList(request));

		if(Arrays.asList(request).isEmpty()) {
			return createSingleErrorResponse("", "You did not specify any members to update");
		}

		if(result.hasErrors()){
			log.debug("Validation errors on input payment form");
			return createErrorResponse(processErrors(result));
		}

		try {
			for(MemberNewPaymentRequest newPaymentRequest : request) {
				log.debug("Processing new payment request {}", newPaymentRequest);
				MemberAdditionalPayment payment = createMemberIncrementalCost(newPaymentRequest.getId(), newPaymentRequest.getCost());
				mailSender.googleEnqueueMessage(payment.getId(), "sendPaymentEmail.htm");
			}
		}
		catch (Exception e) {
			log.error("Error occurred while attempting to process new payment request", e);
			return createSingleErrorResponse("", e.getMessage());
		}
		return createSuccessResponse();
	}

	@RequestMapping(value="/manage/{memberId}/member.json", method=RequestMethod.DELETE, produces="application/json")
	public @ResponseBody ValidationResponse deleteMember(@PathVariable Long memberId) {
		log.debug("Deleting member Id {}", memberId);

		Member member = memberRepo.findOne(memberId);
		ValidationResponse response = null;
		if(member == null) {
			response = createSingleErrorResponse(memberId.toString(), "Selected member ID '" + memberId + "' is invalid.  Please clear your selection, refresh the page, and try again");
		}
		else if (member.getMemberPaid()) {
			response = createSingleErrorResponse(memberId.toString(), "Selected member ID '" + memberId + "' has a payment ID.  You cannot delete members who have valid payment IDs.");
		}
		else {
			memberRepo.delete(member);
			response = createSuccessResponse();
		}

		log.debug("Returning delete member response '{}'", response);
		return response;
	}
	
	@RequestMapping(value="/sendemail/{memberId}/sendAcceptance.htm",method=RequestMethod.POST)
	public @ResponseBody String sendAcceptanceEmail(@PathVariable Long memberId) throws EmailSendException {
		log.debug("Received request to send acceptance email to member {}", memberId);
		Member member = memberRepo.findOne(memberId);
		if(member == null) {
			log.warn("Attempted to send a member confirmation email to member ID '{}', but no valid member found", memberId);
		}
		try {
	      mailSender.sendAcceptanceMessage(member.getEmail(), member.getMemberType().equals("Knights of Columbus - EDW 2473 Council Member"));
	      member.setMemberStatus(MemberStatus.COMPLETE);
	      memberRepo.save(member);
	      return "OK";
      } catch (MessagingException e) {
      	log.error("Unable to send acceptance confirmation for member '{}' due to messaging exception", memberId, e);
      	throw new EmailSendException(e);
      	
      } catch (IOException e) {
      	log.error("Unable to send acceptance confirmation for member '{}' due to IO exception", memberId, e);
      	throw new EmailSendException(e);
      }
	}

	@RequestMapping(value="/sendemail/{paymentId}/sendPaymentEmail.htm",method=RequestMethod.POST)
	public @ResponseBody String sendPaymentRequestEmail(@PathVariable Long paymentId) throws EmailSendException {
		log.debug("Received request to send payment email for ID {}", paymentId);

		try {
			MemberAdditionalPayment additionalPayment = memberAdditionalPaymentRepo.findOne(paymentId);
			if(additionalPayment == null) {
				log.warn("Attempting to send payment email for ID '{}', but provided ID is invalid", paymentId);
			}
			else {
				Member member = memberRepo.findOne(additionalPayment.getMemberId());
				if(member == null) {
					log.warn("Attempted to send a member confirmation email to member ID '{}', but no valid member found", member);
				}
				else {
					StringBuilder params = new StringBuilder()
							.append("memberId").append(':').append(member.getId()).append(',')
							.append("lastName").append(':').append(member.getLastName()).append(',')
							.append("paymentId").append(':').append(paymentId);
					log.debug("Encoding Parameters: {}", params);
					String encodedParam = Base64.encodeBase64URLSafeString(params.toString().getBytes(ENCODING));
					String url = "/start-additional-payment-encoded.htm?encodedUserInfo=" + encodedParam;
					log.debug("Generated encoded URL: {}", url);
					mailSender.sendPaymentEmail(member.getEmail(), url);
				}
			}
			return "OK";
		} catch (MessagingException e) {
			log.error("Unable to send acceptance confirmation for payment '{}' due to messaging exception", paymentId, e);
			throw new EmailSendException(e);

		} catch (IOException e) {
			log.error("Unable to send acceptance confirmation for payment '{}' due to IO exception", paymentId, e);
			throw new EmailSendException(e);
		}
	}

	@ExceptionHandler(EmailSendException.class)
	@ResponseStatus(value=HttpStatus.I_AM_A_TEAPOT)
	private Map<String, String> handleAcceptanceEmailException(EmailSendException e) {
		log.error("Handling exception");
		return Collections.singletonMap("message", e.getMessage());
	}
	
	/**
	 * Process the error messages that occur from the binding of the passed-in object to the Java object via JSR-303 validation
	 * @param result The result of attemptipng to bind the passed-in string to a Java object
	 * @return The list of {@link ErrorMessage} objects
	 */
	private List<ErrorMessage> processErrors(BindingResult result) {
		List<FieldError> allErrors = result.getFieldErrors();
		List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>();
		for (FieldError objectError : allErrors) {
			ErrorMessage error = new ErrorMessage(objectError.getField(), objectError.getDefaultMessage());
			log.debug("Processed error: \"{}\" from object error \"{}\"", error, objectError);
			errorMessages.add(error);
		}
		return errorMessages;
	}
	
	/**
	 * Create a simple success response
	 * @return The ValidationResponse object, with the correct Success status set.
	 */
	private ValidationResponse createSuccessResponse() {
		ValidationResponse res = new ValidationResponse();
		res.setStatus(RESPONSE_SUCCESS);
		return res;
	}
	
	/**
	 * Generate an error validation response with a list of error messages
	 * @param errorMessages Any error messages from a binding
	 * @return The validation response object
	 */
	private ValidationResponse createErrorResponse(List<ErrorMessage> errorMessages) {
		ValidationResponse res = new ValidationResponse();
		res.setStatus(RESPONSE_FAILURE);
		res.setErrorMessageList(errorMessages);
		return res;
	}
	
	/**
	 * Create an error response with a single error message
	 * @param field The field to attach the error message to (for Spring forms)
	 * @param msg The error message to display)
	 * @return The validation response object
	 */
	private ValidationResponse createSingleErrorResponse(String field, String msg) {
		log.debug("Creating error message for field '{}' with message '{}'", field, msg);
		ErrorMessage error = new ErrorMessage(field, msg);
		List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>();
		errorMessages.add(error);
		return createErrorResponse(errorMessages);
	}
	
	/**
	 * Clears the current membership categories and reload with the new list.
	 * This simplifies the process of loading new membership options into the application
	 * @param memberCategories The categories to update the values to
	 */
	@Transactional
	private void clearAndUpdateMembershipCategories(List<MembershipCategory> memberCategories) {
		//TODO make this actually use the database to enforce uniqueness - GAE won't do this
		//TODO Update so that we are updating selected entites, rather than dropping and re-adding
		Set<String> optIds = new HashSet<String>();
		for(MembershipCategory memberCategory : memberCategories) {
			for(MembershipOption memberOpt : memberCategory.getMemberOptions()) {
				if(optIds.contains(memberOpt.getOptionKey())) {
					log.debug("Duplicate key found: {}", memberOpt.getOptionKey());
					throw new RuntimeException("Attempted to add duplicate membership option \"" + memberOpt.getOptionKey() + "\"");
				}
				optIds.add(memberOpt.getOptionKey());
			}
		}
		memberCategoryRepo.deleteAll();
		memberCategoryRepo.save(memberCategories);
		memberCategoryRepo.flush();
	}
	
	/**
	 * Persist the passed-in member into the datastore
	 * @param member The member object to persist
	 * @param opt The option the user selected for their membership.  Used to set additional data, such as the cost.
	 */
	@Transactional
	private String persistMember(final Member member, final MembershipOption opt) {
		log.debug("Member to persist: {}", member);
		
		MemberAdditionalProperties properties = fetchMemberCategoryInfo(opt);
		
		//set the default member status
		member.setMemberStatus(MemberStatus.NEW);
		member.setMemberPaid(false);
		
		//set the membership properties retrieved from the membership options
		member.setMemberCost(properties.getCost());
		
		log.debug("Setting membership category type '{}' with cost '{}' on member", properties.getTabDescription(), properties.getCost());
		member.setMemberType(properties.getTabDescription());
		
		//persist the member into the DB
		log.debug("Persisting member info {}", member);
		memberRepo.save(member);
		log.debug("Entity persisted, now with ID {}", member.getId());
		
		return member.getId().toString();
	}
	
	/**
	 * Fetches properties needed for perisisting a member.
	 * @param opt The membership option to fetch from
	 * @return The Additional properties needed to create a final Member object
	 */
	@Transactional
	private MemberAdditionalProperties fetchMemberCategoryInfo(final MembershipOption opt) {

		MemberAdditionalProperties properties = new MemberAdditionalProperties();

		//refetch the category to get all properties
		Long memberCategoryId;
		memberCategoryId = opt.getMemberCategoryParent().getId();
		log.debug("Fetching membership category with ID: {}", memberCategoryId);
		MembershipCategory cat = memberCategoryRepo.findOne(memberCategoryId);

		properties.setCost(opt.getCost());
		properties.setTabDescription(cat.getTabDescription());

		return properties;
	}
	
	@Transactional
	private List<Member> fetchAllMembers() {
		log.debug("Fetching all members");
		List<Member> membersAndDependents = new ArrayList<Member>();
		for(MemberStatus status : MemberStatus.values()) {
			List<Member> members = fetchMembersByStatus(status);
			log.debug("Returned members count '{}' for status '{}'", members.size(), status);
			for(Member member : members) {
				membersAndDependents.add(member);
			}
		}
		return membersAndDependents;
	}
	
	@Transactional
	private List<Member> fetchMembersByStatus(MemberStatus status) {
		log.debug("Retrieving members with status {}", status);
		List<Member> members = memberRepo.findByMemberStatus(status);
		log.trace("Returned members: '{}'", members);
		return members;
	}

	@Transactional
	private MembershipOption fetchOptionFromRequestOpt(MembershipOption option) {
		return memberOptionRepo.findByOptionKey(option.getOptionKey()).get(0);
	}
	
	/**
	 * Validate the option the member selected as compared to the membership form.
	 * @param opt The membership option selected
	 * @param member The member object
	 * @return Any error messages that occur from validating a member
	 */
	private List<ErrorMessage> internalValidateMemberOptions(MembershipOption opt, Member member) {
		List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>();
		
		if(opt.getOptionKey().equalsIgnoreCase("memberSingle") && member.getDependents().size() > 0) {
			log.warn("Individual selected member individual , but added family members.  This is an invalid choice");
			errorMessages.add(new ErrorMessage("relationType", "You cannot select an individual membership and include additional family members.  An individual memberhsip can only be used by one person."));
		}
		
		return errorMessages;
	}
	
	/**
	 * Validate the member object's dependents passed-in.  These cannot be custom validated as they are dependent on other field values
	 * @param member The member object to validate
	 * @return A list of Error Messages responses containing any errors that were identified
	 */
	private List<ErrorMessage> internalValidateMemberDependents(Member member) {
		List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>();
		
		boolean hasNanny = false;
		for(Dependent dependent : member.getDependents()) {
			if(dependent.getRelationType().equals("NANNY")) {
				if(hasNanny) {
					errorMessages.add(new ErrorMessage("relationType", "You cannot have multiple nanny / caretakers selected."));
				}
				hasNanny = true;
			}
			else if(dependent.getRelationType().equals("CHILD")) {
				if(StringUtils.isEmpty(dependent.getExtraData()) || !StringUtils.isNumeric(dependent.getExtraData())) {
					errorMessages.add(new ErrorMessage("relationType", "You specified a relation type of \"Child\", but did not provide a valid age. This field is required"));
				}
			}
			else if(dependent.getRelationType().equals("OTHER")) {
				if(StringUtils.isEmpty(dependent.getExtraData())) {
					errorMessages.add(new ErrorMessage("relationType", "You specified a relation type of \"Other\", but did not provide an explanation. This field is required"));
				}
			}
		}
		
		return errorMessages;
	}
	
	/**
	 * Fetch the member from the memberId
	 * @param memberId The Member ID the user passed in
	 * @param lastName the member last name
	 * @return The Member object
	 */
	private Member retrieveMember(String memberId, String lastName) {
		log.debug("Fetching member object from Id '{}' and last name '{}'", memberId, lastName);
		try {
			if(StringUtils.isBlank(memberId) || StringUtils.isBlank(lastName)) {
				log.warn("Passed-in memberId '{}' or lastName '{}' has an empty value after trimming, returning null", memberId, lastName);
				return null;
			}
			Member member = memberRepo.findOne(Long.parseLong(memberId));
			if(member != null && member.getLastName() != null && member.getLastName().equalsIgnoreCase(lastName)) {
				log.debug("Found matching member '{}'", member);
				return member;
			}
			else {
				log.warn("Member Id found for ID '{}', but with last name '{}' and not passed-in last name '{}'. Returning null", memberId, (member == null ? null : member.getLastName()), lastName);
				return null;
			}
		}
		catch(NumberFormatException e) {
			log.error("Passed-in member Id '{}' caused a NumberFormatException during memberId parsing. Presuming user entered invalid member Id and returning null", lastName, e);
			return null;
		}
	}

	@Transactional
	private MemberAdditionalPayment createMemberIncrementalCost(Long memberId, Integer newCost) {
		log.debug("Calculating incremental cost for member {} and new cost {}", memberId, newCost);

		Member member = memberRepo.findOne(memberId);
		if(member == null) {
			throw new RuntimeException("Member specified by ID '" + memberId + "' is invalid");
		}
		Integer memberCost = member.getMemberCost();
		Integer incrementalDifference = newCost - memberCost;

		if(incrementalDifference <= 0) {
			throw new RuntimeException(String.format("Incremental cost increase %1$s is a non-positive number, as new cost %2$s is less than or equal to original member paid amount %3$s",
					incrementalDifference, newCost, memberCost));
		}

		MemberAdditionalPayment additionalPayment = new MemberAdditionalPayment();
		additionalPayment.setMemberId(member.getId());
		additionalPayment.setOriginalMemberCost(memberCost);
		additionalPayment.setFinalMemberCost(newCost);
		additionalPayment.setMemberPayment(incrementalDifference);

		memberAdditionalPaymentRepo.save(additionalPayment);

		member.setMemberStatus(MemberStatus.BALANCEDUE);
		member.setMemberPaid(false);

		memberRepo.save(member);

		return additionalPayment;
	}

	@Transactional
	private void fetchAndUpdateAdditionalPayment(Long additionalPaymentId, String paymentId) {
		log.debug("Updating member payment with ID '{}' with payment ID '{}'", additionalPaymentId, paymentId);
		 MemberAdditionalPayment additionalPayment = memberAdditionalPaymentRepo.findOne(additionalPaymentId);

		log.debug("Fetched member payment '{}'", additionalPayment);
		additionalPayment.setPaymentId(paymentId);
		additionalPayment.setMemberPaid(true);

		log.debug("Persisting updated member Payment");
		memberAdditionalPaymentRepo.saveAndFlush(additionalPayment);
	}
	
	/**
	 * Additional properties bean to set on the Member object before persisting. Needs to be retrieved in a single
	 * transaction, so encapsulating this in a bean to return from a method facilitates that.
	 * @author wmoore
	 *
	 */
	private class MemberAdditionalProperties {
		Integer cost;
		String tabDescription;
		public Integer getCost() {
			return cost;
		}
		public void setCost(Integer cost) {
			this.cost = cost;
		}
		public String getTabDescription() {
			return tabDescription;
		}
		public void setTabDescription(String tabDescription) {
			this.tabDescription = tabDescription;
		}
	}
}
