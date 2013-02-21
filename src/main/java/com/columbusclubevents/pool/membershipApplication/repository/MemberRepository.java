package com.columbusclubevents.pool.membershipApplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.columbusclubevents.pool.membershipApplication.model.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
