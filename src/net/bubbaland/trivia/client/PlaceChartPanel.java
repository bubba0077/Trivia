package net.bubbaland.trivia.client;


import java.awt.GridBagConstraints;
import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaCharts;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * A panel with a chart showing announced place by round.
 * 
 * @author Walter Kolczynski
 * 
 */
public class PlaceChartPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	final private static long	serialVersionUID	= -4937894497657719097L;

	/** Data */
	private int[]				places;

	/** The chart panel */
	private ChartPanel			chartPanel;

	/** The local client */
	final private TriviaClient	client;

	/**
	 * Instantiates a new place chart
	 * 
	 * @param client
	 *            The local trivia client
	 * 
	 */
	public PlaceChartPanel(TriviaClient client) {

		super();

		this.client = client;

		this.places = new int[this.client.getTrivia().getNRounds()];

		this.chartPanel = null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update(boolean force) {

		// Get the current Trivia data object
		final Trivia trivia = this.client.getTrivia();
		
		final int nRounds = trivia.getNRounds();

		// Load the round data and determine if there are any changes
		final int[] newPlaces = new int[nRounds];
		final boolean[] announced = new boolean[nRounds];
		boolean change = false;
		for (int r = 0; r < nRounds; r++) {
			newPlaces[r] = trivia.getAnnouncedPlace(r + 1);
			announced[r] = trivia.isAnnounced(r + 1);
			change = change || ( newPlaces[r] != this.places[r] );
		}
		
		this.places = newPlaces;

		// If there are changes, remake the plot
		if (change || force) {
			JFreeChart chart = TriviaCharts.PlaceChartFactory(trivia);

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




}
