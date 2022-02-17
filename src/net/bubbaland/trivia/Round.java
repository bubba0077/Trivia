package net.bubbaland.trivia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	@JsonProperty("nQuestionsNormal")
	final private int					nQuestionsNormal;

	// The number of questions in this round
	@JsonProperty("nQuestions")
	private volatile int				nQuestions;

	// The array holding the questions
	@JsonProperty("questions")
	final private ArrayList<Question>	questions;

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

	// The answer queue for this round
	@JsonProperty("answerQueue")
	private volatile ArrayList<Answer>	answerQueue;

	// The discrepancy text for this round, used if the announced score does not match the calculated score
	@JsonProperty("discrepancyText")
	private volatile String				discrepancyText;

	@JsonProperty("showName")
	private volatile String				showName;

	@JsonProperty("showHost")
	private volatile String				showHost;

	/**
	 * @return the showName
	 */
	public String getShowName() {
		return this.showName;
	}

	/**
	 * @param showName
	 *            the showName to set
	 */
	public void setShowName(String showName) {
		this.showName = showName;
		this.version++;
	}

	/**
	 * @return the showHost
	 */
	public String getShowHost() {
		return this.showHost;
	}

	/**
	 * @param showHost
	 *            the showHost to set
	 */
	public void setShowHost(String showHost) {
		this.showHost = showHost;
		this.version++;
	}

	/**
	 * Creates a new round.
	 *
	 * @param rNumber
	 *            The round number
	 * @param nQuestionsNormal
	 *            The number of questions in a normal round
	 * @param nQuestionsSpeed
	 *            The number of questions in a speed round
	 */
	public Round(int rNumber, int nQuestionsNormal, int nQuestionsSpeed) {

		this.speed = false;
		this.rNumber = rNumber;
		this.nQuestionsSpeed = nQuestionsSpeed;
		this.nQuestionsNormal = nQuestionsNormal;
		this.questions = new ArrayList<Question>(0);
		this.announced = false;
		this.announcedPoints = 0;
		this.place = 1;
		this.discrepancyText = "";
		this.showName = "";
		this.showHost = "";
		this.version = 0;
		this.setNQuestions(this.nQuestionsNormal);
		this.answerQueue = new ArrayList<Answer>(0);
	}

	@JsonCreator
	private Round(@JsonProperty("version") int version, @JsonProperty("rNumber") int rNumber,
			@JsonProperty("nQuestionsSpeed") int nQuestionsSpeed, @JsonProperty("nQuestionsNormal") int nQuestions,
			@JsonProperty("questions") ArrayList<Question> questions, @JsonProperty("speed") boolean speed,
			@JsonProperty("announced") boolean announced, @JsonProperty("announcedPoints") int announcedPoints,
			@JsonProperty("place") int place, @JsonProperty("standings") ScoreEntry[] standings,
			@JsonProperty("answerQueue") ArrayList<Answer> answerQueue, @JsonProperty("showName") String showName,
			@JsonProperty("showHost") String showHost, @JsonProperty("discrepancyText") String discrepancyText) {
		this.version = version;
		this.rNumber = rNumber;
		this.nQuestionsSpeed = nQuestionsSpeed;
		this.nQuestionsNormal = nQuestions;
		this.questions = questions;
		this.speed = speed;
		this.announced = announced;
		this.announcedPoints = announcedPoints;
		this.place = place;
		this.standings = standings;
		this.answerQueue = answerQueue;
		this.showName = showName;
		this.showHost = showHost;
		this.discrepancyText = discrepancyText;
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
			this.getQuestion(qNumber).openQuestion("");
		}
		this.version++;
	}

	/**
	 * Close a question.
	 *
	 * @param qNumber
	 *            The question number
	 * @param userList
	 */
	public synchronized void close(int qNumber, User[] userList) {
		this.getQuestion(qNumber).closeQuestion();
		if (userList != null) {
			Arrays.stream(userList).parallel().forEach(u -> u.endEffort(qNumber));
		}
		this.version++;
	}

	/**
	 * Get whether each question in this round has been open.
	 *
	 * @return Array indicating whether each question has been open
	 */
	public Boolean[] eachBeenOpen() {
		return IntStream.rangeClosed(1, this.nQuestions).parallel().mapToObj(q -> this.getQuestion(q).beenOpen())
				.toArray(Boolean[]::new);
	}

	/**
	 * Get whether each question in this round was answered correctly.
	 *
	 * @return Array indicating whether each question is correct
	 */
	public Boolean[] eachCorrect() {
		return IntStream.rangeClosed(1, this.nQuestions).parallel().mapToObj(q -> this.getQuestion(q).isCorrect())
				.toArray(Boolean[]::new);
	}

	/**
	 * Get whether each question in this round is open.
	 *
	 * @return Array indicating whether each question is open
	 */
	public Boolean[] eachOpen() {
		return IntStream.rangeClosed(1, this.nQuestions).parallel().mapToObj(q -> this.getQuestion(q).isOpen())
				.toArray(Boolean[]::new);
	}

	/**
	 * Edit question data.
	 *
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
	public void editQuestion(int qNumber, int value, String qText, String aText, boolean isCorrect, String submitter) {
		this.editQuestion(qNumber, value, qText);
		if (isCorrect) {
			this.markCorrect(qNumber, aText, submitter);
		} else {
			this.markQuestionIncorrect(qNumber);
		}
		if (aText != null) {
			this.setAnswerText(qNumber, aText);
		}
	}

	public void editQuestion(int qNumber, int value, String qText) {
		this.setValue(qNumber, value);
		this.setQuestionText(qNumber, qText);
	}

	/**
	 * Gets whether the score for this round has been announced
	 *
	 * @return Whether the score for this round has been announced
	 */
	public int getAnnouncedPoints() {
		return this.announcedPoints;
	}

	public Answer[] getAnswerQueue() {
		final Answer[] queue = new Answer[this.answerQueue.size()];
		this.answerQueue.toArray(queue);
		return queue;
	}

	public Answer getAnswer(int queueIndex) {
		return this.answerQueue.get(queueIndex);
	}

	/**
	 * Gets the size of the answer queue.
	 *
	 * @return The answer queue size
	 */
	public int getAnswerQueueSize() {
		return this.answerQueue.size();
	}

	public void changeUserName(String oldName, String newName) {
		this.questions.stream().parallel().forEach(q -> q.changeUserName(oldName, newName));
		this.answerQueue.parallelStream().forEach(a -> a.changeUserName(oldName, newName));
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
		return this.questions.stream().map(q -> q.getAnswerText()).toArray(String[]::new);
	}

	/**
	 * Gets the points earned for each question in this round.
	 *
	 * @return Array of points earned
	 */
	public int[] getEachEarned() {
		return this.questions.stream().mapToInt(a -> a.getEarned()).toArray();
	}

	/**
	 * Gets the text for each question in this round.
	 *
	 * @return Array of question text
	 */
	public String[] getEachQuestionText() {
		return this.questions.stream().map(q -> q.getQuestionText()).toArray(String[]::new);
	}

	/**
	 * Gets the name of the submitter for each correct answer in this round.
	 *
	 * @return Array of submitter names
	 */
	public String[] getEachSubmitter() {
		return this.questions.stream().map(q -> q.getSubmitter()).toArray(String[]::new);
	}

	/**
	 * Gets the value of each question in this round.
	 *
	 * @return Array of question values
	 */
	public int[] getEachValue() {
		return this.questions.stream().mapToInt(q -> q.getQuestionValue()).toArray();
	}

	/**
	 * Gets the total points earned for questions in this round.
	 *
	 * @return The total points earned
	 */
	public int getEarned() {
		return this.questions.stream().parallel().mapToInt(q -> q.getEarned()).sum();
	}


	/**
	 * Gets the number of questions in this round.
	 *
	 * @return The number of questions in this round
	 */
	public int getNQuestions() {
		return this.nQuestions;
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
		return this.questions.get(qNumber - 1);
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
	 * Gets the total value of questions in this round.
	 *
	 * @return The total value of this round
	 */
	public int getValue() {
		return this.questions.stream().mapToInt(q -> q.getQuestionValue()).sum();
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
		return this.getQuestion(qNumber).isCorrect();
	}

	/**
	 * Checks if there is a mismatch between the announced score and the calculated score
	 *
	 * @return true, if there is a mismatch
	 */
	public boolean isMismatch() {
		if (this.announcedPoints == -1) {
			return false;
		}
		return ( this.announcedPoints != this.getValue() );
	}

	// /**
	// * Checks if a question is currently open
	// *
	// * @param qNumber
	// * The question number
	// * @return true, if the question is open
	// */
	// public boolean isOpen(int qNumber) {
	// return this.getQuestion(qNumber).isOpen();
	// }

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
	 * @param userList
	 * @param operator
	 *            The operator who accepted the correct answer
	 *
	 */
	public synchronized void markCorrect(int queueIndex, String caller, User[] userList) {
		final Answer answer = this.answerQueue.get(queueIndex);
		answer.markAnswerCorrect(caller);
		final int qNumber = answer.getQNumber();
		final String answerText = answer.getAnswerText();
		final String submitter = answer.getSubmitter();
		this.getQuestion(qNumber).markQuestionCorrect(answerText, submitter);
		if (userList != null) {
			Arrays.stream(userList).parallel().forEach(u -> u.endEffort(qNumber));
		}
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
	public synchronized void markCorrect(int qNumber, String answerText, String submitter) {
		this.getQuestion(qNumber).markQuestionCorrect(answerText, submitter);
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
		final String aText = answer.getAnswerText().replaceAll("\\s+", "");
		final int qNumber = answer.getQNumber();
		this.answerQueue.parallelStream()
				.filter(a -> a.getQueueLocation() != queueIndex + 1 && a.getQNumber() == qNumber
						&& a.getAnswerText().replaceAll("\\s+", "").equalsIgnoreCase(aText)
						&& a.getStatus() != Status.DUPLICATE)
				.forEach(a -> a.changeAgreement(a.getSubmitter(), Agreement.AGREE));
		this.version++;
	}

	/**
	 * Mark a question as incorrect.
	 *
	 * @param qNumber
	 *            The question number
	 */
	public synchronized void markQuestionIncorrect(int qNumber) {
		this.getQuestion(qNumber).markQuestionIncorrect();
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
	public synchronized void markAnswerIncorrect(int queueIndex) {
		final Answer answer = this.answerQueue.get(queueIndex);
		final Answer.Status oldStatus = answer.getStatus();
		final int qNumber = answer.getQNumber();
		if (oldStatus == Answer.Status.CORRECT) {
			this.getQuestion(qNumber).openQuestion("");
		}
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
	public synchronized void markAnswerIncorrect(int queueIndex, String caller) {
		final Answer answer = this.answerQueue.get(queueIndex);
		final Answer.Status oldStatus = answer.getStatus();
		answer.markIncorrect(caller);
		final int qNumber = answer.getQNumber();
		if (oldStatus == Answer.Status.CORRECT) {
			this.getQuestion(qNumber).openQuestion("");
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
			this.getQuestion(qNumber).openQuestion("");
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
			this.getQuestion(qNumber).openQuestion("");
		}
		this.version++;
	}

	public synchronized void changeAgreement(String user, int queueIndex, Answer.Agreement agreement) {
		this.answerQueue.get(queueIndex).changeAgreement(user, agreement);
		this.version++;
	}

	public Agreement getAgreement(String user, int queueIndex) {
		return this.answerQueue.get(queueIndex).getAgreement(user);
	}

	public Question[] getQuestions() {
		return this.questions.parallelStream().collect(Collectors.toList()).toArray(new Question[this.nQuestions]);
	}

	/**
	 * Get the number of correct answers in this round
	 *
	 * @return The number of correct answers
	 */
	public int nCorrect() {
		return (int) this.questions.stream().parallel().filter(q -> q.isCorrect()).count();
	}

	/**
	 * Get the lowest unopened question number. If all questions have been opened, returns the last question number.
	 *
	 * @return The question number that should be opened next
	 */
	public int nextToOpen() {
		return this.questions.stream().parallel().filter(q -> !q.beenOpen()).mapToInt(q -> q.getQuestionNumber()).min()
				.orElse(this.getNQuestions());
	}

	/**
	 * Get the highest question number that has been opened.
	 *
	 * @return Highest question that has been opened
	 */
	public int maxBeenOpen() {
		return this.questions.stream().parallel().filter(q -> q.beenOpen()).mapToInt(q -> q.getQuestionNumber()).max()
				.orElse(0);
	}

	/**
	 * Get the number of open questions in this round
	 *
	 * @return The number of open questions
	 */
	public int nOpen() {
		return (int) this.questions.stream().filter(q -> q.isOpen()).count();
	}

	public int nUnopened() {
		return (int) this.questions.stream().filter(q -> !q.beenOpen()).count();
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
		this.getQuestion(qNumber).openQuestion(user);
		this.version++;
	}

	public synchronized void reopen(int qNumber) {
		this.getQuestion(qNumber).openQuestion("");
		this.answerQueue.parallelStream()
				.filter(a -> a.getQNumber() == qNumber && a.getStatus() == Answer.Status.CORRECT)
				.forEach(a -> a.markUncalled());
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
		boolean isDuplicate = this.answerQueue.parallelStream()
				.anyMatch(a -> a.getQNumber() == qNumber
						&& a.getAnswerText().replaceAll("\\s+", "").equalsIgnoreCase(answer.replaceAll("\\s+", ""))
						&& a.getStatus() != Status.DUPLICATE);
		this.answerQueue.add(new Answer(queueIndex + 1, qNumber, answer, submitter, confidence));
		if (isDuplicate) {
			this.markDuplicate(queueIndex);
		}
		this.version++;
	}

	public synchronized void remapQuestion(int oldQNumber, int newQNumber) {
		this.getQuestion(newQNumber).copy(this.getQuestion(oldQNumber));
		this.getQuestion(oldQNumber).resetQuestion();
		this.answerQueue.parallelStream().filter(a -> a.getQNumber() == oldQNumber)
				.forEach(a -> a.setQNumber(newQNumber));
		this.version++;
	}

	/**
	 * Reset a question.
	 *
	 * @param qNumber
	 *            The question number
	 */
	public synchronized void resetQuestion(int qNumber) {
		this.getQuestion(qNumber).resetQuestion();
		this.answerQueue.parallelStream().filter(a -> a.getQNumber() == qNumber)
				.forEach(a -> this.answerQueue.remove(a));
		IntStream.range(1, this.answerQueue.size()).forEach(a -> this.answerQueue.get(a - 1).setQueueLocation(a));
		this.version++;
	}

	/**
	 * Returns if the round is over (all questions have been opened and are now closed).
	 *
	 * @return true, if the round is over
	 */
	public boolean roundOver() {
		return this.questions.stream().parallel().filter(q -> q.beenOpen() && !q.isOpen()).count() == this
				.getNQuestions();
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
		this.getQuestion(qNumber).setAnswerText(answer);
		this.version++;
	}

	/**
	 * Sets the discrepancy text for this round.
	 *
	 * @param discrepancyText
	 *            The new discrepancy text
	 */
	public synchronized void setDiscrepencyText(String discrepancyText) {
		this.discrepancyText = discrepancyText;
		this.version++;
	}

	public synchronized void setNQuestions(int nQuestions) {
		while (nQuestions > this.questions.size()) {
			this.questions.add(new Question(this.questions.size() + 1));
		}
		while (nQuestions < this.questions.size()) {
			this.questions.remove(this.getQuestion(this.questions.size()));
		}
		this.nQuestions = nQuestions;
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
	public synchronized void setOperator(int queueIndex, String operator) {
		final Answer answer = this.answerQueue.get(queueIndex);
		answer.setOperator(operator);
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
		this.getQuestion(qNumber).setQuestionText(question);
		this.version++;
	}

	/**
	 * Make this a speed round
	 *
	 * @param isSpeed
	 */
	public synchronized void setSpeed(boolean isSpeed) {
		this.speed = isSpeed;
		this.setNQuestions(isSpeed ? this.nQuestionsSpeed : this.nQuestionsNormal);
	}

	/**
	 * Sets the announced standings for this round.
	 *
	 * @param standings
	 *            Array of ScoreEntry representing each team's score this round
	 */
	public synchronized void setStandings(ScoreEntry[] standings, String teamName) {
		this.announced = true;
		this.standings = standings;
		for (final ScoreEntry entry : standings) {
			if (entry.getTeamName().equalsIgnoreCase(teamName)) {
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
		this.getQuestion(qNumber).setSubmitter(submitter);
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
		this.getQuestion(qNumber).setValue(value);
		this.version++;
	}

	/**
	 * Make this a normal round
	 */
	public synchronized void unsetSpeed() {
		this.speed = false;
		this.version++;
	}

	public String toString() {
		String s = "=== Data for round " + this.rNumber + " ===\n";
		// s = s + "Team name: " + this.teamName + "\n";
		s = s + "Version: " + this.version + " nQuestions: " + this.nQuestionsNormal + " nQuestions(speed): "
				+ this.nQuestionsSpeed + "\n";
		s = s + "Speed Round: " + this.speed + "\n";
		s = s + "Announced: " + this.announced + " Announced Place: " + this.place + " Announced Score: "
				+ this.announcedPoints + "\n";
		s = s + "Discrepency text: " + this.discrepancyText + "\n";
		if (!( this.standings == null )) {
			s = s + "== Standings for round " + this.rNumber + " ==\n";
			for (ScoreEntry e : this.standings) {
				s = s + e.toString() + "\n";
			}
		}
		s = s + "== Questions for round " + this.rNumber + " ==\n";
		for (Question q : this.questions) {
			s = s + q.toString() + "\n";
		}
		s = s + "== Answer queue for round " + this.rNumber + " ==\n";
		for (Answer a : this.answerQueue) {
			s = s + a.toString() + "\n";
		}
		return s;
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
	public synchronized void setAnswer(int qNumber, String answer, String submitter, int confidence, String status,
			String caller, String operator, String timestamp) {
		final Answer newAnswer =
				new Answer(this.answerQueue.size() + 1, qNumber, answer, submitter, confidence, timestamp);
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
				newAnswer.markAnswerCorrect(caller);
				newAnswer.setOperator(operator);
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
	public Question[] getOpenQuestions() {
		this.questions.stream().parallel()
				.forEach(q -> System.out.println("Q" + q.getQuestionNumber() + ": " + q.isOpen()));
		return this.questions.stream().parallel().filter(q -> q.isOpen()).toArray(Question[]::new);
	}

}
