# GEMINI.md — Njangi Platform
> Monorepo — `tontine-app/`  
> **Plateforme de Gestion de Tontine Camerounaise (Njangi)**

---

## 1. CONTEXTE DU PROJET

Digitalisation de la gouvernance communautaire et de l'épargne rotative camerounaise (**Njangi**)[cite: 1] : réunions, sanctions, ordre de passage, multi-cotisations simultanées (pot rotatif, secours/décès, caisse de réserve)[cite: 1].

| Sous-projet | Dossier | Stack | Rôle |
|---|---|---|---|
| Backend | `backend/` | Java 25 · Spring Boot 4 · Eureka · Kafka | Microservices métier, transactions, notifications |
| Frontend Web | `frontendWeb/` | Angular 22 · Tailwind CSS | Portail web complet (dashboards rôles, gestion) |
| Mobile | `frontendMobile/` | Ionic 8 · Angular 22 · Tailwind CSS · Capacitor | Application Android-first (offline partiel 3G) |

**Acteurs & Rôles dynamiques (un utilisateur a un rôle par groupe)[cite: 1] :**
- **Créateur** — droits techniques initiaux, rétrogradé en membre simple dès l'élection du premier bureau[cite: 1]
- **Président** — ordre du jour, sanctions, conduite des séances, nomination du bureau[cite: 1]
- **Trésorier** — enregistrement cash avec preuve, validations Mobile Money, gestion caisse[cite: 1]
- **Secrétaire** — comptes-rendus PDF, présences, convocations[cite: 1]
- **Membre / Auditeur** — cotisations, suivi du tour de pot, consultation des comptes[cite: 1]

**Devise :** XAF (FCFA) uniquement[cite: 1] · **Téléphone :** `+237XXXXXXXXX`[cite: 1] · **Siège :** Fixe ou Rotatif[cite: 1]

---

## 2. SERVICES BACKEND & PORTS

| Service | Port | Schéma BDD | Rôle & Architecture |
|---|---|---|---|
| eureka-server | 8761 | — | Découverte & enregistrement des services |
| api-gateway | 9090 | — | Spring MVC Gateway, routage Eureka, Auth SSO/OTP[cite: 1] |
| ms-authentification | 8001 | `auth` | Auth SSO, OTP téléphone, sessions[cite: 1] |
| ms-membres | 8002 | `membres` | **Hexagonale & DDD pur** : profils, rôles par groupe[cite: 1] |
| ms-groupes | 8003 | `groupes` | En couches : groupes, sessions, mandats de bureau[cite: 1] |
| ms-reunions | 8004 | `reunions` | En couches : ordre du jour, présences, comptes-rendus[cite: 1] |
| ms-cotisations | 8005 | `cotisations` | En couches : types de cotisations, cycles, pot[cite: 1] |
| ms-paiements | 8006 | `paiements` | En couches : Cash (reçu obligatoire), MTN MoMo, Orange Money[cite: 1] |
| ms-penalites | 8007 | `penalites` | En couches : barème d'amendes, sanctions[cite: 1] |
| ms-notifications | 8008 | `notifications` | Push FCM, SMS fallback, Email[cite: 1] |
| ms-statistiques | 8009 | `statistiques` | Reporting, bilans, exports[cite: 1] |

Kafka topics : `auth.events` · `session.events` · `cotisation.events` · `paiement.events` · `sanction.events`

**Toutes les requêtes clientes transitent obligatoirement par la Gateway :**
- Web : `http://localhost:9090`
- Mobile (Émulateur Android) : `http://10.0.2.2:9090`
- Production (LuxVps) : `https://api.njangi.cm`[cite: 1]

---

## 3. RÈGLES COMMUNES — TOUS LES SOUS-PROJETS

- **TypeScript strict** : `strict: true` et `noImplicitAny: true`. Type `any` strictement interdit.
- **DTOs partagés** : interfaces TypeScript définies dans `shared/models/` mutualisées entre Web et Mobile.
- **Format de réponse API uniforme** : `{ "success": boolean, "message": string, "data": T, "errorCode": string }`
- **Format monétaire** : montant en XAF sans décimales (`10 000 XAF`).
- **Isolation des données** : instance PostgreSQL unique, mais **aucun accès inter-schémas direct**. Communications via Kafka ou Gateway + Eureka.
- **Sécurité financière** : traçabilité immuable de tout flux d'argent, clé d'idempotence obligatoire sur tout paiement/sanction.

---

## 4. BACKEND — Java 25 & Spring Boot 4

### Règles d'Architecture
- **`ms-membres` (DDD & Architecture Hexagonale pure) :**
  - Dossier `domain/` : logique métier pure, entités et value objects immuables, **zéro dépendance Spring/JPA**.
  - Dossier `application/` : cas d'usage (Use Cases), ports d'entrée et de sortie.
  - Dossier `infrastructure/` : adaptateurs JPA (schéma `membres`), contrôleurs REST, adaptateurs Kafka/Eureka.
- **Autres microservices (Architecture en couches standard) :**
  - Structure directe `Controller → Service → Repository` avec DTOs et Mappers.

### Standards Java 25 & Framework
- **Java 25 moderne** : `record` obligatoire pour tous les DTOs, événements Kafka et Value Objects. Virtual Threads activés systématiquement.
- **Service Discovery** : tout microservice s'enregistre auprès d'Eureka (`@EnableDiscoveryClient`). Pas d'URLs codées en dur.
- **Transactions** : `@Transactional` uniquement sur les méthodes de Service/Use Cases qui écrivent en base.
- **Erreurs** : RFC 9457 `ProblemDetail` pour les erreurs HTTP. Exceptions métier explicites (ex. `BureauDejaEluException`, `CotisationMontantInvalideException`).
- **Migrations BDD** : Flyway obligatoire par schéma. `ddl-auto=create` strictement interdit.

---

## 5. FRONTEND (WEB & MOBILE) — Angular 22 & Ionic 8

### Règles Strictes d'Implémentation (CRITIQUE)

1. **Séparation des fichiers (Zéro Inline) :**
   - **INTERDICTION** d'utiliser `template: '...'` ou `styles: ['...']`.
   - Chaque composant / page comportera **3 fichiers distincts** :
     - `[name].component.ts` (ou `.page.ts` sur mobile) : logique réactive
     - `[name].component.html` (ou `.page.html`) : structure HTML et classes Tailwind CSS
     - `[name].component.scss` (ou `.page.scss`) : styles SCSS spécifiques ou directives `@apply`
2. **Signal Forms UNIQUEMENT :**
   - **INTERDICTION** des `FormGroup`, `FormControl`, `FormBuilder` de `@angular/forms`.
   - **INTERDICTION** des Template-driven forms (`ngModel`).
   - Tout formulaire (adhésion, saisie de cotisation, vote, création de groupe) utilise exclusivement **Signal Forms** (état piloté par `signal()`, validation réactive via `computed()`).
3. **Bannissement des Promises JavaScript :**
   - **INTERDICTION** de `Promise`, `async/await`, `.then()`, `.catch()`.
   - Tout flux asynchrone (appels HTTP, timers, événements réseau) doit être orchestré via **Signals** ou **RxJS** (`toSignal`, `takeUntilDestroyed()`, pipe `async`).
4. **Styling : Tailwind CSS :**
   - Classes utilitaires Tailwind prioritaires pour la disposition, le responsive et les thèmes à fort contraste (WCAG 2.1 AA pour lisibilité extérieure sous fort soleil)[cite: 1].
5. **Réutilisation Maximale (DRY) :**
   - Tout composant visuel récurrent (badge de statut de cotisation, carte de tontine, sélecteur de montant FCFA, aperçu de preuve de paiement cash, modale d'amende) doit être placé dans un package partagé `shared/ui/` réutilisable par Angular Web et Ionic Mobile[cite: 1].

---

## 6. SPÉCIFICITÉS PAR PLATEFORME

### Frontend Web (`frontendWeb/`)
- Angular 22 Standalone Components, routage via `RouterLink` et `Router`.
- Tableaux de bord dynamiques basculant selon le rôle actif de l'utilisateur dans le groupe consulté (Vue Président, Vue Trésorier, Vue Membre)[cite: 1].

### Mobile Ionic 8 (`frontendMobile/`)
- Composants `<ion-*>` intégrés avec classes Tailwind. Navigation via `NavController`.
- Stockage sécurisé des tokens JWT via `@capacitor/preferences` (jamais `localStorage`).
- Mode dégradé / Offline-first partiel : détection réseau via `@capacitor/network`, mise en file d'attente locale (SQLite) pour consultation des statuts et saisies hors ligne en cas de réseau 3G instable[cite: 1].

---

## 7. MODÈLE MÉTIER & PAIEMENTS (NJANGI)

### Distinction Clé : Session vs Mandat[cite: 1]
- **Session** : cycle complet d'un tour de tontine (ex. session 2026-2027)[cite: 1]. Début, fin, attribution du pot par réunion[cite: 1].
- **Mandat** : période de fonction du bureau élu (Président, Trésorier, Secrétaire)[cite: 1]. Peut chevaucher plusieurs sessions[cite: 1].

### Enregistrement des Paiements[cite: 1]
- **Cash** : obligatoirement validé par le trésorier avec pièce jointe (reçu signé / photo)[cite: 1].
- **Mobile Money** : intégration Orange Money & MTN MoMo via module dédié et webhooks idempotents[cite: 1].
- Statuts de cotisation : `EN_ATTENTE` → `PAYE` / `EN_RETARD` → `PENALISE`[cite: 1].

---

## 8. NE JAMAIS FAIRE

| Interdit | Correct |
|---|---|
| `Promise`, `async/await`, `.then()` | Angular Signals + RxJS (`takeUntilDestroyed()`) |
| `FormGroup`, `FormControl`, `ngModel` | Signal Forms (`signal` + validation `computed`) |
| Templates ou styles inline (`template:`, `styles:`) | 3 fichiers séparés : `.ts`, `.html`, `.scss` |
| `any` en TypeScript | Typage strict ou `unknown` typé |
| Architecture hexagonale sur `groupes` ou `cotisations` | Architecture en couches standard (DDD réservé à `membres`)[cite: 1] |
| Jointures SQL directes entre schémas BDD | Événements Kafka ou appels REST via Gateway + Eureka |
| URLs backend en dur | `environment.apiUrl` via Gateway |
| Injection par constructeur | `inject()` au niveau propriété |
| `localStorage` sur Ionic Mobile | `@capacitor/preferences` |

---

## 9. SKILLS & MODULES AGENTIQUES

Avant d'exécuter une tâche, charger ou consulter la compétence / référence associée issue du scan du workspace :

### Frontend Web & Mobile (Angular 22 / Ionic 8)
| Domaine | Référence Skill / Guide |
|---|---|
| Guide Global Angular 22 & Bonnes pratiques | [.agents/skills/angular-developer/SKILL.md](file:///d:/projet/njangi/.agents/skills/angular-developer/SKILL.md) |
| Formulaires Signals (Signal Forms exclusifs) | [.agents/skills/angular-developer/references/signal-forms.md](file:///d:/projet/njangi/.agents/skills/angular-developer/references/signal-forms.md) |
| Réactivité, Signals & Linked Signal | [.agents/skills/angular-developer/references/signals-overview.md](file:///d:/projet/njangi/.agents/skills/angular-developer/references/signals-overview.md) · [linked-signal.md](file:///d:/projet/njangi/.agents/skills/angular-developer/references/linked-signal.md) · [resource.md](file:///d:/projet/njangi/.agents/skills/angular-developer/references/resource.md) |
| Styling & Tailwind CSS | [.agents/skills/angular-developer/references/tailwind-css.md](file:///d:/projet/njangi/.agents/skills/angular-developer/references/tailwind-css.md) |
| Routage & Navigation | [.agents/skills/angular-developer/references/define-routes.md](file:///d:/projet/njangi/.agents/skills/angular-developer/references/define-routes.md) · [route-guards.md](file:///d:/projet/njangi/.agents/skills/angular-developer/references/route-guards.md) |
| Injection de dépendances (`inject()`) | [.agents/skills/angular-developer/references/di-fundamentals.md](file:///d:/projet/njangi/.agents/skills/angular-developer/references/di-fundamentals.md) |

### Backend Microservices (Spring Boot 4 & Java 25)
| Domaine | Référence Skill |
|---|---|
| Architecture en couches (Controller / Service / DTO / Repo) | [.agents/skills/spring-boot-4/layered-architecture/SKILL.md](file:///d:/projet/njangi/.agents/skills/spring-boot-4/layered-architecture/SKILL.md) |
| Migrations BDD & Versioning Flyway | [.agents/skills/spring-boot-4/flyway-migrations/SKILL.md](file:///d:/projet/njangi/.agents/skills/spring-boot-4/flyway-migrations/SKILL.md) |
| Persistance, Repositories JPA & N+1 | [.agents/skills/spring-boot-4/spring-data-jpa/SKILL.md](file:///d:/projet/njangi/.agents/skills/spring-boot-4/spring-data-jpa/SKILL.md) |
| Transactions ACID & `@Transactional` | [.agents/skills/spring-boot-4/transactional-patterns/SKILL.md](file:///d:/projet/njangi/.agents/skills/spring-boot-4/transactional-patterns/SKILL.md) |
| Conventions REST API & Responses | [.agents/skills/spring-boot-4/rest-api-conventions/SKILL.md](file:///d:/projet/njangi/.agents/skills/spring-boot-4/rest-api-conventions/SKILL.md) |
| Gestion des erreurs RFC 9457 (`ProblemDetail`) | [.agents/skills/spring-boot-4/problem-details-rfc9457/SKILL.md](file:///d:/projet/njangi/.agents/skills/spring-boot-4/problem-details-rfc9457/SKILL.md) |
| Sécurité OAuth2 Resource Server & JWT | [.agents/skills/spring-boot-4/oauth2-resource-server/SKILL.md](file:///d:/projet/njangi/.agents/skills/spring-boot-4/oauth2-resource-server/SKILL.md) |
| Hypermedia & Spring HATEOAS | [.agents/skills/spring-boot-4/hateoas/SKILL.md](file:///d:/projet/njangi/.agents/skills/spring-boot-4/hateoas/SKILL.md) |
| Architecture Hexagonale & DDD (`ms-membres`) | Définie en Section 4 (`domain/`, `application/`, `infrastructure/`) |

### Métier Njangi & Plateforme Mobile
| Domaine | Référence |
|---|---|
| Règles métier Njangi (Rôles, Réunions, Cotisations, Sanctions) | [cahier_des_charges_tontine.md](file:///d:/projet/njangi/cahier_des_charges_tontine.md) |
| Outillage Mobile Android (Émulateurs, Builds, CLI) | Plugin `android-cli` ([SKILL.md](file:///C:/Users/fokou/.gemini/config/plugins/android-cli-plugin/skills/SKILL.md)) |