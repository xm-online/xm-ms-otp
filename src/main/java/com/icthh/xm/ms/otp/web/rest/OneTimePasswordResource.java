package com.icthh.xm.ms.otp.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.ms.otp.service.OneTimePasswordService;
import com.icthh.xm.ms.otp.web.rest.errors.BadRequestAlertException;
import com.icthh.xm.ms.otp.web.rest.util.HeaderUtil;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDTO;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing OneTimePassword.
 */
@RestController
@RequestMapping("/api")
public class OneTimePasswordResource {

    private final Logger log = LoggerFactory.getLogger(OneTimePasswordResource.class);

    private static final String ENTITY_NAME = "otpOneTimePassword";

    private final OneTimePasswordService oneTimePasswordService;

    public OneTimePasswordResource(OneTimePasswordService oneTimePasswordService) {
        this.oneTimePasswordService = oneTimePasswordService;
    }

    /**
     * POST  /one-time-passwords : Create a new oneTimePassword.
     *
     * @param oneTimePasswordDTO the oneTimePasswordDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new oneTimePasswordDTO, or with status 400 (Bad Request) if the oneTimePassword has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/one-time-passwords")
    @Timed
    public ResponseEntity<OneTimePasswordDTO> createOneTimePassword(@Valid @RequestBody OneTimePasswordDTO oneTimePasswordDTO) throws URISyntaxException {
        log.debug("REST request to save OneTimePassword : {}", oneTimePasswordDTO);
        if (oneTimePasswordDTO.getId() != null) {
            throw new BadRequestAlertException("A new oneTimePassword cannot already have an ID", ENTITY_NAME, "idexists");
        }
        OneTimePasswordDTO result = oneTimePasswordService.save(oneTimePasswordDTO);
        return ResponseEntity.created(new URI("/api/one-time-passwords/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /one-time-passwords : Updates an existing oneTimePassword.
     *
     * @param oneTimePasswordDTO the oneTimePasswordDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated oneTimePasswordDTO,
     * or with status 400 (Bad Request) if the oneTimePasswordDTO is not valid,
     * or with status 500 (Internal Server Error) if the oneTimePasswordDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/one-time-passwords")
    @Timed
    public ResponseEntity<OneTimePasswordDTO> updateOneTimePassword(@Valid @RequestBody OneTimePasswordDTO oneTimePasswordDTO) throws URISyntaxException {
        log.debug("REST request to update OneTimePassword : {}", oneTimePasswordDTO);
        if (oneTimePasswordDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        OneTimePasswordDTO result = oneTimePasswordService.save(oneTimePasswordDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, oneTimePasswordDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /one-time-passwords : get all the oneTimePasswords.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of oneTimePasswords in body
     */
    @GetMapping("/one-time-passwords")
    @Timed
    public List<OneTimePasswordDTO> getAllOneTimePasswords() {
        log.debug("REST request to get all OneTimePasswords");
        return oneTimePasswordService.findAll();
    }

    /**
     * GET  /one-time-passwords/:id : get the "id" oneTimePassword.
     *
     * @param id the id of the oneTimePasswordDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the oneTimePasswordDTO, or with status 404 (Not Found)
     */
    @GetMapping("/one-time-passwords/{id}")
    @Timed
    public ResponseEntity<OneTimePasswordDTO> getOneTimePassword(@PathVariable Long id) {
        log.debug("REST request to get OneTimePassword : {}", id);
        Optional<OneTimePasswordDTO> oneTimePasswordDTO = oneTimePasswordService.findOne(id);
        return ResponseUtil.wrapOrNotFound(oneTimePasswordDTO);
    }

    /**
     * DELETE  /one-time-passwords/:id : delete the "id" oneTimePassword.
     *
     * @param id the id of the oneTimePasswordDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/one-time-passwords/{id}")
    @Timed
    public ResponseEntity<Void> deleteOneTimePassword(@PathVariable Long id) {
        log.debug("REST request to delete OneTimePassword : {}", id);
        oneTimePasswordService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
