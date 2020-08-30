package space_invaders;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;

import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.google.gson.Gson;

import shared.GameMain;
import shared.Inventory;
import shared.Item;
import shared.ItemInfo;
import shared.ItemListJson;
import shared.ItemsInfo;
import shared.RequestProtocol;
import shared.ResponseProtocol;
import shared.Slot;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;

import java.util.*;

public class SpaceInvaders extends Canvas implements GameMain
{

//	public static final boolean LOGIN_PANEL_ON = true;
	public static final boolean LOGIN_PANEL_ON = false;

	public static final String ServerURLPrefix = "http://localhost:8080/space_invaders_server/";
//	public static final String ServerURLPrefix = "http://192.168.1.73:8080/space_invaders_server/";
	
	public static GameMain gameMain;
	public static JFrame frame;
	
	public static Random r = new Random();
	
	public static final int TITLE_BAR_HEIGHT = 19;

	public static final int W = 630;
	public static final int H = 600;
	
	public static final int H_ADDED = 300;
	
	public static final int ROW_SIZE = 5;
	public static final int COL_SIZE = 5;
	
	public static final int START_BUTTON_WIDTH = 200;
	public static final int START_BUTTON_HEIGHT = 50;
	
	public static final int GAME_OVER_LABEL_WIDTH = 300;
	public static final int GAME_OVER_LABEL_HEIGHT = 100;
	
	public static final int SCORE_GAP = 10;

	public static final int SCORE_WIDTH = 50;
	public static final int SCORE_HEIGHT = 20;
	public static final int SCORE_START_X = W/2-SCORE_WIDTH-SCORE_GAP;
	public static final int SCORE_START_Y = 5;

	public static final int CURRENT_SCORE_WIDTH = 60;
	public static final int CURRENT_SCORE_HEIGHT = 20;
	public static final int CURRENT_SCORE_START_X = W/2;
	public static final int CURRENT_SCORE_START_Y = 5;

	//-----------------------------------------------------
	// General
	//-----------------------------------------------------

//	private int x, y, dx, dy, radius;
//	private int p1x, p1y, p1speed, p1w, p1h;
//
//	private boolean r1died;
//	private int numLives = 3;
//	private int score = 0;
	public static boolean gameStart;
	public static boolean gameOver;
	public static int score;

	Timer mainGameThread;
	Thread updateSpaceshipInfoThread;
	Timer requestOtherSpaceshipInfoThread;
	Thread updateAlienInfoThread;
	
	// To detect client involved lag
	Timer lagDetectorThread;
	long prevClockTime;
	int prevCurFrames;
	
	public static boolean online = false;

    //-------------------------------------------------------------------------
	// panels and components
	//-------------------------------------------------------------------------
	public static JButton btStart;
	
	public static JLabel lblGameOver;
	public static JLabel lblScore;
	public static JLabel lblCurrentScore;

	//-----------------------------------------------------
	// Lives
	//-----------------------------------------------------
	public static final int NUM_LIVES = 3;

	public static JLabel[] lives;
	public static int numLives;

	//-----------------------------------------------------
	// Enemy
	//-----------------------------------------------------
	public static final int ENEMIES_ROW_SIZE = 4;
	public static final int ENEMIES_COL_SIZE = 9;

	public static Enemy[][] enemies;
	public static int numEnemies;
	
	public static Queue<Enemy> deadAliensQueue; // 죽은 alien을 상대방에게 전하기 위해 일단 여기에 넣고, OPERATION_UPDATE_ALIEN를 처리하는 thread에서 한개씩 보냄.

	//-----------------------------------------------------
	// Spaceship
	//-----------------------------------------------------
	public static Spaceship spaceship;
	public static Spaceship otherSpaceship;
	
//	public static Vector otherSpaceshipInfo;
	public static double otherSpaceshipPrevX;
	public static int otherSpaceshipPrevXDir;
	public static int otherSpaceshipPrevTime;

	public static double otherSpaceshipCurX;
	public static int otherSpaceshipCurXDir;
	public static int otherSpaceshipCurTime;

	public static final double SPACESHIP_START_X = 0;
	public static final double SPACESHIP_START_Y = 0;
	public static final int SPACESHIP_WIDTH = 44;
	public static final int SPACESHIP_HEIGHT = 34;
	public static final double SPACESHIP_X_SPEED = 5.0;
	public static final double SPACESHIP_Y_SPEED = 5.0;
	public static final String SPACESHIP_01_IMAGE_FILENAME = "assets/spaceship_01.png";
	public static final String SPACESHIP_02_IMAGE_FILENAME = "assets/spaceship_02.png";

	public static boolean spaceshipRightKeyPressed;
	public static boolean spaceshipLeftKeyPressed;

	public static boolean spaceshipKeyPressed;
	
	public static int userIDAfterLogin;

	//-----------------------------------------------------
	// Player's Info
	//-----------------------------------------------------
	public static int gold; // MUST be downloaded from the server after login.
	
	//-----------------------------------------------------
	// Inventory
	//-----------------------------------------------------
	public static Inventory inventory; // MUST be downloaded from the server after login.
	public static Slot slot; // MUST be downloaded from the server after login.
	
	//-------------------------------------------------------------------------
	// Sound/Music
	//-------------------------------------------------------------------------
	public static AudioClip backgroundMusic01;

	//-------------------------------------------------------------------------
	// Barricades
	//-------------------------------------------------------------------------
	public static final int NUM_BARRICADES = 4;
	public static final int BARRICADE_GAP = 30;

	public static final int BARRICADE_START_X = BARRICADE_GAP;
	public static final int BARRICADE_START_Y = 350;

	Barricade[] barricades;
	
	public static int curFrames;
	
	//-------------------------------------------------------------------------
	// Panels
	//-------------------------------------------------------------------------
	public static LoginPanel loginPanel;
	public static ChatPanel chatPanel;
	public static InventoryPanel inventoryPanel;
	public static SlotsPanel slotsPanel;
	public static ShopPanel shopPanel;
	
	public static SlotsUIPanel slotsUIPanel;
	
	//-------------------------------------------------------------------------
	// Items
	//-------------------------------------------------------------------------
	public static boolean doublePointActivated;
	
	//-------------------------------------------------------------------------
	// Fast Protocol
	//-------------------------------------------------------------------------
	public static final String JSON_STRING_TO_UPDATE_SPACESHIP_PREFIX_BEFORE_ID = "{\"operation\":\"update_spaceship\",\"id\":\""; 
	public static final String JSON_STRING_TO_UPDATE_SPACESHIP_PREFIX_BEFORE_X = "\",\"pwd\":\"\",\"message\":\"{\\\"x\\\":"; 
	public static final String JSON_STRING_TO_UPDATE_SPACESHIP_PREFIX_BEFORE_XDIR = ",\\\"xDir\\\":"; 
	public static final String JSON_STRING_TO_UPDATE_SPACESHIP_PREFIX_BEFORE_TIME = ",\\\"time\\\":"; 
	public static final String JSON_STRING_TO_UPDATE_SPACESHIP_POSTFIX_AFTER_TIME = "}\"}"; 

	public static String jsonStringToUpdateSpaceshipPrefixBeforeX = ""; // LoginPanel에서 로그인이 성공적으로 되면, assemble된다. login id가 필요하므로..
	public static String jsonStringToUpdateSpaceshipPrefixBeforeXDir = JSON_STRING_TO_UPDATE_SPACESHIP_PREFIX_BEFORE_XDIR;
	public static String jsonStringToUpdateSpaceshipPrefixBeforeTime = JSON_STRING_TO_UPDATE_SPACESHIP_PREFIX_BEFORE_TIME;
	public static String jsonStringToUpdateSpaceshipPrefixAfterTime = JSON_STRING_TO_UPDATE_SPACESHIP_POSTFIX_AFTER_TIME;
	public static String jsonStringToUpdateSpaceship = ""; // 위의 3개는 최종적으로 RequestProtocol을 보낼때 assemble된다.
	
	//-------------------------------------------------------------------------
	// ghost recon testing
	//-------------------------------------------------------------------------
	public static final int NUM_FRAMES_TO_SKIP_FOR_GHOST_RECON = 30; 

	public static boolean delayProtocolKeyPressed;
	public static int countDelayProtocol;	
	
	//-------------------------------------------------------------------------
	// syncing aliens
	//-------------------------------------------------------------------------
	public static boolean aliensSyncStarted;
	public static boolean aliensSyncFinished;
	
	//-------------------------------------------------------------------------
	// timers
	//-------------------------------------------------------------------------
	public static Timer updateInventoryTimer;

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public SpaceInvaders() {
		gameMain = this;
		init();
		
    	System.out.println("SpaceInvaders: getX():" + getX() + " getY():" + getY());
	}

	public void init()
	{
		System.out.println("hello1");
		curFrames = 0;
		prevCurFrames = 0;
	    this.setBounds(0, 0, W, H);
	    frame.add(this, new Integer(0), 0);
	    
	    btStart = new JButton("START"); // construct a JButton
        btStart.setBounds(W/2 - START_BUTTON_WIDTH/2, H/2 - START_BUTTON_HEIGHT/2, START_BUTTON_WIDTH, START_BUTTON_HEIGHT);
        btStart.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            // display/center the jdialog when the button is pressed
//            JDialog d = new JDialog(frame, "Hello", true);
//            d.setBounds(100, 100, 300, 200);
//            d.setLocationRelativeTo(frame);
//            d.setVisible(true);
        	  gameStart = true;
        	  gameOver = false;
        	  Component c = (Component)e.getSource();
          	  c.setVisible(false);
          	  lblGameOver.setVisible(false);
          	  
          	  prevClockTime = System.nanoTime();
          		//((JPanel)c.getParent()).revalidate();
          }
        });
	    frame.add( btStart , new Integer(1), 0);                     // add the label to the JFrame
	    
	    //-------------------------------------------------
	    // init enemy
	    //-------------------------------------------------
	    // create an enemy
	    enemies = new Enemy[ENEMIES_ROW_SIZE][ENEMIES_COL_SIZE];
	    numEnemies = 0;
	    
	    for (int i = 0; i < enemies.length; i++) {
		    for (int j = 0; j < enemies[0].length; j++) {
		    	enemies[i][j] = new Enemy(i, j, j * (Enemy.ENEMY_WIDTH + Enemy.ENEMY_GAP), i * (Enemy.ENEMY_HEIGHT + Enemy.ENEMY_GAP), "assets/alien01", Bullet.ENEMY_BULLET_SHOOT_SOUND_FILENAME);
			    frame.add(enemies[i][j].lblEnemy, new Integer(1), 0);
			    // enemy를 미리 조금 죽인후에 시작할 때 사용.. 아래는 밑쪽의 2개 row총 18마리를 죽인 상태로 시작.
//			    if (i >= 2) {
//			    	enemies[i][j].lblEnemy.setVisible(false);
//			    	enemies[i][j].enemyAlive = false;
//			    }
//			    else {
//			    	enemies[i][j].lblEnemy.setVisible(true);
//			    }
			    numEnemies++;
		    }
	    }
//    	Enemy.lblEnemyXSpeed += 18 * Enemy.ENEMY_X_SPEED_INC;
	    
		deadAliensQueue = new LinkedList<Enemy>();
		
	    //-------------------------------------------------
	    // init Spaceship
	    //-------------------------------------------------
		spaceship = new Spaceship(SPACESHIP_START_X, H-SPACESHIP_HEIGHT-TITLE_BAR_HEIGHT, 0, 0,
				SPACESHIP_X_SPEED, SPACESHIP_Y_SPEED, curFrames, SPACESHIP_01_IMAGE_FILENAME, true);
//		System.out.println(spaceship.toJson());
		
		otherSpaceship = new Spaceship(SPACESHIP_START_X, H-SPACESHIP_HEIGHT-TITLE_BAR_HEIGHT, 0, 0,
				SPACESHIP_X_SPEED, SPACESHIP_Y_SPEED, curFrames, SPACESHIP_02_IMAGE_FILENAME, false);
//		otherSpaceshipInfo = new Vector();
		otherSpaceshipPrevX = 0.0;
		otherSpaceshipPrevXDir = 0;
		otherSpaceshipPrevTime = -1;

		spaceshipRightKeyPressed = false;
		spaceshipLeftKeyPressed = false;
		
		spaceshipKeyPressed = false;

		userIDAfterLogin = 0;
		
		//-----------------------------------------------------
		// Player's Info
		//-----------------------------------------------------
		gold = 1000; // MUST be downloaded from the server after login. ??????????????????????????????????????????????

		//-----------------------------------------------------
		// Inventory
		//-----------------------------------------------------
		inventory = new Inventory();
		
		// MUST be downloaded from the server after login. ???????????????????????????????????????????????????????????
//		inventory.addItem(new SkinChangingItem(new Color(255, 0, 0)));
//		inventory.addItem(new DoublePointItem());
//		inventory.addItem(new SkinChangingItem(new Color(0, 255, 0)));
//		inventory.addItem(new SkinChangingItem(new Color(0, 0, 255)));
//		inventory.addItem(new DoublePointItem());
//		inventory.addItem(new DoublePointItem());
		
		//-----------------------------------------------------t
		// Slot
		//-----------------------------------------------------
		slot = new Slot();
		
		//-----------------------------------------------------
		// Items
		//-----------------------------------------------------
		doublePointActivated = false;

	    //-------------------------------------------------
	    // init lives
	    //-------------------------------------------------
		lives = new JLabel[NUM_LIVES];
		numLives = 0;

		for (int i = 0; i < NUM_LIVES; i++) {
			lives[i] = new JLabel(new ImageIcon(spaceship.image));
			lives[i].setBounds(i * (SPACESHIP_WIDTH + 5), 0, SPACESHIP_WIDTH, SPACESHIP_HEIGHT);
		    frame.add(lives[i], new Integer(1), 0);                     // add the label to the JFrame
		    lives[i].setVisible(true);
		    
		    numLives++;
		}
		//??????????????? ToDo: decrease num lives when the spaceship is hit.
		
	    //-------------------------------------------------
	    // init bullet
	    //-------------------------------------------------
		spaceship.bullet = new Bullet(Bullet.SPACESHIP_BULLET_WIDTH, Bullet.SPACESHIP_BULLET_HEIGHT,
				Bullet.SPACESHIP_BULLET_X_SPEED, Bullet.SPACESHIP_BULLET_Y_SPEED,
				Bullet.SPACESHIP_BULLET_X_DIR, Bullet.SPACESHIP_BULLET_Y_DIR,
				Bullet.SPACESHIP_BULLET_COLOR, Bullet.SPACESHIP_BULLET_SHOOT_SOUND_FILENAME,
				SPACESHIP_WIDTH, SPACESHIP_HEIGHT);
		
		otherSpaceship.bullet = new Bullet(Bullet.SPACESHIP_BULLET_WIDTH, Bullet.SPACESHIP_BULLET_HEIGHT,
				Bullet.SPACESHIP_BULLET_X_SPEED, Bullet.SPACESHIP_BULLET_Y_SPEED,
				Bullet.SPACESHIP_BULLET_X_DIR, Bullet.SPACESHIP_BULLET_Y_DIR,
				Bullet.SPACESHIP_BULLET_COLOR, Bullet.SPACESHIP_BULLET_SHOOT_SOUND_FILENAME,
				SPACESHIP_WIDTH, SPACESHIP_HEIGHT);
		
	    //-------------------------------------------------
	    // init baricades
	    //-------------------------------------------------
		barricades = new Barricade[NUM_BARRICADES];
		for (int i = 0; i < NUM_BARRICADES; i++) {
			barricades[i] = new Barricade(BARRICADE_START_X + i * (Barricade.BARRICADE_WIDTH + BARRICADE_GAP), BARRICADE_START_Y);
		}
		
	    //-------------------------------------------------
	    // Add "Game Over" label
	    //-------------------------------------------------
	    lblGameOver = new JLabel("Game Over");
	    lblGameOver.setFont(new Font(lblGameOver.getFont().getName(), Font.BOLD, 48));
	    lblGameOver.setHorizontalAlignment(SwingConstants.CENTER);
	    lblGameOver.setBackground(Color.yellow);
	    lblGameOver.setOpaque(true);
	    lblGameOver.setBounds(W/2 - GAME_OVER_LABEL_WIDTH/2, H/2 - START_BUTTON_HEIGHT/2 - GAME_OVER_LABEL_HEIGHT - 5, GAME_OVER_LABEL_WIDTH, GAME_OVER_LABEL_HEIGHT);
	    frame.add( lblGameOver , new Integer(1), 2);                     // add the button to the JFrame
	    lblGameOver.setVisible(false);
	    
	    //-------------------------------------------------
	    // Add "Score / Current Score" label
	    //-------------------------------------------------
	    lblScore = new JLabel("Score");
	    lblScore.setFont(new Font(lblScore.getFont().getName(), Font.BOLD, 18));
	    lblScore.setHorizontalAlignment(SwingConstants.CENTER);
	    lblScore.setForeground(Color.WHITE);
	    lblScore.setOpaque(false);
	    lblScore.setBounds(SCORE_START_X, SCORE_START_Y, SCORE_WIDTH, SCORE_HEIGHT);
	    frame.add( lblScore , new Integer(1), 0);                     // add the button to the JFrame
	    lblScore.setVisible(true);

	    lblCurrentScore = new JLabel("00000");
	    lblCurrentScore.setFont(new Font(lblCurrentScore.getFont().getName(), Font.BOLD, 18));
	    lblCurrentScore.setHorizontalAlignment(SwingConstants.CENTER);
	    lblCurrentScore.setForeground(Color.WHITE);
	    lblCurrentScore.setOpaque(false);
	    lblCurrentScore.setBounds(CURRENT_SCORE_START_X, CURRENT_SCORE_START_Y, CURRENT_SCORE_WIDTH, CURRENT_SCORE_HEIGHT);
	    frame.add( lblCurrentScore , new Integer(1), 0);                     // add the button to the JFrame
	    lblCurrentScore.setVisible(true);
	    
	    aliensSyncStarted = false;
	    aliensSyncFinished = false;
	    
	    //-------------------------------------------------
	    // init game
	    //-------------------------------------------------
		enableEvents(java.awt.AWTEvent.KEY_EVENT_MASK);
		//requestFocus();
		reset();
		
		mainGameThread = new Timer(true);
		mainGameThread.schedule(new java.util.TimerTask() {
				public void run()
		        {
			        update();
			        repaint();
		        }
		},0,30);

		//---------------------------------------------------------------------
		// OPERATION_UPDATE_SPACESHIP: User의 현재 위치등의 정보를 Thread를 이용하여 주기적으로 전달.
		//---------------------------------------------------------------------
		startUpdateSpaceshipInfoThread();
		
		//---------------------------------------------------------------------
		// OPERATION_UPDATE_ALIEN: 죽은 alien 한개 정보를 Thread를 이용하여 주기적으로 전달.
		//---------------------------------------------------------------------
		startUpdateAlienInfoThread();
		
	    //-------------------------------------------------
		// init background and play(forever)
	    //-------------------------------------------------
		try {
			URL url = (new java.io.File("assets/POL-mecha-world-short.wav")).toURI().toURL();
			backgroundMusic01 = Applet.newAudioClip(url);
        } catch(MalformedURLException murle) {
        	murle.printStackTrace();
        }
		
		/**
		 * Using another thread to play background music
		 */
        Thread t1 = new Thread() {
           public void run() {
        	   backgroundMusic01.loop();
           }
        };
        t1.start();
        t1.run();

	    //-------------------------------------------------
		// init general
	    //-------------------------------------------------
		score = 0;
		String strScore = String.format("%05d", score);
		lblCurrentScore.setText("" + strScore);

		gameStart = false;
		gameOver = false;
		
	    //-------------------------------------------------
		// init variables to test ghost recon
	    //-------------------------------------------------
		delayProtocolKeyPressed = false;
		countDelayProtocol = 0;	
		
	    //-------------------------------------------------
		// lagDetectorThread
	    //-------------------------------------------------
		lagDetectorThread = new Timer(true);
		lagDetectorThread.schedule(new java.util.TimerTask() {
				public void run()
		        {
			        long currentClockTime = System.nanoTime();
			        long diffClockTime = currentClockTime - prevClockTime;
			        int numFramesExpected = (int)(diffClockTime / (1000000000.0/30.0));
			        int diffCurFrames = curFrames - prevCurFrames;

			        int diffFrames = Math.abs(diffCurFrames - numFramesExpected);

			        if (diffFrames > 10) {
//			        	System.out.println("WARNING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//						System.out.println("curFrames  = " + curFrames);
//						System.out.println("diffFrames = " + diffFrames);
			        }
			        
			        prevClockTime = currentClockTime;
			        prevCurFrames = curFrames;
			        
//					System.out.println(curFrames);
		        }
		},0,1000); // 1000 = 1 second
		System.out.println("hello2");
		
		updateInventoryTimer = null;
	}
	
	public void restart()
	{
		curFrames = 0;

		mainGameThread.cancel();
////		t.purge();
		
		//-------------------------------------------------
	    // init enemy
	    //-------------------------------------------------
	    // create an enemy
	    numEnemies = 0;
 
	    for (int i = 0; i < enemies.length; i++) {
		    for (int j = 0; j < enemies[0].length; j++) {
		    	enemies[i][j].reset(j * (Enemy.ENEMY_WIDTH + Enemy.ENEMY_GAP), i * (Enemy.ENEMY_HEIGHT + Enemy.ENEMY_GAP));
			    enemies[i][j].lblEnemy.setVisible(true);
			    numEnemies++;
		    }
	    }
	    
	    //-------------------------------------------------
	    // init Spaceship
	    //-------------------------------------------------
		spaceship.reset(SPACESHIP_START_X, H-SPACESHIP_HEIGHT-TITLE_BAR_HEIGHT, 0, 0,
				SPACESHIP_X_SPEED, SPACESHIP_Y_SPEED, curFrames, true);
		
		spaceshipRightKeyPressed = false;
		spaceshipLeftKeyPressed = false;
		
		spaceshipKeyPressed = false;
	    
		userIDAfterLogin = 0;
		
	    //-------------------------------------------------
	    // init lives
	    //-------------------------------------------------
		numLives = 0;

		for (int i = 0; i < NUM_LIVES; i++) {
		    lives[i].setVisible(true);
		    numLives++;
		}
	    		
	    //-------------------------------------------------
	    // init bullet
	    //-------------------------------------------------
		spaceship.bullet.alive = false;	

	    //-------------------------------------------------
	    // init baricades
	    //-------------------------------------------------
		for (int i = 0; i < NUM_BARRICADES; i++) {
			barricades[i].reset();
		}
		
	    //-------------------------------------------------
		// init general
	    //-------------------------------------------------
		score = 0;
		String strScore = String.format("%05d", score);
		lblCurrentScore.setText("" + strScore);

		gameStart = false;
		gameOver = false;
		
	    //-------------------------------------------------
		// restart timer
	    //-------------------------------------------------
		mainGameThread = new Timer(true);
		mainGameThread.schedule(new java.util.TimerTask() {
				public void run()
		        {
			        update();
			        repaint();
		        }
		},0,30);
		
//		updateSpaceshipInfoThread = new Timer(true);
//		updateSpaceshipInfoThread.schedule(new java.util.TimerTask() {
//				public void run()
//		        {
//			        
//		        }
//		},0,30);
		
		requestOtherSpaceshipInfoThread = new Timer(true);
		requestOtherSpaceshipInfoThread.schedule(new java.util.TimerTask() {
				public void run()
		        {
			        
		        }
		},0,30);
		
		updateInventoryTimer = null;
	}
	
	// General한 request 동작을 모두 넣음.
	public static ResponseProtocol request(String ServerName, RequestProtocol rp) {
		ResponseProtocol respProt = null;
		
		Gson gson = new Gson();
		String jsonString = gson.toJson(rp);
    	
		try {
		    URL url = new URL(SpaceInvaders.ServerURLPrefix + ServerName); // ServerName: "SpaceInvadersServer" or "SpaceInvadersMerchantServer"
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    conn.setDoOutput(true);
		    conn.setRequestMethod("POST");
		    conn.setRequestProperty("Content-Type", "application/json");
		    
	        OutputStream os = conn.getOutputStream();
	        os.write(jsonString.getBytes());
	        os.flush();
	        
		    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String respJsonString = "";
			String decodedString = in.readLine();
			while (decodedString != null) {
    			System.out.println("Client: " + decodedString);
				
				respJsonString += decodedString;
				decodedString = in.readLine();
			}
			conn.disconnect();
			System.out.println("respJsonString: [" + respJsonString + "]");
			respProt = gson.fromJson(respJsonString.toString(), ResponseProtocol.class);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return respProt;
    }
	
	public static void startUpdateInventoryTimer() { 
		updateInventoryTimer = new Timer(true);
		updateInventoryTimer.schedule(new java.util.TimerTask() {
			public void run()
	        {
				if (!SpaceInvaders.online) {
					return;
				}
				
				Gson gson = new Gson();

				System.out.println("updateInventoryTimer");
	    		
				ResponseProtocol respProt = request("SpaceInvadersMerchantServer", new RequestProtocol(RequestProtocol.OPERATION_INVENTORY_UPDATE, "" + SpaceInvaders.userIDAfterLogin, "", "", "java"));	

	    		if (respProt.result.equals("true")) {
//	    			SpaceInvaders.inventory.removeAllItems();
	    				
    				System.out.println("updateInventoryTimer: respProt.info [" + respProt.info + "]");
                    ItemsInfo invFromServer = gson.fromJson(respProt.info.toString(), ItemsInfo.class);
    				// ***** 다를 경우만 inventory를 바꾸고, 똑같을 경우는 손대지 않는다. 따라서 받아온 itemsInfo는 버리면 된다.
    				
    				// 서버로부터 받아온 inventory가 현재 client의 inventory와 틀릴 때만, 적용한다.
	    		    // 201610180941: 이제는 invFromServer가 ItemsInfo이다.
                    if (!SpaceInvaders.inventory.equalsToItemsInfo(invFromServer)) {
    					inventory = ItemsInfo.createInventory(invFromServer);
    					
	    				if (inventoryPanel.isShowing()) {
		    				// refresh itemsList on the panel
		    				((DefaultListModel)inventoryPanel.itemsList.getModel()).clear();
		    				System.out.println("if (inventoryPanel.isShowing()): clear");
		    				Item[] invItemsArray = SpaceInvaders.inventory.dump();
		    				for (int i = 0; i < invItemsArray.length; i++) {
		    					((DefaultListModel)inventoryPanel.itemsList.getModel()).addElement(invItemsArray[i]);
		    				}	
		    				System.out.println("if (inventoryPanel.isShowing()): refreshed");
	    				}
	    				else if (shopPanel.isShowing()) {
		    				// refresh itemsList on the panel
		    				((DefaultListModel)shopPanel.invItemsList.getModel()).clear();
		    				System.out.println("if (shopPanel.isShowing()): clear");
		    				Item[] invItemsArray = SpaceInvaders.inventory.dump();
		    				for (int i = 0; i < invItemsArray.length; i++) {
		    					((DefaultListModel)shopPanel.invItemsList.getModel()).addElement(invItemsArray[i]);
		    				}
		    				System.out.println("if (shopPanel.isShowing()): refreshed");
	    				}
	    				else if (slotsPanel.isShowing()) {
		    				// refresh itemsList on the panel
		    				((DefaultListModel)slotsPanel.invItemsList.getModel()).clear();
		    				System.out.println("if (slotsPanel.isShowing()): clear");
		    				Item[] invItemsArray = SpaceInvaders.inventory.dump();
		    				for (int i = 0; i < invItemsArray.length; i++) {
		    					((DefaultListModel)slotsPanel.invItemsList.getModel()).addElement(invItemsArray[i]);
		    				}
		    				System.out.println("if (slotsPanel.isShowing()): refreshed");
	    				}
    				}
    			}
    			else {
    				JOptionPane.showMessageDialog(null, "SpaceInvaders::startUpdateInventoryTimer(): error: Invalid login information!! Please try again!");
    			}
	        }
		},0,5000);
	}
	
	public static void stopUpdateInventoryTimer() {
		if (updateInventoryTimer != null) {
			updateInventoryTimer.cancel();
		}
	}
	
	public static void updateSlot() { 
		Gson gson = new Gson();

		System.out.println("updateSlot");
		slot.removeAllItems();
		
		ResponseProtocol respProt = request("SpaceInvadersMerchantServer", new RequestProtocol(RequestProtocol.OPERATION_SLOT_UPDATE, "" + SpaceInvaders.userIDAfterLogin, "", "", "java"));
			
		if (respProt.result.equals("true")) {
			System.out.println("updateSlot: respProt.info [" + respProt.info + "]");
			
			if (respProt.info.toString().equals("{}")) {
				return;
			}
			
			ItemListJson slotList = gson.fromJson(respProt.info.toString(), ItemListJson.class);

			slotList.items = slotList.items.trim();
			StringTokenizer itemsTokens = new StringTokenizer (slotList.items, ";");
			
			int index = 0;
			while (itemsTokens.hasMoreTokens()) {
				String curItemToken = itemsTokens.nextToken();
				System.out.println("Client: curItemToken [" + curItemToken + "]");

				curItemToken = curItemToken.trim();
				curItemToken = curItemToken.substring(1, curItemToken.length()-1);
				StringTokenizer attrTokens = new StringTokenizer (curItemToken, ",");
			
                ItemInfo curItemInfo = new ItemInfo();
                
                while (attrTokens.hasMoreTokens()) {
                    String curToken = attrTokens.nextToken();
                    System.out.println("Client: curToken [" + curToken + "]");

                    StringTokenizer keyAndVal = new StringTokenizer(curToken, ":");
                    
                    String key = keyAndVal.nextToken().trim();
                    String val = keyAndVal.nextToken().trim();
                    
                    key = key.substring(1, key.length()-1);
                    val = val.substring(1, val.length()-1);
                    if (key.equals("name")) {
                        curItemInfo.name = val;
                    }
                    else if (key.equals("class_name")) {
                        curItemInfo.className = val;
                    }
                    else if (key.equals("price")) {
                        curItemInfo.price = Integer.parseInt(val);
                    }
                    else if (key.equals("image_file_name")) {
                        curItemInfo.imageFileName = val;
                    }
                    else if (key.equals("description")) {
                        curItemInfo.description = val;
                    }
                }
                
                Item curItem = Item.createItemObj(curItemInfo);
                
                if (curItem == null) { // error
                    System.out.println("SpaceInvaders::startUpdateInventoryTimer(): error: no such item: " + curItemInfo.className);
                    System.exit(1);
                }
                
                curItem.setGameMain(SpaceInvaders.gameMain);

				SpaceInvaders.slot.insertItemAt(curItem, index);
//							((DefaultListModel)SpaceInvaders.shopPanel.shopItemsList.getModel()).addElement(curItem.toString());
				index++;
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "SpaceInvaders::updateSlot(): error: Invalid login information!! Please try again!");
		}

		slotsPanel.update();
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public void reset()
	{ 	
//		numLives = 3;
//		score = 0;
				 
		gameStart = false;
		gameOver = false;
	}
 
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public void paint(Graphics g) { // render()
		if (gameOver) {
			return;
		}
		
//		g.setColor(Color.black);
//		g.drawString("Lives: " + numLives, 10, 20);
//		g.drawString("Score: " + score, 420, 20);
//		if (numLives <= 0) {
//			reset();
//			gameOver = true;
//      	    btStart.setVisible(true);
//    	    lblGameOver.setVisible(true);
//		}
//		if (score == ROW_SIZE*COL_SIZE) {
//			reset();
//			gameOver = true;
//      	    btStart.setVisible(true);
//		}
//

		//-------------------------------------------------
		// draw enemy1
		//-------------------------------------------------
		
		//-------------------------------------------------
		// draw Spaceship
		//-------------------------------------------------
		spaceship.paint(g);

		//-------------------------------------------------
		// draw Spaceship
		//-------------------------------------------------
		otherSpaceship.paint(g);

		//-------------------------------------------------
		// enemies are shooting bullets.
		//-------------------------------------------------
	    for (int i = 0; i < enemies.length; i++) {
	    	for (int j = 0; j < enemies[0].length; j++) {
				enemies[i][j].bullet.paint(g);
		    }
	    }
	    
	    //-------------------------------------------------
	    // draw barricades
	    //-------------------------------------------------
		for (int i = 0; i < NUM_BARRICADES; i++) {
			barricades[i].paint(g);
		}
		
		if (slotsUIPanel != null) {
			slotsUIPanel.repaint();
		}
	}
 
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public void processKeyEvent(KeyEvent e) {
	    if ( e.getID() == KeyEvent.KEY_PRESSED ) {
			//-------------------------------------------------
			// move Spaceship
			//-------------------------------------------------
	        if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
	        	spaceship.xDir = 1;
	        	spaceshipRightKeyPressed = true;
	        }
	 
	        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
	        	spaceship.xDir = -1;
	        	spaceshipLeftKeyPressed = true;
	        }

			//-------------------------------------------------
			// shoot bullet
			//-------------------------------------------------
	        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
	        	spaceshipKeyPressed = true;
	        }
	        
			//-------------------------------------------------
			// delay protocol key for testing
			//-------------------------------------------------
	        if (e.getKeyCode() == KeyEvent.VK_Z) {
	        	delayProtocolKeyPressed = true;
	        }
	        
			//-------------------------------------------------
			// inventory toggle
			//-------------------------------------------------
	        if (e.getKeyCode() == KeyEvent.VK_I) {
	        	if (online && !gameStart && slotsPanel != null && inventoryPanel != null) {
	        		slotsPanel.setVisible(false);
	        		inventoryPanel.setVisible(!inventoryPanel.isVisible());
	        	}
	        }

			//-------------------------------------------------
			// inventory <-> slots panel toggle
			//-------------------------------------------------
	        if (e.getKeyCode() == KeyEvent.VK_M) {
	        	if (online && !gameStart && slotsPanel != null && inventoryPanel != null) {
	        		inventoryPanel.setVisible(false);
	        		slotsPanel.setVisible(!slotsPanel.isVisible());
	        	}
	        }
	      //-------------------------------------------------
	      // use slots
		  //-------------------------------------------------
	        if (e.getKeyCode() == KeyEvent.VK_1) {
	        	if (online && gameStart && slotsPanel != null && inventoryPanel != null) {
	        		slot.useItemAt(0);
	        	}
	        }
	        if (e.getKeyCode() == KeyEvent.VK_2) {
	        	if (online && gameStart && slotsPanel != null && inventoryPanel != null) {
	        		slot.useItemAt(1);
	        	}
	        }
	        if (e.getKeyCode() == KeyEvent.VK_3) {
	        	if (online && gameStart && slotsPanel != null && inventoryPanel != null) {
	        		slot.useItemAt(2);
	        	}
	        }
	        if (e.getKeyCode() == KeyEvent.VK_4) {
	        	if (online && gameStart && slotsPanel != null && inventoryPanel != null) {
	        		slot.useItemAt(3);
	        	}
	        }
	        if (e.getKeyCode() == KeyEvent.VK_5) {
	        	if (online && gameStart && slotsPanel != null && inventoryPanel != null) {
	        		slot.useItemAt(4);
	        	}
	        }
	    }

	    if ( e.getID() == KeyEvent.KEY_RELEASED ) {
			//-------------------------------------------------
			// move Spaceship
			//-------------------------------------------------
	        if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
	        	spaceshipRightKeyPressed = false;
	        	if (!spaceshipLeftKeyPressed) {
	        		spaceship.xDir = 0;
	        	}
	        	else {
	        		spaceship.xDir = -1;
	        	}
	        }
	 
	        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
	        	spaceshipLeftKeyPressed = false;
	        	if (!spaceshipRightKeyPressed) {
	        		spaceship.xDir = 0;
	        	}
	        	else {
	        		spaceship.xDir = 1;
	        	}
	        }
	        
			//-------------------------------------------------
			// bullet stop
			//-------------------------------------------------
	        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
	        	spaceshipKeyPressed = false;
	        }
	    }
	}
 
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public void update()
    {
		if (!gameStart) {
			return;
		}

		if (gameOver) {
			return;
		}

		//-------------------------------------------------
		// hit enemy
		//-------------------------------------------------
	    for (int i = 0; i < enemies.length && !gameOver; i++) {
		    for (int j = 0; j < enemies[0].length && !gameOver; j++) {
				if (spaceship.bullet.alive && enemies[i][j].enemyAlive) {
					if (enemies[i][j].isHit(spaceship.bullet.x, spaceship.bullet.y)) {
						spaceship.bullet.alive = false;
						enemies[i][j].enemyAlive = false;
						enemies[i][j].lblEnemy.show(false);
						Enemy.lblEnemyXSpeed += Enemy.ENEMY_X_SPEED_INC;
						
						if (doublePointActivated) {
							score += 2*Enemy.ENEMY_SCORE;
						}
						else {
							score += Enemy.ENEMY_SCORE;
						}
						String strScore = String.format("%05d", score);
						lblCurrentScore.setText("" + strScore);
						
						numEnemies--;
						
						deadAliensQueue.add(enemies[i][j]);
						
//						if (numEnemies <= 0) {
//							gameOver = true;
//						}
					}
				}
		    }
		}
		
		//-------------------------------------------------
		// move enemies
		//-------------------------------------------------
	    if(!gameOver) {
			if (Enemy.lblEnemyXDir == 1) {
			    for (int j = enemies[0].length-1; j >= 0; j--) {
				    for (int i = 0; i < enemies.length; i++) {
//						if (enemies[i][j].enemyAlive) {
						enemies[i][j].move();
						
						if (enemies[i][j].enemyAlive) {
							for (int k = 0; k < NUM_BARRICADES; k++) {
								barricades[k].detectCollision((int)enemies[i][j].lblEnemyX, (int)enemies[i][j].lblEnemyY + enemies[i][j].enemiesStartY, enemies[i][j].ENEMY_WIDTH, enemies[i][j].ENEMY_HEIGHT);
							}
						}
				    }
			    }
			}
			else {
			    for (int j = 0; j < enemies[0].length; j++) {
				    for (int i = 0; i < enemies.length; i++) {
						enemies[i][j].move();

						if (enemies[i][j].enemyAlive) {
							for (int k = 0; k < NUM_BARRICADES; k++) {
								barricades[k].detectCollision((int)enemies[i][j].lblEnemyX, (int)enemies[i][j].lblEnemyY + enemies[i][j].enemiesStartY, enemies[i][j].ENEMY_WIDTH, enemies[i][j].ENEMY_HEIGHT);
							}
						}
				    }
			    }
		    }
	    }
	    
		//-------------------------------------------------
		// enemies are shooting bullets.
		//-------------------------------------------------
	    for (int j = 0; j < enemies[0].length && !gameOver; j++) {
	    	boolean currentColumnDone = false;
		    for (int i = enemies.length-1; i >= 0 && !currentColumnDone && !gameOver; i--) {
				if (enemies[i][j].enemyAlive) {
					enemies[i][j].shoot();
					currentColumnDone = true;
				}
		    }
	    }
	    
	    //-------------------------------------------------
	    // hit barricades
	    //-------------------------------------------------
		for (int i = 0; i < NUM_BARRICADES; i++) {
			if (barricades[i].detectCollision((int)spaceship.bullet.x, (int)spaceship.bullet.y, spaceship.bullet.width, spaceship.bullet.height)) {
				spaceship.bullet.alive = false;
			}
		}
		
		//-------------------------------------------------
		// check if one of enemies(leaves) collides the spaceship. 
		//-------------------------------------------------
	    for (int j = 0; j < enemies[0].length && !gameOver; j++) {
	    	boolean currentColumnDone = false;
		    for (int i = enemies.length-1; i >= 0 && !currentColumnDone && !gameOver; i--) {
				if (enemies[i][j].enemyAlive) {
					if (enemies[i][j].detectCollisionBySpaceship(spaceship.x, spaceship.y, SPACESHIP_WIDTH, SPACESHIP_HEIGHT)) {
						gameOver = true;
					}
				}
		    }
	    }
	    
		//-------------------------------------------------
		// enemies are shooting bullets.
		//-------------------------------------------------
	    for (int i = 0; i < enemies.length && !gameOver; i++) {
	    	for (int j = 0; j < enemies[0].length && !gameOver; j++) {
	    		if (enemies[i][j].bullet.alive) {
				    //-------------------------------------------------
				    // hit barricades
				    //-------------------------------------------------
					for (int k = 0; k < NUM_BARRICADES && enemies[i][j].bullet.alive; k++) {
						if (barricades[k].detectCollision((int)enemies[i][j].bullet.x, (int)enemies[i][j].bullet.y, enemies[i][j].bullet.width, enemies[i][j].bullet.height)) {
							enemies[i][j].bullet.alive = false;
						}
					}
	
					if (enemies[i][j].bullet.alive) {
						enemies[i][j].moveBullet();
					}
	    		}
		    }
	    }
	    
		//-------------------------------------------------
		// update spaceship
		//-------------------------------------------------
	    spaceship.update();

		//-------------------------------------------------
		// update otherSpaceship
		//-------------------------------------------------
//		if (gameStart && otherSpaceshipInfo.size() > 2) {
//			// 마지먹과 그 전것을 빼서 처리.(Ghost Recon)
//			Spaceship curOtherSpaceship = (Spaceship)otherSpaceshipInfo.get(otherSpaceshipInfo.size()-1);
//			Spaceship prevOtherSpaceship = (Spaceship)otherSpaceshipInfo.get(otherSpaceshipInfo.size()-2);
//			// 만약 curOtherSpaceship을 빼는 동안, 다시 어디선가 한개가 더 add되어버리면, 같은 것을 두개를 뺄 가능성이 있다.
//			if (curOtherSpaceship != prevOtherSpaceship) {
////				double xDiff = Math.abs(curOtherSpaceship.x - prevOtherSpaceship.x);
//				
//			}
//			otherSpaceship.xDir = curOtherSpaceship.xDir;
//		}
//
////	    if (gameStart) {
////	    	otherSpaceship.xDir = 1;
////	    }
//	    
		otherSpaceship.update();

	    //-------------------------------------------------
		// update lives(HUD)
		//-------------------------------------------------
		if (!gameOver) {
			for (int i = numLives; i < NUM_LIVES; i++) {
			    lives[i].setVisible(false);
			}
		}
		
		curFrames++;
    }
	
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	// OPERATION_UPDATE_SPACESHIP: User의 현재 위치등의 정보를 Thread를 이용하여 주기적으로 전달.
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	public void startUpdateSpaceshipInfoThread() {
		updateSpaceshipInfoThread = new Thread() {
			public void run(){
				while (true) {
	//				System.out.println("Thread Running");
					
					if (gameStart && !gameOver) {
						if (!SpaceInvaders.online) {
							try {
								Thread.sleep(100);
							} catch(InterruptedException e) {
							}
							continue;
						}
		//					System.out.println("updateSpaceshipInfoThread");
		
		//					String jsonString = spaceship.toJson();
		//					System.out.println("updateSpaceshipInfoThread:: spaceship.toJson(): " + jsonString);
		
							// 디버깅을 위해 spaceship정보를 json으로 바꾼 것을 다시 Spaceship object로 만든다음 다시 toJson()을 해서 확인.
		//					Spaceship newSpaceship = Spaceship.fromJson(jsonString);
		//					System.out.println("updateSpaceshipInfoThread:: Spaceship.fromJson(jsonString).toJson(): " + newSpaceship.toJson());
		
	//					RequestProtocol rp1 = new RequestProtocol(RequestProtocol.OPERATION_UPDATE_SPACESHIP, SpaceInvaders.loginPanel.userText.getText(), "", spaceship.toJson());
	//					
						Gson gson = new Gson();
	//		    		String jsonString = gson.toJson(rp1);
	//		    		System.out.println("updateSpaceshipInfoThread:: jsonString:                  |" + jsonString + "|");
						
						// 위에서 너무 오래 걸리기 때문에, 아래처럼 바로 조립해서 보내도록 하드 코딩.
						jsonStringToUpdateSpaceship =
							jsonStringToUpdateSpaceshipPrefixBeforeX + spaceship.x + jsonStringToUpdateSpaceshipPrefixBeforeXDir +
							spaceship.xDir + jsonStringToUpdateSpaceshipPrefixBeforeTime + curFrames + jsonStringToUpdateSpaceshipPrefixAfterTime;
	//		    		System.out.println("updateSpaceshipInfoThread:: jsonStringToUpdateSpaceship: |" + jsonStringToUpdateSpaceship + "|");
	
			    		try {
			    		    URL url = new URL(SpaceInvaders.ServerURLPrefix + "SpaceInvadersServer");
			    		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			    		    conn.setDoOutput(true);
			    		    conn.setRequestMethod("POST");
			    		    conn.setRequestProperty("Content-Type", "application/json");
			    		    
			    	        OutputStream os = conn.getOutputStream();
			    	        os.write(jsonStringToUpdateSpaceship.getBytes());
			    	        os.flush();
			    	        
	//		    		    System.out.println("Client: conn.getResponseMessage() : " + conn.getResponseMessage());
			    		    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			    		    String respJsonString = "";
			    			String decodedString = in.readLine();
			    			while (decodedString != null) {
	//		    				System.out.println("Client: " + decodedString);
			    				
			    				respJsonString += decodedString;
			    				decodedString = in.readLine();
			    			}
			    			conn.disconnect();
	//	    				System.out.println("Client: respJsonString [" + respJsonString + "]");
	
			    			//-------------------------------------------------
			    			// 아래의 프로토콜이 너무 오래 걸리므로, 받은 것에서 정보를 직접 바로 떼어낸 다음. 바로 적용한다.
			    			//-------------------------------------------------
	//		    			ResponseProtocol respProt = gson.fromJson(respJsonString.toString(), ResponseProtocol.class);
	//		    			
	//		    			if (respProt.result.equals("true")) {
	//		    				// return된 것을 가지고 따로 할 것은 없는 듯.
	//		    				System.out.println("SpaceInvaders::updateSpaceshipInfoThread::run(): respProt.info = |" + respProt.info + "|");
	//		    				Spaceship curOtherSpaceshipInfo = Spaceship.fromJson(respProt.info);
	//
	//		    				System.out.println("SpaceInvaders::updateSpaceshipInfoThread::run(): curOtherSpaceshipInfo.toJson() = " + curOtherSpaceshipInfo.toJson());
	////		    				otherSpaceship.x = curOtherSpaceshipInfo.x;
	//		    				otherSpaceshipInfo.add(curOtherSpaceshipInfo);
	//		    			}
	//		    			else {
	////		    				JOptionPane.showMessageDialog(null, "error: Invalid login information!! Please try again!");
	//		    			}
			    			//-------------------------------------------------
			    			// 여기에서는 바로 otherSpaceship의 x와 xDir에 바로 적용.
			    			//-------------------------------------------------
			    			//SpaceInvaders::updateSpaceshipInfoThread::run(): respProt.info =                 |{"x":0.0,"y":547.0,"xDir":0,"yDir":0,"xSpeed":5.0,"ySpeed":5.0}|
			    			//SpaceInvaders::updateSpaceshipInfoThread::run(): curOtherSpaceshipInfo.toJson() = {"x":0.0,"y":547.0,"xDir":0,"yDir":0,"xSpeed":5.0,"ySpeed":5.0}
	//		    			System.out.println("SpaceInvaders::updateSpaceshipInfoThread::run(): respJsonString = |" + respJsonString + "|");
			    			//SpaceInvaders::updateSpaceshipInfoThread::run(): respJsonString = |{"operation":"update_spaceship","id":"qwe123","result":"true","info":"{\"x\":20.0,\"y\":547.0,\"xDir\":0,\"yDir\":0,\"xSpeed\":5.0,\"ySpeed\":5.0}"}|
	
			    			// 상대 플레이어가 있을 경우, true가 돌아옴.
			    			if (respJsonString.indexOf("true") != -1) {
			    				System.out.println("if (respJsonString.indexOf(\"true\") != -1) {");
			    				//---------------------------------------------
			    				// 첫번째 접속한 플레이어 ONLY: aliensSynced가 true라면 첫번째 플레이어는 이미 내 alien정보를 처음 한번 upload했다는 뜻이고, 두번째 플레이어 입장에서는 첫번째 player의 alien정보를 download했다는 신호이다.
			    				//---------------------------------------------
			    				if (!aliensSyncStarted && userIDAfterLogin == 0) {
				    				System.out.println("if (!aliensSyncStarted && userIDAfterLogin == 0) {");
			    					
			    					AliensInfo aliensInfo = new AliensInfo();
			    					aliensInfo.initMapByAliensArray((int)(enemies[0][0].lblEnemyX), (int)(enemies[0][0].lblEnemyY),
			    						enemies[0][0].lblEnemyXDir, enemies[0][0].lblEnemyXSpeed, Enemy.enemiesStartY,
			    						enemies.length, enemies[0].length, enemies);
			    					
			    		    		String aliensInfoJsonString = gson.toJson(aliensInfo);
			    		    		
			    		    		RequestProtocol rp1 = new RequestProtocol(
			    		    			RequestProtocol.OPERATION_ALIEN_SYNCING_UPLOAD,
			    		    			"" + userIDAfterLogin, "", // we don't need password
			    		    			aliensInfoJsonString, "java");
			    		    		
			    		    		String jsonStringAlienSyncingUpload = gson.toJson(rp1);
	//		    		    		System.out.println("Client: OPERATION_ALIEN_SYNCING_UPLOAD::jsonStringAlienSyncingUpload = |" + jsonStringAlienSyncingUpload + "|");
			    		    		
			    		    		System.out.println();
	
					    		    url = new URL(SpaceInvaders.ServerURLPrefix + "SpaceInvadersServer");
					    		    conn = (HttpURLConnection) url.openConnection();
					    		    conn.setDoOutput(true);
					    		    conn.setRequestMethod("POST");
					    		    conn.setRequestProperty("Content-Type", "application/json");
					    		    
					    	        os = conn.getOutputStream();
					    	        os.write(jsonStringAlienSyncingUpload.getBytes());
					    	        os.flush();
	//		    		    		System.out.println("Client: jsonStringAlienSyncingUpload sent");
	
					    		    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					    		    respJsonString = "";
					    			decodedString = in.readLine();
					    			while (decodedString != null) {
			//		    				System.out.println("Client: " + decodedString);
					    				
					    				respJsonString += decodedString;
					    				decodedString = in.readLine();
					    			}
					    			
					    	        // OPERATION_ALIEN_SYNCING_UPLOAD은 그냥 보내기만 하면 된다.
					    	        // 만약 잘 처리되었는지 확인할 경우, respProtocol을 확인하여 result가 true인지 확인할 것.
					    	        // 또 한가지 문제는, 만약 두번째 player가 aliens info를 성공적으로 download하고 시작했는지 handshaking을 할 것. 나중에....(201605240933)
					    	        
					    			conn.disconnect();
	
			    					aliensSyncStarted = true;
			    				}
	
			    				//---------------------------------------------
			    				// 첫번째 플레이어: 일반적인 내 위치와 상대방 위치 교환
			    				//---------------------------------------------
			    				else if (aliensSyncStarted && userIDAfterLogin == 0) {
			    					System.out.println("else if (aliensSyncStarted && userIDAfterLogin == 0) {");
			    					
				    				if (delayProtocolKeyPressed) {
				    					delayProtocolKeyPressed = false;
				    					countDelayProtocol = NUM_FRAMES_TO_SKIP_FOR_GHOST_RECON;	
				    				}
				    				
				    				if (countDelayProtocol > 0) {
				    					countDelayProtocol--;
				    				}
				    				else {
	//			    					System.out.println("respJsonString : |" + respJsonString + "|");
		//			    				otherSpaceshipPrevX = 0.0;
		//			    				otherSpaceshipPrevXDir = 0;
		//			    				otherSpaceshipPrevTime = -1;
		
						    			int startIndexOfX = respJsonString.indexOf("x\\\":") + 4;
		//				    			System.out.println("startIndexOfX = " + startIndexOfX);
						    			int endIndexOfX = respJsonString.indexOf(",\\\"xD");
		//				    			System.out.println("endIndexOfX = " + endIndexOfX);
						    			
						    			otherSpaceshipCurX = Double.parseDouble(respJsonString.substring(startIndexOfX, endIndexOfX));
			
						    			int startIndexOfXDir = respJsonString.indexOf("xDir\\\":") + 7;
		//				    			System.out.println("startIndexOfXDir = " + startIndexOfXDir);
						    			int endIndexOfXDir = respJsonString.indexOf(",\\\"ti");
		//				    			System.out.println("endIndexOfXDir = " + endIndexOfXDir);
		//				    			System.out.println("respJsonString.substring(startIndexOfXDir, endIndexOfXDir) = " + respJsonString.substring(startIndexOfXDir, endIndexOfXDir));
						    			otherSpaceshipCurXDir = Integer.parseInt(respJsonString.substring(startIndexOfXDir, endIndexOfXDir));
						    			
						    			int startIndexOfTime = respJsonString.indexOf("time\\\":") + 7;
		//				    			System.out.println("startIndexOfTime = " + startIndexOfTime);
						    			int endIndexOfTime = respJsonString.indexOf("}");
		//				    			System.out.println("endIndexOfTime = " + endIndexOfTime);
		//				    			System.out.println("respJsonString.substring(startIndexOfTime, endIndexOfTime) = " + respJsonString.substring(startIndexOfTime, endIndexOfTime));
						    			otherSpaceshipCurTime = Integer.parseInt(respJsonString.substring(startIndexOfTime, endIndexOfTime));
						    			
						    			if (otherSpaceshipPrevTime == -1) {
						    				otherSpaceship.x = otherSpaceshipCurX;
						    				otherSpaceship.xDir = otherSpaceshipCurXDir;
						    				otherSpaceship.time = otherSpaceshipCurTime;
						    			}
						    			else { // Ghost Recon when the previous info exists
		//				    				if (otherSpaceship.painted) {
		//					    				// 만약 curOtherSpaceship을 빼는 동안, 다시 어디선가 한개가 더 add되어버리면, 같은 것을 두개를 뺄 가능성이 있다.
		//					    				double xDiff = otherSpaceshipCurX - otherSpaceship.x;
		//					    				otherSpaceship.xDir = 0;
		//					    				if (xDiff < 0) {
		//					    					otherSpaceship.xDir = -1;
		//					    				}
		//					    				else if (xDiff > 0) {
		//					    					otherSpaceship.xDir = 1;
		//					    				}
		//					    				
		//					    				xDiff = Math.abs(xDiff);
		//					    				
		//					    				if (xDiff <= SPACESHIP_X_SPEED) {
		//					    					otherSpaceship.xSpeed = SPACESHIP_X_SPEED;
		//					    					otherSpaceship.x = otherSpaceshipCurX;
		//					    					otherSpaceship.xDir = 0;
		//					    				}
		//					    				else if (xDiff > otherSpaceship.xSpeed) {
		//					    					otherSpaceship.xSpeed = SPACESHIP_X_SPEED;
		////					    					System.out.println("else if (xDiff > otherSpaceship.xSpeed) {");
		//					    				}
		//					    				
		//	//				    				int timeDiff = Math.abs(otherSpaceshipPrevTime - otherSpaceshipCurTime);
		//					    				
		//	//				    				otherSpaceship.xDir = otherSpaceshipCurXDir;
		//					    				otherSpaceship.painted = false;
		////				    					System.out.println("otherSpaceship.xSpeed = " + otherSpaceship.xSpeed);
		//				    				}
						    			} 
						    			otherSpaceshipPrevX = otherSpaceship.x;
					    				otherSpaceshipPrevXDir = otherSpaceship.xDir;
					    				otherSpaceshipPrevTime = otherSpaceship.time;
				    				}
			    				}
			    				
			    				//---------------------------------------------
			    				// 두번째 접속한 플레이어 ONLY
			    				//---------------------------------------------
			    				else if (!aliensSyncStarted && !aliensSyncFinished && userIDAfterLogin == 1) {
			    					System.out.println("else if (!aliensSyncStarted && !aliensSyncFinished && userIDAfterLogin == 1) {");
			    		    		RequestProtocol rpAlienSyncing = new RequestProtocol(
			    		    				RequestProtocol.OPERATION_ALIEN_SYNCING_DOWNLOAD,
			    		    				"" + userIDAfterLogin, "", "", "java");
			    		    		
			    		    		gson = new Gson();
			    		    		String jsonString = gson.toJson(rpAlienSyncing);
			    		    		
			    		    		try {
			    		    		    url = new URL(ServerURLPrefix + "SpaceInvadersServer");
			    		    		    conn = (HttpURLConnection) url.openConnection();
			    		    		    conn.setDoOutput(true);
			    		    		    conn.setRequestMethod("POST");
			    		    		    conn.setRequestProperty("Content-Type", "application/json");
			    		    		    
			    		    	        os = conn.getOutputStream();
			    		    	        os.write(jsonString.getBytes());
			    		    	        os.flush();
			    		    	        
	//		    		    		    System.out.println("Client: conn.getResponseMessage() : " + conn.getResponseMessage());
			    		    		    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			    		    		    respJsonString = "";
			    		    			decodedString = in.readLine();
			    		    			
			    		    			while (decodedString != null) {
	//				    		    				System.out.println("Client: " + decodedString);
			    		    				
			    		    				respJsonString += decodedString;
			    		    				decodedString = in.readLine();
			    		    			}
			    		    			conn.disconnect();
				    		    		System.out.println("Client: respJsonString: " + respJsonString);
	
			    		    			ResponseProtocol respProt = gson.fromJson(respJsonString.toString(), ResponseProtocol.class);
			    		    			
						    			if (respProt.result.equals("true")) {
				    		    		    System.out.println("Alien Syncing Info Receiving");
	
				    		    			//????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
				    		    			// 상대방의 정보를 푸는 곳.
				    		    			//????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
						    				AliensInfo aliensInfo = gson.fromJson(respProt.info, AliensInfo.class);
						    				aliensInfo.readMapByAliensArray(enemies);
	
					    		    		// 만약 위에서 정상적으로 첫번째 플레이어의 alien정보가 download되었다면 aliensSynced에 true를 넣는다. ???????????????????????????????????????????????????????????????? 확인하고 사용할 것.
						    				aliensSyncStarted = true;
						    				aliensSyncFinished = true;
						    			}
			    		    		} catch (MalformedURLException e1) {
			    		    			e1.printStackTrace();
			    		    			System.out.println("MalformedURLException: " + e1);
			    		    		} catch (IOException e1) {
			    		    			e1.printStackTrace();
			    		    			System.out.println("IOException: " + e1);
			    		    		}
			    				}
			    				//---------------------------------------------
			    				// 두번째 플레이어: 일반적인 내 위치와 상대방 위치 교환
			    				//---------------------------------------------
			    				else if (aliensSyncStarted && userIDAfterLogin == 1) {
			    					System.out.println("else if (aliensSyncStarted && userIDAfterLogin == 1) {");
	
			    					if (delayProtocolKeyPressed) {
				    					delayProtocolKeyPressed = false;
				    					countDelayProtocol = NUM_FRAMES_TO_SKIP_FOR_GHOST_RECON;	
				    				}
				    				
				    				if (countDelayProtocol > 0) {
				    					countDelayProtocol--;
				    				}
				    				else {
	//			    					System.out.println("respJsonString : |" + respJsonString + "|");
		//			    				otherSpaceshipPrevX = 0.0;
		//			    				otherSpaceshipPrevXDir = 0;
		//			    				otherSpaceshipPrevTime = -1;
		
						    			int startIndexOfX = respJsonString.indexOf("x\\\":") + 4;
		//				    			System.out.println("startIndexOfX = " + startIndexOfX);
						    			int endIndexOfX = respJsonString.indexOf(",\\\"xD");
		//				    			System.out.println("endIndexOfX = " + endIndexOfX);
						    			
						    			otherSpaceshipCurX = Double.parseDouble(respJsonString.substring(startIndexOfX, endIndexOfX));
			
						    			int startIndexOfXDir = respJsonString.indexOf("xDir\\\":") + 7;
		//				    			System.out.println("startIndexOfXDir = " + startIndexOfXDir);
						    			int endIndexOfXDir = respJsonString.indexOf(",\\\"ti");
		//				    			System.out.println("endIndexOfXDir = " + endIndexOfXDir);
		//				    			System.out.println("respJsonString.substring(startIndexOfXDir, endIndexOfXDir) = " + respJsonString.substring(startIndexOfXDir, endIndexOfXDir));
						    			otherSpaceshipCurXDir = Integer.parseInt(respJsonString.substring(startIndexOfXDir, endIndexOfXDir));
						    			
						    			int startIndexOfTime = respJsonString.indexOf("time\\\":") + 7;
		//				    			System.out.println("startIndexOfTime = " + startIndexOfTime);
						    			int endIndexOfTime = respJsonString.indexOf("}");
		//				    			System.out.println("endIndexOfTime = " + endIndexOfTime);
		//				    			System.out.println("respJsonString.substring(startIndexOfTime, endIndexOfTime) = " + respJsonString.substring(startIndexOfTime, endIndexOfTime));
						    			otherSpaceshipCurTime = Integer.parseInt(respJsonString.substring(startIndexOfTime, endIndexOfTime));
						    			
						    			if (otherSpaceshipPrevTime == -1) {
						    				otherSpaceship.x = otherSpaceshipCurX;
						    				otherSpaceship.xDir = otherSpaceshipCurXDir;
						    				otherSpaceship.time = otherSpaceshipCurTime;
						    			}
						    			else { // Ghost Recon when the previous info exists
		//				    				if (otherSpaceship.painted) {
		//					    				// 만약 curOtherSpaceship을 빼는 동안, 다시 어디선가 한개가 더 add되어버리면, 같은 것을 두개를 뺄 가능성이 있다.
		//					    				double xDiff = otherSpaceshipCurX - otherSpaceship.x;
		//					    				otherSpaceship.xDir = 0;
		//					    				if (xDiff < 0) {
		//					    					otherSpaceship.xDir = -1;
		//					    				}
		//					    				else if (xDiff > 0) {
		//					    					otherSpaceship.xDir = 1;
		//					    				}
		//					    				
		//					    				xDiff = Math.abs(xDiff);
		//					    				
		//					    				if (xDiff <= SPACESHIP_X_SPEED) {
		//					    					otherSpaceship.xSpeed = SPACESHIP_X_SPEED;
		//					    					otherSpaceship.x = otherSpaceshipCurX;
		//					    					otherSpaceship.xDir = 0;
		//					    				}
		//					    				else if (xDiff > otherSpaceship.xSpeed) {
		//					    					otherSpaceship.xSpeed = SPACESHIP_X_SPEED;
		////					    					System.out.println("else if (xDiff > otherSpaceship.xSpeed) {");
		//					    				}
		//					    				
		//	//				    				int timeDiff = Math.abs(otherSpaceshipPrevTime - otherSpaceshipCurTime);
		//					    				
		//	//				    				otherSpaceship.xDir = otherSpaceshipCurXDir;
		//					    				otherSpaceship.painted = false;
		////				    					System.out.println("otherSpaceship.xSpeed = " + otherSpaceship.xSpeed);
		//				    				}
						    			}
						    			otherSpaceshipPrevX = otherSpaceship.x;
					    				otherSpaceshipPrevXDir = otherSpaceship.xDir;
					    				otherSpaceshipPrevTime = otherSpaceship.time;
				    				}
			    				}			    
			    			}
			    		} catch (MalformedURLException e1) {
			    			e1.printStackTrace();
	//		    			System.out.println("MalformedURLException: " + e1);
			    		} catch (IOException e1) {
			    			e1.printStackTrace();
	//		    			System.out.println("IOException: " + e1);
			    		}
					}				
					try {
						Thread.sleep(100);
					} catch(InterruptedException e) {
					}
				}
			}
		};
		updateSpaceshipInfoThread.start();
	}
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	// OPERATION_UPDATE_SPACESHIP: end
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------

	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	// OPERATION_UPDATE_ALIEN: 죽은 alien 한개 정보를 Thread를 이용하여 주기적으로 전달.
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	public void startUpdateAlienInfoThread() {
		updateAlienInfoThread = new Thread(){
			public void run(){
				while (true) {
					if (!SpaceInvaders.online) {
						try {
							Thread.sleep(100);
						} catch(InterruptedException e) {
						}
						continue;
					}
					
					if (gameStart && !gameOver && (userIDAfterLogin == 0 || (aliensSyncStarted && aliensSyncFinished))) {
						Enemy curDeadAlien = deadAliensQueue.peek();
						String deadAlienInfo = ""; 
						if (curDeadAlien != null) {
							deadAliensQueue.remove();
							deadAlienInfo = "{\"row\":" + curDeadAlien.row + ", \"col\":" + curDeadAlien.col + "}";
	//			    		System.out.println("updateAlienInfoThread:: deadAlienInfo: |" + deadAlienInfo + "|");
						}
						else {
							deadAlienInfo = "{\"row\":-1, \"col\":-1}";
						}
						RequestProtocol rp1 = new RequestProtocol(RequestProtocol.OPERATION_UPDATE_ALIEN, "" + userIDAfterLogin, "", deadAlienInfo, "java");
	
						Gson gson = new Gson();
			    		String jsonString = gson.toJson(rp1);
	//		    		System.out.println("updateAlienInfoThread:: jsonString: |" + jsonString + "|");
	
			    		try {
			    		    URL url = new URL(SpaceInvaders.ServerURLPrefix + "SpaceInvadersServer");
			    		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			    		    conn.setDoOutput(true);
			    		    conn.setRequestMethod("POST");
			    		    conn.setRequestProperty("Content-Type", "application/json");
			    		    
			    	        OutputStream os = conn.getOutputStream();
			    	        os.write(jsonString.getBytes());
			    	        os.flush();
			    	        
	//		    		    System.out.println("Client: conn.getResponseMessage() : " + conn.getResponseMessage());
			    		    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			    		    String respJsonString = "";
			    			String decodedString = in.readLine();
			    			while (decodedString != null) {
	//		    				System.out.println("Client: " + decodedString);
			    				
			    				respJsonString += decodedString;
			    				decodedString = in.readLine();
			    			}
			    			conn.disconnect();
	//	    				System.out.println("Client: respJsonString [" + respJsonString + "]");
	
			    			//-------------------------------------------------
			    			// 아래의 프로토콜이 너무 오래 걸리므로, 받은 것에서 정보를 직접 바로 떼어낸 다음. 바로 적용한다.
			    			//-------------------------------------------------
	//{"operation":"update_alien","id":"0","result":"true","info":"{\"row\":-1, \"col\":-1}"}
	
			    			// 상대 플레이어가 dead alien의 정보를 보냈을 경우, true가 돌아옴.
			    			if (respJsonString.indexOf("true") != -1) {
			    				int deadAlienRow = -1; 
			    				int deadAlienCol = -1;
			    				
	//	    					System.out.println("OPERATION_UPDATE_ALIEN::respJsonString : |" + respJsonString + "|");
	//	    					System.out.println("updateAlienInfoThread hello1");
				    			int startIndexOfDeadAlienRow = respJsonString.indexOf("row\\\":") + 6;
	//	    					System.out.println("updateAlienInfoThread hello2 startIndexOfDeadAlienRow = " + startIndexOfDeadAlienRow);
	//			    			System.out.println("startIndexOfDeadAlienRow = " + startIndexOfDeadAlienRow);
				    			int endIndexOfDeadAlienRow = respJsonString.indexOf(", \\\"col");
	//	    					System.out.println("updateAlienInfoThread hello3 endIndexOfDeadAlienRow = " + endIndexOfDeadAlienRow);
	//			    			System.out.println("endIndexOfDeadAlienRow = " + endIndexOfDeadAlienRow);
				    			
				    			deadAlienRow = Integer.parseInt(respJsonString.substring(startIndexOfDeadAlienRow, endIndexOfDeadAlienRow));
	//	    					System.out.println("updateAlienInfoThread hello4 respJsonString.substring(startIndexOfDeadAlienRow, endIndexOfDeadAlienRow) = " + respJsonString.substring(startIndexOfDeadAlienRow, endIndexOfDeadAlienRow));
	
				    			int startIndexOfDeadAlienCol = respJsonString.indexOf("col\\\":") + 6;
	//			    			System.out.println("startIndexOfDeadAlienCol = " + startIndexOfDeadAlienCol);
				    			
	//	    					System.out.println("updateAlienInfoThread hello5 startIndexOfDeadAlienCol = " + startIndexOfDeadAlienCol);
	
				    			int endIndexOfDeadAlienCol = respJsonString.indexOf("}\"}");
	//	    					System.out.println("updateAlienInfoThread hello6 endIndexOfDeadAlienCol = " + endIndexOfDeadAlienCol);
	//			    			System.out.println("endIndexOfDeadAlienCol = " + endIndexOfDeadAlienCol);
	//			    			System.out.println("respJsonString.substring(startIndexOfDeadAlienCol, endIndexOfDeadAlienCol) = " + respJsonString.substring(startIndexOfDeadAlienCol, endIndexOfDeadAlienCol));
				    			deadAlienCol = Integer.parseInt(respJsonString.substring(startIndexOfDeadAlienCol, endIndexOfDeadAlienCol));
	
	//	    					System.out.println("updateAlienInfoThread hello7 respJsonString.substring(startIndexOfDeadAlienCol, endIndexOfDeadAlienCol)) = " + respJsonString.substring(startIndexOfDeadAlienCol, endIndexOfDeadAlienCol));
	
				    			if (deadAlienRow != -1) {
						    		System.out.println("updateAlienInfoThread:: deadAlienRow = " + deadAlienRow + " deadAlienCol = " + deadAlienCol);
						    		
						    		// 두번째 플레이어의 경우: 초기 enemy들의 sync가 끝났을 경우에 한해서, 첫번째 플레이어가 이미 죽은 enemy의 개별(한마리씩) 정보를
						    		// 보내올 때, 이미 sync에 의해서 죽어있으면, 그것을 중복처리하면 안된다.
						    		if (userIDAfterLogin == 1 && aliensSyncFinished && enemies[deadAlienRow][deadAlienCol].enemyAlive) {
										enemies[deadAlienRow][deadAlienCol].enemyAlive = false;
										enemies[deadAlienRow][deadAlienCol].lblEnemy.show(false);
										Enemy.lblEnemyXSpeed += Enemy.ENEMY_X_SPEED_INC;
										
										numEnemies--;
						    		}
						    		// 첫번째 플레이어의 경우: sync를 보내는 쪽이기 때문에, 중복하여 죽는 현상이 없으므로, 두번째 플레이어가 보내오는 죽인 enemy의
						    		// 정보를 그냥 update하면 된다.
						    		else if (userIDAfterLogin == 0) {
										enemies[deadAlienRow][deadAlienCol].enemyAlive = false;
										enemies[deadAlienRow][deadAlienCol].lblEnemy.show(false);
										Enemy.lblEnemyXSpeed += Enemy.ENEMY_X_SPEED_INC;
										
										numEnemies--;
						    		}
				    			}
	//	    					System.out.println("updateAlienInfoThread hello8");
			    			}
			    			
							if (deadAliensQueue.size() == 0 && numEnemies <= 0) {
								gameOver = true;
								gameStart = false;
							    lblGameOver.setVisible(true);
							    btStart.setVisible(true);
							    restart();
							}
							
			    		} catch (MalformedURLException e1) {
			    			e1.printStackTrace();
	//		    			System.out.println("MalformedURLException: " + e1);
			    		} catch (IOException e1) {
			    			e1.printStackTrace();
	//		    			System.out.println("IOException: " + e1);
			    		}
					}				
					try {
						Thread.sleep(100);
					} catch(InterruptedException e) {
					}
				}
			}
		};
		updateAlienInfoThread.start();
	}
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	// OPERATION_UPDATE_ALIEN: end
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	//---------------------------------------------------------------------
	
	// Check whether the spaceship is hit SADby bullet.(Bullet::move() will call this function)
	public static boolean isHit(double bulletX, double bulletY) {
		return  bulletX >= spaceship.x &&
				bulletX < spaceship.x + SPACESHIP_WIDTH &&
				bulletY >= spaceship.y &&
			    bulletY < spaceship.y + SPACESHIP_HEIGHT;
	}
	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public boolean isFocusable() 
    {
		return true;
    }

    //==================================================================================================== getIamge()
    // This is the function we made to get Image by URL
    // We could use "Toolkit.getDefaultToolkit().getImage(imageURL)" directly
    //==================================================================================================== 
    /**
     * Returns an Image object that can then be painted on the screen. 
     * The url argument must specify an absolute {@link URL}. The name
     * argument is a specifier that is relative to the url argument. 
     * <p>
     * This method always returns immediately, whether or not the 
     * image exists. When this applet attempts to draw the image on
     * the screen, the data will be loaded. The graphics primitives 
     * that draw the image will incrementally paint on the screen. 
     *
     * @param  path  a string that can represents the base location of the image
     * @return      the image at the specified path
     * @see         Image
     */
    public static Image getImage(String path) {
        Image tempImage = null;
        try {
            //URL imageURL = Main.class.getResource(path);
            URL imageURL = (new java.io.File(path)).toURI().toURL();
            
            tempImage =  Toolkit.getDefaultToolkit().getImage(imageURL);
        }
        catch (Exception e) {
            System.out.println("HttpChatApplet :: An error occured - " + e.getMessage());
        }
        return tempImage;
    }
    
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
    // implements GameMain
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public boolean getDoublePointActivated() {
		return doublePointActivated;
	}
	
	public void setDoublePointActivated(boolean doublePointActivated) {
		this.doublePointActivated = doublePointActivated;
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	// main()
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public static void main(String[] args)
    {
//		RequestProtocol rp1 = new RequestProtocol("kang123", "qwe123");
//		
//		Gson gson = new Gson();
//		String jsonString = gson.toJson(rp1);
//		
////		System.out.println(jsonString);
////
////		RequestProtocol rp2 = gson.fromJson(jsonString, RequestProtocol.class);
////		
////		System.out.println(rp2);
//
//		try {
//		    URL url = new URL("http://localhost:8080/space_invaders_server/SpaceInvadersServer");
//		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//		    conn.setDoOutput(true);
//		    conn.setRequestMethod("POST");
//		    conn.setRequestProperty("Content-Type", "application/json");
//		    
//		    // send {"id"="sam, "pwd"="qwe123"}
////	        String jsonString = "{\"id\"=\"sam\", \"pwd\"=\"qwe123\"}";
//	        System.out.println("Client: jsonString : " + jsonString);
//	        
//	        OutputStream os = conn.getOutputStream();
//	        os.write(jsonString.getBytes());
//	        os.flush();
//	        
//		    System.out.println("Client: conn.getResponseMessage() : " + conn.getResponseMessage());
//		    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//			String decodedString = in.readLine();
//			while (decodedString != null) {
//				System.out.println("Client: " + decodedString);
//				
//				decodedString = in.readLine();
//			}
//			
////			//========================================================================
////			// Parsing JSON array  {chatRoomLists [{}]}
////			//========================================================================
////			if (decodedString == null) {
////				System.out.println("decodedString == null ");
////				return;
////			}
////			else if (decodedString.charAt(0) == '{') {
////				JSONObject json = JSONObject.fromObject(decodedString);
////				String reponseStatus = json.getString("responseStatus");
////				if (reponseStatus.equals("In room!")) {
////					currentChatRoom = new ChatRoom(json.getString("chatRoomName"), new User(json.getString("hostId")));
////					loggedIn = true;
////					if (DEBUG) { 
////						System.out.println("=================================");
////						System.out.println("currentChatRoom created : [ " + currentChatRoom.getRoomName() + " ] , host name : [ " + currentChatRoom.gethostID() + " ]");
////						System.out.println("=================================");
////					}
////				}
////				else {	// If creating new chat room failed, re update chat room lists information on lobbyPanel
////					JSONArray jsonArray = json.getJSONArray("chatRoomList");
////					for (int i = 0, size = jsonArray.size();jsonArray != null && i < size; i++) {
////					  JSONObject objectInArray = jsonArray.getJSONObject(i);
////					  if (objectInArray != null) {
////						  String curChatRoomName = objectInArray.getString("chatRoomName");
////						  String curChatRoomHostName = objectInArray.getString("hostId");
////						  if (DEBUG) {
////							  System.out.println("=================================");
////							  System.out.println("[ " + reponseStatus + " ]");
////							  System.out.println("Room (  " + i + "  ) : ");
////							  System.out.println("    current valid chat room name : " + curChatRoomName);
////							  System.out.println("    current valid chat room host name : " + curChatRoomHostName + "\n");
////							  System.out.println("=================================");
////						  }
////						  ChatRoom curChatRoom = new ChatRoom(curChatRoomName, new User(curChatRoomHostName));
////						  currentValidChatRoomList.add(numChatRooms , curChatRoom);
////						  numChatRooms++;
////						  loggedIn = false;
////					  }
////					}
////				}
////			}
////			else {
////				System.out.println("decodedString : " + decodedString);
////			}
////			//========================================================================
////			//========================================================================
////			in.close();
//			conn.disconnect();
//		} catch (MalformedURLException e1) {
//			e1.printStackTrace();
//			System.out.println("MalformedURLException: " + e1);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//			System.out.println("IOException: " + e1);
//		}
//
//		
//		System.exit(1);
		
	    //-------------------------------------------------
		// create a game
	    //-------------------------------------------------
		SpaceInvaders.frame = new JFrame("Space Invaders");
	    frame.setLayout(null);      // set the layout manager
	    frame.setSize(W+ChatPanel.W,H);
	    Toolkit tool = frame.getToolkit();
	    Dimension screenSize = tool.getScreenSize();
	    int width = screenSize.width;
	    int height = screenSize.height;
	    System.out.println("width = " + width + " height = " + height);
	    frame.setBounds((width/3),(height/4), W+ChatPanel.W, H);
	    ImageIcon img = new ImageIcon("frameIcon.GIF");
	    frame.setIconImage(img.getImage());
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.getContentPane().setBackground(Color.BLACK);
	    SpaceInvaders gamePanel = new SpaceInvaders();
	    
	    //-------------------------------------------------
		// show login panel
	    //-------------------------------------------------
	    if (LOGIN_PANEL_ON) {
		    loginPanel = new LoginPanel();
		    loginPanel.setBounds(0, 0, loginPanel.W, loginPanel.H);
		    frame.add(loginPanel, new Integer(20), 0);
		    loginPanel.setVisible(true);
	    }
	    
	    // 아래의 panel들과 각종 thread들은 성공적으로 login되었을때, SpaceInvaders.online을
	    // LoginPanel에서 true로 만들면서, 그때 만들어지게 된다.
	    chatPanel = null;
	    inventoryPanel = null;
	    slotsPanel = null;
	    shopPanel = null;
	    slotsUIPanel = null;
	    
//	    //-------------------------------------------------
//	    // show chat panel
//	    //-------------------------------------------------
//	    chatPanel = new ChatPanel();
//	    chatPanel.setBounds(0, 600, chatPanel.W, chatPanel.H);
//	    frame.add(chatPanel, new Integer(11), 0);
//
//	    //-------------------------------------------------
//	    // show inventory panel
//	    //-------------------------------------------------
//	    inventoryPanel = new InventoryPanel();
//	    inventoryPanel.setBounds(0, 0, inventoryPanel.W, inventoryPanel.H);
//	    inventoryPanel.setVisible(false);
//	    frame.add(inventoryPanel, new Integer(10), 0);
//
//	    //-------------------------------------------------
//	    // show slots panel
//	    //-------------------------------------------------
//	    slotsPanel = new SlotsPanel();
//	    slotsPanel.setBounds(0, 0, slotsPanel.W, slotsPanel.H);
//	    slotsPanel.setVisible(false);
//	    frame.add(slotsPanel, new Integer(10), 0);
//	    
//	    //-------------------------------------------------
//	    // show shop panel
//	    //-------------------------------------------------
//	    shopPanel = new ShopPanel(); 
//	    shopPanel.setBounds(0, 0, shopPanel.W, shopPanel.H);
//	    shopPanel.setVisible(false);
//	    frame.add(shopPanel, new Integer(15), 0);
//
//	    //-------------------------------------------------
//	    // Add "Slots UI(Graphic)" panel
//	    //-------------------------------------------------
//	    slotsUIPanel = new SlotsUIPanel();
//	    slotsUIPanel.setBounds(10, 600, slotsUIPanel.W, slotsUIPanel.H);
//	    frame.add(slotsUIPanel, new Integer(5), 0);
//	    slotsUIPanel.setVisible(true);

	    //-------------------------------------------------
		// show frame
	    //-------------------------------------------------
	    SetLook.lookAndFeel();
		frame.setVisible(true);
	}
	
	public static void turnOnPanels () {
	    //-------------------------------------------------
	    // show inventory panel
	    //-------------------------------------------------
	    inventoryPanel = new InventoryPanel();
	    inventoryPanel.setBounds(0, 0, inventoryPanel.W, inventoryPanel.H);
	    inventoryPanel.setVisible(false);
	    frame.add(inventoryPanel, new Integer(10), 0);

	    //-------------------------------------------------
	    // show slots panel
	    //-------------------------------------------------
	    slotsPanel = new SlotsPanel();
	    slotsPanel.setBounds(0, 0, slotsPanel.W, slotsPanel.H);
	    slotsPanel.setVisible(false);
	    frame.add(slotsPanel, new Integer(10), 0);
	    
	    //-------------------------------------------------
	    // show shop panel
	    //-------------------------------------------------
	    shopPanel = new ShopPanel(); 
	    shopPanel.setBounds(0, 0, shopPanel.W, shopPanel.H);
	    shopPanel.setVisible(false);
	    frame.add(shopPanel, new Integer(15), 0);

	    //-------------------------------------------------
	    // Add "Slots UI(Graphic)" panel
	    //-------------------------------------------------
	    slotsUIPanel = new SlotsUIPanel();
	    slotsUIPanel.setBounds(W+7, ChatPanel.H + 8, slotsUIPanel.W, slotsUIPanel.H);
    	System.out.println("SlotsUIPanel: getX():" + slotsUIPanel.getX() + " getY():" + slotsUIPanel.getY());

	    frame.add(slotsUIPanel, new Integer(5), 0);
	    slotsUIPanel.setVisible(true);
	    
	    //-------------------------------------------------
	    // show chat panel
	    //-------------------------------------------------
	    chatPanel = new ChatPanel();
	    chatPanel.setBounds(W, 0, chatPanel.W, chatPanel.H);
	    frame.add(chatPanel, new Integer(0), 0);
	    chatPanel.setVisible(true);
	    chatPanel.setBounds(W+100, 0, chatPanel.W, chatPanel.H);
	    chatPanel.setBounds(W, 0, chatPanel.W, chatPanel.H);
	}
}

class SetLook
{
    public static void lookAndFeel()
    {
        try {
            	String s = UIManager.getSystemLookAndFeelClassName();
            	UIManager.setLookAndFeel(s);
            }
        catch(Exception e){}
    }
}