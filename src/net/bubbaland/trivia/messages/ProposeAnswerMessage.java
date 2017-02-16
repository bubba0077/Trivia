package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProposeAnswerMessage extends QuestionMessage {

	/**
	 * @return the answerText
	 */
	public String getAnswerText() {
		return this.answerText;
	}

	/**
	 * @return the confidence
	 */
	public int getConfidence() {
		return this.confidence;
	}

	@JsonProperty("answerText")
	final String	answerText;
	@JsonProperty("confidence")
	final int		confidence;

	@JsonCreator
	public ProposeAnswerMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("questionNumber") int questionNumber, @JsonProperty("answerText") String answerText,
			@JsonProperty("confidence") int confidence) {
		super(roundNumber, questionNumber);
		this.answerText = answerText;
		this.confidence = confidence;
	}

}
