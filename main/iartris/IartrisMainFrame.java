package main.iartris;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

import res.ResClass;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.LinkedList;

public class IartrisMainFrame extends JFrame 
{
	private static final long serialVersionUID = 1L;
	private static final String NAME = "iarTris 1.0";
    private static final int CELL_H = 24;
    
    private Font font;
    private JPanel playPanel;
    private JLabel score;
    private JLabel lines;
    private JLabel time;
    private JLabel cpuNameLabel;
    private JLabel scrollerLabel;
    private JLabel levelLabel;
    private JLabel hiScoreLabel;
    
    private String cpuName;

    private Scrollbar scroller;
    
    private Movement movement;
    private JPanel[][] cells;
    private TetrisGrid tg;
    private JPanel[][] next;
    private int nextX;
    private int nextY;
    private int elapsedTime;
    private Figure f;
    private LinkedList<Figure> fNext;
   
    private int nSteps;
    private int lookup;
    private int challengeLines;
    
    private FigureFactory ff;
    private boolean isNewFigureDroped;
    private boolean isGameOver;
    private boolean isPause;
    private Color nextBg;
    private TimeThread tt;
    private KeyListener keyHandler;
   
    // menu
    private JMenuItem iartrisRestart;
    private JMenuItem iartrisPause;
    private JMenuItem iartrisHiScore;
    private JMenuItem iartrisExit;
   
    private JPanel hiScorePanel;
    private JPanel gameSettingsMenu;
    
    private class GridThread extends Thread
    {  
        private int count = 0;
        
        public void run() {
            try {
                while (true) {
                    if (isGameOver || isPause) {
                        Thread.sleep(50);
                    } else {
                    	if(isNewFigureDroped) {
							isNewFigureDroped = false;
						    count = 0;
						    nextMove();
						    continue;
                        }
                    	else                     
                            Thread.sleep(50);		// drop speed
                        
                        count += 50;
                        
                        if (count + 50*tg.getLevel() >= 1100)
                        {
                            count = 0;
                            nextY++;
                            nextMove();
                        }
                    } 
                }
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }
    }
    
    private class TimeThread extends Thread {
        
        private int hours;
        private int min;
        private int sec;
        
        private int count;
        
        private void incSec() {
            elapsedTime++;
        	sec++;
            if(sec == 60) {
                sec = 0;
                min++;
            }
            if(min == 60) {
                min = 0;
                hours++;
            } 
        }
        
        private void resetTime() {
            hours = min = sec = 0;
        }
        
        public void run() {
            try {
                while (true) {
                    Thread.sleep(50);
                    if (isGameOver) {
                        Graphics g = playPanel.getGraphics();
                        Font font = new Font(g.getFont().getFontName(), Font.BOLD, 24);
                        g.setFont(font);
                        g.drawString("GAME OVER", 47, 250);

                    } else if(isPause) {
                        time.setText("PAUSED");
                    } else if(count >= 1000) {
                        count = 0;
                        incSec();
                        time.setText(this.toString());
                    } else {
                        count+=50;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            if(hours < 10) {
                sb.append('0');
            }
            sb.append(hours);
            
            sb.append(':');
            
            if(min < 10) {
                sb.append('0');
            }
            sb.append(min);
            
            sb.append(':');
            
            if(sec < 10) {
                sb.append('0');
            }
            sb.append(sec);
            
            return sb.toString();
        }
    }
    
    public IartrisMainFrame() {
        super(NAME);
        
        nSteps = 3;
        fNext = new LinkedList<Figure>(); 
        
        setIconImage(loadImage("icon.jpg"));
        
        lookup = 1;
        challengeLines = 0;
        showAiSelector();
        
        keyHandler = new KeyAdapter(){

            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if(code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                    moveLeft();
                } else if(code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                    moveRight();
                } else if(code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                    moveDown();
                } else if(code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                    rotation();
                } else if(code == KeyEvent.VK_SPACE) {
                    moveDrop();
                }                
            }
        };
        addKeyListener(keyHandler);
               
        font = new Font("Dialog", Font.PLAIN, 12);
        tg = new TetrisGrid();
        
        tg.addChallenge(challengeLines);
        
        
        ff = new FigureFactory();
        nextBg = new Color(238,238,238);
        
        initMenu();

        JPanel all = new JPanel(new BorderLayout());
        all.add(getLeftPanel(), BorderLayout.WEST);		// next piece
        all.add(getPlayPanel(), BorderLayout.CENTER);
        all.add(getMenuPanel(), BorderLayout.EAST);		// score, level, time
        all.add(getCopyrightPanel(), BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().add(all, BorderLayout.CENTER);
        pack();
        this.setResizable(false);

        
        for (int i = 0; i != nSteps; i++)
        	fNext.add(ff.getRandomFigure());
        
        dropNext();
        
    
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	
            	if (movement == null)
            		return;
                
                int mov = movement.getInstruction();
                
                switch (mov)
                {
	                case Constants.LEFT:   moveLeft();  break;
	                case Constants.RIGHT:  moveRight(); break;
	                case Constants.ROTATE: rotation();  break;
	                case Constants.DROP:   moveDrop();  break;
                }
            }
        };
        new Timer(Constants.PC_PLAYING_TIME_MS, taskPerformer).start();
   
        
        
        
        
        GridThread gt = new GridThread();
        tt = new TimeThread();
        gt.start();
        tt.start();
        
        paintTG();

        addWindowFocusListener(new WindowFocusListener(){

            public void windowGainedFocus(WindowEvent arg0) {}

            public void windowLostFocus(WindowEvent arg0) {
                isPause = true;
            }
        });
        
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
        setVisible(true);
    }
    
    private void initMenu() {
        
        MenuHandler mH = new MenuHandler();
        
        JMenuBar menu = new JMenuBar(); 
        setJMenuBar(menu);
        
        JMenu miartris = new JMenu();
        menu.add(miartris);
        miartris.setText("Menu");
        miartris.setMnemonic('J');
        {
            iartrisRestart = new JMenuItem("Restart");
            miartris.add(iartrisRestart);
            setKeyAcceleratorMenu(iartrisRestart, 'R',0);
            iartrisRestart.addActionListener(mH);
            iartrisRestart.setMnemonic('R');
            
            iartrisPause = new JMenuItem("Pause");
            miartris.add(iartrisPause);
            setKeyAcceleratorMenu(iartrisPause, 'P',0);
            iartrisPause.addActionListener(mH);
            iartrisPause.setMnemonic('P');
            
            miartris.addSeparator();
            
            iartrisHiScore = new JMenuItem("HiScore");
            miartris.add(iartrisHiScore);
            setKeyAcceleratorMenu(iartrisHiScore, 'H',0);
            iartrisHiScore.addActionListener(mH);
            iartrisHiScore.setMnemonic('H');
            
            miartris.addSeparator();
            
            iartrisExit = new JMenuItem("Exit");
            miartris.add(iartrisExit);
            setKeyAcceleratorMenu(iartrisExit, KeyEvent.VK_ESCAPE, 0);
            iartrisExit.addActionListener(mH);
            iartrisExit.setMnemonic('X');
        }
    }
    
    private void setKeyAcceleratorMenu(JMenuItem mi, int keyCode, int mask) {
        KeyStroke ks = KeyStroke.getKeyStroke(keyCode, mask);
        mi.setAccelerator(ks);
    }

    private JPanel getPlayPanel() {
        playPanel = new JPanel();
        playPanel.setLayout(new GridLayout(20,10));
        playPanel.setPreferredSize(new Dimension(10*CELL_H, 20*CELL_H));

        cells = new JPanel[20][10];
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                cells[i][j] = new JPanel();
                cells[i][j].setBackground(Color.WHITE);
                cells[i][j].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                playPanel.add(cells[i][j]);
            }
        }
        return playPanel;
    }
    
    private JPanel getLeftPanel(){
    	JPanel r = new JPanel();
        BoxLayout rL = new BoxLayout(r,BoxLayout.Y_AXIS);
        r.setLayout(rL);
        r.setBorder(new EtchedBorder());
        Dimension ra = new Dimension(5, 0);
        next = new JPanel[4][4];
        JPanel nextP = new JPanel();
        nextP.setLayout(new GridLayout(4,4));
        Dimension d = new Dimension(4*18, 4*18);
        nextP.setMinimumSize(d);
        nextP.setPreferredSize(d);
        nextP.setMaximumSize(d);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                next[i][j] = new JPanel();
                nextP.add(next[i][j]);
            }
        }
        
        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        jp.add(new JLabel("NEXT:"));
        jp.add(Box.createHorizontalGlue());
        r.add(jp);
        r.add(nextP);
        
        r.add(Box.createRigidArea(new Dimension(100, 10)));
        
        return r;
    }
    
    
    
    private JPanel getMenuPanel() {
        JPanel r = new JPanel();
        BoxLayout rL = new BoxLayout(r,BoxLayout.Y_AXIS);
        r.setLayout(rL);
        r.setBorder(new EtchedBorder());
        Dimension ra = new Dimension(5, 0);
        
        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        jp.add(new JLabel("HI-SCORE:"));
        jp.add(Box.createHorizontalGlue());
        r.add(jp);
        
        hiScoreLabel = new JLabel(""+tg.hiScore[0].score);
        hiScoreLabel.setForeground(Color.RED);
        
        
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        jp.add(hiScoreLabel);
        jp.add(Box.createHorizontalGlue());
        r.add(jp);
        
        r.add(Box.createVerticalStrut(5));
        
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        jp.add(new JLabel("SCORE:"));
        jp.add(Box.createHorizontalGlue());
        r.add(jp);
        
        score = new JLabel("0");
        score.setForeground(Color.BLUE);
        
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        jp.add(score);
        jp.add(Box.createHorizontalGlue());
        r.add(jp);
        
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        jp.add(new JLabel("LINES:"));
        jp.add(Box.createHorizontalGlue());
        r.add(jp);
        
        lines = new JLabel("0");
        lines.setForeground(Color.BLUE);
        
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        jp.add(lines);
        jp.add(Box.createHorizontalGlue());
        r.add(jp);
        
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        jp.add(new JLabel("LEVEL:"));
        jp.add(Box.createHorizontalGlue());
        r.add(jp);
        
        levelLabel = new JLabel("1");
        levelLabel.setForeground(Color.BLUE);
        
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        jp.add(levelLabel);
        jp.add(Box.createHorizontalGlue());
        r.add(jp);
        
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        jp.add(new JLabel("TIME:"));
        jp.add(Box.createHorizontalGlue());
        r.add(jp);
        
        time = new JLabel("00:00:00");
        time.setForeground(Color.BLUE);
        
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        jp.add(time);
        jp.add(Box.createHorizontalGlue());
        r.add(jp);
        
        JLabel cpuLabel = new JLabel("\n0");
        cpuLabel.setForeground(Color.GREEN);
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        jp.add(new JLabel("ALGORITHM:"));
        jp.add(Box.createHorizontalGlue());
        r.add(jp);
        
        cpuNameLabel = new JLabel(cpuName);
        cpuNameLabel.setForeground(Color.BLUE);
        
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        jp.add(cpuNameLabel);
        jp.add(Box.createHorizontalGlue());
        r.add(jp);
        
        r.add(Box.createVerticalGlue());
        
        //BUTTONS
        r.add(Box.createRigidArea(new Dimension(0, 10)));
        
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        JButton restartBut = new JButton("Restart");
        restartBut.setToolTipText("Press 'R'");
        restartBut.setFocusable(false);
        restartBut.addKeyListener(keyHandler);
        restartBut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                restart();
            }
        });
        Dimension d = new Dimension(90, 30);
        restartBut.setMinimumSize(d);
        restartBut.setPreferredSize(d);
        restartBut.setMaximumSize(d);
        jp.add(restartBut);
        jp.add(Box.createHorizontalGlue());
        r.add(jp);
        
        r.add(Box.createRigidArea(new Dimension(0, 5)));
        
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
        jp.add(Box.createRigidArea(ra));
        JButton pauseBut = new JButton("Pause");
        pauseBut.setToolTipText("Press 'P'");
        pauseBut.setFocusable(false);
        pauseBut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                pause();
            }
        });
        pauseBut.setMinimumSize(d);
        pauseBut.setPreferredSize(d);
        pauseBut.setMaximumSize(d);
        jp.add(pauseBut);
        jp.add(Box.createHorizontalGlue());
        r.add(jp);

        return r;
    }
    
    
    private JPanel getCopyrightPanel() {
        JPanel r = new JPanel(new BorderLayout());
        BoxLayout rL = new BoxLayout(r,BoxLayout.LINE_AXIS);
        r.setLayout(rL);
        r.setBorder(new EtchedBorder());
        r.add(Box.createRigidArea(new Dimension(75,0)));
        
        JLabel jL = new JLabel("IART 2009 - Francisco Pinto, João Ribeiro, João Xavier");
        jL.setFont(font);
        
        r.add(jL);
        r.setAlignmentX(CENTER_ALIGNMENT);
        
        return r;
    }
    
    static Image loadImage(String imageName) {
        try {
            Image im = ImageIO.read(new BufferedInputStream(
                    new ResClass().getClass().getResourceAsStream(imageName)));
            return im;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return null;
        }
    }
    
    private synchronized void nextMove() {
        f.setOffset(nextX, nextY);
        
        if(tg.addFigure(f)) {
            dropNext();
            f.setOffset(nextX, nextY);
                       
        } else {
            clearOldPosition();
        }
        paintNewPosition();
        
        if(isGameOver) {
            int tmp = tg.updateHiScore();
            if(tmp >= 0) {
                
                tg.saveHiScore(cpuName, tmp);
                
                if(tmp == 0)
                    hiScoreLabel.setText(""+tg.hiScore[0].score);
            }
        } 
    }
    
    private void clearOldPosition() {
        for (int j = 0; j < 4; j++) {
            cells[f.arrY[j]+f.offsetYLast][f.arrX[j]+f.offsetXLast].setBackground(Color.WHITE);
            cells[f.arrY[j]+f.offsetYLast][f.arrX[j]+f.offsetXLast].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
    }
    
    private void paintNewPosition() {
        for (int j = 0; j < 4; j++) {
            cells[f.arrY[j]+f.offsetY][f.arrX[j]+f.offsetX].setBackground(f.getGolor());
            cells[f.arrY[j]+f.offsetY][f.arrX[j]+f.offsetX].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        } 
    }
    
    private void paintTG() {
        int i = 0;
        Color c;
        for (int[] arr : tg.gLines) {
            for (int j = 0; j < arr.length; j++) {
                if(arr[j]!= 0) {
                    switch (arr[j]) {
                    case Figure.I: c = Figure.COL_I; break;
                    case Figure.T: c = Figure.COL_T; break;
                    case Figure.O: c = Figure.COL_O; break;
                    case Figure.J: c = Figure.COL_J; break;
                    case Figure.L: c = Figure.COL_L; break;
                    case Figure.S: c = Figure.COL_S; break;
                    default: c = Figure.COL_Z; break;
                    }
                    cells[i][j].setBackground(c);
                    cells[i][j].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
                } else {
                    cells[i][j].setBackground(Color.WHITE);
                    cells[i][j].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                } 
            }
            i++;
        }
    }
    
    private void showNextFigures()
    {
    	//for(int i=0; i!= nSteps; i++)
    		showNext(fNext.get(1));
    }
    
    
    private void showNext(Figure f) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                next[i][j].setBackground(nextBg);
                next[i][j].setBorder(BorderFactory.createEmptyBorder());
            }
        }
        
        for (int j = 0; j < f.arrX.length; j++) {
            next[f.arrY[j]][f.arrX[j]].setBackground(f.getGolor());
            next[f.arrY[j]][f.arrX[j]].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        }
    }
    
    private void dropNext() {
        if(isGameOver) return;
        nextX = 4;
        nextY = 0;

        score.setText(""+tg.getScore());
        lines.setText(""+tg.getLines());
        levelLabel.setText(tg.getLevel()+" / 20");
        
        f = fNext.getFirst();
        
        fNext.addLast(ff.getRandomFigure());
        showNextFigures();
        
        
        movement = search(tg, 0);
        
        
        fNext.removeFirst();
      
        isGameOver = tg.isGameOver(f);
        isNewFigureDroped = true;
    }
    
    private void moveLeft() {
        if(isGameOver || isPause) return;
        if(nextX-1 >= 0) {
            if (tg.isNextMoveValid(f,f.offsetX-1,f.offsetY)) {
                nextX--;
                nextMove();
            }
        }
    }
    
    private void moveLeftDummy(Figure f, TetrisGrid temp)
    {
        if(isGameOver || isPause) return;
        
        if(nextX-1 >= 0)
        {
            if (temp.isNextMoveValid(f,f.offsetX-1,f.offsetY))
            {
                nextX--;
                f.setOffset(nextX, nextY);
            }
        }
    }
    
    private void moveRight() {
        if(isGameOver || isPause) return;
        if(f.getMaxRightOffset()+1 < 10) {
            if (tg.isNextMoveValid(f,f.offsetX+1,f.offsetY)) {
                nextX++;
                nextMove();
            }
        }
    }
    
    private void moveRightDummy(Figure f, TetrisGrid temp)
    {
        if(isGameOver || isPause) return;
        if(f.getMaxRightOffset()+1 < 10)
        {
            if (temp.isNextMoveValid(f,f.offsetX+1,f.offsetY))
            {
                nextX++;
                f.setOffset(nextX, nextY);
            }
        }
    }
    
    private synchronized void moveDown() {
        if(isGameOver || isPause) return;
        nextY++;
        nextMove();
    }
    
    private synchronized void moveDrop() {
        if(isGameOver || isPause) return;
        
        f.offsetYLast = f.offsetY;
        f.offsetXLast = f.offsetX;
        clearOldPosition();
        
        while(tg.isNextMoveValid(f, f.offsetX, f.offsetY)) {
            f.setOffset(f.offsetX, f.offsetY+1);
        }
        
        tg.addFigure(f);
        paintTG();
        dropNext();
        nextMove();   
    }
    
    private synchronized void moveDropDummy(Figure f, TetrisGrid temp)
    {
        if(isGameOver || isPause) return;
        
        f.offsetYLast = f.offsetY;
        f.offsetXLast = f.offsetX;
                
        while(temp.isNextMoveValid(f, f.offsetX, f.offsetY))
            f.setOffset(f.offsetX, f.offsetY+1);
        
        temp.addFigure(f);
    }
    
    private synchronized void rotation() {
        if(isGameOver || isPause) return;
        for (int j = 0; j < f.arrX.length; j++) {
            cells[f.arrY[j]+f.offsetY][f.arrX[j]+f.offsetX].setBackground(Color.WHITE);
            cells[f.arrY[j]+f.offsetY][f.arrX[j]+f.offsetX].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        f.rotationRight();
        if(!tg.isNextMoveValid(f,f.offsetX,f.offsetY)) {
            f.rotationLeft();
        }
        nextMove();
    }
    
    private synchronized void rotationDummy(Figure f, TetrisGrid temp) {
        if(isGameOver || isPause) return;
        
        f.rotationRight();
        if(!temp.isNextMoveValid(f,f.offsetX,f.offsetY)) {
            f.rotationLeft();
        }
    }
    
    private synchronized void pause() {
        isPause = !isPause;
    }

    private void restart() {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                tg.gLines.get(i)[j] = 0;
                cells[i][j].setBackground(Color.WHITE);
                cells[i][j].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            }
        } 
        
        paintTG();
        
        ff.resetCounts();
        isGameOver = false;

        fNext.clear();
        for (int i = 0; i != nSteps; i++)
        	fNext.add(ff.getRandomFigure());
        
        tt.resetTime();
        time.setText("00:00:00");
        tg.resetStats();
        
        lookup = 1;
        
        challengeLines = 0; 
        showAiSelector();
        
        tg.addChallenge(challengeLines);
        paintTG();
        
        isPause = false;
        dropNext();
        nextMove();
    }
    
    private void showAiSelector()
    {
    	cpuName = "Skilled";
    	lookup = 1;
    	
    	if (cpuNameLabel == null)
    		cpuNameLabel = new JLabel(cpuName);
    	else
    		cpuNameLabel.setText(cpuName);
    	
    	gameSettingsMenu = new JPanel(null);
    	gameSettingsMenu.setLayout(new GridLayout(3, 0));
    	
    	
    	JRadioButton greedyButton = new JRadioButton("Greedy", false);
    	greedyButton.addActionListener(new RadioListener() );
    	greedyButton.setActionCommand("Greedy");
    	
    	JRadioButton h0Button = new JRadioButton("Skilled", true);
    	h0Button.addActionListener(new RadioListener() );
    	h0Button.setActionCommand("Skilled");
    	
    	JRadioButton h1Button = new JRadioButton("Lookahead", false);
    	h1Button.addActionListener(new RadioListener() );
    	h1Button.setActionCommand("Lookahead");
    	
    	JRadioButton humanButton = new JRadioButton("Human", false);
    	humanButton.addActionListener(new RadioListener() );
    	humanButton.setActionCommand("Human");
    	
    	ButtonGroup bgroup = new ButtonGroup();
    	bgroup.add(greedyButton);
    	bgroup.add(h0Button);
    	bgroup.add(h1Button);
    	bgroup.add(humanButton);

    	JPanel radioPanel = new JPanel();
    	radioPanel.setLayout(new GridLayout(2, 1));
    	radioPanel.add(h0Button);
    	radioPanel.add(greedyButton);
    	radioPanel.add(h1Button);
    	radioPanel.add(humanButton);
    
    	int min=0, max=11;
    	scroller = new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, min, max);
    	scroller.addAdjustmentListener(new ScrollerListener() );
    	
    	scrollerLabel = new JLabel("Challenge: \n starting with 0 extra line(s)");
    	
    	
    	gameSettingsMenu.add(radioPanel);
    	gameSettingsMenu.add(scrollerLabel);
    	gameSettingsMenu.add(scroller);
    	
    	
    	JOptionPane.showMessageDialog(this,gameSettingsMenu,"Game Settings", 
                JOptionPane.PLAIN_MESSAGE );
    }

    private class ScrollerListener implements AdjustmentListener
    {
		public void adjustmentValueChanged(AdjustmentEvent arg0) {
			challengeLines = arg0.getValue();
			scrollerLabel.setText("Challenge: \n starting with " 
					+ arg0.getValue() + " extra line(s)"  );
		}
    }
    
    private class RadioListener implements ActionListener
    {
    	public void actionPerformed(ActionEvent ae) {
            cpuName = ae.getActionCommand();
            cpuNameLabel.setText(cpuName);
            if(cpuName == "Human")
            	lookup = 0;
            else if (cpuName == "Skilled" || cpuName == "Greedy")
            	lookup = 1;
            else if (cpuName == "Lookahead")
            	lookup = 2;
        }
    }
    
    private void showHiScore() {
        setHiScorePanel();
        
        JOptionPane.showMessageDialog(this,hiScorePanel,"HI SCORE", 
                JOptionPane.PLAIN_MESSAGE, 
                new ImageIcon(loadImage("splash32.png")));
        
        hiScorePanel = null;
    }

    private void setHiScorePanel() {
        hiScorePanel = new JPanel(new BorderLayout());
        
        String[] colNames = {"Place", "Points", "Lines", "Name"};
        String[][] data = new String[tg.hiScore.length+1][colNames.length];
        data[0] = colNames;
        for (int i = 0; i < tg.hiScore.length; i++) {
            data[i+1] = new String[colNames.length];
            data[i+1][0] = (i+1)+".";
            data[i+1][1] = (""+tg.hiScore[i].score);
            data[i+1][2] = (""+tg.hiScore[i].lines);
            data[i+1][3] = (""+tg.hiScore[i].name);
        }
        
        JTable table = new JTable(data, colNames);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setBackground(new Color(230,255,255));
        table.setEnabled(false);
        
        hiScorePanel.add(table,BorderLayout.CENTER);
    }
    
    private class MenuHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
                JMenuItem tmp = (JMenuItem) e.getSource();
                if (tmp == iartrisRestart) {
                    restart();
                } else if (tmp == iartrisPause) {
                    pause();
                } else if (tmp == iartrisHiScore) {
                    showHiScore();
                } else if (tmp == iartrisExit) {
                    System.exit(0);
                }
            } catch (Exception exc) {
                exc.printStackTrace(System.out);
            }
        }
    }


    Movement search(TetrisGrid old, int level)
    {
    	if (level == lookup)
    		return null;
    	
    	Figure f = fNext.get(level);
    	TetrisGrid temp = new TetrisGrid(old);
    	
    	ArrayList<Movement> movementTemp = new ArrayList<Movement>();
    	
    	Movement mov;
    	
    	int maxRotation;
    	
    	switch (f.getGridVal())
    	{
	    	case Figure.I:
	    	case Figure.S:
	    	case Figure.Z:
	    		maxRotation = 2;
	    		break;
	    		
	    	case Figure.O:
	    		maxRotation = 1;
	    		
	    	default:
	    		maxRotation = 4;
    	}
    	       	
    	for (int xM = -4; xM <= 5; xM++)
    	{       		
    		if (xM <= 0)
    		{
    			for (int i = 0; i != maxRotation; i++)
    			{
    				for (int h = 0; h != xM; h--)
        				moveLeftDummy(f, temp);
    				
    				for (int j = 0; j != i; j++)
    					rotationDummy(f, temp);
    				
    				moveDropDummy(f, temp);
    				
    				mov = new Movement(-xM, 0, i, search(temp, level + 1));
    	    				
    				mov.setScore(cpuName, temp.countHoles(), temp.getHeight(), temp.averageHeight(),
        					temp.deltaHeight(), temp.variance(), temp.numPiecesOverload(),
        					temp.weightedHeight());     			
        			movementTemp.add(mov);
        			        			
        			temp = new TetrisGrid(old);
        			nextX = 4;
        	        nextY = 0;
    				f.resetRotation(); 
    				f.setOffset(4, 0);
    			}
    		}
			else
			{
    			for (int i = 0; i != maxRotation; i++)
    			{
    				for (int h = 1; h <= xM; h++)
        				moveRightDummy(f, temp);
        			        				
    				for (int j = 0; j != i; j++)
    					rotationDummy(f, temp);
    				
    				mov = new Movement(0, xM, i, search(temp, level + 1));
    				
    				moveDropDummy(f, temp);
    				mov.setScore(cpuName, temp.countHoles(), temp.getHeight(), temp.averageHeight(),
        					temp.deltaHeight(), temp.variance(), temp.numPiecesOverload(),
        					temp.weightedHeight());    
        			
        			movementTemp.add(mov);
        			
        			temp = new TetrisGrid(old);	
        			nextX = 4;
        	        nextY = 0;
    				f.resetRotation(); 
    				f.setOffset(4, 0);
    			}
    		}
       	}
    	
    	for (int i = 1; i != maxRotation; i++)
    	{
    		for (int h = 1; h <= 5; h++)
    		{
    			for (int j = 0; j != i; j++)
        			rotationDummy(f, temp);
    			
    			for (int k = 0; k != h; k++)
    				moveRightDummy(f, temp);
    		
        		mov = new Movement(0, h, i, search(temp, level + 1));
				
				moveDropDummy(f, temp);
				mov.setScore(cpuName, temp.countHoles(), temp.getHeight(), temp.averageHeight(),
    					temp.deltaHeight(), temp.variance(), temp.numPiecesOverload(),
    					temp.weightedHeight());   
    			
    			movementTemp.add(mov);
    			
    			temp = new TetrisGrid(old);	
    			nextX = 4;
    	        nextY = 0;
				f.resetRotation(); 
				f.setOffset(4, 0);
    		}
    	}
    	
    	return getBest(movementTemp);
    }

	Movement getBest(ArrayList<Movement> movementTemp) 
	{
		Movement best = null;
		float bestScore = Float.MAX_VALUE;
		
		for (Movement m: movementTemp)
			if (m.getScore() < bestScore)
			{
				bestScore = m.getScore();
				best = m;
			}
		
		return best;
	}
}

