package net.bubbaland.trivia;

import java.util.Date;

public class User {

	// Array of the last round versions sent to this client
	private volatile int[]	roundVersions;
	// User name for this client
	private volatile String	userName;
	// Role of this client
	private volatile Role	role;
	// Last time this client sent a command
	private volatile Date	lastActive;
	// Time changed to current role
	private volatile Date	lastRollChange;

	private volatile int	currentEffort;

	public User() {
		this.lastActive = new Date();
		this.userName = "";
		this.role = Role.RESEARCHER;
		this.roundVersions = null;
		this.lastRollChange = new Date();
		this.currentEffort = 0;
	}

	public User(int nRounds) {
		this();
		this.roundVersions = new int[nRounds];
	}

	public Date getLastRollChange() {
		return this.lastRollChange;
	}

	public Date getLastActive() {
		return this.lastActive;
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
		this.lastActive = new Date();
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
		this.lastRollChange = new Date();
	}

	public enum Role {
		TYPIST, CALLER, RESEARCHER, IDLE
	}

	public int compareTo(User otherUser) {
		if (this.getUserName().equals(otherUser.getUserName())) {
			return this.getRole().compareTo(otherUser.getRole());
		} else {
			return this.getUserName().compareTo(otherUser.getUserName());
		}
	}

	public String toString() {
		return this.userName;
	}

	public String fullString() {
		return this.userName + " Role: " + this.role + " Last Active: " + this.lastActive;
	}

}
