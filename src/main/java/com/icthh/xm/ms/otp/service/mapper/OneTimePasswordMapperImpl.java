package com.icthh.xm.ms.otp.service.mapper;

import com.icthh.xm.ms.otp.domain.OneTimePassword;
import com.icthh.xm.ms.otp.service.dto.OneTimePasswordDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OneTimePasswordMapperImpl implements OneTimePasswordMapper {

    @Override
    public OneTimePassword toEntity(OneTimePasswordDTO dto) {
        return null;
    }

    @Override
    public OneTimePasswordDTO toDto(OneTimePassword entity) {
        return null;
    }

    @Override
    public List<OneTimePassword> toEntity(List<OneTimePasswordDTO> dtoList) {
        return null;
    }

    @Override
    public List<OneTimePasswordDTO> toDto(List<OneTimePassword> entityList) {
        return null;
    }
}
