package modele;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import modele.entites.AssociationObjet;
import modele.entites.AttributObjet;
import modele.entites.ClasseObjet;
import modele.entites.HeritageObjet;
import modele.entites.InterfaceObjet;
import modele.entites.LiaisonObjet;
import modele.entites.MultipliciteObjet;
import modele.outil.ParsingUtil;

/**
 * Classe utilitaire responsable de l'analyse syntaxique (parsing) manuelle des
 * fichiers Java.
 */
public class AnalyseurUML {
    private static final int MULT_INDEFINIE = 999999999;

    // Listes pour stocker les relations en attente de résolution
    private HashMap<String, String>             lstIntentionHeritage;
    private HashMap<String, ArrayList<String>>  lstInterfaces;

    public AnalyseurUML() {
        this.lstIntentionHeritage = new HashMap<String, String>();
        this.lstInterfaces = new HashMap<String, ArrayList<String>>();
    }

    public void resetRelations() {
        this.lstIntentionHeritage.clear();
        this.lstInterfaces.clear();
    }

    public HashMap<String, String> getIntentionsHeritage() {
        return this.lstIntentionHeritage;
    }

    public HashMap<String, ArrayList<String>> getInterfaces() {
        return this.lstInterfaces;
    }

    public ClasseObjet analyserFichierUnique(String chemin) {
        
        File file = new File(chemin);
        String nomFichier = file.getName().replace(".java", "");



        // NOUVEAU : Utilisation d'une pile pour gérer la hiérarchie des classes
        java.util.Stack<ClasseObjet> pileClasses = new java.util.Stack<>();
        ClasseObjet classeRacine = null;

        // NOUVEAU : Compteur pour suivre la profondeur des blocs { }
        int niveauAccolades = 0;

        String ligneBrute;
        boolean commentaireBlocActif = false;

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                ligneBrute = sc.nextLine().trim();
                if (ligneBrute.isEmpty())
                    continue;

                // --- GESTION DES COMMENTAIRES (Logique existante conservée) ---
                if (commentaireBlocActif) {
                    if (ligneBrute.contains("*/")) {
                        commentaireBlocActif = false;
                        if (ligneBrute.endsWith("*/"))
                            continue;
                        ligneBrute = ligneBrute.substring(ligneBrute.indexOf("*/") + 2).trim();
                    } else
                        continue;
                }
                if (ligneBrute.startsWith("/*")) {
                    if (!ligneBrute.contains("*/")) {
                        commentaireBlocActif = true;
                        continue;
                    } else {
                        int finCom = ligneBrute.lastIndexOf("*/");
                        if (finCom + 2 < ligneBrute.length())
                            ligneBrute = ligneBrute.substring(finCom + 2).trim();
                        else
                            continue;
                    }
                }
                if (ligneBrute.startsWith("//"))
                    continue;
                if (ligneBrute.contains("//"))
                    ligneBrute = ligneBrute.substring(0, ligneBrute.indexOf("//")).trim();
                if (ligneBrute.startsWith("package") || ligneBrute.startsWith("import"))
                    continue;
                // --- DÉTECTION D'UNE DÉCLARATION DE CLASSE (RACINE OU INTERNE) ---
                // Version simple : on cherche les mots-clés et on vérifie qu'ils ne sont
                // pas à l'intérieur d'une chaîne entre guillemets.
                String[] mots = {"class", "interface", "record", "enum"};
                String foundKeyword = null;
                int foundIdx = -1;
                int nameStart = -1;

                for (String kw : mots) {
                    int idx = ligneBrute.indexOf(kw);
                    while (idx != -1) {
                        // si le mot est dans une chaîne, on ignore
                        if (estDansGuillemets(ligneBrute, idx)) {
                            idx = ligneBrute.indexOf(kw, idx + 1);
                            continue;
                        }

                        // vérifier que le caractère précédent n'est pas un identifiant (pour éviter les sous-chaînes)
                        boolean okBefore = (idx == 0) || !Character.isJavaIdentifierPart(ligneBrute.charAt(idx - 1));
                        if (!okBefore) {
                            idx = ligneBrute.indexOf(kw, idx + 1);
                            continue;
                        }

                        // trouver le début du nom après le mot-clé
                        int after = idx + kw.length();

                        // Si le mot-clé est immédiatement suivi d'un caractère d'identifiant
                        // (ex: "classeRacine" contient "class" suivi de 'e'), alors il s'agit
                        // d'un identifiant plus long et non d'une déclaration : on ignore.
                        if (after < ligneBrute.length() && Character.isJavaIdentifierPart(ligneBrute.charAt(after))) {
                            idx = ligneBrute.indexOf(kw, idx + 1);
                            continue;
                        }

                        // Sinon on saute les espaces et on vérifie qu'un nom commence bien
                        while (after < ligneBrute.length() && Character.isWhitespace(ligneBrute.charAt(after)))
                            after++;
                        if (after < ligneBrute.length() && Character.isJavaIdentifierStart(ligneBrute.charAt(after))) {
                            foundKeyword = kw;
                            foundIdx = idx;
                            nameStart = after;
                            break;
                        }
                        idx = ligneBrute.indexOf(kw, idx + 1);
                    }
                    if (foundKeyword != null) break;
                }

                if (foundKeyword != null) {
                    // Ne pas afficher simplement le mot-clé "class" sous le bloc :
                    // garder vide pour les classes normales, mais conserver "abstract"
                    // si la déclaration est abstraite. Pour les autres mots-clés
                    // (interface/record/enum) on conserve le terme.
                    String specifique = foundKeyword.equals("class") ? "" : foundKeyword;
                    if (ligneBrute.contains("abstract") && foundKeyword.equals("class")) {
                        // Utiliser la même valeur que le reste du code attend ("abstract class")
                        specifique = "abstract class";
                    }

                    // Extraire le nom sans split ni regex
                    StringBuilder nameBuilder = new StringBuilder();
                    int p = nameStart;
                    while (p < ligneBrute.length() && Character.isJavaIdentifierPart(ligneBrute.charAt(p))) {
                        nameBuilder.append(ligneBrute.charAt(p));
                        p++;
                    }
                    String nomEntite = nameBuilder.toString();
                    if (nomEntite.isEmpty()) nomEntite = nomFichier;

                    ClasseObjet nouvelleClasse = new ClasseObjet(new ArrayList<>(), new ArrayList<>(), nomEntite,
                            specifique);

                    // DÉTECTION DES INTERFACES IMPLÉMENTÉES
                    if (ligneBrute.contains(" implements ")) 
                    {
                        String partieImplements = ligneBrute.substring(ligneBrute.indexOf(" implements ") + 12);
                        
                        // Nettoyage de la fin de ligne (accolade ou extends éventuel)
                        if (partieImplements.contains("{")) partieImplements = partieImplements.split("\\{")[0];

                        // Utilisation du Scanner avec délimiteur
                        Scanner delimiterScanner = new Scanner(partieImplements);
                        // Délimiteur : virgule entourée d'espaces OU espaces simples
                        // On utilise une regex qui ignore les virgules à l'intérieur des chevrons < >
                        delimiterScanner.useDelimiter(",(?![^<>]*>)|\\s+");

                        ArrayList<String> listeInterfaces = new ArrayList<>();
                        while (delimiterScanner.hasNext()) {
                            String nomInterface = delimiterScanner.next().trim();
                            if (!nomInterface.isEmpty()) {
                                // Nettoyage des génériques si nécessaire
                                if (nomInterface.contains("<")) {
                                    nomInterface = nomInterface.substring(0, nomInterface.indexOf("<")).trim();
                                }
                                listeInterfaces.add(nomInterface);
                            }
                        }
                        if (!listeInterfaces.isEmpty()) {
                            this.lstInterfaces.put(nomEntite, listeInterfaces);
                        }
                        delimiterScanner.close();
                    }

                    // LOGIQUE DE PILE : Si la pile n'est pas vide, c'est une classe interne
                    if (classeRacine == null) {
                        classeRacine = nouvelleClasse;
                    } else if (!pileClasses.isEmpty()) {
                        pileClasses.peek().ajouterClasseInterne(nouvelleClasse);
                    }

                    // On pousse la classe sur la pile pour y ajouter ses membres
                    pileClasses.push(nouvelleClasse);

                    // Gestion héritage (logique simplifiée pour l'exemple)
                    if (ligneBrute.contains("extends")) {
                        String parent = ParsingUtil
                                .lireNom(ligneBrute.substring(ligneBrute.indexOf("extends") + 7).trim());
                        this.lstIntentionHeritage.put(nomEntite, parent);
                    }

                    // Si la ligne contient déjà l'accolade ouvrante, on l'incrémente ici
                    if (ligneBrute.contains("{"))
                        niveauAccolades++;
                    continue;
                }

                // --- GESTION DES ACCOLADES ET ISOLATION DES MEMBRES ---
                if (ligneBrute.contains("{") && !ligneBrute.contains(" class "))
                    niveauAccolades++;

                // ISOLATION : On n'extrait que si on est au niveau de la classe (niveau ==
                // taille pile)
                // Cela évite de prendre les variables locales dans les méthodes (niveau >
                // taille pile)
                if (!pileClasses.isEmpty() && niveauAccolades == pileClasses.size()) {
                    ClasseObjet classeCourante = pileClasses.peek();
                    boolean estStatique = ligneBrute.contains("static");
                    boolean estFinal = ligneBrute.contains("final");

                    // Extraction Attributs
                    if (ligneBrute.endsWith(";") && !ligneBrute.contains("(")) {
                        ParsingUtil.extraireAttribut(ligneBrute, estStatique, estFinal, classeCourante.getattributs());
                    }
                    // Extraction Méthodes (nécessite une visibilité pour éviter les blocs
                    // statiques)
                    else if (ligneBrute.contains("(") && (ligneBrute.contains("public")
                            || ligneBrute.contains("private") || ligneBrute.contains("protected")) && !ligneBrute.contains("=")) {
                        ParsingUtil.extraireMethode(ligneBrute, estStatique, classeCourante.getNom(),
                                classeCourante.getMethodes());
                    }
                }

                // SORTIE DE CONTEXTE : On dépile quand on ferme une classe
                if (ligneBrute.contains("}")) {
                    // Si l'accolade fermante correspond à la fin d'une classe
                    if (niveauAccolades == pileClasses.size() && !pileClasses.isEmpty()) {
                        pileClasses.pop();
                    }
                    niveauAccolades--;
                }
            }
        } catch (FileNotFoundException e) {
            return null;
        }

        return classeRacine;
    }

    /**
     * Vérifie si la position idx dans la ligne se trouve à l'intérieur d'une
     * chaîne de caractères délimitée par des guillemets doubles.
     * Méthode simple : compte le nombre de '"' avant idx. Si impair -> dedans.
     */
    private boolean estDansGuillemets(String ligne, int idx) {
        if (ligne == null || idx <= 0) return false;
        int count = 0;
        for (int i = 0; i < Math.min(idx, ligne.length()); i++) {
            if (ligne.charAt(i) == '"') count++;
        }
        return (count % 2) == 1;
    }

    public List<AssociationObjet> detecterAssociations(List<ClasseObjet> classes,
            HashMap<String, ClasseObjet> mapClasses) {
        List<AssociationObjet> associationsFinales = new ArrayList<AssociationObjet>();
        List<AssociationObjet> associationsUnidirectionnelles = new ArrayList<AssociationObjet>();
        HashSet<AssociationObjet> associationsDansFusion = new HashSet<AssociationObjet>();

        for (ClasseObjet classeOrigine : classes) {
            if (classeOrigine.getattributs() == null)
                continue;

            for (AttributObjet attribut : classeOrigine.getattributs()) {
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

                if (estCollection)
                    multCible = new MultipliciteObjet(0, MULT_INDEFINIE);

                if (mapClasses.containsKey(typeCible) && !typeCible.equals(classeOrigine.getNom())) {
                    ClasseObjet classeCible = mapClasses.get(typeCible);

                    AssociationObjet associationPotentielle = new AssociationObjet(classeCible, classeOrigine,
                            multCible, multOrigine, attribut.getNom(), true);
                    associationsUnidirectionnelles.add(associationPotentielle);
                }
            }
        }

        HashSet<String> pairesDeClassesFusionnees = new HashSet<>();

        for (AssociationObjet assoOrigine : associationsUnidirectionnelles) {
            if (associationsDansFusion.contains(assoOrigine))
                continue;

            ClasseObjet classeA = assoOrigine.getClasseFille();
            ClasseObjet classeB = assoOrigine.getClasseMere();

            String nomA = classeA.getNom();
            String nomB = classeB.getNom();
            String clePaire = (nomA.compareTo(nomB) < 0) ? nomA + ":" + nomB : nomB + ":" + nomA;

            if (pairesDeClassesFusionnees.contains(clePaire))
                continue;

            AssociationObjet assoInverse = null;
            for (AssociationObjet a : associationsUnidirectionnelles) {
                if (!associationsDansFusion.contains(a) && a.getClasseFille() == classeB
                        && a.getClasseMere() == classeA) {
                    assoInverse = a;
                    break;
                }
            }

            if (assoInverse != null) {
                associationsDansFusion.add(assoOrigine);
                associationsDansFusion.add(assoInverse);
                pairesDeClassesFusionnees.add(clePaire);

                MultipliciteObjet multA_role = assoInverse.getMultDest();
                MultipliciteObjet multB_role = assoOrigine.getMultDest();

                AssociationObjet assoFinale = new AssociationObjet(classeB, classeA, multB_role, multA_role,
                        null, false);
                associationsFinales.add(assoFinale);
            }
        }

        for (AssociationObjet asso : associationsUnidirectionnelles) {
            if (!associationsDansFusion.contains(asso))
                associationsFinales.add(asso);
        }
        return associationsFinales;
    }

    public ArrayList<File> ClassesDuDossier(String cheminDossier) {
        File dossier = new File(cheminDossier);
        File[] tousLesFichiers = dossier.listFiles();
        ArrayList<File> fichiersJava = new ArrayList<File>();

        if (tousLesFichiers != null) {
            for (File f : tousLesFichiers) {
                if (f.getName().endsWith(".java"))
                    fichiersJava.add(f);
            }
        }
        return fichiersJava;
    }

    /**
     * Résout les héritages à partir des intentions stockées dans
     * lstIntentionHeritage,
     * en utilisant la map mapClasses pour retrouver les objets ClasseObjet réels.
     * Retourne la liste des HeritageObjet résultantes.
     */
    public List<HeritageObjet> resoudreHeritage(HashMap<String, ClasseObjet> mapClasses) {
        List<HeritageObjet> resultat = new ArrayList<HeritageObjet>();
        HashSet<String> heritagesAjoutes = new HashSet<String>();

        for (String intention : this.lstIntentionHeritage.keySet()) {
            String nomEnfant = intention;
            String nomParent = this.lstIntentionHeritage.get(intention);
            if (nomParent == null)
                continue;

            if (mapClasses.containsKey(nomEnfant) && mapClasses.containsKey(nomParent)) {
                ClasseObjet classeEnfant = mapClasses.get(nomEnfant);
                ClasseObjet classeParent = mapClasses.get(nomParent);

                String cle = nomParent + "->" + nomEnfant;

                if (!heritagesAjoutes.contains(cle)) {
                    resultat.add(new HeritageObjet(classeParent, classeEnfant));
                    heritagesAjoutes.add(cle);
                }
            } else if (!"Object".equals(nomParent))
                System.out.println("Avertissement: Super-classe '" + nomParent +
                        "' déclarée pour '" + nomEnfant +
                        "', mais non trouvée dans le répertoire analysé.");
        }
        return resultat;
    }

    /**
     * Résout les implémentations d'interfaces à partir de lstInterfaces,
     * en utilisant mapClasses pour retrouver les ClasseObjet. Retourne la liste
     * d'InterfaceObjet.
     */
    public List<InterfaceObjet> resoudreImplementation(HashMap<String, ClasseObjet> mapClasses) {
        HashMap<String, InterfaceObjet> regroupement = new HashMap<String, InterfaceObjet>();

        for (String nomClasseConcrète : this.lstInterfaces.keySet()) {
            ArrayList<String> listesInterfaces = this.lstInterfaces.get(nomClasseConcrète);
            if (!mapClasses.containsKey(nomClasseConcrète)) {
                System.out.println("Avertissement: Classe concrète '" + nomClasseConcrète
                        + "' déclarée pour implémentation mais non trouvée.");
                continue;
            }

            ClasseObjet classeConcrète = mapClasses.get(nomClasseConcrète);

            for (String nomInterfaceStr : listesInterfaces) {
                if (nomInterfaceStr == null || nomInterfaceStr.trim().isEmpty())
                    continue;

                if (!mapClasses.containsKey(nomInterfaceStr)) {
                    System.out.println("Avertissement: Interface '" + nomInterfaceStr + "' implémentée par '"
                            + nomClasseConcrète + "' non trouvée dans le répertoire.");
                    continue;
                }

                ClasseObjet interfaceClasse = mapClasses.get(nomInterfaceStr);

                if (!regroupement.containsKey(nomClasseConcrète))
                    regroupement.put(nomClasseConcrète, new InterfaceObjet(classeConcrète));

                regroupement.get(nomClasseConcrète).ajouterInterface(interfaceClasse);
            }
        }
        return new ArrayList<>(regroupement.values());
    }

    /**
     * Réinitialise le compteur de liaisons et renumérote les liaisons fournies.
     */
    public void renumeroterLiaisonsFinales(List<LiaisonObjet> toutesLesLiaisons) {
        LiaisonObjet.reinitialiserCompteur();
        int nouveauCompteur = 1;
        for (LiaisonObjet liaison : toutesLesLiaisons)
            liaison.setNum(nouveauCompteur++);
    }
}