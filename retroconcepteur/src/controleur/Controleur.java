package controleur;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import modele.Sauvegarde;
import modele.entites.AssociationObjet;
import modele.entites.AttributObjet;
import modele.entites.ClasseObjet;
import modele.entites.HeritageObjet;
import modele.entites.InterfaceObjet;
import modele.entites.LiaisonObjet;
import modele.entites.MethodeObjet;
import modele.outil.DispositionOptimiseur;
import vue.BlocClasse;
import vue.ExportHelper;
import vue.FenetrePrincipale;
import vue.LiaisonVue;
import vue.LiaisonVue.TypeLiaison;
import vue.PresentationMapper;

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
    private modele.AnalyseMetier metierComplet;
    private FenetrePrincipale vuePrincipale;
    private String cheminProjetActuel;
    /**
     * Constructeur du contrôleur.
     * Initialise le modèle d'analyse et crée la fenêtre principale de
     * l'application.
     */
    public Controleur() {
        this.metierComplet = new modele.AnalyseMetier();
        this.vuePrincipale = new FenetrePrincipale(this);
        this.cheminProjetActuel = null;
    }

    // nouveau (permettre l'exportation)
    public void exporterDiagramme() {
        ExportHelper.exportDiagram(this.vuePrincipale);
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

        this.cheminProjetActuel = cheminProjet;

        reafficherAvecFiltreExternes();
    }

    /**
     * Active/désactive l'affichage des classes externes et réaffiche.
     */
    public void setAfficherClassesExternes(boolean afficher) {
        if (vuePrincipale != null && vuePrincipale.getPanneauDiagramme() != null) {
            vuePrincipale.getPanneauDiagramme().setAfficherClassesExternes(afficher);
        }
        reafficherAvecFiltreExternes();
    }

    /**
     * Reconstruit les blocs affichés selon le filtre d'affichage des classes
     * externes.
     */
    private void reafficherAvecFiltreExternes() 
    {
        List<ClasseObjet> classes = this.metierComplet.getClasses();
        boolean afficherExternes = true;
        boolean afficherAttr = true;
        boolean afficherMeth = true;
        if (vuePrincipale != null && vuePrincipale.getPanneauDiagramme() != null) {
            afficherExternes = vuePrincipale.getPanneauDiagramme().isAfficherClassesExternes();
            afficherAttr = vuePrincipale.getPanneauDiagramme().isAfficherAttributs();
            afficherMeth = vuePrincipale.getPanneauDiagramme().isAfficherMethodes();
        }
        List<BlocClasse> blocs = vue.DiagramPresenter.buildBlocs(classes, afficherExternes, afficherAttr, afficherMeth, 50, 50);
        // set on the view directly
        if (vuePrincipale != null) {
            vuePrincipale.getPanneauDiagramme().setBlocsClasses(blocs);
        }
        majAffichage();
    }

    /**
     * Crée récursivement les blocs graphiques et gère l'affichage des types spécifiques.
     * Cette méthode assure que chaque bloc (classe racine ou interne) est initialisé 
     * avec ses membres et son stéréotype UML.
     */
    // bloc construction delegated to DiagramPresenter

    // helper removed: use DiagramPresenter to rebuild blocs when needed

    public void ajouterMethodes() 
    {
        // Rebuild blocs with methods visible
        reafficherAvecFiltreExternes();
    }

    public void ajouterAttributs() 
    {
        // Rebuild blocs with attributes visible
        reafficherAvecFiltreExternes();
    }

    /**
     * Met à jour l'affichage des blocs et des liaisons.
     */
    private void majAffichage() 
    {
        List<AssociationObjet> associations = this.metierComplet.getAssociations();
        List<HeritageObjet> heritages = this.metierComplet.getHeritages();
        List<InterfaceObjet> implementations = this.metierComplet.getImplementations();

        List<LiaisonVue> liaisonsVue = vue.DiagramPresenter.buildLiaisons(associations, heritages, implementations, this.metierComplet.getClasses());
        if (vuePrincipale != null) {
            vuePrincipale.getPanneauDiagramme().setLiaisonsVue(liaisonsVue);
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
            List<BlocClasse> blocsAvecTailles) 
    {
        return DispositionOptimiseur.calculerPositionsOptimales(classes, liaisons, blocsAvecTailles);
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
        return DispositionOptimiseur.assignerCouches(classes, liaisons);
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
        DispositionOptimiseur.minimiserCroisements(coucheCouranteIndex, nomsCoucheCourante, nomsCoucheFixe, liaisons, blocMap, forward);
    }

    // ... (Reste des méthodes auxiliaires et main inchangés) ...

    /**
     * Sauvegarde les positions actuelles des blocs du diagramme.
     * 
     * @note Cette méthode est actuellement en développement
     */
    public void sauvegarde() {
        // Délégué au module de sauvegarde métier. On n'interroge pas l'état
        // graphique depuis le contrôleur pour respecter MVC.
        if (this.cheminProjetActuel == null || this.cheminProjetActuel.isEmpty()) {
            // Pas de projet courant : sauvegarde dans fichier local par défaut
            Sauvegarde.sauvegarder(".", "diagramme.txt");
        } else {
            Sauvegarde.sauvegarder(this.cheminProjetActuel, this.cheminProjetActuel + "/DiagrammeUML.txt");
        }
    }

    public void sauvegarde(String dossier, String fichier) {
        Sauvegarde.sauvegarder(dossier, fichier);
    }

    /**
     * Supprime la classe actuellement sélectionnée du diagramme.
     * 
     * @note Cette méthode est actuellement en développement
     */
    /**
     * Supprime la classe identifiée par son nom. Le nom doit provenir de la Vue
     * (ex : PanneauDiagramme.getNomClasseSelectionnee()).
     *
     * @param nomClasse nom de la classe métier à supprimer
     * @return nom supprimé ou null
     */
    public String supprimerClasseSelectionnee(String nomClasse) {
        if (nomClasse == null || nomClasse.isEmpty()) return null;

        // Supprimer côté métier
        metierComplet.supprimerClasse(nomClasse);

        // Rebuild the view to reflect model changes
        reafficherAvecFiltreExternes();

        return nomClasse;
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
    private List<LiaisonVue> convertirLiaisons(List<? extends LiaisonObjet> liaisons, TypeLiaison type)
    {
        return PresentationMapper.convertirLiaisons(liaisons, type, metierComplet.getClasses());
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
        return PresentationMapper.convertirAttributs(attributs, classe, metierComplet.getClasses());
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
        return PresentationMapper.convertirMethodes(methodes, classe);
    }

    private BlocClasse creerBlocComplet(ClasseObjet c, int x, int y) 
    {
    return PresentationMapper.creerBlocComplet(c, x, y);
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


    public String getCheminProjetActuel() {
        return this.cheminProjetActuel;
    }

    public void toggleAttributs() 
    {
        if (vuePrincipale != null && vuePrincipale.getPanneauDiagramme() != null) {
            boolean cur = vuePrincipale.getPanneauDiagramme().isAfficherAttributs();
            vuePrincipale.getPanneauDiagramme().setAfficherAttributs(!cur);
        }
        rafraichirMembres();
    }   

    public void toggleMethodes() 
    {
        if (vuePrincipale != null && vuePrincipale.getPanneauDiagramme() != null) {
            boolean cur = vuePrincipale.getPanneauDiagramme().isAfficherMethodes();
            vuePrincipale.getPanneauDiagramme().setAfficherMethodes(!cur);
        }
        rafraichirMembres();
    }

    private void rafraichirMembres() 
    {
        // Rebuild blocs to reflect member visibility changes
        reafficherAvecFiltreExternes();
    }

    public static void main(String[] args) 
    {
        if (args == null || args.length == 0) {
            new Controleur();
            return;
        }

        String mode = args[0].toLowerCase();
        if ("gui".equals(mode) || "graphique".equals(mode)) {
            Controleur ctrl = new Controleur();
            if (args.length > 1) {
                ctrl.analyserEtAfficherDiagramme(args[1]);
            }
            return;
        }

        if ("console".equals(mode) || "cui".equals(mode)) {
            if (args.length < 2) {
                new vue.ConsoleVue().afficherUsage();
                System.out.println("Usage: java -cp class controleur.Controleur console <chemin_du_repertoire>");
                return;
            }

            String chemin = args[1];
                modele.AnalyseMetier metier = new modele.AnalyseMetier();
            boolean ok = metier.analyserDossier(chemin);
            vue.ConsoleVue vue = new vue.ConsoleVue();
            if (!ok) {
                vue.afficherMessage("Erreur : impossible d'analyser le dossier '" + chemin + "'.");
                return;
            }

            vue.afficherClasses(metier.getClasses());
            vue.afficherAssociations(metier.getAssociations());
            vue.afficherHeritages(metier.getHeritages());
            vue.afficherImplementations(metier.getImplementations());
            return;
        }

        new vue.ConsoleVue().afficherUsage();
        System.out.println("Modes supportés: (aucun)=GUI | gui | console <chemin_du_repertoire>");
    }
}