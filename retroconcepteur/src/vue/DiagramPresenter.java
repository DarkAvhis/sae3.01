package vue;

import java.util.ArrayList;
import java.util.List;
import modele.entites.AssociationObjet;
import modele.entites.ClasseObjet;
import modele.entites.HeritageObjet;
import modele.entites.InterfaceObjet;

/**
 * Présente les données métier sous forme de blocs et liaisons pour la vue.
 * Cette classe centralise la logique de transformation modèle->vue et permet
 * de garder le contrôleur léger.
 */
public class DiagramPresenter {

    public static List<BlocClasse> buildBlocs(List<ClasseObjet> classes, boolean afficherClassesExternes,
            boolean afficherAttributs, boolean afficherMethodes, int startX, int startY) {
        List<BlocClasse> blocs = new ArrayList<>();

        int x = startX;
        int y = startY;

        for (ClasseObjet c : classes) {
            // Filtre des classes externes
            boolean estExterne = (c.getSpecifique() != null && c.getSpecifique().equals("externe"));
            if (!afficherClassesExternes && estExterne) continue;

            // Conversion des membres selon les flags
            List<String> attrVue = afficherAttributs ? PresentationMapper.convertirAttributs(c.getattributs(), c, classes) : new ArrayList<>();
            List<String> methVue = afficherMethodes ? PresentationMapper.convertirMethodes(c.getMethodes(), c) : new ArrayList<>();

            BlocClasse bloc = new BlocClasse(c.getNom(), x, y, attrVue, methVue);

            if (c.getSpecifique() != null && !c.getSpecifique().isEmpty()) {
                bloc.setTypeSpecifique(c.getSpecifique());
                if (c.getSpecifique().equals("interface")) bloc.setInterface(true);
            } else if (c.getNom().contains("Interface")) {
                bloc.setInterface(true);
                bloc.setTypeSpecifique("interface");
            }

            if (estExterne) bloc.setExterne(true);

            blocs.add(bloc);

            // classes internes affichées avec offset
            int offsetX = 40;
            int offsetY = 180;
            for (ClasseObjet inner : c.getClassesInternes()) {
                // Réutiliser la même logique pour les internes
                List<String> attrInner = afficherAttributs ? PresentationMapper.convertirAttributs(inner.getattributs(), inner, classes) : new ArrayList<>();
                List<String> methInner = afficherMethodes ? PresentationMapper.convertirMethodes(inner.getMethodes(), inner) : new ArrayList<>();
                BlocClasse blocInner = new BlocClasse(inner.getNom(), x + offsetX, y + offsetY, attrInner, methInner);
                if (inner.getSpecifique() != null && inner.getSpecifique().equals("externe")) blocInner.setExterne(true);
                blocs.add(blocInner);
            }

            x += 300;
            if (x > 1200) { x = startX; y += 300; }
        }

        return blocs;
    }

    public static List<LiaisonVue> buildLiaisons(List<AssociationObjet> associations, List<HeritageObjet> heritages,
            List<InterfaceObjet> implementations, List<ClasseObjet> classes) {
        List<LiaisonVue> liaisons = new ArrayList<>();

        liaisons.addAll(PresentationMapper.convertirLiaisons(associations, LiaisonVue.TypeLiaison.ASSOCIATION_UNIDI, classes));
        liaisons.addAll(PresentationMapper.convertirLiaisons(heritages, LiaisonVue.TypeLiaison.HERITAGE, classes));
        liaisons.addAll(PresentationMapper.convertirLiaisons(implementations, LiaisonVue.TypeLiaison.IMPLEMENTATION, classes));

        for (ClasseObjet c : classes) {
            for (ClasseObjet inner : c.getClassesInternes()) {
                liaisons.add(new LiaisonVue(inner.getNom(), c.getNom(), LiaisonVue.TypeLiaison.NESTED, null, null));
            }
        }

        return liaisons;
    }
}
