package com.icthh.xm.ms.otp.repository;

import com.icthh.xm.ms.otp.domain.OneTimePassword;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;


/**
 * Spring Data  repository for the OneTimePassword entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OneTimePasswordRepository extends JpaRepository<OneTimePassword, Long> {

    //currently it is to be used in lep only
    @Deprecated(since = "2.0.6", forRemoval = true)
    OneTimePassword findTopByReceiverOrderByIdDesc(String receiver);

    /**
     * Retrieve OTP with newest start date for given receiver.
     *
     * @param receiver OTP receiver
     * @return OTP with newest start date
     */
    OneTimePassword findTopByReceiverOrderByStartDateDesc(String receiver);

    List<OneTimePassword> findAllByReceiverAndTypeKeyAndStartDateGreaterThanEqual(String receiver,
                                                                                  String typeKey,
                                                                                  Instant startDate);
}
