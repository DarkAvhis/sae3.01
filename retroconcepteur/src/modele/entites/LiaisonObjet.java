package src.modele.entites;

import src.modele.LiaisonObjet;

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
    /*-------------------------------------- */
	/* Attributs                             */
	/*-------------------------------------- */
	protected String      nomAttribut;
	protected ClasseObjet classeFille;
	protected ClasseObjet classeMere;
	protected int         num;

	private static int    nbLiaisons = 0;

    /*-------------------------------------- */
	/* Constructeurs                         */
	/*-------------------------------------- */
	public LiaisonObjet(String nomAttribut, ClasseObjet classeMere, ClasseObjet classeFille)
	{
		this.nomAttribut = nomAttribut ;
		this.classeMere  = classeMere  ;
		this.classeFille = classeFille ;
		this.num         = ++LiaisonObjet.nbLiaisons;
	}

    public LiaisonObjet(ClasseObjet classeMere, ClasseObjet classeFille) 
	{
        this("",classeMere,classeFille);
		this.num          = ++LiaisonObjet.nbLiaisons;
	}

    public LiaisonObjet(ClasseObjet classeFille) 
	{
		this("",null,classeFille);
		this.num          = ++LiaisonObjet.nbLiaisons;
	}

	/*-------------------------------------- */
	/* Les Accesseurs                        */
	/*-------------------------------------- */
    public String      getNomAttribut() { return this.nomAttribut; }
    public ClasseObjet getClasseFille() { return this.classeFille; }
    public ClasseObjet getClasseMere () { return  this.classeMere; }
    public int         getNum        () { return  this.num;        }
    
	/*-------------------------------------- */
	/* Modificateurs                         */
	/*-------------------------------------- */
    public void setNomAttribut(String      nomAttribut) { this.nomAttribut = nomAttribut ;   }
    public void setClasseFille(ClasseObjet classeFille) { this.classeFille = classeFille ;   }
    public void setClasseMere (ClasseObjet classeMere ) { this.classeMere  = classeMere  ;   }
    public void setNum        (int         num        ) { this.num         = num         ;   }

    /*-------------------------------------- */
	/* Autre methodes                        */
	/*-------------------------------------- */
    public static void reinitialiserCompteur() {LiaisonObjet.nbLiaisons = 0;}
}