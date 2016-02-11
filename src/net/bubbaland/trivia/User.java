package net.bubbaland.trivia;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class User implements Serializable {

	private static final long	serialVersionUID	= 1932880137949465272L;

	// Array of the last round versions sent to this client
	@JsonProperty("roundVersions")
	private volatile int[]		roundVersions;
	// User name for this client
	@JsonProperty("userName")
	private volatile String		userName;
	// Role of this client
	@JsonProperty("role")
	private volatile Role		role;
	// Last time this client sent a command
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JsonProperty("lastActive")
	private volatile DateTime	lastActive;
	// Time changed to current role
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JsonProperty("lastRoleChange")
	private volatile DateTime	lastRoleChange;
	// Question user is working on
	@JsonProperty("currentEffort")
	private volatile int		currentEffort;

	public User() {
		this("", Role.RESEARCHER, null, DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), 0);
	}

	@JsonCreator
	public User(@JsonProperty("userName") String userName, @JsonProperty("role") Role role,
			@JsonProperty("roundVersions") int[] roundVersions, @JsonProperty("lastActive") DateTime lastActive,
			@JsonProperty("lastRoleChange") DateTime lastRoleChange, @JsonProperty("currentEffort") int currentEffort) {
		this.userName = userName;
		this.role = role;
		this.roundVersions = roundVersions;
		this.lastActive = lastActive;
		this.lastRoleChange = lastRoleChange;
		this.currentEffort = currentEffort;
	}

	public User(int nRounds) {
		this("", Role.RESEARCHER, new int[nRounds], DateTime.now(DateTimeZone.UTC), DateTime.now(DateTimeZone.UTC), 0);
	}

	public DateTime getLastRollChange() {
		return this.lastRoleChange;
	}

	public DateTime getLastActive() {
		return this.lastActive;
	}

	public Duration timeSinceLastActive() {
		return new Duration(this.lastActive, DateTime.now(DateTimeZone.UTC));
	}

	public Duration timeSinceLastRollChange() {
		return new Duration(this.lastRoleChange, DateTime.now(DateTimeZone.UTC));
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
		this.lastActive = DateTime.now(DateTimeZone.UTC);
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
		this.lastRoleChange = DateTime.now(DateTimeZone.UTC);
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
