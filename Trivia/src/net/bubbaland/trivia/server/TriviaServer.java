package net.bubbaland.trivia.server;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.swing.JComboBox;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.bubbaland.trivia.Answer;
import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaInterface;
import net.bubbaland.trivia.client.CorrectEntryPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The Class TriviaServer.
 */
@WebService
public class TriviaServer extends UnicastRemoteObject implements TriviaInterface, ActionListener {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -8062985452301507239L;
	
	/** The Constant N_ROUNDS. */
	private static final int	N_ROUNDS			= 50;
	
	/** The Constant N_QUESTIONS. */
	private static final int	N_QUESTIONS			= 18;
	
	/** The Constant N_NORMAL_Q. */
	private static final int	N_NORMAL_Q			= 9;
	
	private static final int	SAVE_FREQUENCY		= 5 * 60000;
	
	private static final String SAVE_DIR			= "saves";
	
	private static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy_MMM_dd_HHmm");
	private static final SimpleDateFormat stringDateFormat = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
	
	/** The trivia. */
	private Trivia				trivia;

	/**
	 * Instantiates a new trivia server.
	 *
	 * @throws RemoteException the remote exception
	 */
	public TriviaServer() throws RemoteException {
		this.trivia = new Trivia( N_ROUNDS, N_QUESTIONS, N_NORMAL_Q );
		
		loadState("saves/Rd01_2013_Oct_05_1100.xml");
		
		String[] saves = listSaves();
		for(String save : saves) {
			System.out.println(save);
		}
		
		// Create timer that will poll server for changes		
		Timer backupTimer = new Timer( SAVE_FREQUENCY, this );
		backupTimer.start();
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
	 * @see net.bubbaland.trivia.server.TriviaInterface#setDiscrepancyText(int, java.lang.String)
	 */
	public void setDiscrepancyText(int rNumber, String discrepancyText) throws RemoteException{
		trivia.setDiscrepencyText(rNumber, discrepancyText);
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
	
	private void saveState() {
		
		Date time = new Date();
		
		String filename = SAVE_DIR + "/Rd" + String.format("%02d", trivia.getRoundNumber() ) + "_" + fileDateFormat.format(time)+".xml";
		String createTime = stringDateFormat.format(time);
		
		try {			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			Document doc = docBuilder.newDocument();
			Element triviaElement = doc.createElement("Trivia");
			doc.appendChild(triviaElement);
			
			Attr attribute = doc.createAttribute("Save_Time");	
			attribute.setValue(createTime);
			triviaElement.setAttributeNode(attribute);			
			
			Element element = doc.createElement("Number_of_Teams");			
			element.appendChild(doc.createTextNode(trivia.getNTeams() + ""));
			triviaElement.appendChild(element);
			
			element = doc.createElement("Current_Round");			
			element.appendChild(doc.createTextNode(trivia.getRoundNumber() + ""));
			triviaElement.appendChild(element);
			
			int nRounds = trivia.getNRounds();
			
			for(int r=0; r<trivia.getRoundNumber(); r++) {
				Element roundElement = doc.createElement("Round");
				triviaElement.appendChild(roundElement);
				
				attribute = doc.createAttribute("number");	
				attribute.setValue( (r+1) + "" );
				roundElement.setAttributeNode(attribute);		
				
				element = doc.createElement("Speed");
				element.appendChild( doc.createTextNode( trivia.isSpeed(r+1) + "" ) );
				roundElement.appendChild(element);
				
				element = doc.createElement("Announced");			
				element.appendChild( doc.createTextNode( trivia.isAnnounced(r+1) + "" ) );
				roundElement.appendChild(element);
				
				element = doc.createElement("Announced_Score");			
				element.appendChild( doc.createTextNode( trivia.getAnnouncedPoints(r+1) + "" ) );
				roundElement.appendChild(element);
				
				element = doc.createElement("Announced_Place");
				element.appendChild( doc.createTextNode( trivia.getAnnouncedPlace(r+1) + "" ) );
				roundElement.appendChild(element);
				
				element = doc.createElement("Discrepancy_Text");			
				element.appendChild( doc.createTextNode( trivia.getDiscrepencyText(r+1) + "" ) );
				roundElement.appendChild(element);
				
				for(int q=0; q<trivia.getNQuestions(r+1); q++) {
					Element questionElement = doc.createElement("Question");
					roundElement.appendChild(questionElement);
					
					attribute = doc.createAttribute("number");	
					attribute.setValue( (q+1) + "" );
					questionElement.setAttributeNode(attribute);
					
					element = doc.createElement("Been_Open");
					element.appendChild( doc.createTextNode( trivia.beenOpen( r+1, q+1) + "") );
					questionElement.appendChild(element);
					
					element = doc.createElement("Is_Open");			
					element.appendChild( doc.createTextNode( trivia.isOpen( r+1, q+1) + "") );
					questionElement.appendChild(element);
					
					element = doc.createElement("Value");			
					element.appendChild( doc.createTextNode( trivia.getValue( r+1, q+1) + "") );
					questionElement.appendChild(element);
					
					element = doc.createElement("Question_Text");			
					element.appendChild( doc.createTextNode( trivia.getQuestionText( r+1, q+1) + "") );
					questionElement.appendChild(element);
					
					element = doc.createElement("Answer_Text");
					element.appendChild( doc.createTextNode( trivia.getAnswerText( r+1, q+1) + "") );
					questionElement.appendChild(element);
					
					element = doc.createElement("Is_Correct");			
					element.appendChild( doc.createTextNode( trivia.isCorrect( r+1, q+1) + "") );
					questionElement.appendChild(element);
					
					element = doc.createElement("Submitter");			
					element.appendChild( doc.createTextNode( trivia.getSubmitter( r+1, q+1) + "") );
					questionElement.appendChild(element);
					
					element = doc.createElement("Operator");			
					element.appendChild( doc.createTextNode( trivia.getOperator( r+1, q+1) + "") );
					questionElement.appendChild(element);
				}				
			}
			
			int queueSize = trivia.getAnswerQueueSize();			
			
			Element queueElement = doc.createElement("Answer_Queue");
			triviaElement.appendChild(queueElement);
			
			attribute = doc.createAttribute("size");	
			attribute.setValue( queueSize + "" );
			queueElement.setAttributeNode(attribute);
			
			for(int a=0; a<queueSize; a++) {
				Element answerElement = doc.createElement("Proposed_Answer");
				queueElement.appendChild(answerElement);
				
				element = doc.createElement("Question_Number");
				element.appendChild( doc.createTextNode( trivia.getAnswerQueueQNumber(a) + "" ) );
				answerElement.appendChild(element);
				
				element = doc.createElement("Status");
				element.appendChild( doc.createTextNode( trivia.getAnswerQueueStatus(a) ) );
				answerElement.appendChild(element);
				
				element = doc.createElement("Timestamp");
				element.appendChild( doc.createTextNode( trivia.getAnswerQueueTimestamp(a) ) );
				answerElement.appendChild(element);
				
				element = doc.createElement("Answer_Text");
				element.appendChild( doc.createTextNode( trivia.getAnswerQueueAnswer(a) ) );
				answerElement.appendChild(element);				
				
				element = doc.createElement("Submitter");
				element.appendChild( doc.createTextNode( trivia.getAnswerQueueSubmitter(a) ) );
				answerElement.appendChild(element);
				
				element = doc.createElement("Confidence");
				element.appendChild( doc.createTextNode( trivia.getAnswerQueueConfidence(a) + "" ) );
				answerElement.appendChild(element);
				
				element = doc.createElement("Caller");
				element.appendChild( doc.createTextNode( trivia.getAnswerQueueCaller(a) ) );
				answerElement.appendChild(element);
				
				element = doc.createElement("Operator");
				element.appendChild( doc.createTextNode( trivia.getAnswerQueueOperator(a) ) );
				answerElement.appendChild(element);				
				
			}	
			
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filename));
			
			transformer.transform(source, result);
			
			System.out.println("Saved state to " + filename );
			
		} catch (ParserConfigurationException e) {
			System.out.println("Couldn't save data to file " + filename);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}		
		
	}
	
	public synchronized void loadState(String stateFile) {
		trivia.reset();
		try {
			File infile = new File(stateFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(infile);
		 
			doc.getDocumentElement().normalize();
			
			Element triviaElement = doc.getDocumentElement();
						
			trivia.setNTeams(Integer.parseInt(triviaElement.getElementsByTagName("Number_of_Teams").item(0).getTextContent()));
			trivia.setCurrentRound(Integer.parseInt(triviaElement.getElementsByTagName("Current_Round").item(0).getTextContent()));
			
			NodeList roundElements = triviaElement.getElementsByTagName("Round");
			
			for(int r=0; r<roundElements.getLength(); r++) {
				Element roundElement = (Element) roundElements.item(r);
				int rNumber = Integer.parseInt(roundElement.getAttribute("number"));
				
				boolean speed = roundElement.getElementsByTagName("Speed").item(0).getTextContent().equals("true");
				if(speed) {
					trivia.setSpeed(rNumber);
				}
				
				boolean announced = roundElement.getElementsByTagName("Announced").item(0).getTextContent().equals("true");
				int announcedPoints = Integer.parseInt(roundElement.getElementsByTagName("Announced_Score").item(0).getTextContent());
				int announcedPlace = Integer.parseInt(roundElement.getElementsByTagName("Announced_Place").item(0).getTextContent());
				if(announced) {
					trivia.setAnnounced(rNumber, announcedPoints, announcedPlace);
				}
				trivia.setDiscrepencyText(rNumber, roundElement.getElementsByTagName("Discrepancy_Text").item(0).getTextContent());
				
				NodeList questionElements = roundElement.getElementsByTagName("Question");
				
				for(int q=0; q<questionElements.getLength(); q++) {
					Element questionElement = (Element) questionElements.item(q);
					int qNumber = Integer.parseInt(questionElement.getAttribute("number"));

					boolean beenOpen = questionElement.getElementsByTagName("Been_Open").item(0).getTextContent().equals("true");
					boolean isOpen = questionElement.getElementsByTagName("Is_Open").item(0).getTextContent().equals("true");
					boolean isCorrect = questionElement.getElementsByTagName("Is_Correct").item(0).getTextContent().equals("true");
					int value = Integer.parseInt(questionElement.getElementsByTagName("Value").item(0).getTextContent());
					String question = questionElement.getElementsByTagName("Question_Text").item(0).getTextContent();
					String answer = questionElement.getElementsByTagName("Answer_Text").item(0).getTextContent();
					String submitter = questionElement.getElementsByTagName("Submitter").item(0).getTextContent();
					String operator = questionElement.getElementsByTagName("Operator").item(0).getTextContent();

					if(beenOpen) {
						trivia.open(rNumber, qNumber, value, question);
						
						if(isCorrect) {
							trivia.markCorrect(rNumber, qNumber, answer, submitter, operator);
						} else if (!isOpen) {
							trivia.close(rNumber, qNumber);
						}						
					}					
				}			
			}
			
			Element element = (Element) triviaElement.getElementsByTagName("Answer_Queue").item(0);
			NodeList answerElements = element.getElementsByTagName("Proposed_Answer");
			
			System.out.println(answerElements.getLength());

			for(int a=0; a<answerElements.getLength(); a++) {
				Element answerElement = (Element) answerElements.item(a);
				
				int qNumber = Integer.parseInt(answerElement.getElementsByTagName("Question_Number").item(0).getTextContent());
				String status = answerElement.getElementsByTagName("Status").item(0).getTextContent();
				String timestamp = answerElement.getElementsByTagName("Timestamp").item(0).getTextContent();
				String answer = answerElement.getElementsByTagName("Answer_Text").item(0).getTextContent();
				String submitter = answerElement.getElementsByTagName("Submitter").item(0).getTextContent();
				int confidence = Integer.parseInt(answerElement.getElementsByTagName("Confidence").item(0).getTextContent());
				String caller = answerElement.getElementsByTagName("Caller").item(0).getTextContent();
				String operator = answerElement.getElementsByTagName("Operator").item(0).getTextContent();
				
				trivia.proposeAnswer(qNumber, answer, submitter, confidence);
				
				switch ( status ) {
				case "Not Called In":
					trivia.markUncalled( a );
					break;
				case "Calling":
					trivia.callIn( a, caller );
					break;
				case "Incorrect":
					trivia.markIncorrect( a, caller );
					break;
				case "Partial":
					trivia.markPartial( a, caller);
					break;
				case "Correct":
					trivia.markCorrect( a, caller, operator);
					break;
				default:
					break;
				}
				
			}
			
						
		} catch ( ParserConfigurationException e) {
			
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Loaded state from " + stateFile);
			
	}
	
	public String[] listSaves() {
		File folder = new File(SAVE_DIR);
		File[] files = folder.listFiles();
		int nFiles = files.length;
		String[] filenames = new String[nFiles];
		for(int f=0; f<nFiles; f++) {
			filenames[f] = files[f].getName();			
		}
		Arrays.sort(filenames, Collections.reverseOrder());
		return filenames;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.saveState();		
	}
	

}
