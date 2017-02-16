package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.trivia.Round;

public class UpdateRoundsMessage extends Message {

	@JsonProperty("updatedRounds")
	final Round[] updatedRounds;

	/**
	 * @return the updatedRounds
	 */
	public Round[] getUpdatedRounds() {
		return this.updatedRounds;
	}

	@JsonCreator
	public UpdateRoundsMessage(@JsonProperty("updatedRounds") Round[] updatedRounds) {
		this.updatedRounds = updatedRounds;
	}

}
