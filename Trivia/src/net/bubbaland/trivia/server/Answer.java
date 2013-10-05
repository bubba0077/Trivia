package net.bubbaland.trivia.server;

import java.text.SimpleDateFormat;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The Class Answer.
 */
public class Answer {

	/** The q number. */
	private int				qNumber;
	
	/** The answer. */
	private String			answer;
	
	/** The confidence. */
	private volatile int	confidence;
	
	/** The operator. */
	private volatile String	timestamp, submitter, caller, operator;
	
	/** The status. */
	private volatile Status	status;

	/**
	 * The Enum Status.
	 */
	protected static enum Status {
		
		/** The not called in. */
		NOT_CALLED_IN, 
 /** The calling. */
 CALLING, 
 /** The incorrect. */
 INCORRECT, 
 /** The partial. */
 PARTIAL, 
 /** The correct. */
 CORRECT
	};

	/**
	 * Instantiates a new answer.
	 *
	 * @param qNumber the q number
	 * @param answer the answer
	 * @param submitter the submitter
	 */
	public Answer( int qNumber, String answer, String submitter ) {
		this( qNumber, answer, submitter, -1 );
	}

	/**
	 * Instantiates a new answer.
	 *
	 * @param qNumber the q number
	 * @param answer the answer
	 * @param submitter the submitter
	 * @param confidence the confidence
	 */
	public Answer( int qNumber, String answer, String submitter, int confidence ) {
		this.qNumber = qNumber;
		this.answer = answer;
		this.submitter = submitter;
		this.confidence = confidence;
		Date time = new Date();
		SimpleDateFormat timeFormat = new SimpleDateFormat( "HH:mm:ss" );
		this.timestamp = timeFormat.format( time );
		this.caller = "";
		this.operator = "";
		this.status = Status.NOT_CALLED_IN;
	}

	/**
	 * Gets the q number.
	 *
	 * @return the q number
	 */
	public int getQNumber() {
		return qNumber;
	}

	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * Gets the answer.
	 *
	 * @return the answer
	 */
	public String getAnswer() {
		return answer;
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
	 * Gets the caller.
	 *
	 * @return the caller
	 */
	public String getCaller() {
		return caller;
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
	 * Gets the confidence.
	 *
	 * @return the confidence
	 */
	public int getConfidence() {
		return confidence;
	}

	/**
	 * Gets the status string.
	 *
	 * @return the status string
	 */
	public String getStatusString() {
		switch ( status ) {
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
	 * Call in.
	 *
	 * @param caller the caller
	 */
	public void callIn(String caller) {
		this.caller = caller;
		this.operator = "";
		this.status = Status.CALLING;
	}

	/**
	 * Mark incorrect.
	 *
	 * @param caller the caller
	 */
	public void markIncorrect(String caller) {
		this.caller = caller;
		this.operator = "";
		this.status = Status.INCORRECT;
	}

	/**
	 * Mark partial.
	 *
	 * @param caller the caller
	 */
	public void markPartial(String caller) {
		this.caller = caller;
		this.operator = "";
		this.status = Status.PARTIAL;
	}

	/**
	 * Mark correct.
	 *
	 * @param caller the caller
	 * @param operator the operator
	 */
	public void markCorrect(String caller, String operator) {
		this.caller = caller;
		this.operator = operator;
		this.status = Status.CORRECT;
	}

	/**
	 * Mark uncalled.
	 */
	public void markUncalled() {
		this.caller = "";
		this.operator = "";
		this.status = Status.NOT_CALLED_IN;
	}

}
