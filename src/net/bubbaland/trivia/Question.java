package net.bubbaland.trivia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * A data structure for questions.
 *
 * The <code>Question</code> class contains all of the data for a particular question.
 *
 * @author Walter Kolczynski
 *
 */
public class Question implements Serializable {

	private static final long	serialVersionUID	= 5230169250657519692L;

	public static final Pattern	VISUAL_PATTERN		= Pattern.compile("([Vv]isual )([Tt]rivia )?(#)?([0-9]+)");

	// The question number
	@JsonProperty("qNumber")
	private final int			qNumber;

	// The value of the question
	@JsonProperty("value")
	private volatile int		value;

	// The text of the question
	@JsonProperty("question")
	private volatile String		question;

	// The text of the correct answer
	@JsonProperty("answer")
	private volatile String		answer;

	// The user who submitted the correct answer
	@JsonProperty("submitter")
	private volatile String		submitter;

	// Whether the question is currently open
	@JsonProperty("isOpen")
	private volatile boolean	isOpen;

	// Whether the question has ever been open
	@JsonProperty("beenOpen")
	private volatile boolean	beenOpen;

	// Whether the question was answered correctly
	@JsonProperty("correct")
	private volatile boolean	correct;

	@JsonProperty("visualTrivia")
	private volatile int		visualTrivia;

	/**
	 * Creates a new question.
	 *
	 * @param round
	 *            The round to which this question belongs
	 * @param qNumber
	 *            The question number
	 */
	public Question(int qNumber) {
		this(qNumber, 0, "", "", "", "", false, false, false, 0);
	}

	@JsonCreator
	private Question(@JsonProperty("qNumber") int qNumber, @JsonProperty("value") int value,
			@JsonProperty("question") String question, @JsonProperty("answer") String answer,
			@JsonProperty("operator") String operator, @JsonProperty("submitter") String submitter,
			@JsonProperty("isOpen") boolean open, @JsonProperty("beenOpen") boolean beenOpen,
			@JsonProperty("correct") boolean correct, @JsonProperty("visualTrivia") int visualTrivia) {
		this.qNumber = qNumber;
		this.value = value;
		this.question = question;
		this.answer = answer;
		this.submitter = submitter;
		this.isOpen = open;
		this.beenOpen = beenOpen;
		this.correct = correct;
		this.visualTrivia = visualTrivia;
	}

	/**
	 * Checks if the question has ever been open
	 *
	 * @return True if the question has been open
	 */
	public boolean beenOpen() {
		return this.beenOpen;
	}

	/**
	 * Close this question
	 *
	 * @param answer
	 *            TODO
	 */
	protected void closeQuestion() {
		this.isOpen = false;
	}

	/**
	 * Gets the text of the answer for this question
	 *
	 * @return The answer to this question
	 */
	public String getAnswerText() {
		return this.answer;
	}

	/**
	 * Gets the number of points earned on this question
	 *
	 * @return The number of points earned for this question
	 */
	public int getEarned() {
		if (this.correct)
			return this.value;
		else
			return 0;
	}

	/**
	 * Gets the question number
	 *
	 * @return The question number
	 */
	public int getQuestionNumber() {
		return this.qNumber;
	}

	/**
	 * Gets the text of this question
	 *
	 * @return The question's text
	 */
	public String getQuestionText() {
		return this.question;
	}

	/**
	 * Gets the user who submitted the correct answer for this questino (if any)
	 *
	 * @return The submitter's name
	 */
	public String getSubmitter() {
		return this.submitter;
	}

	/**
	 * Gets the value of this question
	 *
	 * @return The question's value
	 */
	public int getQuestionValue() {
		return this.value;
	}

	/**
	 * Checks if the question is correct
	 *
	 * @return True if the question was answered correct
	 */
	public boolean isCorrect() {
		return this.correct;
	}

	/**
	 * Checks if the question is open
	 *
	 * @return True if the question is currently open
	 */
	public boolean isOpen() {
		return this.isOpen;
	}

	/**
	 * Mark this question as correct
	 *
	 * @param answer
	 *            The correct answer
	 * @param submitter
	 *            The user who submitted the correct answer
	 * @param operator
	 *            The operator who accepted the correct answer
	 */
	protected void markQuestionCorrect(String answer, String submitter) {
		this.correct = true;
		this.isOpen = false;
		this.answer = answer;
		this.submitter = submitter;
	}

	/**
	 * Mark this question as incorrect
	 */
	protected void markQuestionIncorrect() {
		this.correct = false;
		this.answer = "";
		this.submitter = "";
	}

	/**
	 * Open this question
	 */
	protected void openQuestion(String user) {
		if (this.beenOpen == false) {
			this.question = "Question " + this.qNumber + " is being typed in by " + user + "...";
		}
		this.isOpen = true;
		this.beenOpen = true;
		this.correct = false;
		this.submitter = "";
	}

	/**
	 * Reset the question
	 */
	protected void resetQuestion() {
		this.value = 0;
		this.question = "";
		this.answer = "";
		this.submitter = "";
		this.isOpen = false;
		this.beenOpen = false;
		this.correct = false;
	}

	/**
	 * Sets the correct answer for this question
	 *
	 * @param answer
	 *            The new answer text
	 */
	protected void setAnswerText(String answer) {
		this.answer = answer;
	}

	/**
	 * Sets the text of this question
	 *
	 * @param question
	 *            The new question text
	 */
	protected void setQuestionText(String question) {
		this.question = question;
		Matcher matcher = VISUAL_PATTERN.matcher(this.question);
		this.visualTrivia = matcher.find() ? Integer.parseInt(matcher.group(4)) : 0;
		if (this.visualTrivia != 0) {
			System.out.println("Visual trivia " + this.visualTrivia + " detected");
		}
	}

	/**
	 * Sets the submitter for this question
	 *
	 * @param submitter
	 *            The new submitter
	 */
	protected void setSubmitter(String submitter) {
		this.submitter = submitter;
	}

	/**
	 * Sets the value of this question
	 *
	 * @param value
	 *            The new value for this question
	 */
	protected void setValue(int value) {
		this.value = value;
	}

	protected void copy(Question question) {
		this.value = question.value;
		this.question = question.question;
		this.answer = question.answer;
		this.submitter = question.submitter;
		this.isOpen = question.isOpen;
		this.beenOpen = question.beenOpen;
		this.correct = question.correct;
	}

	protected void changeUserName(String oldName, String newName) {
		if (this.submitter.equals(oldName)) {
			this.submitter = newName;
		}
	}

	public User[] getEffort(User[] userList) {
		ArrayList<User> userEffortList = new ArrayList<User>();
		for (User user : userList) {
			if (user.getEffort() == this.qNumber) {
				userEffortList.add(user);
			}
		}
		if (userEffortList.size() == 0) {
			return null;
		}
		User[] userEffort = new User[userEffortList.size()];
		userEffortList.toArray(userEffort);
		return userEffort;
	}

	public int getVisualTrivia() {
		return this.visualTrivia;
	}

	public String toString() {
		return ( "Q#" + this.qNumber + " worth " + this.value + "\n" + "Open: " + this.isOpen + " Been Open: "
				+ this.beenOpen + " Correct: " + this.correct + "(" + this.submitter + ")" + "\n" + "Q: "
				+ this.question + "\n" + "A: " + this.answer + "\n" + "Visual Trivia: " + this.visualTrivia + "\n" );
	}

	public boolean equals(Question question) {
		return EqualsBuilder.reflectionEquals(this, question);
	}

}
