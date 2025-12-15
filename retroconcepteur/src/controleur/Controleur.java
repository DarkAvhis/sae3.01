package controleur;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import modele.entites.AssociationObjet;
import modele.entites.AttributObjet;
import modele.entites.ClasseObjet;
import modele.entites.HeritageObjet;
import modele.entites.InterfaceObjet;
import modele.entites.LiaisonObjet;
import modele.entites.MethodeObjet;
import vue.BlocClasse;
import vue.FenetrePrincipale;
import vue.LiaisonVue;
import vue.LiaisonVue.TypeLiaison;

/**
 * Contrôleur principal de l'application de génération de diagrammes UML.
 * 
 * Cette classe fait le lien entre le modèle (analyse des classes Java) et la
 * vue (interface graphique).
 * Elle orchestre l'analyse des fichiers Java, le calcul des positions optimales
 * des classes dans le diagramme
 * et la conversion des données du modèle vers la vue.
 * 
 * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT, Ariunbayar
 *         BUYANBADRAKH, Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class Controleur {
    private AnalyseIHMControleur metierComplet;
    private FenetrePrincipale vuePrincipale;
    private boolean afficherClassesExternes = true;

    // Données du dernier projet analysé
    private String dernierCheminProjet;
    private List<ClasseObjet> dernieresClasses;
    private List<AssociationObjet> dernieresAssociations;
    private List<HeritageObjet> derniersHeritages;
    private List<InterfaceObjet> dernieresImplementations;
    private List<LiaisonVue> dernieresToutesLiaisonsVue;
    private HashMap<String, Point> dernieresPositions;

    // --- Constantes pour le Layout Hiérarchique ---
    private static final int H_LAYER_SPACING = 150; // Espacement vertical minimum entre les couches
    private static final int W_NODE_SPACING = 100; // Espacement horizontal minimum entre les blocs (votre demande)
    private static final int Y_ANCHOR = 50; // Ancrage Y
    private static final int X_ANCHOR = 50; // Ancrage X
    private static final int ITERATIONS = 10; // Nombre d'itérations pour la minimisation des croisements

    /**
     * Constructeur du contrôleur.
     * Initialise le modèle d'analyse et crée la fenêtre principale de
     * l'application.
     */
    public Controleur() {
        this.metierComplet = new AnalyseIHMControleur();
        this.vuePrincipale = new FenetrePrincipale(this);
    }

    /**
     * Analyse un projet Java et affiche le diagramme UML correspondant.
     * 
     * Cette méthode lance l'analyse complète du dossier de projet, récupère les
     * classes,
     * les associations, les héritages et les implémentations d'interfaces, puis
     * calcule
     * les positions optimales avant d'afficher le diagramme.
     * 
     * @param cheminProjet Chemin absolu vers le dossier contenant les fichiers Java
     *                     à analyser
     */
    public void analyserEtAfficherDiagramme(String cheminProjet) {
        if (!this.metierComplet.analyserDossier(cheminProjet)) {
            return;
        }

        this.dernierCheminProjet = cheminProjet;
        this.dernieresClasses = this.metierComplet.getClasses();
        this.dernieresAssociations = this.metierComplet.getAssociations();
        this.derniersHeritages = this.metierComplet.getHeritages();
        this.dernieresImplementations = this.metierComplet.getImplementations();

        afficherDiagrammeAvecDonnees();
    }

    private void afficherDiagrammeAvecDonnees() {
        List<ClasseObjet> classes = this.dernieresClasses;
        List<AssociationObjet> associations = this.dernieresAssociations;
        List<HeritageObjet> heritages = this.derniersHeritages;
        List<InterfaceObjet> implementations = this.dernieresImplementations;

        HashSet<String> classesExternesAGriser = new HashSet<String>();

        // Héritages : super-classes externes
        for (HeritageObjet h : heritages) {
            ClasseObjet mere = h.getClasseMere();
            if ("externe".equals(mere.getSpecifique())) {
                classesExternesAGriser.add(mere.getNom());
            }
        }

        // Interfaces : interfaces externes implémentées
        for (InterfaceObjet impl : implementations) {
            for (ClasseObjet interfaceClasse : impl.getLstInterfaces()) {
                if ("externe".equals(interfaceClasse.getSpecifique())) {
                    classesExternesAGriser.add(interfaceClasse.getNom());
                }
            }
        }

        // Associations : classes cibles externes
        for (AssociationObjet assoc : associations) {
            ClasseObjet mere = assoc.getClasseMere();
            ClasseObjet fille = assoc.getClasseFille();
            if ("externe".equals(mere.getSpecifique())) {
                classesExternesAGriser.add(mere.getNom());
            }
            if ("externe".equals(fille.getSpecifique())) {
                classesExternesAGriser.add(fille.getNom());
            }
        }

        List<LiaisonVue> toutesLiaisonsVue = new ArrayList<>();
        toutesLiaisonsVue.addAll(convertirLiaisons(associations, TypeLiaison.ASSOCIATION_UNIDI));
        toutesLiaisonsVue.addAll(convertirLiaisons(heritages, TypeLiaison.HERITAGE));
        toutesLiaisonsVue.addAll(convertirLiaisons(implementations, TypeLiaison.IMPLEMENTATION));

        this.dernieresToutesLiaisonsVue = toutesLiaisonsVue;

        // Filtrer les classes externes si l'affichage est désactivé
        List<ClasseObjet> classesAffichees = classes;
        if (!afficherClassesExternes) {
            classesAffichees = new ArrayList<>();
            for (ClasseObjet c : classes) {
                if (!"externe".equals(c.getSpecifique())) {
                    classesAffichees.add(c);
                }
            }
        }

        List<BlocClasse> blocsAvecTailles = new ArrayList<>();
        for (ClasseObjet c : classesAffichees) {
            boolean masquerDetails = classesExternesAGriser.contains(c.getNom());

            List<String> attrVue = masquerDetails ? new ArrayList<String>()
                    : this.convertirAttributs(c.getattributs(), c);
            List<String> methVue = masquerDetails ? new ArrayList<String>()
                    : this.convertirMethodes(c.getMethodes(), c);
            blocsAvecTailles.add(new BlocClasse(c.getNom(), 0, 0, attrVue, methVue));
        }

        // --- Calcul des Positions Optimales (Hiérarchique) ---
        HashMap<String, Point> positionsOptimales = calculerPositionsOptimales(classesAffichees, toutesLiaisonsVue,
                blocsAvecTailles);

        this.dernieresPositions = positionsOptimales;

        List<BlocClasse> blocsVue = new ArrayList<>();

        for (ClasseObjet c : classesAffichees) {
            Point pos = positionsOptimales.get(c.getNom());

            boolean masquerDetails = classesExternesAGriser.contains(c.getNom());

            List<String> attrVue = masquerDetails ? new ArrayList<String>()
                    : this.convertirAttributs(c.getattributs(), c);
            List<String> methVue = masquerDetails ? new ArrayList<String>()
                    : this.convertirMethodes(c.getMethodes(), c);

            BlocClasse bloc = new BlocClasse(c.getNom(), pos.x, pos.y, attrVue, methVue);

            bloc.setSuperClasse(masquerDetails);

            if (c.getNom().contains("Interface")) {
                bloc.setInterface(true);
            }

            blocsVue.add(bloc);
        }

        if (this.vuePrincipale != null) {
            this.vuePrincipale.getPanneauDiagramme().setBlocsClasses(blocsVue);
            this.vuePrincipale.getPanneauDiagramme().setLiaisonsVue(toutesLiaisonsVue);
            this.vuePrincipale.getPanneauDiagramme().repaint();
        }
    }

    /**
     * Calcule les positions optimales des classes dans le diagramme.
     * 
     * Utilise un algorithme hiérarchique inspiré de Sugiyama pour organiser les
     * classes
     * en couches et minimiser les croisements de liaisons.
     * 
     * @param classes          Liste des classes à positionner
     * @param liaisons         Liste des liaisons entre les classes
     * @param blocsAvecTailles Liste des blocs graphiques avec leurs dimensions
     *                         calculées
     * @return Map associant chaque nom de classe à sa position (Point) dans le
     *         diagramme
     */
    private HashMap<String, Point> calculerPositionsOptimales(List<ClasseObjet> classes, List<LiaisonVue> liaisons,
            List<BlocClasse> blocsAvecTailles) {
        HashMap<String, BlocClasse> blocMap = new HashMap<>();

        // Map de référence pour la taille et l'objet
        for (BlocClasse bloc : blocsAvecTailles) {
            blocMap.put(bloc.getNom(), bloc);
        }

        // --- PHASE 1: ASSIGNATION DES COUCHES (Ranking) ---
        HashMap<String, Integer> couches = assignerCouches(classes, liaisons);

        // Grouper les classes par couche
        HashMap<Integer, List<String>> classesParCouche = new HashMap<>();
        for (ClasseObjet classe : classes) {
            int couche = couches.get(classe.getNom());
            classesParCouche.computeIfAbsent(couche, k -> new ArrayList<>()).add(classe.getNom());
        }

        // --- PHASE 2: MINIMISATION DES CROISEMENTS (Ordering - Barycentre) ---
        List<Integer> indexCouches = new ArrayList<>(classesParCouche.keySet());
        Collections.sort(indexCouches);

        for (int iter = 0; iter < ITERATIONS; iter++) {
            for (int i = 0; i < indexCouches.size(); i++) {
                int coucheCourante = indexCouches.get(i);

                if (i + 1 < indexCouches.size()) {
                    int coucheSuivante = indexCouches.get(i + 1);
                    minimiserCroisements(coucheCourante, classesParCouche.get(coucheCourante),
                            classesParCouche.get(coucheSuivante), liaisons, blocMap, true);
                }

                if (i > 0) {
                    int couchePrecedente = indexCouches.get(i - 1);
                    minimiserCroisements(coucheCourante, classesParCouche.get(coucheCourante),
                            classesParCouche.get(couchePrecedente), liaisons, blocMap, false);
                }
            }
        }

        // --- PHASE 3: ASSIGNATION DES COORDONNÉES (Placement) ---
        HashMap<String, Point> positions = new HashMap<>();
        int y_courant = Y_ANCHOR; // ANCRE Y

        for (int couche : indexCouches) {
            List<String> nomsCouche = classesParCouche.get(couche);

            int x_courant = X_ANCHOR; // ANCRE X
            int max_height_couche = 0;

            // Placer les nœuds
            for (int i = 0; i < nomsCouche.size(); i++) {
                String nom = nomsCouche.get(i);
                BlocClasse bloc = blocMap.get(nom);

                // Appliquer les coordonnées finales
                positions.put(nom, new Point(x_courant, y_courant));

                // Mettre à jour les compteurs
                x_courant += bloc.getLargeur() + W_NODE_SPACING; // Espacement de 100px
                max_height_couche = Math.max(max_height_couche, bloc.getHauteur());

                // ANCRAGE DE LA TOUTE PREMIÈRE CLASSE EN (50, 50)
                if (couche == indexCouches.get(0) && i == 0) {
                    positions.put(nom, new Point(X_ANCHOR, Y_ANCHOR));
                }
            }

            // Passer à la couche suivante : Y_courant + hauteur max de cette couche +
            // espacement
            y_courant += max_height_couche + H_LAYER_SPACING;
        }

        return positions;
    }

    /**
     * Assigne une couche (niveau hiérarchique) à chaque classe.
     * 
     * Les classes parentes sont placées dans des couches supérieures,
     * les classes enfants dans des couches inférieures.
     * 
     * @param classes  Liste des classes à organiser
     * @param liaisons Liste des liaisons entre classes
     * @return Map associant chaque nom de classe à son numéro de couche
     */
    private HashMap<String, Integer> assignerCouches(List<ClasseObjet> classes, List<LiaisonVue> liaisons) {
        HashMap<String, Integer> couches = new HashMap<>();
        classes.forEach(c -> couches.put(c.getNom(), 0));

        boolean changed = true;
        while (changed) {
            changed = false;
            for (LiaisonVue liaison : liaisons) {
                if (liaison.getType() == TypeLiaison.HERITAGE || liaison.getType() == TypeLiaison.IMPLEMENTATION) {

                    String parent = liaison.getNomClasseDest();
                    String enfant = liaison.getNomClasseOrig();

                    int coucheParent = couches.getOrDefault(parent, 0);
                    int coucheEnfant = couches.getOrDefault(enfant, 0);

                    if (coucheEnfant <= coucheParent) {
                        couches.put(enfant, coucheParent + 1);
                        changed = true;
                    }
                }
            }
        }
        return couches;
    }

    /**
     * Minimise les croisements de liaisons en réorganisant les classes d'une
     * couche.
     * 
     * Utilise la méthode du barycentre pour optimiser l'ordre des classes
     * en fonction de leurs connexions avec la couche adjacente.
     * 
     * @param coucheCouranteIndex Index de la couche courante
     * @param nomsCoucheCourante  Noms des classes de la couche à réorganiser
     * @param nomsCoucheFixe      Noms des classes de la couche de référence
     * @param liaisons            Liste des liaisons entre classes
     * @param blocMap             Map des blocs graphiques
     * @param forward             true pour parcours descendant, false pour parcours
     *                            ascendant
     */
    private void minimiserCroisements(int coucheCouranteIndex, List<String> nomsCoucheCourante,
            List<String> nomsCoucheFixe, List<LiaisonVue> liaisons, HashMap<String, BlocClasse> blocMap,
            boolean forward) {
        HashMap<String, Double> barycentres = new HashMap<>();

        for (String nomCourant : nomsCoucheCourante) {
            double positionFixeTotale = 0;
            int voisins = 0;

            for (LiaisonVue liaison : liaisons) {
                String nomVoisin = null;

                if (forward && liaison.getNomClasseOrig().equals(nomCourant)
                        && nomsCoucheFixe.contains(liaison.getNomClasseDest())) {
                    nomVoisin = liaison.getNomClasseDest();
                } else if (!forward && liaison.getNomClasseDest().equals(nomCourant)
                        && nomsCoucheFixe.contains(liaison.getNomClasseOrig())) {
                    nomVoisin = liaison.getNomClasseOrig();
                }

                if (nomVoisin != null) {
                    int indexVoisin = nomsCoucheFixe.indexOf(nomVoisin);
                    positionFixeTotale += indexVoisin;
                    voisins++;
                }
            }

            if (voisins > 0) {
                barycentres.put(nomCourant, positionFixeTotale / voisins);
            } else {
                barycentres.put(nomCourant, (double) nomsCoucheCourante.indexOf(nomCourant));
            }
        }

        Collections.sort(nomsCoucheCourante, Comparator.comparingDouble(barycentres::get));
    }

    // ... (Reste des méthodes auxiliaires et main inchangés) ...

    /**
     * Sauvegarde les positions actuelles des blocs du diagramme.
     * 
     * @note Cette méthode est actuellement en développement
     */
    public void sauvegarde() {
        if (this.vuePrincipale == null)
            return;
        List<BlocClasse> blocs = this.vuePrincipale.getPanneauDiagramme().getBlocsClasses();
        System.out.println("Sauvegarde des positions de " + blocs.size() + " blocs.");
    }

    /**
     * Supprime la classe actuellement sélectionnée du diagramme.
     * 
     * @note Cette méthode est actuellement en développement
     */
    public void supprimerClasseSelectionnee() {
        if (this.vuePrincipale == null)
            return;
        BlocClasse bloc = this.vuePrincipale.getPanneauDiagramme().getBlocsClasseSelectionnee();
        // ... (Logique de suppression inchangée) ...
    }

    /**
     * Convertit les liaisons du modèle vers les liaisons de la vue.
     * 
     * Transforme les objets LiaisonObjet (modèle métier) en objets LiaisonVue
     * (représentation graphique) en extrayant les informations nécessaires à
     * l'affichage.
     * 
     * @param liaisons Liste des liaisons du modèle à convertir
     * @param type     Type de liaison (ASSOCIATION, HERITAGE, IMPLEMENTATION)
     * @return Liste des liaisons prêtes pour l'affichage graphique
     */
    private List<LiaisonVue> convertirLiaisons(List<? extends LiaisonObjet> liaisons, TypeLiaison type) {
        List<LiaisonVue> liaisonsVue = new ArrayList<>();
        for (LiaisonObjet liaison : liaisons) {
            // Traitement spécial pour les InterfaceObjet qui stockent les interfaces dans
            // une liste
            if (liaison instanceof InterfaceObjet) {
                InterfaceObjet interfaceLiaison = (InterfaceObjet) liaison;
                if (interfaceLiaison.getClasseFille() == null) {
                    continue;
                }
                String nomClasseConcrete = interfaceLiaison.getClasseFille().getNom();

                // Parcourir la liste des interfaces implémentées
                List<ClasseObjet> interfaces = interfaceLiaison.getLstInterfaces();
                for (ClasseObjet interfaceClass : interfaces) {
                    if (interfaceClass != null) {
                        liaisonsVue.add(new LiaisonVue(nomClasseConcrete, interfaceClass.getNom(), type, null, null));
                    }
                }
            } else {
                // Vérifier que les deux classes existent pour les autres liaisons
                if (liaison.getClasseFille() == null || liaison.getClasseMere() == null) {
                    System.err.println("Attention: liaison avec classe null ignorée");
                    continue;
                }

                String nomOrig = liaison.getClasseFille().getNom();
                String nomDest = liaison.getClasseMere().getNom();

                String multOrig = null;
                String multDest = null;

                if (liaison instanceof AssociationObjet) {
                    AssociationObjet asso = (AssociationObjet) liaison;

                    multOrig = asso.getMultOrig() != null ? asso.getMultOrig().toString() : "1..1";
                    multDest = asso.getMultDest() != null ? asso.getMultDest().toString() : "1..1";
                }

                liaisonsVue.add(new LiaisonVue(nomOrig, nomDest, type, multOrig, multDest));
            }
        }
        return liaisonsVue;
    }

    /**
     * Convertit les attributs d'une classe en format d'affichage UML.
     * 
     * Transforme les objets AttributObjet en chaînes formatées selon la notation
     * UML,
     * incluant la visibilité, le nom, le type et les modificateurs (static).
     * 
     * @param attributs Liste des attributs à convertir
     * @param classe    Classe contenant les attributs (pour accéder aux méthodes de
     *                  conversion)
     * @return Liste des attributs formatés pour l'affichage
     */
    private List<String> convertirAttributs(List<AttributObjet> attributs, ClasseObjet classe) {
        List<String> liste = new ArrayList<>();
        for (AttributObjet att : attributs) {
            String staticFlag = att.estStatique() ? " {static}" : "";
            char visibilite = classe.changementVisibilite(att.getVisibilite());

            String s = visibilite + " " + att.getNom() + " : " + att.getType() + staticFlag;
            liste.add(s);
        }
        return liste;
    }

    /**
     * Convertit les méthodes d'une classe en format d'affichage UML.
     * 
     * Transforme les objets MethodeObjet en chaînes formatées selon la notation
     * UML,
     * incluant la visibilité, le nom, les paramètres, le type de retour et les
     * modificateurs (static).
     * 
     * @param methodes Liste des méthodes à convertir
     * @param classe   Classe contenant les méthodes (pour accéder aux méthodes de
     *                 conversion)
     * @return Liste des méthodes formatées pour l'affichage
     */
    private List<String> convertirMethodes(List<MethodeObjet> methodes, ClasseObjet classe) {
        List<String> liste = new ArrayList<>();
        for (MethodeObjet met : methodes) {
            String staticFlag = met.estStatique() ? "{static} " : "";
            char visibilite = classe.changementVisibilite(met.getVisibilite());

            String params = classe.affichageParametre(met.getParametres());
            String retour = classe.retourType(met.getRetourType());

            String s = visibilite + staticFlag + met.getNom() + params + retour;
            liste.add(s);
        }
        return liste;
    }

    /**
     * Optimise la disposition des blocs de classe dans le diagramme.
     * 
     * Déclenche l'algorithme d'optimisation de la disposition pour améliorer
     * la lisibilité du diagramme en réduisant les croisements de liaisons.
     */
    public void optimiserDisposition() {
        if (this.vuePrincipale != null) {
            this.vuePrincipale.getPanneauDiagramme().optimiserDisposition();
        }
    }

    /**
     * Active ou désactive l'affichage des classes externes.
     * 
     * @param afficher true pour afficher les classes externes, false pour les
     *                 masquer
     */
    public void setAfficherClassesExternes(boolean afficher) {
        this.afficherClassesExternes = afficher;
        if (this.dernieresClasses != null) {
            afficherDiagrammeAvecDonnees();
        }
    }

    /**
     * Point d'entrée principal de l'application.
     * 
     * Crée une instance du contrôleur qui initialise l'interface graphique.
     * 
     * @param args Arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        new Controleur();
    }
}
