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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.columbusclubevents.pool.membershipApplication.model.Member;
import com.columbusclubevents.pool.membershipApplication.model.MemberRequest;
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
	public @ResponseBody ValidationResponse submitForm(Model model, @RequestBody @Valid MemberRequest memberRequest, BindingResult result) {
		log.debug("Received POST request on submit-membership-form.json with member request {}", memberRequest);
		Member member = memberRequest.getMember();
		ValidationResponse res = new ValidationResponse();
		if(result.hasErrors()){
			log.debug("Validation errors on input member form");
			res.setStatus("FAIL");
			res.setErrorMessageList(processErrors(result));
		}
		else {
			log.debug("Validation of member form succeeded!");
			MembershipOption opt = memberRequest.getMembershipOption();
			String memberIdent = persistMember(member, opt);
			res.setSuccessIdentifier(memberIdent);
			res.setLastName(member.getLastName());
			res.setStatus("SUCCESS");
		}

		return res;
	}
	
	@RequestMapping(value="/application-complete.htm")
	public String showSuccessPage(Model model, @RequestParam("memberId") String memberId, @RequestParam("lastName") String lastName) {
		log.debug("Application complete detected wtih memberId '{}' and last name '{}'", memberId, lastName);
		
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
			PaymentCreditCard paymentCC = new PaymentCreditCard();
			paymentCC.setMemberId(memberId);
			paymentCC.setLastName(lastName);
			PaymentPaypal paymentPaypal = new PaymentPaypal();
			//paymentPaypal.setMemberId(memberId);
			//paymentPaypal.setLastName(lastName);
			model.addAttribute("paymentCC", paymentCC);
			model.addAttribute("paymentPaypal", paymentPaypal);
			return "create-payment";
		}
		else {
			return "member-no-match";
		}
	}
	
	@RequestMapping(value="/submit-payment-cc.htm", method=RequestMethod.POST)
	public String payCC(PaymentCreditCard paymentCC, BindingResult result) {
		log.debug("Received request to pay with Credit Card");

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
			log.error("Exception occurred parsing credit card request");
			return "payment-error";
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
		member.setMemberStatus("new");
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
