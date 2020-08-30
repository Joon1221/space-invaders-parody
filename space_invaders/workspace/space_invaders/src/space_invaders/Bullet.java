package space_invaders;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Graphics;
import java.net.MalformedURLException;
import java.net.URL;

public class Bullet {
	public static final int SPACESHIP_BULLET_WIDTH = 2;
	public static final int SPACESHIP_BULLET_HEIGHT = 15;
	public static final double SPACESHIP_BULLET_X_SPEED = 0.0;
	public static final double SPACESHIP_BULLET_Y_SPEED = 16.0;
	public static final int SPACESHIP_BULLET_X_DIR= 0;
	public static final int SPACESHIP_BULLET_Y_DIR = -1;
	public static final Color SPACESHIP_BULLET_COLOR = Color.white;
	public static final String SPACESHIP_BULLET_SHOOT_SOUND_FILENAME = "assets/Laser-SoundBible.com-602495617.wav";
	
	public static final int ENEMY_BULLET_WIDTH = 2;
	public static final int ENEMY_BULLET_HEIGHT = 15;
	public static final double ENEMY_BULLET_X_SPEED = 0.0;
	public static final double ENEMY_BULLET_Y_SPEED = 8.0;
	public static final int ENEMY_BULLET_X_DIR= 0;
	public static final int ENEMY_BULLET_Y_DIR = 1;
	public static final Color ENEMY_BULLET_COLOR = Color.yellow;
	public static final String ENEMY_BULLET_SHOOT_SOUND_FILENAME = "assets/Laser-SoundBible.com-602495617.wav";

	public int width;
	public int height;
	
	public Color color;

	public int xDir;
	public int yDir;
	public double xSpeed;
	public double ySpeed;

	public int shooterW; // to calculate the start pos
	public int shooterH; // to calculate the start pos

	public double x;
	public double y;

	public boolean alive;
	
	public AudioClip shootSound;

	public Bullet() {
		init(0, 0, 0, 0, 0, 0, Color.white, (AudioClip)null, 0, 0);
	}

	public Bullet(int width, int height,
			   double xSpeed, double ySpeed,
			   int xDir, int yDir,
			   Color color, String shootSoundFilename,
			   int shooterW, int shooterH) {
		AudioClip newShootSound = null;
		try {
			URL url = (new java.io.File(shootSoundFilename)).toURI().toURL();
			newShootSound = Applet.newAudioClip(url);
        } catch(MalformedURLException murle) {
        	murle.printStackTrace();
        }
		init(width, height, xSpeed, ySpeed, xDir, yDir, color, newShootSound, shooterW, shooterH);
	}

	public Bullet(int width, int height,
			   double xSpeed, double ySpeed,
			   int xDir, int yDir,
			   Color color, AudioClip shootSound,
			   int shooterW, int shooterH) {
		init(width, height, xSpeed, ySpeed, xDir, yDir, color, shootSound, shooterW, shooterH);
	}

	public void init(int width, int height,
		   double xSpeed, double ySpeed,
		   int xDir, int yDir,
		   Color color, AudioClip shootSound,
		   int shooterW, int shooterH) {
		this.width = width;
		this.height = height;
		
		this.color = color;
		this.shootSound = shootSound;
		
		this.shooterW = shooterW;
		this.shooterH = shooterH;
		
		x = -1;
		y = -1;
		
		this.xSpeed = xSpeed;
		this.ySpeed = ySpeed;
		
		this.xDir = xDir;
		this.yDir = yDir;

		alive = false;
	}
	
	public void paint(Graphics g) {
		if (alive) {
			g.setColor(color);
			g.fillRect((int)x, (int)y, width, height);
		}
	}
	
	public void shoot(int shooterX, int shooterY) {
    	if (!alive) {
    		x = shooterX + shooterW/2 - width/2;
    		y = shooterY + height * yDir;
        	alive = true;
        	shootSound.play();
    	}
	}
	
	public void move() {
    	if (alive) {
			if (yDir == -1 && y < 0 - height) {
				alive = false;
			}
			else if (yDir == 1 && y > SpaceInvaders.H) {
				alive = false;
			}
			else {
				y += yDir * ySpeed;
			}
			
			if (SpaceInvaders.isHit(x, y + height)) {
				alive = false;
				SpaceInvaders.numLives--;
				System.out.println("Bullet::move(): AlienInvaders.numLives = " + SpaceInvaders.numLives);
				if (SpaceInvaders.numLives <= 0) {
					System.out.println("Bullet::move(): if (AlienInvaders.numLives <= 0) {");
					SpaceInvaders.gameOver = true;
				}
			}
		}
	}
}
