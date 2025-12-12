package src.modele;

import java.util.HashMap;

/**
 * Représente une méthode d'une classe UML.
 * 
 * Une méthode possède un nom, une visibilité, un type de retour,
 * une liste de paramètres et peut être marquée comme statique.
 * 
 * @author Quentin MORVAN, Valentin LEROY, Celim CHAOU, Enzo DUMONT, Ariunbayar
 *         BUYANBADRAKH, Yassine EL MAADI
 * @date 12 décembre 2025
 */
public class MethodeObjet
{
    /** Nom de la méthode */
    private String nom;
    /** Visibilité de la méthode : public, private ou protected */
    private String visibilite;
    /** Type de retour de la méthode (null ou void si aucun retour) */
    private String retourType;
    /** Paramètres de la méthode : clé = nom, valeur = type */
    private HashMap<String, String> parametres;
    /** Indique si la méthode est statique */
    private boolean estStatique;

    // ----------------- Constructeurs -----------------

    /**
     * Constructeur complet d'une méthode.
     *
     * @param nom         Nom de la méthode
     * @param parametres  Paramètres sous forme de HashMap (nom -> type)
     * @param retourType  Type de retour (peut être null pour un constructeur)
     * @param visibilite  Visibilité de la méthode ("public", "private",
     *                    "protected")
     * @param estStatique true si la méthode est statique, false sinon
     */
    public MethodeObjet(String nom, HashMap<String, String> parametres, String retourType, String visibilite,
            boolean estStatique) 
{
        this.nom = nom;
        this.parametres = parametres;
        this.retourType = retourType;
        this.visibilite = visibilite;
        this.estStatique = estStatique;
    }

    /**
     * Constructeur simplifié d'une méthode sans type de retour.
     * 
     * Utilisé principalement pour les constructeurs.
     *
     * @param nom         Nom de la méthode
     * @param parametres  Paramètres sous forme de HashMap (nom -> type)
     * @param visibilite  Visibilité de la méthode
     * @param estStatique true si la méthode est statique, false sinon
     */
    public MethodeObjet(String nom, HashMap<String, String> parametres, String visibilite, boolean estStatique) 
{
        this.nom = nom;
        this.parametres = parametres;
        this.visibilite = visibilite;
        this.estStatique = estStatique;
    }

    // ----------------- Getters -----------------

    /** @return Nom de la méthode */
    public String getNom() 
{
        return nom;
    }

    /** @return Visibilité de la méthode */
    public String getVisibilite() 
{
        return visibilite;
    }

    /** @return Type de retour de la méthode */
    public String getRetourType() 
{
        return retourType;
    }

    /** @return Paramètres de la méthode sous forme de HashMap (nom -> type) */
    public HashMap<String, String> getParametres() 
{
        return parametres;
    }

    /** @return true si la méthode est statique, false sinon */
    public boolean estStatique() 
{
        return estStatique;
    }

    // ----------------- Setters -----------------

    /** @param nom Nouveau nom de la méthode */
    public void setNom(String nom) 
{
        this.nom = nom;
    }

    /** @param visibilite Nouvelle visibilité de la méthode */
    public void setVisibilite(String visibilite) 
{
        this.visibilite = visibilite;
    }

    /** @param retourType Nouveau type de retour */
    public void setRetourType(String retourType) 
{
        this.retourType = retourType;
    }

    /** @param parametres Nouvelle liste de paramètres (nom -> type) */
    public void setParametres(HashMap<String, String> parametres) 
{
        this.parametres = parametres;
    }

    /** @param statique Définit si la méthode est statique */
    public void setStatique(boolean estStatique) 
{
        this.estStatique = estStatique;
    }

}
