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
     
}
