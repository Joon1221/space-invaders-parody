package space_invaders;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Enemy {
	public static final double ENEMY_START_X = 0;
	public static final double ENEMY_START_Y = 50;
	public static final int ENEMY_WIDTH = 48;
	public static final int ENEMY_HEIGHT = 36;
	public static final double ENEMY_X_SPEED_INC = 0.15;
	public static final double ENEMY_X_SPEED = 0.75;
//	public static final double ENEMY_X_SPEED = 2.25;
	public static final double ENEMY_Y_SPEED = ENEMY_HEIGHT/2.0;

	public static final double ENEMY_ANIM_SPEED = 15;

	public static final double ENEMY_GAP = 5;

	public static final double ENEMY_SCORE = 5;

	public JLabel lblEnemy;
	public int curImageIcon;
	public ImageIcon[] imgEnemy;
	
	public int row; // aliens무리들의 2D array에서의 row와 col.
	public int col;
	
	public boolean enemyAlive;
	
	public static int enemiesStartY; // 전체 enemies의 맨 위쪽 위치로 여기에서부터 lblEnemyY가 더해져서 그려진다.
	public double lblEnemyX;
	public double lblEnemyY;
	
	public static int lblEnemyXDir;
	public static int lblEnemyYDir;
	public static double lblEnemyXSpeed;
	public static double lblEnemyYSpeed;
	
	public Bullet bullet;
	public static final long ENEMY_SHOOT_INTERVAL_MIN = 4000;
	public static final long ENEMY_SHOOT_INTERVAL_MAX = 7000;
	
//	long estimatedTime = System.currentTimeMillis() - startTime;
	public long randomShootingInterval;
	public long lastShootTimeMillis;
	
	public Enemy() {
	    this(-1, -1, 0, 0, null, null);
	}
	public Enemy(int row, int col, double lblEnemyX, double lblEnemyY, String imageFilename, String shootSoundFilename) {
		this.row = row;
		this.col = col;
		
	    enemyAlive = true;
	    
	    enemiesStartY = (int)ENEMY_START_Y;
	    
		this.lblEnemyX = lblEnemyX;
		this.lblEnemyY = lblEnemyY;
		lblEnemyXDir = 1;
		lblEnemyYDir = 0;
		lblEnemyXSpeed = ENEMY_X_SPEED;
		lblEnemyYSpeed = ENEMY_Y_SPEED;

		imgEnemy = new ImageIcon[2];
		imgEnemy[0] = new ImageIcon(imageFilename + "_01.png");
	    imgEnemy[1] = new ImageIcon(imageFilename + "_02.png");
	    lblEnemy = new JLabel(imgEnemy[0]);
	    curImageIcon = 0;
	    lblEnemy.setBounds((int)lblEnemyX, enemiesStartY + (int)lblEnemyY, ENEMY_WIDTH, ENEMY_HEIGHT);
	    
	    bullet = new Bullet(Bullet.ENEMY_BULLET_WIDTH, Bullet.ENEMY_BULLET_HEIGHT,
				Bullet.ENEMY_BULLET_X_SPEED, Bullet.ENEMY_BULLET_Y_SPEED,
				Bullet.ENEMY_BULLET_X_DIR, Bullet.ENEMY_BULLET_Y_DIR,
				Bullet.ENEMY_BULLET_COLOR, Bullet.ENEMY_BULLET_SHOOT_SOUND_FILENAME,
				ENEMY_WIDTH, ENEMY_HEIGHT);
	    
	    lastShootTimeMillis = -1;
	}
	
	public void reset(double lblEnemyX, double lblEnemyY) {
	    enemyAlive = true;
	    
	    enemiesStartY = (int)ENEMY_START_Y;
	    
		this.lblEnemyX = lblEnemyX;
		this.lblEnemyY = lblEnemyY;
		lblEnemyXDir = 1;
		lblEnemyYDir = 0;
		lblEnemyXSpeed = ENEMY_X_SPEED;
		lblEnemyYSpeed = ENEMY_Y_SPEED;
		
	    curImageIcon = 0;
	    lblEnemy.setBounds((int)lblEnemyX, enemiesStartY + (int)lblEnemyY, ENEMY_WIDTH, ENEMY_HEIGHT);

	    lastShootTimeMillis = -1;
	    
		bullet.alive = false;
	}
	
	public void move() {
		if (SpaceInvaders.curFrames % ENEMY_ANIM_SPEED == 0) {
			if (curImageIcon == 0) { 
				lblEnemy.setIcon(imgEnemy[1]);
				curImageIcon = 1;
			}
			else {
				lblEnemy.setIcon(imgEnemy[0]);
				curImageIcon = 0;
			}
		}
		
		if (enemyAlive &&
			!(lblEnemyX + lblEnemyXDir * lblEnemyXSpeed >= 0 &&
			  lblEnemyX + lblEnemyXDir * lblEnemyXSpeed <= SpaceInvaders.W - ENEMY_WIDTH)) {
			lblEnemyXDir *= -1;
			enemiesStartY += ENEMY_Y_SPEED;
		}
		lblEnemyX += lblEnemyXDir * lblEnemyXSpeed;
		
		if (enemyAlive) {
			lblEnemy.setBounds((int)lblEnemyX, (int)(lblEnemyY + enemiesStartY), ENEMY_WIDTH, ENEMY_HEIGHT);
		}
	}
	
	public boolean isHit(double bulletX, double bulletY) {
		return  bulletX >= lblEnemyX &&
				bulletX < lblEnemyX + ENEMY_WIDTH &&
				bulletY >= lblEnemyY + enemiesStartY &&
			    bulletY < lblEnemyY + enemiesStartY + ENEMY_HEIGHT;
	}

	public boolean isInSpaceship(double spaceshipX, double spaceshipY, double spaceshipW, double spaceshipH) {
		return 	(lblEnemyX >= spaceshipX &&
				 lblEnemyX < spaceshipX + spaceshipW &&
				 lblEnemyY >= spaceshipY &&
				 lblEnemyY < spaceshipY + spaceshipH) ||
				(lblEnemyX + ENEMY_WIDTH >= spaceshipX &&
				 lblEnemyX + ENEMY_WIDTH < spaceshipX + spaceshipW &&
				 lblEnemyY >= spaceshipY &&
				 lblEnemyY < spaceshipY + spaceshipH) ||
				(lblEnemyX >= spaceshipX &&
				 lblEnemyX < spaceshipX + spaceshipW &&
				 lblEnemyY + enemiesStartY + ENEMY_HEIGHT >= spaceshipY &&
				 lblEnemyY + enemiesStartY + ENEMY_HEIGHT < spaceshipY + spaceshipH) ||
				(lblEnemyX + ENEMY_WIDTH >= spaceshipX &&
				 lblEnemyX + ENEMY_WIDTH < spaceshipX + spaceshipW &&
				 lblEnemyY + enemiesStartY + ENEMY_HEIGHT >= spaceshipY &&
				 lblEnemyY + enemiesStartY + ENEMY_HEIGHT < spaceshipY + spaceshipH);
	}
	
	public boolean detectCollisionBySpaceship(double spaceshipX, double spaceshipY, double spaceshipW, double spaceshipH) {
		return  isHit(spaceshipX, spaceshipY) || 
				isHit(spaceshipX + spaceshipW - 1, spaceshipY) ||
				isHit(spaceshipX, spaceshipY + spaceshipH - 1) ||
				isHit(spaceshipX + spaceshipW - 1, spaceshipY + spaceshipH - 1) ||
				isInSpaceship(spaceshipX, spaceshipY, spaceshipW, spaceshipH);
	}

	public void shoot() {
		if (enemyAlive && !bullet.alive) {
			if (lastShootTimeMillis == -1) {
				lastShootTimeMillis = System.currentTimeMillis();
				randomShootingInterval = SpaceInvaders.r.nextInt((int)ENEMY_SHOOT_INTERVAL_MAX);
				return;
			}
			
			long estimatedTime = System.currentTimeMillis() - lastShootTimeMillis;
			if (estimatedTime > randomShootingInterval) {
				bullet.shoot((int)lblEnemyX, (int)(lblEnemyY + enemiesStartY));
				lastShootTimeMillis = System.currentTimeMillis();
				randomShootingInterval = SpaceInvaders.r.nextInt((int)ENEMY_SHOOT_INTERVAL_MAX - (int)ENEMY_SHOOT_INTERVAL_MIN) + (int)ENEMY_SHOOT_INTERVAL_MIN;
			}
		}
	}

	public void moveBullet() {
		bullet.move();
	}
}
