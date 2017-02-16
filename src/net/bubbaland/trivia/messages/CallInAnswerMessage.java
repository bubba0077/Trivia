package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CallInAnswerMessage extends AnswerMessage {

	@JsonCreator
	public CallInAnswerMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("queueIndex") int queueIndex) {
		super(roundNumber, queueIndex);
	}
}