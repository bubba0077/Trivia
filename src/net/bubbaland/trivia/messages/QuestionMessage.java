package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuestionMessage extends RoundMessage {

	@JsonProperty("questionNumber")
	final int questionNumber;

	/**
	 * @return the questionNumber
	 */
	public int getQuestionNumber() {
		return this.questionNumber;
	}

	public QuestionMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("questionNumber") int questionNumber) {
		super(roundNumber);
		this.questionNumber = questionNumber;
	}
}
