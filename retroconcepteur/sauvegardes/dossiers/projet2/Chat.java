public class Chat 
{
	private String nom;
	private int age;
	private String couleur;

	public Chat(String nom, int age, String couleur) 
	{
		this.nom = nom;
		this.age = age;
		this.couleur = couleur;
	}

	public String getNom() 
	{
		return nom;
	}

	public int getAge() 
	{
		return age;
	}

	public String getCouleur() 
	{
		return couleur;
	}

	public void miauler() 
	{
		System.out.println(nom + "Miaou");
	}

	public void vieillir() 
	{
		age++;
	}
}
