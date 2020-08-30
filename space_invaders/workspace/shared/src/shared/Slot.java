package shared;

import java.util.Vector;

public class Slot {
	public static final int CAPACITY = 5;
	
	public Item[] items;
	public int numItems;
	
	public Slot() {
		items = new Item[CAPACITY];
		numItems = 0;
		
		for (int i = 0; i < CAPACITY; i++) {
			items[i] = new EmptyItem();
		}
	}
	
	public int addItem(Item item) {
		if (numItems >= CAPACITY) {
			return -1;
		}
		
		for (int i = 0; i < CAPACITY; i++) {
			if (items[i] instanceof EmptyItem) {
				items[i] = item;
				// SpaceInvaders::updateSlot()등에서 EmptyItem을 add하는 경우도 있다.
				// 어차피 EmptyItem이 있는 빈 슬롯에 들어가기 때문에 상관은 없지만, 만약
				// EmptyItem이 add될 경우는, numItems는 증가되면 안 된다.
				if (!(item instanceof EmptyItem)) {
					numItems++;
				}
				return i;
			}
		}
		
		return -1;
	}

	public boolean insertItemAt(Item item, int index) {
		if (numItems >= CAPACITY) {
			return false;
		}
		
		if (index < 0 || index >= CAPACITY) {
			return false;
		}
		
		if (items[index] instanceof EmptyItem) {
			items[index] = item;
			// SpaceInvaders::updateSlot()등에서 EmptyItem을 add하는 경우도 있다.
			// 어차피 EmptyItem이 있는 빈 슬롯에 들어가기 때문에 상관은 없지만, 만약
			// EmptyItem이 add될 경우는, numItems는 증가되면 안 된다.
			if (!(item instanceof EmptyItem)) {
				numItems++;
			}
			return true;
		}

		
		return false;
	}

	public Item removeItemAt(int index) {
		if (index < 0 || index >= CAPACITY) {
			return null;
		}

		Item itemToRemove = items[index];
		if (!(itemToRemove instanceof EmptyItem)) {
			items[index] = new EmptyItem();
			numItems--;
		}
		return itemToRemove;
	}
	
	public void removeAllItems() {
		numItems = 0;

		for (int i = 0; i < CAPACITY; i++) {
			items[i] = new EmptyItem();
		}
	}

	public Item getItemAt(int index) {
		if (index < 0 || index >= CAPACITY) {
			return null;
		}

		return items[index];
	}
	
	public int size() {
		return numItems;
	}

	public int capacity() {
		return CAPACITY;
	}

	public Item[] dump() {
		return items;
	}
	
	public void useItemAt(int index) {
		if (!(items[index] instanceof EmptyItem)) {
			items[index].use();
			items[index] = null;
			numItems--;
		}
	}
}
