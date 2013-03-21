package com.columbusclubevents.pool.membershipApplication.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.columbusclubevents.pool.membershipApplication.model.Dependent;
import com.columbusclubevents.pool.membershipApplication.model.Member;
import com.columbusclubevents.pool.membershipApplication.model.MemberRequest;
import com.columbusclubevents.pool.membershipApplication.model.MemberStatus;
import com.columbusclubevents.pool.membershipApplication.model.MembershipCategory;
import com.columbusclubevents.pool.membershipApplication.model.MembershipOption;
import com.columbusclubevents.pool.membershipApplication.model.MembershipOptionsList;
import com.columbusclubevents.pool.membershipApplication.paypal.PaypalRestWrapper;
import com.columbusclubevents.pool.membershipApplication.paypal.json.request.PaymentCreditCard;
import com.columbusclubevents.pool.membershipApplication.paypal.json.request.PaymentPaypal;
import com.columbusclubevents.pool.membershipApplication.repository.MemberRepository;
import com.columbusclubevents.pool.membershipApplication.repository.MembershipCategoryRepository;
import com.columbusclubevents.pool.membershipApplication.repository.MembershipOptionRepsository;
import com.columbusclubevents.pool.membershipApplication.validation.ErrorMessage;
import com.columbusclubevents.pool.membershipApplication.validation.ValidationResponse;

@Controller
public class ApplicationController {
	Logger log = LoggerFactory.getLogger(ApplicationController.class);
	
	private static final String RESPONSE_SUCCESS = "SUCCESS";
	private static final String RESPONSE_FAILURE = "FAIL";
	
	@Autowired
	private MembershipCategoryRepository memberCategoryRepo;
	
	@Autowired
	private MembershipOptionRepsository memberOptionRepo;
	
	@Autowired
	private MemberRepository memberRepo;

	@Autowired
	private PaypalRestWrapper paypalWrapper;
	//private PaypalAdaptivePaymentWrapper paypalWrapper;

	@RequestMapping(value="/applicationBootstrap.htm",method=RequestMethod.GET)
	public String bootstrapForm(Model model){
		log.debug("Received GET request on applicationBootstrap");
		return "application-bootstrap";
	}
	
	@RequestMapping(value="/start-membership-form",method=RequestMethod.GET)
	public String startForm(Model model) {
		log.debug("Received GET request on start-membership-form");
		MembershipOptionsList options = getCurrentOptions();
		model.addAttribute("membershipOptionsList", options);
		return "membership-form";
	}
	
	@RequestMapping(value="/submit-membership-form.json", method=RequestMethod.POST,consumes="application/json", produces="application/json")
	public @ResponseBody ValidationResponse submitMemberForm(Model model, @RequestBody @Valid MemberRequest memberRequest, BindingResult result) {
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
			MembershipOption opt = memberRequest.getMembershipOption();
			String memberIdent = persistMember(member, opt);
			ValidationResponse res = createSuccessResponse();
			res.setSuccessIdentifier(memberIdent);
			res.setLastName(member.getLastName());
			return res;
		}
	}
	
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

	@RequestMapping(value="/start-payment.htm")
	public String startPayment(Model model, @RequestParam("id") String memberId, @RequestParam("lastName") String lastName) {
		log.debug("Received request to start payment for member ID '{}' with last name '{}'", memberId, lastName);
		//String redirectUrl = "https://www.paypal.com/";
		//paypalWrapper.postPayment();
		
		//log.debug("Forwarding user to redirect url '{}'", redirectUrl);
		//return new ModelAndView(new ExternalRedirectView(redirectUrl));

		Member member = retrieveMember(memberId, lastName);
		if(member != null) {
			//prep the new models
			PaymentCreditCard paymentCC = PaymentCreditCard.fromMember(member);
			PaymentPaypal paymentPaypal = new PaymentPaypal();
			model.addAttribute("paymentCC", paymentCC);
			model.addAttribute("paymentPaypal", paymentPaypal);
			return "create-payment";
		}
		else {
			return "member-no-match";
		}
	}
	
	@RequestMapping(value="/submit-payment-cc.json", method=RequestMethod.POST, consumes="application/json", produces="application/json")
	public @ResponseBody ValidationResponse payCC(Model model, @RequestBody @Valid PaymentCreditCard paymentCC, BindingResult result) {
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
		paymentCC.setFirstName(member.getFirstName());
		paymentCC.setAmount(member.getMemberCost().toString());
		
		try {
			if(paypalWrapper.postCCPayment(paymentCC)) {
				member.setMemberPaid(true);
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
				log.error("Invalid model passed into payCC: '{}'", paymentCC);
				return createSingleErrorResponse("id", "Payment unable to be processed by Paypal.");
			}
		}
		catch (Exception e) {
			log.error("Exception occurred parsing credit card request", e);
			return createSingleErrorResponse("id", "Error occurred during paypal processing.");
		}
	}
	/*
	public String payCC(@RequestBody MultiValueMap<String, String> formInput, BindingResult result) {
		log.debug("Received request to pay with Credit Card");
				
		log.debug("Form input contained map '{}'", formInput);
		PaymentCreditCard paymentCC = PaymentCreditCard.fromMultiValueMap(formInput);
		
		String memberId = paymentCC.getMemberId();
		String lastName = paymentCC.getLastName();
		Member member = retrieveMember(memberId, lastName);
		
		if(member == null) {
			log.error("Invalid model passed into payCC: '{}'", paymentCC);
			return "member-no-match";
		}
		
		log.debug("Setting additional properties on input CC request");
		paymentCC.setFirstName(member.getFirstName());
		paymentCC.setAmount(member.getMemberCost().toString());
		
		try {
			if(paypalWrapper.postCCPayment(paymentCC)) {
				return "success";
			} 
			else {
				return "payment-error";
			}
		}
		catch (Exception e) {
			log.error("Exception occurred parsing credit card request", e);
			return "payment-error";
		}
	}
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


	@RequestMapping(value="/retrieve-member.htm")
	public String retrieveSuccessPage(Model model) {
		log.debug("Received GET request on retrieve-member");
		return "retrieve-member";
	}
	
	@RequestMapping(value="/manage/manage-rates.htm",method= RequestMethod.GET)
	public String getRatesForm(Model model) {
		log.debug("Received GET request on manage-rates");
		return "manage-rates";
	}
	
	@RequestMapping(value="/manage/manage-rates.json",method=RequestMethod.POST,consumes="application/json", produces="application/json")
	public @ResponseBody ValidationResponse updateRates(Model model, @RequestBody @Valid MembershipOptionsList memberOpts, BindingResult result) {
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
	
	@RequestMapping(value="/manage/get-default-rate.json", method={RequestMethod.GET, RequestMethod.POST}) 
	public @ResponseBody MembershipOptionsList getDefaultRates(Model model) {
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
	 * @param errorMessages
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
		log.debug("Creating error message for field '{}' with message '{}'");
		ErrorMessage error = new ErrorMessage(field, msg);
		List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>();
		errorMessages.add(error);
		return createErrorResponse(errorMessages);
	}
	
	//TODO Update so that we are updating selected entites, rather than dropping and re-adding
	
	@Transactional
	private void clearAndUpdateMembershipCategories(List<MembershipCategory> memberCategories) {
		//TODO make this actually use the database to enforce uniqueness
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
	 * @param member
	 * @param opt
	 */
	@Transactional
	private String persistMember(Member member, MembershipOption opt) {
		log.debug("Member returned: {}", member);
		//fetch the remaining option data from the backing store by the option key
		opt = memberOptionRepo.findByOptionKey(opt.getOptionKey()).get(0);
		log.debug("Found membership option {}", opt);
		
		//set the default member status
		member.setMemberStatus(MemberStatus.NEW);
		member.setMemberPaid(false);
		
		//set the membership properties retrieved from the membership options
		member.setMemberCost(opt.getCost());
		
		//refetch the category to get all properties
		MembershipCategory cat = memberCategoryRepo.findOne(opt.getMemberCategoryParent().getId());
		log.debug("Setting membership category type on object: {}", cat);
		member.setMemberType(cat.getTabDescription());
		
		//persist the member into the DB
		log.debug("Persisting member info {}", member);
		memberRepo.save(member);
		log.debug("Entity persisted, now with ID {}", member.getId());
		
		/*
		//generate the code for the member going forward
		String identifier = StringUtils.rightPad(StringUtils.left(
				member.getLastName(), 4), 4, '_').concat(member.getId().toString());
		 */
		
		return member.getId().toString();
	}
	
	/**
	 * Validate the option the member selected as compared to the membership form.
	 * @param opt The membership option selected
	 * @param member The member object
	 * @return
	 */
	private List<ErrorMessage> internalValidateMemberOptions(MembershipOption opt, Member member) {
		List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>();
		
		if(opt.getOptionKey().equalsIgnoreCase("memberSwimPass") && member.getDependents().size() > 0) {
			log.warn("Individual selected member multi-swim pass, but added family members.  This is an invalid");
			errorMessages.add(new ErrorMessage("relationType", "You cannot select a multi-swim pass for additional family members.  A multi-swim pass can only be used by the council member."));
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
			/*
			String lastName4Chars = StringUtils.strip(StringUtils.left(memberId, 4), "_");
			if(StringUtils.isBlank(lastName4Chars)) {
				log.warn("Passed-in memberId '{}' has an empty name after trimming, returning null", memberId);
				return null;
			}
			Long appId = Long.parseLong(memberId.substring(4));
			List<Member> members = memberRepo.lastNameLikeAndId(lastName4Chars, lastName4Chars + "\uFFFD", appId);
			*/
			/*
			List<Member> members = memberRepo.findByLastNameAndId(lastName, Long.parseLong(memberId));
			log.debug("Executed member search, results: {}", members);
			if(members.size() != 1) {
				log.warn("Non-single result returned by query on member Id '{}' and last name '{}', invalid state, returning null", memberId, lastName);
				return null;
			}
			else {
				return members.get(0);
			}
			*/
			Member member = memberRepo.findOne(Long.parseLong(memberId));
			if(member != null && member.getLastName() != null && member.getLastName().equalsIgnoreCase(lastName)) {
				log.debug("Found matching member '{}'", member);
				return member;
			}
			else {
				log.warn("Member Id found for ID '{}', but with last name '{}' and not passed-in last name '{}'. Returning null", new Object[] {memberId, (member == null ? null : member.getLastName()), lastName});
				return null;
			}
		}
		catch(NumberFormatException e) {
			log.error("Passed-in member Id '{}' caused a NumberFormatException during memberId parsing. Presuming user entered invalid member Id and returning null", lastName, e);
			return null;
		}
	}
}
