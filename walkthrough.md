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

#### 5. Microservice Réunions & Séances (`ms-reunions`)
- **[ReunionsApplication.java](file:///d:/projet/njangi/ms-reunions/src/main/java/com/njangi/reunions/ReunionsApplication.java)** : Microservice de gestion des réunions, séances en direct, émargements et procès-verbaux sur le port standard **8004** avec `@EnableDiscoveryClient`.
- **Architecture en Couches Standard (GEMINI.md)** : Controller → Service → Repository avec records Java 25 et entités POJO sans Lombok.
- **Entités JPA (Schéma `reunions`)** :
  - [Reunion.java](file:///d:/projet/njangi/ms-reunions/src/main/java/com/njangi/reunions/entity/Reunion.java) : ID, groupeId, sessionTontineId, titre, dateReunion, lieuReunion, typeSiege (`FIXE` vs `ROTATIF`), hoteId, statut (`PLANIFIEE`, `EN_COURS`, `TERMINEE`, `ANNULEE`), ordreJour, compteRendu, présidence et trésorerie.
  - [Presence.java](file:///d:/projet/njangi/ms-reunions/src/main/java/com/njangi/reunions/entity/Presence.java) : reunionId, membreId, statut (`PRESENT`, `RETARD`, `EXCUSE`, `ABSENT`), heureArrivee, justification, procuration, mandataireId.
- **Cycle de Vie & Conduite de Séance en Direct** :
  - Démarrage direct (`PUT /api/v1/reunions/{id}/demarrer`) déclenchant l'événement Kafka `reunion.demarree`.
  - Clôture et archivage du PV (`PUT /api/v1/reunions/{id}/cloturer`) déclenchant `reunion.terminee`.
  - Émargement unitaire et appel nominatif par lot (`POST /api/v1/reunions/{id}/presences/batch`).
  - Calcul dynamique du quorum et statistiques de présence (`GET /api/v1/reunions/{id}/quorum`).
- **Événements Kafka** : [ReunionEventPublisher.java](file:///d:/projet/njangi/ms-reunions/src/main/java/com/njangi/reunions/event/ReunionEventPublisher.java) publiant sur `reunion.events` et `reunion.terminee`.
- **Documentation Swagger UI** : [OpenApiConfig.java](file:///d:/projet/njangi/ms-reunions/src/main/java/com/njangi/reunions/config/OpenApiConfig.java) accessible sur `http://localhost:8004/swagger-ui.html`.
- **Contrôleur REST** : [ReunionController.java](file:///d:/projet/njangi/ms-reunions/src/main/java/com/njangi/reunions/controller/ReunionController.java) avec réponses uniformes [ApiResponse.java](file:///d:/projet/njangi/ms-reunions/src/main/java/com/njangi/reunions/dto/ApiResponse.java).
- **Tests Unitaires** : [ReunionServiceTest.java](file:///d:/projet/njangi/ms-reunions/src/test/java/com/njangi/reunions/service/ReunionServiceTest.java) et [PresenceServiceTest.java](file:///d:/projet/njangi/ms-reunions/src/test/java/com/njangi/reunions/service/PresenceServiceTest.java) (**10/10 tests réussis**).

#### 6. Microservice Groupes, Sessions & Mandats (`ms-groupes`)
- **[GroupesApplication.java](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/GroupesApplication.java)** : Microservice de gestion des tontines, adhésions, sessions et mandats du bureau sur le port standard **8003** avec `@EnableDiscoveryClient`.
- **Architecture en Couches Standard (GEMINI.md)** : Controller → Service → Repository avec records Java 25 et entités POJO sans Lombok.
- **Entités JPA (Schéma `groupes`)** :
  - [Groupe.java](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/entity/Groupe.java) : ID, nom, description, typeSiege (`FIXE` vs `ROTATIF`), adresseSiege, codeInvitation, createurId, actif.
  - [GroupeMembre.java](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/entity/GroupeMembre.java) : groupeId, membreId, role (`CREATEUR`, `PRESIDENT`, `TRESORIER`, `SECRETAIRE`, `MEMBRE`, `AUDITEUR`), dateAdhesion, actif.
  - [SessionTontine.java](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/entity/SessionTontine.java) : Distinction clé **Session vs Mandat**, cycle annuel ou pluri-mensuel du pot (`PLANIFIEE`, `EN_COURS`, `CLOTUREE`), montantPotEstime, nbToursPrevu, unicité stricte de la session active par groupe.
  - [MandatBureau.java](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/entity/MandatBureau.java) : Gouvernance du bureau exécutif (président, trésorier, secrétaire), dates de début/fin, statut (`ACTIF`, `EXPIRE`, `REVOQUE`).
- **Règle Métier Critique — Démotion du Créateur** :
  - Le créateur dispose des privilèges initiaux pour paramétrer le groupe.
  - Dès l'élection du premier bureau (`POST /api/v1/groupes/{id}/bureau`), le créateur est **automatiquement rétrogradé en simple MEMBRE** dans `GroupeMembre` et un événement Kafka `bureau.elu` est émis.
- **Services Métier & Événements Kafka** :
  - [GroupeService.java](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/service/GroupeService.java) : Création de tontine, génération cryptographique de code d'invitation, adhésion par code, attribution de rôles.
  - [SessionTontineService.java](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/service/SessionTontineService.java) : Planification, démarrage avec contrôle d'unicité, clôture.
  - [MandatBureauService.java](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/service/MandatBureauService.java) : Élection, expiration automatique des anciens mandats, rétrogradation du créateur.
  - [GroupeEventPublisher.java](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/event/GroupeEventPublisher.java) : Publication sur `groupe.events`, `session.events`, `bureau.events`.
- **Documentation Swagger UI & Sécurité** :
  - [OpenApiConfig.java](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/config/OpenApiConfig.java) accessible sur `http://localhost:8003/swagger-ui.html`.
  - [SecurityConfig.java](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/config/SecurityConfig.java) : Stateless avec filtres de sécurité.
- **Contrôleurs REST** :
  - [GroupeController.java](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/controller/GroupeController.java) (`/api/v1/groupes`).
  - [SessionTontineController.java](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/controller/SessionTontineController.java) (`/api/v1/groupes/{groupeId}/sessions`).
  - [MandatBureauController.java](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/controller/MandatBureauController.java) (`/api/v1/groupes/{groupeId}/bureau`).
- **Tests Unitaires** : [GroupeServiceTest.java](file:///d:/projet/njangi/ms-groupes/src/test/java/com/njangi/groupes/service/GroupeServiceTest.java), [SessionTontineServiceTest.java](file:///d:/projet/njangi/ms-groupes/src/test/java/com/njangi/groupes/service/SessionTontineServiceTest.java), [MandatBureauServiceTest.java](file:///d:/projet/njangi/ms-groupes/src/test/java/com/njangi/groupes/service/MandatBureauServiceTest.java) (**10/10 tests réussis**).

#### 7. Microservice Cotisations & Cagnottes (`ms-cotisations`)
- **[CotisationsApplication.java](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/CotisationsApplication.java)** : Microservice de gestion des multi-cotisations simultanées, cycles de pot rotatif, appels de fonds et décaissements sur le port standard **8005** avec `@EnableDiscoveryClient`.
- **Architecture en Couches Standard (GEMINI.md)** : Controller → Service → Repository avec records Java 25 et entités POJO sans Lombok.
- **Entités JPA (Schéma `cotisations`)** :
  - [TypeCotisation.java](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/entity/TypeCotisation.java) : ID, groupeId, libelle, description, categorie (`ROTATIVE_POT`, `SECOURS_DECES`, `CAISSE_RESERVE`, `EVENEMENTIELLE`), montant en XAF, estRotatif, estObligatoire, statut.
  - [Cotisation.java](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/entity/Cotisation.java) : ID, groupeId, membreId, typeCotisationId, reunionId, montantDu, montantPaye, statut (`EN_ATTENTE`, `PARTIEL`, `PAYE`, `EN_RETARD`, `PENALISE`), dateLimitePaiement, datePaiement.
  - [PotSession.java](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/entity/PotSession.java) : ID, groupeId, sessionTontineId, reunionId, membreBeneficiaireId, ordrePassage, montantTotal, montantNet, statut (`PLANIFIE`, `ATTRIBUE`, `DECAISSE`, `ANNULE`), modeVersement (`CASH`, `MTN_MOMO`, `ORANGE_MONEY`, `VIREMENT`), referencePaiement, dateAttribution, dateVersement.
- **Multi-Cotisations Simultanées & Tour de Pot** :
  - Paramétrage flexible par groupe des différentes caisses (Cagnotte rotative, Secours mutuel, Réserve bloquée).
  - Planification complète du tour de pot par session avec ordre de passage des bénéficiaires.
  - Génération automatique par lot des cotisations dues pour tous les membres actifs lors d'une séance.
  - Gestion des versements partiels ou totaux avec transition automatique vers `PAYE` et publication Kafka.
  - Attribution du pot en séance et décaissement traçable par le trésorier (reçu ou référence MoMo).
- **Services Métier & Événements Kafka** :
  - [TypeCotisationService.java](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/service/TypeCotisationService.java) : Création, consultation et désactivation des caisses.
  - [CotisationService.java](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/service/CotisationService.java) : Génération par réunion, enregistrement de paiement, pointage des retards.
  - [PotSessionService.java](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/service/PotSessionService.java) : Planification du tour, attribution et décaissement du pot.
  - [CotisationEventPublisher.java](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/event/CotisationEventPublisher.java) : Publication sur `cotisation.events`, `cotisation.payee`, `pot.verse`.
- **Documentation Swagger UI & Sécurité** :
  - [OpenApiConfig.java](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/config/OpenApiConfig.java) accessible sur `http://localhost:8005/swagger-ui.html`.
  - [SecurityConfig.java](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/config/SecurityConfig.java) : Stateless avec filtres de sécurité.
- **Contrôleurs REST** :
  - [TypeCotisationController.java](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/controller/TypeCotisationController.java) (`/api/v1/cotisations/types`).
  - [CotisationController.java](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/controller/CotisationController.java) (`/api/v1/cotisations`).
  - [PotSessionController.java](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/controller/PotSessionController.java) (`/api/v1/cotisations/pots`).
- **Tests Unitaires** : [TypeCotisationServiceTest.java](file:///d:/projet/njangi/ms-cotisations/src/test/java/com/njangi/cotisations/service/TypeCotisationServiceTest.java), [CotisationServiceTest.java](file:///d:/projet/njangi/ms-cotisations/src/test/java/com/njangi/cotisations/service/CotisationServiceTest.java), [PotSessionServiceTest.java](file:///d:/projet/njangi/ms-cotisations/src/test/java/com/njangi/cotisations/service/PotSessionServiceTest.java) (**11/11 tests réussis**).

#### 8. Microservice Caisse & Paiements (`ms-paiements`)
- **[PaiementsApplication.java](file:///d:/projet/njangi/ms-paiements/src/main/java/com/njangi/paiements/PaiementsApplication.java)** : Microservice de gestion et traçabilité des règlements de cotisations sur le port standard **8006** avec `@EnableDiscoveryClient`.
- **Architecture en Couches Standard (GEMINI.md)** : Controller → Service → Repository avec records Java 25 et entités POJO sans Lombok.
- **Entités JPA (Schéma `paiements`)** :
  - [Paiement.java](file:///d:/projet/njangi/ms-paiements/src/main/java/com/njangi/paiements/entity/Paiement.java) : ID, cotisationId, membreId, groupeId, montant en XAF, modePaiement (`CASH`, `MTN_MOMO`, `ORANGE_MONEY`), cleIdempotence (unique), reference (unique), pieceJointeUrl, numeroTelephone, operateur, statut (`EN_ATTENTE_VALIDATION`, `EN_COURS`, `VALIDE`, `REJETE`, `ECHOUE`), validePar, dateValidation, commentaire.
- **Règles Métier Financières Strictes (GEMINI.md)** :
  - **Règle Cash & Preuve Obligatoire** : Tout versement en espèces exige impérativement une pièce jointe (reçu physique signé / photo du bordereau). Le paiement reste en attente (`EN_ATTENTE_VALIDATION`) jusqu'à validation formelle par le trésorier.
  - **Validation / Rejet Trésorier** : Le trésorier examine la preuve visuelle et valide (`VALIDE`) ou rejette (`REJETE`) le versement avec horodatage et justification.
  - **Mobile Money MTN & Orange** : Génération de références de transaction uniques, statut initial `EN_COURS`.
  - **Idempotence Obligatoire** : Clé d'idempotence unique protégeant contre les doubles paiements lors d'instabilité réseau 3G.
  - **Webhooks Opérateurs Idempotents** : Endpoint partenaire réconciliant les statuts de paiement avec prise en charge d'appels multiples sans altération d'état.
- **Services Métier & Événements Kafka** :
  - [PaiementService.java](file:///d:/projet/njangi/ms-paiements/src/main/java/com/njangi/paiements/service/PaiementService.java) : Initiation cash avec preuve obligatoire, validation trésorier, initiation Mobile Money, traitement webhook idempotent.
  - [PaiementEventPublisher.java](file:///d:/projet/njangi/ms-paiements/src/main/java/com/njangi/paiements/event/PaiementEventPublisher.java) : Publication sur `paiement.events` et `paiement.valide`.
- **Documentation Swagger UI & Sécurité** :
  - [OpenApiConfig.java](file:///d:/projet/njangi/ms-paiements/src/main/java/com/njangi/paiements/config/OpenApiConfig.java) accessible sur `http://localhost:8006/swagger-ui.html`.
  - [SecurityConfig.java](file:///d:/projet/njangi/ms-paiements/src/main/java/com/njangi/paiements/config/SecurityConfig.java) : Stateless avec filtres de sécurité.
- **Contrôleur REST** :
  - [PaiementController.java](file:///d:/projet/njangi/ms-paiements/src/main/java/com/njangi/paiements/controller/PaiementController.java) (`/api/v1/paiements/cash`, `/valider-cash`, `/mobile-money`, `/webhooks/{operateur}`).
- **Tests Unitaires** : [PaiementServiceTest.java](file:///d:/projet/njangi/ms-paiements/src/test/java/com/njangi/paiements/service/PaiementServiceTest.java) (**7/7 tests réussis**).

#### 9. Microservice Pénalités & Sanctions (`ms-penalites`)
- **[PenalitesApplication.java](file:///d:/projet/njangi/ms-penalites/src/main/java/com/njangi/penalites/PenalitesApplication.java)** : Microservice de gestion du barème disciplinaire, sanctions infligées en séance et recouvrement des amendes sur le port standard **8007** avec `@EnableDiscoveryClient`.
- **Architecture en Couches Standard (GEMINI.md)** : Controller → Service → Repository avec records Java 25 et entités POJO sans Lombok.
- **Entités JPA (Schéma `penalites`)** :
  - [TarificationPenalite.java](file:///d:/projet/njangi/ms-penalites/src/main/java/com/njangi/penalites/entity/TarificationPenalite.java) : ID, groupeId, typeInfraction (`RETARD_PAIEMENT`, `ABSENCE`, `RETARD_REUNION`, `NON_RESPECT_REGLES`, `TROUBLE_SEANCE`), montant en XAF, actif, contrainte d'unicité `(groupe_id, type_infraction)`.
  - [Penalite.java](file:///d:/projet/njangi/ms-penalites/src/main/java/com/njangi/penalites/entity/Penalite.java) : ID, membreId, groupeId, sessionId, reunionId, typeInfraction, montant en XAF, statut (`EN_ATTENTE`, `PAYEE`, `ANNULEE`), motif, datePaiement, creeLe.
- **Règles Métier Disciplinaires & Traçabilité** :
  - **Barème Paramétrable par Groupe** : Chaque tontine fixe librement ses amendes (ex: 1 000 XAF pour retard, 5 000 XAF pour absence, 2 500 XAF pour bavardage ou sonnerie de téléphone).
  - **Résolution Automatique du Tarif** : Lors de l'application d'une sanction, le montant est automatiquement résolu depuis le barème actif du groupe (ou ajusté manuellement par le bureau).
  - **Recouvrement & Encaissement** : Suivi des statuts d'amendes avec horodatage du règlement (`PAYEE`) ou dispense motivée par le bureau (`ANNULEE`).
  - **Écoute Automatique des Retards** : Détection des retards de paiement de cotisations via écoute Kafka sur `cotisation.events` et génération automatique de l'amende `RETARD_PAIEMENT`.
- **Services Métier & Événements Kafka** :
  - [TarificationService.java](file:///d:/projet/njangi/ms-penalites/src/main/java/com/njangi/penalites/service/TarificationService.java) : Définition, mise à jour et consultation des tarifs.
  - [PenaliteService.java](file:///d:/projet/njangi/ms-penalites/src/main/java/com/njangi/penalites/service/PenaliteService.java) : Application d'amende, paiement, annulation avec motif.
  - [PenaliteEventPublisher.java](file:///d:/projet/njangi/ms-penalites/src/main/java/com/njangi/penalites/event/PenaliteEventPublisher.java) : Publication sur `sanction.events` (`sanction.infligee`, `sanction.payee`, `sanction.annulee`).
  - [PenaliteEventConsumer.java](file:///d:/projet/njangi/ms-penalites/src/main/java/com/njangi/penalites/kafka/PenaliteEventConsumer.java) : Consommation des événements de retard sur `cotisation.events`.
- **Documentation Swagger UI & Sécurité** :
  - [OpenApiConfig.java](file:///d:/projet/njangi/ms-penalites/src/main/java/com/njangi/penalites/config/OpenApiConfig.java) accessible sur `http://localhost:8007/swagger-ui.html`.
  - [SecurityConfig.java](file:///d:/projet/njangi/ms-penalites/src/main/java/com/njangi/penalites/config/SecurityConfig.java) : Stateless avec filtres de sécurité.
- **Contrôleurs REST** :
  - [TarificationController.java](file:///d:/projet/njangi/ms-penalites/src/main/java/com/njangi/penalites/controller/TarificationController.java) (`/api/v1/penalites/tarifs`).
  - [PenaliteController.java](file:///d:/projet/njangi/ms-penalites/src/main/java/com/njangi/penalites/controller/PenaliteController.java) (`/api/v1/penalites`).
- **Tests Unitaires** : [TarificationServiceTest.java](file:///d:/projet/njangi/ms-penalites/src/test/java/com/njangi/penalites/service/TarificationServiceTest.java) et [PenaliteServiceTest.java](file:///d:/projet/njangi/ms-penalites/src/test/java/com/njangi/penalites/service/PenaliteServiceTest.java) (**9/9 tests réussis**).

#### 10. Microservice Notifications (`ms-notifications`)
- **[NotificationsApplication.java](file:///d:/projet/njangi/ms-notifications/src/main/java/com/njangi/notifications/NotificationsApplication.java)** : Microservice de distribution des notifications multi-canaux sur le port standard **8008** avec `@EnableDiscoveryClient`.
- **Architecture en Couches Standard (GEMINI.md)** : Controller → Service → Repository avec records Java 25 et entités POJO sans Lombok.
- **Entités JPA (Schéma `notifications`)** :
  - [Notification.java](file:///d:/projet/njangi/ms-notifications/src/main/java/com/njangi/notifications/entity/Notification.java) : ID, destinataireId, groupeId, canal (`PUSH`, `SMS`, `EMAIL`, `IN_APP`), type, titre, contenu, statut (`EN_ATTENTE`, `ENVOYEE`, `ECHEC`), lue, creeLe, envoyeeLe, referenceObjet, typeObjet, destinataireContact.
- **Multi-Canaux & Résilience** :
  - **Push FCM (Firebase Cloud Messaging)** : [FirebaseMessagingService.java](file:///d:/projet/njangi/ms-notifications/src/main/java/com/njangi/notifications/service/FirebaseMessagingService.java) avec initialisation résiliente et simulation transparente en environnement de développement / test.
  - **SMS Fallback** : [SmsService.java](file:///d:/projet/njangi/ms-notifications/src/main/java/com/njangi/notifications/service/SmsService.java) prenant en charge les numéros camerounais (`+237...`) et la diaspora E.164 pour les alertes critiques (retards de cotisation, convocations) en cas de réseau 3G dégradé.
  - **Email** : [EmailService.java](file:///d:/projet/njangi/ms-notifications/src/main/java/com/njangi/notifications/service/EmailService.java) via `JavaMailSender` avec simulation transparente.
  - **In-App** : Distribution instantanée pour affichage dans le centre de notifications web et mobile.
- **Consommateur d'Événements Kafka** :
  - [NotificationEventConsumer.java](file:///d:/projet/njangi/ms-notifications/src/main/java/com/njangi/notifications/event/NotificationEventConsumer.java) : Écoute et réagit automatiquement aux événements du domaine (`groupe.cree`, `membre.inscrit`, `cotisation.payee`, `cotisation.retard`, `penalite.appliquee`, `pot.verse`, `notification.demandee`).
- **Contrôleur REST & Swagger** :
  - [NotificationController.java](file:///d:/projet/njangi/ms-notifications/src/main/java/com/njangi/notifications/controller/NotificationController.java) (`/api/v1/notifications`, `/destinataire/{id}`, `/non-lues`, `/count-non-lues`, `/{id}/lue`, `/tout-lire`). Swagger sur `http://localhost:8008/swagger-ui.html`.
- **Tests Unitaires & MockMvc** : [NotificationServiceTest.java](file:///d:/projet/njangi/ms-notifications/src/test/java/com/njangi/notifications/service/NotificationServiceTest.java) et [NotificationControllerTest.java](file:///d:/projet/njangi/ms-notifications/src/test/java/com/njangi/notifications/controller/NotificationControllerTest.java) (**15/15 tests réussis**).

#### 11. Microservice Statistiques & Reporting (`ms-statistiques`)
- **[StatistiquesApplication.java](file:///d:/projet/njangi/ms-statistiques/src/main/java/com/njangi/statistiques/StatistiquesApplication.java)** : Microservice d'agrégation financière, reporting et bilans de trésorerie sur le port standard **8009** avec `@EnableDiscoveryClient`.
- **Architecture en Couches Standard (GEMINI.md)** : Controller → Service → Repository avec records Java 25 et entités POJO sans Lombok.
- **Entités JPA (Schéma `statistiques`)** :
  - [StatistiqueGroupe.java](file:///d:/projet/njangi/ms-statistiques/src/main/java/com/njangi/statistiques/entity/StatistiqueGroupe.java) : ID, groupeId, sessionId, totalCollecte, totalDecaisse, soldeCaisse, totalPenalites, totalCash, totalMomo, nbMembres, nbReunions, tauxParticipation, tauxPresence, calculeLe.
- **Reporting Financier & Ratios Clés** :
  - **Ventilation de Trésorerie** : Ségrégation stricte des flux en espèces (`totalCash`) et Mobile Money MTN/Orange (`totalMomo`).
  - **Équilibre de Caisse Immuable** : `soldeCaisse = totalCollecte + totalPenalites - totalDecaisse`.
  - **Taux de Recouvrement & Assiduité** : Calcul lissé du taux de présence aux séances et du taux de recouvrement des cotisations.
  - **Bilan Financier Consolidé** : Diagnostic automatique de santé financière (`EXCELLENTE`, `DÉFICITAIRE`, `ATTENTION_ASSIDUITE_FAIBLE`).
- **Consommateur d'Événements Kafka** :
  - [StatistiqueEventConsumer.java](file:///d:/projet/njangi/ms-statistiques/src/main/java/com/njangi/statistiques/event/StatistiqueEventConsumer.java) : Agrégation en temps réel sur `cotisation.payee`, `pot.verse`, `penalite.appliquee`, `reunion.terminee`, `membre.inscrit`.
- **Contrôleur REST & Swagger** :
  - [StatistiqueController.java](file:///d:/projet/njangi/ms-statistiques/src/main/java/com/njangi/statistiques/controller/StatistiqueController.java) (`/api/v1/statistiques/groupe/{id}`, `/session/{id}`, `/calculer`, `/bilan`). Swagger sur `http://localhost:8009/swagger-ui.html`.
- **Tests Unitaires & MockMvc** : [StatistiqueServiceTest.java](file:///d:/projet/njangi/ms-statistiques/src/test/java/com/njangi/statistiques/service/StatistiqueServiceTest.java) et [StatistiqueControllerTest.java](file:///d:/projet/njangi/ms-statistiques/src/test/java/com/njangi/statistiques/controller/StatistiqueControllerTest.java) (**12/12 tests réussis**).

#### 12. Schémas PostgreSQL Isolés
- **[init-schemas.sql](file:///d:/projet/njangi/init-schemas.sql)** : 9 schémas isolés créés avec droits applicatifs (`auth`, `membres`, `groupes`, `reunions`, `cotisations`, `paiements`, `penalites`, `notifications`, `statistiques`).

---

## 2. Synthèse des Validations & Conformité GEMINI.md

| Module / Périmètre | Commande de Validation | Résultat | Conformité |
|---|---|---|---|
| **Microservice Auth** (`ms-auth/`) | `mvn test -pl ms-auth` | **BUILD SUCCESS** (7/7 tests passés, Port 8001, `@EnableDiscoveryClient`) | SSO, OTP, JJWT, Diaspora E.164, Swagger OpenAPI 3, OAuth2 Google & Facebook |
| **Microservice Groupes** (`ms-groupes/`) | `mvn test -pl ms-groupes` | **BUILD SUCCESS** (10/10 tests passés, Port 8003, `@EnableDiscoveryClient`) | Session vs Mandat, Siège Fixe/Rotatif, Démotion créateur, Swagger OpenAPI 3 |
| **Microservice Réunions** (`ms-reunions/`) | `mvn test -pl ms-reunions` | **BUILD SUCCESS** (10/10 tests passés, Port 8004, `@EnableDiscoveryClient`) | Séances direct, émargement, quorum, PV, Kafka, Swagger OpenAPI 3 |
| **Microservice Cotisations** (`ms-cotisations/`) | `mvn test -pl ms-cotisations` | **BUILD SUCCESS** (11/11 tests passés, Port 8005, `@EnableDiscoveryClient`) | Multi-cotisations, Tour de pot, Décaissement Cash/MoMo, Swagger OpenAPI 3 |
| **Microservice Paiements** (`ms-paiements/`) | `mvn test -pl ms-paiements` | **BUILD SUCCESS** (7/7 tests passés, Port 8006, `@EnableDiscoveryClient`) | Cash avec reçu signé, MoMo MTN/Orange, Idempotence, Webhooks, Swagger OpenAPI 3 |
| **Microservice Pénalités** (`ms-penalites/`) | `mvn test -pl ms-penalites` | **BUILD SUCCESS** (9/9 tests passés, Port 8007, `@EnableDiscoveryClient`) | Barème d'amendes, sanctions en séance, écoute retards, Swagger OpenAPI 3 |
| **Microservice Notifications** (`ms-notifications/`) | `mvn test -pl ms-notifications` | **BUILD SUCCESS** (15/15 tests passés, Port 8008, `@EnableDiscoveryClient`) | Push FCM, SMS fallback E.164/+237, Email JavaMail, In-App, Kafka Consumer, Swagger 3 |
| **Microservice Statistiques** (`ms-statistiques/`) | `mvn test -pl ms-statistiques` | **BUILD SUCCESS** (12/12 tests passés, Port 8009, `@EnableDiscoveryClient`) | Reporting financier, caisse cash vs momo, bilans consolidés, Kafka Consumer, Swagger 3 |
| **Eureka Server** (`eureka-server/`) | `mvn compile -pl eureka-server` | **BUILD SUCCESS** (Port 8761, `@EnableEurekaServer`) | Serveur de découverte Netflix Eureka |
| **API Gateway** (`api-gateway/`) | `mvn compile -pl api-gateway` | **BUILD SUCCESS** (Port 9090, `@EnableDiscoveryClient`) | Virtual Threads, ProblemDetail RFC 9457, Eureka Client |
| **Microservice Membres** (`ms-membres/`) | `mvn compile -pl ms-membres` | **BUILD SUCCESS** (Port 8002, `@EnableDiscoveryClient`) | Hexagonale pure & DDD, Swagger / OpenAPI 3, Zéro Lombok, Flyway |
| **Multi-Module Backend** | `mvn compile -pl eureka-server,api-gateway,ms-auth,ms-membres,ms-groupes,ms-reunions,ms-cotisations,ms-paiements,ms-penalites,ms-notifications,ms-statistiques` | **BUILD SUCCESS** (11/11 modules compilés avec succès) | Réacteur Maven Spring Cloud 2023 / Boot 3.3 / Java 25 |
| **Frontend Web** (`njanki/`) | `npm run build` | **BUILD SUCCESS** (0 erreur, 11 routes SSR pré-rendues) | Signal Forms purs, Zéro Promise, 3 fichiers distincts, Tailwind, Google & Facebook |
| **Frontend Mobile** (`njanki-mobile/`) | `npm run build` | **BUILD SUCCESS** (0 erreur, bundle `www` généré) | Ionic 8, Capacitor Preferences & Network, Signal Forms, Zéro Promise, Google & Facebook |
| **Android Native APK** (`njanki-mobile/android/`) | `.\gradlew assembleDebug` | **BUILD SUCCESSFUL** (APK 4.4 MB généré) | Capacitor 8, Cleartext Gateway `http://10.0.2.2:9090`, Permissions 3G, Stockage & Caméra |
| **Authentification Sociale** | Google & Facebook OAuth2 JIT | **100% Conforme** | Just-In-Time provisioning + étape de liaison téléphone obligatoire |
| **Support Téléphonique** | Validation unitaire (`AuthServiceTest`) | **100% Conforme** | Cameroun (`+237...`) + Diaspora internationale E.164 (`+33...`, etc.) |
| **Bannissement des Promises** | Analyse statique | **100% Conforme** | Uniquement Angular Signals + RxJS |
| **Bannissement FormBuilder/ngModel** | Analyse statique | **100% Conforme** | Signal Forms exclusifs (`signal()` + `computed()`) |
| **Format Monétaire Camerounais** | Analyse statique | **100% Conforme** | Strictement `XAF` sans décimales |
| **Kafka Mode KRaft** | `docker compose config` | **100% Conforme** | Consensus Raft natif (`broker,controller`), ZooKeeper éliminé |
| **Suite E2E Bout-en-Bout** | `.\scripts\test-e2e-scenario.ps1` | **9/9 PHASES PASSÉES** | Inscription -> Siège Rotatif -> Bureau -> Séance -> Cash/MoMo -> Pot -> Bilan |

---

## 3. Détail de la Suite de Test E2E (9 Phases Validées)

1. **Phase 1 : Authentification & Sécurité (`ms-auth`)** : Inscription et émission de tokens JWT Bearer pour 4 profils distincts (`Créateur`, `Président`, `Trésorier`, `Secrétaire`).
2. **Phase 2 : Profils Membres DDD Hexagonal (`ms-membres`)** : Provisionnement des membres dans le domaine hexagonal avec respect de l'unicité téléphone camerounais/diaspora.
3. **Phase 3 : Gouvernance & Siège Rotatif (`ms-groupes`)** : Création d'une tontine avec siège tournant (`typeSiege: ROTATIF`), planification et démarrage de la session annuelle.
4. **Phase 4 : Multi-Cotisations Simultanées (`ms-cotisations`)** : Configuration des 3 caisses obligatoires (Pot rotatif 50 000 XAF + Secours 10 000 XAF + Réserve 5 000 XAF = 65 000 XAF/séance).
5. **Phase 5 : Élection du Bureau & Démotion du Créateur (`ms-groupes`)** : Élection du premier bureau et rétrogradation automatique du créateur en simple membre sans prérogative d'administration.
6. **Phase 6 : Séance Live, Quorum & Sanctions (`ms-reunions` / `ms-penalites`)** : Pointage des présences par lot (3 Présents, 1 Retard de 25 min), calcul de quorum (100%), et amende disciplinaire de 2 000 XAF.
7. **Phase 7 : Perception des Cotisations & Paiements (`ms-paiements`)** : Renseignement d'un paiement en espèces avec reçu signé validé par le trésorier, et paiement MTN MoMo avec clé d'idempotence.
8. **Phase 8 : Attribution & Décaissement du Pot (`ms-cotisations`)** : Planification du tour de passage, attribution de la cagnotte de 200 000 XAF au bénéficiaire #1 (Président) et décaissement cash sous décharge signée.
9. **Phase 9 : Clôture de Séance, PV & Bilan Consolidé (`ms-reunions` / `ms-statistiques`)** : Archivage du procès-verbal officiel et génération du bilan de trésorerie consolidé (Cash en caisse vs Mobile Money, équilibre comptable).

---

## 4. Démonstration Vidéo Réelle (Données Non-Mockées & Microservices Live)

La vidéo a été enregistrée de bout en bout avec **zéro mock**, orchestrée directement à travers la passerelle Spring Cloud API Gateway (Port 9090), les microservices Spring Boot 4 et la base PostgreSQL 17 native :

![Vidéo Démonstration Réelle Njangi](file:///C:/Users/fokou/.gemini/antigravity-ide/brain/1b4455f3-5eca-42cb-8e33-7e5ce2a0c2aa/njangi_demo_real.webm)

### Caractéristiques de l'enregistrement :
- **Fichier vidéo généré** : `njangi_demo_real.webm` (4.2 Mo, 1280x800)
- **Infrastructure sous-jacente active** :
  - **PostgreSQL 17** : Schemas `auth`, `membres`, `groupes`, `reunions`, `cotisations`, `paiements`, `penalites`, `statistiques` alimentés en données réelles camerounaises.
  - **Apache Kafka 4.1.2 KRaft** : Broker actif sur port 9092 avec diffusion des événements (`auth.events`, `session.events`, `cotisation.events`, etc.).
  - **Eureka Server** : Port 8761 (tous les microservices enregistrés et synchronisés).
  - **Spring MVC API Gateway** : Port 9090 avec routage dynamique et gestion des erreurs RFC 9457 `ProblemDetail`.
  - **Angular 22 Web** : Serveur SSR sur port 4000 connecté dynamiquement à `http://localhost:9090/api/v1`.
- **Parcours interactif filmé** :
  1. **Tableau de bord dynamique** : Démonstration des 5 rôles métier (Président, Trésorier, Secrétaire, Membre, Créateur) avec bascule réactive sans rechargement.
  2. **Gestion des Groupes (`/groupes` & `/groupes/creer`)** : Visualisation des vraies tontines créées en base (*Solidarité Douala Akwa*, *Njangi Diaspora Yaoundé*, etc.).
  3. **Séances Live & Présences (`/reunions`)** : Déroulement de la réunion mensuelle réelle avec appel et ordre du jour officiel.
  4. **Caisse & Règlements (`/paiements`)** : Enregistrement cash avec obligation de pièce jointe (reçu signé) et paiements Mobile Money MTN/Orange.
  5. **Bilan de Trésorerie (`/statistiques`)** : Visualisation des agrégats financiers et ratios de recouvrement calculés en temps réel par `ms-statistiques`.


