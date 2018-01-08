package net.bubbaland.trivia.client.tabpanel;


import java.awt.GridBagConstraints;
import java.util.Properties;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

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
	final private static long	serialVersionUID	= -4937894497657719097L;

	/** Data */
	private int[]				places;

	/** The chart panel */
	private ChartPanel			chartPanel;

	/**
	 * Instantiates a new place chart
	 *
	 * @param client
	 *            The local trivia client
	 *
	 */
	public PlaceChartPanel(TriviaClient client, TriviaFrame parent) {

		super(client, parent);

		this.places = new int[this.client.getTrivia().getNRounds()];

		this.chartPanel = null;

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

		final int nRounds = trivia.getNRounds();

		// Load the round data and determine if there are any changes
		final int[] newPlaces = new int[nRounds];
		boolean change = false;
		for (int r = 0; r < nRounds; r++) {
			newPlaces[r] = trivia.getRound(r + 1).getPlace();
			change = change || ( newPlaces[r] != this.places[r] );
		}

		this.places = newPlaces;

		// If there are changes, remake the plot
		if (change || force) {
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