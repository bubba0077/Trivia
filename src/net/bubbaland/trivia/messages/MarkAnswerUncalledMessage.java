package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MarkAnswerUncalledMessage extends AnswerMessage {
	@JsonCreator
	public MarkAnswerUncalledMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("queueIndex") int queueIndex) {
		super(roundNumber, queueIndex);
	}
}
