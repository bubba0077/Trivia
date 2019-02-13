package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetNQuestionsMessage extends Message {

	@JsonProperty("rNumber")
	final int	rNumber;
	@JsonProperty("nQuestions")
	final int	nQuestions;

	public int getRoundNumber() {
		return this.rNumber;
	}

	public int getNQuestions() {
		return this.nQuestions;
	}

	@JsonCreator
	public SetNQuestionsMessage(@JsonProperty("rNumber") int rNumber, @JsonProperty("nQuestions") int nQuestions) {
		this.rNumber = rNumber;
		this.nQuestions = nQuestions;
	}

}
