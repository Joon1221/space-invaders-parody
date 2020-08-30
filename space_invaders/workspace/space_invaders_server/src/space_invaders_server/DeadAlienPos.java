package space_invaders_server;

public class DeadAlienPos {
	public int row;
	public int col;
	
	public DeadAlienPos() {
		row = -1;
		col = -1;
	}

	public DeadAlienPos(int row, int col) {
		this.row = row;
		this.col = col;
	}
}
