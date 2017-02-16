package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoadSaveMessage extends Message {

	@JsonProperty("saveName")
	final String saveName;

	/**
	 * @return the saveName
	 */
	public String getSaveName() {
		return this.saveName;
	}

	@JsonCreator
	public LoadSaveMessage(@JsonProperty("saveName") String saveName) {
		this.saveName = saveName;
	}

}
