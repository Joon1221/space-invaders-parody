package shared;

public class DoublePointItem extends Item {
	public DoublePointItem() {
		super();
	}

	public DoublePointItem(String name, String className, int price, String imageFileName, String description, String uuid) {
		super(name, className, price, imageFileName, description, uuid);
	}
	
	public void use() {
		if (!gameMain.getDoublePointActivated()) {
			gameMain.setDoublePointActivated(true);
		}
	}
}
