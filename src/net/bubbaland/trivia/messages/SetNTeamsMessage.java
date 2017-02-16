package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetNTeamsMessage extends Message {

	@JsonProperty("nTeams")
	final int nTeams;

	/**
	 * @return the nTeams
	 */
	public int getnTeams() {
		return this.nTeams;
	}

	@JsonCreator
	public SetNTeamsMessage(@JsonProperty("nTeams") int nTeams) {
		this.nTeams = nTeams;
	}

}
