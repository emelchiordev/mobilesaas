# Contexte projet Savio Mobile — module Attestation VE

Ce document sert à expliquer à une autre IA ce qui a été mis en place dans l’app Android **Savio Mobile** (Kotlin, Jetpack Compose, Room, Hilt), avec un focus sur l’**attestation d’entretien** (type « visite d’entretien » / équipements thermiques).

---

## 1. Objectif fonctionnel

Sur la fiche **équipement** d’une intervention, l’utilisateur peut ouvrir une **attestation d’entretien** : il choisit d’abord un **type d’installation** (gaz, fioul, bois, PAC, hybrides), puis remplit une fiche structurée :

- **Mesures** (CO, fumées, rendement, PAC / frigo, etc. selon le type).
- **Points de contrôle** (listes prédéfinies par type, avec statut par ligne).
- **Conclusions** (défauts, recommandations, commentaire, personne présente, remarques par domaine hydraulique / régulation / générateur).

Les données sont **persistées en local (Room)** et **poussées vers le serveur** lors du sync push, sous forme d’opération `SAVE_ATTESTATION_VE`.

---

## 2. Types d’attestation (`type` : chaîne)

Valeurs utilisées dans l’UI et en base :

| Code | Signification (libellé UI) |
|------|----------------------------|
| `GAZ` | Chaudière gaz |
| `FIOUL` | Chaudière fioul |
| `BOIS` | Chaudière bois |
| `PAC` | PAC |
| `PAC_HYBRIDE_GAZ` | PAC hybride gaz |
| `PAC_HYBRIDE_FIOUL` | PAC hybride fioul |

**Clé métier** : une attestation est identifiée par le triplet `(interventionId, equipmentOrder, type)`. Plusieurs attestations peuvent exister pour le même équipement si les types diffèrent.

---

## 3. Entrée utilisateur : choix du type

Fichiers principaux :

- `EquipementDetailScreen.kt` : carte **« Attestation d’entretien »** (masquée si l’équipement est `typeCode == "replaced"`). Clic → bottom sheet de choix du type.
- `AttestationTypePickerSheet.kt` : liste des types avec **suggestion** mise en avant.

La **suggestion** `suggestedAttestationType` est dérivée de `equipment.typeCode` et `equipment.energyCode` (PAC / hybride / gaz / fioul / bois, etc.). L’utilisateur peut néanmoins choisir n’importe quel type proposé.

Navigation : `Screen.AttestationVe` — route  
`attestation-ve/{interventionId}/{equipmentOrder}/{type}`  
(voir `Screen.kt` et branche correspondante dans `AppNavigation.kt`).

---

## 4. Modèle de données (Room)

### 4.1 Table `attestation_ve` — `AttestationVeEntity`

- **PK** : `id` (UUID généré à la création).
- **Filtrage logique** : `interventionId`, `equipmentOrder`, `type`.
- Champs texte pour libellés / remarques / personne présente.
- Champs mesures (souvent saisis comme **texte** côté UI, convertis en **nombre** au push quand c’est pertinent).
- `isDirty` / `updatedAt` : suivi pour synchronisation (comme d’autres entités « dirty » du projet).

Création / migration : `SavioMigrations.kt` — `MIGRATION_24_25`.

### 4.2 Table `attestation_ve_point_controle` — `AttestationVePointControleEntity`

Une ligne par **point de contrôle** renseigné :

- `cle` : identifiant stable du point (ex. `HYD_PURGE`, `GAZ_GEN_SECU`).
- `resultat` : **`V`** (validé), **`N`** (non validé), **`S`** (sans objet — uniquement si le point le permet dans la définition).
- Si le résultat est vidé, la ligne peut être **supprimée** (voir `AttestationVeRepository.savePoint`).

**PK** : `id` = `"$attestationId-$cle"`.

---

## 5. Listes de points de contrôle (hors base)

Fichier **`AttestationVeControlPoints.kt`** : objet singleton qui expose des listes de `ControlPoint` (clé, libellé, type de ligne HEAD / SUBHEAD / BODY, flag `hasSansObjet`).

La fonction **`getPointsForType(type)`** concatène les blocs adaptés :

- Gaz / Fioul / Bois : hydraulique + régulation + bloc générateur correspondant.
- PAC : blocs PAC hydraulique, PAC régulation, PAC générateur.
- Hybrides : mêmes blocs PAC **+** générateur gaz ou fioul en plus.

L’UI (`AttestationVeScreen`) affiche ces listes et compte les `V` / `N` pour un indicateur de progression (voir `AttestationVeViewModel`).

---

## 6. Couche repository

**`AttestationVeRepository`** :

- `getOrCreate(interventionId, equipmentOrder, type)` : récupère ou insère une ligne `attestation_ve`.
- `save(entity)` : met à jour avec `isDirty = true` et `updatedAt`.
- `getFlow(...)` : observe les attestations de l’intervention et filtre sur équipement + type (implémentation actuelle basée sur un flow « par intervention »).
- `getPointsFlow` / `savePoint` : points de contrôle.
- `getDirty` / `markClean` : pour le push.

---

## 7. ViewModel et écran

- **`AttestationVeViewModel`** : charge l’entité, les points, applique les mises à jour de champs, **auto-save** avec délai (~500 ms) + save synchrone au `onCleared`.
- **`AttestationVeScreen`** : onglets / sections (mesures variables selon `type`, onglet points de contrôle, conclusions).

---

## 8. Synchronisation (push)

**`PushRepository`** (construction du batch de push) :

- Récupère les attestations **dirty** via `AttestationVeRepository.getDirty()`.
- Pour chacune, charge les points avec `AttestationVePointControleDao.getByAttestationOnce(...)`.
- Émet une opération **`SAVE_ATTESTATION_VE`** avec un **id d’opération** déterministe :  
  `{interventionId}_attestation_{equipmentOrder}_{type}`  
  (pour corréler la réponse serveur et appeler `markClean`).

**`buildAttestationPayload`** : map JSON-friendly — nombres via `toDoubleOrNull()`, chaînes vides → `null`, plus un tableau **`points`** : `{ cle, resultat }[]`.

> Remarque : à ce stade, la récupération **pull** des attestations depuis l’API de sync (`SyncDto`, etc.) n’a pas été repérée pour ce module ; le flux documenté ici est surtout **création / édition locale + push**.

---

## 9. Périmètre voisin (à ne pas confondre)

- **Mesures analytiques** (`MeasureEntity`, écran mesures, opération `SAVE_MEASURE`) : flux parallèle pour les mesures « fumées » / protocolaires, distinct de l’attestation VE.
- **CERFA fluides frigorigènes** : autre fonctionnalité (template PDF `cerfa15497_04.pdf`, `CerfaPdfService`, écrans `Cerfa*`). Ce n’est **pas** le même écran que l’attestation VE, même si tout peut apparaître sur la même fiche équipement.

---

## 10. Fichiers utiles (index rapide)

| Rôle | Fichiers |
|------|----------|
| Entités Room | `AttestationVeEntity.kt`, `AttestationVePointControleEntity.kt` |
| DAO | `AttestationVeDao.kt`, `AttestationVePointControleDao.kt` |
| Repository | `AttestationVeRepository.kt` |
| Points prédéfinis | `AttestationVeControlPoints.kt` |
| UI attestation | `AttestationVeScreen.kt`, `AttestationVeViewModel.kt`, `AttestationTypePickerSheet.kt` |
| Point d’entrée UI | `EquipementDetailScreen.kt` (carte attestation + suggestion) |
| Navigation | `Screen.kt` (`AttestationVe`), `AppNavigation.kt` |
| Push | `PushRepository.kt` (`SAVE_ATTESTATION_VE`, `buildAttestationPayload`) |
| Migration DB | `SavioMigrations.kt` (`MIGRATION_24_25`) |
| DI Room | `SavioDatabase.kt`, `DatabaseModule.kt` |

---

## 11. Évolutions possibles (pour une IA qui continue le travail)

- Aligner le **pull** serveur si le backend renvoie des attestations (DTO + import Room).
- Vérifier la **cohérence** `getFlow` du repository si le volume d’attestations par intervention augmente (filtrage côté DAO possible).
- Harmoniser les **codes type** avec le catalogue équipement backend si les chaînes divergent.
- Tests instrumentés / unitaires sur `buildAttestationPayload` et sur les transitions dirty/clean.

---

*Document généré pour faciliter le transfert de contexte entre sessions ou outils IA — avril 2026.*
