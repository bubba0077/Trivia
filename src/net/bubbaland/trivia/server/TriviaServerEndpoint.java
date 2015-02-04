package net.bubbaland.trivia.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.Timer;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
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

import net.bubbaland.trivia.ClientMessage;
import net.bubbaland.trivia.ClientMessage.ClientCommand;
import net.bubbaland.trivia.Round;
import net.bubbaland.trivia.ScoreEntry;
import net.bubbaland.trivia.ServerMessage;
import net.bubbaland.trivia.ServerMessage.ServerMessageFactory;
import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaChartFactory;
import net.bubbaland.trivia.Trivia.Role;

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
@ServerEndpoint(decoders = { ClientMessage.MessageDecoder.class }, encoders = { ServerMessage.MessageEncoder.class }, value = "/")
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

	/**
	 * Setup properties
	 */
	final static public Properties							PROPERTIES			= new Properties();


	static {
		final InputStream defaults = TriviaServerEndpoint.class.getResourceAsStream(SETTINGS_FILENAME);

		/**
		 * Default properties
		 */
		try {
			PROPERTIES.load(defaults);
		} catch (final IOException | NullPointerException e) {
			e.printStackTrace();
			System.out.println("Couldn't load default properties file, aborting!");
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
			System.out.println("Couldn't load local properties file, may not exist.");
		}

		TriviaChartFactory.loadProperties(PROPERTIES);

		N_ROUNDS = Integer.parseInt(PROPERTIES.getProperty("nRounds"));
		N_QUESTIONS_NORMAL = Integer.parseInt(PROPERTIES.getProperty("nQuestionsNormal"));
		N_QUESTIONS_SPEED = Integer.parseInt(PROPERTIES.getProperty("nQuestionsSpeed"));
		TEAM_NAME = PROPERTIES.getProperty("TeamName");
		SERVER_URL = PROPERTIES.getProperty("ServerURL");
		SERVER_PORT = Integer.parseInt(PROPERTIES.getProperty("Server.Port"));
		SAVE_FREQUENCY = Integer.parseInt(PROPERTIES.getProperty("SaveFrequency"));
		SAVE_DIR = PROPERTIES.getProperty("SaveDir");
		CHART_DIR = PROPERTIES.getProperty("ChartDir");
		CHART_WIDTH = Integer.parseInt(PROPERTIES.getProperty("Chart.Width"));
		CHART_HEIGHT = Integer.parseInt(PROPERTIES.getProperty("Chart.Height"));
		STANDINGS_BASE_URL = PROPERTIES.getProperty("StandingsURL");


		trivia = new Trivia(TEAM_NAME, N_ROUNDS, N_QUESTIONS_NORMAL, N_QUESTIONS_SPEED);
		sessionList = new Hashtable<Session, TriviaServerEndpoint>(0);

		// Create timer that will make save files
		final Timer backupTimer = new Timer(SAVE_FREQUENCY, new ActionListener() {
			/**
			 * Handle the save timer triggers.
			 */
			public void actionPerformed(ActionEvent e) {
				final int rNumber = TriviaServerEndpoint.trivia.getCurrentRoundNumber();
				for (int r = 1; r < rNumber; r++) {
					// For each past round, try to get announced standings if we don't have them
					if (!TriviaServerEndpoint.trivia.isAnnounced(r)) {
						final ScoreEntry[] standings = getStandings(r);
						if (standings != null) {
							TriviaServerEndpoint.trivia.setStandings(r, standings);
						}
					}
				}
				TriviaServerEndpoint.saveState();
			}
		});
		backupTimer.start();
	}

	private int[]											lastVersions;
	private int												timeToIdle;
	private String											user;
	private Role											role;
	private Date											lastActive;

	/**
	 * Creates a new trivia server.
	 */
	public TriviaServerEndpoint() {
	}

	/**
	 * Get a list of the available saves.
	 * 
	 * @return Array of save file names
	 */
	public static String[] listSaves() {
		final File folder = new File(SAVE_DIR);
		final File[] files = folder.listFiles();
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
			TriviaServerEndpoint.trivia.setNTeams(Integer.parseInt(triviaElement
					.getElementsByTagName("Number_of_Teams").item(0).getTextContent()));
			TriviaServerEndpoint.trivia.setCurrentRound(Integer.parseInt(triviaElement
					.getElementsByTagName("Current_Round").item(0).getTextContent()));

			// Get a list of the round elements
			final NodeList roundElements = triviaElement.getElementsByTagName("Round");

			for (int r = 0; r < roundElements.getLength(); r++) {
				final Element roundElement = (Element) roundElements.item(r);
				// Read the round number
				final int rNumber = Integer.parseInt(roundElement.getAttribute("number"));

				// Read/set if the round is a speed round
				final boolean isSpeed = roundElement.getElementsByTagName("Speed").item(0).getTextContent()
						.equals("true");
				TriviaServerEndpoint.trivia.setSpeed(rNumber, isSpeed);

				TriviaServerEndpoint.trivia.setDiscrepencyText(rNumber,
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
					final int value = Integer.parseInt(questionElement.getElementsByTagName("Value").item(0)
							.getTextContent());
					final String question = questionElement.getElementsByTagName("Question_Text").item(0)
							.getTextContent();
					final String answer = questionElement.getElementsByTagName("Answer_Text").item(0).getTextContent();
					final String submitter = questionElement.getElementsByTagName("Submitter").item(0).getTextContent();
					final String operator = questionElement.getElementsByTagName("Operator").item(0).getTextContent();

					if (beenOpen) {
						TriviaServerEndpoint.trivia.open("From file", rNumber, qNumber, question, value);
						if (isCorrect) {
							TriviaServerEndpoint.trivia.markCorrect(rNumber, qNumber, answer, submitter, operator);
						} else if (!isOpen) {
							TriviaServerEndpoint.trivia.close(rNumber, qNumber, answer);
						}
					}
				}
			}

			final Element element = (Element) triviaElement.getElementsByTagName("Answer_Queue").item(0);

			// Get the list of propsed answer elements in the answer queue
			final NodeList answerElements = element.getElementsByTagName("Proposed_Answer");

			for (int a = 0; a < answerElements.getLength(); a++) {
				final Element answerElement = (Element) answerElements.item(a);

				// Read/set parameters of the answer
				final int qNumber = Integer.parseInt(answerElement.getElementsByTagName("Question_Number").item(0)
						.getTextContent());
				final String status = answerElement.getElementsByTagName("Status").item(0).getTextContent();
				answerElement.getElementsByTagName("Timestamp").item(0).getTextContent();
				final String answer = answerElement.getElementsByTagName("Answer_Text").item(0).getTextContent();
				final String submitter = answerElement.getElementsByTagName("Submitter").item(0).getTextContent();
				final int confidence = Integer.parseInt(answerElement.getElementsByTagName("Confidence").item(0)
						.getTextContent());
				final String caller = answerElement.getElementsByTagName("Caller").item(0).getTextContent();
				final String operator = answerElement.getElementsByTagName("Operator").item(0).getTextContent();

				TriviaServerEndpoint.trivia.setAnswer(qNumber, answer, submitter, confidence, status, caller, operator);

			}
		} catch (final ParserConfigurationException e) {


		} catch (final SAXException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		TriviaServerEndpoint.log("Loaded state from " + stateFile);

		for (int r = 1; r < TriviaServerEndpoint.trivia.getCurrentRoundNumber(); r++) {
			// For each past round, try to get announced standings if we don't have them
			if (!TriviaServerEndpoint.trivia.isAnnounced(r)) {
				final ScoreEntry[] standings = getStandings(r);
				TriviaServerEndpoint.trivia.setStandings(r, standings);
			}
		}

		TriviaServerEndpoint.updateTrivia();
		TriviaServerEndpoint.updateRoundNumber();

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

		//
		final String roundString = "Rd" + String.format("%02d", TriviaServerEndpoint.trivia.getCurrentRoundNumber());

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
			element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getNTeams() + ""));
			triviaElement.appendChild(element);

			// Save the current round number
			element = doc.createElement("Current_Round");
			element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getCurrentRoundNumber() + ""));
			triviaElement.appendChild(element);

			for (int r = 0; r < TriviaServerEndpoint.trivia.getCurrentRoundNumber(); r++) {
				// Create a round element
				final Element roundElement = doc.createElement("Round");
				triviaElement.appendChild(roundElement);

				// The round number
				attribute = doc.createAttribute("number");
				attribute.setValue(( r + 1 ) + "");
				roundElement.setAttributeNode(attribute);

				// Whether it is a speed round
				element = doc.createElement("Speed");
				element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.isSpeed(r + 1) + ""));
				roundElement.appendChild(element);

				// Whether the score has been announced for this round
				element = doc.createElement("Announced");
				element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.isAnnounced(r + 1) + ""));
				roundElement.appendChild(element);

				// The announced score for this round
				element = doc.createElement("Announced_Score");
				element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getAnnouncedPoints(r + 1) + ""));
				roundElement.appendChild(element);

				// The announced place for this round
				element = doc.createElement("Announced_Place");
				element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getAnnouncedPlace(r + 1) + ""));
				roundElement.appendChild(element);

				// The discrepancy text for this round
				element = doc.createElement("Discrepancy_Text");
				element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getDiscrepancyText(r + 1) + ""));
				roundElement.appendChild(element);

				for (int q = 0; q < TriviaServerEndpoint.trivia.getNQuestions(r + 1); q++) {
					// Create a question element
					final Element questionElement = doc.createElement("Question");
					roundElement.appendChild(questionElement);

					// The question number
					attribute = doc.createAttribute("number");
					attribute.setValue(( q + 1 ) + "");
					questionElement.setAttributeNode(attribute);

					// Whether the question has been open
					element = doc.createElement("Been_Open");
					element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.beenOpen(r + 1, q + 1) + ""));
					questionElement.appendChild(element);

					// Whether the question is currently open
					element = doc.createElement("Is_Open");
					element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.isOpen(r + 1, q + 1) + ""));
					questionElement.appendChild(element);

					// The value of the question
					element = doc.createElement("Value");
					element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getValue(r + 1, q + 1) + ""));
					questionElement.appendChild(element);

					// The question text
					element = doc.createElement("Question_Text");
					element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getQuestionText(r + 1, q + 1)
							+ ""));
					questionElement.appendChild(element);

					// The answer
					element = doc.createElement("Answer_Text");
					element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getAnswerText(r + 1, q + 1) + ""));
					questionElement.appendChild(element);

					// Whether this question was answered correctly
					element = doc.createElement("Is_Correct");
					element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.isCorrect(r + 1, q + 1) + ""));
					questionElement.appendChild(element);

					// The submitter for a correctly answered question
					element = doc.createElement("Submitter");
					element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getSubmitter(r + 1, q + 1) + ""));
					questionElement.appendChild(element);

					// The operator who accepted a correct answer
					element = doc.createElement("Operator");
					element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getOperator(r + 1, q + 1) + ""));
					questionElement.appendChild(element);
				}
			}

			// The size of the answer queue for the current round
			final int queueSize = TriviaServerEndpoint.trivia.getAnswerQueueSize(TriviaServerEndpoint.trivia
					.getCurrentRoundNumber());

			// Create a queue element
			final Element queueElement = doc.createElement("Answer_Queue");
			triviaElement.appendChild(queueElement);

			// The size of the queue
			attribute = doc.createAttribute("size");
			attribute.setValue(queueSize + "");
			queueElement.setAttributeNode(attribute);

			for (int a = 0; a < queueSize; a++) {
				// Create a proposed answer element
				final Element answerElement = doc.createElement("Proposed_Answer");
				queueElement.appendChild(answerElement);

				// The question number for this answer
				element = doc.createElement("Question_Number");
				element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getAnswerQueueQNumber(a) + ""));
				answerElement.appendChild(element);

				// The current status of this answer
				element = doc.createElement("Status");
				element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getAnswerQueueStatus(a)));
				answerElement.appendChild(element);

				// The time stamp of this answer
				element = doc.createElement("Timestamp");
				element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getAnswerQueueTimestamp(a)));
				answerElement.appendChild(element);

				// The proposed answer
				element = doc.createElement("Answer_Text");
				element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getAnswerQueueAnswer(a)));
				answerElement.appendChild(element);

				// The submitter of this answer
				element = doc.createElement("Submitter");
				element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getAnswerQueueSubmitter(a)));
				answerElement.appendChild(element);

				// The confidence in this answer
				element = doc.createElement("Confidence");
				element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getAnswerQueueConfidence(a) + ""));
				answerElement.appendChild(element);

				// The user who called this answer in
				element = doc.createElement("Caller");
				element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getAnswerQueueCaller(a)));
				answerElement.appendChild(element);

				// The operator who accepted the answer as correct
				element = doc.createElement("Operator");
				element.appendChild(doc.createTextNode(TriviaServerEndpoint.trivia.getAnswerQueueOperator(a)));
				answerElement.appendChild(element);

			}

			// write the content into xml file
			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			final Transformer transformer = transformerFactory.newTransformer();
			final DOMSource source = new DOMSource(doc);
			final StreamResult result = new StreamResult(new File(filename));
			transformer.transform(source, result);

			TriviaServerEndpoint.log("Saved state to " + filename);

		} catch (final ParserConfigurationException | TransformerException e) {
			System.out.println("Couldn't save data to file " + filename);
		}

		if (TriviaServerEndpoint.trivia.isAnnounced(1)) {
			// Save place chart
			try {
				final JFreeChart chart = TriviaChartFactory.makePlaceChart(TriviaServerEndpoint.trivia);
				filename = CHART_DIR + "/" + roundString + "_placeChart.png";
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved place chart to " + filename);
				filename = CHART_DIR + "/latest_placeChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved place chart to " + filename);
			} catch (final IOException exception) {
				System.out.println("Couldn't save place chart to file " + filename);
			}

			// Save score by round chart
			try {
				final JFreeChart chart = TriviaChartFactory.makeScoreByRoundChart(TriviaServerEndpoint.trivia);
				filename = CHART_DIR + "/" + roundString + "_scoreByRoundChart.png";
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved place chart to " + filename);
				filename = CHART_DIR + "/latest_scoreByRoundChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved score by round chart to " + filename);
			} catch (final IOException exception) {
				System.out.println("Couldn't save score by round chart to file " + filename);
			}

			// Save cumulative score chart
			try {
				final JFreeChart chart = TriviaChartFactory.makeCumulativePointChart(TriviaServerEndpoint.trivia);
				filename = CHART_DIR + "/" + roundString + "_cumulativeScoreChart.png";
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved cumulative score chart to " + filename);
				filename = CHART_DIR + "/latest_cumulativeScoreChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved cumulative score chart to " + filename);
			} catch (final IOException exception) {
				System.out.println("Couldn't save cumulative score chart to file " + filename);
			}

			// Save team comparison chart
			try {
				final JFreeChart chart = TriviaChartFactory.makeTeamComparisonChart(TriviaServerEndpoint.trivia);
				filename = CHART_DIR + "/" + roundString + "_teamComparisonChart.png";
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved team comparison chart to " + filename);
				filename = CHART_DIR + "/latest_teamComparisonChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved team comparison chart to " + filename);
			} catch (final IOException exception) {
				System.out.println("Couldn't save team comparison chart to file " + filename);
			}

			// Save place chart
			filename = CHART_DIR + "/latest_placeChart.png";
			try {
				final File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, TriviaChartFactory.makePlaceChart(TriviaServerEndpoint.trivia),
						CHART_WIDTH, CHART_HEIGHT);
				TriviaServerEndpoint.log("Saved place chart to " + filename);
			} catch (final IOException exception) {
				System.out.println("Couldn't save place chart to file " + filename);
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
				System.out.println("Couldn't save score by round chart to file " + filename);
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
				System.out.println("Couldn't save cumulative score chart to file " + filename);
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
				System.out.println("Couldn't save team comparison chart to file " + filename);
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
			System.out.println("Standings for round " + rNumber + " not available yet.");
			return null;
		} catch (final IOException e) {
			e.printStackTrace();
		}

		System.out.println("Standings for round " + rNumber + " parsed.");
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
		final Server server = new Server(SERVER_URL, SERVER_PORT, "/", null, TriviaServerEndpoint.class);
		try {
			server.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Please press a key to stop the server.");
			reader.readLine();
		} catch (DeploymentException | IOException exception) {
			exception.printStackTrace();
		} finally {
			server.stop();
		}
	}

	// private class TriviaServer {
	// // final private TriviaServer server;
	// private int[] lastVersions;
	// private int timeToIdle, lastCurrentRound;
	// private String user;
	// private Role role;
	// private Date lastActive;
	//
	// public TriviaServer(Session session) {
	// }
	// }

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

		// Build a list of users who are active
		for (final TriviaServerEndpoint client : clients) {
			final Date lastDate = client.lastActive;
			final long diff = ( currentDate.getTime() - lastDate.getTime() ) / 1000;
			if (diff >= timeToIdle && client.user != null) {
				userHash.put(client.user, client.role);
			}
		}
		return userHash;
	}

	private static synchronized void updateTrivia() {
		for (Session session : TriviaServerEndpoint.sessionList.keySet()) {
			TriviaServerEndpoint info = sessionList.get(session);
			Round[] newRounds = TriviaServerEndpoint.trivia.getChangedRounds(info.lastVersions);
			if (newRounds.length != 0) {
				sendMessage(session, ServerMessageFactory.updateRounds(newRounds));
			}
		}
	}

	private static synchronized void updateRoundNumber() {
		for (Session session : TriviaServerEndpoint.sessionList.keySet()) {
			sendMessage(session,
					ServerMessageFactory.updateRoundNumber(TriviaServerEndpoint.trivia.getCurrentRoundNumber()));
		}
	}

	private static synchronized void updateUsers() {
		Collection<TriviaServerEndpoint> clients = TriviaServerEndpoint.sessionList.values();
		for (Session session : TriviaServerEndpoint.sessionList.keySet()) {
			int timeToIdle = sessionList.get(session).timeToIdle;
			sendMessage(session, ServerMessageFactory.updateUserLists(
					TriviaServerEndpoint.getActiveUsers(clients, timeToIdle),
					TriviaServerEndpoint.getIdleUsers(clients, timeToIdle)));
		}
	}

	private static void sendMessage(Session session, ServerMessage message) {
		try {
			session.getBasicRemote().sendObject(message);
		} catch (IOException | EncodeException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		log("New client connecting...");
		TriviaServerEndpoint.sessionList.put(session, this);
	}

	@OnMessage
	public void onMessage(ClientMessage message, Session session) {
		// ClientMessage message = (ClientMessage) message1;
		ClientCommand command = message.getCommand();
		TriviaServerEndpoint info = sessionList.get(session);
		info.lastActive = new Date();
		switch (command) {
			case ADVANCE_ROUND:
				TriviaServerEndpoint.trivia.newRound();
				TriviaServerEndpoint.log("New round started by " + info.user);
				TriviaServerEndpoint.updateRoundNumber();
				break;
			case AGREE:
				TriviaServerEndpoint.trivia.agree(message.getQueueIndex());
				TriviaServerEndpoint.updateTrivia();
				break;
			case CALL_IN:
				TriviaServerEndpoint.trivia.callIn(message.getQueueIndex(), info.user);
				TriviaServerEndpoint.log(info.user + " is calling in item " + message.getQueueIndex()
						+ " in the answer queue.");
				TriviaServerEndpoint.updateTrivia();
				break;
			case CHANGE_USER:
				// this.changeUser(info.user, message.getUser());
				info.user = message.getUser();
				TriviaServerEndpoint.updateUsers();
				break;
			case CLOSE_QUESTION:
				TriviaServerEndpoint.trivia.close(message.getqNumber(), message.getaText());
				TriviaServerEndpoint.log("Question "
						+ message.getqNumber()
						+ " closed, "
						+ TriviaServerEndpoint.trivia.getValue(TriviaServerEndpoint.trivia.getCurrentRoundNumber(),
								message.getqNumber()) + " points earned.");
				TriviaServerEndpoint.updateTrivia();
				break;
			case DISAGREE:
				TriviaServerEndpoint.trivia.disagree(message.getQueueIndex());
				TriviaServerEndpoint.updateTrivia();
				break;
			case EDIT_QUESTION:
				TriviaServerEndpoint.trivia.editQuestion(message.getrNumber(), message.getqNumber(),
						message.getqValue(), message.getqText(), message.getaText(), message.isCorrect(),
						message.getUser(), message.getOperator());
				TriviaServerEndpoint.log("Round " + message.getrNumber() + " Question " + message.getqNumber()
						+ " edited by " + info.user);
				TriviaServerEndpoint.updateTrivia();
				break;
			case LIST_SAVES:
				sendMessage(session, ServerMessageFactory.sendSaveList(TriviaServerEndpoint.listSaves()));
				break;
			case LOAD_STATE:
				TriviaServerEndpoint.loadState(info.user, message.getSaveFilename());
				TriviaServerEndpoint.updateTrivia();
				break;
			case MARK_CORRECT:
				TriviaServerEndpoint.trivia.markCorrect(message.getQueueIndex(), info.user, message.getOperator());
				TriviaServerEndpoint.log("Item "
						+ message.getQueueIndex()
						+ " in the queue is correct, "
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
				TriviaServerEndpoint.trivia.markIncorrect(message.getQueueIndex(), info.user);
				TriviaServerEndpoint.log("Item " + message.getQueueIndex() + " in the queue is incorrect.");
				TriviaServerEndpoint.updateTrivia();
				break;
			case MARK_PARTIAL:
				TriviaServerEndpoint.trivia.markPartial(message.getQueueIndex(), info.user);
				TriviaServerEndpoint.log("Item " + message.getQueueIndex() + " in the queue is partially correct.");
				TriviaServerEndpoint.updateTrivia();
				break;
			case MARK_UNCALLED:
				TriviaServerEndpoint.trivia.markUncalled(message.getQueueIndex());
				TriviaServerEndpoint.log("Item " + message.getQueueIndex() + " status reset to uncalled.");
				TriviaServerEndpoint.updateTrivia();
				break;
			case OPEN_QUESTION:
				TriviaServerEndpoint.trivia.open(message.getqNumber(), message.getqText(), message.getqValue());
				TriviaServerEndpoint.log("Question " + message.getqNumber() + " opened by " + info.user);
				TriviaServerEndpoint.updateTrivia();
				break;
			case PROPOSE_ANSWER:
				TriviaServerEndpoint.trivia.proposeAnswer(message.getqNumber(), message.getaText(), info.user,
						message.getConfidence());
				TriviaServerEndpoint.log(info.user + " submitted an answer for Q" + message.getqNumber()
						+ " with a confidence of " + message.getConfidence() + ":\n" + message.getaText());
				TriviaServerEndpoint.updateTrivia();
				break;
			case REMAP_QUESTION:
				TriviaServerEndpoint.trivia.remapQuestion(message.getOldQNumber(), message.getqNumber());
				TriviaServerEndpoint.log(info.user + " remapped Q" + message.getOldQNumber() + " to Q"
						+ message.getqNumber());
				TriviaServerEndpoint.updateTrivia();
				break;
			case REOPEN_QUESTION:
				TriviaServerEndpoint.trivia.open(message.getqNumber(), TriviaServerEndpoint.trivia
						.getQuestionText(message.getqNumber()), TriviaServerEndpoint.trivia.getValue(
						TriviaServerEndpoint.trivia.getCurrentRoundNumber(), message.getqNumber()));
				TriviaServerEndpoint.log("Q" + message.getqNumber() + " re-opened by " + info.user);
				TriviaServerEndpoint.updateTrivia();
				break;
			case RESET_QUESTION:
				TriviaServerEndpoint.trivia.resetQuestion(message.getqNumber());
				TriviaServerEndpoint.log(info.user + " reset Q" + message.getqNumber());
				TriviaServerEndpoint.updateTrivia();
				break;
			case SET_DISCREPENCY_TEXT:
				TriviaServerEndpoint.trivia.setDiscrepencyText(message.getrNumber(), message.getDiscrepancyText());
				TriviaServerEndpoint.updateTrivia();
				break;
			case SET_IDLE_TIME:
				info.timeToIdle = message.getTimeToIdle();
				break;
			case SET_ROLE:
				info.user = message.getUser();
				info.role = message.getRole();
				TriviaServerEndpoint.updateUsers();
				break;
			case SET_SPEED:
				TriviaServerEndpoint.trivia.setSpeed(message.isSpeed());
				if (message.isSpeed()) {
					TriviaServerEndpoint.log("Making round " + TriviaServerEndpoint.trivia.getCurrentRoundNumber()
							+ " a speed round");
				} else {
					TriviaServerEndpoint.log("Making round " + TriviaServerEndpoint.trivia.getCurrentRoundNumber()
							+ " not a speed round");
				}
				TriviaServerEndpoint.updateTrivia();
				break;
			case FETCH_TRIVIA:
				Trivia trivia = TriviaServerEndpoint.trivia;
				sendMessage(session, ServerMessageFactory.updateTrivia(trivia));
				info.lastVersions = trivia.getVersions();
				break;
		// default:
		// break;

		}
	}

	// @OnError
	// public void onError(Session session) {
	//
	// }

	@OnClose
	public void onClose(Session session) {
		TriviaServerEndpoint.log(TriviaServerEndpoint.sessionList.get(session).user + " disconnected");
		TriviaServerEndpoint.sessionList.remove(session);
	}
}
