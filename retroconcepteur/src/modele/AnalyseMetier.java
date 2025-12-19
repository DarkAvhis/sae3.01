package modele;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import modele.entites.AssociationObjet;
import modele.entites.ClasseObjet;
import modele.entites.HeritageObjet;
import modele.entites.InterfaceObjet;
import modele.entites.LiaisonObjet;
import modele.entites.MethodeObjet;

/**
 * Contrôleur métier déplacé dans le package `modele`.
 */
public class AnalyseMetier {
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
    public AnalyseMetier() {
        this.classes = new ArrayList<ClasseObjet>();
        this.associations = new ArrayList<AssociationObjet>();
        this.heritages = new ArrayList<HeritageObjet>();
        this.implementations = new ArrayList<InterfaceObjet>();

        this.mapClasses = new HashMap<String, ClasseObjet>();
        this.analyseur = new AnalyseurUML();
    }

    // Constructeur intégral capable d'être utilisé par un contrôleur externe
    // pour lancer une analyse mais ne réalise pas d'affichage (séparation MVC).
    public AnalyseMetier(String cheminDossier) {
        this(); // initialise les listes
        this.analyserDossier(cheminDossier);
    }

    /*-------------------------------------- */
    /* Accesseurs */
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
    /* Méthodes */
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

        // Ajouter les constructeurs par défaut si manquants
        ajouterConstructeursParDefaut();

        ajouterClassesExternes();

        this.associations.addAll(
                analyseur.detecterAssociations(this.classes, this.mapClasses));
        this.heritages.addAll(analyseur.resoudreHeritage(this.mapClasses));
        this.implementations.addAll(
                analyseur.resoudreImplementation(this.mapClasses));

        List<LiaisonObjet> toutes = new ArrayList<LiaisonObjet>();
        toutes.addAll(this.associations);
        toutes.addAll(this.heritages);
        toutes.addAll(this.implementations);

        analyseur.renumeroterLiaisonsFinales(toutes);

        return true;
    }

    private void ajouterClassesExternes() {
        HashSet<String> manquantes = new HashSet<String>();

        for (String parent : this.analyseur.getIntentionsHeritage().values()) {
            if (parent == null || "Object".equals(parent)) {
                continue;
            }
            if (!this.mapClasses.containsKey(parent)) {
                manquantes.add(parent);
            }
        }

        for (ArrayList<String> lst : this.analyseur.getInterfaces().values()) {
            for (String iface : lst) {
                if (iface == null || iface.isEmpty()) {
                    continue;
                }
                if (!this.mapClasses.containsKey(iface)) {
                    manquantes.add(iface);
                }
            }
        }

        if (manquantes.isEmpty()) {
            return;
        }

        for (String nom : manquantes) {
            ClasseObjet placeholder = new ClasseObjet(new ArrayList<>(), new ArrayList<>(), nom, "externe");
            this.classes.add(placeholder);
            this.mapClasses.put(nom, placeholder);
        }
    }

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

        this.associations.removeIf(
                a -> a.getClasseFille().getNom().equals(nomClasse)
                        || a.getClasseMere().getNom().equals(nomClasse));

        this.heritages.removeIf(
                h -> h.getClasseFille().getNom().equals(nomClasse)
                        || h.getClasseMere().getNom().equals(nomClasse));
    }

    /**
     * Ajoute un constructeur par défaut à chaque classe qui n'en a pas.
     * Le constructeur par défaut a la signature : public NomClasse()
     */
    private void ajouterConstructeursParDefaut() {
        for (ClasseObjet classe : this.classes) {
            // Les interfaces n'ont pas de constructeurs
            if (classe.getSpecifique() != null && classe.getSpecifique().equals("interface")) {
                continue;
            }

            // Vérifier si la classe a déjà un constructeur
            boolean aUnConstructeur = false;
            if (classe.getMethodes() != null) {
                for (MethodeObjet methode : classe.getMethodes()) {
                    // Un constructeur a retourType = null et son nom = nom de la classe
                    if (methode.getRetourType() == null && methode.getNom().equals(classe.getNom())) {
                        aUnConstructeur = true;
                        break;
                    }
                }
            }

            // Si pas de constructeur, en ajouter un par défaut
            if (!aUnConstructeur) {
                MethodeObjet constructeurParDefaut = new MethodeObjet(
                        classe.getNom(), // nom = nom de la classe
                        new HashMap<String, String>(), // pas de paramètres
                        "public", // visibilité
                        false // pas statique
                );
                classe.getMethodes().add(0, constructeurParDefaut); // Ajouter au début
            }
        }
    }

    /*-------------------------------------- */
    /* Note: suppression du main pour que le seul point d'entrée
     * soit `controleur.Controleur` (respect du souhait d'un seul
     * contrôleur). L'affichage doit être réalisé par le contrôleur
     * (vue séparée du modèle).
     */
}
