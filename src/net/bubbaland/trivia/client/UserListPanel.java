package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
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
	private final JLabel			header;
	private final JList<String>		userList;

	/** Data */
	private Hashtable<String, Role>	userHash;

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
		this.header = this.enclosedLabel("", WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR,
				constraints, FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);

		// Create the user list
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 1;
		this.userList = new JList<String>();
		this.userList.setLayoutOrientation(JList.VERTICAL);
		this.userList.setVisibleRowCount(-1);
		this.userList.setForeground(RESEARCHER_COLOR);
		this.userList.setBackground(BACKGROUND_COLOR);
		this.userList.setFont(this.userList.getFont().deriveFont(FONT_SIZE));
		this.userList.setCellRenderer(new MyCellRenderer());

		// Put the list in a scroll pane
		final JScrollPane pane = new JScrollPane(this.userList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
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
		this.userHash = this.client.getUserHash();
		final String[] users = new String[this.userHash.size()];
		this.userHash.keySet().toArray(users);
		Arrays.sort(users);
		this.header.setText("Active (" + users.length + ")");
		this.userList.setListData(users);
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
			switch (UserListPanel.this.userHash.get(value)) {
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
