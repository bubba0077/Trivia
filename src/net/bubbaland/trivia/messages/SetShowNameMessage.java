package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetShowNameMessage extends RoundMessage {

	@JsonProperty("showName")
	final String showName;

	/**
	 * @return the showName
	 */
	public String getShowName() {
		return this.showName;
	}

	@JsonCreator
	public SetShowNameMessage(@JsonProperty("roundNumber") int roundNumber, @JsonProperty("showName") String showName) {
		super(roundNumber);
		this.showName = showName;
	}

}
