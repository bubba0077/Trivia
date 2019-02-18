package net.bubbaland.trivia;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Properties;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

public class TriviaChartFactory {

	/** Font Size */
	private static float	axisFontSize;

	/**
	 * Colors
	 */
	private static Color	backgroundColor;
	private static Color	announcedColor;
	private static Color	valueColor;
	private static Color	earnedColor;
	private static Color	labelColor;

	/** The upper bound of the vertical axis */
	private static int		maxPoints;

	static {
		System.setProperty("java.awt.headless", "true");
	}

	public static void loadProperties(Properties properties) {
		/**
		 * Colors
		 */
		backgroundColor = new Color(new BigInteger(properties.getProperty("Chart.BackgroundColor"), 16).intValue());
		announcedColor = new Color(new BigInteger(properties.getProperty("Announced.Color"), 16).intValue());
		valueColor = new Color(new BigInteger(properties.getProperty("Value.Color"), 16).intValue());
		earnedColor = new Color(new BigInteger(properties.getProperty("Earned.Color"), 16).intValue());
		labelColor = new Color(new BigInteger(properties.getProperty("Chart.Label.Color"), 16).intValue());

		maxPoints = Integer.parseInt(properties.getProperty("Chart.ByRound.MaxPoints"));
		axisFontSize = Float.parseFloat(properties.getProperty("Chart.Axis.FontSize"));
	}


	/**
	 * Make a circle.
	 *
	 * @param radius
	 *            the radius
	 * @return the shape
	 */
	public static Shape makeCircle(double radius) {
		return new Ellipse2D.Double(-radius, -radius, 2 * radius, 2 * radius);
	}

	/**
	 * Creates a stacked XY plot of the cumulative score after each round.
	 *
	 * @param trivia
	 *            The trivia data
	 * @return A stacked XY plot of the cumulative score after each round
	 */
	public static JFreeChart makeCumulativePointChart(Trivia trivia) {

		final int nRounds = trivia.getNRounds();

		// Read score data and determine if there have been any changes
		final int[] values = new int[nRounds];
		final int[] earneds = new int[nRounds];
		for (int r = 0; r < nRounds; r++) {
			values[r] = trivia.getRound(r + 1).getValue();
			earneds[r] = trivia.getRound(r + 1).getEarned();
		}

		// Create a new dataset
		final DefaultTableXYDataset dataset = new DefaultTableXYDataset();
		// Create a new series for the earned and possible scores
		final XYSeries valueSeries = new XYSeries("Possible", true, false);
		final XYSeries earnedSeries = new XYSeries("Earned", true, false);

		int cumulativeValue = 0;
		int cumulativeEarned = 0;
		for (int r = 0; r < nRounds; r++) {
			// Calculate the cumulative score for each round
			cumulativeValue = cumulativeValue + values[r];
			cumulativeEarned = cumulativeEarned + earneds[r];
			if (values[r] != 0) {
				// If the round has been opened, add the point to the series
				valueSeries.add(r + 1, cumulativeValue - cumulativeEarned);
				earnedSeries.add(r + 1, cumulativeEarned);
			}
			values[r] = values[r];
			earneds[r] = earneds[r];
		}

		// Add the series to the dataset
		dataset.addSeries(earnedSeries);
		dataset.addSeries(valueSeries);

		// Create the Stacked XY plot
		final JFreeChart chart = ChartFactory.createStackedXYAreaChart("Cumulative Score", "Round", "Points", dataset,
				PlotOrientation.VERTICAL, true, true, false);

		// Set the colors of the areas
		final XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		renderer.setSeriesPaint(0, earnedColor);
		renderer.setSeriesPaint(1, valueColor);

		// Set the background color
		final XYPlot plot = chart.getXYPlot();

		// Set axis properties
		final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		xAxis.setRange(0.5, nRounds + 0.5);
		xAxis.setAutoRange(false);
		xAxis.setTickUnit(new NumberTickUnit(5));
		xAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		xAxis.setLabelFont(xAxis.getLabelFont().deriveFont(axisFontSize));
		xAxis.setTickLabelFont(xAxis.getTickLabelFont().deriveFont(axisFontSize));
		yAxis.setLowerBound(0);
		yAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		yAxis.setLabelFont(yAxis.getLabelFont().deriveFont(axisFontSize));
		yAxis.setTickLabelFont(yAxis.getTickLabelFont().deriveFont(axisFontSize));

		plot.setBackgroundPaint(backgroundColor);
		chart.setBackgroundPaint(backgroundColor);
		chart.getLegend().setBackgroundPaint(backgroundColor);

		plot.setDomainGridlinePaint(labelColor);
		plot.setRangeGridlinePaint(labelColor);
		chart.getTitle().setPaint(labelColor);
		chart.getLegend().setItemPaint(labelColor);
		xAxis.setAxisLinePaint(labelColor);
		xAxis.setTickLabelPaint(labelColor);
		xAxis.setTickMarkPaint(labelColor);
		xAxis.setLabelPaint(labelColor);
		yAxis.setAxisLinePaint(labelColor);
		yAxis.setTickLabelPaint(labelColor);
		yAxis.setTickMarkPaint(labelColor);
		yAxis.setLabelPaint(labelColor);

		return chart;
	}

	/**
	 * Create an XY line chart of the team's place after each round.
	 *
	 * @param trivia
	 *            The trivia data to use
	 * @return An XY line chart of the team's place after each round
	 */
	public static JFreeChart makePlaceChart(Trivia trivia) {

		final int nRounds = trivia.getNRounds();
		final int nTeams = trivia.getNTeams();

		// Load the round data and determine if there are any changes
		final int[] places = new int[nRounds];
		final boolean[] announced = new boolean[nRounds];
		for (int r = 0; r < nRounds; r++) {
			places[r] = trivia.getRound(r + 1).getPlace();
			announced[r] = trivia.getRound(r + 1).isAnnounced();
		}

		// Create a new dataset
		final XYSeriesCollection dataset = new XYSeriesCollection();
		// Create a new series
		final XYSeries series = new XYSeries("Place");

		for (int r = 0; r < nRounds; r++) {
			// If the round has been announced, add a new point to the series
			if (announced[r]) {
				series.add(r + 1, places[r]);
			}
		}
		// Add the series to the dataset
		dataset.addSeries(series);

		// Create the XY plot
		final JFreeChart chart = ChartFactory.createXYLineChart("Place by Round", "Round", "Place", dataset,
				PlotOrientation.VERTICAL, false, true, false);

		// Set the background color
		final XYPlot plot = chart.getXYPlot();

		// Set the line color and thickness, and turn on data points
		final XYLineAndShapeRenderer rend = (XYLineAndShapeRenderer) plot.getRenderer();
		rend.setSeriesShapesVisible(0, true);
		rend.setSeriesShape(0, makeCircle(4));
		rend.setSeriesPaint(0, announcedColor);
		rend.setSeriesStroke(0, new BasicStroke(3.0f));

		// Set axis properties
		final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		xAxis.setRange(0.5, nRounds + 0.5);
		xAxis.setAutoRange(false);
		xAxis.setTickUnit(new NumberTickUnit(5));
		xAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		xAxis.setLabelFont(xAxis.getLabelFont().deriveFont(axisFontSize));
		xAxis.setTickLabelFont(xAxis.getTickLabelFont().deriveFont(axisFontSize));
		yAxis.setInverted(true);
		yAxis.setRange(0.5, nTeams + 0.5);
		yAxis.setAutoRange(false);
		yAxis.setTickUnit(new NumberTickUnit(5));
		yAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		yAxis.setLabelFont(yAxis.getLabelFont().deriveFont(axisFontSize));
		yAxis.setTickLabelFont(yAxis.getTickLabelFont().deriveFont(axisFontSize));

		plot.setBackgroundPaint(backgroundColor);
		chart.setBackgroundPaint(backgroundColor);

		plot.setDomainGridlinePaint(labelColor);
		plot.setRangeGridlinePaint(labelColor);
		chart.getTitle().setPaint(labelColor);
		xAxis.setAxisLinePaint(labelColor);
		xAxis.setTickLabelPaint(labelColor);
		xAxis.setTickMarkPaint(labelColor);
		xAxis.setLabelPaint(labelColor);
		yAxis.setAxisLinePaint(labelColor);
		yAxis.setTickLabelPaint(labelColor);
		yAxis.setTickMarkPaint(labelColor);
		yAxis.setLabelPaint(labelColor);

		return chart;
	}

	/**
	 * Create a stacked bar plot of the team's score in each round.
	 *
	 * @param trivia
	 *            The trivia data
	 * @return A stacked bar plot of the team's score in each round
	 */
	public static JFreeChart makeScoreByRoundChart(Trivia trivia) {

		final int nRounds = trivia.getNRounds();

		// Get the scores for each round and check if they are updated
		final int[] values = new int[nRounds];
		final int[] earneds = new int[nRounds];
		for (int r = 0; r < nRounds; r++) {
			values[r] = trivia.getRound(r + 1).getValue();
			earneds[r] = trivia.getRound(r + 1).getEarned();
		}

		// Create a new dataset
		final DefaultTableXYDataset dataset = new DefaultTableXYDataset();
		// Create series for the earned and possible points
		final XYSeries valueSeries = new XYSeries("Possible", true, false);
		final XYSeries earnedSeries = new XYSeries("Earned", true, false);

		for (int r = 0; r < nRounds; r++) {
			if (values[r] != 0) {
				// If the round has been opened, add the point to the series
				valueSeries.add(r + 1, values[r] - earneds[r]);
				earnedSeries.add(r + 1, earneds[r]);
			}
		}
		// Add the series to the dataset
		dataset.addSeries(earnedSeries);
		dataset.addSeries(valueSeries);

		// create a new chart with the plot
		final JFreeChart chart = ChartFactory.createStackedXYAreaChart("Points by Round", "Round", "Points", dataset);

		// Create a new XYBar renderer to override the normal one
		final XYBarRenderer renderer = new StackedXYBarRenderer(0.0);
		renderer.setBarPainter(new StandardXYBarPainter());
		renderer.setDrawBarOutline(false);
		renderer.setShadowVisible(false);
		renderer.setSeriesPaint(0, earnedColor);
		renderer.setSeriesPaint(1, valueColor);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0} Rd {1}: {2}",
				NumberFormat.getIntegerInstance(), NumberFormat.getIntegerInstance()));

		// Replace the renderer
		final XYPlot plot = chart.getXYPlot();
		plot.setRenderer(renderer);

		// Specify the axis parameters
		final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		xAxis.setRange(0.5, nRounds + 0.5);
		xAxis.setAutoRange(false);
		xAxis.setTickUnit(new NumberTickUnit(5));
		xAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		xAxis.setLabelFont(xAxis.getLabelFont().deriveFont(axisFontSize));
		xAxis.setTickLabelFont(xAxis.getTickLabelFont().deriveFont(axisFontSize));
		yAxis.setRange(0, maxPoints);
		yAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		yAxis.setLabelFont(yAxis.getLabelFont().deriveFont(axisFontSize));
		yAxis.setTickLabelFont(yAxis.getTickLabelFont().deriveFont(axisFontSize));

		plot.setBackgroundPaint(backgroundColor);
		chart.setBackgroundPaint(backgroundColor);
		chart.getLegend().setBackgroundPaint(backgroundColor);

		plot.setDomainGridlinePaint(labelColor);
		plot.setRangeGridlinePaint(labelColor);
		chart.getTitle().setPaint(labelColor);
		chart.getLegend().setItemPaint(labelColor);
		xAxis.setAxisLinePaint(labelColor);
		xAxis.setTickLabelPaint(labelColor);
		xAxis.setTickMarkPaint(labelColor);
		xAxis.setLabelPaint(labelColor);
		yAxis.setAxisLinePaint(labelColor);
		yAxis.setTickLabelPaint(labelColor);
		yAxis.setTickMarkPaint(labelColor);
		yAxis.setLabelPaint(labelColor);

		return chart;

	}

	/**
	 * Create an XY plot comparing each team's score in each round relative to ours.
	 *
	 * @param trivia
	 *            The trivia data
	 * @return An XY plot comparing team scores
	 */
	public static JFreeChart makeTeamComparisonChart(Trivia trivia) {

		final int nRounds = trivia.getNRounds();

		int lastAnnounced = 0;
		final ArrayList<ScoreEntry[]> scores = new ArrayList<ScoreEntry[]>(0);

		for (int r = 1; r <= trivia.getNRounds(); r++) {
			if (trivia.getRound(r).isAnnounced()) {
				lastAnnounced = r;
				final ScoreEntry roundStandings[] = ScoreEntry.alphabetize(trivia.getRound(r).getStandings());
				scores.add(roundStandings);
			} else {
				scores.add(null);
			}
		}

		// If no rounds have been announced, don't make a plot
		if (scores.get(0) == null) return null;
		final int nTeams = scores.get(0).length;

		// Create a new dataset
		final YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
		// Create a new XY chart
		final JFreeChart chart = ChartFactory.createXYLineChart("Team Comparison", "Round", "Point Differential",
				dataset, PlotOrientation.VERTICAL, false, true, false);
		// Get the plot renderer
		final DeviationRenderer renderer = new DeviationRenderer(true, false);
		chart.getXYPlot().setRenderer(renderer);
		int minY = 0, maxY = 0;
		for (int t = 0; t < nTeams; t++) {
			final String team = scores.get(0)[t].getTeamName();
			final YIntervalSeries series = new YIntervalSeries(team, true, false);
			// Add a data point for each team with the difference between their score and ours
			if (team.equalsIgnoreCase(trivia.getTeamName())) {
				for (int r = 0; r < lastAnnounced; r++) {
					if (scores.get(r) == null) {
						continue;
					}
					final int ourScore = trivia.getRound(r + 1).getAnnouncedPoints();
					minY = -ourScore;
					maxY = trivia.getCumulativeValue(r + 1) - ourScore;
					series.add(r + 1, 0, minY, maxY);
				}
				renderer.setSeriesStroke(t, new BasicStroke(3F));
				renderer.setSeriesPaint(t, Color.WHITE);
				renderer.setSeriesFillPaint(t, Color.GRAY);
			} else {
				for (int r = 0; r < lastAnnounced; r++) {
					if (scores.get(r) == null || scores.get(r).length == 0) {
						continue;
					}
					final int scoreDiff = scores.get(r)[t].getScore() - trivia.getRound(r + 1).getAnnouncedPoints();
					series.add(r + 1, scoreDiff, scoreDiff, scoreDiff);
				}
			}

			// Plot the data points
			renderer.setSeriesShapesVisible(t, true);
			renderer.setSeriesShape(t, makeCircle(2D));

			// Add the series to the dataset
			dataset.addSeries(series);
		}

		// Change the format of the tooltip
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0} Rd {1}: {2}",
				NumberFormat.getIntegerInstance(), NumberFormat.getIntegerInstance()));
		// Set the background color and axes
		final XYPlot plot = chart.getXYPlot();

		final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		xAxis.setRange(0.5D, nRounds + 0.5D);
		xAxis.setAutoRange(false);
		xAxis.setTickUnit(new NumberTickUnit(5D));
		xAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		xAxis.setLabelFont(xAxis.getLabelFont().deriveFont(axisFontSize));
		xAxis.setTickLabelFont(xAxis.getTickLabelFont().deriveFont(axisFontSize));
		final DecimalFormat format = new DecimalFormat();
		format.setPositivePrefix("+");
		yAxis.setNumberFormatOverride(format);
		yAxis.setLabelFont(yAxis.getLabelFont().deriveFont(axisFontSize));
		yAxis.setTickLabelFont(yAxis.getTickLabelFont().deriveFont(axisFontSize));
		xAxis.setRange(0.5D, nRounds + 0.5D);
		xAxis.setAutoRange(false);
		yAxis.setRangeWithMargins(minY, maxY);
		yAxis.setAutoRange(false);
		plot.setBackgroundPaint(backgroundColor);
		chart.setBackgroundPaint(backgroundColor);

		plot.setDomainGridlinePaint(labelColor);
		plot.setRangeGridlinePaint(labelColor);
		chart.getTitle().setPaint(labelColor);
		xAxis.setAxisLinePaint(labelColor);
		xAxis.setTickLabelPaint(labelColor);
		xAxis.setTickMarkPaint(labelColor);
		xAxis.setLabelPaint(labelColor);
		yAxis.setAxisLinePaint(labelColor);
		yAxis.setTickLabelPaint(labelColor);
		yAxis.setTickMarkPaint(labelColor);
		yAxis.setLabelPaint(labelColor);


		return chart;

	}

}
