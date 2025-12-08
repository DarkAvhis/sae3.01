package modele;

public class HeritageObjet extends LiaisonObjet {

    public HeritageObjet(ClasseObjet classeDest, ClasseObjet classeOrig, MultipliciteObjet multDest, MultipliciteObjet multOrig, String nomAttribut, boolean unidirectionnel) {
        super(nomAttribut, classeDest, classeOrig);
    }
     
}
