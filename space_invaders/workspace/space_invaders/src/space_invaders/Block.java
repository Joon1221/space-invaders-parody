package space_invaders;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Block {
	public static final int BLOCK_WIDTH = 30;
	public static final int BLOCK_HEIGHT = 30;
	
	public static Image image = null;

	public int width;
	public int height;
	
	public double x;
	public double y;
	
	boolean alive;
	
	Block () {
		this(0,0, null);
	}
	
	Block (double x, double y, String imageFilename) {
		this.x = x;
		this.y = y;
		
		if (image == null) {
			image = SpaceInvaders.getImage(imageFilename);
		}
		
		width = BLOCK_WIDTH;
		height = BLOCK_HEIGHT;
		
		alive = true;
	}
	public boolean isHit(double bulletX, double bulletY) {
		return  bulletX >= x &&
				bulletX < x + BLOCK_WIDTH &&
				bulletY >= y &&
			    bulletY < y + BLOCK_HEIGHT;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	
	public void paint(Graphics g) {
		if (alive) {
			g.drawImage(image, (int)x, (int)y, null);
		}
	}
	
	public boolean isOtherInBlock(int otherX, int otherY, int otherW, int otherH) {
		return 	(x <= otherX && // check other's top-left corner is in block 
				 x + width > otherX &&
				 y <= otherY &&
				 y + height > otherY) ||
				(x <= otherX + otherW - 1 && // check other's top-right corner is in block 
				 x + width > otherX + otherW - 1 &&
				 y <= otherY &&
				 y + height > otherY) ||
				(x <= otherX && // check other's bottom-left corner is in block 
				 x + width > otherX &&
				 y <= otherY + otherH - 1 &&
				 y + height > otherY + otherH - 1) ||
				(x <= otherX + otherW - 1 && // check other's bottom-right corner is in block 
				 x + width > otherX + otherW - 1 &&
				 y <= otherY + otherH - 1 &&
				 y + height > otherY + otherH - 1);
	}
	
	public boolean isBlockInOther(int otherX, int otherY, int otherW, int otherH) {
		return 	(otherX <= x && // check other's top-left corner is in block 
				 otherX + otherW > x &&
				 otherY <= y &&
				 otherY + otherH > y) ||
				(otherX <= x + width - 1 && // check other's top-right corner is in block 
				 otherX + otherW > x + width - 1 &&
				 otherY <= y &&
				 otherY + otherH > y) ||
				(otherX <= x && // check other's bottom-left corner is in block 
				 otherX + otherW > x &&
				 otherY <= y + height - 1 &&
				 otherY + otherH > y + height - 1) ||
				(otherX <= x + width - 1 && // check other's bottom-right corner is in block 
				 otherX + otherW > x + width - 1 &&
				 otherY <= y + height - 1 &&
				 otherY + otherH > y + height - 1);
	}
	
	public boolean isHit(int otherX, int otherY, int otherW, int otherH) {
		return isOtherInBlock(otherX, otherY, otherW, otherH) || // check whether other object in me or not
			   isBlockInOther(otherX, otherY, otherW, otherH); // check whether this object(block) is in other object.
	}
}
