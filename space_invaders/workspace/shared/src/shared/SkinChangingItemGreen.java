package shared;

import java.awt.Color;

public class SkinChangingItemGreen extends Item {
	private Color color; 

	public SkinChangingItemGreen() {
		super();
		this.color = new Color(0, 255, 0); // default color = green
	}

	public SkinChangingItemGreen(String name, String className, int price, String imageFileName, String description, String uuid) {
		super(name, className, price, imageFileName, description, uuid);
		this.color = new Color(0, 255, 0); // default color = green
	}

	public String toString() {
		return name;
	}
	
	public void use() {
		System.out.println(name);
	}
}
