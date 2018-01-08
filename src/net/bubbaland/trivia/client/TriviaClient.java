package net.bubbaland.trivia.client;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
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

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.User;
import net.bubbaland.trivia.client.dialog.LoadStateDialog;
import net.bubbaland.trivia.messages.ChangeUserMessage;
import net.bubbaland.trivia.messages.FetchTriviaMessage;
import net.bubbaland.trivia.messages.Message;
import net.bubbaland.trivia.messages.SaveListMessage;
import net.bubbaland.trivia.messages.SetNTeamsMessage;
import net.bubbaland.trivia.messages.SetNVisualMessage;
import net.bubbaland.trivia.messages.SetRoleMessage;
import net.bubbaland.trivia.messages.SetRoundMessage;
import net.bubbaland.trivia.messages.SetTeamNumberMessage;
import net.bubbaland.trivia.messages.TriviaDataMessage;
import net.bubbaland.trivia.messages.UpdateRoundsMessage;
import net.bubbaland.trivia.messages.UserListMessage;

/**
 * Provides the root functionality for connecting to the trivia server and creating the associated GUI.
 *
 * @author Walter Kolczynski
 *
 */
@ClientEndpoint(decoders = { Message.MessageDecoder.class }, encoders = { Message.MessageEncoder.class })
public class TriviaClient implements Runnable {

	private volatile User	user;

	// Hashtable of active users and roles
	private volatile User[]	userList;

	// Time to idle in seconds
	private volatile int	timeToIdle;

	// The remote server
	private Session			session;

	// The local trivia object holding all contest data
	private volatile Trivia	trivia;

	private final TriviaGUI	gui;

	private final String	serverURL;

	/**
	 * Creates a new trivia client
	 */
	public TriviaClient(String serverURL, TriviaGUI gui) {
		this.gui = gui;
		this.serverURL = serverURL;
		this.session = null;
		this.user = new User();
		this.setUserName("User" + String.format("%05d", new Random().nextInt(99999)));
		this.userList = new User[0];
		this.trivia = null;
		this.timeToIdle = Integer.parseInt(TriviaGUI.PROPERTIES.getProperty("UserList.timeToIdle"));
	}

	@Override
	public void run() {
		final ClientManager clientManager = ClientManager.createClient();
		try {
			clientManager.connectToServer(this, URI.create(this.serverURL));
			TriviaClient.this.setUserName(this.user.getUserName());
		} catch (DeploymentException | IOException exception) {
			this.gui.disconnected();
		}
	}

	public int getTimeToIdle() {
		return this.timeToIdle;
	}

	public ArrayList<String> getUserNameList() {
		ArrayList<String> userNameList = new ArrayList<String>();
		for (User user : this.userList) {
			userNameList.add(user.getUserName());
		}
		return userNameList;
	}

	public int[] getVersions() {
		return this.trivia.getVersions();
	}

	public int getCurrentRoundNumber() {
		return this.trivia.getCurrentRoundNumber();
	}

	public void log(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				TriviaClient.this.gui.log(message);
			}
		});
	}

	public User getUser() {
		return this.user;
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
	 * Change the user's role.
	 *
	 * @param role
	 */
	protected void setRole(final User.Role role) {
		this.user.setRole(role);
		( new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				TriviaClient.this.sendMessage(new SetRoleMessage(role));
				;
				return null;
			}

			@Override
			public void done() {

			}
		} ).execute();
	}

	public void setUserName(final String userName) {
		this.user.setUserName(userName);
		( new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				TriviaClient.this.sendMessage(new ChangeUserMessage(userName));
				return null;
			}

			@Override
			public void done() {

			}
		} ).execute();
		this.gui.log("Name set to " + userName);
	}

	public void sendMessage(Message message) {
		if (SwingUtilities.isEventDispatchThread()) {
			System.out.println("Trying to send message from Event Dispatch Thread!");
		}
		try {
			this.session.getBasicRemote().sendObject(message);
		} catch (IOException | EncodeException exception) {
			log("Error transmitting message to server!");
			log(message.toString());
			exception.printStackTrace();
		}
	}

	@OnOpen
	public void onOpen(final Session session, EndpointConfig config) {
		this.gui.log("Connected to trivia server (" + this.serverURL + ").");
		this.session = session;
		this.sendMessage(new FetchTriviaMessage());
	}

	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}

	@OnMessage
	public void onMessage(Message genericMessage) {
		String messageType = genericMessage.getClass().getSimpleName();
		switch (messageType) {
			case "SetRoundMessage": {
				SetRoundMessage message = (SetRoundMessage) genericMessage;
				this.trivia.setCurrentRound(message.getRoundNumber());
				break;
			}
			case "TriviaDataMessage": {
				TriviaDataMessage message = (TriviaDataMessage) genericMessage;
				this.trivia = message.getTrivia();
				break;
			}
			case "UserListMessage": {
				UserListMessage message = (UserListMessage) genericMessage;
				this.userList = message.getUserList();
				break;
			}
			case "SaveListMessage": {
				SaveListMessage message = (SaveListMessage) genericMessage;
				new LoadStateDialog(this, message.getSaveFilenames());
				break;
			}
			case "UpdateRoundsMessage": {
				UpdateRoundsMessage message = (UpdateRoundsMessage) genericMessage;
				this.trivia.updateRounds(message.getUpdatedRounds());
				break;
			}
			case "SetNTeamsMessage": {
				SetNTeamsMessage message = (SetNTeamsMessage) genericMessage;
				this.trivia.setNTeams(message.getnTeams());
				break;
			}
			case "SetNVisualMessage": {
				SetNVisualMessage message = (SetNVisualMessage) genericMessage;
				this.trivia.setNVisual(message.getnVisual());
				break;
			}
			case "SetTeamNumberMessage": {
				SetTeamNumberMessage message = (SetTeamNumberMessage) genericMessage;
				this.trivia.setTeamNumber(message.getTeamNumber());
				break;
			}
			default:
				System.out.println("Unexpected message type received by client!");
		}
		this.gui.updateGUI(false);
	}

	@OnClose
	public void onClose() {
		this.gui.log("Connection closed");
		this.gui.disconnected();
	}

	public User[] getUserList() {
		return userList;
	}

	public void setIdleTime(int timeToIdle) {
		this.timeToIdle = timeToIdle;
	}
}
