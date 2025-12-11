package modele;

/**
 * Représente une liaison générique entre deux classes dans un diagramme UML.
 * Cette classe sert de superclasse pour les relations comme :
 * - Héritage (HeritageObjet)
 * - Implémentation d'interface (InterfaceObjet)
 * 
 * Attributs principaux :
 * classeFille : la classe qui reçoit la relation (ex: enfant ou classe concrète)</li>
 * classeMere  : la classe source de la relation (ex: parent ou interface)</li>
 * nomAttribut : nom optionnel de la relation</li>
 * num         : identifiant unique de la liaison</li>
 */
public class LiaisonObjet 
{
    /** Nom optionnel de la liaison (non utilisé dans l'affichage UML). */
	protected String nomAttribut;
	/** Classe qui reçoit la relation (classe enfant ou classe concrète). */
	protected ClasseObjet classeFille;
	/** Classe source de la relation (classe parent ou interface). */
	protected ClasseObjet classeMere;
    /** Identifiant unique de la liaison. */
	protected int num;

    /** Compteur statique pour attribuer un numéro unique à chaque liaison. */
	private static int nbLiaisons = 0;


	 /**
     * Constructeur d'une liaison entre deux classes.
     *
     * @param nomAttribut Nom de la relation (souvent vide)
     * @param classeMere  Classe source (mere ou interface)
     * @param classeFille Classe cible  (fille ou classe concrète)
     */
	public LiaisonObjet(String nomAttribut, ClasseObjet classeMere, ClasseObjet classeFille)
	{
		this.nomAttribut = nomAttribut ;
		this.classeMere  = classeMere  ;
		this.classeFille = classeFille ;
		num              = ++nbLiaisons;
	}

    public LiaisonObjet(String nomAttribut, ClasseObjet classeFille) 
	{
		this.nomAttribut = nomAttribut ;
		this.classeFille  = classeFille  ;
		num              = ++nbLiaisons;
	}

	// ----------------- Getters -----------------

    /** @return le nom de la liaison */
    public String getNomAttribut()      { return this.nomAttribut; }

    /** @return la classe enfant ou classe concrète */
    public ClasseObjet getClasseFille() { return this.classeFille; }

    /** @return la classe parent ou interface */
    public ClasseObjet getClasseMere() { return  this.classeMere;  }

    /** @return le numéro unique de la liaison */
    public int getNum()                { return  this.num;         }

    public static void reinitialiserCompteur() {nbLiaisons = 0;}

	// ----------------- Setters -----------------

    /** @param nomAttribut nouveau nom de la liaison */
    public void setNomAttribut(String nomAttribut)      { this.nomAttribut = nomAttribut; }

    /** @param classeFille nouvelle classe enfant ou classe concrète */
    public void setClasseFille(ClasseObjet classeFille) { this.classeFille = classeFille; }

    /** @param classeMere nouvelle classe parent ou interface */
    public void setClasseMere(ClasseObjet classeMere)   { this.classeMere = classeMere;   }

    /** @param num nouveau numéro unique de la liaison */
    public void setNum(int num)                         { this.num = num;                 }
	
}
