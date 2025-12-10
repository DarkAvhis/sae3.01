package test;

public class Chien extends Animal implements Capacte
{
    private String race;
    private static final String ESPECE = "Canis familiaris";

    public Chien(String nom, int age, String race)
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
    public String parler()
    {
        return "Woof";
    }

    public void aboyer()
    {
        System.out.println(getNom() + " aboie: " + parler());
    }

    public static Chien creerChienUnitaire()
    {
        return new Chien("Rex", 1, "Inconnue");
    }

    @Override
    public String toString()
    {
        return "Chien{nom=" + getNom() + ", age=" + getAge() + ", race=" + this.race + "}";
    }
}