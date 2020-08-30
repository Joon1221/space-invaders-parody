package shared;

public class EmptyItem extends Item {
	public EmptyItem() {
		super();
	}

	public EmptyItem(String name, String className, int price, String imageFileName, String description, String uuid) {
		super(name, className, price, imageFileName, description, uuid);
	}
	
	public void use() {
	}
}
