package modele;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

import modele.entites.AssociationObjet;
import modele.entites.AttributObjet;
import modele.entites.ClasseObjet;
import modele.entites.HeritageObjet;
import modele.entites.InterfaceObjet;
import modele.entites.LiaisonObjet;
import modele.entites.MultipliciteObjet;
import modele.outil.ParsingUtil;

/**
 * Classe responsable de l'analyse syntaxique (parsing) manuelle des fichiers Java.
 * Optimisée pour éviter l'usage de split() et gérer proprement les collections.
 */
public class AnalyseurUML 
{
    /* -------------------------------------------------------------------------- */
    /* ATTRIBUTS                                                                  */
    /* -------------------------------------------------------------------------- */
    
    private static final int MULT_INDEFINIE = 999999999;

    private HashMap<String,String> lstIntentionHeritage;
    private HashMap<String,ArrayList<String>> lstInterfaces;

    /* -------------------------------------------------------------------------- */
    /* CONSTRUCTEUR                                                               */
    /* -------------------------------------------------------------------------- */

    public AnalyseurUML() 
    {
        this.lstIntentionHeritage = new HashMap<>();
        this.lstInterfaces = new HashMap<>();
    }

    /* -------------------------------------------------------------------------- */
    /* MÉTHODES D'ACCÈS                                                           */
    /* -------------------------------------------------------------------------- */

    public HashMap<String,String> getIntentionsHeritage() 
    {
        return this.lstIntentionHeritage;
    }

    public HashMap<String,ArrayList<String>> getInterfaces() 
    {
        return this.lstInterfaces;
    }

    /* -------------------------------------------------------------------------- */
    /* MÉTHODES DE GESTION ET PARSING                                             */
    /* -------------------------------------------------------------------------- */

    public void resetRelations() 
    {
        this.lstIntentionHeritage.clear();
        this.lstInterfaces.clear();
    }

    /**
     * Analyse un fichier Java sans utiliser split() pour le parcours des données.
     */
    public ClasseObjet analyserFichierUnique(String chemin) 
    {
        File file = new File(chemin);
        String nomFichier = file.getName().replace(".java", "");

        Stack<ClasseObjet> pileClasses = new Stack<>();
        ClasseObjet classeRacine = null;
        int niveauAccolades = 0;

        try (Scanner sc = new Scanner(file)) 
        {
            while (sc.hasNextLine()) 
            {
                String ligneBrute = sc.nextLine().trim();
                
                int idxCommentaire = ligneBrute.indexOf("//");
                if (idxCommentaire != -1) 
                {
                    ligneBrute = ligneBrute.substring(0, idxCommentaire).trim();
                }

                // 2. Ignorer les lignes vides ou purement package/import après nettoyage
                if (ligneBrute.isEmpty() || ligneBrute.startsWith("package") || ligneBrute.startsWith("import")) 
                {
                    continue;
                }

                // 3. Ignorer les lignes qui ne sont que des commentaires Javadoc/Multi-lignes
                if (ligneBrute.startsWith("/*") || ligneBrute.startsWith("*") || ligneBrute.endsWith("*/"))
                {
                    continue;
                }

                // Dans AnalyseurUML.java

                // 1. Détection de déclaration (Class, Interface, Record, Enum)
                String stereotype = ParsingUtil.identifierStereotype(ligneBrute);

                if ((ligneBrute.contains("class ") || ligneBrute.contains("interface ") || 
                    ligneBrute.contains("record ") || ligneBrute.contains("enum ")) && !estDansGuillemets(ligneBrute, ligneBrute.indexOf("class"))) 
                {
                    // Au lieu de split, on cherche le mot après le mot-clé
                    String nomEntite = extraireNomEntiteOptimise(ligneBrute);
                    
                    if (nomEntite.isEmpty() || nomEntite.equals("\"")) continue; // Sécurité anti-guillemet

                    ClasseObjet nouvelleClasse = new ClasseObjet(new ArrayList<>(), new ArrayList<>(), nomEntite, stereotype);

                    if (classeRacine == null) 
                    {
                        classeRacine = nouvelleClasse;
                    } 
                    else if (!pileClasses.isEmpty()) 
                    {
                        pileClasses.peek().ajouterClasseInterne(nouvelleClasse);
                    }

                    pileClasses.push(nouvelleClasse);

                    // Héritage
                    int idxExtends = ligneBrute.indexOf("extends");
                    if (idxExtends != -1) 
                    {
                        String parent = ParsingUtil.lireNom(ligneBrute.substring(idxExtends + 7).trim());
                        this.lstIntentionHeritage.put(nomEntite, parent);
                    }

                    // Interfaces (Optimisé sans split)
                    int idxImplements = ligneBrute.indexOf(" implements ");
                    if (idxImplements != -1) 
                    {
                        extraireInterfacesSansSplit(nomEntite, ligneBrute, idxImplements);
                    }

                    if (ligneBrute.contains("{")) 
                    {
                        niveauAccolades++;
                    }
                    continue;
                }

                // Membres
                if (!pileClasses.isEmpty() && niveauAccolades == pileClasses.size()) 
                {
                    ClasseObjet classeCourante = pileClasses.peek();
                    boolean estStatique = ligneBrute.contains("static");
                    boolean estFinal = ligneBrute.contains("final");

                    if (ligneBrute.endsWith(";") && !ligneBrute.contains("(")) 
                    {
                        ParsingUtil.extraireAttribut(ligneBrute, estStatique, estFinal, classeCourante.getAttributs());
                    } 
                    else if (ligneBrute.contains("(") && !ligneBrute.contains("=")) 
                    {
                        String lignePourMethode = ligneBrute;
                        
                        if (!ligneBrute.contains("public") && "interface".equals(classeCourante.getSpecifique())) 
                        {
                            lignePourMethode = "public " + ligneBrute;
                        }
                        
                        ParsingUtil.extraireMethode(lignePourMethode, estStatique, classeCourante.getNom(), classeCourante.getMethodes());
                    }
                }

                if (ligneBrute.contains("{")) 
                {
                    niveauAccolades++;
                }
                
                if (ligneBrute.contains("}")) 
                {
                    if (niveauAccolades == pileClasses.size() && !pileClasses.isEmpty()) 
                    {
                        pileClasses.pop();
                    }
                    niveauAccolades--;
                }
            }
        } 
        catch (FileNotFoundException e) 
        {
            return null;
        }

        return classeRacine;
    }

    private String extraireNomEntite(String ligne) 
    {
        String[] motsCles = {"class", "interface", "record", "enum"};
        
        for (String mc : motsCles) 
        {
            int idx = ligne.indexOf(mc + " ");
            if (idx != -1) 
            {
                // Correction : On s'assure de ne pas prendre ce qui précède (comme une parenthèse)
                String apresMotCle = ligne.substring(idx + mc.length() + 1).trim();
                
                // On utilise lireNom qui doit ignorer les caractères spéciaux
                return ParsingUtil.lireNom(apresMotCle);
            }
        }
        return "";
    }

    private void extraireInterfacesSansSplit(String nomEntite, String ligne, int idxImplements) 
    {
        int start = idxImplements + 12;
        int end = ligne.indexOf('{', start);
        
        if (end == -1) 
        {
            end = ligne.length();
        }

        String segment = ligne.substring(start, end);
        ArrayList<String> interfaces = new ArrayList<>();
        int currentPos = 0;
        int nextComma;

        while ((nextComma = segment.indexOf(',', currentPos)) != -1) 
        {
            String inter = segment.substring(currentPos, nextComma).trim();
            ajouterInterfaceNettoyee(interfaces, inter);
            currentPos = nextComma + 1;
        }
        
        ajouterInterfaceNettoyee(interfaces, segment.substring(currentPos).trim());

        this.lstInterfaces.put(nomEntite, interfaces);
    }

    private void ajouterInterfaceNettoyee(ArrayList<String> liste, String nomRaw) 
    {
        if (nomRaw.isEmpty()) 
        {
            return;
        }
        
        int idxChevron = nomRaw.indexOf('<');
        String nomPropre = (idxChevron != -1) ? nomRaw.substring(0, idxChevron).trim() : nomRaw;
        
        if (!nomPropre.isEmpty()) 
        {
            liste.add(nomPropre);
        }
    }

    /* -------------------------------------------------------------------------- */
    /* RÉSOLUTION DES RELATIONS ET COLLECTIONS                                    */
    /* -------------------------------------------------------------------------- */

    public List<AssociationObjet> detecterAssociations(List<ClasseObjet> classes, HashMap<String, ClasseObjet> mapClasses) 
    {
        List<AssociationObjet> unidirectionnelles = new ArrayList<>();

        for (ClasseObjet classeOrigine : classes) 
        {
            if (classeOrigine.getAttributs() == null) 
            {
                continue;
            }

            for (AttributObjet attribut : classeOrigine.getAttributs()) 
            {
                String typeAttribut = attribut.getType();
                String typeCible = typeAttribut;
                MultipliciteObjet multCible = new MultipliciteObjet(1, 1);
                boolean estCollection = false;

                // Gestion propre des collections (ArrayList, HashMap, Tableaux) sans split
                if (typeAttribut.contains("<") && typeAttribut.contains(">")) 
                {
                    int idx1 = typeAttribut.indexOf('<');
                    int idx2 = typeAttribut.lastIndexOf('>'); // On prend le dernier chevron pour les imbrications
                    
                    if (idx1 != -1 && idx2 != -1) 
                    {
                        typeCible = typeAttribut.substring(idx1 + 1, idx2).trim();
                        // Pour les HashMap, on nettoie si plusieurs types sont présents
                        int virguleGenerique = typeCible.indexOf(',');
                        if (virguleGenerique != -1)
                        {
                            typeCible = typeCible.substring(virguleGenerique + 1).trim();
                        }
                    }
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
                    unidirectionnelles.add(new AssociationObjet(mapClasses.get(typeCible), classeOrigine, multCible, new MultipliciteObjet(1, 1), attribut.getNom(), true));
                }
            }
        }
        
        return unidirectionnelles; 
    }

    public List<HeritageObjet> resoudreHeritage(HashMap<String, ClasseObjet> mapClasses) 
    {
        List<HeritageObjet> resultat = new ArrayList<>();
        
        // Parcours propre de la Map d'intentions
        for (Map.Entry<String, String> entree : this.lstIntentionHeritage.entrySet()) 
        {
            String enfant = entree.getKey();
            String parent = entree.getValue();
            
            if (mapClasses.containsKey(enfant) && mapClasses.containsKey(parent)) 
            {
                resultat.add(new HeritageObjet(mapClasses.get(parent), mapClasses.get(enfant)));
            }
        }
        return resultat;
    }

    public List<InterfaceObjet> resoudreImplementation(HashMap<String, ClasseObjet> mapClasses) 
    {
        HashMap<String, InterfaceObjet> regroupement = new HashMap<>();
        
        for (Map.Entry<String, ArrayList<String>> entree : this.lstInterfaces.entrySet()) 
        {
            String nomClasse = entree.getKey();
            if (!mapClasses.containsKey(nomClasse)) continue;
            
            ClasseObjet classeConcrète = mapClasses.get(nomClasse);
            InterfaceObjet io = new InterfaceObjet(classeConcrète);
            
            for (String nomI : entree.getValue()) 
            {
                if (mapClasses.containsKey(nomI)) 
                {
                    io.ajouterInterface(mapClasses.get(nomI));
                }
            }
            regroupement.put(nomClasse, io);
        }
        return new ArrayList<>(regroupement.values());
    }

    /* -------------------------------------------------------------------------- */
    /* OUTILS                                                                     */
    /* -------------------------------------------------------------------------- */

    public void renumeroterLiaisonsFinales(List<LiaisonObjet> toutes) 
    {
        LiaisonObjet.reinitialiserCompteur();
        int i = 1;
        
        for (LiaisonObjet l : toutes) 
        {
            l.setNum(i++);
        }
    }

    private boolean estDansGuillemets(String ligne, int idx) 
    {
        if (ligne == null || idx <= 0) return false;
        int count = 0;
        for (int i = 0; i < Math.min(idx, ligne.length()); i++) 
        {
            if (ligne.charAt(i) == '"') count++;
        }
        return (count % 2) == 1; // Si le nombre est impair, on est dans un String
    }

    private String extraireNomEntiteOptimise(String ligne) 
    {
        String[] motsCles = {"class", "interface", "record", "enum"};
        for (String mc : motsCles) 
        {
            int idx = ligne.indexOf(mc + " ");
            if (idx != -1) 
            {
                // On prend ce qui suit le mot-clé et on délègue le nettoyage à lireNom
                return ParsingUtil.lireNom(ligne.substring(idx + mc.length() + 1).trim());
            }
        }
        return "";
    }

    public List<File> ClassesDuDossier(String cheminDossier) 
    {
        File dossier = new File(cheminDossier);
        File[] tousLesFichiers = dossier.listFiles();
        List<File> fichiersJava = new ArrayList<>();

        if (tousLesFichiers != null) 
        {
            for (File f : tousLesFichiers) 
            {
                if (f.isFile() && f.getName().endsWith(".java")) 
                {
                    fichiersJava.add(f);
                }
            }
        }
        return fichiersJava;
    }
}