package net.bubbaland.trivia;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A data structure for proposed answers.
 *
 * The <code>Answer</code> class contains the data and status for a proposed answer. Answers are originally created with
 * the status of NOT_CALLED_IN, and this status is then updated as someone calls the answer in and then receives a
 * response on the correctness of the answer.
 *
 * @author Walter Kolczynski
 */
public class Answer implements Serializable {

	private static final long serialVersionUID = -2367986992067473980L;

	public enum Agreement {
		DISAGREE, NEUTRAL, AGREE
	};

	// Place in the queue
	@JsonProperty("queueLocation")
	volatile private int							queueLocation;

	// The question number
	@JsonProperty("qNumber")
	volatile private int							qNumber;

	// The answer text
	@JsonProperty("answer")
	final private String							answer;

	// The confidence in the answer
	@JsonProperty("confidence")
	final private int								confidence;

	// A counter for the number of +1's
	@JsonProperty("agreement")
	private volatile Hashtable<String, Agreement>	agreement;

	// The timestamp of when the answer was submitted
	@JsonProperty("timestamp")
	final private String							timestamp;

	// The user name of the answer submitter
	@JsonProperty("submitter")
	private String									submitter;

	// The user name of the person who is calling in the answer
	@JsonProperty("caller")
	private volatile String							caller;

	// The operator who accepted a correct answer
	@JsonProperty("operator")
	private volatile String							operator;

	// The current status of the question
	@JsonProperty("status")
	private volatile Status							status;

	final private static SimpleDateFormat			timeFormat	= new SimpleDateFormat("HH:mm:ss");

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
	// public Answer(int queueLocation, int qNumber, String answer, String submitter) {
	// this(queueLocation, qNumber, answer, submitter, -1);
	// }

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
	public Answer(int queueLocation, int qNumber, String answer, String submitter, int confidence) {
		this(queueLocation, qNumber, answer, submitter, confidence, timeFormat.format(new Date()));
	};

	protected Answer(int queueLocation, int qNumber, String answer, String submitter, int confidence,
			String timestamp) {
		this(queueLocation, qNumber, answer, confidence, new Hashtable<String, Agreement>(), timestamp, submitter, "",
				"", Status.NOT_CALLED_IN);
	}

	@JsonCreator
	private Answer(@JsonProperty("queueLocation") int queueLocation, @JsonProperty("qNumber") int qNumber,
			@JsonProperty("answer") String answer, @JsonProperty("confidence") int confidence,
			@JsonProperty("agreement") Hashtable<String, Agreement> agreement,
			@JsonProperty("timestamp") String timestamp, @JsonProperty("submitter") String submitter,
			@JsonProperty("caller") String caller, @JsonProperty("operator") String operator,
			@JsonProperty("status") Status status) {
		this.queueLocation = queueLocation;
		this.qNumber = qNumber;
		this.answer = answer;
		this.confidence = confidence;
		this.agreement = agreement;
		this.timestamp = timestamp;
		this.submitter = submitter;
		this.caller = caller;
		this.operator = operator;
		this.status = status;
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

	public int getQueueLocation() {
		return this.queueLocation;
	}

	public void setQueueLocation(int i) {
		this.queueLocation = i;
	}

	/**
	 * Gets a string representation of the status of this answer
	 *
	 * @return The current status
	 */
	public String getStatusString() {
		switch (this.status) {
			case DUPLICATE:
				return "Duplicate";
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

	public Status getStatus() {
		return this.status;
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
	public void markCorrect(String caller) {
		this.caller = caller;
		this.status = Status.CORRECT;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * Mark this answer as a duplicate
	 */
	public void markDuplicate() {
		this.caller = "";
		this.operator = "";
		this.status = Status.DUPLICATE;
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

	public void setQNumber(int qNumber) {
		this.qNumber = qNumber;
	}

	public void changeAgreement(String user, Agreement agreement) {
		this.agreement.put(user, agreement);
	}

	public int getAgreement() {
		return Collections.frequency(this.agreement.values(), Agreement.AGREE)
				- Collections.frequency(this.agreement.values(), Agreement.DISAGREE);
	}

	public Agreement getAgreement(String user) {
		Agreement agreement = this.agreement.get(user);
		if (agreement == null) {
			agreement = Agreement.NEUTRAL;
		}
		return agreement;
	}

	public void changeName(String oldName, String newName) {
		if (this.submitter.equals(oldName)) {
			this.submitter = newName;
		}
		if (this.caller.equals(oldName)) {
			this.caller = newName;
		}
		if (this.agreement.containsKey(oldName)) {
			final Agreement agreement = this.agreement.get(oldName);
			this.agreement.remove(oldName);
			this.agreement.put(newName, agreement);
		}
	}

	public static class QNumberCompare implements Comparator<Answer> {

		@Override
		public int compare(Answer o1, Answer o2) {
			return ( (Integer) o1.getQNumber() ).compareTo(o2.getQNumber());
		}

	}

	public static class QNumberCompareReverse implements Comparator<Answer> {

		@Override
		public int compare(Answer o1, Answer o2) {
			return ( (Integer) o2.getQNumber() ).compareTo(o1.getQNumber());
		}

	}

	public static class StatusCompare implements Comparator<Answer> {
		@Override
		public int compare(Answer o1, Answer o2) {
			return o1.status.compareTo(o2.status);
		}
	}

	public static class StatusCompareReverse implements Comparator<Answer> {
		@Override
		public int compare(Answer o1, Answer o2) {
			return o2.status.compareTo(o1.status);
		}
	}

	public static class TimestampCompare implements Comparator<Answer> {
		@Override
		public int compare(Answer o1, Answer o2) {
			return ( (Integer) o1.getQueueLocation() ).compareTo(o2.getQueueLocation());
		}
	}

	public static class TimestampCompareReverse implements Comparator<Answer> {
		@Override
		public int compare(Answer o1, Answer o2) {
			return ( (Integer) o2.getQueueLocation() ).compareTo(o1.getQueueLocation());
		}
	}

	// Valid statuses
	public static enum Status {
		DUPLICATE, NOT_CALLED_IN, CALLING, INCORRECT, PARTIAL, CORRECT
	}

}
