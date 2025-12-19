package vue;

import controleur.Controleur;
import java.awt.*;
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
 * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT,
 *         Ariunbayar BUYANBADRAKH, Yassine EL MAADI
 * @date   12 décembre 2025
 */
public class PanneauProjets extends JPanel implements ActionListener
{
    private FenetrePrincipale fenetrePrincipale;
    private PanneauDiagramme  panneauDiagramme;
    private Controleur        controleur;
    private String            cheminDossiers;
    private JButton           boutonAttributs;
    private JButton           boutonMethodes;
    private JButton           boutonActualiser;
    private JPanel            panelProjets;
    private JLabel            titreLabel;

    /**
     * Constructeur du panneau de projets.
     *
     * @param fenetrePrincipale fenêtre principale de l'application
     * @param controleur        contrôleur principal
     * @param panneauDiagramme  panneau d'affichage du diagramme UML
     */
    public PanneauProjets(FenetrePrincipale fenetrePrincipale,Controleur controleur, 
                          PanneauDiagramme  panneauDiagramme )
    {
        this.fenetrePrincipale = fenetrePrincipale;
        this.panneauDiagramme  = panneauDiagramme;
        this.controleur        = controleur;

        this.cheminDossiers = "src";

        this.setLayout(new BorderLayout());
        this.setBackground(new Color(245, 245, 245));
        this.setBorder( BorderFactory.createTitledBorder("test"));

        /* Titre */
        this.titreLabel = new JLabel("Liste des Projets");
        this.titreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        this.titreLabel.setHorizontalAlignment(JLabel.CENTER);

        this.add(this.titreLabel, BorderLayout.NORTH);

        /* Panel projets */
        this.panelProjets = new JPanel();
        this.panelProjets.setLayout(new BoxLayout(this.panelProjets, BoxLayout.Y_AXIS));
        this.panelProjets.setBackground(new Color(245, 245, 245));

        this.chargerProjets(this.panelProjets);

        JScrollPane scrollPane =
            new JScrollPane(this.panelProjets);

        scrollPane.setVerticalScrollBarPolicy  (JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER  );

        this.add(scrollPane, BorderLayout.CENTER);

        /* Boutons */
        JPanel panelBouton = new JPanel(new BorderLayout());
        panelBouton.setBackground(new Color(245, 245, 245));

        this.boutonAttributs   = new JButton("Attributs");
        this.boutonMethodes    = new JButton("Méthodes");
        this.boutonActualiser  = new JButton("Réinitialiser Frame");

        panelBouton.add(this.boutonAttributs,  BorderLayout.NORTH);
        panelBouton.add(this.boutonMethodes,   BorderLayout.CENTER);
        panelBouton.add(this.boutonActualiser, BorderLayout.SOUTH);

        this.boutonAttributs .addActionListener(this);
        this.boutonMethodes  .addActionListener(this);
        this.boutonActualiser.addActionListener(this);

        this.add(panelBouton, BorderLayout.SOUTH);
    }

    public JPanel getPanelProjets()
    {
        return this.panelProjets;
    }

    public PanneauDiagramme getPanneauDiagramme()
    {
        return this.panneauDiagramme;
    }


    private void majCouleurBouton(JButton btn)
    {
        Color actif = new Color(100, 150, 200);

        if (btn.getBackground().equals(actif))
        {
            btn.setBackground(Color.GRAY);
        }
        else
        {
            btn.setBackground(actif);
        }
    }

    /**
     * Ajoute un projet à la liste des projets affichés.
     *
     * @param cheminProjet chemin absolu du dossier du projet
     */
    public void ajouterProjet(String cheminProjet)
    {
        File projet = new File(cheminProjet);

        if (!projet.exists() || !projet.isDirectory())
        {
            return;
        }

        JScrollPane scrollPane = (JScrollPane) this.getComponent(1);

        JPanel panel = (JPanel) scrollPane.getViewport().getView();

        for (Component comp : panel.getComponents())
        {
            if (comp instanceof JButton)
            {
                JButton btn = (JButton) comp;

                if (btn.getText().equals(projet.getName()))
                {
                    return;
                }
            }
        }

        JButton bouton = this.creerBoutonProjet(projet);

        panel.add(bouton);
        panel.add(Box.createVerticalStrut(5));

        panel.revalidate();
        panel.repaint();
    }

    /**
     * Charge la liste des projets.
     *
     * @param panelProjets panneau cible
     */
    public void chargerProjets(JPanel panelProjets)
    {
        File dossier = new File(this.cheminDossiers);

        if (!dossier.exists() || !dossier.isDirectory())
        {
            JLabel erreur = new JLabel("Dossier non trouvé");
            erreur.setForeground(Color.RED);
            panelProjets.add(erreur);
            return;
        }

        File[] projets = dossier.listFiles(File::isDirectory);

        if (projets == null || projets.length == 0)
        {
            JLabel vide = new JLabel("Aucun projet");
            vide.setForeground(Color.GRAY);
            panelProjets.add(vide);
            return;
        }

        for (File projet : projets)
        {
            JButton bouton = this.creerBoutonProjet(projet);
            panelProjets.add(bouton);
            panelProjets.add(Box.createVerticalStrut(5));
        }
    }

    /**
     * Crée un bouton correspondant à un projet.
     *
     * @param projet dossier du projet
     * @return bouton configuré
     */
    public JButton creerBoutonProjet(File projet)
    {
        JButton bouton = new JButton(projet.getName());

        bouton.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 40)
        );
        bouton.setFont(new Font("Arial", Font.PLAIN, 12));
        bouton.setBackground(new Color(100, 150, 200));
        bouton.setForeground(Color.WHITE);
        bouton.setFocusPainted(false);

        bouton.addActionListener( e -> this.fenetrePrincipale.chargerProjet( projet.getAbsolutePath()));

        return bouton;
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == this.boutonActualiser)
        {
            this.panelProjets.removeAll();
            this.chargerProjets(this.panelProjets);
            this.panelProjets.revalidate();
            this.panelProjets.repaint();

            if (this.panneauDiagramme != null)
            {
                this.panneauDiagramme.clearDiagram();
            }
        }

        if (e.getSource() == this.boutonAttributs)
        {
            this.controleur.toggleAttributs();
            this.majCouleurBouton(this.boutonAttributs);
        }

        if (e.getSource() == this.boutonMethodes)
        {
            this.controleur.toggleMethodes();
            this.majCouleurBouton(this.boutonMethodes);
        }
    }
}
