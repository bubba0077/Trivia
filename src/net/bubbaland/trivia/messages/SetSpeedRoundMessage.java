package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetSpeedRoundMessage extends RoundMessage {

	@JsonProperty("nowSpeed")
	final boolean nowSpeed;

	/**
	 * @return the nowSpeed
	 */
	public boolean isNowSpeed() {
		return this.nowSpeed;
	}

	@JsonCreator
	public SetSpeedRoundMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("nowSpeed") boolean nowSpeed) {
		super(roundNumber);
		this.nowSpeed = nowSpeed;
	}

}
