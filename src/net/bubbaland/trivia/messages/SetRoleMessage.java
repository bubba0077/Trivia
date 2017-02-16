package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.trivia.User;

public class SetRoleMessage extends Message {

	@JsonProperty("newRole")
	final User.Role newRole;

	/**
	 * @return the newRole
	 */
	public User.Role getNewRole() {
		return this.newRole;
	}

	@JsonCreator
	public SetRoleMessage(@JsonProperty("newRole") User.Role newRole) {
		this.newRole = newRole;
	}

}
