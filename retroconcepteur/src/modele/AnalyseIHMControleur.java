package modele;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

/**
 * Contrôleur principal pour l'IHM (Interface Homme-Machine).
 * Gère l'orchestration de l'analyse, la détection des relations,
 * et fournit les données au niveau de l'application (Vue).
 */
public class AnalyseIHMControleur
{
    private List<ClasseObjet> classes;
    private HashMap<String, ClasseObjet> mapClasses;
    private List<AssociationObjet> associations;
    private List<HeritageObjet> heritages;
    private List<InterfaceObjet> implementations;
    private AnalyseurUML analyseur;
    
    public AnalyseIHMControleur()
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
        this.analyseur.resetRelations(); // Réinitialise l'intention d'héritage

        List<File> fichiersJava = analyseur.ClassesDuDossier(cheminDossier);
        
        // 1. Analyse de tous les fichiers et construction du modèle
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
        
        // 3. Résolution des liens
        resoudreHeritage();
        resoudreImplementation();

        return true;
    }
    
    /**
     * Convertit la liste des noms de classes (intentions) en objets HeritageObjet réels 
     * en utilisant la map de toutes les classes parsées.
     */
    private void resoudreHeritage()
    {
        // Utilisation d'un HashSet pour éviter les doublons d'affichage des liens
        HashSet<String> heritagesAjoutes = new HashSet<>();
        
        for (String[] intention : analyseur.getIntentionsHeritage())
        {
            String nomEnfant = intention[0];
            String nomParent = intention[1];
            
            // Vérification si les deux classes existent (Enfant doit exister, Parent peut être null/Object)
            if (mapClasses.containsKey(nomEnfant) && mapClasses.containsKey(nomParent))
            {
                ClasseObjet classeEnfant = mapClasses.get(nomEnfant);
                ClasseObjet classeParent = mapClasses.get(nomParent);
                
                String cle = nomParent + "->" + nomEnfant;
                
                if (!heritagesAjoutes.contains(cle))
                {
                    this.heritages.add(new HeritageObjet(classeParent, classeEnfant));
                    heritagesAjoutes.add(cle);
                }
            } 
            else if (!nomParent.equals("Object"))
            {
                // Avertissement si la super-classe est locale mais non trouvée (et n'est pas la classe de base Object)
                System.out.println("Avertissement: Super-classe '" + nomParent + "' déclarée pour '" + nomEnfant + "', mais non trouvée dans le répertoire analysé.");
            }
        }
    }
    
    /**
     * NOUVEAU: Résout les implémentations d'interfaces en objets InterfaceObjet réels.
     */
    // ... dans AnalyseIHMControleur.java

    /**
     * Résout les implémentations d'interfaces en objets InterfaceObjet réels et les regroupe par classe.
     */
    private void resoudreImplementation()
    {
        // Map pour regrouper les interfaces par la CLASSE CONCRÈTE (Clé = Nom Classe Concrète)
        HashMap<String, InterfaceObjet> regroupement = new HashMap<>();

        // Le format de l'intention est String[0] = Classe Concrète, String[1] = Interface
        for (String[] intention : analyseur.getInterfaces())
        {
            String nomClasseConcrète = intention[0];
            String nomInterface = intention[1];
            
            // Résolution dans la map
            if (mapClasses.containsKey(nomClasseConcrète) && mapClasses.containsKey(nomInterface))
            {
                ClasseObjet classeConcrète = mapClasses.get(nomClasseConcrète);
                ClasseObjet interfaceObjet = mapClasses.get(nomInterface);
                
                // 1. Vérifier si la classe concrète a déjà une relation InterfaceObjet
                if (!regroupement.containsKey(nomClasseConcrète))
                {
                    // Si non, créer l'objet InterfaceObjet de base pour cette classe
                    regroupement.put(nomClasseConcrète, new InterfaceObjet(classeConcrète));
                }
                
                // 2. Ajouter l'interface à la relation existante
                regroupement.get(nomClasseConcrète).ajouterInterface(interfaceObjet);
            }
        }
        
        // 3. Transférer tous les objets InterfaceObjet rassemblés vers la liste finale
        this.implementations.addAll(regroupement.values());
    }

// ...
    
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

            System.out.println("\n=== IMPLÉMENTATION (ETAPE 4) ===");
			for (InterfaceObjet impl : controleur.getImplementations())
            {
                System.out.println(impl.toString());
            }
        }
    }
}