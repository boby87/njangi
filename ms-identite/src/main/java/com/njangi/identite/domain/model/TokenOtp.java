package com.njangi.identite.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class TokenOtp {

    private UUID id;
    private String identifiant;
    private String code;
    private LocalDateTime expireLe;
    private boolean utilise;
    private LocalDateTime creeLe;

    public TokenOtp(UUID id, String identifiant, String code, LocalDateTime expireLe, boolean utilise, LocalDateTime creeLe) {
        this.id = id;
        this.identifiant = identifiant;
        this.code = code;
        this.expireLe = expireLe;
        this.utilise = utilise;
        this.creeLe = creeLe != null ? creeLe : LocalDateTime.now();
    }

    public TokenOtp(String identifiant, String code, LocalDateTime expireLe) {
        this(null, identifiant, code, expireLe, false, LocalDateTime.now());
    }

    public boolean isExpire() {
        return LocalDateTime.now().isAfter(this.expireLe);
    }

    public void marquerUtilise() {
        this.utilise = true;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getExpireLe() {
        return expireLe;
    }

    public boolean isUtilise() {
        return utilise;
    }

    public void setUtilise(boolean utilise) {
        this.utilise = utilise;
    }

    public LocalDateTime getCreeLe() {
        return creeLe;
    }
}
