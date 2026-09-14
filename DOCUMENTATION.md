# Documentation d'Architecture & Répertoire des Guides Njangi

Ce fichier référence l'ensemble de la documentation technique et fonctionnelle de la plateforme **Njangi**.

---

## 1. Documents Principaux à la Racine

- **[README.md](file:///d:/projet/njangi/README.md)** : Guide complet du projet, cartographie des microservices, matrice des ports, Swagger, démarrage Docker et Angular/Ionic.
- **[GEMINI.md](file:///d:/projet/njangi/GEMINI.md)** : Directives strictes d'implémentation, règles inviolables (Signal Forms purs, Zéro Promise, Architecture Hexagonale DDD sur `ms-membres`, virtual threads Java 25).
- **[cahier_des_charges_tontine.md](file:///d:/projet/njangi/cahier_des_charges_tontine.md)** : Spécification métier officielle (règles de tontine camerounaise, multi-cotisations, barème de sanctions, ordre de passage du pot).
- **[init-schemas.sql](file:///d:/projet/njangi/init-schemas.sql)** : Script d'initialisation des 9 schémas PostgreSQL isolés (`auth`, `membres`, `groupes`, `reunions`, `cotisations`, `paiements`, `penalites`, `notifications`, `statistiques`).
- **[docker-compose.yml](file:///d:/projet/njangi/docker-compose.yml)** : Définition des conteneurs (PostgreSQL, Kafka en mode KRaft natif sans ZooKeeper, Eureka Server, API Gateway, microservices métier).

---

## 2. Répartition par Composant

### A. Backend Microservices (Java 25 & Spring Boot 4)
- **Découverte Eureka (Port 8761)** : [`eureka-server/pom.xml`](file:///d:/projet/njangi/eureka-server/pom.xml) · [`EurekaServerApplication.java`](file:///d:/projet/njangi/eureka-server/src/main/java/com/njangi/eureka/EurekaServerApplication.java)
- **API Gateway (Port 9090)** : [`api-gateway/pom.xml`](file:///d:/projet/njangi/api-gateway/pom.xml) · [`GatewayProxyController.java`](file:///d:/projet/njangi/api-gateway/src/main/java/com/njangi/gateway/controller/GatewayProxyController.java)
- **Authentification, OTP & Social OAuth2 (Port 8001)** : [`ms-auth/pom.xml`](file:///d:/projet/njangi/ms-auth/pom.xml) · [`AuthController.java`](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/controller/AuthController.java) · [`AuthService.java`](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/service/AuthService.java) (Support Diaspora E.164, Google & Facebook OAuth2, liaison téléphone)
- **Membres & DDD Hexagonale (Port 8002)** : [`ms-membres/pom.xml`](file:///d:/projet/njangi/ms-membres/pom.xml) · [`domain/`](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/) · [`application/`](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/application/) · [`infrastructure/`](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/infrastructure/)
- **Groupes, Sessions & Mandats (Port 8003)** : [`ms-groupes/pom.xml`](file:///d:/projet/njangi/ms-groupes/pom.xml) · [`GroupeController.java`](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/controller/GroupeController.java) · [`SessionTontineController.java`](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/controller/SessionTontineController.java) · [`MandatBureauController.java`](file:///d:/projet/njangi/ms-groupes/src/main/java/com/njangi/groupes/controller/MandatBureauController.java)
- **Réunions, Séances & Présences (Port 8004)** : [`ms-reunions/pom.xml`](file:///d:/projet/njangi/ms-reunions/pom.xml) · [`ReunionController.java`](file:///d:/projet/njangi/ms-reunions/src/main/java/com/njangi/reunions/controller/ReunionController.java) · [`ReunionService.java`](file:///d:/projet/njangi/ms-reunions/src/main/java/com/njangi/reunions/service/ReunionService.java) · [`PresenceService.java`](file:///d:/projet/njangi/ms-reunions/src/main/java/com/njangi/reunions/service/PresenceService.java)
- **Cotisations, Cycles & Tour de Pot (Port 8005)** : [`ms-cotisations/pom.xml`](file:///d:/projet/njangi/ms-cotisations/pom.xml) · [`TypeCotisationController.java`](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/controller/TypeCotisationController.java) · [`CotisationController.java`](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/controller/CotisationController.java) · [`PotSessionController.java`](file:///d:/projet/njangi/ms-cotisations/src/main/java/com/njangi/cotisations/controller/PotSessionController.java)
- **Caisse, Paiements Cash & Mobile Money (Port 8006)** : [`ms-paiements/pom.xml`](file:///d:/projet/njangi/ms-paiements/pom.xml) · [`PaiementController.java`](file:///d:/projet/njangi/ms-paiements/src/main/java/com/njangi/paiements/controller/PaiementController.java) · [`PaiementService.java`](file:///d:/projet/njangi/ms-paiements/src/main/java/com/njangi/paiements/service/PaiementService.java) (Preuve cash obligatoire, MTN MoMo, Orange Money, idempotence et webhooks)
- **Discipline, Barèmes & Pénalités (Port 8007)** : [`ms-penalites/pom.xml`](file:///d:/projet/njangi/ms-penalites/pom.xml) · [`PenaliteController.java`](file:///d:/projet/njangi/ms-penalites/src/main/java/com/njangi/penalites/controller/PenaliteController.java) · [`TarificationPenaliteController.java`](file:///d:/projet/njangi/ms-penalites/src/main/java/com/njangi/penalites/controller/TarificationPenaliteController.java) (Barèmes par groupe, sanctions de séance, régularisation et écoute des retards de cotisation)
- **Notifications & Multi-Canaux (Port 8008)** : [`ms-notifications/pom.xml`](file:///d:/projet/njangi/ms-notifications/pom.xml) · [`NotificationController.java`](file:///d:/projet/njangi/ms-notifications/src/main/java/com/njangi/notifications/controller/NotificationController.java) · [`NotificationService.java`](file:///d:/projet/njangi/ms-notifications/src/main/java/com/njangi/notifications/service/NotificationService.java) (Push FCM, SMS fallback format E.164/+237, Email JavaMail, In-App et consommation de tous les événements métier)
- **Reporting, Bilans & Statistiques (Port 8009)** : [`ms-statistiques/pom.xml`](file:///d:/projet/njangi/ms-statistiques/pom.xml) · [`StatistiqueController.java`](file:///d:/projet/njangi/ms-statistiques/src/main/java/com/njangi/statistiques/controller/StatistiqueController.java) · [`StatistiqueService.java`](file:///d:/projet/njangi/ms-statistiques/src/main/java/com/njangi/statistiques/service/StatistiqueService.java) (Agrégats financiers, caisse cash vs mobile money, ratio de recouvrement, assiduité aux séances et bilans consolidés)

### B. Portail Web (Angular 22 & Tailwind CSS v4)
- [`njanki/src/app/`](file:///d:/projet/njangi/njanki/src/app/)
  - Tableaux de bord dynamiques par rôle : `features/dashboard/views/`
  - Séances en direct et émargement : `features/reunions/`
  - Multi-cotisations et tour de pot : `features/cotisations/`
  - Caisse et paiement cash avec reçu signé : `features/paiements/`
  - Composants partagés : `shared/ui/`

### C. Application Mobile Android (Ionic 8, Angular 22, Tailwind CSS & Capacitor 8)
- **Code Source & Configuration** : [`njanki-mobile/`](file:///d:/projet/njangi/njanki-mobile/) · [`capacitor.config.ts`](file:///d:/projet/njangi/njanki-mobile/capacitor.config.ts) (`cm.njangi.app`, Cleartext activé pour Gateway `http://10.0.2.2:9090`)
- **Projet Android Natif** : [`njanki-mobile/android/`](file:///d:/projet/njangi/njanki-mobile/android/) · [`AndroidManifest.xml`](file:///d:/projet/njangi/njanki-mobile/android/app/src/main/AndroidManifest.xml)
- **Binaire APK Généré** : `njanki-mobile/android/app/build/outputs/apk/debug/app-debug.apk` (4.4 MB)
- **Architecture Réactive (Signals & Zéro Promise)** :
  - Client Gateway réactif : [`MobileApiService`](file:///d:/projet/njangi/njanki-mobile/src/app/core/services/api.service.ts)
  - Surveillance réseau continue : [`NetworkService`](file:///d:/projet/njangi/njanki-mobile/src/app/core/services/network.service.ts)
  - Bandeau interactif réseau & synchronisation : [`NetworkBannerComponent`](file:///d:/projet/njangi/njanki-mobile/src/app/shared/ui/network-banner/network-banner.component.ts)
  - Gestion d'état & file d'attente hors-ligne : [`MobileTontineStateService`](file:///d:/projet/njangi/njanki-mobile/src/app/core/services/tontine-state.service.ts)
  - Sécurité des sessions JWT : [`MobileAuthService`](file:///d:/projet/njangi/njanki-mobile/src/app/core/services/auth.service.ts) via `@capacitor/preferences` (jamais `localStorage`)
- **Commande de Build APK** :
  ```powershell
  cd d:\projet\njangi\njanki-mobile\android
  $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"; $env:ANDROID_HOME = "C:\Users\fokou\AppData\Local\Android\Sdk"; .\gradlew assembleDebug
  ```

---

## 3. URLs Utiles en Local

| Interface | URL Locale |
|---|---|
| **Portail Web Angular** | `http://localhost:4200` |
| **Application Mobile Ionic** | `http://localhost:8100` |
| **Passerelle API (Gateway)** | `http://localhost:9090` |
| **Dashboard Eureka Server** | `http://localhost:8761` |
| **Swagger UI — Authentification** | `http://localhost:8001/swagger-ui.html` |
| **Swagger UI — Membres** | `http://localhost:8002/swagger-ui.html` |
| **Swagger UI — Groupes** | `http://localhost:8003/swagger-ui.html` |
| **Swagger UI — Réunions** | `http://localhost:8004/swagger-ui.html` |
| **Swagger UI — Cotisations** | `http://localhost:8005/swagger-ui.html` |
| **Swagger UI — Paiements** | `http://localhost:8006/swagger-ui.html` |
| **Swagger UI — Pénalités** | `http://localhost:8007/swagger-ui.html` |
| **Swagger UI — Notifications** | `http://localhost:8008/swagger-ui.html` |
| **Swagger UI — Statistiques** | `http://localhost:8009/swagger-ui.html` |
