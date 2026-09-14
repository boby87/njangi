package com.njangi.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "token_otp", schema = "auth")
public class TokenOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String identifiant;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(name = "expire_le", nullable = false)
    private LocalDateTime expireLe;

    @Column(nullable = false)
    private boolean utilise = false;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    public TokenOtp() {
    }

    public TokenOtp(String identifiant, String code, LocalDateTime expireLe) {
        this.identifiant = identifiant;
        this.code = code;
        this.expireLe = expireLe;
        this.utilise = false;
        this.creeLe = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.creeLe == null) {
            this.creeLe = LocalDateTime.now();
        }
    }

    public boolean isExpire() {
        return LocalDateTime.now().isAfter(this.expireLe);
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

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpireLe() {
        return expireLe;
    }

    public void setExpireLe(LocalDateTime expireLe) {
        this.expireLe = expireLe;
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

    public void setCreeLe(LocalDateTime creeLe) {
        this.creeLe = creeLe;
    }
}
