# Feature Tracker & Roadmap Produit — Plateforme Njangi

> **Source de vérité du suivi fonctionnel & technique** (alignée sur [cahier_des_charges_tontine.md](./cahier_des_charges_tontine.md) v2.0 et l'architecture consolidée 4 microservices + Portail Web `njanki/`).  
> **Dernière mise à jour :** 24 novembre 2026

## Légende des statuts
- `✅ Fait` — Implémenté, câblé de bout en bout et testé
- `🟡 Partiel` — API/UI existantes mais intégration externe encore simulée, ou couverture partielle
- `⬜ À faire` — Non démarré
- `⏸️ En pause` — Mis en pause volontairement (ex. client mobile archivé au commit `8f1ffc4`)

---

## 1. Synthèse globale d'avancement

| Domaine | Statut global | Points clés restants |
|---|---|---|
| **Architecture & Socle Technique** | `✅ Fait` | Java 25, Spring Boot 4.0.10, 4 MS + Eureka + Gateway JWT, Docker Compose, 155+ tests |
| **Authentification & SSO (`F-01`–`F-02`)** | `🟡 Partiel` | Google Sign-In OIDC RS256 ✅ ; reste : SMS OTP réel, KYC upload, auto-logout inactivité, page Inscription Web |
| **Groupes, Sessions & Mandats (`F-03`–`F-06`)** | `🟡 Partiel` | CRUD groupes/sessions/mandats/démotion créateur ✅ ; reste : invitations WhatsApp/SMS/lien + approbation + vote pot |
| **Cotisations, Paiements & Pot (`F-07`–`F-11`)** | `🟡 Partiel` | ACID Cash/MoMo + idempotence + webhooks + pénalités ✅ ; reste : connecteurs réels MTN MoMo / Orange Money, upload stockage reçu physique, reçu PDF |
| **Social Njangi (`F-12`–`F-14`)** | `🟡 Partiel` | Multi-cotisations & PV texte ✅ ; reste : export PDF réel des PV, chat de groupe (`F-14`) |
| **Notifications (`F-15`–`F-17`)** | `🟡 Partiel` | Kafka consumer + In-App DB ✅ ; reste : fournisseur SMS réel, SMTP prod, Web Push / FCM, rappels schedulés J-3/J-1 |
| **Dashboards & Statistiques (`F-18`–`F-22`)** | `🟡 Partiel` | 5 vues par rôle + stats groupe ✅ ; reste : export Excel/PDF, fix concurrence Kafka `uq_stat_groupe_session`, vue Auditeur dédiée |
| **Sécurité, RGPD & Prod LuxVps (`F-23`–`F-25`)** | `⬜ À faire` | TLS/HTTPS reverse-proxy LuxVps, 2FA transactions sensibles, RGPD (consentement/suppression/export), CI/CD GitHub Actions |
| **Mobile Android / iOS** | `⏸️ En pause` | Retiré temporairement du workspace (`njanki-mobile/` conservé au commit `8f1ffc4`) |

---

## 2. Suivi détaillé par exigence (`F-01` à `F-25`)

### 3.1 Authentification & Profil (`ms-identite` · `njanki/auth`)

| ID | Fonctionnalité | Priorité | Backend | Web (`njanki/`) | Détails & Reste à faire |
|---|---|---|---|---|---|
| **F-01** | Inscription téléphone (E.164/`+237`) & email | P0 | `✅ Fait` | `🟡 Partiel` | API `/api/v1/auth/inscrire` + normalisation `+237`/Diaspora ✅ ; ajouter un écran dédié d'inscription complète & édition de profil côté Web |
| **F-01b** | Vérification KYC (upload CNI/Passeport) | P2 | `🟡 Partiel` | `⬜ À faire` | Champ `estVerifieKyc` présent ; manque upload sécurisé de pièce d'identité + workflow de validation |
| **F-02a** | Connexion mot de passe + OTP | P0 | `✅ Fait` | `✅ Fait` | Flux OTP complet via Gateway ✅ ; brancher un vrai fournisseur SMS (Twilio / Orange SMS / Nexah Cameroun) à la place du code simulé |
| **F-02b** | Connexion SSO **Google OIDC** | P0 | `✅ Fait` | `✅ Fait` | Vérification cryptographique RS256 JWKS (`GoogleOidcTokenVerifierAdapter`) + GIS Web + liaison téléphone OTP en 2 étapes ✅ |
| **F-02c** | Autres SSO (Facebook / Apple / Microsoft) | P2 | `🟡 Partiel` | `⬜ À faire` | Port hexagonal `SocialTokenVerifierPort` prêt ; implémenter adaptateurs Graph API / Apple OIDC si besoin |
| **F-02d** | Déconnexion auto après inactivité + Refresh Token | P1 | `⬜ À faire` | `⬜ À faire` | Ajouter Refresh Token + timer d'inactivité côté Web |

### 3.2 Groupes, Rôles, Sessions & Mandats (`ms-gouvernance` · `ms-identite`)

| ID | Fonctionnalité | Priorité | Backend | Web (`njanki/`) | Détails & Reste à faire |
|---|---|---|---|---|---|
| **F-03** | Création de groupe (Siège Fixe vs Rotatif, fréquence, règles pénalités) | P0 | `✅ Fait` | `✅ Fait` | CRUD complet + siège fixe/rotatif + barèmes ; ajouter upload photo de groupe et choix de langue (FR/EN) |
| **F-04** | Invitation des membres (lien partageable WhatsApp/SMS, email, approbation) | P0 | `🟡 Partiel` | `⬜ À faire` | Adhésion directe par `ms-identite` ✅ ; créer tokens d'invitation partageables (`GET/POST /invitations`), page publique `/rejoindre/:code` et file d'approbation par le Président/Créateur |
| **F-05a** | Multi-groupes & rôles dynamiques (Créateur, Président, Trésorier, Secrétaire, Membre, Auditeur) | P0 | `✅ Fait` | `✅ Fait` | Rôle par groupe dans `identite.adhesion_groupe` + bascule automatique d'interface ✅ ; renforcer les contrôles RBAC serveur par rôle sur les endpoints sensibles (`ms-gouvernance` / `ms-finances`) |
| **F-05b** | Distinction Session vs Mandat & démotion auto du Créateur | P0 | `✅ Fait` | `✅ Fait` | Élection du 1er bureau rétrograde automatiquement le Créateur en `MEMBRE` ✅ |
| **F-06** | Ordre de réception du pot (manuel, tirage au sort, vote, priorités) | P1 | `🟡 Partiel` | `🟡 Partiel` | Planification & attribution du tour de pot ✅ ; ajouter tirage au sort cryptographique, vote des membres et échange de tour (urgences) |

### 3.3 Cotisations, Paiements & Pénalités (`ms-finances`)

| ID | Fonctionnalité | Priorité | Backend | Web (`njanki/`) | Détails & Reste à faire |
|---|---|---|---|---|---|
| **F-07a** | Paiement Cash par Trésorier + preuve photo obligatoire | P0 | `✅ Fait` | `🟡 Partiel` | Règle métier `PreuveCashObligatoireException` + transaction ACID ✅ ; remplacer l'URL statique par un vrai endpoint **multipart upload** (stockage local LuxVps/MinIO) + aperçu sécurisé |
| **F-07b** | Intégration réelle **MTN MoMo Cameroun** & **Orange Money** | P0 | `🟡 Partiel` | `🟡 Partiel` | Clé d'idempotence + webhooks prêts dans `ms-finances` ; brancher les APIs opérateurs réelles (Collections / Disbursements MTN MoMo & Orange Money WebPay + vérification signature webhook) |
| **F-07c** | Paiement carte bancaire Diaspora (Stripe / CinetPay) | P2 | `⬜ À faire` | `⬜ À faire` | Adaptateur paiement carte pour membres de la diaspora (EUR/USD → XAF) |
| **F-07d** | Reçu numérique PDF certifié par paiement | P1 | `⬜ À faire` | `⬜ À faire` | Génération PDF téléchargeable (OpenPDF/iText) avec QR code de vérification |
| **F-08** | Suivi des cotisations multi-caisses (Pot, Secours, Réserve) | P0 | `✅ Fait` | `✅ Fait` | Statuts `EN_ATTENTE`, `PARTIEL`, `PAYE`, `EN_RETARD`, `PENALISE` ✅ ; ajouter génération automatique en début de séance + rappels automatiques |
| **F-09** | Gestion des pénalités (barèmes, auto/manuel, historique) | P0 | `✅ Fait` | `✅ Fait` | Barèmes + sanction auto sur retard + règlement ACID ✅ |

### 3.4 Cagnotte / Pot (`ms-finances`)

| ID | Fonctionnalité | Priorité | Backend | Web (`njanki/`) | Détails & Reste à faire |
|---|---|---|---|---|---|
| **F-10** | Versement du pot (collecte complète, Mobile Money, accusé réception, frais) | P1 | `🟡 Partiel` | `🟡 Partiel` | Attribution & décaissement API ✅ ; ajouter confirmation explicite d'accusé de réception par le bénéficiaire + gestion des frais de gestion |
| **F-11** | Historique des pots (attendu vs versé, soldes) | P1 | `✅ Fait` | `✅ Fait` | Consultable par session et par groupe ✅ |

### 3.5 Fonctionnalités Sociales Njangi (`ms-gouvernance` · `ms-finances`)

| ID | Fonctionnalité | Priorité | Backend | Web (`njanki/`) | Détails & Reste à faire |
|---|---|---|---|---|---|
| **F-12** | Cotisations événementielles (mariage, deuil, maladie, montant libre) | P1 | `🟡 Partiel` | `🟡 Partiel` | Types de cotisations configurables (`SECOURS_DECES`, etc.) ✅ ; ajouter cotisations ponctuelles à montant libre et participation optionnelle |
| **F-13** | Réunion live, émargement, quorum & Compte-rendu PDF | P1 | `🟡 Partiel` | `🟡 Partiel` | Émargement, ordre du jour, quorum, rédaction PV ✅ ; ajouter **export PDF réel** du procès-verbal + envoi automatique par email à la clôture |
| **F-14** | Chat de groupe (messages, annonces épinglées, partage documents) | P2 | `⬜ À faire` | `⬜ À faire` | Messagerie WebSocket/SSE par groupe + annonces épinglées du Président |

### 3.6 Notifications Multi-Canaux (`ms-engagement`)

| ID | Fonctionnalité | Priorité | Backend | Web (`njanki/`) | Détails & Reste à faire |
|---|---|---|---|---|---|
| **F-15** | Notifications In-App & Push (centre de notifications Web + FCM) | P1 | `🟡 Partiel` | `⬜ À faire` | Persistance Kafka → `engagement.notification` ✅ ; ajouter **cloche de notifications** dans la navbar Web + connecteur Firebase FCM réel |
| **F-16** | Notifications SMS réelles (OTP + fallback hors-ligne) | P0 | `🟡 Partiel` | `N/A` | Architecture prête dans `ms-engagement` ; brancher passerelle SMS Cameroun (Nexah / Orange SMS API / Twilio) |
| **F-17** | Notifications Email réelles (convocations, PV, récap mensuel) | P1 | `🟡 Partiel` | `N/A` | `spring-boot-starter-mail` présent ; configurer SMTP (Brevo/Resend/Postmark) + templates HTML + scheduler rappels `J-3 / J-1 / Jour J` |

### 3.7 Tableaux de bord & Statistiques (`ms-engagement` · `njanki/`)

| ID | Fonctionnalité | Priorité | Backend | Web (`njanki/`) | Détails & Reste à faire |
|---|---|---|---|---|---|
| **F-18** | Tableau de bord Membre | P0 | `✅ Fait` | `✅ Fait` | KPIs tour de pot, prochaine réunion, caisse, empty state, avancement collecte & derniers mouvements certifiés ✅ |
| **F-19** | Tableau de bord Trésorier | P0 | `✅ Fait` | `✅ Fait` | Suivi encaissements, retards, validations paiements ✅ |
| **F-20** | Tableau de bord Président | P0 | `✅ Fait` | `✅ Fait` | Vue globale, réunions, sanctions, ordre du jour ✅ |
| **F-21** | Tableau de bord Créateur | P0 | `✅ Fait` | `✅ Fait` | Paramétrage initial & lancement de l'élection du bureau ✅ |
| **F-21b** | Tableau de bord Auditeur (lecture seule certifiée) | P2 | `🟡 Partiel` | `⬜ À faire` | Ajouter vue dédiée Auditeur avec journal d'audit complet |
| **F-22** | Statistiques groupe & Export Excel / PDF | P1 | `🟡 Partiel` | `🟡 Partiel` | Calcul temps réel via Kafka ✅ ; corriger la race condition `uq_stat_groupe_session` lors de rafales Kafka + ajouter export CSV/Excel/PDF |

### 3.8 Sécurité, RGPD, Accessibilité & Production (`F-23`–`F-25`)

| ID | Fonctionnalité | Priorité | Statut | Détails & Reste à faire |
|---|---|---|---|---|
| **F-23a** | Chiffrement TLS 1.3 (HTTPS) & Reverse Proxy LuxVps | P0 | `⬜ À faire` | Nginx/Caddy + Let's Encrypt pour `njangi.cm` et `api.njangi.cm` |
| **F-23b** | 2FA (OTP/PIN) sur transactions sensibles (décaissement du pot) | P1 | `⬜ À faire` | Exiger une confirmation OTP avant tout versement de pot |
| **F-23c** | Audit log immuable des actions sensibles | P1 | `⬜ À faire` | Table d'audit (qui a validé/sanctionné/modifié quoi et quand) |
| **F-23d** | Contrôle RBAC fin côté microservices (`@PreAuthorize` / rôle dans le groupe) | P0 | `⬜ À faire` | Vérifier côté serveur que seul un Trésorier valide un paiement, seul un Président inflige une sanction, etc. |
| **F-24** | Conformité RGPD (consentement CGU, export données, suppression compte) | P1 | `⬜ À faire` | Pages mentions légales/confidentialité FR+EN + endpoints export JSON & anonymisation compte |
| **F-25** | Accessibilité WCAG 2.1 AA + i18n FR/EN + Mode Clair/Sombre | P1 | `🟡 Partiel` | Contraste WCAG AA renforcé ✅ ; ajouter toggle Clair/Sombre (lisibilité plein soleil) + traduction EN |

---

## 3. Chantiers Techniques & DevOps (Pré-Production)

| Chantier | Priorité | Statut | Détail |
|---|---|---|---|
| **Durcissement concurrence `ms-engagement`** | P0 | `⬜ À faire` | Upsert atomique (`ON CONFLICT DO UPDATE`) ou verrou optimiste sur `engagement.statistique_groupe` (`uq_stat_groupe_session`) |
| **Upload fichiers (reçus cash, avatars, KYC)** | P0 | `⬜ À faire` | Endpoint sécurisé de stockage de fichiers (volume Docker persistant LuxVps ou MinIO S3-compatible) avec contrôle MIME/taille |
| **CI/CD GitHub Actions** | P1 | `⬜ À faire` | Pipeline automatique : `mvn test` (Java 25) + `ng build` + `ng test` (Angular 22) sur chaque push/PR |
| **Déploiement Docker Compose Production (LuxVps)** | P0 | `⬜ À faire` | `docker-compose.prod.yml` (Nginx HTTPS, limites RAM/CPU par conteneur, backups PostgreSQL quotidiens, logs) |
| **Tests E2E automatisés complets** | P1 | `✅ Fait` | Script 9 phases (`scripts/test-e2e-scenario.ps1`) validé en direct (9/9) ✅ |
| **Reprise Mobile (`njanki-mobile/`)** | P2 | `⏸️ En pause` | Restaurable ultérieurement via `git checkout 8f1ffc4 -- njanki-mobile` |

---

## 4. Prochains Sprints Recommandés (Ordre Optimal vers la Mise en Production)

1. **Sprint A — Sécurité & Fiabilité Backend (P0)**
   - [ ] Contrôle RBAC par rôle de groupe dans `ms-gouvernance` et `ms-finances` (`F-23d`)
   - [ ] Fix concurrence `uq_stat_groupe_session` dans `ms-engagement`
   - [ ] Upload réel de preuve photo reçu cash (stockage fichiers) (`F-07a`)
2. **Sprint B — Parcours Utilisateur Complet Web (P0)**
   - [ ] Page Inscription + Édition de profil utilisateur (`F-01`)
   - [ ] Système d'invitations par lien partageable (WhatsApp/SMS) + approbation (`F-04`)
   - [ ] Centre de notifications In-App (cloche dans la navbar) (`F-15`)
3. **Sprint C — Intégrations Réelles Cameroun (P0/P1)**
   - [ ] Fournisseur SMS réel pour OTP (`F-16`) + SMTP réel pour Emails (`F-17`)
   - [ ] Connecteurs API **MTN MoMo** & **Orange Money** + signature webhooks (`F-07b`)
   - [ ] Export PDF réel (PV de réunion `F-13` & Reçus de paiement `F-07d`)
4. **Sprint D — Déploiement Production LuxVps (P0)**
   - [ ] `docker-compose.prod.yml` + Reverse-proxy HTTPS (Caddy/Nginx) + backups PostgreSQL (`F-23a`)
   - [ ] CI/CD GitHub Actions + Conformité RGPD (`F-24`) + Mode Clair/Sombre (`F-25`)
