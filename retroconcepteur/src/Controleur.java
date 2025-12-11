package src;

import modele.AnalyseIHMControleur;
import modele.AttributObjet;
import modele.MethodeObjet;
import modele.ClasseObjet;
import modele.LiaisonObjet; 
import modele.AssociationObjet;
import modele.HeritageObjet;
import modele.InterfaceObjet;
import modele.MultipliciteObjet; 

import vue.FenetrePrincipale;
import vue.BlocClasse; 
import vue.LiaisonVue;
import vue.LiaisonVue.TypeLiaison;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.SwingUtilities;

public class Controleur
{
    // Remplacement d'AnalyseurUML par le contrôleur de la couche Modèle pour une analyse complète
    private AnalyseIHMControleur metierComplet; 
    private FenetrePrincipale vuePrincipale;
<<<<<<< HEAD

    private static final int ITERATIONS = 10; // Nombre d'itérations pour l'optimisation
    private static final int W_NODE_SPACING = 50; // Espacement horizontal entre les nœuds
    private static final int H_LAYER_SPACING = 150; // Espacement vertical entre les couches

=======
>>>>>>> 1a75af4ce57beff2f58b25a2b33006f7002ea172
    public Controleur()
    {
        this.metierComplet   = new AnalyseIHMControleur(); 
        this.vuePrincipale = new FenetrePrincipale(this);
        this.vuePrincipale.setVisible(true);
    }
    
   

    public void analyserEtAfficherDiagramme(String cheminProjet)
    {
        if (!this.metierComplet.analyserDossier(cheminProjet)) 
        {
            return;
        }

        // 1. Récupération des données du Modèle
        List<ClasseObjet> classes = this.metierComplet.getClasses();
        List<AssociationObjet> associations = this.metierComplet.getAssociations();
        List<HeritageObjet> heritages = this.metierComplet.getHeritages();
        List<InterfaceObjet> implementations = this.metierComplet.getImplementations(); 

        // --- 2. Préparation des Liaisons de Vue (pour le calcul et le dessin) ---
        List<LiaisonVue> toutesLiaisonsVue = new ArrayList<>();
        toutesLiaisonsVue.addAll(convertirLiaisons(associations, TypeLiaison.ASSOCIATION_UNIDI)); 
        toutesLiaisonsVue.addAll(convertirLiaisons(heritages, TypeLiaison.HERITAGE));
        toutesLiaisonsVue.addAll(convertirLiaisons(implementations, TypeLiaison.IMPLEMENTATION));

        // --- 3. Calcul des Positions Optimales ---
        // On crée des blocs temporaires pour avoir les tailles exactes (largeur/hauteur)
        List<BlocClasse> blocsAvecTailles = new ArrayList<>();
        for (ClasseObjet c : classes) {
            List<String> attrVue = this.convertirAttributs(c.getattributs(), c);
            List<String> methVue = this.convertirMethodes(c.getMethodes(), c);
            // x=0, y=0 sont arbitraires ici
            blocsAvecTailles.add(new BlocClasse(c.getNom(), 0, 0, attrVue, methVue)); 
        }
        
        HashMap<String, Point> positionsOptimales = calculerPositionsOptimales(classes, toutesLiaisonsVue, blocsAvecTailles);


        // --- 4. Construction des Blocs de Vue avec les Positions Calculées ---
        List<BlocClasse> blocsVue = new ArrayList<>();
        
        for (ClasseObjet c : classes)
        {
            Point pos = positionsOptimales.get(c.getNom());
            
            List<String> attrVue = this.convertirAttributs(c.getattributs(), c);
            List<String> methVue = this.convertirMethodes(c.getMethodes(), c);
            
            // Utilisation des positions calculées
            BlocClasse bloc = new BlocClasse(c.getNom(), pos.x, pos.y, attrVue, methVue);
            
            if (c.getNom().contains("Interface")) {
                bloc.setInterface(true);
            }
            
            blocsVue.add(bloc);
        }

        // 5. Affichage (envoi à la Vue)
        if (this.vuePrincipale != null)
        {
            this.vuePrincipale.getPanneauDiagramme().setBlocsClasses(blocsVue);
            this.vuePrincipale.getPanneauDiagramme().setLiaisonsVue(toutesLiaisonsVue);
            this.vuePrincipale.getPanneauDiagramme().repaint();
        }
    }

    /**
     * Calcule les positions optimales en utilisant un algorithme hiérarchique simplifié (Sugiyama-style).
     * @return Une Map contenant le nom de la classe et sa position calculée.
     */
    private HashMap<String, Point> calculerPositionsOptimales(List<ClasseObjet> classes, List<LiaisonVue> liaisons, List<BlocClasse> blocsAvecTailles)
    {
        HashMap<String, BlocClasse> blocMap = new HashMap<>();
        HashMap<String, ClasseObjet> classeMap = new HashMap<>();
        
        // Map de référence pour la taille et l'objet
        for (BlocClasse bloc : blocsAvecTailles) 
        {
             blocMap.put(bloc.getNom(), bloc);
        }

        for (ClasseObjet classe : classes) 
        {
             classeMap.put(classe.getNom(), classe);
        }

        // --- PHASE 1: ASSIGNATION DES COUCHES (Ranking) ---
        // Déterminer la couche (Y-coord) de chaque classe
        HashMap<String, Integer> couches = assignerCouches(classes, liaisons);

        // Grouper les classes par couche
        HashMap<Integer, List<String>> classesParCouche = new HashMap<>();
        for (ClasseObjet classe : classes) 
        {
            int couche = couches.get(classe.getNom());
            classesParCouche.computeIfAbsent(couche, k -> new ArrayList<>()).add(classe.getNom());
        }

        // --- PHASE 2: MINIMISATION DES CROISEMENTS (Ordering - Barycentre) ---
        // Ordonner les nœuds dans chaque couche pour minimiser les croisements.
        // On itère sur les couches du bas vers le haut pour une meilleure convergence.
        List<Integer> indexCouches = new ArrayList<>(classesParCouche.keySet());
        Collections.sort(indexCouches); 

        for (int iter = 0; iter < ITERATIONS; iter++) 
        {
            // Alterner l'ordre des couches
            for (int i = 0; i < indexCouches.size() - 1; i++)
            {
                int coucheCourante = indexCouches.get(i);
                int coucheSuivante = indexCouches.get(i + 1);
                
                // Calculer les barycentres de la couche suivante
                minimiserCroisements(coucheCourante, classesParCouche.get(coucheCourante), classesParCouche.get(coucheSuivante), liaisons, blocMap, true);
                
                // Calculer les barycentres de la couche courante (en regardant la couche précédente)
                minimiserCroisements(coucheSuivante, classesParCouche.get(coucheSuivante), classesParCouche.get(coucheCourante), liaisons, blocMap, false);
            }
        }


        // --- PHASE 3: ASSIGNATION DES COORDONNÉES (Placement) ---
        HashMap<String, Point> positions = new HashMap<>();
        int y_courant = 50; 
        
        for (int couche : indexCouches)
        {
            List<String> nomsCouche = classesParCouche.get(couche);
            
            // Calculer les positions X de manière séquentielle
            int x_courant = 50;

            for (String nom : nomsCouche) 
            {
                BlocClasse bloc = blocMap.get(nom);

                // Appliquer les coordonnées finales
                positions.put(nom, new Point(x_courant + bloc.getLargeur() / 2, y_courant + bloc.getHauteur() / 2));
                
                x_courant += bloc.getLargeur() + W_NODE_SPACING;
            }
            y_courant += H_LAYER_SPACING; // Espacement entre couches
        }
        
        return positions;
    }
    
    // Helper 1: Détermine la couche Y de chaque classe (très simplifié)
    private HashMap<String, Integer> assignerCouches(List<ClasseObjet> classes, List<LiaisonVue> liaisons) 
    {
        HashMap<String, Integer> couches = new HashMap<>();
        // Initialiser toutes les couches à 0
        classes.forEach(c -> couches.put(c.getNom(), 0));

        // Répéter l'affectation jusqu'à ce qu'il n'y ait plus de changement
        boolean changed = true;
        while (changed) 
        {
            changed = false;
            for (LiaisonVue liaison : liaisons) 
            {
                // Seulement les relations dirigées (Héritage, Implémentation) sont critiques pour le layering
                if (liaison.getType() == TypeLiaison.HERITAGE || liaison.getType() == TypeLiaison.IMPLEMENTATION) 
                {
                    
                    String parent = liaison.getNomClasseDest(); // Parent/Interface (Doit être dans une couche supérieure/plus petite valeur)
                    String enfant = liaison.getNomClasseOrig(); // Enfant/Concrète (Doit être dans une couche inférieure/plus grande valeur)
                    
                    int coucheParent = couches.getOrDefault(parent, 0);
                    int coucheEnfant = couches.getOrDefault(enfant, 0);

                    if (coucheEnfant <= coucheParent) 
                    {
                        // L'enfant doit être dans une couche strictement inférieure/plus grande
                        couches.put(enfant, coucheParent + 1);
                        changed = true;
                    }
                }
            }
        }
        return couches;
    }
    
    // Helper 2: Minimise les croisements en utilisant l'heuristique du Barycentre
    private void minimiserCroisements(int coucheCouranteIndex, List<String> nomsCoucheCourante, List<String> nomsCoucheFixe, List<LiaisonVue> liaisons, HashMap<String, BlocClasse> blocMap, boolean forward) 
    {
        // 1. Calculer le barycentre de chaque nœud de la couche courante
        HashMap<String, Double> barycentres = new HashMap<>();

        for (String nomCourant : nomsCoucheCourante) {
            double positionFixeTotale = 0;
            int voisins = 0;

            for (LiaisonVue liaison : liaisons) {
                String nomVoisin = null;
                // Identifier la direction et le voisin dans la couche fixe
                if (forward && liaison.getNomClasseOrig().equals(nomCourant) && nomsCoucheFixe.contains(liaison.getNomClasseDest())) {
                    nomVoisin = liaison.getNomClasseDest();
                } else if (!forward && liaison.getNomClasseDest().equals(nomCourant) && nomsCoucheFixe.contains(liaison.getNomClasseOrig())) {
                    nomVoisin = liaison.getNomClasseOrig();
                }
                
                if (nomVoisin != null) {
                    // Trouver la position X (indice) du voisin dans la couche fixe
                    int indexVoisin = nomsCoucheFixe.indexOf(nomVoisin);
                    positionFixeTotale += indexVoisin;
                    voisins++;
                }
            }

            if (voisins > 0) {
                barycentres.put(nomCourant, positionFixeTotale / voisins);
            } else {
                // Nœud sans voisin dans la couche fixe : laisser sa position X actuelle (indice)
                barycentres.put(nomCourant, (double) nomsCoucheCourante.indexOf(nomCourant));
            }
        }

        // 2. Trier la couche courante en fonction des barycentres
        Collections.sort(nomsCoucheCourante, Comparator.comparingDouble(barycentres::get));
    }


    public void sauvegarde() 
    {
        if (this.vuePrincipale == null) return;
        List<BlocClasse> blocs = this.vuePrincipale.getPanneauDiagramme().getBlocsClasses();
        System.out.println("Sauvegarde des positions de " + blocs.size() + " blocs.");
    }

    public void supprimerClasseSelectionnee()
    {
        if (this.vuePrincipale == null) return;

        BlocClasse bloc = this.vuePrincipale.getPanneauDiagramme().getBlocsClasseSelectionnee();
        // ... (Logique de suppression inchangée) ...
    }

    private List<LiaisonVue> convertirLiaisons(List<? extends LiaisonObjet> liaisons, TypeLiaison type)
    {
        List<LiaisonVue> liaisonsVue = new ArrayList<>();
        for (LiaisonObjet liaison : liaisons)
        {
            String nomOrig = liaison.getClasseFille().getNom(); 
            String nomDest = liaison.getClasseMere().getNom();
            
            String multOrig = null;
            String multDest = null;
            
            if (liaison instanceof AssociationObjet)
            {
                AssociationObjet asso = (AssociationObjet) liaison;
                
                multOrig = asso.getMultOrig() != null ? asso.getMultOrig().toString() : "1..1"; 
                multDest = asso.getMultDest() != null ? asso.getMultDest().toString() : "1..1"; 
            }
            
            liaisonsVue.add(new LiaisonVue(nomOrig, nomDest, type, multOrig, multDest)); 
        }
        return liaisonsVue;
    }

    private List<String> convertirAttributs(List<AttributObjet> attributs, ClasseObjet classe)
    {
        List<String> liste = new ArrayList<>();
        for (AttributObjet att : attributs)
        {
            String staticFlag = att.estStatique() ? " {static}" : ""; 
            char visibilite = classe.changementVisibilite(att.getVisibilite()); 
            
            String s = visibilite + " " + att.getNom() + " : " + att.getType() + staticFlag; 
            liste.add(s);
        }
        return liste;
    }

    private List<String> convertirMethodes(List<MethodeObjet> methodes, ClasseObjet classe)
    {
        List<String> liste = new ArrayList<>();
        for (MethodeObjet met : methodes)
        {
            String staticFlag = met.estStatique() ? "{static} " : ""; 
            char visibilite = classe.changementVisibilite(met.getVisibilite());
            
            String params = classe.affichageParametre(met.getParametres());
            String retour = classe.retourType(met.getRetourType());
            
            String s = visibilite + staticFlag + met.getNom() + params + retour;
            liste.add(s);
        }
        return liste;
    }


    public static void main(String[] args)
    {
        new Controleur();
    }
}