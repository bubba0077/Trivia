package net.bubbaland.trivia.server;

// TODO: Auto-generated Javadoc
/**
 * The Class Trivia.
 */
public class Trivia {
	
	/** The n teams. */
	private int				nRounds, nTeams;
	
	/** The current round. */
	private volatile Round	currentRound;
	
	/** The rounds. */
	private Round[]			rounds;

	/**
	 * Instantiates a new trivia.
	 *
	 * @param nRounds the n rounds
	 * @param nQuestions the n questions
	 * @param nNormalQ the n normal q
	 */
	public Trivia( int nRounds, int nQuestions, int nNormalQ ) {
		this.nRounds = nRounds;
		this.rounds = new Round[nRounds];
		for ( int r = 0; r < nRounds; r++ ) {
			this.rounds[r] = new Round( r + 1, nQuestions, nNormalQ );
		}
		this.currentRound = rounds[0];
		this.nTeams = 100;
	}

	/**
	 * Gets the round number.
	 *
	 * @return the round number
	 */
	public int getRoundNumber() {
		return this.currentRound.getRoundNumber();
	}
	
	/**
	 * Gets the n teams.
	 *
	 * @return the n teams
	 */
	public int getNTeams() {
		return this.nTeams;
	}

	/**
	 * Gets the n questions.
	 *
	 * @return the n questions
	 */
	public int getNQuestions() {
		return this.currentRound.getNQuestions();
	}

	/**
	 * Gets the n questions.
	 *
	 * @param rNumber the r number
	 * @return the n questions
	 */
	public int getNQuestions(int rNumber) {
		return this.rounds[rNumber - 1].getNQuestions();
	}
	
	/**
	 * Gets the n rounds.
	 *
	 * @return the n rounds
	 */
	public int getNRounds() {
		return this.nRounds;
	}

	/**
	 * Next to open.
	 *
	 * @return the int
	 */
	public int nextToOpen() {
		return currentRound.nextToOpen();
	}

	/**
	 * Gets the open question text.
	 *
	 * @return the open question text
	 */
	public String[] getOpenQuestionText() {
		return currentRound.getOpenQuestionText();
	}

	/**
	 * Gets the open question numbers.
	 *
	 * @return the open question numbers
	 */
	public String[] getOpenQuestionNumbers() {
		return currentRound.getOpenQuestionNumbers();
	}

	/**
	 * Gets the open question values.
	 *
	 * @return the open question values
	 */
	public String[] getOpenQuestionValues() {
		return currentRound.getOpenQuestionValues();
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() {
		int value = 0;
		for ( Round r : this.rounds ) {
			value += r.getValue();
		}
		return value;
	}

	/**
	 * Gets the value.
	 *
	 * @param rNumber the r number
	 * @return the value
	 */
	public int getValue(int rNumber) {
		return rounds[rNumber - 1].getValue();
	}
	
	/**
	 * Gets the cumulative value.
	 *
	 * @param rNumber the r number
	 * @return the cumulative value
	 */
	public int getCumulativeValue(int rNumber) {
		int value = 0;
		for(int r=0; r<rNumber; r++) {
			value += rounds[r].getValue();
		}
		return value;
	}
	
	/**
	 * Gets the value.
	 *
	 * @param rNumber the r number
	 * @param qNumber the q number
	 * @return the value
	 */
	public int getValue(int rNumber, int qNumber) {
		return rounds[rNumber - 1].getValue( qNumber );
	}

	/**
	 * Gets the earned.
	 *
	 * @return the earned
	 */
	public int getEarned() {
		int earned = 0;
		for ( Round r : this.rounds ) {
			earned += r.getEarned();
		}
		return earned;
	}

	/**
	 * Gets the earned.
	 *
	 * @param rNumber the r number
	 * @return the earned
	 */
	public int getEarned(int rNumber) {
		return rounds[rNumber - 1].getEarned();
	}

	/**
	 * Gets the cumulative earned.
	 *
	 * @param rNumber the r number
	 * @return the cumulative earned
	 */
	public int getCumulativeEarned(int rNumber) {
		int earned = 0;
		for(int r=0; r<rNumber; r++) {
			earned += rounds[r].getEarned();
		}
		return earned;
	}
	
	/**
	 * Gets the earned.
	 *
	 * @param rNumber the r number
	 * @param qNumber the q number
	 * @return the earned
	 */
	public int getEarned(int rNumber, int qNumber) {
		return rounds[rNumber - 1].getEarned( qNumber );
	}

	/**
	 * Gets the question text.
	 *
	 * @param qNumber the q number
	 * @return the question text
	 */
	public String getQuestionText(int qNumber) {
		return this.currentRound.getQuestionText( qNumber );
	}

	/**
	 * Gets the question text.
	 *
	 * @param rNumber the r number
	 * @param qNumber the q number
	 * @return the question text
	 */
	public String getQuestionText(int rNumber, int qNumber) {
		return rounds[rNumber - 1].getQuestionText( qNumber );
	}

	/**
	 * Been open.
	 *
	 * @param rNumber the r number
	 * @param qNumber the q number
	 * @return true, if successful
	 */
	public boolean beenOpen(int rNumber, int qNumber) {
		return rounds[rNumber - 1].beenOpen( qNumber );
	}

	/**
	 * Gets the current round value.
	 *
	 * @return the current round value
	 */
	public int getCurrentRoundValue() {
		return this.currentRound.getValue();
	}

	/**
	 * Gets the current round earned.
	 *
	 * @return the current round earned
	 */
	public int getCurrentRoundEarned() {
		return this.currentRound.getEarned();
	}

	/**
	 * Gets the answer queue.
	 *
	 * @return the answer queue
	 */
	public Answer[] getAnswerQueue() {
		return this.currentRound.getAnswerQueue();
	}

	/**
	 * Gets the announced points.
	 *
	 * @param rNumber the r number
	 * @return the announced points
	 */
	public int getAnnouncedPoints(int rNumber) {
		if ( rNumber > 0 ) { return rounds[rNumber - 1].getAnnounced(); }
		return 0;
	}

	/**
	 * Gets the announced place.
	 *
	 * @param rNumber the r number
	 * @return the announced place
	 */
	public int getAnnouncedPlace(int rNumber) {
		if ( rNumber > 0 ) { return rounds[rNumber - 1].getPlace(); }
		return 0;
	}
	
	/**
	 * Gets the answer queue size.
	 *
	 * @return the answer queue size
	 */
	public int getAnswerQueueSize() {
		return currentRound.getAnswerQueueSize();
	}

	/**
	 * Gets the answer queue timestamps.
	 *
	 * @return the answer queue timestamps
	 */
	public String[] getAnswerQueueTimestamps() {
		return currentRound.getAnswerQueueTimestamps();
	}

	/**
	 * Gets the answer queue q numbers.
	 *
	 * @return the answer queue q numbers
	 */
	public int[] getAnswerQueueQNumbers() {
		return currentRound.getAnswerQueueQNumbers();
	}

	/**
	 * Gets the answer queue answers.
	 *
	 * @return the answer queue answers
	 */
	public String[] getAnswerQueueAnswers() {
		return currentRound.getAnswerQueueAnswers();
	}

	/**
	 * Gets the answer queue submitters.
	 *
	 * @return the answer queue submitters
	 */
	public String[] getAnswerQueueSubmitters() {
		return currentRound.getAnswerQueueSubmitters();
	}

	/**
	 * Gets the answer queue confidences.
	 *
	 * @return the answer queue confidences
	 */
	public int[] getAnswerQueueConfidences() {
		return currentRound.getAnswerQueueConfidences();
	}

	/**
	 * Gets the answer queue callers.
	 *
	 * @return the answer queue callers
	 */
	public String[] getAnswerQueueCallers() {
		return currentRound.getAnswerQueueCallers();
	}

	/**
	 * Gets the answer queue operators.
	 *
	 * @return the answer queue operators
	 */
	public String[] getAnswerQueueOperators() {
		return currentRound.getAnswerQueueOperators();
	}

	/**
	 * Gets the answer queue statuses.
	 *
	 * @return the answer queue statuses
	 */
	public String[] getAnswerQueueStatuses() {
		return currentRound.getAnswerQueueStatus();
	}

	/**
	 * Gets the answer queue status.
	 *
	 * @param queueIndex the queue index
	 * @return the answer queue status
	 */
	public String getAnswerQueueStatus(int queueIndex) {
		return currentRound.getAnswerQueueStatus( queueIndex );
	}
	
	/**
	 * Gets the discrepency text.
	 *
	 * @param rNumber the r number
	 * @return the discrepency text
	 */
	public String getDiscrepencyText(int rNumber) {
		return rounds[rNumber - 1].getDiscrepencyText();
	}

	/**
	 * Sets the discrepency text.
	 *
	 * @param rNumber the r number
	 * @param discrepencyText the discrepency text
	 */
	public void setDiscrepencyText(int rNumber, String discrepencyText) {
		rounds[rNumber - 1].setDiscrepencyText(discrepencyText);
	}


	/**
	 * Gets the each earned.
	 *
	 * @param rNumber the r number
	 * @return the each earned
	 */
	public int[] getEachEarned(int rNumber) {
		return rounds[rNumber - 1].getEachEarned();
	}

	/**
	 * Gets the each value.
	 *
	 * @param rNumber the r number
	 * @return the each value
	 */
	public int[] getEachValue(int rNumber) {
		return rounds[rNumber - 1].getEachValue();
	}

	/**
	 * Gets the each question text.
	 *
	 * @param rNumber the r number
	 * @return the each question text
	 */
	public String[] getEachQuestionText(int rNumber) {
		return rounds[rNumber - 1].getEachQuestionText();
	}

	/**
	 * Gets the each answer text.
	 *
	 * @param rNumber the r number
	 * @return the each answer text
	 */
	public String[] getEachAnswerText(int rNumber) {
		return rounds[rNumber - 1].getEachAnswerText();
	}

	/**
	 * Gets the each submitter.
	 *
	 * @param rNumber the r number
	 * @return the each submitter
	 */
	public String[] getEachSubmitter(int rNumber) {
		return rounds[rNumber - 1].getEachSubmitter();
	}

	/**
	 * Gets the each operator.
	 *
	 * @param rNumber the r number
	 * @return the each operator
	 */
	public String[] getEachOperator(int rNumber) {
		return rounds[rNumber - 1].getEachOperator();
	}

	/**
	 * Each been open.
	 *
	 * @param rNumber the r number
	 * @return the boolean[]
	 */
	public boolean[] eachBeenOpen(int rNumber) {
		return rounds[rNumber - 1].eachBeenOpen();
	}

	/**
	 * Each open.
	 *
	 * @param rNumber the r number
	 * @return the boolean[]
	 */
	public boolean[] eachOpen(int rNumber) {
		return rounds[rNumber - 1].eachOpen();
	}

	/**
	 * Each correct.
	 *
	 * @param rNumber the r number
	 * @return the boolean[]
	 */
	public boolean[] eachCorrect(int rNumber) {
		return rounds[rNumber - 1].eachCorrect();
	}

	/**
	 * Checks if is speed.
	 *
	 * @param rNumber the r number
	 * @return true, if is speed
	 */
	public boolean isSpeed(int rNumber) {
		return rounds[rNumber - 1].isSpeed();
	}
	
	/**
	 * Checks if is current speed.
	 *
	 * @return true, if is current speed
	 */
	public boolean isCurrentSpeed() {
		return this.currentRound.isSpeed();
	}

	/**
	 * Checks if is open.
	 *
	 * @param qNumber the q number
	 * @return true, if is open
	 */
	public boolean isOpen(int qNumber) {
		return this.currentRound.isOpen( qNumber );
	}

	/**
	 * Checks if is announced.
	 *
	 * @param rNumber the r number
	 * @return true, if is announced
	 */
	public boolean isAnnounced(int rNumber) {
		if ( rNumber > 0 ) { return rounds[rNumber - 1].isAnnounced(); }
		return false;
	}

	/**
	 * Round over.
	 *
	 * @return true, if successful
	 */
	public boolean roundOver() {
		return this.currentRound.roundOver();
	}

	/**
	 * New round.
	 */
	public void newRound() {
		int currentRoundNumber = this.getRoundNumber();
		if ( currentRoundNumber++ <= this.nRounds ) {
			this.currentRound = rounds[currentRoundNumber - 1];
		}
	}

	/**
	 * Sets the n teams.
	 *
	 * @param nTeams the new n teams
	 */
	public void setNTeams(int nTeams) {
		this.nTeams = nTeams;
	}
	
	/**
	 * Sets the speed.
	 */
	public void setSpeed() {
		this.currentRound.setSpeed();
	}

	/**
	 * Sets the announced.
	 *
	 * @param rNumber the r number
	 * @param score the score
	 * @param place the place
	 */
	public void setAnnounced(int rNumber, int score, int place) {
		if ( rNumber > 0 ) {
			rounds[rNumber - 1].setAnnounced( score, place );
		}
		return;
	}

	/**
	 * Propose answer.
	 *
	 * @param qNumber the q number
	 * @param answer the answer
	 * @param user the user
	 * @param confidence the confidence
	 */
	public void proposeAnswer(int qNumber, String answer, String user, int confidence) {
		this.currentRound.proposeAnswer( qNumber, answer, user, confidence );
	}

	/**
	 * Call in.
	 *
	 * @param queueIndex the queue index
	 * @param caller the caller
	 */
	public void callIn(int queueIndex, String caller) {
		this.currentRound.callIn( queueIndex, caller );
	}

	/**
	 * Mark incorrect.
	 *
	 * @param queueIndex the queue index
	 * @param caller the caller
	 */
	public void markIncorrect(int queueIndex, String caller) {
		this.currentRound.markIncorrect( queueIndex, caller );
	}

	/**
	 * Mark partial.
	 *
	 * @param queueIndex the queue index
	 * @param caller the caller
	 */
	public void markPartial(int queueIndex, String caller) {
		this.currentRound.markPartial( queueIndex, caller );
	}

	/**
	 * Mark correct.
	 *
	 * @param queueIndex the queue index
	 * @param caller the caller
	 * @param operator the operator
	 */
	public void markCorrect(int queueIndex, String caller, String operator) {
		this.currentRound.markCorrect( queueIndex, caller, operator );
	}

	/**
	 * Mark uncalled.
	 *
	 * @param queueIndex the queue index
	 */
	public void markUncalled(int queueIndex) {
		this.currentRound.markUncalled( queueIndex );
	}

	/**
	 * Unset speed.
	 */
	public void unsetSpeed() {
		this.currentRound.unsetSpeed();
	}

	/**
	 * Open.
	 *
	 * @param nQuestion the n question
	 * @param qValue the q value
	 * @param question the question
	 */
	public void open(int nQuestion, int qValue, String question) {
		this.currentRound.open( nQuestion, qValue, question );
	}

	/**
	 * Close.
	 *
	 * @param nQuestion the n question
	 */
	public void close(int nQuestion) {
		this.currentRound.close( nQuestion );
	}

}
