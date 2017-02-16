package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.trivia.Trivia;

public class TriviaDataMessage extends Message {

	@JsonProperty("trivia")
	final Trivia trivia;

	/**
	 * @return the trivia
	 */
	public Trivia getTrivia() {
		return this.trivia;
	}

	@JsonCreator
	public TriviaDataMessage(@JsonProperty("trivia") Trivia trivia) {
		this.trivia = trivia;
	}

}
