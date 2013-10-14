package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.text.NumberFormat;

import net.bubbaland.trivia.Trivia;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

/**
 * A panel with a stacked XY plot that shows the cumulative score by round.
 * 
 * @author Walter Kolczynski
 * 
 */
public class CumulativePointsChartPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -6171512617495834445L;

	/**
	 * Colors
	 */
	final private static Color	BACKGROUND_COLOR	= Color.BLACK;
	final private static Color	VALUE_COLOR			= new Color(30, 144, 255);
	final private static Color	EARNED_COLOR		= Color.GREEN;

	/** Font size */
	final private static float	AXIS_FONT_SIZE		= 16.0f;

	/** The chart panel */
	private ChartPanel			chartPanel;

	/** Data */
	final private int			nRounds;
	private final int[]			values, earneds;

	/** The client */
	final private TriviaClient	client;

	/**
	 * Instantiates a new chart panel
	 * 
	 * @param client
	 *            The local trivia client
	 * 
	 */
	public CumulativePointsChartPanel(TriviaClient client) {
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

		// Read score data and determine if there have been any changes
		final int[] newValues = new int[this.nRounds];
		final int[] newEarneds = new int[this.nRounds];
		boolean change = false;
		for (int r = 0; r < this.nRounds; r++) {
			newValues[r] = trivia.getValue(r + 1);
			newEarneds[r] = trivia.getEarned(r + 1);
			change = change || ( newValues[r] != this.values[r] ) || ( newEarneds[r] != this.earneds[r] );
		}

		// If there has been a change, remake the chart
		if (change || force) {
			// Create a new dataset
			final DefaultTableXYDataset dataset = new DefaultTableXYDataset();
			// Create a new series for the earned and possible scores
			final XYSeries valueSeries = new XYSeries("Values", true, false);
			final XYSeries earnedSeries = new XYSeries("Earned", true, false);

			int cumulativeValue = 0;
			int cumulativeEarned = 0;
			for (int r = 0; r < this.nRounds; r++) {
				// Calculate the cumulative score for each round
				cumulativeValue = cumulativeValue + newValues[r];
				cumulativeEarned = cumulativeEarned + newEarneds[r];
				if (newValues[r] != 0) {
					// If the round has been opened, add the point to the series
					valueSeries.add(r + 1, cumulativeValue - cumulativeEarned);
					earnedSeries.add(r + 1, cumulativeEarned);
				}
				this.values[r] = newValues[r];
				this.earneds[r] = newEarneds[r];
			}

			// Add the series to the dataset
			dataset.addSeries(earnedSeries);
			dataset.addSeries(valueSeries);

			// Create the Stacked XY plot
			final JFreeChart chart = ChartFactory.createStackedXYAreaChart("Cumulative Score", "Round", "Points",
					dataset, PlotOrientation.VERTICAL, true, true, false);

			// Set the colors of the areas
			final XYItemRenderer renderer = chart.getXYPlot().getRenderer();
			renderer.setSeriesPaint(0, EARNED_COLOR);
			renderer.setSeriesPaint(1, VALUE_COLOR);

			// Set the background color
			final XYPlot plot = chart.getXYPlot();
			plot.setBackgroundPaint(BACKGROUND_COLOR);

			// Set axis properties
			final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
			final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
			xAxis.setRange(0.5, this.nRounds + 0.5);
			xAxis.setAutoRange(false);
			xAxis.setTickUnit(new NumberTickUnit(5));
			xAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
			xAxis.setLabelFont(xAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
			xAxis.setTickLabelFont(xAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));
			yAxis.setLowerBound(0);
			yAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
			yAxis.setLabelFont(yAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
			yAxis.setTickLabelFont(yAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));

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

}
