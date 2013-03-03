package com.columbusclubevents.pool.membershipApplication.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.columbusclubevents.pool.membershipApplication.model.Member;
import com.google.appengine.api.datastore.Key;

public interface MemberRepository extends JpaRepository<Member, Key> {

	public List<Member> findByLastNameLikeAndId(String lastName, Long Id);
}
