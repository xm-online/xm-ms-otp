package com.icthh.xm.ms.otp.domain;


import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import com.icthh.xm.ms.otp.domain.enumeration.ReciverTypeKey;

/**
 * A OneTimePassword.
 */
@Entity
@Table(name = "one_time_password")
public class OneTimePassword implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "receiver", nullable = false)
    private String receiver;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "receiver_type_key", nullable = false)
    private ReciverTypeKey receiverTypeKey;

    @NotNull
    @Column(name = "type_key", nullable = false)
    private String typeKey;

    @NotNull
    @Column(name = "state_key", nullable = false)
    private String stateKey;

    @NotNull
    @Column(name = "retries", nullable = false)
    private Integer retries;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @NotNull
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReceiver() {
        return receiver;
    }

    public OneTimePassword receiver(String receiver) {
        this.receiver = receiver;
        return this;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public ReciverTypeKey getReceiverTypeKey() {
        return receiverTypeKey;
    }

    public OneTimePassword receiverTypeKey(ReciverTypeKey receiverTypeKey) {
        this.receiverTypeKey = receiverTypeKey;
        return this;
    }

    public void setReceiverTypeKey(ReciverTypeKey receiverTypeKey) {
        this.receiverTypeKey = receiverTypeKey;
    }

    public String getTypeKey() {
        return typeKey;
    }

    public OneTimePassword typeKey(String typeKey) {
        this.typeKey = typeKey;
        return this;
    }

    public void setTypeKey(String typeKey) {
        this.typeKey = typeKey;
    }

    public String getStateKey() {
        return stateKey;
    }

    public OneTimePassword stateKey(String stateKey) {
        this.stateKey = stateKey;
        return this;
    }

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    public Integer getRetries() {
        return retries;
    }

    public OneTimePassword retries(Integer retries) {
        this.retries = retries;
        return this;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public OneTimePassword startDate(Instant startDate) {
        this.startDate = startDate;
        return this;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public OneTimePassword endDate(Instant endDate) {
        this.endDate = endDate;
        return this;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public OneTimePassword passwordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OneTimePassword oneTimePassword = (OneTimePassword) o;
        if (oneTimePassword.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), oneTimePassword.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "OneTimePassword{" +
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
