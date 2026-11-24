# GEMINI.md — Njangi Platform
> Monorepo — `njangi/`  
> **Plateforme de Gestion de Tontine Camerounaise (Njangi)**

---

## 1. CONTEXTE DU PROJET

Digitalisation de la gouvernance communautaire et de l'épargne rotative camerounaise (**Njangi**) : réunions en direct, sanctions disciplinaires, ordre de passage du pot, multi-cotisations simultanées (pot rotatif, secours/décès, caisse de réserve) et traçabilité financière immuable.

| Sous-projet | Dossier | Stack cible | Rôle |
|---|---|---|---|
| Backend | `eureka-server/`, `api-gateway/`, `ms-*/` | Java 25 · Spring Boot 4.0 · Spring Cloud 2025.1 · Eureka · Kafka | Microservices métier consolidés, transactions ACID, événements |
| Frontend Web | `njanki/` | Angular 22 · SSR · Tailwind CSS v4 | Portail web complet (dashboards par rôle, gestion) |
| Mobile | `njanki-mobile/` | Ionic 9 · Angular 22 · Tailwind CSS v4 · Capacitor 8 | Application Android-first (offline partiel 3G) |
| Partagé | `shared/` | TypeScript strict · Angular Standalone | Source unique de vérité pour DTOs (`models/`) et composants (`ui/`) |

**Acteurs & Rôles dynamiques (un utilisateur possède un rôle par groupe) :**
- **Créateur** — droits techniques initiaux, rétrogradé automatiquement en membre simple dès l'élection du premier bureau.
- **Président** — ordre du jour, sanctions, conduite des séances, nomination du bureau.
- **Trésorier** — enregistrement cash avec reçu signé obligatoire, validations Mobile Money, gestion de la caisse et décaissement du pot.
- **Secrétaire** — comptes-rendus PDF, émargement des présences, convocations.
- **Membre / Auditeur** — cotisations, suivi du tour de pot, consultation transparente des comptes.

**Devise :** XAF (FCFA) sans décimales · **Téléphone :** Cameroun (`+237XXXXXXXXX`) et Diaspora E.164 · **Siège :** Fixe ou Rotatif.

---

## 2. ARCHITECTURE CONSOLIDÉE — SERVICES BACKEND & PORTS

L'architecture backend est consolidée autour de **4 microservices métier cohérents** + **Eureka Server** + **API Gateway** (**6 services Spring Boot** au total, contre 11 initialement) afin d'optimiser la cohérence transactionnelle (ACID) et l'empreinte mémoire sur **LuxVps**.

| Service | Port | Schéma BDD | Périmètre & Architecture | Anciens modules fusionnés |
|---|---|---|---|---|
| `eureka-server` | `8761` | — | Découverte & enregistrement dynamique Netflix Eureka | `eureka-server` |
| `api-gateway` | `9090` | — | Spring MVC Gateway, routage dynamique Eureka (`lb://`), validation JWT OAuth2 | `api-gateway` |
| `ms-identite` | `8001` | `identite` | **Hexagonale & DDD pur** : Auth SSO/OAuth2, OTP E.164, JWT, profils membres, KYC, rôles par groupe | `ms-auth` + `ms-membres` |
| `ms-gouvernance` | `8002` | `gouvernance` | **En couches** : Groupes, siège fixe/rotatif, sessions, mandats de bureau, réunions live, présences, quorum, PV PDF | `ms-groupes` + `ms-reunions` |
| `ms-finances` | `8003` | `finances` | **En couches (ACID)** : Multi-cotisations, cycles, tour de pot, paiements Cash (reçu) / MTN MoMo / Orange Money, idempotence, barèmes et pénalités | `ms-cotisations` + `ms-paiements` + `ms-penalites` |
| `ms-engagement` | `8004` | `engagement` | **En couches (Consumer Kafka)** : Notifications multi-canaux (Push FCM, SMS fallback, Email, In-App) + Reporting, bilans, exports | `ms-notifications` + `ms-statistiques` |

### Contrat Kafka unifié (4 topics par domaine)
- `identite.events` — émis par `ms-identite` (`OTP_DEMANDE`, `UTILISATEUR_INSCRIT`, `MEMBRE_INSCRIT`, `ROLE_ASSIGNE`)
- `gouvernance.events` — émis par `ms-gouvernance` (`GROUPE_CREE`, `SESSION_DEMARREE`, `SESSION_CLOTUREE`, `BUREAU_ELU`, `REUNION_DEMARREE`, `REUNION_TERMINEE`)
- `finances.events` — émis par `ms-finances` (`COTISATION_PAYEE`, `COTISATION_RETARD`, `PAIEMENT_VALIDE`, `POT_VERSE`, `SANCTION_INFLIGEE`, `SANCTION_PAYEE`)
- `engagement.events` — émis par `ms-engagement` (`NOTIFICATION_ENVOYEE`, `BILAN_GENERE`)

### Routage client obligatoire via l'API Gateway (rétrocompatible `/api/v1/*`)
- Web : `http://localhost:9090`
- Mobile (Émulateur Android) : `http://10.0.2.2:9090`
- Production (LuxVps) : `https://api.njangi.cm`

| Préfixe public Gateway | Service cible (Eureka) |
|---|---|
| `/api/v1/auth/**`, `/api/v1/membres/**` | `lb://ms-identite` |
| `/api/v1/groupes/**`, `/api/v1/reunions/**` | `lb://ms-gouvernance` |
| `/api/v1/cotisations/**`, `/api/v1/paiements/**`, `/api/v1/penalites/**` | `lb://ms-finances` |
| `/api/v1/notifications/**`, `/api/v1/statistiques/**` | `lb://ms-engagement` |

---

## 3. RÈGLES COMMUNES — TOUS LES SOUS-PROJETS

- **TypeScript strict** : `strict: true` et `noImplicitAny: true`. Type `any` strictement interdit.
- **DTOs & UI partagés** : `shared/models/` et `shared/ui/` constituent l'unique source de vérité mutualisée entre `njanki/` et `njanki-mobile/` (aucune duplication divergente).
- **Format de réponse API uniforme** : `{ "success": boolean, "message": string, "data": T, "errorCode": string | null }`.
- **Format monétaire** : montant entier en XAF sans décimales (`10 000 XAF`).
- **Isolation des données** : instance PostgreSQL unique avec **4 schémas isolés** (`identite`, `gouvernance`, `finances`, `engagement`). **Aucun accès SQL inter-schémas direct** : communications inter-domaines exclusivement via Kafka ou appels REST via Eureka.
- **Sécurité financière & Auth** : traçabilité immuable de tout flux d'argent, clé d'idempotence obligatoire sur tout paiement/sanction, validation JWT stricte sur la Gateway et propagation de l'identité vers les microservices (aucun secret codé en dur).

---

## 4. BACKEND — Java 25 & Spring Boot 4

> **Socle actif :** Java 25 LTS (`--release 25`), Spring Boot `4.0.10`, Spring Cloud `2025.1.5` (Oakwood), Springdoc OpenAPI `3.0.3`, Jackson 3 (`tools.jackson`), Virtual Threads activés. Détails de la consolidation dans [ARCHITECTURE_MIGRATION.md](./ARCHITECTURE_MIGRATION.md).

### Règles d'Architecture
- **`ms-identite` (DDD & Architecture Hexagonale pure) :**
  - `domain/` : logique métier pure (entités, value objects immuables, ports), **zéro dépendance Spring/JPA/Lombok**.
  - `application/` : cas d'usage (Use Cases), commandes, DTOs.
  - `infrastructure/` : adaptateurs JPA (schéma `identite`), contrôleurs REST (`/api/v1/auth`, `/api/v1/membres`), sécurité JWT/BCrypt, adaptateurs Kafka/Eureka.
- **`ms-gouvernance`, `ms-finances`, `ms-engagement` (Architecture en couches standard) :**
  - Structure `controller → service → repository` avec DTOs (`record`) et Mappers, organisés par sous-domaine interne (ex. `cotisation/`, `paiement/`, `penalite/` dans `ms-finances`).

### Standards Java & Framework
- **Java moderne** : `record` obligatoire pour tous les DTOs, événements Kafka et Value Objects. Virtual Threads activés systématiquement (`spring.threads.virtual.enabled=true`). Zéro Lombok.
- **Service Discovery** : tout microservice s'enregistre auprès d'Eureka (`@EnableDiscoveryClient`). Pas d'URLs codées en dur dans le code Java.
- **Transactions** : `@Transactional` uniquement sur les méthodes de Service/Use Cases qui écrivent en base. Dans `ms-finances`, la validation d'un paiement et la mise à jour de la cotisation associée s'exécutent dans la même transaction ACID.
- **Erreurs** : RFC 9457 `ProblemDetail` systématique via `@RestControllerAdvice`. Exceptions métier explicites (ex. `BureauDejaEluException`, `CotisationMontantInvalideException`, `PreuveCashObligatoireException`).
- **Migrations BDD** : Flyway obligatoire par schéma (`ddl-auto=validate`). `ddl-auto=create` ou `update` strictement interdits.

---

## 5. FRONTEND (WEB & MOBILE) — Angular 22 & Ionic 9

### Règles Strictes d'Implémentation (CRITIQUE)

1. **Séparation des fichiers (Zéro Inline) :**
   - **INTERDICTION** d'utiliser `template: '...'` ou `styles: ['...']`.
   - Chaque composant / page comporte **3 fichiers distincts** : `.ts`, `.html`, `.scss`.
2. **Signal Forms UNIQUEMENT :**
   - **INTERDICTION** des `FormGroup`, `FormControl`, `FormBuilder` de `@angular/forms`.
   - **INTERDICTION** des Template-driven forms (`ngModel`).
   - Tout formulaire utilise exclusivement **Signal Forms** (état piloté par `signal()`, validation réactive via `computed()`).
3. **Bannissement des Promises JavaScript :**
   - **INTERDICTION** de `Promise`, `async/await`, `.then()`, `.catch()`.
   - Tout flux asynchrone (HTTP, Capacitor plugins via `from()`, timers, réseau) est orchestré via **Signals** ou **RxJS** (`toSignal`, `takeUntilDestroyed()`, pipe `async`).
4. **Styling : Tailwind CSS v4 :**
   - Classes utilitaires Tailwind prioritaires pour la disposition, le responsive et les thèmes à fort contraste (**WCAG 2.1 AA** pour lisibilité extérieure).
5. **Réutilisation Maximale (DRY) :**
   - Composants visuels récurrents (`amount-badge`, `status-badge`, `role-badge`, `proof-preview`, `penalty-modal`) et modèles centralisés dans `shared/` et consommés par `njanki/` et `njanki-mobile/`.

---

## 6. SPÉCIFICITÉS PAR PLATEFORME

### Frontend Web (`njanki/`)
- Angular 22 Standalone Components + SSR, routage via `RouterLink` et `Router` avec Route Guards fonctionnels.
- URL de la Gateway injectée via `environments/environment.ts` (jamais codée en dur).
- Tableaux de bord dynamiques basculant selon le rôle actif de l'utilisateur dans le groupe consulté.

### Mobile Ionic (`njanki-mobile/`)
- Ionic 9 (`<ion-*>`) + Angular 22 + Tailwind CSS v4 + Capacitor 8. Navigation via `NavController` / `Router`.
- Stockage sécurisé des tokens JWT via `@capacitor/preferences` (**jamais `localStorage`**).
- Mode dégradé / Offline-first partiel : détection réseau via `@capacitor/network`, file d'attente locale persistée et rejeu idempotent vers la Gateway dès retour du réseau 3G.

---

## 7. MODÈLE MÉTIER & PAIEMENTS (NJANGI)

### Distinction Clé : Session vs Mandat (`ms-gouvernance`)
- **Session** : cycle complet d'un tour de tontine (ex. session 2026-2027), début, fin, attribution du pot par réunion.
- **Mandat** : période de fonction du bureau élu (Président, Trésorier, Secrétaire). Peut chevaucher plusieurs sessions. L'élection du 1er bureau rétrograde automatiquement le Créateur en `MEMBRE`.

### Enregistrement des Paiements (`ms-finances`)
- **Cash** : obligatoirement validé par le trésorier avec pièce jointe (reçu signé / photo).
- **Mobile Money** : intégration Orange Money & MTN MoMo avec clé d'idempotence obligatoire et webhooks idempotents.
- Statuts de cotisation : `EN_ATTENTE` → `PARTIEL` / `PAYE` / `EN_RETARD` → `PENALISE`.

---

## 8. NE JAMAIS FAIRE

| Interdit | Correct |
|---|---|
| `Promise`, `async/await`, `.then()` | Angular Signals + RxJS (`from()`, `takeUntilDestroyed()`) |
| `FormGroup`, `FormControl`, `ngModel` | Signal Forms (`signal()` + validation `computed()`) |
| Templates ou styles inline (`template:`, `styles:`) | 3 fichiers séparés : `.ts`, `.html`, `.scss` |
| `any` en TypeScript | Typage strict ou `unknown` typé |
| Multiplier les micro-services fragmentés (> 4 métier) | Respecter les 4 Bounded Contexts (`identite`, `gouvernance`, `finances`, `engagement`) |
| Architecture hexagonale sur `gouvernance`, `finances` ou `engagement` | Architecture en couches standard (DDD pur réservé à `ms-identite`) |
| Jointures SQL directes entre schémas BDD | Événements Kafka unifiés ou appels REST via Eureka |
| `permitAll()` global en Gateway ou secrets en dur | OAuth2 Resource Server JWT + variables d'environnement |
| URLs backend en dur côté Frontend | `environment.apiUrl` via API Gateway |
| Injection par constructeur en Angular | `inject()` au niveau propriété |
| `localStorage` sur Ionic Mobile | `@capacitor/preferences` |

---

## 9. SKILLS & RÉFÉRENCES DU WORKSPACE

### Documentation Projet
- [README.md](./README.md) — Vue d'ensemble et démarrage rapide
- [DOCUMENTATION.md](./DOCUMENTATION.md) — Index complet de la documentation
- [ARCHITECTURE_MIGRATION.md](./ARCHITECTURE_MIGRATION.md) — Plan détaillé de consolidation 9 → 4 microservices & migration Java 25 / Boot 4
- [cahier_des_charges_tontine.md](./cahier_des_charges_tontine.md) — Spécifications fonctionnelles officielles
- [TESTING_E2E.md](./TESTING_E2E.md) — Guide des tests d'intégration E2E

### Skills & Guides Techniques
| Domaine | Référence |
|---|---|
| Angular 22 & Bonnes pratiques | [.agents/skills/angular-developer/SKILL.md](./.agents/skills/angular-developer/SKILL.md) |
| Signal Forms exclusifs | [.agents/skills/angular-developer/references/signal-forms.md](./.agents/skills/angular-developer/references/signal-forms.md) |
| Réactivité & Signals | [.agents/skills/angular-developer/references/signals-overview.md](./.agents/skills/angular-developer/references/signals-overview.md) |
| Styling Tailwind CSS | [.agents/skills/angular-developer/references/tailwind-css.md](./.agents/skills/angular-developer/references/tailwind-css.md) |
| Spring Boot Architecture en couches | [.agents/skills/spring-boot-4/layered-architecture/SKILL.md](./.agents/skills/spring-boot-4/layered-architecture/SKILL.md) |
| Flyway Migrations | [.agents/skills/spring-boot-4/flyway-migrations/SKILL.md](./.agents/skills/spring-boot-4/flyway-migrations/SKILL.md) |
| Spring Data JPA & Transactions | [.agents/skills/spring-boot-4/spring-data-jpa/SKILL.md](./.agents/skills/spring-boot-4/spring-data-jpa/SKILL.md) · [.agents/skills/spring-boot-4/transactional-patterns/SKILL.md](./.agents/skills/spring-boot-4/transactional-patterns/SKILL.md) |
| Conventions REST & RFC 9457 | [.agents/skills/spring-boot-4/rest-api-conventions/SKILL.md](./.agents/skills/spring-boot-4/rest-api-conventions/SKILL.md) · [.agents/skills/spring-boot-4/problem-details-rfc9457/SKILL.md](./.agents/skills/spring-boot-4/problem-details-rfc9457/SKILL.md) |
| Sécurité OAuth2 Resource Server | [.agents/skills/spring-boot-4/oauth2-resource-server/SKILL.md](./.agents/skills/spring-boot-4/oauth2-resource-server/SKILL.md) |
