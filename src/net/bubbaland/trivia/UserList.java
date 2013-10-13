package net.bubbaland.trivia;

import java.util.Date;
import java.util.Hashtable;

public class UserList {
	
	private Hashtable<String, Date> userList;
	private Hashtable<String, Role> roleList;
	
	public enum Role {CALLER, TYPER, RESEARCHER};
	
	public UserList() {
		this.userList = new Hashtable<String, Date>(0);
		this.roleList = new Hashtable<String, Role>(0);
	}
	
	public void updateUser(String user) {
		this.userList.put(user, new Date());
		if(!roleList.containsKey(user)) {
			this.roleList.put(user, Role.RESEARCHER);
		}
	}
	
	public void updateRole(String user, Role role) {
		this.userList.put(user, new Date());
		this.roleList.put(user, role);
	}
	
	public void changeUser(String oldUser, String newUser) {
		Role role = this.roleList.get(oldUser);
		this.userList.remove(oldUser);
		this.roleList.remove(oldUser);
		this.updateRole(newUser, role);
	}
		
	public Hashtable<String, Role> getRecent(int windowInSec) {
		Date currentDate = new Date();
		Hashtable<String, Role> recentHash = new Hashtable<String, Role>(0);
		
		for(String user : userList.keySet() ) {
			Date lastDate = userList.get(user);
			long diff = ( currentDate.getTime() - lastDate.getTime() )/1000;
			if(diff < windowInSec) {
				recentHash.put(user, roleList.get(user));
			}
		}
		
		return recentHash;
	}
	

}
