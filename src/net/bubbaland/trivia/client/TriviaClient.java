package net.bubbaland.trivia.client;

import java.io.IOException;
import java.net.URI;
import java.util.Hashtable;

import javax.swing.SwingUtilities;
import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import net.bubbaland.trivia.ClientMessage;
import net.bubbaland.trivia.ClientMessage.ClientMessageFactory;
import net.bubbaland.trivia.ServerMessage;
import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.Trivia.Role;

/**
 * Provides the root functionality for connecting to the trivia server and creating the associated GUI.
 * 
 * @author Walter Kolczynski
 * 
 */
@ClientEndpoint(decoders = { ServerMessage.MessageDecoder.class }, encoders = { ClientMessage.MessageEncoder.class })
public class TriviaClient implements Runnable {

	// The user's name
	private volatile String						user;
	// The user's role
	private volatile Role						role;


	// Hashtable of active users and roles
	private volatile Hashtable<String, Role>	activeUserHash;
	// Hashtable of idle users and roles
	private volatile Hashtable<String, Role>	idleUserHash;

	private int									timeToIdle;

	// The remote server
	private Session								session;

	// The local trivia object holding all contest data
	private volatile Trivia						trivia;

	private final TriviaGUI						gui;

	private String								serverURL;

	/**
	 * Creates a new trivia client
	 */
	public TriviaClient(String serverURL, TriviaGUI gui) {
		this.gui = gui;
		this.serverURL = serverURL;
		this.session = null;

		this.activeUserHash = new Hashtable<String, Role>(0);
		this.idleUserHash = new Hashtable<String, Role>(0);
		this.user = null;
		this.role = Role.RESEARCHER;
		this.trivia = null;
		this.timeToIdle = Integer.parseInt(TriviaGUI.PROPERTIES.getProperty("UserList.timeToIdle"));
	}

	public void run() {
		final ClientManager clientManager = ClientManager.createClient();
		try {
			clientManager.connectToServer(this, URI.create(serverURL));
		} catch (DeploymentException | IOException exception) {
			this.gui.disconnected();
		}
		this.setUser(this.user);
	}

	public int[] getVersions() {
		return this.trivia.getVersions();
	}

	public int getCurrentRoundNumber() {
		return this.trivia.getCurrentRoundNumber();
	}

	public void log(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TriviaClient.this.gui.log(message);
			}
		});
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
	public Session getSession() {
		return this.session;
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
		if (this.user == null) {
			// this.server.setRole(user, this.role);
			try {
				session.getBasicRemote().sendObject(ClientMessageFactory.setRole(user, this.role));
			} catch (IOException | EncodeException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			}
		} else {
			// this.server.changeUser(this.user, user);
			try {
				session.getBasicRemote().sendObject(ClientMessageFactory.changeUser(user));
			} catch (IOException | EncodeException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			}
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
			// this.server.setRole(this.user, role);
			try {
				session.getBasicRemote().sendObject(ClientMessageFactory.setRole(user, role));
			} catch (IOException | EncodeException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			}
		} else {
			this.gui.log("Couldn't set role yet, no user name specified");
		}
		this.role = role;
	}

	public void sendMessage(ClientMessage message) {
		if (SwingUtilities.isEventDispatchThread()) {
			System.out.println("Trying to send message from Event Dispatch Thread!");
		}
		try {
			this.session.getBasicRemote().sendObject(message);
		} catch (IOException | EncodeException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
	}

	@OnOpen
	public void onOpen(final Session session, EndpointConfig config) {
		this.gui.log("Connected to trivia server (" + this.serverURL + ").");
		this.session = session;
		this.sendMessage(ClientMessageFactory.setIdleTime(this.timeToIdle));
		this.sendMessage(ClientMessageFactory.fetchTrivia());
	}

	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}

	@OnMessage
	public void onMessage(ServerMessage message) {
		ServerMessage.ServerCommand command = message.getCommand();
		switch (command) {
			case UPDATE_ROUND:
				this.trivia.updateRounds(message.getRounds());
				// this.gui.log("Updating data");
				break;
			case UPDATE_TRIVIA:
				this.trivia = message.getTrivia();
				// this.gui.log("Trivia received");
				break;
			case UPDATE_USER_LISTS:
				this.activeUserHash = message.getActiveUserList();
				this.idleUserHash = message.getIdleUserList();
				// this.gui.log("New user lists received");
				break;
			case LIST_SAVES:
				new LoadStateDialog(this, message.getSaves());
				break;
			case UPDATE_R_NUMBER:
				this.trivia.setCurrentRound(message.getRNumber());
				break;
			default:
				System.out.println("Unknown command received: " + command);
				break;
		}
		this.gui.updateGUI(false);
	}

	@OnClose
	public void onClose() {
		this.gui.log("Connection closed");
		this.gui.disconnected();
	}


}
