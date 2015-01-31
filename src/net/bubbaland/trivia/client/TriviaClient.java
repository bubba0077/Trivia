package net.bubbaland.trivia.client;

// imports for RMI
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

import javax.swing.JOptionPane;
import net.bubbaland.trivia.Round;
import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaClientInterface;
import net.bubbaland.trivia.TriviaInterface;
import net.bubbaland.trivia.UserList.Role;


/**
 * Provides the root functionality for connecting to the trivia server and creating the associated GUI.
 * 
 * @author Walter Kolczynski
 * 
 */
public class TriviaClient extends UnicastRemoteObject implements TriviaClientInterface, Runnable {

	/**
	 * 
	 */
	private static final long					serialVersionUID	= 1L;


	// The user's name
	private volatile String						user;
	// The user's role
	private volatile Role						role;


	// Hashtable of active users and roles
	private volatile Hashtable<String, Role>	activeUserHash;
	// Hashtable of idle users and roles
	private volatile Hashtable<String, Role>	idleUserHash;

	// The remote server
	private final TriviaInterface				server;
	// The local trivia object holding all contest data
	private volatile Trivia						trivia;

	private final TriviaGUI						gui;

	/**
	 * Creates a new trivia client GUI
	 * 
	 * @param server
	 *            The RMI Server
	 */
	public TriviaClient(TriviaInterface server, TriviaGUI gui) throws RemoteException {
		this.server = server;
		this.gui = gui;

		this.activeUserHash = new Hashtable<String, Role>(0);
		this.idleUserHash = new Hashtable<String, Role>(0);
		this.user = null;
		this.role = Role.RESEARCHER;
		this.trivia = null;
	}

	public int[] getVersions() {
		return this.trivia.getVersions();
	}

	public int getCurrentRoundNumber() {
		return this.trivia.getCurrentRoundNumber();
	}

	/**
	 * Display disconnected dialog box and prompt for action
	 */
	public synchronized void disconnected() {

		final String message = "Communication with server failed!";

		final Object[] options = { "Retry", "Exit" };
		final int option = JOptionPane.showOptionDialog(null, message, "Disconnected", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.ERROR_MESSAGE, null, options, options[1]);
		if (option == 1) {
			// Exit the client
			System.exit(0);
		}

	}

	public void log(String message) {
		this.gui.log(message);
	}

	/**
	 * Get the hash of active users and roles.
	 * 
	 * @return The hashtable of users and roles
	 */
	public Hashtable<String, Role> getActiveUserHash() {
		return this.activeUserHash;
	}

	/**
	 * Get the hash of idle users and roles.
	 * 
	 * @return The hashtable of users and roles
	 */
	public Hashtable<String, Role> getIdleUserHash() {
		return this.idleUserHash;
	}

	/**
	 * Get the current user role.
	 * 
	 * @return The user's role
	 */
	public Role getRole() {
		return this.role;
	}

	/**
	 * Get the remote server handle to allow interaction with the server.
	 * 
	 * @return The remote server handle
	 */
	public TriviaInterface getServer() {
		return this.server;
	}

	/**
	 * Return the local Trivia object. When updating the GUI, always get the current Trivia object first to ensure the
	 * most recent data is used. Components should always use this local version to read data to limit server traffic.
	 * 
	 * @return The local Trivia object
	 */
	public Trivia getTrivia() {
		return this.trivia;
	}

	/**
	 * Gets the user name.
	 * 
	 * @return The user name
	 */
	public String getUser() {
		return this.user;
	}

	/**
	 * Sets the user name.
	 * 
	 * @param user
	 *            The new user name
	 */
	public void setUser(String user) {
		int tryNumber = 0;
		boolean success = false;
		while (tryNumber < Integer.parseInt(TriviaGUI.PROPERTIES.getProperty("MaxRetries")) && success == false) {
			tryNumber++;
			try {
				if (this.user == null) {
					this.server.setRole(user, this.role);
				} else {
					this.server.changeUser(this.user, user);
				}
				success = true;
			} catch (final RemoteException e) {
				this.gui.log("Couldn't change user name on server (try #" + tryNumber + ").");
			}
		}

		if (!success) {
			this.disconnected();
			return;
		}

		this.user = user;
	}

	/**
	 * Change the user's role.
	 * 
	 * @param role
	 */
	protected void setRole(Role role) {
		if (this.user != null) {
			int tryNumber = 0;
			boolean success = false;
			while (tryNumber < Integer.parseInt(TriviaGUI.PROPERTIES.getProperty("MaxRetries")) && success == false) {
				tryNumber++;
				try {
					this.server.setRole(this.user, role);
					success = true;
				} catch (final RemoteException e) {
					this.gui.log("Couldn't change role server (try #" + tryNumber + ").");
				}
			}
		} else {
			this.gui.log("Couldn't set role yet, no user name specified");
		}
		this.role = role;
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

	public synchronized void updateRound(int currentRound) {
		this.trivia.setCurrentRound(currentRound);
		this.gui.updateGUI();
	}

	public synchronized void updateTrivia(Round[] newRounds) {
		this.trivia.updateRounds(newRounds);
		this.gui.updateGUI();
	}

	public synchronized void updateActiveUsers(Hashtable<String, Role> newActiveUserList) {
		this.activeUserHash = newActiveUserList;
		this.gui.updateGUI();
	}

	public synchronized void updateIdleUsers(Hashtable<String, Role> newIdleUserList) {
		this.idleUserHash = newIdleUserList;
		this.gui.updateGUI();
	}

	@Override
	public void run() {
		// Wait for trivia object to finish downloading
		int tryNumber = 0;
		boolean success = false;
		while (tryNumber < Integer.parseInt(TriviaGUI.PROPERTIES.getProperty("MaxRetries")) && success == false) {
			tryNumber++;
			try {
				this.trivia = this.server.getTrivia();
				success = true;
			} catch (final RemoteException e) {
				TriviaClient.this.gui.log("Couldn't retrive trivia data from server (try #" + tryNumber + ").");
			}
		}

		// Show disconnected dialog if we could not retrieve the Trivia data
		if (!success || this.trivia == null) {
			this.disconnected();
		}

		try {
			this.server.connect(this);
		} catch (RemoteException exception1) {
			this.disconnected();
		}

		this.gui.log("Successfully connected to server");
		this.gui.updateGUI();

	}

}
