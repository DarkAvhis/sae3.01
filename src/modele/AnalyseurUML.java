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
 * Analyseur UML permettant de réaliser un parsing manuel de fichiers Java afin
 * d'en extraire :
 *     Le nom de la classe</li>
 *     Ses attributs</li>
 *     Ses méthodes</li>
 *     Ses relations d'héritage</li>
 *     Ses implémentations d'interfaces</li>
 *     Ses associations avec d'autres classes du projet
 *
 * L'analyse syntaxique est volontairement simplifiée pour fonctionner sans
 * backend de compilation, en reposant principalement sur des règles
 * textuelles. Les relations (héritage / implémentation) sont stockées sous
 * forme de chaînes pour être résolues plus tard par le contrôleur.
 */
public class AnalyseurUML
{
    /** Valeur représentant une multiplicité indéfinie (ex: *). */
    private static final int MULT_INDEFINIE = 999999999;
    
    /** Liste des intentions d'héritage au format {enfant, parent}. */
	private ArrayList<String[]> lstIntentionHeritage = new ArrayList<>(); 
    /** Liste des relations d'implémentation au format {classe, interface}. */
    private ArrayList<String[]> lstInterfaces        = new ArrayList<>();

    /**
     * Réinitialise toutes les relations enregistrées par l'analyseur.
     */
    public void resetRelations()
    {
        this.lstIntentionHeritage.clear();
        this.lstInterfaces.clear(); 
    }

    /**
     * Retourne la liste des intentions d'héritage détectées lors du parsing.
     *
     * @return liste de couples {nomClasse, nomParent}
     */
    public ArrayList<String[]> getIntentionsHeritage()
    {
        return lstIntentionHeritage;
    }

    /**
     * Retourne la liste des relations classe -> interface détectées.
     *
     * @return liste de couples {nomClasse, nomInterface}
     */
    public ArrayList<String[]> getInterfaces()
    {
        return lstInterfaces;
    }


    /**
     * Analyse un fichier Java donné et construit un objet {@link ClasseObjet}
     * représentant sa structure UML.
     *
     * @param chemin chemin absolu ou relatif vers le fichier Java à analyser
     * @return une instance de ClasseObjet, ou {@code null} si le fichier est introuvable
     */
    public ClasseObjet analyserFichierUnique(String chemin)
    {
        File file = new File(chemin);
        String nomClasse = file.getName().replace(".java", "");

        ArrayList<AttributObjet> attributs           = new ArrayList<>();
        ArrayList<MethodeObjet>  methodes            = new ArrayList<>();
        ArrayList<String>        interfacesDetectees = new ArrayList<>();

        boolean estHeritier = false;
<<<<<<< HEAD
        ArrayList<String> interfacesDetectees = new ArrayList<>();
        String nomParent = null;
        String specifique = "";
        String[] tabSpecifique = {"abstract class", 
                                    "interface",
                                    "enum",    
                                    "record"};
=======
        String  nomParent   = null;
>>>>>>> f293605 (AnalyseurUML javadoc)

        try (Scanner sc = new Scanner(file))
        {
            while (sc.hasNextLine())
            {
                String ligne = sc.nextLine().trim();

                // Ignorer les lignes non pertinentes
                if (ligne.isEmpty() || ligne.startsWith("package") || ligne.startsWith("import") || ligne.equals("}"))
                {
                    continue;
                }

                // détection extends
                if (ligne.contains("class ") && ligne.contains("extends"))
                {
                    estHeritier = true;

                    String suiteApresExtends = ligne.substring(ligne.indexOf("extends") + "extends".length()).trim();
                    int indexEspace   = suiteApresExtends.indexOf(' ');
                    int indexAccolade = suiteApresExtends.indexOf('{');
                    int indexFinNom   = suiteApresExtends.length();

                    if (indexEspace   != -1 && indexEspace   < indexFinNom) indexFinNom = indexEspace  ;
                    if (indexAccolade != -1 && indexAccolade < indexFinNom) indexFinNom = indexAccolade;

                    nomParent = suiteApresExtends.substring(0, indexFinNom).trim();
                }
                
                for (int i = 0; i < tabSpecifique.length; i++)
                {
                    if (ligne.contains(tabSpecifique[i]))
                    {
                        specifique = tabSpecifique[i];
                    }

                }

                if (ligne.contains("record"))
                {
                    int debut = ligne.indexOf('(');
                    int fin = ligne.lastIndexOf(')');

                    if (debut != -1 && fin != -1 && debut < fin)
                    {
                        String contenu = ligne.substring(debut + 1, fin);
                        int start = 0;

                        for( int i =0; i <= contenu.length() ; i++)
                        {
                            if  (i == contenu.length() || contenu.charAt(i) == ',' )
                            {
                                String param = contenu.substring(start,i).trim();
                                start = i +1 ;

                                int idx = param.lastIndexOf(' ');


                                if (idx != -1)
                                {
                                    String type = param.substring(0, idx).trim();
                                    String nom  = param.substring(idx + 1).trim();


                                    attributs.add(new AttributObjet(nom,"instance",type,"private",true));


                                    methodes.add(new MethodeObjet(nom,new HashMap<>(),type,"public",true));
                                }

                            }
                        }
                    }

                    continue;
                }



                // Détection de implements (supports multiple interfaces)
                if (ligne.contains("class ") && ligne.contains("implements"))
                {
                    String suiteApresImplements = ligne.substring(ligne.indexOf("implements") + "implements".length()).trim();
                    int indexAccolade = suiteApresImplements.indexOf('{');

                    if (indexAccolade != -1)
                    {
                        suiteApresImplements = suiteApresImplements.substring(0, indexAccolade).trim();
                    }

                    String[] nomsInterfacesBruts = suiteApresImplements.split(",");
                    for (String elementBrut : nomsInterfacesBruts)
                    {
                        String nomInterface = elementBrut.trim();

                        int idxEspace  = nomInterface.indexOf(' ');
                        int idxChevron = nomInterface.indexOf('<');
                        int idxFin     = nomInterface.length();

                        if (idxEspace  != -1 && idxEspace  < idxFin) idxFin = idxEspace;
                        if (idxChevron != -1 && idxChevron < idxFin) idxFin = idxChevron;
                        nomInterface = nomInterface.substring(0, idxFin).trim();
                        if (!nomInterface.isEmpty())
                        {
                            interfacesDetectees.add(nomInterface);
                        }
                    }
                }

                boolean estStatique =  ligne.contains("static");
                boolean estVisible  = (ligne.startsWith("public") || ligne.startsWith("private") || ligne.startsWith("protected"));

                // Extraction attributs
                if (estVisible && ligne.endsWith(";") && !ligne.contains("("))
                {
                    extraireAttribut(ligne, estStatique, attributs);
                }

                // Extraction méthodes
                else if (estVisible && ligne.contains("(") && !ligne.contains("class "))
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

        ClasseObjet nouvelleClasse = new ClasseObjet(attributs, methodes, nomClasse , specifique);

        // enregistrement héritage
        if (estHeritier && nomParent != null)
        {
            this.lstIntentionHeritage.add(new String[]{nomClasse, nomParent});
        }

        // Enregistrement interfaces
        for (String iface : interfacesDetectees)
        {
            this.lstInterfaces.add(new String[]{nomClasse, iface});
        }

        return nouvelleClasse;
    }

    /**
     * Extrait les informations d'un attribut à partir d'une ligne de code Java.
     *
     * @param ligne ligne du fichier contenant un attribut
     * @param estStatique indique si l'attribut possède le mot-clé static
     * @param attributs liste dans laquelle ajouter l'attribut créé
     */
    public void extraireAttribut(String ligne, boolean estStatique, ArrayList<AttributObjet> attributs)
    {
        if (ligne.contains("="))
        {
            ligne = ligne.substring(0, ligne.indexOf("="));
        }

        String supVirgule = ligne.replace(";", "").trim();
        String[] parts = supVirgule.split("\\s+");
        
        List<String> motsExclus = Arrays.asList("private", "public", "protected", "static", "final", "abstract");

        List<String> motsUtiles = Arrays.stream(parts)
                                        .filter(p -> !p.isEmpty() && !motsExclus.contains(p))
                                        .collect(Collectors.toList());

        if (motsUtiles.size() >= 2)
        {
            String type = motsUtiles.get(motsUtiles.size() - 2);
            String nom  = motsUtiles.get(motsUtiles.size() - 1);
            String visibilite = parts[0]; 
            
            attributs.add(new AttributObjet(nom, estStatique ? "static" : "instance", type, visibilite, estStatique));
        }
    }


    /**
     * Extrait les informations d'une méthode à partir d'une ligne de code.
     *
     * @param ligne ligne du fichier contenant une méthode
     * @param estStatique indique si la méthode est static
     * @param nomClasse nom de la classe analysée (permet de détecter le constructeur)
     * @param methodes liste où ajouter la méthode créée
     */
    public void extraireMethode(String ligne, boolean estStatique, String nomClasse, ArrayList<MethodeObjet> methodes)
    {
        int    indexParenthese = ligne.indexOf('(');
        String avantParenthese = ligne.substring(0, indexParenthese).trim();
        String[] parts = avantParenthese.split("\\s+");
        
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
            typeRetour = null; // constructeur
        }
        else if (motsUtiles.size() >= 2)
        {
            typeRetour = motsUtiles.get(motsUtiles.size() - 2);
        }
        else
        {
            typeRetour = "void";
        }
        
        String contenuParam = ligne.substring(indexParenthese + 1,ligne.lastIndexOf(")")).trim();

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
     * Convertit une liste d'attributs en leur représentation UML sous forme de chaînes.
     *
     * @param attributs liste d'attributs Java
     * @param classe classe associée (nécessite ses méthodes utilitaires)
     * @return liste de chaînes correspondant aux attributs au format UML
     */
    public List<String> convertirAttributs(List<AttributObjet> attributs, ClasseObjet classe)
    {
        List<String> liste = new ArrayList<>();
        for (AttributObjet att : attributs)
        {
            String staticFlag = att.estStatique() ? " {static}" : "";
            // Utiliser la méthode de ClasseObjet pour la visibilité
            char visibilite = classe.changementVisibilite(att.getVisibilite());
            
            String s = visibilite + " " + att.getNom() + " : " + att.getType() + staticFlag; 
            liste.add(s);
        }
        return liste;
    }

    /**
     * Convertit une liste de méthodes en leur représentation UML sous forme textuelle.
     *
     * @param methodes liste de méthodes Java
     * @param classe classe associée (pour la conversion des visibilités/retours/paramètres)
     * @return liste de chaînes UML
     */
    public List<String> convertirMethodes(List<MethodeObjet> methodes, ClasseObjet classe)
    {
        List<String> liste = new ArrayList<>();
        for (MethodeObjet met : methodes)
        {
            String staticFlag = met.estStatique() ? "{static} " : "";
            char visibilite   = classe.changementVisibilite(met.getVisibilite());
            String params     = classe.affichageParametre(met.getParametres());
            String retour     = classe.retourType(met.getRetourType());
            
            String s = visibilite + staticFlag + met.getNom() + params + retour;
            liste.add(s);
        }
        return liste;
    }


    /**
     * Détecte les associations UML basées sur les types d'attributs présents dans les classes.
     *
     * @param classes liste de toutes les classes analysées
     * @param mapClasses dictionnaire nomClasse → ClasseObjet
     * @return liste d'associations UML trouvées
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
                
                // Gestion des collections (List<T>, Set<T>, tableaux, etc.)
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
     * Retourne la liste des fichiers Java contenus dans un dossier donné.
     *
     * @param cheminDossier chemin absolu ou relatif du dossier à analyser
     * @return liste des fichiers .java présents dans le dossier
     */
    public ArrayList<File> ClassesDuDossier(String cheminDossier)
    {
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