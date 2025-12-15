package vue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import controleur.Controleur;

/**
 * Panneau de navigation des projets.
 * 
 * Affiche la liste des dossiers de test disponibles et permet à l'utilisateur
 * de sélectionner un projet pour générer son diagramme UML.
 * 
 * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT, Ariunbayar
 *         BUYANBADRAKH, Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class PanneauProjets extends JPanel implements ActionListener {
    private FenetrePrincipale fenetrePrincipale;
    private PanneauDiagramme panneauDiagramme; // nouveau
    private String cheminDossiers;
    private JButton boutonAttributs;
    private JButton boutonMethodes;
    private JButton boutonActualiser;
    private JPanel panelProjets;
    private JLabel titreLabel;

    /**
     * Constructeur du panneau de projets.
     * 
     * Initialise le panneau avec la liste des projets disponibles et les boutons
     * d'action.
     * 
     * @param fenetrePrincipale La fenêtre principale de l'application
     * @param controleur        Le contrôleur principal
     */
    public PanneauProjets(FenetrePrincipale fenetrePrincipale, Controleur controleur,
            PanneauDiagramme panneauDiagramme) {
        this.fenetrePrincipale = fenetrePrincipale;
        this.panneauDiagramme = panneauDiagramme;

        this.cheminDossiers = this.resoudreCheminTestFinal();

        this.setLayout(new BorderLayout());
        this.setBackground(new Color(245, 245, 245));
        this.setBorder(BorderFactory.createTitledBorder("Projets :"));

        // Titre
        titreLabel = new JLabel("Liste des Projets");
        titreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titreLabel.setHorizontalAlignment(JLabel.CENTER);

        this.add(titreLabel, BorderLayout.NORTH);

        // Panel scrollable
        panelProjets = new JPanel();
        panelProjets.setLayout(new BoxLayout(panelProjets, BoxLayout.Y_AXIS));
        panelProjets.setBackground(new Color(245, 245, 245));

        this.chargerProjets(panelProjets);

        JScrollPane scrollPane = new JScrollPane(panelProjets);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(scrollPane, BorderLayout.CENTER);

        // actualiser la liste
        JPanel panelBouton = new JPanel(new BorderLayout());
        panelBouton.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBouton.setBackground(new Color(245, 245, 245));

        // Empêche BorderLayout.SOUTH de compresser le panel
        panelBouton.setPreferredSize(new Dimension(100, 100));

        boutonAttributs = new JButton("Attributs");
        boutonMethodes = new JButton("Méthodes");
        boutonActualiser = new JButton("Actualiser");

        panelBouton.add(boutonAttributs, BorderLayout.NORTH);
        panelBouton.add(boutonMethodes, BorderLayout.CENTER);
        panelBouton.add(boutonActualiser, BorderLayout.SOUTH);

        this.boutonActualiser.addActionListener(this);

        this.add(panelBouton, BorderLayout.SOUTH);
    }

    /**
     * Détermine le chemin du dossier testFinal en fonction du répertoire courant
     * (exécution depuis la racine ou depuis src).
     */
    private String resoudreCheminTestFinal() {
        String[] chemins = { "./testFinal", "./src/testFinal", "../testFinal" };
        for (String c : chemins) {
            if (new File(c).isDirectory()) {
                return c;
            }
        }
        return "./testFinal"; // défaut
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == boutonActualiser) {
            panelProjets.removeAll();
            this.chargerProjets(panelProjets);
            panelProjets.revalidate();
            panelProjets.repaint();

            /*
             * nouveau permet de clear le panneauDiagramme que
             * si il est pas null
             */
            if (this.panneauDiagramme != null)
                this.panneauDiagramme.clearDiagram();
        }
    }

    /**
     * Ajoute un projet à la liste des projets affichés.
     * 
     * Vérifie que le projet n'existe pas déjà avant de l'ajouter.
     * 
     * @param cheminProjet Chemin absolu vers le dossier du projet
     */
    public void ajouterProjet(String cheminProjet) {
        File projet = new File(cheminProjet);

        // Vérifier que le chemin existe et est un dossier
        if (!projet.exists() || !projet.isDirectory())
            return;

        // Récupérer JScrollPane → viewport → panelProjets
        JScrollPane scrollPane = (JScrollPane) this.getComponent(1);
        JPanel panelProjets = (JPanel) scrollPane.getViewport().getView();

        // 🔍 Vérifier si le projet existe déjà dans le panel
        for (Component comp : panelProjets.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;

                // Ici je suppose que le texte du bouton = nom du dossier
                // (ou tu peux mettre le chemin complet dans setName())
                if (btn.getText().equals(projet.getName()))
                    return; // 👉 Le projet existe déjà, donc on ne l'ajoute pas
            }
        }

        // Créer le bouton
        JButton bouton = creerBoutonProjet(projet);

        // Ajouter au panel
        panelProjets.add(bouton);
        panelProjets.add(Box.createVerticalStrut(5));

        // Rafraîchir
        panelProjets.revalidate();
        panelProjets.repaint();
    }

    /**
     * Charge la liste des projets depuis le dossier spécifié.
     * 
     * Parcourt le dossier et crée un bouton pour chaque sous-dossier trouvé.
     * 
     * @param panelProjets Le panneau dans lequel afficher les boutons de projets
     */
    public void chargerProjets(JPanel panelProjets) {
        File dossier = new File(cheminDossiers);

        File[] projets = dossier.listFiles(File::isDirectory);

        if (projets == null) {
            System.err.println("Le dossier des projets n'existe pas : " + cheminDossiers);
            return;
        }

        if (projets == null || projets.length == 0) {
            JLabel labelVide = new JLabel("Aucun projet");
            labelVide.setForeground(Color.GRAY);
            panelProjets.add(labelVide);
        }

        for (File projet : projets) {
            JButton boutonProjet = creerBoutonProjet(projet);
            panelProjets.add(boutonProjet);
            panelProjets.add(Box.createVerticalStrut(5));
        }
    }

    /**
     * Crée un bouton pour un projet.
     * 
     * Configure l'apparence et l'action du bouton pour charger le projet au clic.
     * 
     * @param projet Le dossier du projet
     * @return Le bouton configuré
     */
    public JButton creerBoutonProjet(File projet) {
        JButton bouton = new JButton(projet.getName());

        bouton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bouton.setFont(new Font("Arial", Font.PLAIN, 12));
        bouton.setBackground(new Color(100, 150, 200));
        bouton.setForeground(Color.WHITE);
        bouton.setFocusPainted(false);

        bouton.addActionListener(e -> {
            fenetrePrincipale.chargerProjet(projet.getAbsolutePath());
        });

        return bouton;
    }

    public JPanel getPanelProjets() {
        return this.panelProjets;
    }

}
