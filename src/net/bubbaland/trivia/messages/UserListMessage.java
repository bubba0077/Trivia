package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.trivia.User;

public class UserListMessage extends Message {

	@JsonProperty("userList")
	final User[] userList;

	/**
	 * @return the userList
	 */
	public User[] getUserList() {
		return this.userList;
	}

	@JsonCreator
	public UserListMessage(@JsonProperty("userList") User[] userList) {
		this.userList = userList;
	}

}
