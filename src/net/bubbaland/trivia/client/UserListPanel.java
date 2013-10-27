package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import net.bubbaland.trivia.UserList.Role;

/**
 * Creates a panel that displays active and idle user names.
 * 
 * @author Walter Kolczynski
 * 
 */
public class UserListPanel extends TriviaPanel {

	private static final long				serialVersionUID	= 4877267114050120590L;

	/**
	 * Colors
	 */
	protected static Color					researcherColor;
	protected static Color					callerColor;
	protected static Color					typistColor;
	protected static Color					idleColor;

	/**
	 * GUI elements that will need to be updated
	 */
	private final JLabel					header;
	private final DefaultListModel<String>	userListModel;
	private final JList<String>				userList;

	/** Data */
	private Hashtable<String, Role>			activeUserHash;
	private Hashtable<String, Role>			idleUserHash;

	/** Data sources */
	private final TriviaClient				client;

	public UserListPanel(TriviaClient client) {

		super();
		this.client = client;

		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.insets = new Insets(0, 10, 0, 0);
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;

		// Create the user list header
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		this.header = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 1;

		// Create the active user list
		this.userListModel = new DefaultListModel<String>();

		this.userList = new JList<String>(this.userListModel);
		this.userList.setLayoutOrientation(JList.VERTICAL);
		this.userList.setVisibleRowCount(-1);
		this.userList.setForeground(researcherColor);
		this.userList.setCellRenderer(new UserCellRenderer());

		final JScrollPane pane = new JScrollPane(this.userList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		pane.setBorder(BorderFactory.createEmptyBorder());
		pane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		pane.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		this.add(pane, constraints);

		loadProperties();
	}

	/**
	 * Update GUI elements
	 */
	@Override
	public void update(boolean force) {
		this.activeUserHash = this.client.getActiveUserHash();
		final String[] users = new String[this.activeUserHash.size()];
		this.activeUserHash.keySet().toArray(users);
		Arrays.sort(users, new CompareRoles());
		this.header.setText("Active (" + users.length + ")");

		this.idleUserHash = this.client.getPassiveUserHash();
		final String[] idleUsers = new String[this.idleUserHash.size()];
		this.idleUserHash.keySet().toArray(idleUsers);
		Arrays.sort(idleUsers);

		this.userListModel.removeAllElements();
		for (String user : users) {
			this.userListModel.addElement(user);
		}
		this.userListModel.addElement("Idle (" + idleUsers.length + ")");
		for (String user : idleUsers) {
			this.userListModel.addElement(user);
		}

	}

	/**
	 * Custom renderer to color user names based on role.
	 * 
	 */
	private class UserCellRenderer extends DefaultListCellRenderer {

		private static final long	serialVersionUID	= -801444128612741125L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			Color color = null;
			this.setHorizontalAlignment(LEFT);

			// Determine color based on the user role
			if (UserListPanel.this.activeUserHash.get(value) != null) {
				switch (UserListPanel.this.activeUserHash.get(value)) {
					case CALLER:
						color = callerColor;
						break;
					case TYPIST:
						color = typistColor;
						break;
					case RESEARCHER:
						color = researcherColor;
						break;
					default:
						color = idleColor;
						break;
				}
			} else {
				color = idleColor;
				if (index == UserListPanel.this.activeUserHash.size()) {
					this.setHorizontalAlignment(CENTER);
				}
			}

			// Set the color
			this.setForeground(color);
			this.setOpaque(true); // otherwise, it's transparent

			return this;
		}
	}

	/**
	 * Sort user names based on role.
	 * 
	 */
	public class CompareRoles implements Comparator<String> {
		@Override
		public int compare(String s1, String s2) {
			Role r1 = UserListPanel.this.activeUserHash.get(s1);
			Role r2 = UserListPanel.this.activeUserHash.get(s2);

			if (r1.equals(r2)) {
				return s1.compareTo(s2);
			} else {
				return r1.compareTo(r2);
			}

		}
	}

	public void loadProperties() {
		/**
		 * Colors
		 */
		final Color headerBackgroundColor = new Color(Integer.parseInt(
				TriviaClient.PROPERTIES.getProperty("UserList.Header.BackgroundColor"), 16));
		final Color headerColor = new Color(Integer.parseInt(
				TriviaClient.PROPERTIES.getProperty("UserList.Header.Color"), 16));
		final Color backgroundColor = new Color(Integer.parseInt(
				TriviaClient.PROPERTIES.getProperty("UserList.BackgroundColor"), 16));
		researcherColor = new Color(Integer.parseInt(TriviaClient.PROPERTIES.getProperty("UserList.Researcher.Color"),
				16));
		callerColor = new Color(Integer.parseInt(TriviaClient.PROPERTIES.getProperty("UserList.Caller.Color"), 16));
		typistColor = new Color(Integer.parseInt(TriviaClient.PROPERTIES.getProperty("UserList.Typist.Color"), 16));
		idleColor = new Color(Integer.parseInt(TriviaClient.PROPERTIES.getProperty("UserList.Idle.Color"), 16));

		/**
		 * Sizes
		 */
		final int headerHeight = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("UserList.Header.Height"));
		final int width = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("UserList.Width"));

		/**
		 * Font Sizes
		 */
		final float headerFontSize = Float.parseFloat(TriviaClient.PROPERTIES.getProperty("UserList.Header.FontSize"));
		final float fontSize = Float.parseFloat(TriviaClient.PROPERTIES.getProperty("UserList.FontSize"));

		setLabelProperties(this.header, width, headerHeight, headerColor, headerBackgroundColor, headerFontSize);
		this.userList.setBackground(backgroundColor);
		this.userList.setFont(this.userList.getFont().deriveFont(fontSize));

	}

}
