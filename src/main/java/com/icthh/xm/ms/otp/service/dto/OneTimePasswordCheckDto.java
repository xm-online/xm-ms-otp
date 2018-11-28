package com.icthh.xm.ms.otp.service.dto;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the OneTimePasswordCheckDto.
 */
public class OneTimePasswordCheckDto implements Serializable {

    @NotNull
    private Long id;

    @NotNull
    private String otp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OneTimePasswordCheckDto oneTimePasswordCheckDto = (OneTimePasswordCheckDto) o;
        if (oneTimePasswordCheckDto.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), oneTimePasswordCheckDto.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "OneTimePasswordDto{" +
            "id=" + getId() +
            "}";
    }
}
