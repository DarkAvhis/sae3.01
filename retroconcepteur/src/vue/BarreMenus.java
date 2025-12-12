package src.vue;

import java.awt.event.*;
import java.io.File;

import javax.swing.*;

import src.Controleur;

public class BarreMenus extends JMenuBar implements ActionListener
{

    // Références aux Classes pour les reconnaître dans actionPerformed
    private JMenuItem nouvelleClasse;
    private JMenuItem ouvrirClasse;
    private JMenuItem sauvegarderClasse;
    private JMenuItem quitterClasse;

    private JMenuItem annulerClasse;
    private JMenuItem retablirClasse;
    private JMenuItem supprimerClasse;

    private JMenuItem         alignerClasse;
    private JMenuItem         optimiserClasse;

    private JMenuItem aProposClasse;

    private PanneauProjets panneauProjets;
    private Controleur controleur;

    public BarreMenus(Controleur controleur, PanneauProjets panneauProjets) 
    {
        this.controleur     = controleur;
        this.panneauProjets = panneauProjets;

        this.add(creerMenuFichier());
        this.add(creerMenuEdition());
        this.add(creerMenuAffichage());
        this.add(creerMenuAide());
    }

    public JMenu creerMenuFichier() 
    {
        JMenu menu = new JMenu("Fichier");

        this.nouvelleClasse    = new JMenuItem("Nouveau projet"       );
        this.ouvrirClasse      = new JMenuItem("Ouvrir projet"        );
        this.sauvegarderClasse = new JMenuItem("Sauvegarder positions");
        this.quitterClasse     = new JMenuItem("Quitter"              );

        this.nouvelleClasse   .addActionListener(this);
        this.ouvrirClasse     .addActionListener(this);
        this.sauvegarderClasse.addActionListener(this);
        this.quitterClasse    .addActionListener(this);

        menu.add(nouvelleClasse   );
        menu.add(ouvrirClasse     );
        menu.addSeparator(      );
        menu.add(sauvegarderClasse);
        menu.add(quitterClasse    );

        return menu;
    }

    public JMenu creerMenuEdition() 
    {
        JMenu menu = new JMenu("Édition");

        this.annulerClasse   = new JMenuItem("Annuler"  );
        this.retablirClasse  = new JMenuItem("Rétablir" );
        this.supprimerClasse = new JMenuItem("Supprimer");

        annulerClasse  .addActionListener(this);
        retablirClasse .addActionListener(this);
        supprimerClasse.addActionListener(this);

        menu.add(annulerClasse  );
        menu.add(retablirClasse );
        menu.addSeparator(    );
        menu.add(supprimerClasse);

        return menu;
    }

    public JMenu creerMenuAffichage() 
    {
        JMenu menu = new JMenu("Affichage");

        this.alignerClasse           = new JMenuItem        ("Aligner les symboles"       );
        this.optimiserClasse         = new JMenuItem        ("Optimiser les positions"    );

        alignerClasse          .addActionListener(this);
        optimiserClasse        .addActionListener(this);

        menu.add(alignerClasse          );
        menu.add(optimiserClasse        );

        return menu;
    }

    public JMenu creerMenuAide() 
    {
        JMenu menu = new JMenu("Aide");

        this.aProposClasse = new JMenuItem("À propos");
        aProposClasse.addActionListener(this);

        menu.add(aProposClasse);

        return menu;
    }
 
    public void actionPerformed(ActionEvent e)
    {
        Object src = e.getSource();
        
             if (src == nouvelleClasse   ) actionNouveauProjet();
        else if (src == ouvrirClasse     ) actionOuvrirProjet ();
        else if (src == sauvegarderClasse) actionSauvegarder  ();
        else if (src == quitterClasse    ) System.exit(0);

        else if (src == annulerClasse  )   actionAnnuler  ();
        else if (src == retablirClasse )   actionRetablir ();
        else if (src == supprimerClasse)   actionSupprimer();

        else if (src == alignerClasse          ) actionAligner           ();
        else if (src == optimiserClasse        ) actionOptimiser         ();
        else if (src == aProposClasse          ) actionAPropos           ();
    }

    public void actionNouveauProjet() 
    { 
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Pas fini"); 
    }

    //ouvrir mes dossier, les parcourir et l'ouvrir
    public void actionOuvrirProjet()  
    { 
        JFileChooser chooser = new JFileChooser(new java.io.File(System.getProperty("user.dir")));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 

        int retour = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));

        if (retour == JFileChooser.APPROVE_OPTION) 
        {
            File dossier = chooser.getSelectedFile();

            if (dossier.exists() && dossier.isDirectory()) 
            {
                try 
                {
                    this.controleur.analyserEtAfficherDiagramme(dossier.getAbsolutePath());
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Dossier chargé : " + dossier.getName());

                    // ajoute dans panneauProjet le projet ouvert
                    this.panneauProjets.ajouterProjet(dossier.getAbsolutePath());
                } 
                catch (Exception ex) 
                {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                        "Erreur lors de l'ouverture du dossier :\n" + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
            else 
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Impossible d'ouvrir ce dossier.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actionSauvegarder()   
    { 
        this.controleur.sauvegarde();
    }

    public void actionAnnuler()       
    { 
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Pas fini");
    }

    public void actionRetablir()      
    { 
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Pas fini"); 
    }

    public void actionSupprimer()     
    { 
     
        int rep = JOptionPane.showConfirmDialog(
            SwingUtilities.getWindowAncestor(this),
            "Voulez-vous vraiment supprimer la classe sélectionnée ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION
        );

        if (rep == JOptionPane.YES_OPTION)
        {
            this.controleur.supprimerClasseSelectionnee();
        }
        
    }


    public void actionAligner()    
    { 
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Pas fini"); 
    }

    public void actionOptimiser()  
    { 
        // Récupérer le panneau du diagramme via le contrôleur
        this.controleur.optimiserDisposition();
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Positions optimisées !", "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    public void actionAPropos()
    {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
            "Modélisation UML - Générateur de Diagrammes\n" +
            "par Quentin MORVAN,\nValentin LEROY,\nCelim CHAOU,\nEnzo DUMONT,\nAriunbayar BUYANBADRAKH,\nYassine EL MAADI",
            "À propos",
            JOptionPane.INFORMATION_MESSAGE);
    }
}