package modele;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Contrôleur principal pour l'IHM (Interface Homme-Machine).
 * Gère l'orchestration de l'analyse, la détection des relations,
 * et fournit les données au niveau de l'application (Vue).
 */
public class AnalyseUMLControleur
{
    private List<ClasseObjet> classes;
    private HashMap<String, ClasseObjet> mapClasses;
    private List<AssociationObjet> associations;
    private List<HeritageObjet> heritages;
    private List<InterfaceObjet> implementations;
    private AnalyseurUML analyseur;
    
    public AnalyseUMLControleur()
    {
        this.classes = new ArrayList<>();
        this.mapClasses = new HashMap<>();
        this.associations = new ArrayList<>();
        this.heritages = new ArrayList<>();
        this.implementations = new ArrayList<>();
        this.analyseur = new AnalyseurUML(); // L'analyseur est délégué ici
    }

    /**
     * Exécute l'analyse complète d'un répertoire.
     * @param cheminDossier Le chemin du répertoire contenant les fichiers Java.
     * @return true si l'analyse a réussi, false sinon.
     */
    public boolean analyserDossier(String cheminDossier)
    {
        File cible = new File(cheminDossier);
        if (!cible.isDirectory())
        {
            System.out.println("Erreur: Le chemin fourni n'est pas un répertoire valide.");
            return false;
        }

        // Réinitialiser les données en cas d'appels multiples
        this.classes.clear();
        this.mapClasses.clear();
        this.associations.clear();
        this.heritages.clear();
        this.implementations.clear();
        this.analyseur.resetRelations(); // Important pour les relations stockées

        List<File> fichiersJava = analyseur.ClassesDuDossier(cheminDossier);
        
        // 1. Analyse de tous les fichiers et construction du modèle (remplit l'héritage interne)
        for (File f : fichiersJava)
        {
            ClasseObjet c = analyseur.analyserFichierUnique(f.getAbsolutePath()); 
            if (c != null)
            {
                this.classes.add(c);
                this.mapClasses.put(c.getNom(), c);
            }
        }
        
        // 2. Détection des relations qui nécessitent toutes les classes chargées
        this.associations.addAll(analyseur.detecterAssociations(this.classes, this.mapClasses));
        this.heritages.addAll(analyseur.getLstHerite());
        // L'implémentation (InterfaceObjet) sera gérée dans l'Étape 4

        return true;
    }
    
    // --- Getters pour l'IHM/Vue ---
    
    public List<ClasseObjet> getClasses()
    {
        return classes;
    }

    public List<AssociationObjet> getAssociations()
    {
        return associations;
    }

    public List<HeritageObjet> getHeritages()
    {
        return heritages;
    }
    
    public List<InterfaceObjet> getImplementations()
    {
        // Sera rempli lors de l'implémentation de l'étape 4 (interfaces)
        return implementations;
    }

    // --- Main pour l'exécution CUI (Point d'entrée de l'application) ---

    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("Usage: java AnalyseIHMControleur <chemin_du_repertoire>");
            return;
        }

        AnalyseIHMControleur controleur = new AnalyseIHMControleur();
        
        if (controleur.analyserDossier(args[0]))
        {
            // Affichage CUI (similaire à la Vue Console)
            
            System.out.println("\n=== DIAGRAMMES DE CLASSES (ETAPE 2 & 3) ===");
            for (ClasseObjet c : controleur.getClasses())
            {
                System.out.println(c.toString());
            }

            System.out.println("\n=== LIAISONS D'ASSOCIATION (ETAPE 3) ===");
            for (AssociationObjet asso : controleur.getAssociations())
            {
                System.out.println(asso.toString());
            }

            System.out.println("\n=== HÉRITAGE (ETAPE 4) ===");
			for (HeritageObjet heri : controleur.getHeritages())
            {
                System.out.println(heri);
            }
            
            // Les implémentations seront affichées ici après l'étape 4
        }
    }
}