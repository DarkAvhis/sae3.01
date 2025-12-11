package modele;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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

    public ArrayList<String[]> getIntentionsHeritage()
    {
        return lstIntentionHeritage;
    }

    public ArrayList<String[]> getInterfaces()
    {
        return lstInterfaces;
    }

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
        String nomEntite  = nomFichier;
        String specifique = "";
        
        boolean estInterface         = false;
        boolean estRecord            = false;
        boolean enTeteTrouve         = false;
        boolean commentaireBlocActif = false;
        
        // Relations
        boolean estHeritier = false;
        String nomParent = null;
        ArrayList<String> interfacesDetectees = new ArrayList<>();
        
        String[] tabSpecifique = { "abstract class", "interface", "enum", "record" };

        try (Scanner sc = new Scanner(file))
        {
            while (sc.hasNextLine())
            {
                String ligneBrute = sc.nextLine().trim();

                // 1. GESTION DES COMMENTAIRES ET LIGNES VIDES
                if (ligneBrute.isEmpty())
                {
                    continue;
                }
                
                if (commentaireBlocActif)
                {
                    if (ligneBrute.contains("*/"))
                    {
                        commentaireBlocActif = false;
                        if (ligneBrute.endsWith("*/"))
                        {
                            continue;
                        }
                        ligneBrute = ligneBrute.substring(ligneBrute.indexOf("*/") + 2).trim();
                    }
                    else
                    {
                        continue;
                    }
                }
                
                if (ligneBrute.startsWith("/*"))
                {
                    if (!ligneBrute.contains("*/"))
                    {
                        commentaireBlocActif = true;
                        continue;
                    }
                    else
                    {
                        int finCom = ligneBrute.lastIndexOf("*/");
                        if (finCom + 2 < ligneBrute.length())
                        {
                            ligneBrute = ligneBrute.substring(finCom + 2).trim();
                        }
                        else
                        {
                            continue;
                        }
                    }
                }

                if (ligneBrute.startsWith("//"))
                {
                    continue;
                }
                if (ligneBrute.contains("//"))
                {
                    ligneBrute = ligneBrute.substring(0, ligneBrute.indexOf("//")).trim();
                }

                if (ligneBrute.startsWith("package") || ligneBrute.startsWith("import"))
                {
                    continue;
                }
                if (ligneBrute.equals("}"))
                {
                    continue;
                }

                // --- 1. ANALYSE DE L'EN-TÊTE ---
                if (!enTeteTrouve)
                {
                    boolean contientMotCle = false;
                    if (ligneBrute.matches(".*\\bclass\\b.*")
                        || ligneBrute.matches(".*\\binterface\\b.*")
                        || ligneBrute.matches(".*\\brecord\\b.*")
                        || ligneBrute.matches(".*\\benum\\b.*"))
                    {
                        contientMotCle = true;
                    }

                    if (contientMotCle)
                    {
                        enTeteTrouve = true;
                        
                        // Identification du type spécifique
                        for (String motCle : tabSpecifique)
                        {
                            if (ligneBrute.contains(motCle))
                            {
                                specifique = motCle;
                                if (motCle.contains("interface"))
                                {
                                    estInterface = true;
                                }
                                if (motCle.contains("record"))
                                {
                                    estRecord = true;
                                }
                                
                                // Extraction du nom réel de l'entité
                                if (estInterface || estRecord)
                                {
                                    int idxDebut = ligneBrute.indexOf(motCle) + motCle.length();
                                    String suite = ligneBrute.substring(idxDebut).trim();
                                    String[] tokens = suite.split("[\\s<{]+");
                                    if (tokens.length > 0 && !tokens[0].isEmpty())
                                    {
                                        nomEntite = tokens[0];
                                    }
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
                            String[] tokens = suite.split("[\\s\\{]+|implements");
                            if (tokens.length > 0)
                            {
                                nomParent = tokens[0].trim();
                            }
                        }

                        // Détection Implémentation (implements)
                        if (ligneBrute.contains("implements"))
                        {
                            int idxImpl = ligneBrute.indexOf("implements") + 10;
                            String suite = ligneBrute.substring(idxImpl).trim();
                            int idxFin = suite.indexOf('{');
                            
                            if (idxFin != -1)
                            {
                                suite = suite.substring(0, idxFin);
                            }
                            
                            String[] parts = suite.split(",");
                            for (String p : parts)
                            {
                                String iName = p.trim().split("[\\s<]+")[0];
                                if (!iName.isEmpty())
                                {
                                    interfacesDetectees.add(iName);
                                }
                            }
                        }
                        
                        // Cas particulier : RECORD
                        if (estRecord)
                        {
                            if (ligneBrute.contains("(") && ligneBrute.contains(")"))
                            {
                                String args = ligneBrute.substring(ligneBrute.indexOf('(') + 1, ligneBrute.lastIndexOf(')'));
                                if (!args.isEmpty())
                                {
                                    String[] params = args.split(",");
                                    for (String p : params)
                                    {
                                        String[] tokens = p.trim().split("\\s+");
                                        if (tokens.length >= 2)
                                        {
                                            String type = tokens[0];
                                            String nom = tokens[1];

                                            // Record → attributs private final + getter implicite
                                            attributs.add(new AttributObjet(nom, "instance", type, "private", false, true));
                                            HashMap<String, String> emptyParams = new HashMap<>();
                                            methodes.add(new MethodeObjet(nom, emptyParams, type, "public", false));
                                        }
                                    }
                                }
                            }
                            continue;
                        }
                        
                        continue;
                    }
                }

                // --- 2. ANALYSE DU CORPS (Attributs et Méthodes) ---
                if (enTeteTrouve)
                {
                    boolean estStatique = ligneBrute.contains("static");
                    boolean estFinal = ligneBrute.contains("final");
                    boolean aVisibilite = (ligneBrute.startsWith("public")
                                           || ligneBrute.startsWith("private")
                                           || ligneBrute.startsWith("protected"));
                    
                    // Interface
                    if (estInterface)
                    {
                        if (ligneBrute.endsWith(";") && !ligneBrute.contains("("))
                        {
                            extraireAttribut(ligneBrute, true, true, attributs);
                        }
                        else if (ligneBrute.contains("(") && ligneBrute.contains(")"))
                        {
                            extraireMethode(ligneBrute, estStatique, nomEntite, methodes);
                        }
                    }
                    // Classe / Abstract / Enum
                    else if (!estRecord)
                    {
                        if (aVisibilite && ligneBrute.endsWith(";") && !ligneBrute.contains("("))
                        {
                            extraireAttribut(ligneBrute, estStatique, estFinal, attributs);
                        }
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
        ClasseObjet nouvelleClasse = new ClasseObjet(attributs, methodes, nomEntite, specifique);

        // Enregistrement des relations
        if (estHeritier && nomParent != null)
        {
            lstIntentionHeritage.add(new String[] { nomEntite, nomParent });
        }
        for (String iface : interfacesDetectees)
        {
            lstInterfaces.add(new String[] { nomEntite, iface });
        }

        return nouvelleClasse;
    }

    /**
     * Extrait un attribut en nettoyant l'initialisation.
     */
    private void extraireAttribut(String ligne, boolean estStatique, boolean estFinal, ArrayList<AttributObjet> attributs)
    {
        if (ligne.contains("="))
        {
            ligne = ligne.substring(0, ligne.indexOf("=")).trim();
        }
        else
        {
            ligne = ligne.replace(";", "").trim();
        }

        String[] parts = ligne.split("\\s+");
        
        List<String> motsUtiles = new ArrayList<>();
        for (String p : parts)
        {
            if (!p.matches("public|private|protected|static|final|transient|volatile"))
            {
                motsUtiles.add(p);
            }
        }

        if (motsUtiles.size() >= 2)
        {
            String type = motsUtiles.get(motsUtiles.size() - 2);
            String nom = motsUtiles.get(motsUtiles.size() - 1);
            String visibilite = parts[0];

            attributs.add(new AttributObjet(nom, estStatique ? "static" : "instance", type, visibilite, estStatique, estFinal));
        }
    }

    /**
     * Extrait une méthode ou un constructeur.
     */
    private void extraireMethode(String ligne, boolean estStatique, String nomClasse, ArrayList<MethodeObjet> methodes)
    {
        int idxParenthOuvrante = ligne.indexOf('(');
        int idxParenthFermante = ligne.lastIndexOf(')');
        
        if (idxParenthOuvrante == -1 || idxParenthFermante == -1)
        {
            return;
        }

        String signature = ligne.substring(0, idxParenthOuvrante).trim();
        String[] parts = signature.split("\\s+");
        
        String visibilite = parts[0];
        String nomMethode = parts[parts.length - 1];
        String typeRetour = "void";

        if (nomMethode.equals(nomClasse))
        {
            typeRetour = null;
        }
        else
        {
            List<String> motsUtiles = new ArrayList<>();
            for (String p : parts)
            {
                if (!p.matches("public|private|protected|static|final|abstract|synchronized|default"))
                {
                    motsUtiles.add(p);
                }
            }
            if (motsUtiles.size() >= 2)
            {
                typeRetour = motsUtiles.get(motsUtiles.size() - 2);
            }
        }

        String paramsStr = ligne.substring(idxParenthOuvrante + 1, idxParenthFermante).trim();
        HashMap<String, String> params = new HashMap<>();
        
        if (!paramsStr.isEmpty())
        {
            String[] args = paramsStr.split(",");
            for (String arg : args)
            {
                String[] tokens = arg.trim().split("\\s+");
                if (tokens.length >= 2)
                {
                    params.put(tokens[1], tokens[0]);
                }
            }
        }

        methodes.add(new MethodeObjet(nomMethode, params, typeRetour, visibilite, estStatique));
    }

    // --- Méthodes suivantes inchangées ---

    public List<AssociationObjet> detecterAssociations(List<ClasseObjet> classes, HashMap<String, ClasseObjet> mapClasses)
    {
        List<AssociationObjet> associations = new ArrayList<>();
        
        for (ClasseObjet classeOrigine : classes)
        {
            if (classeOrigine.getattributs() == null)
            {
                continue;
            }

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
