package net.bubbaland.trivia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.trivia.Answer.Agreement;
import net.bubbaland.trivia.Answer.Status;

/**
 * A data structure for rounds.
 *
 * The <code>Round</code> class contains a number of fields with parameters for the round. There is also an array of
 * <code>Question</code>s that holds data for the individual questions in the round, and a list of <code>Answer</code>s
 * that contain the submitted answers for this round.
 *
 * @author Walter Kolczynski
 */
public class Round implements Serializable {

	private static final long			serialVersionUID	= 1601712912797562923L;

	// Data version number
	@JsonProperty("version")
	private volatile int				version;

	// The round number
	@JsonProperty("rNumber")
	final private int					rNumber;

	// The number of questions in a speed round
	@JsonProperty("nQuestionsSpeed")
	final private int					nQuestionsSpeed;

	// The number of questions in a normal round
	@JsonProperty("nQuestions")
	final private int					nQuestions;

	// The array holding the questions
	@JsonProperty("questions")
	final private Question[]			questions;

	// Whether this is a speed round
	@JsonProperty("speed")
	private volatile boolean			speed;

	// Whether the scores for this round have been announced
	@JsonProperty("announced")
	private volatile boolean			announced;

	// The announced score for our team
	@JsonProperty("announcedPoints")
	private volatile int				announcedPoints;

	// The announced place for our team
	@JsonProperty("place")
	private volatile int				place;

	// All announced scores and places for this round
	@JsonProperty("standings")
	private volatile ScoreEntry[]		standings;

	// Our team name
	@JsonProperty("teamName")
	private final String				teamName;

	// The answer queue for this round
	@JsonProperty("answerQueue")
	private volatile ArrayList<Answer>	answerQueue;

	// The discrepancy text for this round, used if the announced score does not match the calculated score
	@JsonProperty("discrepancyText")
	private String						discrepancyText;

	/**
	 * Creates a new round.
	 *
	 * @param rNumber
	 *            The round number
	 * @param nQuestionsSpeed
	 *            The number of questions in a speed round
	 * @param nQuestions
	 *            The number of questions in a normal round
	 */
	public Round(String teamName, int rNumber, int nQuestionsSpeed, int nQuestions) {

		this.teamName = teamName;
		this.speed = false;
		this.rNumber = rNumber;
		this.nQuestionsSpeed = nQuestionsSpeed;
		this.nQuestions = nQuestions;
		this.questions = new Question[nQuestionsSpeed];
		this.announced = false;
		this.announcedPoints = 0;
		this.place = 1;
		this.discrepancyText = "";
		this.version = 0;

		for (int q = 0; q < nQuestionsSpeed; q++) {
			this.questions[q] = new Question(q + 1);
		}

		this.answerQueue = new ArrayList<Answer>(0);
	}

	@JsonCreator
	private Round(@JsonProperty("version") int version, @JsonProperty("rNumber") int rNumber,
			@JsonProperty("nQuestionsSpeed") int nQuestionsSpeed, @JsonProperty("nQuestions") int nQuestions,
			@JsonProperty("questions") Question[] questions, @JsonProperty("speed") boolean speed,
			@JsonProperty("announced") boolean announced, @JsonProperty("announcedPoints") int announcedPoints,
			@JsonProperty("place") int place, @JsonProperty("standings") ScoreEntry[] standings,
			@JsonProperty("teamName") String teamName, @JsonProperty("answerQueue") ArrayList<Answer> answerQueue,
			@JsonProperty("discrepancyText") String discrepancyText) {
		this.version = version;
		this.rNumber = rNumber;
		this.nQuestionsSpeed = nQuestionsSpeed;
		this.nQuestions = nQuestions;
		this.questions = questions;
		this.speed = speed;
		this.announced = announced;
		this.announcedPoints = announcedPoints;
		this.place = place;
		this.standings = standings;
		this.teamName = teamName;
		this.answerQueue = answerQueue;
		this.discrepancyText = discrepancyText;
	}


	/**
	 * Checks if a question has been open
	 *
	 * @param qNumber
	 *            The question number
	 * @return true, if the question has been open
	 */
	public boolean beenOpen(int qNumber) {
		return this.questions[qNumber - 1].beenOpen();
	}

	/**
	 * Call an answer in the queue in.
	 *
	 * @param queueIndex
	 *            The index of the answer in the queue
	 * @param caller
	 *            The user calling the answer in
	 */
	public synchronized void callIn(int queueIndex, String caller) {
		final Answer answer = this.answerQueue.get(queueIndex);
		final Answer.Status oldStatus = answer.getStatus();
		answer.callIn(caller);
		final int qNumber = answer.getQNumber();
		if (oldStatus == Answer.Status.CORRECT) {
			this.questions[qNumber - 1].markIncorrect();
		}
		this.version++;
	}

	/**
	 * Close a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @param answer
	 *            TODO
	 */
	public synchronized void close(int qNumber, String answer) {
		this.questions[qNumber - 1].close(answer);
		this.version++;
	}

	/**
	 * Get whether each question in this round has been open.
	 *
	 * @return Array indicating whether each question has been open
	 */
	public boolean[] eachBeenOpen() {
		final boolean[] beenOpens = new boolean[this.nQuestionsSpeed];
		for (int q = 0; q < beenOpens.length; q++) {
			beenOpens[q] = this.questions[q].beenOpen();
		}
		return beenOpens;
	}

	/**
	 * Get whether each question in this round was answered correctly.
	 *
	 * @return Array indicating whether each question is correct
	 */
	public boolean[] eachCorrect() {
		final boolean[] corrects = new boolean[this.nQuestionsSpeed];
		for (int q = 0; q < corrects.length; q++) {
			corrects[q] = this.questions[q].isCorrect();
		}
		return corrects;
	}

	/**
	 * Get whether each question in this round is open.
	 *
	 * @return Array indicating whether each question is open
	 */
	public boolean[] eachOpen() {
		final boolean[] opens = new boolean[this.nQuestionsSpeed];
		for (int q = 0; q < opens.length; q++) {
			opens[q] = this.questions[q].isOpen();
		}
		return opens;
	}

	/**
	 * Gets whether the score for this round has been announced
	 *
	 * @return Whether the score for this round has been announced
	 */
	public int getAnnounced() {
		return this.announcedPoints;
	}

	public Answer[] getAnswerQueue() {
		final Answer[] queue = new Answer[this.getAnswerQueueSize()];
		this.answerQueue.toArray(queue);
		return queue;
	}

	/**
	 * Gets the proposed answer text of an answer in the queue.
	 *
	 * @param queueIndex
	 *            The index in the queue of the answer
	 * @return The proposed answer
	 */
	public String getAnswerQueueAnswer(int queueIndex) {
		return this.answerQueue.get(queueIndex).getAnswer();
	}

	/**
	 * Gets the proposed answers in the queue.
	 *
	 * @return Array of the proposed answers
	 */
	public String[] getAnswerQueueAnswers() {
		final int nAnswers = this.getAnswerQueueSize();
		final String[] answers = new String[nAnswers];
		for (int a = 0; a < nAnswers; a++) {
			answers[a] = this.answerQueue.get(a).getAnswer();
		}
		return answers;
	}

	/**
	 * Gets the caller of an answer in the queue. An uncalled answer return an empty string.
	 *
	 * @param queueIndex
	 *            The index in the queue of the answer
	 * @return The caller's name
	 */
	public String getAnswerQueueCaller(int queueIndex) {
		return this.answerQueue.get(queueIndex).getCaller();
	}

	/**
	 * Gets the callers of answers in the queue. Uncalled answers return empty strings.
	 *
	 * @return Array of caller names
	 */
	public String[] getAnswerQueueCallers() {
		final int nAnswers = this.getAnswerQueueSize();
		final String[] callers = new String[nAnswers];
		for (int a = 0; a < nAnswers; a++) {
			callers[a] = this.answerQueue.get(a).getCaller();
		}
		return callers;
	}

	/**
	 * Gets the confidence of an answer in the queue.
	 *
	 * @param queueIndex
	 *            The index in the queue of the answer
	 * @return The confidence
	 */
	public int getAnswerQueueConfidence(int queueIndex) {
		return this.answerQueue.get(queueIndex).getConfidence();
	}

	/**
	 * Gets the confidences of answers in the queue.
	 *
	 * @return Array of confidences
	 */
	public int[] getAnswerQueueConfidences() {
		final int nAnswers = this.getAnswerQueueSize();
		final int[] confidences = new int[nAnswers];
		for (int a = 0; a < nAnswers; a++) {
			confidences[a] = this.answerQueue.get(a).getConfidence();
		}
		return confidences;
	}

	/**
	 * Gets the operator of an answer in the queue. A non-correct answer returns an empty string.
	 *
	 * @param queueIndex
	 *            The index in the queue of the answer
	 * @return The operator
	 */
	public String getAnswerQueueOperator(int queueIndex) {
		return this.answerQueue.get(queueIndex).getOperator();
	}

	/**
	 * Gets the operators who accepted correct answers in the queue. Non-correct answers return empty strings.
	 *
	 * @return Array of operators
	 */
	public String[] getAnswerQueueOperators() {
		final int nAnswers = this.getAnswerQueueSize();
		final String[] operators = new String[nAnswers];
		for (int a = 0; a < nAnswers; a++) {
			operators[a] = this.answerQueue.get(a).getOperator();
		}
		return operators;
	}

	/**
	 * Gets the question number of an answer in the queue.
	 *
	 * @param queueIndex
	 *            The index in the queue of the answer
	 * @return The question number
	 */
	public int getAnswerQueueQNumber(int queueIndex) {
		return this.answerQueue.get(queueIndex).getQNumber();
	}

	/**
	 * Gets the question number of answers in the queue.
	 *
	 * @return Array of question numbers
	 */
	public int[] getAnswerQueueQNumbers() {
		final int nAnswers = this.getAnswerQueueSize();
		final int[] qNumbers = new int[nAnswers];
		for (int a = 0; a < nAnswers; a++) {
			qNumbers[a] = this.answerQueue.get(a).getQNumber();
		}
		return qNumbers;
	}

	/**
	 * Gets the size of the answer queue.
	 *
	 * @return The answer queue size
	 */
	public int getAnswerQueueSize() {
		return this.answerQueue.size();
	}

	/**
	 * Gets the status of each answer in the queue.
	 *
	 * @return Array of statuses
	 */
	public String[] getAnswerQueueStatus() {
		final int nAnswers = this.getAnswerQueueSize();
		final String[] statuses = new String[nAnswers];
		for (int a = 0; a < nAnswers; a++) {
			statuses[a] = this.answerQueue.get(a).getStatusString();
		}
		return statuses;
	}

	/**
	 * Gets the status of an answer in the queue.
	 *
	 * @param queueIndex
	 *            The index in the queue of the answer
	 * @return The status
	 */
	public String getAnswerQueueStatus(int queueIndex) {
		final String status = this.answerQueue.get(queueIndex).getStatusString();
		return status;
	}

	/**
	 * Gets the submitter of an answer in the queue.
	 *
	 * @param queueIndex
	 *            The index in the queue of the answer
	 * @return The submitter's name
	 */
	public String getAnswerQueueSubmitter(int queueIndex) {
		return this.answerQueue.get(queueIndex).getSubmitter();
	}

	/**
	 * Gets the submitters of answers in the queue.
	 *
	 * @return Array of answer submitters
	 */
	public String[] getAnswerQueueSubmitters() {
		final int nAnswers = this.getAnswerQueueSize();
		final String[] submitters = new String[nAnswers];
		for (int a = 0; a < nAnswers; a++) {
			submitters[a] = this.answerQueue.get(a).getSubmitter();
		}
		return submitters;
	}

	public void changeName(String oldName, String newName) {
		for (final Answer answer : this.answerQueue) {
			answer.changeName(oldName, newName);
		}
		for (final Question question : this.questions) {
			question.changeName(oldName, newName);
		}
	}

	/**
	 * Gets the timestamp of an answer in the queue.
	 *
	 * @param queueIndex
	 *            The index in the queue of the answer
	 * @return The timestamp
	 */
	public String getAnswerQueueTimestamp(int queueIndex) {
		return this.answerQueue.get(queueIndex).getTimestamp();
	}

	/**
	 * Gets the timestamp of each answer in the queue.
	 *
	 * @return Array of timestamps
	 */
	public String[] getAnswerQueueTimestamps() {
		final int nAnswers = this.getAnswerQueueSize();
		final String[] timestamps = new String[nAnswers];
		for (int a = 0; a < nAnswers; a++) {
			timestamps[a] = this.answerQueue.get(a).getTimestamp();
		}
		return timestamps;
	}

	/**
	 * Gets the correct answer for a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @return the answer text
	 */
	public String getAnswerText(int qNumber) {
		return this.questions[qNumber - 1].getAnswerText();
	}

	/**
	 * Gets the discrepancy text for this round.
	 *
	 * @return The discrepancy text
	 */
	public String getDiscrepancyText() {
		return this.discrepancyText;
	}

	/**
	 * Gets the answer of each question in this round.
	 *
	 * @return Array of answers
	 */
	public String[] getEachAnswerText() {
		final String[] answers = new String[this.nQuestionsSpeed];
		for (int q = 0; q < answers.length; q++) {
			answers[q] = this.questions[q].getAnswerText();
		}
		return answers;
	}

	/**
	 * Gets the points earned for each question in this round.
	 *
	 * @return Array of points earned
	 */
	public int[] getEachEarned() {
		final int[] earneds = new int[this.nQuestionsSpeed];
		for (int q = 0; q < earneds.length; q++) {
			earneds[q] = this.questions[q].getEarned();
		}
		return earneds;
	}

	/**
	 * Gets the operator for each correct answer in this round.
	 *
	 * @return Array of operators
	 */
	public String[] getEachOperator() {
		final String[] operators = new String[this.nQuestionsSpeed];
		for (int q = 0; q < operators.length; q++) {
			operators[q] = this.questions[q].getOperator();
		}
		return operators;
	}

	/**
	 * Gets the text for each question in this round.
	 *
	 * @return Array of question text
	 */
	public String[] getEachQuestionText() {
		final String[] questions = new String[this.nQuestionsSpeed];
		for (int q = 0; q < questions.length; q++) {
			questions[q] = this.questions[q].getQuestionText();
		}
		return questions;
	}

	/**
	 * Gets the name of the submitter for each correct answer in this round.
	 *
	 * @return Array of submitter names
	 */
	public String[] getEachSubmitter() {
		final String[] submitters = new String[this.nQuestionsSpeed];
		for (int q = 0; q < submitters.length; q++) {
			submitters[q] = this.questions[q].getSubmitter();
		}
		return submitters;
	}

	/**
	 * Gets the value of each question in this round.
	 *
	 * @return Array of question values
	 */
	public int[] getEachValue() {
		final int[] values = new int[this.nQuestionsSpeed];
		for (int q = 0; q < values.length; q++) {
			values[q] = this.questions[q].getValue();
		}
		return values;
	}

	/**
	 * Gets the total points earned for questions in this round.
	 *
	 * @return The total points earned
	 */
	public int getEarned() {
		int value = 0;
		for (final Question q : this.questions) {
			value += q.getEarned();
		}
		return value;
	}

	/**
	 * Gets the points earned for a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @return The points earned
	 */
	public int getEarned(int qNumber) {
		return this.questions[qNumber - 1].getEarned();
	}

	/**
	 * Gets the number of questions in this round.
	 *
	 * @return The number of questions in this round
	 */
	public int getNQuestions() {
		if (this.speed)
			return this.nQuestionsSpeed;
		else
			return this.nQuestions;
	}

	/**
	 * Gets the question numbers of currently open questions.
	 *
	 * @return Array of open question numbers
	 */
	public int[] getOpenQuestionNumbers() {
		final Question[] questions = this.getOpenQuestions();
		final int nOpen = questions.length;
		final int[] qNumbers = new int[nOpen];
		for (int q = 0; q < nOpen; q++) {
			qNumbers[q] = questions[q].getNumber();
		}
		return qNumbers;
	}

	/**
	 * Gets the text of currently open questions.
	 *
	 * @return Array of text for open questions
	 */
	public String[] getOpenQuestionText() {
		final Question[] questions = this.getOpenQuestions();
		final int nOpen = questions.length;
		final String[] questionText = new String[nOpen];
		for (int q = 0; q < nOpen; q++) {
			questionText[q] = questions[q].getQuestionText();
		}
		return questionText;
	}

	/**
	 * Gets the values of currently open questions.
	 *
	 * @return Array of the values for open questions
	 */
	public String[] getOpenQuestionValues() {
		final Question[] questions = this.getOpenQuestions();
		final int nOpen = questions.length;
		final String[] questionValues = new String[nOpen];
		for (int q = 0; q < nOpen; q++) {
			questionValues[q] = "" + questions[q].getValue();
		}
		return questionValues;
	}

	/**
	 * Gets the operator who accepted the correct answer for a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @return The operator
	 */
	public String getOperator(int qNumber) {
		return this.questions[qNumber - 1].getOperator();
	}

	/**
	 * Gets the announced place for this round.
	 *
	 * @return The place
	 */
	public int getPlace() {
		return this.place;
	}

	/**
	 * Gets the question text for the specified question.
	 *
	 * @param qNumber
	 *            The question number
	 * @return The Question
	 */
	public Question getQuestion(int qNumber) {
		return this.questions[qNumber - 1];
	}

	/**
	 * Gets the question text for a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @return the question text
	 */
	public String getQuestionText(int qNumber) {
		return this.questions[qNumber - 1].getQuestionText();
	}


	/**
	 * Gets the round number.
	 *
	 * @return The round number
	 */
	public int getRoundNumber() {
		return this.rNumber;
	}

	/**
	 * Gets the announced standings for this round.
	 *
	 * @return Array of ScoreEntry representing each team's score this round
	 */
	public ScoreEntry[] getStandings() {
		return this.standings;
	}


	/**
	 * Gets the submitter of the correct answer for a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @return The submitter's user name
	 */
	public String getSubmitter(int qNumber) {
		return this.questions[qNumber - 1].getSubmitter();
	}

	/**
	 * Gets the total value of questions in this round.
	 *
	 * @return The total value of this round
	 */
	public int getValue() {
		int value = 0;
		for (final Question q : this.questions) {
			value += q.getValue();
		}
		return value;
	}


	/**
	 * Gets the value of a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @return The value of the question
	 */
	public int getValue(int qNumber) {
		return this.questions[qNumber - 1].getValue();
	}

	public int getVersion() {
		return this.version;
	}


	/**
	 * Checks if this round's score has been announced.
	 *
	 * @return true if the score for this round has been announced
	 */
	public boolean isAnnounced() {
		return this.announced;
	}

	/**
	 * Checks if a question was answered correctly
	 *
	 * @param qNumber
	 *            The question number
	 * @return true, if the question is correct
	 */
	public boolean isCorrect(int qNumber) {
		return this.questions[qNumber - 1].isCorrect();
	}

	/**
	 * Checks if there is a mismatch between the announced score and the calculated score
	 *
	 * @return true, if there is a mismatch
	 */
	public boolean isMismatch() {
		if (this.announcedPoints != -1) return ( this.announcedPoints != this.getValue() );
		return false;
	}

	/**
	 * Checks if a question is currently open
	 *
	 * @param qNumber
	 *            The question number
	 * @return true, if the question is open
	 */
	public boolean isOpen(int qNumber) {
		return this.questions[qNumber - 1].isOpen();
	}

	/**
	 * Checks if this is a speed round.
	 *
	 * @return true if this is a speed round
	 */
	public boolean isSpeed() {
		return this.speed;
	}

	/**
	 * Mark an answer in the queue as partially correct.
	 *
	 * @param queueIndex
	 *            The index of the answer in the queue
	 * @param caller
	 *            The user calling the answer in
	 * @param operator
	 *            The operator who accepted the correct answer
	 *
	 */
	public synchronized void markCorrect(int queueIndex, String caller, String operator) {
		final Answer answer = this.answerQueue.get(queueIndex);
		answer.markCorrect(caller, operator);
		final int qNumber = answer.getQNumber();
		final String answerText = answer.getAnswer();
		final String submitter = answer.getSubmitter();
		this.questions[qNumber - 1].markCorrect(answerText, submitter, operator);
		this.version++;
	}

	/**
	 * Mark a specific question correct (used when loading saves)
	 *
	 * @param qNumber
	 *            The question number
	 * @param answerText
	 *            The correct answer
	 * @param submitter
	 *            The user who submitted the correct answer
	 * @param operator
	 *            The operator who accepted the correct answer
	 *
	 */
	public synchronized void markCorrect(int qNumber, String answerText, String submitter, String operator) {
		this.questions[qNumber - 1].markCorrect(answerText, submitter, operator);
		this.version++;
	}

	/**
	 * Mark an answer in the queue as a duplicate.
	 *
	 * @param queueIndex
	 *            The index of the answer in the queue
	 *
	 */
	public synchronized void markDuplicate(int queueIndex) {
		final Answer answer = this.answerQueue.get(queueIndex);
		answer.markDuplicate();
		final String aText = answer.getAnswer().replaceAll("\\s+", "");
		final int qNumber = answer.getQNumber();
		for (final Answer answerCheck : this.answerQueue) {
			if (answerCheck.getQueueLocation() != queueIndex + 1 && answerCheck.getQNumber() == qNumber
					&& answerCheck.getAnswer().replaceAll("\\s+", "").equalsIgnoreCase(aText)
					&& answerCheck.getStatus() != Status.DUPLICATE) {
				answerCheck.changeAgreement(answer.getSubmitter(), Agreement.AGREE);
			}
		}
		this.version++;
	}

	/**
	 * Mark a question as incorrect.
	 *
	 * @param qNumber
	 *            The question number
	 */
	public synchronized void markIncorrect(int qNumber) {
		this.questions[qNumber - 1].markIncorrect();
		this.version++;
	}

	/**
	 * Mark an answer in the queue as incorrect.
	 *
	 * @param queueIndex
	 *            The index of the answer in the queue
	 * @param caller
	 *            The user calling the answer in
	 */
	public synchronized void markIncorrect(int queueIndex, String caller) {
		final Answer answer = this.answerQueue.get(queueIndex);
		final Answer.Status oldStatus = answer.getStatus();
		answer.markIncorrect(caller);
		final int qNumber = answer.getQNumber();
		if (oldStatus == Answer.Status.CORRECT) {
			this.questions[qNumber - 1].markIncorrect();
		}
		this.version++;
	}

	/**
	 * Mark an answer in the queue as partially correct.
	 *
	 * @param queueIndex
	 *            The index of the answer in the queue
	 * @param caller
	 *            The user calling the answer in
	 */
	public synchronized void markPartial(int queueIndex, String caller) {
		final Answer answer = this.answerQueue.get(queueIndex);
		final Answer.Status oldStatus = answer.getStatus();
		answer.markPartial(caller);
		final int qNumber = answer.getQNumber();
		if (oldStatus == Answer.Status.CORRECT) {
			this.questions[qNumber - 1].markIncorrect();
		}
		this.version++;
	}

	/**
	 * Mark an answer in the queue as uncalled.
	 *
	 * @param queueIndex
	 *            The index of the answer in the queue
	 *
	 */
	public synchronized void markUncalled(int queueIndex) {
		final Answer answer = this.answerQueue.get(queueIndex);
		final Answer.Status oldStatus = answer.getStatus();
		answer.markUncalled();
		final int qNumber = answer.getQNumber();
		if (oldStatus == Answer.Status.CORRECT) {
			this.questions[qNumber - 1].markIncorrect();
		}
		this.version++;
	}

	public synchronized void changeAgreement(String user, int queueIndex, Answer.Agreement agreement) {
		this.answerQueue.get(queueIndex).changeAgreement(user, agreement);
		this.version++;
	}

	public int getAgreement(int queueIndex) {
		return this.answerQueue.get(queueIndex).getAgreement();
	}

	public Agreement getAgreement(String user, int queueIndex) {
		return this.answerQueue.get(queueIndex).getAgreement(user);
	}

	public Question[] getQuestions() {
		return this.questions;
	}

	/**
	 * Get the number of correct answers in this round
	 *
	 * @return The number of correct answers
	 */
	public int nCorrect() {
		int nCorrect = 0;
		for (final Question q : this.questions) {
			if (q.isCorrect()) {
				nCorrect++;
			}
		}
		return nCorrect;
	}

	/**
	 * Get the lowest unopened question number. If all questions have been opened, returns the last question number.
	 *
	 * @return The question number that should be opened next
	 */
	public int nextToOpen() {
		int nextToOpen = 18;
		for (final Question q : this.questions) {
			if (!q.beenOpen()) {
				nextToOpen = q.getNumber();
				break;
			}
		}
		if (nextToOpen > this.getNQuestions())
			return this.getNQuestions();
		else
			return nextToOpen;
	}

	/**
	 * Get the number of open questions in this round
	 *
	 * @return The number of open questions
	 */
	public int nOpen() {
		int nOpen = 0;
		for (final Question q : this.questions) {
			if (q.isOpen()) {
				nOpen++;
			}
		}
		return nOpen;
	}

	public int nUnopened() {
		int nUnopened = 0;
		for (int q = 0; q < this.getNQuestions(); q++) {
			if (!this.questions[q].beenOpen()) {
				nUnopened++;
			}
		}
		return nUnopened;
	}

	/**
	 * Open a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @param qValue
	 * @param qText
	 * @param value
	 *            The value of the question
	 * @param question
	 *            The text of the question
	 */
	public synchronized void open(String user, int qNumber) {
		this.questions[qNumber - 1].open(user);
		// this.questions[qNumber - 1].setQuestionText(qText);
		// this.questions[qNumber - 1].setValue(qValue);
		this.version++;
	}

	public synchronized void reopen(int qNumber) {
		this.questions[qNumber - 1].reopen();
		for (final Answer a : this.answerQueue) {
			if (a.getQNumber() == qNumber && a.getStatus() == Answer.Status.CORRECT) {
				a.markUncalled();
			}
		}
		this.version++;
	}

	/**
	 * Propose an answer for a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @param answer
	 *            The proposed answer
	 * @param submitter
	 *            The user submitting the answer
	 * @param confidence
	 *            The confidence in the answer
	 */
	public synchronized void proposeAnswer(int qNumber, String answer, String submitter, int confidence) {
		final int queueIndex = this.answerQueue.size();
		this.answerQueue.add(new Answer(queueIndex + 1, qNumber, answer, submitter, confidence));
		this.version++;
		for (final Answer answerCheck : this.answerQueue) {
			if (answerCheck.getQueueLocation() != queueIndex + 1 && answerCheck.getQNumber() == qNumber
					&& answerCheck.getAnswer().replaceAll("\\s+", "").equalsIgnoreCase(answer.replaceAll("\\s+", ""))
					&& answerCheck.getStatus() != Status.DUPLICATE) {
				this.markDuplicate(queueIndex);
				// System.out.println("Automatically marked answer #" + ( queueIndex + 1 ) + " as duplicate.");
				return;
			}
		}
	}

	public synchronized void remapQuestion(int oldQNumber, int newQNumber) {
		this.questions[newQNumber - 1].copy(this.getQuestion(oldQNumber));
		this.questions[oldQNumber - 1].reset();
		for (final Answer a : this.answerQueue) {
			if (a.getQNumber() == oldQNumber) {
				a.setQNumber(newQNumber);
			}
		}
		this.version++;
	}

	/**
	 * Reset a question.
	 *
	 * @param qNumber
	 *            The question number
	 */
	public synchronized void resetQuestion(int qNumber) {
		this.questions[qNumber - 1].reset();
		final Iterator<Answer> i = this.answerQueue.iterator();
		while (i.hasNext()) {
			if (i.next().getQNumber() == qNumber) {
				i.remove();
			}
		}
		for (int a = 0; a < this.answerQueue.size(); a++) {
			this.answerQueue.get(a).setQueueLocation(a + 1);
		}
		this.version++;
	}

	/**
	 * Returns if the round is over (all questions have been opened and are now closed).
	 *
	 * @return true, if the round is over
	 */
	public boolean roundOver() {
		boolean roundOver = true;
		for (final Question q : Arrays.copyOfRange(this.questions, 0, this.getNQuestions())) {
			roundOver = roundOver && ( q.beenOpen() && !q.isOpen() );
		}
		return roundOver;
	}

	/**
	 * Sets the announced score for this round.
	 *
	 * @param announcedPoints
	 *            The announced score
	 * @param place
	 *            The announced place
	 */
	public synchronized void setAnnounced(int announcedPoints, int place) {
		this.announced = true;
		this.announcedPoints = announcedPoints;
		this.place = place;
		this.version++;
	}

	/**
	 * Sets the correct answer text of a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @param answer
	 *            The correct answer
	 */
	public synchronized void setAnswerText(int qNumber, String answer) {
		this.questions[qNumber - 1].setAnswerText(answer);
		this.version++;
	}

	/**
	 * Sets the discrepancy text for this round.
	 *
	 * @param discrepancyText
	 *            The new discrepancy text
	 */
	public synchronized void setDiscrepancyText(String discrepancyText) {
		this.discrepancyText = discrepancyText;
		this.version++;
	}

	/**
	 * Sets the operator who accepted the correct answer for a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @param operator
	 *            The operator
	 */
	public synchronized void setOperator(int qNumber, String operator) {
		this.questions[qNumber - 1].setOperator(operator);
		this.version++;
	}

	/**
	 * Sets the question text of a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @param question
	 *            The new value
	 */
	public synchronized void setQuestionText(int qNumber, String question) {
		this.questions[qNumber - 1].setQuestionText(question);
		this.version++;
	}

	/**
	 * Make this a speed round
	 *
	 * @param isSpeed
	 */
	public synchronized void setSpeed(boolean isSpeed) {
		this.speed = isSpeed;
		this.version++;
	}

	/**
	 * Sets the announced standings for this round.
	 *
	 * @param standings
	 *            Array of ScoreEntry representing each team's score this round
	 */
	public synchronized void setStandings(ScoreEntry[] standings) {
		this.announced = true;
		this.standings = standings;
		for (final ScoreEntry entry : standings) {
			if (entry.getTeamName().equalsIgnoreCase(this.teamName)) {
				this.announcedPoints = entry.getScore();
				this.place = entry.getPlace();
			}
		}
		this.version++;
	}

	/**
	 * Sets the user who submitted a correct answer to a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @param submitter
	 *            the submitter
	 */
	public synchronized void setSubmitter(int qNumber, String submitter) {
		this.questions[qNumber - 1].setSubmitter(submitter);
		this.version++;
	}

	/**
	 * Sets the value of a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @param value
	 *            The new value
	 */
	public synchronized void setValue(int qNumber, int value) {
		this.questions[qNumber - 1].setValue(value);
		this.version++;
	}

	/**
	 * Make this a normal round
	 */
	public synchronized void unsetSpeed() {
		this.speed = false;
		this.version++;
	}

	/**
	 * Set an answer in the queue. Used when loading a saved state from file.
	 *
	 * @param qNumber
	 *            The question number
	 * @param answer
	 *            The answer text
	 * @param submitter
	 *            The user submitting the answer
	 * @param confidence
	 *            The confidence in the answer
	 * @param status
	 *            The current status
	 * @param caller
	 *            The user calling the question in
	 * @param operator
	 *            The operator who accepted the correct answer
	 */
	protected synchronized void setAnswer(int qNumber, String answer, String submitter, int confidence, String status,
			String caller, String operator, String timestamp) {
		final Answer newAnswer = new Answer(this.answerQueue.size() + 1, qNumber, answer, submitter, confidence,
				timestamp);
		this.answerQueue.add(newAnswer);
		switch (status) {
			case "Duplicate":
				newAnswer.markDuplicate();
				break;
			case "Calling":
				newAnswer.callIn(caller);
			case "Incorrect":
				newAnswer.markIncorrect(caller);
				break;
			case "Partial":
				newAnswer.markPartial(caller);
				break;
			case "Correct":
				newAnswer.markCorrect(caller, operator);
				break;
			default:
		}
		this.version++;
	}

	/**
	 * Gets the current open questions.
	 *
	 * @return Array of the open Questions
	 */
	private Question[] getOpenQuestions() {
		final int nOpen = this.nOpen();
		final Question[] questions = new Question[nOpen];
		int q1 = 0;
		for (final Question q : this.questions) {
			if (q.isOpen()) {
				questions[q1] = q;
				q1++;
			}
		}
		return questions;
	}

}
