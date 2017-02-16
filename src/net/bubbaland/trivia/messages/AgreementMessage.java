package net.bubbaland.trivia.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.bubbaland.trivia.Answer.Agreement;

public class AgreementMessage extends AnswerMessage {

	/**
	 * @return the agreement
	 */
	public Agreement getAgreement() {
		return this.agreement;
	}

	@JsonProperty("agreement")
	final Agreement agreement;

	@JsonCreator
	public AgreementMessage(@JsonProperty("roundNumber") int roundNumber, @JsonProperty("queueIndex") int queueIndex,
			@JsonProperty("agreement") Agreement agreement) {
		super(roundNumber, queueIndex);
		this.agreement = agreement;
	}

}
