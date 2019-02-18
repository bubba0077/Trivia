package net.bubbaland.trivia.messages;

import java.io.IOException;
import java.io.StringWriter;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class Message {

	@JsonCreator
	public Message() {}

	protected static JsonFactory jsonFactory = new JsonFactory();

	public static class MessageEncoder implements Encoder.Text<Message> {
		@Override
		public void init(final EndpointConfig config) {}

		@Override
		public String encode(final Message message) throws EncodeException {
			// System.out.println("Encoding ClientMessage with command " + message.command);
			final StringWriter writer = new StringWriter();
			final ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.setVisibility(mapper.getVisibilityChecker().with(JsonAutoDetect.Visibility.NONE));
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
		public void destroy() {}
	}

	public static class MessageDecoder implements Decoder.Text<Message> {

		@Override
		public void init(final EndpointConfig config) {}

		@Override
		public Message decode(final String str) throws DecodeException {
			// System.out.println("Decoding ClientMessage");
			final ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.setVisibility(mapper.getVisibilityChecker().with(JsonAutoDetect.Visibility.NONE));
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			Message message = null;
			try {
				message = mapper.readValue(str, Message.class);
			} catch (final IOException exception) {
				exception.printStackTrace();
			}
			// System.out.println("Decoded ClientMessage with command " + message.getCommand());
			return message;
		}


		@Override
		public boolean willDecode(final String str) {
			return true;
		}


		@Override
		public void destroy() {}
	}

}
