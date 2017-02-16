package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetOperatorMessage extends AnswerMessage {

	@JsonProperty("operator")
	final String operator;

	/**
	 * @return the operator
	 */
	public String getOperator() {
		return this.operator;
	}

	@JsonCreator
	public SetOperatorMessage(@JsonProperty("roundNumber") int roundNumber, @JsonProperty("queueIndex") int queueIndex,
			@JsonProperty("operator") String operator) {
		super(roundNumber, queueIndex);
		this.operator = operator;
	}

}
