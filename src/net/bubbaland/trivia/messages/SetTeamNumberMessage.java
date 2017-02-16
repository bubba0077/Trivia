package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetTeamNumberMessage extends Message {

	@JsonProperty("teamNumber")
	final int teamNumber;

	/**
	 * @return the teamNumber
	 */
	public int getTeamNumber() {
		return this.teamNumber;
	}

	@JsonCreator
	public SetTeamNumberMessage(@JsonProperty("teamNumber") int teamNumber) {
		this.teamNumber = teamNumber;
	}

}
