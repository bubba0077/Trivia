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
		CALLER, TYPER, RESEARCHER
	}

	/** Data */
	private final Hashtable<String, Date>	userList;
	private final Hashtable<String, Role>	roleList;

	/**
	 * Create a new empty user list.
	 */
	public UserList() {
		this.userList = new Hashtable<String, Date>(0);
		this.roleList = new Hashtable<String, Role>(0);
	};

	/**
	 * Change a user name and transfer the role.
	 * 
	 * @param oldUser The old user name
	 * @param newUser The new user name
	 */
	public void changeUser(String oldUser, String newUser) {
		final Role role = this.roleList.get(oldUser);
		this.userList.remove(oldUser);
		this.roleList.remove(oldUser);
		this.updateRole(newUser, role);
	}

	/**
	 * Get users who have been active recently.
	 * 
	 * @param windowInSec The maximum time before a user is considered idle
	 * @return A Hashtable containing the active users and corresponding roles.
	 */
	public Hashtable<String, Role> getRecent(int windowInSec) {
		final Date currentDate = new Date();
		final Hashtable<String, Role> recentHash = new Hashtable<String, Role>(0);

		for (final String user : this.userList.keySet()) {
			final Date lastDate = this.userList.get(user);
			final long diff = ( currentDate.getTime() - lastDate.getTime() ) / 1000;
			if (diff < windowInSec) {
				recentHash.put(user, this.roleList.get(user));
			}
		}

		return recentHash;
	}

	/**
	 * Update the role of a user.
	 * 
	 * @param user The user's name
	 * @param role The new role
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
	 */
	public void updateUser(String user) {
		this.userList.put(user, new Date());
		if (!this.roleList.containsKey(user)) {
			this.roleList.put(user, Role.RESEARCHER);
		}
	}
	
}
