package org.webflux.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public abstract class EntityBase {

    public EntityBase() {
        createdAt = Instant.now();
    }
    protected Long id;
    protected User createdBy;
    protected Instant createdAt;
    protected User updatedBy;
    protected Instant updatedAt;
    protected Boolean isDeleted;
    protected Boolean isActivated;
}