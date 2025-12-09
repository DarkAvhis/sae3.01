package modele;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Représente une classe UML analysée à partir d'un fichier Java.
 * Contient son nom, sa liste d'attributs et sa liste de méthodes.
 */
public class ClasseObjet 
{
	/** Liste des attributs appartenant à la classe. */
	private ArrayList<AttributObjet> attributs;
	/** Liste des méthodes de la classe. */
	private ArrayList<MethodeObjet > methodes ;

	/** Nom de la classe. */
	private String nom;


	/**
     * Constructeur principal d'une classe UML.
     *
     * @param attributs liste des attributs de la classe
     * @param methodes  liste des méthodes de la classe
     * @param nom       nom de la classe
     */
	public ClasseObjet(ArrayList<AttributObjet> attributs, ArrayList<MethodeObjet> methodes, String nom) 
	{
		this.attributs = attributs;
		this.methodes = methodes;
		this.nom = nom;
	}

	/** @return le nom de la classe */
	public String                   getNom      () {return nom       ;}

	/** @return la liste des attributs */
	public ArrayList<AttributObjet> getattributs() {return attributs ;}

	/** @return la liste des méthodes */
	public ArrayList<MethodeObjet>  getMethodes () {return methodes  ;}


	/** @param nom nouveau nom de classe */
	public void setNom      (String nom                        ) {this.nom       = nom       ;}
	
	/** @param attributs nouvelle liste d'attributs */
	public void setAttributs(ArrayList<AttributObjet> attributs) {this.attributs = attributs ;}
	
	/** @param methodes nouvelle liste de méthodes */
	public void setMethodes (ArrayList<MethodeObjet>  methodes ) {this.methodes  = methodes  ;}
	

    /**
     * Convertit une visibilité Java en symbole UML.
     *
     * @param visibilite visibilité Java (public, private, protected)
     * @return symbole UML correspondant (+, -, #, ~)
     */
	public char changementVisibilite( String visibilite ) 
	{
		switch (visibilite) 
		{
			case "private"  : return '-';
			case "public"   : return '+';
			case "protected": return '#';
			default:          return '~';
		}
	}


	/**
     * Retourne un texte indiquant la portée (static ou vide).
     * 
     * @param portee valeur de portée (attendue : "static")
     * @return "static" ou " " si non static
     */
	public String changementPortee(String portee)
	{
		switch (portee)
		{
			case "static" : return "static";
			default: return " ";
		}
	}


	/**
     * Formate la liste des paramètres pour l'affichage UML.
     *
     * @param parametre map contenant <nomParamètre, typeParamètre>
     * @return représentation textuelle des paramètres entre parenthèses
     */
    public String affichageParametre(HashMap<String, String> parametre)
    {
        String sRet = "";

        if (parametre != null && !parametre.isEmpty())
        {
            sRet += "(";
            for (String nomParametre : parametre.keySet())
            {
                sRet += nomParametre + ": " + parametre.get(nomParametre) + ", ";
            }
            sRet = sRet.substring(0, sRet.length() - 2);
            sRet += ")";
        }
        else
        {
            sRet = "()";
        }
        return sRet;
    }


	/**
     * Formate le type de retour d'une méthode pour l'affichage UML.
     *
     * @param type type retourné par la méthode
     * @return texte formaté ou vide si aucun type utile
     */
	public String retourType(String type) 
	{
		if (type == null) 
		{
			return " "; 
		}
		if (type.equals("public") || type.equals("void")) 
		{
			return " "; 
		}
		return " : " + type;
	}


	
    /**
     * Affichage formaté de la classe UML (attributs + méthodes).
     *
     * @return représentation textuelle complète de la classe
     */
	@Override
	public String toString() 
	{
		String sRet = "";

		sRet += "-------------------------------------------------------------------------------------------\n";
		sRet += String.format( "%50s" ,  this.nom ) +              "\n";
		sRet += "-------------------------------------------------------------------------------------------\n";

		for (AttributObjet att : attributs) 
		{
			String staticFlag = att.estStatique() ? " {static}" : "";
			sRet += changementVisibilite(att.getVisibilite())  + 
					" " + att.getNom() + " : " + att.getType() + 
					staticFlag + "\n" ; 
		}

		sRet += "-------------------------------------------------------------------------------------------\n";

		for( MethodeObjet met : methodes )
		{
			String staticFlag = met.estStatique() ? "{static} " : "";

			sRet += String.format( "%-2c",    changementVisibilite(met.getVisibilite())) + staticFlag +
					String.format( "%-25s" ,  met.getNom()) + 
					String.format( "%-35s" ,  affichageParametre(met.getParametres()))  + 
					String.format( "%-15s",   retourType(met.getRetourType()) ) + "\n" ; 
		}
		sRet += "-------------------------------------------------------------------------------------------\n";

		return sRet;
	}
}
