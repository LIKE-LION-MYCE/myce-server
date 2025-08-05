package com.myce.common.repository;

import com.myce.common.entity.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

}
