package modele.outil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import modele.entites.AttributObjet;
import modele.entites.MethodeObjet;


public final class ParsingUtil
{
    private ParsingUtil() { }

    public static List<String> separerMots(String s) 
    {
        List<String> tokens;
        String       token ;
        char         c     ;
        
        tokens = new ArrayList<String>();
        token  = "";
        
        for (int i = 0; i < s.length(); i++)
        {
            c = s.charAt(i);
            if (Character.isWhitespace(c))
            {
                if (!token.isEmpty())
                {
                    tokens.add(token);
                    token = "";
                }
            }
            else
            {
                token += c;
            }
        }
        if (!token.isEmpty())   tokens.add(token); 
        
        return tokens;
    }

    public static int indexEspace(String s) 
    {
        for (int i = 0; i < s.length(); i++) 
        {
            if (Character.isWhitespace(s.charAt(i))) 
            { 
                return i; 
            }
        }
        return -1;
    }

    public static int dernierIndexEspace(String s) 
    {
        for (int i = s.length() - 1; i >= 0; i--) 
        {
            if (Character.isWhitespace(s.charAt(i)))
            {
                return i;
            }
        }
        return -1;
    }

    public static String identifierStereotype(String ligne) 
    {
        if (ligne.contains("interface")) return "interface";
        if (ligne.contains("record"))    return "record";
        if (ligne.contains("enum"))      return "enum";
        if (ligne.contains("abstract class")) return "abstract class";
        return ""; // Classe standard
    }   

    public static boolean aModifierMotCle(String s) 
    {
        return "public"  .equals(s) || "private".equals(s) || "protected".equals(s) || 
               "static"  .equals(s) || "final"  .equals(s) || "transient".equals(s) || 
               "volatile".equals(s) ;
    }

    public static boolean aMethodeModif(String s) 
    {
        return "public"      .equals(s) || "private".equals(s) || "protected".equals(s) || 
               "static"      .equals(s) || "final"  .equals(s) || "abstract" .equals(s) || 
               "synchronized".equals(s) || "default".equals(s);
    }

    public static boolean aVisibilite(String s) 
    {
        return "public".equals(s) || "private".equals(s) || "protected".equals(s);
    }

    /**
     * Lit le premier identifiant utile dans la chaîne (arrêt sur espace, '{', '<', ',', '(' ).
     */
    public static String lireNom(String s) 
    {        
        String id ;
        char   c  ;

        s = s.trim();
        
        if (s.isEmpty()){ return ""; }

        id = "";
        for (int i = 0; i < s.length(); i++) 
        {
            c = s.charAt(i);
            if (Character.isWhitespace(c) || c == '{' || c == '<' || c == ',' || c == '(') 
            {
                break;
            }
            id += c;
        }
        return id;
    }

    /**
     * Split par virgules au niveau "top-level" en ignorant les virgules à l'intérieur de < >.
     */
    public static List<String> decoupage(String s) 
    {
        List<String> parties ;
        String part        ;
        int cptChevrons    ;
        
        cptChevrons   = 0  ;
        part       = ""    ;
        parties = new ArrayList<String>();

        for (int i = 0; i < s.length(); i++) 
        {
            char c = s.charAt(i);
            if (c == '<') 
            {
                cptChevrons++;
                part += c;
            } 
            else if (c == '>') 
            {
                if (cptChevrons > 0) 
                {
                    cptChevrons--;
                }
                part += c;
            } 
            else if (c == ',' && cptChevrons == 0) 
            {
                parties.add(part);
                part = "";
            } 
            else 
            {
                part += c;
            }
        }

        if (!part.isEmpty()) { parties.add(part); }
        return parties;
    }

    /**
     * Extrait un attribut en nettoyant l'initialisation.
     * Remarques:
     * - Signature identique à la méthode avant refactor (pour compatibilité).
     * - On travaille avec une ArrayList<AttributObjet> fournie par la classe qui l'apelle.
     */
    public static void extraireAttribut(String ligne, boolean estStatique, boolean estFinal, ArrayList<AttributObjet> attributs) 
    {
        
        List<String> parties      ;
        List<String> motsUtiles ;

        String type             ;
        String nom              ;
        String visibilite       ;

        if (ligne.contains("=")) 
        {
            ligne = ligne.substring(0, ligne.indexOf("=")).trim();
        } 
        else 
        {
            ligne = ligne.replace(";", "").trim();
        }

        parties = separerMots(ligne);

        motsUtiles = new ArrayList<String>();

        for (String p : parties) 
        {
            if (!aModifierMotCle(p)) 
            {
                motsUtiles.add(p);
            }
        }

        if (motsUtiles.size() >= 2) 
        {
            type = motsUtiles.get(motsUtiles.size() - 2);
            nom = motsUtiles.get(motsUtiles.size() - 1);

            visibilite = parties.size() > 0 ? parties.get(0) : "";

            if (!aVisibilite(visibilite)) { visibilite = "public"; }

            attributs.add(new AttributObjet(nom, estStatique ? "static" : "instance", type, visibilite, estStatique, estFinal));
        }
    }

    /**
     * Extrait une méthode ou un constructeur.
     * Remarques:
     * - Signature identique à l’originale (pour compatibilité) :
     *   extraireMethode(String, boolean, String, ArrayList<MethodeObjet>)
     * - Retourne void et ajoute la méthode dans la liste fournie.
     */
    public static void extraireMethode(String ligne, boolean estStatique, String nomClasse, ArrayList<MethodeObjet> methodes) 
    {
        int       idxParenthOuvrante  ;
        int       idxParenthFermante  ;
        int       spaceIndex          ;

        List<String>       parties    ;
        List<String>       motsUtiles ;
        List<String>       paramList  ;

        HashMap<String, String> params;
        
        String             signature  ;
        String             visibilite ;
        String             nomMethode ;
        String             typeRetour ;
        String             paramsStr  ;
        String             p          ;
        String             pType      ;
        String             pNom       ;

        idxParenthOuvrante = ligne.indexOf('(');
        idxParenthFermante = ligne.lastIndexOf(')');

        if (idxParenthOuvrante == -1 || idxParenthFermante == -1) return;

        signature = ligne.substring(0, idxParenthOuvrante).trim();
        parties   = separerMots(signature);

        if (parties.isEmpty()) return;

        visibilite = parties.get(0);
        nomMethode = parties.get(parties.size() - 1);
        typeRetour = "void";

        if (nomMethode.equals(nomClasse)) 
        {
            typeRetour = null; // Constructeur
        } 
        else 
        {
            motsUtiles = new ArrayList<String>();
            for (String par : parties) 
            {
                if (!aMethodeModif(par)) 
                {
                    motsUtiles.add(par);
                }
            }
            if (motsUtiles.size() >= 2) 
            {
                typeRetour = motsUtiles.get(motsUtiles.size() - 2);
            }
        }

        // --- EXTRACTION DES PARAMETRES (parsing manuel) ---
        paramsStr = ligne.substring(idxParenthOuvrante + 1, idxParenthFermante).trim();
        params    = new HashMap<String, String>();

        if (!paramsStr.isEmpty()) 
        {
            paramList = decoupage(paramsStr);

            for (String param : paramList) 
            {
                p = param.trim();
                if (!p.isEmpty()) 
                {
                    spaceIndex = dernierIndexEspace(p);
                    if (spaceIndex > 0) 
                    {
                        pType = p.substring(0, spaceIndex ).trim();
                        pNom  = p.substring(spaceIndex + 1).trim();
                        params.put(pNom, pType);
                    }
                }
            }
        }

        methodes.add(new MethodeObjet(nomMethode, params, typeRetour, visibilite, estStatique));
    }
}