package modele.outil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import modele.entites.AttributObjet;
import modele.entites.MethodeObjet;

public final class ParsingUtil
{
    /* -------------------------------------- */
    /* CONSTRUCTEUR (Privé)                   */
    /* -------------------------------------- */
    private ParsingUtil() 
    { 
    }

    /* -------------------------------------- */
    /* MÉTHODES D'ANALYSE                     */
    /* -------------------------------------- */

    public static String identifierStereotype(String ligne) 
    {
        if (ligne.contains("interface"     )) return "interface";
        if (ligne.contains("record"        )) return "record";
        if (ligne.contains("enum"          )) return "enum";
        if (ligne.contains("abstract class")) return "abstract class";

        return ""; 
    }   

    public static String lireNom(String s) 
    {        
        if (s == null) return "";

        s = s.trim();
        
        // Si la chaîne commence par un guillemet, ce n'est pas un nom de classe valide
        if (s.isEmpty() || s.startsWith("\"") || s.startsWith("'")) return "";

        String id = "";
        for (int i = 0; i < s.length(); i++) 
        {
            char c = s.charAt(i);
            // On arrête au premier délimiteur Java
            if (Character.isWhitespace(c) || c == '{' || c == '<' || c == ',' 
                                          || c == '(' || c == ')' || c == ';' ) 
            {
                break;
            }

            id += c;
        }
        
        // Nettoyage final pour enlever d'éventuels guillemets résiduels
        return id.replace("\"", "").replace("'", "");
    }

    public static void extraireAttribut(String ligne, boolean estStatique, boolean estFinal, 
                                        ArrayList<AttributObjet> attributs                  ) 
    {
        int finIdx = ligne.indexOf('=');

        if (finIdx == -1) finIdx = ligne.indexOf(';');
        
        String declaration = (finIdx != -1) ? ligne.substring(0, finIdx).trim() : ligne.trim();

        int dernierEspace = declaration.lastIndexOf(' ');

        if (dernierEspace != -1)
        {
            String nom   = declaration.substring(dernierEspace + 1).trim();
            String reste = declaration.substring(0, dernierEspace).trim();

            int avantDernierEspace = reste.lastIndexOf(' ');
            String type = (avantDernierEspace != -1) ? reste.substring(avantDernierEspace + 1).trim() : reste;

            String visibilite = "public";

            if (ligne.contains("private"  )) visibilite = "private";
            if (ligne.contains("protected")) visibilite = "protected";

            attributs.add(new AttributObjet(nom, estStatique ? "static" : "instance", type, visibilite, estStatique, estFinal));
        }
    }

    public static void extraireMethode(String ligne, boolean estStatique, String nomClasse, ArrayList<MethodeObjet> methodes) 
    {
        int parenOuvrante = ligne.indexOf('(');
        int parenFermante = ligne.lastIndexOf(')');

        if (parenOuvrante == -1 || parenFermante == -1) return;

        String signature  = ligne.substring(0, parenOuvrante).trim();
        int dernierEspace = signature.lastIndexOf(' ');
        
        if (dernierEspace != -1)
        {
            String nomMethode = signature.substring(dernierEspace + 1).trim();
            String reste = signature.substring(0, dernierEspace).trim();
            
            String typeRetour = "void";

            if (nomMethode.equals(nomClasse)) 
                typeRetour = null; 

            else
            {
                int typeEspace = reste.lastIndexOf(' ');
                typeRetour = (typeEspace != -1) ? reste.substring(typeEspace + 1).trim() : reste;
            }

            String visibilite = "public";

            if (ligne.contains("private"  )) visibilite = "private";
            if (ligne.contains("protected")) visibilite = "protected";

            HashMap<String, String> params = new HashMap<>();

            String paramsStr = ligne.substring(parenOuvrante + 1, parenFermante).trim();
            
            if (!paramsStr.isEmpty())
            {
                List<String> listeParamRaw = decoupage(paramsStr);

                for (String p : listeParamRaw)
                {
                    String pTrim = p.trim();
                    int    sep   = pTrim.lastIndexOf(' ');

                    if (sep != -1)
                        params.put(pTrim.substring(sep + 1).trim(), pTrim.substring(0, sep).trim());
                }
            }
            methodes.add(new MethodeObjet(nomMethode, params, typeRetour, visibilite, estStatique));
        }
    }

    public static List<String> decoupage(String s) 
    {
        List<String> parties = new ArrayList<>();
        String part = "";
        int cptChevrons = 0;

        for (int i = 0; i < s.length(); i++) 
        {
            char c = s.charAt(i);

                 if (c == '<')                     { cptChevrons++; part += c; } 
            else if (c == '>')                     { if (cptChevrons > 0) cptChevrons--; part += c; } 
            else if (c == ',' && cptChevrons == 0) { parties.add(part.trim()); part = ""; } 
            else { part += c; }
        }

        if (!part.isEmpty()) parties.add(part.trim());

        return parties;
    }

    public static String nettoyerLigne(String ligne) 
    {
        if (ligne == null) return "";

        int idxCommentaire = ligne.indexOf("//");

        if (idxCommentaire != -1) ligne = ligne.substring(0, idxCommentaire);
        return ligne.trim();
    }

    /**
     * Extrait le type "simple" (enleve les génériques et tableaux).
     * Règle la redondance entre AnalyseurUML et PresentationMapper.
     */
    public static String extraireTypeSimple(String type) 
    {
        if (type == null) return "";

        String typeSimple = type;

        if (type.contains("<") && type.contains(">")) 
        {
            int idx1   = type.indexOf    ('<');
            int idx2   = type.lastIndexOf('>');
            typeSimple = type.substring(idx1 + 1, idx2).trim();
            
            // Gestion des Maps (ex: HashMap<String, Carre>) -> on prend le dernier type
            int virgule = typeSimple.lastIndexOf(',');

            if (virgule != -1) typeSimple = typeSimple.substring(virgule + 1).trim();

        }
        else if (type.endsWith("[]")) 
            typeSimple = type.substring(0, type.length() - 2).trim();

        return typeSimple;
    }

    public static String nettoyer(String ligne) 
    {
        if (ligne == null) return "";
        
        // 1. Suppression des commentaires de fin de ligne
        int idxCommentaire = ligne.indexOf("//");
        
        if (idxCommentaire != -1) 
            ligne = ligne.substring(0, idxCommentaire);
        
        // 2. Neutralisation des String (Remplace "contenu" par "")
        // Utilise une regex non-gourmande pour vider chaque guillemet
        ligne = ligne.replaceAll("\".*?\"", "\"\""); 

        return ligne.trim();
    }

    /**
     * Vérifie si une ligne est une déclaration de membre (attribut ou méthode).
     */
    public static boolean estMembre(String ligne) 
    {
        return (ligne.endsWith(";") || ligne.contains("(")) && !ligne.contains("class ");
    }
}