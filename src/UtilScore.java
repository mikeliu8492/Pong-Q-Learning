public class UtilScore 
{
	public double utility; //utility for a specific action taken from state
	public int attempts; //previous attempts of that action from specific state
	
	
	//constructor
	public UtilScore()
	{
		this.utility = 0;
		this.attempts = 0;
	}
	
	
	/**
	 * Debugging printing for object
	 */
	public void printUtilityMeasures()
	{
		System.out.println("My Utility:  " + this.utility);
		System.out.println("My attempts:  " + this.attempts);
	}
}
