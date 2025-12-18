package controleur;

import java.awt.Point;
import java.util.ArrayList;
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
    private AnalyseIHMControleur metierComplet;
    private FenetrePrincipale vuePrincipale;
    private List<BlocClasse> blocsVue = new ArrayList<>();
    private String cheminProjetActuel;
    private boolean afficherClassesExternes = true;
    private boolean afficherAttributs = true;
    private boolean afficherMethodes = true;

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
        this.cheminProjetActuel = null;
    }

    // nouveau (permettre l'exportation)
    public void exporterDiagramme() {
        ExportHelper.exportDiagram(this.vuePrincipale);
    }

    /**
     * Exporte le diagramme (positions + liaisons) au format JSON lisible.
     */
    public void exporterDiagrammeJSON() {
        ExportHelper.exportDiagramJSON(this.vuePrincipale);
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
        this.afficherClassesExternes = afficher;
        reafficherAvecFiltreExternes();
    }

    /**
     * Reconstruit les blocs affichés selon le filtre d'affichage des classes
     * externes.
     */
    private void reafficherAvecFiltreExternes() 
    {
        List<ClasseObjet> classes = this.metierComplet.getClasses();
        blocsVue.clear();

        int x = 50, y = 50;
        for (ClasseObjet c : classes) {
            // Créer les blocs pour la classe et TOUTES ses classes internes à plat
            creerBlocsEtLiaisonsRecursif(c, x, y);
            
            x += 300; // Plus d'espace car les classes sont côte à côte
            if (x > 1200) { x = 50; y += 300; }
        }
        majAffichage();
    }

    /**
     * Crée récursivement les blocs graphiques et gère l'affichage des types spécifiques.
     * Cette méthode assure que chaque bloc (classe racine ou interne) est initialisé 
     * avec ses membres et son stéréotype UML.
     */
    private void creerBlocsEtLiaisonsRecursif(ClasseObjet c, int x, int y) 
    {
        // 1. Gestion du filtre des classes externes
        boolean estExterne = (c.getSpecifique() != null && c.getSpecifique().equals("externe"));
        if (!afficherClassesExternes && estExterne) {
            return;
        }

        // 2. Récupération immédiate des données formatées depuis le modèle
        // On utilise les préférences d'affichage (filtres attributs/méthodes) définies dans le contrôleur
        List<String> attrVue = afficherAttributs ? convertirAttributs(c.getattributs(), c) : new ArrayList<>();
        List<String> methVue = afficherMethodes ? convertirMethodes(c.getMethodes(), c) : new ArrayList<>();

        // 3. Instanciation du bloc graphique
        BlocClasse bloc = new BlocClasse(c.getNom(), x, y, attrVue, methVue);
        
        // 4. Rétablissement de l'affichage du type spécifique (stéréotype)
        // On récupère la valeur du modèle pour l'injecter dans la vue
        if (c.getSpecifique() != null && !c.getSpecifique().isEmpty()) {
            bloc.setTypeSpecifique(c.getSpecifique());
            
            // Cas particulier pour forcer le flag interface si le stéréotype est "interface"
            if (c.getSpecifique().equals("interface")) {
                bloc.setInterface(true);
            }
        } else if (c.getNom().contains("Interface")) {
            // Sécurité si le parsing n'a pas détecté le mot-clé mais que le nom est explicite
            bloc.setInterface(true);
            bloc.setTypeSpecifique("interface");
        }
        
        // 5. Marquage du bloc si externe (pour le rendu gris)
        if (estExterne) {
            bloc.setExterne(true);
        }
        
        // Ajout à la liste des blocs gérés par la vue
        blocsVue.add(bloc);

        // 6. Gestion récursive des classes internes
        // On applique un décalage (offset) pour visualiser l'imbrication sur le diagramme
        int offsetX = 40;
        int offsetY = 180;
        for (ClasseObjet inner : c.getClassesInternes()) {
            creerBlocsEtLiaisonsRecursif(inner, x + offsetX, y + offsetY);
        }
    }

    private BlocClasse trouverBlocParNom(String nom) 
    {
        for (BlocClasse b : blocsVue) {
            if (b.getNom().equals(nom)) return b;
        }
        return null;
    }

    public void ajouterMethodes() 
    {
        List<ClasseObjet> classes = this.metierComplet.getClasses();
        for (ClasseObjet c : classes) {
            List<String> methVue = convertirMethodes(c.getMethodes(), c);
            BlocClasse bloc = trouverBlocParNom(c.getNom());
            if (bloc != null) bloc.setMethodes(methVue);
        }
        majAffichage();
    }

    public void ajouterAttributs() 
    {
        List<ClasseObjet> classes = this.metierComplet.getClasses();
        for (ClasseObjet c : classes) {
            List<String> attrVue = convertirAttributs(c.getattributs(), c);
            BlocClasse bloc = trouverBlocParNom(c.getNom());
            if (bloc != null) bloc.setAttributs(attrVue);
        }
        majAffichage();
    }

    /**
     * Met à jour l'affichage des blocs et des liaisons.
     */
    private void majAffichage() 
    {
        List<AssociationObjet> associations = this.metierComplet.getAssociations();
        List<HeritageObjet> heritages = this.metierComplet.getHeritages();
        List<InterfaceObjet> implementations = this.metierComplet.getImplementations();

        List<LiaisonVue> liaisonsVue = new ArrayList<>();
        liaisonsVue.addAll(convertirLiaisons(associations, TypeLiaison.ASSOCIATION_UNIDI));
        liaisonsVue.addAll(convertirLiaisons(heritages, TypeLiaison.HERITAGE));
        liaisonsVue.addAll(convertirLiaisons(implementations, TypeLiaison.IMPLEMENTATION));

        // AJOUT : Récupérer les liaisons de classes internes créées lors du parsing
        for (ClasseObjet c : this.metierComplet.getClasses()) {
            for (ClasseObjet inner : c.getClassesInternes()) {
                liaisonsVue.add(new LiaisonVue(inner.getNom(), c.getNom(), TypeLiaison.NESTED, null, null));
            }
        }

        if (vuePrincipale != null) {
            vuePrincipale.getPanneauDiagramme().setBlocsClasses(blocsVue);
            vuePrincipale.getPanneauDiagramme().setLiaisonsVue(liaisonsVue);
            vuePrincipale.getPanneauDiagramme().repaint();
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
        if (this.vuePrincipale == null)
            return;
        List<BlocClasse> blocs = this.vuePrincipale.getPanneauDiagramme().getBlocsClasses();
        System.out.println("Sauvegarde des positions de " + blocs.size() + " blocs.");
    }

    public void sauvegarde(String dossier, String fichier) {
        Sauvegarde.sauvegarder(dossier, fichier);
    }

    /**
     * Supprime la classe actuellement sélectionnée du diagramme.
     * 
     * @note Cette méthode est actuellement en développement
     */
    public String supprimerClasseSelectionnee() {
        if (vuePrincipale == null)
            return null;

        BlocClasse bloc = vuePrincipale.getPanneauDiagramme().getBlocsClasseSelectionnee();
        if (bloc == null)
            return null;

        String nomClasse = bloc.getNom();

        // Supprimer côté métier
        metierComplet.supprimerClasse(nomClasse);

        // Supprimer le bloc dans la vue
        for (int i = 0; i < blocsVue.size(); i++) {
            if (blocsVue.get(i).getNom().equals(nomClasse)) {
                blocsVue.remove(i);
                break;
            }
        }

        vuePrincipale.getPanneauDiagramme().repaint();

        return nomClasse; // renvoie le nom de la classe supprimée
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

    private void afficherDiagrammeAvecDonnees() 
    {
        // a faire cet aprem j'ai faim la
    }

    public String getCheminProjetActuel() {
        return this.cheminProjetActuel;
    }

    public void toggleAttributs() 
    {
    this.afficherAttributs = !this.afficherAttributs;
    rafraichirMembres();
    }   

    public void toggleMethodes() 
    {
        this.afficherMethodes = !this.afficherMethodes;
        rafraichirMembres();
    }

    private void rafraichirMembres() 
    {
        for (BlocClasse bloc : blocsVue) 
        {
            // Recherche manuelle dans la liste existante
            ClasseObjet classeAssociee = null;
            for (ClasseObjet c : metierComplet.getClasses()) 
            {
                if (c.getNom().equals(bloc.getNom())) 
                {
                    classeAssociee = c;
                    break;
                }
            }

            if (classeAssociee != null) 
            {
                bloc.setAttributs(afficherAttributs ? convertirAttributs(classeAssociee.getattributs(), classeAssociee) : new ArrayList<>());
                bloc.setMethodes(afficherMethodes ? convertirMethodes(classeAssociee.getMethodes(), classeAssociee) : new ArrayList<>());
            }
        }
        vuePrincipale.getPanneauDiagramme().repaint();
    }

    /**
     * Point d'entrée principal de l'application.
     * 
     * Crée une instance du contrôleur qui initialise l'interface graphique.
     * 
     * @param args Arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) 
    {
        new Controleur();
    }
}