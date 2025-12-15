package modele;

import static modele.outil.ParsingUtil.*;

import java.io.File;
import java.io.FileNotFoundException;
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
import modele.entites.MethodeObjet;
import modele.entites.MultipliciteObjet;

/**
 * Classe utilitaire responsable de l'analyse syntaxique (parsing) manuelle des
 * fichiers Java.
 */
public class AnalyseurUML {
    private static final int MULT_INDEFINIE = 999999999;

    // Listes pour stocker les relations en attente de résolution
    private HashMap<String, String> lstIntentionHeritage = new HashMap<String, String>();
    private HashMap<String, ArrayList<String>> lstInterfaces = new HashMap<String, ArrayList<String>>();

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
        ArrayList<AttributObjet> attributs = new ArrayList<AttributObjet>();
        ArrayList<MethodeObjet> methodes = new ArrayList<MethodeObjet>();

        String nomParent = null;
        String nomEntite = nomFichier;
        String specifique = "";

        String ligneBrute;

        boolean estInterface = false;
        boolean estRecord = false;
        boolean enTeteTrouve = false;
        boolean commentaireBlocActif = false;
        boolean estHeritier = false;

        int finCom;

        ArrayList<String> interfacesDetectees = new ArrayList<String>();
        String[] tabSpecifique = { "abstract class", "interface", "enum", "record" };

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                ligneBrute = sc.nextLine().trim();
                if (ligneBrute.isEmpty()) {
                    continue;
                }

                if (commentaireBlocActif) {
                    if (ligneBrute.contains("*/")) {
                        commentaireBlocActif = false;
                        if (ligneBrute.endsWith("*/")) {
                            continue;
                        }
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
                        finCom = ligneBrute.lastIndexOf("*/");
                        if (finCom + 2 < ligneBrute.length())
                            ligneBrute = ligneBrute.substring(finCom + 2).trim();
                        else {
                            continue;
                        }
                    }
                }

                if (ligneBrute.startsWith("//")) {
                    continue;
                }

                if (ligneBrute.contains("//")) {
                    ligneBrute = ligneBrute.substring(0, ligneBrute.indexOf("//")).trim();
                }
                // ignorer package / import
                if (ligneBrute.startsWith("package") || ligneBrute.startsWith("import")) {
                    continue;
                }

                if (ligneBrute.equals("}")) {
                    continue;
                }

                if (!enTeteTrouve && (ligneBrute.contains(" class ") ||
                        ligneBrute.contains("interface ") ||
                        ligneBrute.contains("record ") ||
                        ligneBrute.contains("enum "))) {
                    enTeteTrouve = true;

                    for (String motCle : tabSpecifique) {
                        if (ligneBrute.contains(motCle)) {
                            specifique = motCle;
                            if (motCle.contains("interface"))
                                estInterface = true;
                            if (motCle.contains("record"))
                                estRecord = true;

                            if (estInterface || estRecord) {
                                int idxDebut = ligneBrute.indexOf(motCle) + motCle.length();
                                String suite = ligneBrute.substring(idxDebut).trim();
                                String nom = lireNom(suite);

                                if (!nom.isEmpty())
                                    nomEntite = nom;
                            }
                            break;
                        }
                    }

                    if (ligneBrute.contains("extends")) {
                        estHeritier = true;

                        int idxExtends = ligneBrute.indexOf("extends") + 7;
                        String suite = ligneBrute.substring(idxExtends).trim();

                        String possibleParent = lireNom(suite);

                        if (!possibleParent.isEmpty())
                            nomParent = possibleParent;
                    }

                    if (ligneBrute.contains("implements")) {
                        int idxImpl = ligneBrute.indexOf("implements") + 10;
                        String suite = ligneBrute.substring(idxImpl).trim();
                        int idxFin = suite.indexOf('{');
                        if (idxFin != -1)
                            suite = suite.substring(0, idxFin);

                        int indexDebut = 0;
                        int indexVirgule;

                        while (indexDebut < suite.length()) {
                            indexVirgule = suite.indexOf(',', indexDebut);
                            if (indexVirgule == -1)
                                indexVirgule = suite.length();
                            String interBrute = suite.substring(indexDebut, indexVirgule).trim();
                            if (!interBrute.isEmpty()) {
                                int idxSpace = indexEspace(interBrute);
                                int idxChevron = interBrute.indexOf('<');
                                int idxFinNom = interBrute.length();
                                if (idxSpace != -1)
                                    idxFinNom = Math.min(idxFinNom, idxSpace);
                                if (idxChevron != -1)
                                    idxFinNom = Math.min(idxFinNom, idxChevron);
                                String nomInterface = interBrute.substring(0, idxFinNom).trim();
                                if (!nomInterface.isEmpty())
                                    interfacesDetectees.add(nomInterface);
                            }
                            indexDebut = indexVirgule + 1;
                            if (indexVirgule == suite.length())
                                break;
                        }
                    }

                    if (estRecord) {
                        if (ligneBrute.contains("(") && ligneBrute.contains(")")) {
                            String args = ligneBrute.substring(ligneBrute.indexOf('(') + 1,
                                    ligneBrute.lastIndexOf(')'));
                            args = args.trim();
                            if (!args.isEmpty()) {
                                List<String> params = decoupage(args);
                                for (String p : params) {
                                    String trimmed = p.trim();
                                    List<String> tokens = separerMots(trimmed);
                                    if (tokens.size() >= 2) {
                                        String nom = tokens.get(tokens.size() - 1);
                                        String type = "";
                                        for (int i = 0; i < tokens.size() - 1; i++) {
                                            if (i > 0)
                                                type += ' ';
                                            type += tokens.get(i);
                                        }
                                        attributs.add(new AttributObjet(nom, "instance", type, "private", false, true));
                                        HashMap<String, String> emptyParams = new HashMap<String, String>();
                                        methodes.add(new MethodeObjet(nom, emptyParams, type, "public", false));
                                    }
                                }
                            }
                        }
                        continue;
                    }
                    continue;
                }

                if (enTeteTrouve) {
                    boolean estStatique = ligneBrute.contains("static");
                    boolean estFinal = ligneBrute.contains("final");
                    boolean aVisibilite = (ligneBrute.startsWith("public") ||
                            ligneBrute.startsWith("private") ||
                            ligneBrute.startsWith("protected"));

                    if (estInterface) {
                        if (ligneBrute.endsWith(";") && !ligneBrute.contains("(")) {
                            extraireAttribut(ligneBrute, true, true, attributs);
                        } else if (ligneBrute.contains("(") && ligneBrute.contains(")")) {
                            extraireMethode(ligneBrute, estStatique, nomEntite, methodes);
                        }
                    } else if (!estRecord) {
                        if (aVisibilite && ligneBrute.endsWith(";") && !ligneBrute.contains("(")) {
                            extraireAttribut(ligneBrute, estStatique, estFinal, attributs);
                        }

                        else if (aVisibilite && ligneBrute.contains("(") && !ligneBrute.contains("class ")) {
                            extraireMethode(ligneBrute, estStatique, nomEntite, methodes);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Fichier non trouvé: " + chemin);
            return null;
        }

        ClasseObjet nouvelleClasse = new ClasseObjet(attributs, methodes, nomEntite, specifique);

        if (estHeritier && nomParent != null) {
            this.lstIntentionHeritage.put(nomEntite, nomParent);
        }
        for (String iface : interfacesDetectees) {
            if (!this.lstInterfaces.containsKey(nomEntite)) {
                this.lstInterfaces.put(nomEntite, new ArrayList<String>());
            }
            this.lstInterfaces.get(nomEntite).add(iface);
        }
        return nouvelleClasse;
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
                String typeExterne = typeAttribut;
                MultipliciteObjet multCible = new MultipliciteObjet(1, 1);
                MultipliciteObjet multOrigine = new MultipliciteObjet(1, 1);
                boolean estCollection = false;

                if (typeAttribut.contains("<") && typeAttribut.contains(">")) {
                    int idx1 = typeAttribut.indexOf('<');
                    int idx2 = typeAttribut.indexOf('>', idx1 + 1);
                    if (idx1 != -1 && idx2 != -1 && idx2 > idx1) {
                        typeCible = typeAttribut.substring(idx1 + 1, idx2).trim();
                        typeExterne = typeAttribut.substring(0, idx1).trim();
                    }
                    estCollection = true;
                } else if (typeAttribut.endsWith("[]")) {
                    typeCible = typeAttribut.replace("[]", "").trim();
                    typeExterne = typeCible;
                    estCollection = true;
                } else {
                    int idxChevronUnique = typeAttribut.indexOf('<');
                    if (idxChevronUnique != -1) {
                        typeExterne = typeAttribut.substring(0, idxChevronUnique).trim();
                    }
                }

                if (typeExterne.contains("<")) {
                    typeExterne = typeExterne.substring(0, typeExterne.indexOf('<')).trim();
                }

                if (estCollection)
                    multCible = new MultipliciteObjet(0, MULT_INDEFINIE);

                // Association vers le type externe direct (ex: ArrayList, JMenuBar)
                if (mapClasses.containsKey(typeExterne) && !typeExterne.equals(classeOrigine.getNom())) {
                    ClasseObjet classeExterne = mapClasses.get(typeExterne);

                    AssociationObjet associationExterne = new AssociationObjet(classeExterne, classeOrigine,
                            new MultipliciteObjet(1, 1), multOrigine, attribut.getNom(), true);
                    associationsUnidirectionnelles.add(associationExterne);
                }

                if (mapClasses.containsKey(typeCible) && !typeCible.equals(classeOrigine.getNom())
                        && !typeCible.equals(typeExterne)) {
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