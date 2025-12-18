package vue;

import java.util.ArrayList;
import java.util.List;

import modele.entites.AssociationObjet;
import modele.entites.AttributObjet;
import modele.entites.ClasseObjet;
import modele.entites.InterfaceObjet;
import modele.entites.LiaisonObjet;
import modele.entites.MethodeObjet;

/**
 * Classe utilitaire pour convertir les objets métier en représentation vue.
 */
public class PresentationMapper {

    public static List<LiaisonVue> convertirLiaisons(List<? extends LiaisonObjet> liaisons, LiaisonVue.TypeLiaison type,
            List<ClasseObjet> classes) {
        List<LiaisonVue> liaisonsVue = new ArrayList<>();

        for (LiaisonObjet liaison : liaisons) {
            if (liaison instanceof InterfaceObjet) {
                InterfaceObjet interfaceLiaison = (InterfaceObjet) liaison;
                if (interfaceLiaison.getClasseFille() == null)
                    continue;
                String nomClasseConcrete = interfaceLiaison.getClasseFille().getNom();
                for (ClasseObjet interfaceClass : interfaceLiaison.getLstInterfaces()) {
                    if (interfaceClass != null) {
                        liaisonsVue.add(new LiaisonVue(nomClasseConcrete, interfaceClass.getNom(), type, null, null));
                    }
                }
            } else {
                if (liaison.getClasseFille() == null || liaison.getClasseMere() == null)
                    continue;

                String nomOrig = liaison.getClasseFille().getNom();
                String nomDest = liaison.getClasseMere().getNom();
                String multOrig = null;
                String multDest = null;

                if (liaison instanceof AssociationObjet) {
                    AssociationObjet asso = (AssociationObjet) liaison;
                    multOrig = asso.getMultOrig() != null ? asso.getMultOrig().toString() : "1..1";
                    multDest = asso.getMultDest() != null ? asso.getMultDest().toString() : "1..1";

                    List<String> props = new ArrayList<>();
                    if (asso.isFrozen())
                        props.add("frozen");
                    if (asso.isAddOnly())
                        props.add("addOnly");
                    if (asso.isRequete())
                        props.add("requête");
                    String propsStr = props.isEmpty() ? "" : "{" + String.join(", ", props) + "}";

                    liaisonsVue.add(new LiaisonVue(
                            nomOrig, nomDest, type, multOrig, multDest,
                            asso.getRoleOrig(), asso.getRoleDest(),
                            asso.getRoleOrigOffsetAlong(), asso.getRoleOrigOffsetPerp(),
                            asso.getRoleDestOffsetAlong(), asso.getRoleDestOffsetPerp(),
                            propsStr, ""
                    ));
                } else {
                    liaisonsVue.add(new LiaisonVue(nomOrig, nomDest, type, multOrig, multDest));
                }
            }
        }

        // Liaisons issues des attributs
        for (ClasseObjet c : classes) {
            for (AttributObjet att : c.getattributs()) {
                String typeAtt = att.getType();
                String typeSimple = typeAtt;
                if (typeAtt.endsWith("[]"))
                    typeSimple = typeAtt.replace("[]", "");
                else if (typeAtt.contains("<") && typeAtt.contains(">"))
                    typeSimple = typeAtt.substring(typeAtt.indexOf("<") + 1, typeAtt.indexOf(">"));

                for (ClasseObjet cible : classes) {
                    if (cible.getNom().equals(typeSimple) && !cible.getNom().equals(c.getNom())) {
                        boolean existe = false;
                        for (LiaisonVue lv : liaisonsVue) {
                            if ((lv.getNomClasseOrig().equals(c.getNom()) && lv.getNomClasseDest().equals(cible.getNom())) ||
                                (lv.getNomClasseOrig().equals(cible.getNom()) && lv.getNomClasseDest().equals(c.getNom()))) {
                                existe = true;
                                break;
                            }
                        }
                        if (existe)
                            continue;

                        liaisonsVue.add(new LiaisonVue(
                                c.getNom(),
                                cible.getNom(),
                                LiaisonVue.TypeLiaison.ASSOCIATION_UNIDI,
                                null,
                                null,
                                att.getNom(),
                                null,
                                0,0,0,0,
                                "", ""
                        ));
                    }
                }
            }
        }

        return liaisonsVue;
    }

    public static List<String> convertirAttributs(List<AttributObjet> attributs, ClasseObjet classe, List<ClasseObjet> classesProjet) {
        List<String> liste = new ArrayList<>();
        List<String> nomsClassesProjet = new ArrayList<>();
        for (ClasseObjet c : classesProjet) nomsClassesProjet.add(c.getNom());

        for (AttributObjet att : attributs) {
            String type = att.getType();
            String typeSimple = type;
            if (type.contains("<") && type.contains(">")) {
                int idx1 = type.indexOf('<');
                int idx2 = type.indexOf('>', idx1 + 1);
                if (idx1 != -1 && idx2 != -1 && idx2 > idx1) {
                    typeSimple = type.substring(idx1 + 1, idx2).trim();
                }
            } else if (type.endsWith("[]")) {
                typeSimple = type.replace("[]", "").trim();
            }

            if (nomsClassesProjet.contains(typeSimple) && !typeSimple.equals(classe.getNom())) {
                continue;
            }

            String staticFlag = att.estStatique() ? " {static}" : "";
            String finalFlag = att.estFinale() ? " {final}" : "";
            char visibilite = classe.changementVisibilite(att.getVisibilite());

            String mult = "";
            if (att.getMultiplicite() != null) {
                mult = " [" + att.getMultiplicite().toString() + "]";
            }

            List<String> props = new ArrayList<>();
            if (att.estFrozen()) props.add("frozen");
            if (att.estAddOnly()) props.add("addOnly");
            if (att.estRequete()) props.add("requête");
            String propsStr = props.isEmpty() ? "" : " {" + String.join(", ", props) + "}";

            String s = visibilite + " " + att.getNom() + " : " + att.getType() + mult + staticFlag + finalFlag
                    + propsStr;
            liste.add(s);
        }
        return liste;
    }

    public static List<String> convertirMethodes(List<MethodeObjet> methodes, ClasseObjet classe) {
        List<String> liste = new ArrayList<>();
        for (MethodeObjet met : methodes) {
            String staticFlag = met.estStatique() ? "{static} " : "";
            char visibilite = classe.changementVisibilite(met.getVisibilite());

            String params = classe.affichageParametre(met.getParametres());
            String retour = classe.retourType(met.getRetourType());

            String s = visibilite + staticFlag + met.getNom() + params + retour;
            liste.add(s);
        }

        java.util.Set<String> nomsMethodesExistants = new java.util.HashSet<String>();
        for (MethodeObjet met : methodes) {
            if (met.getNom() != null) nomsMethodesExistants.add(met.getNom());
        }

        for (AttributObjet att : classe.getattributs()) {
            String nomAttr = att.getNom();
            if (nomAttr == null || nomAttr.isEmpty()) continue;

            String nomGetter;
            String type = att.getType() != null ? att.getType().trim() : "";
            if (type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("Boolean")) {
                nomGetter =  nomAttr;
            } else {
                nomGetter = nomAttr;
            }

            if (nomsMethodesExistants.contains(nomGetter)) continue;

            char vis = classe.changementVisibilite("public");
            String retour = classe.retourType(type);
            String s = vis + " " + nomGetter + "()" + retour;
            liste.add(s);
        }
        return liste;
    }

    public static BlocClasse creerBlocComplet(ClasseObjet c, int x, int y) {
        BlocClasse bloc = new BlocClasse(c.getNom(), x, y , new ArrayList<>(), new ArrayList<>());

        if (c.getNom().contains("Interface") || (c.getSpecifique() != null && c.getSpecifique().equals("interface"))) {
            bloc.setInterface(true);
            bloc.setTypeSpecifique("interface");
        } else if (c.getSpecifique() != null && (c.getSpecifique().equals("record") || c.getSpecifique().equals("abstract class"))) {
            bloc.setTypeSpecifique(c.getSpecifique());
        }
        if (c.getSpecifique() != null && c.getSpecifique().equals("externe"))
            bloc.setExterne(true);

        return bloc;
    }
}
