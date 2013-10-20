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

	public enum Role {
		TYPIST, CALLER, RESEARCHER, IDLE
	}

	/** Data */
	// User list that is tracks when the user makes a change
	private final Hashtable<String, Date>	activeUserList;
	// User list that is tracks when a data refresh is requested
	private final Hashtable<String, Date>	passiveUserList;
	// List of user roles
	private final Hashtable<String, Role>	roleList;

	/**
	 * Create a new empty user list.
	 */
	public UserList() {
		this.activeUserList = new Hashtable<String, Date>(0);
		this.passiveUserList = new Hashtable<String, Date>(0);
		this.roleList = new Hashtable<String, Role>(0);
	};

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
		this.activeUserList.remove(oldUser);
		this.passiveUserList.remove(oldUser);
		this.roleList.remove(oldUser);
		this.updateRole(newUser, role);
	}

	/**
	 * Get the users and roles that have been active. Active means having changed something on the server.
	 * 
	 * @param window
	 *            Number of seconds without making a change before becoming idle
	 * @return The user names and roles of users who have been active within the activity window
	 */
	public Hashtable<String, Role> getActive(int window, int timeout) {
		final Date currentDate = new Date();
		final Hashtable<String, Role> userHash = new Hashtable<String, Role>(0);

		// Build a list of users who are active
		for (final String user : this.activeUserList.keySet()) {
			final Date lastDate = this.activeUserList.get(user);
			final long diff = ( currentDate.getTime() - lastDate.getTime() ) / 1000;
			if (diff < window) {
				userHash.put(user, this.roleList.get(user));
			}
		}
		// Remove users who have timed out
		for (final String user : this.passiveUserList.keySet()) {
			final Date lastDate = this.passiveUserList.get(user);
			final long diff = ( currentDate.getTime() - lastDate.getTime() ) / 1000;
			if (diff < timeout) {
				userHash.remove(user);
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
	public Hashtable<String, Role> getIdle(int window, int timeout) {
		final Date currentDate = new Date();
		final Hashtable<String, Role> userHash = new Hashtable<String, Role>(0);

		// Build a list of users getting updates
		for (final String user : this.passiveUserList.keySet()) {
			final Date lastDate = this.passiveUserList.get(user);
			final long diff = ( currentDate.getTime() - lastDate.getTime() ) / 1000;
			if (diff < timeout) {
				userHash.put(user, Role.IDLE);
			}
		}

		// Remove active users
		for (final String user : this.activeUserList.keySet()) {
			final Date lastDate = this.activeUserList.get(user);
			final long diff = ( currentDate.getTime() - lastDate.getTime() ) / 1000;
			if (diff < window) {
				userHash.remove(user);
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
		this.activeUserList.put(user, new Date());
		// Update last activity time
		this.passiveUserList.put(user, new Date());
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
		this.activeUserList.put(user, new Date());
		this.passiveUserList.put(user, new Date());
		if (!this.roleList.containsKey(user)) {
			this.roleList.put(user, Role.RESEARCHER);
		}
	}

	/**
	 * Update user's last contact time.
	 * 
	 * @param user
	 *            The user's name
	 */
	public void userHandshake(String user) {
		this.passiveUserList.put(user, new Date());
	}


}
