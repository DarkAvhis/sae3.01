package src.vue;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ExportIHM 
{
    public static void exportComponent(JComponent component, String path) throws IOException 
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

        ImageIO.write(image, "png", new File(path));
    }
}