# Cahier des Charges — Application de Tontine Camerounaise (Njangi)

**Version :** 1.0  
**Date :** 10 septembre 2026  
**Statut :** Draft

---

## 1. Contexte et objectifs

### 1.1 Contexte

La tontine (appelée **Njangi** au Cameroun) est bien plus qu'un simple système d'épargne rotative. C'est un **système complet de gestion de réunion communautaire** qui englobe : la conduite structurée des séances (ordre du jour, tour de parole, compte-rendu), l'application de sanctions en cas de manquement aux règles, et la gestion financière multi-cotisations au sein d'un même groupe.

Un groupe de tontine peut ainsi créer plusieurs types de cotisations simultanément : cotisation principale rotative, cotisation d'entraide pour les événements (mariage, deuil, maladie), cotisation de solidarité, fonds de réserve, etc. Chaque type de cotisation a ses propres règles, montants et bénéficiaires.

Aujourd'hui, la gestion de ces tontines se fait encore majoritairement à la main (carnet, cahier, Excel), entraînant des risques d'erreurs, de litiges et de pertes d'information. Une application numérique permettrait de structurer, sécuriser et fluidifier l'ensemble de ces pratiques.

### 1.2 Objectifs

- Digitaliser la **gestion complète des réunions de tontine** (ordre du jour, présences, sanctions, décisions)
- Permettre à un groupe de créer et gérer **plusieurs types de cotisations** au sein d'une même tontine
- Offrir transparence et traçabilité à tous les membres sur l'ensemble des flux financiers
- Réduire les conflits liés aux erreurs de gestion et aux malentendus
- Faciliter la gestion à distance pour les membres de la diaspora
- Respecter et numériser les pratiques culturelles locales du Njangi

---

## 2. Périmètre du projet

### 2.1 Plateformes cibles

| Plateforme | Priorité |
|---|---|
| Application Web (navigateur) | P1 — MVP |
| Application Mobile Android | P1 — MVP |
| Application Mobile iOS | P2 — Phase 2 |

### 2.2 Public cible

- Particuliers camerounais (locale et diaspora)
- Membres de groupes de tontine existants
- Organisateurs/animateurs de tontine
- Associations et groupes communautaires

---

## 3. Fonctionnalités

### 3.1 Authentification et profil utilisateur

**F-01 — Inscription**
- Inscription par numéro de téléphone (OTP SMS) ou email
- Vérification d'identité par photo de pièce d'identité (optionnel, pour lever les limites de montants)
- Profil : nom, prénom, photo, numéro de téléphone, ville/pays

**F-02 — Connexion**
- Connexion par mot de passe + OTP
- Connexion SSO (Google, Microsoft, Apple) pour simplifier l'accès
- Biométrie (empreinte / Face ID) sur mobile
- Déconnexion automatique après inactivité

---

### 3.2 Gestion des groupes de tontine

**F-03 — Création d'un groupe**
- Nom du groupe, description, photo
- Fréquence des réunions : hebdomadaire / mensuelle / bimensuelle / personnalisée
- Montant de la cotisation par membre
- Nombre maximum de membres
- **Type de siège** : le créateur doit obligatoirement préciser si la réunion se tient dans un lieu fixe ou en rotation chez les membres
  - **Siège fixe** : adresse permanente du groupe (local associatif, domicile désigné, salle louée)
  - **Siège rotatif** : chaque réunion se tient chez un membre différent, selon un ordre défini (tirage au sort, ordre alphabétique, ordre de réception du pot, ou manuel)
- Règles de pénalité en cas de retard avec tarification détaillée par type de manquement (retard de paiement, absence non justifiée, retard à la réunion, non-respect des règles, etc.)
- Langue du groupe : français / anglais / langue locale

**F-04 — Invitation des membres**
- Invitation par lien partageable (WhatsApp, SMS)
- Invitation par numéro de téléphone
- Invitation par email
- Approbation par l'organisateur avant intégration

**F-05 — Rôles dans un groupe**

Un membre peut appartenir à **plusieurs groupes simultanément** et avoir un rôle différent dans chacun. Son tableau de bord s'adapte dynamiquement selon le rôle qu'il occupe dans chaque groupe.

#### Notion de session vs. mandat de bureau

Ces deux concepts sont distincts et doivent être gérés indépendamment dans l'application :

- **Session** : période d'un cycle complet de la tontine, pendant laquelle tous les membres cotisent et reçoivent le pot à tour de rôle (ex. : session 2025-2026). Une session a une date de début, une date de fin et un ensemble de règles financières fixées à l'avance. Une session peut contenir plusieurs réunions.

- **Mandat de bureau** : période pendant laquelle un bureau élu (Président, Trésorier, Secrétaire) exerce ses fonctions. Un mandat peut couvrir une ou plusieurs sessions selon les règles du groupe. Il est possible de renouveler le bureau sans clôturer la session, ou inversement de clôturer une session sans changer de bureau.

#### Cycle de vie des droits du Créateur

- Le **Créateur** est la personne qui a initialisé le groupe dans l'application. Il dispose de droits techniques provisoires (paramétrage initial, invitation des premiers membres) jusqu'à l'élection du premier bureau.
- **Dès qu'un Président est élu, le Créateur perd automatiquement ses droits techniques** et redevient un membre simple ou occupe un rôle attribué par le Président (ex. : Trésorier, Secrétaire).
- Le Créateur reste identifié comme fondateur dans l'historique du groupe, sans aucun privilège opérationnel.

| Rôle | Désignation | Droits | Durée |
|---|---|---|---|
| Créateur | Automatique (fondateur) | Paramétrage initial, invitation des premiers membres | Jusqu'à l'élection du Président |
| Président | Élu par les membres en session | Gestion complète du groupe, ordre du jour, sanctions, nomination du bureau | Durée de la session |
| Trésorier | Nommé par le Président | Enregistrement et validation des paiements, gestion des caisses | Durée de la session |
| Secrétaire | Nommé par le Président | Comptes-rendus, calendrier, convocations | Durée de la session |
| Membre | Inscription approuvée | Consultation, paiement de ses cotisations, vote | Permanente |
| Auditeur | Nommé par le Président | Lecture seule, vérification des comptes | Durée de la session |

**F-06 — Ordre de réception du pot**
- Attribution manuelle par l'organisateur
- Tirage au sort automatique
- Vote entre membres
- Gestion des priorités (urgences, événements)

---

### 3.3 Gestion des cotisations

**F-07 — Enregistrement des paiements**
- Paiement en espèces (cash) enregistré manuellement par le trésorier avec confirmation
- Intégration avec Mobile Money : Orange Money, MTN Mobile Money
- Paiement par carte bancaire (Stripe / CinetPay)
- Preuve de paiement obligatoire pour les paiements cash (photo reçu signé, capture d'écran)
- Génération d'un reçu numérique pour chaque paiement enregistré

**F-08 — Suivi des cotisations**
- Tableau de bord de la session en cours
- Liste des membres ayant payé / non payé
- Statuts : Payé / En attente / En retard / Pénalisé
- Rappels automatiques avant la date limite

**F-09 — Gestion des pénalités**
- Pénalité définie à la création du groupe (montant fixe ou %)
- Application automatique ou manuelle par le trésorier
- Historique des pénalités par membre

---

### 3.4 Gestion de la cagnotte (le Pot)

**F-10 — Versement du pot**
- Confirmation de la collecte complète avant versement
- Versement via Mobile Money au bénéficiaire
- Accusé de réception du bénéficiaire
- Déduction des frais de gestion (si applicable)

**F-11 — Historique des pots**
- Qui a reçu quoi et quand
- Montants réels vs. montants attendus
- Soldes et ajustements

---

### 3.5 Fonctionnalités sociales (spécifique au Njangi)

**F-12 — Cotisations événementielles**
- Collecte spéciale pour un événement (mariage, baptême, deuil, maladie)
- Montant libre ou fixe par membre
- Visibilité et participation optionnelle

**F-13 — Compte-rendu de réunion**
- Rapport automatique généré après chaque session
- Points abordés, présences, décisions, paiements
- Exportable en PDF

**F-14 — Chat de groupe**
- Messagerie interne au groupe
- Annonces de l'organisateur (épinglées)
- Partage de documents (comptes rendus, relevés)

---

### 3.6 Notifications

**F-15 — Notifications push (mobile)**
- Rappel de paiement (J-3, J-1, Jour J)
- Confirmation de paiement reçu
- Notification quand c'est votre tour de recevoir le pot
- Alerte de retard de paiement d'un membre
- Convocation à une réunion (ordre du jour joint)
- Résultat d'un vote ou d'une décision

**F-16 — Notifications SMS**
- Fallback SMS si pas de connexion internet
- OTP de connexion

**F-17 — Notifications email**
- Convocation officielle à une réunion avec ordre du jour en pièce jointe
- Compte-rendu de réunion envoyé automatiquement après chaque session
- Récapitulatif mensuel des cotisations et de l'état du groupe
- Alertes importantes (changement de rôle, exclusion, modification des règles du groupe)

---

### 3.7 Tableau de bord et statistiques

Chaque utilisateur dispose d'un **tableau de bord personnalisé** qui s'adapte selon le rôle qu'il occupe dans chaque groupe. Si un membre est Président dans un groupe et simple Membre dans un autre, il verra deux vues distinctes.

**F-18 — Tableau de bord Membre**
- Liste de tous ses groupes avec son rôle dans chacun
- Cotisations payées vs. à venir (par groupe)
- Position dans l'ordre de réception du pot
- Historique complet de sa participation et de ses paiements

**F-19 — Tableau de bord Trésorier**
- État des paiements de la session en cours (payé / en attente / en retard)
- Liste des membres en retard avec montants dus
- Caisse disponible et historique des encaissements
- Validation et enregistrement rapide des paiements

**F-20 — Tableau de bord Président**
- Vue d'ensemble du groupe : membres actifs, taux de présence, taux de collecte
- Gestion de l'ordre du jour de la prochaine réunion
- Membres sanctionnés et historique des pénalités
- Prochaine date de versement du pot et bénéficiaire

**F-21 — Tableau de bord Créateur**
- Vue d'ensemble de tous les groupes qu'il a créés
- Accès aux paramètres techniques du groupe
- Gestion des transferts de propriété et des droits d'administration

**F-22 — Statistiques du groupe**
- Montant total collecté à ce jour (par type de cotisation)
- Taux de participation et de présence moyen
- Évolution des cotisations sur le temps
- Export Excel / PDF

---

### 3.8 Sécurité et conformité

**F-20 — Sécurité des données**
- Chiffrement des données en transit (TLS 1.3) et au repos (AES-256)
- Authentification à deux facteurs obligatoire pour les transactions
- Journalisation des accès et actions sensibles

**F-21 — Conformité RGPD**
- Hébergement des données en Europe (ou Cameroun si disponible)
- Consentement explicite à la collecte des données
- Droit à l'effacement et à la portabilité des données
- Politique de confidentialité claire en français et anglais

**F-22 — Accessibilité**
- Conformité WCAG 2.1 niveau AA
- Support des grandes tailles de police
- Contraste suffisant pour les environnements lumineux (usage extérieur)
- Interface utilisable avec une connexion internet faible (mode offline partiel)

---

## 4. Architecture technique

### 4.1 Stack technologique

| Couche | Technologie | Détail |
|---|---|---|
| Frontend Web | Angular 22 | Application web SPA |
| Application Mobile | Ionic 8 | Application hybride iOS / Android basée sur Angular |
| Backend | Java / Spring Boot 4 | Architecture microservices |
| Gateway | Spring MVC | Point d'entrée unique, routage vers les microservices |
| Messagerie | Apache Kafka | Communication asynchrone entre microservices |
| Base de données | PostgreSQL | Une instance par microservice (isolation des données) |
| Authentification | SSO (OpenID Connect / OAuth2) + OTP | Connexion unifiée et vérification par code |
| Hébergement | LuxVps | Serveur dédié auto-hébergé |
| Stockage fichiers | Stockage local serveur | Fichiers hébergés directement sur LuxVps |
| Paiements | Module à développer | Intégration Mobile Money et paiements à concevoir en interne |
| Notifications | Firebase FCM (push mobile) + SMS + Email | Couverture multi-canal |

### 4.2 Architecture microservices

L'application repose sur une architecture **microservices** avec Spring Boot 4. Chaque domaine métier est un service indépendant, déployable séparément. La gateway Spring MVC centralise toutes les entrées clientes et route vers le bon service.

```
┌─────────────────────────────────────────────────────┐
│                    CLIENTS                          │
│    [Angular 22 Web]        [Ionic 8 Mobile]         │
└──────────────────┬──────────────────────────────────┘
                   │ HTTPS
                   ▼
        ┌─────────────────────┐
        │   API Gateway       │
        │   (Spring MVC)      │
        │   Auth SSO / OTP    │
        └────────┬────────────┘
                 │
    ┌────────────┼─────────────────────────────┐
    │            │                             │
    ▼            ▼                             ▼
[MS Membres] [MS Groupes &      [MS Cotisations &
             Sessions]          Paiements]
    │            │                             │
    ▼            ▼                             ▼
[MS Réunions] [MS Notifications] [MS Pénalités]

         ──── Communication via Apache Kafka ────

Tous les microservices :
  └─► Un SGBD PostgreSQL unique partagé (schémas séparés par service)
  └─► Publient / consomment des événements Kafka

Infrastructure : LuxVps (serveur dédié)
Stockage fichiers : local sur LuxVps
```

#### Stratégie base de données

L'ensemble des microservices partagent **un seul SGBD PostgreSQL**, mais chaque microservice dispose de son propre **schéma** (`schema_membres`, `schema_groupes`, `schema_cotisations`, etc.) afin de maintenir l'isolation logique des données tout en simplifiant l'administration et la maintenance.

#### Microservices identifiés

| Microservice | Responsabilité | Schéma BDD |
|---|---|---|
| MS-Authentification | SSO, OTP, gestion des sessions utilisateur | `auth` |
| MS-Membres | Profils, multi-groupes, rôles par groupe | `membres` |
| MS-Groupes | Création, paramétrage, sessions, mandats de bureau | `groupes` |
| MS-Réunions | Ordre du jour, présences, comptes-rendus, sanctions | `reunions` |
| MS-Cotisations | Types de cotisations, paiements, statuts | `cotisations` |
| MS-Paiements | Module de paiement interne (cash, Mobile Money, carte) | `paiements` |
| MS-Pénalités | Tarification, application, historique | `penalites` |
| MS-Notifications | Envoi push (FCM), SMS, email | `notifications` |
| MS-Statistiques | Tableaux de bord, exports, rapports | `statistiques` |

---

## 5. Contraintes et risques

### 5.1 Contraintes

| Contrainte | Description |
|---|---|
| Budget | Solution open-source et hébergement low-cost. Pas de licence logicielle. |
| Délai | MVP livrable en 3 mois maximum |
| Connectivité | Les utilisateurs ont souvent une connexion 3G/4G instable — mode hors ligne partiel requis |
| Langue | Interface en français (principal) et anglais |
| Mobile Money | Intégration obligatoire Orange Money et MTN Mobile Money Cameroun |

### 5.2 Risques identifiés

| Risque | Probabilité | Impact | Mitigation |
|---|---|---|---|
| Non-respect des délais de paiement | Élevé | Élevé | Système de rappels automatiques et pénalités |
| Litiges entre membres | Moyen | Élevé | Historique immuable et auditable |
| Fraude / faux paiements | Moyen | Élevé | Preuves de paiement obligatoires, double validation |
| Faible adoption numérique | Élevé | Élevé | UX simple, onboarding guidé, mode enregistrement manuel |
| Problèmes d'intégration Mobile Money | Moyen | Moyen | Fallback enregistrement manuel |

---

## 6. Phases de livraison

### Phase 1 — MVP (3 mois)

- Authentification (téléphone + OTP)
- Création et gestion de groupes
- Gestion des membres et rôles
- Enregistrement manuel des paiements (trésorier)
- Tableau de bord basique
- Notifications push et SMS
- Application Web + Android

### Phase 2 — Enrichissement (3 mois supplémentaires)

- Intégration Mobile Money (Orange Money, MTN)
- Cotisations événementielles
- Chat de groupe
- Compte-rendu automatique PDF
- Application iOS

### Phase 3 — Avancé (6 mois supplémentaires)

- Statistiques avancées et export
- Vérification d'identité KYC
- Multi-devises (FCFA, EUR, USD pour la diaspora)
- API pour intégrations tierces

---

## 7. Critères d'acceptation (MVP)

- Un organisateur peut créer un groupe et inviter des membres en moins de 5 minutes
- Un trésorier peut enregistrer un paiement en moins de 30 secondes
- Tous les membres reçoivent une notification dans les 5 minutes suivant un événement
- Le tableau de bord affiche en temps réel l'état des cotisations
- L'application est utilisable avec une connexion 3G (< 3s de chargement)
- Les données sont accessibles même hors connexion (dernière synchronisation)
- Conformité RGPD vérifiée avant le lancement

---

## 8. Estimations

### 8.1 Ressources humaines

| Rôle | Charge estimée (MVP) |
|---|---|
| 1 Chef de projet / Product Owner | 3 mois (temps partiel) |
| 2 Développeurs Full-Stack | 3 mois (temps plein) |
| 1 Designer UI/UX | 1 mois |
| 1 Testeur QA | 1 mois |

### 8.2 Coûts d'infrastructure (mensuel, MVP)

| Service | Coût estimé |
|---|---|
| Hébergement backend (Railway) | ~20 USD/mois |
| Base de données PostgreSQL | ~10 USD/mois |
| Firebase (Auth + FCM) | Gratuit (plan Spark) |
| Twilio SMS (100 SMS/mois) | ~5 USD/mois |
| Cloudinary (stockage) | Gratuit (plan free) |
| **Total infrastructure** | **~35 USD/mois** |

---

## 9. Glossaire

| Terme | Définition |
|---|---|
| Tontine / Njangi | Système d'épargne rotative communautaire |
| Pot | La cagnotte totale collectée lors d'un cycle |
| Cotisation | La contribution individuelle d'un membre |
| Cycle | Une période complète où tous les membres ont reçu le pot |
| Organisateur | Membre fondateur responsable du groupe |
| Trésorier | Membre chargé de collecter et enregistrer les paiements |
| Mobile Money | Service de paiement mobile (Orange Money, MTN MoMo) |
| FCFA | Franc CFA, monnaie principale du Cameroun |

---

*Document rédigé avec Claude Code — Anthropic*
