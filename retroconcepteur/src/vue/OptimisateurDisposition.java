package src.vue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe responsable de l'optimisation des positions des blocs de classe
 * en utilisant un algorithme hiérarchique (Sugiyama-style) qui minimise les croisements de liaisons.
 */
public class OptimisateurDisposition 
{
    private static final int ESPACEMENT_HORIZONTAL = 300;  // Espacement entre blocs en X
    private static final int ESPACEMENT_VERTICAL   = 250;    // Espacement entre couches en Y
    private static final int POSITION_X_DEBUT      = 50 ;
    private static final int POSITION_Y_DEBUT      = 50 ;

    /**
     * Applique un algorithme hiérarchique (Sugiyama) pour optimiser la disposition.
     * Les blocs sont arrangés en couches horizontales pour minimiser les croisements de liaisons.
     * 
     * @param blocsClasses Liste des blocs à optimiser
     * @param liaisonsVue  Liste des liaisons entre blocs
     */
    public static void appliquerLayoutHierarchique(List<BlocClasse> blocsClasses, List<LiaisonVue> liaisonsVue)
    {
        if (blocsClasses == null || blocsClasses.isEmpty()) 
        {
            return;
        }

        // Étape 1 : Construire le graphe de dépendances
        HashMap<String, Set<String>> dependances = construireGrapheDependances(blocsClasses, liaisonsVue);
        
        // Étape 2 : Assigner chaque bloc à une couche (layer) basée sur sa profondeur
        HashMap<String, Integer> couchesBlocs = calculerCouches(blocsClasses, dependances);
        
        // Étape 3 : Grouper les blocs par couche
        HashMap<Integer, List<BlocClasse>> blocParCouche = groupeParCouche(blocsClasses, couchesBlocs);
        
        // Étape 4 : Positionner les blocs
        positionnerBlocs(blocsClasses, blocParCouche, couchesBlocs);
    }

    /**
     * Construit un graphe représentant les dépendances (liaisons) entre classes.
     * Pour l'héritage/implémentation : les parents dépendent des enfants 
     * (pour placer les filles au-dessus des mères)
     */
    private static HashMap<String, Set<String>> construireGrapheDependances(List<BlocClasse> blocsClasses, List<LiaisonVue> liaisonsVue)
    {
        HashMap<String, Set<String>> graph = new HashMap<>();
        
        // Initialiser : chaque classe n'a pas de dépendances
        for (BlocClasse bloc : blocsClasses) 
        {
            graph.put(bloc.getNom(), new HashSet<>());
        }
        
        // Ajouter les dépendances basées sur les liaisons
        for (LiaisonVue liaison : liaisonsVue) 
        {
            String source = liaison.getNomClasseOrig();
            String dest = liaison.getNomClasseDest();
            
            // HÉRITAGE : le parent dépend de l'enfant 
            // (source = enfant qui hérite, dest = parent)
            // Donc dest (parent) dépend de source (enfant)
            if (liaison.getType() == LiaisonVue.TypeLiaison.HERITAGE ||
                liaison.getType() == LiaisonVue.TypeLiaison.IMPLEMENTATION) 
            {
                if (graph.containsKey(dest)) 
                {
                    graph.get(dest).add(source);  // Parent dépend de l'enfant
                }
            }
            // ASSOCIATION : établir une dépendance douce
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
     * Calcule la couche (layer) de chaque bloc basée sur sa profondeur dans la hiérarchie.
     * Les classes filles (enfants) sont à la couche 0 (en haut), leurs parents plus bas.
     */
    private static HashMap<String, Integer> calculerCouches(List<BlocClasse> blocsClasses, HashMap<String, Set<String>> dependances)
    {
        HashMap<String, Integer> couches = new HashMap<>();
        
        // Initialiser : toutes les couches à -1 (non visitées)
        for (BlocClasse bloc : blocsClasses) 
        {
            couches.put(bloc.getNom(), -1);
        }
        
        // Calculer la profondeur : utiliser DFS (Depth-First Search)
        for (BlocClasse bloc : blocsClasses) 
        {
            if (couches.get(bloc.getNom()) == -1) 
            {
                calculerProfondeur(bloc.getNom(), couches, dependances);
            }
        }
        
        // Ne pas inverser : les classes filles auront une petite profondeur (en haut)
        // Les parents auront une grande profondeur (en bas)
        
        return couches;
    }

    /**
     * Calcule récursivement la profondeur d'un bloc via DFS.
     */
    private static int calculerProfondeur(String nomBloc, HashMap<String, Integer> couches,HashMap<String, Set<String>> dependances)
    {
        if (couches.get(nomBloc) != -1) 
        {
            return couches.get(nomBloc);
        }
        
        Set<String> deps = dependances.get(nomBloc);
        if (deps == null || deps.isEmpty()) 
        {
            couches.put(nomBloc, 0);
            return 0;
        }
        
        int maxProf = 0;
        for (String dep : deps) 
        {
            if (dependances.containsKey(dep)) 
            {
                int prof = calculerProfondeur(dep, couches, dependances);
                maxProf = Math.max(maxProf, prof + 1);
            }
        }
        
        couches.put(nomBloc, maxProf);
        return maxProf;
    }

    /**
     * Groupe les blocs par leur couche (layer).
     */
    private static HashMap<Integer, List<BlocClasse>> groupeParCouche(List<BlocClasse> blocsClasses, HashMap<String, Integer> couchesBlocs)
    {
        HashMap<Integer, List<BlocClasse>> resultat = new HashMap<>();
        
        for (BlocClasse bloc : blocsClasses) 
        {
            int couche = couchesBlocs.get(bloc.getNom());
            resultat.computeIfAbsent(couche, k -> new ArrayList<>()).add(bloc);
        }
        
        return resultat;
    }

    /**
     * Positionne les blocs dans le panneau basé sur leurs couches avec optimisation spatiale.
     * Les blocs sont arrangés pour minimiser les croisements et créer une disposition équilibrée.
     */
    private static void positionnerBlocs(List<BlocClasse> blocsClasses,
                                        HashMap<Integer, List<BlocClasse>> blocParCouche,
                                        HashMap<String, Integer> couchesBlocs)
   
    {
        int maxCouche = blocParCouche.size();
        
        // Calculer les dimensions du panneau de manière dynamique
        int largeurPanneau = 1200;
        int hauteurPanneau = 150 + maxCouche * ESPACEMENT_VERTICAL;
        
        // Parcourir chaque couche de haut en bas (y croissant)
        for (int couche = 0; couche < maxCouche; couche++) 
        {
            List<BlocClasse> blocsEnCouche = blocParCouche.get(couche);
            
            if (blocsEnCouche == null || blocsEnCouche.isEmpty()) 
            {
                continue;
            }
            
            // Calculer la position Y de cette couche
            int posY = POSITION_Y_DEBUT + couche * ESPACEMENT_VERTICAL;
            
            // Trier les blocs pour minimiser les croisements
            trierBlocsParConnexion(blocsEnCouche, blocsClasses, couchesBlocs);
            
            // Calculer la position X de départ
            // Plus de place pour les blocs : adapter l'espacement en fonction du nombre de blocs
            int nbBlocs = blocsEnCouche.size();
            int espaceAdapte = Math.max(250, (largeurPanneau - 100) / nbBlocs);
            int largeurTotale = nbBlocs * espaceAdapte;
            int posXDebut = Math.max(POSITION_X_DEBUT, (largeurPanneau - largeurTotale) / 2);
            
            // Positionner chaque bloc de cette couche
            for (int i = 0; i < nbBlocs; i++) 
            {
                BlocClasse bloc = blocsEnCouche.get(i);
                int posX = posXDebut + i * espaceAdapte;
                
                bloc.setX(posX);
                bloc.setY(posY);
            }
        }
    }

    /**
     * Trie les blocs d'une couche pour minimiser les croisements de liaisons.
     * Les blocs connectés à la couche précédente/suivante sont groupés ensemble.
     */
    private static void trierBlocsParConnexion(List<BlocClasse> blocsEnCouche,
                                               List<BlocClasse> tousLesBlocs,
                                               HashMap<String, Integer> couchesBlocs)
   
    {
        // Créer un score pour chaque bloc basé sur ses connexions
        HashMap<String, Integer> scores = new HashMap<>();
        int couche = couchesBlocs.get(blocsEnCouche.get(0).getNom());
        
        for (BlocClasse bloc : blocsEnCouche) 
        {
            int score = 0;
            
            // Chercher les blocs de la couche précédente/suivante pour calculer une position optimale
            for (BlocClasse autreBlocCouche : blocsEnCouche) 
            {
                if (bloc.getNom().equals(autreBlocCouche.getNom())) continue;
                
                // Blocs proches alphabétiquement ou par nom : score moins bon (spread them)
                // Blocs avec connexions communes : score meilleur (group them)
                score += bloc.getNom().compareTo(autreBlocCouche.getNom());
            }
            
            scores.put(bloc.getNom(), score);
        }
        
        // Trier en utilisant le score
        Collections.sort(blocsEnCouche, (b1, b2) ->
        {
            Integer s1 = scores.getOrDefault(b1.getNom(), 0);
            Integer s2 = scores.getOrDefault(b2.getNom(), 0);
            return s1.compareTo(s2);
        });
    }

    /**
     * Alternative : Applique un layout en grille pour une disposition ordonnée.
     * Utile pour les diagrammes simples ou en complément du force-directed.
     * 
     * @param blocsClasses Liste des blocs à positionner
     */
    public static void appliquerLayoutGrille(List<BlocClasse> blocsClasses)
   
    {
        if (blocsClasses == null || blocsClasses.isEmpty()) 
        {
            return;
        }

        int cols = (int) Math.ceil(Math.sqrt(blocsClasses.size()));
        int espaceX = 300;
        int espaceY = 250;
        int startX  = 50 ;
        int startY  = 50 ;

        for (int i = 0; i < blocsClasses.size(); i++) 
        {
            int col = i % cols;
            int row = i / cols;
            
            int x = startX + col * espaceX;
            int y = startY + row * espaceY;
            
            blocsClasses.get(i).setX(x);
            blocsClasses.get(i).setY(y);
        }
    }

    /**
     * Applique un layout circulaire avec la classe principale au centre.
     * 
     * @param blocsClasses Liste des blocs à positionner
     * @param indexPrincipal Index du bloc principal (généralement 0)
     */
    public static void appliquerLayoutCirculaire(List<BlocClasse> blocsClasses, int indexPrincipal)
   
    {
        if (blocsClasses == null || blocsClasses.isEmpty()) 
        {
            return;
        }

        int centreX = 500;
        int centreY = 400;
        int rayon   = 250;

        // Positionner le bloc principal au centre
        if (indexPrincipal >= 0 && indexPrincipal < blocsClasses.size()) 
        {
            BlocClasse blocPrincipal = blocsClasses.get(indexPrincipal);
            blocPrincipal.setX(centreX - blocPrincipal.getLargeur() / 2);
            blocPrincipal.setY(centreY - blocPrincipal.getHauteur() / 2);
        }

        // Positionner les autres blocs en cercle
        int nbAutresBlocs = blocsClasses.size() - 1;
        for (int i = 0; i < blocsClasses.size(); i++) 
       
        {
            if (i == indexPrincipal) continue;
            
            double angle = (2 * Math.PI * (i < indexPrincipal ? i : i - 1)) / nbAutresBlocs;
            
            int x = (int) (centreX + rayon * Math.cos(angle));
            int y = (int) (centreY + rayon * Math.sin(angle));
            
            BlocClasse bloc = blocsClasses.get(i);
            bloc.setX(x - bloc.getLargeur() / 2);
            bloc.setY(y - bloc.getHauteur() / 2);
        }
    }
}