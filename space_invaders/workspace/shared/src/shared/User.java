package shared;

public class User {
	public int userNumID; // database에 등록할 때 주어지는 고유 숫자 id이다. inventory table등에서 string으로 된 id대신에 사용되어진다.
	public String id; // login할 때 사용하는 string으로 된 id.
//	public String pwd; // 여기에 저장하지 않도록.
	
	public int gold;
	public int level;
	public int exp;
	public int high_score;
	public int num_played;
	public int num_win;
	public int num_tie;
	
	public String spaceshipID; // 이것은 server쪽에 접속한 spaceship의 순서대로 주는 id이다.

	public User() {
		userNumID = -1;
		id = "";
		
		gold = 0;
		level = 0;
		exp = 0;
		high_score = 0;
		num_played = 0;
		num_win = 0;
		num_tie = 0;
		
		spaceshipID = "-1";
	}

	public User(int userNumID, String id, int gold, int level, int exp, int high_score, int num_played, int num_win, int num_tie, String spaceshipID) {
		this.userNumID = userNumID;
		this.id = id;
		
		this.gold = gold;
		this.level = level;
		this.exp = exp;
		this.high_score = high_score;
		this.num_played = num_played;
		this.num_win = num_win;
		this.num_tie = num_tie;
		
		this.spaceshipID = spaceshipID;
	}
}
