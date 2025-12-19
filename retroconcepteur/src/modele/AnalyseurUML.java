package modele;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
 
 */
public class AnalyseurUML 
{
    /* -------------------------------------------------------------------------- */
    /* ATTRIBUTS                                 */
    /* -------------------------------------------------------------------------- */
    
    private static final int MULT_INDEFINIE = 999999999;

    private HashMap<String, String> lstIntentionHeritage;
    private HashMap<String, ArrayList<String>> lstInterfaces;

    /* -------------------------------------------------------------------------- */
    /* CONSTRUCTEUR                                */
    /* -------------------------------------------------------------------------- */

    public AnalyseurUML() 
    {
        this.lstIntentionHeritage = new HashMap<>();
        this.lstInterfaces = new HashMap<>();
    }

    /* -------------------------------------------------------------------------- */
    /* MÉTHODES D'ACCÈS                              */
    /* -------------------------------------------------------------------------- */

    public HashMap<String, String> getIntentionsHeritage() 
    {
        return this.lstIntentionHeritage;
    }

    public HashMap<String, ArrayList<String>> getInterfaces() 
    {
        return this.lstInterfaces;
    }

    /* -------------------------------------------------------------------------- */
    /* MÉTHODES DE GESTION                            */
    /* -------------------------------------------------------------------------- */

    public void resetRelations() 
    {
        this.lstIntentionHeritage.clear();
        this.lstInterfaces.clear();
    }

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
                
                if (ligneBrute.isEmpty() || ligneBrute.startsWith("//") || 
                    ligneBrute.startsWith("package") || ligneBrute.startsWith("import")) 
                {
                    continue;
                }

                // 1. Détection de déclaration (Class, Interface, Record, Enum)
                String stereotype = ParsingUtil.identifierStereotype(ligneBrute);
                
                if (ligneBrute.contains("class ") || ligneBrute.contains("interface ") || 
                    ligneBrute.contains("record ") || ligneBrute.contains("enum ")) 
                {
                    String nomEntite = extraireNomEntite(ligneBrute);
                    
                    if (nomEntite.isEmpty()) 
                    {
                        nomEntite = nomFichier;
                    }

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

                    // Résolution de l'héritage (extends)
                    int idxExtends = ligneBrute.indexOf("extends");
                    if (idxExtends != -1) 
                    {
                        String parent = ParsingUtil.lireNom(ligneBrute.substring(idxExtends + 7).trim());
                        this.lstIntentionHeritage.put(nomEntite, parent);
                    }

                    int idxImplements = ligneBrute.indexOf(" implements ");
                    if (idxImplements != -1) 
                    {
                        extraireInterfaces(nomEntite, ligneBrute, idxImplements);
                    }

                    if (ligneBrute.contains("{")) 
                    {
                        niveauAccolades++;
                    }
                    continue;
                }

                // 2. Gestion des membres (Attributs et Méthodes)
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

                // 3. Gestion des accolades
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

    /**
     * Parcourt la ligne manuellement pour trouver le nom après le mot-clé (class, interface, etc.)
     */
    private String extraireNomEntite(String ligne) 
    {
        String[] motsCles = {"class", "interface", "record", "enum"};
        
        for (String mc : motsCles) 
        {
            int idx = ligne.indexOf(mc + " ");
            if (idx != -1) 
            {
                return ParsingUtil.lireNom(ligne.substring(idx + mc.length() + 1).trim());
            }
        }
        return "";
    }

    private void extraireInterfaces(String nomEntite, String ligne, int idxImplements) 
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
    /* RÉSOLUTION DES RELATIONS                           */
    /* -------------------------------------------------------------------------- */

    public List<AssociationObjet> detecterAssociations(List<ClasseObjet> classes, HashMap<String, ClasseObjet> mapClasses) 
    {
        List<AssociationObjet> associationsFinales = new ArrayList<>();
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

                if (typeAttribut.contains("<") && typeAttribut.contains(">")) 
                {
                    int idx1 = typeAttribut.indexOf('<');
                    int idx2 = typeAttribut.indexOf('>', idx1 + 1);
                    
                    if (idx1 != -1 && idx2 != -1) 
                    {
                        typeCible = typeAttribut.substring(idx1 + 1, idx2).trim();
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
        
        for (String enfant : this.lstIntentionHeritage.keySet()) 
        {
            String parent = this.lstIntentionHeritage.get(enfant);
            
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
        
        for (String nomClasse : this.lstInterfaces.keySet()) 
        {
            if (!mapClasses.containsKey(nomClasse)) 
            {
                continue;
            }
            
            ClasseObjet classeConcrète = mapClasses.get(nomClasse);
            InterfaceObjet io = new InterfaceObjet(classeConcrète);
            
            for (String nomI : this.lstInterfaces.get(nomClasse)) 
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
    /* OUTILS ET UTILITAIRES                          */
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

    public List<File> ClassesDuDossier(String cheminDossier) 
    {
        File dossier = new File(cheminDossier);
        File[] tousLesFichiers = dossier.listFiles();
        List<File> fichiersJava = new ArrayList<>();

        if (tousLesFichiers != null) 
        {
            for (File f : tousLesFichiers) 
            {
                // On ne prend que les fichiers .java
                if (f.isFile() && f.getName().endsWith(".java")) 
                {
                    fichiersJava.add(f);
                }
            }
        }
        return fichiersJava;
    }
}