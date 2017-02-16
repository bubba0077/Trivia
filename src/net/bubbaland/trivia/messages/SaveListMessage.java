package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SaveListMessage extends Message {

	@JsonProperty("saveFilenames")
	final String[] saveFilenames;

	/**
	 * @return the saveFilenames
	 */
	public String[] getSaveFilenames() {
		return this.saveFilenames;
	}

	@JsonCreator
	public SaveListMessage(@JsonProperty("saveFilenames") String[] saveFilenames) {
		this.saveFilenames = saveFilenames;
	}

}
