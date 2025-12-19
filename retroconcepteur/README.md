# Présentation du projet

Ce projet consiste à développer un outil de rétro‑conception Java‑UML. L’objectif est d’analyser automatiquement des fichiers .java afin de produire une représentation UML conforme au formalisme des diagrammes de classes : classes et interfaces, attributs, méthodes, associations et multiplicités, héritage et implémentation.

Le projet fonctionne désormais selon deux modes :
- CUI (Console User Interface) : affichage textuel UML
- GUI (Graphical User Interface / IHM) : visualisation graphique interactive du diagramme

Ce dossier correspond au rendu final du projet.

### Équipe projet – Groupe 7

- Quentin MORVAN G2
- Valentin LEROY G2
- Celim CHAOU F1
- Enzo DUMONT G1
- Ariunbayar BUYANBADRAKH G1
- Yassine EL MAADI G1


# Exécution du programme

Voici la commande à utiliser pour compiler et exécuter, selon votre système d'exploitation.

### Étapes communes :

1) Ouvrez un terminal (ou un terminal PowerShell sous Windows)

2) Placez-vous dans le dossier du projet : `cd retroconcepteur`

3) Lancez la commande correspondante à votre système :

   - Sous Windows :
     - Pour compiler : `javac -d class @compile.list`
     - Pour exécuter : `java -cp class controleur.Controleur gui`              (mode graphique) ou 
                       `java -cp class controleur.Controleur console <chemin>` (mode console)
                       `./run.bat` (choisir gui ou console)

   - Sous Linux/macOS :
     - Pour compiler : `javac -d class @compile.list`
     - Pour exécuter : `./run.sh` (choisir gui ou console)

Le script `run.sh` facilite l'exécution sous Linux/macOS en compilant automatiquement et en proposant le choix du mode.


# Arborescence du projet

```
sae3.01/
└── retroconcepteur/
    ├── class/                     # Fichiers compilés (.class)
    │   ├── controleur/
    │   │   └── Controleur.class
    │   ├── modele/
    │   │   ├── AnalyseMetier.class
    │   │   ├── AnalyseurUML.class
    │   │   ├── Sauvegarde.class
    │   │   ├── entites/
    │   │   │   ├── AssociationObjet.class
    │   │   │   ├── AttributObjet.class
    │   │   │   ├── ClasseObjet.class
    │   │   │   ├── HeritageObjet.class
    │   │   │   ├── InterfaceObjet.class
    │   │   │   ├── LiaisonObjet.class
    │   │   │   ├── MethodeObjet.class
    │   │   │   └── MultipliciteObjet.class
    │   │   └── outil/
    │   │       ├── DispositionOptimiseur.class
    │   │       └── ParsingUtil.class
    │   └── vue/
    │       ├── BarreMenus.class
    │       ├── BlocClasse.class
    │       ├── ConsoleVue.class
    │       ├── DiagramPresenter.class
    │       ├── EditeurLiaisonDialog.class
    │       ├── ExportHelper.class
    │       ├── FenetrePrincipale.class
    │       ├── LiaisonVue.class
    │       ├── PanneauDiagramme.class
    │       ├── PanneauProjets.class
    │       └── PresentationMapper.class
    │
    ├── src/                       # Code source Java
    │   ├── controleur/
    │   │   └── Controleur.java
    │   ├── modele/
    │   │   ├── AnalyseMetier.java
    │   │   ├── AnalyseurUML.java
    │   │   ├── Sauvegarde.java
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
    │   │       ├── DispositionOptimiseur.java
    │   │       └── ParsingUtil.java
    │   └── vue/
    │       ├── BarreMenus.java
    │       ├── BlocClasse.java
    │       ├── ConsoleVue.java
    │       ├── DiagramPresenter.java
    │       ├── EditeurLiaisonDialog.java
    │       ├── ExportHelper.java
    │       ├── FenetrePrincipale.java
    │       ├── LiaisonVue.java
    │       ├── PanneauDiagramme.java
    │       ├── PanneauProjets.java
    │       └── PresentationMapper.java
    │
    ├── test/                      # Jeux de tests intermédiaires
    │   ├── Animal.java
    │   ├── Capacte.java
    │   ├── Chat.java
    │   ├── Chien.java
    │   ├── ChienT.java
    │   ├── Disque.java
    │   ├── Ferme.java
    │   ├── Parler.java
    │   └── Point.java
    │
    ├── testFinal/                 # Jeux de tests finaux
    │   ├── Carre.java
    │   ├── Disque.java
    │   ├── ISurface.java
    │   ├── Point.java
    │   └── Rectangle.java
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
    ├── Structure.txt              # Description de la structure
    ├── .gitignore
    └── README.md                  # Ce fichier
```
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
    ├── Structure.txt              # Description de la structure
    ├── .gitignore
    └── README.md                  # Ce fichier


