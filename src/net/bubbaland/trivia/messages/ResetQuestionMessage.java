package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResetQuestionMessage extends QuestionMessage {

	@JsonCreator
	public ResetQuestionMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("questionNumber") int questionNumber) {
		super(roundNumber, questionNumber);
	}

}
