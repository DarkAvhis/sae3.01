package modele;

public class LiaisonObjet 
{

	protected String nomAttribut; //Non utilisé en affichage
	protected ClasseObjet classeFille;
	protected ClasseObjet classeMere;
	protected int num;

	private static int nbLiaisons = 0;

	public LiaisonObjet(String nomAttribut, ClasseObjet classeDest, ClasseObjet classeOrig) {
		num =  ++nbLiaisons;
		this.classeMere = classeDest;
		this.classeFille = classeOrig;
		this.nomAttribut = nomAttribut;
	}

	public String      getNomAttribut() {    return nomAttribut ;   }
	public ClasseObjet getClasseFille () {    return classeFille  ;   }
	public ClasseObjet getClasseMere () {    return classeMere  ;   }
	public int         getNum        () {    return num         ;   }


	public void setNomAttribut( String      nomAttribut ) {    this.nomAttribut = nomAttribut ;   }
	public void setClasseOrig ( ClasseObjet classeOrig  ) {    this.classeFille  = classeFille  ;   }
	public void setClasseDest ( ClasseObjet classeDest  ) {    this.classeMere  = classeMere  ;   }
	public void setNum        ( int         num         ) {    this.num         = num         ;   }

	
}
