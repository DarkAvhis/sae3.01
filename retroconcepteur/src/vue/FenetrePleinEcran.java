package vue;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Fenêtre plein écran pour afficher la classe complète avec tous ses attributs et méthodes.
 * 
 * S'ouvre au clic-droit sur un bloc classe et affiche :
 * - Tous les attributs
 * - Tous les paramètres de toutes les méthodes
 * 
 * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT, Ariunbayar
 *         BUYANBADRAKH, Yassine EL MAADI
 * @date 16 décembre 2025
 */
public class FenetrePleinEcran extends JFrame {
    private JTextArea textArea;
    private BlocClasse bloc;

    public FenetrePleinEcran(BlocClasse bloc) {
        this.bloc = bloc;
        
        this.setTitle("Détails de la classe: " + bloc.getNom());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(700, 600);
        this.setLocationRelativeTo(null);
        this.setResizable(true);

        // Créer la zone de texte
        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // Ajouter un ascenseur
        JScrollPane scrollPane = new JScrollPane(textArea);
        this.add(scrollPane, BorderLayout.CENTER);

        afficherDetailsComplets();

        this.setVisible(true);
    }

    /**
     * Affiche tous les détails de la classe (attributs et méthodes complets).
     */
    private void afficherDetailsComplets() {
        String contenu = "\n";
        contenu += "┌─────────────────────────────────────────────┐\n";
        contenu += "│  CLASS: " + bloc.getNom() + "\n";
        contenu += "└─────────────────────────────────────────────┘\n\n";

        contenu += "ATTRIBUTS (" + bloc.getAttributsComplets().size() + ")\n";
        contenu += "─────────────────────────────────────────────\n";
        if (bloc.getAttributsComplets().isEmpty()) {
            contenu += "   (aucun)\n";
        } else {
            for (String attr : bloc.getAttributsComplets()) {
                contenu += "    " + attr + "\n";
            }
        }

        contenu += "\nMETHODES (" + bloc.getMethodesCompletes().size() + ")\n";
        contenu += "─────────────────────────────────────────────\n";
        if (bloc.getMethodesCompletes().isEmpty()) {
            contenu += "   (aucune)\n";
        } else {
            for (String method : bloc.getMethodesCompletes()) {
                contenu += "    " + method + "\n";
            }
        }

        textArea.setText(contenu);
    }
}
