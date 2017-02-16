package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetRoundMessage extends RoundMessage {

	@JsonCreator
	public SetRoundMessage(@JsonProperty("roundNumber") int roundNumber) {
		super(roundNumber);
	}

}
