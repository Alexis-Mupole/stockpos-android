# Guide de Contribution à StockPOS / Contributing Guide 🚀

> **🇫🇷 Merci de vouloir aider les PME à mieux gérer leur commerce.**
> **🇬🇧 Thank you for wanting to help SMEs better manage their business.**

**🌐 Langue / Language:** [🇫🇷 Français](#) · [🇬🇧 English](#)

---

## 🇫🇷 Français

Merci de votre intérêt pour **StockPOS** ! Ce projet est une initiative open source portée par **Alexis Mupole** dont **l'objectif est d'aider les PME, les petits commerçants et les organisations à disposer d'un outil de caisse et de gestion d'inventaire moderne, accessible, hors-ligne et gratuit**.

Toutes les contributions sont les bienvenues : nouvelles fonctionnalités, corrections de bugs, améliorations de performances, traductions, design UI/UX et documentation.

**Chaque contribution a un impact direct** : une correction de bogue de scan ou une nouvelle langue signifie qu'un commerçant quelque part encaiffe plus vite et plus juste.

### 📌 Sommaire

1. [Code de Conduite](#-code-de-conduite)
2. [Processus de Contribution](#-processus-de-contribution)
3. [Normes de Code & Bonnes Pratiques](#-normes-de-code--bonnes-pratiques)
4. [Ajouter vos Coordonnées dans CONTRIBUTORS.md](#-noubliez-pas-dajouter-vos-coordonnées-)
5. [Rapports de Bugs & Suggestions](#-rapports-de-bugs--suggestions)

### 📜 Code de Conduite

En participant à ce projet, vous vous engagez à respecter notre [Code de Conduite](CODE_OF_CONDUCT.md) pour maintenir un environnement accueillant, inclusif et respectueux pour tous les contributeurs.

### 🔄 Processus de Contribution

#### 1. Cloner et configurer le projet

```bash
# 1. Forkez le dépôt sur GitHub
# 2. Clonez votre fork en local
git clone https://github.com/<votre-nom-d-utilisateur>/stockpos-android.git
cd stockpos-android

# 3. Créez une branche dédiée à votre travail
git checkout -b feature/nom-de-votre-fonctionnalite
# ou pour un bug
git checkout -b fix/description-du-correctif
```

#### 2. Développement et tests

* Utilisez **Android Studio** (Koala, Ladybug ou version plus récente).
* Vérifiez la compilation locale avant de commiter :

  ```bash
  ./gradlew assembleDebug
  ./gradlew test
  ```

* Assurez-vous que l'application respecte les contraintes hors-ligne (**Offline-First**) : aucune fonctionnalité ne doit dépendre d'un appel réseau pour fonctionner.
* Si votre contribution touche l'interface, la chaîne fournie doit exister en **français et en anglais**.

#### 3. Messages de commit

Nous recommandons la convention des **Conventional Commits** :

* `feat: ajout de l'exportation CSV des ventes`
* `fix: correction du calcul de rendu de monnaie en espèces`
* `docs: mise à jour du guide d'installation et ajout de contributeur`
* `perf: optimisation de la requête DAO de recherche produit`

#### 4. N'oubliez pas d'ajouter vos coordonnées !

Ouvrez le fichier [`CONTRIBUTORS.md`](CONTRIBUTORS.md) et ajoutez votre profil dans le tableau des contributeurs afin que votre nom apparaisse dans l'histoire officielle du projet et dans les futures releases !

#### 5. Soumettre la Pull Request

* Poussez votre branche sur votre fork GitHub.
* Ouvrez une **Pull Request (PR)** vers la branche `main` du projet d'origine.
* Décrivez précisément les changements apportés et joignez des captures d'écran si des modifications d'interface ont été effectuées.

### 📐 Normes de Code & Bonnes Pratiques

#### Architecture & Couches

* **Clean Architecture & MVVM** : Respectez la séparation stricte entre Couche UI (Jetpack Compose), Couche Domaine (Use Cases, Modèles métier), et Couche Données (Room Entities, DAOs, Repositories).
* **Gestion des Threads** : Utilisez impérativement `Dispatchers.IO` pour toute opération de base de données (Room), d'I/O fichiers (PDF, JSON/CSV), et de réseau (Google Drive).
* **Argent en centimes** : Stockez tous les montants en `Long` représentant des centimes. **Ne jamais utiliser `Double` pour de l'argent.**
* **Prix figés** : Ne modifiez jamais une vente enregistrée. Les corrections passent par un remboursement (`SaleStatus.REFUNDED` / `PARTIALLY_REFUNDED`), jamais par une mise à jour silencieuse.
* **Sécurité & Confidentialité** : Ne jamais stocker de clés ou de codes PIN en clair (utiliser `PinHasher`). Ne journalisez jamais un code PIN.

#### Jetpack Compose & UI

* Utilisez exclusivement les composants **Material 3 (M3)**.
* Respectez la grille de 8.dp pour l'espacement et assurez une surface de toucher minimale de 48.dp.
* Prévoyez systématiquement la prise en charge bilingue via la classe `AppStrings` (Français et Anglais).
* Toutes les actions clés doivent comporter un `testTag` unique en snake_case (ex. `Modifier.testTag("checkout_cash_button")`).

#### Documentation

* Les fichiers `.md` du projet sont **bilingues** (Français / Anglais). Si vous ajoutez une section, ajoutez **les deux** versions.
* Corrigez les liens et les commandes : le dépôt est `https://github.com/Alexis-Mupole/stockpos-android`.

### 🐛 Rapports de Bugs & Suggestions

| Type / Type | Where / Où |
|---|---|
| Bug / Bogue | Ouvrez une *issue* avec les étapes de reproduction / Open an *issue* with reproduction steps |
| Amélioration / Enhancement | Ouvrez une *issue* ou une *discussion* / Open an *issue* or *discussion* |
| Question | Ouvrez une *discussion* GitHub / Open a GitHub *discussion* |

### 💬 Des Questions ou Besoin d'Aide ?

N'hésitez pas à ouvrir une discussion GitHub ou une issue pour échanger avec **Alexis Mupole** et l'équipe des mainteneurs. Ensemble, bâtissons les meilleurs outils numériques ouverts !

---

## 🇬🇧 English

Thank you for your interest in **StockPOS**! This is an open source initiative led by **Alexis Mupole** whose **goal is to give SMEs, small businesses, and organizations access to a modern, accessible, offline-capable, and free point-of-sale and inventory tool**.

All contributions are welcome: new features, bug fixes, performance improvements, translations, UI/UX design, and documentation.

**Every contribution has a direct impact**: fixing a scanning bug or adding a new language means a shop owner somewhere rings up sales faster and more accurately.

### 📌 Table of Contents

1. [Code of Conduct](#-code-of-conduct)
2. [Contribution Process](#-contribution-process)
3. [Code Standards & Best Practices](#-code-standards--best-practices)
4. [Add Your Details to CONTRIBUTORS.md](#-remember-to-add-your-details-)
5. [Bug Reports & Suggestions](#-bug-reports--suggestions)

### 📜 Code of Conduct

By participating in this project, you agree to uphold our [Code of Conduct](CODE_OF_CONDUCT.md) to maintain a welcoming, inclusive, and respectful environment for all contributors.

### 🔄 Contribution Process

#### 1. Clone and set up the project

```bash
# 1. Fork the repository on GitHub
# 2. Clone your fork locally
git clone https://github.com/<your-username>/stockpos-android.git
cd stockpos-android

# 3. Create a dedicated branch for your work
git checkout -b feature/your-feature-name
# or for a bug
git checkout -b fix/bug-description
```

#### 2. Development and testing

* Use **Android Studio** (Koala, Ladybug, or newer).
* Verify the build locally before committing:

  ```bash
  ./gradlew assembleDebug
  ./gradlew test
  ```

* Make sure the app respects the **offline-first** constraints: no feature may depend on a network call in order to work.
* If your contribution touches the UI, the string it provides must exist in **both French and English**.

#### 3. Commit messages

We recommend the **Conventional Commits** convention:

* `feat: add CSV export of sales`
* `fix: correct cash change calculation`
* `docs: update installation guide and add contributor`
* `perf: optimize product search DAO query`

#### 4. Remember to add your details!

Open [`CONTRIBUTORS.md`](CONTRIBUTORS.md) and add your profile to the contributors table so your name appears in the project's official history and in future releases!

#### 5. Submit the Pull Request

* Push your branch to your GitHub fork.
* Open a **Pull Request (PR)** against the `main` branch of the original project.
* Describe your changes precisely and attach screenshots if you modified the interface.

### 📐 Code Standards & Best Practices

#### Architecture & Layers

* **Clean Architecture & MVVM**: Respect the strict separation between the UI layer (Jetpack Compose), the Domain layer (Use Cases, business models), and the Data layer (Room Entities, DAOs, Repositories).
* **Threading**: Imperatively use `Dispatchers.IO` for any database (Room), file I/O (PDF, JSON/CSV), or network (Google Drive) operation.
* **Money in minor units**: Store all amounts as `Long` in minor units. **Never use `Double` for money.**
* **Frozen prices**: Never modify a recorded sale. Corrections go through a refund (`SaleStatus.REFUNDED` / `PARTIALLY_REFUNDED`), never a silent update.
* **Security & confidentiality**: Never store keys or PINs in plain text (use `PinHasher`). Never log a PIN.

#### Jetpack Compose & UI

* Use **Material 3 (M3)** components exclusively.
* Follow the 8.dp spacing grid and ensure a minimum touch target of 48.dp.
* Always provide bilingual support through the `AppStrings` class (French and English).
* All key actions must have a unique snake_case `testTag` (e.g. `Modifier.testTag("checkout_cash_button")`).

#### Documentation

* The project's `.md` files are **bilingual** (French / English). If you add a section, add **both** versions.
* Fix links and commands: the repository is `https://github.com/Alexis-Mupole/stockpos-android`.

### 🐛 Bug Reports & Suggestions

| Type | Where |
|---|---|
| Bug | Open an *issue* with reproduction steps |
| Enhancement | Open an *issue* or a *discussion* |
| Question | Open a GitHub *discussion* |

### 💬 Questions or Need Help?

Don't hesitate to open a GitHub discussion or an issue to talk with **Alexis Mupole** and the maintainers. Together, let's build the best open digital tools!

---

## 📚 Documents Liés / Related Documents

* [`README.md`](README.md) — Présentation du projet / Project overview
* [`ARCHITECTURE.md`](ARCHITECTURE.md) — Architecture technique / Technical architecture
* [`CONTRIBUTORS.md`](CONTRIBUTORS.md) — Liste des contributeurs / Contributors list
* [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md) — Code de conduite / Code of conduct

---

<div align="center">
  <b>StockPOS</b> — 🇫🇷 Chaque ligne de code aide une PME à vendre.<br>
  🇬🇧 Every line of code helps an SME sell.
</div>