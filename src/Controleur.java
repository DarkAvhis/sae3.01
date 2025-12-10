package src;

import modele.AnalyseurUML;
import modele.AssociationObjet;
import modele.AttributObjet;
import modele.ClasseObjet;
import modele.MethodeObjet;
import vue.FenetrePrincipale;
import vue.BlocClasse; // Pour convertir le modèle en vue

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.SwingUtilities;

public class Controleur
{
    private AnalyseurUML modeleAnalyseur;
    private FenetrePrincipale vuePrincipale;
    
    // Pour simuler l'état du diagramme (à intégrer au modèle plus tard)
    private HashMap<String, ClasseObjet> classesChargees;

    public Controleur()
    {
        this.modeleAnalyseur = new AnalyseurUML();
        this.classesChargees = new HashMap<>();
    }
    
    public void demarrerApplication()
    {
        // Création de la Vue. Elle doit être faite sur l'EDT.
        SwingUtilities.invokeLater(() -> 
        {
            this.vuePrincipale = new FenetrePrincipale(this);
            this.vuePrincipale.setVisible(true);
        });
    }
    
    // Ajoutez ici d'autres méthodes de contrôle (annuler, exporter, etc.)
    public void exporterDiagramme()
    {
        // Logique d'export : appel au modèle pour générer l'image, 
        // puis interaction avec la Vue pour le dialogue de sauvegarde.
        System.out.println("Contrôleur: Exporter le diagramme");
    }

    public void analyserEtAfficherDiagramme(String cheminProjet)
    {
        // ... (1. Appel du Modèle pour récupérer les fichiers - Logique conservée)
        List<File> fichiersJava = this.modeleAnalyseur.ClassesDuDossier(cheminProjet);
        List<ClasseObjet> classes = new ArrayList<>();
        HashMap<String, ClasseObjet> mapClasses = new HashMap<>();

        for (File f : fichiersJava)
        {
            ClasseObjet c = this.modeleAnalyseur.analyserFichierUnique(f.getAbsolutePath());
            if (c != null)
            {
                classes.add(c);
                mapClasses.put(c.getNom(), c);
            }
        }

        this.classesChargees = mapClasses;
        
        // 2. Préparer les objets de Vue (BlocClasse) à partir des objets de Modèle (ClasseObjet)
        List<BlocClasse> blocsVue = new ArrayList<>();
        int x = 50;
        int y = 50;

        for (ClasseObjet classeModele : classes)
        {
            // CONVERSION DES ATTRIBUTS ET MÉTHODES ICI
            List<String> attributsVue = convertirAttributs(classeModele.getattributs(), classeModele);
            List<String> methodesVue  = convertirMethodes(classeModele.getMethodes(), classeModele);

            // Création du BlocClasse avec les détails
            BlocClasse bloc = new BlocClasse(
                classeModele.getNom(), 
                x, y, 
                attributsVue, 
                methodesVue
            );
            blocsVue.add(bloc);
            
            x += 250;
            // ... (Logique de positionnement conservée)
            if (x > 1000) { // Valeur arbitraire, doit être lié à la taille de la fenêtre
                x = 50;
                y += 200;
            }
        }

        // ... (3. Demander à la Vue de s'actualiser - Logique conservée)
        if (this.vuePrincipale != null && this.vuePrincipale.getPanneauDiagramme() != null)
        {
            this.vuePrincipale.getPanneauDiagramme().afficherDiagramme(blocsVue);
        }
    }
    
    public List<String> convertirAttributs(List<AttributObjet> attributs, ClasseObjet classe)
    {
        return this.modeleAnalyseur.convertirAttributs(attributs, classe);
    }

    public List<String> convertirMethodes(List<MethodeObjet> methodes, ClasseObjet classe)
    {
        return this.modeleAnalyseur.convertirMethodes(methodes, classe);
    }

    // Ancienne logique de main, simplifiée
    public static void main(String[] args)
    {
        Controleur controleur = new Controleur();
        controleur.demarrerApplication();
        
        // Si des arguments sont passés (pour l'analyse automatique au démarrage), on peut les traiter ici.
        if (args.length > 0)
        {
             // Logique pour analyser immédiatement le dossier/fichier donné en argument
             controleur.analyserEtAfficherDiagramme(args[0]);
        }
    }
}
