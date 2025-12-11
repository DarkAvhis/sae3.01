package modele;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Classe utilitaire responsable de l'analyse syntaxique (parsing) manuelle des fichiers Java.
 * Version modifiée : n'utilise plus String.split(...) — parsing manuel à la place.
 * Les helper builders ont été remplacés par des concaténations (+=) comme demandé.
 */
public class AnalyseurUML
{
    private static final int MULT_INDEFINIE = 999999999;
    
    // Listes pour stocker les relations en attente de résolution
    private ArrayList<String[]> lstIntentionHeritage = new ArrayList<>(); 
    private ArrayList<String[]> lstInterfaces = new ArrayList<>();

    /**
     * Réinitialise les listes de relations stockées.
     */
    public void resetRelations()
    {
        this.lstIntentionHeritage.clear();
        this.lstInterfaces.clear(); 
    }

    public ArrayList<String[]> getIntentionsHeritage() { return lstIntentionHeritage; }
    public ArrayList<String[]> getInterfaces()         { return lstInterfaces;        }

    /**
     * Analyse un fichier Java et construit l'objet ClasseObjet correspondant.
     */
    public ClasseObjet analyserFichierUnique(String chemin)
    {
        File file = new File(chemin);
        String nomFichier = file.getName().replace(".java", "");
        
        // Listes finales
        ArrayList<AttributObjet> attributs = new ArrayList<>();
        ArrayList<MethodeObjet> methodes = new ArrayList<>();
        
        // États de l'analyse
        String nomEntite = nomFichier; 
        String specifique = ""; 
        boolean estInterface = false;
        boolean estRecord = false;
        boolean enTeteTrouve = false; 
        boolean commentaireBlocActif = false;
        
        // Relations
        boolean estHeritier = false;
        String nomParent = null;
        ArrayList<String> interfacesDetectees = new ArrayList<>();
        
        String[] tabSpecifique = {"abstract class", "interface", "enum", "record"}; 

        try (Scanner sc = new Scanner(file))
        {
            while (sc.hasNextLine())
            {
                String ligneBrute = sc.nextLine().trim();

                // 1. GESTION DES COMMENTAIRES ET LIGNES VIDES (Logique inchangée)
                if (ligneBrute.isEmpty()) continue;
                
                if (commentaireBlocActif) {
                    if (ligneBrute.contains("*/")) {
                        commentaireBlocActif = false;
                        if (ligneBrute.endsWith("*/")) continue;
                        ligneBrute = ligneBrute.substring(ligneBrute.indexOf("*/") + 2).trim();
                    } else {
                        continue;
                    }
                }
                
                if (ligneBrute.startsWith("/*")) {
                    if (!ligneBrute.contains("*/")) {
                        commentaireBlocActif = true;
                        continue;
                    } else {
                        int finCom = ligneBrute.lastIndexOf("*/");
                        if (finCom + 2 < ligneBrute.length()) {
                            ligneBrute = ligneBrute.substring(finCom + 2).trim();
                        } else {
                            continue; 
                        }
                    }
                }

                if (ligneBrute.startsWith("//")) continue;
                if (ligneBrute.contains("//")) {
                    ligneBrute = ligneBrute.substring(0, ligneBrute.indexOf("//")).trim();
                }

                if (ligneBrute.startsWith("package") || ligneBrute.startsWith("import")) continue;
                if (ligneBrute.equals("}")) continue;

                // --- 1. ANALYSE DE L'EN-TÊTE ---
                if (!enTeteTrouve && (ligneBrute.contains(" class ") || ligneBrute.contains("interface ") || ligneBrute.contains("record ") || ligneBrute.contains("enum ")))
                {
                    enTeteTrouve = true;
                    
                    // Identification du type spécifique (Logique inchangée)
                    for (String motCle : tabSpecifique)
                    {
                        if (ligneBrute.contains(motCle))
                        {
                            specifique = motCle;
                            if (motCle.contains("interface")) estInterface = true;
                            if (motCle.contains("record"))    estRecord = true;
                            
                            // Récupération du vrai nom (parsing manuel)
                            if (estInterface || estRecord) {
                                int idxDebut = ligneBrute.indexOf(motCle) + motCle.length();
                                String suite = ligneBrute.substring(idxDebut).trim();
                                String nom = lireNom(suite);
                                if (!nom.isEmpty()) nomEntite = nom;
                            }
                            break;
                        }
                    }

                    // Détection Héritage (extends)
                    if (ligneBrute.contains("extends"))
                    {
                        estHeritier = true;
                        int idxExtends = ligneBrute.indexOf("extends") + 7;
                        String suite = ligneBrute.substring(idxExtends).trim();
                        // Lire le premier identifiant utile (manuellement)
                        String possibleParent = lireNom(suite);
                        if (!possibleParent.isEmpty()) nomParent = possibleParent;
                    }

                    // Détection Implémentation (implements) --- OPTIMISATION ICI ---
                    if (ligneBrute.contains("implements"))
                    {
                        int idxImpl = ligneBrute.indexOf("implements") + 10;
                        String suite = ligneBrute.substring(idxImpl).trim();
                        int idxFin = suite.indexOf('{');
                        if (idxFin != -1) suite = suite.substring(0, idxFin);
                        
                        // Boucle optimisée pour éviter split() sur la virgule
                        int indexDebut = 0;
                        int indexVirgule;
                        
                        while (indexDebut < suite.length())
                        {
                            indexVirgule = suite.indexOf(',', indexDebut);
                            if (indexVirgule == -1)
                            {
                                indexVirgule = suite.length();
                            }
                            
                            String interBrute = suite.substring(indexDebut, indexVirgule).trim();
                            
                            if (!interBrute.isEmpty())
                            {
                                // Nettoyage des génériques (<) et espaces
                                int idxSpace = indexEspace(interBrute);
                                int idxChevron = interBrute.indexOf('<');
                                int idxFinNom = interBrute.length();

                                if (idxSpace != -1) idxFinNom = Math.min(idxFinNom, idxSpace);
                                if (idxChevron != -1) idxFinNom = Math.min(idxFinNom, idxChevron);

                                String nomInterface = interBrute.substring(0, idxFinNom).trim();
                                
                                if (!nomInterface.isEmpty())
                                {
                                    interfacesDetectees.add(nomInterface);
                                }
                            }
                            
                            indexDebut = indexVirgule + 1;
                            if (indexVirgule == suite.length()) break;
                        }
                    }
                    
                    // Cas particulier : RECORD (parsing manuel des paramètres)
                    if (estRecord)
                    {
                        if (ligneBrute.contains("(") && ligneBrute.contains(")")) {
                            String args = ligneBrute.substring(ligneBrute.indexOf('(') + 1, ligneBrute.lastIndexOf(')'));
                            args = args.trim();
                            if (!args.isEmpty()) {
                                List<String> params = decoupage(args);
                                for (String p : params) {
                                    String trimmed = p.trim();
                                    List<String> tokens = separerMots(trimmed);
                                    if (tokens.size() >= 2) {
                                        // type peut être composé (ex: List<String>), on prend tout sauf le dernier token
                                        String nom = tokens.get(tokens.size() - 1);
                                        String type = "";
                                        for (int i = 0; i < tokens.size() - 1; i++) {
                                            if (i > 0) type += ' ';
                                            type += tokens.get(i);
                                        }

                                        attributs.add(new AttributObjet(nom, "instance", type, "private", false, true));
                                        HashMap<String, String> emptyParams = new HashMap<>();
                                        methodes.add(new MethodeObjet(nom, emptyParams, type, "public", false)); 
                                    }
                                }
                            }
                        }
                        continue; 
                    }
                    
                    continue; // Passer la ligne d'en-tête
                }

                // --- 2. ANALYSE DU CORPS (Attributs et Méthodes) ---
                if (enTeteTrouve)
                {
                    boolean estStatique = ligneBrute.contains("static");
                    boolean estFinal    = ligneBrute.contains("final");
                    boolean aVisibilite = (ligneBrute.startsWith("public") || ligneBrute.startsWith("private") || ligneBrute.startsWith("protected"));
                    
                    // Si Interface : on cherche les constantes ou les méthodes
                    if (estInterface)
                    {
                        // Détection Constantes d'interface (Attributs)
                        if (ligneBrute.endsWith(";") && !ligneBrute.contains("(")) 
                        {
                            // Implicitement PUBLIC STATIC FINAL
                            extraireAttribut(ligneBrute, true, true, attributs); 
                        }
                        // Détection Méthodes d'interface
                        else if (ligneBrute.contains("(") && ligneBrute.contains(")")) 
                        {
                            extraireMethode(ligneBrute, estStatique, nomEntite, methodes);
                        }
                    }
                    // Si Classe / Abstract / Enum
                    else if (!estRecord) 
                    {
                        // Détection Attribut : finit par ';' et PAS de parenthèses '('
                        if (aVisibilite && ligneBrute.endsWith(";") && !ligneBrute.contains("("))
                        {
                            extraireAttribut(ligneBrute, estStatique, estFinal, attributs); 
                        }
                        // Détection Méthode : contient '(''
                        else if (aVisibilite && ligneBrute.contains("(") && !ligneBrute.contains("class "))
                        {
                            extraireMethode(ligneBrute, estStatique, nomEntite, methodes);
                        }
                    }
                }
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Fichier non trouvé: " + chemin);
            return null;
        }

        // --- 3. CRÉATION DE L'OBJET FINAL ---
        
        // Si c'est une interface, on force une liste d'attributs vide (la logique les a déjà retirés sauf s'ils sont des constantes)
        if (estInterface) {
            // Dans ce contexte, on ne touche pas à la liste 'attributs' si des constantes ont été trouvées,
            // pour permettre leur affichage.
        }

        ClasseObjet nouvelleClasse = new ClasseObjet(attributs, methodes, nomEntite, specifique);

        // Enregistrement des relations pour le contrôleur
        if (estHeritier && nomParent != null) {
            lstIntentionHeritage.add(new String[]{nomEntite, nomParent});
        }
        for (String iface : interfacesDetectees) {
            lstInterfaces.add(new String[]{nomEntite, iface});
        }

        return nouvelleClasse;
    }

    /**
     * Extrait un attribut en nettoyant l'initialisation.
     */
    private void extraireAttribut(String ligne, boolean estStatique, boolean estFinal, ArrayList<AttributObjet> attributs)
    {
        // 1. Ignorer l'initialisation (= ...)
        if (ligne.contains("=")) {
            ligne = ligne.substring(0, ligne.indexOf("=")).trim();
        } else {
            ligne = ligne.replace(";", "").trim();
        }

        List<String> parts = separerMots(ligne);
        
        // 2. Filtrer les mots-clés pour ne garder que Type et Nom
        List<String> motsUtiles = new ArrayList<>();
        for (String p : parts) {
            if (!aModifierMotCle(p)) {
                motsUtiles.add(p);
            }
        }

        if (motsUtiles.size() >= 2) {
            String type = motsUtiles.get(motsUtiles.size() - 2); 
            String nom = motsUtiles.get(motsUtiles.size() - 1); 
            
            String visibilite = parts.size() > 0 ? parts.get(0) : ""; 
            if (!aVisibilite(visibilite)) {
                 visibilite = "public"; // Cas des constantes d'interface ou visibilité omise
            }

            // Utilisation du nouveau constructeur
            attributs.add(new AttributObjet(nom, estStatique ? "static" : "instance", type, visibilite, estStatique, estFinal));
        }
    }

    /**
     * Extrait une méthode ou un constructeur.
     * --- OPTIMISATION : parsing manuel des paramètres et tokens ---
     */
    private void extraireMethode(String ligne, boolean estStatique, String nomClasse, ArrayList<MethodeObjet> methodes)
    {
        int idxParenthOuvrante = ligne.indexOf('(');
        int idxParenthFermante = ligne.lastIndexOf(')');
        
        if (idxParenthOuvrante == -1 || idxParenthFermante == -1) return;

        String signature = ligne.substring(0, idxParenthOuvrante).trim();
        List<String> parts = separerMots(signature);
        
        if (parts.isEmpty()) return;

        String visibilite = parts.get(0);
        String nomMethode = parts.get(parts.size() - 1);
        String typeRetour = "void"; 

        if (nomMethode.equals(nomClasse)) {
            typeRetour = null; // Constructeur
        } else { 
            List<String> motsUtiles = new ArrayList<>();
            for (String p : parts) {
                if (!aMethodeModif(p)) {
                    motsUtiles.add(p);
                }
            }
            if (motsUtiles.size() >= 2) {
                typeRetour = motsUtiles.get(motsUtiles.size() - 2);
            }
        }

        // --- EXTRACTION DES PARAMETRES (parsing manuel) ---
        String paramsStr = ligne.substring(idxParenthOuvrante + 1, idxParenthFermante).trim();
        HashMap<String, String> params = new HashMap<>();
        
        if (!paramsStr.isEmpty()) {
            List<String> paramList = decoupage(paramsStr);

            for (String param : paramList) {
                String p = param.trim();
                if (!p.isEmpty()) {
                    int spaceIndex = dernierIndexEspace(p);
                    if (spaceIndex > 0) {
                        String pType = p.substring(0, spaceIndex).trim();
                        String pNom = p.substring(spaceIndex + 1).trim();
                        params.put(pNom, pType);
                    }
                }
            }
        }

        methodes.add(new MethodeObjet(nomMethode, params, typeRetour, visibilite, estStatique));
    }

    // --- Les méthodes detecterAssociations et ClassesDuDossier sont inchangées mais utilisent maintenant separerMots si besoin ---
    
    public List<AssociationObjet> detecterAssociations(List<ClasseObjet> classes, HashMap<String, ClasseObjet> mapClasses)
    {
        List<AssociationObjet> associations = new ArrayList<>();
        
        for (ClasseObjet classeOrigine : classes)
        {
            if (classeOrigine.getattributs() == null) continue;

            for (AttributObjet attribut : classeOrigine.getattributs())
            {
                String typeAttribut = attribut.getType();
                String typeCible = typeAttribut;
                MultipliciteObjet multCible = new MultipliciteObjet(1, 1); 
                MultipliciteObjet multOrigine = new MultipliciteObjet(1, 1);
                boolean estCollection = false;
                
                if (typeAttribut.contains("<") && typeAttribut.contains(">")) {
                    int idx1 = typeAttribut.indexOf('<');
                    int idx2 = typeAttribut.indexOf('>', idx1 + 1);
                    if (idx1 != -1 && idx2 != -1 && idx2 > idx1) {
                        typeCible = typeAttribut.substring(idx1 + 1, idx2).trim();
                    }
                    estCollection = true;
                } else if (typeAttribut.endsWith("[]")) {
                    typeCible = typeAttribut.replace("[]", "").trim();
                    estCollection = true;
                }

                if (estCollection) multCible = new MultipliciteObjet(0, MULT_INDEFINIE); 
                
                if (mapClasses.containsKey(typeCible) && !typeCible.equals(classeOrigine.getNom()))
                {
                    ClasseObjet classeCible = mapClasses.get(typeCible);
                    AssociationObjet association = new AssociationObjet(
                            classeCible, classeOrigine, multCible, multOrigine, attribut.getNom(), true
                    );
                    associations.add(association);
                }
            }
        }
        return associations;
    }

    public ArrayList<File> ClassesDuDossier(String cheminDossier)
    {
        File dossier = new File(cheminDossier);
        File[] tousLesFichiers = dossier.listFiles();
        ArrayList<File> fichiersJava = new ArrayList<>();
        if (tousLesFichiers != null) {
            for (File f : tousLesFichiers) {
                if (f.getName().endsWith(".java")) {
                    fichiersJava.add(f);
                }
            }
        }
        return fichiersJava;
    }

    // -------------------- Methodes d'aide pour le parsing sans split vu que on a pas compris quand l'utiliser ou pas --------------------

    private List<String> separerMots(String s) {
        List<String> tokens = new ArrayList<>();
        String token = "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) {
                if (!token.isEmpty()) {
                    tokens.add(token);
                    token = "";
                }
            } else {
                token += c; 
            }
        }
        if (!token.isEmpty()) tokens.add(token);
        return tokens;
    }

    private int indexEspace(String s) {
        for (int i = 0; i < s.length(); i++) if (Character.isWhitespace(s.charAt(i))) return i;
        return -1;
    }

    private int dernierIndexEspace(String s) {
        for (int i = s.length() - 1; i >= 0; i--) if (Character.isWhitespace(s.charAt(i))) return i;
        return -1;
    }

    private boolean aModifierMotCle(String s) {
        return s.equals("public") || s.equals("private") || s.equals("protected")
                || s.equals("static") || s.equals("final") || s.equals("transient") || s.equals("volatile");
    }

    private boolean aMethodeModif(String s) {
        return s.equals("public") || s.equals("private") || s.equals("protected")
                || s.equals("static") || s.equals("final") || s.equals("abstract") || s.equals("synchronized") || s.equals("default");
    }

    private boolean aVisibilite(String s) {
        return s.equals("public") || s.equals("private") || s.equals("protected");
    }

    /**
     * Lit le premier identifiant utile dans la chaîne (arrêt sur espace, '{', '<', ',', '(' ).
     */
    private String lireNom(String s) {
        s = s.trim();
        if (s.isEmpty()) return "";
        String id = "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c) || c == '{' || c == '<' || c == ',' || c == '(') break;
            id += c; // concaténation volontaire
        }
        return id;
    }

    /**
     * Split par virgules au niveau "top-level" en ignorant les virgules à l'intérieur de < >.
     * Construit les fragments via concaténation (+=) au lieu de StringBuilder.
     */
    private List<String> decoupage(String s) {
        List<String> parts = new ArrayList<>();
        String part = "";
        int depthAngle = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '<') {
                depthAngle++;
                part += c;
            } else if (c == '>') {
                if (depthAngle > 0) depthAngle--;
                part += c;
            } else if (c == ',' && depthAngle == 0) {
                parts.add(part);
                part = "";
            } else {
                part += c;
            }
        }
        if (!part.isEmpty()) parts.add(part);
        return parts;
    }
}
