package space_invaders;

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

import shared.EmptyItem;
import shared.Item;
import shared.RequestProtocol;
import shared.ResponseProtocol;

public class SlotsPanel extends JPanel implements ComponentListener {
	public static final int W = SpaceInvaders.W;
	public static final int H = SpaceInvaders.H;

	private JLabel slotsTitle;
	
	private JScrollPane scrollPaneInvItemsList;
	public JList<Item> invItemsList;

	private JScrollPane scrollPaneSlotsItemsList;
	private JList<String> slotsItemsList;
	
	private JTextField userText;

	private JButton moveToItemListButton;
	private JButton moveToSelectedItemListButton;

	private JButton exitButton;

	public SlotsPanel() {
		System.out.println("SlotsPanel::SlotsPanel()");
		setLayout(null);
		
        this.addComponentListener(this);
		
		// title
		slotsTitle = new JLabel("Slots");
		slotsTitle.setFont(new Font("Serif", Font.PLAIN, 28));
		slotsTitle.setBounds(SpaceInvaders.W / 2 - 60, 10, SpaceInvaders.W / 2, 50);
		add(slotsTitle);
		
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

		// selected item list
		slotsItemsList = new JList<String>(); //data has type Object[]
		slotsItemsList.setModel(new DefaultListModel<String>());
		Item[] slotsItemsArray = SpaceInvaders.slot.dump();
		for (int i = 0; i < slotsItemsArray.length; i++) {
			String nameItem = "[" + i + "] ";
			if (slotsItemsArray[i] instanceof EmptyItem) {
				nameItem += " <EMPTY>";
			}
			else {
				nameItem += slotsItemsArray[i].toString();
			}
			((DefaultListModel)slotsItemsList.getModel()).addElement(nameItem);
		}
		slotsItemsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		slotsItemsList.setLayoutOrientation(JList.VERTICAL);
		slotsItemsList.setVisibleRowCount(-1);
				

		scrollPaneSlotsItemsList = new JScrollPane(slotsItemsList);
		scrollPaneSlotsItemsList.setBounds(SpaceInvaders.W - 10 - SpaceInvaders.W / 3, 70, SpaceInvaders.W / 3, SpaceInvaders.H - 120);
		scrollPaneSlotsItemsList.setVisible(true);
		add(scrollPaneSlotsItemsList);

		// move inv to slots button

		moveToItemListButton= new JButton("->");
		int moveToItemListButtonWidth = 175; 
		int moveToItemListButtonHeight = 80;
		moveToItemListButton.setBounds(SpaceInvaders.W / 2 - moveToItemListButtonWidth / 2 , 100 + moveToItemListButtonHeight, moveToItemListButtonWidth, moveToItemListButtonHeight);
		add(moveToItemListButton);
		moveToItemListButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (Item o : invItemsList.getSelectedValuesList())
				{
					int indexInsertedInSlots = SpaceInvaders.slot.addItem(o);
					
					if (indexInsertedInSlots != -1) {
						SpaceInvaders.inventory.removeItem(o);
						
					    ((DefaultListModel<Item>)invItemsList.getModel()).removeElement(o);
					    slotAdd(o.toString(), indexInsertedInSlots);
					}
					
					update();
				}
				
				// 아래것은 index로 처리한다. 즉 위와 똑같다.
//				int[] indices = invItemsList.getSelectedIndices();
//				System.out.println("indices.length = " + indices.length);
//				System.out.println("invItemsList.getModel().getSize(); = " + invItemsList.getModel().getSize());
//				
//				for (int i = indices.length - 1; i >= 0; i--) {
//					if (indices[i] >= 0) { //Remove only if a particular item is selected
//						System.out.println("indices[" + i + "] = " + indices[i]);
//						DefaultListModel<Item> model = (DefaultListModel<Item>)invItemsList.getModel();
//						model.remove(indices[i]);
//					}
//				}
		    }
	    });

		// move slots to inv button
		moveToSelectedItemListButton = new JButton("<-");
		int moveToSelectedItemListButtonWidth = 175;
		int moveToSelectedItemListButtonHeight = 80; 
		moveToSelectedItemListButton.setBounds(SpaceInvaders.W / 2 - moveToSelectedItemListButtonWidth / 2 , 275 + moveToItemListButtonHeight, moveToSelectedItemListButtonWidth, moveToSelectedItemListButtonHeight);
		add(moveToSelectedItemListButton);
		moveToSelectedItemListButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] indices = slotsItemsList.getSelectedIndices();
				
				for (int i = 0; i < indices.length; i++) {
					Item curItem = SpaceInvaders.slot.removeItemAt(indices[i]);
					if (!(curItem instanceof EmptyItem)) {
						((DefaultListModel)invItemsList.getModel()).addElement(curItem);
						System.out.println("Client: slot remove indices[i] =" + indices[i]);
						slotRemove(indices[i]);
					}
				}
				update();
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
	        	SpaceInvaders.slotsPanel.setVisible(false);
	        }
        });
	}
	
	public void componentHidden(ComponentEvent e) {
		System.out.println("SlotsPanel::componentHidden");
		SpaceInvaders.stopUpdateInventoryTimer();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
    }
	
	public void componentShown(ComponentEvent e) {
		System.out.println("SlotsPanel::componentShown");
		SpaceInvaders.startUpdateInventoryTimer();

		invItemsList.setModel(new DefaultListModel<Item>());
		Item[] invItemsArray = SpaceInvaders.inventory.dump();
		for (int i = 0; i < invItemsArray.length; i++) {
			((DefaultListModel)invItemsList.getModel()).addElement(invItemsArray[i]);
		}
	}
	
	public void update() {
		((DefaultListModel)slotsItemsList.getModel()).clear();
		
		Item[] slotsItemsArray = SpaceInvaders.slot.dump();
		for (int i = 0; i < slotsItemsArray.length; i++) {
			String nameItem = "[" + i + "] ";
			if (slotsItemsArray[i] instanceof EmptyItem) {
				nameItem += " <EMPTY>";
			}
			else {
				nameItem += slotsItemsArray[i].toString();
			}
			((DefaultListModel)slotsItemsList.getModel()).addElement(nameItem);
		}
	}
	
	public boolean inventoryAdd(String uuid) {
		return SpaceInvaders.request("SpaceInvadersMerchantServer", new RequestProtocol(RequestProtocol.OPERATION_INVENTORY_ADD_FROM_SLOT, "" + SpaceInvaders.userIDAfterLogin, "", uuid, "java")).result.equals("true");
    }

	public boolean inventoryRemove(String uuid) {
		return SpaceInvaders.request("SpaceInvadersMerchantServer", new RequestProtocol(RequestProtocol.OPERATION_INVENTORY_REMOVE, "" + SpaceInvaders.userIDAfterLogin, "", uuid, "java")).result.equals("true");
	}

	public boolean slotAdd(String uuid, int slotIndex) {
		return SpaceInvaders.request("SpaceInvadersMerchantServer", new RequestProtocol(RequestProtocol.OPERATION_SLOT_ADD_FROM_INVENTORY, "" + SpaceInvaders.userIDAfterLogin, "", "{\"item_desc\":\"" + uuid + "\", \"slot_index\":" + slotIndex + "}", "java")).result.equals("true");
    }

	public boolean slotRemove(int slotIndex) {
		return SpaceInvaders.request("SpaceInvadersMerchantServer", new RequestProtocol(RequestProtocol.OPERATION_SLOT_REMOVE, "" + SpaceInvaders.userIDAfterLogin, "", "" + slotIndex, "java")).result.equals("true");
	}
}
