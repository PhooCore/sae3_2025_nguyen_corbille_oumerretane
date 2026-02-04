#  🚗  Application de Gestion de Stationnement - Toulouse

## Table des matières

- [Description du projet](#description-du-projet)
- [Problématique](#problématique)
- [Méthodologie de développement](#méthodologie-de-développement)
- [Architecture et technologies](#architecture-et-technologies)
- [Fonctionnalités principales](#fonctionnalités-principales)
  - [Espace utilisateur](#espace-utilisateur)
  - [Espace administrateur](#espace-administrateur)
- [Organisation des sprints](#organisation-des-sprints)
- [Installation et Configuration](#installation-et-configuration)
- [Compétences développées](#compétences-développées)

## Description du projet

Ce projet a été réalisé en ce moment même pendant notre **2e année de BUT Informatique** en Parcours Réalisation d'Application, en trio.

L'application a pour objectif de permettre a des utilisateurs de se stationner dans Toulouse que ce soit en parking ou en voirie.

Vous pouvez retrouver au dessus le MCD qui nous permis de faire notre base de données. Il y a aussi le Story Mapping qui nous a lui permis de réaliser nos User Stories.


## Problématique

Le stationnement en voirie à Toulouse est fortement réglementé, avec des règles spécifiques pour chaque zone (jaune, orange, rouge, verte, bleue) et des tarifs variables en fonction de la durée et de la localisation. Les usagers doivent naviguer entre différentes zones, horaires, et méthodes de paiement, ce qui peut être complexe et chronophage. De plus, il existe des options de stationnement gratuit ou à tarif réduit, mais elles ne sont pas toujours bien connues ou accessibles.

Notre application propose de gérer ces informations aussi bien pour les utilisateurs que pour les gestionnaires, en fournissant des informations claires sur les zones de stationnement, les tarifs, les horaires, et les options de paiement. Elle intègre également des fonctionnalités pour le paiement et la localisation des places disponibles.

## Méthodologie de développement

Le projet a été développé en suivant la **méthode Agile Scrum** :

- **3 sprints** d'environ 1 mois chacun
- Développement itératif et incrémental
- Gestion de projet sur GitLab
- Chaque sprint s'est concentré sur des fonctionnalités spécifiques

## Architecture et technologies

### Architecture logicielle

- **Pattern MVC** (Modèle-Vue-Contrôleur) pour une séparation claire des responsabilités
- **DAO** (Data Access Object) pour la gestion de la persistance des données

### Technologies utilisées

- **Java** comme langage de programmation
- **Eclipse IDE** comme environnement de développement
- **Windows Builder** pour la conception de l'interface graphique
- **JavaFX** pour l'interface graphique
- **MySQL** comme système de gestion de base de données
- **JDBC** pour la connexion à la base de données
- **GitLab** pour le versionnement et la gestion de projet

### Modélisation

- **Diagrammes états-transitions** pour la modélisation du comportement de l'application

## Fonctionnalités principales

### Système d'authentification

L'application nécessite une authentification pour accéder aux différentes fonctionnalités.

### Espace utilisateur

- **Stationnement** : Possibilité de stationner en voirie ou dans un parking
- **Carte interactive** : Visualisation des parkings sur une map JavaFX avec leurs emplacements
- **Gestion du profil** :
  - Consultation de l'historique des paiements
  - Suivi des stationnements
  - Gestion des véhicules enregistrés
- **Favoris** : Sauvegarde des parkings favoris
- **Barre de recherche** : Recherche rapide de parkings
- **Feedback** : Possibilité d'envoyer des retours sur l'application

### Espace administrateur

Accessible via un bouton dédié lorsqu'on est connecté sur un compte administrateur :

- **Gestion des parkings** : Ajout et modification de parkings
- **Gestion des utilisateurs** : Consultation de la liste des utilisateurs
- **Gestion des feedbacks** : Réponse aux retours des utilisateurs

## Organisation des sprints

Le projet a été structuré en **3 sprints principaux** :

1. **Sprint 1** : Mise en place de l'architecture MVC, système d'authentification, fonctionnalités de base du côté utilisateur, gestion des stationnements et paiements
2. **Sprint 2** : Intégration de la carte interactive en PNG , Espace de l'administrateur
3. **Sprint 3** : Intégration d'une réelle MAP interactive, favoris, feedback, structure DAO respecté

## Installation et Configuration

### Prérequis

- Java JDK 8 ou supérieur
- Eclipse IDE
- MySQL (ou accès à une base de données MySQL)
- Connecteur MySQL JDBC

### Configuration de la base de données

1. Copiez le fichier `db.properties.template` et renommez-le en `db.properties`
2. Remplissez vos informations de connexion dans `db.properties` :

```properties
db.host=VOTRE_HOST
db.database=VOTRE_DATABASE
db.port=3306
db.user=VOTRE_USER
db.password=VOTRE_PASSWORD
```

3. Placez le fichier `db.properties` à la racine du projet ou dans le dossier `/src/main/java/modele/dao`

⚠️ **Important** : Le fichier `db.properties` contient des informations sensibles et ne doit **JAMAIS** être partagé ou versionné sur Git. Il est automatiquement ignoré par le `.gitignore`.

### Lancement du projet

1. Importez le projet dans Eclipse
2. Assurez-vous que le connecteur MySQL JDBC est dans le classpath
3. Configurez votre fichier `db.properties` avec vos identifiants
4. Lancez l'application

## Compétences développées

- Travail en équipe et gestion de projet Agile
- Architecture logicielle (MVC, DAO)
- Développement d'interfaces graphiques avec JavaFX et Windows Builder
- Intégration cartographique
- Modélisation avec diagrammes états-transitions
- Gestion de base de données MySQL et requêtes SQL
- Connexion à une base de données avec JDBC
- Gestion sécurisée des configurations et identifiants
- Utilisation de GitLab pour le versionnement et la collaboration

---

## Auteur

* **NGUYEN Phuong** | [GitHub-PhooCore](https://github.com/PhooCore)
* **OUMERRETANE Emmy** | [GitHub-emmyo-git](https://github.com/emmyo-git)
* **CORBILLÉ Iris** | [GitHub-iriscrbl](https://github.com/iriscrbl)

Projet réalisé dans le cadre de la 2e année de BUT Informatique.

---

## Licence

Projet académique réalisé à des fins pédagogiques.

⭐ *Si ce projet vous a été utile, n'hésitez pas à lui donner une étoile !*
