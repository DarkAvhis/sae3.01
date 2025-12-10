package modele;


/**
 * Représente une association entre deux classes dans un diagramme UML.
 */
public class AssociationObjet extends LiaisonObjet 
{
    
    // Suppression des attributs masqués (classeDest, classeOrig)
    
	private boolean estUnidirectionnel;
    private MultipliciteObjet multiDest;
	private MultipliciteObjet multiOrig;


	/*-------------------------------------- */
	/* Constructeur                          */
	/*-------------------------------------- */
    
    // Le constructeur reçoit classeDest et classeOrig (qui deviennent classeMere et classeFille dans LiaisonObjet)
    public AssociationObjet(ClasseObjet classeMere, ClasseObjet classeFille, MultipliciteObjet multiDest, MultipliciteObjet multiOrig, String nomAttribut, boolean unidirectionnel) 
    {
        // Appel au constructeur de LiaisonObjet (important)
        super(nomAttribut, classeMere, classeFille);
        this.multiDest          = multiDest;
        this.multiOrig          = multiOrig;
        this.estUnidirectionnel = unidirectionnel;         
    }

    /*-------------------------------------- */
	/* Les Accesseurs                        */
	/*-------------------------------------- */
    
    // Note: Les accesseurs getMultOrig/Dest et setMultOrig/Dest sont inchangés.
    public MultipliciteObjet getMultOrig       () {    return multiOrig        ;   }
    public MultipliciteObjet getMultDest       () {    return multiDest        ;   }
    public boolean           getUnidirectionnel() {    return estUnidirectionnel ;   } // Correction: doit utiliser estUnidirectionnel si c'est la variable utilisée dans le constructeur.

    public void setMultOrig       (MultipliciteObjet multOrig) {    this.multiOrig        = multOrig        ;   }
    public void setMultDest       (MultipliciteObjet multDest) {    this.multiDest        = multDest        ;   }
    public void setUnidirectionnel(boolean unidirectionnel   ) {    this.estUnidirectionnel = unidirectionnel ;   } // Correction: doit modifier estUnidirectionnel

    /*-------------------------------------- */
	/* toString (Correction)                 */
	/*-------------------------------------- */
   
    @Override
    public String toString() 
    {
        String sens    = (this.estUnidirectionnel) ? "unidirectionnelle"       : "bidirectionnelle";
        
        // CORRECTION MAJEURE: Utilisation des accesseurs de LiaisonObjet (getClasseFille/Mere)
        String origine = (this.getClasseFille() != null) ? this.getClasseFille().getNom()  : "?"; // Classe FILLE = Origine de l'association (classe qui possède l'attribut)
        String dest    = (this.getClasseMere() != null) ? this.getClasseMere().getNom()  : "?";   // Classe MERE  = Destination de l'association (type de l'attribut)
        
        String multO   = (this.multiOrig  != null) ? this.multiOrig.toString() : "?";
        String multD   = (this.multiDest  != null) ? this.multiDest.toString() : "?";

        return String.format("Association %d : %s de %s(%s) vers %s(%s)",
                             this.getNum(), sens, origine, multO, dest, multD);
    }
     
}