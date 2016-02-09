package net.bubbaland.trivia;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

public class User implements Serializable {

	private static final long		serialVersionUID	= 1932880137949465272L;

	// Array of the last round versions sent to this client
	@JsonProperty("roundVersions")
	private volatile int[]			roundVersions;
	// User name for this client
	@JsonProperty("userName")
	private volatile String			userName;
	// Role of this client
	@JsonProperty("role")
	private volatile Role			role;
	// Last time this client sent a command
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonProperty("lastActive")
	private volatile LocalDateTime	lastActive;
	// Time changed to current role
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonProperty("lastRoleChange")
	private volatile LocalDateTime	lastRoleChange;
	// Question user is working on
	@JsonProperty("currentEffort")
	private volatile int			currentEffort;

	public User() {
		this("", Role.RESEARCHER, null, LocalDateTime.now(ZoneOffset.UTC), LocalDateTime.now(ZoneOffset.UTC), 0);
	}

	@JsonCreator
	public User(@JsonProperty("userName") String userName, @JsonProperty("role") Role role,
			@JsonProperty("roundVersions") int[] roundVersions, @JsonProperty("lastActive") LocalDateTime lastActive,
			@JsonProperty("lastRoleChange") LocalDateTime lastRoleChange,
			@JsonProperty("currentEffort") int currentEffort) {
		this.userName = userName;
		this.role = role;
		this.roundVersions = roundVersions;
		this.lastActive = lastActive;
		this.lastRoleChange = lastRoleChange;
		this.currentEffort = currentEffort;
	}

	public User(int nRounds) {
		this("", Role.RESEARCHER, new int[nRounds], LocalDateTime.now(ZoneOffset.UTC),
				LocalDateTime.now(ZoneOffset.UTC), 0);
	}

	public LocalDateTime getLastRollChange() {
		return this.lastRoleChange;
	}

	public LocalDateTime getLastActive() {
		return this.lastActive;
	}

	public Duration timeSinceLastActive() {
		return Duration.between(this.lastActive, LocalDateTime.now(ZoneOffset.UTC));
	}

	public Duration timeSinceLastRollChange() {
		return Duration.between(this.lastRoleChange, LocalDateTime.now(ZoneOffset.UTC));
	}

	public void setEffort(int qNumber) {
		this.currentEffort = qNumber;
	}

	public int getEffort() {
		return this.currentEffort;
	}

	public void endEffort(int qNumber) {
		if (this.currentEffort == qNumber) {
			this.currentEffort = 0;
		}
	}

	public void updateActivity() {
		this.lastActive = LocalDateTime.now(ZoneOffset.UTC);
	}

	/**
	 * @return the lastVersions
	 */
	public int[] getRoundVersions() {
		return this.roundVersions;
	}

	/**
	 * @param lastVersions
	 *            the lastVersions to set
	 */
	public void setRoundVersions(int[] lastVersions) {
		this.roundVersions = lastVersions;
	}

	/**
	 * @return the user
	 */
	public String getUserName() {
		return this.userName;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUserName(String user) {
		this.userName = user;
	}

	/**
	 * @return the role
	 */
	public User.Role getRole() {
		return this.role;
	}

	/**
	 * @param role
	 *            the role to set
	 */
	public void setRole(User.Role role) {
		this.role = role;
		this.lastRoleChange = LocalDateTime.now(ZoneOffset.UTC);
	}

	public enum Role {
		TYPIST, CALLER, RESEARCHER, IDLE
	}

	public int compareTo(User otherUser) {
		if (this.getRole() == otherUser.getRole()) {
			return this.getUserName().compareTo(otherUser.getUserName());
		} else {
			return this.getRole().compareTo(otherUser.getRole());
		}
	}

	public String toString() {
		return this.userName;
	}

	public String fullString() {
		return this.userName + " Role: " + this.role + " Last Active: " + this.lastActive;
	}


}
