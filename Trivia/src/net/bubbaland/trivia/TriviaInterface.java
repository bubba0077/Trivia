package net.bubbaland.trivia;

import java.rmi.*;

// TODO: Auto-generated Javadoc
/**
 * The Interface TriviaInterface.
 */
public interface TriviaInterface extends Remote {

	// need all the methods of TriviaServer here

	/**
	 * Sets the discrepency text.
	 *
	 * @param rNumber the r number
	 * @param discrepencyText the discrepency text
	 * @throws RemoteException the remote exception
	 */
	public void setDiscrepencyText(int rNumber, String discrepencyText) throws RemoteException;

	/**
	 * New round.
	 *
	 * @throws RemoteException the remote exception
	 */
	public void newRound() throws RemoteException;

	/**
	 * Sets the n teams.
	 *
	 * @param nTeams the new n teams
	 * @throws RemoteException the remote exception
	 */
	public void setNTeams(int nTeams) throws RemoteException;
	
	/**
	 * Sets the announced.
	 *
	 * @param rNumber the r number
	 * @param score the score
	 * @param place the place
	 * @throws RemoteException the remote exception
	 */
	public void setAnnounced(int rNumber, int score, int place) throws RemoteException;

	/**
	 * Propose answer.
	 *
	 * @param qNumber the q number
	 * @param answer the answer
	 * @param submitter the submitter
	 * @param confidence the confidence
	 * @throws RemoteException the remote exception
	 */
	public void proposeAnswer(int qNumber, String answer, String submitter, int confidence) throws RemoteException;

	/**
	 * Call in.
	 *
	 * @param queueIndex the queue index
	 * @param caller the caller
	 * @throws RemoteException the remote exception
	 */
	public void callIn(int queueIndex, String caller) throws RemoteException;

	/**
	 * Mark incorrect.
	 *
	 * @param queueIndex the queue index
	 * @param caller the caller
	 * @throws RemoteException the remote exception
	 */
	public void markIncorrect(int queueIndex, String caller) throws RemoteException;

	/**
	 * Mark partial.
	 *
	 * @param queueIndex the queue index
	 * @param caller the caller
	 * @throws RemoteException the remote exception
	 */
	public void markPartial(int queueIndex, String caller) throws RemoteException;

	/**
	 * Mark correct.
	 *
	 * @param queueIndex the queue index
	 * @param caller the caller
	 * @param operator the operator
	 * @throws RemoteException the remote exception
	 */
	public void markCorrect(int queueIndex, String caller, String operator) throws RemoteException;

	/**
	 * Mark uncalled.
	 *
	 * @param queueIndex the queue index
	 * @throws RemoteException the remote exception
	 */
	public void markUncalled(int queueIndex) throws RemoteException;

	/**
	 * Sets the speed.
	 *
	 * @throws RemoteException the remote exception
	 */
	public void setSpeed() throws RemoteException;

	/**
	 * Unset speed.
	 *
	 * @throws RemoteException the remote exception
	 */
	public void unsetSpeed() throws RemoteException;

	/**
	 * Open.
	 *
	 * @param qNumber the q number
	 * @param qValue the q value
	 * @param question the question
	 * @throws RemoteException the remote exception
	 */
	public void open(int qNumber, int qValue, String question) throws RemoteException;

	/**
	 * Close.
	 *
	 * @param qNumber the q number
	 * @throws RemoteException the remote exception
	 */
	public void close(int qNumber) throws RemoteException;
	
	public Trivia getTrivia() throws RemoteException;

}
