package main.iartris;

public class Movement implements Comparable<Movement>
{
	private int leftM;
	private int rightM;
	private int rotations;
	private float score;
	
	private Movement child;
	
	public Movement(int leftM, int rightM, int rotations, Movement movement)
	{
		this.leftM = leftM;
		this.rightM = rightM;
		this.rotations = rotations;
		this.child = movement;
	}

	public float getScore()
	{
		return score;
	}
	
	public void setScore(String cpuName, int holes, int height, float average,
			float delta, float variance, int overload, float weight)
	{	
		if (cpuName == "Greedy")
		{
			if (height > Constants.DESPAIR_LINE)
				score =
					height  * Constants.GREEDY_MAX_HEIGHT_DESPAIR_VALUE +
					holes	* Constants.GREEDY_HOLES_DESPAIR_VALUE;
			else
				score =
					height  * Constants.GREEDY_MAX_HEIGHT_VALUE +
					holes	* Constants.GREEDY_HOLES_VALUE;			
		}
		else
		{
			if (height > Constants.DESPAIR_LINE)
				score =
					height   * Constants.MAX_HEIGHT_DESPAIR_VALUE +
					holes    * Constants.HOLES_DESPAIR_VALUE +
					average  * Constants.AVERAGE_HEIGHT_DESPAIR_VALUE +
					delta    * Constants.DELTA_HEIGHT_DESPAIR_VALUE +
					variance * Constants.VARIANCE_HEIGHT_DESPAIR_VALUE + 
					overload * Constants.PIECE_OVERLOAD_DESPAIR_VALUE + 
					weight   * Constants.WEIGHTED_HEIGHT_DESPAIR_VALUE;
		    else
		    	score =
					height   * Constants.MAX_HEIGHT_VALUE +
					holes    * Constants.HOLES_VALUE +
					average  * Constants.AVERAGE_HEIGHT_VALUE +
					delta    * Constants.DELTA_HEIGHT_VALUE +
					variance * Constants.VARIANCE_HEIGHT_VALUE + 
					overload * Constants.PIECE_OVERLOAD_VALUE + 
					weight   * Constants.WEIGHTED_HEIGHT_VALUE;
		}
		
		if (child != null)
			score += child.score;
	}
	
	public int getInstruction()
	{
		if (rotations != 0)
		{
			rotations--;
			return Constants.ROTATE;
		}
		
		if (leftM != 0)
		{
			leftM--;
			return Constants.LEFT;
		}
		
		if (rightM != 0)
		{
			rightM--;
			return Constants.RIGHT;
		}	
		
		return Constants.DROP;
	}
	
	public String toString()
	{
		String retval = "\n";
		retval += "Left: " + leftM + "\n";
		retval += "Right: " + rightM + "\n";
		retval += "Rotation: " + rotations + "\n";
		retval += "Score: " + score + "\n";
		
		return retval;
	}

	public int compareTo(Movement m)
	{
		if (this.score < m.score)
			return -1;
		else if (this.score == m.score)
			return 0;
		else
			return 1;
	}	
}
