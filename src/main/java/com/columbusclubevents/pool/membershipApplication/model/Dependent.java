package com.columbusclubevents.pool.membershipApplication.model;

import javax.validation.constraints.NotNull;

public class Dependent {

	@NotNull
	public Member parent;
	
	@NotNull
	private UserName name;
}
