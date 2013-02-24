package com.columbusclubevents.pool.membershipApplication.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

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
import com.columbusclubevents.pool.membershipApplication.model.MembershipCategory;
import com.columbusclubevents.pool.membershipApplication.model.MembershipOption;
import com.columbusclubevents.pool.membershipApplication.model.MembershipOptionsList;
import com.columbusclubevents.pool.membershipApplication.model.validation.ErrorMessage;
import com.columbusclubevents.pool.membershipApplication.model.validation.ValidationResponse;
import com.columbusclubevents.pool.membershipApplication.repository.MemberRepository;
import com.columbusclubevents.pool.membershipApplication.repository.MembershipCategoryRepository;

@Controller
public class ApplicationController {
	Logger log = LoggerFactory.getLogger(ApplicationController.class);
	
	@Autowired
	private MembershipCategoryRepository memberCategoryRepo;
	
	@Autowired
	private MemberRepository memberRepo;

	@RequestMapping(value="/applicationBootstrap",method=RequestMethod.GET)
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
	public @ResponseBody ValidationResponse submitForm(Model model, @RequestBody @Valid Member member, BindingResult result) {
		log.debug("Received POST request on submit-membership-form.json");
		log.debug("Member returned: {}", member);
		ValidationResponse res = new ValidationResponse();
		if(result.hasErrors()){
			log.debug("Validation errors on input member form");
			res.setStatus("FAIL");
			res.setErrorMessageList(processErrors(result));
		}
		else {
			log.debug("Validation of member form succeeded!");
			//memberRepo.save(member);
			res.setStatus("SUCCESS");
		}

		return res;
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
			ErrorMessage error = new ErrorMessage(objectError.getField(), objectError.getField() + "  " + objectError.getDefaultMessage());
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
}
