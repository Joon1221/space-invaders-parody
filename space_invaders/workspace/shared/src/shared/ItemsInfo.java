package shared;

public class ItemsInfo {
    public int numItems;
	public ItemInfo[] items;
	
	public ItemsInfo(int capacity) {
	    numItems = capacity;
		items = new ItemInfo[numItems];
	}
	
	public String toString() {
		String s = "";
		for (int i = 0; i < numItems; i++) {
			s += items[i].toString();
		}
		return s;
	}
	
	public static Inventory createInventory(ItemsInfo itemsInfo) {
        Inventory newInv = new Inventory();

        for (int i = 0; i < itemsInfo.numItems; i++) {
            newInv.addItem(Item.createItemObj(itemsInfo.items[i]));
        }
        
        return newInv;
	}
}
