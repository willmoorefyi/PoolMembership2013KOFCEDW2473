package com.columbusclubevents.pool.membershipApplication.model;

import com.google.common.base.Objects;

/**
 * @author webs.com
 */
public class MemberNewPaymentRequest {
	private Long id;
	private Integer cost;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getCost() {
		return cost;
	}

	public void setCost(Integer cost) {
		this.cost = cost;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("id", id)
				.add("cost", cost)
				.toString();
	}
}
