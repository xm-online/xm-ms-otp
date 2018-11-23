package com.icthh.xm.ms.otp.service.mapper;

import com.icthh.xm.ms.otp.domain.*;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity OneTimePassword and its DTO OneTimePasswordDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface OneTimePasswordMapper extends EntityMapper<OneTimePasswordDTO, OneTimePassword> {

    default OneTimePassword fromId(Long id) {
        if (id == null) {
            return null;
        }
        OneTimePassword oneTimePassword = new OneTimePassword();
        oneTimePassword.setId(id);
        return oneTimePassword;
    }
}
