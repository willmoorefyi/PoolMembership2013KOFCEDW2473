package com.columbusclubevents.pool.membershipApplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.columbusclubevents.pool.membershipApplication.model.MembershipCategory;

public interface MembershipCategoryRepository extends JpaRepository<MembershipCategory, Long> {

	//@Query("SELECT mo FROM MembershipCategory mo WHERE (:membershipOption) MEMBER OF (mo.memberOptions)")
	//List<MembershipCategory> findByMembershipOption(@Param("membershipOption") MembershipOption membershipOption);
}
