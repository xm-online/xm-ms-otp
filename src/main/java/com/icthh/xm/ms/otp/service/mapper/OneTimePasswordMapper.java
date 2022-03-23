package com.icthh.xm.ms.otp.service.mapper;

import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for the entity OneTimePassword and its DTO OneTimePasswordDto.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OneTimePasswordMapper extends EntityMapper<OneTimePasswordDto, OneTimePassword> {

    default OneTimePassword fromId(Long id) {
        if (id == null) {
            return null;
        }
        OneTimePassword oneTimePassword = new OneTimePassword();
        oneTimePassword.setId(id);
        return oneTimePassword;
    }
}
