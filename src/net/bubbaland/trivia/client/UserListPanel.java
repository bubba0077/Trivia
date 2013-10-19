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

	private static final long				serialVersionUID		= 4877267114050120590L;

	/**
	 * Colors
	 */
	private static final Color				HEADER_BACKGROUND_COLOR	= Color.DARK_GRAY;
	private static final Color				HEADER_TEXT_COLOR		= Color.WHITE;
	private static final Color				BACKGROUND_COLOR		= Color.LIGHT_GRAY;
	protected static final Color			RESEARCHER_COLOR		= Color.BLACK;
	protected static final Color			CALLER_COLOR			= Color.BLUE;
	protected static final Color			TYPIST_COLOR			= Color.RED;
	protected static final Color			IDLE_COLOR				= Color.GRAY;

	/**
	 * Sizes
	 */
	private static final int				WIDTH					= 85;
	private static final int				HEADER_HEIGHT			= 12;
	private static final int				HEIGHT					= 0;

	/** Font sizes */
	private static final float				FONT_SIZE				= 10f;

	/**
	 * GUI elements that will need to be updated
	 */
	private final JLabel					header;
	private final DefaultListModel<String>	userList;

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
		this.header = this.enclosedLabel("", WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR,
				constraints, FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 1;

		// Create the active user list
		this.userList = new DefaultListModel<String>();

		JList<String> list = new JList<String>(this.userList);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		list.setForeground(RESEARCHER_COLOR);
		list.setBackground(BACKGROUND_COLOR);
		list.setFont(list.getFont().deriveFont(FONT_SIZE));
		list.setCellRenderer(new MyCellRenderer());

		final JScrollPane pane = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		pane.setBorder(BorderFactory.createEmptyBorder());
		pane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		pane.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		this.add(pane, constraints);
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

		this.userList.removeAllElements();
		for (String user : users) {
			this.userList.addElement(user);
		}
		this.userList.addElement("Idle (" + idleUsers.length + ")");
		for (String user : idleUsers) {
			this.userList.addElement(user);
		}

	}

	/**
	 * Custom renderer to color user names based on role.
	 * 
	 */
	private class MyCellRenderer extends DefaultListCellRenderer {

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
						color = CALLER_COLOR;
						break;
					case TYPIST:
						color = TYPIST_COLOR;
						break;
					case RESEARCHER:
						color = RESEARCHER_COLOR;
						break;
					default:
						color = IDLE_COLOR;
						break;
				}
			} else {
				color = IDLE_COLOR;
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

}
