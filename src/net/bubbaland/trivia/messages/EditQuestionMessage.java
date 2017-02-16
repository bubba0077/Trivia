package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EditQuestionMessage extends QuestionMessage {

	/**
	 * @return the questionText
	 */
	public String getQuestionText() {
		return this.questionText;
	}

	/**
	 * @return the answerText
	 */
	public String getAnswerText() {
		return this.answerText;
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return this.value;
	}

	/**
	 * @return the isCorrect
	 */
	public boolean isCorrect() {
		return this.isCorrect;
	}

	@JsonProperty("questionText")
	final String	questionText;
	@JsonProperty("answerText")
	final String	answerText;
	@JsonProperty("submitter")
	final String	submitter;

	/**
	 * @return the submitter
	 */
	public String getSubmitter() {
		return this.submitter;
	}

	@JsonProperty("value")
	final int		value;
	@JsonProperty("isCorrect")
	final boolean	isCorrect;

	@JsonCreator
	public EditQuestionMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("questionNumber") int questionNumber, @JsonProperty("questionText") String questionText,
			@JsonProperty("value") int value, @JsonProperty("answerText") String answerText,
			@JsonProperty("isCorrect") boolean isCorrect, @JsonProperty("submitter") String submitter) {
		super(roundNumber, questionNumber);
		this.questionText = questionText;
		this.value = value;
		this.answerText = answerText;
		this.isCorrect = isCorrect;
		this.submitter = submitter;
	}

}
