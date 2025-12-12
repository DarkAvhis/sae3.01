public class Chien {
	private String nom;
	private int age;

	public Chien(String nom, int age) 
	{
		this.nom = nom;
		this.age = age;
	}

	public String getNom() 
	{
		return nom;
	}

	public int getAge() 
	{
		return age;
	}

	public void aboyer() 
	{
		System.out.println(nom + "Ouaf");
	}
}