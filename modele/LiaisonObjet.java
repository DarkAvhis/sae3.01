package modele;

public class LiaisonObjet {

    protected String nomAttribut; //Non utilisé en affichage
    protected ClasseObjet classeOrig;
    protected ClasseObjet classeDest;

    public LiaisonObjet(String nomAttribut, ClasseObjet classeDest, ClasseObjet classeOrig) {
        this.classeDest = classeDest;
        this.classeOrig = classeOrig;
        this.nomAttribut = nomAttribut;
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

    
}
