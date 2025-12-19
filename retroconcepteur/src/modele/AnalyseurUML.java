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
 * Classe responsable de l'analyse syntaxique (parsing) des fichiers Java.
 */
public class AnalyseurUML 
{
    private HashMap<String,String> lstIntentionHeritage;
    private HashMap<String,ArrayList<String>> lstInterfaces;

    public AnalyseurUML() 
    {
        this.lstIntentionHeritage = new HashMap<>();
        this.lstInterfaces = new HashMap<>();
    }

    public HashMap<String,String           > getIntentionsHeritage() { return this.lstIntentionHeritage; }
    public HashMap<String,ArrayList<String>> getInterfaces        () { return this.lstInterfaces; }

    public void resetRelations() 
    {
        this.lstIntentionHeritage.clear();
        this.lstInterfaces.clear();
    }

    public ClasseObjet analyserFichierUnique(String chemin) 
    {
        File file = new File(chemin);
        Stack<ClasseObjet> pileClasses = new Stack<>();
        ClasseObjet classeRacine = null;
        int niveauAccolades = 0;

        try (Scanner sc = new Scanner(file)) 
        {
            while (sc.hasNextLine()) 
            {
                // ÉTAPE 1 : Nettoyage centralisé (Commentaires + Strings neutralisés)
                String ligneBrute = ParsingUtil.nettoyer(sc.nextLine());

                // Filtrage des lignes sans intérêt structurel
                if (ligneBrute.isEmpty() || ligneBrute.startsWith("package") || 
                    ligneBrute.startsWith("import") || ligneBrute.startsWith("/*") || 
                    ligneBrute.startsWith("*") || ligneBrute.endsWith("*/")) 
                {
                    continue;
                }

                // ÉTAPE 2 : Détection des déclarations (uniquement hors Strings)
                String stereotype = ParsingUtil.identifierStereotype(ligneBrute);
                boolean estDeclaration = (ligneBrute.contains("class ") || ligneBrute.contains("interface ") || 
                                        ligneBrute.contains("record ") || ligneBrute.contains("enum "));

                if (estDeclaration) 
                {
                    String nomEntite = extraireNomEntite(ligneBrute);
                    if (nomEntite.isEmpty()) continue;

                    ClasseObjet nouvelleClasse = new ClasseObjet(new ArrayList<>(), new ArrayList<>(), nomEntite, stereotype);

                    // Gestion de la hiérarchie (classes internes)
                    if (classeRacine == null) classeRacine = nouvelleClasse;
                    else if (!pileClasses.isEmpty()) pileClasses.peek().ajouterClasseInterne(nouvelleClasse);
                    
                    pileClasses.push(nouvelleClasse);

                    // Cas spécifique Record
                    if ("record".equals(stereotype)) 
                    {
                        int startP = ligneBrute.indexOf('(');
                        int endP = ligneBrute.lastIndexOf(')');

                        if (startP != -1 && endP > startP) 
                        {
                            String composants = ligneBrute.substring(startP + 1, endP);
                            for (String comp : composants.split(",")) 
                            {
                                String c = comp.trim(); 
                                if (!c.isEmpty()) 
                                {
                                    int lastSpace = c.lastIndexOf(' ');
                                    if (lastSpace != -1) 
                                    {
                                        String type = c.substring(0, lastSpace).trim();
                                        String nom  = c.substring(lastSpace + 1).trim();
                                        nouvelleClasse.getAttributs().add(new AttributObjet(nom, "instance", type, "private", false, true));
                                    }
                                }
                            }
                        }
                    }

                    // Héritage et Interfaces
                    int idxExtends = ligneBrute.indexOf("extends ");
                    if (idxExtends != -1) 
                    {
                        this.lstIntentionHeritage.put(nomEntite, ParsingUtil.lireNom
                        (ligneBrute.substring(idxExtends + 8)));
                    }

                    int idxImplements = ligneBrute.indexOf(" implements ");
                    if (idxImplements != -1) 
                    { 
                        extraireInterfacesSansSplit(nomEntite, ligneBrute, idxImplements);
                    }

                    // On incrémente si l'accolade est sur la ligne de déclaration
                    if (ligneBrute.contains("{")) niveauAccolades++; continue;
                }

                // ÉTAPE 3 : Analyse des membres (Attributs/Méthodes)
                if (!pileClasses.isEmpty() && niveauAccolades == pileClasses.size()) 
                {
                    ClasseObjet classeCourante = pileClasses.peek();
                    boolean estStatique = ligneBrute.contains("static");
                    boolean estFinal    = ligneBrute.contains("final" );

                    // Constantes Enum
                    if ("enum".equals(classeCourante.getSpecifique())) 
                    {
                        if (!ligneBrute.contains("(") && !ligneBrute.contains("class") && 
                           (ligneBrute.endsWith(",") || ligneBrute.endsWith(";"))) 
                        {
                            String constante = ligneBrute.replace(",", "").replace(";", "").trim();
                            if (!constante.isEmpty() && !constante.contains(" ")) 
                            {
                                classeCourante.getAttributs().add(new AttributObjet(constante, "static", classeCourante.getNom(), "public", true, true));
                                continue;
                            }
                        }
                    }

                    // Attributs et Méthodes classiques
                    if (ligneBrute.endsWith(";") && !ligneBrute.contains("(")) 
                    {
                        ParsingUtil.extraireAttribut(ligneBrute, estStatique, estFinal, classeCourante.getAttributs());
                    } 
                    else if (ligneBrute.contains("(") && !ligneBrute.contains("=")) 
                    {
                        String l = ligneBrute;
                        if (!l.contains("public") && "interface".equals(classeCourante.getSpecifique())) l = "public " + l;
                        ParsingUtil.extraireMethode(l, estStatique, classeCourante.getNom(), classeCourante.getMethodes());
                    }
                }

                // ÉTAPE 4 : Gestion précise de la profondeur
                if (ligneBrute.contains("{")) niveauAccolades++;
                if (ligneBrute.contains("}")) 
                {
                    if (niveauAccolades == pileClasses.size() && !pileClasses.isEmpty()) pileClasses.pop();
                    {
                        niveauAccolades--;
                    }
                }
            }
        } catch (FileNotFoundException e) { return null; }

        return classeRacine;
    }

    /**
     * Correction : Harmonisation du nom de la méthode utilisée par analyserFichierUnique
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

    private void extraireInterfacesSansSplit(String nomEntite, String ligne, int idxImplements) 
    {
        int start = idxImplements + 12;
        int end   = ligne.indexOf('{', start);
        if (end == -1) end = ligne.length();

        String segment = ligne.substring(start, end);
        ArrayList<String> interfaces = new ArrayList<>();
        for (String s : ParsingUtil.decoupage(segment)) 
        {
            String inter = s.trim();
            if (inter.contains("<")) inter = inter.substring(0, inter.indexOf('<')).trim();
            if (!inter.isEmpty()) interfaces.add(inter);
        }
        this.lstInterfaces.put(nomEntite, interfaces);
    }

    public List<AssociationObjet> detecterAssociations(List<ClasseObjet> classes, HashMap<String, ClasseObjet> mapClasses) 
    {
        List<AssociationObjet> temporaire = new ArrayList<>();
        
        for (ClasseObjet origine : classes) 
        {
            if (origine.getAttributs() == null) continue;
            
            for (AttributObjet att : origine.getAttributs()) 
            {
                // Utilisation de la méthode centralisée
                String typeCible = ParsingUtil.extraireTypeSimple(att.getType());
                boolean estColl = att.getType().contains("<") || att.getType().endsWith("[]");

                if (mapClasses.containsKey(typeCible) && !typeCible.equals(origine.getNom())) 
                {
                    AssociationObjet asso = new AssociationObjet( mapClasses.get(typeCible), origine, 
                                            new MultipliciteObjet(estColl ? 0 : 1, estColl ? 999999999 : 1), 
                                            null, att.getNom(), true );
                    asso.setRoleDest(att.getNom());
                    temporaire.add(asso);
                }
            }
        }
        
        // Fusion bidirectionnelle (reste inchangé mais fiabilisé)
        List<AssociationObjet> resultat = new ArrayList<>();
        while (!temporaire.isEmpty()) 
        {
            AssociationObjet a1  = temporaire.remove(0);
            AssociationObjet inv = null;
            for (AssociationObjet a2 : temporaire) 
            {
                if (a1.getClasseMere().getNom().equals(a2.getClasseFille().getNom()) && 
                    a1.getClasseFille().getNom().equals(a2.getClasseMere().getNom())) 
                {
                    inv = a2; break;
                }
            }
            if (inv != null) 
            {
                temporaire.remove(inv);
                AssociationObjet bi = new AssociationObjet(a1.getClasseMere(), a1.getClasseFille(),
                                      a1.getMultDest(), inv.getMultDest(), "", false);
                bi.setRoleDest(a1.getRoleDest()); bi.setRoleOrig(inv.getRoleDest());
                resultat.add(bi);
            } 
            else resultat.add(a1);
        }
        return resultat;
    }

    public List<HeritageObjet> resoudreHeritage(HashMap<String, ClasseObjet> mapClasses) 
    {
        List<HeritageObjet> res = new ArrayList<>();
        for (Map.Entry<String, String> e : lstIntentionHeritage.entrySet()) 
        {
            if (mapClasses.containsKey(e.getKey()) && mapClasses.containsKey(e.getValue()))
            {
                res.add(new HeritageObjet(mapClasses.get(e.getKey()), mapClasses.get(e.getValue())));
            }
        }
        return res;
    }

    public List<InterfaceObjet> resoudreImplementation(HashMap<String, ClasseObjet> mapClasses) 
    {
        List<InterfaceObjet> res = new ArrayList<>();
        for (Map.Entry<String, ArrayList<String>> e : lstInterfaces.entrySet()) 
        {
            if (!mapClasses.containsKey(e.getKey())) continue;
            InterfaceObjet io = new InterfaceObjet(mapClasses.get(e.getKey()));
            for (String i : e.getValue()) if (mapClasses.containsKey(i)) io.ajouterInterface(mapClasses.get(i));
            res.add(io);
        }
        return res;
    }

    public List<File> ClassesDuDossier(String chemin) 
    {
        File d = new File(chemin);
        List<File> res = new ArrayList<>();
        File[] files = d.listFiles();
        if (files != null) for (File f : files) if (f.isFile() && f.getName().endsWith(".java")) res.add(f);
        {
            return res;
        }
    }

    public void renumeroterLiaisonsFinales(List<LiaisonObjet> toutes) 
    {
        LiaisonObjet.reinitialiserCompteur();
        int i = 1; for (LiaisonObjet l : toutes) l.setNum(i++);
    }
}