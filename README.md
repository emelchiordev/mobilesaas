# Savio Mobile (saviomobilite)

Application Android pour **techniciens de terrain** : consultation des interventions du jour, suivi sur site, photos, compte-rendu, signatures et synchronisation avec un backend HTTP.

---

## Objectif produit

Permettre au technicien de :

- Voir sa **tournée** (interventions planifiées par date).
- **Démarrer** une intervention, consulter client / équipements / contrat, prendre des **photos**.
- **Clôturer** une intervention : compte-rendu, choix du ou des **types réels** d’intervention (chips), signatures technicien / client selon les règles métier, envoi des opérations au serveur.
- Fonctionner **hors ligne partiellement** : données en cache locale (Room), file d’opérations à pousser, synchro périodique (WorkManager).

---

## Prérequis

| Élément | Version / remarque |
|--------|---------------------|
| Android Studio | Hedgehog ou plus récent recommandé |
| JDK | **11** (configuré dans Gradle) |
| SDK compile | **36** (voir `app/build.gradle.kts`) |
| minSdk | **28** |
| Appareil / émulateur | API 28+ |

---

## Stack technique

- **Langage** : Kotlin  
- **UI** : Jetpack **Compose** + Material 3  
- **Architecture** : MVVM, repositories, couche `data` (local + remote)  
- **Injection** : **Hilt** (Dagger)  
- **Réseau** : **Retrofit** + **OkHttp** (Gson), intercepteur d’auth  
- **Persistance** : **Room** (SQLite), **DataStore** (tokens / session)  
- **Tâches en arrière-plan** : **WorkManager** (synchro push / sync)  
- **Images** : Coil, **CameraX** pour la capture photo  

---

## Configuration backend

L’URL de base de l’API est définie dans `app/build.gradle.kts` :

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://…/\"")
```

Elle est exposée en **`BuildConfig.API_BASE_URL`** et utilisée par `NetworkModule` (Retrofit). **À adapter** selon l’environnement (machine locale, staging, production). Pour du multi-environnement propre, on pourra plus tard externaliser (flavors, `local.properties`, etc.).

---

## Architecture du code (`app/src/main/java/re/melchior/saviomobile`)

| Dossier | Rôle |
|--------|------|
| `data/local` | Entités Room, DAOs, `SavioDatabase`, migrations, `TokenDataStore` |
| `data/remote` | APIs Retrofit (`AuthApi`, `SyncApi`, `PushApi`, `TourneeApi`, `DocumentApi`…), DTOs Gson, intercepteurs (`AuthInterceptor`, `AuthEventBus`) |
| `data/repository` | `AuthRepository`, `SyncRepository`, `PushRepository`, `PhotoRepository`, etc. |
| `di` | Modules Hilt (`NetworkModule`, `ApiModule`, `DatabaseModule`, `WorkerModule`…) |
| `ui/navigation` | `AppNavigation` : graphe Compose, routes écran |
| `ui/screen` | Écrans par domaine : `auth`, `tournee`, `intervention` (dont `cloture`) |
| `ui/theme` | Thème Material / couleurs Savio |
| `worker` | `SyncWorker` : synchronisation en tâche de fond |

**Point d’entrée** : `MainActivity` — thème, navigation, enregistrement du worker périodique de synchro.

---

## Flux métier importants

### Authentification

Connexion via API ; jetons stockés (DataStore) ; les appels HTTP passent par un intercepteur qui attache l’auth ; en cas de **401**, événement global pour renvoyer vers l’écran de login.

### Synchronisation des interventions

- **Pull** : `SyncApi` — récupération des interventions du jour (et référentiels), écriture Room avec logique de **fusion** (ne pas écraser une intervention déjà terminée localement avec les données serveur complètes).
- **Types réels** : le pull peut fournir une liste `actualTypes` par intervention ; stockage dans la table `intervention_actual_types` (Room v10+).
- **Push** : `PushApi` — envoi des opérations `START_INTERVENTION`, `COMPLETE_INTERVENTION`, etc. Le **COMPLETE** inclut notamment `report`, `completedAt`, **`actualTypeIds`** (liste) et **`actualTypeId`** (premier id, compatibilité).

### Clôture d’intervention

1. **Étape rapport** : saisie du compte-rendu si requis par les types sélectionnés ; sélection **multiple** des types réels (chips Material 3).
2. **Étape signatures** : même sélection (reprise via argument de navigation) ; signature technicien ; signature client si au moins un type exige `requireClientSignature` ; alerte si passage d’une VE planifiée à des types non-VE.
3. Persistance locale puis **WorkManager** pour pousser la synchro quand le réseau est disponible.

---

## Base de données Room

- Fichier : `savio.db` (voir `DatabaseModule`).
- **Migrations** : ex. `MIGRATION_9_10` pour la table `intervention_actual_types`. Un `fallbackToDestructiveMigration()` reste en secours pour le développement ; en production, privilégier des migrations explicites.

---

## Build & exécution

```bash
# Windows (PowerShell / cmd)
gradlew assembleDebug

# Linux / macOS
./gradlew assembleDebug
```

Ouvrir le dossier racine dans **Android Studio**, synchroniser Gradle, choisir un module `app`, lancer sur un appareil ou un émulateur.

**Tests** : `test/` (unitaires), `androidTest/` (instrumentés) — à enrichir selon la stratégie d’équipe.

---

## Conventions utiles pour les contributeurs

- **Package** : `re.melchior.saviomobile`.
- **DTOs** : suffixe `Dto`, champs alignés sur le JSON serveur (`@SerializedName`).
- **Entités Room** : suffixe `Entity` ; clôture multi-types : `InterventionActualTypeEntity` liée à `InterventionEntity`.
- **Navigation** : routes centralisées dans `Screen` (`AppNavigation.kt`) ; arguments encodés quand nécessaire (ex. clés de types séparées pour la clôture).

---

## Ressources & dépendances

Les versions sont pilotées par **`gradle/libs.versions.toml`** (Compose BOM, Room, Hilt, Retrofit, etc.). En cas de montée de version, vérifier les notes de migration AndroidX / Compose.

---

## Licence & contact

À compléter selon la politique du projet (propriétaire, équipe, canal support interne).
