package modele.outil;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import modele.entites.ClasseObjet;
import vue.BlocClasse;
import vue.LiaisonVue;

/**
 * Algorithme d'optimisation de disposition extrait du contrôleur.
 * Permet de calculer des positions optimales des blocs en séparant la
 * responsabilité du contrôleur vers le modèle / métier.
 */
public class DispositionOptimiseur {
    private static final int H_LAYER_SPACING = 150;
    private static final int W_NODE_SPACING = 100;
    private static final int Y_ANCHOR = 50;
    private static final int X_ANCHOR = 50;
    private static final int ITERATIONS = 10;

    public static HashMap<String, Point> calculerPositionsOptimales(List<ClasseObjet> classes, List<LiaisonVue> liaisons,
            List<BlocClasse> blocsAvecTailles) {
        HashMap<String, BlocClasse> blocMap = new HashMap<>();

        for (BlocClasse bloc : blocsAvecTailles) {
            blocMap.put(bloc.getNom(), bloc);
        }

        HashMap<String, Integer> couches = assignerCouches(classes, liaisons);

        HashMap<Integer, List<String>> classesParCouche = new HashMap<>();
        for (ClasseObjet classe : classes) {
            int couche = couches.get(classe.getNom());
            classesParCouche.computeIfAbsent(couche, k -> new ArrayList<>()).add(classe.getNom());
        }

        List<Integer> indexCouches = new ArrayList<>(classesParCouche.keySet());
        Collections.sort(indexCouches);

        for (int iter = 0; iter < ITERATIONS; iter++) {
            for (int i = 0; i < indexCouches.size(); i++) {
                int coucheCourante = indexCouches.get(i);

                if (i + 1 < indexCouches.size()) {
                    int coucheSuivante = indexCouches.get(i + 1);
                    minimiserCroisements(coucheCourante, classesParCouche.get(coucheCourante),
                            classesParCouche.get(coucheSuivante), liaisons, blocMap, true);
                }

                if (i > 0) {
                    int couchePrecedente = indexCouches.get(i - 1);
                    minimiserCroisements(coucheCourante, classesParCouche.get(coucheCourante),
                            classesParCouche.get(couchePrecedente), liaisons, blocMap, false);
                }
            }
        }

        HashMap<String, Point> positions = new HashMap<>();
        int y_courant = Y_ANCHOR;

        for (int couche : indexCouches) {
            List<String> nomsCouche = classesParCouche.get(couche);

            int x_courant = X_ANCHOR;
            int max_height_couche = 0;

            for (int i = 0; i < nomsCouche.size(); i++) {
                String nom = nomsCouche.get(i);
                BlocClasse bloc = blocMap.get(nom);

                positions.put(nom, new Point(x_courant, y_courant));

                x_courant += bloc.getLargeur() + W_NODE_SPACING;
                max_height_couche = Math.max(max_height_couche, bloc.getHauteur());

                if (couche == indexCouches.get(0) && i == 0) {
                    positions.put(nom, new Point(X_ANCHOR, Y_ANCHOR));
                }
            }

            y_courant += max_height_couche + H_LAYER_SPACING;
        }

        return positions;
    }

    public static HashMap<String, Integer> assignerCouches(List<ClasseObjet> classes, List<LiaisonVue> liaisons) {
        HashMap<String, Integer> couches = new HashMap<>();
        classes.forEach(c -> couches.put(c.getNom(), 0));

        boolean changed = true;
        while (changed) {
            changed = false;
            for (LiaisonVue liaison : liaisons) {
                if (liaison.getType() == LiaisonVue.TypeLiaison.HERITAGE
                        || liaison.getType() == LiaisonVue.TypeLiaison.IMPLEMENTATION) {

                    String parent = liaison.getNomClasseDest();
                    String enfant = liaison.getNomClasseOrig();

                    int coucheParent = couches.getOrDefault(parent, 0);
                    int coucheEnfant = couches.getOrDefault(enfant, 0);

                    if (coucheEnfant <= coucheParent) {
                        couches.put(enfant, coucheParent + 1);
                        changed = true;
                    }
                }
            }
        }
        return couches;
    }

    public static void minimiserCroisements(int coucheCouranteIndex, List<String> nomsCoucheCourante,
            List<String> nomsCoucheFixe, List<LiaisonVue> liaisons, HashMap<String, BlocClasse> blocMap,
            boolean forward) {
        java.util.HashMap<String, Double> barycentres = new java.util.HashMap<>();

        for (String nomCourant : nomsCoucheCourante) {
            double positionFixeTotale = 0;
            int voisins = 0;

            for (LiaisonVue liaison : liaisons) {
                String nomVoisin = null;

                if (forward && liaison.getNomClasseOrig().equals(nomCourant)
                        && nomsCoucheFixe.contains(liaison.getNomClasseDest())) {
                    nomVoisin = liaison.getNomClasseDest();
                } else if (!forward && liaison.getNomClasseDest().equals(nomCourant)
                        && nomsCoucheFixe.contains(liaison.getNomClasseOrig())) {
                    nomVoisin = liaison.getNomClasseOrig();
                }

                if (nomVoisin != null) {
                    int indexVoisin = nomsCoucheFixe.indexOf(nomVoisin);
                    positionFixeTotale += indexVoisin;
                    voisins++;
                }
            }

            if (voisins > 0) {
                barycentres.put(nomCourant, positionFixeTotale / voisins);
            } else {
                barycentres.put(nomCourant, (double) nomsCoucheCourante.indexOf(nomCourant));
            }
        }

        Collections.sort(nomsCoucheCourante, Comparator.comparingDouble(barycentres::get));
    }
}
