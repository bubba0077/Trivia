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

import net.bubbaland.trivia.Answer.Agreement;
import net.bubbaland.trivia.Round;
import net.bubbaland.trivia.ScoreEntry;
import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaChartFactory;
import net.bubbaland.trivia.User;
import net.bubbaland.trivia.User.Role;
import net.bubbaland.trivia.messages.AgreementMessage;
import net.bubbaland.trivia.messages.CallInAnswerMessage;
import net.bubbaland.trivia.messages.ChangeUserMessage;
import net.bubbaland.trivia.messages.CloseQuestionMessage;
import net.bubbaland.trivia.messages.EditQuestionMessage;
import net.bubbaland.trivia.messages.SetDiscrepencyTextMessage;
import net.bubbaland.trivia.messages.SetEffortMessage;
import net.bubbaland.trivia.messages.SetNQuestionsMessage;
import net.bubbaland.trivia.messages.SetNVisualMessage;
import net.bubbaland.trivia.messages.SetOperatorMessage;
import net.bubbaland.trivia.messages.SetQuestionAnswerMessage;
import net.bubbaland.trivia.messages.SetQuestionMessage;
import net.bubbaland.trivia.messages.SetRoleMessage;
import net.bubbaland.trivia.messages.LoadSaveMessage;
import net.bubbaland.trivia.messages.MarkAnswerCorrectMessage;
import net.bubbaland.trivia.messages.MarkAnswerDuplicateMessage;
import net.bubbaland.trivia.messages.MarkAnswerIncorrectMessage;
import net.bubbaland.trivia.messages.MarkAnswerPartialMessage;
import net.bubbaland.trivia.messages.MarkAnswerUncalledMessage;
import net.bubbaland.trivia.messages.Message;
import net.bubbaland.trivia.messages.OpenQuestionMessage;
import net.bubbaland.trivia.messages.ProposeAnswerMessage;
import net.bubbaland.trivia.messages.RemapQuestionMessage;
import net.bubbaland.trivia.messages.ReopenQuestionMessage;
import net.bubbaland.trivia.messages.ResetQuestionMessage;
import net.bubbaland.trivia.messages.SaveListMessage;
import net.bubbaland.trivia.messages.SetRoundMessage;
import net.bubbaland.trivia.messages.SetShowHostMessage;
import net.bubbaland.trivia.messages.SetShowNameMessage;
import net.bubbaland.trivia.messages.SetSpeedRoundMessage;
import net.bubbaland.trivia.messages.SetTeamNumberMessage;
import net.bubbaland.trivia.messages.TriviaDataMessage;
import net.bubbaland.trivia.messages.UpdateRoundsMessage;
import net.bubbaland.trivia.messages.UserListMessage;

public class TriviaServer {

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
	// Boolean that tracks whether the server is running
	private boolean										isRunning;
	// The number of questions in a normal round
	private int											nQuestionsNormal;
	// The number of questions in a speed round
	private int											nQuestionsSpeed;
	// The maximum number of questions allowed
	private int											nQuestionsMax;
	// The number of rounds
	private int											nRounds;
	// Directory to hold backups (must exist)
	private String										saveDirectory;
	// Frequency of backups (seconds)
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
	static public final SimpleDateFormat				stringDateFormat	=
			new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
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
		// Create timer that will make save files
		this.restartTimer();
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
		this.nQuestionsMax = Integer.parseInt(this.properties.getProperty("nQuestionsMax"));
		this.teamName = this.properties.getProperty("TeamName");
		this.teamNumber = Integer.parseInt(this.properties.getProperty("TeamNumber"));
		this.serverURL = this.properties.getProperty("ServerURL");
		this.serverPort = Integer.parseInt(this.properties.getProperty("Server.Port"));
		this.saveFrequency = Integer.parseInt(this.properties.getProperty("SaveFrequency"));
		this.standingsFrequency = Integer.parseInt(this.properties.getProperty("StandingsFrequency"));
		this.saveDirectory = this.properties.getProperty("SaveDir");
		this.chartDirectory = this.properties.getProperty("ChartDir");
		this.chartWidth = Integer.parseInt(this.properties.getProperty("Chart.Width"));
		this.chartHeight = Integer.parseInt(this.properties.getProperty("Chart.Height"));
		this.standingsBaseURL = this.properties.getProperty("StandingsURL");

		/**
		 * Create a new trivia data object and list of connected clients
		 */
		this.trivia = new Trivia(this.teamName, this.teamNumber, this.nRounds, this.nQuestionsNormal,
				this.nQuestionsSpeed, this.nQuestionsMax);
		this.sessionList = new Hashtable<Session, TriviaServerEndpoint>(0);
		this.isRunning = false;
	}

	public User[] getUserList() {
		final ArrayList<User> users = new ArrayList<User>();
		// Build a list of users who are active
		for (final TriviaServerEndpoint client : this.sessionList.values()) {
			users.add(client.getUser());
		}
		return users.toArray(new User[0]);
	}

	public void processIncomingMessage(Message genericMessage, Session session) {
		String messageType = genericMessage.getClass().getSimpleName();
		final TriviaServerEndpoint userConnection = this.sessionList.get(session);
		final User user = userConnection.getUser();
		final String userName = user.getUserName();
		user.updateActivity();

		switch (messageType) {
			case "GetSaveListMessage": {
				this.sendMessage(session, new SaveListMessage(this.saveMediator.listSaves()));
				log(userName + " requested the save list");
				break;
			}
			case "LoadSaveMessage": {
				LoadSaveMessage message = (LoadSaveMessage) genericMessage;
				String saveName = message.getSaveName();
				this.trivia = this.saveMediator.loadState(this.trivia, userName, saveName);
				log("Loaded state from " + message.getSaveName());
				this.broadcastMessage(new TriviaDataMessage(trivia));

				try {
					for (int r = 1; r <= TriviaServer.this.trivia.getCurrentRoundNumber(); r++) {
						// For each past round, try to get announced standings if we don't have them
						if (!TriviaServer.this.trivia.getRound(r).isAnnounced()) {
							final ScoreEntry[] standings = TriviaServer.this.fetchStandings(r);
							if (standings != null) {
								TriviaServer.this.trivia.getRound(r).setStandings(standings,
										TriviaServer.this.trivia.getTeamName());
								TriviaServer.this.broadcastChangedRounds();
							}
						}
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}

				log(userName + " loaded save from file " + saveName);
				break;
			}
			case "FetchTriviaMessage": {
				final Trivia trivia = this.trivia;
				this.sendMessage(session, new TriviaDataMessage(trivia));
				user.setRoundVersions(trivia.getVersions());
				log(userName + " requested the trivia data object");
				break;
			}
			case "RestartTimerMessage": {
				this.restartTimer();
				TriviaServer.log("Timers restarted by " + user.getUserName());
				log(userName + " restarted the timers");
				break;
			}

			/**
			 * User messages
			 */
			case "ChangeUserMessage": {
				ChangeUserMessage message = (ChangeUserMessage) genericMessage;
				String newUserName = message.getNewUserName();
				user.setUserName(newUserName);
				this.trivia.changeUserName(userName, newUserName);
				log(userName + " changed their name to " + newUserName);
				this.broadcastMessage(new UserListMessage(this.getUserList()));
				break;
			}

			case "SetRoleMessage": {
				SetRoleMessage message = (SetRoleMessage) genericMessage;
				Role newRole = message.getNewRole();
				user.setRole(newRole);
				log(userName + " changed hir role to " + newRole);
				break;
			}

			/**
			 * Trivia messages
			 */
			case "SetRoundMessage": {
				SetRoundMessage message = (SetRoundMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				this.trivia.setCurrentRoundNumber(rNumber);
				this.broadcastMessage(message);
				log(userName + " set the current round to " + rNumber);
				break;
			}
			case "SetNVisualMessage": {
				SetNVisualMessage message = (SetNVisualMessage) genericMessage;
				int nVisual = message.getnVisual();
				this.trivia.setNVisual(nVisual);
				this.broadcastMessage(message);
				log(userName + " set the number of visual trivia to " + nVisual);
				break;
			}
			case "SetTeamNumberMessage": {
				SetTeamNumberMessage message = (SetTeamNumberMessage) genericMessage;
				int teamNumber = message.getTeamNumber();
				this.trivia.setTeamNumber(teamNumber);
				this.broadcastMessage(message);
				log(userName + " set the team number to " + teamNumber);
				break;
			}


			/**
			 * Round messages
			 */
			case "SetDiscrepencyTextMessage": {
				SetDiscrepencyTextMessage message = (SetDiscrepencyTextMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				String discrepencyText = message.getDiscrepencyText();
				this.trivia.getRound(rNumber).setDiscrepencyText(discrepencyText);
				this.broadcastChangedRounds();
				log(userName + " set the discrepency text for round " + rNumber + " to " + teamNumber);
				break;
			}
			case "SetSpeedRoundMessage": {
				SetSpeedRoundMessage message = (SetSpeedRoundMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				boolean nowSpeed = message.isNowSpeed();
				this.trivia.getRound(rNumber).setSpeed(nowSpeed);
				this.broadcastChangedRounds();
				log(userName + " set the isSpeed for round " + rNumber + " to " + nowSpeed);
				break;
			}
			case "SetShowNameMessage": {
				SetShowNameMessage message = (SetShowNameMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				String showName = message.getShowName();
				this.trivia.getRound(rNumber).setShowName(showName);
				this.broadcastChangedRounds();
				log(userName + " set the show name for round " + rNumber + " to " + showName);
				break;
			}
			case "SetShowHostMessage": {
				SetShowHostMessage message = (SetShowHostMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				String showHost = message.getShowHost();
				this.trivia.getRound(rNumber).setShowHost(showHost);
				this.broadcastChangedRounds();
				log(userName + " set the show host for round " + rNumber + " to " + showHost);
				break;
			}
			case "SetNQuestionsMessage": {
				SetNQuestionsMessage message = (SetNQuestionsMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int nQuestions = message.getNQuestions();
				this.trivia.getRound(rNumber).setNQuestions(nQuestions);
				this.broadcastChangedRounds();
				log(userName + " set the number of questions for round " + rNumber + " to " + nQuestions);
				break;
			}


			/**
			 * Question messages
			 */
			case "OpenQuestionMessage": {
				OpenQuestionMessage message = (OpenQuestionMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int qNumber = message.getQuestionNumber();
				if (this.trivia.getCurrentRoundNumber() != rNumber) {
					log("Refusing to open question from previous round requested by " + user + ": round " + rNumber
							+ " question " + qNumber);
				}
				this.trivia.getRound(rNumber).open(userName, qNumber);
				this.broadcastChangedRounds();
				log(userName + " opened round " + rNumber + " question " + qNumber);
				break;
			}
			case "CloseQuestionMessage": {
				CloseQuestionMessage message = (CloseQuestionMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int qNumber = message.getQuestionNumber();
				this.trivia.getRound(rNumber).close(qNumber, this.getUserList());
				this.broadcastChangedRounds();
				log(userName + " closed round " + rNumber + " question " + qNumber);
				break;
			}
			case "ReopenQuestionMessage": {
				ReopenQuestionMessage message = (ReopenQuestionMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int qNumber = message.getQuestionNumber();
				if (this.trivia.getCurrentRoundNumber() != rNumber) {
					log("Refusing to reopen question from previous round requested by " + user + ": round " + rNumber
							+ " question " + qNumber);
				}
				this.trivia.getRound(rNumber).reopen(qNumber);
				this.broadcastChangedRounds();
				log(userName + " reopened round " + rNumber + " question " + qNumber);
				break;
			}
			case "ResetQuestionMessage": {
				ResetQuestionMessage message = (ResetQuestionMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int qNumber = message.getQuestionNumber();
				if (this.trivia.getCurrentRoundNumber() != rNumber) {
					log("Refusing to reset question from previous round requested by " + user + ": round " + rNumber
							+ " question " + qNumber);
				}
				this.trivia.getRound(rNumber).resetQuestion(qNumber);
				this.broadcastChangedRounds();
				log(userName + " reset round " + rNumber + " question " + qNumber);
				break;
			}
			case "RemapQuestionMessage": {
				RemapQuestionMessage message = (RemapQuestionMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int qNumber = message.getQuestionNumber();
				int newQNumber = message.getNewQuestionNumber();
				this.trivia.getRound(rNumber).remapQuestion(qNumber, newQNumber);
				this.broadcastChangedRounds();
				log(userName + " changed round " + rNumber + " question " + qNumber + " to " + newQNumber);
				break;
			}
			case "SetQuestionMessage": {
				SetQuestionMessage message = (SetQuestionMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int qNumber = message.getQuestionNumber();
				String qText = message.getQuestionText();
				int qValue = message.getValue();
				this.trivia.getRound(rNumber).setQuestionText(qNumber, qText);
				this.trivia.getRound(rNumber).setValue(qNumber, qValue);
				this.broadcastChangedRounds();
				log(userName + " set round " + rNumber + " question " + qNumber + " to a value of " + qValue
						+ " with the text:\n" + qText);
				break;
			}
			case "SetQuestionAnswerMessage": {
				SetQuestionAnswerMessage message = (SetQuestionAnswerMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int qNumber = message.getQuestionNumber();
				String aText = message.getAnswerText();
				this.trivia.getRound(rNumber).setAnswerText(qNumber, aText);
				this.broadcastChangedRounds();
				log(userName + " set round " + rNumber + " question " + qNumber + "'s answer to:\n" + aText);
				break;
			}
			case "EditQuestionMessage": {
				EditQuestionMessage message = (EditQuestionMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int qNumber = message.getQuestionNumber();
				Round round = this.trivia.getRound(rNumber);
				String qText = message.getQuestionText();
				int qValue = message.getValue();
				String answerText = message.getAnswerText();
				boolean isCorrect = message.isCorrect();
				String submitter = message.getSubmitter();
				if (answerText == null) {
					round.editQuestion(qNumber, qValue, qText);
				} else {
					round.editQuestion(qNumber, qValue, qText, answerText, isCorrect, submitter);
				}
				this.broadcastChangedRounds();
				log(userName + " edited round " + rNumber + " question " + qNumber + " to:\n" + "Value: " + qValue
						+ "\n" + "Question: " + qText + "\n" + "Answer: " + answerText + "\n" + "Correct: " + isCorrect
						+ "\n" + "Submitter: " + submitter);
				break;
			}

			/**
			 * Answer Messages
			 */
			case "CallInAnswerMessage": {
				CallInAnswerMessage message = (CallInAnswerMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int queueIndex = message.getQueueIndex();
				this.trivia.getRound(rNumber).callIn(queueIndex, userName);
				this.broadcastChangedRounds();
				log(userName + " is calling in round " + rNumber + " index " + queueIndex);
				break;
			}
			case "MarkAnswerCorrectMessage": {
				MarkAnswerCorrectMessage message = (MarkAnswerCorrectMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int queueIndex = message.getQueueIndex();
				this.trivia.getRound(rNumber).markCorrect(queueIndex, userName, this.getUserList());
				this.broadcastChangedRounds();
				log(userName + " marked round " + rNumber + " index " + queueIndex + " as correct");
				break;
			}
			case "MarkAnswerDuplicateMessage": {
				MarkAnswerDuplicateMessage message = (MarkAnswerDuplicateMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int queueIndex = message.getQueueIndex();
				this.trivia.getRound(rNumber).markDuplicate(queueIndex);
				this.broadcastChangedRounds();
				log(userName + " marked round " + rNumber + " index " + queueIndex + " as a duplicate");
				break;
			}
			case "MarkAnswerIncorrectMessage": {
				MarkAnswerIncorrectMessage message = (MarkAnswerIncorrectMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int queueIndex = message.getQueueIndex();
				this.trivia.getRound(rNumber).markAnswerIncorrect(queueIndex, userName);
				this.broadcastChangedRounds();
				log(userName + " marked round " + rNumber + " index " + queueIndex + " as incorrect");
				break;
			}
			case "MarkAnswerPartialMessage": {
				MarkAnswerPartialMessage message = (MarkAnswerPartialMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int queueIndex = message.getQueueIndex();
				this.trivia.getRound(rNumber).markPartial(queueIndex, userName);
				this.broadcastChangedRounds();
				log(userName + " marked round " + rNumber + " index " + queueIndex + " as partially correct");
				break;
			}
			case "MarkAnswerUncalledMessage": {
				MarkAnswerUncalledMessage message = (MarkAnswerUncalledMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int queueIndex = message.getQueueIndex();
				this.trivia.getRound(rNumber).markUncalled(queueIndex);
				this.broadcastChangedRounds();
				log(userName + " marked round " + rNumber + " index " + queueIndex + " as uncalled");
				break;
			}

			case "SetOperatorMessage": {
				SetOperatorMessage message = (SetOperatorMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int queueIndex = message.getQueueIndex();
				String operator = "";
				if (message.getOperator() != null) {
					operator = message.getOperator();
				}
				this.trivia.getRound(rNumber).setOperator(queueIndex, operator);
				this.broadcastChangedRounds();
				log(userName + " set the operator for round " + rNumber + " index " + queueIndex + " to " + operator);
				break;
			}

			case "ProposeAnswerMessage": {
				ProposeAnswerMessage message = (ProposeAnswerMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int qNumber = message.getQuestionNumber();
				String answerText = message.getAnswerText();
				int confidence = message.getConfidence();
				this.trivia.getRound(rNumber).proposeAnswer(qNumber, answerText, userName, confidence);
				this.broadcastChangedRounds();
				log(userName + " proposed an answer for round " + rNumber + " question " + qNumber
						+ " with a confidence of " + confidence + ":\n" + answerText);
				break;
			}

			case "AgreementMessage": {
				AgreementMessage message = (AgreementMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int queueIndex = message.getQueueIndex();
				Agreement agreement = message.getAgreement();
				this.trivia.getRound(message.getRoundNumber()).getAnswer(queueIndex).changeAgreement(userName,
						agreement);
				this.broadcastChangedRounds();
				log(userName + " changed hir agreement with round " + rNumber + " index " + queueIndex + " to "
						+ agreement);
				break;
			}
			case "SetEffortMessage": {
				SetEffortMessage message = (SetEffortMessage) genericMessage;
				int rNumber = message.getRoundNumber();
				int qNumber = message.getQuestionNumber();
				user.setEffort(qNumber);
				this.broadcastChangedRounds();
				log(userName + " changed hir current effort to round " + rNumber + " question " + qNumber);
				break;
			}
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
		TriviaServer server = TriviaServerEndpoint.getServer();
		try {
			server.start();
			server.isRunning = true;
			while (server.isRunning) {}
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
		log("Trying to retrieve standings from: " + urlString);
		try {
			// Try to read the URL
			final org.jsoup.nodes.Document htmlDoc = Jsoup.connect(urlString).userAgent("Mozilla/5.0").get();
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
			log("Standings for round " + rNumber + " not available yet.");
			// e.printStackTrace();
			return null;
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
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
		if (this.saveTimer != null) {
			this.saveTimer.shutdown();
		}
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

		if (this.standingsTimer != null) {
			this.standingsTimer.shutdown();
		}
		this.standingsTimer = Executors.newSingleThreadScheduledExecutor();
		this.standingsTimer.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					for (int r = 1; r < TriviaServer.this.trivia.getCurrentRoundNumber(); r++) {
						// For each past round, try to get announced standings if we don't have them
						if (!TriviaServer.this.trivia.getRound(r).isAnnounced()) {
							final ScoreEntry[] standings = TriviaServer.this.fetchStandings(r);
							if (standings != null) {
								TriviaServer.this.trivia.getRound(r).setStandings(standings,
										TriviaServer.this.trivia.getTeamName());
								TriviaServer.this.broadcastChangedRounds();
							}
						}
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}, this.standingsFrequency, this.standingsFrequency, TimeUnit.SECONDS);
	}

	/**
	 * Send a message to the specified client
	 *
	 * @param session
	 * @param message
	 */
	private void sendMessage(Session session, Message message) {
		if (session == null) return;
		session.getAsyncRemote().sendObject(message);
	}

	private void broadcastMessage(Message message) {
		for (Session session : this.sessionList.keySet()) {
			this.sendMessage(session, message);
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
					this.sendMessage(session, new UpdateRoundsMessage(newRounds));
				}
			}
		}
	}

	public void addUser(Session session, TriviaServerEndpoint user) {
		TriviaServer.log("New client connecting... temporarily named " + user.getUser().getUserName());
		this.sessionList.put(session, user);
		this.broadcastMessage(new UserListMessage(this.getUserList()));
	}

	public void removeUser(Session session) {
		TriviaServer.log(this.sessionList.get(session).getUser() + " disconnected");
		this.sessionList.remove(session);
		this.broadcastMessage(new UserListMessage(this.getUserList()));
	}

	public void communicationsError(Session session, Throwable throwable) {
		TriviaServer
				.log("Error while communicating with " + this.sessionList.get(session).getUser().getUserName() + ":");
		throwable.printStackTrace();
	}
}
