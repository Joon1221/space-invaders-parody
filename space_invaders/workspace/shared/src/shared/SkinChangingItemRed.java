package shared;

import java.awt.Color;

public class SkinChangingItemRed extends Item {
	private Color color; 

	public SkinChangingItemRed() {
		super();
		this.color = new Color(255, 0, 0); // default color = red
	}

	public SkinChangingItemRed(String name, String className, int price, String imageFileName, String description, String uuid) {
		super(name, className, price, imageFileName, description, uuid);
		this.color = new Color(255, 0, 0); // default color = red
	}

	public String toString() {
		return name;
	}
	
	public void use() {
		System.out.println(name);
	}
}
