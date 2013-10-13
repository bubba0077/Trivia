package net.bubbaland.trivia.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

public class UserList {
	
	private Hashtable<String, Date> userList;
	
	public UserList() {
		this.userList = new Hashtable<String, Date>(0);
	}
	
	public void updateUser(String user) {
		this.userList.put(user, new Date());
	}
		
	public String[] getRecent(int windowInSec) {
		Date currentDate = new Date();
		ArrayList<String> recentList = new ArrayList<String>(0);
		
		for(String user : userList.keySet() ) {
			Date lastDate = userList.get(user);
			long diff = ( currentDate.getTime() - lastDate.getTime() )/1000;
			if(diff < windowInSec) {
				recentList.add(user);
			}
		}
		
		String[] recent = new String[recentList.size()];
		recentList.toArray(recent);
		return recent;
		
	}
	

}
