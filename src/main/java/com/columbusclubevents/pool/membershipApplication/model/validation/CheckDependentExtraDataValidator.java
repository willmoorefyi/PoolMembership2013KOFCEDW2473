package com.columbusclubevents.pool.membershipApplication.model.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckDependentExtraDataValidator implements ConstraintValidator<CheckDependentExtraData, String> {

	@Override
	public void initialize(CheckDependentExtraData constraintAnnotation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		// TODO Auto-generated method stub
		return false;
	}

}
