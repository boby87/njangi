# Documentation d'Architecture & Répertoire des Guides Njangi

Ce fichier centralise l'ensemble de la documentation technique, fonctionnelle et opérationnelle de la plateforme **Njangi**.

---

## 1. Documents Principaux

- **[README.md](./README.md)** : Vue d'ensemble du projet, architecture consolidée à 4 microservices métier (+ Eureka + Gateway), matrice des ports et démarrage rapide.
- **[GEMINI.md](./GEMINI.md)** : Directives strictes d'architecture et conventions de code (Signal Forms purs, Zéro Promise, DDD Hexagonal sur `ms-identite`, Virtual Threads, Java 25 / Spring Boot 4 cible).
- **[ARCHITECTURE_MIGRATION.md](./ARCHITECTURE_MIGRATION.md)** : Plan complet de consolidation des 9 anciens microservices vers les 4 microservices métier cibles, unification du contrat Kafka, durcissement sécurité JWT et montée vers Java 25 / Spring Boot 4.
- **[cahier_des_charges_tontine.md](./cahier_des_charges_tontine.md)** : Spécification fonctionnelle et métier officielle (règles Njangi, Session vs Mandat, Siège Fixe vs Rotatif, multi-cotisations, sanctions).
- **[TESTING_E2E.md](./TESTING_E2E.md)** : Guide des tests d'intégration bout-en-bout (9 phases fonctionnelles via API Gateway et Newman/Postman).
- **[SSO_GOOGLE_GUIDE.md](./SSO_GOOGLE_GUIDE.md)** : Guide complet d'intégration Google Sign-In OIDC (vérification RS256 JWKS sur `ms-identite`, Google Identity Services Web, Capacitor Android, configuration Google Cloud Console).
- **[walkthrough.md](./walkthrough.md)** : Historique de livraison initiale du prototype.
- **[init-schemas.sql](./init-schemas.sql)** & **[docker-compose.yml](./docker-compose.yml)** : Initialisation PostgreSQL et orchestration conteneurisée.

---

## 2. Cartographie Cible des Composants (Architecture Consolidée)

### A. Infrastructure Backend
- **Découverte Eureka (Port `8761`)** : `eureka-server/` — Registre dynamique Netflix Eureka.
- **API Gateway (Port `9090`)** : `api-gateway/` — Point d'entrée unique Spring MVC + Virtual Threads, validation JWT (OAuth2 Resource Server), routage dynamique `lb://` vers les 4 services métier tout en conservant les préfixes `/api/v1/*`.

### B. Les 4 Microservices Métier (Cible Java 25 & Spring Boot 4)

| Service cible | Port | Schéma BDD | Anciens services regroupés | Responsabilités clés |
|---|---:|---|---|---|
| **`ms-identite`** | `8001` | `identite` | `ms-auth` + `ms-membres` | **Architecture Hexagonale & DDD pur** : SSO/OAuth2 (Google, Facebook), OTP téléphone E.164/`+237`, émission JWT, profils membres, KYC, adhésions et rôles par groupe |
| **`ms-gouvernance`** | `8002` | `gouvernance` | `ms-groupes` + `ms-reunions` | Groupes de tontine, siège Fixe vs Rotatif, sessions annuelles, mandats de bureau (démotion auto du créateur), séances live, ordre du jour, présences par lot, quorum, PV PDF |
| **`ms-finances`** | `8003` | `finances` | `ms-cotisations` + `ms-paiements` + `ms-penalites` | Multi-cotisations simultanées, cycles & tour de pot, paiements Cash (reçu signé obligatoire) & Mobile Money (MTN MoMo, Orange Money, idempotence, webhooks), barèmes et sanctions disciplinaires en transactions ACID |
| **`ms-engagement`** | `8004` | `engagement` | `ms-notifications` + `ms-statistiques` | Consommateur Kafka centralisé : notifications multi-canaux (Push FCM, SMS fallback, Email, In-App) et reporting financier (trésorerie Cash vs MoMo, recouvrement, assiduité, bilans) |

### C. Portail Web (`njanki/`) — Angular 22 · SSR · Tailwind CSS v4
- Tableaux de bord dynamiques par rôle : `njanki/src/app/features/dashboard/views/`
- Modules métier : `auth/` (OTP + Google Sign-In OIDC), `groupes/`, `reunions/`, `cotisations/`, `paiements/`, `penalites/`, `statistiques/`
- Services réactifs : `njanki/src/app/core/services/`
- Modèles & UI réutilisables (`@shared/*`) : `njanki/src/app/shared/models/` et `njanki/src/app/shared/ui/` (`amount-badge`, `status-badge`, `role-badge`, `proof-preview`, `penalty-modal`).

> *Note : l'application mobile (`njanki-mobile/`) a été retirée temporairement du workspace (disponible dans l'historique Git au commit `8f1ffc4`).*

---

## 3. URLs Utiles en Développement Local

| Interface / Service | URL Locale |
|---|---|
| **Portail Web Angular** | `http://localhost:4200` |
| **Passerelle API (Gateway)** | `http://localhost:9090` |
| **Dashboard Eureka Server** | `http://localhost:8761` |
| **Swagger UI — `ms-identite`** | `http://localhost:8001/swagger-ui.html` |
| **Swagger UI — `ms-gouvernance`** | `http://localhost:8002/swagger-ui.html` |
| **Swagger UI — `ms-finances`** | `http://localhost:8003/swagger-ui.html` |
| **Swagger UI — `ms-engagement`** | `http://localhost:8004/swagger-ui.html` |

---

## 4. Outillage & Scripts

- [scripts/start-docker-infra.ps1](./scripts/start-docker-infra.ps1) : Démarrage vérifié de PostgreSQL, Kafka KRaft et Eureka.
- [scripts/test-e2e-scenario.ps1](./scripts/test-e2e-scenario.ps1) : Orchestration des 9 phases de validation E2E via l'API Gateway.
- [postman/njangi-e2e.postman_collection.json](./postman/njangi-e2e.postman_collection.json) & [postman/njangi-e2e.postman_environment.json](./postman/njangi-e2e.postman_environment.json) : Exécution automatisée via Newman (CI/CD).
