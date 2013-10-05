package net.bubbaland.trivia.server;

import javax.jws.WebMethod;
import javax.jws.WebService;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaInterface;

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
				+ trivia.getValue( trivia.getRoundNumber(), trivia.getAnswerQueueQNumbers()[queueIndex] ) + " points earned!" );
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

//	/* (non-Javadoc)
//	 * @see net.bubbaland.trivia.server.TriviaInterface#isCurrentSpeed()
//	 */
//	public boolean isCurrentSpeed() throws RemoteException {
//		return trivia.isCurrentSpeed();
//	}
//	
//	/* (non-Javadoc)
//	 * @see net.bubbaland.trivia.server.TriviaInterface#isSpeed(int)
//	 */
//	public boolean isSpeed(int rNumber) throws RemoteException {
//		return trivia.isSpeed(rNumber);
//	}
//
//	/* (non-Javadoc)
//	 * @see net.bubbaland.trivia.server.TriviaInterface#isOpen(int)
//	 */
//	public boolean isOpen(int qNumber) throws RemoteException {
//		return trivia.isOpen( qNumber );
//	}
//
//	/* (non-Javadoc)
//	 * @see net.bubbaland.trivia.server.TriviaInterface#isAnnounced(int)
//	 */
//	public boolean isAnnounced(int rNumber) throws RemoteException {
//		return trivia.isAnnounced( rNumber );
//	}
//
//	/* (non-Javadoc)
//	 * @see net.bubbaland.trivia.server.TriviaInterface#roundOver()
//	 */
//	public boolean roundOver() throws RemoteException {
//		return trivia.roundOver();
//	}

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
		System.out.println( "Question " + qNumber + " closed, " + trivia.getValue( trivia.getRoundNumber(), qNumber )
				+ " points earned." );
	}
	
	public Trivia getTrivia() throws RemoteException {
		return trivia;
	}

//	/**
//	 * Test.
//	 */
//	public void test() {
//		try {
//			String[] timestamps = getAnswerQueueTimestamps();
//			for ( int i = 0; i < timestamps.length; i++ ) {
//				System.out.println( timestamps[i] );
//			}
//		} catch ( Exception e ) {
//			e.getStackTrace();
//		}
//
//	}

}
