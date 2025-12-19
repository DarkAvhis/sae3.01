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

    public ClasseObjet analyserFichierUnique(String chemin) 
    {
        File file = new File(chemin);
        String nomFichier = file.getName().replace(".java", "");

        // Pile pour gérer les classes internes et le contexte de parsing
        java.util.Stack<ClasseObjet> pileClasses = new java.util.Stack<>();
        ClasseObjet classeRacine = null;
        int niveauAccolades = 0;

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String ligneBrute = sc.nextLine().trim();
                if (ligneBrute.isEmpty() || ligneBrute.startsWith("//") || ligneBrute.startsWith("package") || ligneBrute.startsWith("import")) {
                    continue;
                }

                // 1. Détection de déclaration (Class, Interface, Record, Enum)
                // On utilise ParsingUtil pour identifier le type de structure
                String stereotype = ParsingUtil.identifierStereotype(ligneBrute);
                
                // Vérification si la ligne contient une nouvelle déclaration d'entité
                if (ligneBrute.contains("class ") || ligneBrute.contains("interface ") || 
                    ligneBrute.contains("record ") || ligneBrute.contains("enum ")) {
                    
                    // Extraction du nom via ParsingUtil
                    String nomEntite = ParsingUtil.lireNom(ligneBrute.substring(ligneBrute.indexOf(
                        ligneBrute.contains("interface") ? "interface" : 
                        (ligneBrute.contains("record") ? "record" : "class")
                    )).trim().split("\\s+")[1]);

                    if (nomEntite.isEmpty()) nomEntite = nomFichier;

                    ClasseObjet nouvelleClasse = new ClasseObjet(new ArrayList<>(), new ArrayList<>(), nomEntite, stereotype);

                    // Gestion de la hiérarchie : si la pile n'est pas vide, c'est une classe interne
                    if (classeRacine == null) {
                        classeRacine = nouvelleClasse;
                    } else if (!pileClasses.isEmpty()) {
                        pileClasses.peek().ajouterClasseInterne(nouvelleClasse);
                    }

                    pileClasses.push(nouvelleClasse);

                    // Résolution des héritages
                    if (ligneBrute.contains("extends")) {
                        String parent = ParsingUtil.lireNom(ligneBrute.substring(ligneBrute.indexOf("extends") + 7).trim());
                        this.lstIntentionHeritage.put(nomEntite, parent);
                    }

                    // Résolution unique des interfaces (évite la redondance dans PresentationMapper)
                    if (ligneBrute.contains(" implements ")) {
                        String partieImplements = ligneBrute.substring(ligneBrute.indexOf(" implements ") + 12);
                        if (partieImplements.contains("{")) partieImplements = partieImplements.split("\\{")[0];
                        
                        ArrayList<String> interfaces = new ArrayList<>();
                        for (String s : partieImplements.split(",")) {
                            interfaces.add(s.trim().split("<")[0]); // Nettoyage des génériques
                        }
                        this.lstInterfaces.put(nomEntite, interfaces);
                    }

                    if (ligneBrute.contains("{")) niveauAccolades++;
                    continue;
                }

                // 2. Gestion des membres (Attributs et Méthodes)
                // On n'extrait que si on est dans le bloc d'une classe (niveauAccolades == pile.size())
                if (!pileClasses.isEmpty() && niveauAccolades == pileClasses.size()) {
                    ClasseObjet classeCourante = pileClasses.peek();
                    boolean estStatique = ligneBrute.contains("static");
                    boolean estFinal = ligneBrute.contains("final");

                    // Délégation de l'extraction à ParsingUtil pour éviter la duplication de logique
                    if (ligneBrute.endsWith(";") && !ligneBrute.contains("(")) {
                        ParsingUtil.extraireAttribut(ligneBrute, estStatique, estFinal, classeCourante.getAttributs());
                    } 
                    else if (ligneBrute.contains("(") && !ligneBrute.contains("=")) {
                        // Normalisation pour les interfaces (méthodes implicitement publiques)
                        String lignePourMethode = ligneBrute;
                        if (!ligneBrute.contains("public") && "interface".equals(classeCourante.getSpecifique())) {
                            lignePourMethode = "public " + ligneBrute;
                        }
                        ParsingUtil.extraireMethode(lignePourMethode, estStatique, classeCourante.getNom(), classeCourante.getMethodes());
                    }
                }

                // 3. Gestion des accolades pour le dépilage
                if (ligneBrute.contains("{")) niveauAccolades++;
                if (ligneBrute.contains("}")) {
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
            if (classeOrigine.getAttributs() == null)
                continue;

            for (AttributObjet attribut : classeOrigine.getAttributs()) {
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