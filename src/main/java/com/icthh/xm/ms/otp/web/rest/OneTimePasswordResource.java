package com.icthh.xm.ms.otp.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import com.icthh.xm.commons.security.XmAuthenticationContextHolder;
import com.icthh.xm.ms.otp.config.Constants;
import com.icthh.xm.ms.otp.repository.UaaRepository;
import com.icthh.xm.ms.otp.service.LoginPageRefreshableConfiguration;
import com.icthh.xm.ms.otp.service.OneTimePasswordService;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordCheckDto;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import com.icthh.xm.ms.otp.web.rest.errors.BadRequestAlertException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.icthh.xm.ms.otp.config.Constants.ACCESS_TOKEN;
import static com.icthh.xm.ms.otp.config.Constants.MSISDN;
import static java.util.Objects.requireNonNull;

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
    private final LoginPageRefreshableConfiguration loginPageRefreshableConfiguration;
    private final UaaRepository uaaRepository;
    private final XmAuthenticationContextHolder authenticationContextHolder;

    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    @Timed
    public ResponseEntity<String> login() {

        if (loginPageRefreshableConfiguration.getLoginHtmlContent() == null){
            return ResponseEntity.notFound().build();
        }
        log.debug("REST login request");
        return ResponseEntity.ok(loginPageRefreshableConfiguration.getLoginHtmlContent());
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

    /**
     * POST  /one-time-password/validate : Validate an existing oneTimePassword and redirect page.
     * @see OneTimePasswordResource#checkOneTimePassword(OneTimePasswordCheckDto)
     */
    @PreAuthorize("hasPermission({'oneTimePasswordCheckDto': #oneTimePasswordCheckDto} ,'OTP.ONETIMEPASSWORD.CHECK')")
    @PostMapping("/one-time-password/validate")
    @Timed
    public RedirectView checkOneTimePasswordAndRedirectWithCode(@Valid @RequestBody OneTimePasswordCheckDto oneTimePasswordCheckDto,
                                                                @RequestHeader(value = "redirect-uri") String redirectUri) throws URISyntaxException {
        redirectUri = decodeUrl(redirectUri);
        checkOneTimePassword(oneTimePasswordCheckDto);
        OneTimePasswordDto dto = oneTimePasswordService.findOne(oneTimePasswordCheckDto.getId()).get();
        String code =  uaaRepository.getOAuth2Token(ImmutableMap.of(MSISDN, dto.getReceiver()));
        return new RedirectView(new StringBuilder(redirectUri).append("?code=").append(code).toString(), true);
    }

    @PostMapping("/oauth/token")
    @Timed
    public ResponseEntity validateCode(@RequestParam(name = "code") String code) {
        return ResponseEntity.ok(ImmutableMap.of(ACCESS_TOKEN, code));
    }

    @GetMapping("/userinfo")
    @Timed
    public ResponseEntity getUserInfo() {
        Optional<String> login = authenticationContextHolder.getContext().getAdditionalDetailsValue(MSISDN);
        if (!login.isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        String phoneNumber = login.get();
        return ResponseEntity.ok(new HashMap<String, String>(){{
            put("phoneNumber", phoneNumber);
            put("id", phoneNumber);
        }});
    }

    @SneakyThrows
    private String decodeUrl(String url){
        return URLDecoder.decode(url, StandardCharsets.UTF_8.name());
    }


}
