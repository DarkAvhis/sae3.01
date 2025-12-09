package modele;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
// Import java.util.HashSet est répété, une seule déclaration suffit.

/**
 * Classe utilitaire pour l'analyse syntaxique (parsing) des fichiers Java.
 * Gère l'extraction des membres (attributs/méthodes) et la détection des relations simples (héritage).
 */
public class AnalyseurUML
{
    /**
     * Constante représentant une multiplicité indéfinie.
     */
    private static final int MULT_INDEFINIE = 999999999;
    
    // Ces listes sont remplies lors de l'analyse d'un dossier et sont lues par le Contrôleur
	private ArrayList<HeritageObjet> lstHerite = new ArrayList<>();
    private HashSet<String> heritagesAjoutes = new HashSet<>();
    // private ArrayList<InterfaceObjet> lstInterfaces = new ArrayList<>(); // Pour l'étape 4 Implémentation

    // La méthode main est supprimée d'AnalyseurUML car elle est déplacée vers AnalyseIHMControleur

    /**
     * Réinitialise les listes de relations stockées pour une nouvelle série d'analyses.
     * Cruciale pour permettre au Contrôleur d'analyser plusieurs dossiers ou de raffraîchir les données.
     */
    public void resetRelations()
    {
        this.lstHerite.clear();
        this.heritagesAjoutes.clear();
        // this.lstInterfaces.clear(); // Pour l'étape 4 Implémentation
    }

    /**
     * Getter pour la liste d'héritage, utilisé par le contrôleur (Étape 4).
     */
    public ArrayList<HeritageObjet> getLstHerite()
    {
        return lstHerite;
    }

    /**
     * Analyse un fichier Java et construit l'objet ClasseObjet correspondant.
     * Gère la détection du mot-clé 'extends'.
     */
    public ClasseObjet analyserFichierUnique(String chemin)
    {
        File f = new File(chemin);
        String nomClasse = f.getName().replace(".java", "");

        ArrayList<AttributObjet> attributs = new ArrayList<>();
        ArrayList<MethodeObjet> methodes = new ArrayList<>();
        boolean estHeritier = false ;

        // Ces variables sont utilisées pour stocker temporairement l'information d'héritage
        ClasseObjet classeDest = null;
        String nomParent = null; 

        // --- Début de la logique de recherche de fichiers pour la classe parente ---
        ArrayList<File> lstFichier = new ArrayList<>();
        File parentDir = f.getParentFile();
        
        if (parentDir != null)
        {
            // Nous utilisons directement ClassesDuDossier sur le répertoire parent
            lstFichier = ClassesDuDossier(parentDir.getAbsolutePath());
        }
        // --- Fin de la logique de recherche de fichiers pour la classe parente ---

        try (Scanner sc = new Scanner(f))
        {
            while (sc.hasNextLine())
            {
                String ligne = sc.nextLine().trim();

                if (ligne.isEmpty() || ligne.startsWith("package") || ligne.startsWith("import") || ligne.equals("}"))
                {
                    continue;
                }

                // --- DÉTECTION HÉRITAGE (ETAPE 4) ---
                if (ligne.contains("class ") && ligne.contains("extends"))
                {
                    estHeritier = true;
                    // Logique d'extraction du nom parent
                    String afterExtends = ligne.substring(ligne.indexOf("extends") + 7).trim();
                    int indexEspace = afterExtends.indexOf(' ');
                    int indexAccolade = afterExtends.indexOf('{');
                    int indexFinNom = afterExtends.length();
                    
                    if (indexEspace != -1 && indexEspace < indexFinNom) indexFinNom = indexEspace;
                    if (indexAccolade != -1 && indexAccolade < indexFinNom) indexFinNom = indexAccolade;

                    nomParent = afterExtends.substring(0, indexFinNom).trim();
                    
                    // --- Résolution locale de la classe parente ---
                    for (File fichier : lstFichier)
                    {
                        String baseName = fichier.getName().replaceAll("\\.java$", "");
                        if (baseName.equals(nomParent))
                        {
                            // ATTENTION: Ceci provoque une analyse récursive ! 
                            // Dans un environnement JDK uniquement, c'est parfois nécessaire
                            // mais peut entraîner des problèmes si le fichier contient des dépendances circulaires.
                            // Nous allons laisser le Contrôleur faire la résolution finale pour éviter la récursion,
                            // en stockant juste le nom du parent.
                            // Pour respecter la logique initiale fournie, nous conservons cette boucle
                            // en la modifiant pour ne pas ré-analyser l'objet mais juste trouver le fichier.
                            
                            // Pour l'analyse récursive, on mettrait: classeDest = analyserFichierUnique(fichier.getAbsolutePath());
                            // Pour le moment, nous laissons 'classeDest' à null ici, et la remplissons avec une classe bidon
                            // contenant juste le nom, que le Contrôleur devra résoudre plus tard.
                            classeDest = new ClasseObjet(new ArrayList<>(), new ArrayList<>(), nomParent);
                            break;
                        }
                    }
                    // --- Fin de la résolution locale ---
                }

                boolean estStatique = ligne.contains("static");
                boolean aVisibilite = (ligne.startsWith("public") || ligne.startsWith("private") || ligne.startsWith("protected"));

                if (ligneVisibile && ligne.endsWith(";") && !ligne.contains("("))
                {
                    extraireAttribut(ligne, estStatique, attributs);
                }

                else if (ligneVisibile && ligne.contains("(") && !ligne.contains("class "))
                {
                    extraireMethode(ligne, estStatique, nomClasse, methodes);
                }
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Fichier non trouvé: " + chemin);
            return null;
        }

        return new ClasseObjet(attributs, methodes, nomClasse);
    }

    private void extraireAttribut(String ligne, boolean estStatique, ArrayList<AttributObjet> attributs)
    {
        // ... (Logique inchangée pour extraireAttribut) ...
        if (ligne.contains("="))
		{
			ligne = ligne.substring(0, ligne.indexOf("="));
		}

        String propre = ligne.replace(";", "").trim();
        String[] parts = propre.split("\\s+");
        
        List<String> motsExclus = Arrays.asList("private", "public", "protected", "static", "final", "abstract");

        List<String> motsUtiles = Arrays.stream(parts)
                                        .filter(p -> !p.isEmpty() && !motsExclus.contains(p))
                                        .collect(Collectors.toList());

        if (motsUtiles.size() >= 2)
        {
            String type       = motsUtiles.get(motsUtiles.size() - 2);
            String nom        = motsUtiles.get(motsUtiles.size() - 1);
            String visibilite = parts[0]; 
            
            attributs.add(new AttributObjet(nom, estStatique ? "static" : "instance", type, visibilite, estStatique));
        }
    }
    

    /**
     * Extrait une méthode depuis une ligne de code Java et l'ajoute à la liste.
     * 
     * @param ligne Ligne du code source
     * @param estStatique Indique si la méthode est statique
     * @param nomClasse Nom de la classe analysée
     * @param methodes Liste de méthodes à compléter
     */
    private void extraireMethode(String ligne, boolean estStatique, String nomClasse, ArrayList<MethodeObjet> methodes)
    {
        // ... (Logique inchangée pour extraireMethode) ...
        int indexParenthese = ligne.indexOf('(');
        String avantParenthese = ligne.substring(0, indexParenthese).trim();
        String[] parts         = avantParenthese.split("\\s+");
        
        List<String> motsExclus = Arrays.asList("private", "public", "protected", "static", "final", "abstract");

        List<String> motsUtiles = Arrays.stream(parts)
                                        .filter(p -> !p.isEmpty() && !motsExclus.contains(p))
                                        .collect(Collectors.toList());

        if (motsUtiles.isEmpty())
        {
            return;
        }

        String nomMethode = motsUtiles.get(motsUtiles.size() - 1);
        String visibilite = parts[0];
        String typeRetour = null;
        
        if (nomMethode.equals(nomClasse))
        {
            typeRetour = null;
        }
        else if (motsUtiles.size() >= 2)
        {
            typeRetour = motsUtiles.get(motsUtiles.size() - 2);
        }
        else
        {
            typeRetour = "void";
        }
        
        String contenuParam = ligne.substring(
                indexParenthese + 1,
                ligne.lastIndexOf(")")
        ).trim();

        HashMap<String, String> params = new HashMap<>();

        if (!contenuParam.isEmpty())
        {
            String[] listeParams = contenuParam.split(",");

            for (String p : listeParams)
            {
                p = p.trim(); 
                int spaceIndex = p.lastIndexOf(" ");
                if (spaceIndex > 0)
                {
                    String type = p.substring(0, spaceIndex).trim();
                    String name = p.substring(spaceIndex + 1).trim();
                    params.put(name, type);
                }
            }
        }

        methodes.add(new MethodeObjet(nomMethode, params, typeRetour, visibilite, estStatique));
    }



    /**
     * Détecte les associations entre classes à partir des attributs de chaque classe.
     * 
     * @param classes Liste des classes analysées
     * @param mapClasses Map pour retrouver rapidement une classe par son nom
     * @return Liste d'associations détectées
     */
    public List<AssociationObjet> detecterAssociations(List<ClasseObjet> classes, HashMap<String, ClasseObjet> mapClasses)
    {
        // ... (Logique inchangée pour detecterAssociations) ...
        List<AssociationObjet> associations = new ArrayList<>();
        
        for (ClasseObjet classeOrigine : classes)
        {
            for (AttributObjet attribut : classeOrigine.getattributs())
            {
                String typeAttribut = attribut.getType();
                String typeCible = typeAttribut;
                
                MultipliciteObjet multCible = new MultipliciteObjet(1, 1); 
                MultipliciteObjet multOrigine = new MultipliciteObjet(1, 1);

                boolean estCollection = false;
                
                if (typeAttribut.contains("<") && typeAttribut.contains(">"))
                {
                    typeCible = typeAttribut.substring(typeAttribut.indexOf("<") + 1, typeAttribut.indexOf(">")).trim();
                    estCollection = true;
                }
                else if (typeAttribut.endsWith("[]"))
                {
                    typeCible = typeAttribut.replace("[]", "").trim();
                    estCollection = true;
                }

                if (estCollection)
                {
                    multCible = new MultipliciteObjet(0, MULT_INDEFINIE); 
                }
                
                if (mapClasses.containsKey(typeCible) && !typeCible.equals(classeOrigine.getNom()))
                {
                    ClasseObjet classeCible = mapClasses.get(typeCible);

                    AssociationObjet association = new AssociationObjet(
                            classeCible, 
                            classeOrigine, 
                            multCible,       
                            multOrigine,     
                            attribut.getNom(), 
                            true            
                    );
                    associations.add(association);
                }
            }
        }
        return associations;
    }

    public ArrayList<File> ClassesDuDossier(String cheminDossier)
    {
        // ... (Logique inchangée pour ClassesDuDossier) ...
        File dossier = new File(cheminDossier);
        File[] tousLesFichiers = dossier.listFiles();
        ArrayList<File> fichiersJava = new ArrayList<>();
        
        if (tousLesFichiers != null)
        {
            for (File f : tousLesFichiers)
            {
                if (f.getName().endsWith(".java"))
                {
                    fichiersJava.add(f);
                }
            }
        }
        return fichiersJava;
    }
}