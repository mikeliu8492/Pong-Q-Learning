import java.util.*;



public class GameSession 
{
	/*
	maximum number of discrete row/column/paddle positions
	*/
	
	private int MAX_UNITS;
	
	//boolean for determining if game is over
	private boolean gameOver;
	
	//bounces in current game
	private int bounce = 0;
	
	/**
	 * Variables that are the game state
	 * 
	 */
	
	//velocity variables
	private double xVelocity;
	private double yVelocity;
	
	//ball position variables
	private double xPosition;
	private double yPosition;
	
	//paddle position and height variables
	private double paddlePosition;
	private double PADDLE_HEIGHT = 0.2;
	
	

	/**
	 * Initialize game session.
	 * 
	 * @param maxUnits		Units of representation of row/column and paddle positions.
	 */
	public GameSession(int maxUnits)
	{
		this.MAX_UNITS = maxUnits;
		
		this.xVelocity = 0.03;
		this.yVelocity = 0.01;
		
		this.xPosition = 0.5;
		this.yPosition = 0.5;
		
		this.paddlePosition = 0.5 - PADDLE_HEIGHT/2;
	}
	
	/**
	 * Determines if ball's x-position >= 1 AND that the ball's y-position
	 * IS within the upper and lower bounds of the paddle.
	 * 
	 * If this is true, return true and randomize the ball's new trajectory accordingly.
	 * Else, return false.
	 * 
	 * @return		bool for if the ball is sucessfully bounced by paddle
	 */
	
	public boolean isHit()
	{
		if(this.xPosition >= 1.0 && this.xVelocity > 0)
		{
			double paddleBottom = this.paddlePosition;
			double paddleTop = this.paddlePosition + 0.2;
			if(this.yPosition >= paddleBottom && this.yPosition <= paddleTop)
			{
				this.xPosition = 2-this.xPosition;
				
				this.xVelocity = newVelocityX(this.xVelocity);
				
				//cap the x and y velocities
				if(Math.abs(this.xVelocity) >= 1)
				{
					if(this.xVelocity > 0)
						this.xVelocity = 0.9;
					else
						this.xVelocity = -0.9;
				}
				
				this.yVelocity = newVelocityY(this.yVelocity);
				if(Math.abs(this.yVelocity) >= 1)
				{
					if(this.yVelocity > 0)
						this.yVelocity = 0.9;
					else
						this.yVelocity = -0.9;
				}
				
				//increment your bounces
				bounce++;
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Determines if ball's x-position >= 1 AND that the ball's y-position
	 * is NOT within the upper and lower bounds of the paddle.
	 * 
	 * If this is true, return true.
	 * Else, return false.
	 * 
	 * @return		bool for if the ball goes out of bounds
	 */
	public boolean checkMiss()
	{
		if(this.xPosition >= 1.0 && this.xVelocity > 0)
		{
			double paddleBottom = this.paddlePosition;
			double paddleTop = this.paddlePosition + 0.2;
			if(this.yPosition < paddleBottom || this.yPosition > paddleTop)
			{
				gameOver = true;
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Move the ball in the game during each time iteration.  Simulates bounces
	 * as perfect elastic collision by reversing course of directional velocities.
	 * Also keeps ball in bounds.
	 * 
	 */
	public void moveBall()
	{
		this.xPosition += this.xVelocity;
		this.yPosition += this.yVelocity;
		
		if (this.yPosition <= 0 || this.yPosition >= 1)
		{
			if (this.yPosition <= 0)
				this.yPosition = -this.yPosition;
			else if (this.yPosition >= 1)
				this.yPosition = 2-this.yPosition;
			this.yVelocity = -this.yVelocity;
		}
		
		if (this.xPosition <= 0)
		{
			this.xPosition = -this.xPosition;
			this.xVelocity = -this.xVelocity;
		}
		
		
	}
	
	
	/**
	 * Print current board state for debugging purposes.
	 */
	public void printCurrentState()
	{
		System.out.println("X Velocity:  " + this.xVelocity);
		System.out.println("Y Velocity:  " + this.yVelocity);
		System.out.println("X Position:  " + this.xPosition);
		System.out.println("Y Position:  " + this.yPosition);
		System.out.println("Paddle Position:  " + this.paddlePosition);
		System.out.println("\n\n");
	}
	
	
	/**
	 * Uses the DISCRETE_MAX_UNIT to calculate a discrete state that most closely
	 * represents the current game state.
	 * 
	 * @return		discrete state for you to hash and find proper 
	 */
	public DiscreteState closestDiscrete()
	{
		int paddlePosition = (int) Math.floor(MAX_UNITS*this.paddlePosition/(1-this.PADDLE_HEIGHT));
				
	
		if(paddlePosition < 0)
			paddlePosition = 0;
		if(paddlePosition > MAX_UNITS-1)
			paddlePosition = MAX_UNITS-1;
		
		int xPosRep = (int) Math.floor(MAX_UNITS*this.xPosition);
		
		if (xPosRep < 0)
			xPosRep = 0;
		else if (xPosRep > MAX_UNITS-1)
			xPosRep = MAX_UNITS-1;
		
		int yPosRep = (int) Math.floor(MAX_UNITS*this.yPosition);
		
		if (yPosRep < 0)
			yPosRep = 0;
		else if(yPosRep > MAX_UNITS-1)
			yPosRep = MAX_UNITS-1;
		
		int xVelRep;
		int yVelRep;
		
		if (this.xVelocity > 0)
			xVelRep = 1;
		else
			xVelRep = -1;
		
		if (Math.abs(this.yVelocity) < 0.015)
			yVelRep = 0;
		else if(this.yVelocity <= -0.015)
			yVelRep = -1;
		else
			yVelRep = 1;
		
		DiscreteState currentRep = new DiscreteState(yPosRep, xPosRep, xVelRep, yVelRep, paddlePosition);
		
		return currentRep;
	}
	
	
	
	/**
	 * Moves paddle up. if paddle is too close to top, then paddle reset to 0 position.
	 */
	public void movePaddleUp()
	{
		if(this.paddlePosition <= 0.76)
			this.paddlePosition += 0.04;
		else
			this.paddlePosition = 0;
	}
	
	/**
	 * Moves paddle down.  If paddle too close to bottom, then paddle resets to top.
	 */
	public void movePaddleDown()
	{
		if(this.paddlePosition >= 0.04)
			this.paddlePosition -= 0.04;
		else
			this.paddlePosition = 0.8;
	}
	
	
	/**
	 * 
	 * @return boolean to determine if game has concluded
	 */
	public boolean getGameOver()
	{
		return gameOver;
	}
	
	
	/**
	 * 
	 * @return # consecutive bounces on paddle this game
	 */
	public int bouncesThisGame()
	{
		return this.bounce;
	}
	
	/**
	 * prints # consecutive bounces this game
	 */
	public void printBounces()
	{
		System.out.println("Bounces:  " + this.bounce);
	}
	
	
	/**
	 * When the ball hits the paddle, x-velocity is randomized using current time as seed.
	 * Makes sure that abs(x-velocity) > 0.03
	 * 
	 * @param previous		previous x-velocity
	 * @return				new x-velocity
	 */
	private double newVelocityX(double previous)
	{
		Random generator = new Random(System.currentTimeMillis());
		double copyOfPrevious = previous;
		do
		{
			double minX = -0.015;
			double maxX = 0.015;
			double difference = maxX-minX;
			
			copyOfPrevious = -previous + minX + difference*generator.nextDouble();
			
		}while (Math.abs(copyOfPrevious) <= 0.03);
		
		return copyOfPrevious;
	}
	
	
	/**
	 * 
	 * When the ball hits the paddle, y-velocity is randomize using current time as seed.
	 * 
	 * @param previous		previous y-velocity
	 * @return				new y-velocity
	 */
	private double newVelocityY(double previous)
	{
		Random generator = new Random(System.currentTimeMillis());
		double copyOfPrevious = previous;
		
		double minX = -0.03;
		double maxX = 0.03;
		double difference = maxX-minX;
			
		copyOfPrevious = -previous + minX + difference*generator.nextDouble();
		
		return copyOfPrevious;
	}
	
	
	
	
	/**
	 * Displays a 20x20 ascii representation of the game screen.
	 */
	public void displayAnimation()
	{
		char [][] screenDisplay = new char[22][22];
		
		
		int xRep = (int)(Math.floor(20*this.xPosition))+1;
		int yRep = (int)(Math.floor(20*this.yPosition))+1;
		

		
		int paddleBound = (int)(Math.floor(20*this.paddlePosition)+1);
		System.out.println("Paddlebound:  " + paddleBound);
		
		for(int row = 0; row < 22; row++)
		{
			for(int col = 0; col < 22; col++)
			{
				if(col == 0)
					screenDisplay[row][col] = '|';
				if(row == 0 || row == 21)
					screenDisplay[row][col] = '-';
				
				if(row > 0 && row < 21 && col > 0 && col < 21)
				{
					if(row == xRep && col == yRep && col != 21)	
						screenDisplay[row][col] = '.';
					else
						screenDisplay[row][col] = ' ';
				}

				
				if(col == 21)
				{
					if(row >= paddleBound &&  row <= paddleBound+3)
						screenDisplay[row][col] = '|';
					else
						screenDisplay[row][col] = ' ';
				}
				
				System.out.print(screenDisplay[row][col]);
			}
			
			System.out.println("");
		}
		
		System.out.println("\n\n");
	}
	
	
	/**
	 * 
	 * @return boolean for if ball is moving towards the paddle.  Ignore as it was used for debugging.
	 */
	
	public boolean isTowardPaddle()
	{
		return this.xVelocity > 0;
	}
}
