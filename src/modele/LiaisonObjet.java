package modele;

public class LiaisonObjet 
{

	protected String nomAttribut; //Non utilisé en affichage
	protected ClasseObjet classeOrig;
	protected ClasseObjet classeDest;
	protected int num;

	private static int nbLiaisons = 0;

	public LiaisonObjet(String nomAttribut, ClasseObjet classeDest, ClasseObjet classeOrig) {
		num =  ++nbLiaisons;
		this.classeDest = classeDest;
		this.classeOrig = classeOrig;
		this.nomAttribut = nomAttribut;
	}

	public String      getNomAttribut() {    return nomAttribut ;   }
	public ClasseObjet getClasseOrig () {    return classeOrig  ;   }
	public ClasseObjet getClasseDest () {    return classeDest  ;   }
	public int         getNum        () {    return num         ;   }


	public void setNomAttribut( String      nomAttribut ) {    this.nomAttribut = nomAttribut ;   }
	public void setClasseOrig ( ClasseObjet classeOrig  ) {    this.classeOrig  = classeOrig  ;   }
	public void setClasseDest ( ClasseObjet classeDest  ) {    this.classeDest  = classeDest  ;   }
	public void setNum        ( int         num         ) {    this.num         = num         ;   }

	
}
