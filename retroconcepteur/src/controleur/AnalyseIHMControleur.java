package controleur;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import modele.entites.ClasseObjet;
import modele.entites.AssociationObjet;
import modele.entites.HeritageObjet;
import modele.entites.InterfaceObjet;
import modele.entites.LiaisonObjet;
import modele.AnalyseurUML;
import vue.ConsoleVue;

/**
 * Contrôleur principal pour l'IHM (Interface Homme-Machine).
 */
public class AnalyseIHMControleur 
{
    /*-------------------------------------- */
	/* Attributs                             */
	/*-------------------------------------- */
    private List<ClasseObjet>               classes;
    private HashMap<String, ClasseObjet>    mapClasses;
    private List<AssociationObjet>          associations;
    private List<HeritageObjet>             heritages;
    private List<InterfaceObjet>            implementations;
    private AnalyseurUML                    analyseur;

    /*-------------------------------------- */
	/* Constructeur                          */
	/*-------------------------------------- */

    // Constructeur par défaut
    public AnalyseIHMControleur() 
    {
        this.classes         = new ArrayList<ClasseObjet>     ();
        this.associations    = new ArrayList<AssociationObjet>();
        this.heritages       = new ArrayList<HeritageObjet>   ();
        this.implementations = new ArrayList<InterfaceObjet>  ();

        this.mapClasses      = new HashMap<String, ClasseObjet>();
        this.analyseur       = new AnalyseurUML();
    }

    // Constructeur intégral qui fait l'analyse et affiche
    public AnalyseIHMControleur(String cheminDossier) 
    {
        this(); // initialise les listes

        ConsoleVue vue = new ConsoleVue();

        boolean ok = this.analyserDossier(cheminDossier);
        if (!ok) 
        {
            vue.afficherMessage("Erreur : l'analyse a échoué pour le chemin '" + cheminDossier + "'.");
            return;
        }

        vue.afficherClasses        (this.getClasses        ());
        vue.afficherAssociations   (this.getAssociations   ());
        vue.afficherHeritages      (this.getHeritages      ());
        vue.afficherImplementations(this.getImplementations());
    }

    /*-------------------------------------- */
	/* Les Accesseurs                        */
	/*-------------------------------------- */
    public List<ClasseObjet>      getClasses        () { return this.classes         ;  }
    public List<AssociationObjet> getAssociations   () { return this.associations    ;  }
    public List<HeritageObjet>    getHeritages      () { return this.heritages       ;  }
    public List<InterfaceObjet>   getImplementations() { return this.implementations ;  }

    /*-------------------------------------- */
	/* Methode autre                         */
	/*-------------------------------------- */

    /**
     * Analyse complète du dossier.
    */
    public boolean analyserDossier(String cheminDossier) 
    {
        File cible = new File(cheminDossier);
        if (!cible.isDirectory()) {return false;}

        this.classes        .clear         ();     
        this.mapClasses     .clear         ();  
        this.associations   .clear         ();
        this.heritages      .clear         ();
        this.implementations.clear         ();
        this.analyseur      .resetRelations();

        List<File> fichiersJava = analyseur.ClassesDuDossier(cheminDossier);

        for (File f : fichiersJava) 
        {
            ClasseObjet c = analyseur.analyserFichierUnique(f.getAbsolutePath());
            if (c != null) 
            {
                this.classes.add(c);
                this.mapClasses.put(c.getNom(), c);
            }
        }

        this.associations   .addAll(analyseur.detecterAssociations  (this.classes, this.mapClasses));
        this.heritages      .addAll(analyseur.resoudreHeritage      (this.mapClasses              ));
        this.implementations.addAll(analyseur.resoudreImplementation(this.mapClasses              ));

        List<LiaisonObjet> toutes = new ArrayList<>();

        toutes.addAll(this.associations)   ;
        toutes.addAll(this.heritages)      ;
        toutes.addAll(this.implementations);

        analyseur.renumeroterLiaisonsFinales(toutes);

        return true;
    }

    /**
     * Suppression d'une classe et des relations associées.
     */
    public void supprimerClasse(String nomClasse) 
    {
        if (nomClasse == null || nomClasse.isEmpty()) {return;}

        ClasseObjet aSupprimer = null;

        for (ClasseObjet c : this.classes) 
        {
            if (nomClasse.equals(c.getNom())) 
            {
                aSupprimer = c;
                break;
            }
        }

        if (aSupprimer != null)
        {
            this.classes.remove(aSupprimer);
        }

        this.mapClasses.remove(nomClasse);

        this.associations.removeIf(a -> a.getClasseFille().getNom().equals(nomClasse) ||
                                        a.getClasseMere ().getNom().equals(nomClasse) );

        this.heritages.removeIf(h -> h.getClasseFille().getNom().equals(nomClasse) ||
                                     h.getClasseMere ().getNom().equals(nomClasse) );
    }

    /*-------------------------------------- */
	/* Main                                  */
	/*-------------------------------------- */
    public static void main(String[] args) 
    {
        ConsoleVue vue = new ConsoleVue();

        if (args.length == 0) 
        {
            vue.afficherUsage();
            return;
        }

        new AnalyseIHMControleur(args[0]);
    }
}