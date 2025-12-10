package vue;

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

    private JCheckBoxMenuItem afficherAttributsClasse;
    private JCheckBoxMenuItem afficherMethodesClasse;
    private JMenuItem         alignerClasse;
    private JMenuItem         optimiserClasse;

    private JMenuItem aProposClasse;

    private PanneauProjets panneauProjets;

    public BarreMenus(Controleur controleur, PanneauProjets panneauProjets) 
    {
<<<<<<< HEAD:retroconcepteur/src/vue/BarreMenus.java
        
=======
        this.controleur     = controleur;
        this.panneauProjets = panneauProjets;

>>>>>>> b720a42b10e613d36f8cac112876beabca0e536e:src/vue/BarreMenus.java
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

        this.afficherAttributsClasse = new JCheckBoxMenuItem("Afficher attributs",false);
        this.afficherMethodesClasse  = new JCheckBoxMenuItem("Afficher méthodes", false);
        this.alignerClasse           = new JMenuItem        ("Aligner les symboles"       );
        this.optimiserClasse         = new JMenuItem        ("Optimiser les positions"    );

        afficherAttributsClasse.addActionListener(this);
        afficherMethodesClasse .addActionListener(this);
        alignerClasse          .addActionListener(this);
        optimiserClasse        .addActionListener(this);

        menu.add(afficherAttributsClasse);
        menu.add(afficherMethodesClasse );
        menu.addSeparator(            );
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

        else if (src == afficherAttributsClasse) actionAffichageAttributs();
        else if (src == afficherMethodesClasse ) actionAffichageMethodes ();
        else if (src == alignerClasse          ) actionAligner           ();
        else if (src == optimiserClasse        ) actionOptimiser         ();
        else if (src == aProposClasse          ) actionAPropos           ();
    }

    public void actionNouveauProjet() 
    { 
        JOptionPane.showMessageDialog(null, "Pas fini"); 
    }

    //ouvrir mes dossier, les parcourir et l'ouvrir
    public void actionOuvrirProjet()  
    { 
        JFileChooser chooser = new JFileChooser(new java.io.File(System.getProperty("user.dir")));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 

        int retour = chooser.showOpenDialog(this);

        if (retour == JFileChooser.APPROVE_OPTION) 
        {
            File dossier = chooser.getSelectedFile();

            if (dossier.exists() && dossier.isDirectory()) 
            {
                try 
                {
                    this.controleur.analyserEtAfficherDiagramme(dossier.getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "Dossier chargé : " + dossier.getName());

                    // ajoute dans panneauProjet le projet ouvert
                    this.panneauProjets.ajouterProjet(dossier.getAbsolutePath());
                } 
                catch (Exception ex) 
                {
                    JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'ouverture du dossier :\n" + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
            else 
                JOptionPane.showMessageDialog(this, "Impossible d'ouvrir ce dossier.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actionSauvegarder()   
    { 
        this.controleur.sauvegarde();
    }

    public void actionAnnuler()       
    { 
        JOptionPane.showMessageDialog(null, "Pas fini");
    }

    public void actionRetablir()      
    { 
        JOptionPane.showMessageDialog(null, "Pas fini"); 
    }

    public void actionSupprimer()     
    { 
     
        int rep = JOptionPane.showConfirmDialog(
            null,
            "Voulez-vous vraiment supprimer la classe sélectionnée ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION
        );

        if (rep == JOptionPane.YES_OPTION)
        {
            this.controleur.supprimerClasseSelectionnee();
        }
        
    }

    public void actionAffichageAttributs() 
    { 
        /* À implémenter */ 
    }

    public void actionAffichageMethodes()  
    { 
        /* À implémenter */ 
    }

    public void actionAligner()    
    { 
        JOptionPane.showMessageDialog(null, "Pas fini"); 
    }

    public void actionOptimiser()  
    { 
        JOptionPane.showMessageDialog(null, "Pas fini"); 
    }

    public void actionAPropos()
    {
        JOptionPane.showMessageDialog(null,
            "Modélisation UML - Générateur de Diagrammes\n" +
            "par Quentin MORVAN,\nValentin LEROY,\nCelim CHAOU,\nEnzo DUMONT,\nAriunbayar BUYANBADRAKH,\nYassine EL MAADI",
            "À propos",
            JOptionPane.INFORMATION_MESSAGE);
    }
}