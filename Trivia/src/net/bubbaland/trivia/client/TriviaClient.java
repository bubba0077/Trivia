package net.bubbaland.trivia.client;

//imports for GUI
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.bubbaland.trivia.*;

import java.net.MalformedURLException;
//imports for RMI
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * This class provides the root function for connecting to the trivia server and creating the associated GUI.
 *
 * @author Walter Kolczynski 
 * 
 */
public class TriviaClient extends TriviaPanel  implements ActionListener {

	// The Constant serialVersionUID. 
	private static final long	serialVersionUID	= 5464403297756091690L;

	// The refresh rate of the GUI elements (in milliseconds) 
	final private static int	REFRESH_RATE		= 500;
	// The maximum number of retries the client will make when failing in communication with the server
	protected static final int	MAX_RETRIES			= 10;
	
	// Height of the status bar at the bottom of the GUI	
	final static private int	STATUS_HEIGHT		= 14;
	// Font size of the status text
	final static private float	STATUS_FONT_SIZE	= 12.0f;
	
	
	// URL for RMI server
	final static private String	TRIVIA_SERVER_URL	= "rmi://www.bubbaland.net:1099/TriviaInterface";
	// URL for the IRC client
	final static private String	IRC_CLIENT_URL		= "http://webchat.freenode.net/";
	// IRC channel to join on connection to IRC server
	final static private String	IRC_CHANNEL			= "%23kneedeeptrivia";
	
	// The local trivia object holding all contest data
	private volatile Trivia trivia;
	// The remote server
	private TriviaInterface server;

	/**
	 * GUI Components
	 */	
	// The tabbed pane
	private JTabbedPane		book;
	// Individual pages in the tabbed pane
	private TriviaPanel[]	pages;
	// The status bar at the bottom
	private JLabel			statusBar;
	// The user's name
	private String			user;
	
	/**
	 * Creates a new trivia client GUI
	 *
	 * @param server The RMI Server
	 */
	private TriviaClient( JFrame parent, TriviaInterface server ) {

		// Call parent constructor and use a GridBagLayout
		super( new GridBagLayout() );
		
		// Create a prompt requesting the user name
		new UserLogin( this );
		
		this.server = server;
		
		
		/**
		 * Setup the menu
		 */
		JMenuBar menuBar = new JMenuBar();
		// Add the menu to the parent frame
		parent.setJMenuBar(menuBar);
		
		// Make User Menu
		JMenu menu = new JMenu("User");
		menuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem("Change name", KeyEvent.VK_N);
		menuItem.setActionCommand("Change name");
		menuItem.addActionListener(this);		
		menu.add(menuItem);
		
		// Make Admin Menu pinned to the right
		menuBar.add(Box.createHorizontalGlue());
		menu = new JMenu("Admin");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Load state", KeyEvent.VK_L);
		menuItem.setActionCommand("Load state");
		menuItem.addActionListener(this);		
		menu.add(menuItem);
		
		
		/**
		 * Setup status bar at bottom
		 */		
		// Set up layout constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;				
		
		// Put the status bar at the bottom and do not adjust the size of the status bar
		constraints.gridx = 0;			constraints.gridy = 1;
		constraints.weightx = 0.0;		constraints.weighty = 0.0;
				
		// Create status bar
		this.statusBar = enclosedLabel( "", 0, STATUS_HEIGHT, this.getForeground(), this.getBackground(), constraints,
				STATUS_FONT_SIZE, JLabel.LEFT, JLabel.CENTER );
		
		
		/**
		 *  Create a local copy of the Trivia object
		 */
		int tryNumber = 0;
		boolean success = false;
		while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
			tryNumber++;
			try {
				this.trivia = server.getTrivia();
				success = true;
			} catch ( RemoteException e ) {
				this.log( "Couldn't retrive trivia data from server (try #" + tryNumber + ")." );
				e.printStackTrace();
			}
		}

		// Show disconnected dialog if we could not retrieve the Trivia data
		if ( !success || this.trivia == null ) {
			this.disconnected();
			return;
		}
		
		
		/**
		 * Create browser pane for IRC web client
		 */		
		// Create panel that contains web browser for IRC
		String url = IRC_CLIENT_URL + "?nick=" + user + "&channels=" + IRC_CHANNEL;
		BrowserPanel browser = new BrowserPanel( url );
		browser.setPreferredSize( new Dimension( 0, 204 ) );
		
		/**
		 * Create main content area
		 */		
		// Create the tabbed pane
		this.book = new JTabbedPane();
		
			// Array of the tabs
			pages = new TriviaPanel[8];

			// Create content for workflow tab
			pages[0] = new WorkflowPanel( server, this );
			book.addTab( "Workflow", pages[0] );

			// Create content for current round tab
			pages[1] = new RoundPanel( server, this );
			book.addTab( "Current", pages[1] );

			// Create content for history tab
			pages[2] = new HistoryPanel( server, this );
			book.addTab( "History", pages[2] );

			// Create content for Score by Round tab
			pages[3] = new ScoreByRoundPanel( server, this );
			book.addTab( "By Round", pages[3] );

			// Create place chart
			pages[4] = new PlaceChartPanel( this );
			book.addTab( "Place Chart", pages[4] );

			// Create score by round chart
			pages[5] = new ScoreByRoundChartPanel( this );
			book.addTab( "Score Chart", pages[5] );

			// Create cumulative score chart
			pages[6] = new CumulativePointsChartPanel( this );
			book.addTab( "Cumulative Score Chart", pages[6] );
			
			// Create team copmarison chart
			pages[7] = new TeamComparisonPanel( this );
			book.addTab( "Team Comparison", pages[7] );
		
		/**
		 * Create the split pane separating the tabbed pane and the broswer pane
		 */
		// Put the tabbed pane and browser panel in an adjustable vertical split pane
		JSplitPane splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, this.book, browser );
		// Put the split pane at the top of the window
		constraints.gridx = 0;		constraints.gridy = 0;	
		// When the window resizes, adjust the split pane size
		constraints.weightx = 1.0;	constraints.weighty = 1.0;
		splitPane.setResizeWeight(1.0);
		add( splitPane, constraints );
			
		/**
		 * 
		 */
		// Create timer that will poll server for changes		
		Timer refreshTimer = new Timer( REFRESH_RATE, this );
		refreshTimer.setActionCommand("Timer");
		refreshTimer.start();
		
		// Post welcome to status bar
		this.log( "Welcome " + this.user );
	}

	/**
	 * Create and show the GUI.
	 */
	private static void createAndShowGUI() {
		// Create the application window
		JFrame frame = new JFrame( "Trivia" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		// Initialize server variable
		TriviaInterface triviaServer = null;

		// Initiate connection to RMI server
		int tryNumber = 0;
		boolean success = false;
		while ( tryNumber < MAX_RETRIES && success == false ) {
			tryNumber++;
			try {
				// Connect to RMI server
				triviaServer = (TriviaInterface)Naming.lookup( TRIVIA_SERVER_URL );
			} catch ( MalformedURLException | NotBoundException | RemoteException e ) {
				// Connection failed
				System.out.println("Initial connection to server failed (try #" + tryNumber + ")");	
				
				if(tryNumber == MAX_RETRIES) {
					// Maximum retries reached, pop up disconnected dialog
					String message = "Could not connect to server.";
	
					Object[] options = { "Retry", "Exit" };
					int option = JOptionPane.showOptionDialog( null, message, "Disconnected", JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.ERROR_MESSAGE, null, options, options[1] );
					
					if ( option == JOptionPane.OK_OPTION ) {
						// Retry connection
						tryNumber = 0;
					} else {
						// Exit
						System.exit(0);
					}
				}
			}
		}
		System.out.println( "Connected to trivia server (" + TRIVIA_SERVER_URL + ")." );
				
		// Initialize GUI and place in window
		try {
			frame.add( new TriviaClient( frame, triviaServer ), BorderLayout.CENTER );
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		// Display the window.
		frame.pack();
		frame.setVisible( true );
		
		
		
	}

	/**
	 * Entry point for the client application
	 *
	 * @param args Not used
	 */
	public static void main(String[] args) {
		// Schedule a job to create and show the GUI
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		} );
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		switch(command) {
		
		case "Timer":
			// Triggered by update timer
			this.update();
			break;
		case "Change name":
			// Triggered by change name, prompt for new name
			new UserLogin(this);
			break;
		case "Load state":
			// Triggered by change state, prompt for save file
			new LoadStatePrompt(this.server, this);
			break;
		case "Set team number":
//			new TeamNumberPrompt(this);
			break;
		}
	}

	/**
	 * Gets the user name
	 *
	 * @return the user name
	 */
	public String getUser() {
		return this.user;
	}

	/**
	 * Sets the user name
	 *
	 * @param user the new user name
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Display message in the status bar and in console
	 *
	 * @param message message to log
	 */
	public void log(String message) {
		// Display message in status bar
		this.statusBar.setText( message );
		// Print message to console
		System.out.println( "LOG: " + message );
	}

	/**
	 * Display disconnected dialog box and prompt for action
	 */
	public synchronized void disconnected() {

		String message = "Communication with server failed!";

		Object[] options = { "Retry", "Exit" };
		int option = JOptionPane.showOptionDialog( null, message, "Disconnected", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.ERROR_MESSAGE, null, options, options[1] );
		if ( option == JOptionPane.CANCEL_OPTION ) {
			// Exit the client
			WindowEvent wev = new WindowEvent( (JFrame)this.book.getParent(), WindowEvent.WINDOW_CLOSING );
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent( wev );
			System.exit(0);
		}

	}
	
	/**
	 * Return the local Trivia object. When updating the GUI, always get the current Trivia object first to ensure the
	 * most recent data is used. Components should always use this local version to read data to limit server traffic.
	 * 
	 * @return the local Trivia object
	 */
	public Trivia getTrivia() {
		return this.trivia;
	}

	/**
	 * Convert a cardinal number into its ordinal counterpart
	 *
	 * @param cardinal The number to convert to ordinal form
	 * @return String with the ordinal representation of the number (e.g., 1st, 2nd, 3rd, etc.)
	 */
	public static String ordinalize(int cardinal) {
		// Short-circuit for teen numbers that don't follow normal rules
		if ( 10 < cardinal % 100 && cardinal % 100 < 14 ) { return cardinal + "th"; }
		// Ordinal suffix depends on the ones digit
		int modulus = cardinal % 10;
		switch ( modulus ) {
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

	/**
	 * Update the GUI
	 */
	@Override
	public synchronized void update() {
		
		// Synchronize the local Trivia data to match the server 
		int tryNumber = 0;
		boolean success = false;
		while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
			tryNumber++;
			try {
				this.trivia = server.getTrivia();
				success = true;
			} catch ( RemoteException e ) {
				e.printStackTrace();
				this.log( "Couldn't retrive trivia data from server (try #" + tryNumber + ")." );
			}
		}

		if ( !success ) {
			this.disconnected();
			return;
		}
		
		// Update each individual tab in the GUI		
		for ( TriviaPanel page : pages ) {
			page.update();
		}		
	}

}
