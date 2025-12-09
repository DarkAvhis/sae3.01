package modele;

public class HeritageObjet extends LiaisonObjet 
{

    public HeritageObjet(ClasseObjet classeDest, ClasseObjet classeOrig) 
    {
        super(null, classeDest, classeOrig);
    }

    public String toString() 
    {
        return classeOrig.getNom() + " hérite de " + classeDest.getNom();
    }
     
}
