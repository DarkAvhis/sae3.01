package vue;

import controleur.Controleur;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
public class PanneauProjets extends JPanel implements ActionListener
{
    private FenetrePrincipale fenetrePrincipale;
    private PanneauDiagramme panneauDiagramme; // nouveau
    private Controleur controleur ; 
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
    public PanneauProjets(FenetrePrincipale fenetrePrincipale, Controleur controleur, PanneauDiagramme panneauDiagramme) 
{
        this.fenetrePrincipale = fenetrePrincipale;
        this.panneauDiagramme = panneauDiagramme; // nouveau
        this.controleur        = controleur ; 

        this.cheminDossiers = "src";

        this.setLayout(new BorderLayout());
        this.setBackground(new Color(245, 245, 245));
        this.setBorder(BorderFactory.createTitledBorder("test"));

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
        panelBouton.setBackground(new Color(245, 245, 245));

        boutonAttributs = new JButton("Attributs");
        boutonMethodes = new JButton("Méthodes");
        boutonActualiser = new JButton("Actualiser");

        panelBouton.add(boutonAttributs, BorderLayout.NORTH);
        panelBouton.add(boutonMethodes, BorderLayout.CENTER);
        panelBouton.add(boutonActualiser, BorderLayout.SOUTH);

       
        this.boutonAttributs.addActionListener(this);
        this.boutonMethodes.addActionListener(this);
        this.boutonActualiser.addActionListener(this);


        this.add(panelBouton, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {

         
        if( e.getSource() == boutonActualiser )
        {
            panelProjets.removeAll();
            this.chargerProjets(panelProjets);
            panelProjets.revalidate();
            panelProjets.repaint();

            if( this.panneauDiagramme != null ) this.panneauDiagramme.clearDiagram();
        }

        if (e.getSource() == boutonAttributs) 
        {
        controleur.toggleAttributs();
        majCouleurBouton(boutonAttributs);
        }
        if (e.getSource() == boutonMethodes) 
        {
            controleur.toggleMethodes();
            majCouleurBouton(boutonMethodes);
        }

        
    }

    private void majCouleurBouton(JButton btn) 
    {
        // Change la couleur pour indiquer si le filtre est actif ou non
        if (btn.getBackground().equals(new Color(100, 150, 200))) 
        {
            btn.setBackground(Color.GRAY);
        } 
        else 
        {
            btn.setBackground(new Color(100, 150, 200));
        }
    }

    /**
     * Ajoute un projet à la liste des projets affichés.
     * 
     * Vérifie que le projet n'existe pas déjà avant de l'ajouter.
     * 
     * @param cheminProjet Chemin absolu vers le dossier du projet
     */
    public void ajouterProjet(String cheminProjet) 
    {
        File projet = new File(cheminProjet);

        // Vérifier que le chemin existe et est un dossier
        if (!projet.exists() || !projet.isDirectory()) return;

        // Récupérer JScrollPane → viewport → panelProjets
        JScrollPane scrollPane = (JScrollPane) this.getComponent(1);
        JPanel panelProjets    = (JPanel) scrollPane.getViewport().getView();

        // 🔍 Vérifier si le projet existe déjà dans le panel
        for (Component comp : panelProjets.getComponents()) 
        {
            if (comp instanceof JButton) 
            {
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
    public void chargerProjets(JPanel panelProjets) 
    {
        File dossier = new File(cheminDossiers);

        if (!dossier.exists() || !dossier.isDirectory()) 
    {
            JLabel labelErreur = new JLabel("Dossier non trouvé");
            labelErreur.setForeground(Color.RED);
            panelProjets.add(labelErreur);
        }

        File[] projets = dossier.listFiles(File::isDirectory);

        if (projets == null || projets.length == 0) 
        {
            JLabel labelVide = new JLabel("Aucun projet");
            labelVide.setForeground(Color.GRAY);
            panelProjets.add(labelVide);
        }

        for (File projet : projets) 
        {
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
    public JButton creerBoutonProjet(File projet) 
    {
        JButton bouton = new JButton(projet.getName());

        bouton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bouton.setFont(new Font("Arial", Font.PLAIN, 12));
        bouton.setBackground(new Color(100, 150, 200));
        bouton.setForeground(Color.WHITE);
        bouton.setFocusPainted(false);

        bouton.addActionListener(e ->
        {
            fenetrePrincipale.chargerProjet(projet.getAbsolutePath());
        });

        return bouton;
    }

    public JPanel getPanelProjets() {return this.panelProjets;} 

}
