package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MarkAnswerCorrectMessage extends AnswerMessage {

	@JsonCreator
	public MarkAnswerCorrectMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("queueIndex") int queueIndex) {
		super(roundNumber, queueIndex);
	}

}
