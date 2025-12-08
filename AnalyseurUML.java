import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.*;

public class AnalyseurUML 
{
    public static void main(String[] args) 
    {
        
    }



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