package controleur;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import modele.AnalyseurUML;
import modele.entites.AssociationObjet;
import modele.entites.ClasseObjet;
import modele.entites.HeritageObjet;
import modele.entites.InterfaceObjet;
import modele.entites.LiaisonObjet;
import vue.ConsoleVue;

/**
 * Contrôleur principal pour l'IHM (Interface Homme-Machine).
 */
public class AnalyseIHMControleur {
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
    public AnalyseIHMControleur() {
        this.classes = new ArrayList<ClasseObjet>();
        this.associations = new ArrayList<AssociationObjet>();
        this.heritages = new ArrayList<HeritageObjet>();
        this.implementations = new ArrayList<InterfaceObjet>();

        this.mapClasses = new HashMap<String, ClasseObjet>();
        this.analyseur = new AnalyseurUML();
    }

    // Constructeur intégral qui fait l'analyse et affiche
    public AnalyseIHMControleur(String cheminDossier) {
        this(); // initialise les listes

        ConsoleVue vue = new ConsoleVue();

        boolean ok = this.analyserDossier(cheminDossier);
        if (!ok) {
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
    public List<ClasseObjet> getClasses() {
        return this.classes;
    }

    public List<AssociationObjet> getAssociations() {
        return this.associations;
    }

    public List<HeritageObjet> getHeritages() {
        return this.heritages;
    }

    public List<InterfaceObjet> getImplementations() {
        return this.implementations;
    }

    /*-------------------------------------- */
    /* Methode autre */
    /*-------------------------------------- */

    /**
     * Analyse complète du dossier.
     */
    public boolean analyserDossier(String cheminDossier) {
        File cible = new File(cheminDossier);
        if (!cible.isDirectory()) {
            return false;
        }

        this.classes.clear();
        this.mapClasses.clear();
        this.associations.clear();
        this.heritages.clear();
        this.implementations.clear();
        this.analyseur.resetRelations();

        List<File> fichiersJava = analyseur.ClassesDuDossier(cheminDossier);

        for (File f : fichiersJava) {
            ClasseObjet c = analyseur.analyserFichierUnique(f.getAbsolutePath());
            if (c != null) {
                this.classes.add(c);
                this.mapClasses.put(c.getNom(), c);
            }
        }

        // Ajouter des classes "externes" manquantes (JDK/Bibliothèques) référencées
        // par héritage, interfaces ou attributs génériques/tableaux.
        ajouterClassesExternes();

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
     * Ajoute des placeholders pour les classes externes au projet (JDK, libs)
     * afin qu'elles soient affichées dans le diagramme.
     *
     * Les noms proviennent de:
     * - parents (extends) détectés par l'analyseur
     * - interfaces implémentées
     * - types d'attributs (T dans List<T>, T[])
     */
    private void ajouterClassesExternes() {
        HashSet<String> manquantes = new HashSet<>();

        // 1) Parents (extends)
        for (String parent : this.analyseur.getIntentionsHeritage().values()) {
            if (parent == null)
                continue;
            if ("Object".equals(parent))
                continue;
            if (!this.mapClasses.containsKey(parent))
                manquantes.add(parent);
        }

        // 2) Interfaces implémentées
        for (java.util.ArrayList<String> lst : this.analyseur.getInterfaces().values()) {
            for (String iface : lst) {
                if (iface == null || iface.isEmpty())
                    continue;
                if (!this.mapClasses.containsKey(iface))
                    manquantes.add(iface);
            }
        }

        // 3) Types d'attributs (List<T> => T, T[] => T)
        for (ClasseObjet c : this.classes) {
            if (c.getattributs() == null)
                continue;
            for (modele.entites.AttributObjet att : c.getattributs()) {
                String typeBrut = att.getType();
                if (typeBrut == null || typeBrut.isEmpty())
                    continue;
                String principal = extraireTypePrincipal(typeBrut);
                if (principal == null || principal.isEmpty())
                    continue;
                if (principal.equals(c.getNom()))
                    continue;
                // Heuristique simple: commence par majuscule => prob. classe
                if (!this.mapClasses.containsKey(principal) && Character.isUpperCase(principal.charAt(0))) {
                    manquantes.add(principal);
                }
            }
        }

        if (manquantes.isEmpty())
            return;

        // 4) Création des placeholders
        for (String nom : manquantes) {
            ClasseObjet placeholder = new ClasseObjet(new ArrayList<>(), new ArrayList<>(), nom, "externe");
            this.classes.add(placeholder);
            this.mapClasses.put(nom, placeholder);
        }
    }

    /**
     * Extrait le type principal référencé par un type générique/array.
     * ex: "List<Person>" -> "Person", "Person[]" -> "Person".
     */
    private String extraireTypePrincipal(String type) {
        String t = type.trim();
        // Si générique: récupérer entre le premier < et le premier >
        int i1 = t.indexOf('<');
        int i2 = (i1 >= 0) ? t.indexOf('>', i1 + 1) : -1;
        if (i1 >= 0 && i2 > i1) {
            String inner = t.substring(i1 + 1, i2).trim();
            // Nettoyer wildcards/contraintes simples
            inner = inner.replace("? extends", "").replace("? super", "").trim();
            // Si présence de virgules (List<A,B>), garder le premier symbole plausible
            int comma = inner.indexOf(',');
            if (comma > 0)
                inner = inner.substring(0, comma).trim();
            return nettoyerNom(inner);
        }

        // Si tableau: Type[] => Type
        if (t.endsWith("[]")) {
            return nettoyerNom(t.substring(0, t.length() - 2).trim());
        }

        return nettoyerNom(t);
    }

    private String nettoyerNom(String nom) {
        // Retire les arguments de méthode potentiels, les chevrons restants et packages
        // fully-qualified
        String s = nom;
        int space = s.indexOf(' ');
        if (space > 0)
            s = s.substring(0, space);
        int lt = s.indexOf('<');
        if (lt > 0)
            s = s.substring(0, lt);
        if (s.contains(".")) {
            // garder la partie simple du nom
            s = s.substring(s.lastIndexOf('.') + 1);
        }
        return s.trim();
    }

    /**
     * Suppression d'une classe et des relations associées.
     */
    public void supprimerClasse(String nomClasse) {
        if (nomClasse == null || nomClasse.isEmpty()) {
            return;
        }

        ClasseObjet aSupprimer = null;

        for (ClasseObjet c : this.classes) {
            if (nomClasse.equals(c.getNom())) {
                aSupprimer = c;
                break;
            }
        }

        if (aSupprimer != null) {
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
    public static void main(String[] args) {
        ConsoleVue vue = new ConsoleVue();

        if (args.length == 0) {
            vue.afficherUsage();
            return;
        }

        new AnalyseIHMControleur(args[0]);
    }
}