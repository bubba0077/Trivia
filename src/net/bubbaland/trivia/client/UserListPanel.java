package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class UserListPanel extends TriviaPanel {
	
	private static final long	serialVersionUID	= 4877267114050120590L;
	private static final int WIDTH = 85;
	private static final int HEADER_HEIGHT = 12;
	private static final int HEIGHT = 0;
	private static final Color HEADER_BACKGROUND_COLOR = Color.DARK_GRAY;
	private static final Color HEADER_TEXT_COLOR = Color.WHITE;
	private static final Color BACKGROUND_COLOR = Color.LIGHT_GRAY;
	private static final Color TEXT_COLOR = Color.BLACK;
	
	private static final float FONT_SIZE = 10f;
	
	private String[] users;
	private final JLabel header;
	private final JList<String> userList;

	/**
	 * Data sources
	 */
	private final TriviaClient		client;

	public UserListPanel(TriviaClient client) {
		
		super();
		this.client = client;
		this.users = client.getUserList();
				
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.insets = new Insets(0,10,0,0);
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		
		header = this.enclosedLabel("", WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
		
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 1;
				
		this.userList = new JList<String>();
		this.userList.setLayoutOrientation(JList.VERTICAL);
		this.userList.setVisibleRowCount(-1);
		this.userList.setForeground(TEXT_COLOR);
		this.userList.setBackground(BACKGROUND_COLOR);
		this.userList.setFont(this.userList.getFont().deriveFont(FONT_SIZE));
		
		final JScrollPane pane = new JScrollPane(this.userList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		pane.setBorder(BorderFactory.createEmptyBorder());
		pane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		pane.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		this.add(pane, constraints);
		
	}
	
	
	@Override
	public void update() {
		this.users = client.getUserList();
		Arrays.sort(this.users);
		this.header.setText("Active (" + users.length + ")");
		this.userList.setListData(users);		
	}

}
