package net.bubbaland.trivia;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A data structure for proposed answers.
 * 
 * The <code>Answer</code> class contains the data and status for a proposed answer. Answers are originally created with the status of NOT_CALLED_IN, and this status is then updated as someone calls the answer in and then receives a response on the correctness of the answer.
 * 
 * @author Walter Kolczynski
 */
public class Answer implements Serializable {

	// Valid statuses
	protected static enum Status {
		NOT_CALLED_IN, CALLING, INCORRECT, PARTIAL, CORRECT
	}

	private static final long	serialVersionUID	= -2367986992067473980L;

	// The question number
	final private int			qNumber;

	// The answer text
	final private String		answer;

	// The confidence in the answer
	final private int			confidence;

	// The timestamp of when the answer was submitted
	final private String		timestamp;

	// The user name of the answer submitter
	final private String		submitter;

	// The user name of the person who is calling in the answer
	private volatile String		caller;

	// The operator who accepted a correct answer
	private volatile String		operator;

	// The current status of the question
	private volatile Status		status;											;

	/**
	 * Create a new answer
	 * 
	 * @param qNumber
	 *            The question number
	 * @param answer
	 *            The proposed answer text
	 * @param submitter
	 *            The user submitting the answer
	 */
	public Answer(int qNumber, String answer, String submitter) {
		this(qNumber, answer, submitter, -1);
	}

	/**
	 * Instantiates a new answer.
	 * 
	 * @param qNumber
	 *            The question number
	 * @param answer
	 *            The proposed answer text
	 * @param submitter
	 *            The user submitting the answer
	 * @param confidence
	 *            Confidence in the proposed answer
	 */
	public Answer(int qNumber, String answer, String submitter, int confidence) {
		this.qNumber = qNumber;
		this.answer = answer;
		this.submitter = submitter;
		this.confidence = confidence;
		final Date time = new Date();
		final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		this.timestamp = timeFormat.format(time);
		this.caller = "";
		this.operator = "";
		this.status = Status.NOT_CALLED_IN;
	}

	/**
	 * Mark this answer as being called in
	 * 
	 * @param caller
	 *            The caller's user name
	 */
	public void callIn(String caller) {
		this.caller = caller;
		this.operator = "";
		this.status = Status.CALLING;
	}

	/**
	 * Gets the answer text
	 * 
	 * @return The answer text
	 */
	public String getAnswer() {
		return this.answer;
	}

	/**
	 * Gets the last person who updated the status
	 * 
	 * @return The caller's user name
	 */
	public String getCaller() {
		return this.caller;
	}

	/**
	 * Gets the confidence in the answer
	 * 
	 * @return The confidence
	 */
	public int getConfidence() {
		return this.confidence;
	}

	/**
	 * Gets the operator name
	 * 
	 * @return The operator name
	 */
	public String getOperator() {
		return this.operator;
	}

	/**
	 * Gets the question number of this answer
	 * 
	 * @return The question number
	 */
	public int getQNumber() {
		return this.qNumber;
	}

	/**
	 * Gets a string representation of the status of this answer
	 * 
	 * @return The current status
	 */
	public String getStatusString() {
		switch (this.status) {
			case NOT_CALLED_IN:
				return "Not Called In";
			case CALLING:
				return "Calling";
			case INCORRECT:
				return "Incorrect";
			case PARTIAL:
				return "Partial";
			case CORRECT:
				return "Correct";
			default:
				return "Unknown";
		}
	}

	/**
	 * Gets the submitter's user name
	 * 
	 * @return The submitter's user name
	 */
	public String getSubmitter() {
		return this.submitter;
	}

	/**
	 * Gets the timestamp
	 * 
	 * @return The timestamp
	 */
	public String getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Mark this answer as correct
	 * 
	 * @param caller
	 *            The caller's user name
	 * @param operator
	 *            The operator who awarded the question
	 */
	public void markCorrect(String caller, String operator) {
		this.caller = caller;
		this.operator = operator;
		this.status = Status.CORRECT;
	}

	/**
	 * Mark this answer as incorrect
	 * 
	 * @param caller
	 *            The caller's user name
	 */
	public void markIncorrect(String caller) {
		this.caller = caller;
		this.operator = "";
		this.status = Status.INCORRECT;
	}

	/**
	 * Mark this answer as partially correct
	 * 
	 * @param caller
	 *            The caller's user name
	 */
	public void markPartial(String caller) {
		this.caller = caller;
		this.operator = "";
		this.status = Status.PARTIAL;
	}

	/**
	 * Reset this answer to uncalled
	 */
	public void markUncalled() {
		this.caller = "";
		this.operator = "";
		this.status = Status.NOT_CALLED_IN;
	}

}
