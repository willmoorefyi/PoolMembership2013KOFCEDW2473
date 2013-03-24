package com.columbusclubevents.pool.membershipApplication.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.columbusclubevents.pool.membershipApplication.model.Member;
import com.columbusclubevents.pool.membershipApplication.model.MemberStatus;

public interface MemberRepository extends JpaRepository<Member, Long> {

	public List<Member> findByMemberStatus(MemberStatus memberStatus);
	
	//this always throws a null pointer exception. Appengine / DataNucleus doesn't appear to like these queries, I guess?
	@Query("SELECT m FROM Member m WHERE m.lastName >= (:lastName) AND m.lastName < (:lastNameAllChars) AND m.id = (:id)")
	public List<Member> lastNameLikeAndId(@Param("lastName") String lastName, @Param("lastNameAllChars") String lastNameAllChars, @Param("id") Long id);
	
	//as far as I can tell this doesn't appear to work.  Perhaps querying by ID isn't supported?  Looking at the id in the explorer seems to imply it might not be
	public List<Member> findByLastNameAndId(String lastName, Long id);
}
