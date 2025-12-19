package controleur;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import modele.Sauvegarde;
import modele.entites.AssociationObjet;
import modele.entites.ClasseObjet;
import modele.entites.HeritageObjet;
import modele.entites.InterfaceObjet;
import vue.BlocClasse;
import vue.ExportHelper;
import vue.FenetrePrincipale;
import vue.LiaisonVue;


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
public class Controleur
{
    private modele.AnalyseMetier  metierComplet;
    private FenetrePrincipale     vuePrincipale;
    private String                cheminProjetActuel;
    /**
     * Constructeur du contrôleur.
     * Initialise le modèle d'analyse et crée la fenêtre principale de
     * l'application.
     */
    public Controleur()
    {
        this.metierComplet = new modele.AnalyseMetier();
        this.vuePrincipale = new FenetrePrincipale(this);
    }

    public void exporterDiagramme() 
    {
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
    public void analyserEtAfficherDiagramme(String cheminProjet) 
    {
        // Si l'analyse échoue, on pourrait vouloir vider la vue
        if (!this.metierComplet.analyserDossier(cheminProjet)) 
        {
            // Envoi de listes vides pour "nettoyer" visuellement
            this.vuePrincipale.getPanneauDiagramme().setBlocsClasses(new ArrayList<>());
            this.vuePrincipale.getPanneauDiagramme().setLiaisonsVue(new ArrayList<>());
            return;
        }

        this.cheminProjetActuel = cheminProjet;
        reafficherAvecFiltreExternes(); // Reconstruit et set les nouveaux blocs
    }

    /**
     * Active/désactive l'affichage des classes externes et réaffiche.
     */
    public void setAfficherClassesExternes(boolean afficher)
    {
        if (vuePrincipale != null && vuePrincipale.getPanneauDiagramme() != null)
        {
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
        boolean afficherAttr     = true;
        boolean afficherMeth     = true;

        if (vuePrincipale != null && vuePrincipale.getPanneauDiagramme() != null)
        {
            afficherExternes = vuePrincipale.getPanneauDiagramme().isAfficherClassesExternes();
            afficherAttr = vuePrincipale.getPanneauDiagramme().isAfficherAttributs();
            afficherMeth = vuePrincipale.getPanneauDiagramme().isAfficherMethodes();
        }
        List<BlocClasse> blocs = vue.DiagramPresenter.buildBlocs(classes, afficherExternes, afficherAttr, afficherMeth, 50, 50);

        if (vuePrincipale != null) 
        {
            vuePrincipale.getPanneauDiagramme().setBlocsClasses(blocs);
        }
        majAffichage();
    }

    /**
     * Met à jour l'affichage des blocs et des liaisons.
     */
    private void majAffichage() 
    {
        List<AssociationObjet> associations    = this.metierComplet.getAssociations   ();
        List<HeritageObjet>    heritages       = this.metierComplet.getHeritages      ();
        List<InterfaceObjet>   implementations = this.metierComplet.getImplementations();

        List<LiaisonVue> liaisonsVue = vue.DiagramPresenter.buildLiaisons(associations,
                heritages, implementations, this.metierComplet.getClasses());

        if (vuePrincipale != null) vuePrincipale.getPanneauDiagramme().setLiaisonsVue(liaisonsVue);
        
    }


    /**
     * Sauvegarde les positions actuelles des blocs du diagramme.
     * 
     * @note Cette méthode est actuellement en développement
     */
    public void sauvegarde()
    {
        if (this.cheminProjetActuel == null || this.cheminProjetActuel.isEmpty())
        {
            // Pas de projet courant : sauvegarde dans fichier local par défaut
            Sauvegarde.sauvegarder(".", "diagramme.txt");
        } else
        {
            Sauvegarde.sauvegarder(this.cheminProjetActuel, this.cheminProjetActuel + "/DiagrammeUML.txt");
        }
    }

    public void sauvegarde(String dossier, String fichier) 
    {
        Sauvegarde.sauvegarder(dossier, fichier);
    }

    /* 
     * Supprime la classe identifiée par son nom. Le nom doit provenir de la Vue
     * (ex : PanneauDiagramme.getNomClasseSelectionnee()).
     *
     * @param nomClasse nom de la classe métier à supprimer
     * @return nom supprimé ou null
     */
    public String supprimerClasseSelectionnee(String nomClasse) 
    {
        if (nomClasse == null || nomClasse.isEmpty()) return null;

        metierComplet.supprimerClasse(nomClasse);

        reafficherAvecFiltreExternes();

        return nomClasse;
    }

    /**
     * Optimise la disposition des blocs de classe dans le diagramme.
     * 
     * Déclenche l'algorithme d'optimisation de la disposition pour améliorer
     * la lisibilité du diagramme en réduisant les croisements de liaisons.
     */
    public void optimiserDisposition() 
    {
        if (this.vuePrincipale != null) 
        {
            this.vuePrincipale.getPanneauDiagramme().optimiserDisposition();
        }
    }


    public String getCheminProjetActuel() { return this.cheminProjetActuel; }

    public void toggleAttributs() 
    {
        if (vuePrincipale != null && vuePrincipale.getPanneauDiagramme() != null) 
        {
            boolean cur = vuePrincipale.getPanneauDiagramme().isAfficherAttributs();
            vuePrincipale.getPanneauDiagramme().setAfficherAttributs(!cur);
            // Appel direct à la reconstruction
            reafficherAvecFiltreExternes();
        }
    }   

    public void toggleMethodes() 
    {
        if (vuePrincipale != null && vuePrincipale.getPanneauDiagramme() != null) 
        {
            boolean cur = vuePrincipale.getPanneauDiagramme().isAfficherMethodes();
            vuePrincipale.getPanneauDiagramme().setAfficherMethodes(!cur);
            // Appel direct à la reconstruction
            reafficherAvecFiltreExternes();
        }
    }

    public void demanderOptimisationDisposition() 
    {
        // 1. Récupérer les données
        List<ClasseObjet> classes = metierComplet.getClasses();
        List<LiaisonVue> liaisons = vuePrincipale.getPanneauDiagramme().getLiaisonsVue();

        // 2. Calculer via le modèle centralisé
        Map<String, Point> nouvellesPositions = modele.outil.DispositionOptimiseur.calculerPositions(classes, liaisons);

        // 3. Mettre à jour les blocs dans la vue
        for (BlocClasse bloc : vuePrincipale.getPanneauDiagramme().getBlocsClasses()) {
            Point p = nouvellesPositions.get(bloc.getNom());
            if (p != null) {
                bloc.setX(p.x);
                bloc.setY(p.y);
            }
        }
        
        // 4. Rafraîchir l'affichage
        vuePrincipale.getPanneauDiagramme().repaint();
    }

    public static void main(String[] args) 
    {
        if (args == null || args.length == 0) 
        {
            new Controleur();
            return;
        }

        String mode = args[0].toLowerCase();
        if ("gui".equals(mode) || "graphique".equals(mode)) 
        {
            Controleur ctrl = new Controleur();
            if (args.length > 1) ctrl.analyserEtAfficherDiagramme(args[1]);
            return;
        }

        if ("console".equals(mode) || "cui".equals(mode)) 
        {
            if (args.length < 2) 
            {
                new vue.ConsoleVue().afficherUsage();
                System.out.println("Usage: java -cp class controleur.Controleur console <chemin_du_repertoire>");
                return;
            }

            String chemin = args[1];
                modele.AnalyseMetier metier = new modele.AnalyseMetier();
            boolean ok = metier.analyserDossier(chemin);
            vue.ConsoleVue vue = new vue.ConsoleVue();
            if (!ok) 
            {
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