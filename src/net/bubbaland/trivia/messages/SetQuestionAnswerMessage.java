package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetQuestionAnswerMessage extends QuestionMessage {

	@JsonProperty("answerText")
	final String answerText;

	/**
	 * @return the answerText
	 */
	public String getAnswerText() {
		return this.answerText;
	}

	@JsonCreator
	public SetQuestionAnswerMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("questionNumber") int questionNumber, @JsonProperty("answerText") String answerText) {
		super(roundNumber, questionNumber);
		this.answerText = answerText;
	}

}
