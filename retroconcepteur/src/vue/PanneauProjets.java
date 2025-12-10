package vue;

import java.awt.*;
import java.io.File;
import javax.swing.*;

import src.Controleur;

public class PanneauProjets extends JPanel
{
    private FenetrePrincipale fenetrePrincipale;
    private String cheminDossiers;

    public PanneauProjets(FenetrePrincipale fenetrePrincipale, Controleur controleur) 
    {
        this.fenetrePrincipale = fenetrePrincipale;
        
        this.cheminDossiers = "src";

        this.setLayout(new BorderLayout());
        this.setBackground(new Color(245, 245, 245));
        this.setBorder(BorderFactory.createTitledBorder("test"));

        // Titre
        JLabel titreLabel = new JLabel("Liste des Projets");
        titreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titreLabel.setHorizontalAlignment(JLabel.CENTER);
        
        this.add(titreLabel, BorderLayout.NORTH);

        // Panel scrollable
        JPanel panelProjets = new JPanel();
        panelProjets.setLayout(new BoxLayout(panelProjets, BoxLayout.Y_AXIS));
        panelProjets.setBackground(new Color(245, 245, 245));

        this.chargerProjets(panelProjets);

        JScrollPane scrollPane = new JScrollPane(panelProjets);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        this.add(scrollPane, BorderLayout.CENTER);

        //actualiser la liste
        JButton boutonActualiser = new JButton("Actualiser");
        boutonActualiser.addActionListener(e -> 
        {
            panelProjets.removeAll();
            this.chargerProjets(panelProjets);
            panelProjets.revalidate();
            panelProjets.repaint();
        });

        this.add(boutonActualiser, BorderLayout.SOUTH);
    }

    //Permet d'ajouter le projet
    public void ajouterProjet(String cheminProjet)
    {
        File projet = new File(cheminProjet);

        // Vérifier que le chemin existe et est un dossier
        if (!projet.exists() || !projet.isDirectory())
            return;

        // Récupérer JScrollPane → viewport → panelProjets
        JScrollPane scrollPane   = (JScrollPane) this.getComponent(1);
        JPanel      panelProjets = (JPanel) scrollPane.getViewport().getView();

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
}