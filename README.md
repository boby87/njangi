# Plateforme Njangi — Gestion de Tontine Camerounaise & Diaspora

> **Digitalisation de la gouvernance communautaire et de l'épargne rotative camerounaise (Njangi)**  
> Réunions en direct, sanctions disciplinaires, ordre de passage du pot, multi-cotisations simultanées (pot rotatif, secours/décès, caisse de réserve) et traçabilité financière immuable.

---

## 1. Vue d'Ensemble du Projet

La plateforme **Njangi** digitalise les pratiques traditionnelles d'épargne rotative et de solidarité camerounaise tout en intégrant les réalités de la diaspora internationale :

- **Rôles dynamiques par groupe** : Un utilisateur possède un rôle spécifique dans chaque tontine à laquelle il adhère :
  - **Président** : Conduite des séances, validation de l'ordre du jour, quorum, sanctions disciplinaires instantanées.
  - **Trésorier** : Enregistrement cash avec photo obligatoire du reçu physique signé, validation Mobile Money (Orange Money / MTN MoMo), décaissement du pot.
  - **Secrétaire** : Émargement des présences, rédaction et publication des procès-verbaux (PV) officiels.
  - **Membre / Auditeur** : Suivi des cotisations, calendrier du tour de pot, transparence financière.
  - **Créateur** : Droits techniques d'initialisation, rétrogradé automatiquement en membre simple dès l'élection du premier bureau.
- **Distinction Session vs Mandat** :
  - **Session** : Cycle complet d'un tour de tontine (ex. 2026-2027) avec attribution du pot par réunion.
  - **Mandat** : Période de fonction du bureau exécutif élu, pouvant chevaucher plusieurs sessions.
- **Multi-Cotisations Simultanées** :
  - **Pot rotatif (Cagnotte)** : Montant fixe reversé à tour de rôle (ex. `50 000 XAF`).
  - **Caisse de Secours / Décès** : Fonds d'assistance mutuelle (ex. `5 000 XAF`).
  - **Caisse de Réserve** : Épargne collective bloquée (ex. `10 000 XAF`).
- **Support Téléphonique International (E.164 & Diaspora)** : Normalisation automatique des numéros camerounais (`+237...`) et prise en charge E.164 (`+33...`, `+1...`, `+49...`, etc.).
- **Devise** : **XAF (FCFA)** sans décimales.

---

## 2. Architecture Consolidée (4 Microservices Métier + Infra)

Pour garantir des transactions financières atomiques (ACID), éliminer les ruptures de contrats Kafka et réduire l'empreinte serveur sur **LuxVps**, l'architecture backend est consolidée de **11 services** à **6 services Spring Boot** (**4 microservices métier** + **Eureka Server** + **API Gateway**).

> **Socle technique actif :** Backend consolidé sur **Java 25**, **Spring Boot 4.0.10**, **Spring Cloud 2025.1.5** (Eureka) et **Springdoc 3.0.3**. Voir le détail dans **[ARCHITECTURE_MIGRATION.md](./ARCHITECTURE_MIGRATION.md)**.

```text
njangi/
├── eureka-server/          # Découverte Netflix Eureka (Port 8761)
├── api-gateway/            # Passerelle API Spring MVC, JWT OAuth2 & Eureka (Port 9090)
├── ms-identite/            # [DDD Hexagonal] Auth SSO/OTP/JWT + Profils Membres & Rôles (Port 8001)
├── ms-gouvernance/         # Groupes, Sessions, Mandats, Réunions live, Présences, PV (Port 8002)
├── ms-finances/            # Multi-cotisations, Tour de pot, Paiements Cash/MoMo, Pénalités (Port 8003)
├── ms-engagement/          # Notifications (FCM, SMS, Email, In-App) & Statistiques/Bilans (Port 8004)
├── njanki/                 # Portail Web — Angular 22 · SSR · Tailwind CSS v4 (inclut src/app/shared/)
├── postman/                # Collection E2E Postman / Newman
├── scripts/                # Scripts Docker, seed et tests E2E PowerShell
├── docker-compose.yml      # PostgreSQL 16, Kafka KRaft, Eureka, Gateway et 4 MS métier
└── init-schemas.sql        # Initialisation des 4 schémas PostgreSQL isolés
```

---

## 3. Matrice des Services, Ports & Routes Gateway

Toutes les requêtes clientes transitent **obligatoirement** par l'API Gateway :
- **Web (Local)** : `http://localhost:9090`
- **Production (LuxVps)** : `https://api.njangi.cm`

| Service | Port | Schéma BDD | Routes exposées via Gateway | Architecture | Swagger UI |
|---|---:|---|---|---|---|
| **eureka-server** | `8761` | — | — | Netflix Eureka Discovery | `http://localhost:8761` |
| **api-gateway** | `9090` | — | `/api/v1/**` | Spring MVC + Virtual Threads + JWT | `http://localhost:9090/actuator/health` |
| **ms-identite** | `8001` | `identite` | `/api/v1/auth/**`, `/api/v1/membres/**` | **Hexagonale pure & DDD** | `http://localhost:8001/swagger-ui.html` |
| **ms-gouvernance** | `8002` | `gouvernance` | `/api/v1/groupes/**`, `/api/v1/reunions/**` | En couches | `http://localhost:8002/swagger-ui.html` |
| **ms-finances** | `8003` | `finances` | `/api/v1/cotisations/**`, `/api/v1/paiements/**`, `/api/v1/penalites/**` | En couches (ACID) | `http://localhost:8003/swagger-ui.html` |
| **ms-engagement** | `8004` | `engagement` | `/api/v1/notifications/**`, `/api/v1/statistiques/**` | En couches (Consumer Kafka) | `http://localhost:8004/swagger-ui.html` |

### Contrat Kafka Unifié (4 Topics)
- `identite.events` : OTP, inscriptions, profils membres, assignation de rôles (`ms-identite` → `ms-engagement`).
- `gouvernance.events` : Création de groupe, cycles de sessions, élection du bureau, début/clôture de réunions (`ms-gouvernance` → `ms-engagement`, `ms-identite`).
- `finances.events` : Cotisations payées/en retard, paiements validés, décaissements du pot, sanctions disciplinaires (`ms-finances` → `ms-engagement`).
- `engagement.events` : Accusés d'envoi de notifications et génération de bilans consolidés (`ms-engagement`).

---

## 4. Frontend Web

### Portail Web (`njanki/`) — Angular 22 · SSR · Tailwind CSS v4
- **Architecture réactive stricte** ([GEMINI.md](./GEMINI.md)) : Zéro `Promise`, Signal Forms exclusifs (`signal()` + `computed()`), zéro inline (`.ts`/`.html`/`.scss`), zéro `any`, injection via `inject()`.
- **Tableau de bord dynamique par rôle (`/dashboard`)** : Vue Président, Vue Trésorier, Vue Secrétaire, Vue Membre, Vue Créateur.
- **Modules** : Authentification (OTP + Google Sign-In OIDC), Groupes, Réunions live & PV, Multi-cotisations & Tour de pot, Caisse/Paiements, Pénalités, Statistiques.

> *Note : le sous-projet mobile (`njanki-mobile/`) a été retiré temporairement du workspace (conservé dans l'historique Git au commit `8f1ffc4`) afin de concentrer le développement sur le backend et le portail Web.*

---

## 5. Guide de Démarrage Rapide

### Prérequis
- **Java 25 LTS** (Oracle JDK ou Eclipse Temurin)
- **Maven 3.9+**
- **Node.js 20+** & **npm 10+**
- **Docker & Docker Compose**

### Étape 1 : Démarrer l'Infrastructure (PostgreSQL, Kafka KRaft, Eureka)
```powershell
.\scripts\start-docker-infra.ps1
# ou :
docker compose up -d postgres kafka eureka-server
```

### Étape 2 : Compiler & Tester le Backend
```bash
mvn clean test
```

### Étape 3 : Lancer le Portail Web (`http://localhost:4200`)
```bash
cd njanki
npm install
npm start
```

---

## 6. Format Uniforme des Réponses d'API

### Succès
```json
{
  "success": true,
  "message": "Opération réussie",
  "data": { "...": "..." },
  "errorCode": null
}
```

### Erreur (RFC 9457 `ProblemDetail`)
```json
{
  "type": "https://api.njangi.cm/errors/bad-request",
  "title": "Requête invalide",
  "status": 400,
  "detail": "Le code OTP a expiré. Veuillez en demander un nouveau.",
  "timestamp": "2026-09-14T07:30:00Z"
}
```

---

## 7. Documentation Complète

Consultez **[DOCUMENTATION.md](./DOCUMENTATION.md)** pour l'index complet, **[ARCHITECTURE_MIGRATION.md](./ARCHITECTURE_MIGRATION.md)** pour le plan de consolidation des microservices et **[TESTING_E2E.md](./TESTING_E2E.md)** pour la suite de tests bout-en-bout.
