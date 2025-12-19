package modele;

import java.io.*;
import java.util.*;

/**
 * Classe pour sauvegarder des classes en UML
 */
public class Sauvegarde
{
    /**
     * Sauvegarde les classes dans un fichier
     */
    public static void sauvegarder(String dossier, String fichier)
    {
        try
        {
            File dossierFile = new File(dossier);

            if (!dossierFile.exists() || !dossierFile.isDirectory())
            {
                System.out.println("Erreur : dossier invalide ou introuvable : " + dossier);
                return;
            }

            File outFile = new File(fichier);

            if (outFile.getParentFile() != null)
                outFile.getParentFile().mkdirs();

            FileWriter writer = new FileWriter(outFile);
            
            // Trouver tous les fichiers .java DANS LE dossier fournis UNIQUEMENT
            List<File> fichiers = trouverFichiers(dossierFile);
            
            writer.write("=== DIAGRAMME UML ===\n\n");
            
            // Pour chaque fichier
            for (File f : fichiers)
            {
                writer.write("Classe : " + f.getName()   + "\n");
                writer.write("--------------------------\n");
                
                // Lire le fichier
                BufferedReader reader = new BufferedReader(new FileReader(f));
                String ligne;
                
                writer.write("Attributs:\n");
                while ((ligne = reader.readLine()) != null)
                {
                    // Si c'est un attribut
                    if (ligne.contains("private ") || ligne.contains("public "))
                    {
                        if (ligne.contains(";") && !ligne.contains("("))
                            writer.write("  " + ligne + "\n");
                    }
                }
                reader.close();
                
                // Relire pour les méthodes
                reader = new BufferedReader(new FileReader(f));

                writer.write("\nMéthodes:\n");

                while ((ligne = reader.readLine()) != null)
                {
                    // Si c'est une méthode
                    if (ligne.contains("public ") || ligne.contains("private "))
                    {
                        if (ligne.contains("(") && ligne.contains(")"))
                        {
                            if (!ligne.contains(";"))
                                writer.write("  " + ligne + "\n");
                        }
                    }
                }
                reader.close();
                
                writer.write("\n\n");
            }
            
            writer.close();

            System.out.println("Fichier sauvegardé : " + outFile.getAbsolutePath());

        }catch (Exception e){  System.out.println("Erreur : " + e.getMessage()); }
    }
    
    /**
     * Trouve les fichiers .java
     */
    private static List<File> trouverFichiers(File dossier)
    {
        List<File> fichiers = new ArrayList<>();
        
        File[] files = dossier.listFiles();

        if (files != null)
        {
            for (File f : files)
            {
                if (f.isDirectory())
                    fichiers.addAll(trouverFichiers(f));

                else if (f.getName().endsWith(".java"))
                    fichiers.add(f);
            }
        }
        return fichiers;
    }
    
    /**
     * Test
     */
    public static void main(String[] args)
    {
        sauvegarder("src", "diagramme.txt");
    }
}