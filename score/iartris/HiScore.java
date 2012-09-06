package score.iartris;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import main.iartris.Constants;

public class HiScore implements Serializable
{
	private static final long serialVersionUID = 1L;
	public int score;
    public int lines;
    public int h, m, s;
    public String name;
    
    public static void write(HiScore[] score, File file) 
    throws IOException {
        
        FileOutputStream fs = new FileOutputStream(file);
        ObjectOutputStream os = new ObjectOutputStream(fs);
        for (int i = 0; i < score.length; i++) {
            os.writeObject(score[i]);
        }
        
        os.close();
        os = null; fs = null;
    }
    
    public static HiScore[] load(String file) 
    throws ClassNotFoundException, IOException{
        HiScore[] r = new HiScore[Constants.HIGHSCORE_N];	// nº algoritmos em teste
        FileInputStream fs = new FileInputStream(file);
        ObjectInputStream is = new ObjectInputStream(fs);
        for (int i = 0; i < r.length; i++) {
            HiScore res = (HiScore)is.readObject();
            r[i] = res;
        }
        
        is.close();
        is = null; fs = null;
        return r;
    }
}
