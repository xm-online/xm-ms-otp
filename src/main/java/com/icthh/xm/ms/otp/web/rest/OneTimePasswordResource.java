package com.icthh.xm.ms.otp.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.ms.otp.service.OneTimePasswordService;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordCheckDto;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import com.icthh.xm.ms.otp.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URISyntaxException;

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
     * POST  /one-time-password : Create a new oneTimePassword.
     *
     * @param oneTimePasswordDto the oneTimePasswordDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new oneTimePasswordDto,
     * or with status 400 (Bad Request) if the oneTimePassword has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PreAuthorize("hasPermission({'oneTimePasswordDto': #oneTimePasswordDto} ,'OTP.ONETIMEPASSWORD.GENERATE')")
    @PostMapping("/one-time-password")
    @Timed
    public ResponseEntity<OneTimePasswordDto> generateOneTimePassword(
        @Valid @RequestBody OneTimePasswordDto oneTimePasswordDto) throws URISyntaxException {

        log.debug("REST request to generate OneTimePassword : {}", oneTimePasswordDto);

        OneTimePasswordDto result = oneTimePasswordService.generate(oneTimePasswordDto);
        return ResponseEntity.ok(result);
    }

    /**
     * POST  /one-time-password/check : Validate an existing oneTimePassword.
     *
     * @param oneTimePasswordCheckDto the otp for checking
     * @return the ResponseEntity with status 200 (OK)
     * or with status 400 (Bad Request) if the oneTimePasswordCheckDto is not valid,
     * or with status 500 (Internal Server Error) if the oneTimePasswordCheckDto couldn't be checked
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PreAuthorize("hasPermission({'oneTimePasswordCheckDto': #oneTimePasswordCheckDto} ,'OTP.ONETIMEPASSWORD.CHECK')")
    @PostMapping("/one-time-password/check")
    @Timed
    public ResponseEntity<OneTimePasswordCheckDto> checkOneTimePassword(
        @Valid @RequestBody OneTimePasswordCheckDto oneTimePasswordCheckDto) throws URISyntaxException {
        log.debug("REST request to update OneTimePassword : {}", oneTimePasswordCheckDto);
        if (oneTimePasswordCheckDto.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        oneTimePasswordService.check(oneTimePasswordCheckDto);
        return ResponseEntity.ok(oneTimePasswordCheckDto);
    }
}
