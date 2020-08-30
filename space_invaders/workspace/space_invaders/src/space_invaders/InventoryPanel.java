package space_invaders;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Timer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.text.DefaultCaret;

import com.google.gson.Gson;

import shared.Item;
import shared.ItemInfo;
import shared.ItemListJson;
import shared.ItemsInfo;
import shared.RequestProtocol;
import shared.ResponseProtocol;

public class InventoryPanel extends JPanel implements ComponentListener {
	public static final int W = SpaceInvaders.W;
	public static final int H = SpaceInvaders.H;

	private JLabel inventoryTitle;
	
	private JScrollPane scrollPane;
	public JList<Item> itemsList;
	
	private JTextField userText;

	private JLabel goldLabel;

	private JButton shopButton;
	private JButton exitButton;
		
	public InventoryPanel() {
		setLayout(null);
		
        this.addComponentListener(this);
		
		// title
		inventoryTitle = new JLabel("Inventory");
		inventoryTitle.setFont(new Font("Serif", Font.PLAIN, 28));
		inventoryTitle.setBounds(SpaceInvaders.W / 2 - 60, 10, SpaceInvaders.W / 2, 50);
		add(inventoryTitle);
		
		// item list
		// ***** 바로 아래처럼 constructor에서 dump()를 이용하여 init하면 편하지만..
		//       그렇게 하지 않는 이유는, "itemsList.setModel(new DefaultListModel<Item>());"이 라인후
		//       JList의 data가 reset되어 다 사라지므로, 아래처럼 for loop을 이용하여 일일히 넣어주게 되었다.
//		itemsList = new JList<Item>(SpaceInvaders.inventory.dump()); //data has type Object[]
		itemsList = new JList<Item>(); //data has type Object[]
		itemsList.setModel(new DefaultListModel<Item>());
		Item[] invItemsArray = SpaceInvaders.inventory.dump();
		for (int i = 0; i < invItemsArray.length; i++) {
			((DefaultListModel<Item>)itemsList.getModel()).addElement(invItemsArray[i]);
		}
		itemsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		itemsList.setLayoutOrientation(JList.VERTICAL);
		itemsList.setVisibleRowCount(-1);
		
		JScrollPane listScroller = new JScrollPane(itemsList);
		listScroller.setBounds(10, 70, SpaceInvaders.W / 3 * 2, SpaceInvaders.H - 120);
		listScroller.setVisible(true);
		add(listScroller);

		// gold
		goldLabel = new JLabel("Gold: " + SpaceInvaders.gold );
		goldLabel.setFont(new Font("Serif", Font.PLAIN, 16));
		goldLabel.setBounds(10, SpaceInvaders.W - 80, SpaceInvaders.W / 2, 50);
		add(goldLabel);

		// shop button
		shopButton = new JButton("Shop");
		int shopButtonWidth = 175; 
		int shopButtonHeight = 80; 
		shopButton.setBounds(SpaceInvaders.W - shopButtonWidth - 10, 70, shopButtonWidth, shopButtonHeight);
		add(shopButton);
		shopButton.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	SpaceInvaders.inventoryPanel.setVisible(false);
	        	SpaceInvaders.shopPanel.setVisible(true);
				Gson gson = new Gson();
//	        	RequestProtocol rp1 = new RequestProtocol(RequestProtocol.OPERATION_SHOP_UPDATE_ITEMS, SpaceInvaders.loginPanel.userText.getText(), "");
//	    		String jsonString = gson.toJson(rp1);
//	    		System.out.println("OPERATION_SHOP_UPDATE_ITEMS:: jsonString:                  |" + jsonString + "|");
//
//	    		//				String jsonStringToUpdateShopItems = "{\"operation\"=\"" + RequestProtocol.OPERATION_SHOP_UPDATE_ITEMS + "\", ";
//	        	
//	    		try {
//	    		    URL url = new URL(SpaceInvaders.ServerURLPrefix + "SpaceInvadersMerchantServer");
//	    		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//	    		    conn.setDoOutput(true);
//	    		    conn.setRequestMethod("POST");
//	    		    conn.setRequestProperty("Content-Type", "application/json");
//	    		    
//	    	        OutputStream os = conn.getOutputStream();
//	    	        os.write(jsonString.getBytes());
//	    	        os.flush();
//	    	        
////		    		    System.out.println("Client: conn.getResponseMessage() : " + conn.getResponseMessage());
//	    		    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//	    		    String respJsonString = "";
//	    			String decodedString = in.readLine();
//	    			while (decodedString != null) {
//		    			System.out.println("Client: " + decodedString);
//	    				
//	    				respJsonString += decodedString;
//	    				decodedString = in.readLine();
//	    			}
//	    			conn.disconnect();
////	    				System.out.println("Client: respJsonString [" + respJsonString + "]");
//	    			ResponseProtocol respProt = gson.fromJson(respJsonString.toString(), ResponseProtocol.class);

				    System.out.println("SpaceInvaders.request(\"SpaceInvadersMerchantServer\")");
				    ResponseProtocol respProt = SpaceInvaders.request("SpaceInvadersMerchantServer", new RequestProtocol(RequestProtocol.OPERATION_SHOP_UPDATE_ITEMS, "" + SpaceInvaders.userIDAfterLogin, "", "", "java"));   
				
					//respProt.info = "{"numItems"=0, "items"="{"className"="a", "desc"="a"}; {"className"="b", "desc"="b"}; {"className"="c", "desc"="c"}; {"className"="d", "desc"="d"}"}";
	    			if (respProt.result.equals("true")) {    			    
	                    ItemsInfo shopFromServer = gson.fromJson(respProt.info.toString(), ItemsInfo.class);

	                    for (int i = 0; i < shopFromServer.items.length; i++) {
	                        Item curItem = shopFromServer.items[i].createItem();
	                        SpaceInvaders.shopPanel.shopItemsList.add(curItem);
	                        ((DefaultListModel<String>)SpaceInvaders.shopPanel.shopItemNamesList.getModel()).addElement(curItem.toString());

	                    }				
	    			}
//	    		} catch (MalformedURLException e1) {
//	    			e1.printStackTrace();
////		    			System.out.println("MalformedURLException: " + e1);
//	    		} catch (IOException e1) { 
//	    			e1.printStackTrace();
////		    			System.out.println("IOException: " + e1);
//	    		}
	        }
        });

		// exit button
		exitButton = new JButton("Exit");
		int exitButtonWidth = 175;
		int exitButtonHeight = 80; 
		exitButton.setBounds(SpaceInvaders.W - exitButtonWidth - 10, 70 + 10 + shopButtonHeight, exitButtonWidth, exitButtonHeight);
		add(exitButton);
		exitButton.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	SpaceInvaders.inventoryPanel.setVisible(false);
	        }
        });
	}
	
	public void componentHidden(ComponentEvent e) {
		System.out.println("componentHidden");
		SpaceInvaders.stopUpdateInventoryTimer();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
    }
	
	public void componentShown(ComponentEvent e) {
		System.out.println("componentShown");
		SpaceInvaders.startUpdateInventoryTimer();
		
		itemsList.setModel(new DefaultListModel<Item>());
		Item[] invItemsArray = SpaceInvaders.inventory.dump();
		for (int i = 0; i < invItemsArray.length; i++) {
			((DefaultListModel<Item>)itemsList.getModel()).addElement(invItemsArray[i]);
		}
	}

}
