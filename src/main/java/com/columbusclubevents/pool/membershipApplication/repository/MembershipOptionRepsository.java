package com.columbusclubevents.pool.membershipApplication.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.columbusclubevents.pool.membershipApplication.model.MembershipOption;
import com.google.appengine.api.datastore.Key;

public interface MembershipOptionRepsository extends JpaRepository<MembershipOption, Key> {

	public List<MembershipOption> findByOptionKey(String optionKey);
}
