package modele;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Classe utilitaire responsable de l'analyse syntaxique (parsing) manuelle des fichiers Java.
 * Gère l'extraction des membres (attributs/méthodes) et la détection des relations.
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
        boolean enTeteTrouve = false; // Passe à true une fois la ligne "public class..." passée
        
        // Relations
        boolean estHeritier = false;
        String nomParent = null;
        ArrayList<String> interfacesDetectees = new ArrayList<>();
        
        String[] tabSpecifique = {"abstract class", "interface", "enum", "record"}; 

        try (Scanner sc = new Scanner(file))
        {
            while (sc.hasNextLine())
            {
                String ligne = sc.nextLine().trim();

                // Ignorer les lignes vides, imports, packages
                if (ligne.isEmpty() || ligne.startsWith("package") || ligne.startsWith("import")) continue;
                if (ligne.equals("}")) continue;

                // --- 1. ANALYSE DE L'EN-TÊTE ---
                if (!enTeteTrouve && (ligne.contains("class ") || ligne.contains("interface ") || ligne.contains("record ") || ligne.contains("enum ")))
                {
                    enTeteTrouve = true;
                    
                    // Identification du type spécifique
                    for (String motCle : tabSpecifique)
                    {
                        if (ligne.contains(motCle))
                        {
                            specifique = motCle;
                            if (motCle.contains("interface")) estInterface = true;
                            if (motCle.contains("record"))    estRecord = true;
                            
                            // Récupération du vrai nom si interface (ex: "public interface ITest {")
                            if (estInterface || estRecord) {
                                int idxDebut = ligne.indexOf(motCle) + motCle.length();
                                String suite = ligne.substring(idxDebut).trim();
                                // Le nom est le premier mot, s'arrêtant à ' ', '{', ou 'extends'
                                String[] tokens = suite.split("[\\s<{]+");
                                if (tokens.length > 0) nomEntite = tokens[0];
                            }
                            break;
                        }
                    }

                    // Détection Héritage (extends)
                    if (ligne.contains("extends"))
                    {
                        estHeritier = true;
                        int idxExtends = ligne.indexOf("extends") + 7;
                        String suite = ligne.substring(idxExtends).trim();
                        // S'arrête à 'implements' ou '{'
                        String[] tokens = suite.split("[\\s{]+"); 
                        if (tokens.length > 0) nomParent = tokens[0];
                    }

                    // Détection Implémentation (implements)
                    if (ligne.contains("implements"))
                    {
                        int idxImpl = ligne.indexOf("implements") + 10;
                        String suite = ligne.substring(idxImpl).trim();
                        int idxFin = suite.indexOf('{');
                        if (idxFin != -1) suite = suite.substring(0, idxFin);
                        
                        String[] parts = suite.split(",");
                        for (String p : parts) {
                            String iName = p.trim().split("[\\s<]+")[0]; // Nettoyage génériques
                            if (!iName.isEmpty()) interfacesDetectees.add(iName);
                        }
                    }
                    
                    // Cas particulier : RECORD (attributs dans l'en-tête)
                    if (estRecord)
                    {
                        if (ligne.contains("(") && ligne.contains(")")) {
                            String args = ligne.substring(ligne.indexOf('(') + 1, ligne.lastIndexOf(')'));
                            if (!args.isEmpty()) {
                                String[] params = args.split(",");
                                for (String p : params) {
                                    String[] tokens = p.trim().split("\\s+");
                                    if (tokens.length >= 2) {
                                        String type = tokens[0];
                                        String nom = tokens[1];
                                        // Pour un record, ce sont des attributs privés et des méthodes publiques
                                        attributs.add(new AttributObjet(nom, "instance", type, "private", false));
                                        // On ajoute aussi les accesseurs implicites des records
                                        HashMap<String, String> emptyParams = new HashMap<>();
                                        methodes.add(new MethodeObjet(nom, emptyParams, type, "public", false)); 
                                    }
                                }
                            }
                        }
                        // Les records n'ont généralement pas d'autres attributs, on continue
                        continue; 
                    }
                    
                    // On a traité l'en-tête, on passe à la ligne suivante pour le corps
                    continue; 
                }

                // --- 2. ANALYSE DU CORPS (Attributs et Méthodes) ---
                if (enTeteTrouve)
                {
                    boolean estStatique = ligne.contains("static");
                    // On vérifie si c'est une déclaration de membre (doit avoir une visibilité explicite selon vos règles)
                    boolean aVisibilite = (ligne.startsWith("public") || ligne.startsWith("private") || ligne.startsWith("protected"));
                    
                    // Si Interface : tout est méthode (si contient parenthèses), pas d'attributs (sauf constantes ignorées ici)
                    if (estInterface)
                    {
                        // Méthode d'interface : "void methode();" ou "default void methode() {"
                        if (ligne.contains("(") && ligne.contains(")")) 
                        {
                            // On extrait la méthode. Le nomEntite est le nom de l'interface.
                            extraireMethode(ligne, estStatique, nomEntite, methodes);
                        }
                    }
                    // Si Classe / Abstract / Enum
                    else 
                    {
                        // Détection Attribut : finit par ';' et PAS de parenthèses '('
                        if (aVisibilite && ligne.endsWith(";") && !ligne.contains("("))
                        {
                            extraireAttribut(ligne, estStatique, attributs);
                        }
                        // Détection Méthode : contient '('
                        else if (aVisibilite && ligne.contains("(") && !ligne.contains("class "))
                        {
                            // Gère "void methode() {" et "abstract void methode();"
                            extraireMethode(ligne, estStatique, nomEntite, methodes);
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
        
        // Si c'est une interface, on force une liste d'attributs vide (pour éviter les erreurs d'affichage)
        if (estInterface) {
            attributs = new ArrayList<>(); 
        }

        // On utilise nomEntite (qui a été mis à jour si c'était une interface)
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
     * Extrait un attribut.
     * Exemple : "private int age;"
     */
    private void extraireAttribut(String ligne, boolean estStatique, ArrayList<AttributObjet> attributs)
    {
        // Nettoyage de l'initialisation éventuelle " = 0;"
        if (ligne.contains("=")) {
            ligne = ligne.substring(0, ligne.indexOf("=")).trim();
        } else {
            ligne = ligne.replace(";", "").trim();
        }

        String[] parts = ligne.split("\\s+");
        // On filtre les mots clés pour trouver Type et Nom
        List<String> motsUtiles = new ArrayList<>();
        for (String p : parts) {
            if (!p.matches("public|private|protected|static|final|transient|volatile")) {
                motsUtiles.add(p);
            }
        }

        if (motsUtiles.size() >= 2) {
            String type = motsUtiles.get(motsUtiles.size() - 2); // Avant-dernier
            String nom = motsUtiles.get(motsUtiles.size() - 1);  // Dernier
            String visibilite = parts[0]; // Le premier est la visibilité

            attributs.add(new AttributObjet(nom, estStatique ? "static" : "instance", type, visibilite, estStatique));
        }
    }

    /**
     * Extrait une méthode.
     * Exemple : "public void parler() {" ou "public Point(int x)"
     */
    private void extraireMethode(String ligne, boolean estStatique, String nomClasse, ArrayList<MethodeObjet> methodes)
    {
        // Nettoyage de la fin de ligne ('{' ou ';')
        int idxParenthOuvrante = ligne.indexOf('(');
        int idxParenthFermante = ligne.lastIndexOf(')');
        
        if (idxParenthOuvrante == -1 || idxParenthFermante == -1) return;

        // Partie avant les parenthèses : "public void parler"
        String signature = ligne.substring(0, idxParenthOuvrante).trim();
        String[] parts = signature.split("\\s+");
        
        String visibilite = parts[0];
        String nomMethode = parts[parts.length - 1];
        String typeRetour = "void"; // Par défaut

        // Gestion du type de retour
        if (nomMethode.equals(nomClasse)) {
            typeRetour = null; // Constructeur
        } else if (parts.length >= 3) { 
            // "public type nom" -> type est l'avant dernier
            // Il faut ignorer static/final/abstract dans le compte
            List<String> motsUtiles = new ArrayList<>();
            for (String p : parts) {
                if (!p.matches("public|private|protected|static|final|abstract|synchronized")) {
                    motsUtiles.add(p);
                }
            }
            if (motsUtiles.size() >= 2) {
                typeRetour = motsUtiles.get(motsUtiles.size() - 2);
            }
        }

        // Partie paramètres : "int x, String y"
        String paramsStr = ligne.substring(idxParenthOuvrante + 1, idxParenthFermante).trim();
        HashMap<String, String> params = new HashMap<>();
        
        if (!paramsStr.isEmpty()) {
            String[] args = paramsStr.split(",");
            for (String arg : args) {
                String[] tokens = arg.trim().split("\\s+");
                if (tokens.length >= 2) {
                    // Type Nom (ex: "int x")
                    String pType = tokens[0];
                    String pNom = tokens[1];
                    params.put(pNom, pType);
                }
            }
        }

        methodes.add(new MethodeObjet(nomMethode, params, typeRetour, visibilite, estStatique));
    }

    // --- Méthodes utilitaires conservées pour compatibilité ---
    
    public List<AssociationObjet> detecterAssociations(List<ClasseObjet> classes, HashMap<String, ClasseObjet> mapClasses)
    {
        List<AssociationObjet> associations = new ArrayList<>();
        
        for (ClasseObjet classeOrigine : classes)
        {
            if (classeOrigine.getattributs() == null) continue; // Sécurité

            for (AttributObjet attribut : classeOrigine.getattributs())
            {
                String typeAttribut = attribut.getType();
                String typeCible = typeAttribut;
                MultipliciteObjet multCible = new MultipliciteObjet(1, 1); 
                MultipliciteObjet multOrigine = new MultipliciteObjet(1, 1);
                boolean estCollection = false;
                
                if (typeAttribut.contains("<") && typeAttribut.contains(">")) {
                    typeCible = typeAttribut.substring(typeAttribut.indexOf("<") + 1, typeAttribut.indexOf(">")).trim();
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
}