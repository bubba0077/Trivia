package net.bubbaland.trivia.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.websocket.DeploymentException;
import javax.websocket.Session;

import org.glassfish.tyrus.server.Server;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import net.bubbaland.trivia.ClientMessage;
import net.bubbaland.trivia.ClientMessage.ClientCommand;
import net.bubbaland.trivia.Round;
import net.bubbaland.trivia.ScoreEntry;
import net.bubbaland.trivia.ServerMessage;
import net.bubbaland.trivia.ServerMessage.ServerMessageFactory;
import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaChartFactory;
import net.bubbaland.trivia.User;

public class TriviaServer {

	private ScheduledExecutorService					idleTimer;
	private ScheduledExecutorService					saveTimer;
	private ScheduledExecutorService					standingsTimer;
	private InputStream									defaults;
	private final Server								server;

	/**
	 * Setup properties
	 */
	private final Properties							properties;
	// Base URL for hourly standings
	public String										standingsBaseURL;
	// Directory to hold charts for publishing (must exist)
	private String										chartDirectory;
	// Size of chart for web
	private int											chartHeight;
	private int											chartWidth;
	// Date format to use for backup file names
	public static final SimpleDateFormat				fileDateFormat		= new SimpleDateFormat("yyyy_MMM_dd_HHmm");
	// Frequency to check for standings (milliseconds)
	private int											idleFrequency;
	// Boolean that tracks whether the server is running
	private boolean										isRunning;
	// The number of questions in a normal round
	private int											nQuestionsNormal;
	// The number of questions in a speed round
	private int											nQuestionsSpeed;
	// The number of rounds
	private int											nRounds;
	// Directory to hold backups (must exist)
	private String										saveDirectory;
	// Frequency of backups (milliseconds)
	private int											saveFrequency;
	// Port to use for the server (must be open to internet)
	private int											serverPort;
	// The server URL
	private String										serverURL;
	private Hashtable<Session, TriviaServerEndpoint>	sessionList;
	// File name holding the server settings
	final private String								SETTINGS_FILENAME	= ".trivia-server-settings";
	// Frequency to check for standings (milliseconds)
	private int											standingsFrequency;
	// Date format to use inside backup files
	static public final SimpleDateFormat				stringDateFormat	= new SimpleDateFormat(
			"yyyy MMM dd HH:mm:ss");
	// The team name
	private String										teamName;
	// The team number
	private int											teamNumber;
	// The Trivia object that holds all of the contest data
	private Trivia										trivia;

	final private SaveMediator							saveMediator;

	public TriviaServer() {
		TriviaServer.log("Starting server...");
		this.properties = new Properties();
		this.loadSettings();
		this.saveMediator = new SaveMediator(this.saveDirectory, this.chartDirectory);
		this.server = new Server(this.serverURL, this.serverPort, "/", null, TriviaServerEndpoint.class);
	}

	private void start() throws DeploymentException {
		this.server.start();
	}

	private void stop() {
		this.server.stop();
	}

	Trivia getTrivia() {
		return this.trivia;
	}

	private void loadSettings() {
		// Get default properties from the package
		this.defaults = TriviaServerEndpoint.class.getResourceAsStream(this.SETTINGS_FILENAME);

		/**
		 * Default properties
		 */
		try {
			this.properties.load(this.defaults);
		} catch (final IOException | NullPointerException e) {
			e.printStackTrace();
			TriviaServer.log("Couldn't load default properties file, aborting!");
			System.exit(-1);
		}

		/**
		 * Load saved properties from file
		 */
		final File file = new File(System.getProperty("user.home") + "/" + this.SETTINGS_FILENAME);
		try {
			final BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
			this.properties.load(fileBuffer);
		} catch (final IOException e) {
			TriviaServer.log("Couldn't load local properties file, may not exist.");
		}

		TriviaChartFactory.loadProperties(this.properties);

		/**
		 * Parse properties into variables
		 */
		this.nRounds = Integer.parseInt(this.properties.getProperty("nRounds"));
		this.nQuestionsNormal = Integer.parseInt(this.properties.getProperty("nQuestionsNormal"));
		this.nQuestionsSpeed = Integer.parseInt(this.properties.getProperty("nQuestionsSpeed"));
		this.teamName = this.properties.getProperty("TeamName");
		this.teamNumber = Integer.parseInt(this.properties.getProperty("TeamNumber"));
		this.serverURL = this.properties.getProperty("ServerURL");
		this.serverPort = Integer.parseInt(this.properties.getProperty("Server.Port"));
		this.saveFrequency = Integer.parseInt(this.properties.getProperty("SaveFrequency"));
		this.standingsFrequency = Integer.parseInt(this.properties.getProperty("StandingsFrequency"));
		this.idleFrequency = Integer.parseInt(this.properties.getProperty("IdleFrequency"));
		this.saveDirectory = this.properties.getProperty("SaveDir");
		this.chartDirectory = this.properties.getProperty("ChartDir");
		this.chartWidth = Integer.parseInt(this.properties.getProperty("Chart.Width"));
		this.chartHeight = Integer.parseInt(this.properties.getProperty("Chart.Height"));
		this.standingsBaseURL = this.properties.getProperty("StandingsURL");

		/**
		 * Create a new trivia data object and list of connected clients
		 */
		this.trivia = new Trivia(this.teamName, this.teamNumber, this.nRounds, this.nQuestionsNormal,
				this.nQuestionsSpeed);
		this.sessionList = new Hashtable<Session, TriviaServerEndpoint>(0);
		this.isRunning = false;

		// Create timer that will make save files
		this.restartTimer();
	}

	public User[] getUserList() {
		final ArrayList<User> users = new ArrayList<User>();
		// Build a list of users who are active
		for (final TriviaServerEndpoint client : this.sessionList.values()) {
			users.add(client.getUser());
		}
		return users.toArray(new User[0]);
	}

	public void processIncomingMessage(ClientMessage message, Session session) {
		final ClientCommand command = message.getCommand();
		final TriviaServerEndpoint userConnection = this.sessionList.get(session);
		final User user = userConnection.getUser();
		switch (command) {
			case ADVANCE_ROUND:
				this.trivia.newRound();
				TriviaServer.log("New round started by " + user.getUserName());
				this.broadcastRoundNumber();
				break;
			case CHANGE_AGREEMENT:
				this.trivia.changeAgreement(user.getUserName(), message.getQueueIndex(), message.getAgreement());
				this.broadcastChangedRounds();
				break;
			case CALL_IN:
				this.trivia.callIn(message.getQueueIndex(), user.getUserName());
				TriviaServer.log(user.getUserName() + " is calling in item " + message.getQueueIndex()
						+ " in the answer queue.");
				this.broadcastChangedRounds();
				break;
			case CHANGE_USER:
				this.trivia.changeName(user.getUserName(), message.getUser());
				user.setUserName(message.getUser());
				this.broadcastChangedRounds();
				this.broadcastUsers();
				break;
			case CLOSE_QUESTION:
				this.trivia.close(message.getqNumber());
				TriviaServer.log("Question " + message.getqNumber() + " closed, "
						+ this.trivia.getValue(this.trivia.getCurrentRoundNumber(), message.getqNumber()));
				this.broadcastChangedRounds();
				break;
			case EDIT_QUESTION:
				if (message.getaText() == null) {
					this.trivia.editQuestion(message.getrNumber(), message.getqNumber(), message.getqValue(),
							message.getqText());
				} else {
					this.trivia.editQuestion(message.getrNumber(), message.getqNumber(), message.getqValue(),
							message.getqText(), message.getaText(), message.isCorrect(), message.getUser());
				}
				TriviaServer.log("Round " + message.getrNumber() + " Question " + message.getqNumber() + " edited by "
						+ user.getUserName());
				this.broadcastChangedRounds();
				break;
			case LIST_SAVES:
				this.sendMessage(session, ServerMessageFactory.sendSaveList(this.saveMediator.listSaves()));
				break;
			case LOAD_STATE:

				this.trivia = this.saveMediator.loadState(this.trivia, user.getUserName(), message.getSaveFilename());
				log("Loaded state from " + message.getSaveFilename());

				for (int r = 1; r < trivia.getCurrentRoundNumber(); r++) {
					// For each past round, try to get announced standings if we don't have them
					if (!trivia.isAnnounced(r)) {
						final ScoreEntry[] standings = this.fetchStandings(r);
						if (standings != null) {
							trivia.setStandings(r, standings);
						}
					}
				}

				// Notify clients of the updated data
				this.broadcastRoundNumber();
				this.broadcastChangedRounds();
				break;
			case MARK_CORRECT:
				this.trivia.markCorrect(message.getQueueIndex(), user.getUserName());
				TriviaServer
						.log("Item " + message.getQueueIndex() + " in the queue is correct, "
								+ this.trivia.getValue(this.trivia.getCurrentRoundNumber(),
										this.trivia.getAnswerQueueQNumbers()[message.getQueueIndex()])
						+ " points earned!");
				this.broadcastChangedRounds();
				break;
			case MARK_DUPLICATE:
				this.trivia.markDuplicate(message.getQueueIndex());
				TriviaServer.log("Item " + message.getQueueIndex() + " marked as duplicate.");
				this.broadcastChangedRounds();
				break;
			case MARK_INCORRECT:
				this.trivia.markIncorrect(message.getQueueIndex(), user.getUserName());
				TriviaServer.log("Item " + message.getQueueIndex() + " in the queue is incorrect.");
				this.broadcastChangedRounds();
				break;
			case MARK_PARTIAL:
				this.trivia.markPartial(message.getQueueIndex(), user.getUserName());
				TriviaServer.log("Item " + message.getQueueIndex() + " in the queue is partially correct.");
				this.broadcastChangedRounds();
				break;
			case MARK_UNCALLED:
				this.trivia.markUncalled(message.getQueueIndex());
				TriviaServer.log("Item " + message.getQueueIndex() + " status reset to uncalled.");
				this.broadcastChangedRounds();
				break;
			case OPEN_QUESTION:
				final int qNumber = message.getqNumber();
				this.trivia.open(user.getUserName(), qNumber);
				TriviaServer.log("Question " + qNumber + " is being typed in by " + user.getUserName() + "...");
				this.broadcastChangedRounds();
				break;
			case PROPOSE_ANSWER:
				this.trivia.proposeAnswer(message.getqNumber(), message.getaText(), user.getUserName(),
						message.getConfidence());
				TriviaServer.log(user.getUserName() + " submitted an answer for Q" + message.getqNumber()
						+ " with a confidence of " + message.getConfidence() + ":\n" + message.getaText());
				this.broadcastChangedRounds();
				break;
			case REMAP_QUESTION:
				this.trivia.remapQuestion(message.getOldQNumber(), message.getqNumber());
				TriviaServer.log(
						user.getUserName() + " remapped Q" + message.getOldQNumber() + " to Q" + message.getqNumber());
				this.broadcastChangedRounds();
				break;
			case REOPEN_QUESTION:
				this.trivia.reopen(message.getqNumber());
				TriviaServer.log("Q" + message.getqNumber() + " re-opened by " + user.getUserName());
				this.broadcastChangedRounds();
				break;
			case RESET_QUESTION:
				this.trivia.resetQuestion(message.getqNumber());
				TriviaServer.log(user.getUserName() + " reset Q" + message.getqNumber());
				this.broadcastChangedRounds();
				break;
			case SET_DISCREPENCY_TEXT:
				this.trivia.setDiscrepencyText(message.getrNumber(), message.getDiscrepancyText());
				this.broadcastChangedRounds();
				break;
			case SET_ROLE:
				user.setUserName(message.getUser());
				user.setRole(message.getRole());
				this.broadcastUsers();
				break;
			case SET_SPEED:
				this.trivia.setSpeed(message.isSpeed());
				if (message.isSpeed()) {
					TriviaServer.log("Making round " + this.trivia.getCurrentRoundNumber() + " a speed round");
				} else {
					TriviaServer.log("Making round " + this.trivia.getCurrentRoundNumber() + " not a speed round");
				}
				this.broadcastChangedRounds();
				break;
			case SET_OPERATOR:
				this.trivia.setOperator(message.getQueueIndex(), message.getOperator());
				TriviaServer.log("Operator for queue index " + message.getQueueIndex() + " set by " + user.getUserName()
						+ " to " + message.getOperator());
				this.broadcastChangedRounds();
				break;
			case SET_QUESTION:
				this.trivia.setQuestionText(message.getqNumber(), message.getqText());
				this.trivia.setQuestionValue(message.getqNumber(), message.getqValue());
				TriviaServer.log("Question #" + message.getqNumber() + " set to " + message.getqText()
						+ "with a value of " + message.getqValue() + " by " + user.getUserName());
				this.broadcastChangedRounds();
				break;
			case SET_ANSWER:
				this.trivia.setAnswer(message.getQueueIndex(), message.getaText());
				this.broadcastChangedRounds();
				break;
			case FETCH_TRIVIA:
				final Trivia trivia = this.trivia;
				this.sendMessage(session, ServerMessageFactory.updateTrivia(trivia));
				user.setRoundVersions(trivia.getVersions());
				break;
			case RESTART_TIMER:
				this.restartTimer();
				break;
			case SET_N_VISUAL:
				this.trivia.setNVisual(message.getNVisual());
				this.broadcastNVisual();
				TriviaServer
						.log("Number of visual trivia set to " + message.getNVisual() + " by " + user.getUserName());
				break;
			case SET_TEAM_NUMBER:
				this.trivia.setTeamNumber(message.getTeamNumber());
				this.broadcastTeamNumber();
				TriviaServer.log("Team number set to " + message.getTeamNumber() + " by " + user.getUserName());
				break;
			case SET_SHOW_NAME:
				this.trivia.setShowName(message.getrNumber(), message.getShowName());
				this.broadcastChangedRounds();
				TriviaServer.log("Show name for round " + message.getrNumber() + " set to " + message.getShowName()
						+ " by " + user.getUserName());
				break;
			case SET_SHOW_HOST:
				this.trivia.setShowHost(message.getrNumber(), message.getShowHost());
				this.broadcastChangedRounds();
				TriviaServer.log("Show host for round " + message.getrNumber() + " set to " + message.getShowHost()
						+ " by " + user.getUserName());
				break;
			default:
				TriviaServer.log("Unknown message received by server!" + message.toString());
				break;
		}
	}

	/**
	 * Entry point for the server application.
	 *
	 * @param args
	 *            Unused
	 * @throws RemoteException
	 *             A remote exception
	 */
	public static void main(String args[]) {
		final TriviaServer server = new TriviaServer();
		try {
			server.start();
			server.isRunning = true;
			while (server.isRunning) {
			}
		} catch (final DeploymentException exception) {
			exception.printStackTrace();
			server.stop();
		}
	}

	/**
	 * Fetches the standings for a round from KVSC.
	 *
	 * @param rNumber
	 *            The round number
	 * @return Array of ScoreEntry that have the standing data
	 */
	private ScoreEntry[] fetchStandings(int rNumber) {

		final ArrayList<ScoreEntry> standingsList = new ArrayList<ScoreEntry>(0);

		// The URL where the file is hosted
		final String urlString = this.standingsBaseURL + String.format("%02d", rNumber) + ".htm";
		try {
			// Try to read the URL
			final org.jsoup.nodes.Document htmlDoc = Jsoup.connect(urlString).get();
			// Parse the table with the standings from the HTML file
			final Elements table = htmlDoc.select("table");
			// Get all rows after the first one (which is the header row)
			for (final org.jsoup.nodes.Element row : table.select("tr:gt(0)")) {
				// Get all of the columns in the row
				final Elements rowData = row.select("td");
				// Parse the zeroth element as the place
				int place = Integer.parseInt(rowData.get(0).text());
				// Parse the first element as the team name
				final String team = rowData.get(1).text();
				// Parse the second element as the score
				final int score = Integer.parseInt(rowData.get(2).text().replaceAll(",", ""));

				// If the score for this line is the same as the previous (a tie), overwrite place to be the same
				final int entryNumber = standingsList.size();
				if (entryNumber > 0) {
					final int lastPlace = standingsList.get(entryNumber - 1).getPlace();
					final int lastScore = standingsList.get(entryNumber - 1).getScore();
					if (score == lastScore) {
						place = lastPlace;
					}
				}

				// Create a new ScoreEntry to hold the standing and add it to the list
				standingsList.add(new ScoreEntry(team, score, place));
			}

		} catch (final HttpStatusException e) {
			// The file doesn't exist yet
			// log("Standings for round " + rNumber + " not available yet.");
			return null;
		} catch (final IOException e) {
			e.printStackTrace();
		}

		TriviaServer.log("Standings for round " + rNumber + " parsed.");
		return standingsList.toArray(new ScoreEntry[standingsList.size()]);
	}

	/**
	 * Print a message with timestamp to the console.
	 *
	 * @param message
	 *            The message
	 */
	public static void log(String message) {
		final Date date = new Date();
		System.out.println(stringDateFormat.format(date) + ": " + message);
	}

	private void restartTimer() {
		// Create timer that will make save files
		this.saveTimer = Executors.newSingleThreadScheduledExecutor();
		this.saveTimer.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					TriviaServer.this.saveMediator.saveState(TriviaServer.this.trivia);
					TriviaServer.this.saveMediator.saveCharts(TriviaServer.this.trivia, TriviaServer.this.chartWidth,
							TriviaServer.this.chartHeight);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}, this.saveFrequency, this.saveFrequency, TimeUnit.SECONDS);

		this.standingsTimer = Executors.newSingleThreadScheduledExecutor();
		this.standingsTimer.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					for (int r = 1; r < TriviaServer.this.trivia.getCurrentRoundNumber(); r++) {
						// For each past round, try to get announced standings if we don't have them
						if (!TriviaServer.this.trivia.isAnnounced(r)) {
							final ScoreEntry[] standings = TriviaServer.this.fetchStandings(r);
							if (standings != null) {
								TriviaServer.this.trivia.setStandings(r, standings);
								TriviaServer.this.broadcastNTeams();
								TriviaServer.this.broadcastChangedRounds();
							}
						}
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}, this.standingsFrequency, this.standingsFrequency, TimeUnit.SECONDS);

		this.idleTimer = Executors.newSingleThreadScheduledExecutor();
		this.idleTimer.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					TriviaServer.this.broadcastUsers();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}, this.idleFrequency, this.idleFrequency, TimeUnit.SECONDS);
	}

	/**
	 * Send a message to the specified client
	 *
	 * @param session
	 * @param message
	 */
	private void sendMessage(Session session, ServerMessage message) {
		if (session == null) return;
		session.getAsyncRemote().sendObject(message);
	}

	/**
	 * Send the current round number to each connected client
	 */
	private void broadcastRoundNumber() {
		final Set<Session> sessions = this.sessionList.keySet();
		for (final Session session : sessions) {
			this.sendMessage(session, ServerMessageFactory.updateRoundNumber(this.trivia.getCurrentRoundNumber()));
		}
	}

	/**
	 * Send updated trivia information to each connected client
	 */
	private void broadcastChangedRounds() {
		final Set<Session> sessions = this.sessionList.keySet();
		for (final Session session : sessions) {
			if (session != null) {
				final TriviaServerEndpoint info = this.sessionList.get(session);
				final Round[] newRounds = this.trivia.getChangedRounds(info.getUser().getRoundVersions());
				if (newRounds.length != 0) {
					this.sendMessage(session, ServerMessageFactory.updateRounds(newRounds));
				}
			}
		}
	}

	private void broadcastNTeams() {
		final int nTeams = this.trivia.getNTeams();
		final Set<Session> sessions = this.sessionList.keySet();
		for (final Session session : sessions) {
			if (session != null) {
				this.sendMessage(session, ServerMessageFactory.updateNTeams(nTeams));
			}
		}
	}

	private void broadcastTeamNumber() {
		final int teamNumber = this.trivia.getTeamNumber();
		final Set<Session> sessions = this.sessionList.keySet();
		for (final Session session : sessions) {
			if (session != null) {
				this.sendMessage(session, ServerMessageFactory.updateTeamNumber(teamNumber));
			}
		}
	}

	private void broadcastNVisual() {
		final int nTeams = this.trivia.getNTeams();
		final Set<Session> sessions = this.sessionList.keySet();
		for (final Session session : sessions) {
			if (session != null) {
				this.sendMessage(session, ServerMessageFactory.updateNVisual(nTeams));
			}
		}
	}

	/**
	 * Send the active and idle user lists to each connected client
	 */
	private void broadcastUsers() {
		final Set<Session> sessions = this.sessionList.keySet();
		final User[] userList = this.getUserList();
		for (final Session session : sessions) {
			this.sendMessage(session, ServerMessageFactory.updateUserList(userList));
		}
	}

	public void addUser(Session session, TriviaServerEndpoint user) {
		TriviaServer.log("New client connecting...");
		this.sessionList.put(session, user);
	}

	public void removeUser(Session session) {
		TriviaServer.log(this.sessionList.get(session).getUser() + " disconnected");
		this.sessionList.remove(session);
	}

	public void communicationsError(Session session, Throwable throwable) {
		TriviaServer
				.log("Error while communicating with " + this.sessionList.get(session).getUser().getUserName() + ":");
		throwable.printStackTrace();
	}
}
