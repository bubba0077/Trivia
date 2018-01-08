package net.bubbaland.trivia;

import java.io.Serializable;
import java.util.ArrayList;
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
	@JsonProperty("nQuestions")
	final private int			nQuestions;

	// The number of questions in a speed round
	@JsonProperty("nQuestionsSpeed")
	final private int			nQuestionsSpeed;

	// The number of teams in the contest
	@JsonProperty("nTeams")
	private volatile int		nTeams;

	// Number of visual trivias
	@JsonProperty("nVisual")
	private volatile int		nVisual;

	// The current round
	// private volatile Round currentRound;
	@JsonProperty("rNumber")
	private volatile int		rNumber;

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
	 */
	public Trivia(String teamName, int teamNumber, int nRounds, int nQuestions, int nQuestionsSpeed) {
		this.teamName = teamName;
		this.teamNumber = teamNumber;
		this.nRounds = nRounds;
		this.nQuestions = nQuestions;
		this.nQuestionsSpeed = nQuestionsSpeed;
		this.rounds = new Round[nRounds];
		for (int r = 0; r < nRounds; r++) {
			this.rounds[r] = new Round(r + 1, nQuestionsSpeed, nQuestions);
		}
		this.rNumber = 1;
		this.nTeams = 100;
		this.nVisual = 50;
	}

	@JsonCreator
	private Trivia(@JsonProperty("teamName") String teamName, @JsonProperty("teamNumber") int teamNumber,
			@JsonProperty("nRounds") int nRounds, @JsonProperty("nQuestions") int nQuestions,
			@JsonProperty("nQuestionsSpeed") int nQuestionsSpeed, @JsonProperty("nTeams") int nTeams,
			@JsonProperty("nVisual") int nVisuals, @JsonProperty("rNumber") int rNumber,
			@JsonProperty("rounds") Round[] rounds) {
		this.teamName = teamName;
		this.teamNumber = teamNumber;
		this.nRounds = nRounds;
		this.nQuestions = nQuestions;
		this.nQuestionsSpeed = nQuestionsSpeed;
		this.nTeams = nTeams;
		this.rNumber = rNumber;
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
		final ArrayList<Round> changedRoundList = new ArrayList<Round>(0);
		for (int r = 0; r < this.nRounds; r++) {
			final Round round = this.rounds[r];
			if (oldVersions[r] != round.getVersion()) {
				changedRoundList.add(round);
			}
		}
		final Round[] changedRounds = new Round[changedRoundList.size()];
		changedRoundList.toArray(changedRounds);
		return changedRounds;
	}

	/**
	 * Gets the cumulative points earned through a round
	 *
	 * @param rNumber
	 *            The round number
	 * @return The cumulative number of points earned
	 */
	public int getCumulativeEarned(int rNumber) {
		int earned = 0;
		for (int r = 0; r < rNumber; r++) {
			earned += this.rounds[r].getEarned();
		}
		return earned;
	}

	/**
	 * Gets the cumulative value of questions through a round.
	 *
	 * @param rNumber
	 *            The round number
	 * @return The cumulative value
	 */
	public int getCumulativeValue(int rNumber) {
		int value = 0;
		for (int r = 0; r < rNumber; r++) {
			value += this.rounds[r].getValue();
		}
		return value;
	}

	/**
	 * Gets the current round number.
	 *
	 * @return The current round number
	 */
	public int getCurrentRoundNumber() {
		return this.rNumber;
	}

	public Round getCurrentRound() {
		return this.rounds[this.rNumber - 1];
	}


	/**
	 * Gets the total points earned for the contest.
	 *
	 * @return The number of points earned
	 */
	public int getEarned() {
		int earned = 0;
		for (final Round r : this.rounds) {
			earned += r.getEarned();
		}
		return earned;
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
		int nAnnounced = 0;
		for (Round round : this.rounds) {
			if (round.isAnnounced()) {
				nAnnounced = round.getRoundNumber();
			}
		}
		return nAnnounced;
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
		int value = 0;
		for (final Round r : this.rounds) {
			value += r.getValue();
		}
		return value;
	}

	public Round getRound(int r) {
		return this.rounds[r - 1];
	}

	public Round[] getRounds() {
		return this.rounds;
	}

	/**
	 * Get the version of each Round.
	 *
	 * @return The version number for each round
	 */
	public int[] getVersions() {
		final int[] versions = new int[this.nRounds];
		for (int r = 0; r < this.nRounds; r++) {
			versions[r] = this.rounds[r].getVersion();
		}
		return versions;
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
				|| !IntStream.range(0, lastAnnounced).parallel().allMatch(r -> IntStream.range(0, standings.size())
						.parallel().allMatch(t -> oldStandings.get(r)[t].equals(standings.get(r)[t])));
	}

	/**
	 * Start a new round.
	 */
	public void newRound() {
		if (this.getCurrentRound().nOpen() == 0 && this.getCurrentRound().nUnopened() == 0
				&& this.rNumber + 1 <= this.nRounds) {
			this.rNumber++;
		}
	}

	/**
	 * Reset the entire trivia contest.
	 */
	public void reset() {
		for (int r = 0; r < this.nRounds; r++) {
			this.rounds[r] = new Round(r + 1, this.nQuestionsSpeed, this.nQuestions);
		}
	}

	/**
	 * Change the current round.
	 *
	 * @param rNumber
	 *            The new current round number
	 */
	public void setCurrentRound(int rNumber) {
		this.rNumber = rNumber;
	}

	public void setNVisual(int nVisual) {
		this.nVisual = nVisual;
	}

	public int getNVisual() {
		return this.nVisual;
	}

	public boolean[] getVisualTriviaUsed() {
		boolean[] visualTriviaUsed = new boolean[this.nVisual];
		for (Round r : rounds) {
			for (Question q : r.getQuestions()) {
				if (q.getVisualTrivia() != 0 && q.getVisualTrivia() <= this.nVisual) {
					visualTriviaUsed[q.getVisualTrivia() - 1] = true;
				}
			}
		}
		return visualTriviaUsed;
	}

	/**
	 * Replace rounds with newer version retrieved from server.
	 *
	 * @param newRounds
	 *            The rounds with updated versions
	 */
	public synchronized void updateRounds(Round[] newRounds) {
		final int nNew = newRounds.length;
		for (int r = 0; r < nNew; r++) {
			final Round newRound = newRounds[r];
			final int rNumber = newRound.getRoundNumber();
			this.rounds[rNumber - 1] = newRound;
			if (newRound.isAnnounced()) {
				this.nTeams = newRound.getStandings().length;
			}
		}
	}

	public void changeName(String oldName, String newName) {
		this.getCurrentRound().changeName(oldName, newName);
	}

	public String toString() {
		String s = "Data dump for entire Trivia object\n";
		s = s + "Team Name: " + this.teamName + "  #Teams: " + this.nTeams + "\n";
		s = s + "nRounds: " + this.nRounds + " nQuestions: " + this.nQuestions + "nQuestions(speed): "
				+ this.nQuestionsSpeed + "\n";
		s = s + "Current round: " + this.rNumber + "\n";
		for (Round r : this.rounds) {
			s = s + r.toString();
		}
		return s;
	}
}
