package test;

public abstract class Animal
{
    private String nom;
    private int age;
    private static int compteur = 0;

    public Animal(String nom, int age)
    {
        this.nom = nom;
        this.age = age;
        synchronized (Animal.class)
        {
            compteur++;
        }
    }

    public String getNom()
    {
        return this.nom;
    }

    public int getAge()
    {
        return this.age;
    }

    public void setNom(String nom)
    {
        this.nom = nom;
    }

    public synchronized void setAge(int age)
    {
        this.age = age;
    }

    public static synchronized int getCompteur()
    {
        return compteur;
    }

    public abstract String parler();

    @Override
    public String toString()
    {
        return "Animal{nom=" + this.nom + ", age=" + this.age + "}";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Animal a = (Animal) o;
        if (this.age != a.age) return false;
        return this.nom == null ? a.nom == null : this.nom.equals(a.nom);
    }

    @Override
    public int hashCode()
    {
        int result = 17;
        result = 31 * result + (nom == null ? 0 : nom.hashCode());
        result = 31 * result + age;
        return result;
    }
}