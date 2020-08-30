package shared;

import java.awt.Color;

public class SkinChangingItemBlue extends Item {
	private Color color; 

	public SkinChangingItemBlue() {
		super();
		this.color = new Color(0, 0, 255); // default color = blue
	}

	public SkinChangingItemBlue(String name, String className, int price, String imageFileName, String description, String uuid) {
		super(name, className, price, imageFileName, description, uuid);
		this.color = new Color(0, 0, 255); // default color = blue
	}


	public String toString() {
		return name;
	}
	
	public void use() {
		System.out.println(name);
	}
}
