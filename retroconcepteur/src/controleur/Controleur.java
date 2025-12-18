package controleur;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import modele.Sauvegarde;
import modele.entites.AssociationObjet;
import modele.entites.AttributObjet;
import modele.entites.ClasseObjet;
import modele.entites.HeritageObjet;
import modele.entites.InterfaceObjet;
import modele.entites.LiaisonObjet;
import modele.entites.MethodeObjet;
import vue.BlocClasse;
import vue.ExportIHM;
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
        if (this.vuePrincipale == null)
            return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exporter le diagramme");
        chooser.setSelectedFile(new File("diagramme.png"));
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("Image PNG", "png"));

        int choix = chooser.showSaveDialog(this.vuePrincipale);
        if (choix != JFileChooser.APPROVE_OPTION)
            return;

        File fichier = chooser.getSelectedFile();

        if (!fichier.getName().toLowerCase().endsWith(".png")) {
            fichier = new File(fichier.getAbsolutePath() + ".png");
        }

        try {
            ExportIHM.exportComponent(
                    this.vuePrincipale.getPanneauDiagramme(), // ✅ BON composant
                    fichier // ✅ BON type
            );
            JOptionPane.showMessageDialog(
                    this.vuePrincipale,
                    "Le diagramme a été exporté avec succès.\n\nFichier :\n"
                            + fichier.getAbsolutePath(),
                    "Export réussi",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this.vuePrincipale,
                    "Erreur lors de l'export du diagramme :\n" + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Exporte le diagramme (positions + liaisons) au format JSON lisible.
     */
    public void exporterDiagrammeJSON() {
        if (this.vuePrincipale == null)
            return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exporter le diagramme (JSON)");
        chooser.setSelectedFile(new File("diagramme.json"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Fichier JSON", "json"));

        int choix = chooser.showSaveDialog(this.vuePrincipale);
        if (choix != JFileChooser.APPROVE_OPTION)
            return;

        File fichier = chooser.getSelectedFile();

        if (!fichier.getName().toLowerCase().endsWith(".json")) {
            fichier = new File(fichier.getAbsolutePath() + ".json");
        }

        try {
            List<vue.BlocClasse> blocs = this.vuePrincipale.getPanneauDiagramme().getBlocsClasses();
            List<vue.LiaisonVue> liaisons = this.vuePrincipale.getPanneauDiagramme().getLiaisonsVue();

            // Utiliser la version enrichie de l'export qui inclut attributs/méthodes
            
            JOptionPane.showMessageDialog(this.vuePrincipale,
                    "Diagramme exporté en JSON :\n" + fichier.getAbsolutePath(),
                    "Export JSON réussi", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this.vuePrincipale,
                    "Erreur lors de l'export JSON :\n" + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
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
    List<LiaisonVue> liaisonsVue = new ArrayList<>();
    List<ClasseObjet> classes = metierComplet.getClasses();

    // 1️⃣ Liaisons classiques
    for (LiaisonObjet liaison : liaisons) {
        if (liaison instanceof InterfaceObjet) {
            InterfaceObjet interfaceLiaison = (InterfaceObjet) liaison;
            if (interfaceLiaison.getClasseFille() == null) continue;
            String nomClasseConcrete = interfaceLiaison.getClasseFille().getNom();
            for (ClasseObjet interfaceClass : interfaceLiaison.getLstInterfaces()) {
                if (interfaceClass != null) {
                    liaisonsVue.add(new LiaisonVue(nomClasseConcrete, interfaceClass.getNom(), type, null, null));
                }
            }
        } else {
            if (liaison.getClasseFille() == null || liaison.getClasseMere() == null) continue;

            String nomOrig = liaison.getClasseFille().getNom();
            String nomDest = liaison.getClasseMere().getNom();
            String multOrig = null;
            String multDest = null;

            if (liaison instanceof AssociationObjet) {
                AssociationObjet asso = (AssociationObjet) liaison;
                multOrig = asso.getMultOrig() != null ? asso.getMultOrig().toString() : "1..1";
                multDest = asso.getMultDest() != null ? asso.getMultDest().toString() : "1..1";

                List<String> props = new ArrayList<>();
                if (asso.isFrozen()) props.add("frozen");
                if (asso.isAddOnly()) props.add("addOnly");
                if (asso.isRequete()) props.add("requête");
                String propsStr = props.isEmpty() ? "" : "{" + String.join(", ", props) + "}";

                liaisonsVue.add(new LiaisonVue(
                        nomOrig, nomDest, type, multOrig, multDest,
                        asso.getRoleOrig(), asso.getRoleDest(),
                        asso.getRoleOrigOffsetAlong(), asso.getRoleOrigOffsetPerp(),
                        asso.getRoleDestOffsetAlong(), asso.getRoleDestOffsetPerp(),
                        propsStr, ""
                ));
            } else {
                liaisonsVue.add(new LiaisonVue(nomOrig, nomDest, type, multOrig, multDest));
            }
        }
    }
    // 2️⃣ Liaisons issues des attributs (uniquement si aucune liaison n'existe déjà)
        for (ClasseObjet c : classes) 
        {
            for (AttributObjet att : c.getattributs()) {
                String typeAtt = att.getType();
                String typeSimple = typeAtt;
                if (typeAtt.endsWith("[]")) typeSimple = typeAtt.replace("[]", "");
                else if (typeAtt.contains("<") && typeAtt.contains(">"))
                    typeSimple = typeAtt.substring(typeAtt.indexOf("<") + 1, typeAtt.indexOf(">"));

                for (ClasseObjet cible : classes) {
                    if (cible.getNom().equals(typeSimple) && !cible.getNom().equals(c.getNom())) {
                        boolean existe = false;
                        for (LiaisonVue lv : liaisonsVue) {
                            if ((lv.getNomClasseOrig().equals(c.getNom()) && lv.getNomClasseDest().equals(cible.getNom())) ||
                                (lv.getNomClasseOrig().equals(cible.getNom()) && lv.getNomClasseDest().equals(c.getNom()))) {
                                existe = true;
                                break;
                            }
                        }
                        if (existe) continue;

                        liaisonsVue.add(new LiaisonVue(
                                c.getNom(),
                                cible.getNom(),
                                LiaisonVue.TypeLiaison.ASSOCIATION_UNIDI,
                                null,
                                null,
                                att.getNom(),
                                null,
                                0,0,0,0,
                                "", ""
                        ));
                    }
                }
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
        // Récupérer la liste des noms de classes du projet (dans le même package)
        List<String> nomsClassesProjet = new ArrayList<>();
        for (ClasseObjet c : this.metierComplet.getClasses()) {
            nomsClassesProjet.add(c.getNom());
        }

        for (AttributObjet att : attributs) {
            String type = att.getType();
            String typeSimple = type;
            // Gérer les collections/génériques/array
            if (type.contains("<") && type.contains(">")) {
                int idx1 = type.indexOf('<');
                int idx2 = type.indexOf('>', idx1 + 1);
                if (idx1 != -1 && idx2 != -1 && idx2 > idx1) {
                    typeSimple = type.substring(idx1 + 1, idx2).trim();
                }
            } else if (type.endsWith("[]")) {
                typeSimple = type.replace("[]", "").trim();
            }

            // Si le type correspond à une classe du projet, on ne l'affiche pas
            if (nomsClassesProjet.contains(typeSimple) && !typeSimple.equals(classe.getNom())) {
                continue;
            }

            String staticFlag = att.estStatique() ? " {static}" : "";
            String finalFlag = att.estFinale() ? " {final}" : "";
            char visibilite = classe.changementVisibilite(att.getVisibilite());

            String mult = "";
            if (att.getMultiplicite() != null) {
                mult = " [" + att.getMultiplicite().toString() + "]";
            }

            List<String> props = new ArrayList<>();
            if (att.estFrozen())
                props.add("frozen");
            if (att.estAddOnly())
                props.add("addOnly");
            if (att.estRequete())
                props.add("requête");
            String propsStr = props.isEmpty() ? "" : " {" + String.join(", ", props) + "}";

            String s = visibilite + " " + att.getNom() + " : " + att.getType() + mult + staticFlag + finalFlag
                    + propsStr;
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

        // Générer des getters par défaut pour les attributs qui n'ont pas de getter
        // Détecter les noms de méthodes existants pour éviter les doublons
        java.util.Set<String> nomsMethodesExistants = new java.util.HashSet<String>();
        for (MethodeObjet met : methodes) {
            if (met.getNom() != null) nomsMethodesExistants.add(met.getNom());
        }

        for (AttributObjet att : classe.getattributs()) {
            String nomAttr = att.getNom();
            if (nomAttr == null || nomAttr.isEmpty()) continue;

            // Construire le nom du getter
            String nomGetter;
            String type = att.getType() != null ? att.getType().trim() : "";
            if (type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("Boolean")) {
                // isX
                nomGetter =  nomAttr;
            } else {
                nomGetter = nomAttr;
            }

            if (nomsMethodesExistants.contains(nomGetter)) continue;

            // Construire la signature affichée (visibilité publique par défaut)
            char vis = classe.changementVisibilite("public");
            String retour = classe.retourType(type);
            String s = vis + " " + nomGetter + "()" + retour;
            liste.add(s);
        }
        return liste;
    }

    private BlocClasse creerBlocComplet(ClasseObjet c, int x, int y) 
    {
    // 1. Création du bloc principal
        BlocClasse bloc = new BlocClasse(c.getNom(), x, y , new ArrayList<>(), new ArrayList<>());

        // 2. Configuration des propriétés (interface, externe...)
        if (c.getNom().contains("Interface") || (c.getSpecifique() != null && c.getSpecifique().equals("interface"))) {
            bloc.setInterface(true);
            bloc.setTypeSpecifique("interface");
        } else if (c.getSpecifique() != null && (c.getSpecifique().equals("record") || c.getSpecifique().equals("abstract class"))) {
            bloc.setTypeSpecifique(c.getSpecifique());
        }
        if (c.getSpecifique() != null && c.getSpecifique().equals("externe"))
            bloc.setExterne(true);

        

        return bloc;
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