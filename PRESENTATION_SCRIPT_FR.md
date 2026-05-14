# Script de Présentation Détaillé : ArtConnect Pro (3-4 Minutes)

Ce script est conçu pour une présentation fluide après la partie SQL, avec un focus sur l'architecture Java et la persistance.

---

## 1. Transition & Architecture (0:00 - 1:00)
**Objectif :** Expliquer le passage du SQL au Java via le pattern DAO.

*   **Parole :** 
    > "Merci [Nom du camarade]. Maintenant que nous avons vu le 'moteur' SQL, je vais vous montrer comment l'application Java ArtConnect Pro communique avec lui. 
    > 
    > Pour cela, nous avons utilisé le pattern **DAO (Data Access Object)**. Le DAO est un tampon entre nos données et notre logique métier. Son rôle est simple : isoler la façon dont on stocke les données. 
    > 
    > Si vous regardez le fichier `GalleryDao.java`, c'est une **interface**. Elle définit *ce que* l'on peut faire (sauvegarder, lire, supprimer), mais pas *comment*. C'est l'implémentation `JdbcGalleryDao.java` qui contient le code technique."

*   **À montrer :** 
    *   `src/main/java/com/project/artconnect/dao/GalleryDao.java` (L'interface propre).
    *   `src/main/java/com/project/artconnect/persistence/JdbcGalleryDao.java` (Le code concret).

---

## 2. Le fonctionnement JDBC & CRUD (1:00 - 2:00)
**Objectif :** Expliquer la technologie et les 4 opérations de base.

*   **Parole :** 
    > "Pour réaliser cette connexion, nous utilisons **JDBC (Java Database Connectivity)**. C'est l'API standard de Java pour discuter avec MySQL. 
    > 
    > Dans le fichier `JdbcGalleryDao.java`, nous gérons les opérations **CRUD** (Create, Read, Update, Delete) :
    > 1. **Create** : La méthode `save()` utilise un `PreparedStatement` pour envoyer un `INSERT INTO` à la base.
    > 2. **Read** : La méthode `findAll()` exécute un `SELECT` et transforme chaque ligne du `ResultSet` en un objet Java `Gallery`.
    > 3. **Update** : La méthode `update()` met à jour les informations en base.
    > 4. **Delete** : La méthode `delete()` supprime l'entrée correspondante.
    > 
    > L'avantage ? Si demain nous changeons MySQL pour une autre base, seule cette classe change, le reste de l'application reste intact."

*   **À montrer :** 
    *   Dans `JdbcGalleryDao.java`, pointez les méthodes : `save` (ligne 50), `findAll` (ligne 33), `update` (ligne 66).

---

## 3. Démonstration en Direct (2:00 - 3:00)
**Objectif :** Prouver que tout fonctionne en temps réel.

*   **Action 1 : CREATE & READ**
    *   "Je vais ajouter une nouvelle galerie : 'Galerie Belle-Vue' avec une note de 4.5."
    *   *Action :* Remplissez le formulaire dans l'onglet **Galleries** et cliquez sur **Ajouter**.
    *   "Ici, Java a appelé `save()`, MySQL a exécuté l'INSERT, et le tableau s'est rafraîchi via `findAll()`."

*   **Action 2 : UPDATE & TRIGGERS**
    *   "Maintenant, modifions sa note à 10. Rappelez-vous du Trigger SQL : il va brider la note à 5."
    *   *Action :* Sélectionnez la galerie, mettez '10' dans la note, cliquez sur **Modifier**.
    *   "La note est devenue 5.0. C'est la preuve que notre code Java subit bien les règles métier de la base de données."

*   **Action 3 : FONCTION & CALCUL**
    *   *Action :* Allez dans l'onglet **Workshops**, sélectionnez-en un et cliquez sur **💰 Calculer Revenu Max**.
    *   "Ce calcul n'est pas fait en Java, mais par une **Fonction SQL** appelée via JDBC. C'est plus rapide et plus sûr."

---

## 4. Performance & Conclusion (3:00 - 3:30)
**Objectif :** Terminer sur une note technique pro.

*   **Parole :** 
    > "Pour finir, nous avons optimisé le démarrage. Grâce à un **Thread asynchrone** dans `ServiceProvider.java`, l'interface s'ouvre instantanément et les données MySQL sont chargées en tâche de fond. 
    > 
    > En résumé, ArtConnect Pro est une application robuste qui respecte les standards de l'industrie (DAO, JDBC) pour offrir une gestion d'art fluide et professionnelle. Merci de votre attention."

*   **À montrer :** 
    *   `src/main/java/com/project/artconnect/util/ServiceProvider.java` (Le thread de chargement ligne 51).

---

## 📂 Rappel des fichiers clés
1.  **`GalleryDao.java`** : L'interface (Le contrat).
2.  **`JdbcGalleryDao.java`** : Le code JDBC (Le CRUD).
3.  **`ServiceProvider.java`** : L'aiguillage (L'async).
4.  **`MainApp.java`** : Le point d'entrée.
