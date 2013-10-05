package net.bubbaland.trivia.server;

import javax.jws.WebMethod;
import javax.jws.WebService;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;

// TODO: Auto-generated Javadoc
/**
 * The Class TriviaServer.
 */
@WebService
public class TriviaServer extends UnicastRemoteObject implements TriviaInterface {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -8062985452301507239L;
	
	/** The Constant N_ROUNDS. */
	private static final int	N_ROUNDS			= 50;
	
	/** The Constant N_QUESTIONS. */
	private static final int	N_QUESTIONS			= 18;
	
	/** The Constant N_NORMAL_Q. */
	private static final int	N_NORMAL_Q			= 9;
	
	/** The trivia. */
	private Trivia				trivia;

	/**
	 * Instantiates a new trivia server.
	 *
	 * @throws RemoteException the remote exception
	 */
	public TriviaServer() throws RemoteException {
		this.trivia = new Trivia( N_ROUNDS, N_QUESTIONS, N_NORMAL_Q );
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String args[]) throws Exception {

		LocateRegistry.createRegistry( 1099 );

		if ( System.getSecurityManager() == null ) {
			// System.setSecurityManager(new RMISecurityManager());
		}

		TriviaServer server = new TriviaServer();

		try {
			Naming.bind( "TriviaInterface", server );
			System.out.println( "Trivia Server is Ready" );
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		// server.test();

	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getRoundNumber()
	 */
	@WebMethod
	public int getRoundNumber() throws RemoteException {
		return trivia.getRoundNumber();
	}
	
	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getNTeams()
	 */
	public int getNTeams() throws RemoteException {
		return trivia.getNTeams();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getMaxQuestions()
	 */
	public int getMaxQuestions() throws RemoteException {
		return TriviaServer.N_QUESTIONS;
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getNQuestions()
	 */
	public int getNQuestions() throws RemoteException {
		return trivia.getNQuestions();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getNQuestions(int)
	 */
	public int getNQuestions(int rNumber) throws RemoteException {
		return trivia.getNQuestions( rNumber );
	}
	
	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getNRounds()
	 */
	public int getNRounds() throws RemoteException {
		return trivia.getNRounds();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#nextToOpen()
	 */
	public int nextToOpen() throws RemoteException {
		return trivia.nextToOpen();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getValue()
	 */
	@WebMethod
	public int getValue() throws RemoteException {
		return trivia.getValue();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getValue(int)
	 */
	public int getValue(int rNumber) throws RemoteException {
		return trivia.getValue( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getCumulativeValue(int)
	 */
	public int getCumulativeValue(int rNumber) throws RemoteException {
		return trivia.getCumulativeValue( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getValue(int, int)
	 */
	public int getValue(int rNumber, int qNumber) throws RemoteException {
		return trivia.getValue( rNumber, qNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getEarned()
	 */
	@WebMethod
	public int getEarned() throws RemoteException {
		return trivia.getEarned();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getEarned(int)
	 */
	public int getEarned(int rNumber) throws RemoteException {
		return trivia.getEarned( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getCumulativeEarned(int)
	 */
	public int getCumulativeEarned(int rNumber) throws RemoteException {
		return trivia.getCumulativeEarned( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getEarned(int, int)
	 */
	public int getEarned(int rNumber, int qNumber) throws RemoteException {
		return trivia.getEarned( rNumber, qNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getQuestionText(int)
	 */
	public String getQuestionText(int qNumber) throws RemoteException {
		return trivia.getQuestionText( qNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getQuestionText(int, int)
	 */
	public String getQuestionText(int rNumber, int qNumber) throws RemoteException {
		return trivia.getQuestionText( rNumber, qNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#beenOpen(int, int)
	 */
	public boolean beenOpen(int rNumber, int qNumber) throws RemoteException {
		return trivia.beenOpen( rNumber, qNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getEachEarned(int)
	 */
	public int[] getEachEarned(int rNumber) throws RemoteException {
		return trivia.getEachEarned( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getEachValue(int)
	 */
	public int[] getEachValue(int rNumber) throws RemoteException {
		return trivia.getEachValue( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getEachQuestionText(int)
	 */
	public String[] getEachQuestionText(int rNumber) throws RemoteException {
		return trivia.getEachQuestionText( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getEachAnswerText(int)
	 */
	public String[] getEachAnswerText(int rNumber) throws RemoteException {
		return trivia.getEachAnswerText( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getEachSubmitter(int)
	 */
	public String[] getEachSubmitter(int rNumber) throws RemoteException {
		return trivia.getEachSubmitter( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getEachOperator(int)
	 */
	public String[] getEachOperator(int rNumber) throws RemoteException {
		return trivia.getEachOperator( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#eachBeenOpen(int)
	 */
	public boolean[] eachBeenOpen(int rNumber) throws RemoteException {
		return trivia.eachBeenOpen( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#eachOpen(int)
	 */
	public boolean[] eachOpen(int rNumber) throws RemoteException {
		return trivia.eachOpen( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#eachCorrect(int)
	 */
	public boolean[] eachCorrect(int rNumber) throws RemoteException {
		return trivia.eachCorrect( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getCurrentRoundValue()
	 */
	@WebMethod
	public int getCurrentRoundValue() throws RemoteException {
		return trivia.getCurrentRoundValue();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getCurrentRoundEarned()
	 */
	@WebMethod
	public int getCurrentRoundEarned() throws RemoteException {
		return trivia.getCurrentRoundEarned();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getAnnouncedPoints(int)
	 */
	public int getAnnouncedPoints(int rNumber) throws RemoteException {
		return trivia.getAnnouncedPoints( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getAnnouncedPlace(int)
	 */
	public int getAnnouncedPlace(int rNumber) throws RemoteException {
		return trivia.getAnnouncedPlace( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getAnswerQueueSize()
	 */
	public int getAnswerQueueSize() throws RemoteException {
		return trivia.getAnswerQueueSize();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getOpenQuestionText()
	 */
	public String[] getOpenQuestionText() throws RemoteException {
		return trivia.getOpenQuestionText();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getOpenQuestionNumbers()
	 */
	public String[] getOpenQuestionNumbers() throws RemoteException {
		return trivia.getOpenQuestionNumbers();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getOpenQuestionValues()
	 */
	public String[] getOpenQuestionValues() throws RemoteException {
		return trivia.getOpenQuestionValues();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getAnswerQueueTimestamps()
	 */
	public String[] getAnswerQueueTimestamps() throws RemoteException {
		return trivia.getAnswerQueueTimestamps();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getAnswerQueueQNumbers()
	 */
	public int[] getAnswerQueueQNumbers() throws RemoteException {
		return trivia.getAnswerQueueQNumbers();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getAnswerQueueAnswers()
	 */
	public String[] getAnswerQueueAnswers() throws RemoteException {
		return trivia.getAnswerQueueAnswers();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getAnswerQueueSubmitters()
	 */
	public String[] getAnswerQueueSubmitters() throws RemoteException {
		return trivia.getAnswerQueueSubmitters();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getAnswerQueueConfidences()
	 */
	public int[] getAnswerQueueConfidences() throws RemoteException {
		return trivia.getAnswerQueueConfidences();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getAnswerQueueCallers()
	 */
	public String[] getAnswerQueueCallers() throws RemoteException {
		return trivia.getAnswerQueueCallers();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getAnswerQueueOperators()
	 */
	public String[] getAnswerQueueOperators() throws RemoteException {
		return trivia.getAnswerQueueOperators();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getAnswerQueueStatuses()
	 */
	public String[] getAnswerQueueStatuses() throws RemoteException {
		return trivia.getAnswerQueueStatuses();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getAnswerQueueStatus(int)
	 */
	public String getAnswerQueueStatus(int queueIndex) throws RemoteException {
		return trivia.getAnswerQueueStatus( queueIndex );
	}
	
	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#getDiscrepencyText(int)
	 */
	public String getDiscrepencyText(int rNumber) throws RemoteException {
		return trivia.getDiscrepencyText(rNumber);
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#setDiscrepencyText(int, java.lang.String)
	 */
	public void setDiscrepencyText(int rNumber, String discrepencyText) throws RemoteException{
		trivia.setDiscrepencyText(rNumber, discrepencyText);
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#newRound()
	 */
	@WebMethod
	public void newRound() throws RemoteException {
		System.out.println( "New round starting..." );
		trivia.newRound();
	}
	
	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#setNTeams(int)
	 */
	public void setNTeams(int nTeams) throws RemoteException {
		trivia.setNTeams(nTeams);
	}
	

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#setSpeed()
	 */
	@WebMethod
	public void setSpeed() throws RemoteException {
		System.out.println( "Making round " + trivia.getRoundNumber() + " a speed round" );
		trivia.setSpeed();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#setAnnounced(int, int, int)
	 */
	public void setAnnounced(int rNumber, int score, int place) throws RemoteException {
		trivia.setAnnounced( rNumber, score, place );
		System.out.println( "Announced for round " + rNumber + ":" );
		System.out.println( "Score: " + score + "  Place: " + place );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#proposeAnswer(int, java.lang.String, java.lang.String, int)
	 */
	public void proposeAnswer(int qNumber, String answer, String submitter, int confidence) throws RemoteException {
		trivia.proposeAnswer( qNumber, answer, submitter, confidence );
		System.out.println( submitter + " submitted an answer for Q" + qNumber + " with a confidence of " + confidence
				+ ":" );
		System.out.println( answer );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#callIn(int, java.lang.String)
	 */
	public void callIn(int queueIndex, String caller) throws RemoteException {
		trivia.callIn( queueIndex, caller );
		System.out.println( caller + " is calling in item " + queueIndex + " in the answer queue." );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#markIncorrect(int, java.lang.String)
	 */
	public void markIncorrect(int queueIndex, String caller) throws RemoteException {
		trivia.markIncorrect( queueIndex, caller );
		System.out.println( "Item " + queueIndex + " in the queue is incorrect." );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#markPartial(int, java.lang.String)
	 */
	public void markPartial(int queueIndex, String caller) throws RemoteException {
		trivia.markPartial( queueIndex, caller );
		System.out.println( "Item " + queueIndex + " in the queue is partially correct." );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#markCorrect(int, java.lang.String, java.lang.String)
	 */
	public void markCorrect(int queueIndex, String caller, String operator) throws RemoteException {
		trivia.markCorrect( queueIndex, caller, operator );
		System.out.println( "Item " + queueIndex + " in the queue is correct, "
				+ trivia.getValue( trivia.getRoundNumber(), getAnswerQueueQNumbers()[queueIndex] ) + " points earned!" );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#markUncalled(int)
	 */
	public void markUncalled(int queueIndex) throws RemoteException {
		trivia.markUncalled( queueIndex );
		System.out.println( "Item " + queueIndex + " status reset to uncalled." );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#unsetSpeed()
	 */
	@WebMethod
	public void unsetSpeed() throws RemoteException {
		System.out.println( "Making round " + trivia.getRoundNumber() + " a normal round" );
		trivia.unsetSpeed();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#isCurrentSpeed()
	 */
	public boolean isCurrentSpeed() throws RemoteException {
		return trivia.isCurrentSpeed();
	}
	
	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#isSpeed(int)
	 */
	public boolean isSpeed(int rNumber) throws RemoteException {
		return trivia.isSpeed(rNumber);
	}


	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#isOpen(int)
	 */
	public boolean isOpen(int qNumber) throws RemoteException {
		return trivia.isOpen( qNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#isAnnounced(int)
	 */
	public boolean isAnnounced(int rNumber) throws RemoteException {
		return trivia.isAnnounced( rNumber );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#roundOver()
	 */
	public boolean roundOver() throws RemoteException {
		return trivia.roundOver();
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#open(int, int, java.lang.String)
	 */
	public void open(int qNumber, int qValue, String question) throws RemoteException {
		trivia.open( qNumber, qValue, question );
		System.out.println( "Question " + qNumber + " opened for " + qValue + " Points:" );
		System.out.println( question );
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.server.TriviaInterface#close(int)
	 */
	public void close(int qNumber) throws RemoteException {
		trivia.close( qNumber );
		System.out.println( "Question " + qNumber + " closed, " + getValue( this.getRoundNumber(), qNumber )
				+ " points earned." );
	}

	/**
	 * Test.
	 */
	public void test() {
		try {
			String[] timestamps = getAnswerQueueTimestamps();
			for ( int i = 0; i < timestamps.length; i++ ) {
				System.out.println( timestamps[i] );
			}
		} catch ( Exception e ) {
			e.getStackTrace();
		}

	}

}
