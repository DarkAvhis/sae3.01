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
 * Classe utilitaire permettant de convertir les objets métier
 * en objets exploitables par la vue.
 */
public class PresentationMapper
{
    public static List<LiaisonVue> convertirLiaisons(List<? extends LiaisonObjet> liaisons,
                                                 LiaisonVue.TypeLiaison type,
                                                 List<ClasseObjet> classes)
    {
        List<LiaisonVue> liaisonsVue = new ArrayList<>();

        for (LiaisonObjet liaison : liaisons)
        {
            if (liaison instanceof InterfaceObjet)
            {
                InterfaceObjet interfaceLiaison = (InterfaceObjet) liaison;
                if (interfaceLiaison.getClasseFille() == null) continue;

                String nomClasseConcrete = interfaceLiaison.getClasseFille().getNom();
                for (ClasseObjet interfaceClass : interfaceLiaison.getLstInterfaces())
                {
                    if (interfaceClass != null)
                    {
                        liaisonsVue.add(new LiaisonVue(nomClasseConcrete, interfaceClass.getNom(),
                                                    type, null, null));
                    }
                }
            }
            else
            {
                if (liaison.getClasseFille() == null || liaison.getClasseMere() == null) continue;

                String nomOrig = liaison.getClasseFille().getNom();
                String nomDest = liaison.getClasseMere().getNom();
                String multOrig = "";
                String multDest = "";

                if (liaison instanceof AssociationObjet)
                {
                    AssociationObjet asso = (AssociationObjet) liaison;

                    // On récupère les multiplicités (chaîne vide si null pour ne pas afficher "1..1" partout)
                    multOrig = (asso.getMultOrig() != null) ? asso.getMultOrig().toString() : "";
                    multDest = (asso.getMultDest() != null) ? asso.getMultDest().toString() : "";

                    // On détermine le type exact (UNI ou BIDI)
                    LiaisonVue.TypeLiaison typeActuel = asso.getUnidirectionnel() ? 
                                                    LiaisonVue.TypeLiaison.ASSOCIATION_UNIDI : 
                                                    LiaisonVue.TypeLiaison.ASSOCIATION_BIDI;

                    List<String> props = new ArrayList<>();
                    if (asso.isFrozen())  props.add("frozen");
                    if (asso.isAddOnly()) props.add("addOnly");
                    if (asso.isRequete()) props.add("requête");

                    String propsStr = props.isEmpty() ? "" : "{" + String.join(", ", props) + "}";

                    // On transmet les rôles (noms d'attributs) pour qu'ils s'affichent sur la ligne
                    liaisonsVue.add(new LiaisonVue(nomOrig, nomDest, typeActuel, multOrig, multDest, 
                                                asso.getRoleOrig(), asso.getRoleDest(), 
                                                asso.getRoleOrigOffsetAlong(), asso.getRoleOrigOffsetPerp(),
                                                asso.getRoleDestOffsetAlong(), asso.getRoleDestOffsetPerp(),
                                                propsStr, ""));
                }
                else
                {
                    // Héritage ou autres liens simples
                    liaisonsVue.add(new LiaisonVue(nomOrig, nomDest, type, multOrig, multDest));
                }
            }
        }
        return liaisonsVue;
    }

    
    public static List<String> convertirAttributs(List<AttributObjet> attributs, ClasseObjet classe, List<ClasseObjet> classesProjet)
    {
        List<String> liste = new ArrayList<>();
        
        for (AttributObjet att : attributs)
        {
            String type = att.getType();
            String typeSimple = type;

            if (type.contains("<") && type.contains(">"))
            {
                int idx1 = type.indexOf('<');
                int idx2 = type.lastIndexOf('>');
                typeSimple = type.substring(idx1 + 1, idx2).trim();
                
                int virgule = typeSimple.indexOf(',');
                if (virgule != -1) typeSimple = typeSimple.substring(virgule + 1).trim();
            }
            else if (type.endsWith("[]"))
            {
                typeSimple = type.substring(0, type.length() - 2).trim();
            }

            boolean estLiaison = false;
            for (ClasseObjet c : classesProjet)
            {
                if (c.getNom().equals(typeSimple) && !typeSimple.equals(classe.getNom()))
                {
                    estLiaison = true;
                    break;
                }
            }
            if (estLiaison) continue;

            char visibilite = classe.changementVisibilite(att.getVisibilite());
            String staticFlag = att.estStatique() ? " {static}" : "";
            String finalFlag  = att.estFinale()   ? " {final}"  : "";
            
            liste.add(visibilite + " " + att.getNom() + " : " + type + staticFlag + finalFlag);
        }
        return liste;
    }

    public static List<String> convertirMethodes(List<MethodeObjet> methodes, ClasseObjet classe)
    {
        List<String> liste = new ArrayList<>();

        for (MethodeObjet met : methodes)
        {
            char visibilite = classe.changementVisibilite(met.getVisibilite());
            String staticFlag = met.estStatique() ? "{static} " : "";
            String params = classe.affichageParametre(met.getParametres());
            String retour = classe.retourType(met.getRetourType());

            liste.add(visibilite + staticFlag + met.getNom() + params + retour);
        }
        return liste;
    }

    public static BlocClasse creerBlocComplet(ClasseObjet c, int x, int y)
    {
        // On initialise le bloc avec le nom de la classe
        BlocClasse bloc = new BlocClasse(c.getNom(), x, y, new ArrayList<>(), new ArrayList<>());

        // Gestion des stéréotypes pour l'affichage (en dessous du nom)
        if ("interface".equals(c.getSpecifique())) 
        {
            bloc.setInterface(true);
            bloc.setTypeSpecifique("interface");
        }
        else if ("record".equals(c.getSpecifique())) 
        {
            bloc.setTypeSpecifique("record");
        }
        else if ("enum".equals(c.getSpecifique())) // <-- AJOUTER CE BLOC
        {
            bloc.setTypeSpecifique("enum");
        }
        else if ("abstract class".equals(c.getSpecifique()))
        {
            bloc.setTypeSpecifique("abstract class");
        }

        // Gestion du flag pour les classes externes
        if ("externe".equals(c.getSpecifique()))
        {
            bloc.setExterne(true);
        }

        return bloc;
    }
}
