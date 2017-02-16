package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AnswerMessage extends RoundMessage {

	@JsonProperty("queueIndex")
	final int queueIndex;

	/**
	 * @return the queueIndex
	 */
	public int getQueueIndex() {
		return this.queueIndex;
	}

	@JsonCreator
	public AnswerMessage(@JsonProperty("roundNumber") int roundNumber, @JsonProperty("queueIndex") int queueIndex) {
		super(roundNumber);
		this.queueIndex = queueIndex;
	}

}