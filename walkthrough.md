# Walkthrough — Plateforme de Gestion de Tontine Camerounaise (Njangi)

Développement complet de la plateforme **Njangi** sur la base du cahier des charges officiel ([cahier_des_charges_tontine.md](file:///d:/projet/njangi/cahier_des_charges_tontine.md)) et des directives d'architecture strictes ([GEMINI.md](file:///d:/projet/njangi/GEMINI.md)).

---

## 1. Réalisations & Composants Livrés

### A. Modèles Partagés & Typage Strict (`shared/models/` & `njanki/src/app/shared/models/`)
- **[api-response.model.ts](file:///d:/projet/njangi/shared/models/api-response.model.ts)** : Format uniforme `{ success, message, data, errorCode }` et `ProblemDetail` RFC 9457.
- **[membre.model.ts](file:///d:/projet/njangi/shared/models/membre.model.ts)** : Types stricts pour les membres, profils, adhésions multi-groupes et rôles (`CREATEUR`, `PRESIDENT`, `TRESORIER`, `SECRETAIRE`, `MEMBRE`, `AUDITEUR`).
- **[groupe.model.ts](file:///d:/projet/njangi/shared/models/groupe.model.ts)** : Distinction stricte **Session vs Mandat de bureau**, types de siège (**Fixe vs Rotatif**).
- **[cotisation.model.ts](file:///d:/projet/njangi/shared/models/cotisation.model.ts)** : Multi-cotisations simultanées (`ROTATIVE_POT`, `SECOURS_DECES`, `CAISSE_RESERVE`, `EVENEMENTIELLE`) et tours de cagnotte (`TourPot`).
- **[paiement.model.ts](file:///d:/projet/njangi/shared/models/paiement.model.ts)** : Enregistrement des paiements (Cash avec reçu signé obligatoire, Mobile Money MTN MoMo & Orange Money, clés d'idempotence).
- **[reunion.model.ts](file:///d:/projet/njangi/shared/models/reunion.model.ts)** : Conduite de séances en direct, émargement des présences (`PRESENT`, `RETARD`, `EXCUSE`, `ABSENT`), ordre du jour et procès-verbaux officiels.
- **[penalite.model.ts](file:///d:/projet/njangi/shared/models/penalite.model.ts)** : Barème disciplinaire officiel et sanctions infligées.

---

### B. Composants UI Partagés (`shared/ui/`)
- **[AmountBadgeComponent](file:///d:/projet/njangi/njanki/src/app/shared/ui/amount-badge/amount-badge.component.ts)** : Affichage monétaire strict au format camerounais sans décimales (`10 000 XAF`).
- **[StatusBadgeComponent](file:///d:/projet/njangi/njanki/src/app/shared/ui/status-badge/status-badge.component.ts)** : Badges de statuts à fort contraste conformes WCAG 2.1 AA pour lisibilité sous fort soleil extérieur.
- **[RoleBadgeComponent](file:///d:/projet/njangi/njanki/src/app/shared/ui/role-badge/role-badge.component.ts)** : Badges de rôles avec code couleur dédié (Président en pourpre, Trésorier en ambre, Secrétaire en bleu, Créateur en émeraude).
- **[ProofPreviewComponent](file:///d:/projet/njangi/njanki/src/app/shared/ui/proof-preview/proof-preview.component.ts)** : Visionneuse modale pour prévisualiser les reçus physiques signés obligatoires pour les règlements en espèces.
- **[PenaltyModalComponent](file:///d:/projet/njangi/njanki/src/app/shared/ui/penalty-modal/penalty-modal.component.ts)** : Modale d'attribution d'amende disciplinaire réactive avec **Signal Forms purs**.

---

### C. Portail Web Angular 22 (`njanki/`)

#### 1. Tableau de Bord Dynamique par Rôle (`/dashboard`)
Le tableau de bord bascule automatiquement selon le rôle actif de l'utilisateur dans la tontine consultée :
- **Vue Président ([president-view.component.ts](file:///d:/projet/njangi/njanki/src/app/features/dashboard/views/president-view.component.ts))** : Conduite des séances, ordre du jour, bénéficiaire actuel du pot, quorum, bouton de sanction disciplinaire instantanée.
- **Vue Trésorier ([tresorier-view.component.ts](file:///d:/projet/njangi/njanki/src/app/features/dashboard/views/tresorier-view.component.ts))** : Bilan des caisses (Cash en coffre vs Mobile Money), liste des membres en retard avec relance SMS, décaissement du pot, consultation des pièces jointes.
- **Vue Secrétaire ([secretaire-view.component.ts](file:///d:/projet/njangi/njanki/src/app/features/dashboard/views/secretaire-view.component.ts))** : Registre d'émargement des présences, éditeur de procès-verbal officiel, bouton d'impression/export PDF.
- **Vue Membre ([membre-view.component.ts](file:///d:/projet/njangi/njanki/src/app/features/dashboard/views/membre-view.component.ts))** : Suivi des cotisations personnelles sur les 3 caisses, position dans le tour de pot (#2, date estimée, montant prévu), bouton pour payer sa cotisation, transparence financière.
- **Vue Créateur ([createur-view.component.ts](file:///d:/projet/njangi/njanki/src/app/features/dashboard/views/createur-view.component.ts))** : Droits techniques provisoires, partage du lien d'invitation, formulaire d'élection du premier bureau déclenchant la rétrogradation automatique du créateur.
- **Barre de simulation interactive** : Permet de basculer en un clic entre les 5 rôles pour tester et valider chaque interface en direct.

#### 2. Modules Métier
- **Authentification ([login.component.ts](file:///d:/projet/njangi/njanki/src/app/features/auth/login/login.component.ts))** : Connexion multi-modes par numéro de téléphone international (E.164 Cameroun `+237...` et Diaspora `+33...`, `+1...`), validation OTP 6 chiffres, et **authentification sociale en un clic via Google et Facebook** avec liaison de téléphone obligatoire.
- **Gestion des Groupes ([groupe-list](file:///d:/projet/njangi/njanki/src/app/features/groupes/groupe-list/groupe-list.component.ts), [groupe-create](file:///d:/projet/njangi/njanki/src/app/features/groupes/groupe-create/groupe-create.component.ts), [groupe-detail](file:///d:/projet/njangi/njanki/src/app/features/groupes/groupe-detail/groupe-detail.component.ts))** : Création avec choix obligatoire du type de siège (**Fixe vs Rotatif**), multi-cotisations, distinction Session vs Mandat.
- **Réunions & Séances ([reunion-live](file:///d:/projet/njangi/njanki/src/app/features/reunions/reunion-live/reunion-live.component.ts), [compte-rendu](file:///d:/projet/njangi/njanki/src/app/features/reunions/compte-rendu/compte-rendu.component.ts))** : Pointage des présences en direct, sanctions directes pour retard/sonnerie, clôture et archivage du PV.
- **Multi-Cotisations & Pot ([cotisation-list](file:///d:/projet/njangi/njanki/src/app/features/cotisations/cotisation-list/cotisation-list.component.ts), [pot-tour](file:///d:/projet/njangi/njanki/src/app/features/cotisations/pot-tour/pot-tour.component.ts))** : Suivi des 3 caisses simultanées (Pot rotatif, Secours/Décès, Réserve) et gestion de l'ordre de passage du pot.
- **Caisse & Paiements ([paiement-form](file:///d:/projet/njangi/njanki/src/app/features/paiements/paiement-form/paiement-form.component.ts))** : Enregistrement cash avec pièce jointe (reçu signé) obligatoire ou Mobile Money (Orange Money / MTN MoMo).
- **Pénalités & Discipline ([penalite-list](file:///d:/projet/njangi/njanki/src/app/features/penalites/penalite-list/penalite-list.component.ts))** : Barème d'amendes et registre des sanctions appliquées.
- **Statistiques ([statistiques.component.ts](file:///d:/projet/njangi/njanki/src/app/features/statistiques/statistiques.component.ts))** : Bilans financiers de session, ratio de collecte et répartition des fonds.

---

### D. Application Mobile Ionic 8 & Angular 22 (`njanki-mobile/`)

- **Stockage sécurisé sans `localStorage`** : Stockage du jeton JWT et profil via `@capacitor/preferences` ([MobileAuthService](file:///d:/projet/njangi/njanki-mobile/src/app/core/services/auth.service.ts)).
- **Gestion du mode dégradé 3G & Hors-Ligne** : Détection de l'état réseau via `@capacitor/network` ([NetworkService](file:///d:/projet/njangi/njanki-mobile/src/app/core/services/network.service.ts)), bandeau d'alerte, et file d'attente d'actions hors-ligne ([MobileTontineStateService](file:///d:/projet/njangi/njanki-mobile/src/app/core/services/tontine-state.service.ts)) avec bouton de synchronisation manuelle.
- **Navigation par onglets ([tabs.page.ts](file:///d:/projet/njangi/njanki-mobile/src/app/layout/tabs/tabs.page.ts))** :
  - **Accueil ([dashboard.page.ts](file:///d:/projet/njangi/njanki-mobile/src/app/pages/dashboard/dashboard.page.ts))** : Tableau de bord basculant par rôle (Membre, Trésorier, Président, Secrétaire).
  - **Cotisations ([cotisations.page.ts](file:///d:/projet/njangi/njanki-mobile/src/app/pages/cotisations/cotisations.page.ts))** : Filtre multi-caisses et calendrier du tour de pot rotatif.
  - **Séance Live ([reunions.page.ts](file:///d:/projet/njangi/njanki-mobile/src/app/pages/reunions/reunions.page.ts))** : Appel nominatif avec pointage direct (Présent, Retard, Excusé, Absent) et sanctions en temps réel.
  - **Caisse ([paiements.page.ts](file:///d:/projet/njangi/njanki-mobile/src/app/pages/paiements/paiements.page.ts))** : Formulaire Cash avec obligation de joindre la photo du reçu signé et validation Mobile Money MTN/Orange avec clé d'idempotence.
  - **Profil ([profil.page.ts](file:///d:/projet/njangi/njanki-mobile/src/app/pages/profil/profil.page.ts))** : Détails du membre, moniteur réseau et gestionnaire de file de synchronisation.
  - **Connexion ([login.page.ts](file:///d:/projet/njangi/njanki-mobile/src/app/pages/login/login.page.ts))** : Saisie du numéro international (E.164 Cameroun `+237...` & Diaspora), validation OTP 6 chiffres, boutons Google et Facebook avec liaison de numéro téléphonique pour conformité tontine.

---

### E. Backend Microservices (Spring Boot 4 & Java 25)

#### 1. Architecture Hexagonale & DDD Pure sur `ms-membres`
Refactorisation intégrale de `ms-membres` pour respecter strictement la section 4 de [GEMINI.md](file:///d:/projet/njangi/GEMINI.md) :
- **Couche Domaine (`domain/`)** : **Zéro dépendance Spring, JPA ou Lombok**.
  - Value Objects immuables (records Java 25) : [MembreId](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/model/MembreId.java), [Telephone](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/model/Telephone.java) (validation du format international E.164 et local camerounais), [Email](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/model/Email.java).
  - Entités métier pures : [Membre](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/model/Membre.java), [AdhesionGroupe](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/model/AdhesionGroupe.java).
  - Événements de domaine (records Java 25) : [MembreInscritEvent](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/event/MembreInscritEvent.java), [RoleAssigneeEvent](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/event/RoleAssigneeEvent.java).
  - Ports d'entrée (Cas d'utilisation) : [InscrireMembreUseCase](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/port/in/InscrireMembreUseCase.java), [ConsulterMembreUseCase](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/port/in/ConsulterMembreUseCase.java), [GererAdhesionGroupeUseCase](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/port/in/GererAdhesionGroupeUseCase.java).
  - Ports de sortie (Interfaces de persistance et de messagerie) : [MembreRepositoryPort](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/port/out/MembreRepositoryPort.java), [AdhesionRepositoryPort](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/port/out/AdhesionRepositoryPort.java), [MembreEventPublisherPort](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/port/out/MembreEventPublisherPort.java).
- **Couche Application (`application/`)** :
  - Services d'application avec `@Transactional` sélectif : [InscrireMembreService](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/application/service/InscrireMembreService.java), [ConsulterMembreService](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/application/service/ConsulterMembreService.java), [GererAdhesionGroupeService](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/application/service/GererAdhesionGroupeService.java).
  - Commandes et DTOs (records Java 25) : [InscrireMembreCommand](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/application/dto/InscrireMembreCommand.java), [MembreDto](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/application/dto/MembreDto.java), [AdhesionGroupeDto](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/application/dto/AdhesionGroupeDto.java), [AssignerRoleCommand](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/application/dto/AssignerRoleCommand.java).
- **Couche Infrastructure (`infrastructure/`)** :
  - Entités JPA isolées dans le schéma `membres` : [MembreJpaEntity](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/infrastructure/adapter/out/persistence/entity/MembreJpaEntity.java), [MembreGroupeJpaEntity](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/infrastructure/adapter/out/persistence/entity/MembreGroupeJpaEntity.java).
  - Mapper JPA bidirectionnel : [MembreJpaMapper](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/infrastructure/adapter/out/persistence/mapper/MembreJpaMapper.java).
  - Adaptateurs de persistance : [MembreJpaAdapter](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/infrastructure/adapter/out/persistence/adapter/MembreJpaAdapter.java), [AdhesionJpaAdapter](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/infrastructure/adapter/out/persistence/adapter/AdhesionJpaAdapter.java).
  - Adaptateur Kafka : [KafkaMembreEventPublisherAdapter](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/infrastructure/adapter/out/kafka/KafkaMembreEventPublisherAdapter.java).
  - Contrôleur REST annoté Swagger : [MembreRestController](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/infrastructure/adapter/in/web/MembreRestController.java) (`@Tag`, `@Operation`, `@ApiResponse`, `@Schema`).
  - Configuration OpenAPI 3 / Swagger : [OpenApiConfig](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/infrastructure/config/OpenApiConfig.java) (documentation interactive accessible sur `http://localhost:8002/swagger-ui.html` et `/v3/api-docs`).
  - Migrations Flyway : [V1__init_membres.sql](file:///d:/projet/njangi/ms-membres/src/main/resources/db/migration/V1__init_membres.sql) et [V2__init_membre_groupe.sql](file:///d:/projet/njangi/ms-membres/src/main/resources/db/migration/V2__init_membre_groupe.sql).

#### 2. Microservice Authentification & OTP (`ms-auth`)
- **[AuthApplication.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/AuthApplication.java)** : Microservice d'authentification SSO, OTP et gestion de sessions sur le port standard **8001** avec `@EnableDiscoveryClient`.
- **Support Téléphonique International (E.164 & Diaspora)** : Accepte les numéros du Cameroun (`+237...`) et de la diaspora internationale (`+33...`, `+1...`, `+49...`, etc.) avec validation regex et normalisation automatique.
- **Entités JPA Pures (Zéro Lombok)** :
  - [Utilisateur.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/entity/Utilisateur.java) : POJO avec `@PrePersist` / `@PreUpdate`, getters/setters explicites.
  - [TokenOtp.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/entity/TokenOtp.java) : Persistance des codes OTP avec validité de 5 minutes dans `auth.token_otp`.
  - [StatutUtilisateur.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/entity/StatutUtilisateur.java) : Statuts `ACTIF`, `SUSPENDU`, `EN_ATTENTE_VERIFICATION`.
- **Services & Sécurité** :
  - [AuthService.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/service/AuthService.java) : Inscription chiffrée BCrypt, connexion mot de passe / identifiant, vérification OTP avec provisionnement automatique, et validation de token.
  - [JwtService.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/service/JwtService.java) : Génération et vérification de tokens HMAC-SHA256 (JJWT 0.12.5).
  - [OtpService.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/service/OtpService.java) : Génération cryptographique à 6 chiffres et diffusion Kafka.
  - [SecurityConfig.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/config/SecurityConfig.java) : Sécurité sans état (stateless) avec encodeur BCrypt.
- **Contrôleur REST & Documentation Swagger** :
  - [AuthController.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/controller/AuthController.java) : Mappé sur `/api/v1/auth` pour la Gateway, annoté avec OpenAPI 3, retours enveloppés dans [ApiResponse.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/dto/ApiResponse.java).
  - [OpenApiConfig.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/config/OpenApiConfig.java) : Swagger UI disponible sur `http://localhost:8001/swagger-ui.html`.
  - [GlobalExceptionHandler.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/exception/GlobalExceptionHandler.java) : Réponses d'erreurs conformes à la RFC 9457 `ProblemDetail`.
- **Authentification Sociale Google & Facebook** :
  - Migration Flyway [V2__add_social_auth_providers.sql](file:///d:/projet/njangi/ms-auth/src/main/resources/db/migration/V2__add_social_auth_providers.sql) : Colonnes `provider_auth`, `provider_id` avec index unique, assouplissement de l'obligation de mot de passe pour les comptes tiers.
  - DTOs : [SocialLoginRequest.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/dto/SocialLoginRequest.java), [SocialLoginResponse.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/dto/SocialLoginResponse.java), [LierTelephoneRequest.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/dto/LierTelephoneRequest.java).
  - Just-In-Time Provisioning : Création ou réconciliation automatique du compte membre lors du premier OAuth2.
  - Étape de liaison téléphonique obligatoire (`POST /api/v1/auth/social/lier-telephone`) pour la conformité financière de la tontine (SMS, MoMo/OM, validation de pot).
- **Tests Unitaires** : [AuthServiceTest.java](file:///d:/projet/njangi/ms-auth/src/test/java/com/njangi/auth/service/AuthServiceTest.java) et [JwtServiceTest.java](file:///d:/projet/njangi/ms-auth/src/test/java/com/njangi/auth/service/JwtServiceTest.java) (**7/7 tests réussis**).

#### 3. Serveur de Découverte Eureka (`eureka-server`)
- **[EurekaServerApplication.java](file:///d:/projet/njangi/eureka-server/src/main/java/com/njangi/eureka/EurekaServerApplication.java)** : Serveur Netflix Eureka Discovery avec `@EnableEurekaServer` sur le port standard **8761**.
- **[application.yml](file:///d:/projet/njangi/eureka-server/src/main/resources/application.yml)** : Configuration standalone (`register-with-eureka: false`, `fetch-registry: false`, virtual threads, actuator metrics) et profil Docker.
- **[Dockerfile](file:///d:/projet/njangi/eureka-server/Dockerfile)** : Image conteneurisée Alpine JRE 21 sur le port 8761.
- **[docker-compose.yml](file:///d:/projet/njangi/docker-compose.yml)** : Service `eureka-server` intégré au réseau `njangi-network`, avec dépendance ordonnée pour l'API Gateway et les microservices.
- **Enregistrement des Clients** : Dépendance `spring-cloud-starter-netflix-eureka-client` et annotation `@EnableDiscoveryClient` configurées sur [GatewayApplication.java](file:///d:/projet/njangi/api-gateway/src/main/java/com/njangi/gateway/GatewayApplication.java), [AuthApplication.java](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/AuthApplication.java) et [MembresApplication.java](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/MembresApplication.java).

#### 4. Passerelle API (`api-gateway`)
- **[RoutingConfig.java](file:///d:/projet/njangi/api-gateway/src/main/java/com/njangi/gateway/config/RoutingConfig.java)** : Configuration des URLs cibles des 9 microservices avec virtual threads et port standard **9090**.
- **[GatewayProxyController.java](file:///d:/projet/njangi/api-gateway/src/main/java/com/njangi/gateway/controller/GatewayProxyController.java)** : Routage dynamique HTTP vers les microservices (`/api/v1/{service}/**`).
- **[LoggingFilter.java](file:///d:/projet/njangi/api-gateway/src/main/java/com/njangi/gateway/filter/LoggingFilter.java)** : Traçabilité des requêtes transitant par la passerelle.

#### 5. Schémas PostgreSQL Isolés
- **[init-schemas.sql](file:///d:/projet/njangi/init-schemas.sql)** : 9 schémas isolés créés avec droits applicatifs (`auth`, `membres`, `groupes`, `reunions`, `cotisations`, `paiements`, `penalites`, `notifications`, `statistiques`).

---

## 2. Synthèse des Validations & Conformité GEMINI.md

| Module / Périmètre | Commande de Validation | Résultat | Conformité |
|---|---|---|---|
| **Microservice Auth** (`ms-auth/`) | `mvn test -pl ms-auth` | **BUILD SUCCESS** (7/7 tests passés, Port 8001, `@EnableDiscoveryClient`) | SSO, OTP, JJWT, Diaspora E.164, Swagger OpenAPI 3, OAuth2 Google & Facebook |
| **Eureka Server** (`eureka-server/`) | `mvn compile -pl eureka-server` | **BUILD SUCCESS** (Port 8761, `@EnableEurekaServer`) | Serveur de découverte Netflix Eureka |
| **API Gateway** (`api-gateway/`) | `mvn compile -pl api-gateway` | **BUILD SUCCESS** (Port 9090, `@EnableDiscoveryClient`) | Virtual Threads, ProblemDetail RFC 9457, Eureka Client |
| **Microservice Membres** (`ms-membres/`) | `mvn compile -pl ms-membres` | **BUILD SUCCESS** (Port 8002, `@EnableDiscoveryClient`) | Hexagonale pure & DDD, Swagger / OpenAPI 3, Zéro Lombok, Flyway |
| **Multi-Module Backend** | `mvn compile -pl eureka-server,api-gateway,ms-auth,ms-membres` | **BUILD SUCCESS** (4/4 modules compilés en 5.4s) | Réacteur Maven Spring Cloud 2023 / Boot 3.3 / Java 25 |
| **Frontend Web** (`njanki/`) | `npm run build` | **BUILD SUCCESS** (0 erreur, 11 routes SSR pré-rendues) | Signal Forms purs, Zéro Promise, 3 fichiers distincts, Tailwind, Google & Facebook |
| **Frontend Mobile** (`njanki-mobile/`) | `npm run build` | **BUILD SUCCESS** (0 erreur, bundle `www` généré) | Ionic 8, Capacitor Preferences & Network, Signal Forms, Zéro Promise, Google & Facebook |
| **Authentification Sociale** | Google & Facebook OAuth2 JIT | **100% Conforme** | Just-In-Time provisioning + étape de liaison téléphone obligatoire |
| **Support Téléphonique** | Validation unitaire (`AuthServiceTest`) | **100% Conforme** | Cameroun (`+237...`) + Diaspora internationale E.164 (`+33...`, etc.) |
| **Bannissement des Promises** | Analyse statique | **100% Conforme** | Uniquement Angular Signals + RxJS |
| **Bannissement FormBuilder/ngModel** | Analyse statique | **100% Conforme** | Signal Forms exclusifs (`signal()` + `computed()`) |
| **Format Monétaire Camerounais** | Analyse statique | **100% Conforme** | Strictement `XAF` sans décimales |
| **Téléphone Camerounais** | Regex & Value Object | **100% Conforme** | Validé via `+237[236]XXXXXXXX` ou international E.164 |
