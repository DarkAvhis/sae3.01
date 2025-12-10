package vue;

import java.awt.*;
import javax.swing.*;

import src.Controleur;

public class FenetrePrincipale extends JFrame 
{

    private Controleur controleur;
    private PanneauProjets panneauProjets;
    private PanneauDiagramme panneauDiagramme;

    // Mise à jour : le constructeur prend maintenant le contrôleur
    public FenetrePrincipale(Controleur controleur) 
    {
        this.controleur = controleur;
        
        this.setTitle("Générateur de diagramme UML"); 
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1400, 800);
        this.setLocationRelativeTo(null);
        this.setResizable(true);

        this.panneauProjets   = new PanneauProjets(this, controleur);
        this.panneauDiagramme = new PanneauDiagramme(controleur);  

        this.setLayout(new BorderLayout());
        
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            true,
            this.panneauProjets,
            this.panneauDiagramme
        );
        splitPane.setDividerLocation(250);
        splitPane.setOneTouchExpandable(true);

        this.add(splitPane, BorderLayout.CENTER);
        this.add(new BarreMenus(controleur), BorderLayout.NORTH); // Changement ici
    }

    // Cette méthode reste, mais appellera maintenant le Contrôleur
    public void chargerProjet(String cheminProjet) 
    {
        this.controleur.analyserEtAfficherDiagramme(cheminProjet); 
    }

    public PanneauDiagramme getPanneauDiagramme() 
    {
       return this.panneauDiagramme;
    }
}