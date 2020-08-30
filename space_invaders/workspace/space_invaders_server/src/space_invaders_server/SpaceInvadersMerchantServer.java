package space_invaders_server;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import shared.DoublePointItem;
import shared.Inventory;
import shared.Item;
import shared.ItemInfo;
import shared.ItemsInfo;
import shared.RequestProtocol;
import shared.ResponseProtocol;
import shared.SkinChangingItemRed;
import shared.SkinChangingItemGreen;
import shared.SkinChangingItemBlue;
import shared.User;
import space_invaders.Enemy;
import space_invaders.SpaceInvaders;
import space_invaders.Spaceship;

import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.UUID;

//if (reqProt.operation.equals(RequestProtocol.OPERATION_SHOP_UPDATE_ITEMS)) {
//else if (reqProt.operation.equals(RequestProtocol.OPERATION_SHOP_PURCHASE_ITEM)) {
//else if (reqProt.operation.equals(RequestProtocol.OPERATION_INVENTORY_ADD_FROM_SLOT)) {
//else if (reqProt.operation.equals(RequestProtocol.OPERATION_SHOP_SELL_ITEM_FROM_INVENTORY) || 
//         reqProt.operation.equals(RequestProtocol.OPERATION_INVENTORY_REMOVE)) {
//else if (reqProt.operation.equals(RequestProtocol.OPERATION_INVENTORY_UPDATE)) {
//else if (reqProt.operation.equals(RequestProtocol.OPERATION_SLOT_UPDATE)) {
//else if (reqProt.operation.equals(RequestProtocol.OPERATION_SLOT_ADD_FROM_INVENTORY)) {
//else if (reqProt.operation.equals(RequestProtocol.OPERATION_SLOT_REMOVE)) {


@WebServlet("/SpaceInvadersMerchantServer")
public class SpaceInvadersMerchantServer extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static final int MAX_SESSION_TIME = 1;
    
    public static final String ID_KEY = "id";
       
    public static final int MAX_NUM_SPACESHIP_INFO = 50;
    
    public static final int MAX_NUM_SHOP_ITEMS = 10;
    
    // MySQL쪽에서 가져온 Empty를 제외한 item list(중복이 없고, uuid가 없음) 맨 처음 서버가 켜질때,
    // constructor에서 한번만 MySQL로부터 읽어온다.
    public static ItemsInfo itemTable;
    public static Item[] shopItems;

    
    public static Random r = new Random();
    
    /**
     * @see SpaceInvadersMerchantServer#SpaceInvadersMerchantServer()
     */
    public SpaceInvadersMerchantServer() {
        super();
        System.out.println("SpaceInvadersServer::SpaceInvadersServer(): started");
        
        //=====================================================================
        // MySQL에서 item table에서 Empty빼고 읽어들임.
        //=====================================================================
        System.out.println("SpaceInvadersServer::SpaceInvadersServer(): read item tabel from MySQL");

        Connection connection = null;
        PreparedStatement query = null;
        Statement stmt = null;
        
        boolean result = false;
        String respMessage = "";
        shopItems = null;


        try {
//            out.println("Create the driver instance.<br>");
            Class.forName("com.mysql.jdbc.Driver").newInstance();

//            out.println("Get the connection.<br>");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/space_invaders", "root", "qwe123"); // EC2
            
            query = connection.prepareStatement( "SELECT * FROM item WHERE name!=\'Empty\'");
            
            ResultSet rs = query.executeQuery();
            
            rs.last();
            int numRows = rs.getRow();
            rs.beforeFirst();
            System.out.println("SpaceInvadersServer::SpaceInvadersServer(): Number of rows: " + numRows);

            if (numRows > 0) { // 해당 id로 register된 유저가 있을 경우..
                itemTable = new ItemsInfo(numRows);
                for (int i = 0; i < numRows; i++) {
                    rs.next();
                    
                    itemTable.items[i] = new ItemInfo();
                
                    itemTable.items[i].name = rs.getString(2);
                    itemTable.items[i].className = rs.getString(3);
                    itemTable.items[i].price = rs.getInt(4);
                    itemTable.items[i].imageFileName = rs.getString(5);
                    itemTable.items[i].description = rs.getString(6);
                    itemTable.items[i].uuid = "";
                }
            }
            System.out.println("SpaceInvadersServer::SpaceInvadersServer(): itemTable ----------------------");
            System.out.println(itemTable);
            System.out.println("----------------------------------------------------------------------------");
        }
        catch (Exception e) {
            System.out.println(e.toString());
            System.exit(1);
        }
    }

    /**
     * @see SpaceInvadersMerchantServer#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        out.println("SpaceInvadersMerchantServer started!!");
    }

    /**
     * @see SpaceInvadersServer#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        java.util.Date date= new java.util.Date();
//        System.out.println(new Timestamp(date.getTime()));
        
        ServletContext application = getServletConfig().getServletContext();
        PrintWriter out = response.getWriter();

        String ipAddress = request.getHeader("X-FORWARDED-FOR");  
        if (ipAddress == null) {  
            ipAddress = request.getRemoteAddr();  
        }
//        System.out.println("SpaceInvadersServer: LOGIN: ip address = " + ipAddress);
       
        if (application.getAttribute("num_spaceships") == null) {
            application.setAttribute("num_spaceships", 0);
        }

        //=====================================================================
        // get RequestProtocol from json
        //=====================================================================
        StringBuilder jsonFromClient = new StringBuilder();
        String s;
        while ((s = request.getReader().readLine()) != null) {
            jsonFromClient.append(s);
        }
        
        System.out.println("Server: jsonFromClient = " + jsonFromClient);

        Gson gson = new Gson();

        RequestProtocol reqProt = gson.fromJson(jsonFromClient.toString(), RequestProtocol.class);
        
//        System.out.println("Server: reqProt.toString() = " + reqProt);
        
        //=====================================================================
        // credential: MySQL에서 login정보 확인.
        //=====================================================================
        Connection connection = null;
        PreparedStatement query = null;
        Statement stmt = null;
        
        boolean result = false;
        String respMessage = "";

        try {
//            out.println("Create the driver instance.<br>");
            Class.forName("com.mysql.jdbc.Driver").newInstance();

//            out.println("Get the connection.<br>");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/space_invaders", "root", "qwe123"); // EC2

            //-----------------------------------------------------------------
            // RequestProtocol.OPERATION_SHOP_UPDATE_ITEMS
            //-----------------------------------------------------------------
            if (reqProt.operation.equals(RequestProtocol.OPERATION_SHOP_UPDATE_ITEMS)) {
                System.out.println(" else if (reqProt.operation.equals(RequestProtocol.OPERATION_SHOP_UPDATE_ITEMS)) {");
                
                // (201610110940) 앨거리듬: shop에게 update시킬 item들을 random으로 만들기.
                //    1. mySql쪽에서 item table의 empty를 제외한 item list을 가져와서 ItemsInfo를 만든다.
                //    2. 위의 item list로부터 random shop items를 또 다른 ItemsInfo로 만들어낸다.
                
                
                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "" + true);
                        
                System.out.println("Server::OPERATION_SHOP_UPDATE_ITEMS: hello1");
                
                if (shopItems == null) {
                    System.out.println("Server::OPERATION_SHOP_UPDATE_ITEMS: hello1a");

                    shopItems = new Item[MAX_NUM_SHOP_ITEMS];
                    for (int i = 0; i < MAX_NUM_SHOP_ITEMS; i++) {
                        System.out.println("Server::OPERATION_SHOP_UPDATE_ITEMS: hello1b");
                        int num = r.nextInt(itemTable.items.length);
                        shopItems[i] = Item.createItemObj(itemTable.items[num]);
                    }
                }
                
                System.out.println("Server::OPERATION_SHOP_UPDATE_ITEMS: hello2");

                ItemsInfo itemsInfo = new ItemsInfo(MAX_NUM_SHOP_ITEMS);
                    
                System.out.println("Server::OPERATION_SHOP_UPDATE_ITEMS: hello3");

                for (int i = 0; i < MAX_NUM_SHOP_ITEMS; i++) {  
                    System.out.println("Server::OPERATION_SHOP_UPDATE_ITEMS: hello3 i=" + i);
                    itemsInfo.items[i] = shopItems[i].createItemInfo();
                }
         
                System.out.println("Server::OPERATION_SHOP_UPDATE_ITEMS: hello4");

//              String jsonString = gson.toJson(respProt);
//              String jsonString = "{\"operation\":\"shop_update_items\",\"id\":\"" + respProt.id + "\",\"result\":\"true\",\"info\":\"" + respProt.info + "\"}";
                respProt.info = gson.toJson(itemsInfo);
                
                String respJsonString = gson.toJson(respProt);
                
                if (reqProt.platform.equals("ios")) {
                    System.out.println("if (reqProt.platform.equals(\"ios\")) {");
                    
                    // 우선 ios에서 NSJSONSerialization를 이용하여 respProt를 다시 NSDictionary로 바꿀때,
                    // info의 string안에 추가로 \가 붙는 것을 방지하기 위해 떼어낸다.
                    respJsonString = respJsonString.replace("\\\"", "\"");
                    
                    // info의 데이터가 json이어야만 하므로, 그 앞과 맨 뒤에 붙은 "을 떼어내야 한다.
//                    String testString = "{\"operation\":\"inventory_update\",\"id\":\"3\",\"result\":\"true\",\"info\":\"{\"numItems\":2,\"items\":[{\"name\":\"Skin Changing Item Green\",\"className\":\"SkinChangingItemGreen\",\"price\":1,\"imageFileName\":\"skin_changing_item_green.png\",\"description\":\"Skin Changing Item Green\",\"uuid\":\"a1\"},{\"name\":\"Double Point Item\",\"className\":\"DoublePointItem\",\"price\":5,\"imageFileName\":\"double_point_item.png\",\"description\":\"Double Point Item\",\"uuid\":\"a2\"}]}\"}";

                    int indexFound = respJsonString.indexOf("info");
                    indexFound += 6;
                    respJsonString = respJsonString.substring(0, indexFound) + respJsonString.substring(indexFound + 1);
                    
                    respJsonString = respJsonString.substring(0, respJsonString.length() - 2) + respJsonString.substring(respJsonString.length() - 1);                    
                    System.out.println("ios::respJsonString: " + respJsonString);
//                    System.exit(1);
                }
                
                System.out.println("Server::OPERATION_SHOP_UPDATE_ITEMS: respJsonString = |||" + respJsonString + "|||");
                response.setContentType("application/json");
                out.println(respJsonString);
            }
            else if (reqProt.operation.equals(RequestProtocol.OPERATION_SHOP_PURCHASE_ITEM)) {
                // 아래는 client쪽에서 보내온 request json string
                // {"operation":"shop_purchase_item","id":"asd123","pwd":"","message":"Skin Changing Item Blue"}
                // usersOnline에 해당 id가 login되어 있다고 하면 login을 시키지 않는다.
                HashMap<String, User> usersOnline = (HashMap<String, User>)application.getAttribute("users_online");
                
                if (usersOnline == null) {
                    usersOnline = new HashMap<String, User>();
                    application.setAttribute("users_online", usersOnline);
                }
          
                if (!usersOnline.containsKey(reqProt.id)) {
                    result = false;
                    respMessage = "error: user with the id of " + reqProt.id + " does not exist!";
                }
                else {
                    // curUser에는 현재 유저가 login할 때, database로부터 retrieve한 user가 정보가 모두 들어있다.
                    User curUser = (User)usersOnline.get(reqProt.id);
                    
                    // 앨거리듬 설명: client가 purchase한 item을 해당 user의 inventory table에 add한다.

                    // 앨거리듬: Step1: 주어진 desc과 같은 item의 id를 space_invaders database의 item table에서 name column애서
                    //                찾아서(딱 한개의 record가 나오지 않으면 문) 그 id column을 얻어낸다.
                    query = connection.prepareStatement("SELECT * FROM item WHERE name=\'" + reqProt.message + "\'");
                    
                    ResultSet rsFromItem = query.executeQuery();
                    
                    rsFromItem.last();
                    int numRowsOfRsFromItem = rsFromItem.getRow();
                    rsFromItem.beforeFirst();
                    
                    if (numRowsOfRsFromItem != 1) { // error
                        result = false;
                        respMessage = "error: no such item exist. id = [" + reqProt.message + "]";
                    }
                    else {
                        result = true;
                        respMessage = "server: [" + reqProt.message + "] has been added successfully!";

                        rsFromItem.next();
                        int itemID = rsFromItem.getInt(1);
                        
                        // 앨거리듬: Step3: 찾아낸 user id와 item id, 새로 만든 랜덤 uuid로 inventory table에 add한다.
                        query = connection.prepareStatement("INSERT INTO `inventory` (`owner`, `item_id`, `uuid`) VALUES ('" + curUser.userNumID + "', '" + itemID + "', '" + UUID.randomUUID() + "')");
                        query.execute();
                    }
                }
                
                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "" + result);
                respProt.info = respMessage;
                String jsonString = gson.toJson(respProt);
                response.setContentType("application/json");
                out.println(jsonString);
                
                if (reqProt.operation.equals(RequestProtocol.OPERATION_SHOP_PURCHASE_ITEM)) {
                    // remove gold from the user ??????????????????????????????????????????????????????????????????????????????????????????? 
                }
            }
            else if (reqProt.operation.equals(RequestProtocol.OPERATION_INVENTORY_ADD_FROM_SLOT)) {
                // 아래는 client쪽에서 보내온 request json string
                // {"operation":"shop_purchase_item","id":"asd123","pwd":"","message":"Skin Changing Item Blue"}
                // usersOnline에 해당 id가 login되어 있다고 하면 login을 시키지 않는다.
                HashMap<String, User> usersOnline = (HashMap<String, User>)application.getAttribute("users_online");
                
                if (usersOnline == null) {
                    usersOnline = new HashMap<String, User>();
                    application.setAttribute("users_online", usersOnline);
                }
          
                if (!usersOnline.containsKey(reqProt.id)) {
                    result = false;
                    respMessage = "error: user with the id of " + reqProt.id + " does not exist!";
                }
                else {
                    // curUser에는 현재 유저가 login할 때, database로부터 retrieve한 user가 정보가 모두 들어있다.
                    User curUser = (User)usersOnline.get(reqProt.id);
                    
                    // 앨거리듬 설명: client가 purchase한 item을 해당 user의 inventory table에 add한다.

                    // 앨거리듬: Step1: 주어진 desc과 같은 item의 id를 space_invaders database의 item table에서 name column애서
                    //                찾아서(딱 한개의 record가 나오지 않으면 문) 그 id column을 얻어낸다.
                    query = connection.prepareStatement("SELECT * FROM slot WHERE uuid=\'" + reqProt.message + "\'");
                    
                    ResultSet rsFromSlot = query.executeQuery();
                    
                    rsFromSlot.last();
                    int numRowsOfRsFromSlot = rsFromSlot.getRow();
                    rsFromSlot.beforeFirst();
                    
                    if (numRowsOfRsFromSlot != 1) { // error
                        result = false;
                        respMessage = "error: no such item exist. id = [" + reqProt.message + "]";
                    }
                    else {
                        result = true;
                        respMessage = "server: [" + reqProt.message + "] has been added successfully!";

                        rsFromSlot.next();
                        int slotID = rsFromSlot.getInt(1);
                        int itemID = rsFromSlot.getInt(3);
                        
                        // 앨거리듬: Step3: 찾아낸 user id와 item id, 새로 만든 랜덤 uuid로 inventory table에 add한다.
                        query = connection.prepareStatement("INSERT INTO `inventory` (`owner`, `item_id`, `uuid`) VALUES ('" + curUser.userNumID + "', '" + itemID + "', '" + UUID.randomUUID() + "')");
                        query.execute();
                        
                        // 위에서 successfully item이 inventory에 add되면, 이제 slot에서 지울 것.
                        query = connection.prepareStatement("UPDATE `slot` SET `item_id`=0 WHERE `slot_id`=" + slotID);

                        query.execute();
                    }
                }
                    
                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "" + result);
                respProt.info = respMessage;
                String jsonString = gson.toJson(respProt);
                response.setContentType("application/json");
                out.println(jsonString);
                
                if (reqProt.operation.equals(RequestProtocol.OPERATION_SHOP_PURCHASE_ITEM)) {
                    // remove gold from the user ??????????????????????????????????????????????????????????????????????????????????????????? 
                }
            }
            else if (reqProt.operation.equals(RequestProtocol.OPERATION_SHOP_SELL_ITEM_FROM_INVENTORY) || 
                     reqProt.operation.equals(RequestProtocol.OPERATION_INVENTORY_REMOVE)) {
                HashMap<String, User> usersOnline = (HashMap<String, User>)application.getAttribute("users_online");
                
                if (usersOnline == null) {
                    usersOnline = new HashMap<String, User>();
                    application.setAttribute("users_online", usersOnline);
                }
          
                if (!usersOnline.containsKey(reqProt.id)) {
                    result = false;
                    respMessage = "error: user with the id of " + reqProt.id + " does not exist!";
                }
                else {
                    // curUser에는 현재 유저가 login할 때, database로부터 retrieve한 user가 정보가 모두 들어있다.
                    User curUser = (User)usersOnline.get(reqProt.id);
                    
                    // 앨거리듬 설명: client가 sell한 item을 해당 user의 inventory table에 remove한다.

                    // 앨거리듬: Step1: 주어진 desc과 같은 item의 id를 space_invaders database의 item table에서 name column애서
                    //                찾아서(딱 한개의 record가 나오지 않으면 문) 그 id column을 얻어낸다.
                    query = connection.prepareStatement("SELECT * FROM item WHERE name=\'" + reqProt.message + "\'");
                    
                    ResultSet rsFromItem = query.executeQuery();
                    
                    rsFromItem.last();
                    int numRowsOfRsFromItem = rsFromItem.getRow();
                    rsFromItem.beforeFirst();
                    
                    if (numRowsOfRsFromItem != 1) { // error
                        result = false;
                        respMessage = "error: no such item exist. id = [" + reqProt.message + "]";
                    }
                    else {
                        rsFromItem.next();
                        int itemID = rsFromItem.getInt(1);
                        
                        // 앨거리듬: Step3: 해당 owner의 숫자형 id와 item의 id로 inventory를 search하여 얻어낸 list중
                        //                맨 첫번째 아이템의 inv_id를 얻어낸다.
                        query = connection.prepareStatement( "SELECT * FROM inventory WHERE owner=\'" + curUser.userNumID + "\' and item_id=\'" + itemID + "\'");
                        
                        ResultSet rsFromInv = query.executeQuery();
                        
                        rsFromInv.last();
                        int numRows = rsFromInv.getRow();
                        rsFromInv.beforeFirst();
                       
                        if (numRows == 0) {
                            result = false;
                            respMessage = "error: no such item exist. id = [" + reqProt.message + "]";
                        }
                        else {
                            result = true;
                            respMessage = "server: [" + reqProt.message + "] has been sold successfully!";

                            rsFromInv.next();
                            int invID = rsFromInv.getInt(1);
                            
                            query = connection.prepareStatement("DELETE FROM inventory WHERE inv_id=" + invID);
                            query.execute();
                        }
                    }
                }
                
                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "" + result);
                respProt.info = respMessage;
                String jsonString = gson.toJson(respProt);
                response.setContentType("application/json");
                out.println(jsonString);
                
                if (reqProt.operation.equals(RequestProtocol.OPERATION_SHOP_SELL_ITEM_FROM_INVENTORY)) {
                    // add gold to the user ??????????????????????????????????????????????????????????????????????????????????????????? 
                }
            }
            else if (reqProt.operation.equals(RequestProtocol.OPERATION_INVENTORY_UPDATE)) {
                // usersOnline에 해당 id가 login되어 있다고 하면 login을 시키지 않는다.
                HashMap<String, User> usersOnline = (HashMap<String, User>)application.getAttribute("users_online");
                
                if (usersOnline == null) {
                    usersOnline = new HashMap<String, User>();
                    application.setAttribute("users_online", usersOnline);
                }

                // you can just use the spaceshipID to retrieve the user object from usersOnline(users_online)
                String jsonString = "";

                // 해당 id가 usersOnline에 없다는 것은, login이 안 되었다는 것이므로, login가능. 
                if (usersOnline.containsKey(reqProt.id)) {
                    result = true;

                    User user = usersOnline.get(reqProt.id);
                    
                    // Algorithm: setting players inventory after logging in
                    
                    // Step 1: use userNumID to retrieve all the items in that players inventory
                    
                    // Step 2: use the item id that was just retrieved to find the description of the item
                    
                    // Step 3: use the description to make an item
                    
                    // Step 4: add the item to the inventory
                    
                    System.out.println("hello0");
                    query = connection.prepareStatement("SELECT * FROM inventory WHERE owner=\'" + user.userNumID + "\'");
                    
                    ResultSet rsFromInv = query.executeQuery();
                    
                    rsFromInv.last();
                    int numRowsOfRsFromInv = rsFromInv.getRow();
                    rsFromInv.beforeFirst();
                    System.out.println("hello1");

                    if (numRowsOfRsFromInv > 0) {
                        ItemsInfo itemsInfo = new ItemsInfo(numRowsOfRsFromInv);
                        
                        for (int i = 0; i < numRowsOfRsFromInv; i++) {
                              rsFromInv.next();
                              
                              itemsInfo.items[i] = new ItemInfo();
                              
                              int itemID = rsFromInv.getInt(3);
                              String uuid = rsFromInv.getString(4);
                              
                              System.out.println("hello3: itemID = " + itemID);
                              
                              query = connection.prepareStatement("SELECT * FROM item WHERE id=" + itemID);
                              
                              ResultSet rsFromItem = query.executeQuery();
                              
                              rsFromItem.last();
                              int numRowsOfRsFromItem = rsFromItem.getRow();
                              rsFromItem.beforeFirst();
                              
                              rsFromItem.next();
                             
                              itemsInfo.items[i].name = rsFromItem.getString(2);
                              itemsInfo.items[i].className = rsFromItem.getString(3);
                              itemsInfo.items[i].price = rsFromItem.getInt(4);
                              itemsInfo.items[i].imageFileName = rsFromItem.getString(5);
                              itemsInfo.items[i].description = rsFromItem.getString(6);
                              itemsInfo.items[i].uuid = uuid;
                          }
                          jsonString = gson.toJson(itemsInfo);
                          System.out.println("jsonString = " + jsonString);
                    }
                    // no items in slot for the user
                    else {
                        result = true;
                        jsonString = "{\"numItems\":0, \"items\":\"\"}";
                    }
                }
                // 에러 처리해야함.
                else {
                    result = false;
                    jsonString = "error: user is not online";
                }
                
                //-------------------------------------------------------------
                // assemble ResponseProtocol
                //-------------------------------------------------------------
                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "" + result, jsonString);
                String respJsonString = gson.toJson(respProt);
                if (reqProt.platform.equals("ios")) {
                    System.out.println("if (reqProt.platform.equals(\"ios\")) {");
                    
                    // 우선 ios에서 NSJSONSerialization를 이용하여 respProt를 다시 NSDictionary로 바꿀때,
                    // info의 string안에 추가로 \가 붙는 것을 방지하기 위해 떼어낸다.
                    respJsonString = respJsonString.replace("\\\"", "\"");
                    
                    // info의 데이터가 json이어야만 하므로, 그 앞과 맨 뒤에 붙은 "을 떼어내야 한다.
//                    String testString = "{\"operation\":\"inventory_update\",\"id\":\"3\",\"result\":\"true\",\"info\":\"{\"numItems\":2,\"items\":[{\"name\":\"Skin Changing Item Green\",\"className\":\"SkinChangingItemGreen\",\"price\":1,\"imageFileName\":\"skin_changing_item_green.png\",\"description\":\"Skin Changing Item Green\",\"uuid\":\"a1\"},{\"name\":\"Double Point Item\",\"className\":\"DoublePointItem\",\"price\":5,\"imageFileName\":\"double_point_item.png\",\"description\":\"Double Point Item\",\"uuid\":\"a2\"}]}\"}";

                    int indexFound = respJsonString.indexOf("info");
                    indexFound += 6;
                    respJsonString = respJsonString.substring(0, indexFound) + respJsonString.substring(indexFound + 1);
                    
                    respJsonString = respJsonString.substring(0, respJsonString.length() - 2) + respJsonString.substring(respJsonString.length() - 1);                    
                    System.out.println("ios::respJsonString: " + respJsonString);
//                    System.exit(1);
                }

                response.setContentType("application/json");
                out.println(respJsonString);
            }
            else if (reqProt.operation.equals(RequestProtocol.OPERATION_SLOT_UPDATE)) {
                // usersOnline에 해당 id가 login되어 있다고 하면 login을 시키지 않는다.
                HashMap<String, User> usersOnline = (HashMap<String, User>)application.getAttribute("users_online");
                
                if (usersOnline == null) {
                    usersOnline = new HashMap<String, User>();
                    application.setAttribute("users_online", usersOnline);
                }

                // you can just use the spaceshipID to retrieve the user object from usersOnline(users_online)
                String jsonString = "";

                // 해당 id가 usersOnline에 없다는 것은, login이 안 되었다는 것이므로, login가능. 
                if (usersOnline.containsKey(reqProt.id)) {
                    result = true;

                    User user = usersOnline.get(reqProt.id);
                    
                    // Algorithm: setting players inventory after logging in
                    
                    // Step 1: use userNumID to retrieve all the items in that players inventory
                    
                    // Step 2: use the item id that was just retrieved to find the description of the item
                    
                    // Step 3: use the description to make an item
                    
                    // Step 4: add the item to the inventory
                    
                    System.out.println("hello0");
                    query = connection.prepareStatement("SELECT * FROM slot WHERE owner=\'" + user.userNumID + "\'");
                    
                    ResultSet rsFromSlot = query.executeQuery();
                    
                    rsFromSlot.last();
                    int numRowsOfRsFromSlot = rsFromSlot.getRow();
                    rsFromSlot.beforeFirst();
                    System.out.println("hello1");

                    if (numRowsOfRsFromSlot > 0) {
                        System.out.println("hello2");

                        String itemList = "{\"numItems\":" + numRowsOfRsFromSlot + ", \"items\":\"";
//                            result = true;
//                            respMessage = "server: [" + reqProt.message + "] has been added successfully!";
//
//                            rsFromInv.next();
//                            int itemID = rsFromInv.getInt(1);
//                            
//                            // 앨거리듬: Step3: 찾아낸 user id와 item id로 inventory table에 add한다.
//                            query = connection.prepareStatement("INSERT INTO `inventory` (`owner`, `item_id`) VALUES ('" + curUser.userNumID + "', '" + itemID + "')");
//                            query.execute()
                        for (int i = 0; i < numRowsOfRsFromSlot; i++) {
                            System.out.println("hello3: i = " + i);

                            rsFromSlot.next();
                            
                            int itemID = rsFromSlot.getInt(3);
                            String uuid = rsFromSlot.getString(4);
                            
                            System.out.println("hello3: itemID = " + itemID);
                            
                            query = connection.prepareStatement("SELECT * FROM item WHERE id=" + itemID);
                            
                            ResultSet rsFromItem = query.executeQuery();
                            
                            rsFromItem.last();
                            int numRowsOfRsFromItem = rsFromItem.getRow();
                            rsFromItem.beforeFirst();
                            
                            rsFromItem.next();
                           
                            itemList += "{\\\"name\\\":\\\"";
                            itemList += rsFromItem.getString(2);
                            itemList += "\\\", \\\"class_name\\\":\\\"";
                            itemList += rsFromItem.getString(3);
                            itemList += "\\\", \\\"price\\\":\\\"";
                            itemList += rsFromItem.getInt(4);
                            itemList += "\\\", \\\"image_file_name\\\":\\\"";
                            itemList += rsFromItem.getString(5);
                            itemList += "\\\", \\\"description\\\":\\\"";
                            itemList += rsFromItem.getString(6);
                            itemList += "\\\", \\\"uuid\\\":\\\"";
                            itemList += uuid;
                            itemList += "\\\"}";
                            
                            if (i < numRowsOfRsFromSlot-1) {
                                itemList += "; "; 
                            }
                        }
                        itemList += "\"}";
                        jsonString = itemList;
                        System.out.println("itemList = " + itemList);
                    }
                    // no items in slot for the user
                    else {
                        result = true;
                        jsonString = "{\"numItems\":0, \"items\":\"\"}";
                    }
                    
                    //-------------------------------------------------------------
                    // assemble ResponseProtocol
                    //-------------------------------------------------------------
                    ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "" + result, jsonString);
                    String respJsonString = gson.toJson(respProt);
                    
                    response.setContentType("application/json");
                    out.println(respJsonString);
                }
            }
            else if (reqProt.operation.equals(RequestProtocol.OPERATION_SLOT_ADD_FROM_INVENTORY)) {
                // {"item_desc":"Empty", "slot_index":3}
                
                // 아래는 client쪽에서 보내온 request json string
                // {"operation":"shop_purchase_item","id":"asd123","pwd":"","message":"{\"item_desc\":\"Empty\", \"slot_index\":3}"}
                // usersOnline에 해당 id가 login되어 있다고 하면 login을 시키지 않는다.
                HashMap<String, User> usersOnline = (HashMap<String, User>)application.getAttribute("users_online");
                System.out.println("slot1");
                if (usersOnline == null) {
                    usersOnline = new HashMap<String, User>();
                    application.setAttribute("users_online", usersOnline);
                }
                System.out.println("slot2");

                if (!usersOnline.containsKey(reqProt.id)) {
                    System.out.println("slot3");
                    result = false;
                    respMessage = "error: user with the id of " + reqProt.id + " does not exist!";
                }
                else {
                    System.out.println("slot4");
                    // curUser에는 현재 유저가 login할 때, database로부터 retrieve한 user가 정보가 모두 들어있다.
                    User curUser = (User)usersOnline.get(reqProt.id);
                    
                    String desc = "";
                    String slotIndex = "";

                    desc = reqProt.message.trim();
                    
                    System.out.println("slot4.5");

                    desc = desc.substring(1, desc.length()-1);
                    StringTokenizer itemsTokens = new StringTokenizer (desc, ",");
                    System.out.println("slot5");

                    while (itemsTokens.hasMoreTokens()) {
                        String curItemToken = itemsTokens.nextToken();
                        System.out.println("Client: curItemToken [" + curItemToken + "]");

                        StringTokenizer keyAndVal = new StringTokenizer(curItemToken, ":");
                        
                        String key = keyAndVal.nextToken().trim();
                        String val = keyAndVal.nextToken().trim();
                        
                        if (key.equals("\"item_desc\"")) { 
                            desc = val.substring(1, val.length()-1);
                        }
                        else if (key.equals("\"slot_index\"")) {
                            slotIndex = val.substring(0, val.length());
                        }
                    }
                    System.out.println("slot6");

                    query = connection.prepareStatement("SELECT * FROM item WHERE name=\'" + desc + "\'");
                    
                    ResultSet rsFromItem = query.executeQuery();
                    
                    rsFromItem.last();
                    int numRowsOfRsFromItem = rsFromItem.getRow();
                    rsFromItem.beforeFirst();
                    
                    int itemID = -1;
                    if (numRowsOfRsFromItem != 1) { // error
                        result = false;
                        respMessage = "error: no such item exist. desc = [" + desc + "]";
                        System.out.println(respMessage);
                    }
                    else {
                        rsFromItem.next();
                        itemID = rsFromItem.getInt(1);
                        System.out.println("itemID :" + itemID);

                    }
                    
                    query = connection.prepareStatement("SELECT * FROM slot WHERE `slot_index`=" + slotIndex + " and `owner`=" + curUser.userNumID);
                    
                    ResultSet rsFromSlot = query.executeQuery();
                    
                    rsFromSlot.last();
                    int numRowsOfRsFromSlot = rsFromSlot.getRow();
                    rsFromSlot.beforeFirst();
                    
                    int slotID = -1;
                    if (numRowsOfRsFromSlot != 1) { // error
                        result = false;
                        respMessage = "error: no such slot exist. slotIndex = [" + slotIndex + "] owner = [" + curUser.userNumID + "]";
                        System.out.println(respMessage);

                    }
                    else {
                        rsFromSlot.next();
                        slotID = rsFromSlot.getInt(1);
                        System.out.println("slotID :" + slotID);

                    }

                    if (itemID != -1 && slotID != -1) {
                        query = connection.prepareStatement("UPDATE `slot` SET `item_id`=" + itemID + "  WHERE `slot_id`=" + slotID);

                        query.execute();
                        System.out.println("query: " + "UPDATE `slot` SET `item_id`=" + itemID + "  WHERE `slot_id`=" + slotID);

                        System.out.println("updated done");
                        
                        result = true;
                    }
                }
                System.out.println("respMessage:" + respMessage);
//                System.exit(1);
                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "" + result);
                respProt.info = respMessage;
                String jsonString = gson.toJson(respProt);
                response.setContentType("application/json");
                out.println(jsonString);
                
                //-------------------------------------------------------------
                // add login information to session
                //-------------------------------------------------------------
//                // Create a session object if it is already not  created.
//                HttpSession session = request.getSession();
//                String id = reqProt.id;
//
//                // Check if this is new comer on your web page.
//                if (session.isNew()) {
//                    session.setMaxInactiveInterval(60000); 
//                    session.setAttribute(ID_KEY, id);
//                }
                
                //-------------------------------------------------------------
                // assemble ResponseProtocol
                //-------------------------------------------------------------
//                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, userID, "" + result);
//                String jsonString = gson.toJson(respProt);
//                
//                response.setContentType("application/json");
//                out.println(jsonString);
//                
////                HttpSession session2 = request.getSession();
////                System.out.println("SpaceInvadersServer: " + session2.getAttribute(ID_KEY));
//
//                if (result) { 
//                    Vector chatMessages = (Vector)application.getAttribute("chat_messages");
//                    if (chatMessages == null) {
//                        chatMessages = new Vector();
//                        application.setAttribute("chat_messages", chatMessages);
//                        chatMessages.add("Welcome! New Chat Room started!!");
//                    }
//    
//                    usersIPAddress.put(userID, ipAddress);
//                }
            }
            else if (reqProt.operation.equals(RequestProtocol.OPERATION_SLOT_REMOVE)) {
                HashMap<String, User> usersOnline = (HashMap<String, User>)application.getAttribute("users_online");
                
                if (usersOnline == null) {
                    usersOnline = new HashMap<String, User>();
                    application.setAttribute("users_online", usersOnline);
                }
          
                if (!usersOnline.containsKey(reqProt.id)) {
                    result = false;
                    respMessage = "error: user with the id of " + reqProt.id + " does not exist!";
                }
                else {
                    // curUser에는 현재 유저가 login할 때, database로부터 retrieve한 user가 정보가 모두 들어있다.
                    User curUser = (User)usersOnline.get(reqProt.id);
                    
                    // 앨거리듬 설명: client가 sell한 item을 해당 user의 inventory table에 remove한다.

                    // 앨거리듬: Step1: 주어진 desc과 같은 item의 id를 space_invaders database의 item table에서 name column애서
                    //                찾아서(딱 한개의 record가 나오지 않으면 문) 그 id column을 얻어낸다.
                
                    query = connection.prepareStatement("SELECT * FROM slot WHERE `slot_index`=" + reqProt.message + " and `owner`=" + curUser.userNumID);
                    
                    ResultSet rsFromSlot = query.executeQuery();
                    
                    rsFromSlot.last();
                    int numRowsOfRsFromSlot = rsFromSlot.getRow();
                    rsFromSlot.beforeFirst();
                    
                    int slotID = -1;
                    if (numRowsOfRsFromSlot != 1) { // error
                        result = false;
                        respMessage = "error: no such slot exist. slotIndex = [" + reqProt.message + "] owner = [" + curUser.userNumID + "]";
                        System.out.println(respMessage);

                    }
                    else {
                        rsFromSlot.next();
                        slotID = rsFromSlot.getInt(1);
                        System.out.println("slotID :" + slotID);
                    }
                    
                    System.out.println("slot remove :" + "UPDATE `slot` SET `item_id`=0 WHERE `slot_id`=" + slotID);

                    query = connection.prepareStatement("UPDATE `slot` SET `item_id`=0 WHERE `slot_id`=" + slotID);

                    query.execute();
                }
                
                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "" + result);
                respProt.info = respMessage;
                String jsonString = gson.toJson(respProt);
                response.setContentType("application/json");
                out.println(jsonString);
            }
            //-----------------------------------------------------------------
            // no such operations or malformed.
            //-----------------------------------------------------------------
            else {
                // assemble ResponseProtocol
                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, "", "" + result);
                respProt.info = "SpaceInvadersMerchantServer: error - no such operation exist!";
                String jsonString = gson.toJson(respProt);
                response.setContentType("application/json");
                out.println(jsonString);
            }
        } catch (Exception e)
        {
            out.println(e.toString()+"<br>");
        }
    }
}
