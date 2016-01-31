package net.bubbaland.trivia.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.glassfish.tyrus.server.Server;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.bubbaland.trivia.Answer;
import net.bubbaland.trivia.ClientMessage;
import net.bubbaland.trivia.ClientMessage.ClientCommand;
import net.bubbaland.trivia.Question;
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
	private final SimpleDateFormat						fileDateFormat		= new SimpleDateFormat("yyyy_MMM_dd_HHmm");
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
	static private final SimpleDateFormat				stringDateFormat	= new SimpleDateFormat(
			"yyyy MMM dd HH:mm:ss");
	// The team name
	private String										teamName;
	// The Trivia object that holds all of the contest data
	private Trivia										trivia;

	public TriviaServer() {
		this.log("Starting server...");
		this.properties = new Properties();
		this.loadSettings();
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
			this.log("Couldn't load default properties file, aborting!");
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
			this.log("Couldn't load local properties file, may not exist.");
		}

		TriviaChartFactory.loadProperties(this.properties);

		/**
		 * Parse properties into variables
		 */
		this.nRounds = Integer.parseInt(this.properties.getProperty("nRounds"));
		this.nQuestionsNormal = Integer.parseInt(this.properties.getProperty("nQuestionsNormal"));
		this.nQuestionsSpeed = Integer.parseInt(this.properties.getProperty("nQuestionsSpeed"));
		this.teamName = this.properties.getProperty("TeamName");
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
		this.trivia = new Trivia(this.teamName, this.nRounds, this.nQuestionsNormal, this.nQuestionsSpeed);
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
				this.log("New round started by " + user.getUserName());
				this.broadcastRoundNumber();
				break;
			case CHANGE_AGREEMENT:
				this.trivia.changeAgreement(user.getUserName(), message.getQueueIndex(), message.getAgreement());
				this.broadcastChangedRounds();
				break;
			case CALL_IN:
				this.trivia.callIn(message.getQueueIndex(), user.getUserName());
				this.log(user.getUserName() + " is calling in item " + message.getQueueIndex()
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
				this.log("Question " + message.getqNumber() + " closed, "
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
				this.log("Round " + message.getrNumber() + " Question " + message.getqNumber() + " edited by "
						+ user.getUserName());
				this.broadcastChangedRounds();
				break;
			case LIST_SAVES:
				this.sendMessage(session, ServerMessageFactory.sendSaveList(this.listSaves()));
				break;
			case LOAD_STATE:
				this.loadState(user.getUserName(), message.getSaveFilename());
				this.broadcastChangedRounds();
				break;
			case MARK_CORRECT:
				this.trivia.markCorrect(message.getQueueIndex(), user.getUserName());
				this.log(
						"Item " + message.getQueueIndex() + " in the queue is correct, "
								+ this.trivia.getValue(this.trivia.getCurrentRoundNumber(),
										this.trivia.getAnswerQueueQNumbers()[message.getQueueIndex()])
						+ " points earned!");
				this.broadcastChangedRounds();
				break;
			case MARK_DUPLICATE:
				this.trivia.markDuplicate(message.getQueueIndex());
				this.log("Item " + message.getQueueIndex() + " marked as duplicate.");
				this.broadcastChangedRounds();
				break;
			case MARK_INCORRECT:
				this.trivia.markIncorrect(message.getQueueIndex(), user.getUserName());
				this.log("Item " + message.getQueueIndex() + " in the queue is incorrect.");
				this.broadcastChangedRounds();
				break;
			case MARK_PARTIAL:
				this.trivia.markPartial(message.getQueueIndex(), user.getUserName());
				this.log("Item " + message.getQueueIndex() + " in the queue is partially correct.");
				this.broadcastChangedRounds();
				break;
			case MARK_UNCALLED:
				this.trivia.markUncalled(message.getQueueIndex());
				this.log("Item " + message.getQueueIndex() + " status reset to uncalled.");
				this.broadcastChangedRounds();
				break;
			case OPEN_QUESTION:
				final int qNumber = message.getqNumber();
				this.trivia.open(user.getUserName(), qNumber);
				this.log("Question " + qNumber + " is being typed in by " + user.getUserName() + "...");
				this.broadcastChangedRounds();
				break;
			case PROPOSE_ANSWER:
				this.trivia.proposeAnswer(message.getqNumber(), message.getaText(), user.getUserName(),
						message.getConfidence());
				this.log(user.getUserName() + " submitted an answer for Q" + message.getqNumber()
						+ " with a confidence of " + message.getConfidence() + ":\n" + message.getaText());
				this.broadcastChangedRounds();
				break;
			case REMAP_QUESTION:
				this.trivia.remapQuestion(message.getOldQNumber(), message.getqNumber());
				this.log(user.getUserName() + " remapped Q" + message.getOldQNumber() + " to Q" + message.getqNumber());
				this.broadcastChangedRounds();
				break;
			case REOPEN_QUESTION:
				this.trivia.reopen(message.getqNumber());
				this.log("Q" + message.getqNumber() + " re-opened by " + user.getUserName());
				this.broadcastChangedRounds();
				break;
			case RESET_QUESTION:
				this.trivia.resetQuestion(message.getqNumber());
				this.log(user.getUserName() + " reset Q" + message.getqNumber());
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
					this.log("Making round " + this.trivia.getCurrentRoundNumber() + " a speed round");
				} else {
					this.log("Making round " + this.trivia.getCurrentRoundNumber() + " not a speed round");
				}
				this.broadcastChangedRounds();
				break;
			case SET_OPERATOR:
				this.trivia.setOperator(message.getQueueIndex(), message.getOperator());
				this.log("Operator for queue index " + message.getQueueIndex() + " set by " + user.getUserName()
						+ " to " + message.getOperator());
				this.broadcastChangedRounds();
				break;
			case SET_QUESTION:
				this.trivia.setQuestionText(message.getqNumber(), message.getqText());
				this.trivia.setQuestionValue(message.getqNumber(), message.getqValue());
				this.log("Question #" + message.getqNumber() + " set to " + message.getqText() + "with a value of "
						+ message.getqValue() + " by " + user.getUserName());
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
				this.log("Number of visual trivia set to " + message.getNVisual() + " by " + user.getUserName());
				break;
			default:
				this.log("Unknown message received by server!" + message.toString());
				break;
		}
	}

	/**
	 * Get a list of the available saves.
	 *
	 * @return Array of save file names
	 */
	public String[] listSaves() {
		final File folder = new File(this.saveDirectory);
		final File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				// Only get XML files
				if (file.getName().toLowerCase().endsWith(".xml")) return true;
				return false;
			}
		});
		final int nFiles = files.length;
		final String[] filenames = new String[nFiles];
		for (int f = 0; f < nFiles; f++) {
			filenames[f] = files[f].getName();
		}
		Arrays.sort(filenames, Collections.reverseOrder());
		return filenames;
	}

	/**
	 * Loads a trivia state from file.
	 *
	 * @param stateFile
	 *            The name of the file to load
	 */
	public void loadState(String user, String stateFile) {
		// The full qualified file name
		stateFile = this.saveDirectory + "/" + stateFile;

		// Clear all data from the trivia contest
		this.trivia.reset();

		// Make a private copy to prevent interference
		final Trivia trivia = this.trivia;

		try {
			// Open the save file
			final File infile = new File(stateFile);
			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(infile);
			doc.getDocumentElement().normalize();

			// Get the top-level element
			final Element triviaElement = doc.getDocumentElement();

			// Read/set the trivia parameters
			trivia.setNTeams(
					Integer.parseInt(triviaElement.getElementsByTagName("Number_of_Teams").item(0).getTextContent()));
			trivia.setCurrentRound(
					Integer.parseInt(triviaElement.getElementsByTagName("Current_Round").item(0).getTextContent()));

			// Get a list of the round elements
			final NodeList roundElements = triviaElement.getElementsByTagName("Round");

			for (int r = 0; r < roundElements.getLength(); r++) {
				final Element roundElement = (Element) roundElements.item(r);
				// Read the round number
				final int rNumber = Integer.parseInt(roundElement.getAttribute("number"));
				this.log("Reading data for round " + rNumber);

				// Read/set if the round is a speed round
				final boolean isSpeed = roundElement.getElementsByTagName("Speed").item(0).getTextContent()
						.equals("true");
				trivia.setSpeed(rNumber, isSpeed);

				trivia.setDiscrepencyText(rNumber,
						roundElement.getElementsByTagName("Discrepancy_Text").item(0).getTextContent());

				// Get a list of the question elements in this round
				final NodeList questionElements = roundElement.getElementsByTagName("Question");

				for (int q = 0; q < questionElements.getLength(); q++) {
					final Element questionElement = (Element) questionElements.item(q);
					// Read the question number
					final int qNumber = Integer.parseInt(questionElement.getAttribute("number"));

					// Read/set question parameters
					final boolean beenOpen = questionElement.getElementsByTagName("Been_Open").item(0).getTextContent()
							.equals("true");
					final boolean isOpen = questionElement.getElementsByTagName("Is_Open").item(0).getTextContent()
							.equals("true");
					final boolean isCorrect = questionElement.getElementsByTagName("Is_Correct").item(0)
							.getTextContent().equals("true");
					final int value = Integer
							.parseInt(questionElement.getElementsByTagName("Value").item(0).getTextContent());
					final String question = questionElement.getElementsByTagName("Question_Text").item(0)
							.getTextContent();
					final String answer = questionElement.getElementsByTagName("Answer_Text").item(0).getTextContent();
					final String submitter = questionElement.getElementsByTagName("Submitter").item(0).getTextContent();

					if (beenOpen) {
						trivia.open("From file", rNumber, qNumber);
						trivia.editQuestion(rNumber, qNumber, value, question, answer, isCorrect, submitter);
						if (!isOpen) {
							trivia.close(rNumber, qNumber);
						}
					}
				}

				final Element element = (Element) roundElement.getElementsByTagName("Answer_Queue").item(0);

				if (element != null) {
					// Get the list of propsed answer elements in the answer queue
					final NodeList answerElements = element.getElementsByTagName("Proposed_Answer");

					for (int a = 0; a < answerElements.getLength(); a++) {
						final Element answerElement = (Element) answerElements.item(a);

						// Read/set parameters of the answer
						final int qNumber = Integer.parseInt(
								answerElement.getElementsByTagName("Question_Number").item(0).getTextContent());
						final String status = answerElement.getElementsByTagName("Status").item(0).getTextContent();
						final String timestamp = answerElement.getElementsByTagName("Timestamp").item(0)
								.getTextContent();
						final String answer = answerElement.getElementsByTagName("Answer_Text").item(0)
								.getTextContent();
						final String submitter = answerElement.getElementsByTagName("Submitter").item(0)
								.getTextContent();
						final int confidence = Integer
								.parseInt(answerElement.getElementsByTagName("Confidence").item(0).getTextContent());
						final String caller = answerElement.getElementsByTagName("Caller").item(0).getTextContent();
						final String operator = answerElement.getElementsByTagName("Operator").item(0).getTextContent();

						trivia.setAnswer(rNumber, qNumber, answer, submitter, confidence, status, caller, operator,
								timestamp);
					}
				}
			}

		} catch (final ParserConfigurationException | SAXException | IOException e) {
		}

		this.log("Loaded state from " + stateFile);

		for (int r = 1; r < trivia.getCurrentRoundNumber(); r++) {
			// For each past round, try to get announced standings if we don't have them
			if (!trivia.isAnnounced(r)) {
				final ScoreEntry[] standings = this.fetchStandings(r);
				if (standings != null) {
					trivia.setStandings(r, standings);
				}
			}
		}

		// Copy the loaded data back to the trivia object
		this.trivia = trivia;

		System.out.print(trivia);

		// Notify clients of the updated data
		this.broadcastRoundNumber();
		this.broadcastChangedRounds();
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

		this.log("Standings for round " + rNumber + " parsed.");
		return standingsList.toArray(new ScoreEntry[standingsList.size()]);
	}

	/**
	 * Print a message with timestamp to the console.
	 *
	 * @param message
	 *            The message
	 */
	@SuppressWarnings("static-access")
	void log(String message) {
		final Date date = new Date();
		System.out.println(this.stringDateFormat.format(date) + ": " + message);
	}

	private void restartTimer() {
		// Create timer that will make save files
		this.saveTimer = Executors.newSingleThreadScheduledExecutor();
		this.saveTimer.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					TriviaServer.this.saveState();
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
	 * Save the current trivia state to an xml file.
	 */
	private void saveState() {

		// The current date/time
		final Date time = new Date();

		// Make a local copy of the trivia object in case it changes during writing
		final Trivia trivia = this.trivia;

		//
		final String roundString = "Rd" + String.format("%02d", trivia.getCurrentRoundNumber());

		// Timestamp used as part of the filename (no spaces, descending precision)
		String filename = this.saveDirectory + "/" + roundString + "_" + this.fileDateFormat.format(time) + ".xml";
		// Timestamp used in the save file
		@SuppressWarnings("static-access")
		final String createTime = this.stringDateFormat.format(time);

		try {
			// Create a document
			final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			final Document doc = docBuilder.newDocument();

			// Create the top-level element
			final Element triviaElement = doc.createElement("Trivia");
			doc.appendChild(triviaElement);

			// Make the save time an attribute of Trivia
			Attr attribute = doc.createAttribute("Save_Time");
			attribute.setValue(createTime);
			triviaElement.setAttributeNode(attribute);

			// Save the number of teams
			Element element = doc.createElement("Number_of_Teams");
			element.appendChild(doc.createTextNode(trivia.getNTeams() + ""));
			triviaElement.appendChild(element);

			// Save the current round number
			element = doc.createElement("Current_Round");
			element.appendChild(doc.createTextNode(trivia.getCurrentRoundNumber() + ""));
			triviaElement.appendChild(element);

			for (final Round r : trivia.getRounds()) {
				// Create a round element
				final Element roundElement = doc.createElement("Round");
				triviaElement.appendChild(roundElement);

				// The round number
				attribute = doc.createAttribute("number");
				attribute.setValue(r.getRoundNumber() + "");
				roundElement.setAttributeNode(attribute);

				// Whether it is a speed round
				element = doc.createElement("Speed");
				element.appendChild(doc.createTextNode(r.isSpeed() + ""));
				roundElement.appendChild(element);

				// Whether the score has been announced for this round
				element = doc.createElement("Announced");
				element.appendChild(doc.createTextNode(r.isAnnounced() + ""));
				roundElement.appendChild(element);

				// The announced score for this round
				element = doc.createElement("Announced_Score");
				element.appendChild(doc.createTextNode(r.getAnnounced() + ""));
				roundElement.appendChild(element);

				// The announced place for this round
				element = doc.createElement("Announced_Place");
				element.appendChild(doc.createTextNode(r.getPlace() + ""));
				roundElement.appendChild(element);

				// The discrepancy text for this round
				element = doc.createElement("Discrepancy_Text");
				element.appendChild(doc.createTextNode(r.getDiscrepancyText() + ""));
				roundElement.appendChild(element);

				final Element qListElement = doc.createElement("Questions");
				roundElement.appendChild(qListElement);

				for (final Question q : r.getQuestions()) {
					// Create a question element
					final Element questionElement = doc.createElement("Question");
					qListElement.appendChild(questionElement);

					// The question number
					attribute = doc.createAttribute("number");
					attribute.setValue(q.getNumber() + "");
					questionElement.setAttributeNode(attribute);

					// Whether the question has been open
					element = doc.createElement("Been_Open");
					element.appendChild(doc.createTextNode(q.beenOpen() + ""));
					questionElement.appendChild(element);

					// Whether the question is currently open
					element = doc.createElement("Is_Open");
					element.appendChild(doc.createTextNode(q.isOpen() + ""));
					questionElement.appendChild(element);

					// The value of the question
					element = doc.createElement("Value");
					element.appendChild(doc.createTextNode(q.getValue() + ""));
					questionElement.appendChild(element);

					// The question text
					element = doc.createElement("Question_Text");
					element.appendChild(doc.createTextNode(q.getQuestionText() + ""));
					questionElement.appendChild(element);

					// The answer
					element = doc.createElement("Answer_Text");
					element.appendChild(doc.createTextNode(q.getAnswerText() + ""));
					questionElement.appendChild(element);

					// Whether this question was answered correctly
					element = doc.createElement("Is_Correct");
					element.appendChild(doc.createTextNode(q.isCorrect() + ""));
					questionElement.appendChild(element);

					// The submitter for a correctly answered question
					element = doc.createElement("Submitter");
					element.appendChild(doc.createTextNode(q.getSubmitter() + ""));
					questionElement.appendChild(element);
				}

				// The size of the answer queue for the current round
				final int queueSize = r.getAnswerQueueSize();

				// Create a queue element
				final Element queueElement = doc.createElement("Answer_Queue");
				roundElement.appendChild(queueElement);

				// The size of the queue
				attribute = doc.createAttribute("size");
				attribute.setValue(queueSize + "");
				queueElement.setAttributeNode(attribute);

				for (final Answer a : r.getAnswerQueue()) {
					// Create a proposed answer element
					final Element answerElement = doc.createElement("Proposed_Answer");
					queueElement.appendChild(answerElement);

					// The question number for this answer
					element = doc.createElement("Question_Number");
					element.appendChild(doc.createTextNode(a.getQNumber() + ""));
					answerElement.appendChild(element);

					// The current status of this answer
					element = doc.createElement("Status");
					element.appendChild(doc.createTextNode(a.getStatusString()));
					answerElement.appendChild(element);

					// The time stamp of this answer
					element = doc.createElement("Timestamp");
					element.appendChild(doc.createTextNode(a.getTimestamp()));
					answerElement.appendChild(element);

					// The proposed answer
					element = doc.createElement("Answer_Text");
					element.appendChild(doc.createTextNode(a.getAnswer()));
					answerElement.appendChild(element);

					// The submitter of this answer
					element = doc.createElement("Submitter");
					element.appendChild(doc.createTextNode(a.getSubmitter()));
					answerElement.appendChild(element);

					// The confidence in this answer
					element = doc.createElement("Confidence");
					element.appendChild(doc.createTextNode(a.getConfidence() + ""));
					answerElement.appendChild(element);

					// The user who called this answer in
					element = doc.createElement("Caller");
					element.appendChild(doc.createTextNode(a.getCaller()));
					answerElement.appendChild(element);

					// The operator who accepted the answer
					element = doc.createElement("Operator");
					element.appendChild(doc.createTextNode(a.getOperator()));
					answerElement.appendChild(element);
				}
			}

			// write the content into xml file
			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			final Transformer transformer = transformerFactory.newTransformer();
			final DOMSource source = new DOMSource(doc);
			final StreamResult result = new StreamResult(new File(filename));
			transformer.transform(source, result);

			this.log("Saved state to " + filename);

		} catch (final ParserConfigurationException | TransformerException e) {
			this.log("Couldn't save data to file " + filename);
		}

		if (trivia.isAnnounced(1)) {
			// Save place chart
			try {
				final JFreeChart chart = TriviaChartFactory.makePlaceChart(trivia);
				filename = this.chartDirectory + "/" + roundString + "_placeChart.png";
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, this.chartWidth, this.chartHeight);
				this.log("Saved place chart to " + filename);
				filename = this.chartDirectory + "/latest_placeChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, this.chartWidth, this.chartHeight);
				this.log("Saved place chart to " + filename);
			} catch (final IOException exception) {
				this.log("Couldn't save place chart to file " + filename);
			}

			// Save score by round chart
			try {
				final JFreeChart chart = TriviaChartFactory.makeScoreByRoundChart(trivia);
				filename = this.chartDirectory + "/" + roundString + "_scoreByRoundChart.png";
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, this.chartWidth, this.chartHeight);
				this.log("Saved place chart to " + filename);
				filename = this.chartDirectory + "/latest_scoreByRoundChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, this.chartWidth, this.chartHeight);
				this.log("Saved score by round chart to " + filename);
			} catch (final IOException exception) {
				this.log("Couldn't save score by round chart to file " + filename);
			}

			// Save cumulative score chart
			try {
				final JFreeChart chart = TriviaChartFactory.makeCumulativePointChart(trivia);
				filename = this.chartDirectory + "/" + roundString + "_cumulativeScoreChart.png";
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, this.chartWidth, this.chartHeight);
				this.log("Saved cumulative score chart to " + filename);
				filename = this.chartDirectory + "/latest_cumulativeScoreChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, this.chartWidth, this.chartHeight);
				this.log("Saved cumulative score chart to " + filename);
			} catch (final IOException exception) {
				this.log("Couldn't save cumulative score chart to file " + filename);
			}

			// Save team comparison chart
			try {
				final JFreeChart chart = TriviaChartFactory.makeTeamComparisonChart(trivia);
				filename = this.chartDirectory + "/" + roundString + "_teamComparisonChart.png";
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, this.chartWidth, this.chartHeight);
				this.log("Saved team comparison chart to " + filename);
				filename = this.chartDirectory + "/latest_teamComparisonChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, this.chartWidth, this.chartHeight);
				this.log("Saved team comparison chart to " + filename);
			} catch (final IOException exception) {
				this.log("Couldn't save team comparison chart to file " + filename);
			}

			// Save place chart
			filename = this.chartDirectory + "/latest_placeChart.png";
			try {
				final File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, TriviaChartFactory.makePlaceChart(this.trivia), this.chartWidth,
						this.chartHeight);
				this.log("Saved place chart to " + filename);
			} catch (final IOException exception) {
				this.log("Couldn't save place chart to file " + filename);
			}

			// Save score by round chart
			filename = this.chartDirectory + "/latest_scoreByRoundChart.png";
			try {
				final File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, TriviaChartFactory.makeScoreByRoundChart(this.trivia),
						this.chartWidth, this.chartHeight);
				this.log("Saved score by round chart to " + filename);
			} catch (final IOException exception) {
				this.log("Couldn't save score by round chart to file " + filename);
			}

			// Save cumulative score chart
			filename = this.chartDirectory + "/latest_cumulativeScoreChart.png";
			try {
				final File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, TriviaChartFactory.makeCumulativePointChart(this.trivia),
						this.chartWidth, this.chartHeight);
				this.log("Saved cumulative score chart to " + filename);
			} catch (final IOException exception) {
				this.log("Couldn't save cumulative score chart to file " + filename);
			}

			// Save team comparison chart
			filename = this.chartDirectory + "/latest_teamComparisonChart.png";
			try {
				final File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, TriviaChartFactory.makeTeamComparisonChart(this.trivia),
						this.chartWidth, this.chartHeight);
				this.log("Saved team comparison chart to " + filename);
			} catch (final IOException exception) {
				this.log("Couldn't save team comparison chart to file " + filename);
			}


		}

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
		this.log("New client connecting...");
		this.sessionList.put(session, user);
	}

	public void removeUser(Session session) {
		this.log(this.sessionList.get(session).getUser() + " disconnected");
		this.sessionList.remove(session);
	}

	public void communicationsError(Session session, Throwable throwable) {
		this.log("Error while communicating with " + this.sessionList.get(session).getUser().getUserName() + ":");
		throwable.printStackTrace();
	}


}
