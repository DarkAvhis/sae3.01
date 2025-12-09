package vue;

import javax.swing.*;
import java.awt.*;

public class FenetrePrincipale extends JFrame 
{

    private PanneauProjets panneauProjets;
    private PanneauDiagramme panneauDiagramme;

    public FenetrePrincipale() 
    {
        this.setTitle("Générateur de diagramme UML");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1400, 800);
        this.setLocationRelativeTo(null);
        this.setResizable(true);

        this.panneauProjets = new PanneauProjets(this);
        this.panneauDiagramme = new PanneauDiagramme();

        this.setLayout(new BorderLayout());
        
        // permettre le redimensionnement
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            true,
            this.panneauProjets,
            this.panneauDiagramme
        );
        splitPane.setDividerLocation(250);
        splitPane.setOneTouchExpandable(true);

        this.add(splitPane, BorderLayout.CENTER);
        this.add(new BarreMenus(), BorderLayout.NORTH);
    }

    public void chargerProjet(String cheminProjet) 
    {
        this.panneauDiagramme.chargerProjet(cheminProjet);
    }

    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(() -> 
        {
            FenetrePrincipale fenetre = new FenetrePrincipale();
            fenetre.setVisible(true);
        });
    }
}
