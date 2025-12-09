public class Disque 
{
	private double rayon;
	private Point centre;

	public Disque(double rayon, Point centre) 
	{
		this.rayon  = rayon;
		this.centre = centre;
	}

	public double getRayon() 
	{
		return this.rayon;
	}

	public void setRayon(double rayon) 
	{
		this.rayon = rayon;
	}

	public Point getCentre() 
	{
		return this.centre;
	}

	public void setCentre(Point centre) 
	{
		this.centre = centre;
	}
	
	public double calculerAire() 
	{
		return Math.PI * Math.pow(this.rayon, 2);
	}

	public double calculerPerimetre() 
	{
		return 2 * Math.PI * this.rayon;
	}
}
