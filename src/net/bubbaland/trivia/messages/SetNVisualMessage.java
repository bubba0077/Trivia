package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetNVisualMessage extends Message {

	@JsonProperty("nVisual")
	final int nVisual;

	/**
	 * @return the nVisual
	 */
	public int getnVisual() {
		return this.nVisual;
	}

	@JsonCreator
	public SetNVisualMessage(@JsonProperty("nVisual") int nVisual) {
		this.nVisual = nVisual;
	}

}
