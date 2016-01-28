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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
// import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// import javax.swing.Timer;
import javax.websocket.DeploymentException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
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
import net.bubbaland.trivia.Trivia.Role;
import net.bubbaland.trivia.TriviaChartFactory;

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

	// File name holding the server settings
	final static private String								SETTINGS_FILENAME	= ".trivia-server-settings";

	// The number of rounds
	private static int										N_ROUNDS;

	// The number of questions in a normal round
	private static int										N_QUESTIONS_NORMAL;

	// The number of questions in a speed round
	private static int										N_QUESTIONS_SPEED;

	// The team name
	private static String									TEAM_NAME;

	// Base URL for hourly standings
	public static String									STANDINGS_BASE_URL;

	// The server URL
	private static String									SERVER_URL;

	// Port to use for the server (must be open to internet)
	private static int										SERVER_PORT;

	// Frequency of backups (milliseconds)
	private static int										SAVE_FREQUENCY;

	// Frequency to check for standings (milliseconds)
	private static int										STANDINGS_FREQUENCY;

	// Frequency to check for standings (milliseconds)
	private static int										IDLE_FREQUENCY;

	// Directory to hold backups (must exist)
	private static String									SAVE_DIR;

	// Directory to hold charts for publishing (must exist)
	private static String									CHART_DIR;

	// Size of chart for web
	private static int										CHART_WIDTH;
	private static int										CHART_HEIGHT;

	// Date format to use for backup file names
	private static final SimpleDateFormat					fileDateFormat		= new SimpleDateFormat(
			"yyyy_MMM_dd_HHmm");

	// Date format to use inside backup files
	private static final SimpleDateFormat					stringDateFormat	= new SimpleDateFormat(
			"yyyy MMM dd HH:mm:ss");

	private static Hashtable<Session, TriviaServerEndpoint>	sessionList;

	// The Trivia object that holds all of the contest data
	private static Trivia									trivia;

	// Boolean that tracks whether the server is running
	private static boolean									isRunning;

	// private static Timer timer;
	private static ScheduledExecutorService					saveTimer, standingsTimer, idleTimer;

	/**
	 * Setup properties
	 */
	final static public Properties							PROPERTIES			= new Properties();


	static {
		// Get default properties from the package
		final InputStream defaults = TriviaServerEndpoint.class.getResourceAsStream(SETTINGS_FILENAME);

		/**
		 * Default properties
		 */
		try {
			PROPERTIES.load(defaults);
		} catch (final IOException | NullPointerException e) {
			e.printStackTrace();
			log("Couldn't load default properties file, aborting!");
			System.exit(-1);
		}

		/**
		 * Load saved properties from file
		 */
		final File file = new File(System.getProperty("user.home") + "/" + SETTINGS_FILENAME);
		try {
			final BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
			PROPERTIES.load(fileBuffer);
		} catch (final IOException e) {
			log("Couldn't load local properties file, may not exist.");
		}

		TriviaChartFactory.loadProperties(PROPERTIES);

		/**
		 * Parse properties into static variables
		 */
		N_ROUNDS = Integer.parseInt(PROPERTIES.getProperty("nRounds"));
		N_QUESTIONS_NORMAL = Integer.parseInt(PROPERTIES.getProperty("nQuestionsNormal"));
		N_QUESTIONS_SPEED = Integer.parseInt(PROPERTIES.getProperty("nQuestionsSpeed"));
		TEAM_NAME = PROPERTIES.getProperty("TeamName");
		SERVER_URL = PROPERTIES.getProperty("ServerURL");
		SERVER_PORT = Integer.parseInt(PROPERTIES.getProperty("Server.Port"));
		SAVE_FREQUENCY = Integer.parseInt(PROPERTIES.getProperty("SaveFrequency"));
		STANDINGS_FREQUENCY = Integer.parseInt(PROPERTIES.getProperty("StandingsFrequency"));
		IDLE_FREQUENCY = Integer.parseInt(PROPERTIES.getProperty("IdleFrequency"));
		SAVE_DIR = PROPERTIES.getProperty("SaveDir");
		CHART_DIR = PROPERTIES.getProperty("ChartDir");
		CHART_WIDTH = Integer.parseInt(PROPERTIES.getProperty("Chart.Width"));
		CHART_HEIGHT = Integer.parseInt(PROPERTIES.getProperty("Chart.Height"));
		STANDINGS_BASE_URL = PROPERTIES.getProperty("StandingsURL");


		/**
		 * Create a new trivia data object and list of connected clients
		 */
		trivia = new Trivia(TEAM_NAME, N_ROUNDS, N_QUESTIONS_NORMAL, N_QUESTIONS_SPEED);
		sessionList = new Hashtable<Session, TriviaServerEndpoint>(0);
		isRunning = false;

		// Create timer that will make save files
		restartTimer();
	}

	// Array of the last round versions sent to this client
	private int[]	lastVersions;
	// Time before this client considers a user idle (in ms)
	private int		timeToIdle;
	// User name for this client
	private String	user;
	// Role of this client
	private Role	role;
	// Last time this client sent a command
	private Date	lastActive;

	/**
	 * Creates a new trivia server endpoint.
	 */
	public TriviaServerEndpoint() {
		this.lastActive = new Date();
		this.timeToIdle = 600;
		this.user = "";
		this.role = Role.RESEARCHER;
		this.lastVersions = new int[N_ROUNDS];
	}

	private static void restartTimer() {
		// Create timer that will make save files
		saveTimer = Executors.newSingleThreadScheduledExecutor();
		saveTimer.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					TriviaServerEndpoint.saveState();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}, SAVE_FREQUENCY, SAVE_FREQUENCY, TimeUnit.SECONDS);

		standingsTimer = Executors.newSingleThreadScheduledExecutor();
		standingsTimer.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					for (int r = 1; r < TriviaServerEndpoint.trivia.getCurrentRoundNumber(); r++) {
						// For each past round, try to get announced standings if we don't have them
						if (!TriviaServerEndpoint.trivia.isAnnounced(r)) {
							final ScoreEntry[] standings = getStandings(r);
							if (standings != null) {
								TriviaServerEndpoint.trivia.setStandings(r, standings);
							}
						}
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}, STANDINGS_FREQUENCY, STANDINGS_FREQUENCY, TimeUnit.SECONDS);

		idleTimer = Executors.newSingleThreadScheduledExecutor();
		idleTimer.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					updateUsers();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}, IDLE_FREQUENCY, IDLE_FREQUENCY, TimeUnit.SECONDS);
	}

	/**
	 * Get a list of the available saves.
	 *
	 * @return Array of save file names
	 */
	public static String[] listSaves() {
		final File folder = new File(SAVE_DIR);
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
	public static void loadState(String user, String stateFile) {
		// The full qualified file name
		stateFile = SAVE_DIR + "/" + stateFile;

		// Clear all data from the trivia contest
		TriviaServerEndpoint.trivia.reset();

		// Make a private copy to prevent interference
		final Trivia trivia = TriviaServerEndpoint.trivia;

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
				log("Reading data for round " + rNumber);

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

		TriviaServerEndpoint.log("Loaded state from " + stateFile);

		for (int r = 1; r < trivia.getCurrentRoundNumber(); r++) {
			// For each past round, try to get announced standings if we don't have them
			if (!trivia.isAnnounced(r)) {
				final ScoreEntry[] standings = getStandings(r);
				if (standings != null) {
					trivia.setStandings(r, standings);
				}
			}
		}

		// Copy the loaded data back to the trivia object
		TriviaServerEndpoint.trivia = trivia;

		System.out.print(trivia);

		// Notify clients of the updated data
		TriviaServerEndpoint.updateRoundNumber();
		TriviaServerEndpoint.updateTrivia();
	}

	/**
	 * Print a message with timestamp to the console.
	 *
	 * @param message
	 *            The message
	 */
	private static void log(String message) {
		final Date date = new Date();
		System.out.println(stringDateFormat.format(date) + ": " + message);
	}

	/**
	 * Save the current trivia state to an xml file.
	 */
	private static void saveState() {

		// The current date/time
		final Date time = new Date();

		// Make a local copy of the trivia object in case it changes during writing
		final Trivia trivia = TriviaServerEndpoint.trivia;

		//
		final String roundString = "Rd" + String.format("%02d", trivia.getCurrentRoundNumber());

		// Timestamp used as part of the filename (no spaces, descending precision)
		String filename = SAVE_DIR + "/" + roundString + "_" + fileDateFormat.format(time) + ".xml";
		// Timestamp used in the save file
		final String createTime = stringDateFormat.format(time);

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

			TriviaServerEndpoint.log("Saved state to " + filename);

		} catch (final ParserConfigurationException | TransformerException e) {
			log("Couldn't save data to file " + filename);
		}

		if (trivia.isAnnounced(1)) {
			// Save place chart
			try {
				final JFreeChart chart = TriviaChartFactory.makePlaceChart(trivia);
				filename = CHART_DIR + "/" + roundString + "_placeChart.png";
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved place chart to " + filename);
				filename = CHART_DIR + "/latest_placeChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved place chart to " + filename);
			} catch (final IOException exception) {
				log("Couldn't save place chart to file " + filename);
			}

			// Save score by round chart
			try {
				final JFreeChart chart = TriviaChartFactory.makeScoreByRoundChart(trivia);
				filename = CHART_DIR + "/" + roundString + "_scoreByRoundChart.png";
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved place chart to " + filename);
				filename = CHART_DIR + "/latest_scoreByRoundChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved score by round chart to " + filename);
			} catch (final IOException exception) {
				log("Couldn't save score by round chart to file " + filename);
			}

			// Save cumulative score chart
			try {
				final JFreeChart chart = TriviaChartFactory.makeCumulativePointChart(trivia);
				filename = CHART_DIR + "/" + roundString + "_cumulativeScoreChart.png";
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved cumulative score chart to " + filename);
				filename = CHART_DIR + "/latest_cumulativeScoreChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved cumulative score chart to " + filename);
			} catch (final IOException exception) {
				log("Couldn't save cumulative score chart to file " + filename);
			}

			// Save team comparison chart
			try {
				final JFreeChart chart = TriviaChartFactory.makeTeamComparisonChart(trivia);
				filename = CHART_DIR + "/" + roundString + "_teamComparisonChart.png";
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved team comparison chart to " + filename);
				filename = CHART_DIR + "/latest_teamComparisonChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved team comparison chart to " + filename);
			} catch (final IOException exception) {
				log("Couldn't save team comparison chart to file " + filename);
			}

			// Save place chart
			filename = CHART_DIR + "/latest_placeChart.png";
			try {
				final File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, TriviaChartFactory.makePlaceChart(TriviaServerEndpoint.trivia),
						CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved place chart to " + filename);
			} catch (final IOException exception) {
				log("Couldn't save place chart to file " + filename);
			}

			// Save score by round chart
			filename = CHART_DIR + "/latest_scoreByRoundChart.png";
			try {
				final File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file,
						TriviaChartFactory.makeScoreByRoundChart(TriviaServerEndpoint.trivia), CHART_WIDTH,
						CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved score by round chart to " + filename);
			} catch (final IOException exception) {
				log("Couldn't save score by round chart to file " + filename);
			}

			// Save cumulative score chart
			filename = CHART_DIR + "/latest_cumulativeScoreChart.png";
			try {
				final File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file,
						TriviaChartFactory.makeCumulativePointChart(TriviaServerEndpoint.trivia), CHART_WIDTH,
						CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved cumulative score chart to " + filename);
			} catch (final IOException exception) {
				log("Couldn't save cumulative score chart to file " + filename);
			}

			// Save team comparison chart
			filename = CHART_DIR + "/latest_teamComparisonChart.png";
			try {
				final File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file,
						TriviaChartFactory.makeTeamComparisonChart(TriviaServerEndpoint.trivia), CHART_WIDTH,
						CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved team comparison chart to " + filename);
			} catch (final IOException exception) {
				log("Couldn't save team comparison chart to file " + filename);
			}


		}

	}

	/**
	 * Fetches the standings for a round from KVSC.
	 *
	 * @param rNumber
	 *            The round number
	 * @return Array of ScoreEntry that have the standing data
	 */
	private static ScoreEntry[] getStandings(int rNumber) {

		final ArrayList<ScoreEntry> standingsList = new ArrayList<ScoreEntry>(0);

		// The URL where the file is hosted
		final String urlString = STANDINGS_BASE_URL + String.format("%02d", rNumber) + ".htm";
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

		log("Standings for round " + rNumber + " parsed.");
		return standingsList.toArray(new ScoreEntry[standingsList.size()]);
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
		log("Starting server...");
		final Server server = new Server(SERVER_URL, SERVER_PORT, "/", null, TriviaServerEndpoint.class);
		try {
			server.start();
			TriviaServerEndpoint.isRunning = true;
			while (TriviaServerEndpoint.isRunning) {
			}
		} catch (final DeploymentException exception) {
			exception.printStackTrace();
		} finally {
			server.stop();
		}
	}

	/**
	 * Get the users and roles that have been active. Active means having changed something on the server.
	 *
	 * @param window
	 *            Number of seconds without making a change before becoming idle
	 * @return The user names and roles of users who have been active within the activity window
	 */
	public static Hashtable<String, Role> getActiveUsers(Collection<TriviaServerEndpoint> clients, int timeToIdle) {
		final Date currentDate = new Date();
		final Hashtable<String, Role> userHash = new Hashtable<String, Role>(0);

		// Build a list of users who are active
		for (final TriviaServerEndpoint client : clients) {
			final Date lastDate = client.lastActive;
			final long diff = ( currentDate.getTime() - lastDate.getTime() ) / 1000;
			if (diff < timeToIdle && client.user != null) {
				userHash.put(client.user, client.role);
				// userHash.put(client.user + " (" + diff + ")", client.role);
			}
		}
		return userHash;
	}

	/**
	 * Get the users and roles that are idle. Idle means they are still contacting the server for updates, but haven't
	 * made any changes.
	 *
	 * @param window
	 *            Number of seconds without making a change before becoming idle
	 * @param timeout
	 *            Number of second before a disconnected user should be considered timed out
	 * @return The user names and roles of users who have not been active but have still received an update within the
	 *         timeout window
	 */
	public static Hashtable<String, Role> getIdleUsers(Collection<TriviaServerEndpoint> clients, int timeToIdle) {
		final Date currentDate = new Date();
		final Hashtable<String, Role> userHash = new Hashtable<String, Role>(0);
		final Set<String> activeUsers = getActiveUsers(clients, timeToIdle).keySet();

		// Build a list of users who are active
		for (final TriviaServerEndpoint client : clients) {
			final Date lastDate = client.lastActive;
			final long diff = ( currentDate.getTime() - lastDate.getTime() ) / 1000;
			if (diff >= timeToIdle && client.user != null && !activeUsers.contains(client.user)) {
				userHash.put(client.user, client.role);
				// userHash.put(client.user + " (" + diff + ")", client.role);
			}
		}
		return userHash;
	}

	/**
	 * Send updated trivia information to each connected client
	 */
	private static void updateTrivia() {
		final Set<Session> sessions = TriviaServerEndpoint.sessionList.keySet();
		for (final Session session : sessions) {
			if (session != null) {
				final TriviaServerEndpoint info = sessionList.get(session);
				final Round[] newRounds = TriviaServerEndpoint.trivia.getChangedRounds(info.lastVersions);
				if (newRounds.length != 0) {
					sendMessage(session, ServerMessageFactory.updateRounds(newRounds));
				}
			}
		}
	}

	/**
	 * Send the current round number to each connected client
	 */
	private static void updateRoundNumber() {
		final Set<Session> sessions = TriviaServerEndpoint.sessionList.keySet();
		for (final Session session : sessions) {
			sendMessage(session,
					ServerMessageFactory.updateRoundNumber(TriviaServerEndpoint.trivia.getCurrentRoundNumber()));
		}
	}

	/**
	 * Send the active and idle user lists to each connected client
	 */
	private static void updateUsers() {
		final Collection<TriviaServerEndpoint> clients = TriviaServerEndpoint.sessionList.values();
		final Set<Session> sessions = TriviaServerEndpoint.sessionList.keySet();
		for (final Session session : sessions) {
			final int timeToIdle = sessionList.get(session).timeToIdle;
			sendMessage(session,
					ServerMessageFactory.updateUserLists(TriviaServerEndpoint.getActiveUsers(clients, timeToIdle),
							TriviaServerEndpoint.getIdleUsers(clients, timeToIdle)));
		}
	}

	/**
	 * Send a message to the specified client
	 *
	 * @param session
	 * @param message
	 */
	private static void sendMessage(Session session, ServerMessage message) {
		if (session == null) return;
		session.getAsyncRemote().sendObject(message);
	}

	/**
	 * Initial hook when a client first connects (TriviaServerEndpoint() is automatically called as well)
	 *
	 * @param session
	 * @param config
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		log("New client connecting...");
		TriviaServerEndpoint.sessionList.put(session, this);
	}

	/**
	 * Handle a message from the client
	 *
	 * @param message
	 * @param session
	 */
	@OnMessage
	public void onMessage(ClientMessage message, Session session) {
		final ClientCommand command = message.getCommand();
		this.lastActive = new Date();
		switch (command) {
			case ADVANCE_ROUND:
				TriviaServerEndpoint.trivia.newRound();
				TriviaServerEndpoint.log("New round started by " + this.user);
				TriviaServerEndpoint.updateRoundNumber();
				break;
			case CHANGE_AGREEMENT:
				TriviaServerEndpoint.trivia.changeAgreement(this.user, message.getQueueIndex(), message.getAgreement());
				TriviaServerEndpoint.updateTrivia();
				break;
			case CALL_IN:
				TriviaServerEndpoint.trivia.callIn(message.getQueueIndex(), this.user);
				TriviaServerEndpoint
						.log(this.user + " is calling in item " + message.getQueueIndex() + " in the answer queue.");
				TriviaServerEndpoint.updateTrivia();
				break;
			case CHANGE_USER:
				TriviaServerEndpoint.trivia.changeName(this.user, message.getUser());
				this.user = message.getUser();
				TriviaServerEndpoint.updateTrivia();
				TriviaServerEndpoint.updateUsers();
				break;
			case CLOSE_QUESTION:
				TriviaServerEndpoint.trivia.close(message.getqNumber());
				TriviaServerEndpoint.log("Question " + message.getqNumber() + " closed, " + TriviaServerEndpoint.trivia
						.getValue(TriviaServerEndpoint.trivia.getCurrentRoundNumber(), message.getqNumber()));
				TriviaServerEndpoint.updateTrivia();
				break;
			case EDIT_QUESTION:
				if (message.getaText() == null) {
					TriviaServerEndpoint.trivia.editQuestion(message.getrNumber(), message.getqNumber(),
							message.getqValue(), message.getqText());
				} else {
					TriviaServerEndpoint.trivia.editQuestion(message.getrNumber(), message.getqNumber(),
							message.getqValue(), message.getqText(), message.getaText(), message.isCorrect(),
							message.getUser());
				}
				TriviaServerEndpoint.log("Round " + message.getrNumber() + " Question " + message.getqNumber()
						+ " edited by " + this.user);
				TriviaServerEndpoint.updateTrivia();
				break;
			case LIST_SAVES:
				sendMessage(session, ServerMessageFactory.sendSaveList(TriviaServerEndpoint.listSaves()));
				break;
			case LOAD_STATE:
				TriviaServerEndpoint.loadState(this.user, message.getSaveFilename());
				TriviaServerEndpoint.updateTrivia();
				break;
			case MARK_CORRECT:
				TriviaServerEndpoint.trivia.markCorrect(message.getQueueIndex(), this.user);
				TriviaServerEndpoint.log("Item " + message.getQueueIndex() + " in the queue is correct, "
						+ TriviaServerEndpoint.trivia.getValue(TriviaServerEndpoint.trivia.getCurrentRoundNumber(),
								TriviaServerEndpoint.trivia.getAnswerQueueQNumbers()[message.getQueueIndex()])
						+ " points earned!");
				TriviaServerEndpoint.updateTrivia();
				break;
			case MARK_DUPLICATE:
				TriviaServerEndpoint.trivia.markDuplicate(message.getQueueIndex());
				TriviaServerEndpoint.log("Item " + message.getQueueIndex() + " marked as duplicate.");
				TriviaServerEndpoint.updateTrivia();
				break;
			case MARK_INCORRECT:
				TriviaServerEndpoint.trivia.markIncorrect(message.getQueueIndex(), this.user);
				TriviaServerEndpoint.log("Item " + message.getQueueIndex() + " in the queue is incorrect.");
				TriviaServerEndpoint.updateTrivia();
				break;
			case MARK_PARTIAL:
				TriviaServerEndpoint.trivia.markPartial(message.getQueueIndex(), this.user);
				TriviaServerEndpoint.log("Item " + message.getQueueIndex() + " in the queue is partially correct.");
				TriviaServerEndpoint.updateTrivia();
				break;
			case MARK_UNCALLED:
				TriviaServerEndpoint.trivia.markUncalled(message.getQueueIndex());
				TriviaServerEndpoint.log("Item " + message.getQueueIndex() + " status reset to uncalled.");
				TriviaServerEndpoint.updateTrivia();
				break;
			case OPEN_QUESTION:
				final int qNumber = message.getqNumber();
				// final String qText = message.getqText();
				// final int qValue = message.getqValue();
				TriviaServerEndpoint.trivia.open(this.user, qNumber);
				TriviaServerEndpoint.log("Question " + qNumber + " is being typed in by " + this.user + "...");
				TriviaServerEndpoint.updateTrivia();
				break;
			case PROPOSE_ANSWER:
				TriviaServerEndpoint.trivia.proposeAnswer(message.getqNumber(), message.getaText(), this.user,
						message.getConfidence());
				TriviaServerEndpoint.log(this.user + " submitted an answer for Q" + message.getqNumber()
						+ " with a confidence of " + message.getConfidence() + ":\n" + message.getaText());
				TriviaServerEndpoint.updateTrivia();
				break;
			case REMAP_QUESTION:
				TriviaServerEndpoint.trivia.remapQuestion(message.getOldQNumber(), message.getqNumber());
				TriviaServerEndpoint
						.log(this.user + " remapped Q" + message.getOldQNumber() + " to Q" + message.getqNumber());
				TriviaServerEndpoint.updateTrivia();
				break;
			case REOPEN_QUESTION:
				TriviaServerEndpoint.trivia.reopen(message.getqNumber());
				TriviaServerEndpoint.log("Q" + message.getqNumber() + " re-opened by " + this.user);
				TriviaServerEndpoint.updateTrivia();
				break;
			case RESET_QUESTION:
				TriviaServerEndpoint.trivia.resetQuestion(message.getqNumber());
				TriviaServerEndpoint.log(this.user + " reset Q" + message.getqNumber());
				TriviaServerEndpoint.updateTrivia();
				break;
			case SET_DISCREPENCY_TEXT:
				TriviaServerEndpoint.trivia.setDiscrepencyText(message.getrNumber(), message.getDiscrepancyText());
				TriviaServerEndpoint.updateTrivia();
				break;
			case SET_IDLE_TIME:
				this.timeToIdle = message.getTimeToIdle();
				log(this.user + " time to idle set to " + this.timeToIdle);
				break;
			case SET_ROLE:
				this.user = message.getUser();
				this.role = message.getRole();
				TriviaServerEndpoint.updateUsers();
				break;
			case SET_SPEED:
				TriviaServerEndpoint.trivia.setSpeed(message.isSpeed());
				if (message.isSpeed()) {
					TriviaServerEndpoint.log(
							"Making round " + TriviaServerEndpoint.trivia.getCurrentRoundNumber() + " a speed round");
				} else {
					TriviaServerEndpoint.log("Making round " + TriviaServerEndpoint.trivia.getCurrentRoundNumber()
							+ " not a speed round");
				}
				TriviaServerEndpoint.updateTrivia();
				break;
			case SET_OPERATOR:
				TriviaServerEndpoint.trivia.setOperator(message.getQueueIndex(), message.getOperator());
				TriviaServerEndpoint.log("Operator for queue index " + message.getQueueIndex() + " set by " + this.user
						+ " to " + message.getOperator());
				TriviaServerEndpoint.updateTrivia();
				break;
			case SET_QUESTION:
				TriviaServerEndpoint.trivia.setQuestionText(message.getqNumber(), message.getqText());
				TriviaServerEndpoint.trivia.setQuestionValue(message.getqNumber(), message.getqValue());
				TriviaServerEndpoint.log("Question #" + message.getqNumber() + " set to " + message.getqText()
						+ "with a value of " + message.getqValue() + " by " + this.user);
				TriviaServerEndpoint.updateTrivia();
				break;
			case SET_ANSWER:
				TriviaServerEndpoint.trivia.setAnswer(message.getQueueIndex(), message.getaText());
				TriviaServerEndpoint.updateTrivia();
				break;
			case FETCH_TRIVIA:
				final Trivia trivia = TriviaServerEndpoint.trivia;
				sendMessage(session, ServerMessageFactory.updateTrivia(trivia));
				this.lastVersions = trivia.getVersions();
				break;
			case RESTART_TIMER:
				restartTimer();
				break;
			default:
				TriviaServerEndpoint.log("Unknown message received by server!" + message.toString());
				break;
		}
	}

	/**
	 * Handle error in communicating with a client
	 *
	 * @param session
	 */
	@OnError
	public void onError(Session session, Throwable throwable) {
		log("Error while communicating with " + TriviaServerEndpoint.sessionList.get(session).user + ":");
		throwable.printStackTrace();
	}

	/**
	 * Handle a client disconnection
	 *
	 * @param session
	 */
	@OnClose
	public void onClose(Session session) {
		TriviaServerEndpoint.log(TriviaServerEndpoint.sessionList.get(session).user + " disconnected");
		TriviaServerEndpoint.sessionList.remove(session);
	}
}
