package space_invaders;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Spaceship {
	public double x;
	public double y;
	public int xDir;
	public int yDir;
	public double xSpeed;
	public double ySpeed;
	public int time;

	public boolean controlByKey; // key로 컨트롤하는 main spaceship만 true이고 상대방은 만들면서 꺼두어야 한다.
	public boolean painted;
	
//	public JLabel lblSpaceship;
//	public ImageIcon imgSpaceship;
	
	public BufferedImage image;

	//-----------------------------------------------------
	// Spaceship's Bullet
	//-----------------------------------------------------
	public Bullet bullet;
	
	public Spaceship() {
		x = 0.0;
		y = 0.0;
		xDir = 0;
		yDir = 0;
		xSpeed = 0.0;
		ySpeed = 0.0;
		time = 0;
		
		controlByKey = true;
		painted = false;

//		lblSpaceship = null;
//		imgSpaceship = null;
		image = null;
	}
	
	public Spaceship(double x, double y, int xDir, int yDir,
		double xSpeed, double ySpeed, int time, String spaceshipImageFilename, boolean controlByKey) {		
		this.x = x;
		this.y = y;
		this.xDir = xDir;
		this.yDir = yDir;
		this.xSpeed = xSpeed;
		this.ySpeed = ySpeed;
		this.time = time;
		
		this.controlByKey = controlByKey;
		painted = false;

//	    imgSpaceship = new ImageIcon(spaceshipImageFilename);
//	    lblSpaceship = new JLabel(imgSpaceship);
//	    lblSpaceship.setBounds((int)x, (int)y, SpaceInvaders.SPACESHIP_WIDTH, SpaceInvaders.SPACESHIP_HEIGHT);
//	    SpaceInvaders.frame.add(lblSpaceship , new Integer(1), 0);                     // add the label to the JFrame
//	    lblSpaceship.setVisible(true);
	    String imageFileNameToOpen = "src/space_invaders/../../" + spaceshipImageFilename;
	    try {
	    	image = ImageIO.read(new File(imageFileNameToOpen));
	    } catch (IOException e) {
	    	System.out.println("error: failed to open image file: " + imageFileNameToOpen);
	    	System.exit(1);
	    }
	}
	
	public void reset(double x, double y, int xDir, int yDir,
		double xSpeed, double ySpeed, int time, boolean controlByKey) {		
		this.x = x;
		this.y = y;
		this.xDir = xDir;
		this.yDir = yDir;
		this.xSpeed = xSpeed;
		this.ySpeed = ySpeed;
		this.time = time;

		this.controlByKey = controlByKey;
		painted = false;

//	    lblSpaceship.setBounds((int)x, (int)y, SpaceInvaders.SPACESHIP_WIDTH, SpaceInvaders.SPACESHIP_HEIGHT);
//	    lblSpaceship.setVisible(true);
	}

	public void paint(Graphics g) {
		//-------------------------------------------------
		// draw Spaceship
		//-------------------------------------------------
//		lblSpaceship.setBounds((int)x, (int)y, SpaceInvaders.SPACESHIP_WIDTH, SpaceInvaders.SPACESHIP_HEIGHT);
		g.drawImage(image, (int)x, (int)y, null);

		//-------------------------------------------------
		// draw bullet
		//-------------------------------------------------
		bullet.paint(g);
		painted = true;
	}
	
	public void update() {
		//-------------------------------------------------
		// move spaceship
		//-------------------------------------------------
		if (!SpaceInvaders.gameOver && x >= 0 && x <= SpaceInvaders.W-SpaceInvaders.SPACESHIP_WIDTH) {
			if (!controlByKey) {
				if (painted) {
    				// 만약 curOtherSpaceship을 빼는 동안, 다시 어디선가 한개가 더 add되어버리면, 같은 것을 두개를 뺄 가능성이 있다.
    				double xDiff = SpaceInvaders.otherSpaceshipCurX - x;
    				xDir = 0;
    				if (xDiff < 0) {
    					xDir = -1;
    				}
    				else if (xDiff > 0) {
    					xDir = 1;
    				}
    				
    				xDiff = Math.abs(xDiff);
    				
    				if (xDiff <= SpaceInvaders.SPACESHIP_X_SPEED) {
    					xSpeed = SpaceInvaders.SPACESHIP_X_SPEED;
    					x = SpaceInvaders.otherSpaceshipCurX;
    					xDir = 0;
    				}
    				else if (xDiff > xSpeed) {
    					xSpeed = SpaceInvaders.SPACESHIP_X_SPEED;
//    					System.out.println("else if (xDiff > otherSpaceship.xSpeed) {");
    				}
    				
//				    				int timeDiff = Math.abs(otherSpaceshipPrevTime - otherSpaceshipCurTime);
    				
//				    				otherSpaceship.xDir = otherSpaceshipCurXDir;
    				painted = false;
//					System.out.println("otherSpaceship.xSpeed = " + otherSpaceship.xSpeed);
				}
			}
			
			if (xDir == -1 && x-xSpeed < 0) {
				x = 0;
			}
			else if (xDir == 1 && x+xSpeed >= SpaceInvaders.W-SpaceInvaders.SPACESHIP_WIDTH) {
				x = SpaceInvaders.W-SpaceInvaders.SPACESHIP_WIDTH;
			}
			else {
				x += xDir * xSpeed;
			}
		}

		//-------------------------------------------------
		// shoot spaceship's bullet
		//-------------------------------------------------
		if (!SpaceInvaders.gameOver && controlByKey && SpaceInvaders.spaceshipKeyPressed) {
			bullet.shoot((int)x, (int)y);		
		}
		
		//-------------------------------------------------
		// move spaceship's bullet
		//-------------------------------------------------
		if (!SpaceInvaders.gameOver) {
			bullet.move();
		}
	}
	
//	public double x;
//	public double y;
//	public int xDir;
//	public int yDir;
//	public double xSpeed;
//	public double ySpeed;
		
	public String toJson() {
		String jsonString = "{";
		
		jsonString += "\"x\":" + x;
//		jsonString += ",\"y\":" + y;
		jsonString += ",\"xDir\":" + xDir;
//		jsonString += ",\"yDir\":" + yDir;
//		jsonString += ",\"xSpeed\":" + xSpeed;
//		jsonString += ",\"ySpeed\":" + ySpeed;
		jsonString += ",\"time\":" + time;

		
		jsonString += "}";
		
		return jsonString;
	}
	
	public static Spaceship fromJson(String jsonString) {
		Spaceship newSpaceship = new Spaceship();
		
//		String jsonString = "   { "x":10.0, "y" : 10    } ";
		jsonString = jsonString.trim();
//		String jsonString = "{ " ":"", "" : "  "    }";
		jsonString = jsonString.substring(1, jsonString.length()-1);
//		String jsonString = " " ":"", "" : "  "    ";
		StringTokenizer st = new StringTokenizer(jsonString, ",");
//		" " ":""
//		"" : "  "    ";

		while (st.hasMoreTokens()) {
			String curToken = st.nextToken();
			
			StringTokenizer keyAndVal = new StringTokenizer(curToken, ":");
			
			String key = keyAndVal.nextToken().trim();
			String val = keyAndVal.nextToken().trim();
			
			key = key.substring(1, key.length()-1);
			
			if (key.equals("x")) {
				newSpaceship.x = Double.parseDouble(val);
			}
//			else if (key.equals("y")) {
//				newSpaceship.y = Double.parseDouble(val);
//			}
			else if (key.equals("xDir")) {
				newSpaceship.xDir = Integer.parseInt(val);
			}
//			else if (key.equals("yDir")) {
//				newSpaceship.yDir = Integer.parseInt(val);
//			}
//			else if (key.equals("xSpeed")) {
//				newSpaceship.xSpeed = Double.parseDouble(val);
//			}
//			else if (key.equals("ySpeed")) {
//				newSpaceship.ySpeed = Double.parseDouble(val);
//			}
			else if (key.equals("time")) {
				newSpaceship.time = Integer.parseInt(val);
			}
			else {
				System.out.println("Spaceship::fromJson(): no such key = " + key);
				System.exit(1);
			}
		}
		
		return newSpaceship;
	}

	//-------------------------------------------------------------------------
	// (201605100939) x값과 xDir값만 넣고 빼는 것으로.. 고치기 위해서, 이전 버전을 백업.
	//-------------------------------------------------------------------------
//	public String toJson() {
//		String jsonString = "{";
//		
//		jsonString += "\"x\":" + x;
//		jsonString += ",\"y\":" + y;
//		jsonString += ",\"xDir\":" + xDir;
//		jsonString += ",\"yDir\":" + yDir;
//		jsonString += ",\"xSpeed\":" + xSpeed;
//		jsonString += ",\"ySpeed\":" + ySpeed;
//		
//		jsonString += "}";
//		
//		return jsonString;
//	}
//

//	public static Spaceship fromJson(String jsonString) {
//		Spaceship newSpaceship = new Spaceship();
//		
////		String jsonString = "   { "x":10.0, "y" : 10    } ";
//		jsonString = jsonString.trim();
////		String jsonString = "{ " ":"", "" : "  "    }";
//		jsonString = jsonString.substring(1, jsonString.length()-1);
////		String jsonString = " " ":"", "" : "  "    ";
//		StringTokenizer st = new StringTokenizer(jsonString, ",");
////		" " ":""
////		"" : "  "    ";
//
//		while (st.hasMoreTokens()) {
//			String curToken = st.nextToken();
//			
//			StringTokenizer keyAndVal = new StringTokenizer(curToken, ":");
//			
//			String key = keyAndVal.nextToken().trim();
//			String val = keyAndVal.nextToken().trim();
//			
//			key = key.substring(1, key.length()-1);
//			
//			if (key.equals("x")) {
//				newSpaceship.x = Double.parseDouble(val);
//			}
//			else if (key.equals("y")) {
//				newSpaceship.y = Double.parseDouble(val);
//			}
//			else if (key.equals("xDir")) {
//				newSpaceship.xDir = Integer.parseInt(val);
//			}
//			else if (key.equals("yDir")) {
//				newSpaceship.yDir = Integer.parseInt(val);
//			}
//			else if (key.equals("xSpeed")) {
//				newSpaceship.xSpeed = Double.parseDouble(val);
//			}
//			else if (key.equals("ySpeed")) {
//				newSpaceship.ySpeed = Double.parseDouble(val);
//			}
//			else {
//				System.out.println("Spaceship::fromJson(): no such key = " + key);
//				System.exit(1);
//			}
//		}
//		
//		return newSpaceship;
//	}
}
