package vue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe responsable de l'optimisation de la disposition des blocs UML
 * selon une organisation hiérarchique verticale.
 *
 * Les héritages et implémentations structurent les couches.
 * Les associations n'influencent pas la hiérarchie.
 */
public class OptimisateurDisposition {

    private static final int ESPACEMENT_HORIZONTAL = 260;
    private static final int ESPACEMENT_VERTICAL = 220;
    private static final int POSITION_X_DEBUT = 80;
    private static final int POSITION_Y_DEBUT = 60;

    /**
     * Applique une disposition hiérarchique UML.
     * Méthode conservée pour compatibilité avec le projet existant.
     *
     * @param blocsClasses liste des blocs de classes
     * @param liaisonsVue  liste des liaisons UML
     */
    public static void appliquerLayoutHierarchique(
            List<BlocClasse> blocsClasses,
            List<LiaisonVue> liaisonsVue) {

        if (blocsClasses == null || blocsClasses.isEmpty()) {
            return;
        }

        // 1. Construire le graphe de dépendances UML
        HashMap<String, Set<String>> dependances =
                construireGrapheDependances(blocsClasses, liaisonsVue);

        // 2. Calcul des couches hiérarchiques
        HashMap<String, Integer> couches =
                calculerCouches(blocsClasses, dependances);

        // 3. Regroupement par couche
        HashMap<Integer, List<BlocClasse>> blocsParCouche =
                groupeParCouche(blocsClasses, couches);

        // 4. Positionnement graphique
        positionnerBlocs(blocsParCouche);
    }

    /**
     * Construit un graphe UML basé uniquement sur
     * l'héritage et l'implémentation.
     */
    private static HashMap<String, Set<String>> construireGrapheDependances(
            List<BlocClasse> blocsClasses,
            List<LiaisonVue> liaisonsVue) {

        HashMap<String, Set<String>> graph = new HashMap<>();

        for (BlocClasse bloc : blocsClasses) {
            graph.put(bloc.getNom(), new HashSet<>());
        }

        for (LiaisonVue liaison : liaisonsVue) {
            if (liaison.getType() == LiaisonVue.TypeLiaison.HERITAGE
                    || liaison.getType() == LiaisonVue.TypeLiaison.IMPLEMENTATION) {

                String parent = liaison.getNomClasseDest();
                String enfant = liaison.getNomClasseOrig();

                if (graph.containsKey(parent)) {
                    graph.get(parent).add(enfant);
                }
            }
        }

        return graph;
    }

    /**
     * Calcule la couche hiérarchique de chaque bloc.
     */
    private static HashMap<String, Integer> calculerCouches(
            List<BlocClasse> blocsClasses,
            HashMap<String, Set<String>> dependances) {

        HashMap<String, Integer> couches = new HashMap<>();

        for (BlocClasse bloc : blocsClasses) {
            couches.put(bloc.getNom(), -1);
        }

        for (BlocClasse bloc : blocsClasses) {
            if (couches.get(bloc.getNom()) == -1) {
                calculerProfondeur(
                        bloc.getNom(),
                        couches,
                        dependances,
                        new HashSet<>());
            }
        }

        return couches;
    }

    /**
     * Calcul récursif de la profondeur hiérarchique.
     */
    private static int calculerProfondeur(
            String nomBloc,
            HashMap<String, Integer> couches,
            HashMap<String, Set<String>> dependances,
            Set<String> enCours) {

        if (couches.get(nomBloc) != -1) {
            return couches.get(nomBloc);
        }

        if (enCours.contains(nomBloc)) {
            couches.put(nomBloc, 0);
            return 0;
        }

        enCours.add(nomBloc);

        int max = 0;
        Set<String> enfants = dependances.get(nomBloc);

        if (enfants != null) {
            for (String enfant : enfants) {
                if (dependances.containsKey(enfant)) {
                    int prof = calculerProfondeur(
                            enfant,
                            couches,
                            dependances,
                            enCours);
                    max = Math.max(max, prof + 1);
                }
            }
        }

        enCours.remove(nomBloc);
        couches.put(nomBloc, max);
        return max;
    }

    /**
     * Groupe les blocs par couche hiérarchique.
     */
    private static HashMap<Integer, List<BlocClasse>> groupeParCouche(
            List<BlocClasse> blocsClasses,
            HashMap<String, Integer> couches) {

        HashMap<Integer, List<BlocClasse>> resultat = new HashMap<>();

        for (BlocClasse bloc : blocsClasses) {
            int couche = couches.get(bloc.getNom());
            resultat.computeIfAbsent(couche, k -> new ArrayList<>()).add(bloc);
        }

        return resultat;
    }

    /**
     * Positionne les blocs graphiquement selon leurs couches.
     */
    private static void positionnerBlocs(
            HashMap<Integer, List<BlocClasse>> blocsParCouche) {

        int largeurPanneau = 1200;

        for (int couche : blocsParCouche.keySet()) {
            List<BlocClasse> blocs = blocsParCouche.get(couche);

            int y = POSITION_Y_DEBUT + couche * ESPACEMENT_VERTICAL;
            int espace = Math.max(
                    ESPACEMENT_HORIZONTAL,
                    largeurPanneau / (blocs.size() + 1));

            int x = POSITION_X_DEBUT;

            for (BlocClasse bloc : blocs) {
                bloc.setX(x);
                bloc.setY(y);
                x += espace;
            }
        }
    }
}
