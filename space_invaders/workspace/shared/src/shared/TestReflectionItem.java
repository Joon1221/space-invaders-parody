package shared;

public class TestReflectionItem {
	public static void main(String[] args) {
		ItemInfo itemInfo = new ItemInfo("Double Point Item", "DoublePointItem", 10, "double_point_item.png", "Double Point Item", "");
		Item item = Item.createItemObj(itemInfo);
		System.out.println(item);

		// 아래처럼 성공적으로 출력됨.
//		Item::Item(): error - failed to open image file: src/space_invaders/../../assets/items/double_point_item.png
//		Double Point Item
	}
}
