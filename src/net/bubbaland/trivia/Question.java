package net.bubbaland.trivia;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

	// The question number
	@JsonProperty("qNumber")
	private int					qNumber;

	// The value of the question
	@JsonProperty("value")
	private volatile int		value;

	// The text of the question
	@JsonProperty("question")
	private volatile String		question;

	// The text of the correct answer
	@JsonProperty("answer")
	private volatile String		answer;

	// The operator who accepted the correct answer
	@JsonProperty("operator")
	private volatile String		operator;

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

	/**
	 * Creates a new question.
	 * 
	 * @param round
	 *            The round to which this question belongs
	 * @param qNumber
	 *            The question number
	 */
	public Question(int qNumber) {
		this.qNumber = qNumber;
		this.value = 0;
		this.question = "";
		this.answer = "";
		this.operator = "";
		this.submitter = "";
		this.isOpen = false;
		this.beenOpen = false;
		this.correct = false;
	}

	@JsonCreator
	private Question(@JsonProperty("qNumber") int qNumber, @JsonProperty("value") int value,
			@JsonProperty("question") String question, @JsonProperty("answer") String answer,
			@JsonProperty("operator") String operator, @JsonProperty("submitter") String submitter,
			@JsonProperty("isOpen") boolean open, @JsonProperty("beenOpen") boolean beenOpen,
			@JsonProperty("correct") boolean correct) {
		this.qNumber = qNumber;
		this.value = value;
		this.question = question;
		this.answer = answer;
		this.operator = operator;
		this.submitter = submitter;
		this.isOpen = open;
		this.beenOpen = beenOpen;
		this.correct = correct;
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
	public void close(String answer) {
		this.answer = answer;
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
	public int getNumber() {
		return this.qNumber;
	}

	/**
	 * Gets the operator who took the correct answer for this question (if any)
	 * 
	 * @return The operator name
	 */
	public String getOperator() {
		return this.operator;
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
	public int getValue() {
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
	public void markCorrect(String answer, String submitter, String operator) {
		this.correct = true;
		this.isOpen = false;
		this.answer = answer;
		this.operator = operator;
		this.submitter = submitter;
	}

	/**
	 * Mark this question as incorrect
	 */
	public void markIncorrect() {
		this.correct = false;
		this.isOpen = true;
		this.answer = "";
		this.operator = "";
		this.submitter = "";
	}

	/**
	 * Open this question
	 */
	public void open() {
		this.isOpen = true;
		this.beenOpen = true;
	}

	public void reopen() {
		this.open();
		this.correct = false;
	}

	/**
	 * Reset the question
	 */
	public void reset() {
		this.value = 0;
		this.question = "";
		this.answer = "";
		this.operator = "";
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
	public void setAnswerText(String answer) {
		this.answer = answer;
	}

	/**
	 * Sets the operator for this question
	 * 
	 * @param operator
	 *            The new operator
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * Sets the text of this question
	 * 
	 * @param question
	 *            The new question text
	 */
	public void setQuestionText(String question) {
		this.question = question;
	}

	/**
	 * Sets the submitter for this question
	 * 
	 * @param submitter
	 *            The new submitter
	 */
	public void setSubmitter(String submitter) {
		this.submitter = submitter;
	}

	/**
	 * Sets the value of this question
	 * 
	 * @param value
	 *            The new value for this question
	 */
	public void setValue(int value) {
		this.value = value;
	}

	protected void copy(Question question) {
		this.value = question.value;
		this.question = question.question;
		this.answer = question.answer;
		this.operator = question.operator;
		this.submitter = question.submitter;
		this.isOpen = question.isOpen;
		this.beenOpen = question.beenOpen;
		this.correct = question.correct;
	}

}
