# Guide d'Intégration Google Sign-In (OIDC / SSO) — Plateforme Njangi

Ce guide explique comment configurer et activer l'authentification **Google Sign-In (OpenID Connect)** de bout en bout sur :
- **Backend** : `ms-identite` (vérification cryptographique RS256 via JWKS Google) + `api-gateway`
- **Frontend Web** : `njanki/` (Google Identity Services — GIS)
- **Mobile Android** : `njanki-mobile/` (Google Identity Services PWA + plugin natif Capacitor Android)

---

## 1. Flux de sécurité OIDC (Zero Trust)

```
[Web GIS / Mobile Android]
        │  1. Obtient un Google ID Token (JWT signé RS256 par Google)
        ▼
[API Gateway :9090] ──► POST /api/v1/auth/social/login { "provider": "GOOGLE", "token": "<ID_TOKEN>" }
        │
        ▼
[ms-identite :8001] (GoogleOidcTokenVerifierAdapter)
        │  2. Vérifie signature RS256 contre https://www.googleapis.com/oauth2/v3/certs (JWKS)
        │  3. Contrôle iss ∈ {https://accounts.google.com, accounts.google.com}
        │  4. Contrôle aud ∈ {GOOGLE_CLIENT_ID, GOOGLE_WEB_CLIENT_ID, GOOGLE_ANDROID_CLIENT_ID}
        │  5. Contrôle expiration (exp) + email_verified avant toute fusion par email
        │  6. Provisionne/retrouve l'utilisateur (identite.utilisateur) et émet le JWT Njangi (HS256)
        ▼
[Client] ──► Si telephoneRequis=true → liaison du numéro Mobile Money (+237 / Diaspora) confirmée par OTP
```

> **Aucun claim client non signé** (`email`, `providerId`, `nom`) n'est accepté en production. La liaison automatique sur un compte existant par email exige `email_verified = true` pour empêcher toute prise de contrôle de compte (*account takeover*).

---

## 2. Création des identifiants sur Google Cloud Console

1. Ouvrir [Google Cloud Console](https://console.cloud.google.com/) et sélectionner (ou créer) le projet **Njangi**.
2. Aller dans **API et services → Écran de consentement OAuth** :
   - Type : **Externe**
   - Nom de l'application : `Njangi`
   - Scopes requis : `openid`, `email`, `profile`
3. Aller dans **API et services → Identifiants → + Créer des identifiants → ID client OAuth** :

### A. Client Web (requis pour `njanki/`, le backend `ms-identite`, et Capacitor Android)
- Type d'application : **Application Web**
- **Origines JavaScript autorisées** :
  - Dev Web : `http://localhost:4200`
  - Dev Mobile Web/Ionic : `http://localhost:8100`
  - Production : `https://njangi.cm` (et `https://www.njangi.cm`)
- Récupérer le **Client ID** généré : `xxxxx-yyyyy.apps.googleusercontent.com`.

### B. Client Android (pour l'APK / Play Store de `njanki-mobile/`)
- Type d'application : **Android**
- Nom du package : celui défini dans `njanki-mobile/capacitor.config.ts` (`appId`)
- Empreinte de certificat **SHA-1** :
  ```bash
  # Clé de debug locale Android
  keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
  ```
- Récupérer le **Client ID Android** généré.

---

## 3. Configuration des variables d'environnement

### Développement local (`docker-compose.yml` ou fichier `.env` à la racine)
Créer un fichier `.env` (non versionné) à la racine du monorepo :

```env
GOOGLE_WEB_CLIENT_ID=1234567890-web.apps.googleusercontent.com
GOOGLE_ANDROID_CLIENT_ID=1234567890-android.apps.googleusercontent.com
NJANGI_SSO_ALLOW_DEV_FALLBACK=false
```

Puis reconstruire et relancer `ms-identite` et `api-gateway` :
```bash
mvn -pl ms-identite,api-gateway clean package -DskipTests
docker compose up -d --build ms-identite api-gateway
```

> **Note :** Les frontends Web (`njanki/`) et Mobile (`njanki-mobile/`) récupèrent **automatiquement** le `googleClientId` public via `GET /api/v1/auth/social/config` — il n'est donc même pas nécessaire de dupliquer le Client ID dans les fichiers `environment.ts` (même si vous pouvez l'y renseigner en surcharge statique si souhaité).

### Production (LuxVps)
Sur le serveur de production, définir impérativement :
```env
GOOGLE_WEB_CLIENT_ID=<votre-client-id-web>
GOOGLE_ANDROID_CLIENT_ID=<votre-client-id-android>
NJANGI_SSO_ALLOW_DEV_FALLBACK=false
```
Avec `NJANGI_SSO_ALLOW_DEV_FALLBACK=false`, toute tentative de connexion sociale sans Google ID Token cryptographiquement valide est immédiatement rejetée (HTTP 400 RFC 9457 `ProblemDetail`).

---

## 4. Activation native sur Android (`njanki-mobile/`)

Sur navigateur/PWA, `GoogleSsoMobileService` charge automatiquement Google Identity Services sans dépendance supplémentaire.  
Pour le **build APK Android natif** (Google Credential Manager natif sans WebView) :

```bash
cd njanki-mobile
npm install @capgo/capacitor-social-login
npx cap sync android
```

`GoogleSsoMobileService` détecte automatiquement `window.Capacitor.Plugins.SocialLogin` lorsqu'il est présent sur Android et demande un `idToken` OIDC vérifié par `ms-identite` (en utilisant le `GOOGLE_WEB_CLIENT_ID` comme `webClientId` côté Android, conformément aux exigences Google OIDC).

---

## 5. Vérification rapide

1. Vérifier que la configuration publique SSO est exposée par la Gateway :
   ```bash
   curl http://localhost:9090/api/v1/auth/social/config
   ```
   Réponse attendue :
   ```json
   {
     "success": true,
     "message": "Opération réussie",
     "data": {
       "googleEnabled": true,
       "googleClientId": "1234567890-web.apps.googleusercontent.com",
       "devFallbackEnabled": false
     }
   }
   ```
2. Ouvrir `http://localhost:4200/login` : le bouton officiel **Google Identity Services** apparaît automatiquement à la place du bouton fallback dev.
3. Après connexion Google, si aucun numéro de téléphone n'est encore lié au compte, l'écran de liaison Mobile Money (+237 / Diaspora) avec confirmation OTP s'affiche automatiquement.
