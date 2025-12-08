package vue;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Fenêtre principale de l'application de modélisation UML.
 * Contient deux panneaux : un pour les projets (gauche) et un pour le diagramme UML (droite).
 */
public class FenetrePrincipale extends JFrame {

    private PanneauProjets panneauProjets;
    private PanneauDiagramme panneauDiagramme;

    public FenetrePrincipale() {
        setTitle("Modélisation UML - Générateur de Diagrammes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setResizable(true);

        // Créer les deux panneaux principaux
        panneauProjets = new PanneauProjets(this);
        panneauDiagramme = new PanneauDiagramme();

        // Layout principal : deux colonnes
        setLayout(new BorderLayout());
        
        // Divider pour permettre le redimensionnement
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            true,
            panneauProjets,
            panneauDiagramme
        );
        splitPane.setDividerLocation(250);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);
        add(new BarreMenus(), BorderLayout.NORTH);
    }

    /**
     * Change le projet courant et affiche ses classes dans le diagramme.
     */
    public void chargerProjet(String cheminProjet) {
        panneauDiagramme.chargerProjet(cheminProjet);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FenetrePrincipale fenetre = new FenetrePrincipale();
            fenetre.setVisible(true);
        });
    }
}
