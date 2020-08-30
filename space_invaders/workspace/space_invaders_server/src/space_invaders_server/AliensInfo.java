package space_invaders_server;

public class AliensInfo {
	public static final double SYNC_X_DIST_TO_ADD = 90.0;

	public static final char ALIVE = 'T';
	public static final char DEAD = 'F';
	
	// 화면에 그릴 장소.
	public int topLeftX;
	public int topLeftY;

	public int xDir;
	public double xSpeed;

	public int enemiesStartY;

	// aliens부대에서 row와 col의 size.
	public int rowSize;
	public int colSize;

	public String map;
	
	public AliensInfo() {
		topLeftX = 0;
		topLeftY = 0;
		
		xDir = 0;
		xSpeed = 0.0;
		
		enemiesStartY = 0;

		rowSize = 0;
		colSize = 0;
		
		map = "";
	}
	
	public AliensInfo(int topLeftX, int topLeftY, int xDir, double xSpeed, int enemiesStartY, int rowSize, int colSize, String map) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;

		this.xDir = xDir;
		this.xSpeed = xSpeed;
		
		this.enemiesStartY = enemiesStartY;

		this.rowSize = rowSize;
		this.colSize = colSize;

		this.map = map;
	}
}
