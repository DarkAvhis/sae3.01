package src.modele;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Contrôleur principal pour l'analyse des fichiers Java et la construction du
 * modèle UML.
 * 
 * Cette classe orchestre l'analyse complète d'un répertoire de fichiers Java :
 * - Analyse syntaxique de chaque fichier
 * - Détection des associations entre classes
 * - Résolution des relations d'héritage
 * - Résolution des implémentations d'interfaces
 * 
 * Elle fournit les données structurées au contrôleur principal pour l'affichage
 * dans la vue.
 * 
 * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT, Ariunbayar
 *         BUYANBADRAKH, Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class AnalyseIHMControleur
    {
    private List<ClasseObjet> classes;
    private HashMap<String, ClasseObjet> mapClasses;
    private List<AssociationObjet> associations;
    private List<HeritageObjet> heritages;
    private List<InterfaceObjet> implementations;
    private AnalyseurUML analyseur;

    /**
     * Constructeur du contrôleur d'analyse.
     * 
     * Initialise les structures de données pour stocker les classes et leurs
     * relations.
     */
    public AnalyseIHMControleur() 
    {
        this.classes = new ArrayList<>();
        this.associations = new ArrayList<>();
        this.heritages = new ArrayList<>();
        this.implementations = new ArrayList<>();

        this.mapClasses = new HashMap<>();
        this.analyseur = new AnalyseurUML();
    }

    /**
     * Exécute l'analyse complète d'un répertoire de fichiers Java.
     * 
     * Processus d'analyse en 3 étapes :
     * 1. Analyse de tous les fichiers Java et construction des objets ClasseObjet
     * 2. Détection des associations entre classes (attributs de type classe)
     * 3. Résolution des relations d'héritage et d'implémentation d'interfaces
     * 
     * @param cheminDossier Chemin absolu vers le répertoire contenant les fichiers
     *                      Java
     * @return true si l'analyse a réussi, false en cas d'erreur
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
        this.reinitialiser(); // Réinitialise l'intention d'héritage

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
        this.resoudreHeritage();
        this.resoudreImplementation();
        this.renumeroterLiaisonsFinales();

        return true;
    }

    /**
     * Résout les relations d'héritage entre classes.
     * 
     * Convertit les intentions d'héritage (paires de noms de classes) en objets
     * HeritageObjet
     * en utilisant la map des classes analysées. Évite les doublons et vérifie
     * l'existence
     * des classes parent et enfant.
     */
    private void resoudreHeritage() 
    {
        // Utilisation d'un HashSet pour éviter les doublons d'affichage des liens
        HashSet<String> heritagesAjoutes = new HashSet<>();

        for (String[] intention : analyseur.getIntentionsHeritage()) 
        {
            String nomEnfant = intention[0];
            String nomParent = intention[1];

            // Vérification si les deux classes existent (Enfant doit exister, Parent peut
            // être null/Object)
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
                // Avertissement si la super-classe est locale mais non trouvée (et n'est pas la
                // classe de base Object)
                System.out.println("Avertissement: Super-classe '" + nomParent + "' déclarée pour '" + nomEnfant
                        + "', mais non trouvée dans le répertoire analysé.");
            }
        }
    }

    /**
     * Résout les implémentations d'interfaces en objets InterfaceObjet réels.
     */
    // ... dans AnalyseIHMControleur.java

    /**
     * Résout les implémentations d'interfaces.
     * 
     * Regroupe toutes les interfaces implémentées par une même classe concrète
     * dans un seul objet InterfaceObjet. Cela évite de créer une relation séparée
     * pour chaque interface implémentée.
     */
    private void resoudreImplementation() 
    {
        // Map pour regrouper les interfaces par la CLASSE CONCRÈTE (Clé = Nom Classe
        // Concrète)
        HashMap<String, InterfaceObjet> regroupement = new HashMap<>();

        // Le format de l'intention est String[0] = Classe Concrète, String[1] =
        // Interface
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

    /**
     * Supprime une classe de la liste.
     * 
     * @param classe Nom de la classe à supprimer
     * @note Cette méthode est actuellement en développement
     */
    public void supprimerClasse(String classe) 
    {
        if (classe == null)
            return;
        this.classes.remove(classe);
    }

    /**
     * Réinitialise toutes les données d'analyse.
     * 
     * Vide les listes de classes, associations, héritages et implémentations.
     * Réinitialise également l'analyseur syntaxique.
     */
    public void reinitialiser() 
    {
        this.classes.clear();
        this.mapClasses.clear();
        this.associations.clear();
        this.heritages.clear();
        this.implementations.clear();
        this.analyseur.resetRelations(); // Réinitialise l'intention d'héritage
    }

    /**
     * Renumérote toutes les liaisons de manière séquentielle.
     * 
     * Assure que les numéros de liaisons sont consécutifs en commençant à 1.
     * Regroupe toutes les liaisons (associations, héritages, implémentations)
     * pour un affichage cohérent.
     */
    private void renumeroterLiaisonsFinales() 
    {
        // Réinitialiser le compteur statique pour éviter les conflits dans les logs ou
        // les numéros futurs.
        LiaisonObjet.reinitialiserCompteur();

        // Rassembler toutes les liaisons finales à renuméroter
        List<LiaisonObjet> toutesLesLiaisons = new ArrayList<>();
        toutesLesLiaisons.addAll(this.associations);
        toutesLesLiaisons.addAll(this.heritages);
        toutesLesLiaisons.addAll(this.implementations);

        int nouveauCompteur = 1;

        // On renumérote chaque liaison dans l'ordre pour un affichage propre
        for (LiaisonObjet liaison : toutesLesLiaisons) 
        {
            liaison.setNum(nouveauCompteur++);
        }
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

    /**
     * Point d'entrée pour l'exécution en mode console (CUI).
     * 
     * Permet de tester l'analyse d'un répertoire sans interface graphique.
     * Affiche les résultats de l'analyse dans la console.
     * 
     * @param args Arguments de la ligne de commande (chemin du répertoire à
     *             analyser)
     */
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
                System.out.println(heri.toString());
            }

            System.out.println("\n=== IMPLÉMENTATION (ETAPE 4) ===");
            for (InterfaceObjet impl : controleur.getImplementations()) 
    {
                System.out.println(impl.toString());
            }
        }
    }
}
