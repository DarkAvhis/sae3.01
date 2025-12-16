package controleur;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import modele.AnalyseurUML;
import modele.entites.AssociationObjet;
import modele.entites.AttributObjet;
import modele.entites.ClasseObjet;
import modele.entites.HeritageObjet;
import modele.entites.InterfaceObjet;
import modele.entites.LiaisonObjet;
import modele.entites.MethodeObjet;
import vue.ConsoleVue;

/**
 * Contrôleur principal pour l'IHM (Interface Homme-Machine).
 */
public class AnalyseIHMControleur 
{
    /*-------------------------------------- */
    /* Attributs */
    /*-------------------------------------- */
    private List<ClasseObjet> classes;
    private HashMap<String, ClasseObjet> mapClasses;
    private List<AssociationObjet> associations;
    private List<HeritageObjet> heritages;
    private List<InterfaceObjet> implementations;
    private AnalyseurUML analyseur;

    /*-------------------------------------- */
    /* Constructeur */
    /*-------------------------------------- */

    // Constructeur par défaut
    public AnalyseIHMControleur() 
    {
        this.classes = new ArrayList<ClasseObjet>();
        this.associations = new ArrayList<AssociationObjet>();
        this.heritages = new ArrayList<HeritageObjet>();
        this.implementations = new ArrayList<InterfaceObjet>();

        this.mapClasses = new HashMap<String, ClasseObjet>();
        this.analyseur = new AnalyseurUML();
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

        vue.afficherClasses(this.getClasses());
        vue.afficherAssociations(this.getAssociations());
        vue.afficherHeritages(this.getHeritages());
        vue.afficherImplementations(this.getImplementations());
    }

    /*-------------------------------------- */
    /* Les Accesseurs */
    /*-------------------------------------- */
    public List<ClasseObjet> getClasses() 
    {
        return this.classes;
    }

    public List<AssociationObjet> getAssociations() 
    {
        return this.associations;
    }

    public List<HeritageObjet> getHeritages() 
    {
        return this.heritages;
    }

    public List<InterfaceObjet> getImplementations() 
    {
        return this.implementations;
    }

    /*-------------------------------------- */
    /* Methode autre */
    /*-------------------------------------- */

    /**
     * Analyse complète du dossier.
     */
    public boolean analyserDossier(String cheminDossier) 
    {
        File cible = new File(cheminDossier);
        if (!cible.isDirectory()) 
        {
            return false;
        }

        this.classes.clear();
        this.mapClasses.clear();
        this.associations.clear();
        this.heritages.clear();
        this.implementations.clear();
        this.analyseur.resetRelations();

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

        this.ajouterClassesExternes();

        this.associations.addAll(analyseur.detecterAssociations(this.classes, this.mapClasses));
        this.heritages.addAll(analyseur.resoudreHeritage(this.mapClasses));
        this.implementations.addAll(analyseur.resoudreImplementation(this.mapClasses));

        List<LiaisonObjet> toutes = new ArrayList<>();

        toutes.addAll(this.associations);
        toutes.addAll(this.heritages);
        toutes.addAll(this.implementations);

        analyseur.renumeroterLiaisonsFinales(toutes);

        return true;
    }

    /**
     * Suppression d'une classe et des relations associées.
     */
    public void supprimerClasse(String nomClasse) 
    {
        if (nomClasse == null || nomClasse.isEmpty()) 
        {
            return;
        }

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
                a.getClasseMere().getNom().equals(nomClasse));

        this.heritages.removeIf(h -> h.getClasseFille().getNom().equals(nomClasse) ||
                h.getClasseMere().getNom().equals(nomClasse));
    }

    /*-------------------------------------- */
    /* Main */
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

    /**
     * Ajoute des classes "placeholder" pour les types externes (JDK ou libs) afin
     * que
     * le diagramme affiche aussi les ArrayList, JMenuBar, etc.
     */
    private void ajouterClassesExternes() 
    {
        HashSet<String> dejaAjoutees = new HashSet<String>(this.mapClasses.keySet());

        // Héritages détectés
        for (String enfant : this.analyseur.getIntentionsHeritage().keySet()) 
        {
            String parent = this.analyseur.getIntentionsHeritage().get(enfant);
            if (parent != null && !parent.equals("Object")) 
            {
                this.ajouterClasseExterneSiNecessaire(parent, dejaAjoutees);
            }
        }

        // Interfaces implémentées
        for (String classe : this.analyseur.getInterfaces().keySet()) 
        {
            ArrayList<String> interfaces = this.analyseur.getInterfaces().get(classe);
            for (String nomInterface : interfaces) 
            {
                this.ajouterClasseExterneSiNecessaire(nomInterface, dejaAjoutees);
            }
        }

        // Types des attributs (créer une copie pour éviter
        // ConcurrentModificationException)
        List<ClasseObjet> classesSnapshot = new ArrayList<>(this.classes);
        for (ClasseObjet classeCourante : classesSnapshot) 
        {
            if (classeCourante.getattributs() == null) 
            {
                continue;
            }

            for (AttributObjet attribut : classeCourante.getattributs()) 
            {
                this.ajouterClassesExternesDepuisType(attribut.getType(), dejaAjoutees);
            }
        }
    }

    private void ajouterClassesExternesDepuisType(String typeBrut, HashSet<String> dejaAjoutees) 
    {
        if (typeBrut == null) 
        {
            return;
        }

        String typeNettoye = typeBrut.trim();
        if (typeNettoye.isEmpty()) 
        {
            return;
        }

        // Type externe direct (ArrayList<...> => ArrayList)
        String typeExterne = typeNettoye;
        int idxChevron = typeNettoye.indexOf('<');
        if (idxChevron != -1) 
        {
            typeExterne = typeNettoye.substring(0, idxChevron).trim();
        }
        typeExterne = typeExterne.replace("[]", "").trim();
        this.ajouterClasseExterneSiNecessaire(typeExterne, dejaAjoutees);

        // Types internes aux génériques (ArrayList<Point> => Point)
        if (idxChevron != -1) 
        {
            int idxFin = typeNettoye.indexOf('>', idxChevron);
            if (idxFin != -1) 
            {
                String internes = typeNettoye.substring(idxChevron + 1, idxFin);
                String[] candidats = internes.split(",");
                for (String cand : candidats) 
                {
                    String nettoye = cand.replace("? extends", "").replace("? super", "").trim();
                    nettoye = nettoye.replace("[]", "").trim();
                    this.ajouterClasseExterneSiNecessaire(nettoye, dejaAjoutees);
                }
            }
        }
    }

    private void ajouterClasseExterneSiNecessaire(String nomType, HashSet<String> dejaAjoutees) 
    {
        if (nomType == null || nomType.isEmpty()) 
        {
            return;
        }

        if (this.estTypePrimitif(nomType) || dejaAjoutees.contains(nomType)) 
        {
            return;
        }

        ClasseObjet placeholder = new ClasseObjet(new ArrayList<AttributObjet>(), new ArrayList<MethodeObjet>(),
                nomType, "externe");
        this.classes.add(placeholder);
        this.mapClasses.put(nomType, placeholder);
        dejaAjoutees.add(nomType);
    }

    private boolean estTypePrimitif(String nomType) 
    {
        return nomType.equals("int") || nomType.equals("double") || nomType.equals("float") ||
                nomType.equals("boolean") || nomType.equals("char") || nomType.equals("byte") ||
                nomType.equals("short") || nomType.equals("long") || nomType.equals("void");
    }
}