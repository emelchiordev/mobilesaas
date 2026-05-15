# Savio Mobile (saviomobilite)

Application Android **SAVIO** pour **techniciens de terrain** : consultation de la tournée, suivi d’intervention sur site, photos, compte-rendu, facturation locale, signatures et synchronisation avec un backend HTTP.

---

## Contexte produit

L’app sert de **terminal terrain** relié à un serveur (planning, référentiels, envoi des opérations). Le technicien :

- consulte ses **interventions par jour** ;
- **démarre** une intervention, accède au **client**, aux **équipements**, au **contrat**, aux **photos** ;
- peut **facturer** (brouillon local puis envoi selon les règles métier) ;
- **clôture** l’intervention : compte-rendu, **types réels** d’intervention (sélection multiple), **signatures** (technicien / client selon les types) ;
- travaille **hors ligne partiellement** : données en cache (**Room**), file d’opérations à pousser, synchro déclenchée au pull manuel et via **WorkManager** selon la configuration.

Une intervention **en cours** (`in_progress`) est persistée en local : en cas de fermeture brutale de l’app, une **bannière de reprise** sur le planning propose de reprendre ou d’ignorer (réinitialisation locale).

---

## Prérequis

| Élément | Version / remarque |
|--------|---------------------|
| Android Studio | Récent (Gradle dans le projet) |
| JDK | **11** (voir `app/build.gradle.kts`) |
| compileSdk | **36** |
| minSdk | **28** |
| Appareil / émulateur | API 28+ |

---

## Stack technique

| Couche | Technologies |
|--------|----------------|
| Langage | **Kotlin** |
| UI | **Jetpack Compose**, **Material 3**, `material3-window-size-class` (mise en page téléphone / tablette) |
| Architecture | **MVVM**, `ViewModel`, repositories, couche `data` (local + remote) |
| Injection | **Hilt** (KSP) |
| Réseau | **Retrofit** + **OkHttp** (Gson), intercepteur d’authentification, bus d’événements (`AuthEventBus`) |
| Persistance | **Room** (SQLite), **DataStore** (tokens / session) |
| Arrière-plan | **WorkManager** (+ extension Hilt), workers de synchro |
| Médias | **Coil**, **CameraX** (capture photo) |
| Autres | **Accompanist Permissions**, etc. |

Les versions sont centralisées dans **`gradle/libs.versions.toml`**.

---

## Configuration backend

**Variants :** **debug** (`applicationId` suffix `.debug`) pointe par défaut vers `http://10.0.2.2:3000/` (émulateur → localhost de la machine hôte). Pour une **tablette sur le même réseau**, ajoute dans **`local.properties`** (non versionné) :

```properties
DEV_BASE_URL=http://192.168.1.XX:3000
```

Optionnel pour le catalogue BAN local :

```properties
DEV_BAN_BASE_URL=http://192.168.1.XX:3001
```

**release** utilise `https://api.savio.re/` et `https://ban.melchior.re/` pour `BAN_API_BASE_URL` (adapter si besoin).

L’URL principale Retrofit est **`BuildConfig.BASE_URL`** (voir `NetworkModule`). Changement d’environnement : **Build > Select Build Variant** (debug vs release).

---

## Architecture du code

`app/src/main/java/re/melchior/saviomobile/`

| Dossier | Rôle |
|--------|------|
| `data/local` | Entités Room, DAOs, `SavioDatabase`, migrations, `TokenDataStore` |
| `data/remote` | APIs Retrofit (`AuthApi`, `SyncApi`, …), DTOs Gson, intercepteurs |
| `data/repository` | `AuthRepository`, `SyncRepository`, `PushRepository`, `PhotoSyncRepository`, `InvoiceRepository`, … |
| `di` | Modules Hilt (`NetworkModule`, `DatabaseModule`, …) |
| `ui/navigation` | `AppNavigation`, routes `Screen` |
| `ui/screen` | Écrans par domaine : `auth`, `tournee`, `intervention`, `invoice`, clôture |
| `ui/theme` | Thème Material / couleurs Savio |
| `worker` | Workers (ex. synchro) |

**Point d’entrée** : `MainActivity` — thème, navigation, `WindowSizeClass` pour l’UI adaptative.

---

## Synchronisation

### Pull (interventions & référentiels)

- **`SyncRepository.pull(date)`** appelle l’API (ex. `SyncApi`) avec éventuellement `If-Modified-Since` selon les réglages locaux.
- Les interventions reçues sont fusionnées en Room avec une logique de **non-régression** : une intervention **déjà terminée ou en validation côté terrain** n’est pas écrasée par le serveur de façon destructive (`insertAllSafe` / règles par statut `syncStatus`).
- Référentiels (types d’intervention, équipements, etc.), **historique** unité, **équipements** liés aux interventions, **types réels** synchronisés (`intervention_actual_types`) selon les règles métier.

### Push / opérations différées

- Les actions terrain (démarrage, clôture, etc.) passent par **`PushRepository`** / API dédiée selon le modèle du projet.
- Les **factures** et mises à jour peuvent produire des entrées **`pending_updates`** (Room) pour envoi ultérieur (ex. soumission de facture avec lignes et paiements).
- **WorkManager** peut enchaîner les tâches de synchro lorsque le réseau est disponible (voir workers et enqueue dans le code).

### Photos & signatures

- **`PhotoSyncRepository`** (et flux associés) gère l’upload des photos / signatures en attente, souvent déclenché depuis le **planning** (`pull` / refresh) en complément du push interventions.

### Authentification

- Jetons dans **DataStore** ; les requêtes HTTP portent l’auth via intercepteur.
- En **401**, **`AuthEventBus`** notifie → retour vers **`Login`** (stack nettoyée).

---

## UI adaptative (téléphone / tablette)

- **`rememberSavioWindowSize`**, dérivé de **`WindowSizeClass`** : en fenêtre **élargie** (`EXPANDED`), la **tournée** utilise **`TourneeTabletScreen`** : sidebar liste + **détail** dans un second `NavHost` (`TourneeAdaptiveLayout`, `TourneeDetailNavHost`).
- En mode **compact**, **`TourneeScreen`** (liste plein écran + navigation vers le détail).

---

## Écrans et navigation (aperçu)

Les routes sont définies dans **`Screen`** (`AppNavigation.kt`). Flux principal :

| Zone | Écran / composable | Rôle |
|------|---------------------|------|
| Auth | `LoginScreen`, `SelectSocieteScreen` | Connexion, choix de société si applicable |
| Planning | `TourneeScreen` / `TourneeTabletScreen` | Liste du jour, changement de date, refresh, bannière **reprise d’intervention** si `in_progress` en base |
| Détail | `InterventionDetailScreen` | Fiche intervention avant / pendant accès (démarrer, infos) |
| Terrain | `InterventionActiveScreen` | Intervention **en cours** : adresse, équipements, photos, facture, **clôturer**. Retour système **bloqué** ; sortie volontaire via **croix** (dialogue de confirmation → reset local + retour planning) |
| Clôture | `ClotureRapportScreen` → `ClotureSignatureScreen` | Rapport, types réels, signatures puis retour tournée |
| Facturation | `InvoiceScreen` | Facture liée à l’intervention (brouillon local, validation selon règles) |
| Annexes | `ClientDetailScreen`, `EquipementDetailScreen` | Fiches depuis l’intervention active |
| Photos | `PhotosScreen`, `CameraScreen` | Galerie / prise de vue **CameraX** |

Navigation globale : **`NavHost`** racine dans `AppNavigation` ; côté tablette, un **`NavHost`** imbriqué pour le volet détail (`TourneeDetailNavHost`).

---

## Base de données Room

- Fichier SQLite géré par **`SavioDatabase`** (nom côté app selon `DatabaseModule`).
- **Migrations** explicites dans `SavioMigrations.kt` ; en développement, un secours destructif peut exister — en production, privilégier des migrations testées à chaque évolution de schéma.

---

## Build & exécution

```bash
# Windows
gradlew assembleDebug

# Linux / macOS
./gradlew assembleDebug
```

Ouvrir la racine du repo dans **Android Studio**, synchroniser Gradle, lancer le module **`app`**.

**Tests** : répertoires `test/` et `androidTest/` — à faire évoluer selon la stratégie d’équipe.

---

## Conventions utiles (contributeurs)

- **Package** : `re.melchior.saviomobile`.
- **DTOs** : suffixe `Dto`, champs alignés JSON (`@SerializedName` si besoin).
- **Entités Room** : suffixe `Entity` ; navigation : routes et `createRoute(...)` dans `Screen`.
- **Clôture multi-types** : types réels stockés / transmis (voir `InterventionActualTypeEntity`, arguments de navigation clôture).

---

## Licence & contact

À compléter selon la politique interne (propriétaire, équipe, support).
