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
	private final Hashtable<String, Date>	activeUserList;
	private final Hashtable<String, Date>	passiveUserList;
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
	 * Get users who have been active recently.
	 * 
	 * @param windowInSec
	 *            The maximum time before a user is considered idle
	 * @return A Hashtable containing the active users and corresponding roles.
	 */
	public Hashtable<String, Role> getActive(int windowInSec) {
		final Date currentDate = new Date();
		final Hashtable<String, Role> userHash = new Hashtable<String, Role>(0);

		for (final String user : this.activeUserList.keySet()) {
			final Date lastDate = this.activeUserList.get(user);
			final long diff = ( currentDate.getTime() - lastDate.getTime() ) / 1000;
			if (diff < windowInSec) {
				userHash.put(user, this.roleList.get(user));
			}
		}
		return userHash;
	}

	public Hashtable<String, Role> getPassive(int windowInSec, int timeoutInSec) {
		final Date currentDate = new Date();
		final Hashtable<String, Role> userHash = new Hashtable<String, Role>(0);

		for (final String user : this.passiveUserList.keySet()) {
			final Date lastDate = this.passiveUserList.get(user);
			final long diff = ( currentDate.getTime() - lastDate.getTime() ) / 1000;
			if (diff < timeoutInSec) {
				userHash.put(user, this.roleList.get(user));
			}
		}
		for (final String user : this.activeUserList.keySet()) {
			final Date lastDate = this.activeUserList.get(user);
			final long diff = ( currentDate.getTime() - lastDate.getTime() ) / 1000;
			if (diff < windowInSec) {
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
	 */
	public void updateUserActivity(String user) {
		this.activeUserList.put(user, new Date());
		this.passiveUserList.put(user, new Date());
		if (!this.roleList.containsKey(user)) {
			this.roleList.put(user, Role.RESEARCHER);
		}
	}

	public void userHandshake(String user) {
		this.passiveUserList.put(user, new Date());
	}


}
