package net.bubbaland.trivia;

import java.io.IOException;

import org.joda.time.DateTime;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

public class DateTimeSerializer extends StdScalarSerializer<DateTime> {

	private static final long serialVersionUID = 9026743660436634486L;

	public DateTimeSerializer() {
		super(DateTime.class);
	}

	protected DateTimeSerializer(Class<DateTime> t) {
		super(t);
	}

	@Override
	public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeStringField("date-time", value + "");
		jgen.writeEndObject();
	}
}
