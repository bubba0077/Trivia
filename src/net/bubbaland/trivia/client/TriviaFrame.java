package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javazoom.jl.player.Player;
import net.bubbaland.trivia.ClientMessage.ClientMessageFactory;
import net.bubbaland.trivia.Trivia.Role;

/**
 * Creates a top-level window for displaying the trivia GUI.
 * 
 * @author Walter Kolczynski
 * 
 */
public class TriviaFrame extends JFrame implements ChangeListener, ActionListener {

	private static final long serialVersionUID = -3639363131235278472L;

	// Sort menu items
	final private JRadioButtonMenuItem	researcherMenuItem, callerMenuItem, typistMenuItem;
	final private JCheckBoxMenuItem		hideClosedMenuItem;
	final private JCheckBoxMenuItem		hideDuplicatesMenuItem;
	final private JMenuItem			sortTimestampAscendingMenuItem;
	final private JMenuItem			sortTimestampDescendingMenuItem;
	final private JMenuItem			sortQNumberAscendingMenuItem;
	final private JMenuItem			sortQNumberDescendingMenuItem;
	final private JMenuItem			sortStatusAscendingMenuItem;
	final private JMenuItem			sortStatusDescendingMenuItem;
	final private JMenuItem			muteMenuItem;
	final private SpinnerMenuItem		idleSpinner;
	// The status bar at the bottom
	final private JLabel			statusBar;

	final private TriviaClient	client;
	final private TriviaGUI		gui;

	final static private TriviaAudio NEW_ANSWER_PLAYER = new TriviaAudio(TriviaGUI.NEW_ANSWER_SOUND_FILENAME);

	final private DnDTabbedPane tabbedPane;

	/**
	 * Creates a new frame based on a drag-drop event from the tabbed pane in another frame. This is done when a tab
	 * is dragged outside of all other TriviaFrames.
	 * 
	 * @param client
	 *            The root client
	 * @param a_event
	 *            The drag-drop event
	 */
	public TriviaFrame(TriviaClient client, TriviaGUI gui, DropTargetDropEvent a_event, Point location) {
	this(client, gui);
	this.tabbedPane.convertTab(this.tabbedPane.getTabTransferData(a_event),
		this.tabbedPane.getTargetTabIndex(a_event.getLocation()));
	this.tabbedPane.setSelectedIndex(0);
	this.pack();
	this.setLocation(location);
	this.tabbedPane.addChangeListener(this);
	setCursor(null);
	}

	/**
	 * Creates a new frame with specified tabs.
	 * 
	 * @param client
	 *            The root client
	 * @param initialTabs
	 *            Tabs to open initially
	 */
	public TriviaFrame(TriviaClient client, TriviaGUI gui, String[] initialTabs) {
	this(client, gui);
	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	for (final String tabName : initialTabs) {
		this.tabbedPane.addTab(tabName, gui.getTab(this, tabName.replaceFirst(" \\([0-9]*\\)", "")));
	}
	this.tabbedPane.setSelectedIndex(0);
	this.tabbedPane.addChangeListener(this);
	loadPosition();
	setCursor(null);
	}

	/**
	 * Internal constructor containing code common to the public constructors.
	 * 
	 * @param client
	 *            The root client
	 */
	private TriviaFrame(TriviaClient client, TriviaGUI gui) {
	super();
	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

	this.client = client;
	this.gui = gui;

	final String title = this.gui.nextWindowName();
	this.setTitle(title);
	this.setName(title);
	loadPosition();

	// Notify the client this frame exists
	this.gui.registerWindow(this);

	// Create a new panel to hold all GUI elements for the frame
	final TriviaMainPanel panel = new TriviaMainPanel(this.client, this) {
		private static final long serialVersionUID = -3431542881790392652L;

		@Override
		public void updateGUI(boolean forceUpdate) {
		}

		@Override
		protected void loadProperties(Properties properties) {
		}
	};

	/**
	 * Setup the menus
	 */
	{
		final JMenuBar menuBar = new JMenuBar();

		// Make Trivia Menu
		JMenu menu = new JMenu("Trivia");
		menu.setMnemonic(KeyEvent.VK_T);
		menuBar.add(menu);

		final JMenu roleMenu = new JMenu("Change Role...");
		roleMenu.setMnemonic(KeyEvent.VK_R);
		menu.add(roleMenu);

		final ButtonGroup roleOptions = new ButtonGroup();
		this.researcherMenuItem = new JRadioButtonMenuItem("Researcher");
		this.researcherMenuItem.setActionCommand("Researcher");
		this.researcherMenuItem.setMnemonic(KeyEvent.VK_R);
		this.researcherMenuItem.addActionListener(this);
		this.researcherMenuItem.setForeground(UserListPanel.researcherColor);
		roleOptions.add(this.researcherMenuItem);
		roleMenu.add(this.researcherMenuItem);

		this.callerMenuItem = new JRadioButtonMenuItem("Caller");
		this.callerMenuItem.setActionCommand("Caller");
		this.callerMenuItem.setMnemonic(KeyEvent.VK_C);
		this.callerMenuItem.addActionListener(this);
		this.callerMenuItem.setSelected(false);
		this.callerMenuItem.setForeground(UserListPanel.callerColor);
		roleOptions.add(this.callerMenuItem);
		roleMenu.add(this.callerMenuItem);

		this.typistMenuItem = new JRadioButtonMenuItem("Typist");
		this.typistMenuItem.setActionCommand("Typist");
		this.typistMenuItem.setMnemonic(KeyEvent.VK_T);
		this.typistMenuItem.addActionListener(this);
		this.typistMenuItem.setSelected(false);
		this.typistMenuItem.setForeground(UserListPanel.typistColor);
		roleOptions.add(this.typistMenuItem);
		roleMenu.add(this.typistMenuItem);


		JMenuItem menuItem = new JMenuItem("Change Name", KeyEvent.VK_N);
		menuItem.setDisplayedMnemonicIndex(7);
		menuItem.setActionCommand("Change name");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		final JMenu idleMenu = new JMenu("Adjust time-to-idle");
		this.idleSpinner = new SpinnerMenuItem(new SpinnerNumberModel(
			Integer.parseInt(TriviaGUI.PROPERTIES.getProperty("UserList.timeToIdle")), 0, 3600, 60));
		// this.idleSlider.setMajorTickSpacing(10);
		// this.idleSlider.setMinorTickSpacing(5);
		// this.idleSlider.setPaintLabels(true);
		// this.idleSlider.setPaintTicks(true);
		this.idleSpinner.addChangeListener(this);
		idleMenu.add(this.idleSpinner);
		menu.add(idleMenu);

		menuItem = new JMenuItem("Load Default Settings");
		menuItem.setActionCommand("Load Default Settings");
		menuItem.setMnemonic(KeyEvent.VK_D);
		menuItem.setDisplayedMnemonicIndex(5);
		menuItem.addActionListener(this);
		menu.add(menuItem);

		menuItem = new JMenuItem("Exit");
		menuItem.setActionCommand("Exit");
		menuItem.setMnemonic(KeyEvent.VK_X);
		menuItem.addActionListener(this);
		menu.add(menuItem);

		// Make Queue Menu
		menu = new JMenu("Queue");
		menu.setMnemonic(KeyEvent.VK_Q);
		menuBar.add(menu);

		this.muteMenuItem = new JCheckBoxMenuItem("Mute new answer notification");
		this.muteMenuItem.setMnemonic(KeyEvent.VK_M);
		this.muteMenuItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		this.muteMenuItem.setSelected(this.gui.isMute());
		this.muteMenuItem.setActionCommand("Mute");
		this.muteMenuItem.addActionListener(this);
		menu.add(this.muteMenuItem);


		final JMenu filter = new JMenu("Filter...");
		filter.setMnemonic(KeyEvent.VK_F);
		menu.add(filter);

		this.hideClosedMenuItem = new JCheckBoxMenuItem("Hide answers to closed questions");
		this.hideClosedMenuItem.setMnemonic(KeyEvent.VK_H);
		this.hideClosedMenuItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		this.hideClosedMenuItem.setSelected(this.gui.isHideClosed());
		this.hideClosedMenuItem.setActionCommand("Hide Closed");
		this.hideClosedMenuItem.addActionListener(this);
		filter.add(this.hideClosedMenuItem);

		this.hideDuplicatesMenuItem = new JCheckBoxMenuItem("Hide duplicate answers");
		this.hideDuplicatesMenuItem.setMnemonic(KeyEvent.VK_D);
		this.hideDuplicatesMenuItem.setDisplayedMnemonicIndex(5);
		this.hideDuplicatesMenuItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		this.hideDuplicatesMenuItem.setSelected(this.gui.isHideDuplicates());
		this.hideDuplicatesMenuItem.setActionCommand("Hide Duplicates");
		this.hideDuplicatesMenuItem.addActionListener(this);
		filter.add(this.hideDuplicatesMenuItem);

		menuItem = new JMenuItem("Filter by Q#", KeyEvent.VK_N);
		menuItem.setActionCommand("Filter by Number");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		filter.add(menuItem);

		menuItem = new JMenuItem("Filter by Text", KeyEvent.VK_T);
		menuItem.setActionCommand("Filter by Text");
		menuItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItem.addActionListener(this);
		menu.add(menuItem);
		filter.add(menuItem);

		final JMenu sortMenu = new JMenu("Sort by...");
		sortMenu.setMnemonic(KeyEvent.VK_S);

		final JMenu timestampSort = new JMenu("Timestamp");
		timestampSort.setMnemonic(KeyEvent.VK_T);
		sortMenu.add(timestampSort);

		final ButtonGroup sortOptions = new ButtonGroup();
		this.sortTimestampAscendingMenuItem = new JRadioButtonMenuItem("Ascending");
		this.sortTimestampAscendingMenuItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		this.sortTimestampAscendingMenuItem.setMnemonic(KeyEvent.VK_A);
		this.sortTimestampAscendingMenuItem.setActionCommand("Sort Timestamp Ascending");
		this.sortTimestampAscendingMenuItem.addActionListener(this);
		if (this.gui.getQueueSort() == TriviaGUI.QueueSort.TIMESTAMP_ASCENDING) {
		this.sortTimestampAscendingMenuItem.setSelected(true);
		}
		sortOptions.add(this.sortTimestampAscendingMenuItem);
		timestampSort.add(this.sortTimestampAscendingMenuItem);

		this.sortTimestampDescendingMenuItem = new JRadioButtonMenuItem("Descending");
		this.sortTimestampDescendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + ActionEvent.SHIFT_MASK));
		this.sortTimestampDescendingMenuItem.setMnemonic(KeyEvent.VK_D);
		this.sortTimestampDescendingMenuItem.setActionCommand("Sort Timestamp Descending");
		this.sortTimestampDescendingMenuItem.addActionListener(this);
		if (this.gui.getQueueSort() == TriviaGUI.QueueSort.TIMESTAMP_DESCENDING) {
		this.sortTimestampDescendingMenuItem.setSelected(true);
		}
		sortOptions.add(this.sortTimestampDescendingMenuItem);
		timestampSort.add(this.sortTimestampDescendingMenuItem);

		final JMenu qNumberSort = new JMenu("Question Number");
		qNumberSort.setMnemonic(KeyEvent.VK_Q);
		sortMenu.add(qNumberSort);

		this.sortQNumberAscendingMenuItem = new JRadioButtonMenuItem("Ascending");
		this.sortQNumberAscendingMenuItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		this.sortQNumberAscendingMenuItem.setMnemonic(KeyEvent.VK_A);
		this.sortQNumberAscendingMenuItem.setActionCommand("Sort Question Number Ascending");
		this.sortQNumberAscendingMenuItem.addActionListener(this);
		if (this.gui.getQueueSort() == TriviaGUI.QueueSort.QNUMBER_ASCENDING) {
		this.sortQNumberAscendingMenuItem.setSelected(true);
		}
		sortOptions.add(this.sortQNumberAscendingMenuItem);
		qNumberSort.add(this.sortQNumberAscendingMenuItem);

		this.sortQNumberDescendingMenuItem = new JRadioButtonMenuItem("Descending");
		this.sortQNumberDescendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + ActionEvent.SHIFT_MASK));
		this.sortQNumberDescendingMenuItem.setMnemonic(KeyEvent.VK_D);
		this.sortQNumberDescendingMenuItem.setActionCommand("Sort Question Number Descending");
		this.sortQNumberDescendingMenuItem.addActionListener(this);
		if (this.gui.getQueueSort() == TriviaGUI.QueueSort.QNUMBER_DESCENDING) {
		this.sortQNumberDescendingMenuItem.setSelected(true);
		}
		sortOptions.add(this.sortQNumberDescendingMenuItem);
		qNumberSort.add(this.sortQNumberDescendingMenuItem);

		final JMenu statusSort = new JMenu("Status");
		statusSort.setMnemonic(KeyEvent.VK_S);
		sortMenu.add(statusSort);

		this.sortStatusAscendingMenuItem = new JRadioButtonMenuItem("Ascending");
		this.sortStatusAscendingMenuItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		this.sortStatusAscendingMenuItem.setMnemonic(KeyEvent.VK_A);
		this.sortStatusAscendingMenuItem.setActionCommand("Sort Status Ascending");
		this.sortStatusAscendingMenuItem.addActionListener(this);
		if (this.gui.getQueueSort() == TriviaGUI.QueueSort.STATUS_ASCENDING) {
		this.sortStatusAscendingMenuItem.setSelected(true);
		}
		sortOptions.add(this.sortStatusAscendingMenuItem);
		statusSort.add(this.sortStatusAscendingMenuItem);

		this.sortStatusDescendingMenuItem = new JRadioButtonMenuItem("Descending");
		this.sortStatusDescendingMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + ActionEvent.SHIFT_MASK));
		this.sortStatusDescendingMenuItem.setMnemonic(KeyEvent.VK_D);
		this.sortStatusDescendingMenuItem.setActionCommand("Sort Status Descending");
		this.sortStatusDescendingMenuItem.addActionListener(this);
		if (this.gui.getQueueSort() == TriviaGUI.QueueSort.STATUS_DESCENDING) {
		this.sortStatusDescendingMenuItem.setSelected(true);
		}
		sortOptions.add(this.sortStatusDescendingMenuItem);
		statusSort.add(this.sortStatusDescendingMenuItem);

		menu.add(sortMenu);

		// Make Info Menu
		final JMenu infoMenu = new JMenu("External");
		infoMenu.setMnemonic(KeyEvent.VK_E);
		menuItem = new JMenuItem("KVSC");
		menuItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItem.setActionCommand("Open KVSC");
		menuItem.addActionListener(this);
		infoMenu.add(menuItem);

		menuItem = new JMenuItem("Audio Recordings");
		menuItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItem.setActionCommand("Open audio");
		menuItem.addActionListener(this);
		infoMenu.add(menuItem);

		menuItem = new JMenuItem("Wiki", KeyEvent.VK_W);
		menuItem.setActionCommand("Open wiki");
		menuItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItem.addActionListener(this);
		infoMenu.add(menuItem);

		menuItem = new JMenuItem("Bugs/Requests", KeyEvent.VK_I);
		menuItem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItem.setActionCommand("Open issues");
		menuItem.addActionListener(this);
		infoMenu.add(menuItem);

		menuBar.add(infoMenu);

		// Make Admin Menu pinned to the right
		menuBar.add(Box.createHorizontalGlue());
		menu = new JMenu("Admin");
		menu.setMnemonic(KeyEvent.VK_A);
		menuBar.add(menu);

		menuItem = new JMenuItem("Restart Timer", KeyEvent.VK_T);
		menuItem.setActionCommand("Restart Timer");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		menuItem = new JMenuItem("Load State", KeyEvent.VK_L);
		menuItem.setActionCommand("Load state");
		menuItem.addActionListener(this);
		menu.add(menuItem);

		this.setJMenuBar(menuBar);
	}

	// Set up layout constraints
	final GridBagConstraints constraints = new GridBagConstraints();
	constraints.fill = GridBagConstraints.BOTH;

	// Put the status bar at the bottom and do not adjust the size of the status bar
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.weightx = 0.0;
	constraints.weighty = 0.0;

	/**
	 * Setup status bar at bottom
	 */
	// Create status bar
	this.statusBar = panel.enclosedLabel("", 0, 0, this.getForeground(), this.getBackground(), constraints, 0,
		SwingConstants.LEFT, SwingConstants.CENTER);

	// Create drag & drop tabbed pane
	this.tabbedPane = new DnDTabbedPane(this.client, this.gui, this);

	// Setup layout constraints
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.weightx = 1.0;
	constraints.weighty = 1.0;

	// Add the tabbed pane to the panel
	panel.add(this.tabbedPane, constraints);

	// Add the panel to the frame and display the frame
	this.add(panel);
	this.setVisible(true);

	// Load the properties
	this.loadProperties();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
	final String command = e.getActionCommand();
	switch (command) {
		case "Change name":
		// Triggered by change name, prompt for new name
		new UserLoginDialog(this.client);
		break;
		case "Load state":
		// Triggered by change state, prompt for save file
		( new SwingWorker<Void, Void>() {
			public Void doInBackground() {
			TriviaFrame.this.client.sendMessage(ClientMessageFactory.listSaves());
			return null;
			}

			public void done() {

			}
		} ).execute();
		break;
		case "Restart Timer":
		// Triggered by change state, prompt for save file
		( new SwingWorker<Void, Void>() {
			public Void doInBackground() {
			TriviaFrame.this.client.sendMessage(ClientMessageFactory.restartTimer());
			return null;
			}

			public void done() {

			}
		} ).execute();
		break;
		case "Hide Closed":
		// Triggered by change to Hide Closed menu item
		this.gui.setHideClosed(( (JCheckBoxMenuItem) e.getSource() ).isSelected());
		this.updateGUI(true);
		break;
		case "Hide Duplicates":
		// Triggered by change to Hide Duplicate menu item
		this.gui.setHideDuplicates(( (JCheckBoxMenuItem) e.getSource() ).isSelected());
		this.updateGUI(true);
		break;
		case "Filter by Number":
		this.gui.showNumberFilterDialog();
		break;
		case "Filter by Text":
		this.gui.showTextFilterDialog();
		break;
		case "Mute":
		this.gui.setMute(( (JCheckBoxMenuItem) e.getSource() ).isSelected());
		this.updateGUI(true);
		break;
		case "Sort Timestamp Ascending":
		// Triggered by Timestamp Sort menu item
		this.setSort(TriviaGUI.QueueSort.TIMESTAMP_ASCENDING);
		this.updateGUI(true);
		break;
		case "Sort Question Number Ascending":
		// Triggered by Question Number Sort menu item
		this.setSort(TriviaGUI.QueueSort.QNUMBER_ASCENDING);
		this.updateGUI(true);
		break;
		case "Sort Status Ascending":
		// Triggered by Status Sort menu item
		this.setSort(TriviaGUI.QueueSort.STATUS_ASCENDING);
		this.updateGUI(true);
		break;
		case "Sort Timestamp Descending":
		// Triggered by Timestamp Sort menu item
		this.setSort(TriviaGUI.QueueSort.TIMESTAMP_DESCENDING);
		this.updateGUI(true);
		break;
		case "Sort Question Number Descending":
		// Triggered by Question Number Sort menu item
		this.setSort(TriviaGUI.QueueSort.QNUMBER_DESCENDING);
		this.updateGUI(true);
		break;
		case "Sort Status Descending":
		// Triggered by Status Sort menu item
		this.setSort(TriviaGUI.QueueSort.STATUS_DESCENDING);
		this.updateGUI(true);
		break;
		case "Caller":
		// Triggered by Caller Role menu item
		this.client.setRole(Role.CALLER);
		break;
		case "Typist":
		// Triggered by Typist Role menu item
		this.client.setRole(Role.TYPIST);
		break;
		case "Researcher":
		// Triggered by Researcher Role menu item
		this.client.setRole(Role.RESEARCHER);
		break;
		case "Load Default Settings":
		// Triggered by Reset window positions menu item
		this.gui.resetProperties();
		break;
		case "Open wiki":
		// Triggered by Open wiki menu item
		try {
			Desktop.getDesktop().browse(new URI(TriviaGUI.WIKI_URL));
		} catch (IOException | URISyntaxException exception) {
			TriviaFrame.this.log("Couldn't open a browser window");
		}
		break;
		case "Open KVSC":
		// Triggered by Open wiki menu item
		try {
			Desktop.getDesktop().browse(new URI(TriviaGUI.KVSC_URL));
		} catch (IOException | URISyntaxException exception) {
			TriviaFrame.this.log("Couldn't open a browser window");
		}
		break;
		case "Open issues":
		// Triggered by Open wiki menu item
		try {
			Desktop.getDesktop().browse(new URI(TriviaGUI.ISSUES_URL));
		} catch (IOException | URISyntaxException exception) {
			TriviaFrame.this.log("Couldn't open a browser window");
		}
		break;
		case "Open audio":
		// Triggered by Open audio menu item
		try {
			Desktop.getDesktop().browse(new URI(TriviaGUI.AUDIO_URL));
		} catch (IOException | URISyntaxException exception) {
			TriviaFrame.this.log("Couldn't open a browser window");
		}
		break;
		case "Exit":
		// Tell client to exit the program
		this.gui.endProgram();
		break;
	}
	}

	/**
	 * Get the root client.
	 * 
	 * @return The root client
	 */
	// public TriviaClient getClient() {
	// return this.client;
	// }

	/**
	 * Get the answer queue sort method.
	 * 
	 * @return The sort method
	 */
	public TriviaGUI.QueueSort getQueueSort() {
	return this.gui.getQueueSort();
	}

	/**
	 * Get the tabbed content pane.
	 * 
	 * @return The tabbed content pane
	 */
	public DnDTabbedPane getTabbedPane() {
	return this.tabbedPane;
	}

	/**
	 * Get whether answers to closed questions should be hidden in the answer queue.
	 * 
	 * @return Whether answers to closed questions should be hidden
	 */
	public boolean hideClosed() {
	return this.gui.isHideClosed();
	}

	/**
	 * Get whether duplicate answers should be hidden in the answer queue.
	 * 
	 * @return Whether duplicate answers should be hidden
	 */
	public boolean hideDuplicates() {
	return this.gui.isHideDuplicates();
	}

	/**
	 * Load all of the properties from the client and apply them.
	 */
	public void loadProperties() {
	final String id = this.getTitle();

	// Load hide options
	this.gui.setHideClosed(Boolean.parseBoolean(this.loadProperty(id, "HideClosed")));
	this.hideClosedMenuItem.setSelected(this.gui.isHideClosed());
	this.gui.setHideDuplicates(Boolean.parseBoolean(this.loadProperty(id, "HideDuplicates")));
	this.hideDuplicatesMenuItem.setSelected(this.gui.isHideClosed());
	this.gui.setMute(Boolean.parseBoolean(this.loadProperty(id, "Mute")));
	this.muteMenuItem.setSelected(this.gui.isMute());

	// Load queue sort method
	switch (this.loadProperty(id, "QueueSort")) {
		case "Sort Timestamp Ascending":
		this.setSort(TriviaGUI.QueueSort.TIMESTAMP_ASCENDING);
		break;
		case "Sort Question Number Ascending":
		this.setSort(TriviaGUI.QueueSort.QNUMBER_ASCENDING);
		break;
		case "Sort Status Ascending":
		this.setSort(TriviaGUI.QueueSort.STATUS_ASCENDING);
		break;
		case "Sort Timestamp Descending":
		this.setSort(TriviaGUI.QueueSort.TIMESTAMP_DESCENDING);
		break;
		case "Sort Question Number Descending":
		this.setSort(TriviaGUI.QueueSort.QNUMBER_DESCENDING);
		break;
		case "Sort Status Descending":
		this.setSort(TriviaGUI.QueueSort.STATUS_DESCENDING);
		break;
		default:
		this.setSort(TriviaGUI.QueueSort.TIMESTAMP_ASCENDING);
		break;
	}

	// Apply to status bar
	final int height = Integer.parseInt(this.loadProperty(id, "StatusBar.Height"));
	final float fontSize = Float.parseFloat(this.loadProperty(id, "StatusBar.FontSize"));
	this.statusBar.getParent().setPreferredSize(new Dimension(0, height));
	this.statusBar.setFont(this.statusBar.getFont().deriveFont(fontSize));

	// Apply colors to role menu items
	this.researcherMenuItem.setForeground(new Color(
		new BigInteger(TriviaGUI.PROPERTIES.getProperty("UserList.Researcher.Color"), 16).intValue()));
	this.callerMenuItem.setForeground(
		new Color(new BigInteger(TriviaGUI.PROPERTIES.getProperty("UserList.Caller.Color"), 16).intValue()));
	this.typistMenuItem.setForeground(
		new Color(new BigInteger(TriviaGUI.PROPERTIES.getProperty("UserList.Typist.Color"), 16).intValue()));

	// Tell all of the tabs to reload the properties
	for (final String tabName : this.tabbedPane.getTabNames()) {
		final int index = this.tabbedPane.indexOfTab(tabName);
		final Component component = this.tabbedPane.getComponentAt(index);
		if (component instanceof TriviaMainPanel) {
		( (TriviaMainPanel) this.tabbedPane.getComponentAt(index) ).loadProperties(TriviaGUI.PROPERTIES);
		}
	}
	}

	/**
	 * Display message in the status bar and in console
	 * 
	 * @param message
	 *            Message to log
	 */
	public void log(String message) {
	// Display message in status bar
	this.statusBar.setText(message);
	}

	/**
	 * Set the answer queue sort method and save to the settings file.
	 * 
	 * @param newSort
	 *            The new sort method
	 */
	public void setSort(TriviaGUI.QueueSort newSort) {
	this.gui.setQueueSort(newSort);
	switch (newSort) {
		case TIMESTAMP_ASCENDING:
		this.sortTimestampAscendingMenuItem.setSelected(true);
		break;
		case TIMESTAMP_DESCENDING:
		this.sortTimestampDescendingMenuItem.setSelected(true);
		break;
		case QNUMBER_ASCENDING:
		this.sortQNumberAscendingMenuItem.setSelected(true);
		break;
		case QNUMBER_DESCENDING:
		this.sortQNumberDescendingMenuItem.setSelected(true);
		break;
		case STATUS_ASCENDING:
		this.sortStatusAscendingMenuItem.setSelected(true);
		break;
		case STATUS_DESCENDING:
		this.sortStatusDescendingMenuItem.setSelected(true);
		break;
	}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
	if (e.getSource().equals(this.tabbedPane)) {
		if (this.tabbedPane.getTabCount() == 1) {
		// If there are no tabs left, hide the frame
		this.setVisible(false);
		// Wait 100 ms to see if the tab is added back, then close if there are still no tabs
		final Timer timer = new Timer(100, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			if (!TriviaFrame.this.isVisible()) {
				DnDTabbedPane.unregisterTabbedPane(TriviaFrame.this.tabbedPane);
				TriviaFrame.this.gui.unregisterWindow(TriviaFrame.this);
				TriviaFrame.this.dispose();
			}
			}
		});
		timer.setRepeats(false);
		timer.start();
		} else {
		this.setVisible(true);
		}
	} else if (e.getSource().equals(this.idleSpinner)) {
		final int timeToIdle = ( (Integer) this.idleSpinner.getValue() ).intValue();
		TriviaGUI.PROPERTIES.setProperty("UserList.timeToIdle", timeToIdle + "");
		( new SwingWorker<Void, Void>() {
		public Void doInBackground() {
			TriviaFrame.this.client.sendMessage(ClientMessageFactory.setIdleTime(timeToIdle));
			return null;
		}

		public void done() {

		}
		} ).execute();
	}
	}

	public void playNewAnswerSound() {
	if (!this.gui.isMute()) {
		NEW_ANSWER_PLAYER.play();
	}
	}

	public void updateGUI(boolean forceUpdate) {
	// Update role
	final Role role = this.client.getRole();
	while (this.researcherMenuItem == null | this.callerMenuItem == null | this.typistMenuItem == null) {
		// System.out.println("Awaiting menu items");
		try {
		Thread.sleep(50);
		} catch (InterruptedException exception) {
		}
	}
	switch (role) {
		case RESEARCHER:
		this.researcherMenuItem.setSelected(true);
		break;
		case CALLER:
		this.callerMenuItem.setSelected(true);
		break;
		case TYPIST:
		this.typistMenuItem.setSelected(true);
		break;
		default:
		break;
	}

	// Propagate update to tabs
	while (this.tabbedPane == null) {
		// System.out.println("Awaiting tabbed pane");
		try {
		Thread.sleep(50);
		} catch (InterruptedException exception) {
		}
	}
	for (final String tabName : this.tabbedPane.getTabNames()) {
		final int index = this.tabbedPane.indexOfTab(tabName);
		final Component component = this.tabbedPane.getComponentAt(index);
		if (component instanceof TriviaMainPanel) {
		( (TriviaMainPanel) this.tabbedPane.getComponentAt(index) ).updateGUI(forceUpdate);
		}
	}
	}

	/**
	 * Save properties.
	 */
	protected void saveProperties() {
	final String id = this.getTitle();
	TriviaGUI.PROPERTIES.setProperty(id + "." + "HideClosed", this.gui.isHideClosed() + "");
	TriviaGUI.PROPERTIES.setProperty(id + "." + "HideDuplicates", this.gui.isHideDuplicates() + "");
	final int height = this.statusBar.getPreferredSize().getSize().height;
	final float fontSize = this.statusBar.getFont().getSize2D();
	TriviaGUI.PROPERTIES.setProperty(id + "." + "StatusBar.Height", height + "");
	TriviaGUI.PROPERTIES.setProperty(id + "." + "StatusBar.FontSize", fontSize + "");
	TriviaGUI.PROPERTIES.setProperty(id + "." + "OpenTabs", this.tabbedPane.getTabNames().toString());
	}

	/**
	 * Load property for this window name. First looks for property specific to this iteration of TriviaFrame, then
	 * looks to the default version.
	 * 
	 * @param id
	 *            The frame's name
	 * @param propertyName
	 *            The property name
	 * @return The property requested
	 */
	private String loadProperty(String id, String propertyName) {
	return TriviaGUI.PROPERTIES.getProperty(id + "." + propertyName,
		TriviaGUI.PROPERTIES.getProperty(propertyName));
	}

	/**
	 * Load the saved position and size of the window from file. If none found, use preferred size of components.
	 * 
	 */
	protected void loadPosition() {
	try {
		final String frameID = this.getName();

		final int x = Integer.parseInt(TriviaGUI.PROPERTIES.getProperty(frameID + ".X"));
		final int y = Integer.parseInt(TriviaGUI.PROPERTIES.getProperty(frameID + ".Y"));
		final int width = Integer.parseInt(TriviaGUI.PROPERTIES.getProperty(frameID + ".Width"));
		final int height = Integer.parseInt(TriviaGUI.PROPERTIES.getProperty(frameID + ".Height"));

		this.setBounds(x, y, width, height);

	} catch (final NumberFormatException e) {
		// System.out.println("Couldn't load window position, may not exist yet.");
		if (this.tabbedPane == null) {
		this.setSize(600, 600);
		} else {
		this.pack();
		}
		this.setLocationRelativeTo(null);
	}
	}

	private static class TriviaAudio {
	private String filename;

	public TriviaAudio(String filename) {
		this.filename = filename;
	}

	public void play() {
		try {
		final Player player = new Player(TriviaGUI.class.getResourceAsStream(filename));
		new Thread() {
			public void run() {
			try {
				player.play();
			} catch (Exception e) {

			} finally {
				player.close();
			}
			}
		}.start();
		} catch (Exception e) {
		System.out.println("Couldn't open audio file");
		e.printStackTrace();
		}
	}
	}

	// Inner class that defines our special slider menu item
	private class SpinnerMenuItem extends JSpinner implements MenuElement {

	private static final long serialVersionUID = 7803892810923109389L;

	private SpinnerMenuItem() {
		super();
	}

	private SpinnerMenuItem(SpinnerModel model) {
		super(model);
	}

	public void processMouseEvent(MouseEvent e, MenuElement path[], MenuSelectionManager manager) {
	}

	public void processKeyEvent(KeyEvent e, MenuElement path[], MenuSelectionManager manager) {
	}

	public void menuSelectionChanged(boolean isIncluded) {
	}

	public MenuElement[] getSubElements() {
		return new MenuElement[0];
	}

	public Component getComponent() {
		return this;
	}
	}

	public TriviaGUI getGUI() {
	return this.gui;
	}


}
