package vue;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Petite classe utilitaire IHM pour gérer l'export depuis la fenêtre principale.
 */
public class ExportHelper {

    public static void exportDiagram(FenetrePrincipale vuePrincipale) {
        if (vuePrincipale == null)
            return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exporter le diagramme");
        chooser.setSelectedFile(new File("diagramme.png"));
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("Image PNG", "png"));

        int choix = chooser.showSaveDialog(vuePrincipale);
        if (choix != JFileChooser.APPROVE_OPTION)
            return;

        File fichier = chooser.getSelectedFile();

        if (!fichier.getName().toLowerCase().endsWith(".png")) {
            fichier = new File(fichier.getAbsolutePath() + ".png");
        }

        try {
            ExportHelper.exportComponent(
                    vuePrincipale.getPanneauDiagramme(),
                    fichier
            );
            JOptionPane.showMessageDialog(
                    vuePrincipale,
                    "Le diagramme a été exporté avec succès.\n\nFichier :\n"
                            + fichier.getAbsolutePath(),
                    "Export réussi",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    vuePrincipale,
                    "Erreur lors de l'export du diagramme :\n" + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void exportComponent(JComponent component, File file) throws IOException
    {
        Dimension size = component.getSize();
        if (size.width == 0 || size.height == 0)
        {
            size = component.getPreferredSize();
            component.setSize(size);
        }

        BufferedImage image = new BufferedImage(
            size.width,
            size.height,
            BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = image.createGraphics();
        component.paintAll(g2d);
        g2d.dispose();

        ImageIO.write(image, "png", file);
    }
}
