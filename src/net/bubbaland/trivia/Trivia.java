package net.bubbaland.trivia;

import java.io.Serializable;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.trivia.Answer.Agreement;

/**
 * The primary data structure for the trivia contest.
 *
 * The <code>Trivia</code> class holds all of the data for a trivia contest. There are a number of fields representing
 * different parameters of the contest, and an array of <code>Round</code>s that hold data for individual rounds.
 *
 * @author Walter Kolczynski
 */
public class Trivia implements Serializable {

	private static final long	serialVersionUID	= -1849743738638088417L;

	// The team name
	@JsonProperty("teamName")
	final private String		teamName;

	// The number of rounds
	@JsonProperty("nRounds")
	final private int			nRounds;

	// The number of questions in a normal round
	@JsonProperty("nQuestions")
	final private int			nQuestions;

	// The number of questions in a speed round
	@JsonProperty("nQuestionsSpeed")
	final private int			nQuestionsSpeed;

	// The number of teams in the contest
	@JsonProperty("nTeams")
	private volatile int		nTeams;

	// Number of visual trivias
	@JsonProperty("nVisual")
	private volatile int		nVisual;

	// The current round
	// private volatile Round currentRound;
	@JsonProperty("rNumber")
	private volatile int		rNumber;

	// Array of all the rounds in the contest
	@JsonProperty("rounds")
	private volatile Round[]	rounds;

	/**
	 * Creates a new trivia contest
	 *
	 * @param nRounds
	 *            The number of rounds
	 * @param nQuestions
	 *            The number of questions in a normal round
	 * @param nQuestionsSpeed
	 *            The number of questions in a speed round
	 */
	public Trivia(String teamName, int nRounds, int nQuestions, int nQuestionsSpeed) {
		this.teamName = teamName;
		this.nRounds = nRounds;
		this.nQuestions = nQuestions;
		this.nQuestionsSpeed = nQuestionsSpeed;
		this.rounds = new Round[nRounds];
		for (int r = 0; r < nRounds; r++) {
			this.rounds[r] = new Round(teamName, r + 1, nQuestionsSpeed, nQuestions);
		}
		this.rNumber = 1;
		this.nTeams = 100;
		this.nVisual = 20;
	}

	@JsonCreator
	private Trivia(@JsonProperty("teamName") String teamName, @JsonProperty("nRounds") int nRounds,
			@JsonProperty("nQuestions") int nQuestions, @JsonProperty("nQuestionsSpeed") int nQuestionsSpeed,
			@JsonProperty("nTeams") int nTeams, @JsonProperty("nVisual") int nVisuals,
			@JsonProperty("rNumber") int rNumber, @JsonProperty("rounds") Round[] rounds) {
		this.teamName = teamName;
		this.nRounds = nRounds;
		this.nQuestions = nQuestions;
		this.nQuestionsSpeed = nQuestionsSpeed;
		this.nTeams = nTeams;
		this.rNumber = rNumber;
		this.rounds = rounds;
		this.nVisual = nVisuals;
	}


	/**
	 * Gets whether a question has ever been opened.
	 *
	 * @param rNumber
	 *            The round number
	 * @param qNumber
	 *            The question number
	 * @return true, if the question has been open
	 */
	public boolean beenOpen(int rNumber, int qNumber) {
		return this.rounds[rNumber - 1].beenOpen(qNumber);
	}

	/**
	 * Call an answer in.
	 *
	 * @param queueIndex
	 *            The location of the answer in the queue
	 * @param caller
	 *            The caller's name
	 */
	public void callIn(int queueIndex, String caller) {
		this.getCurrentRound().callIn(queueIndex, caller);
	}

	/**
	 * Close a question.
	 *
	 * @param rNumber
	 *            The round number
	 * @param qNumber
	 *            The question number
	 */
	public void close(int rNumber, int qNumber) {
		this.rounds[rNumber - 1].close(qNumber);
	}

	/**
	 * Close a question in the current round.
	 *
	 * @param qNumber
	 *            The question number
	 */
	public void close(int qNumber) {
		this.getCurrentRound().close(qNumber);
	}

	/**
	 * Gets whether each question in a round has been open.
	 *
	 * @param rNumber
	 *            The round number
	 * @return Array specifying whether each question has been open
	 */
	public boolean[] eachBeenOpen(int rNumber) {
		return this.rounds[rNumber - 1].eachBeenOpen();
	}

	/**
	 * Gets whether each question in a round was answered correctly.
	 *
	 * @param rNumber
	 *            The round number
	 * @return Array specifying whether each question is correct
	 */
	public boolean[] eachCorrect(int rNumber) {
		return this.rounds[rNumber - 1].eachCorrect();
	}

	/**
	 * Gets whether each question in a round is open.
	 *
	 * @param rNumber
	 *            The round number
	 * @return Array specifying whether each question is open
	 */
	public boolean[] eachOpen(int rNumber) {
		return this.rounds[rNumber - 1].eachOpen();
	}

	/**
	 * Edit question data.
	 *
	 * @param rNumber
	 *            The round number
	 * @param qNumber
	 *            The question number
	 * @param value
	 *            The new question value
	 * @param qText
	 *            The new question text
	 * @param aText
	 *            The new correct answer
	 * @param isCorrect
	 *            Whether the question was answered correctly
	 * @param submitter
	 *            The correct answer submitter
	 * @param operator
	 *            The operator who accepted the correct answer
	 */
	public void editQuestion(int rNumber, int qNumber, int value, String qText, String aText, boolean isCorrect,
			String submitter) {
		this.editQuestion(rNumber, qNumber, value, qText);
		if (isCorrect) {
			this.rounds[rNumber - 1].markCorrect(qNumber, aText, submitter);
		} else {
			this.rounds[rNumber - 1].markIncorrect(qNumber);
		}
		if (aText != null) {
			this.rounds[rNumber - 1].setAnswerText(qNumber, aText);
		}
	}

	public void editQuestion(int rNumber, int qNumber, int value, String qText) {
		this.rounds[rNumber - 1].setValue(qNumber, value);
		this.rounds[rNumber - 1].setQuestionText(qNumber, qText);
	}

	/**
	 * Gets the announced place.
	 *
	 * @param rNumber
	 *            The round number
	 * @return The announced place
	 */
	public int getAnnouncedPlace(int rNumber) {
		if (rNumber > 0) return this.rounds[rNumber - 1].getPlace();
		return 0;
	}

	/**
	 * Gets the announced score for a round
	 *
	 * @param rNumber
	 *            The round number
	 * @return The announced score
	 */
	public int getAnnouncedPoints(int rNumber) {
		if (rNumber > 0) return this.rounds[rNumber - 1].getAnnounced();
		return 0;
	}

	/**
	 * Gets the Answers in the queue for the current round.
	 *
	 * @return The Answers
	 */
	public Answer[] getAnswerQueue() {
		return this.getCurrentRound().getAnswerQueue();
	}


	/**
	 * Gets the Answers in the queue for the current round.
	 *
	 * @return The Answers
	 */
	public Answer[] getAnswerQueue(int rNumber) {
		return this.rounds[rNumber - 1].getAnswerQueue();
	}

	/**
	 * Gets the proposed answer in the queue for the current round.
	 *
	 * @param queueIndex
	 *            The location in the queue
	 * @return The answer text
	 */
	public String getAnswerQueueAnswer(int queueIndex) {
		return this.getCurrentRound().getAnswerQueueAnswer(queueIndex);
	}

	/**
	 * Gets the proposed answers in the queue for the current round.
	 *
	 * @return Array of answers
	 */
	public String[] getAnswerQueueAnswers() {
		return this.getCurrentRound().getAnswerQueueAnswers();
	}

	/**
	 * Gets the caller of an answer in the queue for the current round.
	 *
	 * @param queueIndex
	 *            The location in the queue
	 * @return The caller's name
	 */
	public String getAnswerQueueCaller(int queueIndex) {
		return this.getCurrentRound().getAnswerQueueCaller(queueIndex);
	}

	/**
	 * Gets the callers of answers in the queue for the current round.
	 *
	 * @return Array of caller names
	 */
	public String[] getAnswerQueueCallers() {
		return this.getCurrentRound().getAnswerQueueCallers();
	}

	/**
	 * Gets the confidence in answer in the queue for the current round.
	 *
	 * @param queueIndex
	 *            The location in the queue
	 * @return The confidence
	 */
	public int getAnswerQueueConfidence(int queueIndex) {
		return this.getCurrentRound().getAnswerQueueConfidence(queueIndex);
	}

	/**
	 * Gets the confidences in answers in the queue for the current round.
	 *
	 * @return Array of confidences
	 */
	public int[] getAnswerQueueConfidences() {
		return this.getCurrentRound().getAnswerQueueConfidences();
	}

	/**
	 * Gets the operators who accepted a correct answer in the queue for the current round.
	 *
	 * @param queueIndex
	 *            The location in the queue
	 * @return The operator
	 */
	public String getAnswerQueueOperator(int queueIndex) {
		return this.getCurrentRound().getAnswerQueueOperator(queueIndex);
	}

	/**
	 * Gets the operators who accepted correct answers in the queue for the current round.
	 *
	 * @return Array of operators
	 */
	public String[] getAnswerQueueOperators() {
		return this.getCurrentRound().getAnswerQueueOperators();
	}

	/**
	 * Gets the question number of an answer in the queue for the current round.
	 *
	 * @param queueIndex
	 *            The location in the queue
	 * @return The question number
	 */
	public int getAnswerQueueQNumber(int queueIndex) {
		return this.getCurrentRound().getAnswerQueueQNumber(queueIndex);
	}

	/**
	 * Gets the question number for answers in the queue for the current round.
	 *
	 * @return Array of question numbers
	 */
	public int[] getAnswerQueueQNumbers() {
		return this.getCurrentRound().getAnswerQueueQNumbers();
	}

	/**
	 * Gets the size of the answer queue for the current round.
	 *
	 * @param rNumber
	 *
	 * @return The answer queue size
	 */
	public int getAnswerQueueSize(int rNumber) {
		return this.rounds[rNumber - 1].getAnswerQueueSize();
	}

	/**
	 * Gets the status of an answer in the queue.
	 *
	 * @param queueIndex
	 *            The location in the queue
	 * @return The answer status
	 */
	public String getAnswerQueueStatus(int queueIndex) {
		return this.getCurrentRound().getAnswerQueueStatus(queueIndex);
	}

	/**
	 * Gets the status of answers in the queue
	 *
	 * @return Array of statuses
	 */
	public String[] getAnswerQueueStatuses() {
		return this.getCurrentRound().getAnswerQueueStatus();
	}

	/**
	 * Gets the submitter of an answer in the queue for the current round.
	 *
	 * @param queueIndex
	 *            The location in the queue
	 * @return The submitter's name
	 */
	public String getAnswerQueueSubmitter(int queueIndex) {
		return this.getCurrentRound().getAnswerQueueSubmitter(queueIndex);
	}

	/**
	 * Gets the submitters for answers in the queue of the current round.
	 *
	 * @return Array of submitters' names
	 */
	public String[] getAnswerQueueSubmitters() {
		return this.getCurrentRound().getAnswerQueueSubmitters();
	}

	/**
	 * Gets the timestamp of an answer in the queue for the current round.
	 *
	 * @param queueIndex
	 *            The location in the queue
	 * @return The timestamp of the proposed answer
	 */
	public String getAnswerQueueTimestamp(int queueIndex) {
		return this.getCurrentRound().getAnswerQueueTimestamp(queueIndex);
	}

	/**
	 * Gets the timestamps of answers in the queue for the current round.
	 *
	 * @return Array of timestamps
	 */
	public String[] getAnswerQueueTimestamps() {
		return this.getCurrentRound().getAnswerQueueTimestamps();
	}

	/**
	 * Gets the answer to a question.
	 *
	 * @param rNumber
	 *            The round number
	 * @param qNumber
	 *            The question number
	 * @return The answer text
	 */
	public String getAnswerText(int rNumber, int qNumber) {
		return this.rounds[rNumber - 1].getAnswerText(qNumber);
	}

	/**
	 * Get rounds that have changed. This is the primary method for retrieving updated data from the server.
	 *
	 * @param oldVersions
	 *            The round version numbers the user has.
	 * @return An array of all the rounds that have newer versions.
	 */
	public Round[] getChangedRounds(int[] oldVersions) {
		final ArrayList<Round> changedRoundList = new ArrayList<Round>(0);
		for (int r = 0; r < this.nRounds; r++) {
			final Round round = this.rounds[r];
			if (oldVersions[r] != round.getVersion()) {
				changedRoundList.add(round);
			}
		}
		final Round[] changedRounds = new Round[changedRoundList.size()];
		changedRoundList.toArray(changedRounds);
		return changedRounds;
	}

	/**
	 * Gets the cumulative points earned through a round
	 *
	 * @param rNumber
	 *            The round number
	 * @return The cumulative number of points earned
	 */
	public int getCumulativeEarned(int rNumber) {
		int earned = 0;
		for (int r = 0; r < rNumber; r++) {
			earned += this.rounds[r].getEarned();
		}
		return earned;
	}

	/**
	 * Gets the cumulative value of questions through a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @return The cumulative value
	 */
	public int getCumulativeValue(int rNumber) {
		int value = 0;
		for (int r = 0; r < rNumber; r++) {
			value += this.rounds[r].getValue();
		}
		return value;
	}

	/**
	 * Gets the points earned for questions in the current round.
	 *
	 * @return The points earned
	 */
	public int getCurrentRoundEarned() {
		return this.getCurrentRound().getEarned();
	}

	/**
	 * Gets the current round number.
	 *
	 * @return The current round number
	 */
	public int getCurrentRoundNumber() {
		return this.rNumber;
	}

	private Round getCurrentRound() {
		return this.rounds[this.rNumber - 1];
	}

	/**
	 * Gets the value of questions in the current round.
	 *
	 * @return The value
	 */
	public int getCurrentRoundValue() {
		return this.getCurrentRound().getValue();
	}

	/**
	 * Gets the discrepancy text for a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @return The discrepancy text
	 */
	public String getDiscrepancyText(int rNumber) {
		return this.rounds[rNumber - 1].getDiscrepancyText();
	}

	/**
	 * Gets the answer for each question in a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @return Array of answers
	 */
	public String[] getEachAnswerText(int rNumber) {
		return this.rounds[rNumber - 1].getEachAnswerText();
	}

	/**
	 * Gets the earned points for each question in a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @return Array of points earned
	 */
	public int[] getEachEarned(int rNumber) {
		return this.rounds[rNumber - 1].getEachEarned();
	}

	// /**
	// * Gets the operator for each question in a round.
	// *
	// * @param rNumber
	// * The round number
	// * @return Array of operators
	// */
	// public String[] getEachOperator(int rNumber) {
	// return this.rounds[rNumber - 1].getEachOperator();
	// }

	/**
	 * Gets the text of each question in a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @return Array of questions
	 */
	public String[] getEachQuestionText(int rNumber) {
		return this.rounds[rNumber - 1].getEachQuestionText();
	}

	/**
	 * Gets the submitter for each question in a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @return Array of submitter names
	 */
	public String[] getEachSubmitter(int rNumber) {
		return this.rounds[rNumber - 1].getEachSubmitter();
	}

	/**
	 * Gets the value of each question in a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @return Array of values
	 */
	public int[] getEachValue(int rNumber) {
		return this.rounds[rNumber - 1].getEachValue();
	}


	/**
	 * Gets the total points earned for the contest.
	 *
	 * @return The number of points earned
	 */
	public int getEarned() {
		int earned = 0;
		for (final Round r : this.rounds) {
			earned += r.getEarned();
		}
		return earned;
	}

	/**
	 * Gets the total points earned in a round
	 *
	 * @param rNumber
	 *            The round number
	 * @return The number of points earned
	 */
	public int getEarned(int rNumber) {
		return this.rounds[rNumber - 1].getEarned();
	}

	/**
	 * Gets the points earned on a question.
	 *
	 * @param rNumber
	 *            The round number
	 * @param qNumber
	 *            The question number
	 * @return The number of points earned
	 */
	public int getEarned(int rNumber, int qNumber) {
		return this.rounds[rNumber - 1].getEarned(qNumber);
	}

	/**
	 * Gets the number of questions in a speed round.
	 *
	 * @return The number of questions
	 */
	public int getMaxQuestions() {
		return this.nQuestionsSpeed;
	}

	/**
	 * Gets the number of questions in the current round.
	 *
	 * @return The number of questions
	 */
	public int getNQuestions() {
		return this.getCurrentRound().getNQuestions();
	}

	/**
	 * Gets the number of questions in a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @return The number of questions
	 */
	public int getNQuestions(int rNumber) {
		return this.rounds[rNumber - 1].getNQuestions();
	}

	/**
	 * Gets the number of rounds.
	 *
	 * @return The number of rounds
	 */
	public int getNRounds() {
		return this.nRounds;
	}

	/**
	 * Gets the number of teams in the trivia contest
	 *
	 * @return The number of teams
	 */
	public int getNTeams() {
		return this.nTeams;
	}

	/**
	 * Gets the currently open question numbers.
	 *
	 * @return Array of question numbers
	 */
	public int[] getOpenQuestionNumbers() {
		return this.getCurrentRound().getOpenQuestionNumbers();
	}

	/**
	 * Gets the currently open questions.
	 *
	 * @return Array of questions
	 */
	public String[] getOpenQuestionText() {
		return this.getCurrentRound().getOpenQuestionText();
	}

	/**
	 * Gets the currently open questions' values
	 *
	 * @return Array of question values
	 */
	public String[] getOpenQuestionValues() {
		return this.getCurrentRound().getOpenQuestionValues();
	}

	/**
	 * Gets the operator who accepted a correct answer to a question.
	 *
	 * @param rNumber
	 *            The round number
	 * @param qNumber
	 *            The question number
	 * @return The operator
	 */
	public String getOperator(int rNumber, int queueIndex) {
		return this.rounds[rNumber - 1].getOperator(queueIndex);
	}

	/**
	 * Gets the text of a question in the current round.
	 *
	 * @param qNumber
	 *            The question number
	 * @return The question text
	 */
	public String getQuestionText(int qNumber) {
		return this.getCurrentRound().getQuestionText(qNumber);
	}

	/**
	 * Gets the text of a question.
	 *
	 * @param rNumber
	 *            The round number
	 * @param qNumber
	 *            The question number
	 * @return The question text
	 */
	public String getQuestionText(int rNumber, int qNumber) {
		return this.rounds[rNumber - 1].getQuestionText(qNumber);
	}

	/**
	 * Get the standings for a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @return Array of ScoreEntry
	 */
	public ScoreEntry[] getStandings(int rNumber) {
		return this.rounds[rNumber - 1].getStandings();
	}

	/**
	 * Gets the submitter of a correct answer to a question.
	 *
	 * @param rNumber
	 *            The round number
	 * @param qNumber
	 *            The question number
	 * @return The submitter's name
	 */
	public String getSubmitter(int rNumber, int qNumber) {
		return this.rounds[rNumber - 1].getSubmitter(qNumber);
	}

	/**
	 * Gets the team name.
	 *
	 * @return The team name
	 */
	public String getTeamName() {
		return this.teamName;
	}

	/**
	 * Gets the total value of all questions in the contest.
	 *
	 * @return The total value
	 */
	public int getValue() {
		int value = 0;
		for (final Round r : this.rounds) {
			value += r.getValue();
		}
		return value;
	}

	public Round[] getRounds() {
		return this.rounds;
	}

	/**
	 * Gets the value of all questions in a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @return The value
	 */
	public int getValue(int rNumber) {
		return this.rounds[rNumber - 1].getValue();
	}

	/**
	 * Gets the value of a question.
	 *
	 * @param rNumber
	 *            The round number
	 * @param qNumber
	 *            The question number
	 * @return the value
	 */
	public int getValue(int rNumber, int qNumber) {
		return this.rounds[rNumber - 1].getValue(qNumber);
	}

	/**
	 * Get the version of each Round.
	 *
	 * @return The version number for each round
	 */
	public int[] getVersions() {
		final int[] versions = new int[this.nRounds];
		for (int r = 0; r < this.nRounds; r++) {
			versions[r] = this.rounds[r].getVersion();
		}
		return versions;
	}

	/**
	 * Checks if the score has been announced for a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @return true, if the score has been announced
	 */
	public boolean isAnnounced(int rNumber) {
		if (rNumber > 0) return this.rounds[rNumber - 1].isAnnounced();
		return false;
	}

	/**
	 * Checks if the question was answered correctly.
	 *
	 * @return true, if the question is correct
	 */
	public boolean isCorrect(int rNumber, int qNumber) {
		return this.rounds[rNumber - 1].isCorrect(qNumber);
	}

	/**
	 * Checks if the current round is a speed round.
	 *
	 * @return true, if the round is a speed round
	 */
	public boolean isCurrentSpeed() {
		return this.getCurrentRound().isSpeed();
	}

	/**
	 * Checks if the question in the current round is open.
	 *
	 * @param qNumber
	 *            The question number
	 * @return true, if the question is open
	 */
	public boolean isOpen(int qNumber) {
		return this.getCurrentRound().isOpen(qNumber);
	}

	/**
	 * Checks if the question is open.
	 *
	 * @param rNumber
	 *            The round number
	 * @param qNumber
	 *            The question number
	 * @return true, if the question is open
	 */
	public boolean isOpen(int rNumber, int qNumber) {
		return this.rounds[rNumber - 1].isOpen(qNumber);
	}

	/**
	 * Checks if the round is a speed round.
	 *
	 * @param rNumber
	 *            The round number
	 * @return true, if the round is a speed round
	 */
	public boolean isSpeed(int rNumber) {
		return this.rounds[rNumber - 1].isSpeed();
	}

	/**
	 * Mark an question as correct.
	 *
	 * @param rNumber
	 *            The round number
	 * @param qNumber
	 *            The question number
	 * @param answer
	 *            The answer
	 * @param submitter
	 *            The user who submitted the correct answer
	 * @param operator
	 *            The operator who accepted the answer
	 */
	public void markCorrect(int rNumber, int qNumber, String answer, String submitter) {
		this.rounds[rNumber - 1].markCorrect(qNumber, answer, submitter);
	}

	/**
	 * Mark an answer as correct.
	 *
	 * @param queueIndex
	 *            The location of the answer in the queue
	 * @param caller
	 *            The caller's name
	 * @param operator
	 *            The operator who accepted the answer
	 */
	public void markCorrect(int queueIndex, String caller) {
		this.getCurrentRound().markCorrect(queueIndex, caller);
	}

	/**
	 * Mark an answer as a duplicate.
	 *
	 * @param queueIndex
	 *            The location of the answer in the queue
	 */
	public void markDuplicate(int queueIndex) {
		this.getCurrentRound().markDuplicate(queueIndex);
	}

	/**
	 * Mark an answer as incorrect.
	 *
	 * @param queueIndex
	 *            The location of the answer in the queue
	 * @param caller
	 *            The caller's name
	 */
	public void markIncorrect(int queueIndex, String caller) {
		this.getCurrentRound().markIncorrect(queueIndex, caller);
	}

	/**
	 * Mark an answer as partially correct.
	 *
	 * @param queueIndex
	 *            The location of the answer in the queue
	 * @param caller
	 *            The caller's name
	 */
	public void markPartial(int queueIndex, String caller) {
		this.getCurrentRound().markPartial(queueIndex, caller);
	}

	/**
	 * Mark an answer as uncalled.
	 *
	 * @param queueIndex
	 *            The location of the answer in the queue
	 */
	public void markUncalled(int queueIndex) {
		this.getCurrentRound().markUncalled(queueIndex);
	}

	/**
	 * Start a new round.
	 */
	public void newRound() {
		if (this.getCurrentRound().nOpen() == 0 && this.getCurrentRound().nUnopened() == 0
				&& this.rNumber + 1 <= this.nRounds) {
			this.rNumber++;
		}
	}

	/**
	 * Gets the next question that needs to be opened.
	 *
	 * @return The question number
	 */
	public int nextToOpen() {
		return this.getCurrentRound().nextToOpen();
	}

	/**
	 * Gets the number of questions still to open in the current round.
	 *
	 * @return Number of questions not yet opened
	 */
	public int nUnopened() {
		return this.getCurrentRound().nUnopened();
	}

	/**
	 * Open a question.
	 *
	 * @param rNumber
	 *            The round number
	 * @param qNumber
	 *            The question number
	 * @param qValue
	 *            The question's value
	 * @param question
	 *            The question text
	 */
	public void open(String user, int rNumber, int qNumber) {
		this.rounds[rNumber - 1].open(user, qNumber);
	}

	/**
	 * Open a question in the current round.
	 *
	 * @param qNumber
	 *            The question number
	 * @param qValue
	 *            The question's value
	 * @param question
	 *            The question text
	 */
	public void open(String user, int qNumber) {
		this.getCurrentRound().open(user, qNumber);
	}

	public void reopen(int qNumber) {
		this.getCurrentRound().reopen(qNumber);
	}


	/**
	 * Propose an answer.
	 *
	 * @param qNumber
	 *            The question number
	 * @param answer
	 *            The answer
	 * @param user
	 *            The user's name
	 * @param confidence
	 *            The confidence in the answer
	 */
	public void proposeAnswer(int qNumber, String answer, String user, int confidence) {
		this.getCurrentRound().proposeAnswer(qNumber, answer, user, confidence);
	}

	public void remapQuestion(int oldQNumber, int newQNumber) {
		this.getCurrentRound().remapQuestion(oldQNumber, newQNumber);
	}

	/**
	 * Reset the entire trivia contest.
	 */
	public void reset() {
		for (int r = 0; r < this.nRounds; r++) {
			this.rounds[r] = new Round(this.teamName, r + 1, this.nQuestionsSpeed, this.nQuestions);
		}
	}

	/**
	 * Reset a question.
	 *
	 * @param qNumber
	 *            The question number
	 */
	public void resetQuestion(int qNumber) {
		this.getCurrentRound().resetQuestion(qNumber);
	}

	/**
	 * Checks if the current round is over.
	 *
	 * @return true, if the round is over
	 */
	public boolean roundOver() {
		return this.getCurrentRound().roundOver();
	}

	/**
	 * Sets the announced score for a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @param score
	 *            The announced score
	 * @param place
	 *            The announced place
	 */
	public void setAnnounced(int rNumber, int score, int place) {
		if (rNumber > 0) {
			this.rounds[rNumber - 1].setAnnounced(score, place);
		}
		return;
	}

	public void setAnswer(int rNumber, int qNumber, String answer, String submitter, int confidence, String status,
			String caller, String operator, String timestamp) {
		this.rounds[rNumber - 1].setAnswer(qNumber, answer, submitter, confidence, status, caller, operator, timestamp);
	}

	public void setAnswer(int queueIndex, String answer) {
		getCurrentRound().setAnswerText(getCurrentRound().getAnswerQueueQNumber(queueIndex), answer);
	}

	public void setOperator(int queueIndex, String operator) {
		getCurrentRound().setOperator(queueIndex, operator);
	}

	/**
	 * Change the current round.
	 *
	 * @param rNumber
	 *            The new current round number
	 */
	public void setCurrentRound(int rNumber) {
		this.rNumber = rNumber;
	}

	/**
	 * Sets the discrepancy text for a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @param discrepencyText
	 *            The new discrepancy text
	 */
	public void setDiscrepencyText(int rNumber, String discrepencyText) {
		this.rounds[rNumber - 1].setDiscrepancyText(discrepencyText);
	}

	/**
	 * Sets the number of teams.
	 *
	 * @param nTeams
	 *            The new number of teams
	 */
	public void setNTeams(int nTeams) {
		this.nTeams = nTeams;
	}

	public void setNVisual(int nVisual) {
		this.nVisual = nVisual;
	}

	public int getNVisual() {
		return this.nVisual;
	}

	public void setQuestionText(int qNumber, String qText) {
		this.getCurrentRound().setQuestionText(qNumber, qText);
	}

	public void setQuestionValue(int qNumber, int value) {
		this.getCurrentRound().setValue(qNumber, value);
	}

	public void setQuestionText(int rNumber, int qNumber, String qText) {
		this.rounds[rNumber - 1].setQuestionText(qNumber, qText);
	}

	public void setQuestionValue(int rNumber, int qNumber, int value) {
		this.rounds[rNumber - 1].setValue(qNumber, value);
	}

	/**
	 * Makes the current round a speed round.
	 */
	public void setSpeed(boolean isSpeed) {
		this.getCurrentRound().setSpeed(isSpeed);
	}

	/**
	 * Makes the current round a speed round.
	 */
	public void setSpeed(int rNumber, boolean isSpeed) {
		this.rounds[rNumber - 1].setSpeed(isSpeed);
	}

	/**
	 * Set the standings for a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @param standings
	 *            An array of ScoreEntry
	 */
	public void setStandings(int rNumber, ScoreEntry[] standings) {
		this.nTeams = standings.length;
		this.rounds[rNumber - 1].setStandings(standings);
	}

	/**
	 * Make the current round a normal round.
	 */
	public void unsetSpeed() {
		this.getCurrentRound().unsetSpeed();
	}

	/**
	 * Make the round a normal round.
	 *
	 * @param rNumber
	 *            The round number
	 */
	public void unsetSpeed(int rNumber) {
		this.rounds[rNumber - 1].unsetSpeed();
	}

	/**
	 * Replace rounds with newer version retrieved from server.
	 *
	 * @param newRounds
	 *            The rounds with updated versions
	 */
	public synchronized void updateRounds(Round[] newRounds) {
		final int nNew = newRounds.length;
		for (int r = 0; r < nNew; r++) {
			final Round newRound = newRounds[r];
			final int rNumber = newRound.getRoundNumber();
			this.rounds[rNumber - 1] = newRound;
			if (newRound.isAnnounced()) {
				this.nTeams = newRound.getStandings().length;
			}
		}
	}

	public void changeName(String oldName, String newName) {
		this.getCurrentRound().changeName(oldName, newName);
	}

	public void changeAgreement(String user, int queueIndex, Answer.Agreement agreement) {
		this.getCurrentRound().changeAgreement(user, queueIndex, agreement);
	}

	public int getAgreement(int queueIndex) {
		return this.getCurrentRound().getAgreement(queueIndex);
	}

	public Agreement getAgreement(String user, int queueIndex) {
		return this.getCurrentRound().getAgreement(user, queueIndex);
	}

	public String toString() {
		String s = "Data dump for entire Trivia object\n";
		s = s + "Team Name: " + this.teamName + "  #Teams: " + this.nTeams + "\n";
		s = s + "nRounds: " + this.nRounds + " nQuestions: " + this.nQuestions + "nQuestions(speed): "
				+ this.nQuestionsSpeed + "\n";
		s = s + "Current round: " + this.rNumber + "\n";
		for (Round r : this.rounds) {
			s = s + r.toString();
		}
		return s;
	}
}
