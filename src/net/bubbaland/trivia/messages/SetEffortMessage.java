package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetEffortMessage extends QuestionMessage {

	@JsonCreator
	public SetEffortMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("questionNumber") int questionNumber) {
		super(roundNumber, questionNumber);
	}

}
