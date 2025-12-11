package test;

public class Chat extends Animal 
{
    private String race;
    private static final String ESPECE = "Canis familiaris";
    private Chien[] chien ;
    private Disque[] disques;

    public Chat(String nom, int age, String race)
    {
        super(nom, age);
        this.race = race;
    }

    public String getRace()
    {
        return this.race;
    }

    public void setRace(String race)
    {
        this.race = race;
    }

    @Override
    public synchronized String parler()
    {
        return "Woof";
    }

    public void miauler()
    {
        System.out.println(getNom() + " miaule: " + parler());
    }

    public static Chat creerChienUnitaire()
    {
        return new Chat("Rex", 1, "Inconnue");
    }

    @Override
    public String toString()
    {
        return "Chien{nom=" + getNom() + ", age=" + getAge() + ", race=" + this.race + "}";
    }
    
}
