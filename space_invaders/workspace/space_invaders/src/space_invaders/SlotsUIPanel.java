package space_invaders;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import shared.Item;

public class SlotsUIPanel extends Canvas implements MouseListener, MouseMotionListener {
	public static final int W = 286;
	public static final int H = 50;
	
	public static final int SLOT_W = 46;
	public static final int SLOT_H = 46;

	public static final int NUM_SLOTS = 5;

	public static final int[] SLOT_X = {2, 61, 120, 179, 238};
	public static final int[] SLOT_Y = {2,  2,   2,   2,   2};

	public BufferedImage background;
//	public BufferedImage imgEnemy;
	
	// panel을 drag할 때 필요.
	public int clickedX;
	public int clickedY;
	
	public SlotsUIPanel() {
		init();
	}
	
	public void init() {
		
		addMouseListener(this);
		addMouseMotionListener(this);
		
		String imageFileName = "src/space_invaders/../../assets/slots_ui_panel/slots_ui_bg.png";
	    try {
	    	background = ImageIO.read(new File(imageFileName));
	    } catch (IOException e) {
	    	System.out.println("error: failed to open issac image file: " + imageFileName);
	    	System.exit(1);
	    }
	    
//	    String enemyImageFileName = "src/space_invaders/../../assets/items/double_point_item.png";
//	    try {
//	    	imgEnemy = ImageIO.read(new File(enemyImageFileName));
//	    } catch (IOException e) {
//	    	System.out.println("error: failed to open issac image file: " + enemyImageFileName);
//	    	System.exit(1);
//	    }
	}
	
	public void paint(Graphics g) { // render()
		g.drawImage(background, 0, 0, this);
		//g.drawImage(imgEnemy, 2, 2, this);
		
		for (int i = 0; i < SpaceInvaders.slot.capacity(); i++) {
			Item curItem = SpaceInvaders.slot.getItemAt(i);
			
			if (curItem != null) {
				g.drawImage(curItem.image, SLOT_X[i], SLOT_Y[i], this);
			}
		}
	}
	
	public void update() {
		
    }
	//==================================================================================================== Mouse listener methods
    //==================================================================================================== Mouse listener methods
    //==================================================================================================== Mouse listener methods
    // Mouse listener methods
    //==================================================================================================== Mouse listener methods
    //==================================================================================================== Mouse listener methods
    //==================================================================================================== Mouse listener methods

    @Override
    public void mouseDragged(MouseEvent e) {
    	setBounds(getX() - clickedX + e.getPoint().x, getY() - clickedY + e.getPoint().y, W, H);
    	System.out.println("SlotsUIPanel::mouseDragged()");
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point point = e.getPoint();
		clickedX = e.getPoint().x;
		clickedY = e.getPoint().y;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}
