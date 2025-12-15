# Présentation du projet

Ce projet consiste à développer un outil de rétro‑conception Java‑UML. L’objectif est d’analyser automatiquement des fichiers .java afin de produire une représentation UML conforme au formalisme des diagrammes de classes : classes et interfaces, attributs, méthodes, associations et multiplicités, héritage et implémentation.

Le projet fonctionne désormais selon deux modes :
- CUI (Console User Interface) : affichage textuel UML
- GUI (Graphical User Interface / IHM) : visualisation graphique interactive du diagramme

Ce dossier correspond au rendu du vendredi 19/12, contenant la version finale de l'étape 7.

### Équipe projet – Groupe 7

*Quentin MORVAN G2
*Valentin LEROY G2
*Celim CHAOU F1
*Enzo DUMONT G1
*Ariunbayar BUYANBADRAKH G1
*Yassine EL MAADDI G1


# Exécution du programme

Voici la commande à utiliser pour compiler et exécuter, selon votre système d'exploitation.

### Étapes communes :

1) Ouvrez un terminal (ou un terminal PowerShell sous Windows)

2) Placez-vous dans le dossier du projet : ***cd nom***

3) Lancez les commande correspondante à votre système :

   - Sous Windows     :
    - Pour compiler   :  javac -encoding UTF-8 -d class @compile.list
    - Pour exécuter   :  java -cp class src/AnalyseIHMControleur data

   - Sous Linux/macOS : 
    - Pour compiler   :  javac -d class @compile.list
    - Pour exécuter   :  java -cp class src/AnalyseIHMControleur data


# Arborescence du projet

sae3.01/
└── retroconcepteur/
    ├── class/                     # Fichiers compilés (.class)
    │   └── src/
    │       ├── AnalyseIHMControleur.class
    │       ├── Controleur.class
    │       ├── modele/
    │       │   ├── AnalyseurUML.class
    │       │   ├── entites/
    │       │   │   ├── AssociationObjet.class
    │       │   │   ├── AttributObjet.class
    │       │   │   ├── ClasseObjet.class
    │       │   │   ├── HeritageObjet.class
    │       │   │   ├── InterfaceObjet.class
    │       │   │   ├── LiaisonObjet.class
    │       │   │   ├── MethodeObjet.class
    │       │   │   └── MultipliciteObjet.class
    │       │   └── outil/
    │       │       └── ParsingUtil.class
    │       └── vue/
    │           ├── BarreMenus.class
    │           ├── BlocClasse.class
    │           ├── ConsoleVue.class
    │           ├── ExportIHM.class
    │           ├── FenetrePrincipale.class
    │           ├── LiaisonVue.class
    │           ├── OptimisateurDisposition.class
    │           ├── PanneauDiagramme.class
    │           └── PanneauProjets.class
    │
    ├── src/                       # Code source Java
    │   ├── AnalyseIHMControleur.java
    │   ├── Controleur.java
    │   ├── modele/
    │   │   ├── AnalyseurUML.java
    │   │   ├── entites/
    │   │   │   ├── AssociationObjet.java
    │   │   │   ├── AttributObjet.java
    │   │   │   ├── ClasseObjet.java
    │   │   │   ├── HeritageObjet.java
    │   │   │   ├── InterfaceObjet.java
    │   │   │   ├── LiaisonObjet.java
    │   │   │   ├── MethodeObjet.java
    │   │   │   └── MultipliciteObjet.java
    │   │   └── outil/
    │   │       └── ParsingUtil.java
    │   ├── vue/
    │   │   ├── BarreMenus.java
    │   │   ├── BlocClasse.java
    │   │   ├── ConsoleVue.java
    │   │   ├── ExportIHM.java
    │   │   ├── FenetrePrincipale.java
    │   │   ├── LiaisonVue.java
    │   │   ├── OptimisateurDisposition.java
    │   │   ├── PanneauDiagramme.java
    │   │   └── PanneauProjets.java
    │   ├── test/                  # Jeux de tests intermédiaires
    │   └── testFinal/             # Jeux de tests finaux
    │
    ├── data/                      # Fichiers Java à analyser
    │   ├── Carre.java
    │   ├── Disque.java
    │   ├── ISurface.java
    │   ├── Point.java
    │   └── Rectangle.java
    │
    ├── compile.list               # Liste des fichiers à compiler
    ├── run.sh                     # Script d’exécution Linux/macOS
    ├── run.bat                    # Script d’exécution Windows
    ├── diagramme.png              # Exemple d’export UML
    ├── Structure.txt
    └── README.md

