package space_invaders;

public class AliensInfo {
	public static final double SYNC_X_DIST_TO_ADD = 90.0;
	public static final double SYNC_X_DIST_TO_ADD_INC_RATE_PER_DEAD_ENEMY = 12.0;
	
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

	public double syncXDistToAdd;

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
	
	public void initMapByAliensArray(int topLeftX, int topLeftY, int xDir, double xSpeed, int enemiesStartY, int rowSize, int colSize, Enemy[][] enemies) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;

		this.xDir = xDir;
		this.xSpeed = xSpeed;
		
		this.enemiesStartY = enemiesStartY;
		
		this.rowSize = rowSize;
		this.colSize = colSize;

		map = "";
		
		for (int i = 0; i < rowSize; i++) {
			for (int j = 0; j < colSize; j++) {
				if (enemies[i][j].enemyAlive) {
					map += ALIVE;
				}
				else { // if (!enemies[i][j].enemyAlive) {
					map += DEAD;
				}
			}
		}
		
		syncXDistToAdd = SYNC_X_DIST_TO_ADD;
	}
	
	// 주어진 enemies에 죽은 enemies들의 정보와 위치 정보등을 셋해준다.
	// 일단 주어진 enemies는 현재 enemies와 그 rowSize와 colSize와 같다고 가정하자. 
	public int readMapByAliensArray(Enemy[][] enemies) {
		// [ player2가 접속 직후에 player1이 조금 앞서나간것을 sync하기 위한 "조금 더 진행된것처럼"이라는 앨거리듬 ]
		// 1. 받은 enemy중에 xDir에 따라 가장 far right이나 left을 발견.
		// 2. 그 enemy에 조금 더 앞서간.. 예를 들면 100pixel을 해당 xDir로 더해서, 만약 벽과 부딪히지 않으면, topLeftX에 그대로 해당 dir를 더하거나 빼면 된다.
		// 3. 만약, 벽에 부딪히게 되면. 반사되어 튀어나온 정도의 거리를 topLeftX에 적용한다.
		// 4. 그후에 이전의 코드처럼 그냥 topLeftX를 이용하여 모든 alien의 위치를 계산하면 된다.
		
    	int numDeadEnemy = 0;
	    for (int i = 0; i < enemies.length; i++) {
		    for (int j = 0; j < enemies[0].length; j++) {
		    	if (!enemies[i][j].enemyAlive) {
		    		numDeadEnemy++;
		    	}
		    }
	    }
	    
//	    syncXDistToAdd = SYNC_X_DIST_TO_ADD + (SYNC_X_DIST_TO_ADD_INC_RATE_PER_DEAD_ENEMY * numDeadEnemy);
	    syncXDistToAdd = SYNC_X_DIST_TO_ADD + (numDeadEnemy * numDeadEnemy * 1.17);

		if (xDir > 0) { // 오른쪽으로 가고 있을 경우..
			boolean found = false;
			int farRightRow = -1;
			int farRightCol = -1;

			// 맨 오른쪽 alien을 찾는다.
		    for (int j = enemies[0].length - 1; j >= 0 && !found; j--) {
			    for (int i = 0; i < enemies.length && !found; i++) {
			    	if (enemies[i][j].enemyAlive) {
			    		farRightRow = i;
			    		farRightCol = j;
			    	}
			    }
			}
			
		    // 찾았다면(사실은 다 죽었을리는 없다. 하지만 가능성은 없다.)
		    if (found) {
		    	if (enemies[farRightRow][farRightCol].lblEnemyX + Enemy.ENEMY_WIDTH + syncXDistToAdd < SpaceInvaders.W) {
		    		topLeftX += (int)syncXDistToAdd;
		    	}
		    	else {
		    		topLeftX += (int)(SpaceInvaders.W - (enemies[farRightRow][farRightCol].lblEnemyX + Enemy.ENEMY_WIDTH + syncXDistToAdd));
		    		topLeftY += Enemy.ENEMY_Y_SPEED;
		    	}
		    }
		}
		else { // 왼쪽으로 가고 있을 경우..
			boolean found = false;
			int farLeftRow = -1;
			int farLeftCol = -1;

			// 맨 왼쪽 alien을 찾는다.
		    for (int j = 0; j < enemies[0].length && !found; j++) {
			    for (int i = 0; i < enemies.length && !found; i++) {
			    	if (enemies[i][j].enemyAlive) {
			    		farLeftRow = i;
			    		farLeftCol = j;
			    	}
			    }
			}
			
		    // 찾았다면(사실은 다 죽었을리는 없다. 하지만 가능성은 없다.)
		    if (found) {
		    	if (enemies[farLeftRow][farLeftCol].lblEnemyX - syncXDistToAdd >= 0) {
		    		topLeftX -= (int)syncXDistToAdd;
		    	}
		    	else {
		    		topLeftX = (int)(syncXDistToAdd - enemies[farLeftRow][farLeftCol].lblEnemyX);
		    		topLeftY += Enemy.ENEMY_Y_SPEED;
		    	}
		    }
		}
		
	    int numEnemies = 0;

		enemies[0][0].lblEnemyXSpeed = xSpeed;
		enemies[0][0].enemiesStartY = enemiesStartY;

	    for (int i = 0; i < enemies.length; i++) {
		    for (int j = 0; j < enemies[0].length; j++) {
		    	if (map.charAt(i*colSize + j) == ALIVE) {
		    		enemies[i][j].enemyAlive = true;
		    		enemies[i][j].lblEnemyX = topLeftX + j * (Enemy.ENEMY_WIDTH + Enemy.ENEMY_GAP);
		    		enemies[i][j].lblEnemyY = topLeftY + i * (topLeftY + Enemy.ENEMY_HEIGHT + Enemy.ENEMY_GAP);
				    numEnemies++;
		    	}
		    	else {
		    		enemies[i][j].enemyAlive = false;
					enemies[i][j].lblEnemy.show(false);
		    	}
		    }
	    }
	    
	    return numEnemies;
	}
}
