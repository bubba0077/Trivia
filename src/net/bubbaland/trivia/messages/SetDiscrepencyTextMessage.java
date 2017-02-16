package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetDiscrepencyTextMessage extends RoundMessage {

	@JsonProperty("discrepencyText")
	final String discrepencyText;

	/**
	 * @return the discrepencyText
	 */
	public String getDiscrepencyText() {
		return this.discrepencyText;
	}

	@JsonCreator
	public SetDiscrepencyTextMessage(@JsonProperty("roundNumber") int roundNumber,
			@JsonProperty("discrepencyText") String discrepencyText) {
		super(roundNumber);
		this.discrepencyText = discrepencyText;
	}

}
