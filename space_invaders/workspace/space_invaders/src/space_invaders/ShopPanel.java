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
import java.util.Vector;

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

import shared.DoublePointItem;
import shared.Item;
import shared.ItemListJson;
import shared.RequestProtocol;
import shared.ResponseProtocol;

public class ShopPanel extends JPanel implements ComponentListener {
	public static final int W = SpaceInvaders.W;
	public static final int H = SpaceInvaders.H;

	private JLabel shopTitle;
	
	private JScrollPane scrollPaneInvItemsList;
	public JList<Item> invItemsList;

	private JScrollPane scrollPaneShopItemsList;
	public Vector<Item> shopItemsList;
	public JList<String> shopItemNamesList;
	
//	private JTextField userText;

	private JButton sellButton;
	private JButton buyButton;

	private JButton exitButton;

	public ShopPanel() {
		System.out.println("ShopPanel::ShopPanel()");

		setLayout(null);
		
        this.addComponentListener(this);

		// title
		shopTitle = new JLabel("Shop");
		shopTitle.setFont(new Font("Serif", Font.PLAIN, 28));
		shopTitle.setBounds(SpaceInvaders.W / 2 - 60, 10, SpaceInvaders.W / 2, 50);
		add(shopTitle);
		
		// item list
		invItemsList = new JList<Item>(); //data has type Object[]
		invItemsList.setModel(new DefaultListModel<Item>());
		Item[] invItemsArray = SpaceInvaders.inventory.dump();
		for (int i = 0; i < invItemsArray.length; i++) {
			((DefaultListModel)invItemsList.getModel()).addElement(invItemsArray[i]);
		}
		invItemsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		invItemsList.setLayoutOrientation(JList.VERTICAL);
		invItemsList.setVisibleRowCount(-1);
		
		scrollPaneInvItemsList = new JScrollPane(invItemsList);
		scrollPaneInvItemsList.setBounds(10, 70, SpaceInvaders.W / 3, SpaceInvaders.H - 120);
		scrollPaneInvItemsList.setVisible(true);
		add(scrollPaneInvItemsList);

		// shop list
		shopItemsList = new Vector<Item>();
		shopItemNamesList = new JList<String>(); //data has type Object[]
		shopItemNamesList.setModel(new DefaultListModel<String>());
		
		int size = ((DefaultListModel)shopItemNamesList.getModel()).getSize();
		for (int i = 0; i < size; i++) {
			String nameItem = (String) ((DefaultListModel)shopItemNamesList.getModel()).getElementAt(i);
	
			((DefaultListModel)shopItemNamesList.getModel()).addElement(nameItem);
		}
		
		shopItemNamesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		shopItemNamesList.setLayoutOrientation(JList.VERTICAL);
		shopItemNamesList.setVisibleRowCount(-1);

		scrollPaneShopItemsList = new JScrollPane(shopItemNamesList);
		scrollPaneShopItemsList.setBounds(SpaceInvaders.W - 10 - SpaceInvaders.W / 3, 70, SpaceInvaders.W / 3, SpaceInvaders.H - 120);
		scrollPaneShopItemsList.setVisible(true);
		add(scrollPaneShopItemsList);

		// move inv to slots button

		sellButton= new JButton("Sell");
		int moveToItemListButtonWidth = 175; 
		int moveToItemListButtonHeight = 80;
		sellButton.setBounds(SpaceInvaders.W / 2 - moveToItemListButtonWidth / 2 , 100 + moveToItemListButtonHeight, moveToItemListButtonWidth, moveToItemListButtonHeight);
		add(sellButton);
		sellButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (Item o : invItemsList.getSelectedValuesList()) {
					//?????????????????????????????????????????????????????????
					// 지울 아이템의 gold value에 해당하는 gold값을 Spaceship의 gold에 add해주어야 한다.
					//?????????????????????????????????????????????????????????
					String nameItem = o.toString();

					if (sellItem(nameItem)) {
						SpaceInvaders.inventory.removeItem(o);
						((DefaultListModel<Item>)invItemsList.getModel()).removeElement(o);	
					}
				}
		    }
	    });

		// move slots to inv button
		buyButton = new JButton("Buy");
		int moveToSelectedItemListButtonWidth = 175;
		int moveToSelectedItemListButtonHeight = 80; 
		buyButton.setBounds(SpaceInvaders.W / 2 - moveToSelectedItemListButtonWidth / 2 , 275 + moveToItemListButtonHeight, moveToSelectedItemListButtonWidth, moveToSelectedItemListButtonHeight);
		add(buyButton);
		buyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {	
				int[] indices = shopItemNamesList.getSelectedIndices();
			
				//?????????????????????????????????????????????????????????????
				// gold가 있을 경우, item을 살 수 있게 한다. 물론 gold가 줄어든다.
				//?????????????????????????????????????????????????????????????
				for (int i = 0; i < indices.length; i++) {
					String nameItem = (String) ((DefaultListModel)shopItemNamesList.getModel()).getElementAt(indices[i]);

					//?????????????????????????????????????????????????????????????
					// server가 item을 add할 수 없는 경우?
					// 서버 사정에 따라 조금 후에 다시 try 해주세요라고 하고, 현재 구입을 취소한다.
					//?????????????????????????????????????????????????????????????
					if (purchaseItem(nameItem)) {
						// 성공적으로 add된 경우..
					}
					else {
						// 구입 취소 메시지를 보여줄 것.
						JOptionPane.showMessageDialog(null, "error: purchaseItem(nameItem)", "error: purchaseItem(nameItem)", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});
		
		// exit button
		exitButton = new JButton("Exit");
		int exitButtonWidth = SpaceInvaders.W - 20;
		int exitButtonHeight = 30; 
		exitButton.setBounds(10, SpaceInvaders.H - exitButtonHeight - 10, exitButtonWidth, exitButtonHeight);
		add(exitButton);
		exitButton.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	SpaceInvaders.shopPanel.setVisible(false);
	    		shopItemNamesList.setModel(new DefaultListModel<String>());
	        	
	        	SpaceInvaders.inventoryPanel.setVisible(true);
	        }
        });
	}

	public boolean purchaseItem(String name) {
		return SpaceInvaders.request("SpaceInvadersMerchantServer", new RequestProtocol(RequestProtocol.OPERATION_SHOP_PURCHASE_ITEM, "" + SpaceInvaders.userIDAfterLogin, "", name, "java")).result.equals("true");
    }

	public boolean sellItem(String uuid) {
		return SpaceInvaders.request("SpaceInvadersMerchantServer", new RequestProtocol(RequestProtocol.OPERATION_SHOP_SELL_ITEM_FROM_INVENTORY, "" + SpaceInvaders.userIDAfterLogin, "", uuid, "java")).result.equals("true");
	}

	public void componentHidden(ComponentEvent e) {
		System.out.println("ShopPanel::componentHidden");
		SpaceInvaders.stopUpdateInventoryTimer();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
    }
	
	public void componentShown(ComponentEvent e) {
		System.out.println("ShopPanel::componentShown");
		SpaceInvaders.startUpdateInventoryTimer();

		invItemsList.setModel(new DefaultListModel<Item>());
		Item[] invItemsArray = SpaceInvaders.inventory.dump();
		for (int i = 0; i < invItemsArray.length; i++) {
			((DefaultListModel)invItemsList.getModel()).addElement(invItemsArray[i]);
		}
	}
}
