package com.example.util

class AppStrings(private val lang: String) {

    val isFr = lang.equals("FR", ignoreCase = true)

    // Navigation
    val navHome = if (isFr) "Accueil" else "Home"
    val navSell = if (isFr) "Vente" else "Sell"
    val navInventory = if (isFr) "Stock" else "Inventory"
    val navHistory = if (isFr) "Historique" else "History"
    val navSettings = if (isFr) "Paramètres" else "Settings"

    // Dashboard & Metrics
    val dashboardTitle = if (isFr) "Tableau de Bord" else "Operations Dashboard"
    val filterToday = if (isFr) "Aujourd'hui" else "Today"
    val filter7d = if (isFr) "7 Jours" else "7 Days"
    val filter30d = if (isFr) "30 Jours" else "30 Days"
    val filterAll = if (isFr) "Tout" else "All Time"

    val todaySales = if (isFr) "Ventes du jour" else "Today's Sales"
    val periodSales = if (isFr) "Ventes" else "Sales Revenue"
    val estProfit = if (isFr) "Bénéfice est." else "Est. Profit"
    val ordersCount = if (isFr) "Commandes" else "Orders"
    val itemsSold = if (isFr) "Articles vendus" else "Items Sold"
    val avgOrderVal = if (isFr) "Panier Moyen" else "Avg. Order"
    val lowStock = if (isFr) "Stock faible" else "Low Stock"
    val outOfStock = if (isFr) "Rupture" else "Out of Stock"
    val stockValuation = if (isFr) "Valeur du stock" else "Stock Valuation"
    val retailVal = if (isFr) "Val. Vente" else "Retail Val."
    val costVal = if (isFr) "Val. Coût" else "Cost Val."
    val tenderLabel = if (isFr) "Paiements:" else "Tenders:"

    // POS & Selling
    val startNewSale = if (isFr) "Nouvelle Vente / Caisse" else "Start New Sale / Checkout"
    val startSaleSub = if (isFr) "Scan code-barres & encaissement rapide" else "Barcode scan & fast tender checkout"
    val posTerminal = if (isFr) "Terminal de Caisse" else "POS Terminal"
    val charge = if (isFr) "ENCAISSER" else "CHARGE"
    val totalDue = if (isFr) "Total Dû" else "Total Due"
    val total = if (isFr) "TOTAL" else "TOTAL"
    val change = if (isFr) "RENDU" else "CHANGE"
    val cash = if (isFr) "ESPÈCES" else "CASH"
    val card = if (isFr) "CARTE" else "CARD"
    val mobileMoney = if (isFr) "MOBILE MONEY" else "MOBILE MONEY"
    val tenderedAmount = if (isFr) "Montant Reçu" else "Tendered Cash Amount"
    val saleCompleted = if (isFr) "Vente Enregistrée !" else "Sale Completed!"
    val receipt = if (isFr) "Reçu" else "Receipt"
    val printReceipt = if (isFr) "Imprimer Reçu" else "Print Receipt"
    val sharePdf = if (isFr) "Partager PDF" else "Share PDF"
    val nextSale = if (isFr) "Vente Suivante" else "Next Sale"
    val searchPlaceholder = if (isFr) "Rechercher par nom, SKU ou code-barres..." else "Search by name, SKU or barcode..."
    val emptyCart = if (isFr) "Panier vide. Scannez ou ajoutez des articles." else "Cart is empty. Scan or search items."
    val itemsInCart = if (isFr) "Articles au Panier" else "Items in Cart"
    val clearCart = if (isFr) "Vider Panier" else "Clear Cart"
    val holdSale = if (isFr) "Mettre en attente" else "Hold Sale"
    val recallSale = if (isFr) "Rappeler vente" else "Recall Sale"
    val scanBarcode = if (isFr) "Scanner Code-barres" else "Scan Barcode"
    val stopCamera = if (isFr) "Arrêter Caméra" else "Stop Camera"
    val quickAddBarcode = if (isFr) "Article Inconnu - Créer Produit" else "Unknown Item - Add to Catalog"

    // Inventory & Products
    val inventoryTitle = if (isFr) "Gestion des Stocks" else "Inventory Management"
    val addProduct = if (isFr) "Ajouter Produit" else "Add Product"
    val editProduct = if (isFr) "Modifier Produit" else "Edit Product"
    val productPhoto = if (isFr) "Photo du Produit" else "Product Photo"
    val pickPhoto = if (isFr) "Choisir Photo" else "Pick Photo"
    val changePhoto = if (isFr) "Changer Photo" else "Change Photo"
    val removePhoto = if (isFr) "Supprimer" else "Remove"
    val photoAttached = if (isFr) "Photo attachée" else "Photo attached"
    val tapToAddPhoto = if (isFr) "Ajouter une image produit" else "Add product image"
    val prodName = if (isFr) "Nom du produit *" else "Product Name *"
    val prodSku = if (isFr) "Réf / SKU *" else "SKU *"
    val prodBarcode = if (isFr) "Code-barres" else "Barcode"
    val prodCost = if (isFr) "Prix d'achat" else "Cost Price"
    val prodPrice = if (isFr) "Prix de vente *" else "Sale Price *"
    val prodStock = if (isFr) "Qté en stock" else "Stock Qty"
    val prodReorder = if (isFr) "Seuil d'alerte" else "Alert Level"
    val prodCategory = if (isFr) "Catégorie" else "Category"
    val stockOverview = if (isFr) "Aperçu Stock" else "Stock Overview"
    val adjustStock = if (isFr) "Ajuster Stock" else "Adjust Stock"
    val skusCount = if (isFr) "Articles" else "SKUs"
    val noProducts = if (isFr) "Aucun produit en stock" else "No products in inventory yet"
    val noMatch = if (isFr) "Aucun résultat trouvé" else "No matching products"

    // History & Reports
    val salesAndReports = if (isFr) "Ventes & Rapports" else "Sales & Reports"
    val mySalesHistory = if (isFr) "Mon Historique de Ventes" else "My Sales History"
    val receiptsTab = if (isFr) "Reçus" else "Receipts"
    val sellerPerfTab = if (isFr) "Performance Vendeurs" else "Seller Performance"
    val storeTotalRevenue = if (isFr) "CHIFFRE D'AFFAIRES MAGASIN" else "TOTAL STORE REVENUE"
    val myTotalRevenue = if (isFr) "TOTAL DE MES VENTES" else "MY SALES TOTAL"
    val refund = if (isFr) "Remboursement" else "Refund"
    val reprint = if (isFr) "Réimprimer" else "Reprint"
    val details = if (isFr) "Détails" else "Details"
    val transactionDetails = if (isFr) "Détails de la Transaction" else "Transaction Details"
    val refunded = if (isFr) "Remboursé" else "Refunded"
    val completed = if (isFr) "Complété" else "Completed"
    val totalUnitsSold = if (isFr) "Unités vendues" else "Units sold"

    // Setup Wizard & Config
    val setupCenter = if (isFr) "Configuration Magasin" else "Business Setup Center"
    val setupComplete = if (isFr) "Configuration Active" else "Store Setup Complete"
    val setupProfile = if (isFr) "1. Profil du Magasin" else "1. Store Profile"
    val setupCurrencyTax = if (isFr) "2. Devises & Taxes" else "2. Currency & Tax Rules"
    val setupReceipt = if (isFr) "3. Reçus & Impression" else "3. Receipt & Thermal Printing"
    val setupCategories = if (isFr) "4. Catégories Produits" else "4. Product Categories"
    val setupProducts = if (isFr) "5. Ajouter Produits" else "5. Add First Products"
    val setupRules = if (isFr) "6. Règles de Caisse" else "6. POS Operating & Stock Rules"
    val finalizeSetup = if (isFr) "Finaliser la Configuration" else "Finalize Business Setup"
    val configured = if (isFr) "Configuré" else "Configured"

    // Settings
    val settingsTitle = if (isFr) "Paramètres & Matériel" else "Settings & Hardware"
    val userManagement = if (isFr) "Gestion des Utilisateurs" else "User Management"
    val userManagementSub = if (isFr) "Comptes du personnel, rôles et codes PIN" else "Manage staff accounts, assign roles, reset PINs"
    val businessSetup = if (isFr) "Profil Magasin & Reçus" else "Business & Receipt Setup"
    val businessSetupSub = if (isFr) "Nom, devises, taxes, papier et catégories" else "Store name, tax rules, paper width, categories"
    val thermalPrinter = if (isFr) "Imprimante Thermique Bluetooth" else "Bluetooth Thermal Printer"
    val printerNotPaired = if (isFr) "Aucune imprimante associée (toucher pour choisir)" else "No printer paired (tap to select)"
    val audioBeep = if (isFr) "Bip sonore au scan" else "Scanner Beep Audio"
    val hapticFeedback = if (isFr) "Vibration tactile au scan" else "Scanner Vibration Feedback"
    val languageSection = if (isFr) "Langue de l'application" else "Application Language"
    val languageSubtitle = if (isFr) "Bascule instantanée Français / Anglais" else "Instant toggle French / English"
    val logout = if (isFr) "Déconnexion" else "Logout"
    val lockSession = if (isFr) "Verrouiller Caisse" else "Lock Register"

    // Common Buttons & Labels
    val save = if (isFr) "Enregistrer" else "Save"
    val cancel = if (isFr) "Annuler" else "Cancel"
    val done = if (isFr) "Terminé" else "Done"
    val delete = if (isFr) "Supprimer" else "Delete"
    val close = if (isFr) "Fermer" else "Close"
    val search = if (isFr) "Rechercher" else "Search"
    val confirm = if (isFr) "Confirmer" else "Confirm"
    val recentTransactions = if (isFr) "Transactions Récentes" else "Recent Transactions"
    val viewAll = if (isFr) "Voir Tout" else "View All"
    val noSalesYet = if (isFr) "Aucune vente enregistrée pour le moment." else "No sales recorded yet."
    val languageLabel = if (isFr) "Français (FR)" else "English (EN)"
    val switchLanguage = if (isFr) "English" else "Français"
    val qtyAbbrev = if (isFr) "Qté" else "Qty"
    val pcsAbbrev = if (isFr) "pcs" else "pcs"
    val marginAbbrev = if (isFr) "Marge" else "Margin"
    val costAbbrev = if (isFr) "Coût" else "Cost"
    val eachAbbrev = if (isFr) "/u" else "each"
    val estAbbrev = if (isFr) "Est." else "Est."
    val refAbbrev = if (isFr) "Réf" else "SKU"
    val avgAbbrev = if (isFr) "Moy." else "Avg."
    val tenderAbbrev = if (isFr) "Paiem." else "Tender"

    // About & Open Source
    val aboutApp = if (isFr) "À Propos & Contributeurs" else "About & Contributors"
    val aboutAppSub = if (isFr) "Initiateur du projet, mission open source & équipe" else "Project initiator, open source mission & team"
    val appTagline = if (isFr) "Système de Point de Vente et Gestion de Stock Mobile Open Source" else "Open Source Mobile Point of Sale & Stock Management System"
    val initiatorRole = if (isFr) "Initiateur & Architecte Principal du Projet" else "Project Initiator & Lead Software Architect"
    val initiatorName = "Alexis Mupole"
    val initiatorEducation = if (isFr) {
        "Informaticien & Développeur (Licence en Computer Science aux USA & Licence en Business Computing en Ouganda). Passionné par l'accessibilité et la sécurité numérique."
    } else {
        "Computer Scientist & Software Engineer (B.S. in Computer Science in the USA & B.S. in Business Computing in Uganda). Passionate about digital accessibility and security."
    }
    val initiatorBio = if (isFr) {
        "Consultant en ingénierie numérique, j'accompagne entreprises, ONG et particuliers avec plus de 5 ans d'expérience terrain. Du développement d'applications web sur mesure à la sécurisation de vos systèmes et à la collecte de données mobiles, je conçois des solutions fiables qui transforment vos défis techniques en résultats concrets."
    } else {
        "Digital engineering consultant supporting enterprises, NGOs, and entrepreneurs with over 5 years of real-world field experience. From custom mobile and web applications to infrastructure security and offline data collection, I engineer reliable solutions that transform technical challenges into tangible outcomes."
    }
    val initiatorLocation = "Kinshasa, RD Congo"
    val openSourceCommunity = if (isFr) "Projet Open Source & Communauté" else "Open Source Project & Community"
    val openSourceDesc = if (isFr) {
        "Ce projet est entièrement ouvert et conçu selon une Clean Architecture moderne (Kotlin, Jetpack Compose, Room, WorkManager, Google Drive API). Chaque contributeur est invité à apporter ses améliorations et à ajouter ses coordonnées dans le fichier CONTRIBUTORS.md !"
    } else {
        "This project is 100% open source, architected with modern Clean Architecture principles (Kotlin, Jetpack Compose, Room, WorkManager, Google Drive API). Every developer is welcome to contribute improvements and add their details to the CONTRIBUTORS.md directory!"
    }
    val howToContribute = if (isFr) "Comment Contribuer ?" else "How to Contribute?"
    val contributionGuide = if (isFr) {
        "1. Forkez le dépôt GitHub\n2. Créez votre branche de fonctionnalité (feature/nouvelle-fonctionnalite)\n3. Développez et testez vos modifications avec Jetpack Compose et Room\n4. Ajoutez vos informations dans CONTRIBUTORS.md\n5. Ouvrez une Pull Request avec une description détaillée"
    } else {
        "1. Fork the GitHub repository\n2. Create your feature branch (feature/awesome-feature)\n3. Build and test your changes with Jetpack Compose & Room\n4. Add your profile to CONTRIBUTORS.md\n5. Open a Pull Request with a clear description"
    }
    val licenseNotice = if (isFr) "Licence Open Source Apache 2.0 - Gratuit et Libre d'utilisation" else "Apache 2.0 Open Source License - Free & Libre to Use"
    val shareApp = if (isFr) "Partager l'application" else "Share App"
}
