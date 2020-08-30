package shared;

public class RequestProtocol {
	//-------------------------------------------------------------------------
	// for SpaceInvadersServer
	//-------------------------------------------------------------------------
	public static final String OPERATION_LOGIN = "login";
	public static final String OPERATION_REGISTER = "register";
	public static final String OPERATION_UPDATE_SPACESHIP = "update_spaceship";
//	public static final String OPERATION_UPDATE_OTHER_SPACESHIP = "update_other_spaceship"; // OPERATION_UPDATE_SPACESHIP에서 그 response로 other spaceship의 info를 받기로 일단 결정.
	
	public static final String OPERATION_ALIEN_SYNCING_UPLOAD = "alien_syncing_upload";
	public static final String OPERATION_ALIEN_SYNCING_DOWNLOAD = "alien_syncing_download";
	public static final String OPERATION_ALIEN_SYNCING_DONE = "alien_syncing_done";
	
	public static final String OPERATION_UPDATE_ALIEN = "update_alien"; // 초기 sync(OPERATION_ALIEN_SYNCING_DONE)이후 죽은 alien당 한마리씩 업데이트.

	public static final String OPERATION_SHOP_UPDATE_ITEMS = "shop_update_items";
	public static final String OPERATION_SHOP_PURCHASE_ITEM = "shop_purchase_item";
	public static final String OPERATION_SHOP_SELL_ITEM_FROM_INVENTORY = "shop_sell_item_from_inventory";
	
	public static final String OPERATION_INVENTORY_UPDATE = "inventory_update";
	public static final String OPERATION_INVENTORY_ADD_FROM_SLOT = "inventory_add_from_slot"; // exactly same as shop_purchase_item for now
	public static final String OPERATION_INVENTORY_REMOVE = "inventory_remove"; // 그냥 item을 정리를 위해 destroy할 경우 필요.
	
	public static final String OPERATION_SLOT_UPDATE = "slot_update";
	public static final String OPERATION_SLOT_ADD_FROM_INVENTORY = "slot_add_from_inventory";
	public static final String OPERATION_SLOT_REMOVE = "slot_remove";

	//-------------------------------------------------------------------------
	// for SpaceInvadersChatServer
	//-------------------------------------------------------------------------
	public static final String OPERATION_CHAT_NEW_MESSAGE = "chat_new_message";
	public static final String OPERATION_CHAT_UPDATE_MESSAGES = "chat_update_messages";
	
	//-------------------------------------------------------------------------
	// Member Variables
	//-------------------------------------------------------------------------
	public String operation;
	public String id;
	public String pwd;
	public String message; // holds chat message or json packet  
    public String platform;
	
	//-------------------------------------------------------------------------
	// Methods(=Member Functions)
	//-------------------------------------------------------------------------
	public RequestProtocol() {
		this("", "", "", "", "");

	}

	public RequestProtocol(String operation, String id, String pwd, String message, String platform) {
		this.operation = operation;
		this.id = id;
		this.pwd = pwd;
		this.message = message;
		this.platform = platform;
	}
	
	public String toString() {
		return "operation: " + operation + " / id: " + id + " / pwd: " + pwd;
	}
}
