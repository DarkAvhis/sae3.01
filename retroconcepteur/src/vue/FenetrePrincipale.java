package vue;

import java.awt.*;
import javax.swing.*;

import src.Controleur;

public class FenetrePrincipale extends JFrame 
{
	private Controleur controleur;
	private PanneauProjets panneauProjets;
	private PanneauDiagramme panneauDiagramme;

	public FenetrePrincipale(Controleur controleur) 
	{
		this.controleur = controleur;

		Dimension tailleEcran = Toolkit.getDefaultToolkit().getScreenSize();
		
		this.setTitle("Générateur de diagramme UML"); 
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(tailleEcran.width, tailleEcran.height);
		this.setLocationRelativeTo(null);
		this.setResizable(true);

		this.panneauProjets   = new PanneauProjets(this, controleur);
		this.panneauDiagramme = new PanneauDiagramme(controleur); 
		
		// CORRECTION : Encapsulation de panneauDiagramme dans un JScrollPane
		JScrollPane scrollDiagramme = new JScrollPane(this.panneauDiagramme);
		scrollDiagramme.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollDiagramme.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.setLayout(new BorderLayout());
		
		JSplitPane splitPane = new JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT,
			true,
			this.panneauProjets,
			scrollDiagramme // <-- CORRECTION : Utilisation du JScrollPane
		);
		splitPane.setDividerLocation(250);
		splitPane.setOneTouchExpandable(true);

		this.add(splitPane, BorderLayout.CENTER);
		this.add(new BarreMenus(controleur), BorderLayout.NORTH); 
	}

	public void chargerProjet(String cheminProjet) 
	{
		this.controleur.analyserEtAfficherDiagramme(cheminProjet); 
	}

	public PanneauDiagramme getPanneauDiagramme() 
	{
	   return this.panneauDiagramme;
	}
}