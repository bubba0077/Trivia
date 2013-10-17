package net.bubbaland.trivia;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;

import net.bubbaland.trivia.UserList.Role;

/**
 * The interface for the trivia server.
 *
 * <code>TriviaInterface</code> provides the method API for calls to the trivia server.
 *
 * @author Walter Kolczynski
 *
 */
public interface TriviaInterface extends Remote {

	/**
	 * Call an answer in.
	 *
	 * @param queueIndex
	 *            The location of the answer in the queue
	 * @param caller
	 *            The caller's name
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void callIn(int queueIndex, String caller) throws RemoteException;

	/**
	 * Change a user's name.
	 *
	 * @param oldUser
	 *            The old user name
	 * @param newUser
	 *            The new user name
	 * @throws RemoteException
	 */
	public void changeUser(String oldUser, String newUser) throws RemoteException;

	/**
	 * Close a question.
	 *
	 * @param user
	 *            The user's name
	 * @param qNumber
	 *            The question number
	 * @param answer
	 *            The correct answer
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void close(String user, int qNumber, String answer) throws RemoteException;

	/**
	 * Edit question data.
	 *
	 * @param rNumber
	 *            The round number
	 * @param qNumber
	 *            The question number
	 * @param value
	 *            The new question value
	 * @param qText
	 *            The new question text
	 * @param aText
	 *            The new correct answer
	 * @param isCorrect
	 *            Whether the question was answered correctly
	 * @param submitter
	 *            The correct answer submitter
	 * @param operator
	 *            The operator who accepted the correct answer
	 * @throws RemoteException
	 */
	public void editQuestion(String user, int rNumber, int qNumber, int value, String qText, String aText,
			boolean isCorrect, String submitter, String operator) throws RemoteException;

	/**
	 * Get rounds that have changed. This is the primary method for retrieving updated data from the server.
	 *
	 * @param user
	 *            The user requesting data
	 * @param oldVersions
	 *            The round version numbers the user has.
	 * @return An array of all the rounds that have newer versions.
	 * @throws RemoteException
	 */
	public Round[] getChangedRounds(String user, int[] oldVersions) throws RemoteException;

	/**
	 * Get the current round number
	 *
	 * @return The current round number
	 * @throws RemoteException
	 */
	public int getCurrentRound() throws RemoteException;

	/**
	 * Get the full trivia data structure. This is used primarily when a client starts to initialize their local trivia
	 * data.
	 *
	 * @return The Trivia object
	 * @throws RemoteException
	 *             A remote exception
	 */
	public Trivia getTrivia() throws RemoteException;

	/**
	 * Get the users and roles that have been active. Active means having changed something on the server.
	 *
	 * @param window
	 *            Number of seconds without making a change before becoming idle
	 * @return The user names and roles of users who have been active within the activity window
	 * @throws RemoteException
	 */
	public Hashtable<String, Role> getActiveUsers(int window) throws RemoteException;

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
	 * @throws RemoteException
	 */
	public Hashtable<String, Role> getIdleUsers(int window, int timeout) throws RemoteException;

	/**
	 * Check a user in when they first start their client.
	 *
	 * @param user
	 *            The user's name
	 * @throws RemoteException
	 */
	public void login(String user) throws RemoteException;

	/**
	 * Gets a list of available saves.
	 *
	 * @return Array of save file names
	 * @throws RemoteException
	 *             A remote exception
	 */
	public String[] listSaves() throws RemoteException;

	/**
	 * Load a save state from file.
	 *
	 * @param user
	 *            The user's name
	 * @param stateFile
	 *            The name of the save state file to load.
	 *
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void loadState(String user, String stateFile) throws RemoteException;

	/**
	 * Mark a question correct.
	 *
	 * @param queueIndex
	 *            The location of the answer in the queue
	 * @param caller
	 *            The caller's name
	 * @param operator
	 *            The operator who accepted the answer
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void markCorrect(int queueIndex, String caller, String operator) throws RemoteException;

	/**
	 * Sets the announced scores for a round
	 *
	 * @param rNumber
	 *            The round number
	 * @param score
	 *            The announced score
	 * @param place
	 *            The announced place
	 * @throws RemoteException
	 *             A remote exception
	 */

	/**
	 * Mark a question incorrect.
	 *
	 * @param queueIndex
	 *            The location of the answer in the queue
	 * @param caller
	 *            The caller's name
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void markIncorrect(int queueIndex, String caller) throws RemoteException;

	/**
	 * Mark a question partially correct.
	 *
	 * @param queueIndex
	 *            The location of the answer in the queue
	 * @param caller
	 *            The caller's name
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void markPartial(int queueIndex, String caller) throws RemoteException;

	/**
	 * Mark uncalled.
	 *
	 * @param user
	 *            The user's name
	 * @param queueIndex
	 *            The location of the answer in the queue
	 *
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void markUncalled(String user, int queueIndex) throws RemoteException;

	/**
	 * Starts a new round.
	 *
	 * @param user
	 *            The user's name
	 *
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void newRound(String user) throws RemoteException;

	/**
	 * Open a question
	 *
	 * @param user
	 *            The user's name
	 * @param qNumber
	 *            The question number
	 * @param qValue
	 *            The question's value
	 * @param question
	 *            The question
	 *
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void open(String user, int qNumber, int qValue, String question) throws RemoteException;

	/**
	 * Propose an answer.
	 *
	 * @param qNumber
	 *            The question number
	 * @param answer
	 *            The proposed answer
	 * @param submitter
	 *            The submitter's name
	 * @param confidence
	 *            The confidence in the answer
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void proposeAnswer(int qNumber, String answer, String submitter, int confidence) throws RemoteException;

	/**
	 * Sets the discrepancy text.
	 *
	 * @param user
	 *            The user's name
	 * @param rNumber
	 *            The round number
	 * @param discrepancyText
	 *            The discrepancy text
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void setDiscrepancyText(String user, int rNumber, String discrepancyText) throws RemoteException;

	/**
	 * Change the user's role.
	 *
	 * @param user
	 *            The user name
	 * @param role
	 *            The new role
	 * @throws RemoteException
	 */
	public void setRole(String user, Role role) throws RemoteException;

	/**
	 * Makes the current round a speed round.
	 *
	 * @param user
	 *            The user making the change
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void setSpeed(String user) throws RemoteException;

	/**
	 * Makes the current round a normal round.
	 *
	 * @param user
	 *            The user making the change
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void unsetSpeed(String user) throws RemoteException;

}
