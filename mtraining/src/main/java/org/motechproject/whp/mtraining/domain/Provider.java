package org.motechproject.whp.mtraining.domain;


import org.motechproject.whp.mtraining.web.domain.ActivationStatus;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;

@PersistenceCapable(table = "provider", identityType = IdentityType.APPLICATION)
public class Provider {

    @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
    @PrimaryKey
    private Long id;

    @Persistent(column = "primary_contact_number", defaultFetchGroup = "true")
    private Long callerId;

    @Column(name = "location_id")
    private Location location;

    @Column(name = "activation_status")
    private String activationStatus;

    @Column(name = "remedy_id")
    @Unique(name = "remedy_id")
    @NotNull
    private String remedyId;

    public Provider(Long callerId, Location location, ActivationStatus activationStatus) {
        this.callerId = callerId;
        this.location = location;
        this.remedyId = randomAlphanumeric(20);
        this.activationStatus = activationStatus.getStatus();
    }

    public Long getCallerId() {
        return callerId;
    }

    public Long getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public String getActivationStatus() {
        return activationStatus;
    }

    public String getRemedyId() {
        return remedyId;
    }
}
