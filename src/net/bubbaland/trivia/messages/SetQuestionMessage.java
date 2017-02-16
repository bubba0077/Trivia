package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetQuestionMessage extends QuestionMessage {

	/**
	 * @return the questionText
	 */
	public String getQuestionText() {
		return this.questionText;
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return this.value;
	}

	@JsonProperty("questionText")
	final String	questionText;
	@JsonProperty("value")
	final int		value;

	@JsonCreator
	public SetQuestionMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("questionNumber") int questionNumber, @JsonProperty("questionText") String questionText,
			@JsonProperty("value") int value) {
		super(roundNumber, questionNumber);
		this.questionText = questionText;
		this.value = value;
	}

}
