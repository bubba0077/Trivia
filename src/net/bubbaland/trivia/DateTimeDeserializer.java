
package net.bubbaland.trivia;

import java.io.IOException;

import org.joda.time.DateTime;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

public class DateTimeDeserializer extends StdScalarDeserializer<DateTime> {
	private static final long serialVersionUID = -4897819298997898055L;

	public DateTimeDeserializer() {
		super(DateTime.class);
	}

	protected DateTimeDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public DateTime deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		String fieldName = null;
		String dateTimeStr = null;

		while (jp.hasCurrentToken()) {
			JsonToken token = jp.nextToken();
			if (token == JsonToken.FIELD_NAME) {
				fieldName = jp.getCurrentName();
			} else if (token == JsonToken.VALUE_STRING) {
				if (fieldName.equals("date-time")) {
					dateTimeStr = jp.getValueAsString();
				} else {
					throw new JsonParseException("Unexpected field name", jp.getTokenLocation());
				}
			} else if (token == JsonToken.END_OBJECT) {
				break;
			}
		}

		if (dateTimeStr != null) {
			DateTime dateTime = DateTime.parse(dateTimeStr);
			return dateTime;
		}
		return null;
	}
}
