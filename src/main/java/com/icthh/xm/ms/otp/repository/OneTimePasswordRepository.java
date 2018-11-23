package com.icthh.xm.ms.otp.repository;

import com.icthh.xm.ms.otp.domain.OneTimePassword;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the OneTimePassword entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OneTimePasswordRepository extends JpaRepository<OneTimePassword, Long> {

}
