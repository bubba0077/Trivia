package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MarkAnswerPartialMessage extends AnswerMessage {

	@JsonCreator
	public MarkAnswerPartialMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("queueIndex") int queueIndex) {
		super(roundNumber, queueIndex);
	}

}
