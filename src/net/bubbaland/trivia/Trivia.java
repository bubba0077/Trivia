package net.bubbaland.trivia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The primary data structure for the trivia contest.
 *
 * The <code>Trivia</code> class holds all of the data for a trivia contest. There are a number of fields representing
 * different parameters of the contest, and an array of <code>Round</code>s that hold data for individual rounds.
 *
 * @author Walter Kolczynski
 */
public class Trivia implements Serializable {

	private static final long	serialVersionUID	= -1849743738638088417L;

	// The team name
	@JsonProperty("teamName")
	final private String		teamName;

	// The team number
	@JsonProperty("teamNumber")
	private int					teamNumber;

	// The number of rounds
	@JsonProperty("nRounds")
	final private int			nRounds;

	// The number of questions in a normal round
	@JsonProperty("nQuestionsNormal")
	final private int			nQuestionsNormal;

	// The number of questions in a speed round
	@JsonProperty("nQuestionsSpeed")
	final private int			nQuestionsSpeed;

	// The maximum number of questions allowed
	@JsonProperty("nQuestionsMax")
	final private int			nQuestionsMax;

	// Number of visual trivias
	@JsonProperty("nVisual")
	private volatile int		nVisual;

	// The current round
	@JsonProperty("rNumber")
	private volatile int		currentRoundNumber;

	// Array of all the rounds in the contest
	@JsonProperty("rounds")
	private volatile Round[]	rounds;

	/**
	 * Creates a new trivia contest
	 *
	 * @param nRounds
	 *            The number of rounds
	 * @param nQuestions
	 *            The number of questions in a normal round
	 * @param nQuestionsSpeed
	 *            The number of questions in a speed round
	 * @param nQuestionsMax
	 */
	public Trivia(String teamName, int teamNumber, int nRounds, int nQuestions, int nQuestionsSpeed,
			int nQuestionsMax) {
		this.teamName = teamName;
		this.teamNumber = teamNumber;
		this.nRounds = nRounds;
		this.nQuestionsNormal = nQuestions;
		this.nQuestionsSpeed = nQuestionsSpeed;
		this.nQuestionsMax = nQuestionsMax;
		this.rounds = new Round[nRounds];
		IntStream.rangeClosed(1, nRounds).parallel()
				.forEach(r -> this.rounds[r - 1] = new Round(r, nQuestions, nQuestionsSpeed));
		this.currentRoundNumber = 1;
		this.nVisual = 1;
	}

	@JsonCreator
	private Trivia(@JsonProperty("teamName") String teamName, @JsonProperty("teamNumber") int teamNumber,
			@JsonProperty("nRounds") int nRounds, @JsonProperty("nQuestionsNormal") int nQuestionsNormal,
			@JsonProperty("nQuestionsSpeed") int nQuestionsSpeed, @JsonProperty("nQuestionsMax") int nQuestionsMax,
			@JsonProperty("nVisual") int nVisuals, @JsonProperty("rNumber") int rNumber,
			@JsonProperty("rounds") Round[] rounds) {
		this.teamName = teamName;
		this.teamNumber = teamNumber;
		this.nRounds = nRounds;
		this.nQuestionsNormal = nQuestionsNormal;
		this.nQuestionsSpeed = nQuestionsSpeed;
		this.nQuestionsMax = nQuestionsMax;
		this.currentRoundNumber = rNumber;
		this.rounds = rounds;
		this.nVisual = nVisuals;
	}

	/**
	 * Get rounds that have changed. This is the primary method for retrieving updated data from the server.
	 *
	 * @param oldVersions
	 *            The round version numbers the user has.
	 * @return An array of all the rounds that have newer versions.
	 */
	public Round[] getChangedRounds(int[] oldVersions) {
		return Arrays.stream(this.rounds).parallel().filter(r -> oldVersions[r.getRoundNumber() - 1] != r.getVersion())
				.toArray(Round[]::new);
	}

	/**
	 * Gets the cumulative points earned through a round
	 *
	 * @param rNumber
	 *            The round number
	 * @return The cumulative number of points earned
	 */
	public int getCumulativeEarned(int rNumber) {
		return Arrays.stream(this.rounds).parallel().mapToInt(r -> r.getEarned()).sum();
	}

	/**
	 * Gets the total points earned for the contest.
	 *
	 * @return The number of points earned
	 */
	public int getEarned() {
		return this.getCumulativeEarned(currentRoundNumber);
	}

	/**
	 * Gets the cumulative value of questions through a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @return The cumulative value
	 */
	public int getCumulativeValue(int rNumber) {
		return Arrays.stream(this.rounds).parallel().mapToInt(r -> r.getValue()).sum();
	}

	/**
	 * Gets the current round number.
	 *
	 * @return The current round number
	 */
	public int getCurrentRoundNumber() {
		return this.currentRoundNumber;
	}

	public Round getCurrentRound() {
		return this.rounds[this.currentRoundNumber - 1];
	}

	/**
	 * Gets the number of questions in a speed round.
	 *
	 * @return The number of questions
	 */
	public int getMaxQuestions() {
		return this.nQuestionsSpeed;
	}

	/**
	 * Gets the number of rounds.
	 *
	 * @return The number of rounds
	 */
	public int getNRounds() {
		return this.nRounds;
	}

	public int getLastAnnounced() {
		return Arrays.stream(this.rounds).filter(r -> r.isAnnounced()).mapToInt(r -> r.getRoundNumber()).max()
				.orElse(0);
	}

	/**
	 * Gets the number of teams in the trivia contest
	 *
	 * @return The number of teams
	 */
	public int getNTeams() {
		ScoreEntry[] round1Standings = this.getRound(1).getStandings();
		return round1Standings != null ? round1Standings.length : 1;
	}

	/**
	 * Gets the team name.
	 *
	 * @return The team name
	 */
	public String getTeamName() {
		return this.teamName;
	}

	public int getTeamNumber() {
		return this.teamNumber;
	}

	public void setTeamNumber(int teamNumber) {
		this.teamNumber = teamNumber;
	}

	/**
	 * Gets the total value of all questions in the contest.
	 *
	 * @return The total value
	 */
	public int getValue() {
		return this.getCumulativeValue(this.currentRoundNumber);
	}

	public Round getRound(int rNumber) {
		if (rNumber < 1 || rNumber > this.nRounds) {
			return null;
		}
		return this.rounds[rNumber - 1];
	}

	public Round[] getRounds() {
		return this.rounds;
	}

	private void setRound(Round r) {
		this.rounds[r.getRoundNumber() - 1] = r;
	}

	/**
	 * Get the version of each Round.
	 *
	 * @return The version number for each round
	 */
	public int[] getVersions() {
		return Arrays.stream(this.rounds).parallel().mapToInt(r -> r.getVersion()).toArray();
	}

	public ArrayList<ScoreEntry[]> getFullStandings() {
		ArrayList<ScoreEntry[]> fullStandings = new ArrayList<ScoreEntry[]>();
		for (int r = 1; r <= this.getLastAnnounced(); r++) {
			fullStandings.add(this.getRound(r).getStandings());
		}
		return fullStandings;
	}

	public boolean standingsDifferent(ArrayList<ScoreEntry[]> oldStandings) {
		final ArrayList<ScoreEntry[]> standings = this.getFullStandings();
		final int lastAnnounced = standings.size();
		return oldStandings.size() != lastAnnounced
				|| !IntStream.range(0, lastAnnounced).parallel().filter(r -> oldStandings.get(r) != null)
						.allMatch(r -> IntStream.range(0, standings.size()).parallel()
								.filter(t -> oldStandings.get(r)[t] != null)
								.allMatch(t -> oldStandings.get(r)[t].equals(standings.get(r)[t])));
	}

	/**
	 * Start a new round.
	 */
	public void newRound() {
		if (this.getCurrentRound().nOpen() == 0 && this.getCurrentRound().nUnopened() == 0
				&& this.currentRoundNumber + 1 <= this.nRounds) {
			this.currentRoundNumber++;
		}
	}

	/**
	 * Reset the entire trivia contest.
	 */
	public void reset() {
		Arrays.parallelSetAll(this.rounds,
				(index) -> new Round(index + 1, Trivia.this.nQuestionsNormal, Trivia.this.nQuestionsSpeed));
		this.currentRoundNumber = 1;
	}

	/**
	 * Change the current round.
	 *
	 * @param rNumber
	 *            The new current round number
	 */
	public void setCurrentRoundNumber(int rNumber) {
		this.currentRoundNumber = rNumber;
	}

	public void setNVisual(int nVisual) {
		this.nVisual = nVisual;
	}

	public int getNVisual() {
		return this.nVisual;
	}

	public boolean[] getVisualTriviaUsed() {
		boolean[] visualTriviaUsed = new boolean[this.nVisual];


		Arrays.stream(rounds).parallel().filter(r -> r != null)
				.forEach(r -> Arrays.stream(r.getQuestions()).parallel().forEach(q -> {
					if (q.getVisualTrivia() != 0 && q.getVisualTrivia() <= this.nVisual) {
						visualTriviaUsed[q.getVisualTrivia() - 1] = true;
					}
				}));
		return visualTriviaUsed;
	}

	public ArrayList<String> getOperators() {
		ArrayList<String> allAnswers = new ArrayList<String>();
		Arrays.stream(rounds).forEach(r -> Arrays.stream(r.getAnswerQueue()).parallel().forEach(a -> {
			allAnswers.add(a.getOperator());
		}));
		return allAnswers.stream().distinct().filter(s -> s != null).sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.toCollection(ArrayList<String>::new));
	}

	/**
	 * Replace rounds with newer version retrieved from server.
	 *
	 * @param newRounds
	 *            The rounds with updated versions
	 */
	public synchronized void updateRounds(Round[] newRounds) {
		Arrays.stream(newRounds).parallel().forEach(r -> this.setRound(r));
	}

	public void changeUserName(String oldName, String newName) {
		Arrays.stream(this.rounds).parallel().forEach(r -> r.changeUserName(oldName, newName));
	}

	public String toString() {
		String s = "Data dump for entire Trivia object\n";
		s = s + "Team Name: " + this.teamName + "  #Teams: " + this.getNTeams() + "\n";
		s = s + "nRounds: " + this.nRounds + " nQuestions: " + this.nQuestionsNormal + "nQuestions(speed): "
				+ this.nQuestionsSpeed + "\n";
		s = s + "Current round: " + this.currentRoundNumber + "\n";
		for (Round r : this.rounds) {
			s = s + r.toString();
		}
		return s;
	}
}
