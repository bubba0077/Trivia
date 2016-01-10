package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import java.util.Properties;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaChartFactory;

/**
 * A panel with a chart comparing all of the teams' scores.
 *
 * @author Walter Kolczynski
 *
 */

public class TeamComparisonPanel extends TriviaMainPanel {

	private static final long	serialVersionUID	= 0xaa5a61f8f351d0b3L;

	/** Data */
	private int					lastAnnounced;
	private ChartPanel			chartPanel;

	public TeamComparisonPanel(TriviaClient client, TriviaFrame parent) {
		super(client, parent);
		this.lastAnnounced = 0;
		this.chartPanel = null;
	}

	@Override
	public synchronized void updateGUI(boolean force) {
		final Trivia trivia = this.client.getTrivia();
		boolean change;
		for (change = false; trivia.isAnnounced(this.lastAnnounced + 1); change = true) {
			this.lastAnnounced++;
		}

		if (change) {
			// Make a new team comparison chart
			final JFreeChart chart = TriviaChartFactory.makeTeamComparisonChart(trivia);

			// Replace the existing chart, if there is one
			if (this.chartPanel != null) {
				this.remove(this.chartPanel);
			}
			this.chartPanel = new ChartPanel(chart);

			final GridBagConstraints solo = new GridBagConstraints();
			solo.fill = 1;
			solo.anchor = 10;
			solo.weightx = 1.0D;
			solo.weighty = 1.0D;
			solo.gridx = 0;
			solo.gridy = 0;
			this.add(this.chartPanel, solo);
		}
	}

	@Override
	protected void loadProperties(Properties properties) {
	}

}
