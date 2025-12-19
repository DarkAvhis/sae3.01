package vue;

import java.util.ArrayList;
import java.util.List;
import modele.entites.*;

public class DiagramPresenter {

    public static List<BlocClasse> buildBlocs(List<ClasseObjet> classes, boolean afficherClassesExternes,
            boolean afficherAttributs, boolean afficherMethodes, int startX, int startY) {
        List<BlocClasse> blocs = new ArrayList<>();
        int x = startX;
        int y = startY;

        for (ClasseObjet c : classes) {
            // Utilisation stricte du stéréotype pour le filtrage
            boolean estExterne = "externe".equals(c.getSpecifique());
            if (!afficherClassesExternes && estExterne) continue;

            // Préparation des membres via le PresentationMapper centralisé
            List<String> attrVue = afficherAttributs ? 
                PresentationMapper.convertirAttributs(c.getAttributs(), c, classes) : new ArrayList<>();
            List<String> methVue = afficherMethodes ? 
                PresentationMapper.convertirMethodes(c.getMethodes(), c) : new ArrayList<>();

            BlocClasse bloc = new BlocClasse(c.getNom(), x, y, attrVue, methVue);

            // Définition propre du type (interface, abstract, record) sans se fier au nom du fichier
            if (c.getSpecifique() != null) {
                bloc.setTypeSpecifique(c.getSpecifique());
                if ("interface".equals(c.getSpecifique())) bloc.setInterface(true);
            }
            
            bloc.setExterne(estExterne);
            blocs.add(bloc);

            // Gestion des classes internes (ex: TestInterne dans Disque.java)
            for (ClasseObjet inner : c.getClassesInternes()) {
                List<String> iAttr = afficherAttributs ? 
                    PresentationMapper.convertirAttributs(inner.getAttributs(), inner, classes) : new ArrayList<>();
                List<String> iMeth = afficherMethodes ? 
                    PresentationMapper.convertirMethodes(inner.getMethodes(), inner) : new ArrayList<>();
                
                BlocClasse blocInner = new BlocClasse(inner.getNom(), x + 40, y + 180, iAttr, iMeth);
                blocInner.setExterne("externe".equals(inner.getSpecifique()));
                blocs.add(blocInner);
            }

            // Grille automatique pour éviter que les blocs ne se superposent
            x += 350;
            if (x > 1400) { x = startX; y += 400; }
        }
        return blocs;
    }

    public static List<LiaisonVue> buildLiaisons(List<AssociationObjet> associations, List<HeritageObjet> heritages,
            List<InterfaceObjet> implementations, List<ClasseObjet> classes) {
        List<LiaisonVue> liaisons = new ArrayList<>();

        // Utilisation des méthodes de conversion du PresentationMapper
        liaisons.addAll(PresentationMapper.convertirLiaisons(associations, LiaisonVue.TypeLiaison.ASSOCIATION_UNIDI, classes));
        liaisons.addAll(PresentationMapper.convertirLiaisons(heritages, LiaisonVue.TypeLiaison.HERITAGE, classes));
        liaisons.addAll(PresentationMapper.convertirLiaisons(implementations, LiaisonVue.TypeLiaison.IMPLEMENTATION, classes));

        // Ajout automatique des liens de contenance pour les classes internes
        for (ClasseObjet c : classes) {
            for (ClasseObjet inner : c.getClassesInternes()) {
                liaisons.add(new LiaisonVue(inner.getNom(), c.getNom(), LiaisonVue.TypeLiaison.NESTED, null, null));
            }
        }
        return liaisons;
    }
}