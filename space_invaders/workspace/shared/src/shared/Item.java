package shared;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import javax.imageio.ImageIO;

// 20161011: os's proj로부터 uuid가 들어간 new concept의 진보된 아이템을 가져와서 고치면서, reflection개념을 적용해서
//           item을 무한 add해도 코드를 바꿀 필요없는 방식으로 바꾸었음.
public abstract class Item {
	public static Random r = new Random();
	
	public static final int NUM_ITEM_CLASSES = 4;
	
	public String name; // 실제 아이템 이름. space가 허용됨.
	public String className;
	public int price;
	public String imageFileName;
	public String description;
	public String uuid;

	public GameMain gameMain;
	
	public BufferedImage image;
	
	public Item() {
		name = "NO NAME ITEM";
		className = "";
		price = 0;
		imageFileName = "";
		description = "";
		uuid = "";
		image = null;
	}
	
	public Item (String name, String className, int price, String imageFileName, String description, String uuid) {
		this.name = name;
		this.className = className;
		this.price = price;
		this.imageFileName = imageFileName;
		this.description = description;
		this.uuid = uuid;

		// read in item images
		try {
			image = ImageIO.read(new File("src/space_invaders/../../assets/items/" + imageFileName));
		} catch (IOException e) {
	   		System.out.println("Item::Item(): error - failed to open image file: src/space_invaders/../../assets/items/" + imageFileName);
//	   		image = null;
//	   		System.exit(1); // exit하지 말 것, 왜냐하면, server쪽에서 그림없이 사용할 때가 있다.
		}
	}
	
	public String toString() {
		return name;
	}
	
	public void setGameMain(GameMain gameMain) {
		this.gameMain = gameMain;
	}
	
	public static Item createItemObj(ItemInfo itemInfo) {
	    Item newItem = null;
	    
	    try {
		    ClassLoader classLoader = Item.class.getClassLoader();
	        Class curItemClass = classLoader.loadClass("shared." + itemInfo.className);
	        // when we use default constructor to create a new object of the anonymous class type.
//	        Object anotherDynamicObject = curItemClass.newInstance();
	        
	        // when we use non default constructor to create a new object of the anonymous class type.
	        Constructor constructor = curItemClass.getConstructor(String.class, String.class, int.class, String.class, String.class, String.class);
	        newItem = (Item)constructor.newInstance(itemInfo.name, itemInfo.className, itemInfo.price, itemInfo.imageFileName, itemInfo.description, itemInfo.uuid);
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
    	} catch (InstantiationException e) {
    		e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return newItem;
	}
	
	public ItemInfo createItemInfo() {
	    return new ItemInfo(this);
	}
	
	@Override
	public boolean equals(Object o) {
		return uuid.equals(((Item)o).uuid);
	}
	
	public abstract void use();
}
