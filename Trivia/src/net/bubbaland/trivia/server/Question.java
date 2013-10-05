package net.bubbaland.trivia.server;

// TODO: Auto-generated Javadoc
/**
 * The Class Question.
 */
public class Question {

	/** The round. */
	private Round	round;
	
	/** The q number. */
	private int		qNumber;
	
	/** The value. */
	private int		value;
	
	/** The submitter. */
	private volatile String	question, answer, operator, submitter;
	
	/** The correct. */
	private volatile boolean	open, beenOpen, correct;

	/**
	 * Instantiates a new question.
	 *
	 * @param round the round
	 * @param qNumber the q number
	 */
	public Question( Round round, int qNumber ) {
		this.round = round;
		this.qNumber = qNumber;
		this.value = 0;
		this.question = null;
		this.answer = null;
		this.operator = null;
		this.submitter = null;
		this.open = false;
		this.beenOpen = false;
		this.correct = false;
	}

	/**
	 * Reset.
	 */
	public void reset() {
		this.value = 0;
		this.question = null;
		this.answer = null;
		this.operator = null;
		this.submitter = null;
		this.open = false;
		this.beenOpen = false;
		this.correct = false;
	}

	/**
	 * Checks if is open.
	 *
	 * @return true, if is open
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * Checks if is correct.
	 *
	 * @return true, if is correct
	 */
	public boolean isCorrect() {
		return correct;
	}

	/**
	 * Been open.
	 *
	 * @return true, if successful
	 */
	public boolean beenOpen() {
		return beenOpen;
	}

	/**
	 * Gets the round number.
	 *
	 * @return the round number
	 */
	public int getRoundNumber() {
		return round.getRoundNumber();
	}

	/**
	 * Gets the number.
	 *
	 * @return the number
	 */
	public int getNumber() {
		return qNumber;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Gets the earned.
	 *
	 * @return the earned
	 */
	public int getEarned() {
		if ( correct ) {
			return value;
		} else {
			return 0;
		}
	}

	/**
	 * Gets the question text.
	 *
	 * @return the question text
	 */
	public String getQuestionText() {
		return question;
	}

	/**
	 * Gets the answer text.
	 *
	 * @return the answer text
	 */
	public String getAnswerText() {
		return answer;
	}

	/**
	 * Gets the operator.
	 *
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * Gets the submitter.
	 *
	 * @return the submitter
	 */
	public String getSubmitter() {
		return submitter;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(int value) {
		this.value = value;
	}

	/**
	 * Sets the question text.
	 *
	 * @param question the new question text
	 */
	public void setQuestionText(String question) {
		this.question = question;
	}

	/**
	 * Sets the answer text.
	 *
	 * @param answer the new answer text
	 */
	public void setAnswerText(String answer) {
		this.answer = answer;
	}

	/**
	 * Sets the operator.
	 *
	 * @param operator the new operator
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * Sets the submitter.
	 *
	 * @param submitter the new submitter
	 */
	public void setSubmitter(String submitter) {
		this.submitter = submitter;
	}

	/**
	 * Mark correct.
	 *
	 * @param answer the answer
	 * @param submitter the submitter
	 * @param operator the operator
	 */
	public void markCorrect(String answer, String submitter, String operator) {
		this.correct = true;
		this.open = false;
		this.answer = answer;
		this.operator = operator;
		this.submitter = submitter;
	}

	/**
	 * Mark incorrect.
	 */
	public void markIncorrect() {
		this.correct = false;
	}

	/**
	 * Open.
	 */
	public void open() {
		this.open = true;
		this.beenOpen = true;
	}

	/**
	 * Close.
	 */
	public void close() {
		this.open = false;
	}

}
