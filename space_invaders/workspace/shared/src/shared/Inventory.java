package shared;

import java.util.Vector;

public class Inventory {
	private Vector<Item> items;
	
	public Inventory() {
		items = new Vector<Item>();
	}
	
	public void addItem(Item item) {
		items.add(item);
	}

	public void removeItem(Item item) {
		int indexToRemove = items.indexOf(item);
		if (indexToRemove != -1) {
			items.removeElementAt(indexToRemove);
		}
	}
	
	public void removeAllItems() {
		items.clear();
	}

	public Item getItemAt(int index) {
		return (Item)items.get(index);
	}
	
	public int size() {
		return items.size();
	}
	
	public Item[] dump() {
		Item[] result = new Item[items.size()];
		for (int i = 0; i < items.size(); i++) {
			result[i] = (Item)items.get(i);
		}
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		Inventory otherInv = (Inventory)o;
		
		if (items.size() != otherInv.items.size()) {
			return false;
		}

		for (int i = 0; i < items.size(); i++) {
			if (!items.get(i).equals(otherInv.items.get(i))) {
				return false;
			}
		}

		return true;
	}
	
    public boolean equalsToItemsInfo(ItemsInfo itemsInfo) {
        if (items.size() != itemsInfo.numItems) {
            return false;
        }

        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).uuid.equals(itemsInfo.items[i].uuid)) {
                return false;
            }
        }

        return true;
    }
}
