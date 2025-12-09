package modele;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;


/**
 * Classe AnalyseurUML
 * 
 * Cette classe permet d'analyser des fichiers Java afin de générer
 * des objets représentant des classes, leurs attributs, méthodes
 * et détecter les associations entre classes pour construire un UML.
 */

public class AnalyseurUML
{
    /**
     * Constante représentant une multiplicité indéfinie.
     */
    private static final int MULT_INDEFINIE = 999999999;


    /**
     * Point d'entrée du programme.
     * 
     * @param args Arguments passés au programme : chemin d'un fichier ou d'un dossier Java
     */
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("Usage: java AnalyseurUML <chemin_du_repertoire_ou_fichier_java>");
            return;
        }

        AnalyseurUML analyseur = new AnalyseurUML();
        String       chemin    = args[0];
        File         cible     = new File(chemin);

        if (cible.isFile())
        {
            ClasseObjet classeResultat = analyseur.analyserFichierUnique(chemin);
            
            if (classeResultat != null)
            {
                System.out.println(classeResultat.toString()); 
            }
        }
        else if (cible.isDirectory())
        {
            List<File> fichiersJava = analyseur.ClassesDuDossier(chemin);
            List<ClasseObjet> classes = new ArrayList<>();
            HashMap<String, ClasseObjet> mapClasses = new HashMap<>();

            for (File f : fichiersJava)
            {
                ClasseObjet c = analyseur.analyserFichierUnique(f.getAbsolutePath());
                if (c != null)
                {
                    classes.add(c);
                    mapClasses.put(c.getNom(), c);
                }
            }

            System.out.println("\n=== DIAGRAMMES DE CLASSES (ETAPE 2 & 3) ===");
            for (ClasseObjet c : classes)
            {
                System.out.println(c.toString());
            }

            System.out.println("\n=== LIAISONS D'ASSOCIATION (ETAPE 3) ===");
            List<AssociationObjet> associations = analyseur.detecterAssociations(classes, mapClasses);
            
            for (AssociationObjet asso : associations)
            {
                System.out.println(asso.toString());
            }
        }
        else
        {
            System.out.println("Erreur: Le chemin fourni n'est ni un fichier, ni un répertoire valide.");
        }
    }


    /**
     * Analyse un fichier Java unique pour extraire sa classe, attributs et méthodes.
     * 
     * @param chemin Chemin du fichier Java
     * @return Objet ClasseObjet représentant la classe analysée
     */
    public ClasseObjet analyserFichierUnique(String chemin)
    {
        File f = new File(chemin);
        String nomClasse = f.getName().replace(".java", "");

        ArrayList<AttributObjet> attributs = new ArrayList<>();
        ArrayList<MethodeObjet>  methodes  = new ArrayList<>();

        try (Scanner sc = new Scanner(f))
        {
            while (sc.hasNextLine())
            {
                String ligne = sc.nextLine().trim();

                if (ligne.isEmpty() || ligne.startsWith("package") || ligne.startsWith("import") || ligne.equals("}"))
                {
                    continue;
                }
                
                boolean estStatique   =  ligne.contains("static");
                boolean ligneVisibile = (ligne.startsWith("public") || ligne.startsWith("private") || ligne.startsWith("protected"));

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


    /**
     * Extrait un attribut depuis une ligne de code Java et l'ajoute à la liste.
     * 
     * @param ligne Ligne du code source
     * @param estStatique Indique si l'attribut est statique
     * @param attributs Liste d'attributs à compléter
     */
    private void extraireAttribut(String ligne, boolean estStatique, ArrayList<AttributObjet> attributs)
    {

		if (ligne.contains("="))
		{
			ligne = ligne.substring(0, ligne.indexOf("="));
		}
		
        String propre  = ligne.replace(";", "").trim();
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
        int indexParenthese    = ligne.indexOf('(');
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


    /**
     * Retourne la liste des fichiers Java présents dans un dossier.
     * 
     * @param cheminDossier Chemin du dossier à analyser
     * @return Liste de fichiers Java
     */
    public List<File> ClassesDuDossier(String cheminDossier)
    {
        File dossier = new File(cheminDossier);
        File[] tousLesFichiers = dossier.listFiles();
        List<File> fichiersJava = new ArrayList<>();
        
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