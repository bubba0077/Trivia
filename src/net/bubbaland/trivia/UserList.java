package net.bubbaland.trivia;

import java.util.Date;
import java.util.Hashtable;

/**
 * A data structure to track user names, last activity time and role.
 * 
 * @author Walter Kolczynski
 * 
 */
public class UserList {

	/** Data */
	// User list that is tracks when the user makes a change
	private final Hashtable<String, Date>	userList;
	// List of user roles
	private final Hashtable<String, Role>	roleList;

	/**
	 * Create a new empty user list.
	 */
	public UserList() {
		this.userList = new Hashtable<String, Date>(0);
		this.roleList = new Hashtable<String, Role>(0);
	}

	/**
	 * Change a user name and transfer the role.
	 * 
	 * @param oldUser
	 *            The old user name
	 * @param newUser
	 *            The new user name
	 */
	public void changeUser(String oldUser, String newUser) {
		final Role role = this.roleList.get(oldUser);
		this.userList.remove(oldUser);
		this.roleList.remove(oldUser);
		this.updateRole(newUser, role);
	};

	/**
	 * Get the users and roles that have been active. Active means having changed something on the server.
	 * 
	 * @param window
	 *            Number of seconds without making a change before becoming idle
	 * @return The user names and roles of users who have been active within the activity window
	 */
	public Hashtable<String, Role> getActive(int timeToIdle) {
		final Date currentDate = new Date();
		final Hashtable<String, Role> userHash = new Hashtable<String, Role>(0);

		// Build a list of users who are active
		for (final String user : this.userList.keySet()) {
			final Date lastDate = this.userList.get(user);
			final long diff = ( currentDate.getTime() - lastDate.getTime() ) / 1000;
			if (diff < timeToIdle) {
				userHash.put(user, this.roleList.get(user));
			}
		}
		return userHash;
	}

	/**
	 * Get the users and roles that are idle. Idle means they are still contacting the server for updates, but haven't
	 * made any changes.
	 * 
	 * @param window
	 *            Number of seconds without making a change before becoming idle
	 * @param timeout
	 *            Number of second before a disconnected user should be considered timed out
	 * @return The user names and roles of users who have not been active but have still received an update within the
	 *         timeout window
	 */
	public Hashtable<String, Role> getIdle(int timeToIdle) {
		final Date currentDate = new Date();
		final Hashtable<String, Role> userHash = new Hashtable<String, Role>(0);

		// Build a list of users who are active
		for (final String user : this.userList.keySet()) {
			final Date lastDate = this.userList.get(user);
			final long diff = ( currentDate.getTime() - lastDate.getTime() ) / 1000;
			if (diff >= timeToIdle) {
				userHash.put(user, this.roleList.get(user));
			}
		}
		return userHash;
	}

	/**
	 * Update the role of a user.
	 * 
	 * @param user
	 *            The user's name
	 * @param role
	 *            The new role
	 */
	public void updateRole(String user, Role role) {
		// Update last activity time
		this.userList.put(user, new Date());
		// Change role
		this.roleList.put(user, role);
	}


	/**
	 * Update user's last activity time.
	 * 
	 * @param user
	 *            The user's name
	 */
	public void updateUserActivity(String user) {
		this.userList.put(user, new Date());
		if (!this.roleList.containsKey(user)) {
			this.roleList.put(user, Role.RESEARCHER);
		}
	}

	public void removeUser(String user) {
		this.userList.remove(user);
		this.roleList.remove(user);
	}

	// /**
	// * Update user's last contact time.
	// *
	// * @param user
	// * The user's name
	// */
	// public void userHandshake(String user) {
	// this.passiveUserList.put(user, new Date());
	// }

	public enum Role {
		TYPIST, CALLER, RESEARCHER
	}


}
