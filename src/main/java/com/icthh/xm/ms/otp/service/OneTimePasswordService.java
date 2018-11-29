package com.icthh.xm.ms.otp.service;

import com.icthh.xm.ms.otp.service.dto.OneTimePasswordCheckDto;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing OneTimePassword.
 */
public interface OneTimePasswordService {

    /**
     * Save a oneTimePassword.
     *
     * @param oneTimePasswordDto the entity to generate
     * @return the persisted entity
     */
    OneTimePasswordDto generate(OneTimePasswordDto oneTimePasswordDto);


    /**
     * Validates one time password.
     *
     * @param oneTimePasswordCheckDto the entity to save
     */
    void check(OneTimePasswordCheckDto oneTimePasswordCheckDto);

    /**
     * Get all the oneTimePasswords.
     *
     * @return the list of entities
     */
    List<OneTimePasswordDto> findAll();


    /**
     * Get the "id" oneTimePassword.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Optional<OneTimePasswordDto> findOne(Long id);

    /**
     * Delete the "id" oneTimePassword.
     *
     * @param id the id of the entity
     */
    void delete(Long id);
}
