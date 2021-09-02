package com.icthh.xm.ms.otp.service.dto;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * A DTO for the OneTimePassword entity.
 */
@Getter @Setter
@ToString
public class OneTimePasswordDto implements Serializable {

    private Long id;
    @NotNull
    private String receiver;
    private String stateKey;
    @NotNull
    private ReceiverTypeKey receiverTypeKey;
    @NotNull
    private String typeKey;
    private String langKey;
    private Map<String, Object> model;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OneTimePasswordDto oneTimePasswordDto = (OneTimePasswordDto) o;
        if (oneTimePasswordDto.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), oneTimePasswordDto.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

}
