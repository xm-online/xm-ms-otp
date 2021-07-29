package com.icthh.xm.ms.otp.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.icthh.xm.commons.permission.annotation.PrivilegeDescription;
import com.icthh.xm.commons.security.XmAuthenticationContextHolder;
import com.icthh.xm.ms.otp.service.OneTimePasswordService;
import com.icthh.xm.ms.otp.service.UaaService;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordCheckDto;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import com.icthh.xm.ms.otp.web.rest.errors.BadRequestAlertException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.of;
import static com.icthh.xm.ms.otp.config.Constants.ACCESS_TOKEN;
import static com.icthh.xm.ms.otp.config.Constants.RECEIVER;

/**
 * REST controller for managing OneTimePassword.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OneTimePasswordResource {

    private final Logger log = LoggerFactory.getLogger(OneTimePasswordResource.class);

    private static final String ENTITY_NAME = "otpOneTimePassword";
    private final OneTimePasswordService oneTimePasswordService;
    private final UaaService uaaRepository;
    private final XmAuthenticationContextHolder authenticationContextHolder;

    @Timed
    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView login() {
        return new ModelAndView("loginPageView");
    }

    /**
     * POST  /one-time-password : Create a new oneTimePassword.
     *
     * @param oneTimePasswordDto the oneTimePasswordDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new oneTimePasswordDto,
     * or with status 400 (Bad Request) if the oneTimePassword has already an ID
     */
    @PreAuthorize("hasPermission({'oneTimePasswordDto': #oneTimePasswordDto} ,'OTP.ONETIMEPASSWORD.GENERATE')")
    @PostMapping("/one-time-password")
    @Timed
    @PrivilegeDescription("Privilege to create a new one time password")
    public ResponseEntity<OneTimePasswordDto> generateOneTimePassword(
        @Valid @RequestBody OneTimePasswordDto oneTimePasswordDto) {

        log.debug("REST request to generate OneTimePassword : {}", oneTimePasswordDto);

        OneTimePasswordDto result = oneTimePasswordService.generate(oneTimePasswordDto);
        return ResponseEntity.ok(result);
    }

    /**
     * GET  /one-time-password/{id} : retrieves existing one time password
     *
     * @param id one time password id
     * @return the ResponseEntity with status 200 and one time password info,
     * or with status 404 (Not Found) if the one time password is not found
     */
    @PreAuthorize("hasPermission(null, 'OTP.ONETIMEPASSWORD.GET')")
    @GetMapping("/one-time-password/{id}")
    @Timed
    @PrivilegeDescription("Privilege to get an one time password information")
    public OneTimePasswordDto getOneTimePassword(@PathVariable Long id) {
        log.debug("received request to get oneTimePassword with id: {}", id);

        return oneTimePasswordService.findOne(id)
            .orElseThrow(() -> new NoSuchElementException("Could not find one time password for given id"));
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
    @PrivilegeDescription("Privilege to validate an existing oneTimePassword")
    public ResponseEntity<OneTimePasswordCheckDto> checkOneTimePassword(
        @Valid @RequestBody OneTimePasswordCheckDto oneTimePasswordCheckDto) throws URISyntaxException {
        log.debug("REST request to check OneTimePassword : {}", oneTimePasswordCheckDto);
        if (oneTimePasswordCheckDto.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        oneTimePasswordService.check(oneTimePasswordCheckDto);
        return ResponseEntity.ok(oneTimePasswordCheckDto);
    }

    /**
     * POST  /one-time-password/validate : Validate an existing oneTimePassword and redirect page.
     *
     * @see OneTimePasswordResource#checkOneTimePassword(OneTimePasswordCheckDto)
     */
    @PreAuthorize("hasPermission({'oneTimePasswordCheckDto': #oneTimePasswordCheckDto} ,'OTP.ONETIMEPASSWORD.CHECK')")
    @PostMapping("/one-time-password/validate")
    @Timed
    @PrivilegeDescription("Privilege to validate an existing oneTimePassword")
    public RedirectView checkOneTimePasswordAndRedirectWithCode(@Valid @RequestBody OneTimePasswordCheckDto oneTimePasswordCheckDto,
                                                                @RequestHeader(value = "redirect-uri") String redirectUri) throws URISyntaxException {
        redirectUri = decodeUrl(redirectUri);
        checkOneTimePassword(oneTimePasswordCheckDto);
        OneTimePasswordDto dto = oneTimePasswordService.findOne(oneTimePasswordCheckDto.getId()).get();
        String code = uaaRepository.getOAuth2Token(of(RECEIVER, dto.getReceiver()));
        return new RedirectView(new StringBuilder(redirectUri).append("?code=").append(code).toString(), true);
    }

    /**
     * POST /oauth/token  Convert  "code" parameter into "access_token" in order to satisfy OAuth protocol
     *
     * @param code jwt token
     */
    @PostMapping("/oauth/token")
    @Timed
    public ResponseEntity validateCode(@RequestParam(name = "code") String code) {
        return ResponseEntity.ok(of(ACCESS_TOKEN, code));
    }

    /**
     * GET /userinfo  Return user information gotten from additional details of access_token
     */
    @GetMapping("/userinfo")
    @Timed
    public ResponseEntity getUserInfo() {
        Optional<String> login = authenticationContextHolder.getContext().getAdditionalDetailsValue(RECEIVER);
        if (!login.isPresent()) {
            log.warn("Receiver is not set in access token");
            return ResponseEntity.badRequest().build();
        }
        String phoneNumber = login.get();
        return ResponseEntity.ok(of("phoneNumber", phoneNumber, "id", phoneNumber));
    }

    @SneakyThrows
    private String decodeUrl(String url) {
        return URLDecoder.decode(url, StandardCharsets.UTF_8.name());
    }


}
