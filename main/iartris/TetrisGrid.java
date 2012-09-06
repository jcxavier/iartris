package main.iartris;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JOptionPane;

import score.iartris.*;

public class TetrisGrid implements Serializable
{
	private static final long serialVersionUID = 1L;

	static final String DAT_FILE = "iartris.DAT";
    
    LinkedList<int[]> gLines;
    private int lines;
    private int score;
    private int[] dropLines;
    private int level;
    HiScore[] hiScore;
    
    TetrisGrid() {
        gLines = new LinkedList<int[]>();
        for (int i = 0; i < 20; i++) {
            gLines.add(new int[10]);
        }
        lines = score = 0;
        dropLines = new int[4];
        
        try{
            hiScore = HiScore.load(DAT_FILE);
        } catch (Exception e) {
            hiScore = new HiScore[Constants.HIGHSCORE_N];
            for (int i = 0; i < hiScore.length; i++) {
                hiScore[i] = new HiScore();
                hiScore[i].name = "<empty>";
            }
            File f = new File(DAT_FILE);
            try {
                HiScore.write(hiScore, f);
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(null, "Could not load HiScore!", "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        } 
    }
    
    public TetrisGrid(TetrisGrid tg)
    {
    	gLines = new LinkedList<int[]>();
    	
    	for (int i = 0; i != 20; i++)
    	{
    		int arrayTemp[] = new int[10];
        		
    		for (int j = 0; j != 10; j++)
    			arrayTemp[j] = tg.gLines.get(i)[j];
    			
    		gLines.add(arrayTemp);
    	}
    	
    	//this.gLines = tg.gLines;
    	
        this.lines = tg.lines;
        this.score = tg.score;
        this.dropLines = tg.dropLines;
        this.level = tg.level;
        this.hiScore = tg.hiScore;
	}

	boolean addFigure(Figure f) {
        for (int j = 0; j < f.arrX.length; j++) {
            if(f.arrY[j]+f.offsetY >= 20) {
                f.setOffset(f.offsetXLast,f.offsetYLast);
                addFiguretoGrid(f);
                eliminateLines();
                
                return true;
            }
            if(gLines.get(f.arrY[j]+f.offsetY)[f.arrX[j]+f.offsetX] != 0) {
                f.setOffset(f.offsetXLast,f.offsetYLast);
                addFiguretoGrid(f);
                eliminateLines();
                
                
                // TODO output
                // System.out.println("Res: " + Integer.valueOf(getHeight()*50 + countHoles()*50));
                return true;
            }
        }
        return false;
    }
    
    boolean isNextMoveValid(Figure f, int xOffset, int yOffset) {
        boolean b = true;
        try {
            for (int j = 0; j < f.arrX.length; j++) {
                if(gLines.get(f.arrY[j]+yOffset)[f.arrX[j]+xOffset] != 0) {
                    b = false;
                } 
            }
            return b;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void addFiguretoGrid(Figure f) {
        for (int j = 0; j < f.arrX.length; j++) {
            gLines.get(f.arrY[j]+f.offsetY)[f.arrX[j]+f.offsetX] = f.getGridVal();
        }
    }
    
    private void eliminateLines() {
        int lines = 0;
        for (Iterator<int[]> iter = gLines.iterator(); iter.hasNext();) {
            int[] el = (int[]) iter.next();
            boolean isFull = true;
            for (int j = 0; j < 10; j++) {
                if(el[j]==0) isFull = false;
            }
            if(isFull) {
                iter.remove();
                lines++;
            }
        }

        switch (lines) {
        case 1: score +=  100 +  5*level; break;
        case 2: score +=  400 + 20*level; break;
        case 3: score +=  900 + 45*level; break;
        case 4: score += 1600 + 80*level; break;
        }
        
        this.lines += lines;
        
        level = this.lines / 10;
        //level = 20;
        if(level > 20) level = 20;
        
        if (lines > 0) {
            dropLines[lines-1]++;
        }

        for (int i = 0; i < lines; i++) {
            gLines.add(0,new int[10]);
        }
    }
    
    boolean isGameOver(Figure f) {
        
        return !isNextMoveValid(f, 4, 0);
    }
    
    int getLevel() { return level;}
    
    int getLines() { return lines;}
    
    int getScore() { return score;}
    
    int[] getDropLines() { return dropLines; }
    
    void resetStats() {
        lines = score = level = 0;
        for (int i = 0; i < dropLines.length; i++) {
            dropLines[i] = 0;
        }
    }
    
    int updateHiScore() {
        for (int i = 0; i < hiScore.length; i++) {
            HiScore s = hiScore[i];
            if((s.score < score) || 
              ((s.score == score) && (s.lines >= lines))) {
                //Stack the HiScore
                switch (i) {
                case 0:
                    s = hiScore[1];
                    hiScore[1] = hiScore[0];
                    hiScore[2] = s;
                    s = new HiScore();
                    hiScore[0] = s;
                    break;
                case 1:
                    hiScore[2] = s;
                    s = new HiScore();
                    hiScore[1] = s;
                    break;
                };
                s.score = score;
                s.lines = lines;
                return i;
            } 
        }
        return -1;
    }
    
    void saveHiScore(String Name, int pos) {
        File f = new File(DAT_FILE);
        try {
            hiScore[pos].name = Name;
            HiScore.write(hiScore, f);
        } catch (Exception e1) {
            JOptionPane.showMessageDialog(null, "Could not save HiScore!", "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    
    public String toString() {
        
        StringBuffer sb = new StringBuffer();
        for (int[] arr : gLines) {
            for (int j = 0; j < arr.length; j++) {
                sb.append(arr[j]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
    
    public int getHeight()
	{
		int height = 19;
		int newHeight = 0;

		if(gLines.isEmpty())
			return 0;
		
		for(int i = 0; i < 10 ; i++){

			for(int j = 0; j < gLines.size(); j++) {

				if(gLines.get(j)[i] != 0) {
					newHeight = j-1;
					if(newHeight < height)
						height = newHeight;
					break;
				}				
			}

		}

		return 19-height;
	}
    
    public int countHoles()
	{
		//int height = getHeight();
		int holes = 0;
		boolean pieceFlag = false;
		
		for(int i = 0; i < 10 ; i++)
		{
			for(int j = 0; j < gLines.size(); j++)
			{

				if(gLines.get(j)[i] != 0)
					pieceFlag = true;
					
				else if(pieceFlag)
					holes++;										
			}
			pieceFlag = false;
		}
		
		return holes;
	}
    
    public int numPiecesOverload()
    {
		boolean holeFlag = false;
		int pieces = 0;
		
		for(int i = 0; i < 10 ; i++)
		{
			for(int j = gLines.size()-1; j > 1 ; j--)
			{

				if(gLines.get(j)[i] == 0 && gLines.get(j-1)[i] != 0)
					holeFlag = true;
					
				else if(holeFlag && gLines.get(j)[i] != 0)
					pieces++;										
			}
			holeFlag = false;
		}   	
    	
    	return pieces;
    }
    
    public float[] getHeights()
    {
    	float[] heights = new float[10];
    	

		for(int i = 0; i < 10 ; i++){
			
			for(int j = 0; j < gLines.size(); j++) {

				if(gLines.get(j)[i] != 0) {
					heights[i] = 20-j;
					break;
				}
			}

		}
		
		return heights;
    }
  
    public float weightedHeight()
    {
    	float heights[] = getHeights();
    	float res = 0;
    	
    	for (int i = 0; i != 10; i++)
    		res += heights[i] * i*2;
    	
    	return res;
    }
    
    public float deltaHeight()
    {
    	float heights[] = getHeights();
    	
    	float min = heights[0], max = heights[0];
    	
    	for(int i = 1; i < 10; i++)
    	{
    		if(heights[i] < min)
    			min = heights[i];
    		if(heights[i] > max)
    			max = heights[i];
    		}

    	return max-min;
    }
    
    public float averageHeight()
    {
    	float heights[] = getHeights();
    	
    	float sum = heights[0];
    	
    	for(int i = 1; i < 10; i++)
    		sum += heights[i];

    	return sum / 10;
    }
    
    public float variance()
    {
    	float avg = averageHeight();
    	float heights[] = getHeights();
    	
    	float sum = 0;
    	
    	for (int i = 0; i != 10; i++)
    		sum += Math.abs(heights[i] - avg);
    	
    	return sum;
    }

	public void addChallenge(int lines)
	{
		Random r = new Random();
		
		for (int i = 19; i != 19-lines; i--)
		{
			int line[] = new int[10];
		
			for (int j = 0; j != 10; j++)
				line[j] = r.nextInt(7) + 1;
			
			line[r.nextInt(10)] = 0;
			
			gLines.set(i, line);
		}			
	}
}
