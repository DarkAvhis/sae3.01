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
 */
public class AnalyseurUML 
{
    private HashMap<String,String           > lstIntentionHeritage;
    private HashMap<String,ArrayList<String>> lstInterfaces       ;

    /* -------------------------------------------------------------------------- */
    /* CONSTRUCTEUR                                                               */
    /* -------------------------------------------------------------------------- */

    public AnalyseurUML() 
    {
        this.lstIntentionHeritage = new HashMap<>();
        this.lstInterfaces        = new HashMap<>();
    }

    public HashMap<String,String           > getIntentionsHeritage() { return this.lstIntentionHeritage; }
    public HashMap<String,ArrayList<String>> getInterfaces        () { return this.lstInterfaces       ; }

    public void resetRelations() 
    {
        this.lstIntentionHeritage.clear();
        this.lstInterfaces.clear();
    }

        /**
     * Analyse un fichier Java et extrait la structure (Class, Interface, Record, Enum).
     * Correction : Gère les records sans bloquer la détection des autres types de classes.
     */
    public ClasseObjet analyserFichierUnique(String chemin) 
    {
        File        file            = new File(chemin);
        ClasseObjet classeRacine    = null;
        int         niveauAccolades = 0;

        Stack<ClasseObjet> pileClasses = new Stack<>();

        try (Scanner sc = new Scanner(file)) 
        {
            while (sc.hasNextLine()) 
            {
                String ligneBrute = sc.nextLine().trim();
                
                // 1. Nettoyage des commentaires et lignes inutiles
                int idxCommentaire = ligneBrute.indexOf("//");
                if (idxCommentaire != -1) ligneBrute = ligneBrute.substring(0, idxCommentaire).trim();

                // Filtrage des lignes sans intérêt structurel
                if (ligneBrute.isEmpty()                   || ligneBrute.startsWith("package") || 
                    ligneBrute.startsWith("import") || ligneBrute.startsWith("/*")      || 
                    ligneBrute.startsWith("*")      || ligneBrute.endsWith("*/")           ) 
                {
                    continue;
                }

                // 2. Détection du stéréotype (Utilise ParsingUtil pour identifier le type)
                String stereotype = ParsingUtil.identifierStereotype(ligneBrute);

                boolean estDeclaration = (ligneBrute.contains("class ")  || ligneBrute.contains("interface ") || 
                                          ligneBrute.contains("record ") || ligneBrute.contains("enum ")         );

                if (estDeclaration && !estDansGuillemets(ligneBrute, 0)) 
                {
                    String nomEntite = extraireNomEntite(ligneBrute);

                    if (nomEntite.isEmpty()) continue;

                    // Création de l'objet métier
                    ClasseObjet nouvelleClasse = new ClasseObjet(new ArrayList<>(), new ArrayList<>(), nomEntite, stereotype);

                    // Gestion de la hiérarchie (classes internes)
                    if (classeRacine == null) classeRacine = nouvelleClasse;

                    else if (!pileClasses.isEmpty()) pileClasses.peek().ajouterClasseInterne(nouvelleClasse);
                    
                    pileClasses.push(nouvelleClasse);

                    // Cas spécifique Record
                    if ("record".equals(stereotype)) 
                    {
                        int startP = ligneBrute.indexOf    ('(');
                        int endP   = ligneBrute.lastIndexOf(')');

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

                                        nouvelleClasse.getAttributs().add(new AttributObjet(nom, "instance", type, 
                                                                                "private", false, true));
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
                    if (idxImplements != -1) {
                        extraireInterfacesSansSplit(nomEntite, ligneBrute, idxImplements);

                    if (ligneBrute.contains("{")) niveauAccolades++;
                    continue;
                }

                // 3. Analyse des membres (Attributs et Méthodes)
                if (!pileClasses.isEmpty() && niveauAccolades == pileClasses.size()) 
                {
                    ClasseObjet classeCourante = pileClasses.peek();
                    boolean estStatique = ligneBrute.contains("static");
                    boolean estFinal = ligneBrute.contains("final");

                    // Cas spécial ENUM
                    if ("enum".equals(classeCourante.getSpecifique()) && !ligneBrute.contains("(") && (ligneBrute.endsWith(",") || ligneBrute.endsWith(";"))) {
                        String constante = ligneBrute.replace(",", "").replace(";", "").trim();
                        if (!constante.isEmpty() && Character.isUpperCase(constante.charAt(0))) {
                            classeCourante.getAttributs().add(new AttributObjet(constante, "static", classeCourante.getNom(), "public", true, true));
                            continue;
                        }
                    }

                    // Attributs classiques (finissent par ;)
                    if (ligneBrute.endsWith(";") && !ligneBrute.contains("(")) {
                        ParsingUtil.extraireAttribut(ligneBrute, estStatique, estFinal, classeCourante.getAttributs());
                    } 
                    // Méthodes (contiennent des parenthèses mais pas de =)
                    else if (ligneBrute.contains("(") && !ligneBrute.contains("=")) {
                        String lignePourMethode = ligneBrute;
                        if (!ligneBrute.contains("public") && "interface".equals(classeCourante.getSpecifique())) {
                            lignePourMethode = "public " + ligneBrute;
                        }
                        ParsingUtil.extraireMethode(lignePourMethode, estStatique, classeCourante.getNom(), classeCourante.getMethodes());
                    }
                }

                // 4. Gestion des accolades pour la pile de classes
                if (ligneBrute.contains("{")) niveauAccolades++;
                if (ligneBrute.contains("}")) {
                    if (niveauAccolades == pileClasses.size() && !pileClasses.isEmpty()) {
                        pileClasses.pop();
                    }
                    niveauAccolades--;
                }
            }
        } 
        catch (FileNotFoundException e) { return null; }

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
        List<AssociationObjet> temporaire = new ArrayList<>();

        // 1. Première passe : Extraction de toutes les relations unidirectionnelles
        for (ClasseObjet classeOrigine : classes) 
        {
            if (classeOrigine.getAttributs() == null) continue;

            for (AttributObjet attribut : classeOrigine.getAttributs()) 
            {
                String typeAttribut = attribut.getType();
                String typeCible = typeAttribut;
                MultipliciteObjet multCible = new MultipliciteObjet(1, 1);
                boolean estCollection = false;

                // Analyse du type pour détecter les collections (sans split)
                if (typeAttribut.contains("<") && typeAttribut.contains(">")) 
                {
                    int idx1 = typeAttribut.indexOf('<');
                    int idx2 = typeAttribut.lastIndexOf('>');
                    
                    if (idx1 != -1 && idx2 != -1) 
                    {
                        typeCible = typeAttribut.substring(idx1 + 1, idx2).trim();
                        int virgule = typeCible.indexOf(',');
                        if (virgule != -1) typeCible = typeCible.substring(virgule + 1).trim();
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

                // Si la cible est une classe de notre projet
                if (mapClasses.containsKey(typeCible) && !typeCible.equals(classeOrigine.getNom())) 
                {
                    // On crée une association avec multiplicité source à NULL par défaut pour éviter les doublons
                    AssociationObjet asso = new AssociationObjet(
                        mapClasses.get(typeCible), 
                        classeOrigine, 
                        multCible, 
                        null, // Multiplicité source null
                        attribut.getNom(), 
                        true
                    );
                    // Le nom de l'attribut est le rôle de destination
                    asso.setRoleDest(attribut.getNom());
                    temporaire.add(asso);
                }
            }
        }

        // 2. Deuxième passe : Fusion des relations inverses pour les bidirectionnelles
        List<AssociationObjet> resultatFinal = new ArrayList<>();
        while (!temporaire.isEmpty()) 
        {
            AssociationObjet a1 = temporaire.remove(0);
            AssociationObjet inverse = null;

            for (AssociationObjet a2 : temporaire) 
            {
                if (a1.getClasseMere ().getNom().equals(a2.getClasseFille().getNom()) && 
                    a1.getClasseFille().getNom().equals(a2.getClasseMere ().getNom())     ) 
                {
                    inverse = a2;
                    break;
                }
            }

            if (inverse != null) 
            {
                temporaire.remove(inverse);
                // On fusionne en Bidirectionnelle (false)
                AssociationObjet bi = new AssociationObjet(a1.getClasseMere(), a1.getClasseFille(), 
                                                        a1.getMultDest(), inverse.getMultDest(), 
                                                        "", false);
                bi.setRoleDest(a1.getRoleDest());
                bi.setRoleOrig(inverse.getRoleDest());
                resultatFinal.add(bi);
            } 
            else 
            {
                resultatFinal.add(a1);
            }
        }
        return resultatFinal; 
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
                resultat.add(new HeritageObjet(mapClasses.get(enfant), mapClasses.get(parent)));
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
}