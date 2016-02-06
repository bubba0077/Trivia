package net.bubbaland.trivia;

import java.util.Date;

public class User {

	// Array of the last round versions sent to this client
	private int[]	roundVersions;
	// User name for this client
	private String	userName;
	// Role of this client
	private Role	role;
	// Last time this client sent a command
	private Date	lastActive;
	// Time changed to current role
	private Date	lastRollChange;

	// final public static User IDLE_USER = new User();
	//
	// static {
	// IDLE_USER.setUserName("Idle");
	// IDLE_USER.setRole(Role.IDLE);
	// }

	public User() {
		this.lastActive = new Date();
		this.userName = "";
		this.role = Role.RESEARCHER;
		this.roundVersions = null;
		this.lastRollChange = new Date();
	}

	public User(int nRounds) {
		this.lastActive = new Date();
		this.userName = "";
		this.setRole(Role.RESEARCHER);
		this.roundVersions = new int[nRounds];
	}

	public Date getLastRollChange() {
		return this.lastRollChange;
	}

	public Date getLastActive() {
		return this.lastActive;
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
