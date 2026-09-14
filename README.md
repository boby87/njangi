# Plateforme Njangi — Gestion de Tontine Camerounaise & Diaspora

> **Digitalisation de la gouvernance communautaire et de l'épargne rotative camerounaise (Njangi)**  
> Réunions en direct, sanctions disciplinaires, ordre de passage du pot, multi-cotisations simultanées (pot rotatif, secours/décès, caisse de réserve) et traçabilité financière immuable.

---

## 1. Vue d'Ensemble du Projet

La plateforme **Njangi** digitalise les pratiques traditionnelles d'épargne rotative et de solidarité camerounaise tout en intégrant les réalités de la diaspora internationale :

- **Rôles dynamiques par groupe** : Un utilisateur possède un rôle spécifique dans chaque tontine à laquelle il adhère :
  - **Président** : Conduite des séances, validation de l'ordre du jour, quorum, sanctions disciplinaires instantanées.
  - **Trésorier** : Enregistrement cash avec obligation de téléverser la photo du reçu physique signé, validation Mobile Money (Orange Money / MTN MoMo), décaissement du pot.
  - **Secrétaire** : Émargement des présences, rédaction et publication des procès-verbaux (PV) officiels.
  - **Membre / Auditeur** : Suivi des cotisations, calendrier du tour de pot, transparence financière.
  - **Créateur** : Droits techniques d'initialisation, rétrogradé en membre simple dès l'élection du premier bureau.
- **Distinction Session vs Mandat** :
  - **Session** : Cycle annuel complet d'un tour de tontine (ex. 2026-2027) avec attribution du pot par réunion.
  - **Mandat** : Période de fonction du bureau exécutif élu, pouvant chevaucher plusieurs sessions.
- **Multi-Cotisations Simultanées** :
  - **Pot rotatif (Cagnotte)** : Montant fixe reversé à tour de rôle (ex. `50 000 XAF`).
  - **Caisse de Secours / Décès** : Fonds d'assistance mutuelle (ex. `5 000 XAF`).
  - **Caisse de Réserve** : Épargne collective bloquée (ex. `10 000 XAF`).
- **Support Téléphonique International (E.164 & Diaspora)** :
  - Format local camerounais (`6XXXXXXXX`, `2XXXXXXXX`) normalisé automatiquement en `+237...`.
  - Numéros de la diaspora internationale au standard **E.164** (`+33...`, `+1...`, `+49...`, etc.).
- **Devise** : **XAF (FCFA)** sans décimales.

---

## 2. Architecture Technique & Cartographie des Services

Le projet est structuré sous forme de monorepo complet articulant Backend microservices, Portail Web et Application Mobile.

```
njangi/
├── eureka-server/          # Service de découverte Netflix Eureka (Port 8761)
├── api-gateway/            # Passerelle d'API Spring MVC & Virtual Threads (Port 9090)
├── ms-auth/                # Authentification SSO, OTP persistant, JJWT (Port 8001)
├── ms-membres/             # Profils, Rôles par groupe — Architecture Hexagonale & DDD (Port 8002)
├── ms-groupes/             # Groupes, Sessions, Mandats de bureau (Port 8003)
├── ms-reunions/            # Séances live, Émargement, PVs (Port 8004)
├── ms-cotisations/         # Multi-cotisations, Cycles, Attribution du Pot (Port 8005)
├── ms-paiements/           # Cash (reçu signé), MTN MoMo, Orange Money (Port 8006)
├── ms-penalites/           # Barème d'amendes et registre disciplinaire (Port 8007)
├── ms-notifications/       # Push FCM, SMS fallback, Email (Port 8008)
├── ms-statistiques/        # Rapports financiers et bilans de session (Port 8009)
├── njanki/                 # Portail Web — Angular 22 · SSR · Tailwind CSS v4
├── njanki-mobile/          # Application Mobile — Ionic 8 · Angular 22 · Capacitor
├── shared/                 # Modèles DTOs et interfaces TypeScript partagés
├── docker-compose.yml      # Orchestration PostgreSQL, Kafka, Eureka et microservices
└── init-schemas.sql        # Initialisation des 9 schémas PostgreSQL isolés
```

---

## 3. Matrice des Microservices & Ports

Toutes les requêtes applicatives clientes transitent **obligatoirement** par l'API Gateway :
- **Web** : `http://localhost:9090`
- **Mobile (Émulateur Android)** : `http://10.0.2.2:9090`
- **Production** : `https://api.njangi.cm`

| Service | Port | Schéma BDD | Rôle & Architecture | Documentation Swagger |
|---|---|---|---|---|
| **eureka-server** | **8761** | — | Serveur de découverte Netflix Eureka | `http://localhost:8761` (Dashboard) |
| **api-gateway** | **9090** | — | Routage dynamique, Actuator, Virtual Threads | `http://localhost:9090/actuator/health` |
| **ms-auth** | **8001** | `auth` | Auth SSO, OTP persistant (`auth.token_otp`), JJWT | `http://localhost:8001/swagger-ui.html` |
| **ms-membres** | **8002** | `membres` | **Hexagonale pure & DDD** : profils, rôles, téléphones | `http://localhost:8002/swagger-ui.html` |
| **ms-groupes** | **8003** | `groupes` | En couches : tontines, sessions, mandats, sièges | `http://localhost:8003/swagger-ui.html` |
| **ms-reunions** | **8004** | `reunions` | En couches : séances live, présences, PV officiel | `http://localhost:8004/swagger-ui.html` |
| **ms-cotisations** | **8005** | `cotisations` | En couches : caisses multiples, cycles, tour de pot | `http://localhost:8005/swagger-ui.html` |
| **ms-paiements** | **8006** | `paiements` | En couches : Cash (reçu obligatoire), MoMo/OM | `http://localhost:8006/swagger-ui.html` |
| **ms-penalites** | **8007** | `penalites` | En couches : barème d'amendes et sanctions | `http://localhost:8007/swagger-ui.html` |
| **ms-notifications**| **8008** | `notifications` | En couches : Push FCM, SMS fallback, Email | `http://localhost:8008/swagger-ui.html` |
| **ms-statistiques** | **8009** | `statistiques` | En couches : bilans de session, exports | `http://localhost:8009/swagger-ui.html` |

### Topics Kafka
- `auth.events` : Événements de génération d'OTP, d'inscription et de connexion.
- `session.events` : Cycle de vie des sessions et mandats.
- `cotisation.events` : Appels de fonds, attribution et décaissement du pot.
- `paiement.events` : Validation de règlements cash et notifications Mobile Money.
- `sanction.events` : Amendes disciplinaires infligées en réunion.

---

## 4. Frontend Web (`njanki/`) — Angular 22 & Tailwind CSS v4

Portail d'administration et de consultation complet conçu selon les directives strictes de **[GEMINI.md](file:///d:/projet/njangi/GEMINI.md)** :

- **Architecture Réactive Pure** :
  - **Zéro Promise** : Flux asynchrones gérés exclusivement par **Angular Signals** (`signal()`, `computed()`) et **RxJS** (`takeUntilDestroyed()`).
  - **Signal Forms Exclusifs** : Aucun usage de `FormGroup`, `FormControl` ou `ngModel`. Formulaires entièrement pilotés par des signaux et validations `computed()`.
  - **Zéro Inline** : 3 fichiers distincts par composant (`.component.ts`, `.component.html`, `.component.scss`).
- **Tableau de Bord Dynamique par Rôle (`/dashboard`)** :
  - **Vue Président** : Conduite des séances, ordre du jour, bénéficiaire du pot, sanctions disciplinaires.
  - **Vue Trésorier** : Bilan des caisses (espèces vs Mobile Money), relance des impayés, décaissement.
  - **Vue Secrétaire** : Registre d'émargement des présences, rédaction de PV, impression/export PDF.
  - **Vue Membre** : Suivi des 3 caisses personnelles, rang dans le pot (#2), paiement en ligne.
  - **Vue Créateur** : Partage d'invitation, formulaire d'élection du 1er bureau entraînant sa rétrogradation.
  - **Barre de simulation interactive** : Permet de basculer instantanément entre les 5 rôles pour valider les interfaces.
- **Composants UI Partagés (`shared/ui/`)** :
  - Badges de statuts contrastés WCAG 2.1 AA pour lisibilité sous fort soleil extérieur.
  - Sélecteur de montant monétaire strict en `XAF`.
  - Modale de prévisualisation du reçu signé pour le cash.
  - Modale d'amende disciplinaire réactive.

---

## 5. Application Mobile (`njanki-mobile/`) — Ionic 8 & Capacitor

Application Android-first optimisée pour une utilisation sur le terrain avec réseau 3G instable :

- **Sécurité des Tokens** : Stockage des jetons JWT via `@capacitor/preferences` (**zéro `localStorage`**).
- **Mode Dégradé & Offline-First Partiel** :
  - Détection réseau en temps réel via `@capacitor/network`.
  - File d'attente locale d'actions hors ligne avec bouton de synchronisation manuelle dès le retour de la connexion.
- **Navigation par Onglets** :
  - `Dashboard` : Adapté dynamiquement au rôle actif.
  - `Cotisations` : Multi-caisses simultanées et calendrier du pot rotatif.
  - `Séance Live` : Appel nominatif des présences et pénalités directes.
  - `Caisse` : Paiement cash avec pièce jointe obligatoire ou Mobile Money avec clé d'idempotence.
  - `Profil` : Moniteur réseau et gestionnaire de synchronisation.

---

## 6. Guide de Démarrage Rapide

### Prérequis
- **Java 21 ou 25** (Oracle JDK ou Eclipse Temurin)
- **Maven 3.8+**
- **Node.js 20+** & **npm 10+**
- **Docker & Docker Compose**

---

### Étape 1 : Démarrer l'Infrastructure Docker

Lance PostgreSQL (9 schémas isolés), ZooKeeper, Kafka et Eureka Server :

```bash
docker-compose up -d postgres kafka zookeeper eureka-server
```

---

### Étape 2 : Compiler & Tester le Backend

Compiler l'ensemble des modules Java :

```bash
mvn clean compile -pl eureka-server,api-gateway,ms-auth,ms-membres
```

Lancer les tests unitaires du service d'authentification :

```bash
mvn test -pl ms-auth
```

---

### Étape 3 : Lancer les Services Principaux

Ouvrir des terminaux distincts :

1. **Serveur Eureka (Port 8761) :**
   ```bash
   cd eureka-server && mvn spring-boot:run
   ```
   *Dashboard : http://localhost:8761*

2. **API Gateway (Port 9090) :**
   ```bash
   cd api-gateway && mvn spring-boot:run
   ```

3. **Service Authentification (Port 8001) :**
   ```bash
   cd ms-auth && mvn spring-boot:run
   ```
   *Swagger : http://localhost:8001/swagger-ui.html*

4. **Service Membres (Port 8002) :**
   ```bash
   cd ms-membres && mvn spring-boot:run
   ```
   *Swagger : http://localhost:8002/swagger-ui.html*

---

### Étape 4 : Lancer le Portail Web Angular

```bash
cd njanki
npm install
npm run dev
```
*Application accessible sur : **`http://localhost:4200`***

---

### Étape 5 : Lancer l'Application Mobile Ionic

```bash
cd njanki-mobile
npm install
npm start
```
*Application mobile accessible sur : **`http://localhost:8100`***

Pour exécuter sur un appareil ou émulateur Android :
```bash
npx cap sync android
npx cap open android
```

---

## 7. Format Uniforme des Réponses d'API

Tous les microservices exposent des réponses strictement conformes à la spécification :

### Succès
```json
{
  "success": true,
  "message": "Opération réussie",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "utilisateur": {
      "id": "7f7ecacd-fb0c-4ed4-aca1-7359c88e7ad5",
      "telephone": "+237699123456",
      "email": "jp.mbarga@njangi.cm",
      "nom": "Mbarga",
      "prenom": "Jean-Paul",
      "statut": "ACTIF"
    }
  },
  "errorCode": null
}
```

### Erreur (RFC 9457 ProblemDetail)
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

## 8. Règles d'Architecture Inviolables (Rappel GEMINI.md)

| Règle | Statut | Détail |
|---|---|---|
| **Bannissement des Promises** | Respecté à 100% | Exclusivement Signals et RxJS sur Web et Mobile |
| **Bannissement FormGroup / ngModel** | Respecté à 100% | Exclusivement Signal Forms (`signal()` + `computed()`) |
| **Zéro Inline (HTML/CSS)** | Respecté à 100% | 3 fichiers distincts par composant (`.ts`, `.html`, `.scss`) |
| **Architecture Hexagonale pure** | Respecté à 100% | Appliquée sur `ms-membres` (zéro Spring/JPA dans `domain/`) |
| **Isolation BDD** | Respecté à 100% | Aucun inter-schéma direct ; transit par Gateway ou Kafka |
| **Sécurité des Tokens Mobile** | Respecté à 100% | `@capacitor/preferences` exclusivement (aucun `localStorage`) |
| **Typage Strict** | Respecté à 100% | `strict: true`, aucun type `any` en TypeScript |
