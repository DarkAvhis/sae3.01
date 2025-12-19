package vue;

import java.util.ArrayList;
import java.util.List;

import src.modele.entites.AssociationObjet;
import src.modele.entites.AttributObjet;
import src.modele.entites.ClasseObjet;
import src.modele.entites.MethodeObjet;

public public class DP {
    
    public static List<BlocClasse> buildBlocs(List<ClasseObjet> modeles, boolean filtrerExternes) {
        List<BlocClasse> blocs = new ArrayList<>();
        for (ClasseObjet c : modeles) {
            if (filtrerExternes && "externe".equals(c.getSpecifique())) continue;
            
            // Création du bloc
            BlocClasse bloc = new BlocClasse(c.getNom());
            bloc.setInterface("interface".equals(c.getSpecifique()));
            
            // On utilise PresentationMapper UNIQUEMENT pour le texte
            for (AttributObjet a : c.getattributs()) {
                bloc.ajouterAttribut(PresentationMapper.formaterAttribut(a));
            }
            for (MethodeObjet m : c.getMethodes()) {
                bloc.ajouterMethode(PresentationMapper.formaterMethode(m));
            }
            blocs.add(bloc);
        }
        return blocs;
    }

    public static List<LiaisonVue> buildLiaisons(List<AssociationObjet> associations, List<BlocClasse> blocs) {
        List<LiaisonVue> vues = new ArrayList<>();
        // Logique de création des flèches ici...
        return vues;
    }
} {
    
}
