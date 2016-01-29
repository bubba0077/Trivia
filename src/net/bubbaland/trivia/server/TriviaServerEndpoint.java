package net.bubbaland.trivia.server;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import net.bubbaland.trivia.ClientMessage;
import net.bubbaland.trivia.ServerMessage;
import net.bubbaland.trivia.User;

/**
 * The main server that coordinates the trivia contest.
 *
 * The <code>TriviaServer</code> class contains the <code>main</code> method to start the trivia server and handles all
 * interaction between the centralized <code>Trivia</code> data structure and remote clients. It also downloads the
 * hourly standings from KVSC, parses them and adds them into the <code>Trivia</code> data structure.
 *
 * The class is also responsible for periodically saving the current <code>Trivia</code> state to an XML file and
 * loading a previous state. The save files are stored in <code>SAVE_DIR</code>, which much exist on the server.
 *
 */
@ServerEndpoint(decoders = { ClientMessage.MessageDecoder.class }, encoders = {
		ServerMessage.MessageEncoder.class }, value = "/")
public class TriviaServerEndpoint {

	private static TriviaServer	server	= new TriviaServer();

	private User				user;

	/**
	 * Creates a new trivia server endpoint.
	 */
	public TriviaServerEndpoint() {
		this.user = new User(server.getTrivia().getNRounds());
	}

	public User getUser() {
		return this.user;
	}

	/**
	 * Initial hook when a client first connects (TriviaServerEndpoint() is automatically called as well)
	 *
	 * @param session
	 * @param config
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		TriviaServerEndpoint.server.addUser(session, this);
	}

	/**
	 * Handle a message from the client
	 *
	 * @param message
	 * @param session
	 */
	@OnMessage
	public void onMessage(ClientMessage message, Session session) {
		TriviaServerEndpoint.server.processIncomingMessage(message, session);
	}

	/**
	 * Handle error in communicating with a client
	 *
	 * @param session
	 */
	@SuppressWarnings("static-access")
	@OnError
	public void onError(Session session, Throwable throwable) {
		this.server.communicationsError(session, throwable);
	}

	/**
	 * Handle a client disconnection
	 *
	 * @param session
	 */
	@OnClose
	public void onClose(Session session) {
		TriviaServerEndpoint.server.removeUser(session);
	}

	/**
	 * Entry point for the server application.
	 *
	 * @param args
	 *            Unused
	 * @throws RemoteException
	 *             A remote exception
	 */
	// public void main(String args[]) {
	// this.log("Starting server...");
	// final Server server = new Server(this.SERVER_URL, this.SERVER_PORT, "/", null, TriviaServerEndpoint.class);
	// try {
	// server.start();
	// this.isRunning = true;
	// while (this.isRunning) {
	// }
	// } catch (final DeploymentException exception) {
	// exception.printStackTrace();
	// } finally {
	// server.stop();
	// }
	// }
}
