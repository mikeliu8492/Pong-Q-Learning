//import java.util.*;
//import java.lang.*;

public class DiscreteState 
{
	/**
	 * row, column, and paddle position are discretized based on the MAXIMUM_DISCRETE_UNITS
	 * set forth in the Game Session 
	 */

	//row in discrete representation (x-coordinate)
	public int row;

	//column in discrete representation (y-coordinate)
	public int column;
	
	/**
	 * Velocity in x-direction.
	 * -1 for away from paddle
	 * +1 for towards paddle
	 */
	public int xVelocity;
	
	
	/**
	 * Velocity in y-direction
	 * -1 for velocity upwards in screen where abs(y-velocity) > 0.015
	 * +1 for velocity downwards in screen where abs(y-velocity) > 0.015
	 * 0 for velocity in screen where abs(y-velocity) < 0.015.  Little to no up/down trajectory.
	 */
	public int yVelocity;
	
	//discrete representation of paddle position
	public int paddlePosition;

	
	/**
	 * Constructor for creating a discrete state to evaluate.
	 */
	public DiscreteState(int row, int column, int x, int y, int paddle)
	{
		this.row = row;
		this.column = column;
		this.xVelocity = x;
		this.yVelocity = y;
		this.paddlePosition = paddle;
	}
	
	
	//printing information for debugging purposes
	public void printInfo()
	{
		System.out.println("Row:  " + this.row);
		System.out.println("Column:  " + this.column);
		System.out.println("XVelocity:  " + this.xVelocity);
		System.out.println("YVelocity:  " + this.yVelocity);
		System.out.println("Paddle Position:  " + this.paddlePosition);
	}


}
