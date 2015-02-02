package net.bubbaland.trivia;

import java.io.IOException;
import java.io.StringWriter;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;

import net.bubbaland.trivia.Trivia.Role;

public class ClientMessage {

	// public static class Coder extends JSONCoder<ClientMessage> {
	// }

	protected static JsonFactory	jsonFactory	= new JsonFactory();

	public static enum ClientCommand {
		CALL_IN, CHANGE_USER, CLOSE_QUESTION, EDIT_QUESTION, LIST_SAVES, LOAD_STATE, MARK_CORRECT, MARK_DUPLICATE, MARK_INCORRECT, MARK_PARTIAL, MARK_UNCALLED, ADVANCE_ROUND, OPEN_QUESTION, REOPEN_QUESTION, PROPOSE_ANSWER, REMAP_QUESTION, RESET_QUESTION, SET_DISCREPENCY_TEXT, SET_ROLE, SET_SPEED, AGREE, DISAGREE, SET_IDLE_TIME, FETCH_TRIVIA
	};

	private ClientCommand	command;
	private int				rNumber, qNumber, oldQNumber, queueIndex, qValue, confidence, timeToIdle;
	private String			user, qText, aText, operator, saveFilename, discrepancyText;
	private boolean			correct, speed;
	private Role			role;

	private ClientMessage() {

	}

	private ClientMessage(ClientCommand command) {
		super();
		this.command = command;
	}

	/**
	 * @return the rNumber
	 */
	public int getrNumber() {
		return this.rNumber;
	}

	/**
	 * @return the qNumber
	 */
	public int getqNumber() {
		return this.qNumber;
	}

	/**
	 * @return the newQNumber
	 */
	public int getOldQNumber() {
		return this.oldQNumber;
	}

	/**
	 * @return the queueIndex
	 */
	public int getQueueIndex() {
		return this.queueIndex;
	}

	/**
	 * @return the qValue
	 */
	public int getqValue() {
		return this.qValue;
	}

	/**
	 * @return the confidence
	 */
	public int getConfidence() {
		return this.confidence;
	}

	/**
	 * @return the qText
	 */
	public String getqText() {
		return this.qText;
	}

	/**
	 * @return the aText
	 */
	public String getaText() {
		return this.aText;
	}

	/**
	 * @return the operator
	 */
	public String getOperator() {
		return this.operator;
	}

	/**
	 * @return the saveFilename
	 */
	public String getSaveFilename() {
		return this.saveFilename;
	}

	/**
	 * @return the discrepencyText
	 */
	public String getDiscrepancyText() {
		return this.discrepancyText;
	}

	/**
	 * @return the isCorrect
	 */
	public boolean isCorrect() {
		return this.correct;
	}

	/**
	 * @return the role
	 */
	public Role getRole() {
		return this.role;
	}

	/**
	 * @return the command
	 */
	public ClientCommand getCommand() {
		return this.command;
	}

	public boolean isSpeed() {
		return speed;
	}

	public int getTimeToIdle() {
		return timeToIdle;
	}

	public String getUser() {
		return this.user;
	}

	public static class ClientMessageFactory {

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
		public static ClientMessage callIn(int queueIndex) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.CALL_IN);
			message.queueIndex = queueIndex;
			return message;
		}

		/**
		 * Change a user's name.
		 * 
		 * @param oldUser
		 *            The old user name
		 * @param newUser
		 *            The new user name
		 * @throws RemoteException
		 */
		public static ClientMessage changeUser(String user) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.CHANGE_USER);
			message.user = user;
			return message;
		}

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
		public static ClientMessage close(int qNumber, String aText) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.CLOSE_QUESTION);
			message.qNumber = qNumber;
			message.aText = aText;
			return message;
		}

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
		public static ClientMessage editQuestion(int rNumber, int qNumber, int qValue, String qText, String aText,
				String submitter, boolean isCorrect, String operator) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.EDIT_QUESTION);
			message.rNumber = rNumber;
			message.qNumber = qNumber;
			message.qValue = qValue;
			message.qText = qText;
			message.aText = aText;
			message.user = submitter;
			message.correct = isCorrect;
			message.operator = operator;
			return message;
		}

		/**
		 * Gets a list of available saves.
		 * 
		 * @return Array of save file names
		 * @throws RemoteException
		 *             A remote exception
		 */
		public static ClientMessage listSaves() {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.LIST_SAVES);
			return message;
		}

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
		public static ClientMessage loadState(String saveFilename) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.LOAD_STATE);
			message.saveFilename = saveFilename;
			return message;
		}

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
		public static ClientMessage markCorrect(int queueIndex, String operator) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.MARK_CORRECT);
			message.queueIndex = queueIndex;
			message.operator = operator;
			return message;
		}

		/**
		 * Mark as duplicate.
		 * 
		 * @param user
		 *            The user's name
		 * @param queueIndex
		 *            The location of the answer in the queue
		 * 
		 * @throws RemoteException
		 *             A remote exception
		 */
		public static ClientMessage markDuplicate(int queueIndex) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.MARK_DUPLICATE);
			message.queueIndex = queueIndex;
			return message;
		}

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
		public static ClientMessage markIncorrect(int queueIndex) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.MARK_INCORRECT);
			message.queueIndex = queueIndex;
			return message;
		}

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
		public static ClientMessage markPartial(int queueIndex) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.MARK_PARTIAL);
			message.queueIndex = queueIndex;
			return message;
		}

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
		public static ClientMessage markUncalled(int queueIndex) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.MARK_UNCALLED);
			message.queueIndex = queueIndex;
			return message;
		}

		/**
		 * Starts a new round.
		 * 
		 * @throws RemoteException
		 *             A remote exception
		 */
		public static ClientMessage advanceRound() {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.ADVANCE_ROUND);
			return message;
		}

		/**
		 * Open a question
		 * 
		 * @param user
		 *            The user's name
		 * @param qNumber
		 *            The question number
		 * @param qValue
		 * @param qText
		 * @param qValue
		 *            The question's value
		 * @param question
		 *            The question
		 * 
		 * @throws RemoteException
		 *             A remote exception
		 */
		public static ClientMessage open(int qNumber, String qText, int qValue) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.OPEN_QUESTION);
			message.qNumber = qNumber;
			message.qText = qText;
			message.qValue = qValue;
			return message;
		}

		public static ClientMessage reopen(int qNumber) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.REOPEN_QUESTION);
			message.qNumber = qNumber;
			return message;
		}

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
		public static ClientMessage proposeAnswer(int qNumber, String aText, int confidence) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.PROPOSE_ANSWER);
			message.qNumber = qNumber;
			message.aText = aText;
			message.confidence = confidence;
			return message;
		}

		/**
		 * Remap a question to a new number.
		 * 
		 * @param oldQNumber
		 *            The old question number
		 * @param newQNumber
		 *            The new question number
		 * @throws RemoteException
		 */
		public static ClientMessage remapQuestion(int oldQNumber, int newQNumber) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.REMAP_QUESTION);
			message.oldQNumber = oldQNumber;
			message.qNumber = newQNumber;
			return message;
		}

		/**
		 * Reset a question.
		 * 
		 * @param qNumber
		 *            The question number
		 * @throws RemoteException
		 */
		public static ClientMessage resetQuestion(int qNumber) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.RESET_QUESTION);
			message.qNumber = qNumber;
			return message;
		}

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
		public static ClientMessage setDiscrepancyText(int rNumber, String discrepancyText) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.SET_DISCREPENCY_TEXT);
			message.rNumber = rNumber;
			message.discrepancyText = discrepancyText;
			return message;
		}

		/**
		 * Change the user's role.
		 * 
		 * @param user
		 *            The user name
		 * @param role
		 *            The new role
		 * @throws RemoteException
		 */
		public static ClientMessage setRole(String user, Role role) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.SET_ROLE);
			message.user = user;
			message.role = role;
			return message;
		}

		/**
		 * Makes the current round a speed round.
		 * 
		 * @param user
		 *            The user making the change
		 * @throws RemoteException
		 *             A remote exception
		 */
		public static ClientMessage setSpeed(boolean isSpeed) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.SET_SPEED);
			message.speed = isSpeed;
			return message;
		}

		public static ClientMessage agree(int queueIndex) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.AGREE);
			message.queueIndex = queueIndex;
			return message;
		}

		public static ClientMessage disagree(int queueIndex) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.DISAGREE);
			message.queueIndex = queueIndex;
			return message;
		}

		public static ClientMessage setIdleTime(int timeToIdle) {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.SET_IDLE_TIME);
			message.timeToIdle = timeToIdle;
			return message;
		}

		public static ClientMessage fetchTrivia() {
			ClientMessage message = new ClientMessage(ClientMessage.ClientCommand.FETCH_TRIVIA);
			return message;
		}

	}

	public static class MessageEncoder implements Encoder.Text<ClientMessage> {
		@Override
		public void init(final EndpointConfig config) {
		}

		@Override
		public String encode(final ClientMessage message) throws EncodeException {
			System.out.println("Encoding ClientMessage with command " + message.command);
			StringWriter writer = new StringWriter();
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.setVisibilityChecker(mapper.getVisibilityChecker().with(JsonAutoDetect.Visibility.NONE));
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			JsonGenerator jsonGen;
			try {
				jsonGen = jsonFactory.createGenerator(writer);
				mapper.writeValue(jsonGen, message);
			} catch (IOException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			}
			return writer.toString();
		}

		@Override
		public void destroy() {
		}
	}

	public static class MessageDecoder implements Decoder.Text<ClientMessage> {

		@Override
		public void init(final EndpointConfig config) {
		}

		@Override
		public ClientMessage decode(final String str) throws DecodeException {
			System.out.println("Decoding ClientMessage");
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.setVisibilityChecker(mapper.getVisibilityChecker().with(JsonAutoDetect.Visibility.NONE));
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			ClientMessage message = null;
			try {
				message = mapper.readValue(str, ClientMessage.class);
			} catch (IOException exception) {
				// TODO Auto-generated catch block
				exception.printStackTrace();
			}
			System.out.println("Decoded ClientMessage with command " + message.getCommand());
			return message;
		}


		@Override
		public boolean willDecode(final String str) {
			return true;
		}


		@Override
		public void destroy() {
		}
	}


}
