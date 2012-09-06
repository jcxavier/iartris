package main.iartris;

public final class Constants
{
	// despair line
	public static final int DESPAIR_LINE = 10;
	
	// skilled (height <= DESPAIR_LINE)
	public static final int HOLES_VALUE = 50;
	public static final int MAX_HEIGHT_VALUE = 25;
	public static final int AVERAGE_HEIGHT_VALUE = 3;
	public static final int DELTA_HEIGHT_VALUE = 5;
	public static final int VARIANCE_HEIGHT_VALUE = 5;
	public static final int PIECE_OVERLOAD_VALUE = 1;
	public static final int WEIGHTED_HEIGHT_VALUE = 1;
	
	// greedy (height <= DESPAIR_LINE)
	public static final int GREEDY_HOLES_VALUE = 70;
	public static final int GREEDY_MAX_HEIGHT_VALUE = 30;
	
	// skilled (height > DESPAIR_LINE)
	public static final int HOLES_DESPAIR_VALUE = 35;
	public static final int MAX_HEIGHT_DESPAIR_VALUE = 70;
	public static final int AVERAGE_HEIGHT_DESPAIR_VALUE = 2;
	public static final int DELTA_HEIGHT_DESPAIR_VALUE = 10;
	public static final int VARIANCE_HEIGHT_DESPAIR_VALUE = 5;
	public static final int PIECE_OVERLOAD_DESPAIR_VALUE = 1;
	public static final int WEIGHTED_HEIGHT_DESPAIR_VALUE = 1;
	
	// greedy (height > DESPAIR_LINE)
	public static final int GREEDY_HOLES_DESPAIR_VALUE = 20;
	public static final int GREEDY_MAX_HEIGHT_DESPAIR_VALUE = 80;
	
	// instructions
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int ROTATE = 2;
	public static final int DROP = 3;

	// pc playing time in miliseconds
	public static final int PC_PLAYING_TIME_MS = 100;

	// max number of highscores
	public static final int HIGHSCORE_N = 3;

	
}
