package net.bubbaland.trivia;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface for the trivia server.
 * 
 * <code>TriviaInterface</code> provides the method API for calls to the trivia server.
 * 
 * @author Walter Kolczynski
 * 
 */
public interface TriviaInterface extends Remote {

	// need all the methods of TriviaServer here

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
	 * Close a question.
	 * @param user TODO
	 * @param qNumber
	 *            The question number
	 * @param answer TODO
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void close(String user, int qNumber, String answer) throws RemoteException;

	/**
	 * Get the trivia data structure.
	 * 
	 * @return The Trivia object
	 * @throws RemoteException
	 *             A remote exception
	 */
	public Trivia getTrivia() throws RemoteException;

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
	 * @param user TODO
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
	 * @param user TODO
	 * @param queueIndex
	 *            The location of the answer in the queue
	 * 
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void markUncalled(String user, int queueIndex) throws RemoteException;

	/**
	 * Starts a new round.
	 * @param user TODO
	 * 
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void newRound(String user) throws RemoteException;

	/**
	 * Open a question
	 * @param user TODO
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
//	public void setAnnounced(int rNumber, int score, int place) throws RemoteException;

	/**
	 * Sets the discrepancy text.
	 * @param user TODO
	 * @param rNumber
	 *            The round number
	 * @param discrepancyText
	 *            The discrepancy text
	 * 
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void setDiscrepancyText(String user, int rNumber, String discrepancyText) throws RemoteException;

	/**
	 * Sets the number of teams.
	 * 
	 * @param nTeams
	 *            The number of teams
	 * @throws RemoteException
	 *             A remote exception
	 */
//	public void setNTeams(int nTeams) throws RemoteException;

	/**
	 * Makes the current round a speed round.
	 * @param user TODO
	 * 
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void setSpeed(String user) throws RemoteException;

	/**
	 * Makes the current round a normal round.
	 * @param user TODO
	 * 
	 * @throws RemoteException
	 *             A remote exception
	 */
	public void unsetSpeed(String user) throws RemoteException;
	
	public Round[] getChangedRounds(int[] oldVersions) throws RemoteException;
	
	public int getCurrentRound() throws RemoteException;
	
	public String[] getUserList(int window) throws RemoteException;
	
	public void handshake(String user) throws RemoteException;

}
