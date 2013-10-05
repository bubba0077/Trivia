package net.bubbaland.trivia.server;

import java.rmi.*;

// TODO: Auto-generated Javadoc
/**
 * The Interface TriviaInterface.
 */
public interface TriviaInterface extends Remote {

	// need all the methods of TriviaServer here

	/**
	 * Gets the round number.
	 *
	 * @return the round number
	 * @throws RemoteException the remote exception
	 */
	public int getRoundNumber() throws RemoteException;

	/**
	 * Gets the n teams.
	 *
	 * @return the n teams
	 * @throws RemoteException the remote exception
	 */
	public int getNTeams() throws RemoteException;
	
	/**
	 * Gets the max questions.
	 *
	 * @return the max questions
	 * @throws RemoteException the remote exception
	 */
	public int getMaxQuestions() throws RemoteException;

	/**
	 * Gets the n questions.
	 *
	 * @return the n questions
	 * @throws RemoteException the remote exception
	 */
	public int getNQuestions() throws RemoteException;

	/**
	 * Gets the n questions.
	 *
	 * @param rNumber the r number
	 * @return the n questions
	 * @throws RemoteException the remote exception
	 */
	public int getNQuestions(int rNumber) throws RemoteException;
	
	/**
	 * Gets the n rounds.
	 *
	 * @return the n rounds
	 * @throws RemoteException the remote exception
	 */
	public int getNRounds() throws RemoteException;

	/**
	 * Next to open.
	 *
	 * @return the int
	 * @throws RemoteException the remote exception
	 */
	public int nextToOpen() throws RemoteException;

	/**
	 * Gets the value.
	 *
	 * @return the value
	 * @throws RemoteException the remote exception
	 */
	public int getValue() throws RemoteException;

	/**
	 * Gets the value.
	 *
	 * @param rNumber the r number
	 * @return the value
	 * @throws RemoteException the remote exception
	 */
	public int getValue(int rNumber) throws RemoteException;

	/**
	 * Gets the value.
	 *
	 * @param rNumber the r number
	 * @param qNumber the q number
	 * @return the value
	 * @throws RemoteException the remote exception
	 */
	public int getValue(int rNumber, int qNumber) throws RemoteException;

	/**
	 * Gets the cumulative value.
	 *
	 * @param rNumber the r number
	 * @return the cumulative value
	 * @throws RemoteException the remote exception
	 */
	public int getCumulativeValue(int rNumber) throws RemoteException;
	
	/**
	 * Gets the earned.
	 *
	 * @return the earned
	 * @throws RemoteException the remote exception
	 */
	public int getEarned() throws RemoteException;

	/**
	 * Gets the earned.
	 *
	 * @param rNumber the r number
	 * @return the earned
	 * @throws RemoteException the remote exception
	 */
	public int getEarned(int rNumber) throws RemoteException;

	/**
	 * Gets the cumulative earned.
	 *
	 * @param rNumber the r number
	 * @return the cumulative earned
	 * @throws RemoteException the remote exception
	 */
	public int getCumulativeEarned(int rNumber) throws RemoteException;
	
	/**
	 * Gets the earned.
	 *
	 * @param rNumber the r number
	 * @param qNumber the q number
	 * @return the earned
	 * @throws RemoteException the remote exception
	 */
	public int getEarned(int rNumber, int qNumber) throws RemoteException;

	/**
	 * Gets the current round value.
	 *
	 * @return the current round value
	 * @throws RemoteException the remote exception
	 */
	public int getCurrentRoundValue() throws RemoteException;

	/**
	 * Gets the current round earned.
	 *
	 * @return the current round earned
	 * @throws RemoteException the remote exception
	 */
	public int getCurrentRoundEarned() throws RemoteException;

	/**
	 * Gets the announced points.
	 *
	 * @param rNumber the r number
	 * @return the announced points
	 * @throws RemoteException the remote exception
	 */
	public int getAnnouncedPoints(int rNumber) throws RemoteException;

	/**
	 * Gets the announced place.
	 *
	 * @param rNumber the r number
	 * @return the announced place
	 * @throws RemoteException the remote exception
	 */
	public int getAnnouncedPlace(int rNumber) throws RemoteException;

	/**
	 * Gets the question text.
	 *
	 * @param qNumber the q number
	 * @return the question text
	 * @throws RemoteException the remote exception
	 */
	public String getQuestionText(int qNumber) throws RemoteException;

	/**
	 * Gets the question text.
	 *
	 * @param rNumber the r number
	 * @param qNumber the q number
	 * @return the question text
	 * @throws RemoteException the remote exception
	 */
	public String getQuestionText(int rNumber, int qNumber) throws RemoteException;

	/**
	 * Been open.
	 *
	 * @param rNumber the r number
	 * @param qNumber the q number
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	public boolean beenOpen(int rNumber, int qNumber) throws RemoteException;

	/**
	 * Gets the each earned.
	 *
	 * @param rNumber the r number
	 * @return the each earned
	 * @throws RemoteException the remote exception
	 */
	public int[] getEachEarned(int rNumber) throws RemoteException;

	/**
	 * Gets the each value.
	 *
	 * @param rNumber the r number
	 * @return the each value
	 * @throws RemoteException the remote exception
	 */
	public int[] getEachValue(int rNumber) throws RemoteException;

	/**
	 * Gets the answer queue size.
	 *
	 * @return the answer queue size
	 * @throws RemoteException the remote exception
	 */
	public int getAnswerQueueSize() throws RemoteException;
	
	/**
	 * Gets the discrepency text.
	 *
	 * @param rNumber the r number
	 * @return the discrepency text
	 * @throws RemoteException the remote exception
	 */
	public String getDiscrepencyText(int rNumber) throws RemoteException;

	/**
	 * Sets the discrepency text.
	 *
	 * @param rNumber the r number
	 * @param discrepencyText the discrepency text
	 * @throws RemoteException the remote exception
	 */
	public void setDiscrepencyText(int rNumber, String discrepencyText) throws RemoteException;
	
	/**
	 * Gets the each question text.
	 *
	 * @param rNumber the r number
	 * @return the each question text
	 * @throws RemoteException the remote exception
	 */
	public String[] getEachQuestionText(int rNumber) throws RemoteException;

	/**
	 * Gets the each answer text.
	 *
	 * @param rNumber the r number
	 * @return the each answer text
	 * @throws RemoteException the remote exception
	 */
	public String[] getEachAnswerText(int rNumber) throws RemoteException;

	/**
	 * Gets the each submitter.
	 *
	 * @param rNumber the r number
	 * @return the each submitter
	 * @throws RemoteException the remote exception
	 */
	public String[] getEachSubmitter(int rNumber) throws RemoteException;

	/**
	 * Gets the each operator.
	 *
	 * @param rNumber the r number
	 * @return the each operator
	 * @throws RemoteException the remote exception
	 */
	public String[] getEachOperator(int rNumber) throws RemoteException;

	/**
	 * Each been open.
	 *
	 * @param rNumber the r number
	 * @return the boolean[]
	 * @throws RemoteException the remote exception
	 */
	public boolean[] eachBeenOpen(int rNumber) throws RemoteException;

	/**
	 * Each open.
	 *
	 * @param rNumber the r number
	 * @return the boolean[]
	 * @throws RemoteException the remote exception
	 */
	public boolean[] eachOpen(int rNumber) throws RemoteException;

	/**
	 * Each correct.
	 *
	 * @param rNumber the r number
	 * @return the boolean[]
	 * @throws RemoteException the remote exception
	 */
	public boolean[] eachCorrect(int rNumber) throws RemoteException;

	/**
	 * Gets the answer queue timestamps.
	 *
	 * @return the answer queue timestamps
	 * @throws RemoteException the remote exception
	 */
	public String[] getAnswerQueueTimestamps() throws RemoteException;

	/**
	 * Gets the answer queue q numbers.
	 *
	 * @return the answer queue q numbers
	 * @throws RemoteException the remote exception
	 */
	public int[] getAnswerQueueQNumbers() throws RemoteException;

	/**
	 * Gets the answer queue answers.
	 *
	 * @return the answer queue answers
	 * @throws RemoteException the remote exception
	 */
	public String[] getAnswerQueueAnswers() throws RemoteException;

	/**
	 * Gets the answer queue submitters.
	 *
	 * @return the answer queue submitters
	 * @throws RemoteException the remote exception
	 */
	public String[] getAnswerQueueSubmitters() throws RemoteException;

	/**
	 * Gets the answer queue confidences.
	 *
	 * @return the answer queue confidences
	 * @throws RemoteException the remote exception
	 */
	public int[] getAnswerQueueConfidences() throws RemoteException;

	/**
	 * Gets the answer queue callers.
	 *
	 * @return the answer queue callers
	 * @throws RemoteException the remote exception
	 */
	public String[] getAnswerQueueCallers() throws RemoteException;

	/**
	 * Gets the answer queue operators.
	 *
	 * @return the answer queue operators
	 * @throws RemoteException the remote exception
	 */
	public String[] getAnswerQueueOperators() throws RemoteException;

	/**
	 * Gets the answer queue statuses.
	 *
	 * @return the answer queue statuses
	 * @throws RemoteException the remote exception
	 */
	public String[] getAnswerQueueStatuses() throws RemoteException;

	/**
	 * Gets the answer queue status.
	 *
	 * @param queueIndex the queue index
	 * @return the answer queue status
	 * @throws RemoteException the remote exception
	 */
	public String getAnswerQueueStatus(int queueIndex) throws RemoteException;

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
	 * Gets the open question text.
	 *
	 * @return the open question text
	 * @throws RemoteException the remote exception
	 */
	public String[] getOpenQuestionText() throws RemoteException;

	/**
	 * Gets the open question numbers.
	 *
	 * @return the open question numbers
	 * @throws RemoteException the remote exception
	 */
	public String[] getOpenQuestionNumbers() throws RemoteException;

	/**
	 * Gets the open question values.
	 *
	 * @return the open question values
	 * @throws RemoteException the remote exception
	 */
	public String[] getOpenQuestionValues() throws RemoteException;

	/**
	 * Checks if is current speed.
	 *
	 * @return true, if is current speed
	 * @throws RemoteException the remote exception
	 */
	public boolean isCurrentSpeed() throws RemoteException;

	/**
	 * Checks if is speed.
	 *
	 * @param rNumber the r number
	 * @return true, if is speed
	 * @throws RemoteException the remote exception
	 */
	public boolean isSpeed(int rNumber) throws RemoteException;
	
	/**
	 * Checks if is open.
	 *
	 * @param qNumber the q number
	 * @return true, if is open
	 * @throws RemoteException the remote exception
	 */
	public boolean isOpen(int qNumber) throws RemoteException;

	/**
	 * Checks if is announced.
	 *
	 * @param rNumber the r number
	 * @return true, if is announced
	 * @throws RemoteException the remote exception
	 */
	public boolean isAnnounced(int rNumber) throws RemoteException;

	/**
	 * Round over.
	 *
	 * @return true, if successful
	 * @throws RemoteException the remote exception
	 */
	public boolean roundOver() throws RemoteException;

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

}
