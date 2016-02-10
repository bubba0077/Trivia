package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import net.bubbaland.trivia.User;
import net.bubbaland.trivia.User.Role;

/**
 * Creates a panel that displays active and idle user names.
 *
 * @author Walter Kolczynski
 *
 */
public class UserListPanel extends TriviaMainPanel {

	private static final long				serialVersionUID	= 4877267114050120590L;

	/**
	 * Role Icons
	 */
	private static ImageIcon				callerIcon, pencilIcon;

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
	private final DefaultListModel<User>	userListModel;
	private final JList<User>				userList;

	public UserListPanel(TriviaClient client, TriviaFrame parent) {
		super(client, parent);

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
		this.userListModel = new DefaultListModel<User>();

		this.userList = new JList<User>(this.userListModel);
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

		this.loadProperties(TriviaGUI.PROPERTIES);
	}

	@Override
	public void loadProperties(Properties properties) {
		/**
		 * Colors
		 */
		final Color headerBackgroundColor = new Color(
				Integer.parseInt(properties.getProperty("UserList.Header.BackgroundColor"), 16));
		final Color headerColor = new Color(Integer.parseInt(properties.getProperty("UserList.Header.Color"), 16));
		final Color backgroundColor = new Color(
				Integer.parseInt(properties.getProperty("UserList.BackgroundColor"), 16));
		researcherColor = new Color(Integer.parseInt(properties.getProperty("UserList.Researcher.Color"), 16));
		callerColor = new Color(Integer.parseInt(properties.getProperty("UserList.Caller.Color"), 16));
		typistColor = new Color(Integer.parseInt(properties.getProperty("UserList.Typist.Color"), 16));
		idleColor = new Color(Integer.parseInt(properties.getProperty("UserList.Idle.Color"), 16));

		/**
		 * Sizes
		 */
		final int headerHeight = Integer.parseInt(properties.getProperty("UserList.Header.Height"));
		final int width = Integer.parseInt(properties.getProperty("UserList.Width"));

		/**
		 * Font Sizes
		 */
		final float headerFontSize = Float.parseFloat(properties.getProperty("UserList.Header.FontSize"));
		final float fontSize = Float.parseFloat(properties.getProperty("UserList.FontSize"));

		setLabelProperties(this.header, width, headerHeight, headerColor, headerBackgroundColor, headerFontSize);
		this.userList.setBackground(backgroundColor);
		this.userList.setFont(this.userList.getFont().deriveFont(fontSize));

		final int fontSizeInt = new Float(fontSize).intValue();

		callerIcon = new ImageIcon(new ImageIcon(AnswerQueuePanel.class.getResource("images/phone.png")).getImage()
				.getScaledInstance(fontSizeInt, fontSizeInt, Image.SCALE_SMOOTH));

		pencilIcon = new ImageIcon(new ImageIcon(AnswerQueuePanel.class.getResource("images/pencil.png")).getImage()
				.getScaledInstance(fontSizeInt, fontSizeInt, Image.SCALE_SMOOTH));

	}

	/**
	 * Update GUI elements
	 */
	@Override
	public void updateGUI(boolean force) {
		ArrayList<User> activeUsers = new ArrayList<User>();
		ArrayList<User> idleUsers = new ArrayList<User>();

		Duration timeSinceLastActive = this.client.getUser().timeSinceLastActive();
		Duration activeWindow = Duration.ofSeconds(UserListPanel.this.client.getTimeToIdle());

		for (User user : this.client.getUserList()) {
			if (!activeWindow.isZero() && timeSinceLastActive.compareTo(activeWindow) > 0) {
				idleUsers.add(user);
			} else {
				activeUsers.add(user);
			}
		}
		this.header.setText("Active (" + activeUsers.size() + ")");
		if (!activeUsers.isEmpty()) {
			activeUsers.sort(new CompareActiveUsers());
		}
		if (!idleUsers.isEmpty()) {
			idleUsers.sort(new CompareIdleUsers());
		}

		this.userListModel.removeAllElements();
		for (final User user : activeUsers) {
			this.userListModel.addElement(user);
		}

		User idleUser = new User();
		idleUser.setUserName("Idle (" + idleUsers.size() + ")");
		idleUser.setRole(Role.IDLE);
		this.userListModel.addElement(idleUser);

		for (final User user : idleUsers) {
			this.userListModel.addElement(user);
		}
	}

	/**
	 * Sort user names based on role.
	 *
	 */
	public class CompareActiveUsers implements Comparator<User> {
		@Override
		public int compare(User s1, User s2) {
			return s1.compareTo(s2);
		}
	}

	/**
	 * Sort user names based on role.
	 *
	 */
	public class CompareIdleUsers implements Comparator<User> {
		@Override
		public int compare(User s1, User s2) {
			return s1.getUserName().compareTo(s2.getUserName());
		}
	}

	/**
	 * Custom renderer to color user names based on role.
	 *
	 */
	private class UserCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -801444128612741125L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object userName, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, userName, index, isSelected, cellHasFocus);
			Color color = null;
			ImageIcon icon = null;
			this.setHorizontalAlignment(LEFT);
			this.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

			// Determine color based on the user role
			// int activeWindow = UserListPanel.this.client.getTimeToIdle() * 1000;
			User user = (User) userName;

			switch (user.getRole()) {
				case CALLER:
					color = callerColor;
					icon = callerIcon;
					break;
				case TYPIST:
					color = typistColor;
					icon = pencilIcon;
					break;
				case RESEARCHER:
					color = researcherColor;
					break;
				case IDLE:
					color = idleColor;
					this.setHorizontalAlignment(CENTER);
					break;
				default:
					color = idleColor;
					break;
			}

			Duration timeSinceLastActive = user.timeSinceLastActive();
			Duration timeSinceLastRollChange = user.timeSinceLastRollChange();
			Duration activeWindow = Duration.ofSeconds(UserListPanel.this.client.getTimeToIdle());

			if (!activeWindow.isZero() && timeSinceLastActive.compareTo(activeWindow) > 0) {
				// Idle User
				color = idleColor;
			}

			final String activeTimestamp = durationToString(timeSinceLastActive);
			final String rollTimestamp = durationToString(timeSinceLastRollChange);

			if (user.getRole() != Role.IDLE) {
				this.setToolTipText("<html>" + userName + "<BR>Role: " + user.getRole() + "<BR>Idle for "
						+ activeTimestamp + "<BR>In role for " + rollTimestamp + "</html>");
			}

			// Set the color
			this.setForeground(color);
			this.setIcon(icon);
			this.setOpaque(true); // otherwise, it's transparent

			return this;
		}
	}

	private static String durationToString(Duration duration) {
		String durationString = duration.getSeconds() % 60 + "s";
		if (duration.toMinutes() > 0) {
			durationString = duration.toMinutes() % 60 + "m " + durationString;
			if (duration.toHours() > 0) {
				durationString = duration.toHours() + "h " + durationString;
			}
		}
		return durationString;
	}

}
