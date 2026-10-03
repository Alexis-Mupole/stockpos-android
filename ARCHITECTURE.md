# Architecture Technique & Guide Développeur / StockPOS Architecture 📐

## 1. Principes Fondamentaux
StockPOS repose sur des principes architecturaux stricts garantissant la maintenabilité, l'extensibilité et la robustesse hors-ligne :
1. **Offline-First** : La source de vérité locale est la base de données **Room (SQLite)**. Aucune action utilisateur critique ne dépend d'un appel réseau synchrone.
2. **Clean Architecture & MVVM** : Découplage strict entre la présentation (Jetpack Compose), la logique métier (UseCases), et l'accès aux données (Repositories & DAOs).
3. **Réactivité & Flots Asynchrones** : Utilisation intensive de Kotlin Coroutines et de `StateFlow` / `Flow` pour propager instantanément les modifications de données de la base vers l'UI.
4. **Scope Restreint Google Drive** : Utilisation exclusive du scope `DriveScopes.DRIVE_APPDATA` pour isoler les sauvegardes dans l'espace caché applicatif.

---

## 2. Structure des Couches

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
│   Cloud Backup (Google Drive REST API AppData)         │
│   Background Tasks (WorkManager CoroutineWorkers)      │
└────────────────────────────────────────────────────────┘
```

---

## 3. Schéma Relationnel Room (Entities & DAOs)

### `CategoryEntity`
* `id` : Long (Clé primaire auto-générée)
* `name` : String (Nom unique de la catégorie)
* `colorHex` : String (Couleur personnalisée)
* `iconName` : String (Icône M3)
* `createdAt` : Long (Horodatage UTC)

### `ProductEntity`
* `id` : Long (Clé primaire auto-générée)
* `name` : String (Nom de l'article)
* `retailPriceMinor` : Long (Prix de vente en centimes / minor units)
* `costPriceMinor` : Long (Coût d'achat pour calcul de marge)
* `stockQuantity` : Double (Quantité disponible en stock)
* `lowStockThreshold` : Double (Seuil de déclenchement d'alerte)
* `barcode` : String? (Code-barres EAN13, Code128, QR Code, ou SKU)
* `sku` : String? (Référence interne)
* `categoryId` : Long? (Clé étrangère vers `CategoryEntity.id` avec `ON DELETE SET NULL`)
* `imagePath` : String? (Chemin absolu vers l'image stockée en stockage interne privé)

### `ProductWithCategory`
POJO Room avec `@Embedded` pour le produit et `@Relation(parentColumn = "categoryId", entityColumn = "id")` pour la catégorie associée. Permet une récupération jointe sans écrire de SQL manuel complexe.

### `SaleEntity`, `SaleItemEntity`, `PaymentEntity`
* Chaque vente possède son identifiant unique UUID ou ID incrémental, la date, le caissier, le statut (COMPLETED, REFUNDED), et les totaux.
* `SaleItemEntity` capture le prix unitaire et le coût unitaire figés au moment de la transaction (protection contre les variations ultérieures du catalogue).
* `PaymentEntity` permet le paiement fractionné (ex: 50% Espèces, 50% Mobile Money).

---

## 4. Sauvegarde & Restauration Google Drive (`DriveScopes.DRIVE_APPDATA`)

### Processus de Sauvegarde (`backupDataToDrive`)
1. Extraction complète de la base Room sous forme de structure JSON sécurisée :
   - Schéma de version
   - Date UTC d'exportation
   - Table `categories`
   - Table `products`
   - Table `sales`, `sale_items`, `payments`
   - Paramètres de boutique (`BusinessSettings`)
2. Requête vers l'API Google Drive v3 :
   - Recherche d'un fichier avec `name = 'pos_secure_backup.json'` dans le dossier spécial `'appDataFolder'`.
   - Si existant : mise à jour du contenu via `files().update()`.
   - Si inexistant : création via `files().create()` avec `parents = listOf('appDataFolder')`.
3. Retour du statut (Success, Failure) et mise à jour de l'horodatage dans les paramètres.

### Processus de Restauration (`restoreDataFromDrive`)
1. Requête vers le dossier `'appDataFolder'` pour trouver `pos_secure_backup.json`.
2. Téléchargement du flux de données vers un fichier tampon local chiffré/sécurisé.
3. Désérialisation et validation de l'intégrité du JSON.
4. Exécution d'une transaction atomique Room (`database.runInTransaction`) pour réinsérer les catégories, produits, ventes et paramètres sans risque de corruption en cas d'interruption.

---

## 5. Automatisation avec Jetpack WorkManager

* **`PeriodicWorkRequestBuilder`** : Exécution planifiée (ex. toutes les 6 heures ou 24 heures).
* **Contraintes réseau** : `NetworkType.UNMETERED` pour déclencher les sauvegardes lourdes uniquement en Wi-Fi.
* **Contrainte batterie** : `requiresBatteryNotLow(true)`.

---

## 6. Impression Thermique ESC/POS & Reçus PDF

* **Bluetooth ESC/POS** : Connexion RFCOMM directe avec le profil SPP (Serial Port Profile) UUID `00001101-0000-1000-8000-00805F9B34FB`. Génération des octets ESC/POS (alignement, mise en gras, découpe papier `GS V 66 0`).
* **Android PdfDocument** : Génération de pages vectorielles de reçu avec en-tête, tableau d'articles, totaux, ventilation de taxes et pied de page personnalisé. Partageable instantanément via `FileProvider`.
