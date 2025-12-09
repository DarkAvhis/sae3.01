package modele;

public class AssociationObjet extends LiaisonObjet {

    private boolean unidirectionnel;
    private MultipliciteObjet multOrig;
    private MultipliciteObjet multDest;

    public AssociationObjet(ClasseObjet classeDest, ClasseObjet classeOrig, MultipliciteObjet multDest, MultipliciteObjet multOrig, String nomAttribut, boolean unidirectionnel) {
        super(nomAttribut, classeDest, classeOrig);
        this.multDest = multDest;
        this.multOrig = multOrig;
        this.unidirectionnel = unidirectionnel;
    }

    public boolean isUnidirectionnel() {
        return unidirectionnel;
    }

    public void setUnidirectionnel(boolean unidirectionnel) {
        this.unidirectionnel = unidirectionnel;
    }

    public MultipliciteObjet getMultOrig() {
        return multOrig;
    }

    public void setMultOrig(MultipliciteObjet multOrig) {
        this.multOrig = multOrig;
    }

    public MultipliciteObjet getMultDest() {
        return multDest;
    }

    public void setMultDest(MultipliciteObjet multDest) {
        this.multDest = multDest;
    }
    
    @Override
    public String toString() {
        String sens = (this.unidirectionnel) ? "unidirectionnelle" : "bidirectionnelle";
        String origine = (this.classeOrig != null) ? this.classeOrig.getNom() : "?";
        String dest = (this.classeDest != null) ? this.classeDest.getNom() : "?";
        String multO = (this.multOrig != null) ? this.multOrig.toString() : "?";
        String multD = (this.multDest != null) ? this.multDest.toString() : "?";

        return String.format("Association %d : %s de %s(%s) vers %s(%s)",
                this.getNum(), sens, origine, multO, dest, multD);
    }
     
}
