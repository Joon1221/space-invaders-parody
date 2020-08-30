package space_invaders_server;

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

import shared.RequestProtocol;
import shared.ResponseProtocol;

import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

@WebServlet("/SpaceInvadersChatServer")
public class SpaceInvadersChatServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final int MAX_SESSION_TIME = 1;

    /**
     * @see SpaceInvadersChatServer()
     */
    public SpaceInvadersChatServer() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        PrintWriter out = response.getWriter();
//        out.println("hello");
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        System.out.println("SpaceInvadersChatServer::doPost(): started");
		PrintWriter out = response.getWriter();
		Gson gson = new Gson();
		
	    //=====================================================================
	    // get RequestProtocol from json
	    //=====================================================================
		StringBuilder jsonFromClient = new StringBuilder();
        String s;
        while ((s = request.getReader().readLine()) != null) {
        	jsonFromClient.append(s);
        }
//        System.out.println("SpaceInvadersChatServer: jsonFromClient: [" + jsonFromClient + "]");
		RequestProtocol reqProt = gson.fromJson(jsonFromClient.toString(), RequestProtocol.class);

		//=====================================================================
		// credential: session에서 login정보 확인.
	    //=====================================================================
    	ServletContext application = getServletConfig().getServletContext();

//    	HashMap<String, String> usersOnline = (HashMap<String, String>)application.getAttribute("users_online");
    	HashMap<String, String> usersIPAddress = (HashMap<String, String>)application.getAttribute("users_ip_address");

//    	System.out.println("SpaceInvadersChatServer::doPost(): reqProt.id = " + reqProt.id);
    	
		String ipAddress = request.getHeader("X-FORWARDED-FOR");

	    if (ipAddress == null) {  
	        ipAddress = request.getRemoteAddr();  
	    }

//    	System.out.println("SpaceInvadersChatServer::doPost(): ipAddress = " + ipAddress);
//    	if (usersIPAddress != null) {
//    		System.out.println("SpaceInvadersChatServer::doPost(): usersIPAddress.get(reqProt.id).equals(ipAddress) = " + usersIPAddress.get(reqProt.id).equals(ipAddress));
//    	}

    	if (usersIPAddress == null || !usersIPAddress.containsKey(reqProt.id) || !usersIPAddress.get(reqProt.id).equals(ipAddress)) {
	        // assemble ResponseProtocol
			ResponseProtocol respProt = new ResponseProtocol("", "", "false");
			respProt.info = "SpaceInvadersChatServer: error - login needed!";
			String jsonString = gson.toJson(respProt);
	        response.setContentType("application/json");
//	        System.out.println("SpaceInvadersChatServer: " + jsonString);
	        out.println(jsonString);
	        return;
    	}

//        // Create a session object if it is already not  created.
//        HttpSession session = request.getSession();
//
//        if (session == null) {
//            System.out.println("SpaceInvadersChatServer: session has not been created yet!!!");
//        }
//        
//        System.out.println("SpaceInvadersChatServer: " + request.getSession().getAttribute(SpaceInvadersServer.ID_KEY));
//        
//        // Check if this is new comer on your web service.
//        if (session.isNew()) { // login을 반드시 하고 사용해야 하므로 에러.
//	        session.setAttribute(SpaceInvadersServer.ID_KEY, reqProt.id);
//	        System.out.println("SpaceInvadersChatServer: session.setAttribute(SpaceInvadersServer.ID_KEY, reqProt.id);");
//
////	        ResponseProtocol respProt = new ResponseProtocol("", "", "true");
//
////            // assemble ResponseProtocol
////			ResponseProtocol respProt = new ResponseProtocol("", "", "false");
////			respProt.info = "SpaceInvadersChatServer: error - sesseion is new -> login needed!";
////			String jsonString = gson.toJson(respProt);
////	        response.setContentType("application/json");
////	        System.out.println("SpaceInvadersChatServer: " + jsonString);
////	        out.println(jsonString);
//        }
        
//        else {        	
        	Vector chatMessages = (Vector)application.getAttribute("chat_messages");
        	if (chatMessages == null) {
        		chatMessages = new Vector();
        		application.setAttribute("chat_messages", chatMessages);
        	}
        	
//	        // Check if 
//			String idFromSession = (String)session.getAttribute(SpaceInvadersServer.ID_KEY);
//			if (idFromSession == null || !reqProt.id.equals(idFromSession)) {
//	            // assemble ResponseProtocol
//				ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "false");
//				respProt.info = "SpaceInvadersServer: error - no such id logged in -> login needed!";
//				String jsonString = gson.toJson(respProt);
//		        response.setContentType("application/json");
//		        out.println(jsonString);
//			}
			
	        //-----------------------------------------------------------------
	        // RequestProtocol.OPERATION_CHAT_NEW_MESSAGE
	        //-----------------------------------------------------------------
	        if (reqProt.operation.equals(RequestProtocol.OPERATION_CHAT_NEW_MESSAGE)) {
	        	chatMessages.add(reqProt.id + ": " + reqProt.message);
	        	System.out.println(chatMessages.toString());
	        	
			    // assemble ResponseProtocol
				ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "true");
				String chatMessagesString = "";
				Iterator chatMessagesIter = chatMessages.iterator();
				while (chatMessagesIter.hasNext()) {
					chatMessagesString += chatMessagesIter.next() + "\n";
				}
				respProt.info = chatMessagesString;
				String jsonString = gson.toJson(respProt);
				
		        response.setContentType("application/json");
		        out.println(jsonString);
	        }
	        //-----------------------------------------------------------------
	        // RequestProtocol.OPERATION_CHAT_UPDATE_MESSAGES
	        //-----------------------------------------------------------------
	        else if (reqProt.operation.equals(RequestProtocol.OPERATION_CHAT_UPDATE_MESSAGES)) {
			    // assemble ResponseProtocol
				ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, reqProt.id, "true");
				String chatMessagesString = "";
				Iterator chatMessagesIter = chatMessages.iterator();
				while (chatMessagesIter.hasNext()) {
					chatMessagesString += chatMessagesIter.next() + "\n";
				}
				respProt.info = chatMessagesString;
				String jsonString = gson.toJson(respProt);
				
		        response.setContentType("application/json");
		        out.println(jsonString);
	        }
	        // no such operations or malformed.
	        else {
	            // assemble ResponseProtocol
				ResponseProtocol respProt = new ResponseProtocol(reqProt.operation, "", "false");
				respProt.info = "SpaceInvadersServer: error - no such operation exist!";
				String jsonString = gson.toJson(respProt);
		        response.setContentType("application/json");
		        out.println(jsonString);
	        }
        }
//	}
}
