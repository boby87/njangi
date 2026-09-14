package com.njangi.membres.domain.model;

import java.util.Objects;
import java.util.UUID;

public record MembreId(UUID value) {
    public MembreId {
        Objects.requireNonNull(value, "MembreId cannot be null");
    }

    public static MembreId generate() {
        return new MembreId(UUID.randomUUID());
    }

    public static MembreId from(String id) {
        return new MembreId(UUID.fromString(id));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
