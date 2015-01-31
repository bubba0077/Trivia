package net.bubbaland.trivia.client;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import net.bubbaland.trivia.TriviaChartFactory;
import net.bubbaland.trivia.TriviaInterface;

public class TriviaGUI implements WindowListener {

	// URL for Wiki
	final static protected String					WIKI_URL					= "https://sites.google.com/a/kneedeepintheses.org/information/Home";
	// URL for Issues
	final static protected String					ISSUES_URL					= "https://github.com/bubba0077/Trivia/issues";
	// URL for KVSC
	final static protected String					KVSC_URL					= "http://www.kvsc.org/trivia_news.php";
	// URL base for Visual Trivia Pages
	final static protected String					VISUAL_URL					= "https://sites.google.com/a/kneedeepintheses.org/information/visual-trivia/visual-trivia-";
	// File name of font
	final static private String						FONT_FILENAME				= "fonts/tahoma.ttf";
	// File name to store window positions
	final static private String						DEFAULTS_FILENAME			= ".trivia-defaults";
	// File name to store window positions
	final static private String						SETTINGS_FILENAME			= ".trivia-settings";
	// Settings version to force reloading defaults
	final static private String						SETTINGS_VERSION			= "2";
	// Calendar to track date
	final static private Calendar					TIME						= Calendar.getInstance();
	// Format for log timestamps
	static private SimpleDateFormat					TIMESTAMP_FORMAT;

	//
	final static protected String					NEW_ANSWER_SOUND_FILENAME	= "audio/NewAnswerSound.wav";

	/**
	 * Setup properties
	 */
	final static public Properties					PROPERTIES					= new Properties();
	static {
		/**
		 * Load Nimbus
		 */
		for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if ("Nimbus".equals(info.getName())) {
				try {
					UIManager.setLookAndFeel(new NimbusLookAndFeel() {
						private static final long	serialVersionUID	= -4162111942682867066L;

						@Override
						public UIDefaults getDefaults() {
							UIDefaults ret = super.getDefaults();
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
				} catch (UnsupportedLookAndFeelException exception) {
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
			System.out.println("using defaults");
			loadDefaults();
			PROPERTIES.setProperty("SettingsVersion", SETTINGS_VERSION);
		}

		// Set timestamp format
		TIMESTAMP_FORMAT = new SimpleDateFormat(PROPERTIES.getProperty("TimestampFormat"));
	}

	/** List of available tabs and associated descriptions. */
	final static private Hashtable<String, String>	TAB_DESCRIPTION_HASH;
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

	private TriviaClient							client;

	public TriviaGUI(TriviaInterface server) {
		this.client = null;
		// Initialize list to hold open windows
		this.windowList = new ArrayList<TriviaFrame>(0);

		try {
			this.client = new TriviaClient(server, this);
		} catch (RemoteException exception) {
			// This should never happen
			this.endProgram();
		}

		loadProperties();

		this.client.run();

		// Create a prompt requesting the user name
		String user = PROPERTIES.getProperty("UserName");
		if (user == null) {
			new UserLoginDialog(this.client);
		} else {
			this.client.setUser(user);
		}

		// Create startup frames
		for (int f = 0; PROPERTIES.getProperty("Window" + f) != null; f++) {
			new TriviaFrame(this.client, this, PROPERTIES.getProperty("Window" + f).replaceAll("[\\[\\]]", "")
					.split(", "));
		}

		this.log("Welcome " + this.client.getUser());

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
		final String serverURL = args[0];
		SwingUtilities.invokeLater(new TriviaRunnable(serverURL));
	}

	public TriviaClient getClient() {
		return this.client;
	}

	// List of active windows
	final private ArrayList<TriviaFrame>	windowList;

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
		String timestamp = TIMESTAMP_FORMAT.format(TIME.getTime());
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
		ArrayList<String> windowNames = new ArrayList<String>(0);
		for (TriviaFrame frame : this.windowList) {
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
	public void updateGUI() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for (final TriviaFrame frame : TriviaGUI.this.windowList) {
					frame.updateGUI(false);
				}
			}
		});
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
	 * Create and show the GUI.
	 * 
	 * @param serverURL
	 */
	static void createAndShowGUI(String serverURL) {
		// Initialize server variable
		TriviaInterface triviaServer = null;

		// Initiate connection to RMI server
		int tryNumber = 0;
		boolean success = false;
		while (tryNumber < Integer.parseInt(PROPERTIES.getProperty("MaxRetries")) && success == false) {
			tryNumber++;
			try {
				// Connect to RMI server
				triviaServer = (TriviaInterface) Naming.lookup(serverURL);
				success = true;
			} catch (MalformedURLException | NotBoundException | RemoteException e) {
				// Connection failed
				System.out.println("Initial connection to server failed (try #" + tryNumber + ")");

				if (tryNumber == Integer.parseInt(PROPERTIES.getProperty("MaxRetries"))) {
					final String message = "Could not connect to server.";

					final Object[] options = { "Retry", "Exit" };
					final int option = JOptionPane.showOptionDialog(null, message, "Disconnected",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[1]);

					if (option == JOptionPane.OK_OPTION) {
						// Retry connection
						tryNumber = 0;
					} else {
						// Exit
						System.exit(0);
					}
				}
			}
		}

		System.out.println("Connected to trivia server (" + serverURL + ").");

		new TriviaGUI(triviaServer);
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
		PROPERTIES.setProperty("UserName", this.client.getUser());
		TriviaGUI.savePropertyFile();
		System.exit(0);
	}

	/**
	 * Custom Runnable class to allow passing of command line argument into invokeLater.
	 * 
	 */
	private static class TriviaRunnable implements Runnable {
		private final String	serverURL;

		public TriviaRunnable(String serverURL) {
			this.serverURL = serverURL;
		}

		@Override
		public void run() {
			createAndShowGUI(this.serverURL);
		}
	}
}