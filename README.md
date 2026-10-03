# StockPOS 📱📦💳
> **Mobile Point of Sale & Inventory Management System (Open Source)**
> 
> *Conçu pour les commerçants, PME et organisations nécessitant une solution de caisse et de gestion de stock 100% hors-ligne avec synchronisation Cloud sécurisée sur Google Drive.*

[![Android](https://img.shields.io/badge/Platform-Android_14+-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Storage-Room_SQLite-FF6F00)](https://developer.android.com/training/data-storage/room)
[![Google Drive API](https://img.shields.io/badge/Cloud-Google_Drive_AppData-34A853?logo=googledrive&logoColor=white)](https://developers.google.com/drive)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Bilingual](https://img.shields.io/badge/Languages-Français_%7C_English-informational)](#multilingual-support)

---

## 🌟 Présentation du Projet / Overview

**StockPOS** est une application mobile complète de **Point de Vente (Mobile POS)** et de **Gestion de Stock & Inventaire**, architecturée selon les principes stricts de la **Clean Architecture** et du patron **MVVM** en **Kotlin moderne** et **Jetpack Compose (Material 3)**.

L'application fonctionne en **mode hors-ligne prioritaire (Offline-First)** : aucune connexion Internet n'est requise pour enregistrer des ventes, scanner des codes-barres ou imprimer des reçus. Lorsque le réseau est disponible, les données peuvent être automatiquement ou manuellement sauvegardées dans le dossier applicatif privé de l'utilisateur sur **Google Drive** (`DriveScopes.DRIVE_APPDATA`).

---

## 👤 Initiateur & Architecte du Projet / Project Initiator

<div align="center">
  <img src="app/src/main/res/drawable/img_alexis_mupole_1791008057874.jpg" width="160" height="160" style="border-radius: 50%; object-fit: cover;" alt="Alexis Mupole"/>
  <h3>Alexis Mupole</h3>
  <p><b>Consultant en Ingénierie Numérique • Informaticien & Développeur</b><br>
  📍 Kinshasa, République Démocratique du Congo</p>
</div>

### Profil & Bio
> **Informaticien & Développeur** (Licence en Computer Science aux USA & Licence en Business Computing en Ouganda). Passionné par l'accessibilité et la sécurité numérique.
> 
> *Consultant en ingénierie numérique, j'accompagne entreprises, ONG et particuliers avec plus de 5 ans d'expérience terrain. Du développement d'applications web sur mesure à la sécurisation de vos systèmes et à la collecte de données mobiles, je conçois des solutions fiables qui transforment vos défis techniques en résultats concrets.*

Alexis Mupole a initié **StockPOS** en tant que projet Open Source pour doter les entrepreneurs africains et mondiaux d'un outil de caisse robuste, moderne, gratuit et respectueux de la souveraineté de leurs données commerciales.

---

## 🤝 Communauté Open Source & Comment Contribuer / Open Source Community

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

---

## 🚀 Fonctionnalités Clés & Spécifications des Modules

### MODULE 1 : Base de Données Room & Relations Produits-Catégories
* **`CategoryEntity`** : Identifiant unique, nom de catégorie, icône, couleur et horodatage.
* **`ProductEntity`** : ID, nom, prix de vente, coût d'achat, niveau de stock, stock d'alerte, code-barres / SKU, photo locale (`imagePath`), et clé étrangère `category_id`.
* **`ProductWithCategory`** : Classe de relation Room `@Relation` pour requêter et afficher les produits avec toutes les données de leur catégorie en une seule requête réactive.
* **`ProductDao`** : Opérations complètes CRUD, recherche plein-texte insensible à la casse, filtrage par catégorie (`getProductsByCategory`), requêtes réactives sous forme de `Flow<List<ProductEntity>>`.

### MODULE 2 : Exportation Locale & Partage Système (WhatsApp, Email, Drive)
* **Génération structurée** : Extraction de l'ensemble du catalogue et de l'historique des ventes au format JSON structuré ou CSV prêt pour tableur.
* **`FileProvider` Android sécurisé** : Utilisation des mécanismes d'autorisations temporaires Android (`content://`) via `file_paths.xml`.
* **`ACTION_SEND` Intent** : Ouvre le menu de partage natif Android permettant d'exporter en un clic vers WhatsApp, Gmail, Telegram, Google Drive, ou l'explorateur de fichiers local.

### MODULE 3 : Sauvegarde & Restauration Google Drive (Scope Restreint)
* **Scope Sécurisé Restreint `DriveScopes.DRIVE_APPDATA`** : Les sauvegardes sont stockées dans le dossier caché privé de l'application sur le Google Drive de l'utilisateur (aucun accès aux fichiers personnels de l'utilisateur).
* **`backupDataToDrive`** : Extrait l'état de la base de données Room en `pos_secure_backup.json`. Détecte si une sauvegarde existe déjà pour la mettre à jour, évitant les doublons.
* **`restoreDataFromDrive`** : Télécharge `pos_secure_backup.json`, vérifie son intégrité, et restaure la base de données Room de manière atomique lors d'un changement d'appareil.
* **`CoroutineWorker` (WorkManager)** : Automatisation des sauvegardes périodiques en arrière-plan avec contrainte de connexion **Wi-Fi** / réseau non mesuré.

### MODULE 4 : Terminal de Caisse (POS) & Matériel
* **Scan de code-barres continu par caméra** : Analyse temps-réel via CameraX et ML Kit / ZXing.
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
* Rôles configurables : **Administrateur**, **Gérant / Manager**, et **Vendeur / Caissier**.
* Codes PIN chiffrés et salés (SHA-256 avec sel aléatoire).
* Changement de compte rapide et verrouillage de caisse.

---

## 🏛️ Architecture Logicielle (Clean Architecture & MVVM)

```
app/src/main/java/com/example/
├── data/
│   ├── local/
│   │   ├── converters/      # TypeConverters Room (Date, Enums, UUID)
│   │   ├── dao/             # DAOs réactifs (Product, Category, Sale, User)
│   │   ├── db/              # StockPosDatabase & Migrations Room
│   │   ├── entity/          # Entités Room (Product, Category, Sale, Payment, User)
│   │   └── relations/       # POJOs @Relation (ProductWithCategory, SaleWithItems)
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

---

## 🛠️ Stack Technologique & Dépendances

| Composant | Technologie | Description |
|-----------|-------------|-------------|
| **Langage** | Kotlin 2.0+ | Coroutines, Flow, StateFlow, Serialization |
| **Interface UI** | Jetpack Compose | Material Design 3, Responsive 8dp grid |
| **Base Locale** | Room Database | SQLite abstrait, Relations, Daos réactifs |
| **Asynchronisme** | Kotlin Coroutines | Dispatchers.IO pour la base de données & réseau |
| **Tâches de fond** | Jetpack WorkManager | Sauvegardes Wi-Fi & notifications de stock |
| **Cloud Storage** | Google Drive REST API | Scope `DRIVE_APPDATA` privé |
| **Caméra & Scan** | CameraX & ML Kit | Détection ultra-rapide de codes-barres 1D/2D |
| **Impression** | Android Bluetooth RFCOMM | Commandes brutes ESC/POS standard |
| **Génération PDF** | Android PdfDocument | Reçus vectoriels imprimables et partageables |
| **Images** | Coil Compose | Chargement asynchrone et cache d'images |

---

## 📥 Installation & Lancement en Développement

### Prérequis
* **Android Studio** : Koala, Ladybug ou version supérieure.
* **JDK** : Version 17 ou 21.
* **Android SDK** : Min SDK 26 (Android 8.0 Oreo), Target SDK 34 (Android 14).

### Étapes
1. Clonez le dépôt :
   ```bash
   git clone https://github.com/alexismupole/stockpos-mobile.git
   cd stockpos-mobile
   ```
2. Ouvrez le projet dans Android Studio.
3. Laissez Gradle synchroniser les dépendances.
4. Lancez l'application sur un appareil physique Android ou un émulateur :
   ```bash
   gradle assembleDebug
   ```

---

## 📄 Licence / License

Ce projet est sous licence **Apache 2.0**. Vous êtes libre de l'utiliser, de le modifier et de le distribuer, tant à des fins personnelles que commerciales, sous réserve du respect des conditions de la licence.
Voir le fichier [`LICENSE`](LICENSE) pour plus d'informations.

---

<div align="center">
  <b>StockPOS</b> — Développé avec passion pour l'autonomisation économique locale.<br>
  Initiateur : <b>Alexis Mupole</b> (Kinshasa, RD Congo) & la Communauté Open Source.
</div>
