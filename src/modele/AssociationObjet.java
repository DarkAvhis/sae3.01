package modele;


/**
 * Représente une association entre deux classes dans un diagramme UML.
 * 
 * Une association peut être unidirectionnelle ou bidirectionnelle, 
 * et possède des multiplicité pour l'origine et la destination.
 */
public class AssociationObjet extends LiaisonObjet 
{
    /**
     * Indique si l'association est unidirectionnelle (true) ou bidirectionnelle (false).
     */
	private boolean estUnidirectionnel;

    /**
     * Multiplicité de l'origine de l'association.
     * Multiplicité de la destination de l'association.
     */
    private MultipliciteObjet multiDest;
	private MultipliciteObjet multiOrig;

	/**
     * Constructeur de l'association.
     * 
     * @param classeDest Classe cible de l'association
     * @param classeOrig Classe origine de l'association
     * @param multiDest Multiplicité de la classe cible
     * @param multiOrig Multiplicité de la classe origine
     * @param nomAttribut Nom de l'attribut représentant l'association
     * @param unidirectionnel Vrai si l'association est unidirectionnelle
     */
    public AssociationObjet(ClasseObjet classeDest, ClasseObjet classeOrig, MultipliciteObjet multDest, MultipliciteObjet multOrig, String nomAttribut, boolean unidirectionnel) 
    {
        super(nomAttribut, classeDest, classeOrig);
        this.multiDest          = multDest;
        this.multiOrig          = multOrig;
        this.estUnidirectionnel = unidirectionnel;
    }

    /**
     * Retourne la multiplicité de l'origine de l'association.
     * 
     * @return Multiplicité de l'origine
     */
    public MultipliciteObjet getMultOrig() {return multiOrig;}
    
    /**
     * Retourne la multiplicité de la destination de l'association.
     * 
     * @return Multiplicité de la destination
     */
    public MultipliciteObjet getMultDest() {return multiDest;}



    /**
     * Définit la multiplicité de l'origine de l'association.
     * 
     * @param multOrig Multiplicité de l'origine
     */
    public void setMultOrig(MultipliciteObjet multOrig) {this.multiOrig = multOrig;}

    /**
     * Définit la multiplicité de la destination de l'association.
     * 
     * @param multDest Multiplicité de la destination
     */
    public void setMultDest(MultipliciteObjet multDest) {this.multiDest = multDest;}


    /**
     * Indique si l'association est unidirectionnelle.
     * 
     * @return vrai si unidirectionnelle, sinon faux
     */
    public boolean estUnidirectionnel() 
    {
        return estUnidirectionnel;
    }

    /**
     * Définit si l'association est unidirectionnelle.
     * 
     * @param unidirectionnel vrai pour unidirectionnelle, faux pour bidirectionnelle
     */
    public void setUnidirectionnel(boolean unidirectionnel) 
    {
        this.estUnidirectionnel = unidirectionnel;
    }

    /**
     * Retourne une représentation textuelle de l'association.
     * 
     * Exemple : "Association 1 : unidirectionnelle de ClasseA(1..*) vers ClasseB(1)"
     * 
     * @return Chaîne décrivant l'association
     */
    @Override
    public String toString() 
    {
        String sens    = (this.estUnidirectionnel) ? "unidirectionnelle"       : "bidirectionnelle";
        String origine = (this.classeOrig != null) ? this.classeOrig.getNom()  : "?";
        String dest    = (this.classeDest != null) ? this.classeDest.getNom()  : "?";
        String multO   = (this.multiOrig  != null) ? this.multiOrig.toString() : "?";
        String multD   = (this.multiDest  != null) ? this.multiDest.toString() : "?";

        return String.format("Association %d : %s de %s(%s) vers %s(%s)",
                             this.getNum(), sens, origine, multO, dest, multD);
    }
     
}
