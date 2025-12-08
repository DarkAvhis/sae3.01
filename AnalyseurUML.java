import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AnalyseurUML 
{
    public static void main(String[] args) 
    {
        
    }


    //Méthode pour lire tous les fichiers d'un répertoire
    public List<File> ClassesDuDossier(String cheminDossier)
    {
        File dossier = new File(cheminDossier);
        File[] tousLesFichiers = dossier.listFiles(); 
        List<File> fichiersJava = new ArrayList<>();

        if (tousLesFichiers != null) 
        {
            for (File f : tousLesFichiers) 
            {
                if (f.getName().endsWith(".java")) 
                {
                    fichiersJava.add(f);
                }
                
            }
        }

        return fichiersJava;
    }
    
}