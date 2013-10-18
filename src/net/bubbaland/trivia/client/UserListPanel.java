package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import net.bubbaland.trivia.UserList.Role;

/**
 * Creates a panel that displays active user names.
 * 
 * @author Walter Kolczynski
 * 
 */
public class UserListPanel extends TriviaPanel {

	private static final long		serialVersionUID		= 4877267114050120590L;

	/**
	 * Colors
	 */
	private static final Color		HEADER_BACKGROUND_COLOR	= Color.DARK_GRAY;
	private static final Color		HEADER_TEXT_COLOR		= Color.WHITE;
	private static final Color		BACKGROUND_COLOR		= Color.LIGHT_GRAY;
	protected static final Color	RESEARCHER_COLOR		= Color.BLACK;
	protected static final Color	CALLER_COLOR			= Color.BLUE;
	protected static final Color	TYPER_COLOR				= Color.RED;
	protected static final Color	IDLE_COLOR				= Color.DARK_GRAY;

	/**
	 * Sizes
	 */
	private static final int		WIDTH					= 85;
	private static final int		HEADER_HEIGHT			= 12;
	private static final int		HEIGHT					= 0;

	/** Font sizes */
	private static final float		FONT_SIZE				= 10f;

	/**
	 * GUI elements that will need to be updated
	 */
	private final JLabel			header, idleHeader;
	private final JList<String>		activeUserList;
	private final JList<String>		passiveUserList;

	/** Data */
	private Hashtable<String, Role>	activeUserHash;
	private Hashtable<String, Role>	passiveUserHash;

	/** Data sources */
	private final TriviaClient		client;

	public UserListPanel(TriviaClient client) {

		super();
		this.client = client;

		// Set up layout contraints
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
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(BACKGROUND_COLOR);
		panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		panel.setMinimumSize(new Dimension(WIDTH, HEIGHT));

		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 1;
		// Put the list in a scroll pane
		final JScrollPane pane = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		pane.setBorder(BorderFactory.createEmptyBorder());
		pane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		pane.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		this.add(pane, constraints);

		// Create the active user list
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(0, 0, 0, 0);
		this.activeUserList = new JList<String>();
		this.activeUserList.setLayoutOrientation(JList.VERTICAL);
		this.activeUserList.setVisibleRowCount(-1);
		this.activeUserList.setForeground(RESEARCHER_COLOR);
		this.activeUserList.setBackground(BACKGROUND_COLOR);
		this.activeUserList.setFont(this.activeUserList.getFont().deriveFont(FONT_SIZE));
		this.activeUserList.setCellRenderer(new MyCellRenderer());
		panel.add(this.activeUserList, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		this.idleHeader = new JLabel("", SwingConstants.CENTER);
		this.idleHeader.setVerticalAlignment(SwingConstants.CENTER);
		this.idleHeader.setFont(this.idleHeader.getFont().deriveFont(FONT_SIZE));
		this.idleHeader.setForeground(IDLE_COLOR);
		panel.add(this.idleHeader, constraints);

		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 2;
		this.passiveUserList = new JList<String>();
		this.passiveUserList.setLayoutOrientation(JList.VERTICAL);
		this.passiveUserList.setVisibleRowCount(-1);
		this.passiveUserList.setForeground(IDLE_COLOR);
		this.passiveUserList.setBackground(BACKGROUND_COLOR);
		this.passiveUserList.setFont(this.passiveUserList.getFont().deriveFont(FONT_SIZE));
		panel.add(this.passiveUserList, constraints);
	}

	/**
	 * Update GUI elements
	 */
	@Override
	public void update(boolean force) {
		this.activeUserHash = this.client.getActiveUserHash();
		final String[] users = new String[this.activeUserHash.size()];
		this.activeUserHash.keySet().toArray(users);
		Arrays.sort(users);
		this.header.setText("Active (" + users.length + ")");
		this.activeUserList.setListData(users);

		this.passiveUserHash = this.client.getPassiveUserHash();
		final String[] idleUsers = new String[this.passiveUserHash.size()];
		this.passiveUserHash.keySet().toArray(idleUsers);
		Arrays.sort(idleUsers);
		this.idleHeader.setText("Idle (" + idleUsers.length + ")");
		this.passiveUserList.setListData(idleUsers);
	}

	/**
	 * Custom renderer to color user names based on role.
	 * 
	 */
	private class MyCellRenderer extends DefaultListCellRenderer {

		private static final long	serialVersionUID	= -801444128612741125L;

		@Override
		@SuppressWarnings("rawtypes")
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			Color color = null;

			// Determine color based on the user role
			switch (UserListPanel.this.activeUserHash.get(value)) {
				case CALLER:
					color = CALLER_COLOR;
					break;
				case TYPER:
					color = TYPER_COLOR;
					break;
				case RESEARCHER:
				default:
					color = RESEARCHER_COLOR;
					break;
			}

			// Set the color
			this.setForeground(color);
			this.setOpaque(true); // otherwise, it's transparent

			return this;
		}
	}

}
