package space_invaders;

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
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

import com.google.gson.Gson;

import shared.RequestProtocol;
import shared.ResponseProtocol;

public class ChatPanel extends JPanel {
	public static final int W = 300;
	public static final int H = 250; //SpaceInvaders.H;

	private static JTextArea textArea;
	private static JScrollPane scrollPane;
	private static int prevScrollBarPos;
	private static boolean autoDownScrollBar;
	
	
	private static JTextField userText;

	public ChatPanel() {
		autoDownScrollBar = true;
		prevScrollBarPos = -1;

		System.out.println("ChatPanel::ChatPanel()");
		
	    setSize(W,H);
	    
		setLayout(null);
	
		textArea = new JTextArea(5, 20);
		scrollPane = new JScrollPane(textArea); 
		scrollPane.setBounds(10, 10, W-20, 190);
		textArea.setEditable(false);
		prevScrollBarPos = -1;
		
		add(scrollPane);
		
		userText = new JTextField(20);
		userText.setBounds(10, 210, W-100, 25);
		add(userText);
		userText.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	ChatPanel.sendMessageFromUserTextField();
	        }
        });
		
		JButton sendButton = new JButton("Send");
		sendButton.setBounds(W-100+10, 210, 80, 25);
		add(sendButton);
		sendButton.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	ChatPanel.sendMessageFromUserTextField();
	        }
        });
	
	    //-------------------------------------------------
	    // init game
	    //-------------------------------------------------
		Timer t = new Timer(true);
		t.schedule(new java.util.TimerTask() {
				public void run()
		        {
//					System.out.println("chat messages update timer!!");
					if (!SpaceInvaders.online) {
						return;
					}

//		    		RequestProtocol rp1 = new RequestProtocol(RequestProtocol.OPERATION_CHAT_UPDATE_MESSAGES, SpaceInvaders.loginPanel.userText.getText(), "", "");
		    		RequestProtocol rp1 = new RequestProtocol(RequestProtocol.OPERATION_CHAT_UPDATE_MESSAGES, "" + SpaceInvaders.userIDAfterLogin, "", "", "java");

		    		Gson gson = new Gson();
		    		String jsonString = gson.toJson(rp1);
		    		
		    		try {
		    		    URL url = new URL(SpaceInvaders.ServerURLPrefix + "SpaceInvadersChatServer");
		    		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    		    conn.setDoOutput(true);
		    		    conn.setRequestMethod("POST");
		    		    conn.setRequestProperty("Content-Type", "application/json");
		    		    
		    	        OutputStream os = conn.getOutputStream();
		    	        os.write(jsonString.getBytes());
		    	        os.flush();
		    	        
//		    		    System.out.println("Client: conn.getResponseMessage() : " + conn.getResponseMessage());
		    		    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    		    String respJsonString = "";
		    			String decodedString = in.readLine();
		    			while (decodedString != null) {
//		    				System.out.println("Client: " + decodedString);
		    				
		    				respJsonString += decodedString;
		    				decodedString = in.readLine();
		    			}
		    			conn.disconnect();
//	    				System.out.println("Client: respJsonString [" + respJsonString + "]");

		    			ResponseProtocol respProt = gson.fromJson(respJsonString.toString(), ResponseProtocol.class);
		    			
		    			if (respProt.result.equals("true")) {
//		    				setVisible(false);
//		    				boolean updateCaret = (textArea.getCaretPosition() == textArea.getDocument().getLength());
//		    				boolean updateCaret = true;
//		    				if (prevScrollBarPos != -1 && prevScrollBarPos != scrollPane.getVerticalScrollBar().getValue()) {
//		    					updateCaret = false;
//		    				}
		    				
		    				if (prevScrollBarPos != -1 && prevScrollBarPos != scrollPane.getVerticalScrollBar().getValue()) {
		    					ChatPanel.autoDownScrollBar = false;
		    				}
		    				
		    				textArea.setText(respProt.info);
		    				
//		    				if (updateCaret) {
//			    				System.out.println("Client: scrollPane.getVerticalScrollBar().getValue()   : " + scrollPane.getVerticalScrollBar().getValue());
//			    				System.out.println("Client: scrollPane.getVerticalScrollBar().getMaximum() : " + scrollPane.getVerticalScrollBar().getMaximum());

//		    					textArea.setCaretPosition(textArea.getDocument().getLength());
		    				
		    				
//		    				if (ChatPanel.autoDownScrollBar) {
		    					scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
		    					prevScrollBarPos = scrollPane.getVerticalScrollBar().getValue();
//		    				}
		    				
//			    				System.out.println("Client: scrollPane.getVerticalScrollBar().getValue()   : " + scrollPane.getVerticalScrollBar().getValue());
//		    				}
		    			}
		    			else {
		    				JOptionPane.showMessageDialog(null, "ChatPanel::ChatPanel(): error: Invalid login information!! Please try again!");
		    			}
		    		} catch (MalformedURLException e1) {
		    			e1.printStackTrace();
//		    			System.out.println("MalformedURLException: " + e1);
		    		} catch (IOException e1) {
		    			e1.printStackTrace();
//		    			System.out.println("IOException: " + e1);
		    		}
		        }
		},0,1);
	}
	
	public static void sendMessageFromUserTextField() {
//    	textArea.append(userText.getText() + "\n");
//    	userText.setText("");
    	
//		RequestProtocol rp1 = new RequestProtocol(RequestProtocol.OPERATION_CHAT_NEW_MESSAGE, SpaceInvaders.loginPanel.userText.getText(), "", userText.getText());
		RequestProtocol rp1 = new RequestProtocol(RequestProtocol.OPERATION_CHAT_NEW_MESSAGE, "" + SpaceInvaders.userIDAfterLogin, "", userText.getText(), "java");

		Gson gson = new Gson();
		String jsonString = gson.toJson(rp1);
		
		try {
		    URL url = new URL(SpaceInvaders.ServerURLPrefix + "SpaceInvadersChatServer");
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    conn.setDoOutput(true);
		    conn.setRequestMethod("POST");
		    conn.setRequestProperty("Content-Type", "application/json");
		    
	        OutputStream os = conn.getOutputStream();
	        os.write(jsonString.getBytes());
	        os.flush();
	        
//		    System.out.println("Client: conn.getResponseMessage() : " + conn.getResponseMessage());
		    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String respJsonString = "";
			String decodedString = in.readLine();
			while (decodedString != null) {
//				System.out.println("Client: " + decodedString);
				
				respJsonString += decodedString;
				decodedString = in.readLine();
			}
			conn.disconnect();
//			System.out.println("Client: respJsonString [" + respJsonString + "]");

			ResponseProtocol respProt = gson.fromJson(respJsonString.toString(), ResponseProtocol.class);
			
			if (respProt.result.equals("true")) {
//				setVisible(false);
				textArea.setText(respProt.info);
			}
			else {
				JOptionPane.showMessageDialog(null, "ChatPanel::sendMessageFromUserTextField: error: Invalid login information!! Please try again!");
			}
			
			// 성공적으로 chat message를 서버로 보냈다면, text field를 지운다.
			userText.setText("");

//			textArea.setCaretPosition(textArea.getDocument().getLength());
			scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
			prevScrollBarPos = scrollPane.getVerticalScrollBar().getValue();
			ChatPanel.autoDownScrollBar = true;
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			System.out.println("MalformedURLException: " + e1);
			System.exit(1);
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("IOException: " + e1);
			System.exit(1);
		}
	}
}
