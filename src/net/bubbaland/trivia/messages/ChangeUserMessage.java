package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ChangeUserMessage extends Message {

	@JsonProperty("newUserName")
	final String newUserName;

	/**
	 * @return the newUserName
	 */
	public String getNewUserName() {
		return this.newUserName;
	}

	@JsonCreator
	public ChangeUserMessage(@JsonProperty("newUserName") String newUserName) {
		this.newUserName = newUserName;
	}

}
