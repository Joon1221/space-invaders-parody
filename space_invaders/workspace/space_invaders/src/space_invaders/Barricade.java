package space_invaders;

import java.awt.Graphics;

public class Barricade {
	//-----------------------------------------------------
	// Barricade
	//-----------------------------------------------------
	public Block[][] barricade;
	
	public static final int NUM_BLOCKS_IN_COL = 4;
	public static final int NUM_BLOCKS_IN_ROW = 4;

	public static final int BARRICADE_WIDTH = Block.BLOCK_WIDTH * NUM_BLOCKS_IN_COL;

	public Barricade(int startX, int startY) {
	    barricade = new Block[NUM_BLOCKS_IN_ROW][NUM_BLOCKS_IN_COL];
	    for (int i = 0; i < barricade.length; i++) {
		    for (int j = 0; j < barricade[0].length; j++) {
		    	barricade[i][j] = new Block(j * (Block.BLOCK_WIDTH) + startX, i * (Block.BLOCK_HEIGHT) + startY, "src/space_invaders/../../assets/block.png");
		    }
	    }
	}
	
	public void reset() {
	    for (int i = 0; i < barricade.length; i++) {
		    for (int j = 0; j < barricade[0].length; j++) {
		    	barricade[i][j].setAlive(true);
		    }
	    }
	}
	
	public void paint(Graphics g) {
	    for (int i = 0; i < barricade.length; i++) {
		    for (int j = 0; j < barricade[0].length; j++) {
		    	barricade[i][j].paint(g);
		    }
	    }
	}
	
	public boolean detectCollision(int otherX, int otherY, int otherW, int otherH) {
		boolean hit = false;
	    for (int i = 0; i < barricade.length && !hit; i++) {
		    for (int j = 0; j < barricade[0].length && !hit; j++) {
		    	if (barricade[i][j].isAlive() && barricade[i][j].isHit(otherX, otherY, otherW, otherH)) {
		    		barricade[i][j].setAlive(false);
		    		hit = true;
		    	}
		    }
	    }
	    return hit;
	}
}
