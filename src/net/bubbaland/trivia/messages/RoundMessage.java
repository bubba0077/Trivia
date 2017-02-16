package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RoundMessage extends Message {

	@JsonProperty("roundNumber")
	final int roundNumber;

	/**
	 * @return the roundNumber
	 */
	public int getRoundNumber() {
		return this.roundNumber;
	}

	@JsonCreator
	public RoundMessage(@JsonProperty("roundNumber") int roundNumber) {
		this.roundNumber = roundNumber;
	}


}
