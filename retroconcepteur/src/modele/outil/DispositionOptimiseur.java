package modele.outil;

import java.awt.Point;
import java.util.*;
import modele.entites.ClasseObjet;
import vue.LiaisonVue;

/**
 * Unique classe responsable du calcul des positions du diagramme.
 */
public class DispositionOptimiseur 
{
    // Constantes de mise en page (centralisées ici)
    private static final int ESPACEMENT_X = 260;
    private static final int ESPACEMENT_Y = 220;
    private static final int MARGE_X      = 80 ;
    private static final int MARGE_Y      = 60 ;
    private static final int ITERATIONS   = 5  ;

    /**
     * Calcule la map des positions pour chaque classe du projet.
     */
    public static Map<String, Point> calculerPositions(List<ClasseObjet> classes, List<LiaisonVue> liaisons) 
    {
        if (classes == null || classes.isEmpty()) return new HashMap<>();

        // 1. Assigner les couches (niveaux verticaux)
        Map<String, Integer> couches = assignerCouches(classes, liaisons);

        // 2. Grouper les noms de classes par niveau
        Map<Integer, List<String>> classesParCouche = new TreeMap<>();

        for (ClasseObjet c : classes) 
        {
            int niveau = couches.getOrDefault(c.getNom(), 0);
            classesParCouche.computeIfAbsent(niveau, k -> new ArrayList<>()).add(c.getNom());
        }

        // 3. Optimiser l'ordre horizontal (barycentre) pour réduire les croisements
        for (int i = 0; i < DispositionOptimiseur.ITERATIONS; i++) 
            optimiserOrdreHorizontal(classesParCouche, liaisons);

        // 4. Convertir en coordonnées réelles
        Map<String, Point> positions = new HashMap<>();

        for (Map.Entry<Integer, List<String>> entry : classesParCouche.entrySet()) 
        {
            int niveauY = entry.getKey();
            List<String> noms = entry.getValue();

            for (int i = 0; i < noms.size(); i++) 
            {
                int posX = DispositionOptimiseur.MARGE_X + (i       * DispositionOptimiseur.ESPACEMENT_X);
                int posY = DispositionOptimiseur.MARGE_Y + (niveauY * DispositionOptimiseur.ESPACEMENT_Y);

                positions.put(noms.get(i), new Point(posX, posY));
            }
        }
        return positions;
    }

    private static Map<String, Integer> assignerCouches(List<ClasseObjet> classes, List<LiaisonVue> liaisons) 
    {
        Map<String, Integer> couches = new HashMap<>();
        classes.forEach(c -> couches.put(c.getNom(), 0));

        boolean changement = true;

        while (changement) 
        {
            changement = false;

            for (LiaisonVue l : liaisons) 
            {
                if (l.getType() == LiaisonVue.TypeLiaison.HERITAGE || l.getType() == LiaisonVue.TypeLiaison.IMPLEMENTATION) 
                {
                    int niveauParent = couches.getOrDefault(l.getNomClasseDest(), 0);
                    int niveauEnfant = couches.getOrDefault(l.getNomClasseOrig(), 0);

                    if (niveauEnfant <= niveauParent) 
                    {
                        couches.put(l.getNomClasseOrig(), niveauParent + 1);
                        changement = true;
                    }
                }
            }
        }
        return couches;
    }

    private static void optimiserOrdreHorizontal(Map<Integer, List<String>> couches, List<LiaisonVue> liaisons) 
    {
        List<Integer> niveaux = new ArrayList<>(couches.keySet());

        for (int i = 1; i < niveaux.size(); i++) 
        {
            List<String> coucheFixe     = couches.get(niveaux.get(i - 1));
            List<String> coucheAMouvoir = couches.get(niveaux.get(i));
            
            coucheAMouvoir.sort(Comparator.comparingDouble(nom -> calculerBarycentre(nom, coucheFixe, liaisons)));
        }
    }

    private static double calculerBarycentre(String nom, List<String> couchePrecedente, List<LiaisonVue> liaisons) 
    {
        double sommePositions = 0;
        int    connexions     = 0;

        for (LiaisonVue l : liaisons) 
        {
            if (l.getNomClasseOrig().equals(nom) && couchePrecedente.contains(l.getNomClasseDest())) 
            {
                sommePositions += couchePrecedente.indexOf(l.getNomClasseDest());
                connexions++;
            }
        }
        return connexions == 0 ? 0 : sommePositions / connexions;
    }
}