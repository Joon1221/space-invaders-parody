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

@WebServlet("/SpaceInvadersServer")
public class SpaceInvadersServer extends HttpServlet {
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
     * @see SpaceInvadersServer#SpaceInvadersServer()
     */
    public SpaceInvadersServer() {
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
     * @see SpaceInvadersServer#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        //out.println("hello");
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
            // RequestProtocol.OPERATION_LOGIN
            //-----------------------------------------------------------------
            if (reqProt.operation.equals(RequestProtocol.OPERATION_LOGIN)) {
                System.out.println("SpaceInvadersServer::doPost(): if (reqProt.operation.equals(RequestProtocol.OPERATION_LOGIN)) {");

                query = connection.prepareStatement( "SELECT * FROM users WHERE id=\'" + reqProt.id + "\' and pwd=\'" + reqProt.pwd + "\'");
                
                ResultSet rs = query.executeQuery();
                
                rs.last();
                int numRows = rs.getRow();
                rs.beforeFirst();
                System.out.println("Server: Number of rows: " + numRows);

                // usersOnline에 해당 id가 login되어 있다고 하면 login을 시키지 않는다.
                HashMap<String, User> usersOnline = (HashMap<String, User>)application.getAttribute("users_online");
                HashMap<String, String> usersIPAddress = (HashMap<String, String>)application.getAttribute("users_ip_address");
                
                if (usersOnline == null) {
                    usersOnline = new HashMap<String, User>();
                    application.setAttribute("users_online", usersOnline);
                }
                if (usersIPAddress == null) {
                    usersIPAddress = new HashMap<String, String>();
                    application.setAttribute("users_ip_address", usersIPAddress);
                }

                String spaceshipID = "";
                String jsonString = "";
                if (numRows == 1) { // 해당 id로 register된 유저가 있을 경우..
                    // 해당 id가 usersOnline에 없다는 것은, login이 안 되었다는 것이므로, login가능. 
                    System.out.println("SpaceInvadersServer::doPost():if (numRows == 1) {");
                    if (!usersOnline.containsKey(reqProt.id)) {
                        System.out.println("SpaceInvadersServer::doPost():if (!usersOnline.containsKey(reqProt.id)) {");

                        result = true;

                        spaceshipID = "" + (int)(application.getAttribute("num_spaceships"));
                        application.setAttribute("num_spaceships", (int)(application.getAttribute("num_spaceships")) + 1);
                        
                        rs.next();
                        
                        User newUser = new User();
                        newUser.userNumID = rs.getInt(1);
                        newUser.id = rs.getString(2);
                        newUser.gold = rs.getInt(4);
                        newUser.level = rs.getInt(5);
                        newUser.exp = rs.getInt(6);
                        newUser.high_score = rs.getInt(7);
                        newUser.num_played = rs.getInt(8);
                        newUser.num_win = rs.getInt(9);
                        newUser.num_tie = rs.getInt(10);
                        newUser.spaceshipID = spaceshipID;

                        usersOnline.put(spaceshipID, newUser);
                        System.out.println("SpaceInvadersServer::usersOnline.put(spaceshipID, newUser);");
                        System.out.println("                     newUser = " + newUser);
                        
                        // Algorithm: setting players inventory after logging in
                        
                        // Step 1: use userNumID to retrieve all the items in that players inventory
                        
                        // Step 2: use the item id that was just retrieved to find the description of the item
                        
                        // Step 3: use the description to make an item
                        
                        // Step 4: add the item to the inventory
                        
//                        System.out.println("hello0");
//                        query = connection.prepareStatement("SELECT * FROM inventory WHERE owner=\'" + newUser.userNumID + "\'");
//                        
//                        ResultSet rsFromInv = query.executeQuery();
//                        
//                        rsFromInv.last();
//                        int numRowsOfRsFromInv = rsFromInv.getRow();
//                        rsFromInv.beforeFirst();
//                        System.out.println("hello1");
//
//                        if (numRowsOfRsFromInv > 0) {
//                            System.out.println("hello2");
//
//                            String itemList = "{\"numItems\":" + numRowsOfRsFromInv + ", \"items\":\"";
////                            result = true;
////                            respMessage = "server: [" + reqProt.message + "] has been added successfully!";
////
////                            rsFromInv.next();
////                            int itemID = rsFromInv.getInt(1);
////                            
////                            // 앨거리듬: Step3: 찾아낸 user id와 item id로 inventory table에 add한다.
////                            query = connection.prepareStatement("INSERT INTO `inventory` (`owner`, `item_id`) VALUES ('" + curUser.userNumID + "', '" + itemID + "')");
////                            query.execute()
//                            for (int i = 0; i < numRowsOfRsFromInv; i++) {
//                                System.out.println("hello3: i = " + i);
//
//                                rsFromInv.next();
//                                
//                                int itemID = rsFromInv.getInt(3);
//                                System.out.println("hello3: itemID = " + itemID);
//                                
//                                query = connection.prepareStatement("SELECT * FROM item WHERE id=" + itemID);
//                                
//                                ResultSet rsFromItem = query.executeQuery();
//                                
//                                rsFromItem.last();
//                                int numRowsOfRsFromItem = rsFromItem.getRow();
//                                rsFromItem.beforeFirst();
//                                
//                                rsFromItem.next();
//                               
//                                itemList += "{\\\"desc\\\":\\\"";
//                                itemList += rsFromItem.getString(2);
//                                itemList += "\\\", \\\"imgFileName\\\":\\\"";
//                                itemList += rsFromItem.getString(4);
//                                itemList += "\\\"}";
//                                
//                                if (i < numRowsOfRsFromInv-1) {
//                                    itemList += "; "; 
//                                }
//                            }
//                            itemList += "\"}";
//                            jsonString = itemList;
//                            System.out.println("itemList = " + itemList);
//                            
//                        }
                    }
                }
                
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
//                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, spaceshipID, "" + result, jsonString);
                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, spaceshipID, "" + result, "");

                String respJsonString = gson.toJson(respProt);
                
                response.setContentType("application/json");
                out.println(respJsonString);
                
//                HttpSession session2 = request.getSession();
//                System.out.println("SpaceInvadersServer: " + session2.getAttribute(ID_KEY));

                if (result) { 
                    Vector chatMessages = (Vector)application.getAttribute("chat_messages");
                    if (chatMessages == null) {
                        chatMessages = new Vector();
                        application.setAttribute("chat_messages", chatMessages);
                        chatMessages.add("Welcome! New Chat Room started!!");
                    }
    
                    usersIPAddress.put(spaceshipID, ipAddress);
                }
            }
            
            //-----------------------------------------------------------------
            // RequestProtocol.OPERATION_REGISTER
            //-----------------------------------------------------------------
            else if (reqProt.operation.equals(RequestProtocol.OPERATION_REGISTER)) {
                stmt = connection.createStatement();
                
                String sql = "INSERT INTO users (id, pwd) VALUES (\'" + reqProt.id + "\', \'" + reqProt.pwd + "\')";
//                System.out.println("Server: sql = " + sql);
//                "INSERT INTO \`users\` (\`id\`, \`pwd\`) VALUES (\'asd123\', \'qwe123\');"
                int affectedRows = stmt.executeUpdate(sql);
                
                if (affectedRows == 0) {
                    result = false;
                }
                else {
                    result = true;
                }

                // assemble ResponseProtocol
                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "" + result);
                String jsonString = gson.toJson(respProt);
                
                response.setContentType("application/json");
                out.println(jsonString);
            }
            //-----------------------------------------------------------------
            // RequestProtocol.OPERATION_UPDATE_SPACESHIP
            //-----------------------------------------------------------------
            else if (reqProt.operation.equals(RequestProtocol.OPERATION_UPDATE_SPACESHIP)) {
                //????????????????????????????????????????????????????????????????????????????????????
                // client쪽에서.. SpaceInvaders(line. 346)로부터 보내오는 프로토콜을 처리하면 된다.
                // 처리하기 전에, 현재 플레이어의 움직임들을 저장할 큐를 저장할 HashMap을 application쪽에 만들고 시작해야 한다.(한번만)
//                System.out.println("SpaceInvadersServer::doPost(): reqProt.id: " + reqProt.id);
//
//                System.out.println("OPERATION_UPDATE_SPACESHIP SpaceInvadersServer::doPost(): reqProt.id: " + reqProt.id);
//                System.out.println("OPERATION_UPDATE_SPACESHIP SpaceInvadersServer::doPost(): reqProt.message: " + reqProt.message);

                Spaceship curFrame = Spaceship.fromJson(reqProt.message);
                
                // 플레이어들의 spaceship들의 현재 위치를 저장할 Queue를 저장할 hash map을 없다면 만들어둔다.
//                HashMap<String, Vector> spaceshipsInfo = (HashMap<String, Vector>)application.getAttribute("spaceships_info");
                HashMap<String, Spaceship> spaceshipsInfo = (HashMap<String, Spaceship>)application.getAttribute("spaceships_info");

                if (spaceshipsInfo == null) {
//                    spaceshipsInfo = new HashMap<String, Vector>();
                    spaceshipsInfo = new HashMap<String, Spaceship>();
                    application.setAttribute("spaceships_info", spaceshipsInfo);
                }
                
                // 만약 현재 보내오는 정보를 보내는 유저가 첫번째로 정보를 보낸다면, queue가 없을 것이므로, 새로 만든다음 사용.
                if (!spaceshipsInfo.containsKey(reqProt.id)) {
//                    spaceshipsInfo.put(reqProt.id, new Vector());
                    spaceshipsInfo.put(reqProt.id, new Spaceship());
                }
                
//                Vector spaceshipInfoQueue = spaceshipsInfo.get(reqProt.id);
//                if (spaceshipInfoQueue.size() >= MAX_NUM_SPACESHIP_INFO) {
//                    // 만약 queue가 꽉 찼다면, 가장 오래된 info를 지우고, 최신 info를 뒤에 add한다.
//                    spaceshipInfoQueue.remove(0);
//                }
//                spaceshipInfoQueue.add(curFrame);
                // 그냥 무조건 최신 프레임 한개만 덮어씌운다.
                spaceshipsInfo.put(reqProt.id, curFrame);
                
//                System.out.println("SpaceInvadersServer::doPost(): spaceshipInfoQueue: " + spaceshipInfoQueue);

                //-------------------------------------------------------------
                // 상대방의 최신 위치 정보를 response로 보낸다. -> (201605101003) 속도 개선을 위해서, 접속할 때, 고유 ID를 0과 1로 받은 다음. 내가 0이면 1의 정보를 빼보면 되고, 반대의 경우도 그렇게 하고..
                //                                                       접속하면 고유 ID를 0이나 1로 접속순으로 받게 하고, 그것을 protocol을 주고받을때 ID대신에 주고 받게 한다.
                //-------------------------------------------------------------
                String otherID = null;
//                
//                Iterator iterSpaceshipsInfoKeys = spaceshipsInfo.keySet().iterator();
//                while (iterSpaceshipsInfoKeys.hasNext()) {
//                    String curKey = (String)iterSpaceshipsInfoKeys.next();
//                    if (!curKey.equals(reqProt.id)) {
//                        otherID = curKey;
//                        break;
//                    }
//                }
                // (201605150653) 이제 귀찮게 찾을 필요없이 0이면 1, 1이면 0으로 otherID를 결정하면 된다.
                if (reqProt.id.equals("0")) {
                    otherID = "1";
                }
                else {
                    
                    // lag이 심하게 걸릴 때 잡아내기 위한 메시지. 나중에 다시 체크할 때, 켜고 사용할 것.
                    // 원인이 Apache와 Tomcat인지, Client인지 알아내려면, Client에서 보내오는 packet의 보낸 시간 stamp와 처리한 시간 stamp를 찍은 후, 비교하면..
                    // 누가 어떤 시간대에 일을 하지 않았는지 알 수 있다.
//                    System.out.println(new Timestamp(date.getTime()));

                    otherID = "0";
                }
                
                if (otherID != null) {
//                    Vector otherSpaceshipInfoQueue = spaceshipsInfo.get(otherID);
//                    Spaceship otherSpaceshipInfo = (Spaceship)otherSpaceshipInfoQueue.get(otherSpaceshipInfoQueue.size()-1);
                    Spaceship otherSpaceshipInfo = spaceshipsInfo.get(otherID);
                    // assemble ResponseProtocol
                    
//                    ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "" + true, otherSpaceshipInfo.toJson());
//                    String jsonString = gson.toJson(respProt);
                    String jsonString = "{\"operation\":\"update_spaceship\",\"id\":\"" + reqProt.id +
                            "\",\"result\":\"true\",\"info\":\"{\\\"x\\\":" + otherSpaceshipInfo.x + ",\\\"xDir\\\":" + otherSpaceshipInfo.xDir + ",\\\"time\\\":" + otherSpaceshipInfo.time + "}\"}";
                    
                    // (201605101006) 속도개선을 위해서 미리 조합된 json을 위의 244 대신에 사용.(아래의 정보 사용할 것)
//                    System.out.println("update_spaceship::jsonString for response = |" + jsonString + "|");
                    // jsonString = |{"operation":"update_spaceship","id":"qwe123","result":"true","info":"{\"x\":370.0,\"xDir\":0}"}|
                    
                    response.setContentType("application/json");
                    out.println(jsonString);
                }
                // 상대방 정보가 없을 경우는, 그에 맞는 메시지를 보낸다.
                else {
                    ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "" + false);
                    String jsonString = gson.toJson(respProt);
                    
                    response.setContentType("application/json");
                    out.println(jsonString);
                }
//                System.out.println("reqProt.id = " + reqProt.id);
            }
            else if (reqProt.operation.equals(RequestProtocol.OPERATION_ALIEN_SYNCING_UPLOAD)) {
//                System.out.println("OPERATION_ALIEN_SYNCING_UPLOAD" + reqProt.message);
//                System.exit(1);

                // 첫번째 플레이어가 alien info를 올렸다면, true함께 array정보를 보냄.
                AliensInfo aliensInfo = gson.fromJson(reqProt.message, AliensInfo.class);
                application.setAttribute("aliens_info", aliensInfo);
                
                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, "", "" + true);
                respProt.info = gson.toJson(aliensInfo);
                String jsonString = gson.toJson(respProt);
                response.setContentType("application/json");
                out.println(jsonString);
            }
            else if (reqProt.operation.equals(RequestProtocol.OPERATION_ALIEN_SYNCING_DOWNLOAD)) {
                System.out.println("else if (reqProt.operation.equals(RequestProtocol.OPERATION_ALIEN_SYNCING_DOWNLOAD)) {");
                // 첫번째 플레이어가 alien info를 올렸다면, true함께 array정보를 보냄.
                AliensInfo aliensInfo = (AliensInfo) application.getAttribute("aliens_info");
                if (aliensInfo != null) {
                    ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, "", "" + true);
                    respProt.info = gson.toJson(aliensInfo);
                    String jsonString = gson.toJson(respProt);
                    response.setContentType("application/json");
                    out.println(jsonString);
                    System.out.println("jsonString = " + jsonString);
                }
                else {
                    // still waiting for player 1 to send enemy information
                    ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, "", "" + false);
                    respProt.info = "none";
                    String jsonString = gson.toJson(respProt);
                    response.setContentType("application/json");
                    out.println(jsonString);
                    System.out.println("jsonString = " + jsonString);
                }
            }
            //-----------------------------------------------------------------
            // RequestProtocol.OPERATION_UPDATE_ALIEN
            //-----------------------------------------------------------------
            else if (reqProt.operation.equals(RequestProtocol.OPERATION_UPDATE_ALIEN)) {
//                System.out.println("OPERATION_UPDATE_ALIEN started");
                //????????????????????????????????????????????????????????????????????????????????????
                // client쪽에서.. SpaceInvaders(line. 346)로부터 보내오는 프로토콜을 처리하면 된다.
                // 처리하기 전에, 현재 플레이어의 움직임들을 저장할 큐를 저장할 HashMap을 application쪽에 만들고 시작해야 한다.(한번만)
//                System.out.println("SpaceInvadersServer::doPost(): reqProt.id: " + reqProt.id);
//
//                System.out.println("OPERATION_UPDATE_ALIEN SpaceInvadersServer::doPost(): reqProt.id: " + reqProt.id);
//                System.out.println("OPERATION_UPDATE_ALIEN SpaceInvadersServer::doPost(): reqProt.message: " + reqProt.message);

                DeadAlienPos curDeadAlienInfo = gson.fromJson(reqProt.message, DeadAlienPos.class);

//                System.out.println("OPERATION_UPDATE_ALIEN hello 1");

                // 플레이어들의 spaceship들의 현재 위치를 저장할 Queue를 저장할 hash map을 없다면 만들어둔다.
                HashMap<String, Queue<DeadAlienPos>> deadAliensInfo = (HashMap<String, Queue<DeadAlienPos>>)application.getAttribute("dead_aliens_info");

                if (deadAliensInfo == null) {
//                    System.out.println("OPERATION_UPDATE_ALIEN hello 1a");

                    deadAliensInfo = new HashMap<String, Queue<DeadAlienPos>>();
                    application.setAttribute("dead_aliens_info", deadAliensInfo);
                }

                if (curDeadAlienInfo.row != -1) {
//                    System.out.println("OPERATION_UPDATE_ALIEN hello 2");
                    System.out.println("OPERATION_UPDATE_ALIEN:receiving reqProt.id: " + reqProt.id + " curDeadAlienInfo.row = " + curDeadAlienInfo.row + " curDeadAlienInfo.col = " + curDeadAlienInfo.col);
    
                    // 만약 현재 보내오는 정보를 보내는 유저가 첫번째로 정보를 보낸다면, queue가 없을 것이므로, 새로 만든다음 사용.
                    if (!deadAliensInfo.containsKey(reqProt.id)) {
//                        System.out.println("OPERATION_UPDATE_ALIEN hello 2a");
    
                        deadAliensInfo.put(reqProt.id, new LinkedList<DeadAlienPos>());
                    }
    
//                    System.out.println("OPERATION_UPDATE_ALIEN hello 3");
    
                    Queue<DeadAlienPos> deadAliensQueue = deadAliensInfo.get(reqProt.id);
                    
//                    System.out.println("OPERATION_UPDATE_ALIEN hello 4");
    
                    deadAliensQueue.add(curDeadAlienInfo);
                }
//                System.out.println("OPERATION_UPDATE_ALIEN hello 5");

                //-------------------------------------------------------------
                // 상대방이 없앤 dead alien 한마리의 정보를 queue로부터 빼서 response로 보낸다.
                // -> (201605101003) 속도 개선을 위해서, 접속할 때, 고유 ID를 0과 1로 받은 다음. 내가 0이면 1의 정보를 빼보면 되고, 반대의 경우도 그렇게 하고..
                //                   접속하면 고유 ID를 0이나 1로 접속순으로 받게 하고, 그것을 protocol을 주고받을때 ID대신에 주고 받게 한다.
                //-------------------------------------------------------------
                String otherID = null;

                // (201605150653) 이제 귀찮게 찾을 필요없이 0이면 1, 1이면 0으로 otherID를 결정하면 된다.
                if (reqProt.id.equals("0")) {
                    otherID = "1";
                }
                else {
                    otherID = "0";
                }
                
//                System.out.println("OPERATION_UPDATE_ALIEN hello 6");

                if (otherID != null) {
//                    System.out.println("OPERATION_UPDATE_ALIEN if (otherID != null) {");

                    Queue<DeadAlienPos> otherDeadAliensQueue = deadAliensInfo.get(otherID);
                    
                    DeadAlienPos curOtherDeadAlien = otherDeadAliensQueue.peek();
                    String deadAlienInfo = ""; 
                    if (curOtherDeadAlien != null) {
                        otherDeadAliensQueue.remove();
                        deadAlienInfo = "{\\\"row\\\":" + curOtherDeadAlien.row + ", \\\"col\\\":" + curOtherDeadAlien.col + "}";
                        System.out.println("OPERATION_UPDATE_ALIEN:sending otherID: " + reqProt.id + " curOtherDeadAlien.row = " + curOtherDeadAlien.row + " curOtherDeadAlien.col = " + curOtherDeadAlien.col);
                    }
                    else {
                        deadAlienInfo = "{\\\"row\\\":-1, \\\"col\\\":-1}";
                    }
                    
                    // assemble ResponseProtocol
                    String jsonString = "{\"operation\":\"update_alien\",\"id\":\"" + reqProt.id +
                            "\",\"result\":\"true\",\"info\":\"" + deadAlienInfo + "\"}";
                    
                    // (201605101006) 속도개선을 위해서 미리 조합된 json을 위의 244 대신에 사용.(아래의 정보 사용할 것)
//                    System.out.println("OPERATION_UPDATE_ALIEN jsonString = |" + jsonString + "|");
                    // jsonString = |{"operation":"update_spaceship","id":"qwe123","result":"true","info":"{\"x\":370.0,\"xDir\":0}"}|
                    
                    response.setContentType("application/json");
                    out.println(jsonString);
                }
                // 상대방 정보가 없을 경우는, 그에 맞는 메시지를 보낸다.
                else {
//                    System.out.println("OPERATION_UPDATE_ALIEN if (otherID == null) {");

                    ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "" + false);
                    String jsonString = gson.toJson(respProt);
                    
                    response.setContentType("application/json");
                    out.println(jsonString);
                }
//                System.out.println("reqProt.id = " + reqProt.id); 
//                System.out.println("OPERATION_UPDATE_ALIEN end");
            }
            //-----------------------------------------------------------------
            // RequestProtocol.OPERATION_ALIEN_SYNCING_UPLOAD
            //-----------------------------------------------------------------
            else if (reqProt.operation.equals(RequestProtocol.OPERATION_ALIEN_SYNCING_UPLOAD)) {
                System.out.println("OPERATION_ALIEN_SYNCING_UPLOAD" + reqProt.message);
//                System.exit(1);

                // 첫번째 플레이어가 alien info를 올렸다면, true함께 array정보를 보냄.
                AliensInfo aliensInfo = gson.fromJson(reqProt.message, AliensInfo.class);
                application.setAttribute("aliens_info", aliensInfo);
                
                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, "", "" + true);
                respProt.info = gson.toJson(aliensInfo);
                String jsonString = gson.toJson(respProt);
                response.setContentType("application/json");
                out.println(jsonString);
            }
            //-----------------------------------------------------------------
            // RequestProtocol.OPERATION_ALIEN_SYNCING_DOWNLOAD
            //-----------------------------------------------------------------
            else if (reqProt.operation.equals(RequestProtocol.OPERATION_ALIEN_SYNCING_DOWNLOAD)) {
                // 첫번째 플레이어가 alien info를 올렸다면, true함께 array정보를 보냄.
                AliensInfo aliensInfo = (AliensInfo) application.getAttribute("aliens_info");
                if (aliensInfo != null) {
                    ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, "", "" + true);
                    respProt.info = gson.toJson(aliensInfo);
                    String jsonString = gson.toJson(respProt);
                    response.setContentType("application/json");
                    out.println(jsonString);
                }
                else {
                    // still waiting for player 1 to send enemy information
                    ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, "", "" + false);
                    respProt.info = "";
                    String jsonString = gson.toJson(respProt);
                    response.setContentType("application/json");
                    out.println(jsonString);
                }
            }
            //-----------------------------------------------------------------
            // no such operations or malformed.
            //-----------------------------------------------------------------
            else {
                // assemble ResponseProtocol
                ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, "", "" + result);
                respProt.info = "SpaceInvadersServer: error - no such operation exist!";
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
