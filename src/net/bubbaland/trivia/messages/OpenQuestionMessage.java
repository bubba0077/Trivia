package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenQuestionMessage extends QuestionMessage {

	@JsonCreator
	public OpenQuestionMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("questionNumber") int questionNumber) {
		super(roundNumber, questionNumber);
	}

}
