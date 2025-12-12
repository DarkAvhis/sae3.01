package src;

import src.modele.AnalyseIHMControleur;
import src.modele.AttributObjet;
import src.modele.MethodeObjet;
import src.modele.ClasseObjet;
import src.modele.LiaisonObjet; 
import src.modele.AssociationObjet;
import src.modele.HeritageObjet;
import src.modele.InterfaceObjet;
import src.modele.MultipliciteObjet; 

import src.vue.FenetrePrincipale;
import src.vue.BlocClasse; 
import src.vue.LiaisonVue;
import src.vue.LiaisonVue.TypeLiaison;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Comparator; 
import javax.swing.SwingUtilities;

public class Controleur
{
    private AnalyseIHMControleur metierComplet; 
    private FenetrePrincipale vuePrincipale;

    // --- Constantes pour le Layout Hiérarchique ---
    private static final int H_LAYER_SPACING = 150; // Espacement vertical minimum entre les couches
    private static final int W_NODE_SPACING  = 100; // Espacement horizontal minimum entre les blocs (votre demande)
    private static final int Y_ANCHOR = 50; // Ancrage Y
    private static final int X_ANCHOR = 50; // Ancrage X
    private static final int ITERATIONS = 10; // Nombre d'itérations pour la minimisation des croisements

    public Controleur()
    {
        this.metierComplet   = new AnalyseIHMControleur();
        this.vuePrincipale = new FenetrePrincipale(this);
        this.vuePrincipale.setVisible(true);
    }  

    public void analyserEtAfficherDiagramme(String cheminProjet)
    {
        if (!this.metierComplet.analyserDossier(cheminProjet)) {
            return;
        }

        List<ClasseObjet> classes = this.metierComplet.getClasses();
        List<AssociationObjet> associations = this.metierComplet.getAssociations();
        List<HeritageObjet> heritages = this.metierComplet.getHeritages();
        List<InterfaceObjet> implementations = this.metierComplet.getImplementations(); 

        List<LiaisonVue> toutesLiaisonsVue = new ArrayList<>();
        toutesLiaisonsVue.addAll(convertirLiaisons(associations, TypeLiaison.ASSOCIATION_UNIDI)); 
        toutesLiaisonsVue.addAll(convertirLiaisons(heritages, TypeLiaison.HERITAGE));
        toutesLiaisonsVue.addAll(convertirLiaisons(implementations, TypeLiaison.IMPLEMENTATION));

        List<BlocClasse> blocsAvecTailles = new ArrayList<>();
        for (ClasseObjet c : classes) {
            List<String> attrVue = this.convertirAttributs(c.getattributs(), c);
            List<String> methVue = this.convertirMethodes(c.getMethodes(), c);
            blocsAvecTailles.add(new BlocClasse(c.getNom(), 0, 0, attrVue, methVue)); 
        }
        
        // --- Calcul des Positions Optimales (Hiérarchique) ---
        HashMap<String, Point> positionsOptimales = calculerPositionsOptimales(classes, toutesLiaisonsVue, blocsAvecTailles);

        List<BlocClasse> blocsVue = new ArrayList<>();
        
        for (ClasseObjet c : classes)
        {
            Point pos = positionsOptimales.get(c.getNom());
            
            List<String> attrVue = this.convertirAttributs(c.getattributs(), c);
            List<String> methVue = this.convertirMethodes(c.getMethodes(), c);
            
            BlocClasse bloc = new BlocClasse(c.getNom(), pos.x, pos.y, attrVue, methVue);
            
            if (c.getNom().contains("Interface")) {
                bloc.setInterface(true);
            }
            
            blocsVue.add(bloc);
        }

        if (this.vuePrincipale != null)
        {
            this.vuePrincipale.getPanneauDiagramme().setBlocsClasses(blocsVue);
            this.vuePrincipale.getPanneauDiagramme().setLiaisonsVue(toutesLiaisonsVue);
            this.vuePrincipale.getPanneauDiagramme().repaint();
        }
    }

    /**
     * Calcule les positions optimales en utilisant un algorithme hiérarchique simplifié (Sugiyama-style).
     */
    private HashMap<String, Point> calculerPositionsOptimales(List<ClasseObjet> classes, List<LiaisonVue> liaisons, List<BlocClasse> blocsAvecTailles)
    {
        HashMap<String, BlocClasse> blocMap = new HashMap<>();
        
        // Map de référence pour la taille et l'objet
        for (BlocClasse bloc : blocsAvecTailles) {
             blocMap.put(bloc.getNom(), bloc);
        }

        // --- PHASE 1: ASSIGNATION DES COUCHES (Ranking) ---
        HashMap<String, Integer> couches = assignerCouches(classes, liaisons);

        // Grouper les classes par couche
        HashMap<Integer, List<String>> classesParCouche = new HashMap<>();
        for (ClasseObjet classe : classes) {
            int couche = couches.get(classe.getNom());
            classesParCouche.computeIfAbsent(couche, k -> new ArrayList<>()).add(classe.getNom());
        }

        // --- PHASE 2: MINIMISATION DES CROISEMENTS (Ordering - Barycentre) ---
        List<Integer> indexCouches = new ArrayList<>(classesParCouche.keySet());
        Collections.sort(indexCouches); 

        for (int iter = 0; iter < ITERATIONS; iter++) {
            for (int i = 0; i < indexCouches.size(); i++) {
                int coucheCourante = indexCouches.get(i);
                
                if (i + 1 < indexCouches.size()) { 
                    int coucheSuivante = indexCouches.get(i + 1);
                    minimiserCroisements(coucheCourante, classesParCouche.get(coucheCourante), classesParCouche.get(coucheSuivante), liaisons, blocMap, true);
                }
                
                if (i > 0) { 
                    int couchePrecedente = indexCouches.get(i - 1);
                    minimiserCroisements(coucheCourante, classesParCouche.get(coucheCourante), classesParCouche.get(couchePrecedente), liaisons, blocMap, false);
                }
            }
        }


        // --- PHASE 3: ASSIGNATION DES COORDONNÉES (Placement) ---
        HashMap<String, Point> positions = new HashMap<>();
        int y_courant = Y_ANCHOR; // ANCRE Y
        
        for (int couche : indexCouches) {
            List<String> nomsCouche = classesParCouche.get(couche);
            
            int x_courant = X_ANCHOR; // ANCRE X
            int max_height_couche = 0;
            
            // Placer les nœuds
            for (int i = 0; i < nomsCouche.size(); i++) {
                String nom = nomsCouche.get(i);
                BlocClasse bloc = blocMap.get(nom);

                // Appliquer les coordonnées finales
                positions.put(nom, new Point(x_courant, y_courant));
                
                // Mettre à jour les compteurs
                x_courant += bloc.getLargeur() + W_NODE_SPACING; // Espacement de 100px
                max_height_couche = Math.max(max_height_couche, bloc.getHauteur());
                
                // ANCRAGE DE LA TOUTE PREMIÈRE CLASSE EN (50, 50)
                if (couche == indexCouches.get(0) && i == 0) {
                     positions.put(nom, new Point(X_ANCHOR, Y_ANCHOR));
                }
            }
            
            // Passer à la couche suivante : Y_courant + hauteur max de cette couche + espacement
            y_courant += max_height_couche + H_LAYER_SPACING;
        }
        
        return positions;
    }
    
    // Helper 1: Détermine la couche Y de chaque classe (ranking)
    private HashMap<String, Integer> assignerCouches(List<ClasseObjet> classes, List<LiaisonVue> liaisons) {
        HashMap<String, Integer> couches = new HashMap<>();
        classes.forEach(c -> couches.put(c.getNom(), 0));

        boolean changed = true;
        while (changed) {
            changed = false;
            for (LiaisonVue liaison : liaisons) {
                if (liaison.getType() == TypeLiaison.HERITAGE || liaison.getType() == TypeLiaison.IMPLEMENTATION) {
                    
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
    
    // Helper 2: Minimise les croisements (barycentre)
    private void minimiserCroisements(int coucheCouranteIndex, List<String> nomsCoucheCourante, List<String> nomsCoucheFixe, List<LiaisonVue> liaisons, HashMap<String, BlocClasse> blocMap, boolean forward) 
    {
        HashMap<String, Double> barycentres = new HashMap<>();

        for (String nomCourant : nomsCoucheCourante) {
            double positionFixeTotale = 0;
            int voisins = 0;

            for (LiaisonVue liaison : liaisons) {
                String nomVoisin = null;
                
                if (forward && liaison.getNomClasseOrig().equals(nomCourant) && nomsCoucheFixe.contains(liaison.getNomClasseDest())) {
                    nomVoisin = liaison.getNomClasseDest();
                } 
                else if (!forward && liaison.getNomClasseDest().equals(nomCourant) && nomsCoucheFixe.contains(liaison.getNomClasseOrig())) {
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
    
    // ... (Reste des méthodes auxiliaires et main inchangés) ...

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
            // Traitement spécial pour les InterfaceObjet qui stockent les interfaces dans une liste
            if (liaison instanceof InterfaceObjet)
            {
                InterfaceObjet interfaceLiaison = (InterfaceObjet) liaison;
                if (interfaceLiaison.getClasseFille() == null) {
                    continue;
                }
                String nomClasseConcrete = interfaceLiaison.getClasseFille().getNom();
                
                // Parcourir la liste des interfaces implémentées
                java.util.List<ClasseObjet> interfaces = interfaceLiaison.getLstInterfaces();
                for (ClasseObjet interfaceClass : interfaces)
                {
                    if (interfaceClass != null)
                    {
                        liaisonsVue.add(new LiaisonVue(nomClasseConcrete, interfaceClass.getNom(), type, null, null));
                    }
                }
            }
            else
            {
                // Vérifier que les deux classes existent pour les autres liaisons
                if (liaison.getClasseFille() == null || liaison.getClasseMere() == null) {
                    System.err.println("Attention: liaison avec classe null ignorée");
                    continue;
                }
                
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