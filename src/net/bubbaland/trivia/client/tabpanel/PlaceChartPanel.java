package net.bubbaland.trivia.client.tabpanel;


import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.Properties;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import net.bubbaland.trivia.ScoreEntry;
import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaChartFactory;
import net.bubbaland.trivia.client.TriviaClient;
import net.bubbaland.trivia.client.TriviaFrame;
import net.bubbaland.trivia.client.TriviaMainPanel;

/**
 * A panel with a chart showing announced place by round.
 *
 * @author Walter Kolczynski
 *
 */
public class PlaceChartPanel extends TriviaMainPanel {

	/** The Constant serialVersionUID. */
	final private static long		serialVersionUID	= -4937894497657719097L;

	/** The chart panel */
	private ChartPanel				chartPanel;

	private ArrayList<ScoreEntry[]>	lastStandings;

	/**
	 * Instantiates a new place chart
	 *
	 * @param client
	 *            The local trivia client
	 *
	 */
	public PlaceChartPanel(TriviaClient client, TriviaFrame parent) {
		super(client, parent);
		this.chartPanel = null;
		this.lastStandings = new ArrayList<ScoreEntry[]>();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void updateGUI(boolean force) {
		// Get the current Trivia data object
		final Trivia trivia = this.client.getTrivia();

		// If there are changes, remake the plot
		if (trivia.standingsDifferent(this.lastStandings) || force) {
			this.lastStandings = trivia.getFullStandings();
			final JFreeChart chart = TriviaChartFactory.makePlaceChart(trivia);

			// If there is an old chart panel, remove it
			if (this.chartPanel != null) {
				this.remove(this.chartPanel);
			}

			// Create a new chart panel
			this.chartPanel = new ChartPanel(chart);

			// Add the new chart to the panel
			final GridBagConstraints solo = new GridBagConstraints();
			solo.fill = GridBagConstraints.BOTH;
			solo.anchor = GridBagConstraints.CENTER;
			solo.weightx = 1.0;
			solo.weighty = 1.0;
			solo.gridx = 0;
			solo.gridy = 0;
			this.add(this.chartPanel, solo);

		}

	}

	@Override
	protected void loadProperties(Properties properties) {}


}
