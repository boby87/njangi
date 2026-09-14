# Documentation d'Architecture & Répertoire des Guides Njangi

Ce fichier référence l'ensemble de la documentation technique et fonctionnelle de la plateforme **Njangi**.

---

## 1. Documents Principaux à la Racine

- **[README.md](file:///d:/projet/njangi/README.md)** : Guide complet du projet, cartographie des microservices, matrice des ports, Swagger, démarrage Docker et Angular/Ionic.
- **[GEMINI.md](file:///d:/projet/njangi/GEMINI.md)** : Directives strictes d'implémentation, règles inviolables (Signal Forms purs, Zéro Promise, Architecture Hexagonale DDD sur `ms-membres`, virtual threads Java 25).
- **[cahier_des_charges_tontine.md](file:///d:/projet/njangi/cahier_des_charges_tontine.md)** : Spécification métier officielle (règles de tontine camerounaise, multi-cotisations, barème de sanctions, ordre de passage du pot).
- **[init-schemas.sql](file:///d:/projet/njangi/init-schemas.sql)** : Script d'initialisation des 9 schémas PostgreSQL isolés (`auth`, `membres`, `groupes`, `reunions`, `cotisations`, `paiements`, `penalites`, `notifications`, `statistiques`).
- **[docker-compose.yml](file:///d:/projet/njangi/docker-compose.yml)** : Définition des conteneurs (PostgreSQL, ZooKeeper, Kafka, Eureka Server, API Gateway, microservices métier).

---

## 2. Répartition par Composant

### A. Backend Microservices (Java 25 & Spring Boot 4)
- **Découverte Eureka (Port 8761)** : [`eureka-server/pom.xml`](file:///d:/projet/njangi/eureka-server/pom.xml) · [`EurekaServerApplication.java`](file:///d:/projet/njangi/eureka-server/src/main/java/com/njangi/eureka/EurekaServerApplication.java)
- **API Gateway (Port 9090)** : [`api-gateway/pom.xml`](file:///d:/projet/njangi/api-gateway/pom.xml) · [`GatewayProxyController.java`](file:///d:/projet/njangi/api-gateway/src/main/java/com/njangi/gateway/controller/GatewayProxyController.java)
- **Authentification, OTP & Social OAuth2 (Port 8001)** : [`ms-auth/pom.xml`](file:///d:/projet/njangi/ms-auth/pom.xml) · [`AuthController.java`](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/controller/AuthController.java) · [`AuthService.java`](file:///d:/projet/njangi/ms-auth/src/main/java/com/njangi/auth/service/AuthService.java) (Support Diaspora E.164, Google & Facebook OAuth2, liaison téléphone)
- **Membres & DDD Hexagonale (Port 8002)** : [`ms-membres/pom.xml`](file:///d:/projet/njangi/ms-membres/pom.xml) · [`domain/`](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/domain/) · [`application/`](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/application/) · [`infrastructure/`](file:///d:/projet/njangi/ms-membres/src/main/java/com/njangi/membres/infrastructure/)

### B. Portail Web (Angular 22 & Tailwind CSS v4)
- [`njanki/src/app/`](file:///d:/projet/njangi/njanki/src/app/)
  - Tableaux de bord dynamiques par rôle : `features/dashboard/views/`
  - Séances en direct et émargement : `features/reunions/`
  - Multi-cotisations et tour de pot : `features/cotisations/`
  - Caisse et paiement cash avec reçu signé : `features/paiements/`
  - Composants partagés : `shared/ui/`

### C. Application Mobile (Ionic 8 & Capacitor)
- [`njanki-mobile/src/app/`](file:///d:/projet/njangi/njanki-mobile/src/app/)
  - Onglets applicatifs : `layout/tabs/`
  - Pages réactives : `pages/`
  - Sécurité des tokens via `@capacitor/preferences` : `core/services/auth.service.ts`
  - Mode dégradé 3G via `@capacitor/network` : `core/services/network.service.ts`

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
