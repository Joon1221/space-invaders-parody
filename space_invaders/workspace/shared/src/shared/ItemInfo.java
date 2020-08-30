package shared;

public class ItemInfo {
	
	public String name; // 실제 아이템 이름. space가 허용됨.
	public String className;
	public int price;
	public String imageFileName;
	public String description;
	public String uuid;

	public ItemInfo() {
		name = "NO NAME ITEM";
	}
	
	public ItemInfo(String name, String className, int price, String imageFileName, String description, String uuid) {
		this.name = name;
		this.className = className;
		this.price = price;
		this.imageFileName = imageFileName;
		this.description = description;
		this.uuid = uuid;
	}
	
    public ItemInfo(Item item) {
        name = item.name;
        className = item.className;
        price = item.price;
        imageFileName = item.imageFileName;
        description = item.description;
        uuid = item.uuid;
    }
	
	public String toString() {
		return name;
	}
	
	public Item createItem() {
	    return Item.createItemObj(this);
	}
}
