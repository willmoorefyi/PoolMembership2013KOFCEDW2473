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
import org.springframework.web.bind.annotation.ResponseBody;

import com.columbusclubevents.pool.membershipApplication.model.Member;
import com.columbusclubevents.pool.membershipApplication.model.MemberRequest;
import com.columbusclubevents.pool.membershipApplication.model.MembershipCategory;
import com.columbusclubevents.pool.membershipApplication.model.MembershipOption;
import com.columbusclubevents.pool.membershipApplication.model.MembershipOptionsList;
import com.columbusclubevents.pool.membershipApplication.paypal.PaypalWrapper;
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
	private PaypalWrapper paypalWrapper;

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
			res.setStatus("SUCCESS");
		}

		return res;
	}
	
	@RequestMapping(value="/application-complete.htm")
	public String showSuccessPage(Model model) {
		log.debug("Application complete");
		return "application-complete";
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
		
		//generate the code for the member going forward
		String identifier = StringUtils.rightPad(StringUtils.left(
				member.getLastName(), 4), 4, '_').concat(member.getId().toString());
		
		return identifier;
	}
}
