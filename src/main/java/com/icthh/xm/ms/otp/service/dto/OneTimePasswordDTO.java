package com.icthh.xm.ms.otp.service.dto;

import java.time.Instant;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import com.icthh.xm.ms.otp.domain.enumeration.ReciverTypeKey;

/**
 * A DTO for the OneTimePassword entity.
 */
public class OneTimePasswordDTO implements Serializable {

    private Long id;

    @NotNull
    private String receiver;

    @NotNull
    private ReciverTypeKey receiverTypeKey;

    @NotNull
    private String typeKey;

    @NotNull
    private String stateKey;

    @NotNull
    private Integer retries;

    @NotNull
    private Instant startDate;

    @NotNull
    private Instant endDate;

    @NotNull
    private String passwordHash;

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

    public ReciverTypeKey getReceiverTypeKey() {
        return receiverTypeKey;
    }

    public void setReceiverTypeKey(ReciverTypeKey receiverTypeKey) {
        this.receiverTypeKey = receiverTypeKey;
    }

    public String getTypeKey() {
        return typeKey;
    }

    public void setTypeKey(String typeKey) {
        this.typeKey = typeKey;
    }

    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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
            ", stateKey='" + getStateKey() + "'" +
            ", retries=" + getRetries() +
            ", startDate='" + getStartDate() + "'" +
            ", endDate='" + getEndDate() + "'" +
            ", passwordHash='" + getPasswordHash() + "'" +
            "}";
    }
}
