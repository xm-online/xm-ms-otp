package com.icthh.xm.ms.otp.service.dto;

import java.io.Serializable;
import java.util.Objects;

import com.icthh.xm.ms.otp.domain.enumeration.ReceiverTypeKey;

import javax.validation.constraints.NotNull;

/**
 * A DTO for the OneTimePassword entity.
 */
public class OneTimePasswordDTO implements Serializable {

    private Long id;

    @NotNull
    private String receiver;

    @NotNull
    private ReceiverTypeKey receiverTypeKey;

    @NotNull
    private String typeKey;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public ReceiverTypeKey getReceiverTypeKey() {
        return receiverTypeKey;
    }

    public void setReceiverTypeKey(ReceiverTypeKey receiverTypeKey) {
        this.receiverTypeKey = receiverTypeKey;
    }

    public String getTypeKey() {
        return typeKey;
    }

    public void setTypeKey(String typeKey) {
        this.typeKey = typeKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OneTimePasswordDTO oneTimePasswordDTO = (OneTimePasswordDTO) o;
        if (oneTimePasswordDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), oneTimePasswordDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "OneTimePasswordDTO{" +
            "id=" + getId() +
            ", receiver='" + getReceiver() + "'" +
            ", receiverTypeKey='" + getReceiverTypeKey() + "'" +
            ", typeKey='" + getTypeKey() + "'" +
            "}";
    }
}
