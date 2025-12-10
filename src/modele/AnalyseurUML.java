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
 * Classe utilitaire responsable de l'analyse syntaxique (parsing) manuelle des fichiers Java.
 * Gère l'extraction des membres (attributs/méthodes) et la détection des relations (héritage/implémentation).
 */
public class AnalyseurUML
{
    private static final int MULT_INDEFINIE = 999999999;
    
    // Stocke l'héritage sous forme de paire (Nom Enfant, Nom Parent)
	private ArrayList<String[]> lstIntentionHeritage = new ArrayList<>(); 
    // Stocke l'implémentation (Nom Classe, Nom Interface)
    private ArrayList<String[]> lstInterfaces = new ArrayList<>();

    /**
     * Réinitialise les listes de relations stockées.
     */
    public void resetRelations()
    {
        this.lstIntentionHeritage.clear();
        this.lstInterfaces.clear(); 
    }

    /**
     * Getter pour les intentions d'héritage (Noms des classes seulement).
     */
    public ArrayList<String[]> getIntentionsHeritage()
    {
        return lstIntentionHeritage;
    }

    /**
     * Getter pour les intentions d'implémentation.
     */
    public ArrayList<String[]> getInterfaces()
    {
        return lstInterfaces;
    }

    /**
     * Analyse un fichier Java et construit l'objet ClasseObjet correspondant.
     * Gère la détection de l'entité (Classe, Interface, Record, Enum) et de ses membres.
     */
    public ClasseObjet analyserFichierUnique(String chemin)
    {
        File file = new File(chemin);
        String nomClasse = file.getName().replace(".java", "");
        
        // Variables pour l'entité en cours
        ArrayList<AttributObjet> attributs = new ArrayList<>();
        ArrayList<MethodeObjet> methodes = new ArrayList<>();
        
        // Variables de contrôle de la structure
        String nomEntite = nomClasse; 
        String specifique = ""; 
        boolean estInterface = false;
        boolean enTeteTrouve = false;
        
        // Variables de relation
        boolean estHeritier = false;
        String nomParent = null;
        ArrayList<String> interfacesDetectees = new ArrayList<>();
        
        String[] tabSpecifique = {"abstract class", "interface", "enum", "record"}; 

        try (Scanner sc = new Scanner(file))
        {
            while (sc.hasNextLine())
            {
                String ligne = sc.nextLine().trim();

                if (ligne.isEmpty() || ligne.startsWith("package") || ligne.startsWith("import"))
                {
                    continue;
                }
                
                if (ligne.equals("}"))
                {
                    continue;
                }

                // --- 1. DÉTECTION DE L'EN-TÊTE ET DE LA STRUCTURE ---
                
                if (!enTeteTrouve && (ligne.contains(" class ") || ligne.contains(" interface ") || ligne.contains(" record ") || ligne.contains(" enum ")))
                {
                    enTeteTrouve = true;
                    
                    // Détecter le stéréotype (le plus spécifique d'abord)
                    for (String motCle : tabSpecifique)
                    {
                        if (ligne.contains(motCle))
                        {
                            specifique = motCle;
                            if (motCle.contains("interface")) estInterface = true;
                            
                            // Si c'est une interface, extraire son nom réel
                            if (estInterface) {
                                int indexDebutNom = ligne.indexOf("interface") + "interface".length();
                                int indexFinNom = ligne.indexOf('{', indexDebutNom);
                                if (indexFinNom == -1) indexFinNom = ligne.length();
                                
                                String declaration = ligne.substring(indexDebutNom, indexFinNom).trim();
                                nomEntite = declaration.split("\\s+")[0];
                            }
                            break;
                        }
                    }

                    // Détection extends (Héritage)
                    if (ligne.contains("extends"))
                    {
                        estHeritier = true;

                        int indexDebut = ligne.indexOf("extends") + "extends".length();
                        int indexFin = ligne.contains("implements") ? ligne.indexOf("implements") : ligne.indexOf('{');
                        if (indexFin == -1) indexFin = ligne.length();
                        
                        String afterExtends = ligne.substring(indexDebut, indexFin).trim();
                        nomParent = afterExtends.split("\\s+")[0].trim();
                    }

                    // Détection implements (Implémentation d'Interface)
                    if (ligne.contains("implements"))
                    {
                        int indexImplements = ligne.indexOf("implements") + "implements".length();
                        String afterImplements = ligne.substring(indexImplements).trim();
                        
                        int indexAccolade = afterImplements.indexOf('{');
                        if (indexAccolade != -1)
                        {
                            afterImplements = afterImplements.substring(0, indexAccolade).trim();
                        }
                        
                        String[] parts = afterImplements.split(",");
                        for (String p : parts)
                        {
                            String inter = p.trim();
                            int idxSpace = inter.indexOf(' ');
                            int idxChevron = inter.indexOf('<');
                            int idxFin = inter.length();
                            
                            if (idxSpace != -1) idxFin = Math.min(idxFin, idxSpace);
                            if (idxChevron != -1) idxFin = Math.min(idxFin, idxChevron);

                            inter = inter.substring(0, idxFin).trim();
                            if (!inter.isEmpty())
                            {
                                interfacesDetectees.add(inter);
                            }
                        }
                    }
                    
                    // Traitement spécifique des RECORDS
                    if (specifique.equals("record"))
                    {
                        int debut = ligne.indexOf('(');
                        int fin = ligne.lastIndexOf(')');

                        if (debut != -1 && fin != -1 && debut < fin)
                        {
                            // Logique de parsing du record header pour remplir attributs/methodes
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
                    }
                    
                    continue; // Passer la ligne d'en-tête
                } 
                
                // --- 2. DÉTECTION DES MEMBRES DANS LE CORPS (Après la ligne d'en-tête) ---
                
                if (enTeteTrouve)
                {
                    boolean estStatique = ligne.contains("static");
                    boolean aVisibilite = (ligne.startsWith("public") || ligne.startsWith("private") || ligne.startsWith("protected"));

                    // Extraction des Attributs (non applicable aux interfaces/records)
                    if (!estInterface && !specifique.equals("record") && aVisibilite && ligne.endsWith(";") && !ligne.contains("("))
                    {
                        extraireAttribut(ligne, estStatique, attributs);
                    }
                    
                    // Extraction des Méthodes (Applicable à TOUTES les structures, y compris les interfaces)
                    if (aVisibilite && ligne.contains("(") && !ligne.contains("class "))
                    {
                        // Si interface, nomEntite est le nom de l'interface, sinon nomClasse
                        String nomCible = estInterface ? nomEntite : nomClasse;
                        
                        // Si la ligne se termine par un point-virgule (méthode abstraite/interface) ou un corps ouvrant.
                        // On extrait toujours s'il y a une signature (pour capturer les méthodes d'interface qui finissent par ';')
                        if (ligne.contains(")") && (ligne.endsWith(";") || ligne.endsWith("{")))
                        {
                             extraireMethode(ligne, estStatique, nomCible, methodes); 
                        }
                    }
                }
                
            } // Fin de la boucle while
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Fichier non trouvé: " + chemin);
            return null;
        }
        
        // --- 3. CRÉATION DE L'OBJET FINAL ---
        
        // Le nom final est le nom de l'entité (nomEntite si interface/enum/record, sinon nomClasse)
        String nomFinal = nomEntite;

        // Toutes les méthodes et attributs sont dans les listes 'methodes' et 'attributs'
        ClasseObjet nouvelleClasse = new ClasseObjet(attributs, methodes, nomFinal, specifique);
                      
        // Enregistrement des relations pour le Contrôleur
        if (estHeritier && nomParent != null)
        {
            this.lstIntentionHeritage.add(new String[]{nomClasse, nomParent});
        }
        for (String iface : interfacesDetectees)
        {
            this.lstInterfaces.add(new String[]{nomClasse, iface});
        }

        return nouvelleClasse;
    }

    /**
     * Extrait les informations d'un attribut à partir d'une ligne de code.
     */
    public void extraireAttribut(String ligne, boolean estStatique, ArrayList<AttributObjet> attributs)
    {
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
            String type = motsUtiles.get(motsUtiles.size() - 2);
            String nom = motsUtiles.get(motsUtiles.size() - 1);
            String visibilite = parts[0]; 
            
            attributs.add(new AttributObjet(nom, estStatique ? "static" : "instance", type, visibilite, estStatique));
        }
    }
    
    /**
     * Extrait les informations d'une méthode à partir d'une ligne de code.
     */
    public void extraireMethode(String ligne, boolean estStatique, String nomClasse, ArrayList<MethodeObjet> methodes)
    {
        int indexParenthese = ligne.indexOf('(');
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

    // --- Les méthodes convertirAttributs, convertirMethodes, detecterAssociations, ClassesDuDossier sont conservées (inchangées) ---
    
    /**
     * Logique de conversion (non modifiée)
     */
    public List<String> convertirAttributs(List<AttributObjet> attributs, ClasseObjet classe)
    {
        List<String> liste = new ArrayList<>();
        for (AttributObjet att : attributs)
        {
            String staticFlag = att.estStatique() ? " {static}" : "";
            char visibilite = classe.changementVisibilite(att.getVisibilite());
            
            String s = visibilite + " " + att.getNom() + " : " + att.getType() + staticFlag; 
            liste.add(s);
        }
        return liste;
    }

    /**
     * Logique de conversion (non modifiée)
     */
    public List<String> convertirMethodes(List<MethodeObjet> methodes, ClasseObjet classe)
    {
        List<String> liste = new ArrayList<>();
        for (MethodeObjet met : methodes)
        {
            String staticFlag = met.estStatique() ? "{static} " : "";
            char visibilite = classe.changementVisibilite(met.getVisibilite());
            
            String params = classe.affichageParametre(met.getParametres());
            String retour = classe.retourType(met.getRetourType());
            
            String s = visibilite + staticFlag + met.getNom() + params + retour;
            liste.add(s);
        }
        return liste;
    }

    /**
     * Logique de détection des associations (non modifiée)
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
     * Logique de recherche de fichiers (non modifiée)
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