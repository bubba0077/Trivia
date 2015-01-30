package net.bubbaland.trivia;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;

import net.bubbaland.trivia.UserList.Role;

public interface TriviaClientInterface extends Remote {
	public void updateRound(int currentRound) throws RemoteException;

	public void updateTrivia(Round[] newRounds) throws RemoteException;

	public void updateActiveUsers(Hashtable<String, Role> activeUserList) throws RemoteException;

	public void updateIdleUsers(Hashtable<String, Role> passiveUserList) throws RemoteException;

	public String getUser() throws RemoteException;

	public int[] getVersions() throws RemoteException;

	public int getCurrentRoundNumber() throws RemoteException;

	public Hashtable<String, Role> getActiveUserHash() throws RemoteException;

	public Hashtable<String, Role> getIdleUserHash() throws RemoteException;
}
