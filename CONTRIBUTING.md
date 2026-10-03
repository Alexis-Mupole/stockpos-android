# Guide de Contribution à StockPOS / Contributing Guide 🚀

Merci de votre intérêt pour **StockPOS** ! Ce projet est une initiative open source portée par **Alexis Mupole** visant à offrir une caisse enregistreuse mobile et un outil de gestion d'inventaire moderne, accessible et hors-ligne pour tous.

Toutes les contributions sont les bienvenues : nouvelles fonctionnalités, corrections de bugs, améliorations de performances, traductions, design UI/UX et documentation.

---

## 📌 Sommaire
1. [Code de Conduite](#-code-de-conduite)
2. [Processus de Contribution](#-processus-de-contribution)
3. [Normes de Code & Bonnes Pratiques](#-normes-de-code--bonnes-pratiques)
4. [Ajouter vos Coordonnées dans CONTRIBUTORS.md](#-ajouter-vos-coordonnées-dans-contributorsmd)
5. [Rapports de Bugs & Suggestions](#-rapports-de-bugs--suggestions)

---

## 📜 Code de Conduite
En participant à ce projet, vous vous engagez à respecter notre [Code de Conduite](CODE_OF_CONDUCT.md) pour maintenir un environnement accueillant, inclusif et respectueux pour tous les contributeurs.

---

## 🔄 Processus de Contribution

### 1. Cloner et configurer le projet
```bash
# 1. Forkez le dépôt sur GitHub
# 2. Clonez votre fork en local
git clone https://github.com/<votre-nom-d-utilisateur>/stockpos-mobile.git
cd stockpos-mobile

# 3. Créez une branche dédiée à votre travail
git checkout -b feature/nom-de-votre-fonctionnalite
# ou pour un bug
git checkout -b fix/description-du-correctif
```

### 2. Développement et tests
* Utilisez **Android Studio** (Koala ou version plus récente).
* Vérifiez la compilation locale avant de commiter :
  ```bash
  gradle assembleDebug
  ```
* Assurez-vous que l'application respecte les contraintes hors-ligne (Offline-First).

### 3. Messages de commit
Nous recommandons la convention des **Conventional Commits** :
* `feat: ajout de l'exportation CSV des ventes`
* `fix: correction du calcul de rendu de monnaie en espèces`
* `docs: mise à jour du guide d'installation et ajout de contributeur`
* `perf: optimisation de la requête DAO de recherche produit`

### 4. N'oubliez pas d'ajouter vos coordonnées !
Ouvrez le fichier [`CONTRIBUTORS.md`](CONTRIBUTORS.md) et ajoutez votre profil dans le tableau des contributeurs afin que votre nom apparaisse dans l'histoire officielle du projet et dans les futures releases !

### 5. Soumettre la Pull Request
* Poussez votre branche sur votre fork GitHub.
* Ouvrez une **Pull Request (PR)** vers la branche `main` du projet d'origine.
* Décrivez précisément les changements apportés et joignez des captures d'écran si des modifications d'interface ont été effectuées.

---

## 📐 Normes de Code & Bonnes Pratiques

### Architecture & Couches
* **Clean Architecture & MVVM** : Respectez la séparation stricte entre Couche UI (Jetpack Compose), Couche Domaine (Use Cases, Modèles métier), et Couche Données (Room Entities, DAOs, Repositories).
* **Gestion des Threads** : Utilisez impérativement `Dispatchers.IO` pour toute opération de base de données (Room), d'I/O fichiers (PDF, JSON/CSV), et de réseau (Google Drive).
* **Sécurité & Confidentialité** : Ne jamais stocker de clés ou de codes PIN en clair (utiliser le hachage avec sel `PinHasher`).

### Jetpack Compose & UI
* Utilisez exclusivement les composants **Material 3 (M3)**.
* Respectez la grille de 8.dp pour l'espacement et assurez une surface de toucher minimale de 48.dp.
* Prévoyez systématiquement la prise en charge bilingue via la classe `AppStrings` (Français et Anglais).
* Toutes les actions clés doivent comporter un `testTag` unique en snake_case (ex. `Modifier.testTag("checkout_cash_button")`).

---

## 💬 Des Questions ou Besoin d'Aide ?
N'hésitez pas à ouvrir une discussion GitHub ou une issue pour échanger avec **Alexis Mupole** et l'équipe des mainteneurs. Ensemble, bâtissons les meilleurs outils numériques ouverts !
