package net.bubbaland.trivia;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A data structure for the announced score and place of one team.
 *
 * @author Walter Kolczynski
 *
 */

public class ScoreEntry implements Comparable<ScoreEntry>, Serializable {

	private static final long	serialVersionUID	= -6052352344375555126L;

	@JsonProperty("teamName")
	private final String		teamName;
	@JsonProperty("score")
	private final int			score;
	@JsonProperty("place")
	private final int			place;

	@JsonCreator
	public ScoreEntry(@JsonProperty("teamName") String teamName, @JsonProperty("score") int score,
			@JsonProperty("place") int place) {
		this.teamName = teamName;
		this.score = score;
		this.place = place;
	}

	@Override
	public int compareTo(ScoreEntry o) {
		return this.teamName.compareTo(o.getTeamName());
	}

	public int getPlace() {
		return this.place;
	}

	public int getScore() {
		return this.score;
	}

	public String getTeamName() {
		return this.teamName;
	}

	public String toString() {
		return String.format("%1$3d %2$60s %3$05d", this.place, this.teamName, this.score);
	}

}
