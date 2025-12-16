package vue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe responsable de l'optimisation des positions des blocs de classe
 * en utilisant un algorithme hiérarchique (Sugiyama-style) qui minimise
 * les croisements de liaisons.
 */
public class OptimisateurDisposition
{
    private static final int ESPACEMENT_HORIZONTAL = 300;
    private static final int ESPACEMENT_VERTICAL   = 250;
    private static final int POSITION_X_DEBUT      = 50;
    private static final int POSITION_Y_DEBUT      = 50;

    /**
     * Applique un algorithme hiérarchique (Sugiyama) pour optimiser la disposition.
     *
     * @param blocsClasses Liste des blocs à optimiser
     * @param liaisonsVue  Liste des liaisons entre blocs
     */
    public static void appliquerLayoutHierarchique(
        List<BlocClasse> blocsClasses,
        List<LiaisonVue> liaisonsVue
    )
    {
        if (blocsClasses == null || blocsClasses.isEmpty())
        {
            return;
        }

        HashMap<String, Set<String>> dependances =
            construireGrapheDependances(blocsClasses, liaisonsVue);

        HashMap<String, Integer> couchesBlocs =
            calculerCouches(blocsClasses, dependances);

        HashMap<Integer, List<BlocClasse>> blocParCouche =
            groupeParCouche(blocsClasses, couchesBlocs);

        positionnerBlocs(blocsClasses, blocParCouche, couchesBlocs);
    }

    /**
     * Construit le graphe des dépendances entre classes.
     */
    private static HashMap<String, Set<String>> construireGrapheDependances(
        List<BlocClasse> blocsClasses,
        List<LiaisonVue> liaisonsVue
    )
    {
        HashMap<String, Set<String>> graph = new HashMap<>();

        for (BlocClasse bloc : blocsClasses)
        {
            graph.put(bloc.getNom(), new HashSet<>());
        }

        for (LiaisonVue liaison : liaisonsVue)
        {
            String source = liaison.getNomClasseOrig();
            String dest   = liaison.getNomClasseDest();

            if (liaison.getType() == LiaisonVue.TypeLiaison.HERITAGE
             || liaison.getType() == LiaisonVue.TypeLiaison.IMPLEMENTATION)
            {
                if (graph.containsKey(dest))
                {
                    graph.get(dest).add(source);
                }
            }
            else
            {
                if (graph.containsKey(source))
                {
                    graph.get(source).add(dest);
                }
            }
        }

        return graph;
    }

    /**
     * Calcule la couche de chaque bloc par profondeur hiérarchique.
     */
    private static HashMap<String, Integer> calculerCouches(
        List<BlocClasse> blocsClasses,
        HashMap<String, Set<String>> dependances
    )
    {
        HashMap<String, Integer> couches = new HashMap<>();

        for (BlocClasse bloc : blocsClasses)
        {
            couches.put(bloc.getNom(), -1);
        }

        for (BlocClasse bloc : blocsClasses)
        {
            if (couches.get(bloc.getNom()) == -1)
            {
                calculerProfondeur(
                    bloc.getNom(),
                    couches,
                    dependances,
                    new HashSet<>()
                );
            }
        }

        return couches;
    }

    /**
     * DFS récursif pour calculer la profondeur.
     */
    private static int calculerProfondeur(
        String nomBloc,
        HashMap<String, Integer> couches,
        HashMap<String, Set<String>> dependances,
        Set<String> enCours
    )
    {
        Integer existante = couches.get(nomBloc);

        if (existante != null && existante != -1)
        {
            return existante;
        }

        if (enCours.contains(nomBloc))
        {
            couches.put(nomBloc, 0);
            return 0;
        }

        enCours.add(nomBloc);

        Set<String> deps = dependances.get(nomBloc);

        if (deps == null || deps.isEmpty())
        {
            couches.put(nomBloc, 0);
            enCours.remove(nomBloc);
            return 0;
        }

        int max = 0;

        for (String dep : deps)
        {
            if (dependances.containsKey(dep))
            {
                int prof =
                    calculerProfondeur(dep, couches, dependances, enCours);

                max = Math.max(max, prof + 1);
            }
        }

        enCours.remove(nomBloc);
        couches.put(nomBloc, max);

        return max;
    }

    /**
     * Groupe les blocs par couche.
     */
    private static HashMap<Integer, List<BlocClasse>> groupeParCouche(
        List<BlocClasse> blocsClasses,
        HashMap<String, Integer> couchesBlocs
    )
    {
        HashMap<Integer, List<BlocClasse>> resultat = new HashMap<>();

        for (BlocClasse bloc : blocsClasses)
        {
            int couche = couchesBlocs.get(bloc.getNom());

            resultat
                .computeIfAbsent(couche, k -> new ArrayList<>())
                .add(bloc);
        }

        return resultat;
    }

    /**
     * Positionne les blocs selon leurs couches.
     */
    private static void positionnerBlocs(
        List<BlocClasse> blocsClasses,
        HashMap<Integer, List<BlocClasse>> blocParCouche,
        HashMap<String, Integer> couchesBlocs
    )
    {
        int maxCouche = blocParCouche.size();
        int largeurPanneau = 1200;

        for (int couche = 0; couche < maxCouche; couche++)
        {
            List<BlocClasse> blocsEnCouche = blocParCouche.get(couche);

            if (blocsEnCouche == null || blocsEnCouche.isEmpty())
            {
                continue;
            }

            int posY = POSITION_Y_DEBUT + couche * ESPACEMENT_VERTICAL;

            trierBlocsParConnexion(blocsEnCouche, blocsClasses, couchesBlocs);

            int nbBlocs = blocsEnCouche.size();
            int espace  = Math.max(250, (largeurPanneau - 100) / nbBlocs);
            int total   = nbBlocs * espace;

            int posXDebut =
                Math.max(POSITION_X_DEBUT, (largeurPanneau - total) / 2);

            for (int i = 0; i < nbBlocs; i++)
            {
                BlocClasse bloc = blocsEnCouche.get(i);

                bloc.setX(posXDebut + i * espace);
                bloc.setY(posY);
            }
        }
    }

    /**
     * Trie les blocs d'une couche pour limiter les croisements.
     */
    private static void trierBlocsParConnexion(
        List<BlocClasse> blocsEnCouche,
        List<BlocClasse> tousLesBlocs,
        HashMap<String, Integer> couchesBlocs
    )
    {
        HashMap<String, Integer> scores = new HashMap<>();

        for (BlocClasse bloc : blocsEnCouche)
        {
            int score = 0;

            for (BlocClasse autre : blocsEnCouche)
            {
                if (!bloc.getNom().equals(autre.getNom()))
                {
                    score += bloc.getNom().compareTo(autre.getNom());
                }
            }

            scores.put(bloc.getNom(), score);
        }

        Collections.sort(
            blocsEnCouche,
            (b1, b2) ->
                scores.get(b1.getNom()).compareTo(scores.get(b2.getNom()))
        );
    }

    /**
     * Layout alternatif en grille.
     */
    public static void appliquerLayoutGrille(List<BlocClasse> blocsClasses)
    {
        if (blocsClasses == null || blocsClasses.isEmpty())
        {
            return;
        }

        int cols = (int) Math.ceil(Math.sqrt(blocsClasses.size()));

        for (int i = 0; i < blocsClasses.size(); i++)
        {
            int col = i % cols;
            int row = i / cols;

            blocsClasses.get(i).setX(POSITION_X_DEBUT + col * ESPACEMENT_HORIZONTAL);
            blocsClasses.get(i).setY(POSITION_Y_DEBUT + row * ESPACEMENT_VERTICAL);
        }
    }

    /**
     * Layout circulaire.
     */
    public static void appliquerLayoutCirculaire(
        List<BlocClasse> blocsClasses,
        int indexPrincipal
    )
    {
        if (blocsClasses == null || blocsClasses.isEmpty())
        {
            return;
        }

        int centreX = 500;
        int centreY = 400;
        int rayon   = 250;

        if (indexPrincipal >= 0 && indexPrincipal < blocsClasses.size())
        {
            BlocClasse centre = blocsClasses.get(indexPrincipal);

            centre.setX(centreX - centre.getLargeur() / 2);
            centre.setY(centreY - centre.getHauteur() / 2);
        }

        int nb = blocsClasses.size() - 1;

        for (int i = 0; i < blocsClasses.size(); i++)
        {
            if (i == indexPrincipal)
            {
                continue;
            }

            double angle =
                (2 * Math.PI * (i < indexPrincipal ? i : i - 1)) / nb;

            BlocClasse bloc = blocsClasses.get(i);

            bloc.setX(
                (int) (centreX + rayon * Math.cos(angle))
                - bloc.getLargeur() / 2
            );

            bloc.setY(
                (int) (centreY + rayon * Math.sin(angle))
                - bloc.getHauteur() / 2
            );
        }
    }
}
