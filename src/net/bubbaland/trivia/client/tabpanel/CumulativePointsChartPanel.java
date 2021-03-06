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
 * A panel with a stacked XY plot that shows the cumulative score by round.
 *
 * @author Walter Kolczynski
 *
 */
public class CumulativePointsChartPanel extends TriviaMainPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -6171512617495834445L;

	/** The chart panel */
	private ChartPanel			chartPanel;

	/** Data */
	final private int			nRounds;
	private int[]				values, earneds;

	/**
	 * Instantiates a new chart panel
	 *
	 * @param client
	 *            The local trivia client
	 *
	 */
	public CumulativePointsChartPanel(TriviaClient client, TriviaFrame frame) {
		super(client, frame);

		this.nRounds = client.getTrivia().getNRounds();

		this.values = new int[this.nRounds];
		this.earneds = new int[this.nRounds];

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

		// Read score data and determine if there have been any changes
		final int[] newValues = new int[this.nRounds];
		final int[] newEarneds = new int[this.nRounds];
		boolean change = false;
		for (int r = 0; r < this.nRounds; r++) {
			newValues[r] = trivia.getRound(r + 1).getValue();
			newEarneds[r] = trivia.getRound(r + 1).getEarned();
			change = change || ( newValues[r] != this.values[r] ) || ( newEarneds[r] != this.earneds[r] );
		}

		this.values = newValues;
		this.earneds = newEarneds;

		// If there has been a change, remake the chart
		if (change || force) {
			// Create the Stacked XY plot
			final JFreeChart chart = TriviaChartFactory.makeCumulativePointChart(trivia);

			// Remove the old chart if it exists
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
