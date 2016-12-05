
import java.util.*;


public class Driver 
{
	/*
	 * Keep this as true to enable viewing of GUI for one/multiple games.
	 * 
	 * @ENABLE_VISUAL	- let you see visual representation of games in the test trials
	 * @VISUAL_TRIALS	- number of animated visual test trials of game being played
	 * @SECOND_BETWEEN_GAMES		- number of seconds between the games
	 * 
	 * */
	private static boolean ENABLE_VISUAL = true;
	private static int VISUAL_TRIALS = 20;
	private static int SECONDS_BETWEEN_GAMES = 2;

	
	/**
	 * Number of discrete rows/columns/paddle position representations you set for your game sessions.
	 */
	private static int MAX_DISCRETE = 25;
	
	
	/**
	 * Agent parameters.
	 * @GAMMA - discount rate to determine how much agent wants to seek more immediate rewards
	 * @LEARN_RATE - constant to slow decay of alpha parameter.  a slow decay allows the agent to
	 * 				better incorporate reinforcement in its state/action utility assessment
	 * @MAX_ATTEMPT_EXPLORE - threshold for # of times agent would favor taking an action randomly over
	 * 						  picking max actual utility from given state.  If all any of actions are below
	 * 						  this threshold, then action from state is chosen at random amongst actions that 
	 * 						  have fewer attempts than this threshold.  Otherwise, action is picked based on
	 * 						  maximum utility.
	 * @BIG_REWARD - reward for bouncing on paddle
	 * @BIG_PUNISHMENT - negative reinforcement for losing the game
	 */
	private static double GAMMA = 0.2;
	private static int LEARN_RATE = 100000;
	private static int MAX_ATTEMPT_EXPLORE = 20;
	
	private static int BIG_REWARD = 1;
	private static int BIG_PUNISHMENT = -1;
	
	/**
	 * Constants for # of training and testing games.  Training mode is disabled
	 * in TESTING_GAMES, meaning these games will only use exploitation.
	 */
	private static int TRAINING_GAMES = 200000;
	private static int TESTING_GAMES = 1000;
	
	
	
	/**
	 * Constants for maximum frequencies, cumulative testing games, and below-threshold
	 * state-action pairing
	 */
	private static int maxFreqTraining = 0;
	private static int maxFreqTesting = 0;
	private static int cumulative = 0;
	private static int belowThreshold = 0;
	
	//randomizer for selecting states that need further exploration
	private static Random myRandom = new Random();
	
	//constant for total number of representative states, including the terminal state of "game over"
	private static int STATE_REP_SIZE;
	
	//hash-map to convert integer in Q-state table into a valid current state
	public static HashMap<Integer, DiscreteState> intToState = new HashMap<Integer, DiscreteState>();
	
	
	//hash-map for number of training games and respective consecutive bounces in each game
	public static HashMap<Integer, Integer> bounceFrequencyTrain = new HashMap<Integer, Integer>();
	
	//hash map for number of testing games and respective consecutive bounces in each game
	public static HashMap<Integer, Integer> bounceFrequencyTest = new HashMap<Integer, Integer>();
	
	
	//Q-table referring to states and corresponding actions
	public static UtilScore [][] scoreBoard = null;
	
	//reference to game session object
	public static GameSession myGame = null;
	

	
	/**
	 * Uses number of attempts of an action from a specific state to calculate the
	 * alpha in TD algorithm
	 * 
	 * @param currentUtil current state/action pairing
	 * @return alpha in TD algorithm
	 */
	public static double calculateLearnRate(UtilScore targetUtil)
	{
		double numerator = (double)(LEARN_RATE);
		double denominator = (double)(LEARN_RATE-1+targetUtil.attempts);
		
		return numerator/denominator;
	}
	
	/**
	 * Calculates the new utility and updates a state/action pair
	 * 
	 * @param reward		reward of action previously taken, given by main game loop
	 * @param current		integer representing the current state 
	 * @param pastUtil		use this to obtain utility of previous state/action pairing
	 * @return				new value in which to update the previous state/action pairing
	 */
	
	public static double calculateNewUtility(double reward, int current, UtilScore pastUtil, boolean previouslyHit)
	{
	
		//calculate best current state/action utility, original previous state/action utility, and alpha(learnRate)
		
		double learnRate = calculateLearnRate(pastUtil);
		double pastVal = pastUtil.utility;
		double currentVal = findGreatestUtil(current);
		
		//apply formula
		double newVal = pastVal + learnRate*(reward+GAMMA*currentVal-pastVal);
		
		return newVal;
	}
	
	
	/**
	 * Look at a given state index of the table. 
	 * Choose greatest utility in that row of the table.
	 * 
	 * @param stateRow		row representing the state in question
	 * @return				greatest utility in that state
	 */
	public static double findGreatestUtil(int stateRow)
	{
		double max = Double.MIN_VALUE;
		for(int i = 0; i < 3; i++)
		{
			if(scoreBoard[stateRow][i].utility > max)
				max = scoreBoard[stateRow][i].utility;
		}
		
		return max;
	}
	
	
	/**
	 * Find the best action from current state.  Actions are based on index 0, 1, 2.
	 * 
	 * 0 - paddle moves up 0.04 units.
	 * 1 - paddle moves down 0.04 units.
	 * 2 - paddle stays where it is.
	 * 
	 * If any of the three actions do not meet the exploration threshold, pick amongst the actions that are below
	 * the threshold randomly.
	 * Otherwise, try the action with maximum utility thus far.
	 * 
	 * If it is not in training mode, then it will just use exploitation.
	 * 
	 * @param stateRow			row of table best representing current game state
	 * @param training mode		if true, this function will use the exploration/exploitation tradeoff.
	 * 							otherwise, it will just pick max utility
	 * @return					action from state index row that would be best to take
	 */
	public static int findBestAction(int stateRow, boolean trainingMode)
	{
		double max = Double.MIN_VALUE;
		int idx = 0;
		
		if (trainingMode)
		{
			if(numberBelowExploreThreshold(stateRow)> 0)
			{
				return randomlyChooseAction(stateRow);
			}
			else
			{
				for(int i = 0; i < 3; i++)
				{	
					
					if(scoreBoard[stateRow][i].utility > max)
					{
						max = scoreBoard[stateRow][i].utility;
						idx = i;
					}
				}
			}

				
		}
		else
		{
			for(int i = 0; i < 3; i++)
			{	
				
				if(scoreBoard[stateRow][i].utility > max)
				{
					max = scoreBoard[stateRow][i].utility;
					idx = i;
				}
			}
		}



		return idx;
	}
	
	
	
	/**
	 * Debugging function while testing manually, please ignore.
	 */
	public static void printOptions()
	{
		System.out.println("0) Move paddle up");
		System.out.println("1) Move paddle down");
		System.out.println("2) Stay where you are");
	}
	
	
	/**
	 * Main function.
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String [] args) throws InterruptedException
	{
			
		
		//start time
		double startTime = System.currentTimeMillis();
		
		//populate core Q-table for state/action pairs
		populateHashmaps();
		
		//get # of states
		STATE_REP_SIZE = intToState.size()+1;
		
		//display your parameters
		displayParameters();
	
		//display your game max speed constraints
		GameSession.printMaxSpeeds();
		
		//initialize Q-table
		scoreBoard = new UtilScore[STATE_REP_SIZE][3];
		
		for(int a = 0; a < STATE_REP_SIZE; a++)
		{
			for(int b = 0; b < 3; b++)
				scoreBoard[a][b] = new UtilScore();
		}
		
		
		//train
		trainGames();
		
		
		for (int i = 0; i < STATE_REP_SIZE-1; i++)
		{
			belowThreshold += numberBelowExploreThreshold(i);
		}
		

		//test
		testGames(false, TESTING_GAMES);
		displayResults(TESTING_GAMES);
		
		double endTime = System.currentTimeMillis();
		double minutes = (endTime-startTime)/60000;
		
		System.out.println("Program duration:  " + minutes);
		
		
		System.out.println("\n\n");
		testGames(ENABLE_VISUAL, VISUAL_TRIALS);
		
		System.out.println("\n\n");
		displayResults(VISUAL_TRIALS);
		
		
	}
	
	
	
	/**
	 * Play a game and record the score.
	 *
	 * @param trainingMode		determines if you use the exploration/exploitation tradeoff
	 * @param displayVisual		show the GUI
	 * @throws InterruptedException 
	 */
	
	
	public static void playGame(boolean trainingMode, boolean displayVisual) throws InterruptedException
	{
		//create new game with discrete state representation to calculate properly
		myGame = new GameSession(MAX_DISCRETE, displayVisual);
		
		//set variables for previous state, action, and reward
		DiscreteState previousState = null;
		int previousOption = -1;
		int previousReward = 0;
		
		//set variables for current state, action, and reward
		DiscreteState currentState = null;
		int currentOption = -1;
		int currentReward;
		
		//set booleans for if ball hits the paddle or goes out-of-bounds
		boolean hitPaddle = false;
		boolean gameMissed = false;
		
		
		//continue while game is not over
		while (true)
		{
			/**
			 * assign value to current reward
			 * get +1 if after previous state/action results in ball hitting paddle
			 * get -1 if after previous state/action results in ball going out-of-bounds
			 * get 0 otherwise
			 */
			
			if(hitPaddle)
				currentReward = BIG_REWARD;
			else if(gameMissed)
				currentReward = BIG_PUNISHMENT;
			else
				currentReward = 0;
			
			hitPaddle = false;
			
			//get discrete state of current game state
			currentState = myGame.closestDiscrete();
			
			//get row in table representing current discrete state
			int currentDiscreteRow;
			
			
			/*
			 * if ball goes out-of-bounds and game is over, the assign negative reward to all
			 * state/action pairs in terminal state.  Also, assign state index to terminal state
			 * index (last index).
			 * 
			 * Otherwise, use function to get index of current discrete state.
			 */
			if(myGame.getGameOver())
			{				
				currentDiscreteRow = STATE_REP_SIZE-1;
				for(int i = 0; i < 3; i++)
				{
					scoreBoard[currentDiscreteRow][i].utility = -1;
				}
			}
			else
				currentDiscreteRow = getIntegerRepState(currentState);
			

			
			if (previousState != null)
			{
				int previousStateIdx = getIntegerRepState(previousState);
				UtilScore previousCell = scoreBoard[previousStateIdx][previousOption];
				previousCell.attempts += 1;
				
				double newerValue = calculateNewUtility(previousReward, currentDiscreteRow, previousCell, hitPaddle);
				previousCell.utility = newerValue;
				if(myGame.getGameOver())
					break;
			}
			
			
			//decide based on index of column what best action is (more explained in function documentation)
			currentOption = findBestAction(currentDiscreteRow, trainingMode);
			
			
			//select the action, if currentOption is 2, paddle stays where it is
			if (currentOption == 0)
			{
				myGame.movePaddleUp();
			}
			else if (currentOption == 1)
			{
				myGame.movePaddleDown();
			}
			
			
			//move the ball and update trajectory accordingly
			myGame.moveBall();

			//determine if ball hits or goes out-of-bounds
			hitPaddle = myGame.isHit();
			gameMissed = myGame.checkMiss();
			
			
			if(displayVisual)
			{
				myGame.repaintWindow();
				//Thread.sleep(sleepTime);
			}

			
			//assign current state, action, and reward to their corresponding "previous" counterparts
			previousState = currentState;
			previousReward = currentReward;
			previousOption = currentOption;
		}
	
	
	}

	
	
	/**
	 * Populate the hashmap of basic integer to state representation of board.
	 */
	public static void populateHashmaps()
	{
		int counter = 0;
		for (int a = 0; a < MAX_DISCRETE; a++)
		{
			for(int b = 0; b < MAX_DISCRETE; b++)
			{
				for (int c = -1; c < 2; c += 2)
				{
					for(int d = -1; d < 2; d++)
					{
						for(int e = 0; e< MAX_DISCRETE; e++)
						{
							DiscreteState myState= new DiscreteState(a, b, c, d, e);
							intToState.put(counter, myState);
							counter++;
							
						}
					}
				}
			}
		}
	}
	
	
	
	/**
	 * Based on current discrete representation of game state, perform a reverse hash
	 * so that based on discrete ball position/trajectory and paddle position you can find
	 * the proper index in the Q-table to update.
	 * 
	 * @param current		current discrete state representation I got from the game state
	 * @return				index in Q-table representing state
	 */
	public static int getIntegerRepState(DiscreteState current)
	{
		int boardCoordinateFactor = (current.row*MAX_DISCRETE+current.column)*6*MAX_DISCRETE;
		
		int xVelFactor;
		if (current.xVelocity == -1)
			xVelFactor = 0;
		else
			xVelFactor = 1;
		
		int yVelFactor = current.yVelocity + 1;
		
		int trajectoryFactor = (xVelFactor*3+yVelFactor)*MAX_DISCRETE;
		
		return boardCoordinateFactor + trajectoryFactor + current.paddlePosition;
	}
	
	
	/**
	 * Display the parameters you set for your games and training/testing sessions.
	 */
	public static void displayParameters()
	{
		System.out.println("Max number discrete row/column/paddle positions:  " + MAX_DISCRETE);
		System.out.println("Number of total states:  "  + STATE_REP_SIZE);
		
		System.out.println("Gamma:  " + GAMMA);
		System.out.println("Learn Rate:  " + LEARN_RATE);
		System.out.println("Max attempt explore:  " + MAX_ATTEMPT_EXPLORE);
		
		System.out.println("Training sessions:  " + TRAINING_GAMES);
		System.out.println("Testing sessions:  " + TESTING_GAMES);
	}
	
	
	/**
	 * Perform training sessions of the agent and populate training bounce frequency hash table.
	 * @throws InterruptedException 
	 */
	public static void trainGames() throws InterruptedException
	{
		//train
		
		for(int m = 0; m < TRAINING_GAMES; m++)
		{
			playGame(true, false);
			int bounces = myGame.bouncesThisGame();
			
			if (bounces > maxFreqTraining)
				maxFreqTraining = bounces;
			if(bounceFrequencyTrain.get(bounces) == null)
				bounceFrequencyTrain.put(bounces, 1);
			else
			{
				int toIncrement = bounceFrequencyTrain.get(bounces);
				toIncrement++;
				bounceFrequencyTrain.put(bounces, toIncrement);
			}

		}



	}

	
	/**
	 * Perform testing sessions of the agent and populate testing bounce frequency hash table.
	 * 
	 * @param visualize		- show the animation
	 * @param 
	 * 
	 * @throws InterruptedException 
	 */
	public static void testGames(boolean visualize, int count) throws InterruptedException
	{
		
		//test
		for(int m = 0; m < count; m++)
		{
			playGame(false, visualize);
			int bounces = myGame.bouncesThisGame();
			cumulative += bounces;
			
			if(visualize)
			{
				Thread.sleep(SECONDS_BETWEEN_GAMES*1000);
				myGame.closeGUI();
				int gameNo = m+1;
				System.out.println("Bounces in game  "  + gameNo + ":  "+ myGame.bouncesThisGame());
			}
			
			if (bounces > maxFreqTesting)
				maxFreqTesting = bounces;
			if(bounceFrequencyTest.get(bounces) == null)
				bounceFrequencyTest.put(bounces, 1);
			else
			{
				int toIncrement = bounceFrequencyTest.get(bounces);
				toIncrement++;
				bounceFrequencyTest.put(bounces, toIncrement);
			}
				
		}
		
		
	}
	
	
	/**
	 * Calculates the number of actions from a given state with attempts lower than threshold.
	 * 
	 * @param stateRow		index of a particular state
	 * @return				number of actions that are attempted fewer times than threshold from a given state
	 */
	public static int numberBelowExploreThreshold(int stateRow)
	{
		int total = 0;
		for(int i = 0; i < 3; i++)
		{
			if(scoreBoard[stateRow][i].attempts < MAX_ATTEMPT_EXPLORE)
				total++;
		}
		
		return total;
	}
	
	
	/**
	 * Choose an action from a particular state randomly in exploration mode.
	 * Pre-select actions that have been tried fewer times than threshold from a particular state.
	 * Then randomly choose on of those actions.
	 * 
	 * @param stateRow		index of a particular state
	 * @return				index of action chosen(0 move up, 1 move down, 2 stay)
	 */
	public static int randomlyChooseAction(int stateRow)
	{
		ArrayList<Integer> chosenList = new ArrayList<Integer>();
		for(int i = 0; i < 3; i++)
		{
			if(scoreBoard[stateRow][i].attempts < MAX_ATTEMPT_EXPLORE)
				chosenList.add(i);
		}
		
		int listSize = chosenList.size();
		int bigSize = myRandom.nextInt(listSize);
	
		
		int idx = chosenList.get(bigSize%listSize);
		return idx;
		
	}
	
	/**
	 * 
	 * @param gameTestNumber		number of games you tested on
	 */
	public static void displayResults(int gameTestNumber)
	{
		//data for frequency distribution of bounces/game in training set
		System.out.println("\n\nTraining Set");
		for(int i = 0; i <= maxFreqTraining; i++)
		{
			if(bounceFrequencyTrain.get(i) != null)
				System.out.println("Bounces:  " + i + "  Frequency:  " + bounceFrequencyTrain.get(i));
		}
		
		System.out.println("\n\n\n");
		
		//get number of state/action pairs not explored to the threshold
		
		System.out.println("Number of states/actions below threshold:  " + belowThreshold);
		
		//data for bounce frequency/game in testing set
		System.out.println("\n\nTesting Set");
		for(int i = 0; i <= maxFreqTesting; i++)
		{
			if(bounceFrequencyTest.get(i) != null)
				System.out.println("Bounces:  " + i + "  Frequency:  " + bounceFrequencyTest.get(i));
		}
		
		System.out.println("\n\n\nAverage Bounces: "  + cumulative/gameTestNumber);
		cumulative = 0;
		bounceFrequencyTest.clear();
	}

}
