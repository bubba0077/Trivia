package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.text.NumberFormat;

import net.bubbaland.trivia.Trivia;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

/**
 * A panel that displays a stacked bar chart showing the score in each round.
 * 
 * @author Walter Kolczynski
 * 
 */
public class ScoreByRoundChartPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -5408662436975410795L;

	/** Font size */
	final private static float	AXIS_FONT_SIZE		= 16.0f;

	/**
	 * Colors
	 */
	final private static Color	BACKGROUND_COLOR	= Color.BLACK;
	final private static Color	VALUE_COLOR			= new Color(30, 144, 255);
	final private static Color	EARNED_COLOR		= Color.GREEN;

	/** The upper bound of the vertical axis */
	final private static int	MAX_POINTS			= 750;

	/** Data */
	final private int			nRounds;
	private final int[]			values, earneds;

	/** The chart panel. */
	private ChartPanel			chartPanel;

	/** The local client */
	private final TriviaClient	client;

	/**
	 * Instantiates a new score by round chart panel.
	 * 
	 * @param client
	 *            the client application
	 */
	public ScoreByRoundChartPanel(TriviaClient client) {
		super();

		this.client = client;
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
	public synchronized void update(boolean force) {
		// Get the current Trivia data object
		final Trivia trivia = this.client.getTrivia();

		// Get the scores for each round and check if they are updated
		final int[] newValues = new int[this.nRounds];
		final int[] newEarneds = new int[this.nRounds];
		boolean change = false;
		for (int r = 0; r < this.nRounds; r++) {
			newValues[r] = trivia.getValue(r + 1);
			newEarneds[r] = trivia.getEarned(r + 1);
			change = change || ( newValues[r] != this.values[r] ) || ( newEarneds[r] != this.earneds[r] );
		}

		// If the data has changed, remake the chart
		if (change || force) {
			// Create a new dataset
			final DefaultTableXYDataset dataset = new DefaultTableXYDataset();
			// Create series for the earned and possible points
			final XYSeries valueSeries = new XYSeries("Values", true, false);
			final XYSeries earnedSeries = new XYSeries("Earned", true, false);

			for (int r = 0; r < this.nRounds; r++) {
				if (newValues[r] != 0) {
					// If the round has been opened, add the point to the series
					valueSeries.add(r + 1, newValues[r] - newEarneds[r]);
					earnedSeries.add(r + 1, newEarneds[r]);
				}
				this.values[r] = newValues[r];
				this.earneds[r] = newEarneds[r];
			}

			// Add the series to the dataset
			dataset.addSeries(earnedSeries);
			dataset.addSeries(valueSeries);

			// Create a new XYBar renderer and format
			final XYBarRenderer renderer = new StackedXYBarRenderer(0.0);
			renderer.setBarPainter(new StandardXYBarPainter());
			renderer.setDrawBarOutline(false);
			renderer.setShadowVisible(false);
			renderer.setSeriesPaint(0, EARNED_COLOR);
			renderer.setSeriesPaint(1, VALUE_COLOR);
			renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0} Rd {1}: {2}", NumberFormat
					.getIntegerInstance(), NumberFormat.getIntegerInstance()));

			// Create the new bar plot
			final XYPlot plot = new XYPlot(dataset, new NumberAxis("Round"), new NumberAxis("Points"), renderer);

			// Set the background color
			plot.setBackgroundPaint(BACKGROUND_COLOR);

			// Specify the axis parameters
			final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
			final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
			xAxis.setRange(0.5, this.nRounds + 0.5);
			xAxis.setAutoRange(false);
			xAxis.setTickUnit(new NumberTickUnit(5));
			xAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
			xAxis.setLabelFont(xAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
			xAxis.setTickLabelFont(xAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));
			yAxis.setRange(0, MAX_POINTS);
			yAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
			yAxis.setLabelFont(yAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
			yAxis.setTickLabelFont(yAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));

			// create a new chart with the plot
			final JFreeChart chart = new JFreeChart(plot);
			chart.setTitle("Points by Round");

			// If a chart panel already exists, remove it
			if (this.chartPanel != null) {
				this.remove(this.chartPanel);
			}
			// Add new chart panel
			this.chartPanel = new ChartPanel(chart);

			// Add the chart to the panel
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
