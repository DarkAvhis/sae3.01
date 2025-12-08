import java.io.File;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class LireFichier 
{

    private List<Argument> listeArguments;
    private List<Methode>   listeMethodes;


    public LireFichier(String nomFichier) 
    {
        this.listeArguments  = new ArrayList<Argument>();
        this.listeMethodes   = new ArrayList<Methode>();

        try 
        {
            Scanner scannerFichier = new Scanner(new File("./Point.java"));

            while (scannerFichier.hasNextLine()) 
            {
                Scanner scannerLigne = new Scanner(scannerFichier.nextLine());
                
                while (scannerLigne.hasNext()) 
                {
                    if (scannerLigne.next().equals("private")) 
                    {
                        String type = scannerLigne.next();
                        String nom  = scannerLigne.next().replace(";", "");

                        Argument arg = new Argument(nom, type, "private", "instance");
                        this.listeArguments.add(arg);

                        System.out.println(arg.toString());
                    }

                    if (scannerLigne.next().equals("public"))
                    {
                        String nom        = scannerLigne.next().substring(0, scannerLigne.next().indexOf('('));
                        String typeRetour = scannerLigne.next();
                        String visibilite = "public";

                        List<Argument> argsMethode = new ArrayList<Argument>();
                        Methode methode = new Methode(nom, typeRetour, visibilite, argsMethode);
                        this.listeMethodes.add(methode);

                        System.out.println(methode.toString());
                    }
                }

                scannerLigne.close();
            }
            scannerFichier.close();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }        
    }

    public static void main(String[] args) 
    {
        LireFichier lf = new LireFichier("./Point.java");
    }
}