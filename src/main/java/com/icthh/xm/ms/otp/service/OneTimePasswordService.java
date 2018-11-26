package com.icthh.xm.ms.otp.service;

import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing OneTimePassword.
 */
public interface OneTimePasswordService {

    /**
     * Save a oneTimePassword.
     *
     * @param oneTimePasswordDTO the entity to generate
     * @return the persisted entity
     */
    OneTimePasswordDTO generate(OneTimePasswordDTO oneTimePasswordDTO);

    /**
     * Get all the oneTimePasswords.
     *
     * @return the list of entities
     */
    List<OneTimePasswordDTO> findAll();


    /**
     * Get the "id" oneTimePassword.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Optional<OneTimePasswordDTO> findOne(Long id);

    /**
     * Delete the "id" oneTimePassword.
     *
     * @param id the id of the entity
     */
    void delete(Long id);
}
