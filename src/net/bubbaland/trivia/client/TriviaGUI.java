package net.bubbaland.trivia.client;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import net.bubbaland.trivia.TriviaChartFactory;
import net.bubbaland.trivia.client.TristateCheckBox.TristateState;

public class TriviaGUI implements WindowListener {

	// URL for Wiki
	final static protected String	WIKI_URL					= "https://sites.google.com/a/kneedeepintheses.org/information/Home";
	// URL for Issues
	final static protected String	ISSUES_URL					= "https://github.com/bubba0077/Trivia/issues";
	// URL for KVSC
	final static protected String	KVSC_URL					= "http://www.kvsc.org/trivia_news.php";
	// URL base for Visual Trivia Pages
	final static protected String	VISUAL_URL					= "https://sites.google.com/a/kneedeepintheses.org/information/visual-trivia/visual-trivia-";
	// URL for audio trivia
	final static protected String	AUDIO_URL					= "http://trivia.bgsh.org/audio";
	// File name of font
	final static private String		FONT_FILENAME				= "fonts/tahoma.ttf";
	// File name to store window positions
	final static private String		DEFAULTS_FILENAME			= ".trivia-defaults";
	// File name to store window positions
	final static private String		SETTINGS_FILENAME			= ".trivia-settings";
	// Settings version to force reloading defaults
	final static private String		SETTINGS_VERSION			= "21";
	// Calendar to track date
	final static private Calendar	TIME						= Calendar.getInstance();
	// Format for log timestamps
	static private SimpleDateFormat	TIMESTAMP_FORMAT;

	//
	final static protected String	NEW_ANSWER_SOUND_FILENAME	= "audio/NewAnswerSound.wav";

	// The Hide Closed menu item
	// private volatile boolean hideClosed, hideDuplicates, mute;

	/**
	 * Setup properties
	 */
	final static public Properties	PROPERTIES					= new Properties();

	static {
		/**
		 * Load Nimbus
		 */
		for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				try {
					UIManager.setLookAndFeel(new NimbusLookAndFeel() {
						private static final long serialVersionUID = -4162111942682867066L;

						@Override
						public UIDefaults getDefaults() {
							final UIDefaults ret = super.getDefaults();
							Font font;
							try {
								font = Font.createFont(Font.TRUETYPE_FONT,
										TriviaClient.class.getResourceAsStream(FONT_FILENAME));
								ret.put("defaultFont", font.deriveFont(12f));
							} catch (FontFormatException | IOException exception) {
								exception.printStackTrace();
							}
							return ret;
						}

					});
				} catch (final UnsupportedLookAndFeelException exception) {
					exception.printStackTrace();
				}
			}


		}

		/**
		 * Default properties
		 */
		loadDefaults();

		/**
		 * Load saved properties from file
		 */
		final File file = new File(System.getProperty("user.home") + "/" + SETTINGS_FILENAME);
		try {
			final BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
			PROPERTIES.load(fileBuffer);
		} catch (final IOException e) {
			System.out.println("Couldn't load properties file, may not exist yet.");
		}

		/**
		 * If the version doesn't match, reload defaults
		 */
		final String version = PROPERTIES.getProperty("SettingsVersion");
		if (version == null || !version.equals(SETTINGS_VERSION)) {
			System.out.println("Using defaults");
			loadDefaults();
			PROPERTIES.setProperty("SettingsVersion", SETTINGS_VERSION);
		}

		// Set timestamp format
		TIMESTAMP_FORMAT = new SimpleDateFormat(PROPERTIES.getProperty("TimestampFormat"));
	}

	/** List of available tabs and associated descriptions. */
	final static private Hashtable<String, String> TAB_DESCRIPTION_HASH;

	static {
		TAB_DESCRIPTION_HASH = new Hashtable<String, String>(0);
		TAB_DESCRIPTION_HASH.put("Workflow", "Main tab with summary information, open questions, and answer queue.");
		TAB_DESCRIPTION_HASH.put("Current", "Tab showing question data for the current round.");
		TAB_DESCRIPTION_HASH.put("History", "Tab that can show question data for any round.");
		TAB_DESCRIPTION_HASH.put("By Round", "Tab that displays score information for every round.");
		TAB_DESCRIPTION_HASH.put("Place Chart", "Chart showing the team's place in time");
		TAB_DESCRIPTION_HASH.put("Score Chart", "Chart showing the team's score in each round.");
		TAB_DESCRIPTION_HASH.put("Cumul. Score Chart", "Chart showing the team's total score in time.");
		TAB_DESCRIPTION_HASH.put("Team Comparison", "Chart comparing each team's score to our score in time.");
		TAB_DESCRIPTION_HASH.put("*Open Questions", "List of current open questions");
		TAB_DESCRIPTION_HASH.put("*Answer Queue", "The proposed answer queue for the current round.");
	}

	// final private String serverURL;

	final private TriviaClient				client;
	private final WaitDialog				waitDialog;
	private ArrayList<Integer>				qNumberFilter;
	private Pattern							filterTextPattern;
	private volatile TriviaGUI.QueueSort	queueSort;


	public TriviaGUI(final String serverURL) {
		// Initialize list to hold open windows
		// this.serverURL = serverURL;
		this.windowList = new ArrayList<TriviaFrame>(0);

		this.client = new TriviaClient(serverURL, this);
		this.client.run();

		this.filterTextPattern = Pattern.compile("");
		this.qNumberFilter = new ArrayList<Integer>();


		this.loadProperties();

		// Create a prompt requesting the user name
		final String user = PROPERTIES.getProperty("UserName");
		if (user == null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						new UserLoginDialog(TriviaGUI.this.client);
					}
				});
			} catch (InvocationTargetException | InterruptedException exception) {
				exception.printStackTrace();
			}
		} else {
			this.client.setUser(user);
		}

		this.waitDialog = new WaitDialog(TriviaGUI.this);

		while (this.client.getTrivia() == null) {
			// log("Awaiting trivia data...");
			try {
				Thread.sleep(50);
			} catch (final InterruptedException exception) {
				exception.printStackTrace();
				System.exit(2);
			}
		}
		if (this.waitDialog != null) {
			this.waitDialog.dispose();
		}

		// Create startup frames
		for (int f = 0; PROPERTIES.getProperty("Window" + f) != null; f++) {
			final int f1 = f;
			new TriviaFrame(TriviaGUI.this.client, TriviaGUI.this,
					PROPERTIES.getProperty("Window" + f1).replaceAll("[\\[\\]]", "").split(", "));
		}

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					TriviaGUI.this.updateGUI(true);
				}
			});
		} catch (InvocationTargetException | InterruptedException exception) {
			exception.printStackTrace();
		}
		this.log("Welcome " + this.client.getUser());
	}

	public static SimpleDateFormat getTimestampFormat() {
		return TIMESTAMP_FORMAT;
	}

	/**
	 * Entry point for the client application. Only the first argument is used. If the first argument is "useFX", the
	 * client will include an IRC client panel.
	 *
	 * @param args
	 *            Command line arguments; only "useFX" is recognized as an argument
	 *
	 */
	public static void main(String[] args) {
		// Schedule a job to create and show the GUI
		final String serverURL;
		if (args.length > 0) {
			serverURL = args[0];
		} else {
			serverURL = "ws://localhost:1100";
		}

		new TriviaGUI(serverURL);
	}

	public TriviaClient getClient() {
		return this.client;
	}

	// List of active windows
	final private ArrayList<TriviaFrame> windowList;

	/**
	 * Get the number of registered windows.
	 *
	 * @return The number of windows
	 */
	public int getNTriviaWindows() {
		return this.windowList.size();
	}

	/**
	 * Create a panel to add as a tab to a tabbed pane.
	 *
	 * @param frame
	 *            The window that holds the tabbed pane
	 * @param tabName
	 *            The tab name to create
	 * @return The panel to add as a tab
	 */
	public TriviaMainPanel getTab(TriviaFrame frame, String tabName) {
		TriviaMainPanel panel = null;
		switch (tabName) {
			case "Workflow":
				panel = new WorkflowPanel(this.client, frame);
				break;
			case "Current":
				panel = new RoundPanel(this.client, frame);
				break;
			case "History":
				panel = new HistoryPanel(this.client, frame);
				break;
			case "By Round":
				panel = new ScoreByRoundPanel(this.client, frame);
				break;
			case "Place Chart":
				panel = new PlaceChartPanel(this.client, frame);
				break;
			case "Score Chart":
				panel = new ScoreByRoundChartPanel(this.client, frame);
				break;
			case "Cumul. Score Chart":
				panel = new CumulativePointsChartPanel(this.client, frame);
				break;
			case "Team Comparison":
				panel = new TeamComparisonPanel(this.client, frame);
				break;
			case "Open Questions":
				panel = new OpenQuestionsPanel(this.client, frame);
				break;
			case "Answer Queue":
				panel = new AnswerQueuePanel(this.client, frame);
				break;
		}
		return panel;
	}

	/**
	 * Ask all child windows to reload the properties.
	 */
	public void loadProperties() {
		for (final TriviaFrame frame : this.windowList) {
			frame.loadProperties();
		}
		TriviaChartFactory.loadProperties(PROPERTIES);
		TriviaDialogPanel.loadProperties(PROPERTIES);
	}

	/**
	 * Display message in the status bar and in console
	 *
	 * @param message
	 *            Message to log
	 */
	public void log(String message) {
		final String timestamp = TIMESTAMP_FORMAT.format(TIME.getTime());
		for (final TriviaFrame panel : this.windowList) {
			// Display message in status bar
			panel.log(timestamp + " " + message);
		}
		// Print message to console
		System.out.println(timestamp + " " + message);
	}

	/**
	 * Get the name for the next top-level frame.
	 *
	 * @return The frame name
	 */
	public String nextWindowName() {
		final ArrayList<String> windowNames = new ArrayList<String>(0);
		for (final TriviaFrame frame : this.windowList) {
			windowNames.add(frame.getTitle());
		}
		String name = "Trivia";
		for (int i = 1; windowNames.contains(name); i++) {
			name = "Trivia (" + i + ")";
		}
		return name;
	}

	/**
	 * Register a window as a child of the client. New Trivia Frames do this so the client can track events from them.
	 *
	 * @param frame
	 *            The window to track
	 */
	public void registerWindow(TriviaFrame frame) {
		frame.addWindowListener(this);
		this.windowList.add(frame);
	}

	/**
	 * Reset properties to defaults, then ask all child windows to load the new properties.
	 */
	public void resetProperties() {
		loadDefaults();
		this.loadProperties();
	}

	/**
	 * Unregister a window as a child of the client. This is done when a window closes.
	 *
	 * @param frame
	 *            The window to stop tracking
	 */
	public void unregisterWindow(TriviaFrame frame) {
		frame.removeWindowListener(this);
		this.windowList.remove(frame);
	}

	/**
	 * Update all of the child windows.
	 */
	public void updateGUI(final boolean force) {
		for (final TriviaFrame frame : TriviaGUI.this.windowList) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame.updateGUI(force);
				}
			});
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	/**
	 * When one of the windows tries to close, save the properties and position of the window first. Then exit the
	 * program if there are no open windows left.
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		final Window window = e.getWindow();
		// Same the window position
		savePosition(window);
		if (window instanceof TriviaFrame) {
			( (TriviaFrame) window ).saveProperties();
			DnDTabbedPane.unregisterTabbedPane(( (TriviaFrame) window ).getTabbedPane());

			if (this.windowList.size() == 1) {
				// This is the last window, go through exit procedures
				this.endProgram();
			} else {
				// Remove window from the list
				this.unregisterWindow((TriviaFrame) window);
			}
		}
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	/**
	 * Get the description associated with a tab name
	 *
	 * @param tabName
	 *            The tab name
	 * @return The description associated with the tab name
	 */
	public static String getTabDescription(String tabName) {
		return TAB_DESCRIPTION_HASH.get(tabName);
	}

	/**
	 * Get a list of available tab names.
	 *
	 * @return The available tab names
	 */
	public static Set<String> getTabNames() {
		return TAB_DESCRIPTION_HASH.keySet();
	}

	/**
	 * Clear all saved data from file.
	 *
	 */
	public static void loadDefaults() {
		PROPERTIES.clear();
		final InputStream defaults = TriviaClient.class.getResourceAsStream(DEFAULTS_FILENAME);
		try {
			PROPERTIES.load(defaults);
		} catch (final IOException e) {
			System.out.println("Couldn't load default properties file, aborting!");
			System.exit(-1);
		}
	}

	/**
	 * Save the position and size of the window to file.
	 *
	 * @param window
	 *            The window whose size and position is to be saved
	 *
	 */
	public static void savePosition(Window window) {
		final Rectangle r = window.getBounds();
		final int x = (int) r.getX();
		final int y = (int) r.getY();
		final int width = (int) r.getWidth();
		final int height = (int) r.getHeight();

		final String frameID = window.getName();

		PROPERTIES.setProperty(frameID + ".X", x + "");
		PROPERTIES.setProperty(frameID + ".Y", y + "");
		PROPERTIES.setProperty(frameID + ".Width", width + "");
		PROPERTIES.setProperty(frameID + ".Height", height + "");
	}

	/**
	 * Save the current properties to the settings file.
	 */
	static void savePropertyFile() {
		final File file = new File(System.getProperty("user.home") + "/" + SETTINGS_FILENAME);
		try {
			final BufferedWriter outfileBuffer = new BufferedWriter(new FileWriter(file));
			PROPERTIES.store(outfileBuffer, "Trivia");
			outfileBuffer.close();
		} catch (final IOException e) {
			System.out.println("Error saving properties.");
		}
	}

	/**
	 * Display disconnected dialog box and prompt for action
	 */
	public synchronized void disconnected() {

		final String message = "Communication with server interrupted!";

		final Object[] options = { "Retry", "Exit" };
		final int option = JOptionPane.showOptionDialog(null, message, "Disconnected", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.ERROR_MESSAGE, null, options, options[1]);
		if (option == 1) {
			// Exit the client
			System.exit(0);
		}

		this.client.run();
	}

	/**
	 * Add the current window contents to properties, then save the properties to the settings file and exit.
	 */
	protected void endProgram() {
		// Remove previously saved windows
		for (int f = 0; TriviaGUI.PROPERTIES.getProperty("Window" + f) != null; f++) {
			PROPERTIES.remove("Window" + f);
		}
		// Save tabs in all windows
		for (int f = 0; f < this.getNTriviaWindows(); f++) {
			final String[] tabNames = this.windowList.get(f).getTabbedPane().getTabNames();
			PROPERTIES.setProperty("Window" + f, Arrays.toString(tabNames));
			this.windowList.get(f).saveProperties();
			TriviaGUI.savePosition(this.windowList.get(f));
		}
		if (this.client.getUser() != null) {
			PROPERTIES.setProperty("UserName", this.client.getUser());
		}
		TriviaGUI.savePropertyFile();
		System.exit(0);
	}

	/**
	 * Convert a cardinal number into its ordinal counterpart.
	 *
	 * @param cardinal
	 *            The number to convert to ordinal form
	 * @return String with the ordinal representation of the number (e.g., 1st, 2nd, 3rd, etc.)
	 *
	 */
	public static String ordinalize(int cardinal) {
		// Short-circuit for teen numbers that don't follow normal rules
		if (10 < cardinal % 100 && cardinal % 100 < 14) return cardinal + "th";
		// Ordinal suffix depends on the ones digit
		final int modulus = cardinal % 10;
		switch (modulus) {
			case 1:
				return cardinal + "st";
			case 2:
				return cardinal + "nd";
			case 3:
				return cardinal + "rd";
			default:
				return cardinal + "th";
		}
	}

	public boolean isHideClosed() {
		return PROPERTIES.getProperty("HideClosed").equals("true");
	}

	void setHideClosed(boolean hideClosed) {
		PROPERTIES.setProperty("HideClosed", hideClosed + "");
		this.updateGUI(true);
	}

	public boolean isHideDuplicates() {
		return PROPERTIES.getProperty("HideDuplicates").equals("true");
	}

	void setHideDuplicates(boolean hideDuplicates) {
		PROPERTIES.setProperty("HideDuplicates", hideDuplicates + "");
		this.updateGUI(true);
	}

	public boolean isMute() {
		return PROPERTIES.getProperty("Mute").equals("true");
	}

	void setMute(boolean mute) {
		PROPERTIES.setProperty("Mute", mute + "");
		this.updateGUI(true);
	}

	void resetNumberFilter() {
		this.qNumberFilter = new ArrayList<Integer>(0);
		this.updateGUI(true);
	}

	void resetTextFilter() {
		this.filterTextPattern = Pattern.compile("");
		this.updateGUI(true);
	}

	public Pattern getFilterTextPattern() {
		return this.filterTextPattern;
	}

	public ArrayList<Integer> getFilterNumbers() {
		return this.qNumberFilter;
	}

	void showNumberFilterDialog() {
		new NumberFilterDialog();
	}

	public TriviaGUI.QueueSort getQueueSort() {
		return this.queueSort;
	}

	public void setQueueSort(TriviaGUI.QueueSort queueSort) {
		this.queueSort = queueSort;
	}

	private class NumberFilterDialog extends TriviaDialogPanel implements ActionListener {

		private static final long		serialVersionUID	= -3726819964211373465L;

		final private JCheckBox[]		checkboxes;
		final private TristateCheckBox	checkallBox;
		final private static int		nPerColumn			= 5;

		public NumberFilterDialog() {
			super();

			final int maxQuestions = TriviaGUI.this.client.getTrivia().getMaxQuestions();

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.CENTER;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;

			final JLabel label = new JLabel("Select Questions to Show", SwingConstants.CENTER);
			constraints.gridwidth = (int) Math.ceil(( maxQuestions + 0.0 ) / nPerColumn);
			constraints.gridx = 0;
			constraints.gridy = 0;
			this.add(label, constraints);

			final ArrayList<Integer> filterNumbers = TriviaGUI.this.getFilterNumbers();

			constraints.gridx = 0;
			constraints.gridy = 1;
			this.checkallBox = new TristateCheckBox("All");
			this.checkallBox.setName("All");
			this.checkallBox.addActionListener(this);
			this.add(this.checkallBox, constraints);

			constraints.gridwidth = 1;

			this.checkboxes = new JCheckBox[maxQuestions];
			for (int i = 0; i < this.checkboxes.length; i++) {
				this.checkboxes[i] = new JCheckBox("Q" + ( i + 1 ));
				this.checkboxes[i].setName(( i + 1 ) + "");
				this.checkboxes[i].setSelected(!filterNumbers.contains(i + 1));
				this.checkboxes[i].addActionListener(this);
				constraints.gridx = i / nPerColumn;
				constraints.gridy = 2 + i % nPerColumn;
				this.add(this.checkboxes[i], constraints);
			}

			if (this.allChecked()) {
				this.checkallBox.setState(TristateState.SELECTED);
			} else if (this.noneChecked()) {
				this.checkallBox.setState(TristateState.DESELECTED);
			} else {
				this.checkallBox.setState(TristateState.INDETERMINATE);
			}

			// Display the dialog box
			this.dialog = new TriviaDialog(null, "Set Answer Filter", this, JOptionPane.PLAIN_MESSAGE,
					JOptionPane.OK_CANCEL_OPTION);
			this.dialog.setName("Answer Filter");
			this.dialog.setVisible(true);
		}

		@Override
		public void windowClosed(WindowEvent event) {
			super.windowClosed(event);
			// If the OK button was pressed, add the proposed answer to the queue
			final int option = ( (Integer) this.dialog.getValue() ).intValue();
			if (option == JOptionPane.OK_OPTION) {
				TriviaGUI.this.qNumberFilter = new ArrayList<Integer>(0);
				for (final JCheckBox checkbox : this.checkboxes) {
					if (!checkbox.isSelected()) {
						TriviaGUI.this.qNumberFilter.add(Integer.parseInt(checkbox.getName()));
					}
				}
				TriviaGUI.this.updateGUI(true);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("Item State Changed");
			final JCheckBox source = (JCheckBox) e.getSource();
			if (source.getName() == "All") {
				System.out.println(( (TristateCheckBox) source ).getState().name());
				switch (( (TristateCheckBox) source ).getState()) {
					case DESELECTED:
						this.uncheckAll();
						break;
					case INDETERMINATE:
						break;
					case SELECTED:
						this.checkAll();
						break;
				}
			} else {
				if (this.allChecked()) {
					this.checkallBox.setState(TristateState.SELECTED);
				} else if (this.noneChecked()) {
					this.checkallBox.setState(TristateState.DESELECTED);
				} else {
					this.checkallBox.setState(TristateState.INDETERMINATE);
				}
			}
		}

		private boolean allChecked() {
			boolean allChecked = true;
			for (int i = 0; allChecked && i < this.checkboxes.length; i++) {
				allChecked = allChecked && this.checkboxes[i].isSelected();
			}
			return allChecked;
		}

		private boolean noneChecked() {
			boolean noneChecked = true;
			for (int i = 0; noneChecked && i < this.checkboxes.length; i++) {
				noneChecked = noneChecked && !this.checkboxes[i].isSelected();
			}
			return noneChecked;
		}

		private void uncheckAll() {
			for (final JCheckBox checkboxe : this.checkboxes) {
				checkboxe.setSelected(false);
			}
		}

		private void checkAll() {
			for (final JCheckBox checkboxe : this.checkboxes) {
				checkboxe.setSelected(true);
			}
		}
	}

	public void setTextFilter(String filterText) {
		this.filterTextPattern = Pattern.compile(filterText);
		TriviaGUI.this.updateGUI(true);
	}

	// Queue sort option
	public static enum QueueSort {
		TIMESTAMP_ASCENDING, QNUMBER_ASCENDING, STATUS_ASCENDING, TIMESTAMP_DESCENDING, QNUMBER_DESCENDING, STATUS_DESCENDING
	}
}
