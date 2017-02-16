package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetShowHostMessage extends RoundMessage {

	@JsonProperty("showHost")
	final String showHost;

	/**
	 * @return the showHost
	 */
	public String getShowHost() {
		return this.showHost;
	}

	@JsonCreator
	public SetShowHostMessage(@JsonProperty("roundNumber") int roundNumber, @JsonProperty("showHost") String showHost) {
		super(roundNumber);
		this.showHost = showHost;
	}

}
