package space_invaders;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.google.gson.Gson;

import shared.Item;
import shared.ItemListJson;
import shared.RequestProtocol;
import shared.ResponseProtocol;

public class LoginPanel extends JPanel {
	public static final int W = 630;
	public static final int H = 600;
	
	private JLabel userLabel;
	public JTextField userText;
	private JLabel passwordLabel;
	private JPasswordField passwordText;
	
	public LoginPanel() {
		System.out.println("LoginPanel::LoginPanel()");
		
	    setSize(W,H);
	    
		setLayout(null);
	
		userLabel = new JLabel("User");
		userLabel.setBounds(10, 10, 80, 25);
		add(userLabel);
	
		userText = new JTextField(20);
		userText.setBounds(100, 10, 160, 25);
		add(userText);
	
		passwordLabel = new JLabel("Password");
		passwordLabel.setBounds(10, 40, 80, 25);
		add(passwordLabel);
	
		passwordText = new JPasswordField(20);
		passwordText.setBounds(100, 40, 160, 25);
		add(passwordText);
	
		JButton loginButton = new JButton("login");
		loginButton.setBounds(10, 80, 80, 25);
		add(loginButton);
		loginButton.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	    		RequestProtocol rp1 = new RequestProtocol(RequestProtocol.OPERATION_LOGIN, userText.getText(), passwordText.getText(), "", "java");
	    		
	    		Gson gson = new Gson();
	    		String jsonString = gson.toJson(rp1);
	    		
	    		try {
	    		    URL url = new URL(SpaceInvaders.ServerURLPrefix + "SpaceInvadersServer");
	    		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    		    conn.setDoOutput(true);
	    		    conn.setRequestMethod("POST");
	    		    conn.setRequestProperty("Content-Type", "application/json");
	    		    
	    	        OutputStream os = conn.getOutputStream();
	    	        os.write(jsonString.getBytes());
	    	        os.flush();
	    	        
	    		    System.out.println("Client: conn.getResponseMessage() : " + conn.getResponseMessage());
	    		    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    		    String respJsonString = "";
	    			String decodedString = in.readLine();
	    			while (decodedString != null) {
	    				System.out.println("Client: " + decodedString);
	    				
	    				respJsonString += decodedString;
	    				decodedString = in.readLine();
	    			}
	    			conn.disconnect();
	    			
//	    			respJsonString = respJsonString.replace("\\u003d", "=");
//    				System.out.println("Client: after fixing \\u003d: " + respJsonString);

	    			ResponseProtocol respProt = gson.fromJson(respJsonString.toString(), ResponseProtocol.class);
	    			
	    			if (respProt.result.equals("true")) {
	    				setVisible(false);
	    				SpaceInvaders.turnOnPanels();
	    				// 위에서 panel을 만든 다음에 아래의 online을 켜야만, thread등에서 에러가 발생하지 않는다.
	    				SpaceInvaders.online = true;
	    				
	    				SpaceInvaders.userIDAfterLogin = Integer.parseInt(respProt.id);
	    				
	    				SpaceInvaders.jsonStringToUpdateSpaceshipPrefixBeforeX =
	    						SpaceInvaders.JSON_STRING_TO_UPDATE_SPACESHIP_PREFIX_BEFORE_ID +
//	    						userText.getText() +
	    						respProt.id + // 진짜 아이디 대신에 접속 후, 서버가 보내오는 숫자 ID(0이나 1)가 프로토콜 주고 받을때의 ID가 된다.
	    						SpaceInvaders.JSON_STRING_TO_UPDATE_SPACESHIP_PREFIX_BEFORE_X;

	    				SpaceInvaders.updateSlot();		
	    			}
	    			else {
	    				JOptionPane.showMessageDialog(null, "LoginPanel::LoginPanel(): error: Invalid login information!! Please try again!");
	    			}
	    		} catch (MalformedURLException e1) {
	    			e1.printStackTrace();
	    			System.out.println("MalformedURLException: " + e1);
	    		} catch (IOException e1) {
	    			e1.printStackTrace();
	    			System.out.println("IOException: " + e1);
	    		}
	        }
        });
		
		JButton registerButton = new JButton("register");
		registerButton.setBounds(180, 80, 80, 25);
		add(registerButton);
		registerButton.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	    		RequestProtocol rp1 = new RequestProtocol(RequestProtocol.OPERATION_REGISTER, userText.getText(), passwordText.getText(), "", "java");
	    		
	    		Gson gson = new Gson();
	    		String jsonString = gson.toJson(rp1);
	    		
	    		try {
	    		    URL url = new URL("http://localhost:8080/space_invaders_server/SpaceInvadersServer");
	    		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    		    conn.setDoOutput(true);
	    		    conn.setRequestMethod("POST");
	    		    conn.setRequestProperty("Content-Type", "application/json");
	    		    
	    	        OutputStream os = conn.getOutputStream();
	    	        os.write(jsonString.getBytes());
	    	        os.flush();
	    	        
	    		    System.out.println("Client: conn.getResponseMessage() : " + conn.getResponseMessage());
	    		    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    		    String respJsonString = "";
	    			String decodedString = in.readLine();
	    			while (decodedString != null) {
	    				System.out.println("Client: " + decodedString);
	    				
	    				respJsonString += decodedString;
	    				decodedString = in.readLine();
	    			}
	    			conn.disconnect();
	    		    System.out.println("Client: respJsonString: " + respJsonString);

	    			ResponseProtocol respProt = gson.fromJson(respJsonString.toString(), ResponseProtocol.class);
	    			
	    			if (respProt.result.equals("true")) {
	    				JOptionPane.showMessageDialog(null, "Congratulation!!! Successfully registered!! Now please login!!");
	    			}
	    			else {
	    				JOptionPane.showMessageDialog(null, "LoginPanel::LoginPanel(): error: Invalid(duplicated info) register information!! Please try again!");
	    			}
	    		} catch (MalformedURLException e1) {
	    			e1.printStackTrace();
	    			System.out.println("MalformedURLException: " + e1);
	    		} catch (IOException e1) {
	    			e1.printStackTrace();
	    			System.out.println("IOException: " + e1);
	    		}
	        }
        });
	}
}
