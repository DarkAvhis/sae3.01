public class AssociationObjet {

    private String nomAttribut; //Non utilisé en affichage
    private ClasseObjet classeOrig;
    private ClasseObjet classeDest;
    private boolean unidirectionnel;
    private MultipliciteObjet multOrig;
    private MultipliciteObjet multDest;

    public AssociationObjet(ClasseObjet classeDest, ClasseObjet classeOrig, MultipliciteObjet multDest, MultipliciteObjet multOrig, String nomAttribut, boolean unidirectionnel) {
        this.classeDest = classeDest;
        this.classeOrig = classeOrig;
        this.multDest = multDest;
        this.multOrig = multOrig;
        this.nomAttribut = nomAttribut;
        this.unidirectionnel = unidirectionnel;
    }

    public String getNomAttribut() {
        return nomAttribut;
    }

    public void setNomAttribut(String nomAttribut) {
        this.nomAttribut = nomAttribut;
    }

    public ClasseObjet getClasseOrig() {
        return classeOrig;
    }

    public void setClasseOrig(ClasseObjet classeOrig) {
        this.classeOrig = classeOrig;
    }

    public ClasseObjet getClasseDest() {
        return classeDest;
    }

    public void setClasseDest(ClasseObjet classeDest) {
        this.classeDest = classeDest;
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
