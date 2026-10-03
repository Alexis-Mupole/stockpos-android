# Architecture Technique & Guide Développeur / Technical Architecture & Developer Guide 📐

> **🇫🇷 Comment StockPOS reste rapide, fiable et gratuit pour les PME.**
> **🇬🇧 How StockPOS stays fast, reliable, and free for SMEs.**

**🌐 Langue / Language:** [🇫🇷 Français](#) · [🇬🇧 English](#)

---

## 🎯 Objectif Architectural / Architectural Goal

**🇫🇷 Français**

L'architecture de StockPOS est dictée par un seul objectif : **permettre à une PME de vendre et d'inventorier ses produits aussi vite qu'un grand commerce, sur un téléphone d'entrée de gamme, sans dépendre d'un serveur et sans payer d'abonnement.**

Cela se traduit par des contraintes de conception non négociables :

| Contrainte / Constraint | Raison / Reason |
|---|---|
| **Zéro latence en caisse** | Un commerçant ne peut pas attendre qu'un serveur réponde pour encaisser un client. |
| **Fonctionnement 100% hors-ligne** | Les coupures réseau sont la norme, pas l'exception, dans de nombreuses régions. |
| **Coût de possession nul** | Aucun serveur, aucun abonnement : l'app doit tenir dans un APK Android standard. |
| **Souveraineté des données** | Les données de ventes appartiennent au commerçant. |
| **Robustesse financière** | Une erreur de calcul ou une vente perdue doit être impossible. |

**🇬🇧 English**

StockPOS's architecture is driven by a single goal: **let an SME sell and track inventory as efficiently as a large retail chain, on an entry-level phone, without depending on a server and without paying a subscription.**

This translates into non-negotiable design constraints:

| Constraint | Reason |
|---|---|
| **Zero-latency checkout** | A shop owner cannot wait for a server to ring up a customer. |
| **100% offline operation** | Network outages are the norm, not the exception, in many regions. |
| **Zero cost of ownership** | No server, no subscription: the app must fit in a standard Android APK. |
| **Data sovereignty** | Sales data belongs to the merchant. |
| **Financial robustness** | A miscalculation or a lost sale must be impossible. |

---

## 1. Principes Fondamentaux / Fundamental Principles

**🇫🇷 Français**

1. **Offline-First** : La source de vérité est la base de données **Room (SQLite)** locale. Aucune action utilisateur critique ne dépend d'un appel réseau synchrone.
2. **Clean Architecture & MVVM** : Découplage strict entre la présentation (Jetpack Compose), la logique métier (UseCases), et l'accès aux données (Repositories & DAOs).
3. **Réactivité & Flots Asynchrones** : Utilisation intensive de Kotlin Coroutines et de `StateFlow` / `Flow` pour propager instantanément les modifications de données de la base vers l'UI.
4. **Argent en centimes (minor units)** : Tous les montants monétaires sont stockés en `Long` représentant des centimes, jamais en `Double`, afin d'éliminer les erreurs d'arrondi flottant.
5. **Prix figés à la vente** : Le prix unitaire et le coût unitaire sont capturés au moment de la transaction ; tous les totaux sont calculés en `Long`.

**🇬🇧 English**

1. **Offline-First**: The source of truth is the local **Room (SQLite)** database. No critical user action depends on a synchronous network call.
2. **Clean Architecture & MVVM**: Strict decoupling between the presentation layer (Jetpack Compose), business logic (UseCases), and data access (Repositories & DAOs).
3. **Reactivity & asynchronous flows**: Heavy use of Kotlin Coroutines and `StateFlow` / `Flow` to propagate data changes instantly from the database to the UI.
4. **Money in minor units**: All monetary amounts are stored as `Long` in minor units, never as `Double`, to eliminate floating-point rounding errors.
5. **Prices frozen at sale time**: Unit price and unit cost are captured at transaction time, so later catalogue edits cannot corrupt historical reports. All totals computed in `Long`.

---

## 2. Structure des Couches / Layer Structure

```
┌────────────────────────────────────────────────────────┐
│                   PRESENTATION (UI)                    │
│   Jetpack Compose (M3), ViewModels, Screens, Theme     │
└───────────────────────────▲────────────────────────────┘
                            │ observes StateFlow
┌───────────────────────────┴────────────────────────────┐
│                      DOMAIN LAYER                      │
│   UseCases (Checkout, ScanProduct, AdjustStock, Auth)  │
│   Domain Models (CartState, ReceiptFormatter, PinHash) │
└───────────────────────────▲────────────────────────────┘
                            │ calls
┌───────────────────────────┴────────────────────────────┐
│                       DATA LAYER                       │
│   Repositories (ProductRepo, SaleRepo, SettingsRepo)   │
│   Room Database (DAOs, Entities, Converters, POJOs)    │
│   Local Export (DataExportManager, FileProvider)       │
│   Cloud Backup (Google Drive REST API AppData) 🚧     │
│   Background Tasks (WorkManager CoroutineWorkers)      │
└────────────────────────────────────────────────────────┘
```

**🇫🇷 Français**

Le sens de la dépendance est **toujours vers le bas** : la couche UI ne connaît que la couche domaine, et la couche domaine ne connaît que des interfaces. C'est ce qui permet de remplacer le stockage local ou le cloud sans toucher à l'interface.

**🇬🇧 English**

The dependency direction is **always downward**: the UI layer only knows the domain layer, and the domain layer only knows interfaces. This is what allows local storage or the cloud backend to be swapped without touching the interface.

---

## 3. Schéma Relationnel Room (Entities & DAOs) / Room Relational Schema

### `CategoryEntity` / Entité Catégorie

| Champ / Field | Type | Description / Description |
|---|---|---|
| `id` | Long | Clé primaire auto-générée / Auto-generated primary key |
| `name` | String | Nom de la catégorie / Category name |
| `colorHex` | String | Couleur d'affichage (défaut `#1E88E5`) / Display colour |
| `iconName` | String | Nom d'icône Material 3 (défaut `category`) / Material 3 icon name |
| `isArchived` | Boolean | Archivage logique (soft delete) / Soft delete |

**🇬🇧 English** — Categories are **soft-deleted** via `isArchived`, so a product that was previously categorised never loses its history.

### `ProductEntity` / Entité Produit

| Champ / Field | Type | Description / Description |
|---|---|---|
| `id` | Long | Clé primaire auto-générée / Auto-generated primary key |
| `name` | String | Nom de l'article / Item name |
| `sku` | String | Référence interne / Internal reference |
| `categoryId` | Long | Clé étrangère vers `CategoryEntity.id` |
| `supplierId` | Long? | Fournisseur optionnel / Optional supplier |
| `unit` | String | Unité de vente (défaut `pcs`) / Sales unit |
| `costPrice` | Long | Coût d'achat **en centimes** / Cost price **in minor units** |
| `salePrice` | Long | Prix de vente **en centimes** / Sale price **in minor units** |
| `stockQty` | Int | Quantité en stock / Stock quantity |
| `reorderLevel` | Int | Seuil de réapprovisionnement (défaut `5`) / Reorder threshold |
| `expiryDate` | Long? | Date de péremption (null = non périmé) / Expiry date (null = no expiry) |
| `imagePath` | String? | Image en stockage privé / Image in private storage |
| `isActive` | Boolean | Produit actif / Active product |
| `createdAt` / `updatedAt` | Long | Horodatages UTC / UTC timestamps |

**🇫🇷 Français** — Les prix sont stockés en **centimes dans un `Long`** (`salePrice`, `costPrice`), jamais en `Double` : c'est la garantie qu'une addition de 100 lignes ne dérive pas d'un centime.

**🇬🇧 English** — Prices are stored as **minor units in a `Long`** (`salePrice`, `costPrice`), never as `Double`: this guarantees that summing 100 line items never drifts by a single cent.

### `ProductBarcodeEntity` / Entité Code-Barres

| Champ / Field | Type | Description / Description |
|---|---|---|
| `id` | Long | Clé primaire / Primary key |
| `productId` | Long | Produit associé / Owning product |
| `barcode` | String | EAN-13, Code 128, QR Code ou SKU / EAN-13, Code 128, QR Code, or SKU |
| `packQty` | Int | Taille du conditionnement (défaut `1`) / Pack size |

**🇫🇷 Français** — **Un produit peut porter plusieurs codes-barres** (un par conditionnement : unite, pack de 6, pack de 12). C'est essential en commerce de détail : le même article est scanné sous plusieurs empaquetages.

**🇬🇧 English** — **A product can carry multiple barcodes** (one per pack size: single unit, pack of 6, pack of 12). This is essential in retail: the same item is scanned under several packagings.

### `ProductWithBarcodesAndCategory` / Relation Produit

POJO Room combinant le produit, sa catégorie, son fournisseur et **tous** ses codes-barres :

| Relation | Colonnes | Type |
|---|---|---|
| `product` | `@Embedded` | `ProductEntity` |
| `category` | `categoryId` → `id` | `CategoryEntity?` |
| `supplier` | `supplierId` → `id` | `SupplierEntity?` |
| `barcodes` | `id` → `productId` | `List<ProductBarcodeEntity>` |

**🇬🇧 English** — Room POJO combining the product, its category, its supplier, and **all** of its barcodes. This is the read model used by the inventory screen and the barcode lookup, so scanning a product resolves category, price, and stock in a single reactive query.

### `SupplierEntity` / Fournisseur & `CustomerEntity` / Client

**🇫🇷 Français** — `SupplierEntity` (nom, téléphone, email, adresse, notes, `isArchived`) et `CustomerEntity` (mêmes champs + `balanceOwed: Long` en centimes pour le suivi des dettes clients, très courant dans le commerce de proximité).

**🇬🇧 English** — `SupplierEntity` (name, phone, email, address, notes, `isArchived`) and `CustomerEntity` (same fields plus `balanceOwed: Long` in minor units to track customer credit, which is very common in small local retail).

### `StockMovementEntity` / Mouvement de Stock

**🇫🇷 Français** — Chaque variation de stock est journalisée (`productId`, `type`, `qtyChange`, `qtyAfter`, `unitCost`, `referenceType`, `referenceId`, `userId`). Le type est un `StockMovementType` : `SALE`, `RETURN`, `PURCHASE`, `ADJUSTMENT`, `DAMAGE`, `VOID`.

**🇬🇧 English** — Every stock change is journalled. The type is a `StockMovementType`: `SALE`, `RETURN`, `PURCHASE`, `ADJUSTMENT`, `DAMAGE`, `VOID`. This ledger is what makes shrinkage (casse, vol, erreur de saisie) auditable rather than mysterious.

### `SaleEntity`, `SaleItemEntity`, `PaymentEntity`

| Entité / Entity | Champs clés / Key fields |
|---|---|
| `SaleEntity` | `receiptNo`, `customerId?`, `userId`, `subtotal`, `discountTotal`, `taxTotal`, `total`, `amountPaid`, `changeGiven`, `status`, `note`, `createdAt` |
| `SaleItemEntity` | `saleId`, `productId`, `productNameSnapshot`, `barcodeSnapshot`, `qty`, `unitPriceSnapshot`, `unitCostSnapshot`, `discount`, `taxAmount`, `lineTotal` |
| `PaymentEntity` | `saleId`, `method`, `amount`, `reference?` |

**🇫🇷 Français**

* `SaleEntity.status` est un `SaleStatus` : `COMPLETED`, `REFUNDED`, `PARTIALLY_REFUNDED`, `VOIDED`.
* `SaleItemEntity` porte des **snapshots** (`productNameSnapshot`, `unitPriceSnapshot`, `unitCostSnapshot`) : renommer un produit ou changer son prix **ne réécrit jamais l'historique**. Le rapport de marge d'il y a six mois reste exact.
* `PaymentEntity.method` est un `PaymentMethod` : `CASH`, `MOBILE_MONEY`, `CARD`. Plusieurs lignes par vente permettent le **paiement fractionné** (ex. 50% Espèces + 50% Mobile Money).

**🇬🇧 English**

* `SaleEntity.status` is a `SaleStatus`: `COMPLETED`, `REFUNDED`, `PARTIALLY_REFUNDED`, `VOIDED`.
* `SaleItemEntity` carries **snapshots** (`productNameSnapshot`, `unitPriceSnapshot`, `unitCostSnapshot`): renaming a product or changing its price **never rewrites history**. Last month's margin report stays exact.
* `PaymentEntity.method` is a `PaymentMethod`: `CASH`, `MOBILE_MONEY`, `CARD`. Multiple rows per sale enable **split tender** (e.g. 50% cash + 50% Mobile Money).

### `SaleWithItemsAndPayments` / Relation Vente

POJO Room combinant la vente, ses lignes (`items`), ses paiements (`payments`), le vendeur (`user`) et le client (`customer`) — tout ce dont l'écran d'historique a besoin en une requête.

**🇬🇧 English** — Room POJO combining the sale, its line items, its payments, the seller, and the customer — everything the sales-history screen needs in a single query.

### `UserEntity` / Entité Utilisateur

| Champ / Field | Type | Description / Description |
|---|---|---|
| `id` | Long | Clé primaire / Primary key |
| `name` | String | Nom affiché / Display name |
| `identifier` | String | Identifiant de connexion / Login identifier |
| `pinHash` | String | **Empreinte PBKDF2 du code PIN** / PBKDF2 PIN hash |
| `pinSalt` | String | Sel aléatoire (Base64) / Random salt (Base64) |
| `role` | `UserRole` | `ADMIN`, `MANAGER`, `SELLER` |
| `isActive` | Boolean | Compte actif / Active account |
| `mustChangePin` | Boolean | PIN temporaire à changer / Temporary PIN to change |

**🇬🇧 English** — `role` is a `UserRole`: **`ADMIN`, `MANAGER`, `SELLER`**. Note that neither the PIN nor its hash is ever exposed outside this entity.

---

## 4. Sécurité : Hachage des Codes PIN / Security: PIN Hashing

**🇫🇷 Français**

L'implémentation se trouve dans `domain/security/PinHasher.kt` :

| Paramètre / Parameter | Valeur / Value |
|---|---|
| Algorithme / Algorithm | `PBKDF2WithHmacSHA256` |
| Itérations / Iterations | 12 000 |
| Longueur de clé / Key length | 256 bits |
| Sel / Salt | 16 octets aléatoires (`SecureRandom`) / 16 random bytes (`SecureRandom`) |
| Comparaison / Comparison | `MessageDigest.isEqual` (temps constant / constant-time) |

**Les codes PIN en clair ne sont jamais stockés sur disque ni journalisés, et ne restent en mémoire que le temps nécessaire au hachage.**

**🇬🇧 English**

The implementation lives in `domain/security/PinHasher.kt`:

* **Raw PINs are never written to disk or logs, and never remain in memory longer than necessary for hashing.**

PBKDF2 is a deliberately slow **key-stretching** function: its cost makes brute-forcing a 4–6 digit PIN expensive for an attacker who steals the database, while staying imperceptible (<100 ms) for the legitimate shopkeeper logging in.

---

<a id="drive-backup"></a>

## 5. Sauvegarde & Restauration Google Drive — 🚧 *Prévu / Planned*

> **⚠️ Statut / Status : À IMPLÉMENTER / NOT YET IMPLEMENTED**
> Cette section spécifie le **design cible** de la sauvegarde cloud. Elle ne correspond pas encore à du code existant : le projet ne contient actuellement **ni dépendance Google Drive, ni `DriveScopes`, ni `backupDataToDrive` / `restoreDataFromDrive`**. Les implémentations des usages ci-dessous sont les bienvenues.
>
> **This section specifies the target design for cloud backup. It does not yet correspond to existing code:** the project currently contains **no Google Drive dependency, no `DriveScopes`, and no `backupDataToDrive` / `restoreDataFromDrive`**. Implementations of the use cases below are welcome.

### Processus de Sauvegarde (`backupDataToDrive`) — *à implémenter / to implement*

**🇫🇷 Français**

1. Extraction complète de la base Room sous forme de structure JSON sécurisée :
   * Schéma de version
   * Date UTC d'exportation
   * Tables `categories`, `products`, `sales`, `sale_items`, `payments`
   * Paramètres de boutique (`BusinessSettings`)
2. Requête vers l'API Google Drive v3 :
   * Recherche d'un fichier `pos_secure_backup.json` dans le dossier spécial `appDataFolder`.
   * Si existant : mise à jour via `files().update()`.
   * Si inexistant : création via `files().create()` avec `parents = listOf('appDataFolder')`.
3. Retour du statut (Success/Failure) et mise à jour de l'horodatage dans les paramètres.

**🇬🇧 English**

1. Full extraction of the Room database into a secure JSON structure:
   * Schema version
   * UTC export date
   * `categories`, `products`, `sales`, `sale_items`, `payments` tables
   * Business settings (`BusinessSettings`)
2. Query the Google Drive v3 API:
   * Look for `pos_secure_backup.json` in the special `appDataFolder`.
   * If present: update it via `files().update()`.
   * If absent: create it via `files().create()` with `parents = listOf('appDataFolder')`.
3. Return a status (Success/Failure) and update the timestamp in settings.

### Processus de Restauration (`restoreDataFromDrive`) — *à implémenter / to implement*

**🇫🇷 Français**

1. Requête du dossier `appDataFolder` pour trouver `pos_secure_backup.json`.
2. Téléchargement du flux vers un fichier tampon local.
3. Désérialisation et validation de l'intégrité du JSON.
4. **Transaction atomique Room** (`runInTransaction`) pour réinsérer catégories, produits, ventes et paramètres, sans risque de corruption en cas d'interruption.

**🇬🇧 English**

1. Query the `appDataFolder` for `pos_secure_backup.json`.
2. Download the stream to a local temporary file.
3. Deserialize and validate JSON integrity.
4. **Atomic Room transaction** (`runInTransaction`) to reinsert categories, products, sales, and settings, with no risk of corruption if interrupted.

### 🛡️ Portée du Accès / Access Scope

**🇫🇷 Français**

Seul le scope `DriveScopes.DRIVE_APPDATA` est demandé. L'application ne peut **accéder ni lire aucun fichier personnel** de l'utilisateur : la sauvegarde vit dans un dossier caché que l'utilisateur ne voit pas dans son Drive et que seul StockPOS peut lire.

**🇬🇧 English**

Only the `DriveScopes.DRIVE_APPDATA` scope is requested. The app **cannot access or read any of the user's personal files**: the backup lives in a hidden folder the user cannot see in their Drive and that only StockPOS can read.

---

## 6. Tâches de fond avec Jetpack WorkManager / Background Tasks with WorkManager

**🇫🇷 Français**

* **✅ Implémenté : `LowStockNotificationWorker`** —_notify l'utilisateur lorsque des produits passent sous leur seuil de réapprovisionnement (`reorderLevel`). C'est le seul `Worker` du projet à ce jour.
* **🚧 Prévu : sauvegardes périodiques** — L'architecture cible prévoit un `CoroutineWorker` avec `PeriodicWorkRequestBuilder` pour les sauvegardes cloud described en [section 5](#5-sauvegarde--restauration-google-drive--prévu--planned). Ce worker **n'existe pas encore**.
* **Contraintes prévues** : `NetworkType.UNMETERED` pour ne déclencher les sauvegardes lourdes qu'en Wi-Fi — une PME ne doit pas brûler sa data mobile pour un backup — et `requiresBatteryNotLow(true)`, car un téléphone de caisse ne doit pas s'éteindre au milieu de la journée de vente.

**🇬🇧 English**

* **✅ Implemented: `LowStockNotificationWorker`** — notifies the user when products fall below their reorder threshold (`reorderLevel`). It is currently the only `Worker` in the project.
* **🚧 Planned: periodic backups** — The target architecture calls for a `CoroutineWorker` with `PeriodicWorkRequestBuilder` for the cloud backups described in [section 5](#drive-backup). This worker **does not exist yet**.
* **Planned constraints**: `NetworkType.UNMETERED` so heavy backups only run on Wi-Fi — an SME should not burn mobile data on a backup — and `requiresBatteryNotLow(true)`, because a register phone must not die in the middle of a trading day.

---

## 7. Impression Thermique ESC/POS & Reçus PDF / ESC/POS Thermal Printing & PDF Receipts

**🇫🇷 Français**

* **Bluetooth ESC/POS** : Connexion RFCOMM directe avec le profil SPP UUID `00001101-0000-1000-8000-00805F9B34FB`. Génération des octets ESC/POS (alignement, mise en gras, découpe papier `GS V 66 0`).
* **Android `PdfDocument`** : Génération de pages vectorielles de reçu avec en-tête, tableau d'articles, totaux, ventilation de taxes et pied de page. Partageable instantanément via `FileProvider` — utile pour les clients qui demandent un reçu par WhatsApp.

**🇬🇧 English**

* **Bluetooth ESC/POS**: Direct RFCOMM connection using SPP profile UUID `00001101-0000-1000-8000-00805F9B34FB`. Generates ESC/POS byte streams (alignment, bold, paper cut `GS V 66 0`).
* **Android `PdfDocument`**: Vector receipt pages with header, line-item table, totals, tax breakdown, and footer. Instantly shareable via `FileProvider` — useful when customers ask for a receipt over WhatsApp.

---

## 8. Internationalisation / Internationalization

**🇫🇷 Français**

La couche `util/AppStrings.kt` fournit un système bilingue dynamique (**français / anglais**) sans redémarrage, complété par les ressources Android `res/values/strings.xml` (anglais) et `res/values-fr/strings.xml` (français). Ajouter une troisième langue revient à ajouter un `values-xx/strings.xml` et une entrée dans `AppStrings`.

**🇬🇧 English**

`util/AppStrings.kt` provides a dynamic bilingual system (**French / English**) with no restart, complemented by the Android resources `res/values/strings.xml` (English) and `res/values-fr/strings.xml` (French). Adding a third language means adding a `values-xx/strings.xml` folder and an entry in `AppStrings`.

---

## 📚 Documents Liés / Related Documents

* [`README.md`](README.md) — Présentation du projet et fonctionnalités / Project overview and features
* [`CONTRIBUTING.md`](CONTRIBUTING.md) — Guide de contribution / Contribution guide
* [`CONTRIBUTORS.md`](CONTRIBUTORS.md) — Liste des contributeurs / Contributors list
* [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md) — Code de conduite / Code of conduct

---

<div align="center">
  <b>StockPOS</b> — 🇫🇷 Une architecture pensée pour les réalités du commerce de proximité.<br>
  🇬🇧 An architecture designed around the realities of small local trade.
</div>