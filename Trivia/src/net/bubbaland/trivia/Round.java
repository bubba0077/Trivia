package net.bubbaland.trivia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

// TODO: Auto-generated Javadoc
/**
 * The Class Round.
 */
public class Round implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1601712912797562923L;

	/** The n normal q. */
	private int							rNumber, nQuestions, nNormalQ;
	
	/** The questions. */
	private Question[]					questions;
	
	/** The announced. */
	private volatile boolean			speed, announced;
	
	/** The place. */
	private volatile int				announcedPoints, place;
	
	/** The answer queue. */
	private volatile ArrayList<Answer>	answerQueue;
	
	/** The discrepency text. */
	private String discrepencyText;

	/**
	 * Instantiates a new round.
	 *
	 * @param rNumber the r number
	 * @param nQuestions the n questions
	 * @param nNormalQ the n normal q
	 */
	public Round( int rNumber, int nQuestions, int nNormalQ ) {

		this.speed = false;
		this.rNumber = rNumber;
		this.nQuestions = nQuestions;
		this.nNormalQ = nNormalQ;
		this.questions = new Question[nQuestions];
		this.announced = false;
		this.announcedPoints = 0;
		this.place = 1;
		this.discrepencyText =  "";

		for ( int q = 0; q < nQuestions; q++ ) {
			this.questions[q] = new Question( this, q + 1 );
		}

		this.answerQueue = new ArrayList<Answer>( 0 );
	}

	/**
	 * Checks if is speed.
	 *
	 * @return true, if is speed
	 */
	public boolean isSpeed() {
		return speed;
	}

	/**
	 * Checks if is announced.
	 *
	 * @return true, if is announced
	 */
	public boolean isAnnounced() {
		return announced;
	}

	/**
	 * Gets the round number.
	 *
	 * @return the round number
	 */
	public int getRoundNumber() {
		return rNumber;
	}

	/**
	 * Gets the n questions.
	 *
	 * @return the n questions
	 */
	public int getNQuestions() {
		if ( this.speed ) {
			return nQuestions;
		} else {
			return nNormalQ;
		}
	}

	/**
	 * Gets the question.
	 *
	 * @param qNumber the q number
	 * @return the question
	 */
	public Question getQuestion(int qNumber) {
		return questions[qNumber - 1];
	}

	/**
	 * Gets the open questions.
	 *
	 * @return the open questions
	 */
	private Question[] getOpenQuestions() {
		int nOpen = this.nOpen();
		Question[] questions = new Question[nOpen];
		int q1 = 0;
		for ( Question q : this.questions ) {
			if ( q.isOpen() ) {
				questions[q1] = q;
				q1++;
			}
		}
		return questions;
	}

	/**
	 * Gets the open question text.
	 *
	 * @return the open question text
	 */
	public String[] getOpenQuestionText() {
		Question[] questions = getOpenQuestions();
		int nOpen = questions.length;
		String[] questionText = new String[nOpen];
		for ( int q = 0; q < nOpen; q++ ) {
			questionText[q] = questions[q].getQuestionText();
		}
		return questionText;
	}

	/**
	 * Gets the open question numbers.
	 *
	 * @return the open question numbers
	 */
	public String[] getOpenQuestionNumbers() {
		Question[] questions = getOpenQuestions();
		int nOpen = questions.length;
		String[] qNumbers = new String[nOpen];
		for ( int q = 0; q < nOpen; q++ ) {
			qNumbers[q] = "" + questions[q].getNumber();
		}
		return qNumbers;
	}

	/**
	 * Gets the open question values.
	 *
	 * @return the open question values
	 */
	public String[] getOpenQuestionValues() {
		Question[] questions = getOpenQuestions();
		int nOpen = questions.length;
		String[] questionValues = new String[nOpen];
		for ( int q = 0; q < nOpen; q++ ) {
			questionValues[q] = "" + questions[q].getValue();
		}
		return questionValues;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() {
		int value = 0;
		for ( Question q : this.questions ) {
			value += q.getValue();
		}
		return value;
	}

	/**
	 * Gets the each value.
	 *
	 * @return the each value
	 */
	public int[] getEachValue() {
		int[] values = new int[nQuestions];
		for ( int q = 0; q < values.length; q++ ) {
			values[q] = this.questions[q].getValue();
		}
		return values;
	}

	/**
	 * Gets the earned.
	 *
	 * @return the earned
	 */
	public int getEarned() {
		int value = 0;
		for ( Question q : this.questions ) {
			value += q.getEarned();
		}
		return value;
	}

	/**
	 * Gets the each earned.
	 *
	 * @return the each earned
	 */
	public int[] getEachEarned() {
		int[] earneds = new int[nQuestions];
		for ( int q = 0; q < earneds.length; q++ ) {
			earneds[q] = this.questions[q].getEarned();
		}
		return earneds;
	}

	/**
	 * Gets the each question text.
	 *
	 * @return the each question text
	 */
	public String[] getEachQuestionText() {
		String[] questions = new String[nQuestions];
		for ( int q = 0; q < questions.length; q++ ) {
			questions[q] = this.questions[q].getQuestionText();
		}
		return questions;
	}

	/**
	 * Gets the each answer text.
	 *
	 * @return the each answer text
	 */
	public String[] getEachAnswerText() {
		String[] answers = new String[nQuestions];
		for ( int q = 0; q < answers.length; q++ ) {
			answers[q] = this.questions[q].getAnswerText();
		}
		return answers;
	}

	/**
	 * Gets the each submitter.
	 *
	 * @return the each submitter
	 */
	public String[] getEachSubmitter() {
		String[] submitters = new String[nQuestions];
		for ( int q = 0; q < submitters.length; q++ ) {
			submitters[q] = this.questions[q].getSubmitter();
		}
		return submitters;
	}

	/**
	 * Gets the each operator.
	 *
	 * @return the each operator
	 */
	public String[] getEachOperator() {
		String[] operators = new String[nQuestions];
		for ( int q = 0; q < operators.length; q++ ) {
			operators[q] = this.questions[q].getOperator();
		}
		return operators;
	}

	/**
	 * Each been open.
	 *
	 * @return the boolean[]
	 */
	public boolean[] eachBeenOpen() {
		boolean[] beenOpens = new boolean[nQuestions];
		for ( int q = 0; q < beenOpens.length; q++ ) {
			beenOpens[q] = this.questions[q].beenOpen();
		}
		return beenOpens;
	}

	/**
	 * Each open.
	 *
	 * @return the boolean[]
	 */
	public boolean[] eachOpen() {
		boolean[] opens = new boolean[nQuestions];
		for ( int q = 0; q < opens.length; q++ ) {
			opens[q] = this.questions[q].isOpen();
		}
		return opens;
	}

	/**
	 * Each correct.
	 *
	 * @return the boolean[]
	 */
	public boolean[] eachCorrect() {
		boolean[] corrects = new boolean[nQuestions];
		for ( int q = 0; q < corrects.length; q++ ) {
			corrects[q] = this.questions[q].isCorrect();
		}
		return corrects;
	}

	/**
	 * Gets the announced.
	 *
	 * @return the announced
	 */
	public int getAnnounced() {
		return this.announcedPoints;
	}

	/**
	 * Gets the place.
	 *
	 * @return the place
	 */
	public int getPlace() {
		return this.place;
	}

	/**
	 * N correct.
	 *
	 * @return the int
	 */
	public int nCorrect() {
		int nCorrect = 0;
		for ( Question q : this.questions ) {
			if ( q.isCorrect() ) {
				nCorrect++;
			}
		}
		return nCorrect;
	}

	/**
	 * N open.
	 *
	 * @return the int
	 */
	public int nOpen() {
		int nOpen = 0;
		for ( Question q : this.questions ) {
			if ( q.isOpen() ) {
				nOpen++;
			}
		}
		return nOpen;
	}

	/**
	 * Next to open.
	 *
	 * @return the int
	 */
	public int nextToOpen() {
		int nextToOpen = 18;
		for ( Question q : questions ) {
			if ( !q.beenOpen() ) {
				nextToOpen = q.getNumber();
				break;
			}
		}
		if ( nextToOpen > getNQuestions() ) {
			return getNQuestions();
		} else {
			return nextToOpen;
		}
	}

	/**
	 * Checks if is open.
	 *
	 * @param qNumber the q number
	 * @return true, if is open
	 */
	public boolean isOpen(int qNumber) {
		return questions[qNumber - 1].isOpen();
	}

	/**
	 * Been open.
	 *
	 * @param qNumber the q number
	 * @return true, if successful
	 */
	public boolean beenOpen(int qNumber) {
		return questions[qNumber - 1].beenOpen();
	}

	/**
	 * Checks if is correct.
	 *
	 * @param qNumber the q number
	 * @return true, if is correct
	 */
	public boolean isCorrect(int qNumber) {
		return questions[qNumber - 1].isCorrect();
	}

	/**
	 * Checks if is mismatch.
	 *
	 * @return true, if is mismatch
	 */
	public boolean isMismatch() {
		if ( this.announcedPoints != -1 ) { return ( this.announcedPoints != this.getValue() ); }
		return false;
	}

	/**
	 * Gets the number.
	 *
	 * @param qNumber the q number
	 * @return the number
	 */
	public int getNumber(int qNumber) {
		return questions[qNumber - 1].getNumber();
	}

	/**
	 * Gets the value.
	 *
	 * @param qNumber the q number
	 * @return the value
	 */
	public int getValue(int qNumber) {
		return questions[qNumber - 1].getValue();
	}

	/**
	 * Gets the earned.
	 *
	 * @param qNumber the q number
	 * @return the earned
	 */
	public int getEarned(int qNumber) {
		return questions[qNumber - 1].getEarned();
	}

	/**
	 * Gets the question text.
	 *
	 * @param qNumber the q number
	 * @return the question text
	 */
	public String getQuestionText(int qNumber) {
		return questions[qNumber - 1].getQuestionText();
	}

	/**
	 * Gets the answer text.
	 *
	 * @param qNumber the q number
	 * @return the answer text
	 */
	public String getAnswerText(int qNumber) {
		return questions[qNumber - 1].getAnswerText();
	}

	/**
	 * Gets the operator.
	 *
	 * @param qNumber the q number
	 * @return the operator
	 */
	public String getOperator(int qNumber) {
		return questions[qNumber - 1].getOperator();
	}

	/**
	 * Gets the submitter.
	 *
	 * @param qNumber the q number
	 * @return the submitter
	 */
	public String getSubmitter(int qNumber) {
		return questions[qNumber - 1].getSubmitter();
	}

	/**
	 * Gets the answer queue size.
	 *
	 * @return the answer queue size
	 */
	public int getAnswerQueueSize() {
		return answerQueue.size();
	}

	/**
	 * Gets the answer queue.
	 *
	 * @return the answer queue
	 */
	public Answer[] getAnswerQueue() {
		return (Answer[])answerQueue.toArray();
	}

	/**
	 * Gets the answer queue timestamps.
	 *
	 * @return the answer queue timestamps
	 */
	public String[] getAnswerQueueTimestamps() {
		int nAnswers = getAnswerQueueSize();
		String[] timestamps = new String[nAnswers];
		for ( int a = 0; a < nAnswers; a++ ) {
			timestamps[a] = answerQueue.get( a ).getTimestamp();
		}
		return timestamps;
	}

	/**
	 * Gets the answer queue q numbers.
	 *
	 * @return the answer queue q numbers
	 */
	public int[] getAnswerQueueQNumbers() {
		int nAnswers = getAnswerQueueSize();
		int[] qNumbers = new int[nAnswers];
		for ( int a = 0; a < nAnswers; a++ ) {
			qNumbers[a] = answerQueue.get( a ).getQNumber();
		}
		return qNumbers;
	}

	/**
	 * Gets the answer queue answers.
	 *
	 * @return the answer queue answers
	 */
	public String[] getAnswerQueueAnswers() {
		int nAnswers = getAnswerQueueSize();
		String[] answers = new String[nAnswers];
		for ( int a = 0; a < nAnswers; a++ ) {
			answers[a] = answerQueue.get( a ).getAnswer();
		}
		return answers;
	}

	/**
	 * Gets the answer queue submitters.
	 *
	 * @return the answer queue submitters
	 */
	public String[] getAnswerQueueSubmitters() {
		int nAnswers = getAnswerQueueSize();
		String[] submitters = new String[nAnswers];
		for ( int a = 0; a < nAnswers; a++ ) {
			submitters[a] = answerQueue.get( a ).getSubmitter();
		}
		return submitters;
	}

	/**
	 * Gets the answer queue confidences.
	 *
	 * @return the answer queue confidences
	 */
	public int[] getAnswerQueueConfidences() {
		int nAnswers = getAnswerQueueSize();
		int[] confidences = new int[nAnswers];
		for ( int a = 0; a < nAnswers; a++ ) {
			confidences[a] = answerQueue.get( a ).getConfidence();
		}
		return confidences;
	}

	/**
	 * Gets the answer queue callers.
	 *
	 * @return the answer queue callers
	 */
	public String[] getAnswerQueueCallers() {
		int nAnswers = getAnswerQueueSize();
		String[] callers = new String[nAnswers];
		for ( int a = 0; a < nAnswers; a++ ) {
			callers[a] = answerQueue.get( a ).getCaller();
		}
		return callers;
	}

	/**
	 * Gets the answer queue operators.
	 *
	 * @return the answer queue operators
	 */
	public String[] getAnswerQueueOperators() {
		int nAnswers = getAnswerQueueSize();
		String[] operators = new String[nAnswers];
		for ( int a = 0; a < nAnswers; a++ ) {
			operators[a] = answerQueue.get( a ).getOperator();
		}
		return operators;
	}

	/**
	 * Gets the answer queue status.
	 *
	 * @return the answer queue status
	 */
	public String[] getAnswerQueueStatus() {
		int nAnswers = getAnswerQueueSize();
		String[] statuses = new String[nAnswers];
		for ( int a = 0; a < nAnswers; a++ ) {
			statuses[a] = answerQueue.get( a ).getStatusString();
		}
		return statuses;
	}

	/**
	 * Gets the answer queue status.
	 *
	 * @param queueIndex the queue index
	 * @return the answer queue status
	 */
	public String getAnswerQueueStatus(int queueIndex) {
		String status = answerQueue.get( queueIndex ).getStatusString();
		return status;
	}

	/**
	 * Gets the discrepency text.
	 *
	 * @return the discrepency text
	 */
	public String getDiscrepencyText() {
		return discrepencyText;
	}

	/**
	 * Sets the discrepency text.
	 *
	 * @param discrepencyText the new discrepency text
	 */
	public void setDiscrepencyText(String discrepencyText) {
		this.discrepencyText = discrepencyText;
	}

	/**
	 * Sets the value.
	 *
	 * @param qNumber the q number
	 * @param value the value
	 */
	public void setValue(int qNumber, int value) {
		questions[qNumber - 1].setValue( value );
	}

	/**
	 * Sets the question text.
	 *
	 * @param qNumber the q number
	 * @param question the question
	 */
	public void setQuestionText(int qNumber, String question) {
		questions[qNumber - 1].setQuestionText( question );
	}

	/**
	 * Sets the answer text.
	 *
	 * @param qNumber the q number
	 * @param answer the answer
	 */
	public void setAnswerText(int qNumber, String answer) {
		questions[qNumber - 1].setAnswerText( answer );
	}

	/**
	 * Sets the operator.
	 *
	 * @param qNumber the q number
	 * @param operator the operator
	 */
	public void setOperator(int qNumber, String operator) {
		questions[qNumber - 1].setOperator( operator );
	}

	/**
	 * Sets the submitter.
	 *
	 * @param qNumber the q number
	 * @param submitter the submitter
	 */
	public void setSubmitter(int qNumber, String submitter) {
		questions[qNumber - 1].setSubmitter( submitter );
	}

	/**
	 * Sets the announced.
	 *
	 * @param announcedPoints the announced points
	 * @param place the place
	 */
	public void setAnnounced(int announcedPoints, int place) {
		this.announced = true;
		this.announcedPoints = announcedPoints;
		this.place = place;
	}

	/**
	 * Propose answer.
	 *
	 * @param qNumber the q number
	 * @param answer the answer
	 * @param submitter the submitter
	 * @param confidence the confidence
	 */
	public void proposeAnswer(int qNumber, String answer, String submitter, int confidence) {
		answerQueue.add( new Answer( qNumber, answer, submitter, confidence ) );
	}

	/**
	 * Call in.
	 *
	 * @param queueIndex the queue index
	 * @param caller the caller
	 */
	public void callIn(int queueIndex, String caller) {
		answerQueue.get( queueIndex ).callIn( caller );
	}

	/**
	 * Mark incorrect.
	 *
	 * @param queueIndex the queue index
	 * @param caller the caller
	 */
	public void markIncorrect(int queueIndex, String caller) {
		answerQueue.get( queueIndex ).markIncorrect( caller );
	}

	/**
	 * Mark partial.
	 *
	 * @param queueIndex the queue index
	 * @param caller the caller
	 */
	public void markPartial(int queueIndex, String caller) {
		answerQueue.get( queueIndex ).markPartial( caller );
	}

	/**
	 * Mark correct.
	 *
	 * @param queueIndex the queue index
	 * @param caller the caller
	 * @param operator the operator
	 */
	public void markCorrect(int queueIndex, String caller, String operator) {
		Answer answer = answerQueue.get( queueIndex );
		answer.markCorrect( caller, operator );
		int qNumber = answer.getQNumber();
		String answerText = answer.getAnswer();
		String submitter = answer.getSubmitter();
		questions[qNumber - 1].markCorrect( answerText, submitter, operator );
	}

	/**
	 * Mark uncalled.
	 *
	 * @param queueIndex the queue index
	 */
	public void markUncalled(int queueIndex) {
		answerQueue.get( queueIndex ).markUncalled();
	}

	/**
	 * Mark incorrect.
	 *
	 * @param qNumber the q number
	 */
	public void markIncorrect(int qNumber) {
		questions[qNumber - 1].markIncorrect();
	}

	/**
	 * Open.
	 *
	 * @param qNumber the q number
	 * @param value the value
	 * @param question the question
	 */
	public void open(int qNumber, int value, String question) {
		questions[qNumber - 1].setValue( value );
		questions[qNumber - 1].setQuestionText( question );
		questions[qNumber - 1].open();
	}

	/**
	 * Close.
	 *
	 * @param qNumber the q number
	 */
	public void close(int qNumber) {
		questions[qNumber - 1].close();
	}

	/**
	 * Sets the speed.
	 */
	public void setSpeed() {
		this.speed = true;
	}

	/**
	 * Unset speed.
	 */
	public void unsetSpeed() {
		this.speed = false;
	}

	/**
	 * Round over.
	 *
	 * @return true, if successful
	 */
	public boolean roundOver() {
		boolean roundOver = true;
		for ( Question q : Arrays.copyOfRange( questions, 0, getNQuestions() ) ) {
			roundOver = roundOver && ( q.beenOpen() && !q.isOpen() );
		}
		return roundOver;
	}

	/**
	 * Adds the answer.
	 *
	 * @param qNumber the q number
	 * @param answer the answer
	 * @param submitter the submitter
	 * @param confidence the confidence
	 * @return the answer
	 */
	public Answer addAnswer(int qNumber, String answer, String submitter, int confidence) {
		Answer a = new Answer( qNumber, answer, submitter, confidence );
		answerQueue.add( a );
		return a;
	}

}
