package net.bubbaland.trivia;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.bubbaland.trivia.User.Role;

@SuppressWarnings("unused")
public class ServerMessage {

	// public static class Coder extends JSONCoder<ServerMessage> {
	// }

	protected static JsonFactory jsonFactory = new JsonFactory();

	public static enum ServerCommand {
		UPDATE_R_NUMBER, UPDATE_ROUND, UPDATE_TRIVIA, UPDATE_USER_LISTS, LIST_SAVES
	};

	private ServerCommand	command;
	private int				rNumber;
	private Round[]			rounds;
	private User[]			userList;
	private Trivia			trivia;
	private String[]		saves;

	private ServerMessage() {
	}

	private ServerMessage(ServerCommand command) {
		this.command = command;
	}

	/**
	 * @return the rNumber
	 */
	public int getRNumber() {
		return this.rNumber;
	}

	/**
	 * @param rNumber
	 *            the rNumber to set
	 */
	private void setRNumber(int rNumber) {
		this.rNumber = rNumber;
	}

	/**
	 * @return the rounds
	 */
	public Round[] getRounds() {
		return this.rounds;
	}

	/**
	 * @param rounds
	 *            the rounds to set
	 */
	private void setRounds(Round[] rounds) {
		this.rounds = rounds;
	}

	/**
	 * @return the activeUserList
	 */
	public User[] getUserList() {
		return this.userList;
	}

	/**
	 * @return the trivia
	 */
	public Trivia getTrivia() {
		return this.trivia;
	}

	/**
	 * @param trivia
	 *            the trivia to set
	 */
	private void setTrivia(Trivia trivia) {
		this.trivia = trivia;
	}

	/**
	 * @return the command
	 */
	public ServerCommand getCommand() {
		return this.command;
	}

	public String[] getSaves() {
		return this.saves;
	}

	private void setSaves(String[] saves) {
		this.saves = saves;
	}

	public static class ServerMessageFactory {

		public static ServerMessage updateRoundNumber(int rNumber) {
			final ServerMessage message = new ServerMessage(ServerCommand.UPDATE_R_NUMBER);
			message.setRNumber(rNumber);
			return message;
		}

		public static ServerMessage updateRounds(Round[] newRounds) {
			final ServerMessage message = new ServerMessage(ServerCommand.UPDATE_ROUND);
			message.setRounds(newRounds);
			return message;
		}

		public static ServerMessage updateTrivia(Trivia trivia) {
			final ServerMessage message = new ServerMessage(ServerCommand.UPDATE_TRIVIA);
			message.setTrivia(trivia);
			return message;
		}

		public static ServerMessage updateUserList(User[] userList) {
			final ServerMessage message = new ServerMessage(ServerCommand.UPDATE_USER_LISTS);
			message.setUserList(userList);
			return message;
		}

		public static ServerMessage sendSaveList(String[] savefiles) {
			final ServerMessage message = new ServerMessage(ServerCommand.LIST_SAVES);
			message.setSaves(savefiles);
			return message;
		}

	}

	public static class MessageEncoder implements Encoder.Text<ServerMessage> {
		@Override
		public void init(final EndpointConfig config) {
		}

		@Override
		public String encode(final ServerMessage message) throws EncodeException {
			// System.out.println("Encoding ServerMessage with command " + message.command);
			final StringWriter writer = new StringWriter();
			final ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.setVisibilityChecker(mapper.getVisibilityChecker().with(JsonAutoDetect.Visibility.NONE));
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			JsonGenerator jsonGen;
			try {
				jsonGen = jsonFactory.createGenerator(writer);
				mapper.writeValue(jsonGen, message);
			} catch (final IOException exception) {
				exception.printStackTrace();
			}
			return writer.toString();
		}

		@Override
		public void destroy() {
		}
	}

	public static class MessageDecoder implements Decoder.Text<ServerMessage> {

		@Override
		public void init(final EndpointConfig config) {
		}

		@Override
		public ServerMessage decode(final String str) throws DecodeException {
			// System.out.println("Decoding ServerMessage");
			final ObjectMapper mapper = new ObjectMapper();
			// mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.setVisibilityChecker(mapper.getVisibilityChecker().with(JsonAutoDetect.Visibility.NONE));
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			ServerMessage message = null;
			try {
				message = mapper.readValue(str, ServerMessage.class);
			} catch (final IOException exception) {
				exception.printStackTrace();
			}
			// System.out.println("Decoded ServerMessage with command " + message.getCommand());
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

	private void setUserList(User[] userList) {
		this.userList = userList;
	}

}
