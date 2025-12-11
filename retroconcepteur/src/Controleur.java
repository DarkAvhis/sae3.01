package src;

import modele.AnalyseIHMControleur;
import modele.AttributObjet;
import modele.MethodeObjet;
import modele.ClasseObjet;
import modele.LiaisonObjet; 
import modele.AssociationObjet;
import modele.HeritageObjet;
import modele.InterfaceObjet;

import vue.FenetrePrincipale;
import vue.BlocClasse; 
import vue.LiaisonVue;
import vue.LiaisonVue.TypeLiaison;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class Controleur
{
    // Remplacement d'AnalyseurUML par le contrôleur de la couche Modèle pour une analyse complète
    private AnalyseIHMControleur metierComplet; 
    private FenetrePrincipale vuePrincipale;
    public Controleur()
    {
        this.metierComplet   = new AnalyseIHMControleur(); 
        this.vuePrincipale = new FenetrePrincipale(this);
        this.vuePrincipale.setVisible(true);
    }
    
   

    /**
     * Analyse un dossier projet et met à jour l'affichage du diagramme.
     */
    public void analyserEtAfficherDiagramme(String cheminProjet)
    {
        // 1. Analyse complète via la couche Modèle/Contrôleur
        if (!this.metierComplet.analyserDossier(cheminProjet)) {
            // Gérer l'erreur si l'analyse échoue (ex: dossier non valide)
            return;
        }

        // Récupérer toutes les données du modèle
        List<ClasseObjet> classes = this.metierComplet.getClasses();
        List<AssociationObjet> associations = this.metierComplet.getAssociations();
        List<HeritageObjet> heritages = this.metierComplet.getHeritages();
        List<InterfaceObjet> implementations = this.metierComplet.getImplementations(); 

        // 2. Conversion en objets de Vue (BlocClasse et LiaisonVue)
        List<BlocClasse> blocsVue = new ArrayList<>();
        int x = 50;
        int y = 50;

        for (ClasseObjet c : classes)
        {
            // Les méthodes de conversion ont été déplacées ici dans la dernière étape
            List<String> attrVue = this.convertirAttributs(c.getattributs(), c);
            List<String> methVue = this.convertirMethodes(c.getMethodes(), c);
            
            BlocClasse bloc = new BlocClasse(c.getNom(), x, y, attrVue, methVue);
            
            // DÉTECTION SIMPLE DES INTERFACES (à affiner si nécessaire)
            if (c.getNom().contains("Interface")) {
                bloc.setInterface(true);
            }
            
            blocsVue.add(bloc);
            x += 250; 
            if (x > 1000) { x = 50; y += 200; }
        }

        // Conversion de TOUS les types de liaisons en objets de vue
        List<LiaisonVue> liaisonsVue = new ArrayList<>();
        liaisonsVue.addAll(convertirLiaisons(associations, TypeLiaison.ASSOCIATION_UNIDI)); 
        liaisonsVue.addAll(convertirLiaisons(heritages, TypeLiaison.HERITAGE));             // AJOUT
        liaisonsVue.addAll(convertirLiaisons(implementations, TypeLiaison.IMPLEMENTATION)); // AJOUT

        // 3. Affichage (envoi à la Vue)
        if (this.vuePrincipale != null)
        {
            this.vuePrincipale.getPanneauDiagramme().setBlocsClasses(blocsVue);
            this.vuePrincipale.getPanneauDiagramme().setLiaisonsVue(liaisonsVue);
            this.vuePrincipale.getPanneauDiagramme().repaint();
        }
    }

    public void sauvegarde()
    {

    }

    public void supprimerClasseSelectionnee()
    {
        if (this.vuePrincipale == null) return;

        BlocClasse bloc = this.vuePrincipale.getPanneauDiagramme().getBlocsClasseSelectionnee();

        if (bloc == null)
        {
            javax.swing.JOptionPane.showMessageDialog(null,
                "Aucune classe sélectionnée.",
                "Suppression impossible",
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nomClasse = bloc.getNom();
        this.metierComplet.supprimerClasse(nomClasse);

        // Vue
        var panneau = this.vuePrincipale.getPanneauDiagramme();
        panneau.getBlocsClasses().remove(bloc);

        panneau.getLiaisonsVue().removeIf(l ->
            l.getNomClasseOrig().equals(nomClasse) ||
            l.getNomClasseDest().equals(nomClasse)
        );

        panneau.repaint();
    }


    /**
     * Convertit une liste de LiaisonsObjet (Association, Héritage ou Implémentation) 
     * en une liste de LiaisonVue pour le dessin.
     */
    private List<LiaisonVue> convertirLiaisons(List<? extends LiaisonObjet> liaisons, TypeLiaison type)
    {
        List<LiaisonVue> liaisonsVue = new ArrayList<>();
        for (LiaisonObjet liaison : liaisons)
        {
            // IMPORTANT : L'origine de la flèche (début) est la classe Fille/Concrète
            String nomOrig = liaison.getClasseFille().getNom(); 
            // La destination de la flèche (pointe) est la classe Mère/Interface
            String nomDest = liaison.getClasseMere().getNom();
            
            liaisonsVue.add(new LiaisonVue(nomOrig, nomDest, type)); 
        }
        return liaisonsVue;
    }

    // --- Méthodes de conversion Attributs/Méthodes (conservées ici pour la cohérence) ---

    private List<String> convertirAttributs(List<AttributObjet> attributs, ClasseObjet classe)
    {
        // ... (Logique inchangée pour la vue Attribut) ...
        List<String> liste = new ArrayList<>();
        for (AttributObjet att : attributs)
        {
            String staticFlag = att.estStatique() ? " {static}" : ""; 
            char visibilite = classe.changementVisibilite(att.getVisibilite()); 
            
            String s = visibilite + " " + att.getNom() + " : " + att.getType() + staticFlag; 
            liste.add(s);
        }
        return liste;
    }

    private List<String> convertirMethodes(List<MethodeObjet> methodes, ClasseObjet classe)
    {
        // ... (Logique inchangée pour la vue Méthode) ...
        List<String> liste = new ArrayList<>();
        for (MethodeObjet met : methodes)
        {
            String staticFlag = met.estStatique() ? "{static} " : ""; 
            char visibilite = classe.changementVisibilite(met.getVisibilite());
            
            String params = classe.affichageParametre(met.getParametres());
            String retour = classe.retourType(met.getRetourType());
            
            String s = visibilite + staticFlag + met.getNom() + params + retour;
            liste.add(s);
        }
        return liste;
    }


    public static void main(String[] args)
    {
        new Controleur();
    }
}