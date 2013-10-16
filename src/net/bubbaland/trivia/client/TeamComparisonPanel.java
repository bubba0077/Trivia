package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;

import net.bubbaland.trivia.ScoreEntry;
import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaCharts;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * A panel with a chart comparing all of the teams' scores.
 * 
 * @author Walter Kolczynski
 * 
 */

public class TeamComparisonPanel extends TriviaPanel {

	private static final long		serialVersionUID	= 0xaa5a61f8f351d0b3L;
	private TriviaClient			client;
	private int						lastAnnounced;
	private ChartPanel				chartPanel;
	private ArrayList<ScoreEntry[]>	scores;

	public TeamComparisonPanel(TriviaClient client) {
		super();
		this.client = client;
		this.scores = new ArrayList<ScoreEntry[]>(0);
		this.lastAnnounced = 0;
		this.chartPanel = null;
	}

	@Override
	public synchronized void update(boolean force) {
		final Trivia trivia = this.client.getTrivia();
		boolean change;
		for (change = false; trivia.isAnnounced(this.lastAnnounced + 1); change = true) {
			this.lastAnnounced++;
			final ScoreEntry roundStandings[] = trivia.getStandings(this.lastAnnounced);
			Arrays.sort(roundStandings);
			this.scores.add(roundStandings);
		}

		if (change) {
			JFreeChart chart = TriviaCharts.TeamComparisonChartFactory(trivia);
			
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

	public static Shape makeCircle(double radius) {
		return new Ellipse2D.Double(-radius, -radius, 2 * radius, 2 * radius);
	}

}
