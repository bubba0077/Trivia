package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RemapQuestionMessage extends QuestionMessage {

	@JsonProperty("newQuestionNumber")
	final int newQuestionNumber;

	/**
	 * @return the newQuestionNumber
	 */
	public int getNewQuestionNumber() {
		return this.newQuestionNumber;
	}

	@JsonCreator
	public RemapQuestionMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("questionNumber") int questionNumber,
			@JsonProperty("newQuestionNumber") int newQuestionNumber) {
		super(roundNumber, questionNumber);
		this.newQuestionNumber = newQuestionNumber;
	}

}
