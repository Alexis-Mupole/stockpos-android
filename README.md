# StockPOS 📱📦💳

> **🇫🇷 Caisse mobile & gestion de stock pour PME — Open Source**
> **🇬🇧 Mobile Point of Sale & Inventory Management for SMEs — Open Source**

[![Android](https://img.shields.io/badge/Platform-Android_8.0+-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Storage-Room_SQLite-FF6F00)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Bilingual](https://img.shields.io/badge/Languages-Français_%7C_English-informational)](#-multilingual-support--multilingual)

**🌐 Langue / Language:** [🇫🇷 Français](#) · [🇬🇧 English](#)

---

## 🎯 Notre Mission / Our Mission

**🇫🇷 Français**

**L'objectif de StockPOS est d'aider les PME, les petits commerçants et les organisations à gérer leur stock et leurs ventes de manière fiable — même sans connexion internet et avec un budget limité.**

Concrètement, ce projet vise à lever trois freins qui bloquent les petites entreprises :

* **Le coût** — une caisse enregistreuse professionnelle coûte trop cher pour un commerce de proximité. StockPOS est **gratuit et open source**, sous licence Apache 2.0.
* **La coupure réseau** — les solutions cloud cessation de fonctionner quand Internet tombe. StockPOS fonctionne **100% hors-ligne** : ventes, scans et impressions ne dépendent d'aucun serveur.
* **La souveraineté des données** — les données de ventes appartiennent au commerçant, pas à un fournisseur. StockPOS les conserve **sur l'appareil**, sans jamais les envoyer à un serveur tiers. Une sauvegarde cloud facultative dans le **dossier privé de l'application** sur Google Drive est **prévue** (voir [MODULE 3](#drive-backup)) et ne touchera jamais aux fichiers personnels de l'utilisateur.

Le résultat : un commerçant peut **vendre, inventorier et analyser ses résultats** depuis un simple téléphone Android, dans plusieurs langues, sans formation complexe et sans abonnement mensuel.

**🇬🇧 English**

**The goal of StockPOS is to help SMEs, small businesses, and organizations manage their inventory and sales reliably — even without an internet connection and on a limited budget.**

Concretely, this project aims to remove three barriers that hold small businesses back:

* **Cost** — professional point-of-sale hardware is too expensive for a local shop. StockPOS is **free and open source**, under the Apache 2.0 license.
* **Network outages** — cloud solutions stop working when the internet goes down. StockPOS works **100% offline**: sales, scanning, and printing never depend on a server.
* **Data sovereignty** — sales data belongs to the merchant, not to a vendor. StockPOS keeps it **on the device** and never sends it to a third-party server. An optional cloud backup to the app's **private application folder** on Google Drive is **planned** (see [MODULE 3](#drive-backup)) and will never touch the user's personal files.

The result: a shop owner can **sell, track inventory, and analyse performance** from a single Android phone, in multiple languages, with no complex training and no monthly subscription.

---

## 🌟 Présentation du Projet / Overview

**🇫🇷 Français**

**StockPOS** est une application mobile complète de **Point de Vente (Mobile POS)** et de **Gestion de Stock & Inventaire**, architecturée selon les principes stricts de la **Clean Architecture** et du patron **MVVM** en **Kotlin moderne** et **Jetpack Compose (Material 3)**.

L'application fonctionne en **mode hors-ligne prioritaire (Offline-First)** : aucune connexion Internet n'est requise pour enregistrer des ventes, scanner des codes-barres ou imprimer des reçus. Les données restent sur l'appareil du commerçant, qui garde ainsi la maîtrise de ses informations.

**🇬🇧 English**

**StockPOS** is a complete mobile **Point of Sale (Mobile POS)** and **Inventory Management** application, architected around strict **Clean Architecture** and the **MVVM** pattern, built with modern **Kotlin** and **Jetpack Compose (Material 3)**.

The app runs in **offline-first** mode: no internet connection is required to record sales, scan barcodes, or print receipts. Data lives on the device, and the shop owner stays in control of it.

---

## 👤 Initiateur & Architecte du Projet / Project Initiator

<div align="center">
  <img src="app/src/main/res/drawable/img_alexis_mupole_1791008057874.jpg" width="160" height="160" style="border-radius: 50%; object-fit: cover;" alt="Alexis Mupole"/>
  <h3>Alexis Mupole</h3>
  <p><b>🇫🇷 Consultant en Ingénierie Numérique • Informaticien & Développeur</b><br>
  <b>🇬🇧 Digital Engineering Consultant • IT Specialist & Developer</b><br>
  📍 Kinshasa, République Démocratique du Congo / DR Congo</p>
</div>

### Profil & Bio / Profile & Bio

**🇫🇷 Français**

> **Informaticien & Développeur** (Licence en Computer Science aux USA & Licence en Business Computing en Ouganda). Passionné par l'accessibilité et la sécurité numérique.
>
> *Consultant en ingénierie numérique, j'accompagne entreprises, ONG et particuliers avec plus de 5 ans d'expérience terrain. Du développement d'applications web sur mesure à la sécurisation de vos systèmes et à la collecte de données mobiles, je conçois des solutions fiables qui transforment vos défis techniques en résultats concrets.*

**🇬🇧 English**

> **IT Specialist & Developer** (Bachelor's in Computer Science, USA & Bachelor's in Business Computing, Uganda). Passionate about accessibility and digital security.
>
> *As a digital engineering consultant, I support companies, NGOs, and individuals with over 5 years of hands-on field experience. From building custom web applications to hardening your systems and collecting mobile data, I design reliable solutions that turn technical challenges into concrete results.*

**🇫🇷** Alexis Mupole a initié **StockPOS** en tant que projet Open Source pour doter les entrepreneurs africains et mondiaux d'un outil de caisse robuste, moderne, gratuit et respectueux de la souveraineté de leurs données commerciales.

**🇬🇧** Alexis Mupole started **StockPOS** as an open source project to give African and worldwide entrepreneurs a robust, modern, free point-of-sale tool that respects the sovereignty of their business data.

---

## 🤝 Communauté Open Source & Comment Contribuer / Open Source Community

**🇫🇷 Français**

Ce projet est **100% open-source**. Toute personne qui contribue (développement Kotlin/Compose, tests, design UI/UX, documentation, traductions) peut et doit **ajouter son nom et ses coordonnées** dans le fichier [`CONTRIBUTORS.md`](CONTRIBUTORS.md) !

### Comment ajouter vos coordonnées de contributeur ?

1. **Forkez** ce dépôt sur votre compte GitHub.
2. Créez une branche descriptive : `git checkout -b feature/ma-fonctionnalite` ou `git checkout -b docs/ajout-contributeur-nom`.
3. Effectuez vos améliorations ou corrections.
4. Ouvrez [`CONTRIBUTORS.md`](CONTRIBUTORS.md) et ajoutez votre profil dans le tableau des contributeurs :
   ```markdown
   | Nom & Prénom | Rôle / Spécialité | Localisation | Contributions | Contact / Profil |
   |--------------|-------------------|--------------|---------------|------------------|
   | Votre Nom    | Développeur Kotlin| Ville, Pays  | Nouvelle feat | [GitHub/LinkedIn]|
   ```
5. Soumettez votre **Pull Request** avec une explication claire. Votre profil sera fusionné et célébré dans les notes de mise à jour !

Consultez le guide complet dans [`CONTRIBUTING.md`](CONTRIBUTING.md).

**🇬🇧 English**

This project is **100% open source**. Anyone who contributes (Kotlin/Compose development, testing, UI/UX design, documentation, translations) can and should **add their name and contact details** to [`CONTRIBUTORS.md`](CONTRIBUTORS.md)!

### How to add your contributor profile

1. **Fork** this repository to your GitHub account.
2. Create a descriptive branch: `git checkout -b feature/my-feature` or `git checkout -b docs/add-contributor-name`.
3. Make your improvements or fixes.
4. Open [`CONTRIBUTORS.md`](CONTRIBUTORS.md) and add your profile to the contributors table:
   ```markdown
   | Name | Role / Specialty | Location | Contributions | Contact / Profile |
   |------|------------------|----------|---------------|-------------------|
   | Your Name | Kotlin Developer | City, Country | New feature | [GitHub/LinkedIn] |
   ```
5. Submit your **Pull Request** with a clear explanation. Your profile will be merged and celebrated in the release notes!

See the full guide in [`CONTRIBUTING.md`](CONTRIBUTING.md).

---

## 🚀 Fonctionnalités Clés / Key Features

**🇫🇷 Français**

### MODULE 1 : Base de Données Room & Relations Produits-Catégories
* **`CategoryEntity`** : Identifiant unique, nom de catégorie, icône (`iconName`), couleur (`colorHex`) et archivage logique (`isArchived`).
* **`ProductEntity`** : ID, nom, SKU, prix de vente et coût d'achat **en centimes**, quantité et seuil de réapprovisionnement, date de péremption, photo locale (`imagePath`), et clés étrangères `categoryId` / `supplierId`.
* **`ProductBarcodeEntity`** : **Plusieurs codes-barres par produit** (un par conditionnement), avec la taille du pack (`packQty`).
* **`ProductWithBarcodesAndCategory`** : Classe de relation Room `@Relation` pour requêter et afficher un produit avec sa catégorie, son fournisseur et tous ses codes-barres en une seule requête réactive.
* **`StockMovementEntity`** : Journal d'audit de chaque variation de stock (`SALE`, `RETURN`, `PURCHASE`, `ADJUSTMENT`, `DAMAGE`, `VOID`) — la casse et les pertes deviennent traçables.
* **`ProductDao`** : Opérations complètes CRUD, recherche plein-texte insensible à la casse, filtrage par catégorie (`getProductsByCategory`), requêtes réactives sous forme de `Flow<List<ProductEntity>>`.

### MODULE 2 : Exportation Locale & Partage Système (WhatsApp, Email, Drive)
* **Génération structurée** : Extraction de l'ensemble du catalogue et de l'historique des ventes au format JSON structuré ou CSV prêt pour tableur.
* **`FileProvider` Android sécurisé** : Utilisation des mécanismes d'autorisations temporaires Android (`content://`) via `file_paths.xml`.
* **`ACTION_SEND` Intent** : Ouvre le menu de partage natif Android permettant d'exporter en un clic vers WhatsApp, Gmail, Telegram, Google Drive, ou l'explorateur de fichiers local.

<a id="drive-backup"></a>

### MODULE 3 : Sauvegarde & Restauration Google Drive (Scope Restreint) — 🚧 *Prévu / Planned*

> **⚠️ Statut / Status : À IMPLÉMENTER / NOT YET IMPLEMENTED**
> Cette section décrit une **feuille de route**, pas une fonctionnalité livrée. Le code actuel ne contient pas encore d'intégration Google Drive. Les contributions sont les bienvenues !
>
> **This section describes a roadmap, not a shipped feature.** The current codebase does not yet include Google Drive integration. Contributions are welcome!

**🇫🇷 Français**

* **Scope Sécurisé Restreint `DriveScopes.DRIVE_APPDATA`** : Les sauvegardes seraient stockées dans le dossier caché privé de l'application sur le Google Drive de l'utilisateur (aucun accès aux fichiers personnels de l'utilisateur).
* **`backupDataToDrive`** : Extraire l'état de la base de données Room en `pos_secure_backup.json`. Détecter si une sauvegarde existe déjà pour la mettre à jour, évitant les doublons.
* **`restoreDataFromDrive`** : Télécharger `pos_secure_backup.json`, vérifier son intégrité, et restaurer la base de données Room de manière atomique lors d'un changement d'appareil.
* **`CoroutineWorker` (WorkManager)** : Automatiser des sauvegardes périodiques en arrière-plan avec contrainte de connexion **Wi-Fi** / réseau non mesuré.

**🇬🇧 English**

* **Restricted secure scope `DriveScopes.DRIVE_APPDATA`**: Backups would be stored in the app's hidden private folder on the user's Google Drive (no access to the user's personal files).
* **`backupDataToDrive`**: Extract the Room database state into `pos_secure_backup.json`. Detect an existing backup to update it, avoiding duplicates.
* **`restoreDataFromDrive`**: Download `pos_secure_backup.json`, verify its integrity, and atomically restore the Room database when changing devices.
* **`CoroutineWorker` (WorkManager)**: Automate periodic background backups with a **Wi-Fi** / unmetered network constraint.

### MODULE 4 : Terminal de Caisse (POS) & Matériel
* **Scan de code-barres continu par caméra** : Analyse temps-réel via CameraX et ML Kit.
* **Recherche instantanée** : Suggestions rapides avec aperçu d'images de produits.
* **Moyens de paiement multiples (Multi-Tender)** : Espèces avec calculatrice automatique de rendu de monnaie, Carte bancaire, et Mobile Money (M-Pesa, Orange, Airtel, Wave).
* **Mise en attente de vente (Hold / Recall)** : Permet de suspendre un panier pour servir un autre client et le reprendre immédiatement.
* **Impression thermique ESC/POS** : Impression de reçus sur imprimantes thermiques Bluetooth (format 58mm et 80mm).
* **Reçus PDF & Partage** : Génération vectorielle de reçus PDF partageables directement avec le client.

### MODULE 5 : Tableau de Bord & Analytics
* **Indicateurs financiers en temps réel** : Chiffre d'affaires brut, bénéfice estimé net, marge moyenne en %, nombre de commandes, volume d'articles vendus, et **Panier Moyen (AOV)**.
* **Filtrage temporel interactif** : Aujourd'hui, 7 derniers jours, 30 derniers jours, Historique complet.
* **Répartition par moyen de paiement** : Visualisation des encaissements en Espèces, Carte et Mobile Money.
* **Suivi des ruptures de stock** : Indicateurs de stock critique et de rupture totale.

### MODULE 6 : Sécurité & Multi-Utilisateurs (RBAC)
* Rôles configurables (`UserRole`) : **Administrateur** (`ADMIN`), **Gérant / Manager** (`MANAGER`), et **Vendeur / Caissier** (`SELLER`).
* Codes PIN hachés avec **PBKDF2-HMAC-SHA256** (12 000 itérations) et sel aléatoire de 16 octets ; comparaison en temps constant. Jamais stockés ni journalisés en clair.
* Changement de compte rapide, PIN temporaire à changer (`mustChangePin`) et verrouillage de caisse.

**🇬🇧 English**

### MODULE 1: Room Database & Product-Category Relations
* **`CategoryEntity`**: Unique ID, category name, icon (`iconName`), colour (`colorHex`), and soft-delete flag (`isArchived`).
* **`ProductEntity`**: ID, name, SKU, sale price and cost price **in minor units**, quantity and reorder level, expiry date, local photo (`imagePath`), and `categoryId` / `supplierId` foreign keys.
* **`ProductBarcodeEntity`**: **Multiple barcodes per product** (one per pack size), with pack quantity (`packQty`).
* **`ProductWithBarcodesAndCategory`**: Room `@Relation` class to query and display a product together with its category, supplier, and all of its barcodes in a single reactive query.
* **`StockMovementEntity`**: Audit ledger for every stock change (`SALE`, `RETURN`, `PURCHASE`, `ADJUSTMENT`, `DAMAGE`, `VOID`) — making breakage and shrinkage traceable.
* **`ProductDao`**: Full CRUD operations, case-insensitive full-text search, category filtering (`getProductsByCategory`), reactive queries exposed as `Flow<List<ProductEntity>>`.

### MODULE 2: Local Export & System Sharing (WhatsApp, Email, Drive)
* **Structured generation**: Extraction of the full catalogue and sales history as structured JSON or spreadsheet-ready CSV.
* **Secure Android `FileProvider`**: Uses Android temporary permission mechanisms (`content://`) via `file_paths.xml`.
* **`ACTION_SEND` Intent**: Opens the native Android share sheet for one-click export to WhatsApp, Gmail, Telegram, Google Drive, or the local file manager.

<a id="drive-backup-en"></a>

### MODULE 3: Google Drive Backup & Restore (Restricted Scope) — 🚧 *Planned*

> **⚠️ Status: TO BE IMPLEMENTED**
> This section describes a **roadmap**, not a shipped feature. The current codebase does not yet include Google Drive integration. Contributions are welcome!

* **Restricted secure scope `DriveScopes.DRIVE_APPDATA`**: Backups would be stored in the app's hidden private folder on the user's Google Drive (no access to the user's personal files).
* **`backupDataToDrive`**: Extract the Room database state into `pos_secure_backup.json`. Detect an existing backup to update it, avoiding duplicates.
* **`restoreDataFromDrive`**: Download `pos_secure_backup.json`, verify its integrity, and atomically restore the Room database when changing devices.
* **`CoroutineWorker` (WorkManager)**: Automate periodic background backups with a **Wi-Fi** / unmetered network constraint.

### MODULE 4: Point of Sale (POS) Terminal & Hardware
* **Continuous camera barcode scanning**: Real-time analysis via CameraX and ML Kit.
* **Instant search**: Quick suggestions with product image previews.
* **Multiple payment methods (Multi-Tender)**: Cash with automatic change calculator, bank card, and Mobile Money (M-Pesa, Orange, Airtel, Wave).
* **Sale hold / recall**: Suspend a cart to serve another customer and resume it instantly.
* **ESC/POS thermal printing**: Receipt printing on Bluetooth thermal printers (58mm and 80mm formats).
* **PDF receipts & sharing**: Vector PDF receipt generation, shareable directly with the customer.

### MODULE 5: Dashboard & Analytics
* **Real-time financial indicators**: Gross revenue, estimated net profit, average margin in %, order count, units sold, and **Average Order Value (AOV)**.
* **Interactive time filtering**: Today, last 7 days, last 30 days, full history.
* **Payment method breakdown**: Cash, card, and Mobile Money collection visualization.
* **Stock-out tracking**: Critical stock and full stock-out indicators.

### MODULE 6: Security & Multi-User (RBAC)
* Configurable roles (`UserRole`): **Administrator** (`ADMIN`), **Manager** (`MANAGER`), and **Salesperson / Cashier** (`SELLER`).
* PINs hashed with **PBKDF2-HMAC-SHA256** (12,000 iterations) and a 16-byte random salt; compared in constant time. Never stored or logged in plain text.
* Fast account switching, forced PIN change (`mustChangePin`), and register locking.

---

## 🏛️ Architecture Logicielle (Clean Architecture & MVVM)

**🇫🇷 Français**

```
app/src/main/java/com/example/
├── data/
│   ├── local/
│   │   ├── converters/      # TypeConverters Room (Date, Enums, UUID)
│   │   ├── dao/             # DAOs réactifs (Product, Category, Sale, User)
│   │   ├── db/              # StockPosDatabase & Migrations Room
│   │   ├── entity/          # Entités Room (Product, Category, Sale, Payment, User)
│   │   └── relations/       # POJOs @Relation (ProductWithBarcodesAndCategory, SaleWithItemsAndPayments)
│   ├── repository/          # Implémentations Repository & BusinessSettings
│   └── session/             # SessionManager & état d'authentification
├── domain/
│   ├── model/               # Modèles métier & panier (CartState, CartItem)
│   ├── printer/             # Moteurs d'impression ESC/POS Bluetooth & Reçus PDF
│   ├── security/            # Hachage et salage des codes PIN
│   └── usecase/             # Cas d'utilisation métier (Scan, Checkout, Stock, Auth)
├── di/
│   └── AppContainer.kt      # Injection de dépendances manuelle (Service Locator)
├── ui/
│   ├── about/               # Écran À Propos (Profil Alexis Mupole & Communauté)
│   ├── auth/                # Connexion PIN & Création Admin
│   ├── components/          # Composants UI M3 réutilisables (Pad numérique, Badges)
│   ├── history/             # Historique des ventes, remboursements & rapports
│   ├── home/                # Tableau de bord interactif & Wizard de démarrage
│   ├── inventory/           # Gestion du catalogue, stocks & photos
│   ├── navigation/          # Navigation Compose & gestion du backstack
│   ├── pos/                 # Terminal de vente, scan & encaissement
│   ├── settings/            # Paramètres système, imprimante Bluetooth & utilisateurs
│   ├── setup/               # Assistant de configuration de boutique
│   └── theme/               # Thème Material 3 (Couleurs, Typographies, Formes)
├── util/
│   ├── AppStrings.kt        # Système bilingue dynamique Français / Anglais
│   ├── DataExportManager.kt # Exportation locale JSON/CSV & FileProvider
│   └── ProductImageHelper.kt# Sauvegarde et mise à l'échelle des photos de produits
└── work/
    └── LowStockNotificationWorker.kt # Tâches WorkManager en arrière-plan
```

**🇬🇧 English**

The folder layout is identical; the per-folder comments are documented in French in the source tree. See [`ARCHITECTURE.md`](ARCHITECTURE.md) for the full layer-by-layer explanation in both languages.

---

## 🛠️ Stack Technologique & Dépendances / Tech Stack & Dependencies

| Composant / Component | Technologie / Technology | Description / Description |
|-----------|-------------|-------------|
| **Langage / Language** | Kotlin 2.0+ | Coroutines, Flow, StateFlow / Coroutines, Flow, StateFlow |
| **Interface UI** | Jetpack Compose | Material Design 3, grille responsive 8dp / Material Design 3, responsive 8dp grid |
| **Base Locale / Local Storage** | Room Database | SQLite abstrait, relations, DAOs réactifs / Abstracted SQLite, relations, reactive DAOs |
| **Asynchronisme / Async** | Kotlin Coroutines | `Dispatchers.IO` pour base & réseau / `Dispatchers.IO` for database & network |
| **Tâches de fond / Background** | Jetpack WorkManager | Notifications de stock bas / Low-stock notifications |
| **Cloud Storage** | Google Drive REST API — 🚧 *prévu / planned* | Scope privé `DRIVE_APPDATA` / Private `DRIVE_APPDATA` scope |
| **Caméra & Scan** | CameraX & ML Kit | Détection ultra-rapide 1D/2D / Ultra-fast 1D/2D barcode detection |
| **Impression / Printing** | Android Bluetooth RFCOMM | Commandes ESC/POS brutes / Raw standard ESC/POS commands |
| **Génération PDF** | Android PdfDocument | Reçus vectoriels partageables / Shareable vector receipts |
| **Images** | Coil Compose | Chargement asynchrone & cache / Async loading & caching |

---

## 📥 Installation & Lancement en Développement / Installation & Development Setup

### Prérequis / Prerequisites

**🇫🇷 Français**
* **Android Studio** : Koala, Ladybug ou version supérieure.
* **JDK** : Version 17 ou 21.
* **Android SDK** : Min SDK 26 (Android 8.0 Oreo), Target SDK 36.

**🇬🇧 English**
* **Android Studio**: Koala, Ladybug, or newer.
* **JDK**: Version 17 or 21.
* **Android SDK**: Min SDK 26 (Android 8.0 Oreo), Target SDK 36.

### Étapes / Steps

**🇫🇷 Français**
1. Clonez le dépôt :
   ```bash
   git clone https://github.com/Alexis-Mupole/stockpos-android.git
   cd stockpos-android
   ```
2. Ouvrez le projet dans Android Studio.
3. Laissez Gradle synchroniser les dépendances.
4. Lancez l'application sur un appareil physique Android ou un émulateur :
   ```bash
   ./gradlew assembleDebug
   ```

**🇬🇧 English**
1. Clone the repository:
   ```bash
   git clone https://github.com/Alexis-Mupole/stockpos-android.git
   cd stockpos-android
   ```
2. Open the project in Android Studio.
3. Let Gradle sync the dependencies.
4. Run the app on a physical Android device or an emulator:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🌐 Multilingual Support / Support Multilingue

**🇫🇷 Français**

L'interface de l'application est **bilingue** et bascule dynamiquement entre le **français** et l'**anglais** via la classe `AppStrings`, sans redémarrage. Les ressources de chaînes sont fournies par `res/values/strings.xml` (anglais) et `res/values-fr/strings.xml` (français).

**🇬🇧 English**

The app interface is **bilingual** and switches dynamically between **French** and **English** via the `AppStrings` class, with no restart required. String resources are provided by `res/values/strings.xml` (English) and `res/values-fr/strings.xml` (French).

---

## 📄 Licence / License

**🇫🇷 Français**

Ce projet est sous licence **Apache 2.0**. Vous êtes libre de l'utiliser, de le modifier et de le distribuer, tant à des fins personnelles que commerciales, sous réserve du respect des conditions de la licence. Voir le fichier [`LICENSE`](LICENSE) pour plus d'informations.

**🇬🇧 English**

This project is licensed under the **Apache 2.0 License**. You are free to use, modify, and distribute it, for personal or commercial purposes, provided you comply with the license terms. See the [`LICENSE`](LICENSE) file for more information.

---

<div align="center">
  <b>StockPOS</b> — 🇫🇷 Développé avec passion pour l'autonomisation économique des PME.<br>
  🇬🇧 Built with passion for the economic empowerment of SMEs.<br>
  Initiateur / Initiator: <b>Alexis Mupole</b> (Kinshasa, RD Congo / DR Congo) & la Communauté Open Source / the Open Source Community.
</div>