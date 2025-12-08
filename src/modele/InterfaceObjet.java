package modele;

public class InterfaceObjet extends LiaisonObjet {

    public InterfaceObjet(ClasseObjet classeDest, ClasseObjet classeOrig, MultipliciteObjet multDest, MultipliciteObjet multOrig, String nomAttribut, boolean unidirectionnel) {
        super(nomAttribut, classeDest, classeOrig);
    }

    @Override
    public String toString() {
        return classeDest.getNom() + "\timplémente " + classeOrig.getNom();
    }
     
}
